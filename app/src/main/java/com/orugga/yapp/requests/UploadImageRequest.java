package com.orugga.yapp.requests;

import android.content.Context;
import android.os.Environment;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.orugga.yapp.interfaces.JsonObjectResponse;

import java.io.File;

import static com.orugga.yapp.Constants.Urls.UPLOAD_IMAGE_URL;

/**
 * Created by Alexis on 4/1/2018.
 */

public class UploadImageRequest {
    public static void uploadImage(Context context, String api_token, File fileToUpload, final JsonObjectResponse callback) {
        StringBuilder url = new StringBuilder(UPLOAD_IMAGE_URL);
        url.append("api_token=").append(api_token);
        Ion.with(context)
                .load(url.toString())
                .setTimeout(60 * 1000)
                .setMultipartFile("picture", "image/jpeg", fileToUpload)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            callback.onError(null, e);
                        } else if (!result.get("rta").getAsString().equals("OK")) {
                            callback.onError(result, null);
                        } else {
                            callback.onSuccess(result.getAsJsonObject("data").getAsJsonObject("user"));
                        }
                    }
                });
    }
}
