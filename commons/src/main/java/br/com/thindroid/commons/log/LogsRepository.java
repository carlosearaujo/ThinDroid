package br.com.thindroid.commons.log;

import br.com.thindroid.annotations.Repository;
import br.com.thindroid.commons.Application;
import br.com.thindroid.commons.database.DefaultRepository;

/**
 * Created by Carlos on 26/02/2016.
 */
@Repository(value = "logs", managedClassList = {Logg.class})
public class LogsRepository extends DefaultRepository {

    public static final String REPOSITORY_NAME = "logs";
    private static final int DATABASE_VERSION = 4;

    public LogsRepository() {
        super(Application.getContext(), REPOSITORY_NAME, null, DATABASE_VERSION);
    }
}
