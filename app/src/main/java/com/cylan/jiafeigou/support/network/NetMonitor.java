package com.cylan.jiafeigou.support.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
            AppLogger.d("networkCallbackList:" + networkCallbackList);
            if (networkCallbackList != null) {
                Iterator<String> iterator = networkCallbackList.keySet().iterator();
                while (iterator.hasNext()) {
                    networkCallbackList.get(iterator.next()).onNetworkChanged(context, intent);
                }
            }
        }
    }

    private Map<String, NetworkCallback> networkCallbackList = new HashMap<>();

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
                networkCallbackList.remove(callbacks.getClass().getSimpleName());
                networkCallbackList.put(callbacks.getClass().getSimpleName(), callbacks);
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    throw new IllegalArgumentException("register wifi failed: " + e.getLocalizedMessage());
                }
            }
        }
    }

    public void unregister(NetworkCallback callback) {
        try {
//            if (network != null) ContextUtils.getContext().unregisterReceiver(network);
            if (networkCallbackList != null)
                networkCallbackList.remove(callback.getClass().getSimpleName());
            AppLogger.d("networkCallback remove:" + callback);
        } catch (Exception e) {

        }
    }
}
