package com.cylan.jiafeigou.misc;

import android.app.Notification;
import android.app.PendingIntent;

/**
 * Created by cylan-hunt on 16-11-1.
 */

public interface INotify {

    String KEY_NEED_EMPTY_NOTIFICATION = "key_empty_notification";

    /**
     * 作为一个标志，进入某些特定的页面后，发送 {@link #sendDefaultEmptyNotify()},来清空通知栏，并且保持.
     *
     * @return
     */
    int getEmptyNotifyFlag();

    NotifyBean sendDefaultEmptyNotify();

    void sendNotify(Notification notification);


    void clearAll();

    void sendNotify(NotifyBean notifyBean);


    int BELL_NOTIFY_ID = Math.abs("bell".hashCode());
    int CAM_NOTIFY_ID = Math.abs("cam".hashCode());
    int ACCOUNT_NOTIFY_ID = Math.abs("account".hashCode());

    class NotifyBean {
        /**
         * 标题：一般是"加菲狗"
         */
        public String content;
        /**
         * 副标题：1：想家就用加菲狗，2.未接听，点击回拨。
         */
        public String subContent;
        /**
         * 事件时间，最后一条数据的时间。
         */
        public long time;
//        /**
//         * 条数
//         */
//        public int count;
        /**
         *
         */
        public PendingIntent pendingIntent;
        /**
         * 图标id;
         */
        public int resId;

        /**
         * 是否播放音效
         */
        public boolean sound;
        public boolean vibrate;

        public int notificationId = -1;

    }
}
