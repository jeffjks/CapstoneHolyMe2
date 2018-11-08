package com.example.user_pc.myapplication;

import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.buttonWIFIon).setOnClickListener(this);
        findViewById(R.id.buttonWIFIoff).setOnClickListener(this);
        findViewById(R.id.buttonBrightnessOn).setOnClickListener(this);
        findViewById(R.id.buttonBrightnessOff).setOnClickListener(this);
        findViewById(R.id.buttonKakao).setOnClickListener(this);
        findViewById(R.id.buttonWeather).setOnClickListener(this);
        findViewById(R.id.buttonAppPushListener).setOnClickListener(this);
        findViewById(R.id.buttonMessage).setOnClickListener(this);
        findViewById(R.id.buttonAlarm).setOnClickListener(this);


    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.buttonWIFIon :
                startActivity(new Intent(MainActivity.this, wifiOnActivity.class));
                break;

            case R.id.buttonWIFIoff :
                startActivity(new Intent(MainActivity.this, wifiOffActivity.class));
                break;

            case R.id.buttonBrightnessOn :
                startActivity(new Intent(MainActivity.this, brightnessOnActivity.class));
                break;

            case R.id.buttonBrightnessOff :
                startActivity(new Intent(MainActivity.this, brightnessOffActivity.class));
                break;

            case R.id.buttonWeather :
                startActivity(new Intent(MainActivity.this, weatherActivity.class));
                break;

            case R.id.buttonKakao :
                startActivity(new Intent(MainActivity.this, kakaoActvity.class));
                break;

            case R.id.buttonAppPushListener :
                startActivity(new Intent(MainActivity.this, appPushlistenerActivity.class));
                break;

            case R.id.buttonMessage :
                startActivity(new Intent(MainActivity.this, messageActivity.class));
                break;

            case R.id.buttonAlarm :
                startActivity(new Intent(MainActivity.this, alarmActivity.class));
                break;
        }
    }
}
