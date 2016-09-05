package com.cylan.jiafeigou.support.log;

import android.os.StatFs;

import java.io.File;

/**
 * Created by lxh on 15-8-19.
 */
public class NLogSdCardInfo {
    /**
     * 路径
     */
    private String path;
    /**
     * 总大小
     */
    private long totalSize;
    /**
     * 已使用大小
     */
    private long usedSize;
    /**
     * 剩余大小
     */
    private long freeSize;

    public NLogSdCardInfo(String path) {
        this.path = path;
        this.sf = getStatFs();
        long blockSize = sf.getBlockCount(); // 区块大小 。
        long count = sf.getBlockCount(); // 总块数
        long freeCount = sf.getAvailableBlocks(); //空余块数。
        totalSize = blockSize * count;
        freeSize = blockSize * freeCount;
        this.usedSize = totalSize - freeSize;
    }


    private StatFs sf;

    private synchronized StatFs getStatFs() {
        if (sf == null)
            sf = new StatFs(new File(path).getAbsolutePath());
        return sf;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public long getUsedSize() {
        return usedSize;
    }

    public long getFreeSize() {
        return freeSize;
    }

    public String getPath() {
        return new File(path).getAbsolutePath();
    }


    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("NLogSdCardInfo : ");
        sb.append("path:").append(path)
                .append(" total: ").append(totalSize / 1024 / 1024).append(" MB ,")
                .append(" used: ").append(usedSize / 1024 / 1024).append(" MB ,")
                .append(" free: ").append(freeSize / 1024 / 1024).append(" MB");
        return sb.toString();
    }
}
