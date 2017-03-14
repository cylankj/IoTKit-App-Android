package com.cylan.jiafeigou.misc;

/**
 * Created by cylan-hunt on 17-3-13.
 */

/**
 * 设备属性，设置选项需要显示红点
 */
public class SettingTip {
    public int safe = 1;
    public int autoRecord = 1;
    public int timeLapse = 1;

    public SettingTip setTimeLapse(int timeLapse) {
        this.timeLapse = timeLapse;
        return this;
    }

    public SettingTip setSafe(int safe) {
        this.safe = safe;
        return this;
    }

    public SettingTip setAutoRecord(int autoRecord) {
        this.autoRecord = autoRecord;
        return this;
    }

    public boolean isBeautiful() {
        return safe + autoRecord + timeLapse > 1;
    }
}
