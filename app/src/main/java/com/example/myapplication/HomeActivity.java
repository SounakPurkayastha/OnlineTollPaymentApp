package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker;
import com.treebo.internetavailabilitychecker.InternetConnectivityListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity implements InternetConnectivityListener {

    ArrayList<String> titles = new ArrayList<>(Arrays.asList("Registered Vehicles",
            "Payment History",
            "Show Map",
            "QR Code Receipt",
            "Logout"));
    ArrayList<String> descriptions = new ArrayList<>(Arrays.asList("List of all registered vehicles",
            "Details of all payments made so far",
            "Map showing your current location",
            "QR code for payment made",
            "Log out of app"));
    ArrayList<Integer> images = new ArrayList<>(Arrays.asList(R.mipmap.ic_launcher,
                                                              R.mipmap.ic_launcher,
                                                              R.mipmap.ic_launcher,
                                                              R.mipmap.ic_launcher,
                                                              R.mipmap.ic_launcher));
    static String token;
    ListView listView;
    float dist;
    static ArrayList<Model> vehicles;
    static JsonObjectRequest request;
    static RequestQueue requestQueue;
    static JSONArray jsonArray;
    static DatabaseReference reference2;
    long backPressedTime;
    LocationManager locationManager;
    LocationListener locationListener;
    static ArrayList<TollBooth> tollBooths;
    static float[] results;
    static boolean notificationShown = false;
    boolean permissionAsked = false;
    FusedLocationProviderClient fusedLocationProviderClient;
    static TollBooth nearestTollBooth = null;
    InternetAvailabilityChecker mInternetAvailabilityChecker;
    static boolean tollBoothsLoaded = false;
    JSONObject object;
    static String userId;
    static DatabaseReference reference;
    LVAdapter adapter;
    static String qrcode;
    JsonPlaceHolderApi jsonPlaceHolderApi;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    permissionAsked = true;
                }
            }
        }
    }

    private boolean checkConnection() {
        ConnectivityManager manager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        token = FirebaseInstanceId.getInstance().getToken();
        //Log.i("Token:",token);
        if(Flag.firstLoad) {
            //Toast.makeText(HomeActivity.this,"First load",Toast.LENGTH_SHORT).show();
            Flag.firstLoad = false;
            InternetAvailabilityChecker.init(this);
            mInternetAvailabilityChecker = InternetAvailabilityChecker.getInstance();
            mInternetAvailabilityChecker.addInternetConnectivityListener(this);
            tollBooths = new ArrayList<>();
            vehicles = new ArrayList<>();
            String url = "https://sounakpurkayastha.github.io/OnlineTollPaymentApp/tollbooths.json";
            request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        jsonArray = response.getJSONArray("tollBooths");
                        double latitude;
                        double longitude;
                        String name;
                        for(int i = 0; i < jsonArray.length(); i++) {
                            try {
                                object = jsonArray.getJSONObject(i);
                                latitude = object.getDouble("latitude");
                                longitude = object.getDouble("longitude");
                                name = object.getString("name");
                                tollBooths.add(new TollBooth(new LatLng(latitude,longitude),name));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        results = new float[tollBooths.size()];
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
            requestQueue = Volley.newRequestQueue(HomeActivity.this);
            if(checkConnection())
                requestQueue.add(request);
        }
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference().child(userId).child("QRCode");
        reference2 = FirebaseDatabase.getInstance().getReference().child(userId).child("Vehicle");
        listView = findViewById(R.id.list_view);
        adapter = new LVAdapter(this,titles,descriptions,images);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    if (!checkConnection())
                        Toast.makeText(HomeActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    else
                        startActivity(new Intent(HomeActivity.this, LoadingActivity.class).putExtra("Activity",0));
                }
                if (i == 1) {
                    if (!checkConnection())
                        Toast.makeText(HomeActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(HomeActivity.this, "Payment History", Toast.LENGTH_SHORT).show();
                }
                if (i == 2) {
                    if(!checkConnection())
                        startActivity(new Intent(HomeActivity.this, MapsActivity.class));
                    else
                        startActivity(new Intent(HomeActivity.this, LoadingActivity.class).putExtra("Activity",2));
                }
                if (i == 3) {
                    startActivity(new Intent(HomeActivity.this, LoadingActivity.class).putExtra("Activity",3));
                }
                if (i == 4) {
                    if (!checkConnection())
                        Toast.makeText(HomeActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    else {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(HomeActivity.this, MainActivity.class));
                    }
                }
            }

        });
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(HomeActivity.this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Toll Booth Ahead")
                .setContentText("Please pay the toll")
                .setAutoCancel(true);
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                for(TollBooth tollBooth : tollBooths) {
                    Location.distanceBetween(tollBooth.getLatLng().latitude,tollBooth.getLatLng().longitude,userLocation.latitude,userLocation.longitude,results);
                    if(results[0] < 10000 && !notificationShown) {
                        HomeActivity.this.notify(builder);
                        nearestTollBooth = tollBooth;
                        notificationShown = true;
                    }
                    //dist = results[0];
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        else {
            if(!permissionAsked) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(HomeActivity.this);
        }

        /*Retrofit retrofit = new Retrofit.Builder().baseUrl("http://ec2-18-224-227-221.us-east-2.compute.amazonaws.com")
                .addConverterFactory(GsonConverterFactory.create()).build();
        jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
        reference = FirebaseDatabase.getInstance().getReference().child(HomeActivity.userId).child("QRCode");
        getQRCode();*/

    }

    /*private void getQRCode() {
        Call<Post> call = jsonPlaceHolderApi.getQRCode(URLEncoder.encode(HomeActivity.token));
        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, retrofit2.Response<Post> response) {
                if(!response.isSuccessful()) {
                    return;
                }
                //Log.i("qrcode:","ok1");
                HomeActivity.qrcode = response.body().getQrCode();
                //Log.i("qrcode:","ok2");
                reference.child(reference.push().getKey()).setValue(HomeActivity.qrcode);
                //Log.i("qrcode:","ok3");
                startActivity(new Intent(HomeActivity.this,ResponseActivity.class));
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Log.i("qrcode:",t.getMessage());
            }
        });
    }*/

    @Override
    public void onBackPressed() {
        if (backPressedTime + 1000 > System.currentTimeMillis()) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }
        else {
            Toast.makeText(HomeActivity.this,"Press back again to exit",Toast.LENGTH_SHORT).show();
        }

        backPressedTime = System.currentTimeMillis();
    }

    public void notify(NotificationCompat.Builder builder) {
        Intent intent = new Intent(HomeActivity.this, LoadingActivity.class).putExtra("Activity",4);
        PendingIntent pendingIntent = PendingIntent.getActivity(HomeActivity.this,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,builder.build());
        notificationShown = true;
        MapsActivity.notificationShown = true;
    }

    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        //Log.i("Activity:",this.getClass().getSimpleName());
        if(isConnected && !tollBoothsLoaded){
            tollBoothsLoaded = true;
            requestQueue.add(request);
        }
        else if (!isConnected){
            Toast.makeText(HomeActivity.this,"No internet connection",Toast.LENGTH_SHORT).show();
        }
    }

}
