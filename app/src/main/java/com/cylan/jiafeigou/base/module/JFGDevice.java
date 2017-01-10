package com.cylan.jiafeigou.base.module;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;

import com.cylan.annotation.DPProperty;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_202_MAC;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_205_CHARGING;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_207_DEVICE_VERSION;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_208_DEVICE_SYS_VERSION;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_401_BELL_CALL_STATE;
import static com.cylan.jiafeigou.dp.DpUtils.unpackData;

/*
 *  @项目名：  JFGAndroid 
 *  @包名：    com.cylan.jiafeigou.base.module
 *  @文件名:   JFGDevice
 *  @创建者:   yanzhendong
 *  @创建时间:  2017/1/8 11:03
 *  @描述：    TODO
 */
public abstract class JFGDevice implements Parcelable {
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

    private static List<Long> mSetTypeList = new ArrayList<>();

    static {
        mSetTypeList.add((long) ID_401_BELL_CALL_STATE);
    }

    public JFGDevice() {
    }

    private Field getFieldByMsgId(long msgId) {
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
        return mDPPropertyArray.get(msgId);
    }

    public final boolean setValue(JFGDPMsg msg) {
        return setValue(msg, -1);
    }

    public final boolean setValue(JFGDPMsg msg, long seq) {
        try {
            Field field = getFieldByMsgId(msg.id);
            if (field == null) return false;
            DPProperty dpProperty = field.getAnnotation(DPProperty.class);
            DataPoint value;
            Class<?> type;
            if (dpProperty.isSetType()) {
                Set setValue = (Set) field.get(this);
                if (setValue == null) setValue = new TreeSet();
                field.set(this, setValue);
                type = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                if (DataPoint.class.isAssignableFrom(type) && !(DpMsgDefine.DPPrimary.class.isAssignableFrom(type))) {
                    value = (DataPoint) unpackData(msg.packValue, type);
                    value.version = msg.version;
                    value.id = msg.id;
                    value.seq = seq;
                    return setValue.add(value);
                }
                //下面这段代码测试不通过,但基本不会走到这里
                type = (Class<?>) ((ParameterizedType) type.getGenericSuperclass()).getActualTypeArguments()[0];
                DpMsgDefine.DPPrimary primary = new DpMsgDefine.DPPrimary();
                primary.version = msg.version;
                primary.id = msg.id;
                primary.value = unpackData(msg.packValue, type);
                return setValue.add(primary);

            } else {//不是SetType
                value = (DataPoint) field.get(this);
                if (value == null || value.version < msg.version) {//数据需要更新了
                    type = field.getType();
                    if (DataPoint.class.isAssignableFrom(type) && !(DpMsgDefine.DPPrimary.class.isAssignableFrom(type))) {
                        value = (DataPoint) unpackData(msg.packValue, type);
                        value.version = msg.version;
                        value.id = msg.id;
                        value.seq = seq;
                        field.set(this, value);
                    } else {
                        DpMsgDefine.DPPrimary primary = new DpMsgDefine.DPPrimary();
                        type = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        primary.value = unpackData(msg.packValue, type);
                        primary.version = msg.version;
                        primary.id = msg.id;
                        primary.seq = seq;
                        field.set(this, primary);
                    }
                    return true;
                }
                //数据不需要更新
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isSetType(long msgId) {
        return mSetTypeList.contains(msgId);
    }

    public final <T> T getValue(long msgId) {
        return getValue(msgId, -1);
    }

    public final <T> T getValue(long msgId, long seq) {
        try {
            Field field = getFieldByMsgId(msgId);
            Object value = field.get(this);
            if (value == null || seq == -1) return (T) value;

            if (isSetType(msgId)) {
                Set<DataPoint> result = new TreeSet<>();
                Set<DataPoint> temp = (Set<DataPoint>) value;
                for (DataPoint point : temp) {
                    if (point.seq == seq) result.add(point);
                }
                return (T) result;
            }
            return (T) value;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    public final JFGDevice setDevice(com.cylan.entity.jniCall.JFGDevice device) {
        this.alias = device.alias;
        this.uuid = device.uuid;
        this.sn = device.sn;
        this.shareAccount = device.shareAccount;
        this.pid = device.pid;
        return this;
    }

    /**
     * 一种设备独有的属性
     */
    public ArrayList<JFGDPMsg> queryParameters(Map<Integer, Long> mapVersion) {
        ArrayList<JFGDPMsg> baseList = new ArrayList<>();
        baseList.add(new JFGDPMsg(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.MAC_202), getVersion(mapVersion, 202)));
        baseList.add(new JFGDPMsg(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.DEVICE_VERSION_207), getVersion(mapVersion, 207)));
        baseList.add(new JFGDPMsg(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.DEVICE_SYS_VERSION_208), getVersion(mapVersion, 208)));
        baseList.add(new JFGDPMsg(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.CHARGING_205), getVersion(mapVersion, 205)));
        return baseList;
    }

    protected long getVersion(Map<Integer, Long> map, int msgId) {
        return map != null ? (map.containsKey(msgId) ? map.get(msgId) : 0L) : 0L;
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
