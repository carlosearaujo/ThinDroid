package br.com.thindroid.commons.utils;

import android.util.Log;

import java.lang.Boolean;import java.lang.Class;import java.lang.Exception;import java.lang.Object;import java.lang.String;import java.lang.reflect.Field;

/**
 * Created by Carlos on 25/02/2016.
 */
public class DataMerge {

    private static String TAG = DataMerge.class.getName();

    public static void merge(Object target, Object source){
        Class clazz = target.getClass();
        try {
            for (Field field : clazz.getDeclaredFields()) {
                Boolean isAccessible = field.isAccessible();
                try {
                    if (field.isAnnotationPresent(Mergeable.class)) {
                        field.setAccessible(true);
                        field.set(target, field.get(source));
                        field.setAccessible(isAccessible);
                    }
                }
                finally {
                    if(field != null && isAccessible != null){
                        field.setAccessible(isAccessible);
                    }
                }
            }
        }
        catch (Exception ex){
            Log.i(TAG, "Error when merge objects on class " + clazz.getSimpleName(), ex);
            throw new RuntimeException(ex);
        }
    }



    private static void testDataMerge() {
        /*ManagedApp managedApp = new ManagedApp();
        managedApp.setLocalApkUri("de bobs");
        ManagedApp managedApp1 = new ManagedApp();
        DataMerge.merge(managedApp1, managedApp);
        System.out.print("");*/
    }
}
