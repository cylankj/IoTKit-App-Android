package com.cylan.jiafeigou;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DaemonService1 extends Service {
    public DaemonService1() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
