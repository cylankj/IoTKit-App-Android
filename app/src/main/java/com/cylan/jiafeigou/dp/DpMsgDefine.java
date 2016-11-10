package com.cylan.jiafeigou.dp;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * Created by cylan-hunt on 16-11-9.
 */

public class DpMsgDefine {

    @Message
    public static final class MsgNet {
        @Index(0)
        public int net;
        @Index(1)
        public String ssid;

        @Override
        public String toString() {
            return "MsgNet{" +
                    "net=" + net +
                    ", ssid='" + ssid + '\'' +
                    '}';
        }
    }
}
