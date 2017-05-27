package com.cylan.jiafeigou.push;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.view.View;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ContextUtils;

public class WakeupService extends Service {

    public WakeupService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private int getNotificationSmallIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.mipmap.notify_empty : R.mipmap.ic_launcher;
    }

    @Override
    public int onStartCommand(Intent intent, int fla, int startId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(getNotificationSmallIcon())
                .setLargeIcon(BitmapFactory.decodeResource(ContextUtils.getContext().getResources(),
                        R.mipmap.ic_launcher))
                .setContentTitle(getString(R.string.app_name))
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, NewHomeActivity.class), 0));
        final Notification notification = builder.build();
        int smallIconId = ContextUtils.getContext().getResources().getIdentifier("right_icon", "id", android.R.class.getPackage().getName());
        if (smallIconId != 0) {
            notification.contentView.setViewVisibility(smallIconId, View.INVISIBLE);
//            notification.bigContentView.setViewVisibility(smallIconId, View.INVISIBLE);
        }
        startForeground(1, notification);
        return START_STICKY;
    }
}
