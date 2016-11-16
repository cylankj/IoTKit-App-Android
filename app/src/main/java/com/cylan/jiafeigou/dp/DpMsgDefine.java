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

    @Message
    public static final class MsgTimeZone {
        @Index(0)
        public String timezone;
        @Index(1)
        public int offset;

        @Override
        public String toString() {
            return "MsgTimeZone{" +
                    "timezone='" + timezone + '\'' +
                    ", offset=" + offset +
                    '}';
        }
    }

    @Message
    public static final class Bind {
        @Index(0)
        public boolean isBind;
        @Index(1)
        public String account;
        @Index(2)
        public String oldAccount;

        @Override
        public String toString() {
            return "Bind{" +
                    "isBind=" + isBind +
                    ", accout='" + account + '\'' +
                    ", oldAccount='" + oldAccount + '\'' +
                    '}';
        }
    }

    @Message
    public static final class SdStatus {
        @Index(0)
        public long total;
        @Index(1)
        public long used;
        @Index(2)
        public int err;

        @Override
        public String toString() {
            return "SdStatus{" +
                    "total=" + total +
                    ", used=" + used +
                    ", err=" + err +
                    '}';
        }
    }
}
