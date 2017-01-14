package com.cylan.jiafeigou.support.download.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by hunt on 16-4-26.
 */
public class NetEnv implements NetInterface {


    final static String TAG = "NetEnv";
    private NetConfig.Builder netConfig;
    InnerNetBroadcastReceiver innerNetBroadcastReceiver;

    public NetEnv(NetConfig.Builder netConfig) {
        this.netConfig = netConfig;
    }

    /**
     * 注册网络广播
     */
    public void register() {
        try {
            innerNetBroadcastReceiver = new InnerNetBroadcastReceiver(this);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
            netConfig.getContext().registerReceiver(innerNetBroadcastReceiver, intentFilter);
        } catch (Exception e) {
            Log.w(TAG, e.toString());
        }
    }

    /**
     * 反注册网络广播
     */
    public void unregister() {
        try {
            if (innerNetBroadcastReceiver != null)
                netConfig.getContext().unregisterReceiver(innerNetBroadcastReceiver);
        } catch (Exception e) {
            Log.w(TAG, e.toString());
        }
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnected(int type) {
        Context context = netConfig.getContext();
        final int allowType = netConfig.getAllowNetType();
        if (type == allowType) {
//            LocalBroadcastManager.getInstance(context).sendBroadcast();
        }
    }


    @Override
    public void onChanged(int type) {

    }

    private static class InnerNetBroadcastReceiver extends BroadcastReceiver {

        WeakReference<NetInterface> netInterface;

        public InnerNetBroadcastReceiver(NetInterface netInterface) {
            this.netInterface = new WeakReference<>(netInterface);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            NetInterface anInterface = netInterface.get();
            if (anInterface == null)
                return;
        }
    }
}
