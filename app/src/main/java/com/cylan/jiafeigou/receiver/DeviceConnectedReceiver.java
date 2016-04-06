package com.cylan.jiafeigou.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

/**
 * 接收其他用户已接听的广播，用于防止收到呼叫请求短时间内收到26消息
 * Created by yangc on 2016/3/17.
 */
public class DeviceConnectedReceiver extends BroadcastReceiver {

    private Handler mHandler = null;
    private int msgType = 0;

    public DeviceConnectedReceiver(Handler mHandler, int msgType) {
        this.mHandler = mHandler;
        this.msgType = msgType;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("ID_BELL_CONNECTED"))
            if (mHandler != null && mHandler.hasMessages(msgType)){
                mHandler.sendEmptyMessageDelayed(msgType, 500);
            }
    }
}
