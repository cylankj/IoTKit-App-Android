package com.cylan.jiafeigou.dp;

import com.cylan.jiafeigou.BuildConfig;

import org.junit.Test;
import org.msgpack.MessagePack;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.TimeZone;

/**
 * Created by cylan-hunt on 16-11-24.
 */
//@RunWith(MyTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class DpMsgDefineTest {


    @Test
    public void testObject() throws IOException {
        DpMsgDefine.DPNet net = new DpMsgDefine.DPNet();
        byte[] data = new byte[]{-110, 1, -85, 88, 105, 97, 111, 109, 105, 95, 65, 67, 70, 50};

        System.out.println(unpackData(data, DpMsgDefine.DPNet.class));
        System.out.println(unpackData(new byte[]{5}, int.class));

        data = new byte[]{-107, -50, 88, -44, -46, 55, 0, 0, 1, -95, 48};
        System.out.println(unpackData(data, DpMsgDefine.DPAlarm.class));
        data = new byte[]{-107, -50, 88, -44, -34, -58, 0, 0, 1, -95, 48};
        System.out.println(unpackData(data, DpMsgDefine.DPAlarm.class));

        data = new byte[]{-49, 0, 0, 1, 91, 14, 80, -82, -98};
        System.out.println(unpackData(data, Long.class));

        data = new byte[]{-107, -51, 2, -119, -51, 1, -20, -51, 1, -49, -51, 5, 0, -51, 3, -64};
        System.out.println(unpackData(data, Long.class));
        data = new byte[]{-65, 59};
        System.out.println(unpackData(data, int.class));
    }

    private void setData(long version) {
        System.out.println("version:" + version);
    }

    private <T> T unpackData(byte[] data, Class<T> clazz) throws IOException {
        MessagePack ms = new MessagePack();
        return ms.read(data, clazz);
    }

    @Test
    public void testDefaultTimeZone() {
        TimeZone timeZone = TimeZone.getDefault();
        System.out.println(timeZone.getID());
    }
}