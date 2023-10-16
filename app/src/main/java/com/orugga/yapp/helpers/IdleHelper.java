package com.orugga.yapp.helpers;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Alexis on 18/10/2017.
 */

public class IdleHelper {
    public static void disableForAWhile(final Activity activity, final View view) {
        view.setEnabled(false);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (activity.getApplicationContext() != null)
                            view.setEnabled(true);
                    }
                });
            }
        }, 500);
    }

    public static void ocultarTeclado(Activity activity) {
        View activityView = activity.getCurrentFocus();
        if (activityView != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(activityView.getWindowToken(), 0);
        }
    }
}
