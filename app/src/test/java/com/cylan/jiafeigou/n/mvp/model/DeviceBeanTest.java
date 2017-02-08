package com.cylan.jiafeigou.n.mvp.model;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.MyTestRunner;
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
@RunWith(MyTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
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

}