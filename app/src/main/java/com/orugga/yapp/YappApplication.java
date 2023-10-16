package com.orugga.yapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.livefront.bridge.Bridge;
import com.livefront.bridge.SavedStateHandler;
import com.orm.SugarApp;

import icepick.Icepick;
import io.fabric.sdk.android.Fabric;

/**
 * Created by Alexis on 15/12/2017.
 */

public class YappApplication extends SugarApp{

    @Override
    public void onCreate() {
        Fabric.with(this, new Crashlytics());
        Bridge.initialize(this, new SavedStateHandler() {
            @Override
            public void saveInstanceState(@NonNull Object target, @NonNull Bundle state) {
                Icepick.saveInstanceState(target, state);
            }

            @Override
            public void restoreInstanceState(@NonNull Object target, @Nullable Bundle state) {
                Icepick.restoreInstanceState(target, state);
            }
        });
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
