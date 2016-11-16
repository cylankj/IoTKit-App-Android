package com.cylan.jiafeigou.misc.br;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.support.rxbus.RxBus;

/**
 * Created by cylan-hunt on 16-8-3.
 */
public class TimeTickBroadcast extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (RxBus.getCacheInstance().hasObservers()) {
            RxBus.getCacheInstance().post(new RxEvent.TimeTickEvent());
        }
    }


}
