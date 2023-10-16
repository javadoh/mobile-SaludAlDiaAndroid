package com.orugga.yapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orugga.yapp.adapters.TourItemAdapter;

public class TourActivity extends AppCompatActivity {

    private static final int CANT_TOUR_ITEM = 4;
    private LinearLayout dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour);

        ViewPager pager = findViewById(R.id.pager);
        dots = findViewById(R.id.dots);
        final TextView txtSaltar = findViewById(R.id.txtSaltarIntro);

        pager.setAdapter(new TourItemAdapter(getApplicationContext()));
        pager.setOffscreenPageLimit(3);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateDots(position);
                if (position == CANT_TOUR_ITEM - 1)
                    txtSaltar.setText(R.string.comenzar);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        txtSaltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences(Constants.SharedPrefsKeys.INFO, MODE_PRIVATE).edit();
                editor.putBoolean(Constants.SharedPrefsKeys.INFO_FIRST_TIME_IN_APP, false);
                editor.apply();
                startActivity(new Intent(TourActivity.this, LoginActivity.class));
            }
        });
    }

    private void updateDots(int position) {
        dots.removeAllViews();
        for (int i = 0; i < CANT_TOUR_ITEM; i++) {
            dots.addView(getLayoutInflater().inflate(i == position ? R.layout.pager_dot_on : R.layout.pager_dot_off, dots, false));
        }

    }


}
