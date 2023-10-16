package com.orugga.yapp.requests;

import android.app.Activity;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.orugga.yapp.interfaces.JsonObjectResponse;

import static com.orugga.yapp.Constants.Urls.SUBSCRIBE_TO_PACIENT_PROGRAM_URL;

/**
 * Created by Alexis on 11/12/2017.
 */

public class SubscribeToPacientProgramRequest {
    public static void subscibeToPacientProgram(Activity activity, String apiToken, int pacientProgramId, final JsonObjectResponse callback) {
        try {
            StringBuilder url = new StringBuilder(SUBSCRIBE_TO_PACIENT_PROGRAM_URL)
                    .append("api_token=").append(apiToken)
                    .append("&pacient_program_id=").append(pacientProgramId)
                    .append("&action=add");
            Ion.with(activity)
                    .load(url.toString())
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if(e != null){
                                callback.onError(null, e);
                            } else if (!result.get("rta").getAsString().equals("OK")){
                                callback.onError(null, null);
                            } else {
                                callback.onSuccess(result.getAsJsonObject("data"));
                            }
                        }
                    });

        } catch (Exception e){
            callback.onError(null, e);
        }
    }
}
