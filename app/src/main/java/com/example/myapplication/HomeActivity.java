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
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker;
import com.treebo.internetavailabilitychecker.InternetConnectivityListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class HomeActivity extends AppCompatActivity implements InternetConnectivityListener {

    ListView listView;
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

    //boolean vehiclesLoaded, qrcodeLoaded, boothsLoaded;
    static String qrcode;
    static ArrayList<Model> vehicles;
    JsonObjectRequest request;
    RequestQueue requestQueue;
    static JSONArray jsonArray;

    static DatabaseReference reference2;

    ArrayList<Integer> images = new ArrayList<>(Arrays.asList(R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher));
    long backPressedTime;
    LocationManager locationManager;
    LocationListener locationListener;
    static ArrayList<TollBooth> tollBooths = new ArrayList<>();
    static float[] results;
    static boolean notificationShown = false;
    boolean permissionAsked = false;
    FusedLocationProviderClient fusedLocationProviderClient;
    static TollBooth nearestTollBooth = null;
    InternetAvailabilityChecker mInternetAvailabilityChecker;
    //SharedPreferences sharedPreferences;
    //static JsonObjectRequest request;
    static boolean tollBoothsLoaded = false;
    JSONObject object;
    static String userId;
    static DatabaseReference reference;
    static boolean paymentComplete = false;
    LVAdapter adapter;

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
        //reference = FirebaseDatabase.getInstance().getReference().child(userId).child("QRCode");
        //requestQueue = Volley.newRequestQueue(HomeActivity.this);
        if(Flag.firstLoad) {
            //Toast.makeText(HomeActivity.this,"Checking for QR codes",Toast.LENGTH_SHORT).show();
            Flag.firstLoad = false;
            InternetAvailabilityChecker.init(this);
            mInternetAvailabilityChecker = InternetAvailabilityChecker.getInstance();
            mInternetAvailabilityChecker.addInternetConnectivityListener(this);
        }
        vehicles = new ArrayList<>();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference().child(userId).child("QRCode");
        reference2 = FirebaseDatabase.getInstance().getReference().child(userId).child("Vehicle");
        //initialConnection = false;
        //sharedPreferences = getSharedPreferences("com.example.myapplication",Context.MODE_PRIVATE);
        //sharedPreferences.edit().putBoolean("paymentComplete",true).apply();

        /*if(sharedPreferences.getBoolean("paymentComplete",false)) {
            titles.add("QR Code Receipts");
            descriptions.add("QR codes for payments made");
            images.add(R.mipmap.ic_launcher);
        }*/


        listView = findViewById(R.id.list_view);
        adapter = new LVAdapter(this,titles,descriptions,images);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (i == 0) {
                    if (!checkConnection())
                        Toast.makeText(HomeActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    //else if(!vehiclesLoaded)
                    //    Toast.makeText(HomeActivity.this,"Fetching vehicles, please wait",Toast.LENGTH_SHORT).show();
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
                    LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
                    if (!service.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Toast.makeText(HomeActivity.this, "Please enable Location", Toast.LENGTH_SHORT).show();
                    }
                    //else if(!tollBoothsLoaded)
                    //    Toast.makeText(HomeActivity.this,"Fetching toll booths, please wait",Toast.LENGTH_SHORT).show();
                    else {
                        startActivity(new Intent(HomeActivity.this, LoadingActivity.class).putExtra("Activity",2));
                    }
                }
                if (i == 3) {
                    //if(!qrcodeLoaded)
                    //    Toast.makeText(HomeActivity.this,"Fetching QR code, please wait",Toast.LENGTH_SHORT).show();
                    //else
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
                        //notificationLocation = userLocation;
                        notificationShown = true;
                    }
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
            //fetchLastLocation(builder);
        }

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
                            Toast.makeText(HomeActivity.this,"Internet connection not found",Toast.LENGTH_SHORT).show();
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
        requestQueue.add(request);
        //new Thread2().run();
        //new Thread3().run();

    }

    /*private void fetchLastLocation(final NotificationCompat.Builder builder) {
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null) {
                    currentLocation = location;
                }
                if(location == null)
                    fetchLastLocation(builder);
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
        Intent intent = new Intent(HomeActivity.this, ChooseVehicleActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(HomeActivity.this,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,builder.build());
        notificationShown = true;
        MapsActivity.notificationShown = true;
    }

    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        /*if(isConnected && !tollBoothsLoaded){
            tollBoothsLoaded = true;
            //requestQueue.add(request);
        }
        else if (!isConnected){
            Toast.makeText(HomeActivity.this,"No internet connection",Toast.LENGTH_SHORT).show();
        }*/
    }

    /*class Thread1 implements Runnable {

        @Override
        public void run() {
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!(dataSnapshot.getChildrenCount() == 0)) {
                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                            qrcode = dataSnapshot1.getValue(String.class);
                        }
                    }
                    qrcodeLoaded = true;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }*/

    /*class Thread3 implements Runnable {
        @Override
        public void run() {
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
                                Toast.makeText(HomeActivity.this,"Internet connection not found",Toast.LENGTH_SHORT).show();
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
            boothsLoaded = true;
        }
    }*/

}
