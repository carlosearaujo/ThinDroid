package org.java.thingdroid.commons.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Carlos on 05/02/2016.
 */
public class DateUtils {
    private static SimpleDateFormat mDateFormat;
    private static SimpleDateFormat mDateCompleteFormat;

    private static SimpleDateFormat getOnlyDayFormat(){
        if(mDateFormat == null){
            mDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            mDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        return mDateFormat;
    }

    private static SimpleDateFormat getCompleteDateFormat(){
        if(mDateCompleteFormat == null){
            mDateCompleteFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            mDateCompleteFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        return mDateCompleteFormat;
    }

    public static String onlyDayFormatInGMT(Date date){
        return getOnlyDayFormat().format(date);
    }

    public static String completeFormatInGMT(Date date){
        return getCompleteDateFormat().format(date);
    }

    public static long getTimeInMilliGMT(){
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        return cal.getTimeInMillis();
    }

    public static SimpleDateFormat getGMTZeroDateFormat(String pattern){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return simpleDateFormat;
    }

    public static Calendar getGMTZeroCalendar(){
        return Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    }
}
