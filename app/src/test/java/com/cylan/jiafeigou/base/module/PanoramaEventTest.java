package com.cylan.jiafeigou.base.module;

import org.junit.Test;
import org.msgpack.MessagePack;

import java.io.IOException;

/**
 * Created by yanzhendong on 2017/3/17.
 */
public class PanoramaEventTest {


    @Test
    public void tst() throws IOException {

        byte[] data = new byte[]{-108, 0, -82, 49, 52, 56, 57, 55, 53, 50, 55, 48, 53, 46, 106, 112, 103, -50, 0, 27, -79, 101, -80, 93, -83, -8, 107, 63, -68, -51, -74, 66, 2, -104, 104, -8, -56, -3, -127};

        MessagePack mp = new MessagePack();

        PanoramaEvent.MSG_TYPE_TAKE_PICTURE_RSP read = mp.createBufferUnpacker(data).read(PanoramaEvent.MSG_TYPE_TAKE_PICTURE_RSP.class);

        System.out.println(read);

    }

}