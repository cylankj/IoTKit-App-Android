package com.cylan.jiafeigou.cache.db.module;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.cylan.jiafeigou.utils.MiscUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Arrays;

/**
 * Created by holy on 2017/3/19.
 */

@Entity(generateGettersSetters = false)
public final class DownloadFile implements Comparable<DownloadFile>, Parcelable {
    @Id
    public Long id;
    @Generated(hash = 735721945)
    public String fileName;
    public String uuid;
    public long time;
    public byte[] md5;
    public int fileSize;
    //已经下载的
    public int offset;
    public int state;
    private int place;

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getTimeStamp() {
        return MiscUtils.getValueFrom(fileName);
    }

    @Override
    public int compareTo(@NonNull DownloadFile downloadFile) {
        return getTimeStamp() - downloadFile.getTimeStamp();
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getMd5() {
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

    public int getPlace() {
        return place;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setMd5(byte[] md5) {
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

    public void setPlace(int place) {
        this.place = place;
    }

    @Override
    public String toString() {
        return "DownloadFile{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", md5=" + Arrays.toString(md5) +
                ", fileSize=" + fileSize +
                ", offset=" + offset +
                ", state=" + state +
                ", place=" + place +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.fileName);
        dest.writeLong(this.time);
        dest.writeByteArray(this.md5);
        dest.writeInt(this.fileSize);
        dest.writeInt(this.offset);
        dest.writeInt(this.state);
        dest.writeInt(this.place);
    }

    protected DownloadFile(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.fileName = in.readString();
        this.time = in.readLong();
        this.md5 = in.createByteArray();
        this.fileSize = in.readInt();
        this.offset = in.readInt();
        this.state = in.readInt();
        this.place = in.readInt();
    }

    @Generated(hash = 1874457176)
    public DownloadFile(Long id, String fileName, String uuid, long time, byte[] md5, int fileSize, int offset,
                        int state, int place) {
        this.id = id;
        this.fileName = fileName;
        this.uuid = uuid;
        this.time = time;
        this.md5 = md5;
        this.fileSize = fileSize;
        this.offset = offset;
        this.state = state;
        this.place = place;
    }

    @Generated(hash = 379234666)
    public DownloadFile() {
    }

    public static final Parcelable.Creator<DownloadFile> CREATOR = new Parcelable.Creator<DownloadFile>() {
        @Override
        public DownloadFile createFromParcel(Parcel source) {
            return new DownloadFile(source);
        }

        @Override
        public DownloadFile[] newArray(int size) {
            return new DownloadFile[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DownloadFile file = (DownloadFile) o;

        if (time != file.time) return false;
        return fileName != null ? fileName.equals(file.fileName) : file.fileName == null;

    }

    @Override
    public int hashCode() {
        int result = fileName != null ? fileName.hashCode() : 0;
        result = 31 * result + (int) (time ^ (time >>> 32));
        return result;
    }
}
