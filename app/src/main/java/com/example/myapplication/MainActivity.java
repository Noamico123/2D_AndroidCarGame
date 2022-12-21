package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // Constants
    static final int NUMBER_OF_ROWS = 6;
    static final int NUMBER_OF_COLUMNS = 5;
    static final int DELAY_FIRST_BLOCK = 300;
    static final int DELAY_SECOND_BLOCK = 650;
    static final int DELAY_FIRST_COIN = 500;
    static final int DELAY_SECOND_COIN = 850;
    static final int SCORE_JUMP = 100;
    static final int SCORE_COIN = 50;

    static final int MAX_LEFT_LANE = 25;
    static final int LEFT_LANE = 26;
    static final int CENTER_LANE = 27;
    static final int RIGHT_LANE = 28;
    static final int MAX_RIGHT_LANE = 29;

    // General
    protected boolean shouldMoveForward = true;
    protected TextView score_text_view;
    protected int scoreCounter = 0;
    protected MediaPlayer coin_sound = null;
    protected MediaPlayer crash_sound = null;

    //Generating block's locations, array for blocks and Timers for each block
    protected int firstBlockLocation = new Random().nextInt(NUMBER_OF_COLUMNS);
    protected int secondBlockLocation = new Random().nextInt(NUMBER_OF_COLUMNS);
    protected int firstCoinLocation = new Random().nextInt(NUMBER_OF_COLUMNS);
    protected int secondCoinLocation = new Random().nextInt(NUMBER_OF_COLUMNS);
    protected ArrayList<ImageView> blocksArray;
    Timer firstBlockTimer;
    Timer secondBlockTimer;
    Timer firstCoinTimer;
    Timer secondCoinTimer;

    //Define car's linear layouts and initial location
    protected LinearLayout right_car;
    protected LinearLayout center_car;
    protected LinearLayout left_car;
    protected LinearLayout max_left_car;
    protected LinearLayout max_right_car;
    protected int carLocation = 27;

    //Define heart's ImageView and initial total number of hearts
    protected ImageView right_heart;
    protected ImageView center_heart;
    protected ImageView left_heart;
    protected int numberOfHearts = 3;

    //Define Buttons
    protected ImageButton right_button;
    protected ImageButton left_button;
    protected Button start_btn;

    //Define block's linear layouts
    protected ImageView zero_first;
    protected ImageView zero_second;
    protected ImageView zero_third;
    protected ImageView zero_fourth;
    protected ImageView zero_fifth;

    protected ImageView first_first;
    protected ImageView first_second;
    protected ImageView first_third;
    protected ImageView first_fourth;
    protected ImageView first_fifth;

    protected ImageView second_first;
    protected ImageView second_second;
    protected ImageView second_third;
    protected ImageView second_fourth;
    protected ImageView second_fifth;

    protected ImageView third_first;
    protected ImageView third_second;
    protected ImageView third_third;
    protected ImageView third_fourth;
    protected ImageView third_fifth;

    protected ImageView fourth_first;
    protected ImageView fourth_second;
    protected ImageView fourth_third;
    protected ImageView fourth_fourth;
    protected ImageView fourth_fifth;

    protected ImageView fifth_first;
    protected ImageView fifth_second;
    protected ImageView fifth_third;
    protected ImageView fifth_fourth;
    protected ImageView fifth_fifth;

    private SensorManager sensorManager;
    private Sensor sensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Objects.requireNonNull(this.getSupportActionBar()).hide();
        }
        catch (NullPointerException ignored) {}

        setContentView(R.layout.activity_main);

        initParameters();
        initCarLocation();

        blocksArray = getBlocksArray();
        initBlocksState();

        right_button.setOnClickListener(v -> moveCarToRight());
        left_button.setOnClickListener(v -> moveCarToLeft());
        start_btn.setOnClickListener(v -> startGame());

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        stopGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        start_btn.setVisibility(View.VISIBLE);

    }

    /*
    * ##############################
    * Init Sensor Manager
    * ##############################
    * */
    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        if (x < 0) {
            Log.i("SensorActivity", "You tilt the device right  " + x);
            moveCarToRight();
        }
        if (x > 0) {
            Log.i("SensorActivity", "You tilt the device left  " + x);
            moveCarToLeft();
        }
    }

    /*
     * ##############################
     * Init App
     * ##############################
     * */
    private void initCarLocation() {
        Log.i("INIT","car in the center");
        center_car.setVisibility(View.INVISIBLE);
        right_car.setVisibility(View.INVISIBLE);
        right_car.setVisibility(View.INVISIBLE);
        max_left_car.setVisibility(View.INVISIBLE);
        max_right_car.setVisibility(View.INVISIBLE);
    }

    private void initParameters() {
        Log.i("INIT","start init params");
        initBlocksLinearLayoutViews();
        initCarsLinearLayoutViews();
        initHeartsImageView();
        initButtons();
    }

    private void initCarsLinearLayoutViews() {
        Log.i("INIT","car's possible locations");
        max_left_car = (LinearLayout) findViewById(R.id.sixth_row_first_column);
        left_car = (LinearLayout) findViewById(R.id.sixth_row_second_column);
        center_car = (LinearLayout) findViewById(R.id.sixth_row_third_column);
        right_car = (LinearLayout) findViewById(R.id.sixth_row_forth_column);
        max_right_car = (LinearLayout) findViewById(R.id.sixth_row_fifth_column);
    }

    private void initHeartsImageView() {
        Log.i("INIT","hearts");
        left_heart = (ImageView) findViewById(R.id.top_bar_left_side_first_heart);
        center_heart = (ImageView) findViewById(R.id.top_bar_left_side_second_heart);
        right_heart = (ImageView) findViewById(R.id.top_bar_left_side_third_heart);
    }

    private void initButtons() {
        Log.i("INIT","buttons");
        right_button = (ImageButton) findViewById(R.id.right_button);
        left_button = (ImageButton) findViewById(R.id.left_button);
        start_btn = (Button) findViewById(R.id.start_btn);
    }

    private void initBlocksLinearLayoutViews() {
        Log.i("INIT","blocks locations");
        zero_first = (ImageView) findViewById(R.id.zero_row_first_column_block);
        zero_second = (ImageView) findViewById(R.id.zero_row_second_column_block);
        zero_third = (ImageView) findViewById(R.id.zero_row_third_column_block);
        zero_fourth = (ImageView) findViewById(R.id.zero_row_fourth_column_block);
        zero_fifth = (ImageView) findViewById(R.id.zero_row_fifth_column_block);

        first_first = (ImageView) findViewById(R.id.first_row_first_column_block);
        first_second = (ImageView) findViewById(R.id.first_row_second_column_block);
        first_third = (ImageView) findViewById(R.id.first_row_third_column_block);
        first_fourth = (ImageView) findViewById(R.id.first_row_fourth_column_block);
        first_fifth = (ImageView) findViewById(R.id.first_row_fifth_column_block);

        second_first = (ImageView) findViewById(R.id.second_row_first_column_block);
        second_second = (ImageView) findViewById(R.id.second_row_second_column_block);
        second_third = (ImageView) findViewById(R.id.second_row_third_column_block);
        second_fourth = (ImageView) findViewById(R.id.second_row_fourth_column_block);
        second_fifth = (ImageView) findViewById(R.id.second_row_fifth_column_block);

        third_first = (ImageView) findViewById(R.id.third_row_first_column_block);
        third_second = (ImageView) findViewById(R.id.third_row_second_column_block);
        third_third = (ImageView) findViewById(R.id.third_row_third_column_block);
        third_fourth = (ImageView) findViewById(R.id.third_row_fourth_column_block);
        third_fifth = (ImageView) findViewById(R.id.third_row_fifth_column_block);

        fourth_first = (ImageView) findViewById(R.id.fourth_row_first_column_block);
        fourth_second = (ImageView) findViewById(R.id.fourth_row_second_column_block);
        fourth_third = (ImageView) findViewById(R.id.fourth_row_third_column_block);
        fourth_fourth = (ImageView) findViewById(R.id.fourth_row_fourth_column_block);
        fourth_fifth = (ImageView) findViewById(R.id.fourth_row_fifth_column_block);

        fifth_first = (ImageView) findViewById(R.id.fifth_row_first_column_block);
        fifth_second = (ImageView) findViewById(R.id.fifth_row_second_column_block);
        fifth_third = (ImageView) findViewById(R.id.fifth_row_third_column_block);
        fifth_fourth = (ImageView) findViewById(R.id.fifth_row_fourth_column_block);
        fifth_fifth = (ImageView) findViewById(R.id.fifth_row_fifth_column_block);
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

    private ArrayList<ImageView> getBlocksArray() {
        Log.i("INIT","blocks array list");
        ArrayList<ImageView> blocksArray = new ArrayList<>();

        blocksArray.add(zero_first);
        blocksArray.add(zero_second);
        blocksArray.add(zero_third);
        blocksArray.add(zero_fourth);
        blocksArray.add(zero_fifth);

        blocksArray.add(first_first);
        blocksArray.add(first_second);
        blocksArray.add(first_third);
        blocksArray.add(first_fourth);
        blocksArray.add(first_fifth);

        blocksArray.add(second_first);
        blocksArray.add(second_second);
        blocksArray.add(second_third);
        blocksArray.add(second_fourth);
        blocksArray.add(second_fifth);

        blocksArray.add(third_first);
        blocksArray.add(third_second);
        blocksArray.add(third_third);
        blocksArray.add(third_fourth);
        blocksArray.add(third_fifth);

        blocksArray.add(fourth_first);
        blocksArray.add(fourth_second);
        blocksArray.add(fourth_third);
        blocksArray.add(fourth_fourth);
        blocksArray.add(fourth_fifth);

        blocksArray.add(fifth_first);
        blocksArray.add(fifth_second);
        blocksArray.add(fifth_third);
        blocksArray.add(fifth_fourth);
        blocksArray.add(fifth_fifth);


        Log.i("BlocksArrayLength", String.valueOf(blocksArray.size()));

        return blocksArray;
    }

    /*
    * ##############################
    * Timers
    * ##############################
    * */
    private void startFirstBlockTimer() {
        firstBlockTimer = new Timer();
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
        secondBlockTimer = new Timer();
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

    private void startFirstCoinTimer() {
        firstCoinTimer = new Timer();
        firstCoinTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d("Thread", "FirstCoin: " + Thread.currentThread().getName());
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d("Thread", "FirstCoin: " + Thread.currentThread().getName());
                        updateFirstCoinUI();
                    }
                });
            }
        }, DELAY_FIRST_COIN, DELAY_FIRST_COIN);
    }

    private void startSecondCoinTimer() {
        secondCoinTimer = new Timer();
        secondCoinTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d("Thread", "FirstCoin: " + Thread.currentThread().getName());
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d("Thread", "FirstCoin: " + Thread.currentThread().getName());
                        updateSecondCoinUI();
                    }
                });
            }
        }, DELAY_SECOND_COIN, DELAY_SECOND_COIN);
    }

    private void stopGame() {
        firstBlockTimer.cancel();
        secondBlockTimer.cancel();
        firstCoinTimer.cancel();
        secondCoinTimer.cancel();
    }

    private void startGame() {
        center_car.setVisibility(View.VISIBLE);
        left_heart.setVisibility(View.VISIBLE);
        center_heart.setVisibility(View.VISIBLE);
        right_heart.setVisibility(View.VISIBLE);
        start_btn.setVisibility(View.INVISIBLE);

        score_text_view = (TextView) findViewById(R.id.score_text_view);
        scoreCounter = 0;
        score_text_view.setText(String.valueOf(scoreCounter));

        coin_sound = MediaPlayer.create(MainActivity.this, R.raw.coin_pickup);
        crash_sound = MediaPlayer.create(MainActivity.this, R.raw.crash_sound);
        startFirstBlockTimer();
        startSecondBlockTimer();
        startFirstCoinTimer();
        startSecondCoinTimer();
    }

    /*
     * ##############################
     * Game Control and UI Handlers
     * ##############################
     * */
    private void moveFirstBlock() {
        shouldMoveForward = true;

        if (carLocation == firstBlockLocation) {
            crash_sound.start();
        }

        if (MAX_LEFT_LANE <= firstBlockLocation && firstBlockLocation <= MAX_RIGHT_LANE) {
            isGameOver();
            scoreCounter += SCORE_JUMP;
            score_text_view.setText(String.valueOf(scoreCounter));
            blocksArray.get(firstBlockLocation).setVisibility(View.INVISIBLE);
            firstBlockLocation = new Random().nextInt(NUMBER_OF_COLUMNS);

            while (firstBlockLocation == secondBlockLocation ||
                    firstBlockLocation == firstCoinLocation ||
                    firstBlockLocation == secondCoinLocation) {
                firstBlockLocation = new Random().nextInt(NUMBER_OF_COLUMNS);
            }

            blocksArray.get(firstBlockLocation).setImageResource(R.drawable.block);
            blocksArray.get(firstBlockLocation).setVisibility(View.VISIBLE);
            Log.i("Location" , "firstBlockLocation: " + firstBlockLocation);
            shouldMoveForward = false;
        }

        if (shouldMoveForward){
            blocksArray.get(firstBlockLocation).setVisibility(View.INVISIBLE);
            firstBlockLocation += NUMBER_OF_COLUMNS;
        }

        if (firstBlockLocation < blocksArray.size()) {
            blocksArray.get(firstBlockLocation).setImageResource(R.drawable.block);
            blocksArray.get(firstBlockLocation).setVisibility(View.VISIBLE);
        }
    }

    private void moveSecondBlock() {
        shouldMoveForward = true;

        if (carLocation == secondBlockLocation) {
            crash_sound.start();
        }

        if (MAX_LEFT_LANE <= secondBlockLocation && secondBlockLocation <= MAX_RIGHT_LANE) {
            isGameOver();
            scoreCounter += SCORE_JUMP;
            score_text_view.setText(String.valueOf(scoreCounter));
            blocksArray.get(secondBlockLocation).setVisibility(View.INVISIBLE);
            secondBlockLocation = new Random().nextInt(NUMBER_OF_COLUMNS);

            while (secondBlockLocation == firstBlockLocation ||
                    secondBlockLocation == firstCoinLocation ||
                    secondBlockLocation == secondCoinLocation) {
                secondBlockLocation = new Random().nextInt(NUMBER_OF_COLUMNS);
            }

            blocksArray.get(secondBlockLocation).setImageResource(R.drawable.block);
            blocksArray.get(secondBlockLocation).setVisibility(View.VISIBLE);
            shouldMoveForward = false;
            Log.i("Location" , "secondBlockLocation: " + secondBlockLocation);
        }

        if (shouldMoveForward){
            blocksArray.get(secondBlockLocation).setVisibility(View.INVISIBLE);
            secondBlockLocation += NUMBER_OF_COLUMNS;
        }

        if (secondBlockLocation < blocksArray.size()) {
            blocksArray.get(secondBlockLocation).setImageResource(R.drawable.block);
            blocksArray.get(secondBlockLocation).setVisibility(View.VISIBLE);
        }
    }

    private void moveFirstCoin() {
        shouldMoveForward = true;

        if (MAX_LEFT_LANE <= firstCoinLocation && firstCoinLocation <= MAX_RIGHT_LANE) {
            isGameOver();
            scoreCounter += SCORE_COIN;
            coin_sound.start();
            score_text_view.setText(String.valueOf(scoreCounter));
            blocksArray.get(firstCoinLocation).setVisibility(View.INVISIBLE);
            firstCoinLocation = new Random().nextInt(NUMBER_OF_COLUMNS);

            while (firstCoinLocation == secondBlockLocation ||
                    firstCoinLocation == firstBlockLocation ||
                    firstCoinLocation == secondCoinLocation) {
                firstCoinLocation = new Random().nextInt(NUMBER_OF_COLUMNS);
            }

            blocksArray.get(firstCoinLocation).setImageResource(R.drawable.coin);
            blocksArray.get(firstCoinLocation).setVisibility(View.VISIBLE);
            shouldMoveForward = false;
            Log.i("Location" , "firstCoinLocation: " + firstCoinLocation);
        }

        if (shouldMoveForward){
            blocksArray.get(firstCoinLocation).setVisibility(View.INVISIBLE);
            firstCoinLocation += NUMBER_OF_COLUMNS;
        }

        if (firstCoinLocation < blocksArray.size()) {
            blocksArray.get(firstCoinLocation).setImageResource(R.drawable.coin);
            blocksArray.get(firstCoinLocation).setVisibility(View.VISIBLE);
        }
    }

    private void moveSecondCoin() {
        shouldMoveForward = true;

        if (MAX_LEFT_LANE <= secondCoinLocation && secondCoinLocation <= MAX_RIGHT_LANE) {
            isGameOver();
            scoreCounter += SCORE_COIN;
            coin_sound.start();
            score_text_view.setText(String.valueOf(scoreCounter));
            blocksArray.get(secondCoinLocation).setVisibility(View.INVISIBLE);
            secondCoinLocation = new Random().nextInt(NUMBER_OF_COLUMNS);

            while (secondCoinLocation == secondBlockLocation ||
                    secondCoinLocation == firstBlockLocation ||
                    secondCoinLocation == firstCoinLocation) {
                secondCoinLocation = new Random().nextInt(NUMBER_OF_COLUMNS);
            }

            blocksArray.get(secondCoinLocation).setImageResource(R.drawable.coin);
            blocksArray.get(secondCoinLocation).setVisibility(View.VISIBLE);
            shouldMoveForward = false;
            Log.i("Location" , "secondCoinLocation: " + secondCoinLocation);
        }

        if (shouldMoveForward){
            blocksArray.get(secondCoinLocation).setVisibility(View.INVISIBLE);
            secondCoinLocation += NUMBER_OF_COLUMNS;
        }

        if (secondCoinLocation < blocksArray.size()) {
            blocksArray.get(secondCoinLocation).setImageResource(R.drawable.coin);
            blocksArray.get(secondCoinLocation).setVisibility(View.VISIBLE);
        }
    }

    private void isGameOver() {
        if (carLocation == secondBlockLocation || carLocation == firstBlockLocation ){
            numberOfHearts -= 1;
            handleHeartsAndFinishGame();
            Log.i("BBOOMMM", "carLocation: " + carLocation + "Lane: " + firstBlockLocation);
            Log.i("BBOOMMM", "carLocation: " + carLocation + "Lane: " + secondBlockLocation);
        }
    }

    private void moveCarToRight() {
        Log.i("MOVE","right");
        if (max_left_car.getVisibility() == View.VISIBLE) {
            max_left_car.setVisibility(View.INVISIBLE);
            left_car.setVisibility(View.VISIBLE);
            carLocation = LEFT_LANE;
        }

        else if (left_car.getVisibility() == View.VISIBLE) {
            left_car.setVisibility(View.INVISIBLE);
            center_car.setVisibility(View.VISIBLE);
            carLocation = CENTER_LANE;
        }

        else if (center_car.getVisibility() == View.VISIBLE) {
            center_car.setVisibility(View.INVISIBLE);
            right_car.setVisibility(View.VISIBLE);
            carLocation = RIGHT_LANE;
        }

        else if (right_car.getVisibility() == View.VISIBLE) {
            right_car.setVisibility(View.INVISIBLE);
            max_right_car.setVisibility(View.VISIBLE);
            carLocation = MAX_RIGHT_LANE;
        }
    }

    private void moveCarToLeft() {
        Log.i("MOVE","left");
        if (max_right_car.getVisibility() == View.VISIBLE) {
            max_right_car.setVisibility(View.INVISIBLE);
            right_car.setVisibility(View.VISIBLE);
            carLocation = RIGHT_LANE;
        }

        else if (right_car.getVisibility() == View.VISIBLE) {
            right_car.setVisibility(View.INVISIBLE);
            center_car.setVisibility(View.VISIBLE);
            carLocation = CENTER_LANE;
        }

        else if (center_car.getVisibility() == View.VISIBLE) {
            center_car.setVisibility(View.INVISIBLE);
            left_car.setVisibility(View.VISIBLE);
            carLocation = LEFT_LANE;
        }

        else if (left_car.getVisibility() == View.VISIBLE) {
            left_car.setVisibility(View.INVISIBLE);
            max_left_car.setVisibility(View.VISIBLE);
            carLocation = MAX_LEFT_LANE;
        }
    }

    private void updateFirstBlockUI() {
        moveFirstBlock();
    }

    private void updateSecondBlockUI() {
        moveSecondBlock();
    }

    private void updateFirstCoinUI() {
        moveFirstCoin();
    }

    private void updateSecondCoinUI() {
        moveSecondCoin();
    }


    @SuppressLint("SetTextI18n")
    private void handleHeartsAndFinishGame() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(200);
        }

        if (numberOfHearts == 2) left_heart.setVisibility(View.INVISIBLE);

        if (numberOfHearts == 1) center_heart.setVisibility(View.INVISIBLE);

        if (numberOfHearts <= 0) {
            right_heart.setVisibility(View.INVISIBLE);
            stopGame();
            start_btn.setText("Try Again?");
            start_btn.setVisibility(View.VISIBLE);
            numberOfHearts = 3;

            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(1100, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(1100);
            }
        }
    }
}