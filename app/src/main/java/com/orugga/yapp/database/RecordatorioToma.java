package com.orugga.yapp.database;

import android.app.PendingIntent;

import com.crashlytics.android.Crashlytics;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orm.SugarRecord;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexis on 19/12/2017.
 */

public class RecordatorioToma extends SugarRecord {

    private String reminderJson;
    private boolean repurchaseDateNotified;
    private long _id;

    public RecordatorioToma() {
    }

    public RecordatorioToma(JsonObject reminderJson) {
        this.reminderJson = reminderJson.toString();
        this._id = reminderJson.get("id").getAsLong();
        this.repurchaseDateNotified = false;
    }

    public JsonObject getReminderJson() {
        return new JsonParser().parse(this.reminderJson).getAsJsonObject();
    }

    public void setReminderJson(JsonObject reminderJson) {
        this.reminderJson = reminderJson.toString();
    }

    public void setReminderJson(String reminderJson) {
        this.reminderJson = reminderJson;
    }

    public boolean isRepurchaseDateNotified() {
        return repurchaseDateNotified;
    }

    public void setRepurchaseDateNotified(boolean repurchaseDateNotified) {
        this.repurchaseDateNotified = repurchaseDateNotified;
    }

    public static RecordatorioToma getById(long id){
        try {
            List<RecordatorioToma> list = find(RecordatorioToma.class, "id = " + id);
            return list.get(0);
        } catch (IndexOutOfBoundsException e) {
            Crashlytics.logException(e);
            return null;
        }
    }
}
