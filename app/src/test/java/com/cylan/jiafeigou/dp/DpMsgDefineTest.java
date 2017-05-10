package com.cylan.jiafeigou.dp;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.utils.RandomUtils;
import com.cylan.udpMsgPack.JfgUdpMsg;

import org.junit.Test;
import org.msgpack.MessagePack;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by cylan-hunt on 16-11-24.
 */
//@RunWith(MyTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class DpMsgDefineTest {


    @Test
    public void testObject() throws IOException {
        System.out.println(unpackData(new byte[]{-108, -49, 0, 0, 0, 3, -83, -124, 64, 0, -51, 96, 0, 0, -61}, DpMsgDefine.DPSdStatus.class));
        System.out.println(unpackData(new byte[]{-108, 0, 0, -9, -62}, DpMsgDefine.DPSdStatus.class));
//        DpMsgDefine.DPNet net = new DpMsgDefine.DPNet();
//        byte[] data = new byte[]{-110, 1, -85, 88, 105, 97, 111, 109, 105, 95, 65, 67, 70, 50};
        System.out.println(unpackData(new byte[]{-110, 3, -84, -28, -72, -83, -27, -101, -67, -24, -127, -108, -23, -128, -102}, DpMsgDefine.DPNet.class));
//        System.out.println(unpackData(new byte[]{-108, 0, 0, 0, -62}, DpMsgDefine.DPSdStatus.class));
//        System.out.println(unpackData(new byte[]{-108, -49, 0, 0, 0, 1, -51, -64, 0, 0, 0, -22, -61}, DpMsgDefine.DPSdStatus.class));
//        System.out.println(unpackData(new byte[]{-110, 1, -85, 88, 105, 97, 111, 109, 105, 95, 65, 67, 70, 50}, DpMsgDefine.DPNet.class));
//
//        data = new byte[]{-107, -50, 88, -44, -46, 55, 0, 0, 1, -95, 48};
//        System.out.println(unpackData(data, DpMsgDefine.DPAlarm.class));
//        data = new byte[]{-107, -50, 88, -42, 98, 115, 0, 0, 1, -95, 48};
//        System.out.println(unpackData(data, DpMsgDefine.DPAlarm.class));
//
//        data = new byte[]{-49, 0, 0, 1, 91, 14, 80, -82, -98};
//        System.out.println(unpackData(data, Long.class));
//
//        data = new byte[]{-107, -51, 2, -119, -51, 1, -20, -51, 1, -49, -51, 5, 0, -51, 3, -64};
//        System.out.println(unpackData(data, Long.class));
//        data = new byte[]{-65, 59};
//        System.out.println(unpackData(data, int.class));
        System.out.println(unpackData(new byte[]{-108, -88, 112, 105, 110, 103, 95, 97, 99, 107, -84, 50, 56, 48, 48, 48, 48, 48, 48, 50, 55, 49, 51, 1, 0},
                JfgUdpMsg.UdpRecvHeard.class));

        System.out.println(unpackData(new byte[]{-108, -91, 102, 95, 97, 99, 107, -84, 50, 48, 48, 48, 48, 48, 48, 48, 48, 57, 49, 53, -51, 6, 16, -46, -7, -1, -1, -1},
                TTTest.class));

        System.out.println(unpackData(new byte[]{-107, -50, 89, 18, -77, -10, 1, 7, 1, -95, 48},
                DpMsgDefine.DPAlarm.class));
    }

    @Message
    public static class TTTest extends JfgUdpMsg.UdpRecvHeard {

        @Index(2)
        public int ack;
        @Index(3)
        public int ret;

        @Override
        public String toString() {
            return "TTTest{" +
                    "ack=" + ack +
                    ", ret=" + ret +
                    '}';
        }
    }

    private void setData(long version) {
        System.out.println("version:" + version);
    }

    private <T> T unpackData(byte[] data, Class<T> clazz) throws IOException {
        MessagePack ms = new MessagePack();
        return ms.read(data, clazz);
    }

    int count = 0;

    @Test
    public void testDefaultTimeZone() throws InterruptedException {
        long startTick = System.currentTimeMillis() - RandomUtils.getRandom(2000);
        Thread.sleep(RandomUtils.getRandom(10) * 1000);
        int repeatCount = (int) (90 - (System.currentTimeMillis() - startTick) / 1000);
        System.out.println(repeatCount);

        Observable.just("go")
                .repeat(10)
                .map(new Func1<String, Object>() {
                    @Override
                    public Object call(String s) {
                        count++;
//                        if (count > 5)
//                            throw new TimeoutException();
                        System.out.println("...");
                        return null;
                    }
                })
                .doOnError(throwable -> System.out.println("err:" + throwable.getLocalizedMessage()))
                .subscribe();

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(RandomUtils.getRandom(10));
        }
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return lhs - rhs;
            }
        });
        System.out.println(list);

        System.out.println("HistoryFile".hashCode());
    }


}