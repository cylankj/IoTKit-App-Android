package com.cylan.jiafeigou.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;

import cylan.log.DswLog;
import com.cylan.publicApi.JniPlay;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.engine.MyService;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.Utils;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final int MSG_DISCONNCET = 0x01;

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
                || intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)
                || intent.getAction().equals(Intent.ACTION_USER_PRESENT)
                || intent.getAction().equals(ClientConstants.ACTION_PULL_SERVICE)
                ) {
            if (!Utils.isServerRunning(context, MyService.class.getName())) {
                context.startService(new Intent(context, MyService.class));
            }
        }
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isAvailable()) {
                if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                } else if (netInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
                } else if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                }
                DswLog.v("connect net:" + netInfo.getExtraInfo());
            } else {
                if (!PreferenceUtil.getIsLogout(context)) {
                    DswLog.v("Logout");
                    mHandler.sendEmptyMessageDelayed(MSG_DISCONNCET, 1500);
                }
            }
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_DISCONNCET)
                JniPlay.DisconnectFromServer();
        }
    };

}
