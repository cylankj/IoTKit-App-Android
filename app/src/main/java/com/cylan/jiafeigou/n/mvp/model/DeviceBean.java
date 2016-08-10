package com.cylan.jiafeigou.n.mvp.model;

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
    public int isProtectedMode = 0;
    public int isShared;
    public long msgTime;
    public String cid = "";
    public String alias = "";
}
