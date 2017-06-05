package com.cylan.jiafeigou.support.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by cylan-hunt on 17-2-23.
 */

public class NetMonitor implements NetworkCallback {
    private Network network;
    private final Object lock = new Object();

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

    @Override
    public void onNetworkChanged(Context context, Intent intent) {

    }


    private class Network extends BroadcastReceiver {

        public Network() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            synchronized (lock) {
                Log.d("networkCallbackList", "networkCallbackList:" + networkCallbackList);
                String action = intent.getAction();
                if (networkCallbackList != null) {
                    Iterator<String> iterator = networkCallbackList.keySet().iterator();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        ArrayList<String> filterList = filterMap.get(key);
                        if (filterList.contains(action) && networkCallbackList.containsKey(key))
                            networkCallbackList.get(key).onNetworkChanged(context, intent);
                    }
                }
            }
        }
    }

    private Map<String, NetworkCallback> networkCallbackList = new ConcurrentHashMap<>();

    private HashMap<String, ArrayList<String>> filterMap = new HashMap<>();

    public void registerNet(NetworkCallback callbacks, String[] actions) {
        synchronized (lock) {
            if (actions != null && actions.length > 0) {
                if (network == null)
                    network = new Network();
                try {
                    IntentFilter intentFilter = new IntentFilter();
                    Iterator<String> keySet = filterMap.keySet().iterator();
                    while (keySet.hasNext()) {
                        String key = keySet.next();
                        ArrayList<String> list = filterMap.get(key);
                        if (list != null) {
                            for (String action : list) {
                                intentFilter.addAction(action);
                            }
                        }
                    }
                    for (String action : actions) {
                        intentFilter.addAction(action);
                    }
                    ContextUtils.getContext().registerReceiver(network, intentFilter);
                    filterMap.put(callbacks.getClass().getSimpleName(), toList(actions));
                    networkCallbackList.remove(callbacks.getClass().getSimpleName());
                    networkCallbackList.put(callbacks.getClass().getSimpleName(), callbacks);
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) {
                        throw new IllegalArgumentException("register wifi failed: " + e.getLocalizedMessage());
                    }
                }
            }
        }
    }

    private ArrayList<String> toList(String[] actions) {
        if (actions == null) return new ArrayList<>();
        ArrayList<String> l = new ArrayList<>();
        for (String s : actions) {
            l.add(s);
        }
        return l;
    }

    public void unregister(NetworkCallback callback) {
        synchronized (lock) {
            try {
//            if (network != null) ContextUtils.getContext().unregisterReceiver(network);
                if (networkCallbackList != null)
                    networkCallbackList.remove(callback.getClass().getSimpleName());
                if (filterMap != null) {
                    filterMap.remove(callback.getClass().getSimpleName());
                }
                AppLogger.d("networkCallback remove:" + callback);
            } catch (Exception e) {

            }
        }
    }
}
