package br.com.thindroid.commons.log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carlos.araujo on 14/11/2014.
 */
public enum LogLevel {
    VERBOSE, DEBUG, INFORMATION, WARNING, ERROR;

    @Override
    public String toString() {
        if(this.equals(VERBOSE)){
            return ":V";
        }
        if(this.equals(DEBUG)){
            return ":D";
        }
        if(this.equals(INFORMATION)){
            return ":I";
        }
        if(this.equals(WARNING)){
            return ":W";
        }if(this.equals(ERROR)){
            return ":E";
        }
        return null;
    }

    public List<LogLevel> getAssociatedLevelsSet() {
        List<LogLevel> associatedLevels = new ArrayList<>();
        boolean findThisLevelOnList = false;
        for(LogLevel logLevel : LogLevel.values()){
            if(this.equals(logLevel)){
                findThisLevelOnList = true;
            }
            if(findThisLevelOnList){
                associatedLevels.add(logLevel);
            }
        }
        return associatedLevels;
    }
}
