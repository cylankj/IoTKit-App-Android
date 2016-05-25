package com.cylan.jiafeigou.n.model;

/**
 * Created by hunt on 16-5-14.
 */
public class DeviceBean {

    public int id = 0;
    public int deviceType;
    //-1:offline
    public int netType = -1;
    public int msgCount = 0;
    public int battery;
    public int isProtectdMode = 0;
    public int isShared;
    public int msgTime;
    public String cid = "";
    public String alias = "";
}
