package br.com.thindroid;

/**
 * Created by Carlos on 01/03/2016.
 */
public class Tests {
    @AlarmTask(interval = AlarmTask.MINUTE, wakeUp = true)
    public static void foo(){
        System.out.print("Executing this code every 60 seconds");
    }

    @AlarmTask(interval = AlarmTask.MINUTE, wakeUp = true)
    public static void foo2(){
        System.out.print("Executing this code every 60 seconds");
    }
}
