package com.cylan.jiafeigou.n.base;

import android.app.Application;
import android.content.Intent;

/**
 * Created by hunt on 16-5-14.
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, FirstTaskInitService.class));
    }

}
