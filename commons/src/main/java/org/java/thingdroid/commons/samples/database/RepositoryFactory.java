package org.java.thingdroid.commons.samples.database;

import org.java.thingdroid.commons.database.DefaultRepository;

/**
 * Created by Carlos on 14/07/2015.
 */

public class RepositoryFactory extends org.java.thingdroid.commons.database.RepositoryFactory {

    @Override
    public DefaultRepository getRepositoryImpl(String repositoryName) {
        if(repositoryName == Repository.REPOSITORY_NAME) {
            return new Repository();
        }
        return null;
    }
}
