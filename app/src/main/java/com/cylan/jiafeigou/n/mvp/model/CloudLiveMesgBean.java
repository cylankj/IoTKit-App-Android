package com.cylan.jiafeigou.n.mvp.model;

/**
 * 作者：zsl
 * 创建时间：2016/9/30
 * 描述：
 */
public class CloudLiveMesgBean {

    public String voiceLength;
    public String userIcon;
    public int itemType;

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public CloudLiveMesgBean(String voiceLength, String userIcon) {
        this.voiceLength = voiceLength;
        this.userIcon = userIcon;
    }

}
