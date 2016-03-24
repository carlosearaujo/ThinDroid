package br.com.thindroid.commons.database;

import android.content.Context;
import android.content.Intent;

import br.com.thindroid.commons.Application;
import com.j256.ormlite.dao.Dao;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by carlos.araujo on 13/03/2015.
 */


@SuppressWarnings("unchecked")
public abstract class GenericDao<T extends GenericDao> implements Serializable {

    private transient Class<T> typeParamClass;

    public GenericDao(Class<T> clazz){
        typeParamClass = clazz;
    }

    public abstract <T extends Serializable>  T getId();

    public void createOrUpdate(){
        try {
            getDao().createOrUpdate(this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(){
        delete(this);
    }

    public static <T extends GenericDao>  void delete(final T... data){
        if(data != null && data.length > 0){
            try {
                data[0].getDao().delete(Arrays.asList(data));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void deleteAll(){
        deleteAll(typeParamClass);
    }

    public static void deleteAll(Class clazz) {
        getRepository(clazz).deleteAll(clazz);
    }

    public static <T extends GenericDao> void createOrUpdate(final T... data){
        if(data.length > 0){
            final Dao dao = data[0].getDao();
            try {
                dao.callBatchTasks(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        for(T obj : data){
                            dao.createOrUpdate(obj);
                        }
                        return null;
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<T> getAll(){
        return getAll(typeParamClass);
    }

    public static <D> List<D> getAll(Class<D> clazz){
        Dao<D, Object> dao = getRepository(clazz).getDao(clazz);
        try {
            return dao.queryForAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long getCount() {
        return getCount(typeParamClass);
    }

    protected static long getCount(Class clazz) {
        Dao dao = getRepository(clazz).getDao(clazz);
        try {
            return dao.countOf();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected Dao getDao(){
        return getDao(typeParamClass);
    }

    protected static <E> Dao<E, ?> getDao(Class<E> clazz){
        return getRepository(clazz).getDao(clazz);
    }

    public static <E> E findById(Class<E> clazz, long id) {
        Dao<E, Object> dao = getRepository(clazz).getDao(clazz);
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void notifyDataChange(ChangeType changeType, Class clazz) {
        Context appContext = Application.getContext();
        Intent notifyDataChangeIntent = new Intent();
        notifyDataChangeIntent.setAction(clazz.getName());
        notifyDataChangeIntent.putExtra("type", changeType.toString());
        appContext.sendBroadcast(notifyDataChangeIntent);
    }

    public void notifyDataChange(ChangeType changeType) {
        notifyDataChange(changeType, typeParamClass);
    }

    public static void notifyDataChange(Class clazz, ChangeType changeType) {
        Context appContext = Application.getContext();
        Intent notifyDataChangeIntent = new Intent();
        notifyDataChangeIntent.setAction(clazz.getName());
        notifyDataChangeIntent.putExtra("content", changeType.toString());
        appContext.sendBroadcast(notifyDataChangeIntent);
    }

    public static String getContentChangeFilter(Class<? extends GenericDao> clazz){
        return clazz.getName();
    }

    protected static DefaultRepository getRepository(Class entityClass){
        return RepositoryFactory.resolveRepository(entityClass);
    }

    protected DefaultRepository getRepository(){
        return RepositoryFactory.resolveRepository(typeParamClass);
    }

    public void clearDatabase(){
        try {
            getRepository().clearDatabase();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
