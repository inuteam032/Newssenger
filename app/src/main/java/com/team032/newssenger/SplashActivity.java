package com.team032.newssenger;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class SplashActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    private FirebaseRemoteConfig firebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        hideActionBar();    // 처음 진입화면(splash)에서 ActionBar 가림

//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        getSupportActionBar().setCustomView(R.layout.action_bar);

        // 상단 Status Bar 없애기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        linearLayout = (LinearLayout)findViewById(R.id.splashActivity_linearLayout);

        // 원격 구성 개체 인스턴스를 가져오고, 캐시를 빈번하게 새로고칠 수 있도록 설정
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        firebaseRemoteConfig.setConfigSettings(configSettings);

        // XML 파일에서 in-app 기본값을 설정
//        firebaseRemoteConfig.setDefaults(R.xml.default_config);

        // 서버에서 값을 가져와서(fetch) 앱에 적용(activateFetched)
        firebaseRemoteConfig.fetch(0)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            firebaseRemoteConfig.activateFetched();
                        } else {
                        }
                        displayMessage();
                    }
                });
    }

    // ActionBar를 안보이게 함
    private void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.hide();
        }
    }

    void displayMessage() {
        // default_config.xml에서 값을 받아옴
        String splash_background = firebaseRemoteConfig.getString("splash_background");
        boolean caps = firebaseRemoteConfig.getBoolean("splash_message_caps");
        String splash_message = firebaseRemoteConfig.getString("splash_message");

        // 값 적용
//      linearLayout.setBackgroundColor(Color.parseColor(splash_background));
        if (caps) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(splash_message).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.create().show();
        }

        else {
            startActivity(new Intent(this, LoginActivity.class));
            finish(); // SplashActivity를 닫음
        }
    }
}