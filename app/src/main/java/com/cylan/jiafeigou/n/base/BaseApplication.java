package com.cylan.jiafeigou.n.base;

import android.app.Application;
import android.content.Intent;

import com.cylan.support.DswLog;

/**
 * Created by hunt on 16-5-14.
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, FirstTaskInitService.class));
        DswLog.i("BaseApplication create");
    }

}
