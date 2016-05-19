package com.cylan.jiafeigou.n.engine;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DataSourceService extends Service {


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initNative();
        return START_STICKY;
    }

    private void initNative() {

    }
}
