package com.cylan.jiafeigou.misc.bind;

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
    protected String bindCode;
    private String currentBindUUID = "";

    protected boolean isDogUpgrading = false;

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
        this.currentBindUUID = uuid;
    }

    protected void unSubscribe(Subscription... subscriptions) {
        if (subscriptions != null) {
            for (Subscription s : subscriptions) {
                if (s != null)
                    s.unsubscribe();
            }
        }
    }

    @Override
    public void clean() {
        iBindResult = null;
        devicePortrait = null;
        currentBindUUID = null;
        isDogUpgrading = false;
    }
}
