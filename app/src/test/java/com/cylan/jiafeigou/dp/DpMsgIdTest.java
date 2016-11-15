package com.cylan.jiafeigou.dp;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.MyTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Created by cylan-hunt on 16-11-8.
 */
@RunWith(MyTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class DpMsgIdTest {

    @Test
    public void testRepeatValue() {
        final int count = DpMsgMap.ID_2_CLASS_MAP.size();
        Set<String> keySet = DpMsgMap.NAME_2_ID_MAP.keySet();
        Iterator<String> iterator = keySet.iterator();
        List<Integer> valueList = new ArrayList<>();
        while (iterator.hasNext()) {
            valueList.add(DpMsgMap.NAME_2_ID_MAP.get(iterator.next()));
        }
        final int valueListSize = valueList.size();
        System.out.println(valueListSize);
        valueList = new ArrayList<>(new HashSet<>(valueList));
        assertTrue("不等", valueList.size() == valueListSize);

        for (int i = 201; i < 220; i++) {
            Class<?> clazz = DpMsgMap.ID_2_CLASS_MAP.get(i);
            System.out.println("i: " + i + " " + clazz);
            System.out.println(clazz.isInstance(int.class));
        }
    }

}