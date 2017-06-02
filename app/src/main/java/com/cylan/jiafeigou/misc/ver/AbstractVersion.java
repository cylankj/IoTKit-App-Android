package com.cylan.jiafeigou.misc.ver;

/**
 * Created by hds on 17-5-28.
 */

public abstract class AbstractVersion<T extends IVersion.BaseVersion> implements IVersion<T> {

    protected Portrait portrait;

//    private T defaultVersion = ;



    public void setPortrait(Portrait portrait) {
        this.portrait = portrait;
    }


    public static final class Portrait {
        private String cid;
        private int pid;

        public Portrait setCid(String cid) {
            this.cid = cid;
            return this;
        }

        public Portrait setPid(int pid) {
            this.pid = pid;
            return this;
        }

        public String getCid() {
            return cid;
        }

        public int getPid() {
            return pid;
        }
    }
}
