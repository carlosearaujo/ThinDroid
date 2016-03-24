package br.com.thindroid.commons.scheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import br.com.thindroid.annotations.AlarmTask;
import br.com.thindroid.annotations.AnnotationResolver;
import br.com.thindroid.commons.Application;
import br.com.thindroid.commons.log.LogColetorReceiver;

import static br.com.thindroid.commons.Application.getContext;

/**
 * Created by Carlos on 14/05/2014.
 */
public class Scheduler extends WakefulBroadcastReceiver {

    private static final int MAX_TASKS = 20;
    private Context mContext;

    static final String TAG = Scheduler.class.getName();
    private static final String ACTION_EXECUTE = "br.com.thindroid.scheduler.action.EXECUTE_TASK";

    List<Task> tasks = new ArrayList<>();
    private int tasksCount = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if(isAlarmCall(intent.getAction())){
            resolveAlarmCall(context, intent);
        }
        else {
            resolveBootStrapActions();
        }
    }

    private void resolveAlarmCall(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, TaskExecutor.class);
        serviceIntent.putExtra("bundle", intent.getBundleExtra("bundle"));
        startWakefulService(context, serviceIntent);
    }

    private void resolveBootStrapActions() {
        obtainTasks();
        logFoundedTasks();
        scheduleTasks();
    }

    private void logFoundedTasks() {
        StringBuilder tasksStr = new StringBuilder("Founded tasks: ");
        for(Task task : tasks){
            tasksStr.append(task.getIdentifier() + " ; ");
        }
        Log.d(TAG, tasksStr.toString());
    }

    private void removeAlarmsRunning() {
        Iterator<Task> iterator = tasks.iterator();
        while (iterator.hasNext()){
            Task task = iterator.next();
            if(alarmUp(task)){
                iterator.remove();
            }
        }
    }

    private boolean alarmUp(Task task) {
        boolean alarmUp = (PendingIntent.getBroadcast(mContext, 0,
                new Intent(task.taskAction),
                PendingIntent.FLAG_NO_CREATE) != null);
        return alarmUp;
    }

    private void scheduleTasks() {
        if(!tasks.isEmpty()){
            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            for(Task task : tasks) {
                scheduleTask(alarmManager, task);
            }
        }
    }

    private static void scheduleTask(AlarmManager alarmManager, Task task, boolean forceWakeUp) {
        PendingIntent alarmIntent = buildAlarmIntent(task);
        alarmManager.setRepeating(getAlarmType(task, forceWakeUp), SystemClock.elapsedRealtime(), task.alarmInterval, alarmIntent);
    }

    private static int getAlarmType(Task task, boolean forceWakeUp) {
        return task.wakeUp || forceWakeUp ? AlarmManager.ELAPSED_REALTIME_WAKEUP : AlarmManager.ELAPSED_REALTIME;
    }

    private void scheduleTask(AlarmManager alarmManager, Task task) {
        scheduleTask(alarmManager, task, task.wakeUp);
    }

    private static PendingIntent buildAlarmIntent(Task task) {
        Intent intent = new Intent(task.taskAction);
        Bundle mBundle = new Bundle();
        mBundle.setClassLoader(Task.class.getClassLoader());
        mBundle.putSerializable("task", task);
        intent.putExtra("bundle", mBundle);
        return PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void obtainTasks() {
        AnnotationResolver scheduleResolver = AnnotationResolver.getResolver(AlarmTask.class);
        Method[] managedMethods = scheduleResolver.getManagedElements();
        List<Method> managedClassesList = new ArrayList<>(Arrays.asList(managedMethods));
        try {
            managedClassesList.add(LogColetorReceiver.class.getMethod("register"));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        for (Method method : managedClassesList) {
            checkMethodTask(method);
        }
        removeAlarmsRunning();
    }

    private boolean checkMethodTask(Method method) {
        AlarmTask alarmTask = method.getAnnotation(AlarmTask.class);
        if(alarmTask != null && alarmTask.interval() >= AlarmTask.MINUTE){
            if(method.getParameterTypes().length > 0){
                Log.w(Scheduler.class.getSimpleName(), "Ignoring task " + method + ". Method cannot have params.");
            }
            else{
                tasks.add(new Task(method, alarmTask, getNextActionAvailable()));
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

    private boolean isAlarmCall(String action) {
        return !(Intent.ACTION_BOOT_COMPLETED.equals(action) || Application.ACTION_LIBRARY_START.equals(action));
    }
}
