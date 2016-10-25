package com.cylan.jiafeigou;

/**
 * 作者：zsl
 * 创建时间：2016/10/14
 * 描述：
 */
interface ICloudLiveService{

    void setHangUpFlag(boolean isHangUp);       //设置挂断标记

    boolean getHangUpFlag();                    //获取挂断标记

    void setHangUpResultData(String obj);       //设置挂断结果数据

    String getHangUpResultData();               //获取挂断结果数据

    void setIgnoreFlag(boolean isIgnore);       //设置忽略标记

    boolean getIgnoreFlag();                    //获取忽略标记

    void setIgnoreResultData(String obj);        //设置忽略结果数据

    String getIgnoreResultData();               //获取忽略结果数据
}
