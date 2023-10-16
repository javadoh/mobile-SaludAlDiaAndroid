package com.orugga.yapp.interfaces;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;

public interface PharmacyCallback {
    void onGetLocation(LatLng location);
    void onGetPharmacy(JsonObject pharmacy);
}
