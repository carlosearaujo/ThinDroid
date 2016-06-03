package br.com.thindroid.commons.scheduler;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import br.com.thindroid.commons.Application;
import br.com.thindroid.commons.log.RemoteLog;
import br.com.thindroid.commons.utils.ThreadPoolService;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class TaskExecutor extends ThreadPoolService {

    PowerManager powerManager = (PowerManager) Application.getContext().getSystemService(POWER_SERVICE);

    private static final String TAG = Scheduler.TAG;

    public TaskExecutor() {}

    @Override
    public void executeOnPool(Intent intent) {
        Task task = getTask(intent);
        try {
            dispatchTask(Application.getContext(), task);
        }
        catch (Exception ex){
            if(task.quietly) {
                RemoteLog.w(Scheduler.TAG + " Sync error. Task: " + task.getIdentifier(), ex);
            }
            else{
                throw new RuntimeException(ex);
            }
        }
    }

    private void execute(Task task) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        PowerManager.WakeLock mWakeLock = null;
        try {
            if(task.wakeUp){
                mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, task.getIdentifier());
                mWakeLock.acquire();
            }
            Log.d(TAG, "Calling task " + task.getMethodName() + " on class " + task.getTargetClass());
            Object instance = null;
            if (!Modifier.isStatic(task.method.getModifiers())) {
                instance = task.clazz.newInstance();
            }
            task.method.invoke(instance);
            Log.d(Scheduler.TAG, "Task " + task.getIdentifier() + " executed with success");

        }
        finally {
            releaseLock(mWakeLock);
        }
    }

    private void releaseLock(PowerManager.WakeLock mWakeLock) {
        try {
            if(mWakeLock != null) {
                mWakeLock.release();
            }
        }
        catch (Exception ex){
            Log.i(TAG, "Error when release lock", ex);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Task getTask(Intent intent) {
        return (Task) intent.getBundleExtra("bundle").getSerializable("task");
    }

    private void dispatchTask(Context context, Task task) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        if(task.method == null || task.clazz == null){
            Log.d(TAG, String.format("Cancel task %s. method or class null.", task.getIdentifier()));
            cancelAlarm(context, task.taskAction);
        }
        else{
            execute(task);
        }
    }

    private void cancelAlarm(Context context, String action) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(PendingIntent.getBroadcast(context, 0, new Intent(action), PendingIntent.FLAG_UPDATE_CURRENT));
    }
}

