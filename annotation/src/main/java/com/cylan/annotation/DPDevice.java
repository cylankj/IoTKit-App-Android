package com.cylan.annotation;

/**
 * Created by yzd on 17-1-13.
 */

public enum DPDevice {
    CAMERA("CameraDevice"), DOORBELL("DoorBellDevice"), EFAMILY("EFamilyDevice"), MAGNETIC("MagneticDevice");
    private String name;

    DPDevice(String name) {
        this.name = name;
    }

    public String device() {
        return name;
    }
}
