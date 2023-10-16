package com.orugga.yapp.adapters;


import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orugga.yapp.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class TourItemAdapter extends PagerAdapter {

    private Context context;
    private int[] messages = {
            R.string.tour_1,
            R.string.tour_2,
            R.string.tour_3,
            R.string.tour_4
    };
    private int[] images = {
            R.drawable.onboarding_1,
            R.drawable.onboarding_2,
            R.drawable.onboarding_3,
            R.drawable.onboarding_4
    };

    public TourItemAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = View.inflate(context, R.layout.tour_item, null);

        TextView message = view.findViewById(R.id.tour_message);
        Typeface tf = Typeface.createFromAsset(context.getAssets(), "font/open_sans_bold.ttf");
        message.setTypeface(tf, Typeface.NORMAL);
        message.setText(context.getString(messages[position]));
        ((ImageView) view.findViewById(R.id.tour_image)).setImageResource(images[position]);
        container.addView(view);

        return view;
    }


    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }


}
