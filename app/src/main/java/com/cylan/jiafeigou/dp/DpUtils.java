package com.cylan.jiafeigou.dp;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.support.log.AppLogger;

import org.msgpack.MessagePack;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-11-29.
 */

public class DpUtils {
    private static MessagePack mp = new MessagePack();

    /**
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T unpackData(byte[] data, Class<T> clazz) throws IOException {
        if (clazz == null || data == null) {
            AppLogger.e("value is null: " + clazz);
            return null;
        }
        return mp.createBufferUnpacker(data).read(clazz);
    }

    public static <T> T unpackData(byte[] data, Class<T> clazz, T defaultValue) throws IOException {
        if (clazz == null || data == null) {
            AppLogger.e("value is null: " + clazz);
            return defaultValue;
        }
        return mp.createBufferUnpacker(data).read(clazz);
    }

//    public static <T> T unpack(byte[] bytes, Type type) throws IOException {
//        if (bytes == null || type == null) {
//            AppLogger.e("value is null: " + type);
//            return null;
//        }
//        return (T) mp.createBufferUnpacker(bytes).read(type);
//    }

    public static <T> T getMsg(ArrayList<JFGDPMsg> list, int id, Class<T> tClass) {
        if (list != null) {
            for (JFGDPMsg jfgdpMsg : list) {
                if (jfgdpMsg.id == id) {
                    try {
                        Object o = unpackData(jfgdpMsg.packValue, tClass);
                        if (tClass.isInstance(o))
                            return (T) o;
                    } catch (IOException e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
                }
            }
        }
        return null;
    }

    public static ArrayList<JFGDPMsg> getList(int id, byte[] value, long version) {
        JFGDPMsg jfgdpMsg = new JFGDPMsg();
        jfgdpMsg.id = id;
        jfgdpMsg.version = version;
        jfgdpMsg.packValue = value;
        ArrayList<JFGDPMsg> list = new ArrayList<>();
        list.add(jfgdpMsg);
        return list;
    }

    /**
     * 打包msgpack
     *
     * @param o
     * @return
     */
    public static byte[] pack(Object o) {
        try {
            return mp.write(o);
        } catch (Exception e) {
            AppLogger.e("msgpack err: " + e.getLocalizedMessage());
            return null;
        }
    }

}

//    public static <T> T getSafeValue(T)

