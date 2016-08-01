package com.cylan.jiafeigou.n.base;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.cylan.jiafeigou.support.DebugOptionsImpl;
import com.cylan.utils.Constants;
import com.squareup.leakcanary.LeakCanary;

import java.io.File;

/**
 * Created by hunt on 16-5-14.
 */
public class BaseApplication extends Application {

    private static final String TAG = "BaseApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        enableDebugOptions();
        LeakCanary.install(this);
//        startService(new Intent(this, DaemonService.class));
        startService(new Intent(this, FirstTaskInitService.class));
    }

    private void enableDebugOptions() {
        DebugOptionsImpl options = new DebugOptionsImpl("test");
        options.enableCrashHandler(this, Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + Constants.ROOT_DIR + File.separator + "debug");
        options.enableStrictMode();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                //should release some resource
                Log.d(TAG, "onTrimMemory: " + level);
                break;
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "onLowMemory: ");
    }
}
