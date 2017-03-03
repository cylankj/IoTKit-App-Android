package com.cylan.jiafeigou.dp;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.MyTestRunner;
import com.cylan.jiafeigou.cache.CacheParser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.msgpack.MessagePack;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertNotNull;

/**
 * Created by cylan-hunt on 16-11-8.
 */
@RunWith(MyTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class DpParserTest {

    CacheParser dpParser;

    @Before
    public void setup() {
        dpParser = Mockito.mock(CacheParser.class);
        assertNotNull("dpParser is null?", dpParser);
    }

    @Test
    public void getDpParser() throws Exception {
//        dpParser.registerDpParser();
//        JFGDevice device = Mockito.mock(JFGDevice.class);
//        device.shareAccount = "shut";
//        device.alias = "没有";
//        device.pid = 6;
//        device.sn = "";
//        device.uuid = "20000004958";
//        ArrayList<JFGDevice> list = new ArrayList<>();
//        list.add(device);
//        RxEvent.DeviceListRsp deviceList = new RxEvent.DeviceListRsp(list);
//        RxBus.getCacheInstance().post(deviceList);
//        Thread.sleep(5000);


        String name = "nihao";
        MessagePack msg = new MessagePack();
        byte[] data = msg.write(name);
        System.out.println(data);


    }

    @Test
    public void registerDpParser() throws Exception {

    }

    @Test
    public void unregisterDpParser() throws Exception {

    }

}