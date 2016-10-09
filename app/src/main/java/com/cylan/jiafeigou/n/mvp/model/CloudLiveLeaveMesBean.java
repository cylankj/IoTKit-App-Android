package com.cylan.jiafeigou.n.mvp.model;

/**
 * 作者：zsl
 * 创建时间：2016/10/8
 * 描述：
 */
public class CloudLiveLeaveMesBean {

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

    public String leaveMesgLength;
    public boolean isRead;

}
