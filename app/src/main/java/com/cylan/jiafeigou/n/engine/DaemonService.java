package com.cylan.jiafeigou.n.engine;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.RemoteViews;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.SmartcallActivity;


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
        //http://www.jianshu.com/p/426d85f34561
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.notify_empty);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.remote_view_status_bar);
        builder.setContent(remoteViews);
        builder.setContentTitle("content title");
        builder.setTicker("ticker");
        builder.setContentText("content text");
        final Intent notificationIntent = new Intent(this, SmartcallActivity.class);
        final PendingIntent pi = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(pi);
        final Notification notification = builder.build();
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        getNotificationManagerCompat().notify(getApplicationContext().getPackageName().hashCode(), notification);
        startForeground(getApplicationContext().getPackageName().hashCode(), notification);
        return START_STICKY;
    }

    private NotificationManagerCompat getNotificationManagerCompat() {
        return NotificationManagerCompat.from(getApplicationContext());
    }

}
