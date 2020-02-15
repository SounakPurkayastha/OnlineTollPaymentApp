package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker;
import com.treebo.internetavailabilitychecker.InternetConnectivityListener;

import org.json.JSONArray;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    static boolean notificationShown = false;
    FusedLocationProviderClient fusedLocationProviderClient;
    static Location currentLocation;
    boolean permissionAsked = false;
    private static float zoom;
    private static boolean mapSet = false;
    SharedPreferences sharedPreferences;
    static ArrayList<TollBooth> tollBooths;
    static JSONArray jsonArray;


    final NotificationCompat.Builder builder = new NotificationCompat.Builder(MapsActivity.this)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Toll booth ahead")
            .setContentText("Please pay the toll")
            .setAutoCancel(true);

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = this.getSharedPreferences("com.example.myapplication",MODE_PRIVATE);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        /*InternetAvailabilityChecker.init(this);
        mInternetAvailabilityChecker = InternetAvailabilityChecker.getInstance();
        mInternetAvailabilityChecker.addInternetConnectivityListener(this);*/
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(!service.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if(currentLocation != null) {
                LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
            }
            //Dialog dialog = new Dialog();
            //dialog.show(getSupportFragmentManager(),"location");
            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addApi(LocationServices.API).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(10000 / 2);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            Log.i("fdfdfg", "All location settings are satisfied.");
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            Log.i("fdfdfg", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                            try {
                                // Show the dialog by calling startResolutionForResult(), and check the result
                                // in onActivityResult().
                                status.startResolutionForResult(MapsActivity.this, 0x1);
                            } catch (IntentSender.SendIntentException e) {
                                Log.i("fdfdfg", "PendingIntent unable to execute request.");
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            Log.i("fdfdfg", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                            break;
                    }
                }
            });
        }
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLng(userLocation));
                currentLocation = location;
                if(HomeActivity.tollBooths.size() != 0) {
                    for (TollBooth tollBooth : HomeActivity.tollBooths) {
                        Location.distanceBetween(tollBooth.getLatLng().latitude, tollBooth.getLatLng().longitude, userLocation.latitude, userLocation.longitude, HomeActivity.results);
                        if (HomeActivity.results[0] < 10000 && !notificationShown && !sharedPreferences.getBoolean("paymentComplete", false)) {
                            MapsActivity.this.notify(builder);
                            HomeActivity.nearestTollBooth = tollBooth;
                            HomeActivity.notificationShown = true;
                        }
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
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
            fetchLastLocation();
        }

        if(HomeActivity.tollBooths.size() != 0){
            for(TollBooth tollBooth : MapsActivity.tollBooths) {
                mMap.addMarker(new MarkerOptions().position(tollBooth.getLatLng()).title(tollBooth.getName()));
            }
        }

    }

    private void fetchLastLocation() {
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null) {
                    currentLocation = location;
                    mMap.setMyLocationEnabled(true);
                    LatLng latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
                }
                if(location == null)
                    fetchLastLocation();
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(MapsActivity.this, HomeActivity.class));
        zoom = mMap.getCameraPosition().zoom;
        mapSet = true;
    }

    public void notify(NotificationCompat.Builder builder) {
        Intent intent = new Intent(MapsActivity.this, LoadingActivity.class).putExtra("Activity",4);
        PendingIntent pendingIntent = PendingIntent.getActivity(MapsActivity.this,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,builder.build());
        notificationShown = true;
    }

}
