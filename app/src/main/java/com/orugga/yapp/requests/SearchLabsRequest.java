package com.orugga.yapp.requests;

import android.app.Activity;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.orugga.yapp.interfaces.JsonArrayResponse;

import java.net.URLEncoder;

import static com.orugga.yapp.Constants.Urls.SEARCH_LABS_URL;

/**
 * Created by Alexis on 11/12/2017.
 */

public class SearchLabsRequest {
    public static void searchLabs(Activity activity, String labName, final JsonArrayResponse callback){
        try {
            String lblEncoded = URLEncoder.encode(labName, "UTF-8");
            String url = SEARCH_LABS_URL + "name=" + lblEncoded;
            Ion.with(activity)
                    .load(url)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if(e != null){
                                callback.onError(null, e);
                            } else if (!result.get("rta").getAsString().equals("OK")){
                                callback.onError(null, null);
                            } else {
                                callback.onSuccess(result.getAsJsonObject("data").getAsJsonArray("labs"));
                            }
                        }
                    });
        } catch (Exception e){
            callback.onError(null, e);
        }
    }
}
