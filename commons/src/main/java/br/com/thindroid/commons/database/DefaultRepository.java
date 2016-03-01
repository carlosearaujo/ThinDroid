package br.com.thindroid.commons.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Created by carlos.araujo on 19/12/2014.
 */
public abstract class DefaultRepository extends OrmLiteSqliteOpenHelper {

    public DefaultRepository(Context context, String databaseName, SQLiteDatabase.CursorFactory factory, int databaseVersion) {
        super(context, databaseName, factory, databaseVersion);
    }

    public abstract Class[] getManagedClassList();

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        createAllTables(connectionSource);
    }

    private void createAllTables(ConnectionSource connectionSource) {
        try {
            Class[] classes = getManagedClassList();
            for(Class clazz : classes){
                TableUtils.createTable(connectionSource, clazz);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        dropAllTables(connectionSource);
        onCreate(database, connectionSource);
    }

    private void dropAllTables(ConnectionSource connectionSource) {
        Class[] classes = getManagedClassList();
        for(Class clazz : classes){
            try {
                TableUtils.dropTable(connectionSource, clazz, true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public <D extends Dao<T, ?>, T> D getDao(Class<T> clazz) {
        try {
            D dao = super.getDao(clazz);
            dao.executeRaw("PRAGMA foreign_keys = ON");
            return dao;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    void deleteAll(Class clazz) {
        getWritableDatabase().execSQL("DELETE FROM " + clazz.getSimpleName());
    }

    protected void clearDatabase() throws Exception{
        TransactionManager.callInTransaction(connectionSource, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                dropAllTables(connectionSource);
                createAllTables(connectionSource);
                return null;
            }
        });
    }

    protected abstract String getRepositoryName();
}
