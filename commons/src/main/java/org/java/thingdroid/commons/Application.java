package org.java.thingdroid.commons;

import android.content.Context;
import android.content.Intent;

/**
 * Created by carlos.araujo on 08/01/2015.
 * Fix AsyncTask 'never ends' - Preload class problem. See: https://code.google.com/p/android/issues/detail?id=20915
 */
public class Application extends android.app.Application {

    private static Context applicationContext;
    public static final String ACTION_LIBRARY_START = "com.utils.commons.ACTION_LIBRARY_START";

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        notifyLibraryAndAppStart();
        try {
            Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException e) {
        }
    }

    private void notifyLibraryAndAppStart() {
        sendBroadcast(new Intent(ACTION_LIBRARY_START));
        sendBroadcast(new Intent(getActionAppStart()));
    }

    public static String getActionAppStart() {
        return getContext().getPackageName() + ".ACTION_APP_START";
    }

    public static Context getContext(){
        if(applicationContext == null){
            throw new RuntimeException("Application Context is null. You extends lib Application?");
        }
        return applicationContext;
    }
}
