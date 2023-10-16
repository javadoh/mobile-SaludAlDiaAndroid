package com.orugga.yapp.database;

import com.crashlytics.android.Crashlytics;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by Alexis on 19/12/2017.
 */

public class RecordatorioVisita extends SugarRecord {

    private String reminderJson;
    private long _id;

    public RecordatorioVisita(){
    }

    public RecordatorioVisita(JsonObject reminderJson){
        this.reminderJson = reminderJson.toString();
        this._id = reminderJson.get("id").getAsLong();
    }
    public JsonObject getReminderJson() {
        return new JsonParser().parse(reminderJson).getAsJsonObject();
    }

    public void setReminderJson(JsonObject reminderJson) {
        this.reminderJson = reminderJson.toString();
    }


    public static RecordatorioVisita getById(long id){
        try {
            List<RecordatorioVisita> list = find(RecordatorioVisita.class, "id = " + id);
            return list.get(0);
        } catch (IndexOutOfBoundsException e) {
            Crashlytics.logException(e);
            return null;
        }
    }
}
