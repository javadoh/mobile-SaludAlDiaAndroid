package com.orugga.yapp.requests;

import android.app.Activity;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.orugga.yapp.interfaces.JsonObjectResponse;

import java.net.URLEncoder;

import static com.orugga.yapp.Constants.Urls.CREATE_USER_URL;

/**
 * Created by Alexis on 27/11/2017.
 */

public class CreateUserRequest {
    public static void createUser(Activity activity, String nombre, String email,
                                  String password, final JsonObjectResponse callback) {
        try {
            StringBuilder url = new StringBuilder();
            url.append(CREATE_USER_URL)
                    .append("name=").append(URLEncoder.encode(nombre, "UTF-8"))
                    .append("&email=").append(URLEncoder.encode(email, "UTF-8"))
                    .append("&password=").append(URLEncoder.encode(password, "UTF-8"));

            Ion.with(activity)
                    .load("POST", url.toString())
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null) {
                                callback.onError(null, e);
                            } else if (!result.get("rta").getAsString().equals("OK")) {
                                callback.onError(result.getAsJsonObject("data"), null);
                            } else {
                                callback.onSuccess(result.getAsJsonObject("data").getAsJsonObject("user"));
                            }
                        }
                    });

        } catch (Exception e) {
            callback.onError(null, e);
        }
    }
}
