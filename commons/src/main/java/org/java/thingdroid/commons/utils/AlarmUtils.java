package org.java.thingdroid.commons.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Carlos on 23/12/2015.
 */
public class AlarmUtils {
    public static boolean alarmUp(Context context, String action) {
        return PendingIntent.getBroadcast(context, 0, new Intent(action), PendingIntent.FLAG_NO_CREATE) != null;
    }
}
