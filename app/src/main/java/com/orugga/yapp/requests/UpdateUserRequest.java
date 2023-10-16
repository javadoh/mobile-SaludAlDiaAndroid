package com.orugga.yapp.requests;

import android.app.Activity;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.orugga.yapp.interfaces.JsonObjectResponse;

import java.net.URLEncoder;
import java.util.ArrayList;

import static com.orugga.yapp.Constants.Urls.UPDATE_USER_URL;

/**
 * Created by Alexis on 07/12/2017.
 */

public class UpdateUserRequest {
    public static void updateUser(Activity activity, String apiToken, String email,
                                  String name, String last_name, String birthday, String gender, long region, long comunne,
                                  long healthInsurance, ArrayList<Long> deals,
                                  final JsonObjectResponse callback) {
        try {
            StringBuilder url = new StringBuilder(UPDATE_USER_URL);
            url.append("api_token=").append(apiToken)
                    .append("&email=").append(email);
            if (name != null)
                url.append("&name=").append(URLEncoder.encode(name, "UTF-8"));
            if (last_name != null)
                url.append("&last_name=").append(URLEncoder.encode(last_name, "UTF-8"));
            if (birthday != null)
                url.append("&birth_day=").append(birthday);
            if (gender != null)
               url.append("&gender=").append(gender);
            if (region != -1) {
                url.append("&region_id=").append(region);
                if (comunne != -1)
                    url.append("&comunne_id=").append(comunne);
            }
            if (deals.size() > 0) {
                url.append("&deals=");
                for (int i = 0; i < deals.size(); i++) {
                    url.append(deals.get(i));
                    if (i < deals.size() - 1)
                        url.append(",");
                }
            }
            if (healthInsurance != -1)
                url.append("&healthinsurances=").append(healthInsurance);
            Ion.with(activity)
                    .load("POST", url.toString())
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null) {
                                callback.onError(null, e);
                            } else if (!result.get("rta").getAsString().equals("OK")) {
                                if (result.get("data") != null && result.get("data").isJsonObject()) {
                                    callback.onError(result.getAsJsonObject("data"), null);
                                } else {
                                    callback.onError(result, null);
                                }
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
