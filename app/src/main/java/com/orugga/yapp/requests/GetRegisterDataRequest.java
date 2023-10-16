package com.orugga.yapp.requests;

import android.app.Activity;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.orugga.yapp.interfaces.JsonObjectResponse;

import static com.orugga.yapp.Constants.Urls.GET_REGISTER_DATA_URL;

/**
 * Created by Alexis on 28/11/2017.
 */

public class GetRegisterDataRequest {
    public static void getRegisterData(Activity activity, final JsonObjectResponse callback) {
        try {
            Ion.with(activity)
                    .load(GET_REGISTER_DATA_URL)
                    .setTimeout(5 * 1000)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null) {
                                callback.onError(null, e);
                            } else {
                                callback.onSuccess(result.get("data").getAsJsonObject());
                            }
                        }
                    });
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }
}

