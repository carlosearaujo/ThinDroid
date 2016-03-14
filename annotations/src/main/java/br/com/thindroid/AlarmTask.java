package br.com.thindroid;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Carlos on 23/12/2015.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AlarmTask{

    long MINUTE = 60 * 1000, FIVE_MINUTES = 5* MINUTE, THIRTY_MINUTES = 30 * MINUTE, ONE_HOUR = 60 * MINUTE, DAY = 24 *ONE_HOUR, INFINITE = -1;

    long interval() default INFINITE;
    boolean wakeUp() default false;
}
