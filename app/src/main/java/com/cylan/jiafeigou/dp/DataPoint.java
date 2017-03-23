package com.cylan.jiafeigou.dp;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.LongSparseArray;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ext.annotations.DPProperty;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.msgpack.MessagePack;
import org.msgpack.annotation.Ignore;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Set;
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
    private LongSparseArray<Field> mDPPropertyArray;

    public byte[] toBytes() {
        try {
            MessagePack msgpack = new MessagePack();
            return msgpack.write(this);
        } catch (IOException ex) {
            AppLogger.e("msgpack read byte ex: " + ex.getLocalizedMessage());
            return null;
        }
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
    public int compareTo(DataPoint another) {
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


    /**
     * 避免检查空指针,只针对DataPoint,只针对获取值的情,
     * 如果需要检查非空可调用isNull函数,
     */
    @Deprecated
    public Object $() {
        return this;
    }

    protected final LongSparseArray<Field> getProperties() {
        if (mDPPropertyArray == null) {
            synchronized (this) {
                if (mDPPropertyArray == null) {
                    mDPPropertyArray = new LongSparseArray<>();
                    Field[] fields = getClass().getFields();
                    if (fields != null) {
                        long msgId;
                        Field field;
                        int modifier;
                        for (int i = 0; i < fields.length; i++) {
                            field = fields[i];
                            modifier = field.getModifiers();
                            if ((modifier & Modifier.FINAL) == Modifier.FINAL || (modifier & Modifier.STATIC) == Modifier.STATIC) {
                                continue;
                            }
                            DPProperty dpProperty = field.getAnnotation(DPProperty.class);
                            msgId = dpProperty != null ? dpProperty.msgId() : MSG_ID_VIRTUAL_START + i;
                            mDPPropertyArray.put(msgId, field);
                        }
                    }
                }
            }
        }
        return mDPPropertyArray;
    }

    public boolean updateValue(int msgId, Object value) throws IllegalAccessException {
        Field field = getProperties().get(msgId);
        if (field != null) {
            field.set(this, value);
            return true;
        } else if (BuildConfig.DEBUG) throw new NullPointerException("empty");
        return false;
    }

    /**
     * @param init true:如果字段不为空,使用字段原来的version,false:使用0作为version
     */
    public final ArrayList<JFGDPMsg> getQueryParameters(boolean init) {
        ArrayList<JFGDPMsg> result = new ArrayList<>();
        LongSparseArray<Field> properties = getProperties();
        try {
            Field field;
            DataPoint value;
            long version = 0;
            int msgId;
            for (int i = 0; i < properties.size(); i++) {
                msgId = (int) properties.keyAt(i);
                if (msgId >= MSG_ID_VIRTUAL_START && msgId <= MSG_ID_VIRTUAL_END)
                    continue;//说明当前是虚拟ID,则跳过
                if (init) {
                    field = properties.valueAt(i);
                    value = (DataPoint) field.get(this);
                    version = value != null ? value.dpMsgVersion : version;
                }
                result.add(new JFGDPMsg((int) properties.keyAt(i), version));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }


    public final boolean setValue(JFGDPMsg msg) {
        return setValue(msg, -1);
    }


    public final boolean setValue(long msgId, long version, byte[] packValue, long seq) {
        try {
            if (msgId == -1 || packValue == null) return false;
            Field field = getProperties().get(msgId);
            if (field == null) return false;
            DataPoint value = (DataPoint) field.get(this);
            Class<?> type = field.getType();
            if (DpMsgDefine.DPSet.class.isAssignableFrom(type)) {//setType
                DpMsgDefine.DPSet<DataPoint> setValue = (DpMsgDefine.DPSet<DataPoint>) value;
                if (setValue == null) setValue = new DpMsgDefine.DPSet<>();
                if (setValue.value == null) setValue.value = new TreeSet<>();
                field.set(this, setValue);
                Class<?> paramType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
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

            if (value != null && value.dpMsgVersion > version) return false;//数据已是最新的,无需更新了

            if ((DpMsgDefine.DPPrimary.class.isAssignableFrom(type))) {
                type = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                DpMsgDefine.DPPrimary primary = new DpMsgDefine.DPPrimary();
                primary.value = unpackData(packValue, type);
                primary.dpMsgVersion = version;
                primary.dpMsgId = msgId;
                primary.dpMsgSeq = seq;
                field.set(this, primary);
            } else {
                value = (DataPoint) unpackData(packValue, type);
                value.dpMsgVersion = version;
                value.dpMsgId = id;
                value.dpMsgSeq = seq;
                field.set(this, value);
            }
        } catch (Exception e) {
            AppLogger.e("err: " + msgId + " " + e);
        }
        return true;
    }


    public final boolean setValue(JFGDPMsg msg, long seq) {
        return setValue(msg.id, msg.version, msg.packValue, seq);
    }

    public final <T extends DataPoint> T getValue(long msgId) {
        return getValue(msgId, -1);
    }

    public final <T extends DataPoint> T getValue(long msgId, long seq) {
        try {
            Field field = getProperties().get(msgId);
            if (field == null) return null;
            Object value = field.get(this);
            if (value == null || seq == -1) return (T) value;

            if (value instanceof DpMsgDefine.DPSet) {
                TreeSet<DataPoint> origin = new TreeSet<>();
                Set<DataPoint> temp = getValue(value);
                for (DataPoint point : temp) {
                    if (point.dpMsgSeq == seq) origin.add(point);
                }
                DpMsgDefine.DPSet<DataPoint> result = new DpMsgDefine.DPSet<>();
                result.value = origin;
                result.dpMsgId = msgId;
                result.dpMsgSeq = seq;
                if (origin.size() > 0) {
                    DataPoint first = origin.first();
                    result.dpMsgSeq = first.dpMsgSeq;
                    result.dpMsgId = first.dpMsgId;
                    result.dpMsgVersion = first.dpMsgVersion;
                }
                return (T) result;
            }
            return (T) value;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static final <T> T getValue(Object value) {
        if (value == null) return null;

        if (value instanceof DpMsgDefine.DPSet) {
            return (T) ((DpMsgDefine.DPSet<DataPoint>) value).value;
        }

        if (value instanceof DpMsgDefine.DPPrimary) {
            return (T) ((DpMsgDefine.DPPrimary) value).value;
        }

        return (T) value;
    }
}
