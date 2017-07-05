package com.cylan.jiafeigou.base.module;

import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanzhendong on 2017/3/25.
 */

public enum DPDevice {
    CAMERA(4, 5, 7, 8, 10, 17, 18, 19, 20, 21, 23, 26, 36, 37, 38, 39, 47, 48, 49, 1071, 1088, 1089,
            1090, 1091, 1092, 1152, 1158, 1346, 1347, 1348),
    DOORBELL(6, 15, 22, 24, 25, 27, 44, 46, 1093, 1094, 1159, 1160, 1344, 1345),
    PROPERTY(-1);
    private List<Integer> pids = new ArrayList<>();

    @IntDef({})
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.PARAMETER)
    @interface pid {
    }

    DPDevice(@pid int... pids) {
        for (int pid : pids) {
            this.pids.add(pid);
        }
    }

    public boolean accept(int pid) {
        return pids.contains(pid);
    }

    public static DPDevice belong(int pid) {
        for (DPDevice device : DPDevice.values()) {
            if (device.accept(pid)) return device;
        }
        return null;
    }
}
