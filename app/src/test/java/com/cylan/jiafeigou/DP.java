package com.cylan.jiafeigou;

import android.text.TextUtils;

import com.alibaba.sdk.android.oss.common.auth.HmacSHA1Signature;
import com.bumptech.glide.load.model.GlideUrl;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.live.IFeedRtcp;
import com.cylan.jiafeigou.misc.live.LiveFrameRateMonitor;
import com.cylan.jiafeigou.module.DoorLockHelper;
import com.cylan.jiafeigou.module.message.DPList;
import com.cylan.jiafeigou.module.message.DPMessage;
import com.cylan.jiafeigou.module.message.MIDHeader;
import com.cylan.jiafeigou.module.request.RobotForwardDataV3Request;
import com.cylan.jiafeigou.module.request.RobotForwardDataV3Response;
import com.cylan.jiafeigou.module.request.RobotGetDataResponse;
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
import org.msgpack.packer.BufferPacker;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.unpacker.BufferUnpacker;
import org.msgpack.unpacker.Unpacker;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
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
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
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
                //TODO 现在 VID 写死成 0001
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

        countDownLatch.await();
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

    @Test
    public void testCacheKey() throws Exception {
        GlideUrl glideUrl = new GlideUrl("http:www.baidu.com?key=uiuouo&uid=eotyowtyoey");
        System.out.println(glideUrl.getCacheKey());
    }

    @Test
    public void testH() throws Exception {
        MessagePack pack = new MessagePack();
        BufferPacker bufferPacker = pack.createBufferPacker();
        bufferPacker.writeArrayBegin(5);
        bufferPacker.write(888);
        bufferPacker.write("aaa");
        bufferPacker.write("bbb");
        bufferPacker.write(9999L);
        bufferPacker.write("ccc");
        bufferPacker.write("ddddd");
        bufferPacker.writeArrayEnd();
        byte[] bytes = bufferPacker.toByteArray();
        BufferUnpacker unpacker = pack.createBufferUnpacker(bytes);
        MIDHeader read = unpacker.read(MIDHeader.class);
        System.out.println(read);
    }

    @Test
    public void testMap() throws Exception {
        MessagePack pack = new MessagePack();
        BufferPacker packer = pack.createBufferPacker();
        HashMap<Integer, DPList> hashMap = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            DPList list = new DPList();
            for (int i1 = 0; i1 < 3; i1++) {
                list.add(new DPMessage(3, 88, new byte[]{8, 8, 8, 8}));
            }
            hashMap.put(i, list);
        }
        RobotGetDataResponse response = new RobotGetDataResponse(hashMap);
        packer.write(response);
        byte[] bytes = packer.toByteArray();
        response.rawBytes = bytes;
        BufferUnpacker unpacker = pack.createBufferUnpacker(bytes);
//        DpUtils.mp.register(DPList.class, new MyListTemplate());
//        RobotGetDataResponse convert = new RobotGetDataRequest().convert(response);
//        System.out.println(convert);

    }

    @Test
    public void testG() {
        new C().go();
    }

    abstract class G<T> {

        public void go() {
            getClass().getTypeParameters();
        }
    }

    class C extends G<String> {

    }

    class MyListTemplate extends AbstractTemplate<DPList> {

        @Override
        public void write(Packer packer, DPList dpMessages, boolean b) throws IOException {
            if (dpMessages != null) {
                packer.writeArrayBegin(dpMessages.size());
                for (DPMessage message : dpMessages) {
                    packer.writeArrayBegin(3);
                    packer.write(message.getMsgId());
                    packer.write(message.getVersion());
                    packer.write(message.getValue());
                    packer.writeArrayEnd();
                }
                packer.writeArrayEnd();
            }
        }

        @Override
        public DPList read(Unpacker unpacker, DPList dpMessages, boolean b) throws IOException {
            int count = unpacker.readArrayBegin();
            DPList result = new DPList();
            for (int i = 0; i < count; i++) {
                unpacker.readArrayBegin();
                int msgId = unpacker.readInt();
                long version = unpacker.readLong();
                byte[] bytes = unpacker.readByteArray();
                unpacker.readArrayEnd();
                DPMessage dpMessage = new DPMessage(msgId, version, bytes);
                result.add(dpMessage);
            }
            unpacker.readArrayEnd();
            return result;
        }
    }

    @Test
    public void testV3() throws IOException {
        MessagePack pack = new MessagePack();
        pack.register(DPList.class, new MyListTemplate());
        DPList dpList = new DPList();
        byte[] ddds = pack.write(new DpMsgDefine.DPChangeLockPassword("222", "ddd"));
        System.out.println(Arrays.toString(ddds));
        DPMessage ddd = new DPMessage(3, 0, ddds);
        dpList.add(ddd);
        RobotForwardDataV3Request robotForwardDataV3Request = new RobotForwardDataV3Request("sss", "ssdd", 32, dpList);
        byte[] bytes = pack.write(robotForwardDataV3Request);
        System.out.println(Arrays.toString(bytes));
        System.out.println(pack.createBufferUnpacker(bytes).read(RobotForwardDataV3Request.class).toString());
        BufferUnpacker unpacker = pack.createBufferUnpacker(bytes);
        RobotForwardDataV3Response read = unpacker.read(RobotForwardDataV3Response.class);
        System.out.println(read);
    }

    @Test
    public void testV31() throws IOException {
        DpMsgDefine.DPChangeLockPassword dpChangeLockPassword = new DpMsgDefine.DPChangeLockPassword("DDDD", "SSSSS");
        byte[] pack = DpUtils.pack(dpChangeLockPassword);
        System.out.println(Arrays.toString(pack));
//        byte[] pack1 = DpUtils.pack(new DPMessage(4, 9, pack));
//        DPMessage dpMessage = DpUtils.unpackData(pack1, DPMessage.class);
//        System.out.println(DpUtils.unpack(dpMessage.getValue()));
    }


    @Test
    public void testV3Action() {
        System.out.println(DoorLockHelper.OPEN_DOOR_LOCK_ACTION);
    }

    @Test
    public void testTimeout() throws InterruptedException {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
//                   subscriber.onNext("SSSSSSSSSs");
            }
        })
                .subscribeOn(Schedulers.io())
                .takeUntil(RxBus.getCacheInstance().toObservable(String.class))
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        System.out.println("AAAAAAAAAAAAAAAAAAA");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        System.out.println("error" + throwable.getMessage());
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        System.out.println("SSSSSSSSSSSSSSSSSSSs");
                    }
                });

        Observable.just("")
                .subscribeOn(Schedulers.io())
                .delay(3, TimeUnit.SECONDS)
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        RxBus.getCacheInstance().post("SSSSSSSSSSSSSs");
                    }
                });
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }

    @Test
    public void testUpTime() {
        long time = System.currentTimeMillis() / 1000 - 1514019654;
        int temp = (int) time / 60;
        int minute = temp % 60;
        temp = temp / 60;
        int hour = temp % 24;
        temp = temp / 24;
        int day = temp;
        System.out.println("day:" + day + ",hour:" + hour + ",minute:" + minute);
    }

    @Test
    public void testForEach() {
        List<P> items = new ArrayList<>();
        items.add(new P());
        items.add(new P());
        items.add(null);
        for (P item : items) {
            System.out.println(item.age);
        }
    }

    public static class P {
        String name;
        int age;
    }

    @Test
    public void testFloat() throws Exception {
        Number parse = NumberFormat.getInstance().parse("12.00");
        System.out.println(parse.intValue());
    }

    @Test
    public void testLength() {
        System.out.println(" https://jiafeigou-test.oss-cn-hangzhou.aliyuncs.com:443/long/18603076876/AI/290300000043/1517279718.jpg?security-token=CAISrwR1q6Ft5B2yfSjIpLDdEvDWma5Q2bavcm3jvDInXsRdm%2FSS0Tz2IHpPendgAu8ev%2Fo%2FmGpR6PsYlq0rE8cfHdNHnA2MqsY5yxioRqackTrej9Vd%2Bm3OewW6Dxr8w7WMAYHQR8%2FcffGAck3NkjQJr5LxaTSlWS7TU%2FiOkoU1VskLeQO6YDFaZrJRPRAwh8IGEnHTOP2xSKCA4AzqAVFvpxB3hE5m9K272bf80BfFi0DgweJndu6TY5GvdJtrJ4wtEYX3ju53f6TM0SFX9l1W%2Bbxqy%2FYVoW%2Bf5onFWwALslLEUavd%2BcY9KxRiNOpoWfxGrfHymPx3vfGWi4ns11EXbLkPC3yHHNj4mJKDWoHoP90iJ7HgICaPgJLdNJj8vQ4lbnUGcR9HYMZmcC4oUkR0EmmCcvP8vxKoLw6oUPqCy7pkk8g3nVfp9NSHJleIXvCF3D0EfZQ9YwRwb0ZMmi6DO6YNaF5LaEg1ReTXH4dyZR5TqKzvoEjOWzZ8iXVQufK5JZGfs6sEO4LkRcAEg8hPZpVPvWYrQFPqDq6jkVtTK2BsUPEUsuPkMoTt76SekqfBI7zEC%2FkJt1RVdi2UsyeWVTRRKCT%2B65phCxeR8N%2BDy7fXodEyUlks59lRAACKdssos1F9%2F%2By26BvUqLe7CDfo3ApjooWDptcft3EJJKn037Wg2RfFp2GVbKUDn8PaZXZiWxzfeQYimqvM2CJc%2BEhQzTHoYU9Btg6LvmGrZ921CVXtGoABF3j6TtD3T1%2B8tgQ5bH4PnGLHvsBWPcZZapXV1hct5tgc3P3TqbuHL%2F5YLSvEABrf0ISOMyZWcGrY%2BgWseUbW1Qi%2FV45XyBm9rKIv99eaq9jIWliqx9lcJTgRbTkLAgS%2FCLXRjskFY7OAB6dfvaOiMwOsisOowrw8nMOyoaP1aNM%3D&OSSAccessKeyId=STS.EshYJbtqqntDpKRX2rRKqt193&Expires=1525069921&Signature=W%2BxWV4uECKWRrmnOhrIHE%2FapJ8U%3D".getBytes().length);
    }
}
