package com.cylan.jiafeigou.dp;

import android.os.Parcel;
import android.os.Parcelable;

import com.cylan.jiafeigou.support.log.AppLogger;

import org.msgpack.MessagePack;
import org.msgpack.annotation.Ignore;

import java.io.IOException;

/**
 * Created by cylan-hunt on 16-12-2.
 */

public abstract class DataPoint implements Parcelable, Comparable<DataPoint> {
    @Ignore
    public long id;
    @Ignore
    public long version;
    @Ignore
    public long seq;

    @Ignore
    public byte[] toBytes() {
        try {
            MessagePack msgpack = new MessagePack();
            return msgpack.write(this);
        } catch (IOException ex) {
            AppLogger.e("msgpack read byte ex: " + ex.getLocalizedMessage());
            return null;
        }
    }

    @Ignore
    @Override
    public int describeContents() {
        return 0;
    }

    @Ignore
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.version);
    }

    public DataPoint() {
    }

    protected DataPoint(Parcel in) {
        this.id = in.readLong();
        this.version = in.readLong();
    }

    @Ignore
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataPoint value = (DataPoint) o;

        if (id != value.id) return false;
        return version == value.version && seq == value.seq;

    }

    @Ignore
    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (version ^ (version >>> 32));
        return result;
    }

    @Ignore
    @Override
    public int compareTo(DataPoint another) {
        return version > another.version ? -1 : 1;//降序
    }

    @Ignore
    public static DataPoint getEmpty() {
        return new DataPoint() {
        };
    }
}
