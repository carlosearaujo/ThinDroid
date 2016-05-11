package br.com.thindroid.commons.log.nativelogs;

import android.database.Cursor;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import br.com.thindroid.commons.database.GenericDao;

/**
 * Created by Carlos on 26/02/2016.
 */
@DatabaseTable
public class NativeLog extends GenericDao<NativeLog> {

    private static final String COLUMN_TIME = "timeInMilli";
    private static final String COLUMN_LOGLEVEL = "logLevel";
    private static final String TAG = NativeLog.class.getName();

    private static SimpleDateFormat mDateFormatter;

    public NativeLog(){
        super(NativeLog.class);
    }

    private NativeLog(String className, long time, LogLevel logLevel, String message) {
        this();
        this.timeInMilli = time;
        this.logLevel = logLevel;
        this.message = message;
        this.className = className;
    }

    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField(columnName = COLUMN_TIME, index = true)
    private long timeInMilli;
    @DatabaseField(columnName = COLUMN_LOGLEVEL)
    private LogLevel logLevel;
    @DatabaseField
    private String message;
    @DatabaseField
    private String className;

    public static Cursor getLogsAsCursor(Calendar start, Calendar end, LogLevel logLevel){
        try {
            QueryBuilder<NativeLog, ?> queryBuilder = buildQuery(start, end, logLevel);
            return getRepository(NativeLog.class).getReadableDatabase().rawQuery(queryBuilder.prepareStatementString(), null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static QueryBuilder<NativeLog, ?> buildQuery(Calendar start, Calendar end, LogLevel logLevel) throws SQLException {
        Dao<NativeLog, ?> dao = getDao(NativeLog.class);
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where().between(COLUMN_TIME, start.getTimeInMillis(), end.getTimeInMillis()).and().in(COLUMN_LOGLEVEL, logLevel.getAssociatedLevelsSet());
        return queryBuilder;
    }

    static NativeLog buildLog(Calendar currentDate, String header, StringBuilder log) {
        String[] headerData = header.split(" ");
        try {
            NativeLog newLog = new NativeLog(extractClassName(headerData), extractTime(currentDate, headerData), extractLogLevel(headerData), log.toString());
            return newLog;
        }
        catch (Exception ex){
            android.util.Log.w(TAG, "Error when save log", ex);
            return null;
        }
    }

    private static String extractClassName(String[] headerData) {
        String className = headerData[headerData.length - 2].replaceFirst("./", "");
        return className;
    }

    private static LogLevel extractLogLevel(String[] headerData) {
        String logLevelStr = headerData[headerData.length - 2].substring(0, 1);
        switch (logLevelStr){
            case "V":
                return LogLevel.VERBOSE;
            case "D":
                return LogLevel.DEBUG;
            case "I":
                return LogLevel.INFORMATION;
            case "W":
                return LogLevel.WARNING;
            case "E":
                return LogLevel.ERROR;
            default:
                return null;
        }
    }

    private static long extractTime(Calendar currentDate, String[] headerData) throws ParseException {
        return getDateFormatter().parse(getYear(currentDate, headerData) + "-" + headerData[1] + headerData[2]).getTime();
    }

    private static int getYear(Calendar currentDate, String[] headerData) {
        if(!getMonth(headerData[1]).equals(currentDate.get(Calendar.MONTH)) && currentDate.get(Calendar.MONTH) == Calendar.JANUARY){
            return currentDate.get(Calendar.YEAR) - 1;
        }
        return currentDate.get(Calendar.YEAR);
    }

    private static Integer getMonth(String monthAndDay) {
        return Integer.valueOf(monthAndDay.split("-")[0]);
    }

    private static SimpleDateFormat getDateFormatter() {
        if(mDateFormatter == null){
            mDateFormatter = new SimpleDateFormat("yyyy-MM-ddhh:mm:ss.SSS");
        }
        return mDateFormatter;
    }
}
