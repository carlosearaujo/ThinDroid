package br.com.thindroid.commons.log;

import br.com.thindroid.commons.Application;
import br.com.thindroid.commons.database.DefaultRepository;

/**
 * Created by Carlos on 26/02/2016.
 */
public class LogsRepository extends DefaultRepository {

    public static final String REPOSITORY_NAME = "logs";
    private static final int DATABASE_VERSION = 4;

    public LogsRepository() {
        super(Application.getContext(), REPOSITORY_NAME, null, DATABASE_VERSION);
    }

    @Override
    public Class[] getManagedClassList() {
        return new Class[]{Logg.class};
    }

    @Override
    protected String getRepositoryName() {
        return REPOSITORY_NAME;
    }
}
