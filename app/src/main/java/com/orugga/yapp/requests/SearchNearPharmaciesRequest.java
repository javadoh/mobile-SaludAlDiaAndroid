package com.orugga.yapp.requests;

import android.app.Activity;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.orugga.yapp.interfaces.JsonArrayResponse;

import static com.orugga.yapp.Constants.Urls.SEARCH_PHARMACIES_BY_RADIUS_URL;

/**
 * Created by Alexis on 30/10/2017.
 */

public class SearchNearPharmaciesRequest {
    public static void fetch(Activity activity, String api_token, double latitude, double longitude, double radius, final JsonArrayResponse callback){
        try {
            String url = new StringBuilder().append(SEARCH_PHARMACIES_BY_RADIUS_URL)
                    .append("api_token=").append(api_token)
                    .append("&user_latitude=").append(latitude)
                    .append("&user_longitude=").append(longitude)
                    .append("&status=1")
                    .append("&radius=").append(radius).toString();
            Ion.with(activity)
                    .load(url)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if(e != null){
                                callback.onError(null, e);
                            } else if (!result.get("rta").getAsString().equals("OK")){
                                callback.onError(result, null);
                            } else {
                                callback.onSuccess(result.getAsJsonObject("data").getAsJsonArray("pharmacies"));
                            }
                        }
                    });
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }
}
