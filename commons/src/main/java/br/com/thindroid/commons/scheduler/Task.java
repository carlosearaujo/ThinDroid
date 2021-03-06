package br.com.thindroid.commons.scheduler;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;

import br.com.thindroid.annotations.AlarmTask;

/**
 * Created by Carlos on 23/02/2016.
 */
public class Task implements Serializable{

    private static final String TAG = Task.class.getSimpleName();

    private String methodName;
    private String className;
    Method method;
    Class clazz;
    long alarmInterval;
    boolean wakeUp;
    String taskAction;
    boolean quietly;

    public Task(Method method, AlarmTask alarmTask, String taskAction)  {
        this.method = method;
        alarmInterval = alarmTask.interval();
        this.wakeUp = alarmTask.wakeUp();
        this.taskAction = taskAction;
        this.methodName = method.getName();
        this.className = getTargetClass();
        this.quietly = alarmTask.quietly();
    }

    public Task() {

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
