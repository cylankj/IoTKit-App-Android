package com.cylan.jiafeigou.support.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cylan-hunt on 17-2-23.
 */

public class NetMonitor {
    private Network network;

    private NetMonitor() {
        network = new Network();
    }

    private static NetMonitor netMonitor;


    public static NetMonitor getNetMonitor() {
        if (netMonitor == null) {
            synchronized (NetMonitor.class) {
                if (netMonitor == null)
                    netMonitor = new NetMonitor();
            }
        }
        return netMonitor;
    }


    private class Network extends BroadcastReceiver {

        public Network() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("networkCallbackList", "networkCallbackList:" + networkCallbackList);
            if (networkCallbackList != null)
                for (NetworkCallback callback : networkCallbackList)
                    callback.onNetworkChanged(context, intent);
        }
    }

    private List<NetworkCallback> networkCallbackList = new ArrayList<>();

    public interface NetworkCallback {
        void onNetworkChanged(Context context, Intent intent);
    }


    public void registerNet(NetworkCallback callbacks, String[] actions) {
        if (actions != null && actions.length > 0) {
            if (network == null)
                network = new Network();
            try {
                IntentFilter intentFilter = new IntentFilter();
                for (String action : actions)
                    intentFilter.addAction(action);
                ContextUtils.getContext().registerReceiver(network, intentFilter);
                if (!networkCallbackList.contains(callbacks))
                    networkCallbackList.add(callbacks);
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    throw new IllegalArgumentException("register wifi failed: " + e.getLocalizedMessage());
                }
            }
        }
    }

    public void unregister(NetworkCallback callback) {
        try {
            if (network != null) ContextUtils.getContext().unregisterReceiver(network);
            if (networkCallbackList != null) networkCallbackList.remove(callback);
            Log.d("networkCallbackList", "networkCallbackList:unregister");
        } catch (Exception e) {

        }
    }
}
