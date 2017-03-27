package com.cylan.jiafeigou.dp;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.msgpack.annotation.Ignore;

/**
 * Created by cylan-hunt on 16-12-2.
 */

public abstract class DataPoint implements Parcelable, Comparable<DataPoint> {
    @Ignore
    private static Gson mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    @Ignore
    public long msgId;
    @Ignore
    public long version;

    @Override
    public String toString() {
        return mGson.toJson(this);
    }


    public byte[] toBytes() {
        return DpUtils.pack(this);
    }

    public DataPoint() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        DataPoint value = (DataPoint) o;
        return version == value.version && msgId == value.msgId;

    }

    @Override
    public int hashCode() {
        int result = (int) (msgId ^ (msgId >>> 32));
        result = 31 * result + (int) (version ^ (version >>> 32));
        return result;
    }

    @Override
    public final int compareTo(DataPoint another) {
        return version == another.version ? 0 : version > another.version ? -1 : 1;//降序
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


    protected DataPoint(Parcel in) {
        this.msgId = in.readLong();
        this.version = in.readLong();
    }
}
