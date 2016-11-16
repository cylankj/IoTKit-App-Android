package com.cylan.jiafeigou.dp;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.MyTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.msgpack.MessagePack;
import org.robolectric.annotation.Config;

import java.io.IOException;

/**
 * Created by cylan-hunt on 16-11-16.
 */
@RunWith(MyTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class DpMsgDefineTest {

    @Test
    public void test() {
        byte[] b = new byte[1];
        b[0] = -61;
        try {
            Object o = unpackData(b, String.class);
            System.out.println("O: " + o);
        } catch (IOException e) {
            System.out.println("what: " + e.getLocalizedMessage());
        }
    }

    private <T> T unpackData(byte[] data, Class<T> clazz) throws IOException {
        MessagePack ms = new MessagePack();
        return ms.read(data, clazz);
    }

}