package com.orugga.yapp.interfaces;

import com.google.gson.JsonObject;

/**
 * Created by Alexis on 27/11/2017.
 */

public interface JsonObjectResponse {
    void onSuccess(JsonObject response);
    void onError(JsonObject response, Exception e);
}
