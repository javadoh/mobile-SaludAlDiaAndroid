package com.orugga.yapp.requests;

import android.app.Activity;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.orugga.yapp.interfaces.JsonArrayResponse;

import static com.orugga.yapp.Constants.Urls.GET_PROMOTIONS_URL;

/**
 * Created by Alexis on 12/12/2017.
 */

public class GetPromotionsRequest {
    public static void getPromotions(Activity activity, final JsonArrayResponse callback) {
        try {
            Ion.with(activity)
                    .load(GET_PROMOTIONS_URL)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null) {
                                callback.onError(null, e);
                            } else if (!result.get("rta").getAsString().equals("OK")) {
                                callback.onError(null, null);
                            } else {
                                callback.onSuccess(result.getAsJsonArray("data"));
                            }
                        }
                    });
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }
}
