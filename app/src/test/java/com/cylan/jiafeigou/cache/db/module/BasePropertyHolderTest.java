package com.cylan.jiafeigou.cache.db.module;

import com.cylan.jiafeigou.utils.RandomUtils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by hds on 17-3-28.
 */
public class BasePropertyHolderTest {

    List<Object> list = new ArrayList<>();

    @Test
    public void simpleTest() {
        for (int i = 0; i < 5; i++) {
            boolean what = RandomUtils.getRandom(5) % 2 == 0;
            list.add(what ? true : (RandomUtils.getRandom(7) % 2 == 0) ? i : "what");
        }
        for (int i = 0; i < 5; i++) {
            Object o = list.get(i);
            Object f = get(o, 1);
            System.out.println("list:" + o);
            System.out.println(f);
        }
        long startTick = System.currentTimeMillis() - 10L;
        startTick = System.currentTimeMillis() - startTick;
        System.out.println(10 * 1000L - startTick);
    }

    private <V> V get(Object o, V defaultValue) {
        System.out.println(defaultValue.getClass());
        if (o != null && defaultValue.getClass().isInstance(o)) {
            return (V) o;
        }
        return defaultValue;
    }
}