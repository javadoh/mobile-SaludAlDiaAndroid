package com.orugga.yapp.helpers;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Alexis on 15/12/2017.
 */

public class DateHelper {
    public static Calendar getCalendarDate(int year, int monthOfYear, int dayOfMonth){
        return new DateTime(year, monthOfYear, dayOfMonth, 0, 0).toCalendar(Locale.getDefault());
//        calendar.set(Calendar.YEAR, year);
//        calendar.set(Calendar.MONTH, monthOfYear);
//        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    }

    public static SimpleDateFormat getSimpleDateFormat() {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        return new SimpleDateFormat(myFormat, Locale.getDefault());
    }

    public static String getDisplayiableDate(String stringDate){
        try {
            SimpleDateFormat sdf = DateHelper.getSimpleDateFormat();
            Date date = sdf.parse(stringDate);
            sdf.applyPattern("dd/MM/yy");
            return  sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return stringDate;
        }
    }
}
