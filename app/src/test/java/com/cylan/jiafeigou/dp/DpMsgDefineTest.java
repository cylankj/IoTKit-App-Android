package com.cylan.jiafeigou.dp;

import android.content.Intent;
import android.os.Parcelable;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.MyTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.msgpack.MessagePack;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.TimeZone;

/**
 * Created by cylan-hunt on 16-11-24.
 */
//@RunWith(MyTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class DpMsgDefineTest {


    @Test
    public void testObject() {
        DpMsgDefine.DPNet net = new DpMsgDefine.DPNet();
        net.net = 2;
        net.ssid = "so good";
        Intent intent = new Intent();
        intent.putExtra("yige", net);

        Parcelable p = intent.getParcelableExtra("yige");
        System.out.println("..." + p);
    }

    private <T> T unpackData(byte[] data, Class<T> clazz) throws IOException {
        MessagePack ms = new MessagePack();
        return ms.read(data, clazz);
    }

    @Test
    public void testDefaultTimeZone() {
        TimeZone timeZone = TimeZone.getDefault();
        System.out.println(timeZone.getID());
    }
}