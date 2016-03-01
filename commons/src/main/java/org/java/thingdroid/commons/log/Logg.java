package org.java.thingdroid.commons.log;

import android.database.Cursor;

import org.java.thingdroid.commons.database.GenericDao;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Carlos on 26/02/2016.
 */
@DatabaseTable
public class Logg extends GenericDao<Logg> {

    private static final String COLUMN_TIME = "timeInMilli";
    private static final String COLUMN_LOGLEVEL = "logLevel";
    private static final String TAG = Logg.class.getName();

    private static SimpleDateFormat mDateFormatter;

    public Logg(){
        super(Logg.class);
    }

    private Logg(String className, long time, LogLevel logLevel, String message) {
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

    @Override
    public Long getId() {
        return id;
    }

    @Override
    protected String getRepositoryName() {
        return LogsRepository.REPOSITORY_NAME;
    }

    public static Cursor getLogsAsCursor(Calendar start, Calendar end, LogLevel logLevel){
        try {
            QueryBuilder<Logg, ?> queryBuilder = buildQuery(start, end, logLevel);
            return getRepository(LogsRepository.REPOSITORY_NAME).getReadableDatabase().rawQuery(queryBuilder.prepareStatementString(), null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static QueryBuilder<Logg, ?> buildQuery(Calendar start, Calendar end, LogLevel logLevel) throws SQLException {
        Dao<Logg, ?> dao = getDao(Logg.class, LogsRepository.REPOSITORY_NAME);
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where().between(COLUMN_TIME, start.getTimeInMillis(), end.getTimeInMillis()).and().eq(COLUMN_LOGLEVEL, logLevel);
        return queryBuilder;
    }

    static Logg buildLog(String header, StringBuilder log) {
        String[] headerData = header.split(" ");
        try {
            Logg newLog = new Logg(extractClassName(headerData), extractTime(headerData), extractLogLevel(headerData), log.toString());
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

    private static long extractTime(String[] headerData) throws ParseException {
        return getDateFormatter().parse(headerData[1] + headerData[2]).getTime();
    }

    private static SimpleDateFormat getDateFormatter() {
        if(mDateFormatter == null){
            mDateFormatter = new SimpleDateFormat("MM-ddhh:mm:ss.SSS");
        }
        return mDateFormatter;
    }
}
