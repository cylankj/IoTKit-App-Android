package com.cylan.jiafeigou.cache.db.module;

import android.support.annotation.NonNull;

import com.cylan.jiafeigou.utils.MiscUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;

/**
 * Created by holy on 2017/3/19.
 */

@Entity(generateGettersSetters = false)
public final class DownloadFile implements Comparable<DownloadFile> {
    @Id
    public long id;
    @Generated(hash = 735721945)
    public String fileName;
    public String md5;
    public int fileSize;
    //已经下载的
    public int offset;
    public int state;
    private int place;

    @Keep
    public DownloadFile(long id, String fileName, String md5, int fileSize,
                        int offset, int state, int albumPosition) {
        this.id = id;
        this.fileName = fileName;
        this.md5 = md5;
        this.fileSize = fileSize;
        this.offset = offset;
        this.state = state;
        this.place = albumPosition;
    }

    @Generated(hash = 379234666)
    public DownloadFile() {
    }

    public int getTimeStamp() {
        return MiscUtils.getValueFrom(fileName);
    }

    @Override
    public int compareTo(@NonNull DownloadFile downloadFile) {
        return getTimeStamp() - downloadFile.getTimeStamp();
    }

    public long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMd5() {
        return md5;
    }

    public int getFileSize() {
        return fileSize;
    }

    public int getOffset() {
        return offset;
    }

    public int getState() {
        return state;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    @Override
    public String toString() {
        return "DownloadFile{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", md5='" + md5 + '\'' +
                ", fileSize=" + fileSize +
                ", offset=" + offset +
                ", state=" + state +
                ", place=" + place +
                '}';
    }
}
