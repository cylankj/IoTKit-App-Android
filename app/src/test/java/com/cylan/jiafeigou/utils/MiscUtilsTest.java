package com.cylan.jiafeigou.utils;

import com.cylan.entity.jniCall.JFGDPMsg;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by holy on 2017/3/22.
 */
public class MiscUtilsTest {
    @Test
    public void getCamDateVersionList() throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        List<JFGDPMsg> list = MiscUtils.getCamDateVersionList(calendar.getTimeInMillis());
        for (int i = 0; i < 45; i++)
            System.out.println("" + format.format(new Date(list.get(i).version)));
    }

    @Test
    public void testWechat() {
    }

    @Test
    public void getHostAddressTest() throws UnknownHostException, UnknownHostException {
        // 输出IPv4地址
        InetAddress ipv4Address1 = InetAddress.getByName("1.2.3.4");
        System.out.println("ipv4Address1:" + ipv4Address1.getHostAddress());
        //ipv4Address1:1.2.3.4
        InetAddress ipv4Address0 = InetAddress.getByName("www.qq.com");
        System.out.println("ipv4Address0:" + ipv4Address0.getHostAddress());

        //ipv4Address1:1.2.3.4
        InetAddress ipv4Address2 = InetAddress.getByName("www.ibm.com");
        System.out.println("ipv4Address2:" + ipv4Address2.getHostAddress());
        //ipv4Address2:129.42.60.216
        InetAddress ipv6Address1 = InetAddress.getByName("abcd:123::22ff");
        System.out.println("ipv6Address1:" + ipv6Address1.getHostAddress());
        //ipv6Address1:abcd:123:0:0:0:0:0:22ff
        InetAddress ipv6Address2 = InetAddress.getByName("www.neu6.edu.cn");
        System.out.println("ipv6Address2:" + ipv6Address2.getHostAddress());
    }
}