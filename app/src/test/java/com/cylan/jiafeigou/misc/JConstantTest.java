package com.cylan.jiafeigou.misc;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.MyTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

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

}