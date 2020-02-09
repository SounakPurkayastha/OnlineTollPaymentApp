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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker;
import com.treebo.internetavailabilitychecker.InternetConnectivityListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    //LatLng[] tollBooths = {new LatLng(22.935130, 69.802574)};
    //float[] results = new float[tollBooths.length];
    static boolean notificationShown = false;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;
    boolean permissionAsked = false;
    private static float zoom;
    private static boolean mapSet = false;
    SharedPreferences sharedPreferences;
    InternetAvailabilityChecker mInternetAvailabilityChecker;

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
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLng(userLocation));
                for (TollBooth tollBooth : HomeActivity.tollBooths) {
                    Location.distanceBetween(tollBooth.getLatLng().latitude, tollBooth.getLatLng().longitude, userLocation.latitude, userLocation.longitude, HomeActivity.results);
                    if (HomeActivity.results[0] < 10000 && !notificationShown && !sharedPreferences.getBoolean("paymentComplete",false)) {
                        MapsActivity.this.notify(builder);
                        HomeActivity.nearestTollBooth = tollBooth;
                        HomeActivity.notificationShown = true;
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
                Toast.makeText(MapsActivity.this, "Please enable Location", Toast.LENGTH_SHORT).show();
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

        for(TollBooth tollBooth : HomeActivity.tollBooths) {
            mMap.addMarker(new MarkerOptions().position(tollBooth.getLatLng()).title(tollBooth.getName()));
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
                    //mMap.addMarker(new MarkerOptions().position(latLng).title("You are here"));
                    if(!mapSet)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));
                    else
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
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
