package com.cylan.jiafeigou.dp;

import com.google.gson.Gson;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.util.List;

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
    public static final class BindLog {
        @Index(0)
        public boolean isBind;
        @Index(1)
        public String account;
        @Index(2)
        public String oldAccount;

        @Override
        public String toString() {
            return "BindLog{" +
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

    @Message
    public static final class AlarmInfo {
        @Index(0)
        public int timeStart;
        @Index(1)
        public int timeEnd;
        /**
         * 每周的星期*， 从低位到高位代表周一到周日。如0b00000001代表周一，0b01000000代表周日
         */
        @Index(2)
        public int duration;
    }

    @Message
    public static final class AlarmMsg {//505 报警消息
        @Index(0)
        public int time;
        @Index(1)
        public int isRecording;
        @Index(2)
        public int fileIndex;
        @Index(3)
        public int type;
    }

    @Message//504
    public static final class NotificationInfo {
        @Index(0)
        public int notificatoin;
        @Index(1)
        public int duration;
    }

    @Message
    public static final class TimeLapse {
        @Index(0)
        public int timeStart;
        @Index(1)
        public int timePeriod;
        @Index(2)
        public int timeDuration;
        @Index(3)
        public int status;
    }

    @Message
    public static final class CamCoord {
        @Index(0)
        public int x;
        @Index(1)
        public int y;
        @Index(2)
        public int r;
    }

    @Message
    public static final class BellCallState {

        @Index(0)
        public int state;

        @Index(1)
        public int time;

        @Index(2)
        public int duration;

        @Index(3)
        public int type;
    }

    public static class BaseDpMsg {
        public int msgId;
        public long version;
        public Object o;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BaseDpMsg dpMsg = (BaseDpMsg) o;

            return msgId == dpMsg.msgId;

        }

        @Override
        public int hashCode() {
            return msgId;
        }

        @Override
        public String toString() {
            return "BaseDpMsg{" +
                    "msgId=" + msgId +
                    ", version=" + version +
                    ", o=" + new Gson().toJson(o) +
                    '}';
        }
    }

    public static class BaseDpDevice {
        public int pid;
        public String uuid;
        public String sn;
        public String alias;
        public String shareAccount;

        @Override
        public String toString() {
            return "BaseDpDevice{" +
                    "pid=" + pid +
                    ", uuid='" + uuid + '\'' +
                    ", sn='" + sn + '\'' +
                    ", alias='" + alias + '\'' +
                    ", shareAccount='" + shareAccount + '\'' +
                    '}';
        }
    }

    public static class DpWrap {
        public BaseDpDevice baseDpDevice;
        public List<BaseDpMsg> baseDpMsgList;

        @Override
        public String toString() {
            return "DpWrap{" +
                    "baseDpDevice=" + baseDpDevice +
                    ", baseDpMsgList=" + baseDpMsgList +
                    '}';
        }
    }
}
