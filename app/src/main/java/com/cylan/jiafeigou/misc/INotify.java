package com.cylan.jiafeigou.misc;

import android.app.Notification;
import android.app.PendingIntent;
import android.view.View;

/**
 * Created by cylan-hunt on 16-11-1.
 */

public interface INotify {


    Notification sendDefaultNotify();

    void sendNotify(Notification notification);

    void updateRemoteView(View view);

    void updateTime(long time);

    void updateContent(String text);

    void updateSubContent(String text);

    void updatePendingIntent(PendingIntent intent);
}
