package com.cylan.jiafeigou.dp;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.utils.RandomUtils;
import com.cylan.udpMsgPack.JfgUdpMsg;

import org.junit.Test;
import org.msgpack.MessagePack;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b))
                    .append(",");
        return sb.toString();
    }

    @Message
    public static class Good {
        @Index(0)
        public String cmd;
        @Index(1)
        public String cid;
        @Index(2)
        public String mac;
        @Index(3)
        public byte[] version;

        @Override
        public String toString() {
            return "{" +
                    "cmd='" + cmd + '\'' +
                    ", cid='" + cid + '\'' +
                    ", mac='" + mac + '\'' +
                    ", version=" + Arrays.toString(version) +
                    '}';
        }
    }

    @Test
    public void testObject() throws IOException {

        System.out.println("DPSdcardSummary:" + unpackData(new byte[]{-110,-62,0},
                DpMsgDefine.DPSdcardSummary.class));
        System.out.println("DPBellCallRecord:" + unpackData(new byte[]{-108, 0, -50, 89, 120, 45, -68, 0, 1},
                DpMsgDefine.DPBellCallRecord.class));

        Good good = new Good();
        good.cid = "11111";
        byte[] raw = DpUtils.pack(good);
        System.out.println("raw: " + Arrays.toString(raw));
        System.out.println("raw->obj: " + unpackData(raw, Good.class));
        byte[] array = new byte[]{-108, -86, 102, 95, 112, 105, 110, 103, 95, 97, 99, 107, -84, 50, 57, 48, 48, 48, 48, 48, 48, 48, 49, 50, 50, -79, 65, 67, 58, 56, 51, 58, 70, 51, 58, 56, 49, 58, 54, 54, 58, 67, 70, -72, 49, 46, 48, 46, 48, 46, 48, 48, 50, 54, 44, 49, 46, 48, 46, 48, -108, -95, 80, 107, 104, -95, 80, 107};
        System.out.println(byteArrayToHex(array));
        System.out.println("UdpSecondaryHeard:" + unpackData(new byte[]{-110, -1, -90, 121, 121, 116, 101, 115, 116}, DpMsgDefine.DPNet.class));
        System.out.println("FPingAck:" + unpackData(array, Good.class));
        System.out.println("FPingAck:" + unpackData(new byte[]{49, 46, 48, 46, 48, 46, 48, 48, 50, 54,
                44, 49, 46, 48, 46, 48, -108, -95, 80, 107, 104, -95, 80, 107}, TT.class));
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

        System.out.println(unpackData(new byte[]{-108, -49, 0, 0, 0, 1, -37, 0, 0, 0, -50, 91, 112, 0, 0, 0, -61},
                DpMsgDefine.DPSdStatus.class));

        System.out.println(unpackData(new byte[]{-110, -83, 65, 115, 105, 97, 47, 83, 104, 97, 110, 103, 104, 97, 105, -51, 112, -128},
                DpMsgDefine.DPTimeZone.class));

        System.out.println(unpackData(new byte[]{-108, -86, 115, 101, 116, 95, 97, 112, 95, 114, 115, 112, -84, 54, 48, 48, 54, 48, 48, 48, 48, 48, 49, 48, 48, -96, -46, -117, 8, -6, -73},
                UdpConstant.SetApRsp.class));

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


    @Test
    public void some() {
        try {
            System.out.println(unpackData(new byte[]{-109, -86, 114, 101, 112, 111, 114, 116, 95, 109, 115, 103, -84, 50, 57, 48, 48, 48, 48, 48, 48, 48, 48, 48, 53, -69, -104, -51, 78, 38, -84, 50, 57, 48, 48, 48, 48, 48, 48, 48, 48, 48, 53, -96, -50, 85, -61, 120, -78, -112, 0, 0, -96}, TTest.class));
            System.out.println(unpackData(new byte[]{-104, -51, 78, 38, -84, 50, 57, 48, 48, 48, 48, 48, 48, 48, 48, 48, 53, -96, -50, 85, -61, 120, -78, -112, 0, 0, -96}, Mesg.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Message
    public static final class TTest {
        @Index(0)
        public String what;
        @Index(1)
        public String cid;
        @Index(2)
        public byte[] msg;

        @Override
        public String toString() {
            return "TTest{" +
                    "what='" + what + '\'' +
                    ", cid='" + cid + '\'' +
                    ", msg=" + Arrays.toString(msg) +
                    '}';
        }
    }

    @Message
    public static final class Mesg {
        @Index(0)
        public byte[] ret;
        @Index(1)
        public boolean r;

        @Override
        public String toString() {
            return "Mesg{" +
                    "ret=" + Arrays.toString(ret) +
                    ", r=" + r +
                    '}';
        }
    }

    @Message
    public static class TT {
        @Index(0)
        public int ret;

        @Override
        public String toString() {
            return "TT{" +
                    "ret=" + ret +
                    '}';
        }
    }

    @Message
    public static class T {
        @Index(0)
        public byte[] r;

        @Override
        public String toString() {
            return "T{" +
                    "r=" + r +
                    '}';
        }
    }
}