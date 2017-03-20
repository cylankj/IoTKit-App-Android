package com.cylan.jiafeigou;

import com.cylan.jiafeigou.dp.DpUtils;
import com.google.gson.Gson;

import org.junit.Test;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by yzd on 17-1-11.
 */
public class DP {
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

    @Message
    static class T {
        @Index(0)
        public int ret;//       错误码
        @Index(1)
        public int secends;//   视频录制的时长,单位秒
        @Index(2)
        public int videoType;// 特征值定义：videoTypeShort =1 8s短视频；videoTypeLong =2长视频；
    }

    @Test
    public void test() throws IOException {
//        -110,0,12
        byte[] bytes = new byte[]{-110, -1, -50, 0, 57, 21, -80};
        T t = DpUtils.unpackData(bytes, T.class);
        System.out.println(new Gson().toJson(t));
    }
}
