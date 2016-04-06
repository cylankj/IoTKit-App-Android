package com.cylan.publicApi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Ap {

    /**
     * @WifiManager
     */

    private WifiManager mWifiManager;

    public Ap(Context ctx) {
        mWifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * isWifiApEnabled
     *
     * @return
     */
    public boolean isApEnabled() {
        Boolean enabled = false;
        Method m;
        try {
            m = mWifiManager.getClass().getMethod("isWifiApEnabled");
            enabled = (Boolean) m.invoke(mWifiManager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return enabled;
    }

    /**
     * setWifiApEnabled((WifiConfiguration wifiConfig, boolean enabled)
     */
    public boolean setWifiApEnabled(WifiConfiguration config, boolean enabled) {
        Method m;
        boolean ret = false;
        try {
            m = mWifiManager.getClass().getMethod("setWifiApEnabled",
                    WifiConfiguration.class, boolean.class);
            ret = (Boolean) m.invoke(mWifiManager, config, enabled);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * getWifiApConfiguration()
     *
     * @return
     */
    public WifiConfiguration getWifiApConfiguration() {
        Method m;
        try {
            m = mWifiManager.getClass().getMethod("getWifiApConfiguration");
            return (WifiConfiguration) m.invoke(mWifiManager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * setWifiApConfiguration(WifiConfiguration wifiConfig)
     *
     * @return
     */
    public boolean setWifiApConfiguration(WifiConfiguration wifiConfig) {
        Method m;
        boolean ret = false;
        try {
            m = mWifiManager.getClass().getMethod("setWifiApConfiguration",
                    WifiConfiguration.class);
            ret = (Boolean) m.invoke(mWifiManager, wifiConfig);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return ret;
    }

}
