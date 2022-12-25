package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

public class GameOverActivity extends AppCompatActivity {

    private final int PERMISSION_ID = 20;

    private int score, numOfLanes;
    private boolean vib, tilt;
    private EditText nameEditText;
    private String name;
    private SharedPreferences sharedPreferences;
    private ArrayList<Competitor> leaderboardArr;
    private FusedLocationProviderClient mFusedLocationClient;
    private double lon, lat;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUIVisibility();
        setContentView(R.layout.activity_game_over);

        sharedPreferences = getSharedPreferences("shared_preferences", MODE_PRIVATE);
        leaderboardArr = new ArrayList<>();

        TextView gameScore = findViewById(R.id.gameScore);
        TextView gameOverStr = findViewById(R.id.game_over_title);
        Button playAgainBtn = findViewById(R.id.start_button);
        Button menuBtn = findViewById(R.id.menu_button);
        nameEditText = findViewById(R.id.nameEditText);
        LinearLayout enterNameLayout = findViewById(R.id.enterNameLayout);


        savedInstanceState = getIntent().getExtras();
        if (savedInstanceState != null) {
            score = savedInstanceState.getInt("score");
            numOfLanes = savedInstanceState.getInt("numOfLanes");
            vib = savedInstanceState.getBoolean("vib");
            tilt = savedInstanceState.getBoolean("tilt");
        }
        gameScore.setText(Integer.toString(score));

        if (!checkForNewHighScore()) {
            enterNameLayout.setVisibility(View.GONE);
        } else {
            gameOverStr.setText(R.string.new_best_score_str);
            gameOverStr.setTextSize(50);
        }

        playAgainBtn.setOnClickListener(view -> startGameScreenActivity());

        menuBtn.setOnClickListener(view -> startMainActivity());

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLastLocation();
    }

    // Method that start the main activity.
    public void startMainActivity() {
        name = nameEditText.getText().toString();
        if (checkForNewHighScore()) {
            if (name.trim().length() > 0) {
                if(name.length() > 10){
                    Toast.makeText(GameOverActivity.this, "The name contains too much letters!", Toast.LENGTH_SHORT).show();
                } else {
                    saveHighScore();
                    finish();
                }
            } else {
                Toast.makeText(GameOverActivity.this, "Please enter valid name.", Toast.LENGTH_SHORT).show();
            }
        } else {
            finish();
        }
    }

    // Method that start the game activity.
    public void startGameScreenActivity() {
        Intent intent;
        if (numOfLanes == 3) {
            intent = new Intent(this, ThreeLanesActivity.class);
        } else {
            intent = new Intent(this, FiveLanesActivity.class);
        }
        intent.putExtra("vib", vib);
        intent.putExtra("tilt", tilt);

        name = nameEditText.getText().toString();
        if (checkForNewHighScore()) {
            if (name.trim().length() > 0) {
                if(name.length() > 10){
                    Toast.makeText(GameOverActivity.this, "The name contains too much letters!", Toast.LENGTH_SHORT).show();
                } else {
                    saveHighScore();
                    startActivity(intent);
                    finish();
                }
            } else {
                Toast.makeText(GameOverActivity.this, "Please enter valid name.", Toast.LENGTH_SHORT).show();
            }
        } else {
            startActivity(intent);
            finish();
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

    // Method that save the highest score.
    private void saveHighScore() {
        int highScore = score;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Competitor player = new Competitor("", 0, 0, 0);
        player.setName(name);
        player.setScore(highScore);
        player.setLatitude(lat);
        player.setLongitude(lon);

        String jsonString = sharedPreferences.getString("list", null);
        Gson gson = new Gson();
        if (jsonString != null) {
            Type type = new TypeToken<ArrayList<Competitor>>() {
            }.getType();
            ArrayList<Competitor> listFromGson;
            listFromGson = gson.fromJson(jsonString, type);
            Collections.sort(listFromGson);
            if (listFromGson.size() == 10) {
                if (listFromGson.get(9).getScore() < highScore) {
                    listFromGson.remove(9);
                    listFromGson.add(player);
                }
            } else {
                listFromGson.add(player);
            }
            jsonString = gson.toJson(listFromGson);
        } else {
            leaderboardArr.add(player);
            jsonString = gson.toJson(leaderboardArr);
        }
        editor.putString("list", jsonString);
        editor.apply();

    }

    // Method that check if the score is a new high score.
    public boolean checkForNewHighScore() {
        int highScore = score;
        String jsonString = sharedPreferences.getString("list", null);
        if (jsonString != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Competitor>>() {
            }.getType();
            ArrayList<Competitor> listFromGson;
            listFromGson = gson.fromJson(jsonString, type);
            Collections.sort(listFromGson);
            if (listFromGson.size() == 10) {
                return listFromGson.get(9).getScore() < highScore;
            } else return listFromGson.size() < 10;
        } else {
            return true;
        }
    }

    // Methods that check the user location.
    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        task -> {
                            Location location = task.getResult();

                            if (location == null) {
                                requestNewLocationData();
                            } else {
                                lat = location.getLatitude();
                                lon = location.getLongitude();
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }


    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            lat = mLastLocation.getLatitude();
            lon = mLastLocation.getLongitude();
        }
    };

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setUIVisibility();
    }
}
