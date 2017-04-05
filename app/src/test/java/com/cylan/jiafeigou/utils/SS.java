package com.cylan.jiafeigou.utils;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yzd on 17-1-11.
 */

public class SS {
    @Test
    public void test() {

        Map<String, Good> map = new HashMap<>();
        map.put("what", new Good());
        map.put("what0", new Good());
        Good good = map.get("what");
        good = null;
        System.out.println(map);

    }

    private static class Good {
    }
}
