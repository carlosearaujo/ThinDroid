package br.com.thindroid.commons.database;

import java.util.HashMap;
import java.util.Map;

import br.com.thindroid.annotations.AnnotationResolver;
import br.com.thindroid.annotations.Repository;
import br.com.thindroid.commons.log.LogsRepository;

/**
 * Created by carlos.araujo on 11/04/2015.
 */
class RepositoryFactory {

    private static HashMap<String, DefaultRepository> repositories;

    static DefaultRepository resolveRepository(Class entity){
        DefaultRepository hashResult = findOnHash(entity);
        if(hashResult != null){
            return hashResult;
        }
        else {
            DefaultRepository repository = findOnAllRepositories(entity);
            if(repository == null) {
                throw new IllegalArgumentException(String.format("Repository for entity %s not found", entity.getName()));
            }
            addRepositoryOnHash(repository);
            return repository;
        }
    }

    static DefaultRepository getRepositoryByName(String repositoryName){
        if(!getRepositories().containsKey(repositoryName)){
            addRepositoryOnHash(resolveRepository(repositoryName));
        }
        DefaultRepository repository =  repositories.get(repositoryName);
        if(repository == null){
            throw new IllegalArgumentException(String.format("Repository %s not found", repositoryName));
        }
        return repository;
    }

    private static void addRepositoryOnHash(DefaultRepository defaultRepository) {
        getRepositories().put(defaultRepository.getClass().getAnnotation(Repository.class).value(), defaultRepository);
    }

    private static DefaultRepository findOnAllRepositories(Class entity) {
        if(DefaultRepository.manageEntity(LogsRepository.class, entity)){
            return new LogsRepository();
        }
        Class<? extends DefaultRepository>[] repositoriesClasses = AnnotationResolver.getResolver(Repository.class).getManagedElements();
        for(Class<? extends DefaultRepository> repositoryClass : repositoriesClasses){
            try {
                if(DefaultRepository.manageEntity(repositoryClass, entity)){
                    return repositoryClass.newInstance();
                }
            }
            catch (Exception ex){
                throw new RuntimeException(ex);
            }
        }
        return null;
    }

    private static DefaultRepository findOnHash(Class entity) {
        for(DefaultRepository repository : getRepositories().values()){
            if(repository.manageEntity(entity)){
                return repository;
            }
        }
        return null;
    }

    private static DefaultRepository resolveRepository(String repositoryName) {
        Class[] repositoriesClasses = AnnotationResolver.getResolver(Repository.class).getManagedElements();
        for(Class clazz : repositoriesClasses){
            Repository repositoryAnnotation = (Repository) clazz.getAnnotation(Repository.class);
            if(repositoryAnnotation.value().equals(repositoryName)){
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
