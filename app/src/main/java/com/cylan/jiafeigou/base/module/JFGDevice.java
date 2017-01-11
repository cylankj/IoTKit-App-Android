package com.cylan.jiafeigou.base.module;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;

import com.cylan.annotation.DPProperty;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_202_MAC;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_205_CHARGING;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_207_DEVICE_VERSION;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_208_DEVICE_SYS_VERSION;
import static com.cylan.jiafeigou.dp.DpUtils.unpackData;

/*
 *  @项目名：  JFGAndroid 
 *  @包名：    com.cylan.jiafeigou.base.module
 *  @文件名:   JFGDevice
 *  @创建者:   yanzhendong
 *  @创建时间:  2017/1/8 11:03
 *  @描述：    TODO
 */
public abstract class JFGDevice extends DataPoint<JFGDevice> implements Parcelable {
    public String uuid;
    public String sn;
    public String alias;
    public String shareAccount;
    public int pid;

    @DPProperty(msgId = ID_202_MAC)
    public DpMsgDefine.DPPrimary<String> mac;//DpMsgMap.MAC_202
    @DPProperty(msgId = ID_207_DEVICE_VERSION)
    public DpMsgDefine.DPPrimary<String> device_version;//DpMsgMap.DEVICE_VERSION_207
    @DPProperty(msgId = ID_208_DEVICE_SYS_VERSION)
    public DpMsgDefine.DPPrimary<String> device_sys_version;//DpMsgMap.DEVICE_SYS_VERSION_208
    @DPProperty(msgId = ID_205_CHARGING)
    public DpMsgDefine.DPPrimary<Boolean> charging;//DpMsgMap.CHARGING_205

    private LongSparseArray<Field> mDPPropertyArray;

    public JFGDevice() {
    }

    private LongSparseArray<Field> getProperties() {
        if (mDPPropertyArray == null) {
            synchronized (this) {
                if (mDPPropertyArray == null) {
                    mDPPropertyArray = new LongSparseArray<>();
                    Field[] fields = getClass().getFields();
                    if (fields != null) {
                        for (Field field : fields) {
                            DPProperty dpProperty = field.getAnnotation(DPProperty.class);
                            if (dpProperty == null) continue;
                            mDPPropertyArray.put(dpProperty.msgId(), field);
                        }
                    }
                }
            }
        }
        return mDPPropertyArray;
    }

    public final boolean setValue(JFGDPMsg msg) {
        return setValue(msg, -1);
    }

    public final boolean setValue(JFGDPMsg msg, long seq) {
        AppLogger.d("真在解析消息ID为:" + msg.id + "的DP消息");
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
                if (!(DpMsgDefine.DPPrimary.class.isAssignableFrom(paramType))) {
                    value = (DataPoint) unpackData(msg.packValue, paramType);
                    value.version = msg.version;
                    value.id = msg.id;
                    value.seq = seq;
                    boolean add = setValue.value.add(value);
                    DataPoint first = setValue.value.first();
                    setValue.version = first.version;
                    setValue.seq = first.seq;
                    setValue.id = first.id;
                    return add;
                }
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

    public final <T> T getValue(long msgId) {
        return getValue(msgId, -1);
    }

    public final <T> T getValue(long msgId, long seq) {
        try {
            Field field = getProperties().get(msgId);
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

    public static <T> T getValue(Object value) {
        if (value == null) return null;

        if (value instanceof DpMsgDefine.DPSet) {
            return (T) ((DpMsgDefine.DPSet<DataPoint>) value).value;
        }

        if (value instanceof DpMsgDefine.DPPrimary) {
            return (T) ((DpMsgDefine.DPPrimary) value).value;
        }

        return (T) value;
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
            for (int i = 0; i < properties.size(); i++) {
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


    public final JFGDevice setDevice(com.cylan.entity.jniCall.JFGDevice device) {
        this.alias = device.alias;
        this.uuid = device.uuid;
        this.sn = device.sn;
        this.shareAccount = device.shareAccount;
        this.pid = device.pid;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof JFGDevice) {
            return TextUtils.equals(uuid, ((JFGDevice) o).uuid);
        } else {
            return false;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uuid);
        dest.writeString(this.sn);
        dest.writeString(this.alias);
        dest.writeString(this.shareAccount);
        dest.writeInt(this.pid);
    }

    protected JFGDevice(Parcel in) {
        this.uuid = in.readString();
        this.sn = in.readString();
        this.alias = in.readString();
        this.shareAccount = in.readString();
        this.pid = in.readInt();
    }
}
