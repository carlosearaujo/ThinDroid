package br.com.thindroid.commons.utils;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Carlos on 09/03/2016.
 */
public abstract class ThreadPoolService extends Service {

    private static final String TAG = ThreadPoolService.class.getName();
    public static final int CHECK_POOL_STATUS_TIME = 5 * 1000;
    private static final int POOL_SIZE = Runtime.getRuntime().availableProcessors();

    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(POOL_SIZE, 3 * POOL_SIZE, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    private Handler mHandler;
    private PowerManager.WakeLock wakeLock;
    private boolean wakeUp = false;

    public ThreadPoolService(boolean wakeUp){
        this.wakeUp = wakeUp;
    }

    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {
        configureService(intent);
        executeTask(intent);
        return START_NOT_STICKY;
    }

    private void executeTask(final Intent intent) {
        threadPoolExecutor.execute(new Runnable() {
            private Intent mIntent = intent;

            @Override
            public void run() {
                try {
                    executeOnPool(mIntent);
                } catch (Exception ex) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ex);
                }
            }
        });
    }

    private void configureService(Intent intent) {
        if(mHandler == null) {
            mHandler = new Handler();
            Log.d(TAG, "Starting Service");
            if(wakeUp && (wakeLock == null || !wakeLock.isHeld())) {
                wakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
                wakeLock.acquire();
            }
            waitEmptyPool();
        }
    }

    private void waitEmptyPool() {
        mHandler.postDelayed(new ThreadPoolChecker(), CHECK_POOL_STATUS_TIME);
    }

    private synchronized boolean validateEmptyPool() {
        if (threadPoolExecutor.getActiveCount() == 0) {
            threadPoolExecutor.shutdownNow();
            stopSelf();
            onFinish();
            return true;
        }
        return false;
    }

    protected void onFinish() {
        if(wakeLock.isHeld()){
            wakeLock.release();
        }
    }

    public abstract void executeOnPool(Intent intent);

    class ThreadPoolChecker implements Runnable{
        @Override
        public void run() {
            if(!validateEmptyPool()){
                mHandler.postDelayed(this, CHECK_POOL_STATUS_TIME);
            }
        }
    }
}
