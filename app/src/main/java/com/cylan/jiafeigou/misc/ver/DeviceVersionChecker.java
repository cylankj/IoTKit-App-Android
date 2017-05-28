package com.cylan.jiafeigou.misc.ver;

/**
 * Created by hds on 17-5-28.
 */

public class DeviceVersionChecker extends AbstractVersion<AbstractVersion.AVersion> {


    @Override
    public boolean checkCondition() {
        return false;
    }

    @Override
    public AVersion startCheck() {
        return null;
    }
}
