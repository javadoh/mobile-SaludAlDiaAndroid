package com.orugga.yapp.requests;

import android.app.Activity;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.orugga.yapp.helpers.SessionHelper;
import com.orugga.yapp.interfaces.JsonArrayResponse;

import static com.orugga.yapp.Constants.Urls.SEARCH_DETAIL_PACIENT_PROGRAMS_URL;
import static com.orugga.yapp.Constants.Urls.SEARCH_PACIENT_PROGRAMS_URL;

/**
 * Created by Alexis on 11/12/2017.
 */

public class SearchPacientProgramsRequest {
    public static void searchPacientPrograms(Activity activity, /*int labId,*/ int productId, /*String labName,*/ String productName,
                                             final JsonArrayResponse callback) {
        try {
            StringBuilder url = new StringBuilder(SEARCH_PACIENT_PROGRAMS_URL);
//            if (labId != -1) {
//                url.append("&labs_id=").append(labId);
//            } else if (labName.length() != 0) {
//                url.append("&lab_name=").append(labName);
//            }
            if (productId != -1) {
                url.append("&products_ids=").append(productId);
            } else if (productName.length() != 0) {
                url.append("&product_name=").append(productName);
            }
            Ion.with(activity)
                    .load(url.toString())
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null) {
                                callback.onError(null, e);
                            } else if (!result.get("rta").getAsString().equals("OK")) {
                                callback.onError(null, null);
                            } else {
                                callback.onSuccess(result.getAsJsonObject("data").getAsJsonArray("programs"));
                            }
                        }
                    });
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }

    public static void searchDetailPacientPrograms(Activity activity, int productId, final JsonArrayResponse callback) {
        try {
            StringBuilder url = new StringBuilder(SEARCH_DETAIL_PACIENT_PROGRAMS_URL);
            if (productId != -1) {
                url.append(productId);
                if (SessionHelper.isUserLogedIn(activity)) {
                    url.append("?api_token=").append(SessionHelper.getAccessToken(activity));
                }
                Ion.with(activity)
                        .load(url.toString())
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                if (e != null) {
                                    callback.onError(null, e);
                                } else if (!result.get("rta").getAsString().equals("OK")) {
                                    callback.onError(null, null);
                                } else {
                                    callback.onSuccess(result.getAsJsonObject("data").getAsJsonArray("programs"));
                                }
                            }
                        });
            } else {
                callback.onError(null, null);
            }
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }
}
