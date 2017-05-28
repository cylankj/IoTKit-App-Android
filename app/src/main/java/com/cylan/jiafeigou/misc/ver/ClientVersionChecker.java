package com.cylan.jiafeigou.misc.ver;

/**
 * Created by hds on 17-5-28.
 */

public class ClientVersionChecker implements IVersion<ClientVersionChecker.CVersion> {


    @Override
    public boolean checkCondition() {
        return false;
    }

    @Override
    public CVersion startCheck() {
        return null;
    }

    public static final class CVersion extends IVersion.BaseVersion {

    }

}
