package com.example.user_pc.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import android.app.Notification;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class appPushlistenerService extends NotificationListenerService {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("NotificationListener", "[snowdeer] onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("NotificationListener", "[snowdeer] onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("NotificationListener", "[snowdeer] onDestroy()");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i("NotificationListener", "[snowdeer] onNotificationPosted() - " + sbn.toString());
        Log.i("NotificationListener", "[snowdeer] PackageName:" + sbn.getPackageName());
        Log.i("NotificationListener", "[snowdeer] PostTime:" + sbn.getPostTime());

        Notification notificatin = sbn.getNotification();
        Bundle extras = notificatin.extras;
        String title = extras.getString(Notification.EXTRA_TITLE);
        int smallIconRes = extras.getInt(Notification.EXTRA_SMALL_ICON);
        Bitmap largeIcon = ((Bitmap) extras.getParcelable(Notification.EXTRA_LARGE_ICON));
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);

        Log.i("NotificationListener", "[snowdeer] Title:" + title);
        Log.i("NotificationListener", "[snowdeer] Text:" + text);
        Log.i("NotificationListener", "[snowdeer] Sub Text:" + subText);

        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("USER");
        //myRef.child(mAuth.getCurrentUser().getUid() + "/appPushMessage/" + "title").setValue(1);

        Map<String, Object> update = new HashMap<>();

        String key = myRef.child(mAuth.getCurrentUser().getUid() + "/appPushMessage").push().getKey();

        update.put("/appPushMessage/" + key + "/title", title);
        update.put("/appPushMessage/" + key + "/text", text);
        update.put("/appPushMessage/" + key + "/subText", subText);

        //myRef.updateChildren(update);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i("NotificationListener", "[snowdeer] onNotificationRemoved() - " + sbn.toString());
    }
}