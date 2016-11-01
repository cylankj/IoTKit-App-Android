package com.cylan.jiafeigou.misc;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

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

    private NotificationManagerCompat notificationManagerCompat;

    private NotifyManager() {
        this.context = new WeakReference<>(ContextUtils.getContext());
        NOTIFY_ID = context.get().getPackageName().hashCode();
        notificationManagerCompat = NotificationManagerCompat.from(getContext());
    }

    private Context getContext() {
        if (this.context == null)
            context = new WeakReference<>(ContextUtils.getContext());
        return context.get();
    }

    @Override
    public Notification sendDefaultNotify() {
        //http://www.jianshu.com/p/426d85f34561
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
        builder.setSmallIcon(R.mipmap.notify_empty);
        builder.setContentTitle(getContext().getResources().getString(R.string.app_name));
        final Intent notificationIntent = new Intent(getContext(), NewHomeActivity.class);
        final PendingIntent pi = PendingIntent.getActivity(getContext(), 0, notificationIntent, 0);
        builder.setContentIntent(pi);
        final Notification notification = builder.build();
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        return notification;
    }

    @Override
    public void sendNotify(Notification notification) {
        notificationManagerCompat.notify(NOTIFY_ID, notification);
    }

    @Override
    public void updateRemoteView(View view) {

    }

    @Override
    public void updateTime(long time) {

    }

    @Override
    public void updateContent(String text) {

    }

    @Override
    public void updateSubContent(String text) {

    }

    @Override
    public void updatePendingIntent(PendingIntent intent) {

    }

    private void simpleNotify() {
        Observable.interval(1000, 5000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(new Action1<Long>() {
                    public void call(Long aLong) {
                        // here is the task that should repeat
                    }
                });
    }
}
