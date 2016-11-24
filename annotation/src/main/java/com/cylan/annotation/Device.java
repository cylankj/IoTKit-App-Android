package com.cylan.annotation;

/**
 * Created by cylan-hunt on 16-11-16.
 */

public enum Device {

    CAMERA,//摄像头
    BELL,//门铃
    MAG,//门磁
    CLOUD;//云相框
//    ALL;//所有设备

    Device() {
    }

    public String getName(Device device) {
        return device.name();
    }
}
