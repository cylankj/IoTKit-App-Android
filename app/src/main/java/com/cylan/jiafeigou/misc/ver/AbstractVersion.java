package com.cylan.jiafeigou.misc.ver;

import android.text.TextUtils;

import com.cylan.entity.jniCall.DevUpgradeInfo;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by hds on 17-5-28.
 */

public abstract class AbstractVersion<T extends IVersion.BaseVersion> implements IVersion<T> {
    protected static long lastCheckTime = 0;
    protected Portrait portrait;
    protected BinVersion binVersion;
    protected ShowCondition showCondition;

    public void setShowCondition(ShowCondition showCondition) {
        this.showCondition = showCondition;
    }

    public Portrait getPortrait() {
        return portrait;
    }

    public BinVersion getBinVersion() {
        return binVersion;
    }

    public ShowCondition getShowCondition() {
        return showCondition;
    }

    public void setPortrait(Portrait portrait) {
        this.portrait = portrait;
    }

    public void setBinVersion(BinVersion binVersion) {
        this.binVersion = binVersion;
    }

    public static class BinVersion extends IVersion.BaseVersion {

        public static BinVersion NULL = new BinVersion();
        private ArrayList<DevUpgradeInfo> list;
        private String cid;
        private String tagVersion;
        private String content;
        private long totalSize;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            BinVersion that = (BinVersion) o;

            if (totalSize != that.totalSize) {
                return false;
            }
            if (list != null ? !list.equals(that.list) : that.list != null) {
                return false;
            }
            if (cid != null ? !cid.equals(that.cid) : that.cid != null) {
                return false;
            }
            if (tagVersion != null ? !tagVersion.equals(that.tagVersion) : that.tagVersion != null) {
                return false;
            }
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


        public void setTotalSize(long totalSize) {
            this.totalSize = totalSize;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public void setList(ArrayList<DevUpgradeInfo> list) {
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

        public ArrayList<DevUpgradeInfo> getList() {
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

    protected long totalSize(BinVersion version) {
        if (version == null || version.getList() == null) {
            return 0;
        }
        int count = version.getList().size();
        long size = 0;
        for (int i = 0; i < count; i++) {
            long t = MiscUtils.getFileSizeFromUrl(version.getList().get(i).url);
            version.getList().get(i).fileSize = t;
            size += t;
        }
        return size;
    }

    protected BinVersion getVersionFrom(String uuid) {
        final String content = PreferencesUtils.getString(JConstant.KEY_FIRMWARE_CONTENT + uuid);
        if (TextUtils.isEmpty(content)) {
            return BinVersion.NULL;
        }
        try {
            return new Gson().fromJson(content, BinVersion.class);
        } catch (Exception e) {
            return BinVersion.NULL;
        }
    }
}
