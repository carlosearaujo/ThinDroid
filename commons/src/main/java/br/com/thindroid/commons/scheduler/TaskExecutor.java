package br.com.thindroid.commons.scheduler;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import br.com.thindroid.commons.Application;
import br.com.thindroid.commons.utils.ThreadPoolService;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class TaskExecutor extends ThreadPoolService {

    private static final String TAG = Scheduler.TAG;

    public TaskExecutor() {
        super(true);
    }

    @Override
    protected void onFinish() {
        super.onFinish();
    }

    @Override
    public void executeOnPool(Intent intent) {
        Task task = getTask(intent);
        dispatchTask(intent, Application.getContext(), task);
    }

    private void execute(Task task) {
        try {
            Log.d(TAG, "Calling task " + task.getMethodName() + " on class " + task.getTargetClass());
            Object instance = null;
            if (!Modifier.isStatic(task.method.getModifiers())) {
                instance = task.clazz.newInstance();
            }
            task.method.invoke(instance);
            Log.d(Scheduler.TAG, "Task " + task.getIdentifier() + " executed with success");

        } catch (IllegalAccessException ex) {
            Log.e(Scheduler.TAG, "Sync error. Task: " + task.getIdentifier(), ex);
        } catch (InvocationTargetException ex){
            Log.e(Scheduler.TAG, "Sync error. Task: " + task.getIdentifier(), ex);
        } catch (InstantiationException ex) {
            Log.e(Scheduler.TAG, "Sync error. Task: " + task.getIdentifier(), ex);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Task getTask(Intent intent) {
        return (Task) intent.getBundleExtra("bundle").getSerializable("task");
    }

    private void dispatchTask(Intent intent, Context context, Task task) {
        if(task.method == null || task.clazz == null){
            Log.d(TAG, String.format("Cancel task %s. method or class null.", task.getIdentifier()));
            cancelAlarm(context, task.taskAction);
        }
        else{
            execute(task);
            Scheduler.completeWakefulIntent(intent);
        }
    }

    private void cancelAlarm(Context context, String action) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(PendingIntent.getBroadcast(context, 0, new Intent(action), PendingIntent.FLAG_UPDATE_CURRENT));
    }
}

