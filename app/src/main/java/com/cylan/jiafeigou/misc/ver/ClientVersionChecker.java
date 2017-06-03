package com.cylan.jiafeigou.misc.ver;

import com.cylan.jiafeigou.utils.NetUtils;

/**
 * Created by hds on 17-5-28.
 */

public class ClientVersionChecker implements IVersion<ClientVersionChecker.CVersion> {


    @Override
    public boolean checkCondition() {
        //当前网络不行
        if (NetUtils.getJfgNetType() == 0) return false;

        return true;
    }

    @Override
    public void startCheck() {
    }

    @Override
    public void finalShow() {
    }

    public static final class CVersion extends IVersion.BaseVersion {

    }

}
