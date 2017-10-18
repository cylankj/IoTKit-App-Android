package com.cylan.jiafeigou.n.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.cylan.jiafeigou.cache.db.module.DownloadFile;

/**
 * Created by cylan-hunt on 17-3-15.
 */

public class PAlbumBean implements Parcelable {
    public boolean isDate;
    public boolean selected;
    private DownloadFile downloadFile;


    public void setDownloadFile(DownloadFile downloadFile) {
        this.downloadFile = downloadFile;
    }

    public DownloadFile getDownloadFile() {
        return downloadFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PAlbumBean bean = (PAlbumBean) o;

        if (isDate != bean.isDate) {
            return false;
        }
        return downloadFile != null ? downloadFile.equals(bean.downloadFile) : bean.downloadFile == null;

    }

    @Override
    public int hashCode() {
        int result = (isDate ? 1 : 0);
        result = 31 * result + (downloadFile != null ? downloadFile.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.isDate ? (byte) 1 : (byte) 0);
        dest.writeByte(this.selected ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.downloadFile, flags);
    }

    public PAlbumBean() {
    }

    protected PAlbumBean(Parcel in) {
        this.isDate = in.readByte() != 0;
        this.selected = in.readByte() != 0;
        this.downloadFile = in.readParcelable(DownloadFile.class.getClassLoader());
    }

    public static final Creator<PAlbumBean> CREATOR = new Creator<PAlbumBean>() {
        @Override
        public PAlbumBean createFromParcel(Parcel source) {
            return new PAlbumBean(source);
        }

        @Override
        public PAlbumBean[] newArray(int size) {
            return new PAlbumBean[size];
        }
    };

    @Override
    public String toString() {
        return "PAlbumBean{" +
                "isDate=" + isDate +
                ", selected=" + selected +
                ", downloadFile=" + downloadFile +
                '}';
    }
}
