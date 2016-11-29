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
        MessagePack ms = new MessagePack();
        return ms.read(data, clazz);
    }

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
}
