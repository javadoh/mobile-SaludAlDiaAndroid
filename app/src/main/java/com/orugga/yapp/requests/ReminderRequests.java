package com.orugga.yapp.requests;

import android.app.Activity;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.orugga.yapp.interfaces.JsonArrayResponse;
import com.orugga.yapp.interfaces.JsonObjectResponse;

import java.net.URLEncoder;

import static com.orugga.yapp.Constants.Urls.ADD_TAKE_REMINDER_URL;
import static com.orugga.yapp.Constants.Urls.ADD_VISIT_REMINDER_URL;
import static com.orugga.yapp.Constants.Urls.DELETE_TAKE_REMINDER_URL;
import static com.orugga.yapp.Constants.Urls.DELETE_VISIT_REMINDER_URL;
import static com.orugga.yapp.Constants.Urls.MY_TAKE_REMINDERS_URL;
import static com.orugga.yapp.Constants.Urls.MY_VISIT_REMINDERS_URL;
import static com.orugga.yapp.Constants.Urls.UPDATE_TAKE_REMINDER_URL;
import static com.orugga.yapp.Constants.Urls.UPDATE_VISIT_REMINDER_URL;

/**
 * Created by Alexis on 22/12/2017.
 */

public class ReminderRequests {
    public static void addTakeReminder(Activity activity, String apiToken, String productName, long productId, String startDate, String endDate,
                                       String startTime, int posologia, float dosis, String unity, String note, String repurchaseDate,
                                       final JsonObjectResponse callback) {
        try {
            StringBuilder url = new StringBuilder(ADD_TAKE_REMINDER_URL);
            url.append("api_token=").append(apiToken)
                    .append("&product_name=").append(URLEncoder.encode(productName, "UTF-8"))
                    .append("&product_id=").append(productId)
                    .append("&date_start=").append(startDate)
                    .append("&date_end=").append(endDate)
                    .append("&hour_to=").append(startTime)
                    .append("&posology=").append(posologia)
                    .append("&dose=").append(dosis)
                    .append("&unity=").append(unity)
                    .append("&note=").append(URLEncoder.encode(note, "UTF-8"));
            if (!repurchaseDate.equals(""))
                url.append("&date_repurchase=").append(repurchaseDate);
            Ion.with(activity)
                    .load("POST", url.toString())
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null) {
                                callback.onError(null, e);
                            } else if (!result.get("rta").getAsString().equals("OK")) {
                                callback.onError(result, null);
                            } else {
                                callback.onSuccess(result.getAsJsonObject("data").getAsJsonObject("take"));
                            }
                        }
                    });
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }

    public static void addVisitReminder(Activity activity, String apiToken, String doctorName, String especialidad, String date,
                                        String time, String note, final JsonObjectResponse callback) {
        try {
            StringBuilder url = new StringBuilder(ADD_VISIT_REMINDER_URL);
            url.append("api_token=").append(apiToken)
                    .append("&doctor_name=").append(URLEncoder.encode(doctorName, "UTF-8"))
                    .append("&date=").append(date)
                    .append("&hour=").append(time)
                    .append("&note=").append(URLEncoder.encode(note, "UTF-8"));
            if (!especialidad.equals(""))
                url.append("&specialty=").append(URLEncoder.encode(especialidad, "UTF-8"));
            Ion.with(activity)
                    .load("POST", url.toString())
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null) {
                                callback.onError(null, e);
                            } else if (!result.get("rta").getAsString().equals("OK")) {
                                callback.onError(result, null);
                            } else {
                                callback.onSuccess(result.getAsJsonObject("data").getAsJsonObject("visit"));
                            }
                        }
                    });
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }

    public static void myTakeReminders(Activity activity, String apiToken, final JsonArrayResponse callback) {
        try {
            StringBuilder url = new StringBuilder(MY_TAKE_REMINDERS_URL);
            url.append("api_token=").append(apiToken);
            Ion.with(activity)
                    .load(url.toString())
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null) {
                                callback.onError(null, e);
                            } else if (!result.get("rta").getAsString().equals("OK")) {
                                callback.onError(result.getAsJsonObject("data"), null);
                            } else {
                                callback.onSuccess(result.getAsJsonObject("data").getAsJsonArray("customer").get(0).getAsJsonObject().getAsJsonArray("takes"));
                            }
                        }
                    });
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }

    public static void myVisitReminders(Activity activity, String apiToken, final JsonArrayResponse callback) {
        try {
            StringBuilder url = new StringBuilder(MY_VISIT_REMINDERS_URL);
            url.append("api_token=").append(apiToken);
            Ion.with(activity)
                    .load(url.toString())
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null) {
                                callback.onError(null, e);
                            } else if (!result.get("rta").getAsString().equals("OK")) {
                                callback.onError(result.getAsJsonObject("data"), null);
                            } else {
                                callback.onSuccess(result.getAsJsonObject("data").getAsJsonArray("customer").get(0).getAsJsonObject().getAsJsonArray("visits"));
                            }
                        }
                    });
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }

    public static void deleteTakeReminder(Activity activity, String apiToken, long reminderId, final JsonObjectResponse callback) {
        try {
            StringBuilder url = new StringBuilder(DELETE_TAKE_REMINDER_URL);
            url.append("api_token=").append(apiToken)
                    .append("&take_id=").append(reminderId);
            Ion.with(activity)
                    .load("POST", url.toString())
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

    public static void deleteVisitReminder(Activity activity, String apiToken, long reminderId, final JsonObjectResponse callback) {
        try {
            StringBuilder url = new StringBuilder(DELETE_VISIT_REMINDER_URL);
            url.append("api_token=").append(apiToken)
                    .append("&visit_id=").append(reminderId);

            Ion.with(activity)
                    .load("POST", url.toString())
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

    public static void updateTakeReminder(Activity activity, String apiToken, long reminderId, String productName, long productId, String startDate, String endDate,
                                          String startTime, int posologia, float dosis, String unity, String note, String repurchaseDate,
                                          final JsonObjectResponse callback) {
        try {
            StringBuilder url = new StringBuilder(UPDATE_TAKE_REMINDER_URL);
            url.append("api_token=").append(apiToken)
                    .append("&take_id=").append(reminderId)
                    .append("&product_name=").append(URLEncoder.encode(productName, "UTF-8"))
                    .append("&product_id=").append(productId)
                    .append("&date_start=").append(startDate)
                    .append("&date_end=").append(endDate)
                    .append("&hour_to=").append(startTime)
                    .append("&posology=").append(posologia)
                    .append("&dose=").append(dosis)
                    .append("&unity=").append(unity)
                    .append("&note=").append(URLEncoder.encode(note, "UTF-8"));
            if (!repurchaseDate.equals(""))
                url.append("&date_repurchase=").append(repurchaseDate);
            Ion.with(activity)
                    .load("POST", url.toString())
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null) {
                                callback.onError(null, e);
                            } else if (!result.get("rta").getAsString().equals("OK")) {
                                callback.onError(result, null);
                            } else {
                                callback.onSuccess(result.getAsJsonObject("data").getAsJsonObject("takes"));
                            }
                        }
                    });
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }

    public static void updateVisitReminder(Activity activity, String apiToken, long reminderId, String doctorName, String especialidad, String date,
                                           String time, String note, final JsonObjectResponse callback) {
        try {
            StringBuilder url = new StringBuilder(UPDATE_VISIT_REMINDER_URL);
            url.append("api_token=").append(apiToken)
                    .append("&visit_id=").append(reminderId)
                    .append("&doctor_name=").append(URLEncoder.encode(doctorName, "UTF-8"))
                    .append("&date=").append(date)
                    .append("&hour=").append(time)
                    .append("&note=").append(URLEncoder.encode(note, "UTF-8"));
            if (!especialidad.equals(""))
                url.append("&specialty=").append(URLEncoder.encode(especialidad, "UTF-8"));
            Ion.with(activity)
                    .load("POST", url.toString())
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null) {
                                callback.onError(null, e);
                            } else if (!result.get("rta").getAsString().equals("OK")) {
                                callback.onError(result, null);
                            } else {
                                callback.onSuccess(result.getAsJsonObject("data").getAsJsonObject("visits"));
                            }
                        }
                    });
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }
}
