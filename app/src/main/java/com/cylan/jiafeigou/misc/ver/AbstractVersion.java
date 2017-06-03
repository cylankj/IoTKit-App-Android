package com.cylan.jiafeigou.misc.ver;

/**
 * Created by hds on 17-5-28.
 */

public abstract class AbstractVersion<T extends AbstractVersion.AVersion> implements IVersion<T> {

    protected Portrait portrait;

    public void setPortrait(Portrait portrait) {
        this.portrait = portrait;
    }

    public class AVersion extends BaseVersion {
        private int pid;
        private String cid;
    }

    public static final class Portrait {
        private String cid;
        private int pid;

        public void setCid(String cid) {
            this.cid = cid;
        }

        public void setPid(int pid) {
            this.pid = pid;
        }

        public String getCid() {
            return cid;
        }

        public int getPid() {
            return pid;
        }
    }
}
