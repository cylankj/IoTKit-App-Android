package com.cylan.jiafeigou.push;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class WakeupService extends Service {

    public WakeupService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, new Notification.Builder(this).build());//怎么设置都没用干脆什么也不设置了
        return START_STICKY;
    }
}
