package com.orugga.yapp.interfaces;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Created by Alexis on 25/10/2017.
 */

public interface JsonArrayResponse {
    void onSuccess(JsonArray response);
    void onError(JsonObject response, Exception e);
}
