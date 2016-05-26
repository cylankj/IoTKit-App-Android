package com.cylan.jiafeigou.n.engine;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.cylan.jiafeigou.support.stat.MtaManager;
import com.cylan.sdkjni.JfgCmd;


public class DataSourceService extends Service {
    private static final String TAG = "DataSourceService";

    static {
        System.loadLibrary("jfgsdk");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initNative();
        JfgCmd.getJfgCmd(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void initNative() {
        Log.d(TAG, "let's go initNative:");
        MtaManager.customEvent(this, "DataSourceService", "NativeInit");
    }

}
