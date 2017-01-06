package com.cylan.jiafeigou.n.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.cylan.jiafeigou.dp.BaseDataPoint;

import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * Created by chen on 6/6/16.
 */
@Message
public class MediaBean extends BaseDataPoint implements Comparable<MediaBean>, Parcelable {


    public static final int TYPE_PIC = 0;
    public static final int TYPE_VIDEO = 1;
    public static final int TYPE_LOAD = 2;
    private static MediaBean guideBean;

    @Ignore
    public long version;

    @Index(0)
    public String cid;
    @Index(1)
    public int time;
    @Index(2)
    public int msgType;
    @Index(3)
    public int regionType;
    @Index(4)
    public String fileName;
    @Index(5)
    public String place;


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
        dest.writeString(this.cid);
        dest.writeInt(this.time);
        dest.writeInt(this.msgType);
        dest.writeInt(this.regionType);
        dest.writeString(this.fileName);
        dest.writeString(this.place);
    }

    public MediaBean() {
    }

    protected MediaBean(Parcel in) {
        this.cid = in.readString();
        this.time = in.readInt();
        this.msgType = in.readInt();
        this.regionType = in.readInt();
        this.fileName = in.readString();
        this.place = in.readString();
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
                ", place='" + place + '\'' +
                '}';
    }

    private static MediaBean loadBean = new MediaBean();

    public static MediaBean getEmptyLoadTypeBean() {
        if (loadBean == null)
            loadBean = new MediaBean();
        loadBean.msgType = TYPE_LOAD;
        return loadBean;
    }

    public static MediaBean getGuideBean() {
        if (guideBean == null) {
            guideBean = new MediaBean();
            guideBean.msgType = TYPE_VIDEO;
            guideBean.fileName = "http://yf.cylan.com.cn:82/Garfield/1045020208160b9706425470.mp4";
            guideBean.cid = "www.cylan.com";
        }
        guideBean.time = (int) (System.currentTimeMillis() / 1000);
        return guideBean;
    }
}

