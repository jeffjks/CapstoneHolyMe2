package com.example.user_pc.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private static final String TAG = "AnonymousAuth";
    private static String loginTime, current;
    private boolean flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*********************************************/
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        current = DateFormat.getDateInstance().format(new Date());
        //loginTime = date.format(date);

        mAuth = FirebaseAuth.getInstance();
        signInAnonymously();



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

    /*
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }
    */
    private void signInAnonymously() {
        // [START signin_anonymously]
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");

                            /********************************************************/
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            final DatabaseReference myRef = database.getReference("USER");

                            myRef.child(mAuth.getCurrentUser().getUid() + "/lastLogin").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String text = dataSnapshot.getValue(String.class);

                                    if(text == null) {
                                        Log.d(TAG, "AAAAA");
                                        myRef.child(mAuth.getCurrentUser().getUid() + "/lastLogin").setValue(current);

                                        myRef.child(mAuth.getCurrentUser().getUid() + "/MACRO/wifi").setValue(0);
                                        myRef.child(mAuth.getCurrentUser().getUid() + "/MACRO/brightness").setValue(0);
                                        myRef.child(mAuth.getCurrentUser().getUid() + "/MACRO/weather").setValue(0);
                                        myRef.child(mAuth.getCurrentUser().getUid() + "/MACRO/appPush").setValue(0);
                                        myRef.child(mAuth.getCurrentUser().getUid() + "/MACRO/message").setValue(0);
                                        flag = true;
                                    }
                                    else {
                                        myRef.child(mAuth.getCurrentUser().getUid() + "/lastLogin").setValue(current);
                                        flag = false;
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });




                            /********************************각 매크로 별로 넣어주기 setvalue(0)도 넣어주기***********************/
                            ///FirebaseDatabase database = FirebaseDatabase.getInstance();
                            ///DatabaseReference myRef = database.getReference("USER");

                            ///myRef.child(mAuth.getCurrentUser().getUid() + "/MACRO/" + macroNumber).setValue(1);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                        }
                    }
                });
        // [END signin_anonymously]
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