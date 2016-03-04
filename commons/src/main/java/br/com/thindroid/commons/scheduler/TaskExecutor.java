package br.com.thindroid.commons.scheduler;

import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import br.com.thindroid.AlarmTask;
import br.com.thindroid.commons.Application;

import java.lang.reflect.Modifier;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class TaskExecutor extends IntentService {

    private static final int POOL_SIZE = Runtime.getRuntime().availableProcessors();

    static final String ACTION_EXECUTE_TASK = "ACTION_EXECUTE_TASK";
    private static final String TAG = Scheduler.TAG;
    public static final int CHECK_POOL_STATUS_TIME = 5 * 1000;

    private static ThreadPoolExecutor threadPoolExecutor;
    private static Handler mHandler;
    private static Boolean isStarting = false;

    public TaskExecutor() {
        super("TaskExecutor");
        isStarting = true;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mHandler = new Handler();
        BlockingQueue<Runnable> mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();
        threadPoolExecutor = new ThreadPoolExecutor(POOL_SIZE, 3 * POOL_SIZE, 1, TimeUnit.SECONDS, mDecodeWorkQueue);
        isStarting = false;
        synchronized (isStarting) {
            isStarting.notify();
        }
        Task task = null;
        try {
            Log.d(Scheduler.TAG, "Starting Service");
            task = (Task) intent.getSerializableExtra("task");
            execute(task);
            waitEmptyPool();
            Log.d(Scheduler.TAG, "Service exit - All tasks executed.");
        } catch (Exception ex) {
            Log.w(Scheduler.TAG, "Sync error. Task: " + task.getIdentifier(), ex);
        } finally {
            Scheduler.completeWakefulIntent(intent);
        }
    }

    private void waitEmptyPool() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                synchronized (threadPoolExecutor) {
                    if (threadPoolExecutor.getActiveCount() == 0) {
                        threadPoolExecutor.shutdown();
                        mHandler.getLooper().quit();
                    } else {
                        mHandler.postDelayed(this, CHECK_POOL_STATUS_TIME);
                    }
                }
            }
        }, CHECK_POOL_STATUS_TIME);
        Looper.loop();
    }

    static void executeTask(final Task task){
        if(isStarting != null && isStarting){
            waitServiceStart();
        }
        if(threadPoolExecutor == null){
            startService(task);
        }
        else{
            synchronized (threadPoolExecutor) {
                if(threadPoolExecutor.isShutdown()){
                    startService(task);
                }
                else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            execute(task);
                        }
                    });
                }
            }
        }
    }

    private static void waitServiceStart() {
        synchronized (isStarting) {
            try {
                isStarting.wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void startService(Task task) {
        Intent serviceIntent = new Intent(Application.getContext(), TaskExecutor.class);
        serviceIntent.setAction(TaskExecutor.ACTION_EXECUTE_TASK);
        serviceIntent.putExtra("task", task);
        Scheduler.startWakefulService(Application.getContext(), serviceIntent);
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

    private static void execute(final Task task){

        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "Calling task " + task.getMethodName() + " on class " + task.getTargetClass());
                    Object instance = null;
                    if (!Modifier.isStatic(task.method.getModifiers())) {
                        instance = task.clazz.newInstance();
                    }
                    task.method.invoke(instance);
                    Log.d(Scheduler.TAG, "Task " + task.getIdentifier() + " executed with success");

                }
                catch (Exception ex){
                    Log.w(Scheduler.TAG, "Sync error. Task: " + task.getIdentifier(), ex);
                }
            }
        });
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

