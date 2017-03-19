package com.cylan.jiafeigou.n.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by cylan-hunt on 17-3-15.
 */

public class PAlbumBean implements Parcelable {
    public boolean isDate;
    public long timeInDate;
    public int from;
    public String url;
    public boolean selected;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.isDate ? (byte) 1 : (byte) 0);
        dest.writeLong(this.timeInDate);
        dest.writeInt(this.from);
        dest.writeString(this.url);
        dest.writeByte(this.selected ? (byte) 1 : (byte) 0);
    }

    public PAlbumBean() {
    }

    protected PAlbumBean(Parcel in) {
        this.isDate = in.readByte() != 0;
        this.timeInDate = in.readLong();
        this.from = in.readInt();
        this.url = in.readString();
        this.selected = in.readByte() != 0;
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
}
