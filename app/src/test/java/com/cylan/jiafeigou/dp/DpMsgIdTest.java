package com.cylan.jiafeigou.dp;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.MyTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * Created by cylan-hunt on 16-11-8.
 */
@RunWith(MyTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class DpMsgIdTest {

    @Test
    public void testRepeatValue() {
//        final int count = IdMap.size();
//        Set<String> keySet = DpMsgIdMap.IdMap.keySet();
//        Iterator<String> iterator = keySet.iterator();
//        List<Integer> valueList = new ArrayList<>();
//        while (iterator.hasNext()) {
//            valueList.add(IdMap.get(iterator.next()));
//        }
//        final int valueListSize = valueList.size();
//        System.out.println(valueListSize);
//        valueList = new ArrayList<>(new HashSet<>(valueList));
//        assertTrue("不等", valueList.size() == valueListSize);
//
//        for (int i = 201; i < 220; i++) {
//            Class<?> clazz = DpMsgIdClassMap.Id2ClassMap.get(i);
//            System.out.println("i: " + i + " " + clazz);
//            System.out.println(clazz.isInstance(int.class));
//        }
    }

}