package com.cylan.jiafeigou.utils;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by cylan-hunt on 16-11-10.
 */
public class AESUtilsTest {

    @Test
    public void encrypt() throws Exception {
        String content = "nihao...//121";
        System.out.println(content);
        String afterContent = AESUtils.encrypt(content, content);
        System.out.println(afterContent);
        String deContent = AESUtils.decrypt(content, afterContent);
        System.out.println(deContent);
        assertEquals(true, content.equals(deContent));
    }

    @Test
    public void decrypt() throws Exception {

    }

    @Test
    public void toHex() throws Exception {

    }

    @Test
    public void fromHex() throws Exception {

    }

    @Test
    public void toByte() throws Exception {

    }

    @Test
    public void toHex1() throws Exception {

    }

}