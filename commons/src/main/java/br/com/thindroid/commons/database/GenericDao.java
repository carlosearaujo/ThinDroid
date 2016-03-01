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
        deleteAll(typeParamClass, getRepositoryName());
    }

    public static void deleteAll(Class clazz, String repositoryName) {
        getRepository(repositoryName).deleteAll(clazz);
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
        return getAll(typeParamClass, getRepositoryName());
    }

    public static <D> List<D> getAll(Class<D> clazz, String repositoryName){
        Dao<D, Object> dao = getRepository(repositoryName).getDao(clazz);
        try {
            return dao.queryForAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long getCount() {
        return getCount(typeParamClass, getRepositoryName());
    }

    protected static long getCount(Class clazz, String repositoryName) {
        Dao dao = getRepository(repositoryName).getDao(clazz);
        try {
            return dao.countOf();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected Dao getDao(){
        return getDao(typeParamClass, getRepositoryName());
    }

    protected static <E> Dao<E, ?> getDao(Class<E> clazz, String repositoryName){
        return getRepository(repositoryName).getDao(clazz);
    }

    public static <E> E findById(Class<E> clazz,String repositoryName, long id) {
        Dao<E, Object> dao = getRepository(repositoryName).getDao(clazz);
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
        notifyDataChange(changeType, getClass());
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

    protected static DefaultRepository getRepository(String repositoryName){
        return RepositoryFactory.getRepository(repositoryName);
    }

    protected abstract String getRepositoryName();

    public void clearDatabase(){
        try {
            getRepository(getRepositoryName()).clearDatabase();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
