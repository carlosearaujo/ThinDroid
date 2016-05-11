package br.com.thindroid.commons.log;

import android.os.Environment;

import java.io.File;

import br.com.thindroid.annotations.Repository;
import br.com.thindroid.commons.Application;
import br.com.thindroid.commons.database.DefaultRepository;
import br.com.thindroid.commons.log.nativelogs.NativeLog;

/**
 * Created by Carlos on 26/02/2016.
 */
@Repository(value = "logs", managedClassList = {NativeLog.class, RemoteLog.class})
public class LogsRepository extends DefaultRepository {

    public static final String REPOSITORY_NAME = "logs";
    private static final int DATABASE_VERSION = 1;

    public LogsRepository() {
        super(Application.getContext(), Application.getContext().getExternalCacheDir().getAbsolutePath() + File.separator + REPOSITORY_NAME, null, DATABASE_VERSION);
    }
}
