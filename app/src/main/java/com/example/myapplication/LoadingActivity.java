package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker;
import com.treebo.internetavailabilitychecker.InternetConnectivityListener;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoadingActivity extends AppCompatActivity implements InternetConnectivityListener {

    JsonObjectRequest request;
    RequestQueue requestQueue;
    DatabaseReference reference;
    JsonPlaceHolderApi jsonPlaceHolderApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        int i = getIntent().getExtras().getInt("Activity");
        if(i == 3) {
            HomeActivity.reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!(dataSnapshot.getChildrenCount() == 0)) {
                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                            HomeActivity.qrcode = dataSnapshot1.getValue(String.class);
                        }
                        startActivity(new Intent(LoadingActivity.this,ResponseActivity.class));
                    }
                    else {
                        startActivity(new Intent(LoadingActivity.this,NoCodeActivity.class));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        if(i == 0) {
            HomeActivity.reference2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        HomeActivity.vehicles.add(dataSnapshot1.getValue(Model.class));
                    }
                    startActivity(new Intent(LoadingActivity.this,RegisteredVehiclesActivity.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        if(i == 2) {
            String url = "https://sounakpurkayastha.github.io/OnlineTollPaymentApp/tollbooths.json";
            request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        HomeActivity.jsonArray = response.getJSONArray("tollBooths");
                        double latitude;
                        double longitude;
                        String name;
                        for(int i = 0; i < HomeActivity.jsonArray.length(); i++) {
                            try {
                                JSONObject object = HomeActivity.jsonArray.getJSONObject(i);
                                latitude = object.getDouble("latitude");
                                longitude = object.getDouble("longitude");
                                name = object.getString("name");
                                HomeActivity.tollBooths.add(new TollBooth(new LatLng(latitude,longitude),name));
                            } catch (JSONException e) {
                                Toast.makeText(LoadingActivity.this,"Internet connection not found",Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                        HomeActivity.results = new float[HomeActivity.tollBooths.size()];
                        startActivity(new Intent(LoadingActivity.this,MapsActivity.class));
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
            requestQueue = Volley.newRequestQueue(LoadingActivity.this);
            requestQueue.add(request);
        }
        if(i == 1) {
            Retrofit retrofit = new Retrofit.Builder().baseUrl("http://ec2-18-224-227-221.us-east-2.compute.amazonaws.com")
                    .addConverterFactory(GsonConverterFactory.create()).build();
            jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
            reference = FirebaseDatabase.getInstance().getReference().child(HomeActivity.userId).child("QRCode");
            getQRCode();
        }
    }

    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        if(isConnected){
            //requestQueue = Volley.newRequestQueue(LoadingActivity.this);
            requestQueue.add(request);
        }
        else if (!isConnected){
            Toast.makeText(LoadingActivity.this,"No internet connection",Toast.LENGTH_SHORT).show();
        }
    }

    private void getQRCode() {
        Call<Post> call = jsonPlaceHolderApi.getQRCode(ChooseVehicleActivity.vehicleId);
        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, retrofit2.Response<Post> response) {
                if(!response.isSuccessful()) {
                    return;
                }
                HomeActivity.qrcode = response.body().getQrCode();
                reference.child(reference.push().getKey()).setValue(HomeActivity.qrcode);
                startActivity(new Intent(LoadingActivity.this,ResponseActivity.class));
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {

            }
        });
    }
}
