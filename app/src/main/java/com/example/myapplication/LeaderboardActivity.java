package com.example.myapplication;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class LeaderboardActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap map;
    private Button backBtn;
    private SharedPreferences sharedPreferences;
    FusedLocationProviderClient client;
    protected SupportMapFragment supportMapFragment;
    Double lat, lon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUIVisibility();
        setContentView(R.layout.activity_leaderboard);

        sharedPreferences = getSharedPreferences("shared_preferences", MODE_PRIVATE);

        client = LocationServices.getFusedLocationProviderClient(this);

        backBtn = findViewById(R.id.backButton);
        backBtn.setOnClickListener(view -> finish());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        // ask for user permission to use GPS data
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
            }

            // get location by longitude and latitude
            Task<Location> task = client.getLastLocation();
            task.addOnSuccessListener(location -> {
                if (location != null) {
                    supportMapFragment.getMapAsync(gMap -> {
                        lat = location.getLatitude();
                        lon = location.getLongitude();
                        LatLng latLng = new LatLng(lat, lon);

                        // after having location params - display current location on map
                        MarkerOptions options = new MarkerOptions().position(latLng).title("I am there");
                        Log.d("MAP:", "getLatitude   " + lat);
                        Log.d("MAP:", "getLongitude  " + lon);

                        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

                        gMap.addMarker(options);
                    });
                }
                Log.d("MAP:", "Location is null  ");
            });

        }
        // if we already have a permission - get location by longitude and latitude
        else {
            Task<Location> task = client.getLastLocation();
            task.addOnSuccessListener(location -> {
                if (location != null) {
                    supportMapFragment.getMapAsync(gMap -> {
                        lat = location.getLatitude();
                        lon = location.getLongitude();
                        LatLng latLng = new LatLng(lat, lon);

                        MarkerOptions options = new MarkerOptions().position(latLng).title("I am there");
                        Log.d("MAP:", "getLatitude   " + lat);
                        Log.d("MAP:", "getLongitude  " + lon);

                        // after having location params - display current location on map
                        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

                        gMap.addMarker(options);
                    });
                }
                Log.d("MAP:", "Location is null");
            });
        }
        loadList();
    }

    public void loadList() {
        String jsonString = sharedPreferences.getString("list", null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Competitor>>() {
        }.getType();
        ArrayList<Competitor> listFromGson;
        listFromGson = gson.fromJson(jsonString, type);
        if (listFromGson == null) {
            listFromGson = new ArrayList<>();
        }
        Collections.sort(listFromGson);
        for (int i = 0; i < listFromGson.size(); i++) {
            if (listFromGson.get(i).getLatitude() != 0.0 && listFromGson.get(i).getLongitude() != 0.0) {
                Objects.requireNonNull(map.addMarker(new MarkerOptions().position(new LatLng(listFromGson.get(i).getLatitude(), listFromGson.get(i).getLongitude())))).setTitle((i + 1) + ". " + listFromGson.get(i).getName());

            }
        }
    }

    // Method that set UI flags.
    public void setUIVisibility() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUIVisibility();
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        // PERMISSION GRANTED
                        Log.d("Permissions:", "Permissions Granted");
                    } else {
                        // PERMISSION NOT GRANTED
                        Log.d("Permissions:", "Permissions Denied");
                    }
                }
            }
    );

}


//
//
//public class LeaderboardActivity extends FragmentActivity implements OnMapReadyCallback,
//        LocationListener,GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener{
//
//private GoogleMap mMap;
//        Location mLastLocation;
//        Marker mCurrLocationMarker;
//        GoogleApiClient mGoogleApiClient;
//        LocationRequest mLocationRequest;
//
//@Override
//protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_maps);
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//        .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
//
//        }
//
//@Override
//public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//        if (ContextCompat.checkSelfPermission(this,
//        Manifest.permission.ACCESS_FINE_LOCATION)
//        == PackageManager.PERMISSION_GRANTED) {
//        buildGoogleApiClient();
//        mMap.setMyLocationEnabled(true);
//        }
//        }
//        else {
//        buildGoogleApiClient();
//        mMap.setMyLocationEnabled(true);
//        }
//
//        }
//protected synchronized void buildGoogleApiClient() {
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//        .addConnectionCallbacks(this)
//        .addOnConnectionFailedListener(this)
//        .addApi(LocationServices.API).build();
//        mGoogleApiClient.connect();
//        }
//
//@Override
//public void onConnected(Bundle bundle) {
//
//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(1000);
//        mLocationRequest.setFastestInterval(1000);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//        if (ContextCompat.checkSelfPermission(this,
//        Manifest.permission.ACCESS_FINE_LOCATION)
//        == PackageManager.PERMISSION_GRANTED) {
//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//        }
//
//        }
//
//@Override
//public void onConnectionSuspended(int i) {
//
//        }
//
//@Override
//public void onLocationChanged(Location location) {
//
//        mLastLocation = location;
//        if (mCurrLocationMarker != null) {
//        mCurrLocationMarker.remove();
//        }
//        //Place current location marker
//        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(latLng);
//        markerOptions.title("Current Position");
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
//        mCurrLocationMarker = mMap.addMarker(markerOptions);
//
//        //move map camera
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
//
//        //stop location updates
//        if (mGoogleApiClient != null) {
//        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
//        }
//
//        }
//
//@Override
//public void onConnectionFailed(ConnectionResult connectionResult) {
//
//        }
//
//        }