package com.accessibility;

import android.app.Application;
import android.content.Context;

public class MyApp extends Application {

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        //初始化辅助功能基类
        AccessibilitySampleService.getInstance().init(getApplicationContext());
    }
}