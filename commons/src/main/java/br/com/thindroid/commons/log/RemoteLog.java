package br.com.thindroid.commons.log;

import android.database.Cursor;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import br.com.thindroid.commons.database.GenericDao;
import br.com.thindroid.commons.log.nativelogs.NativeLog;

/**
 * Created by Carlos on 21/04/2016.
 */
@DatabaseTable
public class RemoteLog extends GenericDao<RemoteLog> {

    public static final String COLUMN_TIME = "timeInMilli";
    public static final String COLUMN_LOGLEVEL = "logLevel";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_TAG = "tag";

    @DatabaseField(generatedId = true)
    private Long id;
    @DatabaseField(columnName = COLUMN_LOGLEVEL)
    private LogLevel logLevel;
    @DatabaseField(columnName = COLUMN_MESSAGE)
    private String message;
    @DatabaseField(columnName = COLUMN_TAG)
    private String tag;
    @DatabaseField(columnName = COLUMN_TIME)
    private long timeInMilli;

    public RemoteLog() {
        super(RemoteLog.class);
    }

    private RemoteLog(LogLevel logLevel, String tag, String message, long timeInMilli) {
        this();
        this.logLevel = logLevel;
        this.tag = tag;
        this.message = message;
        this.timeInMilli = timeInMilli;
    }

    //region Get and Set
    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public long getTimeInMilli() {
        return timeInMilli;
    }

    public void setTimeInMilli(long timeInMilli) {
        this.timeInMilli = timeInMilli;
    }

    public Long getId() {
        return id;
    }

    //endregion

    //region Register log methods
    public static void v(Class clazz, Exception ex){
        save(LogLevel.VERBOSE, clazz, ex);
    }

    public static void v(Class clazz, String message){
        save(LogLevel.VERBOSE, clazz, message);
    }

    public static void v(String tag, String message){
        save(LogLevel.VERBOSE, tag, message);
    }

    public static void v(String tag, Exception ex){
        save(LogLevel.VERBOSE, tag, ex);
    }

    public static void d(Class clazz, Exception ex){
        save(LogLevel.DEBUG, clazz, ex);
    }

    public static void d(Class clazz, String message){
        save(LogLevel.DEBUG, clazz, message);
    }

    public static void d(String tag, String message){
        save(LogLevel.DEBUG, tag, message);
    }

    public static void d(String tag, Exception message){
        save(LogLevel.DEBUG, tag, message);
    }

    public static void i(Class clazz, Exception ex){
        save(LogLevel.INFORMATION, clazz, ex);
    }

    public static void i(Class clazz, String message){
        save(LogLevel.INFORMATION, clazz, message);
    }

    public static void i(String tag, String message){
        save(LogLevel.INFORMATION, tag, message);
    }

    public static void i(String tag, Exception ex){
        save(LogLevel.INFORMATION, tag, ex);
    }

    public static void w(Class clazz, Exception ex){
        save(LogLevel.WARNING, clazz, ex);
    }

    public static void w(Class clazz, String message){
        save(LogLevel.WARNING, clazz, message);
    }

    public static void w(String tag, String message){
        save(LogLevel.WARNING, tag, message);
    }

    public static void w(String tag, Exception ex){
        save(LogLevel.WARNING, tag, ex);
    }

    public static void e(Class clazz, Exception ex){
        save(LogLevel.ERROR, clazz, ex);
    }

    public static void e(Class clazz, String message){
        save(LogLevel.ERROR, clazz, message);
    }

    public static void e(String tag, String message){
        save(LogLevel.ERROR, tag, message);
    }

    public static void e(String tag, Exception ex){
        save(LogLevel.WARNING, tag, ex);
    }

    private static void save(LogLevel logLevel, Class clazz, Exception ex) {
        if(clazz == null){
            throw new IllegalArgumentException("Class cannot be null");
        }
        save(logLevel, clazz.getName(), ex);
    }

    private static void save(LogLevel logLevel, String tag, Exception ex) {
        if(ex == null){
            throw new IllegalArgumentException("Exception cannot be null");
        }
        save(logLevel, tag, Log.getStackTraceString(ex));
    }

    private static void save(LogLevel logLevel, Class clazz, String message){
        save(logLevel, clazz.getName(), message);
    }

    @SuppressWarnings("WrongConstant")
    private static void save(LogLevel logLevel, String tag, String message){
        Log.println(logLevel.getPriority(), tag, message);
        RemoteLog remoteLog = new RemoteLog(logLevel, tag, message, System.currentTimeMillis());
        remoteLog.createOrUpdate();
    }
    //endregion

    //region Query
    public static Cursor getLogsAsCursor(Calendar start, Calendar end, LogLevel logLevel){
        return getLogsAsCursor(start, end, logLevel, null);
    }

    public static Cursor getLogsAsCursor(Calendar start, Calendar end, LogLevel logLevel, String tagFilter){
        try {
            QueryBuilder<RemoteLog, ?> queryBuilder = buildQuery(start, end, logLevel, tagFilter);
            return getRepository(NativeLog.class).getReadableDatabase().rawQuery(queryBuilder.prepareStatementString(), null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<RemoteLog> getLogs(Calendar start, Calendar end, LogLevel logLevel){
        return getLogs(start, end, logLevel, null);
    }

    public static List<RemoteLog> getLogs(Calendar start, Calendar end, LogLevel logLevel, String tagFilter){
        Dao<RemoteLog, ?> dao = getDao(RemoteLog.class);
        try {
            QueryBuilder<RemoteLog, ?> queryBuilder = buildQuery(dao, start, end, logLevel, tagFilter);
            return dao.query(queryBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static QueryBuilder<RemoteLog, ?> buildQuery(Calendar start, Calendar end, LogLevel logLevel, String tagFilter) throws SQLException {
        return buildQuery(getDao(RemoteLog.class), start, end, logLevel, tagFilter);
    }

    private static QueryBuilder<RemoteLog, ?> buildQuery(Dao<RemoteLog, ?> dao, Calendar start, Calendar end, LogLevel logLevel, String tagFilter) throws SQLException {
        QueryBuilder queryBuilder = dao.queryBuilder();
        Where where = queryBuilder.where();
        where.between(COLUMN_TIME, start.getTimeInMillis(), end.getTimeInMillis());
        where.and().in(COLUMN_LOGLEVEL, logLevel.getAssociatedLevelsSet());
        if(tagFilter != null){
            where.and().eq(COLUMN_TAG, tagFilter);
        }
        return queryBuilder;
    }

    public static void removeOldLogs(Calendar since){
        Dao<RemoteLog, Object> loggDao = new LogsRepository().getDao(RemoteLog.class);
        DeleteBuilder<RemoteLog, Object> deleteBuilder = loggDao.deleteBuilder();
        try {
            deleteBuilder.where().le("timeInMilli", since.getTimeInMillis());
            loggDao.delete(deleteBuilder.prepare());
        } catch (SQLException e) {
            RemoteLog.w(RemoteLog.class, e);
        }
    }

    public static void removeOldLogs(long millisecondsAgo){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, (int) -millisecondsAgo);
        removeOldLogs(calendar);
    }
    //endregion
}
