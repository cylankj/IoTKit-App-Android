package com.cylan.jiafeigou.misc.ver;

/**
 * Created by hds on 17-6-2.
 */

public class VersionDispatcher {
    public static VersionDispatcher instance;

    public static VersionDispatcher getInstance() {
        if (instance == null) instance = new VersionDispatcher();
        return instance;
    }

//    public <T extends IVersion.BaseVersion> Observable<T> dispatch(AbstractVersion<T> version) {
//
//    }
}
