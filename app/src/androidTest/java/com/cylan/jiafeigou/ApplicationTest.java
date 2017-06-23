package com.cylan.jiafeigou;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.jiafeigou.misc.live.LiveFrameRateMonitor;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * <activity_cloud_live_mesg_call_out_item href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</activity_cloud_live_mesg_call_out_item>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }


    public void testDBHelper() {

        Observable.interval(10, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .subscribe(s -> {


                }, e -> {
                    e.printStackTrace();
                });

    }

    public void testMonitor() throws Exception {
        LiveFrameRateMonitor monitor = new LiveFrameRateMonitor();
        Random random = new Random(System.currentTimeMillis());
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(ret -> {
                    JFGMsgVideoRtcp rtcp = new JFGMsgVideoRtcp();
                    rtcp.frameRate = random.nextInt(30);
                    monitor.feed(rtcp);
                });

        Thread.sleep(100000000);
    }

    public void testMy() {
//        JFGDevice jfgDevice = new JFGDevice();
//        jfgDevice.pid = 5;
//        jfgDevice.alias = "摄像头88";
//        jfgDevice.shareAccount = "";
//        jfgDevice.uuid = "2000000888";
//        DoorBellDevice device = new DoorBellDevice();
////        device.setDevice(jfgDevice);
//        Random random = new Random();
//
//
//        DpMsgDefine.DPBellCallRecord record;
//        JFGDPMsg msg;
//        List<JFGDPMsg> list = new ArrayList<>(100000);
//        for (int i = 0; i < 100000; i++) {
//            msg = new JFGDPMsg();
//            record = new DpMsgDefine.DPBellCallRecord();
//            record.duration = random.nextInt(1000);
//            record.isOK = random.nextInt(2);
//            record.startTime = random.nextInt(1000000);
//            record.type = random.nextInt(5);
//            byte[] pack = DpUtils.pack(record);
//            msg.id = 401;
//            msg.version = random.nextLong();
//            msg.packValue = pack;
//            list.add(msg);
//        }
//        Log.e("AAAAA", "开始解析了.......");
//        long initSubscription = System.currentTimeMillis();
//        for (JFGDPMsg jfgdpMsg : list) {
////            device.setPackValue(jfgdpMsg);
//        }
//        long end = System.currentTimeMillis();
//        Log.e("AAAAA", "解析100000条数据共耗时" + (end - initSubscription) + "毫秒");
    }


}