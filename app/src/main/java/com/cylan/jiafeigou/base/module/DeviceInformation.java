package com.cylan.jiafeigou.base.module;

/**
 * Created by yanzhendong on 2017/5/11.
 */

public class DeviceInformation {
    public String uuid = null;
    public String mac = null;
    public String ip = null;
    public String version = null;
    public int port = -10000;
    public int net = -10000;

    public DeviceInformation(String uuid) {
        this.uuid = uuid;
    }
}
