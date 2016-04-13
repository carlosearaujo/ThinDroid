package br.com.thindroid.commons.log;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.com.thindroid.annotations.AlarmTask;
import br.com.thindroid.commons.Application;
import br.com.thindroid.commons.database.GenericDao;
import br.com.thindroid.commons.utils.HandledException;

import static br.com.thindroid.commons.Application.getContext;

/**
 * Created by Carlos on 12/08/2014.
 */
public class LogColetorReceiver{

    public static final long MINUTE = 60*1000;
    public static final long HOUR = 60*MINUTE;
    public static final long DAY = 24*HOUR;
    private static final String PREFERENCE_LOG_LEVEL = "PREFERENCE_LOG_LEVEL";
    private static final String PREFERENCE_TAG_FILTER = "PREFERENCE_TAG_FILTER";
    private static final String TAG = LogColetorReceiver.class.getSimpleName();
    private static final String PREFERENCE_DATABASE_LOGS_ACTIVATED = "PREFERENCE_DATABASE_LOGS_ACTIVATED";
    private static final String PREFERENCE_FILE_LOGS_ACTIVATED = "PREFERENCE_FILE_LOGS_ACTIVATED";

    private static SimpleDateFormat mDateFormat;

    private static String getDate(){
        if(mDateFormat == null){
            mDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        }
        return mDateFormat.format(new Date());
    }

    public static void changeLogLevel(LogLevel newLogLevel, String tagFilter) {
        SharedPreferences preferences = getPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFERENCE_LOG_LEVEL, newLogLevel != null ? newLogLevel.toString() : null);
        editor.putString(PREFERENCE_TAG_FILTER, tagFilter);
        editor.commit();
    }

    private static SharedPreferences getPreferences() {
        return getContext().getSharedPreferences(LogColetorReceiver.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    private static void saveOnFile(String fileName, String directory, String text) {
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/" + directory);
            myDir.mkdirs();
            File file = new File(myDir, fileName.replaceAll("\\.txt", "") + ".txt");
            try {
                FileWriter pw = new FileWriter(file, true);
                pw.write(text);
                pw.flush();
                pw.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        catch (Exception ex){
            Log.w(TAG, "Error when save text file", ex);
            throw new HandledException("Error when save text file");
        }
    }

    @AlarmTask(interval = 5 * AlarmTask.MINUTE)
    public static void register() {
        if(isActivated()) {
            try {
                StringBuilder log = getLog();
                if(isFileActivated()) {
                    saveOnFile(getDate(), getApplicationLogPath(), log.toString());
                }
                Runtime.getRuntime().exec("logcat -c");
            } catch (HandledException ex) {

            } catch (Exception e) {
                Log.e(TAG, "Error when register log", e);
            }
        }
    }

    private static boolean isDatabaseActivated(){
        return isLogActivated(PREFERENCE_DATABASE_LOGS_ACTIVATED);
    }

    private static boolean isLogActivated(String logType) {
        return getPreferences().getBoolean(logType, false);
    }

    private static boolean isFileActivated() {
        return isLogActivated(PREFERENCE_FILE_LOGS_ACTIVATED);
    }

    public static boolean isActivated() {
        SharedPreferences preferences = getPreferences();
        return preferences.getBoolean(PREFERENCE_DATABASE_LOGS_ACTIVATED, false) || preferences.getBoolean(PREFERENCE_FILE_LOGS_ACTIVATED, false);
    }

    private static StringBuilder getLog() throws IOException {
        BufferedReader bufferedReader = requestLogToLogCat();
        StringBuilder log = transformToString(bufferedReader);
        return log;
    }

    private static StringBuilder transformToString(BufferedReader bufferedReader) {
        boolean databaseLogsEnable = isDatabaseActivated();
        boolean isFileActivated = isFileActivated();
        try {
            StringBuilder log = new StringBuilder();
            String line;
            List<Logg> logs = new ArrayList<>();
            Calendar currentDate = Calendar.getInstance();
            while ((line = bufferedReader.readLine()) != null) {
                if (!isIgnoredData(line)) {
                    if(databaseLogsEnable && startNewMessage(line)) {
                        StringBuilder stringBuilder = new StringBuilder();
                        String header = line;
                        while ((line = bufferedReader.readLine()) != null && !messageEnd(line)) {
                            stringBuilder.append(line + "\n");
                        }
                        Logg newLog = Logg.buildLog(currentDate, header, stringBuilder);
                        if(newLog != null){
                            logs.add(newLog);
                        }
                    }
                    if(isFileActivated){
                        log.append(line + "\n");
                    }
                }
            }
            if(databaseLogsEnable){
                GenericDao.createOrUpdate(logs.toArray(new Logg[0]));
            }
            return log;
        }
        catch (Exception ex){
            Log.w(TAG, "Error when convert log to stringbuilder", ex);
            throw new HandledException("Error when convert log to stringbuilder");
        }
    }

    private static boolean messageEnd(String line) {
        return line.matches("");
    }

    private static boolean isIgnoredData(String line) {
        return line.equals("") ||
               line.contains("beginning of")
               || line.contains("E/SELinux")
               || line.contains("Debugger attempted")
               || line.contains("waiting for the debugger")
               || line.contains("ResourcesManager");
    }

    private static boolean startNewMessage(String line) {
        return line.matches("\\[ \\d{2}-\\d{2} .*");
    }

    private static BufferedReader requestLogToLogCat() {
        try {
            String loglvl = getLogLevel();
            Process process = Runtime.getRuntime().exec(String.format("logcat -d -v long %s | grep %s", "*" + loglvl,android.os.Process.myPid()));
            return new BufferedReader(new InputStreamReader(process.getInputStream()));
        }
        catch (Exception ex){
            Log.w(TAG, "Error when request log to LogCat", ex);
            throw new HandledException("Error when request log to LogCat");
        }
    }

    private static String getTagFilter() {
        return getPreferences().getString(PREFERENCE_TAG_FILTER, null);
    }

    private static String getLogLevel() {
        String loglvl = getPreferences().getString(PREFERENCE_LOG_LEVEL, LogLevel.WARNING.toString());
        return loglvl;
    }

    private static String getApplicationLogPath() {
        Context context = Application.getContext();
        PackageManager packageManager = context.getPackageManager();
        return packageManager.getApplicationLabel(context.getApplicationInfo()).toString().replaceAll(" ", "") + "Logs";
    }

    public static void activate(boolean onFile, boolean onDatabase){
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putBoolean(PREFERENCE_DATABASE_LOGS_ACTIVATED, onDatabase);
        editor.putBoolean(PREFERENCE_FILE_LOGS_ACTIVATED, onFile);
        editor.commit();
    }
}
