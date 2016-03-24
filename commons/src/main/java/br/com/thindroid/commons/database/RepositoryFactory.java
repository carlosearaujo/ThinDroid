package br.com.thindroid.commons.database;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import br.com.thindroid.AnnotationResolver;
import br.com.thindroid.Repository;
import br.com.thindroid.commons.log.LogsRepository;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static br.com.thindroid.commons.Application.getContext;

/**
 * Created by carlos.araujo on 11/04/2015.
 */
public abstract class RepositoryFactory {

    private static HashMap<String, DefaultRepository> repositories;

    static DefaultRepository getRepository(String repositoryName){
        return findRepositoryOnHash(repositoryName);
    }

    private static DefaultRepository findRepositoryOnHash(String repositoryName) {
        if(!getRepositories().containsKey(repositoryName)){
            getRepositories().put(repositoryName, repositoryName == LogsRepository.REPOSITORY_NAME ? new LogsRepository() : resolveRepository(repositoryName));
        }
        DefaultRepository repository =  repositories.get(repositoryName);
        if(repository == null){
            throw new IllegalArgumentException(String.format("Repository %s not found", repositoryName));
        }
        return repository;
    }

    private static DefaultRepository resolveRepository(String repositoryName) {
        Class[] repositoriesClasses = AnnotationResolver.getResolver(Repository.class).getManagedElements();
        for(Class clazz : repositoriesClasses){
            Repository annotation = (Repository) clazz.getAnnotation(Repository.class);
            if(annotation.value().equals(repositoryName)){
                try {
                    return (DefaultRepository) clazz.newInstance();
                }catch (Exception ex){
                    throw new RuntimeException(ex);
                }
            }
        }
        return null;
    }

    private static Map<String, DefaultRepository> getRepositories() {
        if(repositories == null){
            repositories = new HashMap<>();
        }
        return repositories;
    }
}
