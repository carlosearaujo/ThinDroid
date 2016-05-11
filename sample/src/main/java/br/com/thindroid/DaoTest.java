package br.com.thindroid;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import br.com.thindroid.commons.database.GenericDao;

/**
 * Created by Carlos on 24/03/2016.
 */
@DatabaseTable
public class DaoTest extends GenericDao<DaoTest> {

    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField
    private String name;

    public DaoTest() {
        super(DaoTest.class);
    }
}
