package com.cylan.annotation;

/*
 *  @项目名：  APT 
 *  @包名：    com.annotation
 *  @文件名:   DPTarget
 *  @创建者:   yanzhendong
 *  @创建时间:  2017/1/13 22:08
 *  @描述：    TODO
 */
public enum DPTarget {
    @DPInterface(name = "DataPoint")DATAPOINT,
    @DPInterface(name = "JFGDevice")DEVICE,
    @DPInterface(name = "JFGAccount")ACCOUNT,
    @DPInterface(name = "JFGDoorBellDevice", parent = DEVICE)DOORBELL,
    @DPInterface(name = "JFGCameraDevice", parent = DEVICE)CAMERA,
    @DPInterface(name = "JFGEFamilyDevice", parent = DEVICE)EFAMILY,
    @DPInterface(name = "JFGMagnetometerDevice", parent = DEVICE)MAGNETOMETER
}
