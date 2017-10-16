package com.cylan.jiafeigou.dp;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.msgpack.annotation.Ignore;

/**
 * Created by cylan-hunt on 16-12-2.
 */

public abstract class BaseDataPoint implements Parcelable, DataPoint {
    @Ignore
    @JsonIgnore
    private static Gson mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    @Ignore
    @JsonIgnore
    public long msgId;
    @Ignore
    @JsonIgnore
    public long version;

    @Override
    public String toString() {
        return mGson.toJson(this);
    }


    @Override
    public byte[] toBytes() {
        byte[] bytes;
        if (this instanceof DpMsgDefine.DPPrimary) {
            bytes = DpUtils.pack(((DpMsgDefine.DPPrimary) this).value);
        } else {
            bytes = DpUtils.pack(this);
        }
        return bytes == null ? new byte[]{0} : bytes;
    }

    public BaseDataPoint() {
    }

    @Override
    public long getMsgId() {
        return msgId;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    @Override
    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        DataPoint value = (DataPoint) o;
        return version == value.getVersion() && msgId == value.getMsgId();

    }

    @Override
    public int hashCode() {
        int result = (int) (msgId ^ (msgId >>> 32));
        result = 31 * result + (int) (version ^ (version >>> 32));
        return result;
    }

    @Override
    public final int compareTo(DataPoint another) {
        return version == another.getVersion() ? 0 : version > another.getVersion() ? -1 : 1;//降序
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.msgId);
        dest.writeLong(this.version);
    }


    protected BaseDataPoint(Parcel in) {
        this.msgId = in.readLong();
        this.version = in.readLong();
    }
}
