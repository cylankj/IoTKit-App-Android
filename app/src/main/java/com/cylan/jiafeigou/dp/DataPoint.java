package com.cylan.jiafeigou.dp;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.LongSparseArray;

import com.cylan.annotation.DPProperty;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.support.log.AppLogger;

import org.msgpack.MessagePack;
import org.msgpack.annotation.Ignore;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import static com.cylan.jiafeigou.dp.DpUtils.unpackData;

/**
 * Created by cylan-hunt on 16-12-2.
 */

public abstract class DataPoint implements Parcelable, Comparable<DataPoint> {
    @Ignore
    private boolean isNull = false;

    @Ignore
    public static final long MSG_ID_VIRTUAL_START = -888080;
    @Ignore
    public static final long MSG_ID_VIRTUAL_END = 0;

    public boolean isNull() {
        return isNull;
    }

    @Ignore
    public long id;
    @Ignore
    public long version;
    @Ignore
    public long seq;

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
        return version == value.version && id == value.id;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (version ^ (version >>> 32));
        return result;
    }

    @Override
    public int compareTo(DataPoint another) {
        return version == another.version ? 0 : version > another.version ? -1 : 1;//降序
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.version);
        dest.writeLong(this.seq);
    }


    protected DataPoint(Parcel in) {
        this.id = in.readLong();
        this.version = in.readLong();
        this.seq = in.readLong();
    }


    /**
     * 避免检查空指针,只针对DataPoint,只针对获取值的情,
     * 如果需要检查非空可调用isNull函数,
     */
    public Object $() {
        Object value;
        Field field;
        LongSparseArray<Field> properties = getProperties();
        for (int i = 0; i < properties.size(); i++) {
            field = properties.valueAt(i);
            try {
                value = field.get(this);
                if (value == null) {
                    value = field.getType().newInstance();
                }
                field.set(this, value);
                if (value instanceof DataPoint) {
                    DataPoint temp = (DataPoint) value;
                    temp.isNull = true;
                    temp.version = Long.MIN_VALUE;//自动生成的wrap使version最小以便随时覆盖
                    temp.id = properties.keyAt(i);
                }
                if (DpMsgDefine.DPPrimary.class.isAssignableFrom(field.getType())) {
                    DpMsgDefine.DPPrimary primary = (DpMsgDefine.DPPrimary) value;
                    if (primary.value == null) {
                        Class<?> paramType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        primary.value = getPrimaryValue(paramType);
                    }

                } else if (DpMsgDefine.DPSet.class.isAssignableFrom(field.getType())) {
                    DpMsgDefine.DPSet set = (DpMsgDefine.DPSet) value;
                    if (set.value == null) ((DpMsgDefine.DPSet) value).value = new TreeSet();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    private Object getPrimaryValue(Class clz) {
        if (Integer.class.equals(clz)) {
            return 0;
        } else if (String.class.equals(clz)) {
            return "";
        } else if (Byte.class.equals(clz)) {
            return 0;
        } else if (Long.class.equals(clz)) {
            return 0;
        } else if (Double.class.equals(clz)) {
            return 0;
        } else if (Float.class.equals(clz)) {
            return 0;
        } else if (Character.class.equals(clz)) {
            return ' ';
        } else if (Boolean.class.equals(clz)) {
            return false;
        }
        return null;
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
                    version = value != null ? value.version : version;
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

    public final boolean setValue(JFGDPMsg msg, long seq) {
        try {
            Field field = getProperties().get(msg.id);
            if (field == null) return false;

            DataPoint value = (DataPoint) field.get(this);
            Class<?> type = field.getType();
            if (DpMsgDefine.DPSet.class.isAssignableFrom(type)) {//setType
                DpMsgDefine.DPSet<DataPoint> setValue = (DpMsgDefine.DPSet<DataPoint>) value;
                if (setValue == null) setValue = new DpMsgDefine.DPSet<>();
                if (setValue.value == null) setValue.value = new TreeSet<>();
                field.set(this, setValue);
                Class<?> paramType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                value = (DataPoint) unpackData(msg.packValue, paramType);
                value.version = msg.version;
                value.id = msg.id;
                value.seq = seq;
                setValue.value.remove(value);
                boolean add = setValue.value.add(value);
                DataPoint first = setValue.value.first();
                setValue.version = first.version;
                setValue.seq = first.seq;
                setValue.id = first.id;
                ((DataPoint) setValue).isNull = false;
                return add;
            }

            if (value != null && value.version > msg.version) return false;//数据已是最新的,无需更新了

            if ((DpMsgDefine.DPPrimary.class.isAssignableFrom(type))) {
                type = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                DpMsgDefine.DPPrimary primary = new DpMsgDefine.DPPrimary();
                primary.value = unpackData(msg.packValue, type);
                primary.version = msg.version;
                primary.id = msg.id;
                primary.seq = seq;
                field.set(this, primary);
            } else {
                value = (DataPoint) unpackData(msg.packValue, type);
                value.version = msg.version;
                value.id = msg.id;
                value.seq = seq;
                field.set(this, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
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
                    if (point.seq == seq) origin.add(point);
                }
                DpMsgDefine.DPSet<DataPoint> result = new DpMsgDefine.DPSet<>();
                result.value = origin;
                if (origin.size() > 0) {
                    DataPoint first = origin.first();
                    result.seq = first.seq;
                    result.id = first.id;
                    result.version = first.version;
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
