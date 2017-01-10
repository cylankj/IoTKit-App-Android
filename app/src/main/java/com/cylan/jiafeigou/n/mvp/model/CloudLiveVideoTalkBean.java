package com.cylan.jiafeigou.n.mvp.model;

import java.io.Serializable;

/**
 * 作者：zsl
 * 创建时间：2016/10/8
 * 描述：
 */
public class CloudLiveVideoTalkBean implements Serializable {
    public String getVideoLength() {
        return videoLength;
    }

    public void setVideoLength(String videoLength) {
        this.videoLength = videoLength;
    }


    public boolean isHasConnet() {
        return hasConnet;
    }

    public void setHasConnet(boolean hasConnet) {
        this.hasConnet = hasConnet;
    }

    public String getVideoTime() {
        return videoTime;
    }

    public void setVideoTime(String videoTime) {
        this.videoTime = videoTime;
    }

    public String videoTime;
    public boolean hasConnet;
    public String videoLength;

}
