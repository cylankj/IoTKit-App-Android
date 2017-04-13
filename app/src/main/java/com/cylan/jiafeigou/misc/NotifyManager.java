package com.cylan.jiafeigou.misc;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.view.View;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.lang.ref.WeakReference;

/**
 * Created by cylan-hunt on 16-11-1.
 */

public class NotifyManager implements INotify {

    private static NotifyManager notifyManager;
    private WeakReference<Context> context;

    public static NotifyManager getNotifyManager() {
        if (notifyManager == null)
            notifyManager = new NotifyManager();
        return notifyManager;
    }

    private final int NOTIFY_ID;


    private NotifyManager() {
        this.context = new WeakReference<>(ContextUtils.getContext());
        NOTIFY_ID = Math.abs(context.get().getPackageName().hashCode());
    }

    private Context getContext() {
        if (this.context == null)
            context = new WeakReference<>(ContextUtils.getContext());
        return context.get();
    }

    @Override
    public int getEmptyNotifyFlag() {
        return ContextUtils.getContext().getPackageName().hashCode();
    }

    @Override
    public NotifyBean sendDefaultEmptyNotify() {
        NotifyBean notifyBean = new NotifyBean();
        final Intent intent = new Intent(getContext(), NewHomeActivity.class);
        intent.putExtra(INotify.KEY_NEED_EMPTY_NOTIFICATION, "NICE_NAME");
        notifyBean.pendingIntent = PendingIntent.getActivity(getContext(), 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        return notifyBean;
    }

    @Override
    public void sendNotify(Notification notification) {
        NotificationManager mNotificationManager = (NotificationManager) ContextUtils.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFY_ID, notification);
    }


    private int getNotificationSmallIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.mipmap.notify_empty : R.mipmap.ic_launcher;
    }

    @Override
    public void sendNotify(NotifyBean notifyBean) {
        if (notifyBean == null || notifyBean.notificationId == -1)
            throw new IllegalArgumentException("notifyBean.notificationId cannot be  -1");
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
        builder.setSmallIcon(getNotificationSmallIcon());
        builder.setLargeIcon(BitmapFactory.decodeResource(getContext().getResources(),
                notifyBean.resId == -1 ? R.mipmap.ic_launcher : notifyBean.resId));
        builder.setContentTitle(notifyBean.content);
        builder.setContentText(notifyBean.subContent);
        if (notifyBean.pendingIntent != null)
            builder.setContentIntent(notifyBean.pendingIntent);
        if (notifyBean.sound) {
            builder.setSound(Uri.parse("android.resource://" + ContextUtils.getContext().getPackageName() + "/" + R.raw.tips));
        }
        if (notifyBean.vibrate) {
            long[] vibrate = {0, 100, 200, 300};
            builder.setVibrate(vibrate);
        }
        builder.setLights(Color.BLUE, 3000, 3000);
        final Notification notification = builder.build();
        int smallIconId = getContext().getResources().getIdentifier("right_icon", "id", android.R.class.getPackage().getName());
        if (smallIconId != 0) {
            notification.contentView.setViewVisibility(smallIconId, View.INVISIBLE);
//            notification.bigContentView.setViewVisibility(smallIconId, View.INVISIBLE);
        }
        notification.sound = Uri.parse("android.resource://" + ContextUtils.getContext().getPackageName() + "/" + R.raw.tips);
        notification.vibrate = new long[]{0, 100, 200, 300};
//        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
//        notification.flags |= Notification.FLAG_NO_CLEAR;
//        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager mNotificationManager = (NotificationManager) ContextUtils.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFY_ID + notifyBean.notificationId, notification);
    }

}
