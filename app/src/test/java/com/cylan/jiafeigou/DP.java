package com.cylan.jiafeigou;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.live.IFeedRtcp;
import com.cylan.jiafeigou.misc.live.LiveFrameRateMonitor;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BindUtils;
import com.google.gson.Gson;

import org.junit.Test;
import org.msgpack.MessagePack;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by yzd on 17-1-11.
 */
public class DP {
    @Test
    public void testLinkHashMap() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < 10; i++) {
            map.put("item" + i, "AAAAAA" + i);
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.println(entry.getValue());
        }
    }

    @Message
    static class T {
        @Index(0)
        public int ret;//       错误码
        @Index(1)
        public int secends;//   视频录制的时长,单位秒
        @Index(2)
        public int videoType;// 特征值定义：videoTypeShort =1 8s短视频；videoTypeLong =2长视频；
    }

    public static boolean accept(String account, String uuid, String filePath) {
        return filePath.matches("/" + account + "/" + uuid);
    }


    @Test
    public void testMerge() {
        RxBus.getCacheInstance().toObservable(String.class).mergeWith(Observable.create(subscriber -> {
            RxBus.getCacheInstance().post("just for test");
        }))
                .subscribe(ret -> {
                    System.out.println(ret);
                });
//        DpUtils.pack()
    }

    @Test
    public void testMonitor() throws Exception {
        LiveFrameRateMonitor monitor = new LiveFrameRateMonitor();
        IFeedRtcp.MonitorListener listener = new IFeedRtcp.MonitorListener() {
            @Override
            public void onFrameFailed() {
                System.out.println("onFrameFailed");
            }

            @Override
            public void onFrameRate(boolean slow) {
                System.out.println("onFrameRate+" + slow);
            }
        };
        monitor.setMonitorListener(listener);

        Random random = new Random(System.currentTimeMillis());
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(ret -> {
                    JFGMsgVideoRtcp rtcp = new JFGMsgVideoRtcp();
                    rtcp.frameRate = random.nextInt(10);
                    System.out.println("Frame:" + rtcp.frameRate);
                    monitor.feed(rtcp);
                });

        Thread.sleep(100000000);
    }

    @Message
    static class MsgForwardT {
        @Index(0)
        public int mId;
        @Index(1)
        public String mCaller;
        @Index(2)
        public String mCallee;
        @Index(3)
        public long mSeq;
        // 1.如果是客户端发起，则为设备CID数组；
        // 2.如果是设备端发起：
        //    a. 服务器查询主账号，再查询sessid，填充后转发给客户端；
        //    isFriend. dst为账号数组时，服务器查询sessid，填充后转发给客户端（暂未支持）； --- 第三方账号，绑定关系不在加菲狗平台。
        @Index(4)
        public List<String> dst;
        @Index(5)
        public int isAck;//非零需要对端响应，零不需要对端响应
        @Index(6)
        public int type;// 功能定义。见下表定义
        @Index(7)
        public byte[] msg;
    }

    @Test
    public void ssg() throws IOException {
        byte[] msg = {-108, -49, 0, 0, 0, 3, -82, 48, 0, 0, -49, 0, 0, 0, 2, 20, -114, 0, 0, 0, -61};

        System.out.println(new MessagePack().read(msg).toString());
    }

    @Test
    public void test() throws IOException {
//        -110,0,12
        byte[] bytes = new byte[]{-110, -1, -50, 0, 57, 21, -80};
        T t = DpUtils.unpackData(bytes, T.class);
        System.out.println(new Gson().toJson(t));
    }

    @Test
    public void sample() {
        Observable.interval(1, TimeUnit.SECONDS).subscribe(s -> {
            RxBus.getCacheInstance().post("SSSSS" + s);
        }, e -> AppLogger.d(e.getMessage()));

        RxBus.getCacheInstance().toObservable(String.class)
                .cache(2)
                .buffer(2)
                .subscribe(s -> {
                    System.out.println(s);
                }, e -> AppLogger.d(e.getMessage()));
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //   LocalUdpMsg{ip='192.168.103.166', port=10008, data=[-109, -83, 100, 111, 111, 114, 98, 101, 108, 108, 95, 114, 105, 110, 103, -84, 53, 48, 48, 48, 48, 48, 48, 48, 49, 50, 57, 56, -96]}
    }

    @Message
    public static class bellRing {
        @Index(0)
        public String cmd;
        @Index(1)
        public String cid;
        @Index(2)
        public String mac;
    }

    @Test
    public void Bell() {
        byte[] bytes = new byte[]{-109, -83, 100, 111, 111, 114, 98, 101, 108, 108, 95, 114, 105, 110, 103, -84, 53, 48, 48, 48, 48, 48, 48, 48, 49, 50, 57, 56, -96};

        try {
            bellRing bellRing = DpUtils.unpackData(bytes, bellRing.class);
            System.out.println(new Gson().toJson(bellRing));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void sss() {

        new File("/storage", "/mnt/sdcard/333.mp4");

    }

    private String getClientKey(String url) {
        //http://222.222.222.222./
        if (TextUtils.isEmpty(url) && url.contains("/")) {
            return url.substring(url.indexOf("/"), url.length());
        }
        return url;
    }

    @Test
    public void sub() {
        String ss = "eohgoewhgoewhgoewhgoehgoh";
        String s = ss.substring(3, ss.length() - 3);
        System.out.println(s);
    }

    @Test
    public void patternTest() {
//        System.out.println(BindUtils.versionCompare("4", "3.0.0.1011"));
        System.out.println(BindUtils.versionCompare("3.0.0", "3.0.0.1011"));
        System.out.println(BindUtils.versionCompare("3.1.0.1011", "3.0.0"));
//        System.out.println(BindUtils.versionCompare("3.3.0.1011", "3.0.0.1011"));
//        System.out.println(BindUtils.versionCompare("2.1.0.1011", "3.0.0.1011"));
//        System.out.println(BindUtils.versionCompare("3.1.0.1011", "(3.0.0.1011"));
//        System.out.println(BindUtils.versionCompare("3.1.3.1011", "(3.0.0.1011)"));
//        System.out.println(BindUtils.versionCompare("3.1.0.1011", "3.0.0.1011"));
//        System.out.println(BindUtils.versionCompare("3.1.0.1011", "3.0.0.1011"));
//        System.out.println(BindUtils.versionCompare("3.1.0.1011", "3.0.0.1011"));
//        System.out.println(BindUtils.versionCompare("3.1.0.1011", "3.0.0.1011"));
//        System.out.println(BindUtils.versionCompare("3.1.0.1011", "3.0.0.1011"));
//        System.out.println(BindUtils.versionCompare("3.1.0.1011", "3.0.0.1011"));
    }

    @Test
    public void test505() throws Exception {
        MessagePack messagePack = new MessagePack();
        byte[] bytes = new byte[]{-106, -50, 89, -126, -114, 116, 1, 1, 1, -95, 48, -112};
        messagePack.read(bytes);
        System.out.println(messagePack.toString());
    }

    @Test
    public void test515() throws Exception {
        MessagePack messagePack = new MessagePack();
        byte[] bytes = new byte[]{-108, 1, 2, 3, 4};
        messagePack.read(bytes);
        System.out.println(messagePack.toString());
    }


}
