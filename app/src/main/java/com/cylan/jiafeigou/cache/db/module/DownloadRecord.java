package com.cylan.jiafeigou.cache.db.module;

/**
 * Created by yanzhendong on 2017/5/16.
 */
//@Entity
public class DownloadRecord {
//    @Id
    public Long id;
    public String fileName;
    public String account;
    public String uuid;
    public String filePath;
    public long total;
    public long progress;

}
