package com.orugga.yapp.requests;

import android.content.Context;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.orugga.yapp.interfaces.JsonObjectResponse;

import static com.orugga.yapp.Constants.Urls.RESTORE_PASSWORD_URL;

/**
 * Created by Alexis on 26/12/2017.
 */

public class PasswordRequests {
    public static void restorePassword(Context context, String email, final JsonObjectResponse callback) {
        try {
            StringBuilder url = new StringBuilder(RESTORE_PASSWORD_URL)
                    .append("email=").append(email);
            Ion.with(context)
                    .load(url.toString())
                    .setTimeout(15 * 1000)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null) {
                                callback.onError(null, e);
                            } else if (!result.get("rta").getAsString().equals("OK")) {
                                callback.onError(result.getAsJsonObject("data"), null);
                            } else {
                                callback.onSuccess(result.getAsJsonObject("data"));
                            }
                        }
                    });
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }

    public static void updatePassword(Context context, String api_token, String currentPassword, String newPassword, String newPasswordConfirmation, final JsonObjectResponse callback) {
        try {
            StringBuilder url = new StringBuilder(RESTORE_PASSWORD_URL)
                    .append("api_token=").append(api_token)
                    .append("&current_password=").append(currentPassword)
                    .append("&password=").append(newPassword)
                    .append("&password_confirmation=").append(newPasswordConfirmation);
            Ion.with(context)
                    .load(url.toString())
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null) {
                                callback.onError(null, e);
                            } else if (!result.get("rta").getAsString().equals("OK")) {
                                callback.onError(result, null);
                            } else {
                                callback.onSuccess(result.getAsJsonObject("data"));
                            }
                        }
                    });
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }
}
