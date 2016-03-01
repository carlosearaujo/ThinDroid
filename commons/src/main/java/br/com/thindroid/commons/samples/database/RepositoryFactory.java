package br.com.thindroid.commons.samples.database;

import br.com.thindroid.commons.database.DefaultRepository;

/**
 * Created by Carlos on 14/07/2015.
 */

public class RepositoryFactory extends br.com.thindroid.commons.database.RepositoryFactory {

    @Override
    public DefaultRepository getRepositoryImpl(String repositoryName) {
        if(repositoryName == Repository.REPOSITORY_NAME) {
            return new Repository();
        }
        return null;
    }
}
