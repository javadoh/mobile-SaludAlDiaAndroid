package com.orugga.yapp.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.orugga.yapp.R;
import com.orugga.yapp.objects.MapPointDistance;

import java.util.Arrays;

import static com.orugga.yapp.Constants.ApiFields.PHARMACY_LATITUDE;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_LONGITUDE;

/**
 * Created by Alexis on 16/11/2017.
 */

public class MapHelper {
    public static CameraUpdate createCameraUpdateFromPharmacies(JsonArray pharmacies, int markerCuantity, Context context) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if (pharmacies.size() < markerCuantity)
            markerCuantity = pharmacies.size();
        for (int i = 0; i < markerCuantity; i++) {
            JsonObject pharmacy = pharmacies.get(i).getAsJsonObject();
            builder.include(new LatLng(pharmacy.get(PHARMACY_LATITUDE).getAsDouble(), pharmacy.get(PHARMACY_LONGITUDE).getAsDouble()));
        }
        LatLngBounds bounds = builder.build();
        int padding = (int) convertDpToPixel(24, context);
        return CameraUpdateFactory.newLatLngBounds(bounds, padding);
    }

    public static CameraUpdate createCameraUpdateFromPharmacies(LatLng myLocation, JsonArray pharmacies, int markerCuantity, Context context) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(myLocation);
        MapPointDistance[] mapPointDistances = sortPharmaciesByDistances(myLocation, pharmacies);
        if (pharmacies.size() < markerCuantity)
            markerCuantity = pharmacies.size();
        for (int i = 0; i < markerCuantity; i++) {
            builder.include(mapPointDistances[i].getLatLng());
        }
        LatLngBounds bounds = builder.build();
        int padding = (int) convertDpToPixel(60, context);
        return CameraUpdateFactory.newLatLngBounds(bounds, padding);
    }

    public static MapPointDistance[] sortPharmaciesByDistances(LatLng myLocation, JsonArray pharmacies) {
        MapPointDistance[] mapPointDistances = new MapPointDistance[pharmacies.size()];
        for (int i = 0; i < pharmacies.size(); i++) {
            JsonObject pharmacy = pharmacies.get(i).getAsJsonObject();
            LatLng pharmacyLatLng = new LatLng(pharmacy.get(PHARMACY_LATITUDE).getAsDouble(), pharmacy.get(PHARMACY_LONGITUDE).getAsDouble());
            mapPointDistances[i] = new MapPointDistance(pharmacyLatLng, distanceFromTo(myLocation, pharmacyLatLng), pharmacy);
        }
        Arrays.sort(mapPointDistances);
        return mapPointDistances;
    }

    private static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static Double distanceFromTo(LatLng from, LatLng to) {
        double theta = to.longitude - from.longitude;
        double dist = Math.sin(deg2rad(to.latitude)) * Math.sin(deg2rad(from.latitude))
                + Math.cos(deg2rad(to.latitude)) * Math.cos(deg2rad(from.latitude))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        return dist;
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


    public static AlertDialog buildAlertMessageNoGps(final Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.dialog_text_gps_disabled)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_option_yes, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(R.string.dialog_option_no, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        return alert;
    }
}
