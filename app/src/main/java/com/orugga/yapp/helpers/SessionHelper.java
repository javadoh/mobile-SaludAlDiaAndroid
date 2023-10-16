package com.orugga.yapp.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.crashlytics.android.Crashlytics;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orugga.yapp.Constants;
import com.orugga.yapp.database.RecordatorioToma;
import com.orugga.yapp.database.RecordatorioVisita;

/**
 * Created by Luigi on 18/10/2017.
 */

public class SessionHelper {

    public static boolean isUserLogedIn(Context context) {
        return getAccessToken(context) != null;
    }

    @SuppressLint("ApplySharedPref")
    public static void createSession(Context context, String accessToken, JsonObject user) {
        if (context == null) return;
        SharedPreferences pref = context.getSharedPreferences(Constants.SharedPrefsKeys.SESSION, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(Constants.SharedPrefsKeys.SESSION_ACCESS_TOKEN, accessToken);
        edit.putString(Constants.SharedPrefsKeys.SESSION_USER_INFO, user.toString());
        edit.commit();
        logUser(user);
    }

    private static void logUser(JsonObject user) {
        Crashlytics.setUserIdentifier(user.get("id").getAsString());
        Crashlytics.setUserEmail(user.get("email").getAsString());
        Crashlytics.setUserName(user.get("name").getAsString());
    }

    @SuppressLint("ApplySharedPref")
    public static void clearSession(Context context) {
        if (context == null) return;
        SharedPreferences pref = context.getSharedPreferences(Constants.SharedPrefsKeys.SESSION, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.remove(Constants.SharedPrefsKeys.SESSION_ACCESS_TOKEN);
        edit.remove(Constants.SharedPrefsKeys.SESSION_USER_INFO);
        edit.commit();
    }

    public static String getAccessToken(Context context) {
        if (context == null) return null;
        SharedPreferences pref = context.getSharedPreferences(Constants.SharedPrefsKeys.SESSION, Context.MODE_PRIVATE);
        return pref.getString(Constants.SharedPrefsKeys.SESSION_ACCESS_TOKEN, null);
    }

    public static JsonObject getUser(Context context) {
        if (context == null) return null;
        SharedPreferences pref = context.getSharedPreferences(Constants.SharedPrefsKeys.SESSION, Context.MODE_PRIVATE);
        String userInfo = pref.getString(Constants.SharedPrefsKeys.SESSION_USER_INFO, null);
        if (userInfo == null) {
            return null;
        }
        try {
            return (JsonObject) new JsonParser().parse(userInfo);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressLint("ApplySharedPref")
    public static void setUser(Context context, JsonObject user) {
        if (context == null) return;
        SharedPreferences pref = context.getSharedPreferences(Constants.SharedPrefsKeys.SESSION, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.remove(Constants.SharedPrefsKeys.SESSION_USER_INFO);
        edit.putString(Constants.SharedPrefsKeys.SESSION_USER_INFO, user.toString());
        edit.commit();
    }

    @SuppressWarnings("unused")
    public static String getUserAvatar(Context context) {
        if (context == null) return null;
        try {
            JsonObject user = getUser(context);
            if (user == null) return null;
            String avatarUrl = user.get("picture").getAsString();
            if (avatarUrl == null || avatarUrl.length() == 0) return null;
            return avatarUrl;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getUserName(Context context) {
        if (context == null) return null;
        try {
            JsonObject user = getUser(context);
            if (user == null) return null;
            String name = user.get("name").getAsString();
            if (name == null || name.length() == 0) return null;
            return name;
        } catch (Exception e) {
            return null;
        }
    }

    public static void createReminders(JsonArray takes, JsonArray visits) {
        if (takes.size() > 0) {
            for (int i = 0; i < takes.size(); i++) {
                RecordatorioToma recordatorioToma = new RecordatorioToma(takes.get(i).getAsJsonObject());
                recordatorioToma.save();
            }
        }
        if (visits.size() > 0) {
            for (int i = 0; i < visits.size(); i++) {
                RecordatorioVisita recordatorioVisita = new RecordatorioVisita(visits.get(i).getAsJsonObject());
                recordatorioVisita.save();
            }
        }
    }

    public static void setTerms(Context context, boolean accepted) {
        if (context == null) return;
        SharedPreferences.Editor editor = context.getSharedPreferences(Constants.SharedPrefsKeys.SESSION, Context.MODE_PRIVATE).edit();
        editor.putBoolean(Constants.SharedPrefsKeys.SESSION_TERMS_ACCEPTED, accepted);
        editor.apply();
    }

    public static boolean isTermsAccepted(Context context) {
        if (context == null) return false;
        SharedPreferences pref = context.getSharedPreferences(Constants.SharedPrefsKeys.SESSION, Context.MODE_PRIVATE);
        return pref.getBoolean(Constants.SharedPrefsKeys.SESSION_TERMS_ACCEPTED, false);
    }

    public static boolean requiredUserInformationMissing(JsonObject u) {
        return u.get("email").isJsonNull() || u.get("name").isJsonNull() || u.get("gender").isJsonNull()
                || u.get("deals").isJsonNull() || u.get("deals").getAsJsonArray().size() == 0
                || u.get("healthinsurances").isJsonNull() || u.get("healthinsurances").getAsJsonArray().size() == 0;
    }
}
