package com.cylan.jiafeigou.base.module;

/**
 * Created by yanzhendong on 2017/5/13.
 */

public class DownloadPercent {
    public String uuid;
    public String fileName;
    public long progress = 0;
    public long total = 0;
    public int percent = 0;

    public DownloadPercent(String fileName, String uuid) {
        this.fileName = fileName;
        this.uuid = uuid;
    }

    public String getIdentifier() {
        return uuid + "/images/" + fileName;
    }

    public static String getIdentifier(String uuid, String fileName) {
        return uuid + "/images/" + fileName;
    }

    public interface DownloadListener {
        void update(long progress, long total, int percent, boolean error);
    }
}
