package br.com.thindroid;

import android.app.Activity;
import android.os.Bundle;

public class ActivityTests extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_tests);
        test();
    }


    private static void test(){
        new DaoTest().createOrUpdate();
    }

}
