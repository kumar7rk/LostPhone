package com.geeky7.rohit.lostphone;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

/**
 * Created by Rohit on 25/08/2016.
 */
public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}