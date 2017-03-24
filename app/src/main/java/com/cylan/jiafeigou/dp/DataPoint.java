package com.cylan.jiafeigou.dp;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.LongSparseArray;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.base.module.DPProperty;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.msgpack.annotation.Ignore;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.TreeSet;

import static android.R.attr.id;
import static com.cylan.jiafeigou.dp.DpUtils.unpackData;

/**
 * Created by cylan-hunt on 16-12-2.
 */

public abstract class DataPoint implements Parcelable, Comparable<DataPoint> {

    @Ignore
    private boolean isNull = false;
    @Ignore
    private static Gson mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    @Ignore
    public static final long MSG_ID_VIRTUAL_START = -888080;
    @Ignore
    public static final long MSG_ID_VIRTUAL_END = 0;

    public boolean isNull() {
        return isNull;
    }

    @Ignore
    public long dpMsgId;
    @Ignore
    public long dpMsgVersion;
    @Ignore
    public long dpMsgSeq;

    @Override
    public String toString() {
        return mGson.toJson(this);
    }

    @Ignore
    private LongSparseArray<DPProperty> mDPPropertyArray;
    @Ignore
    private LongSparseArray<DataPoint> dpValuePool;

    public byte[] toBytes() {
        return DpUtils.pack(this);
    }

    public DataPoint() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        DataPoint value = (DataPoint) o;
        return dpMsgVersion == value.dpMsgVersion && dpMsgId == value.dpMsgId;

    }

    @Override
    public int hashCode() {
        int result = (int) (dpMsgId ^ (dpMsgId >>> 32));
        result = 31 * result + (int) (dpMsgVersion ^ (dpMsgVersion >>> 32));
        return result;
    }

    @Override
    public final int compareTo(DataPoint another) {
        return dpMsgVersion == another.dpMsgVersion ? 0 : dpMsgVersion > another.dpMsgVersion ? -1 : 1;//降序
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.dpMsgId);
        dest.writeLong(this.dpMsgVersion);
        dest.writeLong(this.dpMsgSeq);
    }


    protected DataPoint(Parcel in) {
        this.dpMsgId = in.readLong();
        this.dpMsgVersion = in.readLong();
        this.dpMsgSeq = in.readLong();
    }

    protected final LongSparseArray<DataPoint> getValuePool() {
        if (dpValuePool == null) {
            synchronized (this) {
                if (dpValuePool == null) {
                    dpValuePool = new LongSparseArray<>();
                }
            }
        }
        return dpValuePool;
    }

    protected final LongSparseArray<DPProperty> getProperties() {
        if (mDPPropertyArray == null) {
            synchronized (this) {
                if (mDPPropertyArray == null) {
                    mDPPropertyArray = new LongSparseArray<>();
                    Field[] fields = getClass().getFields();
                    if (fields != null) {
                        long msgId;
                        Field field;
                        for (int i = 0; i < fields.length; i++) {
                            field = fields[i];
                            DPProperty dpProperty = field.getAnnotation(DPProperty.class);
                            if (dpProperty == null) continue;
                            try {
                                msgId = field.getInt(this);
                                mDPPropertyArray.put(msgId, dpProperty);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return mDPPropertyArray;
    }

    public boolean updateValue(int msgId, DataPoint value) throws IllegalAccessException {
        if (!accept(msgId)) return false;
        DPProperty property = getProperties().get(msgId);
        if (property != null) {
            getValuePool().put(msgId, value);
            return true;
        } else if (BuildConfig.DEBUG) throw new NullPointerException("empty");
        return false;
    }

    /**
     * @param init true:如果字段不为空,使用字段原来的version,false:使用0作为version
     */
    public final ArrayList<JFGDPMsg> getQueryParameters(boolean init) {
        ArrayList<JFGDPMsg> result = new ArrayList<>();
        LongSparseArray<DPProperty> properties = getProperties();
        DataPoint value;
        long version = 0;
        int msgId;
        for (int i = 0; i < properties.size(); i++) {
            msgId = (int) properties.keyAt(i);
            if (msgId >= MSG_ID_VIRTUAL_START && msgId <= MSG_ID_VIRTUAL_END)
                continue;//说明当前是虚拟ID,则跳过
            if (init) {
                value = getValuePool().get(msgId);
                version = value == null ? 0 : value.dpMsgVersion;
            }
            result.add(new JFGDPMsg((int) properties.keyAt(i), version));
        }
        return result;
    }


    public final boolean setValue(JFGDPMsg msg) {
        return setValue(msg, -1);
    }

    public final boolean accept(long msgId) {
        return getProperties().get(msgId) != null;
    }

    public final boolean setValue(long msgId, long version, byte[] packValue, long seq) {
        if (!accept(msgId)) return false;
        try {
            if (msgId == -1 || packValue == null) return false;
            DPProperty property = getProperties().get(msgId);
            if (property == null) return false;
            DataPoint value;
            switch (property.dpType()) {
                case TYPE_FIELD: {
                    value = (DataPoint) unpackData(packValue, property.type());
                    value.dpMsgVersion = version;
                    value.dpMsgId = id;
                    value.dpMsgSeq = seq;
                    getValuePool().put(msgId, value);
                }
                break;
                case TYPE_PRIMARY: {
                    DpMsgDefine.DPPrimary primary = new DpMsgDefine.DPPrimary();
                    primary.value = unpackData(packValue, property.type());
                    primary.dpMsgVersion = version;
                    primary.dpMsgId = msgId;
                    primary.dpMsgSeq = seq;
                    getValuePool().put(msgId, primary);
                }
                break;
                case TYPE_SET: {
                    DpMsgDefine.DPSet<DataPoint> setValue = (DpMsgDefine.DPSet<DataPoint>) getValuePool().get(msgId);
                    if (setValue == null) {
                        setValue = new DpMsgDefine.DPSet<>(new TreeSet<>());
                        getValuePool().put(msgId, setValue);
                    }

                    Class<?> paramType = property.type();
                    value = (DataPoint) unpackData(packValue, paramType);
                    value.dpMsgVersion = version;
                    value.dpMsgId = msgId;
                    value.dpMsgSeq = seq;
                    setValue.value.remove(value);
                    boolean add = setValue.value.add(value);
                    DataPoint first = setValue.value.first();
                    setValue.dpMsgVersion = first.dpMsgVersion;
                    setValue.dpMsgSeq = first.dpMsgSeq;
                    setValue.dpMsgId = first.dpMsgId;
                    ((DataPoint) setValue).isNull = false;
                    return add;
                }
            }
        } catch (Exception e) {
            AppLogger.d("解析消息出现异常: msgId 为:" + msgId + "," + e.getMessage());
        }
        return true;
    }


    public final boolean setValue(JFGDPMsg msg, long seq) {
        return accept(msg.id) && setValue(msg.id, msg.version, msg.packValue, seq);
    }

    //针对 set 类型使用
    public final void clear(int msgId) {
        if (accept(msgId)) {
            DataPoint dataPoint = getValuePool().get(msgId);
            if (dataPoint instanceof DpMsgDefine.DPSet) {
                ((DpMsgDefine.DPSet) dataPoint).value.clear();
            } else {
                getValuePool().remove(msgId);
            }
        }
    }

    public final <T> T $(long msgId, T defaultValue) {
        if (!accept(msgId)) return defaultValue;
        DataPoint value = getValuePool().get(msgId);
        if (value == null) {
            return defaultValue;
        } else if (defaultValue instanceof DataPoint || defaultValue == null) {
            return (T) value;
        } else if (value instanceof DpMsgDefine.DPSet) {
            return (T) ((DpMsgDefine.DPSet) value).list();
        } else if (value instanceof DpMsgDefine.DPPrimary) {
            return (T) ((DpMsgDefine.DPPrimary) value).value;
        } else {
            return defaultValue;
        }
    }
}
