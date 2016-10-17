package com.cylan.jiafeigou;

/**
 * 作者：zsl
 * 创建时间：2016/10/14
 * 描述：
 */
interface ICloudLiveService{
    void setHangUpFlag(boolean isHangUp);
    boolean getHangUpFlag();
    void setHangUpResultData(String obj);
    String getHangUpResultData();
}
