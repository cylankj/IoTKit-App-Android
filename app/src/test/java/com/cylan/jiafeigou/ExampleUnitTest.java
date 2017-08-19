package com.cylan.jiafeigou;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
//gradle build tools 2.2.0暂时不满足，单元测试。
//<fragmentComponent name="ProjectRootManager" version="2" languageLevel="JDK_1_7" default="true" assert-keyword="true" jdk-15="true" project-jdk-name="1.7" project-jdk-type="JavaSDK">/
@RunWith(MyTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testYou() {
        System.out.println("good");
    }

    @Test
    public void testMe() {
        String ss = null;
        System.out.print(ss);
        System.out.println("fileName".hashCode());
    }

    @Test
    public void testLinkHashMap() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < 10; i++) {
            map.put("item" + i, "AAAAAA" + i);
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.println(entry.getValue());
        }
    }


}