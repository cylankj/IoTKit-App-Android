package com.cylan.jiafeigou.cache.db.impl;

import com.cylan.jiafeigou.utils.RandomUtils;

import org.junit.Test;

/**
 * Created by holy on 2017/3/24.
 */
public class BaseDBHelperTest {

    @Test
    public void test() {
        int size = RandomUtils.getRandom(50);
        System.out.printf("account:" + size);
        if (size <= 1) return;
        String result = "";
        for (int i = 0; i < size; ) {
            if (size - i == 3) {
                result = result + buildOr(i + "", "" + (i + 1));
            } else {
                result = result + buildOr(i + "", "" + (i + 1), "" + (i + 2));
            }
            i += 2;
        }
        System.out.println(result);
    }

    private String buildOr(String content1, String content2, String... content) {
        if (content == null || content.length == 0)
            return content1 + "-or-" + content2;
        else {
            String ret = content1 + "-or-" + content2;
            for (int i = 0; i < content.length; i++) {
                ret = ret + "-or-" + content[i];
            }
            return ret;
        }
    }
}