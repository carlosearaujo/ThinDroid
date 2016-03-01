package org.java.thingdroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class Tests extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_tests);
    }

    @AlarmTask
    private static void test(){

    }

}
