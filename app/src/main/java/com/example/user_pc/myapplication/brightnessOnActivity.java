package com.example.user_pc.myapplication;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class brightnessOnActivity extends AppCompatActivity implements View.OnClickListener {

    private WindowManager.LayoutParams save;
    private WindowManager.LayoutParams params;
    private float brightness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brightness_on);

        params = getWindow().getAttributes();
        save = getWindow().getAttributes();
        brightness = save.screenBrightness;

        //Settings.System.putInt(getContentResolver(), "screen_brightness", 180);

        findViewById(R.id.button).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button :
                params.screenBrightness = 0.1f;
                getWindow().setAttributes(params);
                Log.d("brightness1",  params.screenBrightness+"");
                break;

            case R.id.button2 :
                params.screenBrightness = brightness;
                getWindow().setAttributes(params);
                Log.d("brightness2",  brightness+"");
                break;
        }
    }
}