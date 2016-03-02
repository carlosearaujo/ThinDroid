package br.com.thindroid.commons.scheduler;

import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import br.com.thindroid.AlarmTask;
import br.com.thindroid.commons.Application;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class TaskExecutor extends IntentService {

    static final String ACTION_EXECUTE_TASK = "ACTION_EXECUTE_TASK";

    public TaskExecutor() {
        super("TaskExecutor");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Task task = null;
        try {
            task = (Task) intent.getSerializableExtra("task");
            execute(task);
            Log.d(Scheduler.TAG, "Task " + task.getIdentifier() + " executed with success");
        }
        catch (Exception ex){
            Log.w(Scheduler.TAG, "Sync error. Task: " + task.getIdentifier(), ex);
        }
        finally {
            Scheduler.completeWakefulIntent(intent);
        }
    }

    private void checkAlarmMaxLazyWait(Task task) {
        try {
            long maxLazyWait = task.maxLazyWait;
            if (maxLazyWait != AlarmTask.INFINITE) {
                long nextAlarmTime = getNextAlarmTime();
                if(nextAlarmTime > System.currentTimeMillis() + maxLazyWait){
                    Scheduler.scheduleTaskWakeUp(task);
                }
            }
        }
        catch (Exception ex){
            Log.v(Scheduler.TAG, "Ignored error", ex);
            //Ignore
        }
    }

    private long getNextAlarmTime() {
        long nextAlarm;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            nextAlarm = getNextAlarm(Application.getContext());
        }
        else{
            AlarmManager alarmManager = (AlarmManager) Application.getContext().getSystemService(ALARM_SERVICE);
            nextAlarm = alarmManager.getNextAlarmClock().getTriggerTime();
        }
        return nextAlarm;
    }

    private void execute(Task task) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Object instance = null;
        if(!Modifier.isStatic(task.method.getModifiers())) {
            instance = task.clazz.newInstance();
        }
        task.method.invoke(instance);
    }

    public static long getNextAlarm(Context context) {
        DateFormatSymbols symbols = new DateFormatSymbols();
        Map<String, Integer> map = new HashMap<>();
        String[] dayNames = symbols.getShortWeekdays();
        map.put(dayNames[Calendar.MONDAY],Calendar.TUESDAY);
        map.put(dayNames[Calendar.TUESDAY],Calendar.WEDNESDAY);
        map.put(dayNames[Calendar.WEDNESDAY],Calendar.THURSDAY);
        map.put(dayNames[Calendar.THURSDAY],Calendar.FRIDAY);
        map.put(dayNames[Calendar.FRIDAY],Calendar.SATURDAY);
        map.put(dayNames[Calendar.SATURDAY],Calendar.SUNDAY);
        map.put(dayNames[Calendar.SUNDAY],Calendar.MONDAY);
        String nextAlarm = Settings.System.getString(context.getContentResolver(),Settings.System.NEXT_ALARM_FORMATTED);
        if ((nextAlarm==null) || ("".equals(nextAlarm))) {
            return -1;
        }
        String nextAlarmDay = nextAlarm.split(" ")[0];
        int alarmDay = map.get(nextAlarmDay);

        Date now = new Date();
        String dayOfWeek = new SimpleDateFormat("EE", Locale.getDefault()).format(now);
        int today = map.get(dayOfWeek);
        int daysToAlarm = alarmDay-today;
        if (daysToAlarm<0) daysToAlarm+=7;

        try {
            Calendar cal2 = Calendar.getInstance();
            String str = cal2.get(Calendar.YEAR)+"-"+(cal2.get(Calendar.MONTH)+1)+"-"+(cal2.get(Calendar.DAY_OF_MONTH));

            SimpleDateFormat df  = new SimpleDateFormat("yyyy-MM-d hh:mm");

            cal2.setTime(df.parse(str+nextAlarm.substring(nextAlarm.indexOf(" "))));
            cal2.add(Calendar.DAY_OF_YEAR, daysToAlarm);
            return cal2.getTime().getTime();
        } catch (Exception e) {

        }
        return -1;
    }
}
