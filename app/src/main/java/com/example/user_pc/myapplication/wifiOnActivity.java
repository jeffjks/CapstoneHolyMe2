package com.example.user_pc.myapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.net.wifi.WifiManager.WIFI_STATE_ENABLED;

public class wifiOnActivity extends AppCompatActivity {
    private ArrayList<String> nativeFunctionList = new ArrayList<>();

    //private GpsInfo gps;
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private final int DEFAULT_REMAINING_TIME = 60000;
    private final double MINIMUM_DISTANCE = 0.00005; // 0.55km
    private final int REMAINING_TIME_RATE = 1000000;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;

    private TextView wifiSSIDText, latitudeText, longitudeText, remaingingTimeText;
    private Switch wifiSwitch, brightnessSwitch;
    private WifiInfo wifiInfo = null;

    private String ssid = "\"G6_4917\""; // iptime65 // \"G6_4917\"
    private String bssid = null;

    private GpsInfo gps;

    private double latitude = 0, longitude = 0;
    private double des_latitude, des_longitude;
    private int remainingTime = DEFAULT_REMAINING_TIME;

    private WindowManager.LayoutParams save;
    private WindowManager.LayoutParams params;
    private float brightness;

    WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_on_activitiy);
        gps = new GpsInfo(wifiOnActivity.this);

        params = getWindow().getAttributes();
        save = getWindow().getAttributes();
        brightness = save.screenBrightness;

        latitudeText = findViewById(R.id.latitudeText);
        longitudeText = findViewById(R.id.longitudeText);
        remaingingTimeText = findViewById(R.id.remainingTimeText);
        wifiSwitch = findViewById(R.id.wifiSwitch);
        brightnessSwitch = findViewById(R.id.brightnessSwitch);
        wifiSSIDText = findViewById(R.id.wifiSSID);
        final Button registerButton = findViewById(R.id.registerHereButton);
        final Button cancelWifiBrightnessButton = findViewById(R.id.cancelWifiBrightnessButton);

        des_latitude = 37.0;
        des_longitude = -122.0;

        GpsInfo gps = new GpsInfo(wifiOnActivity.this);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        final timeThread thread = new timeThread(gps);
        thread.start();
        wifiThread wThread = new wifiThread();
        wThread.start();

        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                GpsInfo gps;
                gps = new GpsInfo(wifiOnActivity.this);

                wifiSSIDText.setText(wifiInfo.getSSID());

                if (!isPermission) {
                    callPermission();
                    return;
                }

                if (gps.isGetLocation()) { // GPS 사용유무 체크
                    des_latitude = gps.getLatitude(); //37.4; //gps.getLatitude(); (37.5)
                    des_longitude = gps.getLongitude(); // -122.0; //gps.getLongitude(); (126.9)
                    Log.d("MSG", "set destination here");
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                    latitudeText.setText("위도: "+String.valueOf(latitude));
                    longitudeText.setText("경도: "+String.valueOf(longitude));
                    Toast.makeText(wifiOnActivity.this, "현재 위치가 목적지로 등록되었습니다.", Toast.LENGTH_SHORT).show();

                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    final DatabaseReference myRef = database.getReference("USER");

                    myRef.child(mAuth.getCurrentUser().getUid() + "/GPS/latitude").setValue(latitude);
                    myRef.child(mAuth.getCurrentUser().getUid() + "/GPS/longitude").setValue(longitude);
                    if (wifiSwitch.isChecked())
                        myRef.child(mAuth.getCurrentUser().getUid() + "/MACRO/" + "wifi").setValue(1);
                    else
                        myRef.child(mAuth.getCurrentUser().getUid() + "/MACRO/" + "wifi").setValue(0);
                    if (brightnessSwitch.isChecked())
                        myRef.child(mAuth.getCurrentUser().getUid() + "/MACRO/" + "brightness").setValue(1);
                    else
                        myRef.child(mAuth.getCurrentUser().getUid() + "/MACRO/" + "brightness").setValue(0);
                }
                else {
                    gps.showSettingsAlert();
                }
            }
        });

        cancelWifiBrightnessButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                thread.interrupt();
                Toast.makeText(getApplicationContext(), "와이파이/밝기 매크로가 취소되었습니다.", Toast.LENGTH_SHORT).show();
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference myRef = database.getReference("USER");
                myRef.child(mAuth.getCurrentUser().getUid() + "/MACRO/" + "wifi").setValue(0);
                myRef.child(mAuth.getCurrentUser().getUid() + "/MACRO/" + "brightness").setValue(0);
            }
        });

        callPermission();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            latitudeText.setText("위도: "+String.valueOf(latitude));
            longitudeText.setText("경도: "+String.valueOf(longitude));
            remaingingTimeText.setText("다음 시간: "+String.valueOf(remainingTime));
            //wifiSSIDText.setText(ssid);
        }
    };

    public class timeThread extends Thread {

        GpsInfo gps;

        timeThread(GpsInfo gps) {
            this.gps = gps;
        }
        @Override
        public void run() {
            while(true){
                if (!currentThread().isInterrupted()) {
                    Message msg = new Message();
                    handler.sendMessage(msg);
                    Log.d("timeThread", "msg: " + remainingTime);

                    remainingTime = getGPSInfo();
                }

                try {
                    Thread.sleep(remainingTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class wifiThread extends Thread {
        @Override
        public void run() {
            while(true){
                Message msg = new Message();
                handler.sendMessage(msg);
                Log.d("wifiThread", "msg");

                wifiInfo = wifiManager.getConnectionInfo();
                if (wifiManager.getWifiState() == WIFI_STATE_ENABLED) {
                    if (!wifiInfo.getSSID().equals(ssid)) {
                        wifiManager.setWifiEnabled(false);
                        Log.d("wifiThread", "turnOff wifi");
                        Log.d("wifiThread", wifiInfo.getSSID());
                    }
                }

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private int getGPSInfo() {
        if (!isPermission) {
            callPermission();
            return DEFAULT_REMAINING_TIME;
        }

        if (gps.isGetLocation()) { // GPS 사용유무 체크
            double distance;
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            distance = Math.sqrt(Math.pow((des_latitude - latitude), 2) + Math.pow((des_longitude - longitude), 2));
            remainingTime = (int) (distance*REMAINING_TIME_RATE/2);


            if (distance < MINIMUM_DISTANCE) {
                if (wifiManager != null)
                    wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    bssid = wifiInfo.getBSSID();
                }
                if (brightnessSwitch.isChecked()) {
                    params.screenBrightness = brightness;
                    getWindow().setAttributes(params);
                    Log.d("brightness1", params.screenBrightness + "");
                }

                if (wifiSwitch.isChecked()) {
                    wifiManager.setWifiEnabled(true);
                    Message message = handler.obtainMessage();
                    handler.sendMessage(message);
                    Log.d("distance1", "" + distance);
                }
                return DEFAULT_REMAINING_TIME;
            }
            else {
                if (brightnessSwitch.isChecked()) {
                    params.screenBrightness = 0.1f;
                    getWindow().setAttributes(params);
                    Log.d("brightness2",  brightness+"");
                }
                if (wifiSwitch.isChecked()) {
                    Message message = handler.obtainMessage();
                    handler.sendMessage(message);
                    wifiManager.setWifiEnabled(false);
                    Log.d("distance2", "" + distance);
                    Log.d("remainingTime", "" + remainingTime);
                }
                return remainingTime;
            }
        }
        else {
            return DEFAULT_REMAINING_TIME;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessFineLocation = true;

        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            isAccessCoarseLocation = true;
        }

        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }


    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }
}

class GpsInfo extends Service implements LocationListener {
    private final Context mContext;

    boolean isGPSEnabled = false; // 현재 GPS 사용유무
    boolean isNetworkEnabled = false; // 네트워크 사용유무
    boolean isGetLocation = false; // GPS 상태값

    Location location;
    double lat; // 위도
    double lon; // 경도

    // 최소 GPS 정보 업데이트 거리
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    // 최소 GPS 정보 업데이트 시간 (ms)
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    protected LocationManager locationManager;

    public GpsInfo(Context context) {
        this.mContext = context;
        getLocation();
    }

    @TargetApi(23)
    public Location getLocation() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(
                        mContext, android.Manifest.permission.ACCESS_FINE_LOCATION )
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                        mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            return null;
        }

        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
            } else {
                this.isGetLocation = true;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            // 위도 경도 저장
                            lat = location.getLatitude();
                            lon = location.getLongitude();
                        }
                    }
                }

                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                lat = location.getLatitude();
                                lon = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GpsInfo.this);
        }
    }

    public double getLatitude(){
        if(location != null){
            lat = location.getLatitude();
        }
        return lat;
    }

    public double getLongitude(){
        if(location != null){
            lon = location.getLongitude();
        }
        return lon;
    }

    public boolean isGetLocation() {
        return this.isGetLocation;
    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        alertDialog.setTitle("GPS 사용유무셋팅");
        alertDialog.setMessage("GPS 셋팅이 되지 않았을수도 있습니다. \n 설정창으로 가시겠습니까?");

        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContext.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }
}