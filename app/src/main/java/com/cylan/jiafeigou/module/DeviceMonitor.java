package com.cylan.jiafeigou.module;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.dp.DpMsgDefine;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yanzhendong on 2018/1/10.
 */

@SuppressWarnings("unused")
public class DeviceMonitor {
    private static DeviceMonitor instance;
    private ConcurrentHashMap<String, DeviceObserver> mObserverMap = new ConcurrentHashMap<>();
    public static DeviceMonitor getInstance() {
        if (instance == null) {
            synchronized (DeviceMonitor.class) {
                if (instance == null) {
                    instance = new DeviceMonitor();
                }
            }
        }
        return instance;
    }

    public void addObserver(String uuid, DeviceObserver observer) {
    }

    public void removeObserver(String uuid, DeviceObserver observer) {

    }

    public void pushMessage(String uuid, JFGDPMsg msg) {

    }

    public static abstract class DeviceObserver {
        volatile boolean hasSDCard = true;
        volatile int battery = 0;
        volatile boolean standBy = false;
        volatile boolean sdCardFormatted = false;
        volatile String viewMode = "";
        volatile DpMsgDefine.DPNet net;
        volatile DpMsgDefine.DpCoordinate coordinate;

        protected void onDeviceSDCardChanged(boolean hasSDCard) {
        }

        protected void onDeviceBatteryChanged(int battery) {
        }

        protected void onDeviceStandByChanged(boolean standby) {
        }

        protected void onDeviceSDCardFormatted(int value) {
        }

        protected void onDeviceViewModeChanged(String viewMode) {
        }

        protected void onDeviceNetChanged(DpMsgDefine.DPNet dpNet) {
        }

        protected void onDeviceTimezoneChanged(DpMsgDefine.DPTimeZone timeZone) {
        }

        protected void onDeviceCoordinateChanged(DpMsgDefine.DpCoordinate coordinate) {

        }
    }
}
