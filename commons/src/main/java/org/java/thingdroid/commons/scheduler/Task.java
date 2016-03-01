package org.java.thingdroid.commons.scheduler;

import android.util.Log;

import org.java.thingdroid.AlarmTask;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Created by Carlos on 23/02/2016.
 */
class Task implements Serializable{


    private static final String TAG = Task.class.getSimpleName();

    public Task(Method method, AlarmTask alarmTask, String taskAction)  {
        this.method = method;
        this.alarmTask = alarmTask;
        this.taskAction = taskAction;
        this.methodName = method.getName();
        this.className = getTargetClass();
    }

    private String methodName;
    private String className;
    Method method;
    Class clazz;
    AlarmTask alarmTask;
    String taskAction;

    private Task() {

    }

    public String getTargetClass() {
        return this.method.getDeclaringClass().getName();
    }

    public String getIdentifier() {
        return this.method.getDeclaringClass().getSimpleName() + "/" + method.getName();
    }

    private void writeObject(ObjectOutputStream oos)
            throws IOException {
        method = null;
        clazz = null;
        oos.defaultWriteObject();
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        try {
            clazz = Class.forName(className);
            if(clazz == null){
                Log.w(TAG, "Error when get task class " + className);
            }
            method = clazz.getMethod(methodName);
        } catch (Exception e) {
            Log.w(TAG, "Error when get task method on class " + className, e);
        }
    }

    public String getMethodName() {
        return methodName;
    }
}
