package com.cylan.jiafeigou;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.jiafeigou.base.module.BellDevice;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by yzd on 17-1-11.
 */


public class MyTest {


    @Test
    public void testMe() {

        JFGDevice jfgDevice = new JFGDevice();
        jfgDevice.pid = 5;
        jfgDevice.alias = "摄像头88";
        jfgDevice.shareAccount = "";
        jfgDevice.uuid = "2000000888";
        BellDevice device = new BellDevice();
        device.setDevice(jfgDevice);
        Random random = new Random();


        DpMsgDefine.DPBellCallRecord record;
        JFGDPMsg msg;
        List<JFGDPMsg> list = new ArrayList<>(100000);
        for (int i = 0; i < 100000; i++) {
            msg = new JFGDPMsg();
            record = new DpMsgDefine.DPBellCallRecord();
            record.duration = random.nextInt(1000);
            record.isOK = random.nextInt(2);
            record.time = random.nextInt(1000000);
            record.type = random.nextInt(5);
            byte[] pack = DpUtils.pack(record);
            msg.id = 401;
            msg.version = random.nextLong();
            msg.packValue = pack;
            list.add(msg);
        }
        System.out.println("开始解析了.......");
        long start = System.currentTimeMillis();
        for (JFGDPMsg jfgdpMsg : list) {
            device.setValue(jfgdpMsg);
        }
        long end = System.currentTimeMillis();
        System.out.println("解析100000条数据共耗时" + (end - start) / 1000 + "秒");
    }
}
