package com.cylan.jiafeigou.push;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;

public class WakeupService extends Service {

    public WakeupService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int fla, int startId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, NewHomeActivity.class), 0));
        startForeground(1, builder.build());
        return START_STICKY;
    }
}
