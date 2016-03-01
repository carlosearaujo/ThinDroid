package org.java.thingdroid.commons.log;

/**
 * Created by carlos.araujo on 14/11/2014.
 */
public enum LogLevel {
    VERBOSE, DEBUG, INFORMATION, WARNING, ERROR;

    @Override
    public String toString() {
        if(this.equals(VERBOSE)){
            return "*:V";
        }
        if(this.equals(DEBUG)){
            return "*:D";
        }
        if(this.equals(INFORMATION)){
            return "*:I";
        }
        if(this.equals(WARNING)){
            return "*:W";
        }if(this.equals(ERROR)){
            return "*:E";
        }
        return null;
    }
}
