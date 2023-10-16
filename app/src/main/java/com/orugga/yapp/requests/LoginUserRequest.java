package com.orugga.yapp.requests;

import android.app.Activity;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.orugga.yapp.interfaces.JsonObjectResponse;

import static com.orugga.yapp.Constants.Urls.LOGIN_USER_URL;
import static com.orugga.yapp.Constants.Urls.LOGIN_USER_WITH_FACEBOOK_URL;

/**
 * Created by Alexis on 28/11/2017.
 */

public class LoginUserRequest {
    public static void loginUser(Activity activity, String email, String password, final JsonObjectResponse callback) {
        try {
            StringBuilder url = new StringBuilder(LOGIN_USER_URL);
            url.append("email=").append(email)
                    .append("&password=").append(password);
            Ion.with(activity)
                    .load(url.toString())
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

    public static void loginWithFacebook(Activity activity, String facebook_token, final JsonObjectResponse callback) {
        try {
            StringBuilder url = new StringBuilder(LOGIN_USER_WITH_FACEBOOK_URL);
            url.append("facebook_token=").append(facebook_token);
            Ion.with(activity)
                    .load("POST", url.toString())
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null) {
                                callback.onError(null, e);
                            } else if (!result.get("rta").getAsString().equals("OK")) {
                                callback.onError(null, null);
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
