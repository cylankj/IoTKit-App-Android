package com.cylan.jiafeigou.misc.bind;

import rx.Observable;
import rx.Subscription;

/**
 * Created by cylan-hunt on 16-11-14.
 */

public abstract class AFullBind implements IFullBind {

    public static final String TAG = UdpConstant.BIND_TAG;

    public AFullBind(IBindResult iBindResult) {
        this.iBindResult = iBindResult;
    }

    protected IBindResult iBindResult;

    protected boolean isDogUpgrading = false;

    @Override
    public void startPingFPing(String shortUUID) {

    }

    @Override
    public void sendServerInfo(String ip, int host) {

    }

    @Override
    public Observable<Boolean> sendWifiInfo(String uuid, String mac, String ssid, String pwd, int type) {
        return null;
    }

    @Override
    public void sendWifiInfo(String ssid, String pwd, int type) {

    }

    @Override
    public void sendLanguageInfo() {

    }

    @Override
    public void startUpgrade() {

    }

    /**
     * 包含了设备的所有信息
     */
    protected UdpConstant.UdpDevicePortrait devicePortrait;

    public void setDevicePortrait(UdpConstant.UdpDevicePortrait devicePortrait) {
        this.devicePortrait = devicePortrait;
    }

    public UdpConstant.UdpDevicePortrait getDevicePortrait() {
        return devicePortrait;
    }

    @Override
    public void startBind(String uuid, String randomCode) {
    }

    protected void unSubscribe(Subscription... subscriptions) {
        if (subscriptions != null) {
            for (Subscription s : subscriptions) {
                if (s != null) {
                    s.unsubscribe();
                }
            }
        }
    }

    public void setServerLanguage(UdpConstant.UdpDevicePortrait udpDevicePortrait) {
    }

    @Override
    public Observable<UdpConstant.UdpDevicePortrait> getBindObservable(boolean check3G, String shortUUID) {
        return null;
    }

    @Override
    public void clean() {
        iBindResult = null;
        devicePortrait = null;
        isDogUpgrading = false;
    }
}
