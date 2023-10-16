package com.orugga.yapp.requests;

import android.app.Activity;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.orugga.yapp.interfaces.JsonArrayResponse;

import java.util.List;

import static com.orugga.yapp.Constants.Urls.SEARCH_PHARMACIES_BY_RADIUS_PRODUCTS_URL;
import static com.orugga.yapp.helpers.SessionHelper.getAccessToken;
import static com.orugga.yapp.helpers.SessionHelper.isUserLogedIn;

/**
 * Created by Alexis on 03/11/2017.
 */

public class SearchNearPharmaciesWithProductsRequest {
    public static void fetch(Activity activity, double latitude, double longitude, int radius, List<String> productIds, final JsonArrayResponse callback){
        try {
            StringBuilder url = new StringBuilder();
            url.append(SEARCH_PHARMACIES_BY_RADIUS_PRODUCTS_URL)
                    .append("user_latitude=").append(latitude)
                    .append("&user_longitude=").append(longitude)
                    .append("&radius=").append(radius)
                    .append("&product_ids=");
            int i = 0;
            while(i < productIds.size()) {
                url.append(productIds.get(i));
                if (i < productIds.size() -1)
                    url.append(",");
                i++;
            }
            if (isUserLogedIn(activity)){
                url.append("&api_token=").append(getAccessToken(activity));
            }
            Ion.with(activity)
                    .load(url.toString())
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if(e != null) {
                                callback.onError(null, e);
                            } else if (!result.get("rta").getAsString().equals("OK")){
                                callback.onError(result, null);
                            } else {
                                callback.onSuccess(result.getAsJsonObject("data").getAsJsonArray("pharmacies"));
                            }
                        }
                    });
        } catch (Exception e){
            callback.onError(null, e);
        }
    }
}
