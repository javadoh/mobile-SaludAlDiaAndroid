package com.orugga.yapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

class AppRater {
    private final static int TIMES_SHOWED_UNTIL_NOT_SHOW_AGAIN = 5; //Min number of times showed
    private final static int LAUNCHES_UNTIL_PROMPT = 5;//Min number of launches

    static void app_launched(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("dontshowagain", false)) { return ; }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            // set the launch counter to 0 again to wait 5 launches more
            editor.putLong("launch_count", 0);

            // Increment times showed counter
            int times_showed = prefs.getInt("times_showed", 0) + 1;
            editor.putInt("times_showed", times_showed);

            if (times_showed >= TIMES_SHOWED_UNTIL_NOT_SHOW_AGAIN)
                editor.putBoolean("dontshowagain", true);

            showRateDialog(mContext, editor);
        }

        editor.apply();
    }

    private static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
        final Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.dialog_rate_app);
        dialog.getWindow().setBackgroundDrawable(null);

        final LinearLayout starLinearLayout = dialog.findViewById(R.id.starsLinearLayout);

        for (int i = 0; i < 5; i++) {
            CheckBox star = new CheckBox(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(70, 70);
//            params.weight = 1;
            params.setMargins(8,8,8,8);
            star.setLayoutParams(params);
            star.setButtonDrawable(null);
            star.setBackgroundResource(R.drawable.bg_star_check);
            starLinearLayout.addView(star);
            star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int childSize = starLinearLayout.getChildCount();
                    CheckBox actualView = (CheckBox) v;
                    boolean childrenFound = false;
                    for (int j = 0; j < childSize; j++) {
                        CheckBox checkBox = (CheckBox) starLinearLayout.getChildAt(j);
                        checkBox.setChecked(!childrenFound);
                        if (checkBox == actualView) {
                            childrenFound = true;
                        }
                    }
                }
            });
        }

        TextView btnRate = dialog.findViewById(R.id.btnRateNow);
        btnRate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editor.putBoolean("dontshowagain", true);
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mContext.getPackageName())));
                dialog.dismiss();
            }
        });

        TextView btnNotNow = dialog.findViewById(R.id.btnNotNow);
        btnNotNow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
