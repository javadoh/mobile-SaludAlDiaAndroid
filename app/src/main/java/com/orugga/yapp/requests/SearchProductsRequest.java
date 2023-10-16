package com.orugga.yapp.requests;

import android.app.Activity;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.orugga.yapp.Constants;
import com.orugga.yapp.interfaces.JsonArrayResponse;

import java.net.URLEncoder;

/**
 * Created by Alexis on 25/10/2017.
 */

public class SearchProductsRequest {
    public static void fetch(Activity activity, String api_token, String keyword, final JsonArrayResponse callback) {
        try {
            StringBuilder url = new StringBuilder(Constants.Urls.SEARCH_PRODUCTS_URL)
                    .append("name=").append(URLEncoder.encode(keyword, "UTF-8"));
            if (api_token != null)
                url.append("&api_token=").append(api_token);
            Ion.with(activity)
                    .load(url.toString())
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if(e != null){
                                callback.onError(null, e);
                            } else if (!result.get("rta").getAsString().equals("OK")){
                                callback.onError(result, null);
                            } else {
                                callback.onSuccess(result.getAsJsonObject("data").getAsJsonArray("products"));
                            }
                        }
                    });
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }
}
