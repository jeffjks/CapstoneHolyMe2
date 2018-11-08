package com.example.user_pc.myapplication;

import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.Set;

//soundPool
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.View;
import android.widget.Button;

public class appPushlistenerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_pushlistener);
        boolean isPermissionAllowed = isNotiPermissionAllowed();

        if(!isPermissionAllowed) {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }

        //soundpool---------------------------------------------------------------
        //나중에 이벤트 핸들러 작성하여 코드 복붙 가능하도록
        Button b = (Button)findViewById(R.id.buttonSound);

        final SoundPool sp = new SoundPool(1,         // 최대 음악파일의 개수
                AudioManager.STREAM_MUSIC, // 스트림 타입
                0);        // 음질 - 기본값:0

        // 재생하고자하는 음악을 미리 준비
        final int soundID = sp.load(this, // 현재 화면의 제어권자
                R.raw.ouu,    // 음악 파일
                1);        // 우선순위

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp.play(soundID, // 준비한 soundID
                        1,         // 왼쪽 볼륨 float 0.0(작은소리)~1.0(큰소리)
                        1,         // 오른쪽볼륨 float
                        0,         // 우선순위 int
                        0,     // 반복회수 int -1:무한반복, 0:반복안함
                        0.5f);    // 재생속도 float 0.5(절반속도)~2.0(2배속)
                // 음악 재생
            }
        });
    }



    private boolean isNotiPermissionAllowed() {
        Set<String> notiListenerSet = NotificationManagerCompat.getEnabledListenerPackages(this);
        String myPackageName = getPackageName();

        for(String packageName : notiListenerSet) {
            if(packageName == null) {
                continue;
            }
            if(packageName.equals(myPackageName)) {
                return true;
            }
        }

        return false;
    }
}
