package org.java.thingdroid.commons.database;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import org.java.thingdroid.commons.log.LogsRepository;

import java.util.HashMap;
import java.util.Map;

import static org.java.thingdroid.commons.Application.getContext;

/**
 * Created by carlos.araujo on 11/04/2015.
 */
public abstract class RepositoryFactory {

    private static HashMap<String, DefaultRepository> repositories;
    private static RepositoryFactory repositoryFactory;
    public abstract DefaultRepository getRepositoryImpl(String repositoryName);

    static DefaultRepository getRepository(String repositoryName){
        if(repositoryName == LogsRepository.REPOSITORY_NAME){
            return findRepositoryOnHash(repositoryName);
        }
        if(repositoryFactory == null) {
            try {
                ApplicationInfo app = getContext().getPackageManager().getApplicationInfo(getContext().getPackageName(), PackageManager.GET_META_DATA);
                Class repositoryClass = Class.forName(app.metaData.getString("com.android.utils.commons.database.RepositoryFactory"));
                repositoryFactory = (RepositoryFactory) repositoryClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("RepositotyFactory not declared in Manifest. Use meta-data name 'com.android.utils.commons.database.RepositoryFactory'");
            }
        }
        return findRepositoryOnHash(repositoryName);
    }

    private static DefaultRepository findRepositoryOnHash(String repositoryName) {
        if(!getRepositories().containsKey(repositoryName)){
            getRepositories().put(repositoryName, repositoryName == LogsRepository.REPOSITORY_NAME ? new LogsRepository() : repositoryFactory.getRepositoryImpl(repositoryName));
        }
        return repositories.get(repositoryName);
    }

    private static Map getRepositories() {
        if(repositories == null){
            repositories = new HashMap<>();
        }
        return repositories;
    }
}
