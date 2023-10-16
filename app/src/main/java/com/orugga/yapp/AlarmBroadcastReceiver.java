package com.orugga.yapp;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.orugga.yapp.database.RecordatorioToma;
import com.orugga.yapp.database.RecordatorioVisita;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static com.orugga.yapp.Constants.Alarms.ACTION_RECORDATORIO_RECOMPRA;
import static com.orugga.yapp.Constants.Alarms.ACTION_RECORDATORIO_TOMA;
import static com.orugga.yapp.Constants.Alarms.ACTION_RECORDATORIO_VISITA;
import static com.orugga.yapp.Constants.Alarms.NOTIFICATION_CHANELL_ID;
import static com.orugga.yapp.Constants.Alarms.RECOMPRA_REQUEST_CODE_SALT;
import static com.orugga.yapp.Constants.Alarms.VISITA_REQUEST_CODE_SALT;
import static com.orugga.yapp.helpers.SessionHelper.isUserLogedIn;


/**
 * Created by Alexis on 19/12/2017.
 */

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    private final static String TAG = "AlarmBroadcastReceiver";

    private final static int VISITA_SALT = 0x4861;
    private final static int TOMA_SALT = 0x1862;
    private final static int RECOMPRA_SALT = 0x81A5;

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();

        switch (intentAction) {
            case ACTION_BOOT_COMPLETED:
                if (isUserLogedIn(context))
                    setAllAlarms(context);
                break;
            case ACTION_RECORDATORIO_TOMA:
                long id = intent.getLongExtra("id", -1);
                RecordatorioToma recordatorioToma = RecordatorioToma.getById(id);
                if (recordatorioToma == null) return;
                JsonObject reminderJson = recordatorioToma.getReminderJson();
                String medicineName = reminderJson.get("product_name").getAsString();
                String dosis = reminderJson.get("dose").getAsString();
                String unity = reminderJson.get("unity").getAsString();
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANELL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);

                    // Configure the notification channel.
                    notificationChannel.setDescription("Channel description");
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.YELLOW);
                    notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                    notificationChannel.enableVibration(true);
                    notificationManager.createNotificationChannel(notificationChannel);
                }

                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(context, NOTIFICATION_CHANELL_ID)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("Recuerda tomar " + medicineName)
                                .setContentText(dosis + " " + unity)
                                .setPriority(Notification.PRIORITY_HIGH)
                                .setAutoCancel(true);
                //Vibration
                builder.setVibrate(new long[]{0, 1000, 500, 1000});

                //LED
                builder.setLights(Color.YELLOW, 3000, 3000);

                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                builder.setSound(alarmSound);
                // Creates an explicit intent for an Activity in your app
                Intent resultIntent = new Intent(context, MainHomeActivity.class);

                // The stack builder object will contain an artificial back stack for the
                // started Activity.
                // This ensures that navigating backward from the Activity leads out of
                // your app to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                // Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(MainHomeActivity.class);
                // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                builder.setContentIntent(resultPendingIntent);

                // mNotificationId is a unique integer your app uses to identify the
                // notification. For example, to cancel the notification, you can pass its ID
                // number to NotificationManager.cancel().
                notificationManager.notify(reminderJson.get("id").getAsInt() * TOMA_SALT, builder.build());
                //Me fijo si tengo que crear otra alarma
                setRecordatorioTomaAlarm(context, recordatorioToma);
                break;
            case ACTION_RECORDATORIO_RECOMPRA:
                id = intent.getLongExtra("id", -1);
                recordatorioToma = RecordatorioToma.getById(id);
                if (recordatorioToma == null) return;
                if (!recordatorioToma.isRepurchaseDateNotified()) {
                    reminderJson = recordatorioToma.getReminderJson();
                    medicineName = reminderJson.get("product_name").getAsString();
                    String note = reminderJson.get("note") != JsonNull.INSTANCE ? reminderJson.get("note").getAsString() : "";
                    notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANELL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);

                        // Configure the notification channel.
                        notificationChannel.setDescription("Channel description");
                        notificationChannel.enableLights(true);
                        notificationChannel.setLightColor(Color.YELLOW);
                        notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                        notificationChannel.enableVibration(true);
                        notificationManager.createNotificationChannel(notificationChannel);
                    }
                    builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANELL_ID)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Recuerda comprar " + medicineName)
                            .setContentText(note)
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setAutoCancel(true);
                    //Vibration
                    builder.setVibrate(new long[]{0, 1000, 500, 1000});

                    //LED
                    builder.setLights(Color.YELLOW, 3000, 3000);

                    alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    builder.setSound(alarmSound);

                    // Creates an explicit intent for an Activity in your app
                    resultIntent = new Intent(context, MainHomeActivity.class);

                    // The stack builder object will contain an artificial back stack for the
                    // started Activity.
                    // This ensures that navigating backward from the Activity leads out of
                    // your app to the Home screen.
                    stackBuilder = TaskStackBuilder.create(context);
                    // Adds the back stack for the Intent (but not the Intent itself)
                    stackBuilder.addParentStack(MainHomeActivity.class);
                    // Adds the Intent that starts the Activity to the top of the stack
                    stackBuilder.addNextIntent(resultIntent);
                    resultPendingIntent = stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
                    builder.setContentIntent(resultPendingIntent);

                    // mNotificationId is a unique integer your app uses to identify the
                    // notification. For example, to cancel the notification, you can pass its ID
                    // number to NotificationManager.cancel().
                    notificationManager.notify(reminderJson.get("id").getAsInt() * RECOMPRA_SALT, builder.build());
                    recordatorioToma.setRepurchaseDateNotified(true);
                    recordatorioToma.save();
                }
                break;
            case ACTION_RECORDATORIO_VISITA:
                id = intent.getLongExtra("id", -1);
                RecordatorioVisita recordatorioVisita = RecordatorioVisita.getById(id);
                if (recordatorioVisita == null) return;
                reminderJson = recordatorioVisita.getReminderJson();
                String doctorName = reminderJson.get("doctor_name").getAsString();
                String especialidad = reminderJson.get("specialty").getAsString();
                String hour = reminderJson.get("hour").getAsString();
                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANELL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);

                    // Configure the notification channel.
                    notificationChannel.setDescription("Channel description");
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.YELLOW);
                    notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                    notificationChannel.enableVibration(true);
                    notificationManager.createNotificationChannel(notificationChannel);
                }
                builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANELL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(doctorName + " " + hour.substring(0, 5))
                        .setContentText(especialidad)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setAutoCancel(true);
                //Vibration
                builder.setVibrate(new long[]{0, 1000, 500, 1000});

                //LED
                builder.setLights(Color.YELLOW, 3000, 3000);

                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                builder.setSound(alarmSound);
                // Creates an explicit intent for an Activity in your app
                resultIntent = new Intent(context, MainHomeActivity.class);

                // The stack builder object will contain an artificial back stack for the
                // started Activity.
                // This ensures that navigating backward from the Activity leads out of
                // your app to the Home screen.
                stackBuilder = TaskStackBuilder.create(context);
                // Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(MainHomeActivity.class);
                // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                resultPendingIntent = stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
                builder.setContentIntent(resultPendingIntent);


                // mNotificationId is a unique integer your app uses to identify the
                // notification. For example, to cancel the notification, you can pass its ID
                // number to NotificationManager.cancel().
                notificationManager.notify(reminderJson.get("id").getAsInt() * VISITA_SALT, builder.build());
                break;
        }

    }

    private static ArrayList<Integer> getHorasDeTomar(int posologia, int horaInicio) {
        int cantTomasPorDia = 24 / posologia;
        ArrayList<Integer> horariosDeToma = new ArrayList<>();
        for (int i = 0; i < cantTomasPorDia; i++) {
            int siguienteToma = horaInicio + i * posologia;
            horariosDeToma.add(siguienteToma >= 24 ? siguienteToma - 24 : siguienteToma);
        }
        return horariosDeToma;
    }

    public static void setRecordatorioTomaAlarm(Context context, RecordatorioToma recordatorioToma) {
        JsonObject recordatorioTomaJson = recordatorioToma.getReminderJson();
        long nextAlarmTime = getNextTomaAlarmTime(recordatorioTomaJson);
        if (nextAlarmTime != -1) {
            Intent recordatorioIntent = new Intent(context, AlarmBroadcastReceiver.class);
            recordatorioIntent.setAction(ACTION_RECORDATORIO_TOMA);
            recordatorioIntent.putExtra("id", recordatorioToma.getId());
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            int requestCode = recordatorioTomaJson.get("id").getAsInt();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, recordatorioIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (android.os.Build.VERSION.SDK_INT >= 19) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + nextAlarmTime, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + nextAlarmTime, pendingIntent);
            }
        }
    }

    public static void setRecordatorioRecompraAlarm(Context context, RecordatorioToma rt) {
        JsonObject recordatorioTomaJson = rt.getReminderJson();
        if (recordatorioTomaJson.get("date_repurchase") != JsonNull.INSTANCE) {
            long nextAlarmTime = getNextSingleAlarmTime(recordatorioTomaJson.get("date_repurchase").getAsString(), "00:00");
            if (nextAlarmTime != -1) {
                Intent recordatorioIntent = new Intent(context, AlarmBroadcastReceiver.class);
                recordatorioIntent.setAction(ACTION_RECORDATORIO_RECOMPRA);
                recordatorioIntent.putExtra("id", rt.getId());
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                int requestCode = RECOMPRA_REQUEST_CODE_SALT + recordatorioTomaJson.get("id").getAsInt();
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, recordatorioIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                if (android.os.Build.VERSION.SDK_INT >= 19) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + nextAlarmTime, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + nextAlarmTime, pendingIntent);
                }
            }
        }
    }

    public static void setRecordatorioVisitaAlarma(Context context, RecordatorioVisita rv) {
        JsonObject recordatorioVisitaJson = rv.getReminderJson();
        long nextAlarmTime = getNextSingleAlarmTime(recordatorioVisitaJson.get("date").getAsString(), recordatorioVisitaJson.get("hour").getAsString());
        if (nextAlarmTime != -1) {
            Intent recordatorioIntent = new Intent(context, AlarmBroadcastReceiver.class);
            recordatorioIntent.setAction(ACTION_RECORDATORIO_VISITA);
            recordatorioIntent.putExtra("id", rv.getId());
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            int requestCode = VISITA_REQUEST_CODE_SALT + recordatorioVisitaJson.get("id").getAsInt();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, recordatorioIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (android.os.Build.VERSION.SDK_INT >= 19) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + nextAlarmTime, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + nextAlarmTime, pendingIntent);
            }
        }
    }

    private static long getNextTomaAlarmTime(JsonObject recordatorioToma) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        String endDate = recordatorioToma.get("date_end").getAsString();
        String startDate = recordatorioToma.get("date_start").getAsString();
        String startTime = recordatorioToma.get("hour_to").getAsString();
        String[] startDateSplit = startDate.split("-");
        String[] endDateSplit = endDate.split("-");
        String[] startTimeSplit = startTime.split(":");
        int dayStart = Integer.valueOf(startDateSplit[2]);
        int monthStart = Integer.valueOf(startDateSplit[1]);
        int yearStart = Integer.valueOf(startDateSplit[0]);
        int hoursStart = Integer.valueOf(startTimeSplit[0]);
        int minutesStart = Integer.valueOf(startTimeSplit[1]);
        int dayEnd = Integer.valueOf(endDateSplit[2]);
        int monthEnd = Integer.valueOf(endDateSplit[1]);
        int yearEnd = Integer.valueOf(endDateSplit[0]);
        LocalDateTime startDateTime = new LocalDateTime(yearStart, monthStart, dayStart, hoursStart, minutesStart, 0, 0);
        LocalDateTime endDateTime = new LocalDateTime(yearEnd, monthEnd, dayEnd, 23, 59, 59, 0);
        LocalDateTime alarmDateTime = LocalDateTime.now();
        ArrayList<Integer> horariosDeToma = getHorasDeTomar(recordatorioToma.get("posology").getAsInt(), hoursStart);
        if (currentDateTime.isAfter(startDateTime) && currentDateTime.isBefore(endDateTime)) {
            int i = 0;
            if (currentDateTime.getHourOfDay() >= getMax(horariosDeToma) && currentDateTime.getMinuteOfHour() >= minutesStart) {
                alarmDateTime = alarmDateTime.plusDays(1).withTime(getMin(horariosDeToma), minutesStart, 0, 0);
                if (alarmDateTime.isBefore(endDateTime)) {
                    long millis = new Duration(currentDateTime.toDateTime(), alarmDateTime.toDateTime()).getMillis();
                    Log.i(TAG, "Alarma seteada dentro de " + millis + " millis");
                    return millis;
//                    return alarmDateTime.getMillisOfDay() + (86400000 - currentDateTime.getMillisOfDay());
                } else {
                    Log.i(TAG, "La alarma no tiene posterior horario");
                    return -1;
                }
            } else {
                LocalDateTime tomaDateTime = LocalDateTime.now().withTime(horariosDeToma.get(horariosDeToma.size() - 1), minutesStart, 0, 0);
                LocalDateTime nextTomaDateTime = LocalDateTime.now().withTime(horariosDeToma.get(0), minutesStart, 0, 0);
                if (currentDateTime.isAfter(tomaDateTime) && currentDateTime.isBefore(nextTomaDateTime)) {
                    alarmDateTime = alarmDateTime.withTime(horariosDeToma.get(0), minutesStart, 0, 0);
                    long millis = new Duration(currentDateTime.toDateTime(), alarmDateTime.toDateTime()).getMillis();
                    Log.i(TAG, "Alarma seteada dentro de " + millis + " millis");
                    return millis;
//                    return alarmDateTime.getMillisOfDay() - currentDateTime.getMillisOfDay();
                } else {
                    if (horariosDeToma.size() == 1) {
                        alarmDateTime = alarmDateTime.withTime(hoursStart, minutesStart, 0, 0);
                        if (currentDateTime.isAfter(alarmDateTime))
                            alarmDateTime = alarmDateTime.plusDays(1);
                        long millis = new Duration(currentDateTime.toDateTime(), alarmDateTime.toDateTime()).getMillis();
                        Log.i(TAG, "Alarma seteada dentro de " + millis + " millis");
                        return millis;
//                        if (currentDateTime.isAfter(alarmDateTime)) {
//                            Log.i(TAG, "Alarma seteada dentro de " + ((86400000 - currentDateTime.getMillisOfDay()) + alarmDateTime.getMillisOfDay()) + " millis");
//                            return (86400000 - currentDateTime.getMillisOfDay()) +
//                                    alarmDateTime.getMillisOfDay();
//                        } else {
//                            Log.i(TAG, "Alarma seteada dentro de " + (alarmDateTime.getMillisOfDay() - currentDateTime.getMillisOfDay()) + " millis");
//                            return alarmDateTime.getMillisOfDay() - currentDateTime.getMillisOfDay();
//                        }
                    } else {
                        while (i < horariosDeToma.size() - 1) {
                            tomaDateTime = LocalDateTime.now().withTime(horariosDeToma.get(i), minutesStart, 0, 0);
                            nextTomaDateTime = LocalDateTime.now().withTime(horariosDeToma.get(i + 1), minutesStart, 0, 0);
                            if (currentDateTime.isAfter(tomaDateTime) && currentDateTime.isBefore(nextTomaDateTime)) {
                                alarmDateTime = alarmDateTime.withTime(horariosDeToma.get(i + 1), minutesStart, 0, 0);
                                long millis = new Duration(currentDateTime.toDateTime(), alarmDateTime.toDateTime()).getMillis();
                                Log.i(TAG, "Alarma seteada dentro de " + millis + " millis");
                                return millis;
                            }
                            i++;
                        }
                    }
                }
                Log.i(TAG, "La alarma no encontro un horario");
                return -1;
            }
        } else if (currentDateTime.isBefore(startDateTime)) {
            alarmDateTime = alarmDateTime.withDate(yearStart, monthStart, dayStart);
            alarmDateTime = alarmDateTime.withTime(hoursStart, minutesStart, 0, 0);
//            long millis;
//            if (startDateTime.getYear() == currentDateTime.getYear()) {
//                millis = (86400000 - currentDateTime.getMillisOfDay()) +
//                        ((long) 86400000 * (alarmDateTime.getDayOfYear() - currentDateTime.getDayOfYear() - 1)) +
//                        alarmDateTime.getMillisOfDay();
//            } else {
//                millis = (86400000 - currentDateTime.getMillisOfDay()) +
//                        ((long) 86400000 * (365 - currentDateTime.getDayOfYear())) +
//                        (31556952000L * (alarmDateTime.getYear() - currentDateTime.getYear() - 1)) +
//                        (86400000 * (alarmDateTime.getDayOfYear() - 1)) +
//                        (alarmDateTime.getMillisOfDay());
//            }
            long millis = new Duration(currentDateTime.toDateTime(), alarmDateTime.toDateTime()).getMillis();
            Log.i(TAG, "Alarma seteada dentro de " + millis + " millis");
            return millis;
        } else {
            Log.i(TAG, "La alarma no tiene posterior horario");
            return -1;
        }
    }

    private static long getNextSingleAlarmTime(String date, String time) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        String[] dateSplit = date.split("-");
        String[] timeSplit = time.split(":");
        int day = Integer.valueOf(dateSplit[2]);
        int month = Integer.valueOf(dateSplit[1]);
        int year = Integer.valueOf(dateSplit[0]);
        int hours = Integer.valueOf(timeSplit[0]);
        int minutes = Integer.valueOf(timeSplit[1]);
        LocalDateTime alarmDateTime = new LocalDateTime(year, month, day, hours, minutes, 0, 0);
        if (currentDateTime.isBefore(alarmDateTime)) {
            long millis = new Duration(currentDateTime.toDateTime(), alarmDateTime.toDateTime()).getMillis();
            Log.i(TAG, "Alarma seteada dentro de " + millis + " millis");
            return millis;
//            return (alarmDateTime.getMillisOfDay() - currentDateTime.getMillisOfDay()) +
//                    (86400000 * (alarmDateTime.getDayOfYear() - currentDateTime.getDayOfYear() - 1));
        } else if (currentDateTime.getYear() == year && currentDateTime.getDayOfYear() == alarmDateTime.getDayOfYear()) {
            return 0;
        } else {
            return -1;
        }
    }

    private static int getMin(ArrayList<Integer> horariosDeToma) {
        int min = 25;
        for (int hora : horariosDeToma) {
            if (hora < min)
                min = hora;
        }
        return min;
    }

    private static int getMax(ArrayList<Integer> horariosDeToma) {
        int max = -1;
        for (int hora : horariosDeToma) {
            if (hora > max)
                max = hora;
        }
        return max;
    }

    public static void setAllAlarms(Context context) {
        List<RecordatorioToma> recordatorioTomas = RecordatorioToma.listAll(RecordatorioToma.class);
        List<RecordatorioVisita> recordatorioVisitas = RecordatorioVisita.listAll(RecordatorioVisita.class);
        for (RecordatorioToma rt : recordatorioTomas) {
            setRecordatorioTomaAlarm(context, rt);
            setRecordatorioRecompraAlarm(context, rt);
        }
        for (RecordatorioVisita rv : recordatorioVisitas) {
            setRecordatorioVisitaAlarma(context, rv);
        }
    }

    public static void cancelAllAlarms(Context context) {
        List<RecordatorioToma> recordatorioTomas = RecordatorioToma.listAll(RecordatorioToma.class);
        for (RecordatorioToma rt : recordatorioTomas) {
            cancelTomaAlarm(context, rt);
            cancelRecompraAlarm(context, rt);
        }
        List<RecordatorioVisita> recordatorioVisitas = RecordatorioVisita.listAll(RecordatorioVisita.class);
        for (RecordatorioVisita rv : recordatorioVisitas) {
            cancelVisitaAlarm(context, rv);
        }
    }

    public static void cancelTomaAlarm(Context context, RecordatorioToma rt) {
        Intent recordatorioIntent = new Intent(context, AlarmBroadcastReceiver.class);
        recordatorioIntent.setAction(ACTION_RECORDATORIO_TOMA);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int requestCode = rt.getReminderJson().get("id").getAsInt();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, recordatorioIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    public static void cancelRecompraAlarm(Context context, RecordatorioToma rt) {
        Intent recordatorioIntent = new Intent(context, AlarmBroadcastReceiver.class);
        recordatorioIntent.setAction(ACTION_RECORDATORIO_RECOMPRA);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int requestCode = RECOMPRA_REQUEST_CODE_SALT + rt.getReminderJson().get("id").getAsInt();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, recordatorioIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    public static void cancelVisitaAlarm(Context context, RecordatorioVisita rv) {
        Intent recordatorioIntent = new Intent(context, AlarmBroadcastReceiver.class);
        recordatorioIntent.setAction(ACTION_RECORDATORIO_VISITA);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int requestCode = VISITA_REQUEST_CODE_SALT + rv.getReminderJson().get("id").getAsInt();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, recordatorioIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
