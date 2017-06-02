package com.cylan.jiafeigou.misc.ver;

import rx.Observable;

/**
 * Created by hds on 17-5-28.
 */


public interface IVersion<T extends IVersion.BaseVersion> {

    /**
     * 什么条件才去检查
     *
     * @return
     */
    boolean checkCondition();


    /**
     * 开始检测
     */
    Observable<T> startCheck();


    class BaseVersion {
        private String desc;
        private int versionCode;
        private String versionName;
        private String url;
        private String saveDir;
        private String fileName;
        private long lastShowTime;

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public void setVersionCode(int versionCode) {
            this.versionCode = versionCode;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setSaveDir(String saveDir) {
            this.saveDir = saveDir;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public void setLastShowTime(long lastShowTime) {
            this.lastShowTime = lastShowTime;
        }

        public String getDesc() {
            return desc;
        }

        public int getVersionCode() {
            return versionCode;
        }

        public String getVersionName() {
            return versionName;
        }

        public String getUrl() {
            return url;
        }

        public String getSaveDir() {
            return saveDir;
        }

        public String getFileName() {
            return fileName;
        }

        public long getLastShowTime() {
            return lastShowTime;
        }
    }
}
