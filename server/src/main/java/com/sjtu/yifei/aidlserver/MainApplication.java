package com.sjtu.yifei.aidlserver;

import android.app.Application;

import com.sjtu.yifei.IBridge;

/**
 * [description]
 * author: yifei
 * created at 18/6/3 下午8:40
 */

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        IBridge.init(this, "com.sjtu.yifei.aidlserver");
    }
}
