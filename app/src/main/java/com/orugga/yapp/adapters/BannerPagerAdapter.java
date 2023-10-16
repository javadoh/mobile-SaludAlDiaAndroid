package com.orugga.yapp.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.orugga.yapp.R;
import com.orugga.yapp.interfaces.JsonArrayResponse;
import com.orugga.yapp.requests.DownloadImageRequest;
import com.orugga.yapp.requests.GetBannersRequest;

import java.util.ArrayList;

/**
 * Created by Alexis on 13/10/2017.
 */

public class BannerPagerAdapter extends PagerAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private ArrayList<Drawable> mResources;
    private ArrayList<String> downloadUrls;

    private static final int LOOPS = 1;

    public int getFirstPosition(){return 0;}

    public BannerPagerAdapter(Context context, final int[] resources) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResources = new ArrayList<>();
        downloadUrls = new ArrayList<>();
        GetBannersRequest.getBanners(context, new JsonArrayResponse() {
            @Override
            public void onSuccess(JsonArray response) {
                for (int i = 0; i < response.size(); i++) {
                    JsonObject banner = response.get(i).getAsJsonObject();
                    downloadUrls.add(banner.get("full_path_picture_800").getAsString());
                }
                notifyDataSetChanged();
            }

            @Override
            public void onError(JsonObject response, Exception e) {
                if (e != null) {
                    Crashlytics.logException(e);
                }
            }
        });
        for (int resource : resources) {
            mResources.add(context.getResources().getDrawable(resource));
        }
    }

    @Override
    public int getCount() {
        return (mResources.size() + downloadUrls.size()) * LOOPS;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);

        ImageView imageView = itemView.findViewById(R.id.imageView);
        int modPosition = position % (mResources.size() + downloadUrls.size());
        if (modPosition < mResources.size()){
            imageView.setImageDrawable(mResources.get(modPosition));
        } else {
            DownloadImageRequest.downloadImage(mContext, downloadUrls.get(modPosition - mResources.size()), imageView, R.drawable.place_holder_banner);
        }

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout) object);
    }
}
