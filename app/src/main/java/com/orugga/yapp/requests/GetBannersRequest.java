package com.orugga.yapp.requests;

import android.content.Context;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.orugga.yapp.interfaces.JsonArrayResponse;

import static com.orugga.yapp.Constants.Urls.GET_BANNERS_URL;

public class GetBannersRequest {
    public static void getBanners(Context context, final JsonArrayResponse callback) {
        try {
            Ion.with(context)
                    .load("POST", GET_BANNERS_URL)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null) {
                                callback.onError(null, e);
                            } else if (!result.get("rta").getAsString().equals("OK")) {
                                callback.onError(null, null);
                            } else {
                                callback.onSuccess(result.getAsJsonObject("data").getAsJsonArray("banners"));
                            }
                        }
                    });
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }
}
