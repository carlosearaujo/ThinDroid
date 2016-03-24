package br.com.thindroid.commons.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import br.com.thindroid.annotations.AlarmTask;

/**
 * Created by Carlos on 22/12/2015.
 */

/**
 * Send boot event when app install/upgrade.
 */
public class ImprovedBootReceiver extends BroadcastReceiver {

    private static final String ACTION_CHECK_IF_BROADCAST_OK = "com.android.commons.action.CHECK_IF_BROADCAST_OK";
    public static final String ACTION_IMPROVED_BOOT_COMPLETED_OR_APP_INSTALL = "com.android.commons.action.BOOT_COMPLETED_OR_APP_INSTALL";
    public static final long ALARM_TIME = AlarmTask.DAY;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(appReinstallOrSystemReboot(context)){
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_CHECK_IF_BROADCAST_OK), PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + ALARM_TIME, ALARM_TIME, alarmPendingIntent);
            context.sendBroadcast(new Intent(ACTION_IMPROVED_BOOT_COMPLETED_OR_APP_INSTALL));
        }
    }

    private boolean appReinstallOrSystemReboot(Context context) {
        return !AlarmUtils.alarmUp(context, ACTION_CHECK_IF_BROADCAST_OK);
    }


}
