package com.example.user_pc.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

public class messageActivity extends AppCompatActivity {
    private Bundle bundle;
    private WeatherData weatherData;
    private String message;
    private int hour;
    private int minute;

    Button buttonSend;
    EditText textPhoneNo;
    EditText textSMS;

    static final int SMS_SEND_PERMISSON = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        buttonSend = (Button) findViewById(R.id.buttonSend);
        textPhoneNo = (EditText) findViewById(R.id.editTextPhoneNo);
        textSMS = (EditText) findViewById(R.id.editTextSMS);

        bundle = getIntent().getExtras();

        if (bundle != null) {
            weatherData = bundle.getParcelable("weatherData");
            //message = weatherData.getMessage();
            hour = weatherData.getHour();
            minute = weatherData.getMinute();

            textSMS.setText(message);
        }

        //권한이 부여되어 있는지 확인
        int permissonCheck= ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);

        if(permissonCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "SMS 송신권한 있음", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "SMS 송신권한 없음", Toast.LENGTH_SHORT).show();
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                Toast.makeText(getApplicationContext(), "SMS 권한이 필요합니다", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_SEND_PERMISSON);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_SEND_PERMISSON);
            }
        }

        //버튼 클릭이벤트
        buttonSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //입력한 값을 가져와 변수에 담는다
                String phoneNo = textPhoneNo.getText().toString();
                String sms = textSMS.getText().toString();

                try {
                    //전송
                    if (bundle == null) {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phoneNo, null, sms, null, null);
                        Toast.makeText(getApplicationContext(), "전송 완료!", Toast.LENGTH_LONG).show();
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        final DatabaseReference myRef = database.getReference("USER");
                        myRef.child(mAuth.getCurrentUser().getUid() + "/MACRO/" + "message").setValue(1);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "예약 완료!", Toast.LENGTH_LONG).show();
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        final DatabaseReference myRef = database.getReference("USER");
                        myRef.child(mAuth.getCurrentUser().getUid() + "/MACRO/" + "weather").setValue(1);
                        myRef.child(mAuth.getCurrentUser().getUid() + "/MACRO/" + "message").setValue(1);
                        new MessageReservation(message, hour, minute, phoneNo, sms);
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "SMS failed, please try again later!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }
}

class MessageReservation {
    static boolean activate = true;
    String message, phoneNo, sms;
    int hour;
    int minute;
    messageThread thread = new messageThread();

    MessageReservation(String message, int hour, int minute, String phoneNo, String sms) {
        this.message = message;
        this.hour = hour;
        this.minute = minute;
        this.phoneNo = phoneNo;
        this.sms = sms;
        activate = true;
        thread.start();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        }
    };

    public class messageThread extends Thread {
        messageThread() {
        }

        @Override
        public void run() {
            while(!thread.isInterrupted()) {
                if (activate) {
                    Message msg = new Message();
                    handler.sendMessage(msg);
                    long now = System.currentTimeMillis();
                    Calendar calendar = Calendar.getInstance();
                    int hour_now = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute_now = calendar.get(Calendar.MINUTE);

                    if (hour == hour_now && minute == minute_now) {
                        Log.d("msgTest", "send message");
                        weatherNow(0);
                        //SmsManager smsManager = SmsManager.getDefault();
                        //smsManager.sendTextMessage(phoneNo, null, weatherNow(0) + "\n" + sms, null, null);
                        //thread.interrupt();

                        try {
                            Thread.sleep(70000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            Thread.sleep(10000);
                            Log.d("msgTest", "message Thread");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    Thread.interrupted();
                }
            }
        }
    }

    public void weatherNow(int city) {
        WeatherAsyncTask weatherAsyncTask = new WeatherAsyncTask(sms, phoneNo);
        switch(city) {
            case 0: // 서울
                weatherAsyncTask.execute("https://weather.naver.com/rgn/cityWetrCity.nhn?cityRgnCd=CT001013");
                break;
            case 1: // 강릉
                weatherAsyncTask.execute("https://weather.naver.com/rgn/cityWetrCity.nhn?cityRgnCd=CT004001");
                break;
            case 2: // 광주
                weatherAsyncTask.execute("https://weather.naver.com/rgn/cityWetrCity.nhn?cityRgnCd=CT011005");
                break;
            case 3: // 대구
                weatherAsyncTask.execute("https://weather.naver.com/rgn/cityWetrCity.nhn?cityRgnCd=CT007007");
                break;
            case 4: // 대전
                weatherAsyncTask.execute("https://weather.naver.com/rgn/cityWetrCity.nhn?cityRgnCd=CT006005");
                break;
            case 5: // 부산
                weatherAsyncTask.execute("https://weather.naver.com/rgn/cityWetrCity.nhn?cityRgnCd=CT008008");
                break;
            case 6: // 제주
                weatherAsyncTask.execute("https://weather.naver.com/rgn/cityWetrCity.nhn?cityRgnCd=CT012005");
                break;
            default: // 서울
                weatherAsyncTask.execute("https://weather.naver.com/rgn/cityWetrCity.nhn?cityRgnCd=CT001013");
        }
    }
}