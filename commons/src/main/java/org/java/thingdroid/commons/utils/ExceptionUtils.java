package org.java.thingdroid.commons.utils;

/**
 * Created by Carlos on 24/08/2015.
 */
public class ExceptionUtils {
    public static boolean isCause(
            Class<? extends Throwable> expected,
            Throwable exc
    ) {
        return expected.isInstance(exc) || (
                exc != null && isCause(expected, exc.getCause())
        );
    }
}
