package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    private Spinner spinner;
    private boolean vib, tilt, sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUIVisibility();
        setContentView(R.layout.activity_settings);

        spinner = findViewById(R.id.numOfLanesSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.numOfLanes_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        SwitchCompat vibSwitch = findViewById(R.id.vibSwitch);
        SwitchCompat tiltSwitch = findViewById(R.id.tiltSwitch);
        SwitchCompat soundSwitch = findViewById(R.id.soundSwitch);
        Button startBtn = findViewById(R.id.start_button);
        Button cancelBtn = findViewById(R.id.cancel_button);

        vibSwitch.toggle();
        vib = true;
        tilt = false;
        sound = true;

        vibSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    setVib(true);
                    Toast.makeText(SettingsActivity.this, "Vibration: ON", Toast.LENGTH_SHORT).show();
                }
                else{
                    setVib(false);
                    Toast.makeText(SettingsActivity.this, "Vibration: OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tiltSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    setTilt(true);
                    Toast.makeText(SettingsActivity.this, "Tilt: ON", Toast.LENGTH_SHORT).show();
                }
                else{
                    setTilt(false);
                    Toast.makeText(SettingsActivity.this, "Tilt: OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });

        soundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    setSound(true);
                    Toast.makeText(SettingsActivity.this, "Sound: ON", Toast.LENGTH_SHORT).show();
                }
                else{
                    setSound(false);
                    Toast.makeText(SettingsActivity.this, "Sound: OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });



        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGameActivity();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // Method that start the game activity.
    public void startGameActivity(){
        Intent intent;
        int spinnerPos = spinner.getSelectedItemPosition();
        String[] numValues = getResources().getStringArray(R.array.numOfLanes_array);
        final int numOfLanes = Integer.parseInt(numValues[spinnerPos]);
        if(numOfLanes == 3){
            intent = new Intent(this, ThreeLanesActivity.class);
        }
        else {
            intent = new Intent(this, FiveLanesActivity.class);
        }
        intent.putExtra("vib", vib);
        intent.putExtra("tilt", tilt);
        intent.putExtra("sound", sound);
        startActivity(intent);
        finish();
    }

    // Method that set UI flags.
    public void setUIVisibility(){
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    // Getters and setters.
    public void setVib(boolean bool){
        this.vib = bool;
    }

    public boolean getVib(){
        return this.vib;
    }

    public void setTilt(boolean bool){
        this.tilt = bool;
    }

    public boolean getTilt(){
        return this.tilt;
    }

    public void setSound(boolean bool){
        this.tilt = bool;
    }

    public boolean getSound(){
        return this.tilt;
    }

    @Override
    public void onResume(){
        super.onResume();
        setUIVisibility();
    }
}
