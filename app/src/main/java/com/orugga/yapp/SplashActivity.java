package com.orugga.yapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import static com.orugga.yapp.Constants.SharedPrefsKeys.INFO_FIRST_TIME_IN_APP;
import static com.orugga.yapp.helpers.SessionHelper.isUserLogedIn;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_TIME = 2500;
    private boolean isActive = true;

    private Runnable mHandlerTask = new Runnable() {
        @Override
        public void run() {
            if (!isActive) return;
            SharedPreferences pref = getSharedPreferences(Constants.SharedPrefsKeys.INFO, Context.MODE_PRIVATE);
            boolean firstTime = pref.getBoolean(INFO_FIRST_TIME_IN_APP, true);
            if (firstTime) {
                startActivity(new Intent(SplashActivity.this, TourActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, isUserLogedIn(getApplicationContext()) ? MainHomeActivity.class : LoginActivity.class));
            }
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        String version = "";
        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TextView txtVersion = findViewById(R.id.txtVersion);
        txtVersion.setText(version);

        Handler handler = new Handler();
        handler.postDelayed(mHandlerTask, SPLASH_TIME);
    }

    @Override
    protected void onDestroy() {
        isActive = false;
        super.onDestroy();
    }


}