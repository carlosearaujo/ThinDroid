package br.com.thindroid.commons.database;

import br.com.thindroid.commons.Application;

/**
 * Created by Carlos on 14/07/2015.
 */
@br.com.thindroid.annotations.Repository(value = "default-database.db", managedClassList = {})
public class Repository extends DefaultRepository {

    private static final int DATABASE_VERSION = 1;
    public static final String REPOSITORY_NAME = "default-database.db";

    public Repository() {
        super(Application.getContext(), REPOSITORY_NAME, null, DATABASE_VERSION);
    }
}
