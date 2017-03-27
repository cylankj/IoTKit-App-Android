package com.cylan.jiafeigou.base.module;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanzhendong on 2017/3/25.
 */

public class DPProperty {
    private Class<?> type;
    private List<DPDevice> devices;

    public Class<?> type() {
        return this.type;
    }

    public boolean accept(DPDevice device) {
        if (devices == null) return false;
        return devices.contains(device);//以后有时间在搞
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
