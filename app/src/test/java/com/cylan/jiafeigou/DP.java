package com.cylan.jiafeigou;

import com.cylan.jiafeigou.base.module.DProperty;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

import org.junit.Test;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;

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

    @Test
    public void sample() {
        Observable.interval(1, TimeUnit.SECONDS).subscribe(s -> {
            RxBus.getCacheInstance().post("SSSSS" + s);
        },e->AppLogger.d(e.getMessage()));

        RxBus.getCacheInstance().toObservable(String.class)
                .cache(2)
                .buffer(2)
                .subscribe(s -> {
                    System.out.println(s);
                },e-> AppLogger.d(e.getMessage()));
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //   LocalUdpMsg{ip='192.168.103.166', port=10008, data=[-109, -83, 100, 111, 111, 114, 98, 101, 108, 108, 95, 114, 105, 110, 103, -84, 53, 48, 48, 48, 48, 48, 48, 48, 49, 50, 57, 56, -96]}
    }

    @Message
    public static class bellRing {
        @Index(0)
        public String cmd;
        @Index(1)
        public String cid;
        @Index(2)
        public String mac;
    }

    @Test
    public void Bell() {
        byte[] bytes = new byte[]{-109, -83, 100, 111, 111, 114, 98, 101, 108, 108, 95, 114, 105, 110, 103, -84, 53, 48, 48, 48, 48, 48, 48, 48, 49, 50, 57, 56, -96};

        try {
            bellRing bellRing = DpUtils.unpackData(bytes, bellRing.class);
            System.out.println(new Gson().toJson(bellRing));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void sss() {
        Device device = new Device();

        Field[] fields = device.getClass().getFields();
        for (Field field : fields) {
            DProperty annotation = field.getAnnotation(DProperty.class);
            if (annotation != null)
                System.out.println(annotation.type());
        }
    }

}
