package com.cylan.jiafeigou.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by cylan-hunt on 16-11-24.
 */

public class ParcelableUtils {

    public static void write(Class<?> clazz, Parcel dest, Object o, int flags) {
        Log.d("DpMsgDefine", "write::" + clazz + " " + o);
        if (o != null && (o instanceof Boolean || clazz == boolean.class)) {
            dest.writeByte((boolean) o ? (byte) 1 : (byte) 0);
        } else if (o != null && (o instanceof Integer || clazz == int.class)) {
            dest.writeInt((int) o);
        } else if (o != null && o instanceof String) {
            dest.writeString((String) o);
        } else if (o != null && (o instanceof Byte || clazz == byte.class)) {
            dest.writeByte((byte) o);
        } else if (o != null && (o instanceof Long || clazz == long.class)) {
            dest.writeLong((long) o);
        } else if (o != null && (o instanceof Float || clazz == float.class)) {
            dest.writeFloat((float) o);
        } else if (o != null && o instanceof Parcelable) {
            dest.writeParcelable((Parcelable) o, flags);
        }

    }

    public static Object read(Class<?> clazz, Parcel in) {
        Object o = null;
        Log.d("DpMsgDefine", "write::" + clazz + " ");
        if (clazz == int.class || clazz == Integer.class) {
            o = in.readInt();
        } else if (clazz == Float.class || clazz == float.class) {
            o = in.readFloat();
        } else if (clazz == Long.class || clazz == long.class) {
            o = in.readLong();
        } else if (clazz == String.class) {
            o = in.readString();
        } else if (clazz == byte.class || clazz == Byte.class) {
            o = in.readByte();
        } else if (clazz == boolean.class || clazz == Boolean.class) {
            o = in.readByte() == 1;
        } else {
//            o = in.readParcelable(clazz.getClass().getClassLoader());,千万别用clazz.getClass().getClassLoader();
            o = in.readParcelable(clazz.getClassLoader());
        }
        return o;
    }
//别删
//    Class<?> clazz = DpMsgMap.ID_2_CLASS_MAP.get(msgId);
//    Log.d("DpMsgDefine", "DpMsgDefine:  type:" + msgId + " " + clazz);
//    if (clazz == String.class) {
//        Log.d("DpMsgDefine", "DpMsgDefine: String type");
//        this.o = in.readString();
//    }
//    if (clazz == int.class || clazz == Integer.class) {
//        Log.d("DpMsgDefine", "DpMsgDefine: int type");
//        this.o = in.readInt();
//    } else {
//        this.o = in.readParcelable(Object.class.getClassLoader());
//    }
}
