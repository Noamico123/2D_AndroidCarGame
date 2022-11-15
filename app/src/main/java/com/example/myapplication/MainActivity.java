package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    static final int NUMBER_OF_ROWS = 5;
    static final int NUMBER_OF_COLUMNS = 3;
    static final int DELAY_FIRST_BLOCK = 300;
    static final int DELAY_SECOND_BLOCK = 500;

    static final int LEFT_LANE = 12;
    static final int CENTER_LANE = 13;
    static final int RIGHT_LANE = 14;

    protected boolean shouldMoveForward = true;
    protected int carLocation;
    protected ArrayList<LinearLayout> blocksArray;

    protected int firstBlockLocation = new Random().nextInt(NUMBER_OF_COLUMNS);
    protected int secondBlockLocation = new Random().nextInt(NUMBER_OF_COLUMNS);

    //Define car's linear layouts
    protected LinearLayout right_car;
    protected LinearLayout center_car;
    protected LinearLayout left_car;

    //Define Buttons
    protected ImageButton right_button;
    protected ImageButton left_button;
    protected Button start_btn;

    //Define block's linear layouts
    protected LinearLayout first_first;
    protected LinearLayout first_second;
    protected LinearLayout first_third;

    protected LinearLayout second_first;
    protected LinearLayout second_second;
    protected LinearLayout second_third;

    protected LinearLayout third_first;
    protected LinearLayout third_second;
    protected LinearLayout third_third;

    protected LinearLayout fourth_first;
    protected LinearLayout fourth_second;
    protected LinearLayout fourth_third;

    protected LinearLayout fifth_first;
    protected LinearLayout fifth_second;
    protected LinearLayout fifth_third;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initParameters();
        initCarLocation();

        blocksArray = getBlocksArray();
        initBlocksState();

        right_button.setOnClickListener(v -> moveCarToRight());
        left_button.setOnClickListener(v -> moveCarToLeft());
        start_btn.setOnClickListener(v -> startGame());
    }

    Timer secondBlockTimer = new Timer();
    Timer firstBlockTimer = new Timer();

    private void startFirstBlockTimer() {
        firstBlockTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d("Thread", "FirstBlock: " + Thread.currentThread().getName());
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d("Thread", "FirstBlock: " + Thread.currentThread().getName());
                        updateFirstBlockUI();
                    }
                });
            }
        }, DELAY_FIRST_BLOCK, DELAY_FIRST_BLOCK);
    }

    private void startSecondBlockTimer() {
        secondBlockTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d("Thread", "SecondBlock: " + Thread.currentThread().getName());
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d("Thread", "SecondBlock: " + Thread.currentThread().getName());
                        updateSecondBlockUI();
                    }
                });
            }
        }, DELAY_SECOND_BLOCK, DELAY_SECOND_BLOCK);
    }

    private void stopTimer() {
        firstBlockTimer.cancel();
        secondBlockTimer.cancel();
    }

    private void updateFirstBlockUI() {
        moveFirstBlock();
    }

    private void updateSecondBlockUI() {
        moveSecondBlock();
    }

    private void moveFirstBlock() {
        shouldMoveForward = true;

        if (LEFT_LANE <= firstBlockLocation && firstBlockLocation <= RIGHT_LANE) {
            isGameOver();
            blocksArray.get(firstBlockLocation).setVisibility(View.INVISIBLE);
            firstBlockLocation = new Random().nextInt(NUMBER_OF_COLUMNS);
            blocksArray.get(firstBlockLocation).setVisibility(View.VISIBLE);
            Log.i("Location" , "firstBlockLocation: " + firstBlockLocation);
            shouldMoveForward = false;
        }

        if (shouldMoveForward){
            blocksArray.get(firstBlockLocation).setVisibility(View.INVISIBLE);
            firstBlockLocation += NUMBER_OF_COLUMNS;
        }

        if (firstBlockLocation < blocksArray.size()) {
            blocksArray.get(firstBlockLocation).setVisibility(View.VISIBLE);
        }
    }

    private void moveSecondBlock() {
        shouldMoveForward = true;

        if (LEFT_LANE <= secondBlockLocation && secondBlockLocation <= RIGHT_LANE) {
            isGameOver();

            blocksArray.get(secondBlockLocation).setVisibility(View.INVISIBLE);
            secondBlockLocation = new Random().nextInt(NUMBER_OF_COLUMNS);
            blocksArray.get(secondBlockLocation).setVisibility(View.VISIBLE);
            shouldMoveForward = false;
            Log.i("Location" , "secondBlockLocation: " + secondBlockLocation);
        }

        if (shouldMoveForward){
            blocksArray.get(secondBlockLocation).setVisibility(View.INVISIBLE);
            secondBlockLocation += NUMBER_OF_COLUMNS;
        }

        if (secondBlockLocation < blocksArray.size()) {
            blocksArray.get(secondBlockLocation).setVisibility(View.VISIBLE);
        }
    }

    private void isGameOver() {
        if (carLocation == secondBlockLocation || carLocation == firstBlockLocation ){
            stopTimer();
            Log.i("BBOOMMM", "carLocation: " + carLocation + "Lane: " + firstBlockLocation);
            Log.i("BBOOMMM", "carLocation: " + carLocation + "Lane: " + secondBlockLocation);
        }
    }

    private void initCarLocation() {
        Log.i("INIT","car in the center");
        center_car.setVisibility(View.VISIBLE);
        right_car.setVisibility(View.INVISIBLE);
        left_car.setVisibility(View.INVISIBLE);
    }

    private void initParameters() {
        Log.i("INIT","start init params");
        initBlocksLinearLayoutViews();
        initCarsLinearLayoutViews();
        initButtons();
    }

    private void initCarsLinearLayoutViews() {
        Log.i("INIT","car's possible locations");
        left_car = (LinearLayout) findViewById(R.id.sixth_row_first_column);
        center_car = (LinearLayout) findViewById(R.id.sixth_row_second_column);
        right_car = (LinearLayout) findViewById(R.id.sixth_row_third_column);
    }

    private void initButtons() {
        Log.i("INIT","buttons");
        right_button = (ImageButton) findViewById(R.id.right_button);
        left_button = (ImageButton) findViewById(R.id.left_button);
        start_btn = (Button) findViewById(R.id.start_btn);
    }

    private void initBlocksLinearLayoutViews() {
        Log.i("INIT","blocks locations");
        first_first = (LinearLayout) findViewById(R.id.first_row_first_column);
        first_second = (LinearLayout) findViewById(R.id.first_row_second_column);
        first_third = (LinearLayout) findViewById(R.id.first_row_third_column);

        second_first = (LinearLayout) findViewById(R.id.second_row_first_column);
        second_second = (LinearLayout) findViewById(R.id.second_row_second_column);
        second_third = (LinearLayout) findViewById(R.id.second_row_third_column);

        third_first = (LinearLayout) findViewById(R.id.third_row_first_column);
        third_second = (LinearLayout) findViewById(R.id.third_row_second_column);
        third_third = (LinearLayout) findViewById(R.id.third_row_third_column);

        fourth_first = (LinearLayout) findViewById(R.id.fourth_row_first_column);
        fourth_second = (LinearLayout) findViewById(R.id.fourth_row_second_column);
        fourth_third = (LinearLayout) findViewById(R.id.fourth_row_third_column);

        fifth_first = (LinearLayout) findViewById(R.id.fifth_row_first_column);
        fifth_second = (LinearLayout) findViewById(R.id.fifth_row_second_column);
        fifth_third = (LinearLayout) findViewById(R.id.fifth_row_third_column);
    }

    private void moveCarToRight() {
        Log.i("MOVE","right");
        if (left_car.getVisibility() == View.VISIBLE) {
            center_car.setVisibility(View.VISIBLE);
            right_car.setVisibility(View.INVISIBLE);
            left_car.setVisibility(View.INVISIBLE);
            carLocation = CENTER_LANE;
        }
        else if (center_car.getVisibility() == View.VISIBLE) {
            center_car.setVisibility(View.INVISIBLE);
            right_car.setVisibility(View.VISIBLE);
            left_car.setVisibility(View.INVISIBLE);
            carLocation = RIGHT_LANE;
        }
    }

    private void moveCarToLeft() {
        Log.i("MOVE","left");
        if (right_car.getVisibility() == View.VISIBLE) {
            center_car.setVisibility(View.VISIBLE);
            right_car.setVisibility(View.INVISIBLE);
            left_car.setVisibility(View.INVISIBLE);
            carLocation = CENTER_LANE;
        }
        else if (center_car.getVisibility() == View.VISIBLE) {
            center_car.setVisibility(View.INVISIBLE);
            right_car.setVisibility(View.INVISIBLE);
            left_car.setVisibility(View.VISIBLE);
            carLocation = LEFT_LANE;
        }
    }

    private void startGame() {
        start_btn.setVisibility(View.INVISIBLE);
        startFirstBlockTimer();
        startSecondBlockTimer();
    }

    private void initBlocksState() {
        Log.i("STATE","all blocks to invisible state except for two block");
        for (int i = 0; i < blocksArray.size(); i++){
            blocksArray.get(i).setVisibility(View.INVISIBLE);
        }

        while (firstBlockLocation == secondBlockLocation){
            secondBlockLocation = new Random().nextInt(NUMBER_OF_COLUMNS);
        }

        blocksArray.get(firstBlockLocation).setVisibility(View.VISIBLE);
        blocksArray.get(secondBlockLocation).setVisibility(View.VISIBLE);
    }

    private ArrayList<LinearLayout> getBlocksArray() {
        Log.i("INIT","blocks array list");
        ArrayList<LinearLayout> blocksArray = new ArrayList<>();

        blocksArray.add(first_first);
        blocksArray.add(first_second);
        blocksArray.add(first_third);

        blocksArray.add(second_first);
        blocksArray.add(second_second);
        blocksArray.add(second_third);

        blocksArray.add(third_first);
        blocksArray.add(third_second);
        blocksArray.add(third_third);

        blocksArray.add(fourth_first);
        blocksArray.add(fourth_second);
        blocksArray.add(fourth_third);

        blocksArray.add(fifth_first);
        blocksArray.add(fifth_second);
        blocksArray.add(fifth_third);

        Log.i("length of blocks array", String.valueOf(blocksArray.size()));

        return blocksArray;
    }

}