package com.cylan.jiafeigou.support.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.utils.ContextUtils;

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
            if (networkCallback != null) networkCallback.onNetworkChanged(context, intent);
        }
    }

    private NetworkCallback networkCallback;

    public interface NetworkCallback {
        void onNetworkChanged(Context context, Intent intent);
    }

    /**
     * 用来计数,以防忘记反注册
     */
    private int registerCount;

    public void registerNet(NetworkCallback callbacks, String[] actions) {
        if (actions != null && actions.length > 0) {
            if (network == null)
                network = new Network();
            try {
                IntentFilter intentFilter = new IntentFilter();
                for (String action : actions)
                    intentFilter.addAction(action);
                ContextUtils.getContext().registerReceiver(network, intentFilter);
                networkCallback = callbacks;
                registerCount++;
                if (registerCount > 1) {
                    if (BuildConfig.DEBUG)
                        Log.e("NetMonitor", "you should be unregister the pre wifi broadcastReceiver");
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    throw new IllegalArgumentException("register wifi failed: " + e.getLocalizedMessage());
                }
            }
        }
    }

    public void unregister() {
        try {
            if (network != null) ContextUtils.getContext().unregisterReceiver(network);
            registerCount = 0;
            networkCallback = null;
        } catch (Exception e) {

        }
    }
}
