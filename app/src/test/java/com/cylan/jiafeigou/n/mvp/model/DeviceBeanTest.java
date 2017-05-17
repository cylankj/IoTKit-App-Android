package com.cylan.jiafeigou.n.mvp.model;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.MyTestRunner;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.RandomUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by cylan-hunt on 16-11-8.
 */
public class DeviceBeanTest {

    @Test
    public void testHashCode() {
        List<DeviceBean> beanList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            DeviceBean bean = new DeviceBean();
            bean.alias = RandomUtils.getRandom(50) + "";
            bean.uuid = i + "";
            bean.uuid = i + "";
            bean.sn = i + "";
            beanList.add(bean);
        }
        for (int i = 8; i < 12; i++) {
            DeviceBean bean = new DeviceBean();
            bean.alias = RandomUtils.getRandom(50) + "";
            bean.uuid = i + "";
            bean.uuid = i + "";
            bean.sn = i + "";
            beanList.add(bean);
        }
        beanList = new ArrayList<>(new HashSet<>(beanList));
        assertTrue(beanList.size() == 12);


    }


    @Test
    public void testNet() throws InterruptedException {
        System.out.println(pingQQ());
        Thread.sleep(3000);
    }

    public static final boolean pingQQ() {
        try {
            final String ip = "www.qq.com";
            Process p = Runtime.getRuntime().exec("ping -c 3 " + ip);// ping网址3次
            // ping的状态
            return p.waitFor() == 0;
        } catch (Exception e) {
            AppLogger.d("获取真实网络连接状态出错:" + e.getMessage());
        }
        return false;
    }
}