package com.cylan.jiafeigou.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.cylan.jiafeigou.R;

/**
 * author：hebin on 2015/10/31 13:43
 * email：hebin@cylan.com.cn
 */
public class NotificationUtil {

    public static void notifycation(Context context, int id, int icon, String title, String text, boolean isSound, boolean isVibrate, Intent intent) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(text);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setAutoCancel(true);
        if (isSound) {
            mBuilder.setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.tips));
        }
        if (isVibrate) {
            long[] vibrate = {0, 100, 200, 300};
            mBuilder.setVibrate(vibrate);
        }
        mBuilder.setLights(Color.BLUE, 3000, 3000);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                context, id, intent, 0);
        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager.notify(id, mBuilder.build());
    }

    public static void cancelNotifycationById(Context ctx, int notificatonId) {
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(notificatonId);
    }

    public static void cancelAllNotifycation(Context ctx) {
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();
    }

}
