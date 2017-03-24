package com.cylan.jiafeigou.misc.br;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.cylan.jiafeigou.misc.JConstant;

/**
 * Created by cylan-hunt on 16-8-3.
 */
public class TimeTickBroadcast extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        intent.setAction(JConstant.KEY_TIME_TICK_);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


}
