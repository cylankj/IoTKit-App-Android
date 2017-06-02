package com.cylan.jiafeigou.misc.ver;

import android.text.TextUtils;

import com.cylan.entity.jniCall.DevUpgradleInfo;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;

import java.util.ArrayList;

/**
 * Created by hds on 17-5-28.
 */

public abstract class AbstractVersion<T extends IVersion.BaseVersion> implements IVersion<T> {

    protected Portrait portrait;

//    private T defaultVersion = ;


    public void setPortrait(Portrait portrait) {
        this.portrait = portrait;
    }

    public static class BinVersion extends IVersion.BaseVersion {

        public static BinVersion NULL = new BinVersion();
        private ArrayList<DevUpgradleInfo> list;
        private String cid;
        private String tagVersion;
        private String content;
        private long totalSize;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BinVersion that = (BinVersion) o;

            if (totalSize != that.totalSize) return false;
            if (list != null ? !list.equals(that.list) : that.list != null) return false;
            if (cid != null ? !cid.equals(that.cid) : that.cid != null) return false;
            if (tagVersion != null ? !tagVersion.equals(that.tagVersion) : that.tagVersion != null)
                return false;
            return content != null ? content.equals(that.content) : that.content == null;

        }

        @Override
        public int hashCode() {
            int result = list != null ? list.hashCode() : 0;
            result = 31 * result + (cid != null ? cid.hashCode() : 0);
            result = 31 * result + (tagVersion != null ? tagVersion.hashCode() : 0);
            result = 31 * result + (content != null ? content.hashCode() : 0);
            result = 31 * result + (int) (totalSize ^ (totalSize >>> 32));
            return result;
        }

        public boolean isNULL() {
            return this.equals(NULL);
        }

        public boolean showVersion() {
            Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(cid);
            DpMsgDefine.DPNet dpNet = device.$(201, new DpMsgDefine.DPNet());
            //设备离线就不需要弹出来
            if (!JFGRules.isDeviceOnline(dpNet)) {
                return false;
            }
            //局域网弹出
            if (!MiscUtils.isDeviceInWLAN(cid)) return false;
            //弹框的时间,从弹出算起
            long time = getLastShowTime();
            AppLogger.e("记得改回来");
            return list != null && list.size() > 0 && !TextUtils.isEmpty(tagVersion)
                    && (time == 0 || System.currentTimeMillis() - time > 30 * 1000);
        }

        public void setTotalSize(long totalSize) {
            this.totalSize = totalSize;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public void setList(ArrayList<DevUpgradleInfo> list) {
            this.list = list;
        }

        public void setCid(String cid) {
            this.cid = cid;
        }

        public void setTagVersion(String tagVersion) {
            this.tagVersion = tagVersion;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public ArrayList<DevUpgradleInfo> getList() {
            return list;
        }

        public String getCid() {
            return cid;
        }

        public String getTagVersion() {
            return tagVersion;
        }

        public String getContent() {
            return content;
        }

        @Override
        public String toString() {
            return "BinVersion{" +
                    "list=" + list +
                    ", cid='" + cid + '\'' +
                    ", tagVersion='" + tagVersion + '\'' +
                    ", content='" + content + '\'' +
                    ", totalSize=" + totalSize +
                    '}';
        }
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
