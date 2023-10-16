package com.orugga.yapp.requests;

import android.content.Context;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.orugga.yapp.interfaces.JsonObjectResponse;

import static com.orugga.yapp.Constants.Urls.LOG_PACIENT_PROGRAM_DETAIL_URL;

public class LogsRequests {
    public static void logPacientProgramDetail(Context context, String date, double latitude, double longitude, long idPacientProgram,
                                               long idProduct, long idUser, final JsonObjectResponse callback) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("date_request", date);
            body.addProperty("latitude", latitude);
            body.addProperty("longitude", longitude);
            if (idPacientProgram != -1)
                body.addProperty("pacient_programs_id", idPacientProgram);
            if (idProduct != -1)
                body.addProperty("products_id", idProduct);
            if (idUser != -1)
                body.addProperty("customer_id", idUser);
            Ion.with(context)
                    .load("POST", LOG_PACIENT_PROGRAM_DETAIL_URL)
                    .setTimeout(15 * 1000)
                    .setJsonObjectBody(body)
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

}
