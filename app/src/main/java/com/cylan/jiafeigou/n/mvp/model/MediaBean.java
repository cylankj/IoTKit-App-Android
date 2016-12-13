package com.cylan.jiafeigou.n.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by chen on 6/6/16.
 */
public class MediaBean implements Comparable<MediaBean>, Parcelable {


    public static final int TYPE_PIC = 0;
    public static final int TYPE_VIDEO = 1;
    public static final int TYPE_LOAD = 2;
    public String cid;
    public int time;
    public int msgType;
    public int regionType;
    public String fileName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MediaBean mediaBean = (MediaBean) o;

        return time == mediaBean.time;

    }

    @Override
    public int hashCode() {
        return time ^ (time >>> 32);
    }


    @Override
    public int compareTo(MediaBean another) {
        return another != null ? another.time - this.time : 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cid);
        dest.writeLong(this.time);
        dest.writeInt(this.msgType);
        dest.writeInt(this.regionType);
        dest.writeString(this.fileName);
    }

    public MediaBean() {
    }

    protected MediaBean(Parcel in) {
        this.cid = in.readString();
        this.time = in.readInt();
        this.msgType = in.readInt();
        this.regionType = in.readInt();
        this.fileName = in.readString();
    }

    public static final Creator<MediaBean> CREATOR = new Creator<MediaBean>() {
        @Override
        public MediaBean createFromParcel(Parcel source) {
            return new MediaBean(source);
        }

        @Override
        public MediaBean[] newArray(int size) {
            return new MediaBean[size];
        }
    };

    @Override
    public String toString() {
        return "MediaBean{" +
                "cid='" + cid + '\'' +
                ", time=" + time +
                ", msgType=" + msgType +
                ", regionType=" + regionType +
                ", fileName='" + fileName + '\'' +
                '}';
    }

    private static MediaBean loadBean = new MediaBean();

    public static MediaBean getEmptyLoadTypeBean() {
        if (loadBean == null)
            loadBean = new MediaBean();
        loadBean.msgType = TYPE_LOAD;
        return loadBean;
    }
}

