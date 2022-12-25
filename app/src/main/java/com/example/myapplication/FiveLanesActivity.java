package com.example.myapplication;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.Random;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class FiveLanesActivity extends AppCompatActivity implements View.OnTouchListener, SensorEventListener {

    static final double NUMBER_OF_CAR_POS = 5.0;
    static final int NUMBER_OF_LANES = 5;
    static final int POINTS_FOR_COIN = 70;
    static final int POINTS_FOR_AVOIDING_BLOCK = 30;

    private RelativeLayout relativeLayout;
    private int rand, newRand, score, coinRand, coinLastRand;
    private final Handler handler = new Handler();
    private ObjectAnimator animate1Y, animate2Y, animate3Y, animate4Y, animate5Y, coinAnim1, coinAnim2, coinAnim3, coinAnim4, coinAnim5;

    private boolean moveLeft, moveRight, vib, tilt, gameOver = false, ifPlaying = true, coinCollision = false;
    private View carView, firstHeart, secondHeart, thirdHeart;
    private TextView scoreView, timerView;

    private Vibrator vibrator;
    private SensorManager sensorManager;
    private Sensor sensor;
    protected MediaPlayer coin_sound = null;
    protected MediaPlayer crash_sound = null;


    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUIVisibility();
        setContentView(R.layout.activity_five_lanes);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        savedInstanceState = getIntent().getExtras();
        if(savedInstanceState != null){
            vib = savedInstanceState.getBoolean("vib");
            tilt = savedInstanceState.getBoolean("tilt");
        }
        else{
            vib = true;
            tilt = false;
        }

        relativeLayout = findViewById(R.id.gameLayout);
        ImageButton left_button = findViewById(R.id.left_button);
        ImageButton right_button = findViewById(R.id.right_button);
        firstHeart = findViewById(R.id.firstHeart);
        secondHeart = findViewById(R.id.secondHeart);
        thirdHeart = findViewById(R.id.thirdHeart);
        scoreView = findViewById(R.id.score);
        coin_sound = MediaPlayer.create(FiveLanesActivity.this, R.raw.coin_pickup);
        crash_sound = MediaPlayer.create(FiveLanesActivity.this, R.raw.crash_sound);

        left_button.setOnTouchListener(this);
        right_button.setOnTouchListener(this);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if(tilt){
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            right_button.setVisibility(View.GONE);
            left_button.setVisibility(View.GONE);
        }

        relativeLayout.post(() -> {
            // Init the car view to the screen.
            carView = new View(FiveLanesActivity.this);
            carView.setLayoutParams(new RelativeLayout.LayoutParams(relativeLayout.getWidth() / NUMBER_OF_LANES, 250));
            carView.setBackgroundResource(R.drawable.car);
            carView.setX(relativeLayout.getWidth() / (float) NUMBER_OF_LANES * 2);
            carView.setY(relativeLayout.getHeight() - 250);
            relativeLayout.addView(carView);

            // Init a temporary text view for the timer.
            timerView = new TextView((FiveLanesActivity.this));
            timerView.setLayoutParams(new RelativeLayout.LayoutParams(relativeLayout.getWidth() / NUMBER_OF_LANES, 300));
            timerView.setTextSize(80);
            timerView.setTextColor(Color.WHITE);
            timerView.setGravity(Gravity.CENTER);
            timerView.setX(relativeLayout.getWidth() / (float) NUMBER_OF_LANES * 2);
            timerView.setY(relativeLayout.getHeight() / (float)2 - 300);
            relativeLayout.addView(timerView);
        });

        new Thread(() -> {
            try {
                handler.post(() -> new CountDownTimer(4000, 1000) {
                    @SuppressLint("SetTextI18n")
                    public void onTick(long sec) { timerView.setText(Integer.toString((int) (sec / 1000))); }

                    @Override
                    public void onFinish() {
                        ((ViewGroup) timerView.getParent()).removeView(timerView);
                    }
                }.start());
                Thread.sleep(3500);

                rand = new Random().nextInt(NUMBER_OF_LANES);
                coinLastRand = new Random().nextInt(NUMBER_OF_LANES);
                while (!gameOver) {
                    newRand = generateRand(rand);
                    do{
                        coinRand = generateRand(coinLastRand);
                    }while(coinRand == newRand);
                    gameLoop();
                    coinLoop();
                    handler.postDelayed(() -> {
                        if (ifPlaying) {
                            score = Integer.parseInt(scoreView.getText().toString());
                            if(coinCollision){
                                coin_sound.start();
                                score += POINTS_FOR_COIN;
                            }
                            score += POINTS_FOR_AVOIDING_BLOCK;
                            scoreView.setText(Integer.toString(score));
                            coinCollision = false;
                        }
                    }, 1500);
                    rand = newRand;
                    coinLastRand = coinRand;
                    try {
                        Thread.sleep(1000);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                startGameOverActivity();
                finish();
            }
        }).start();

    }

    // Generate different random number from the last one.
    public int generateRand(int lastRand) {
        int randNum;
        do{
            randNum = new Random().nextInt(NUMBER_OF_LANES);
        }while(lastRand == randNum);
        return randNum;
    }

    // Key touch events.
    @SuppressLint({"ClickableViewAccessibility", "NonConstantResourceId"})
    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            switch (view.getId()) {
                case R.id.left_button:
                    moveLeft = true;
                    break;
                case R.id.right_button:
                    moveRight = true;
                    break;
            }
        } else {
            moveLeft = false;
            moveRight = false;
        }
        view.post(this::changePos);

        return false;
    }

    // Change position of the user.
    public void changePos() {
        float carViewX = carView.getX();
        if (moveLeft) {
            carViewX -= (relativeLayout.getWidth() / (float) NUMBER_OF_CAR_POS);

        } else if (moveRight) {
            carViewX += (relativeLayout.getWidth() / (float) NUMBER_OF_CAR_POS);
        }

        if (carViewX < 0) {
            carViewX = 0;
        }
        if (carViewX > relativeLayout.getWidth() - carView.getWidth()) {
            carViewX = relativeLayout.getWidth() - carView.getWidth();
        }
        carView.setX(carViewX);

    }

    // Collision detection.
    public boolean collisionDetection(View view, int i) {
        int viewX = (int) view.getX();
        int viewY = (int) view.getY();
        int viewRightTop = view.getWidth() + viewX;
        int viewRightBottom = view.getHeight() + viewY;
        int carViewX = (int) carView.getX();
        int carViewY = (int) carView.getY();

        if (carViewX >= viewX && carViewX < viewRightTop && carViewY >= viewY && carViewY <= viewRightBottom && ifPlaying) {
            if(i == 0){
                if (thirdHeart.getVisibility() == VISIBLE) {
                    thirdHeart.setVisibility(INVISIBLE);
                    if(vib){
                        crash_sound.start();
                        vibrator.vibrate(300);
                    }
                } else {
                    if (secondHeart.getVisibility() == VISIBLE) {
                        secondHeart.setVisibility(INVISIBLE);
                        if(vib){
                            crash_sound.start();
                            vibrator.vibrate(300);
                        }
                    } else {
                        firstHeart.setVisibility(INVISIBLE);
                        if(vib){
                            crash_sound.start();
                            vibrator.vibrate(500);
                        }
                        gameOver = true;
                    }
                }
            }else{
                coinCollision = true;
            }
            return true;

        }
        return false;
    }

    // Game loop.
    public void gameLoop() {
        handler.post(() -> {
            if (newRand == 0) {
                final View leftView = new View(FiveLanesActivity.this);
                leftView.setLayoutParams(new RelativeLayout.LayoutParams(relativeLayout.getWidth() / NUMBER_OF_LANES, 250));
                leftView.setBackgroundResource(R.drawable.block);
                relativeLayout.addView(leftView);
                leftView.setX(newRand * (relativeLayout.getWidth() / (float) NUMBER_OF_LANES));
                animate1Y = ObjectAnimator.ofFloat(leftView, "translationY", 0f, relativeLayout.getHeight() + 250);
                animate1Y.setDuration(2500);

                leftView.post(() -> {
                    animate1Y.start();
                    animate1Y.addUpdateListener(valueAnimator -> {
                        if (collisionDetection(leftView, 0)) {
                            animate1Y.cancel();
                        }
                        if(gameOver){
                            animate1Y.pause();
                        }
                    });

                    animate1Y.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ((ViewGroup) leftView.getParent()).removeView(leftView);
                        }
                    });

                });
            } else if (newRand == 1) {
                final View centerLeftView = new View(FiveLanesActivity.this);
                centerLeftView.setLayoutParams(new RelativeLayout.LayoutParams(relativeLayout.getWidth() / NUMBER_OF_LANES, 250));
                centerLeftView.setBackgroundResource(R.drawable.block);
                relativeLayout.addView(centerLeftView);
                centerLeftView.setX(newRand * (relativeLayout.getWidth() / (float) NUMBER_OF_LANES));
                animate2Y = ObjectAnimator.ofFloat(centerLeftView, "translationY", 0f, relativeLayout.getHeight() + 250);
                animate2Y.setDuration(2500);
                centerLeftView.post(() -> {
                    animate2Y.start();
                    animate2Y.addUpdateListener(valueAnimator -> {
                        if (collisionDetection(centerLeftView, 0)) {
                            animate2Y.cancel();
                        }
                        if(gameOver){
                            animate2Y.pause();
                        }
                    });
                    animate2Y.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ((ViewGroup) centerLeftView.getParent()).removeView(centerLeftView);
                        }
                    });
                });
            } else if (newRand == 3) {
                final View centerView = new View(FiveLanesActivity.this);
                centerView.setLayoutParams(new RelativeLayout.LayoutParams(relativeLayout.getWidth() / NUMBER_OF_LANES, 250));
                centerView.setBackgroundResource(R.drawable.block);
                relativeLayout.addView(centerView);
                centerView.setX(newRand * (relativeLayout.getWidth() / (float) NUMBER_OF_LANES));
                animate3Y = ObjectAnimator.ofFloat(centerView, "translationY", 0f, relativeLayout.getHeight() + 250);
                animate3Y.setDuration(2500);
                centerView.post(() -> {
                    animate3Y.start();
                    animate3Y.addUpdateListener(valueAnimator -> {
                        if (collisionDetection(centerView, 0)) {
                            animate3Y.cancel();
                        }
                        if (gameOver) {
                            animate3Y.pause();
                        }
                    });
                    animate3Y.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ((ViewGroup) centerView.getParent()).removeView(centerView);
                        }
                    });
                });
            } else if (newRand == 4) {
                final View centerRightView = new View(FiveLanesActivity.this);
                centerRightView.setLayoutParams(new RelativeLayout.LayoutParams(relativeLayout.getWidth() / NUMBER_OF_LANES, 250));
                centerRightView.setBackgroundResource(R.drawable.block);
                relativeLayout.addView(centerRightView);
                centerRightView.setX(newRand * (relativeLayout.getWidth() / (float) NUMBER_OF_LANES));
                animate4Y = ObjectAnimator.ofFloat(centerRightView, "translationY", 0f, relativeLayout.getHeight() + 250);
                animate4Y.setDuration(2500);
                centerRightView.post(() -> {
                    animate4Y.start();
                    animate4Y.addUpdateListener(valueAnimator -> {
                        if (collisionDetection(centerRightView, 0)) {
                            animate4Y.cancel();
                        }
                        if (gameOver) {
                            animate4Y.pause();
                        }
                    });
                    animate4Y.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ((ViewGroup) centerRightView.getParent()).removeView(centerRightView);
                        }
                    });
                });
            }
            else {
                final View rightView = new View(FiveLanesActivity.this);
                rightView.setLayoutParams(new RelativeLayout.LayoutParams(relativeLayout.getWidth() / NUMBER_OF_LANES, 250));
                rightView.setBackgroundResource(R.drawable.block);
                relativeLayout.addView(rightView);
                rightView.setX(newRand * (relativeLayout.getWidth() / (float) NUMBER_OF_LANES));
                animate5Y = ObjectAnimator.ofFloat(rightView, "translationY", 0f, relativeLayout.getHeight() + 250);
                animate5Y.setDuration(2500);
                rightView.post(() -> {
                    animate5Y.start();
                    animate5Y.addUpdateListener(valueAnimator -> {
                        if (collisionDetection(rightView, 0)) {
                            animate5Y.cancel();
                        }
                        if(gameOver){
                            animate5Y.pause();
                        }
                    });
                    animate5Y.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ((ViewGroup) rightView.getParent()).removeView(rightView);
                        }
                    });
                });
            }
        });
    }

    public void coinLoop(){
        handler.post(() -> {
            if (coinRand == 0) {
                final View coinLeftView = new View(FiveLanesActivity.this);
                coinLeftView.setLayoutParams(new RelativeLayout.LayoutParams(relativeLayout.getWidth() / NUMBER_OF_LANES, 250));
                coinLeftView.setBackgroundResource(R.drawable.coin);
                relativeLayout.addView(coinLeftView);
                coinLeftView.setX(coinRand * (relativeLayout.getWidth() / (float) NUMBER_OF_LANES));
                coinAnim1 = ObjectAnimator.ofFloat(coinLeftView, "translationY", 0f, relativeLayout.getHeight() + 250);
                coinAnim1.setDuration(2500);

                coinLeftView.post(() -> {
                    coinAnim1.start();
                    coinAnim1.addUpdateListener(valueAnimator -> {
                        if (collisionDetection(coinLeftView, 1)) {
                            coinAnim1.cancel();
                        }
                        if(gameOver){
                            coinAnim1.pause();
                        }
                    });

                    coinAnim1.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ((ViewGroup) coinLeftView.getParent()).removeView(coinLeftView);
                        }
                    });

                });
            } else if (coinRand == 1) {
                final View coinCenterLeftView = new View(FiveLanesActivity.this);
                coinCenterLeftView.setLayoutParams(new RelativeLayout.LayoutParams(relativeLayout.getWidth() / NUMBER_OF_LANES, 250));
                coinCenterLeftView.setBackgroundResource(R.drawable.coin);
                relativeLayout.addView(coinCenterLeftView);
                coinCenterLeftView.setX(coinRand * (relativeLayout.getWidth() / (float) NUMBER_OF_LANES));
                coinAnim2 = ObjectAnimator.ofFloat(coinCenterLeftView, "translationY", 0f, relativeLayout.getHeight() + 250);
                coinAnim2.setDuration(2500);
                coinCenterLeftView.post(() -> {
                    coinAnim2.start();
                    coinAnim2.addUpdateListener(valueAnimator -> {
                        if (collisionDetection(coinCenterLeftView, 1)) {
                            coinAnim2.cancel();
                        }
                        if(gameOver){
                            coinAnim2.pause();
                        }
                    });
                    coinAnim2.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ((ViewGroup) coinCenterLeftView.getParent()).removeView(coinCenterLeftView);
                        }
                    });
                });
            } else if (coinRand == 3) {
                final View coinCenterView = new View(FiveLanesActivity.this);
                coinCenterView.setLayoutParams(new RelativeLayout.LayoutParams(relativeLayout.getWidth() / NUMBER_OF_LANES, 250));
                coinCenterView.setBackgroundResource(R.drawable.coin);
                relativeLayout.addView(coinCenterView);
                coinCenterView.setX(coinRand * (relativeLayout.getWidth() / (float) NUMBER_OF_LANES));
                coinAnim3 = ObjectAnimator.ofFloat(coinCenterView, "translationY", 0f, relativeLayout.getHeight() + 250);
                coinAnim3.setDuration(2500);
                coinCenterView.post(() -> {
                    coinAnim3.start();
                    coinAnim3.addUpdateListener(valueAnimator -> {
                        if (collisionDetection(coinCenterView, 1)) {
                            coinAnim3.cancel();
                        }
                        if (gameOver) {
                            coinAnim3.pause();
                        }
                    });
                    coinAnim3.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ((ViewGroup) coinCenterView.getParent()).removeView(coinCenterView);
                        }
                    });
                });
            } else if (coinRand == 4) {
                final View coinCenterRightView = new View(FiveLanesActivity.this);
                coinCenterRightView.setLayoutParams(new RelativeLayout.LayoutParams(relativeLayout.getWidth() / NUMBER_OF_LANES, 250));
                coinCenterRightView.setBackgroundResource(R.drawable.coin);
                relativeLayout.addView(coinCenterRightView);
                coinCenterRightView.setX(coinRand * (relativeLayout.getWidth() / (float) NUMBER_OF_LANES));
                coinAnim4 = ObjectAnimator.ofFloat(coinCenterRightView, "translationY", 0f, relativeLayout.getHeight() + 250);
                coinAnim4.setDuration(2500);
                coinCenterRightView.post(() -> {
                    coinAnim4.start();
                    coinAnim4.addUpdateListener(valueAnimator -> {
                        if (collisionDetection(coinCenterRightView, 1)) {
                            coinAnim4.cancel();
                        }
                        if (gameOver) {
                            coinAnim4.pause();
                        }
                    });
                    coinAnim4.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ((ViewGroup) coinCenterRightView.getParent()).removeView(coinCenterRightView);
                        }
                    });
                });
            }
            else {
                final View coinRightView = new View(FiveLanesActivity.this);
                coinRightView.setLayoutParams(new RelativeLayout.LayoutParams(relativeLayout.getWidth() / NUMBER_OF_LANES, 250));
                coinRightView.setBackgroundResource(R.drawable.coin);
                relativeLayout.addView(coinRightView);
                coinRightView.setX(coinRand * (relativeLayout.getWidth() / (float) NUMBER_OF_LANES));
                coinAnim5 = ObjectAnimator.ofFloat(coinRightView, "translationY", 0f, relativeLayout.getHeight() + 250);
                coinAnim5.setDuration(2500);
                coinRightView.post(() -> {
                    coinAnim5.start();
                    coinAnim5.addUpdateListener(valueAnimator -> {
                        if (collisionDetection(coinRightView, 1)) {
                            coinAnim5.cancel();
                        }
                        if(gameOver){
                            coinAnim5.pause();
                        }
                    });
                    coinAnim5.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ((ViewGroup) coinRightView.getParent()).removeView(coinRightView);
                        }
                    });
                });
            }
        });
    }

    // Method that start the game over activity when the game ends.
    public void startGameOverActivity() {
        Intent intent = new Intent(this, GameOverActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("numOfLanes", NUMBER_OF_LANES);
        intent.putExtra("vib", vib);
        intent.putExtra("tilt", tilt);
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


    @Override
    public void onResume() {
        ifPlaying = true;
        setUIVisibility();
        if(tilt){
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        super.onResume();
    }

    @Override
    public void onStop() {
        ifPlaying = false;
        if(tilt){
            sensorManager.unregisterListener(this, sensor);
        }
        super.onStop();
    }

    @Override
    public void onPause() {
        ifPlaying = false;
        if(tilt){
            sensorManager.unregisterListener(this, sensor);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if(tilt){
            sensorManager.unregisterListener(this, sensor);
        }
        super.onDestroy();
    }


    // Method for tilt sensors.
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double xSensor = sensorEvent.values[0];
        if(xSensor > 3 && xSensor < 5){
            moveLeft = true;
            changePos();
            moveLeft = false;
        }
        if(xSensor >= -5 && xSensor < -3){
            moveRight = true;
            changePos();
            moveRight = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
