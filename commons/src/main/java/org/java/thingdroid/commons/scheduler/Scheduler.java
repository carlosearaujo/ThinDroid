package org.java.thingdroid.commons.scheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import org.java.thingdroid.AlarmTask;
import org.java.thingdroid.AnnotationResolver;
import org.java.thingdroid.commons.Application;
import org.java.thingdroid.commons.log.LogColetorReceiver;

import java.lang.reflect.Method;

import static org.java.thingdroid.commons.Application.getContext;

/**
 * Created by Carlos on 14/05/2014.
 */
public class Scheduler extends WakefulBroadcastReceiver {

    public static final int MAX_TASKS = 15;
    private Context mContext;

    static final String TAG = Scheduler.class.getName();
    private static final String ACTION_EXECUTE = "com.utils.commons.scheduler.action.EXECUTE_TASK";

    private Task[] tasks = new Task[MAX_TASKS];
    private int tasksCount = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        mContext = context;
        if(isAlarmCall(action)) {
            dispatchTask((Task) intent.getSerializableExtra("task"));
        }
        else {
            resolveBootStrapActions(action);
        }
    }

    private void dispatchTask(Task task) {
        if(task.method == null || task.clazz == null){
            Log.d(TAG, String.format("Cancel task %s. method or class null.", task.getIdentifier()));
            cancelAlarm(task.taskAction);
        }
        else{
            Log.d(TAG, "Calling task " + task.getMethodName() + " on class " + task.getTargetClass());
            Intent serviceIntent = new Intent(mContext, TaskExecutor.class);
            serviceIntent.setAction(TaskExecutor.ACTION_EXECUTE_TASK);
            serviceIntent.putExtra("task", task);
            Log.d(TAG, "Adding Task " + task.getMethodName() + " in TaskExecutor Queue");
            startWakefulService(mContext, serviceIntent);
        }
    }

    private void cancelAlarm(String action) {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(PendingIntent.getBroadcast(mContext, 0, new Intent(action), PendingIntent.FLAG_UPDATE_CURRENT));
    }

    private void resolveBootStrapActions(String action) {
        AnnotationResolver scheduleResolver = getScheduleResolver();
        if(scheduleResolver != null) {
            Class[] managedClasses = scheduleResolver.getManagedClasses();
            obtainTasks(new Class[]{LogColetorReceiver.class});
            obtainTasks(managedClasses);
            logFoundedTasks();
            if (action.equals(Intent.ACTION_BOOT_COMPLETED) || alarmNotRunning(tasks)) {
                scheduleTasks();
            }
        }
    }

    private void logFoundedTasks() {
        StringBuilder tasksStr = new StringBuilder("Founded tasks: ");
        for(Task task : tasks){
            if(task != null) {
                tasksStr.append(task.getIdentifier() + " ; ");
            }
        }
        Log.d(TAG, tasksStr.toString());
    }

    private boolean alarmNotRunning(Task[] tasks) {
        for(Task task : tasks){
            if(task != null && !alarmUp(task)){
                return true;
            }
        }
        return false;
    }

    private boolean alarmUp(Task task) {
        boolean alarmUp = (PendingIntent.getBroadcast(mContext, 0,
                new Intent(task.taskAction),
                PendingIntent.FLAG_NO_CREATE) != null);
        return alarmUp;
    }

    private void scheduleTasks() {
        cancelAllAlarms();
        if(tasks.length > 0){
            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            for(Task task : tasks) {
                if(task != null) {
                    scheduleTask(alarmManager, task);
                }
            }
        }
    }

    private static void scheduleTask(AlarmManager alarmManager, Task task, boolean forceWakeUp) {
        Intent intent = buildAlarmIntent(task);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(getAlarmType(task, forceWakeUp), SystemClock.elapsedRealtime(), task.alarmTask.interval(), alarmIntent);
    }

    private static int getAlarmType(Task task, boolean forceWakeUp) {
        return task.alarmTask.wakeUp() || forceWakeUp ? AlarmManager.ELAPSED_REALTIME_WAKEUP : AlarmManager.ELAPSED_REALTIME;
    }

    private void scheduleTask(AlarmManager alarmManager, Task task) {
        scheduleTask(alarmManager, task, false);
    }

    static void scheduleTaskWakeUp(Task task){
        scheduleTask((AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE), task, true);
    }

    private static Intent buildAlarmIntent(Task task) {
        Intent intent = new Intent(task.taskAction);
        intent.putExtra("task", task);
        return intent;
    }

    private boolean isAlarmCall(String action) {
        return !(Intent.ACTION_BOOT_COMPLETED.equals(action) || Application.ACTION_LIBRARY_START.equals(action));
    }

    private void obtainTasks(Class[] managedClasses) {
        for (Class managedClass : managedClasses) {
            for(Method method : managedClass.getDeclaredMethods()){
                checkMethodTask(method);
            }
        }
    }

    private void cancelAllAlarms() {
        for(int i = 1; i <= MAX_TASKS ; i++){
            cancelAlarm(ACTION_EXECUTE.concat("_".concat(String.valueOf(i))));
        }
    }

    private boolean checkMethodTask(Method method) {
        AlarmTask alarmTask = method.getAnnotation(AlarmTask.class);
        if(alarmTask != null && alarmTask.interval() >= AlarmTask.MINUTE){
            if(method.getParameterTypes().length > 0){
                Log.w(Scheduler.class.getSimpleName(), "Ignoring task " + method + ". Method cannot have params.");
            }
            else{
                tasks[tasksCount] = new Task(method, alarmTask, getNextActionAvailable());
                return true;
            }
        }
        return false;
    }

    private String getNextActionAvailable() {
        tasksCount++;
        if(tasksCount > MAX_TASKS){
            throw new RuntimeException("Scheduling more than max (" + MAX_TASKS + " tasks)");
        }
        return ACTION_EXECUTE.concat("_").concat(String.valueOf(tasksCount));
    }

    private AnnotationResolver getScheduleResolver() {
        try {
            Class<AnnotationResolver> schedulerResolver = (Class<AnnotationResolver>) Class.forName("org.java.thingdroid.AlarmTaskResolver");
            AnnotationResolver scheduleResolverInstance =  schedulerResolver.newInstance();
            return scheduleResolverInstance;
        }
        catch (Exception ex){
            Log.w(Scheduler.class.getSimpleName() ,
                    "ScheduleResolver not declared in Manifest. Use meta-data name 'com.android.utils.commons.ScheduleResolver'", ex);
            return null;
        }
    }
}
