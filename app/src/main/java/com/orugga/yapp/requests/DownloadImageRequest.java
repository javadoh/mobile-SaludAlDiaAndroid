package com.orugga.yapp.requests;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

/**
 * Created by Alexis on 01/11/2017.
 */

public class DownloadImageRequest {
    public static void downloadImage(Context context, String url, ImageView imageView, int placeHolder) {
        Ion.with(context)
                .load(url)
                .withBitmap()
                .placeholder(placeHolder)
                .error(placeHolder)
                .intoImageView(imageView);
    }

    public static void downloadProfilePhoto(Context context, String url, ImageView imageView, int placeHolder) {
        Ion.with(context)
                .load(url)
                .withBitmap()
                .placeholder(placeHolder)
                .intoImageView(imageView);
    }

    public static void downloadMarker(Context context, String url, FutureCallback<Bitmap> callback) {
        Ion.with(context)
                .load(url)
                .asBitmap()
                .setCallback(callback);
    }
}
