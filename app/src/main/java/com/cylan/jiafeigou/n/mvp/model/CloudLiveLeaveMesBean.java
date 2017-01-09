package com.cylan.jiafeigou.n.mvp.model;

import java.io.Serializable;

/**
 * 作者：zsl
 * 创建时间：2016/10/8
 * 描述：
 */
public class CloudLiveLeaveMesBean implements Serializable {

    public String getLeaveMesgLength() {
        return leaveMesgLength;
    }

    public void setLeaveMesgLength(String leaveMesgLength) {
        this.leaveMesgLength = leaveMesgLength;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getLeaveMesgUrl() {
        return leaveMesgUrl;
    }

    public void setLeaveMesgUrl(String leaveMesgUrl) {
        this.leaveMesgUrl = leaveMesgUrl;
    }

    public String getLeveMesgTime() {
        return leveMesgTime;
    }

    public void setLeveMesgTime(String leveMesgTime) {
        this.leveMesgTime = leveMesgTime;
    }

    public String leaveMesgUrl;
    public boolean isRead;
    public String leaveMesgLength;
    public String leveMesgTime;

}
