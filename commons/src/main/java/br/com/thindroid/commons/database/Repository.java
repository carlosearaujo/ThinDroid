package br.com.thindroid.commons.database;

import br.com.thindroid.commons.Application;
import br.com.thindroid.commons.database.DefaultRepository;

/**
 * Created by Carlos on 14/07/2015.
 */
public class Repository extends DefaultRepository {

    private static final int DATABASE_VERSION = 1;
    public static final String REPOSITORY_NAME = "default-database.db";

    public Repository() {
        super(Application.getContext(), REPOSITORY_NAME, null, DATABASE_VERSION);
    }

    @Override
    public Class[] getManagedClassList() {
        /*PUT CLASS LIST HERE*/
        return new Class[]{};
    }

    @Override
    protected String getRepositoryName() {
        return REPOSITORY_NAME;
    }
}
