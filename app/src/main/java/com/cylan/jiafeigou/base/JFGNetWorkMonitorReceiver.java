package com.cylan.jiafeigou.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class JFGNetWorkMonitorReceiver extends BroadcastReceiver {
    public JFGNetWorkMonitorReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

//        if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
////                BSToast.showLong(context, "网络不可以用");
//            AppLogger.d("当前网络不可用");
//            RxBus.getCacheInstance().post(new RxEvent.NetConnectionEvent(false));
//            //改变背景或者 处理网络的全局变量
//        } else {
//            //改变背景或者 处理网络的全局变量
//            AppLogger.d("当前网络可用");
//            RxBus.getCacheInstance().post(new RxEvent.NetConnectionEvent(true));
//        }
    }
}
