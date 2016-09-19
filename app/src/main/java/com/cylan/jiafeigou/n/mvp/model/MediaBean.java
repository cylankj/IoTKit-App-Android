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
    public long time;
    public int mediaType;
    public String srcUrl;
    public String deviceName;
    public String timeInStr;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MediaBean mediaBean = (MediaBean) o;

        return time == mediaBean.time;

    }

    @Override
    public int hashCode() {
        return (int) (time ^ (time >>> 32));
    }


    @Override
    public int compareTo(MediaBean another) {
        return another != null ? (int) (another.time - this.time) : 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.time);
        dest.writeInt(this.mediaType);
        dest.writeString(this.srcUrl);
        dest.writeString(this.deviceName);
        dest.writeString(this.timeInStr);
    }

    public MediaBean() {
    }

    protected MediaBean(Parcel in) {
        this.time = in.readLong();
        this.mediaType = in.readInt();
        this.srcUrl = in.readString();
        this.deviceName = in.readString();
        this.timeInStr = in.readString();
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
                "time=" + time +
                ", mediaType=" + mediaType +
                ", srcUrl='" + srcUrl + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", timeInStr='" + timeInStr + '\'' +
                '}' + "\n";
    }

    private static MediaBean loadBean = new MediaBean();

    public static MediaBean getEmptyLoadTypeBean() {
        if (loadBean == null)
            loadBean = new MediaBean();
        loadBean.mediaType = TYPE_LOAD;
        return loadBean;
    }
}

