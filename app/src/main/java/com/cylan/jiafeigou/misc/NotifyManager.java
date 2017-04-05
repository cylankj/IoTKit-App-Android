package com.cylan.jiafeigou.misc;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.view.activity.BindDeviceActivity;
import com.cylan.jiafeigou.n.view.bell.DoorBellHomeActivity;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.RandomUtils;

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
    private WeakReference<RemoteViews> remoteView;

    public static NotifyManager getNotifyManager() {
        if (notifyManager == null)
            notifyManager = new NotifyManager();
        return notifyManager;
    }

    private final int NOTIFY_ID;

    private NotificationManagerCompat notificationManagerCompat;

    private NotifyManager() {
        this.context = new WeakReference<>(ContextUtils.getContext());
        NOTIFY_ID = Math.abs(context.get().getPackageName().hashCode());
        notificationManagerCompat = NotificationManagerCompat.from(getContext());
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

    private RemoteViews getRemoteView() {
        if (remoteView == null || remoteView.get() == null) {
            RemoteViews rv = new RemoteViews(ContextUtils.getContext().getPackageName(),
                    R.layout.layout_nomal_notify);
            remoteView = new WeakReference<>(rv);
        }
        return remoteView.get();
    }

    @Override
    public void sendNotify(Notification notification) {
        notificationManagerCompat.notify(NOTIFY_ID, notification);
    }

    /**
     * @param count
     * @return [0, 99]
     */
    private int getCount(int count) {
        return count < 0 ? 0 : (count > 99 ? 99 : count);
    }

    private int getNotificationSmallIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.mipmap.notify_empty : R.mipmap.ic_launcher;
    }

    @Override
    public void sendNotify(NotifyBean notifyBean) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
        builder.setSmallIcon(getNotificationSmallIcon());
//        builder.setContentTitle(getContext().getString(R.string.app_name));
//        builder.setContentText(getContext().getString(R.string.EFAMILY_MISSED_CALL));
        builder.setLargeIcon(BitmapFactory.decodeResource(getContext().getResources(),
                notifyBean.resId == -1 ? R.mipmap.ic_launcher : notifyBean.resId));
        final int count = getCount(notifyBean.count);
        final String title = count == 0 ? getContext().getString(R.string.app_name) :
                String.format(getContext().getString(R.string.app_name) + "(%s%s)", count, getContext().getString(R.string.DOOR_NOT_CONNECT));
        final String subTitle = count == 0 ?
                getContext().getString(R.string.Slogan) : getContext().getString(R.string.EFAMILY_MISSED_CALL);
        builder.setContentTitle(title);
        builder.setContentText(subTitle);
        if (notifyBean.pendingIntent != null)
            builder.setContentIntent(notifyBean.pendingIntent);
        final Notification notification = builder.build();
        int smallIconId = getContext().getResources().getIdentifier("right_icon", "msgId", android.R.class.getPackage().getName());
        if (smallIconId != 0) {
            notification.contentView.setViewVisibility(smallIconId, View.INVISIBLE);
        }
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notificationManagerCompat.notify(NOTIFY_ID, notification);
    }


    public void simpleTestNotify() {
        Observable.interval(1000, 5000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(new Action1<Long>() {
                    public void call(Long aLong) {
                        Class<?> what[] = {NewHomeActivity.class, DoorBellHomeActivity.class, BindDeviceActivity.class};
                        // here is the task that should repeat
                        NotifyBean notifyBean = new NotifyBean();
                        final Intent intent = new Intent(getContext(), what[RandomUtils.getRandom(3)]);
                        intent.putExtra(INotify.KEY_NEED_EMPTY_NOTIFICATION, "NICE_NAME");
                        //PendingIntent.FLAG_UPDATE_CURRENT,更新intent
                        notifyBean.pendingIntent = PendingIntent.getActivity(getContext(), 0, intent,
                                PendingIntent.FLAG_ONE_SHOT);
                        notifyBean.count = RandomUtils.getRandom(500);
                        notifyBean.time = System.currentTimeMillis() - RandomUtils.getRandom(1000) * 1000;
                        notifyBean.sound = true;
                        sendNotify(notifyBean);
                        Log.d("simpleTestNotify", "simpleTestNotify: ");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("wth: " + throwable.getLocalizedMessage());
                    }
                });
    }
}
