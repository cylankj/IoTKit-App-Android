package com.cylan.jiafeigou.misc.ver;

/**
 * Created by hds on 17-5-28.
 */

public abstract class AbstractVersion<T extends AbstractVersion.AVersion> implements IVersion<T> {


    public class AVersion extends BaseVersion {
        private int pid;
        private String cid;
    }
}
