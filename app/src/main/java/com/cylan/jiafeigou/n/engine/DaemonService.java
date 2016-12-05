package com.cylan.jiafeigou.n.engine;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.cylan.jiafeigou.misc.NotifyManager;


public class DaemonService extends Service {

    /**
     * Creates an Service.  Invoked by your subclass's constructor.
     * <p/>
     * name Used to name the worker thread, important only for debugging.
     */
    public DaemonService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotifyManager.getNotifyManager()
                .sendNotify(NotifyManager
                        .getNotifyManager()
                        .sendDefaultEmptyNotify());
//        NotifyManager.getNotifyManager().simpleTestNotify();
        return START_STICKY;
    }


}
