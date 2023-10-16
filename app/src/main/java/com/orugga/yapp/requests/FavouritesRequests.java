package com.orugga.yapp.requests;

import android.app.Activity;
import android.content.Context;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.orugga.yapp.interfaces.JsonArrayResponse;
import com.orugga.yapp.interfaces.JsonObjectResponse;

import static com.orugga.yapp.Constants.Urls.ADD_FAVOURITE_PHARMACIE_URL;
import static com.orugga.yapp.Constants.Urls.ADD_FAVOURITE_PRODUCT_URL;
import static com.orugga.yapp.Constants.Urls.MY_FAVOURITES_PHARMACIES_URL;
import static com.orugga.yapp.Constants.Urls.MY_FAVOURITES_PRODUCTS_URL;

/**
 * Created by Alexis on 25/12/2017.
 */

public class FavouritesRequests {
    public static void getMyFavouritesProducts(Activity activity,  String apiToken, final JsonArrayResponse callback){
        try {
            StringBuilder url = new StringBuilder(MY_FAVOURITES_PRODUCTS_URL)
                    .append("api_token=").append(apiToken);
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
                                callback.onSuccess(result.getAsJsonObject("data").getAsJsonArray("customer").get(0).getAsJsonObject().getAsJsonArray("products"));
                            }
                        }
                    });
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }



    public static void AddDeleteFavouriteProduct(Context context, String apiToken, long productId, String action, final JsonObjectResponse callback){
        try {
            StringBuilder url = new StringBuilder(ADD_FAVOURITE_PRODUCT_URL)
                    .append("api_token=").append(apiToken)
                    .append("&product_id=").append(productId)
                    .append("&action=").append(action);
            Ion.with(context)
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
                                callback.onSuccess(result.getAsJsonObject("data"));
                            }
                        }
                    });
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }


    public static void getMyFavouritesPharmacies(Activity activity, String apiToken, final JsonArrayResponse callback){
        try {
            StringBuilder url = new StringBuilder(MY_FAVOURITES_PHARMACIES_URL)
                    .append("api_token=").append(apiToken);
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
                                callback.onSuccess(result.getAsJsonObject("data").getAsJsonArray("customer").get(0).getAsJsonObject().getAsJsonArray("pharmacies"));
                            }
                        }
                    });
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }



    public static void AddDeleteFavouritePharmacy(Context context, String apiToken, long productId, String action, final JsonObjectResponse callback){
        try {
            StringBuilder url = new StringBuilder(ADD_FAVOURITE_PHARMACIE_URL)
                    .append("api_token=").append(apiToken)
                    .append("&pharmacies_id=").append(productId)
                    .append("&action=").append(action);
            Ion.with(context)
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
                                callback.onSuccess(result.getAsJsonObject("data"));
                            }
                        }
                    });
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }
}
