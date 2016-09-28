package com.puwang.camerademo;

import android.app.Application;
import android.util.DisplayMetrics;

/**
 * Created by Leo on 2016/9/27.
 */

public class MyApplication extends Application{

    protected static MyApplication       mInstance;
    private DisplayMetrics     displayMetrics = null;
    public int getScreenHeight() {
        if (this.displayMetrics == null) {
            setDisplayMetrics(getResources().getDisplayMetrics());
        }
        return this.displayMetrics.heightPixels;
    }

    public int getScreenWidth() {
        if (this.displayMetrics == null) {
            setDisplayMetrics(getResources().getDisplayMetrics());
        }
        return this.displayMetrics.widthPixels;
    }

    public void setDisplayMetrics(DisplayMetrics DisplayMetrics) {
        this.displayMetrics = DisplayMetrics;
    }

    public static MyApplication getApp() {
        if (mInstance != null && mInstance instanceof MyApplication) {
            return (MyApplication) mInstance;
        } else {
            mInstance = new MyApplication();
            mInstance.onCreate();
            return (MyApplication) mInstance;
        }
    }
}
