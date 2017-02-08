package com.cylan.jiafeigou.misc;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.MyTestRunner;
import com.cylan.jiafeigou.utils.RandomUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by cylan-hunt on 16-11-8.
 */
@RunWith(MyTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class JConstantTest {

    @Test
    public void testMap() {
        for (int i = 0; i < JConstant.OS_MAX_COUNT; i++) {
            assertNotNull(JConstant.onLineIconMap.get(i));
            assertNotNull(JConstant.offLineIconMap.get(i));
        }
    }

    @Test
    public void testPatterns() {
        String str = "abcdefghigklmnopkrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder nameBuilder = new StringBuilder();
        for (int j = 0; j < 1000; j++) {
            nameBuilder.setLength(0);
            nameBuilder.append("DOG-");
            for (int i = 0; i < 6; i++) {
                nameBuilder.append(str.charAt(RandomUtils.getRandom(str.length())));
            }
            System.out.println(nameBuilder.toString());
            assertTrue(JConstant.JFG_DOG_DEVICE_REG.matcher(nameBuilder.toString()).find());
        }
    }
}