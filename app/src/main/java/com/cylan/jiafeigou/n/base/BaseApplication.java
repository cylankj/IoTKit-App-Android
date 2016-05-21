package com.cylan.jiafeigou.n.base;

import android.app.Application;
import android.content.Intent;
import android.os.StrictMode;
import android.util.Log;

import com.cylan.BuildConfig;
import com.cylan.jiafeigou.n.support.DaemonService;

/**
 * Created by hunt on 16-5-14.
 */
public class BaseApplication extends Application {

    static final String TAG = "BaseApplication";

    @Override
    public void onCreate() {
        enableStrictMode(BuildConfig.DEBUG);
        super.onCreate();
        Log.d(TAG, "application onCreate");
        startService(new Intent(this, DaemonService.class));
        startService(new Intent(this, FirstTaskInitService.class));
    }

    private void enableStrictMode(boolean enable) {
        if (!enable)
            return;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .detectAll()
                .penaltyLog()
                .penaltyDialog()
                .build());
    }
}
