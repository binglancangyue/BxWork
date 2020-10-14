package com.bixin.bxfm.view.activity;

import android.app.Application;

/**
 * @author Altair
 * @date :2020.01.06 下午 03:13
 * @description:
 */
public class FmApplication extends Application {
    private static FmApplication myApplication = null;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
    }

    public static FmApplication getInstance() {
        return myApplication;
    }

}
