package com.cylan.jiafeigou.n.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.utils.TimeUtils;

/**
 * Created by cylan-hunt on 16-8-3.
 */
public class BellCallRecordBean implements Comparable<BellCallRecordBean>, Parcelable {
    public String url;
    //应该隐藏
    public String date, timeStr;
    public long timeInLong;
    public int answerState;
    public int type;

    public long version;

    /**
     * 1：选中  0：默认
     */
    public boolean selected;

    @Override
    public int compareTo(BellCallRecordBean another) {
        return (int) (another.timeInLong - this.timeInLong);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeString(this.date);
        dest.writeString(this.timeStr);
        dest.writeLong(this.timeInLong);
        dest.writeInt(this.answerState);
        dest.writeInt(this.type);
        dest.writeLong(this.version);
        dest.writeByte(this.selected ? (byte) 1 : (byte) 0);
    }

    public BellCallRecordBean() {
    }

    protected BellCallRecordBean(Parcel in) {
        this.url = in.readString();
        this.date = in.readString();
        this.timeStr = in.readString();
        this.timeInLong = in.readLong();
        this.answerState = in.readInt();
        this.type = in.readInt();
        this.version = in.readLong();
        this.selected = in.readByte() != 0;
    }

    public static final Creator<BellCallRecordBean> CREATOR = new Creator<BellCallRecordBean>() {
        @Override
        public BellCallRecordBean createFromParcel(Parcel source) {
            return new BellCallRecordBean(source);
        }

        @Override
        public BellCallRecordBean[] newArray(int size) {
            return new BellCallRecordBean[size];
        }
    };

    public static BellCallRecordBean parse(DpMsgDefine.DPBellCallRecord record) {
        if (record == null) return null;
        BellCallRecordBean result = new BellCallRecordBean();
        result.answerState = record.isOK;
        result.timeInLong = record.time * 1000L;
        result.timeStr = TimeUtils.getHH_MM(record.time * 1000L);
        result.date = TimeUtils.getBellRecordTime(record.time * 1000L);
        result.type = record.type;
        result.version = record.dpMsgVersion;
        return result;
    }
}
