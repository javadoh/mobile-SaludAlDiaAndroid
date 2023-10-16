package com.orugga.yapp.objects;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;

/**
 * Created by Alexis on 21/11/2017.
 */

public class MapPointDistance implements Comparable<MapPointDistance> {

    private LatLng latLng;
    private double distance;
    private JsonObject jsonObject;

    public MapPointDistance(LatLng latLng, double distance) {
        this.latLng = latLng;
        this.distance = distance;
    }

    public MapPointDistance(LatLng latLng, double distance, JsonObject jsonObject) {
        this.latLng = latLng;
        this.distance = distance;
        this.jsonObject = jsonObject;
    }

    public JsonObject getJsonObject() {
        return jsonObject;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public int compareTo(@NonNull MapPointDistance mapPoint) {
        double compareDistance = mapPoint.getDistance();
        //precision of the distance, add more 0's for more accuracy
        int precision = 10000;
        //ascending order
        return (int) ((this.distance - compareDistance)*precision);
    }

}
