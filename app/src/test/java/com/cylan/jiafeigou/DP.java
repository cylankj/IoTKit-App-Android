package com.cylan.jiafeigou;

import android.text.TextUtils;

import com.alibaba.sdk.android.oss.common.auth.HmacSHA1Signature;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.live.IFeedRtcp;
import com.cylan.jiafeigou.misc.live.LiveFrameRateMonitor;
import com.cylan.jiafeigou.module.SubscriptionManager;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AESUtil;
import com.cylan.jiafeigou.utils.BindUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.github.promeg.pinyinhelper.Pinyin;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;

import org.junit.Test;
import org.msgpack.MessagePack;
import org.msgpack.annotation.Index;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Response;
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

    @org.msgpack.annotation.Message
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
    public void testPinYin() {
        String pinyin = Pinyin.toPinyin("2324242", "");
        System.out.println(pinyin);
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

    @org.msgpack.annotation.Message
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

    @org.msgpack.annotation.Message
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


    @Test
    public void testDP() throws Exception {
        MessageBufferPacker packer = org.msgpack.core.MessagePack.newDefaultBufferPacker();
        packer.packInt(4);
        byte[] bytes = packer.toByteArray();

        MessageUnpacker messageUnpacker = org.msgpack.core.MessagePack.newDefaultUnpacker(bytes);
        String toString = messageUnpacker.unpackValue().toString();

        JsonObject fromJson = new Gson().fromJson(toString, new TypeToken<JsonObject>() {
        }.getType());
        System.out.println(new Gson().toJson(fromJson));
//        System.out.println(messageUnpacker.unpackValue().toJson());

    }


    @Test
    public void testPageMessage() {
//        List<Integer> filter = PAGE_MESSAGE.PAGE_HOME.filter(OS_PROPERTY.OS_DOG_1W_V2_2000_7.getProperties());
//
//        System.out.println(filter);
    }

    @Test
    public void testTuple() {

    }

    @Test
    public void test517() throws IOException {
//        ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
//        Object value = objectMapper.readValue(new byte[]{-107, 2, -95, 2, 1, 0, 1}, Object.class);
        DpMsgDefine.DPCameraLiveRtmpStatus rtmpStatus = DpUtils.unpackData(new byte[]{-107, 2, -38, 0, 51, 114, 116, 109, 112, 58, 47, 47, 97, 46, 114, 116, 109, 112, 46, 121, 111, 117, 116, 117, 98, 101, 46, 99, 111, 109, 47, 108, 105, 118, 101, 50, 47, 51, 114, 97, 117, 45, 101, 57, 106, 99, 45, 107, 54, 106, 100, 45, 54, 122, 55, 48, 2, -50, 89, -74, 80, 52, 0}, DpMsgDefine.DPCameraLiveRtmpStatus.class);
        System.out.println(rtmpStatus);
    }

    @Test
    public void testMethod() {
        String ssss = new GoogleAuthorizationCodeRequestUrl("985304692675-g88597eecjhpu5b3cn16b2s8f817rush.apps.googleusercontent.com", "http://www.baidu.com", Arrays.asList("sssss")).build();
        System.out.println(ssss);

    }

    public void methodName() {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
    }


    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public static class DPHeader {
        public int id;
    }

    public static class A extends DPHeader {
        public A() {
        }

        public A(String n) {
            this.n = n;
            id = 888;
        }

        public String n;
    }

    public static class B extends DPHeader {
        public B() {

        }

        public long b;

        public B(long b) {
            this.b = b;
            id = 666;
        }
    }


    @JsonFormat(shape = JsonFormat.Shape.ARRAY, with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public static class GetDataRsp {

        @JsonDeserialize(using = ContentConverter.class)
        public Map<Integer, Object> maps;

        @Override
        public String toString() {
            return "GetDataRsp{" +
                    "maps=" + maps +
                    '}';
        }
    }


    public static class ContentConverter extends StdDeserializer<Map<Integer, Object>> {
        protected ContentConverter() {
            super(Map.class);
        }

        @Override
        public Map<Integer, Object> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            Map<Integer, Object> maps = new HashMap<>();
            TreeNode treeNode = p.readValueAsTree();

            treeNode.get("");

            return maps;
        }
    }

    @Test
    public void testGraphPath() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());

        List<Object> temp = new ArrayList<>();
        temp.add(3);
        temp.add(5);
        temp.add("DDDDDDDDDDD");
        byte[] bytes = objectMapper.writeValueAsBytes(temp);

        Object o = objectMapper.readValue(bytes, Object.class);
        SSS sss = objectMapper.convertValue(o, SSS.class);
        System.out.println(o);
        System.out.println(sss);


    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder({"a", "b"})
    public static class SSS {
        public int a;
        public int b;
        public Object bytes;

        @Override
        public String toString() {
            return "SSS{" +
                    "a=" + a +
                    ", b=" + b +
                    ", bytes=" + bytes +
                    '}';
        }
    }

    @Test
    public void testAIService() throws Exception {
        String timeMillis = String.valueOf(System.currentTimeMillis() / 1000);
        System.out.println(timeMillis);
        String seceret = "6ZVBcFK6NLMg0zwjY0uuwdBiXUs7D1d9";
        String sign = AESUtil.sign(JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_QUERY_API, seceret, timeMillis);
        Response response = OkGo.post("http://yf.robotscloud.com/aiservice/v1/search_face")
                .cacheMode(CacheMode.REQUEST_FAILED_READ_CACHE)
                .params(JConstant.RobotCloudApi.ROBOTSCLOUD_VID, "0001")
                .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SERVICE_KEY, "v0UAlWduk09lvo4qUWZOaNcZeiACHEwm")
                .params(JConstant.RobotCloudApi.ROBOTSCLOUD_BUSINESS, "1")
                .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SERVICETYPE, "1")
                .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SIGN, sign)
                .params(JConstant.RobotCloudApi.ROBOTSCLOUD_TIMESTAMP, timeMillis)
                .execute();
        System.out.println(response.body().string());

        Thread.sleep(1000000);
    }

    @Test
    public void testHmac() throws Exception {
        String timeMillis = String.valueOf(10000000);
        System.out.println(timeMillis);
        //WfZCXJwU7j3eLjA4IWBVFSHgoH0=
        String seceret = "6ZVBcFK6NLMg0zwjY0uuwdBiXUs7D1d9";
        String sign = AESUtil.sign(JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_QUERY_API, seceret, timeMillis);

        System.out.println(sign);

        System.out.println(new HmacSHA1Signature().computeSignature(seceret,
                JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_QUERY_API + "\n" + timeMillis));
    }

    @Test
    public void testMethodOverCall() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(0);
        Observable.create(s -> {
            s.add(new SubscriptionManager.AbstractSubscription() {
                @Override
                protected void onUnsubscribe() {
                    System.out.println("$$$$$$$$$$$$$4");
                }
            });

//            s.onNext("AAA");
            s.onNext("AAAAAAAAAA");
        })
                .flatMap(ret -> Observable.from(new String[]{"SSS", "DDDDD"}))
                .flatMap(ret -> {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return Observable.from(new String[]{"SSS", "DDDDD"});
                })
                .subscribe(ret -> {
                    System.out.println("BBBBBBBBBBB");
                }, e -> {
                    countDownLatch.countDown();
                    System.err.println(e);
                }, () -> {
                    countDownLatch.countDown();
                    System.out.println("AAAAAAAAAA");
                });
        countDownLatch.await();
    }

    SubscriptionManager manager = new SubscriptionManager();

    public String mmm() {
        manager.atomicMethod()
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    return s;
                })
                .delay(3000, TimeUnit.MILLISECONDS)
                .subscribe(System.out::println, System.err::println, () -> {
                    System.out.println("completed");
                });
        return "";
    }

    @Test
    public void testFetch() throws Exception {
        byte[] bytes = new byte[]{-107, -84, 50, 57, 48, 49, 48, 48, 48, 48, 48, 48, 48, 51, 2, -38, 0, 32, 50, 48, 49, 55, 49, 48, 51, 49, 49, 55, 49, 55, 49, 53, 118, 76, 54, 80, 103, 80, 77, 55, 49, 120, 120, 118, 120, 100, 69, 106, 80, 98, 0, -111, -109, -51, 1, -7, -49, 0, 0, 1, 95, 96, -33, 85, 80, -38, 0, 49, -104, -50, 89, -13, -17, 114, 0, 0, 1, -95, 48, -112, 0, -111, -38, 0, 32, 50, 48, 49, 55, 49, 48, 50, 56, 49, 48, 52, 54, 49, 48, 76, 116, 76, 80, 87, 97, 115, 113, 57, 113, 79, 97, 71, 104, 49, 113, 98, 110
        };
        System.out.println(new MessagePack().read(bytes));
    }

    @Test
    public void valueTest() throws Exception {
        MessagePack pack = new MessagePack();
        DpMsgDefine.DPAlarm dpAlarm = new DpMsgDefine.DPAlarm();
        dpAlarm.humanNum = 0;
        dpAlarm.version = 39393;
        dpAlarm.face_id = new String[]{"ewghoegheogh"};
        dpAlarm.msgId = 505;
        dpAlarm.isRecording = 1;
        dpAlarm.fileIndex = 2;
        dpAlarm.objects = new int[]{1, 3};
        dpAlarm.ossType = 2;
        dpAlarm.time = 368362486;
        dpAlarm.tly = "1";
        DpMsgDefine.DpMessage messageHeader = new DpMsgDefine.DpMessage();
        messageHeader.msgId = 505;
        messageHeader.version = 47497969;
        messageHeader.value = pack.read(pack.write(dpAlarm));
        byte[] bytes = pack.write(messageHeader);
        DpMsgDefine.DpMessage read = pack.createBufferUnpacker(bytes).read(DpMsgDefine.DpMessage.class);
        System.out.println("SSSSSSSs");

//        System.out.println(value);

    }

}
