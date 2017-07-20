package com.cylan.jiafeigou.base.module;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanzhendong on 2017/3/25.
 */

public class DPProperty {
    public static final int LEVEL_DEFAULT = 0x00;
    public static final int LEVEL_HOME = 1;//首页,有限刷新
    public static final int LEVEL_TOP = 1 << 1;
    private Class<?> type;
    private List<DPDevice> devices;
    //级别,高优先级的,表是在主页刷新
    private int propertyLevel;

    public Class<?> type() {
        return this.type;
    }

    public int getPropertyLevel() {
        return propertyLevel;
    }

    public boolean accept(DPDevice device) {
        if (devices == null) return false;
        return devices.contains(device);//以后有时间在搞
    }

    public DPProperty(Class<?> type, int propertyLevel, DPDevice... devices) {
        this.type = type;
        this.propertyLevel = propertyLevel;
        if (devices != null) {
            this.devices = new ArrayList<>();
            for (DPDevice device : devices) {
                this.devices.add(device);
            }
        }
    }

    public DPProperty(Class<?> type, DPDevice... devices) {
        this.type = type;
        if (devices != null) {
            this.devices = new ArrayList<>();
            for (DPDevice device : devices) {
                this.devices.add(device);
            }
        }
    }

    public boolean isProperty() {
        return devices != null && devices.size() > 0;
    }

}
