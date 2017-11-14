package com.cylan.jiafeigou.dp;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;
import org.msgpack.annotation.Optional;
import org.msgpack.type.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by cylan-hunt on 16-11-9.
 */

/**
 * @Deprecated msgPack 将升级到0.8 版本,不以反射的方式来解析了,将会通过静态方法的方式
 */
public class DpMsgDefine {
    @org.msgpack.annotation.Message
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(value = {"standby", "alarmEnable", "led", "autoRecord"})
    public static final class DPStandby extends BaseDataPoint {
        @Index(0)
        public boolean standby;
        @Index(1)
        public boolean alarmEnable;
        @Index(2)
        public boolean led;
        @Index(3)
        public int autoRecord;

        public static DPStandby empty() {
            return new DPStandby();
        }

        public DPStandby() {
            this.msgId = DpMsgMap.ID_508_CAMERA_STANDBY_FLAG;
        }

        @Override
        public String toString() {
            return "DPStandby{" +
                    "standby=" + standby +
                    ", alarmEnable=" + alarmEnable +
                    ", led=" + led +
                    ", autoRecord=" + autoRecord +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeByte(this.standby ? (byte) 1 : (byte) 0);
            dest.writeByte(this.alarmEnable ? (byte) 1 : (byte) 0);
            dest.writeByte(this.led ? (byte) 1 : (byte) 0);
            dest.writeInt(this.autoRecord);
        }

        protected DPStandby(Parcel in) {
            super(in);
            this.standby = in.readByte() != 0;
            this.alarmEnable = in.readByte() != 0;
            this.led = in.readByte() != 0;
            this.autoRecord = in.readInt();
        }

        public static final Creator<DPStandby> CREATOR = new Creator<DPStandby>() {
            @Override
            public DPStandby createFromParcel(Parcel source) {
                return new DPStandby(source);
            }

            @Override
            public DPStandby[] newArray(int size) {
                return new DPStandby[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(value = {"net", "ssid"})
    public static final class DPNet extends BaseDataPoint {
        /**
         * |NET_CONNECT | -1 | #绑定后的连接中 |
         * |NET_OFFLINE |  0 | #不在线 |
         * |NET_WIFI    |  1 | #WIFI网络 |
         * |NET_2G      |  2 | #2G网络 |
         * |NET_3G      |  3 | #3G网络 |
         * |NET_4G      |  4 | #4G网络  |
         * |NET_5G      |  5 | #5G网络  |
         */
        @Index(0)
        public int net = 0;
        @Index(1)
        public String ssid;


        @Override
        public String toString() {
            return "DPNet{" +
                    "net=" + net +
                    ", ssid='" + ssid + '\'' +
                    '}';
        }

        public static String getNormalString(DPNet net) {
            String result = null;
            switch (net.net) {
                case -1:
                    result = "绑定后的连接中";
                    break;
                case 0:
                    result = "设备离线中";
                    break;
                case 1:
                    result = TextUtils.isEmpty(net.ssid) ? "WiFi未开启" : net.ssid;
                    break;
                case 2:
                    result = "2G网络";
                    break;
                case 3:
                    result = "3G网络";
                    break;
                case 4:
                    result = "4G网络";
                    break;
                case 5:
                    result = "5G网络";
                    break;
            }
            return result;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.net);
            dest.writeString(this.ssid);
        }

        public DPNet() {
        }

        public DPNet(int net, String ssid) {
            this.net = net;
            this.ssid = ssid;
        }

        protected DPNet(Parcel in) {
            super(in);
            this.net = in.readInt();
            this.ssid = in.readString();
        }

        public static final Creator<DPNet> CREATOR = new Creator<DPNet>() {
            @Override
            public DPNet createFromParcel(Parcel source) {
                return new DPNet(source);
            }

            @Override
            public DPNet[] newArray(int size) {
                return new DPNet[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(value = {"timezone", "offset"})
    public static final class DPTimeZone extends BaseDataPoint {
        @Index(0)
        public String timezone;
        @Index(1)
        public int offset;

        @Override
        public String toString() {
            return "DPTimeZone{" +
                    "timezone='" + timezone + '\'' +
                    ", offset=" + offset +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(this.timezone);
            dest.writeInt(this.offset);
        }

        public DPTimeZone() {
            TimeZone timeZone = TimeZone.getDefault();
            this.timezone = timeZone.getID();
        }

        protected DPTimeZone(Parcel in) {
            super(in);
            this.timezone = in.readString();
            this.offset = in.readInt();
        }

        public static final Creator<DPTimeZone> CREATOR = new Creator<DPTimeZone>() {
            @Override
            public DPTimeZone createFromParcel(Parcel source) {
                return new DPTimeZone(source);
            }

            @Override
            public DPTimeZone[] newArray(int size) {
                return new DPTimeZone[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(value = {"isBind", "account", "oldAccount"})
    public static final class DPBindLog extends BaseDataPoint {

        @Index(0)
        public boolean isBind;
        @Index(1)
        public String account;
        @Index(2)
        public String oldAccount;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeByte(this.isBind ? (byte) 1 : (byte) 0);
            dest.writeString(this.account);
            dest.writeString(this.oldAccount);
        }

        public DPBindLog() {
        }

        protected DPBindLog(Parcel in) {
            super(in);
            this.isBind = in.readByte() != 0;
            this.account = in.readString();
            this.oldAccount = in.readString();
        }

        public static final Creator<DPBindLog> CREATOR = new Creator<DPBindLog>() {
            @Override
            public DPBindLog createFromParcel(Parcel source) {
                return new DPBindLog(source);
            }

            @Override
            public DPBindLog[] newArray(int size) {
                return new DPBindLog[size];
            }
        };

        @Override
        public String toString() {
            return "DPBindLog{" +
                    "isBind=" + isBind +
                    ", account='" + account + '\'' +
                    ", oldAccount='" + oldAccount + '\'' +
                    '}';
        }
    }

    //系统消息使用
    @org.msgpack.annotation.Message
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(value = {"hasSdcard", "errCode"})
    public static final class DPSdcardSummary extends BaseDataPoint implements Parcelable {
        @Index(0)
        public boolean hasSdcard;
        @Index(1)
        public int errCode;

        public DPSdcardSummary() {
        }

        protected DPSdcardSummary(Parcel in) {
            super(in);
            hasSdcard = in.readByte() != 0;
            errCode = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeByte((byte) (hasSdcard ? 1 : 0));
            dest.writeInt(errCode);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<DPSdcardSummary> CREATOR = new Creator<DPSdcardSummary>() {
            @Override
            public DPSdcardSummary createFromParcel(Parcel in) {
                return new DPSdcardSummary(in);
            }

            @Override
            public DPSdcardSummary[] newArray(int size) {
                return new DPSdcardSummary[size];
            }
        };

        @Override
        public String toString() {
            return "DPSdcardSummary{" +
                    "hasSdcard=" + hasSdcard +
                    ", errCode=" + errCode +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            DPSdcardSummary summary = (DPSdcardSummary) o;

            if (hasSdcard != summary.hasSdcard) {
                return false;
            }
            return errCode == summary.errCode;

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (hasSdcard ? 1 : 0);
            result = 31 * result + errCode;
            return result;
        }
    }

    @org.msgpack.annotation.Message
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(value = {"total", "used", "err", "hasSdcard"})
    public static final class DPSdStatus extends BaseDataPoint implements Parcelable {
        @Index(0)
        public long total;
        @Index(1)
        public long used;
        @Index(2)
        public int err = -1;
        @Index(3)
        public boolean hasSdcard;

        public DPSdStatus() {
        }

        @Override
        public String toString() {
            return "DPSdStatus{" +
                    "total=" + total +
                    ", used=" + used +
                    ", err=" + err +
                    ", hasSdcard=" + hasSdcard +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeLong(this.total);
            dest.writeLong(this.used);
            dest.writeInt(this.err);
            dest.writeInt(this.hasSdcard ? 1 : 0);
        }

        protected DPSdStatus(Parcel in) {
            super(in);
            this.total = in.readLong();
            this.used = in.readLong();
            this.err = in.readInt();
            this.hasSdcard = in.readInt() == 1;
        }

        public static final Creator<DPSdStatus> CREATOR = new Creator<DPSdStatus>() {
            @Override
            public DPSdStatus createFromParcel(Parcel source) {
                return new DPSdStatus(source);
            }

            @Override
            public DPSdStatus[] newArray(int size) {
                return new DPSdStatus[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public static final class DPSdStatusInt extends BaseDataPoint implements Parcelable {
        @Index(0)
        public long total;
        @Index(1)
        public long used;
        @Index(2)
        public int err = -1;
        @Index(3)
        public int hasSdcard;

        public DPSdStatusInt() {
        }

        @Override
        public String toString() {
            return "DPSdStatus{" +
                    "total=" + total +
                    ", used=" + used +
                    ", err=" + err +
                    ", hasSdcard=" + hasSdcard +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeLong(this.total);
            dest.writeLong(this.used);
            dest.writeInt(this.err);
            dest.writeInt(this.hasSdcard);
        }

        protected DPSdStatusInt(Parcel in) {
            super(in);
            this.total = in.readLong();
            this.used = in.readLong();
            this.err = in.readInt();
            this.hasSdcard = in.readInt();
        }

        public static final Creator<DPSdStatusInt> CREATOR = new Creator<DPSdStatusInt>() {
            @Override
            public DPSdStatusInt createFromParcel(Parcel source) {
                return new DPSdStatusInt(source);
            }

            @Override
            public DPSdStatusInt[] newArray(int size) {
                return new DPSdStatusInt[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(value = {"timeStart", "timeEnd"})
    public static final class DPAlarmInfo extends BaseDataPoint implements Parcelable {
        @Index(0)
        public int timeStart;
        @Index(1)
        public int timeEnd;
        /**
         * 每周的星期*， 从低位到高位代表周一到周日。如0b00000001代表周一，0b01000000代表周日
         */
        @Index(2)
        public int day;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.timeStart);
            dest.writeInt(this.timeEnd);
            dest.writeInt(this.day);
        }

        public DPAlarmInfo() {
            timeStart = 0;
            timeEnd = 5947;
            day = 127;//默认开启
        }

        protected DPAlarmInfo(Parcel in) {
            super(in);
            this.timeStart = in.readInt();
            this.timeEnd = in.readInt();
            this.day = in.readInt();
        }

        public static final Creator<DPAlarmInfo> CREATOR = new Creator<DPAlarmInfo>() {
            @Override
            public DPAlarmInfo createFromParcel(Parcel source) {
                return new DPAlarmInfo(source);
            }

            @Override
            public DPAlarmInfo[] newArray(int size) {
                return new DPAlarmInfo[size];
            }
        };

        @Override
        public String toString() {
            return "DPAlarmInfo{" +
                    "timeStart=" + timeStart +
                    ", timeEnd=" + timeEnd +
                    ", duration=" + day +
                    '}';
        }
    }

    @org.msgpack.annotation.Message
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(value = {"time", "isRecording", "fileIndex", "ossType", "tly", "objects"})
    public static final class DPAlarm extends BaseDataPoint implements Parcelable {//505 报警消息
        @Index(0)
        public int time;
        @Index(1)
        public int isRecording;
        @Index(2)
        public int fileIndex;
        @Index(3)
        public int ossType;
        @Index(4)
        public String tly;//全景设备陀螺仪。'0'俯视, '1' 平视。
        @Index(5)
        @Optional
        public int[] objects;
        @Optional
        @Index(6)
        public int humanNum = -1;
        @Optional
        @Index(7)
        public String[] face_id;

        @Ignore
        public static DPAlarm empty = new DPAlarm();

        public DPAlarm() {
        }

        @Override
        public String toString() {
            return "DPAlarm{" +
                    "time=" + time +
                    ", isRecording=" + isRecording +
                    ", fileIndex=" + fileIndex +
                    ", type=" + ossType +
                    ", tly='" + tly + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            DPAlarm dpAlarm = (DPAlarm) o;

            if (time != dpAlarm.time) {
                return false;
            }
            if (isRecording != dpAlarm.isRecording) {
                return false;
            }
            if (fileIndex != dpAlarm.fileIndex) {
                return false;
            }
            if (ossType != dpAlarm.ossType) {
                return false;
            }
            return tly != null ? tly.equals(dpAlarm.tly) : dpAlarm.tly == null;

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + time;
            result = 31 * result + isRecording;
            result = 31 * result + fileIndex;
            result = 31 * result + ossType;
            result = 31 * result + (tly != null ? tly.hashCode() : 0);
            return result;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.time);
            dest.writeInt(this.isRecording);
            dest.writeInt(this.fileIndex);
            dest.writeInt(this.ossType);
            dest.writeString(this.tly);
            dest.writeIntArray(this.objects);
            dest.writeInt(this.humanNum);
            dest.writeStringArray(this.face_id);
        }

        protected DPAlarm(Parcel in) {
            super(in);
            this.time = in.readInt();
            this.isRecording = in.readInt();
            this.fileIndex = in.readInt();
            this.ossType = in.readInt();
            this.tly = in.readString();
            this.objects = in.createIntArray();
            this.humanNum = in.readInt();
            this.face_id = in.createStringArray();
        }

        public static final Creator<DPAlarm> CREATOR = new Creator<DPAlarm>() {
            @Override
            public DPAlarm createFromParcel(Parcel source) {
                return new DPAlarm(source);
            }

            @Override
            public DPAlarm[] newArray(int size) {
                return new DPAlarm[size];
            }
        };
    }

    @org.msgpack.annotation.Message//504
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(value = {"notification", "duration"})
    public static final class DPNotificationInfo extends BaseDataPoint implements Parcelable {
        @Index(0)
        public int notification;
        @Index(1)
        public int duration;

        @Ignore
        public static DPNotificationInfo empty = new DPNotificationInfo();

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            DPNotificationInfo that = (DPNotificationInfo) o;

            if (notification != that.notification) {
                return false;
            }
            return duration == that.duration;

        }

        @Override
        public int hashCode() {
            int result = notification;
            result = 31 * result + duration;
            return result;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.notification);
            dest.writeInt(this.duration);
        }

        @Override
        public String toString() {
            return "DPNotificationInfo{" +
                    "notification=" + notification +
                    ", duration=" + duration +
                    '}';
        }

        public DPNotificationInfo() {
        }

        protected DPNotificationInfo(Parcel in) {
            super(in);
            this.notification = in.readInt();
            this.duration = in.readInt();
        }

        public static final Creator<DPNotificationInfo> CREATOR = new Creator<DPNotificationInfo>() {
            @Override
            public DPNotificationInfo createFromParcel(Parcel source) {
                return new DPNotificationInfo(source);
            }

            @Override
            public DPNotificationInfo[] newArray(int size) {
                return new DPNotificationInfo[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(value = {"timeStart", "timePeriod", "timeDuration", "status"})
    public static final class DPTimeLapse extends BaseDataPoint implements Parcelable {
        @Index(0)
        public int timeStart;
        @Index(1)
        public int timePeriod;
        @Index(2)
        public int timeDuration;
        @Index(3)
        public int status;

        @Ignore
        public static DPTimeLapse empty = new DPTimeLapse();

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.timeStart);
            dest.writeInt(this.timePeriod);
            dest.writeInt(this.timeDuration);
            dest.writeInt(this.status);
        }

        public DPTimeLapse() {
        }

        protected DPTimeLapse(Parcel in) {
            super(in);
            this.timeStart = in.readInt();
            this.timePeriod = in.readInt();
            this.timeDuration = in.readInt();
            this.status = in.readInt();
        }

        public static final Creator<DPTimeLapse> CREATOR = new Creator<DPTimeLapse>() {
            @Override
            public DPTimeLapse createFromParcel(Parcel source) {
                return new DPTimeLapse(source);
            }

            @Override
            public DPTimeLapse[] newArray(int size) {
                return new DPTimeLapse[size];
            }
        };

        @Override
        public String toString() {
            return "DPTimeLapse{" +
                    "timeStart=" + timeStart +
                    ", timePeriod=" + timePeriod +
                    ", timeDuration=" + timeDuration +
                    ", status=" + status +
                    '}';
        }
    }

    @org.msgpack.annotation.Message
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(value = {"x", "y", "r"})
    public static final class DPCamCoord extends BaseDataPoint implements Parcelable {
        @Index(0)
        public int x;
        @Index(1)
        public int y;
        @Index(2)
        public int r;

        @Ignore
        public static DPCamCoord empty = new DPCamCoord();

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.x);
            dest.writeInt(this.y);
            dest.writeInt(this.r);
        }

        public DPCamCoord() {
        }

        protected DPCamCoord(Parcel in) {
            super(in);
            this.x = in.readInt();
            this.y = in.readInt();
            this.r = in.readInt();
        }

        public static final Creator<DPCamCoord> CREATOR = new Creator<DPCamCoord>() {
            @Override
            public DPCamCoord createFromParcel(Parcel source) {
                return new DPCamCoord(source);
            }

            @Override
            public DPCamCoord[] newArray(int size) {
                return new DPCamCoord[size];
            }
        };

        @Override
        public String toString() {
            return "DPCamCoord{" +
                    "x=" + x +
                    ", y=" + y +
                    ", r=" + r +
                    '}';
        }
    }

    @org.msgpack.annotation.Message
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(value = {"isOK", "time", "duration", "type", "isRecording", "fileIndex"})
    public static final class DPBellCallRecord extends BaseDataPoint implements Parcelable {

        @Index(0)
        public int isOK;

        @Index(1)
        public int time;

        @Index(2)
        public int duration;

        @Index(3)
        public int type;
        @Index(4)
        @Optional
        public int isRecording = -1;//默认为- 1吧,//0为假:1为真
        @Index(5)
        @Optional
        public int fileIndex = -1;//默认为-1 ,因为听说新版本默认为零

        @Ignore
        public static DPBellCallRecord empty = new DPBellCallRecord();

        public DPBellCallRecord() {
        }

        public DPAlarm converToAlarm() {
            DPAlarm dpAlarm = new DPAlarm();
            dpAlarm.time = time;
            dpAlarm.version = version;
            dpAlarm.msgId = 505;
            dpAlarm.ossType = type;
            dpAlarm.tly = "";
            dpAlarm.isRecording = isRecording;
            dpAlarm.fileIndex = fileIndex;
            return dpAlarm;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.isOK);
            dest.writeInt(this.time);
            dest.writeInt(this.duration);
            dest.writeInt(this.type);
        }

        protected DPBellCallRecord(Parcel in) {
            super(in);
            this.isOK = in.readInt();
            this.time = in.readInt();
            this.duration = in.readInt();
            this.type = in.readInt();
        }

        public static final Creator<DPBellCallRecord> CREATOR = new Creator<DPBellCallRecord>() {
            @Override
            public DPBellCallRecord createFromParcel(Parcel source) {
                return new DPBellCallRecord(source);
            }

            @Override
            public DPBellCallRecord[] newArray(int size) {
                return new DPBellCallRecord[size];
            }
        };

        @Override
        public String toString() {
            return "DPBellCallRecord{" +
                    "isOK=" + isOK +
                    ", time=" + time +
                    ", duration=" + duration +
                    ", type=" + type +
                    ", isRecording=" + isRecording +
                    ", fileIndex=" + fileIndex +
                    '}';
        }
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(value = {"value"})
    public static final class DPPrimary<T> extends BaseDataPoint {
        @Index(0)
        public T value;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeValue(value);
        }

        public DPPrimary() {
        }

        public DPPrimary(T value) {
            this.value = value;
        }

        public DPPrimary(T value, int msgId) {
            this.value = value;
            this.msgId = msgId;
        }

        public DPPrimary(T value, int msgId, long version) {
            this.value = value;
            this.msgId = msgId;
            this.version = version;
        }

        @Override
        public byte[] toBytes() {
            return DpUtils.pack(value);
        }

        protected DPPrimary(Parcel in) {
            super(in);
            this.value = (T) in.readValue(Object.class.getClassLoader());
        }

        public static final Creator<DPPrimary> CREATOR = new Creator<DPPrimary>() {
            @Override
            public DPPrimary createFromParcel(Parcel source) {
                return new DPPrimary(source);
            }

            @Override
            public DPPrimary[] newArray(int size) {
                return new DPPrimary[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(value = {"cid", "time", "msgType", "fileName", "place"})
    public static final class DPWonderItem extends BaseDataPoint implements Parcelable {

        public static final int TYPE_PIC = 0;
        public static final int TYPE_VIDEO = 1;
        public static final int TYPE_LOAD = 2;
        public static final int TYPE_NO_MORE = 3;
        private static DPWonderItem guideBean;
        private static DPWonderItem nomore;

        @Index(0)
        public String cid;
        @Index(1)
        public int time;
        @Index(2)
        public int msgType;
        @Index(3)
        public int regionType;
        @Index(4)
        public String fileName;
        @Index(5)
        public String place;

        @Override
        public int describeContents() {
            return 0;
        }


        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.cid);
            dest.writeInt(this.time);
            dest.writeInt(this.msgType);
            dest.writeInt(this.regionType);
            dest.writeString(this.fileName);
            dest.writeString(this.place);
        }

        public DPWonderItem() {
            msgId = 602;
        }

        protected DPWonderItem(Parcel in) {
            this.cid = in.readString();
            this.time = in.readInt();
            this.msgType = in.readInt();
            this.regionType = in.readInt();
            this.fileName = in.readString();
            this.place = in.readString();
        }

        public static final Creator<DPWonderItem> CREATOR = new Creator<DPWonderItem>() {
            @Override
            public DPWonderItem createFromParcel(Parcel source) {
                return new DPWonderItem(source);
            }

            @Override
            public DPWonderItem[] newArray(int size) {
                return new DPWonderItem[size];
            }
        };

        private static DPWonderItem loadBean = new DPWonderItem();

        public static DPWonderItem getEmptyLoadTypeBean() {
            if (loadBean == null) {
                loadBean = new DPWonderItem();
            }
            loadBean.msgType = TYPE_LOAD;
            return loadBean;
        }

        public static DPWonderItem getNoMoreTypeBean() {
            if (nomore == null) {
                nomore = new DPWonderItem();
            }
            nomore.msgType = TYPE_NO_MORE;
            return nomore;
        }

        public static DPWonderItem getGuideBean() {
            if (guideBean == null) {
                guideBean = new DPWonderItem();
                guideBean.msgType = TYPE_VIDEO;
                guideBean.fileName = "http://yf.cylan.com.cn:82/Garfield/1045020208160b9706425470.mp4";
                guideBean.cid = "www.cylan.com";
            }
            guideBean.time = (int) (System.currentTimeMillis() / 1000);
            return guideBean;
        }

        @Override
        public String toString() {
            return "DPWonderItem{" +
                    "cid='" + cid + '\'' +
                    ", time=" + time +
                    ", msgType=" + msgType +
                    ", regionType=" + regionType +
                    ", fileName='" + fileName + '\'' +
                    ", place='" + place + '\'' +
                    '}';
        }
    }

    @org.msgpack.annotation.Message
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(value = {"cid", "isDone", "account", "sn", "pid"})
    public static final class DPMineMesg {
        @Index(0)
        public String cid;
        @Index(1)
        public boolean isDone;
        @Optional
        @Index(2)
        public String account;
        @Optional
        @Index(3)
        public String sn;
        @Optional
        @Index(4)
        public int pid;

        @Override
        public String toString() {
            return "DPMineMesg{" +
                    "cid='" + cid + '\'' +
                    ", isDone=" + isDone +
                    ", account='" + account + '\'' +
                    ", sn='" + sn + '\'' +
                    ", pid=" + pid +
                    '}';
        }
    }

    @org.msgpack.annotation.Message
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(value = {"title", "content"})
    public static final class DPSystemMesg {
        @Index(0)
        public String title;
        @Index(1)
        public String content;

        @Override
        public String toString() {
            return "DPSystemMesg{" +
                    "title='" + title + '\'' +
                    ", content='" + content + '\'' +
                    '}';
        }
    }

    @org.msgpack.annotation.Message
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(value = {"id", "time", "count"})
    public static final class DPUnreadCount {
        @Index(0)
        public int id;
        @Index(1)
        public long time;
        @Index(2)
        public int count;

        public DPUnreadCount() {

        }

        public DPUnreadCount(int id, long time, int count) {
            this.id = id;
            this.time = time;
            this.count = count;
        }

    }


    public static class V3Header {

    }

    /**
     * 历史录像日历列表请求
     */
    @org.msgpack.annotation.Message
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(value = {"beginTime", "limit", "asc"})
    public static final class V3DateListReq {
        @Index(0)
        public int beginTime;
        @Index(1)
        public int limit;
        @Index(2)
        public boolean asc;

        public V3DateListReq() {
        }

        @Override
        public String toString() {
            return "V3DateListReq{" +
                    "beginTime=" + beginTime +
                    ", limit=" + limit +
                    ", asc=" + asc +
                    '}';
        }
    }

    @org.msgpack.annotation.Message
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(value = {"cid", "time", "msgType", "regionType", "fileName", "desc", "url"})
    public static class DPShareItem extends BaseDataPoint {
        @Index(0)
        public String cid;
        @Index(1)
        public int time;
        @Index(2)
        public int msgType;
        @Index(3)
        public int regionType;
        @Index(4)
        public String fileName;
        @Index(5)
        public String desc;
        @Index(6)
        public String url;

        public DPWonderItem toWonderItem() {
            DPWonderItem wonderItem = new DPWonderItem();
            wonderItem.cid = cid;
            wonderItem.time = time;
            wonderItem.msgType = msgType;
            wonderItem.regionType = regionType;
            wonderItem.fileName = fileName;
            wonderItem.place = desc;
            return wonderItem;
        }

        public DPShareItem() {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(this.cid);
            dest.writeInt(this.time);
            dest.writeInt(this.msgType);
            dest.writeInt(this.regionType);
            dest.writeString(this.fileName);
            dest.writeString(this.desc);
            dest.writeString(this.url);
        }

        protected DPShareItem(Parcel in) {
            super(in);
            this.cid = in.readString();
            this.time = in.readInt();
            this.msgType = in.readInt();
            this.regionType = in.readInt();
            this.fileName = in.readString();
            this.desc = in.readString();
            this.url = in.readString();
        }

        public static final Creator<DPShareItem> CREATOR = new Creator<DPShareItem>() {
            @Override
            public DPShareItem createFromParcel(Parcel source) {
                return new DPShareItem(source);
            }

            @Override
            public DPShareItem[] newArray(int size) {
                return new DPShareItem[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(value = {"upgrade"})
    public static class DPBaseUpgradeStatus extends BaseDataPoint {
        @Index(0)
        public int upgrade;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.upgrade);
        }

        public DPBaseUpgradeStatus() {
        }

        protected DPBaseUpgradeStatus(Parcel in) {
            super(in);
            this.upgrade = in.readInt();
        }

        public static final Creator<DPBaseUpgradeStatus> CREATOR = new Creator<DPBaseUpgradeStatus>() {
            @Override
            public DPBaseUpgradeStatus createFromParcel(Parcel source) {
                return new DPBaseUpgradeStatus(source);
            }

            @Override
            public DPBaseUpgradeStatus[] newArray(int size) {
                return new DPBaseUpgradeStatus[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    @JsonFormat(shape = JsonFormat.Shape.ARRAY, with = {JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY})
    @JsonPropertyOrder(value = {"recordEnable"})
    public static class DPAutoRecordWatcher extends BaseDataPoint {
        @Index(0)
        public boolean recordEnable;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeByte(this.recordEnable ? (byte) 1 : (byte) 0);
        }

        public DPAutoRecordWatcher() {
        }

        public DPAutoRecordWatcher(boolean recordEnable) {
            this.recordEnable = recordEnable;
        }

        protected DPAutoRecordWatcher(Parcel in) {
            super(in);
            this.recordEnable = in.readByte() != 0;
        }

        public static final Creator<DPAutoRecordWatcher> CREATOR = new Creator<DPAutoRecordWatcher>() {
            @Override
            public DPAutoRecordWatcher createFromParcel(Parcel source) {
                return new DPAutoRecordWatcher(source);
            }

            @Override
            public DPAutoRecordWatcher[] newArray(int size) {
                return new DPAutoRecordWatcher[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(value = {"x", "y", "r", "w", "h"})
    public static class DpCoordinate extends BaseDataPoint {
        @Index(0)
        public int x;
        @Index(1)
        public int y;
        @Index(2)
        public int r;
        @Index(3)
        public int w;
        @Index(4)
        public int h;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.x);
            dest.writeInt(this.y);
            dest.writeInt(this.r);
            dest.writeInt(this.w);
            dest.writeInt(this.h);
        }

        public DpCoordinate() {
        }

        protected DpCoordinate(Parcel in) {
            super(in);
            this.x = in.readInt();
            this.y = in.readInt();
            this.r = in.readInt();
            this.w = in.readInt();
            this.h = in.readInt();
        }

        public static final Creator<DpCoordinate> CREATOR = new Creator<DpCoordinate>() {
            @Override
            public DpCoordinate createFromParcel(Parcel source) {
                return new DpCoordinate(source);
            }

            @Override
            public DpCoordinate[] newArray(int size) {
                return new DpCoordinate[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(value = {"enable", "startTime", "endTime"})
    public static final class BellDeepSleep extends BaseDataPoint {
        @Index(0)
        public boolean enable;
        @Index(1)
        public int startTime;
        @Index(2)
        public int endTime;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeByte(this.enable ? (byte) 1 : (byte) 0);
            dest.writeInt(this.startTime);
            dest.writeInt(this.endTime);
        }

        public BellDeepSleep() {
        }

        protected BellDeepSleep(Parcel in) {
            super(in);
            this.enable = in.readByte() != 0;
            this.startTime = in.readInt();
            this.endTime = in.readInt();
        }

        public static final Creator<BellDeepSleep> CREATOR = new Creator<BellDeepSleep>() {
            @Override
            public BellDeepSleep createFromParcel(Parcel source) {
                return new BellDeepSleep(source);
            }

            @Override
            public BellDeepSleep[] newArray(int size) {
                return new BellDeepSleep[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    public static class DPCameraLiveRtmpCtrl extends BaseDataPoint {
        @Index(0)
        public String url;
        @Index(1)
        public int enable;
        @Index(2)
        public int liveType;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public String toString() {
            return "DPCameraLiveRtmpCtrl{" +
                    "url='" + url + '\'' +
                    ", enable=" + enable +
                    ", liveType=" + liveType +
                    '}';
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(this.url);
            dest.writeInt(this.enable);
        }

        public DPCameraLiveRtmpCtrl() {
        }

        public DPCameraLiveRtmpCtrl(String url, int enable, int liveType) {
            this.url = url;
            this.enable = enable;
            this.liveType = liveType;
        }

        protected DPCameraLiveRtmpCtrl(Parcel in) {
            super(in);
            this.url = in.readString();
            this.enable = in.readInt();
        }

        public static final Creator<DPCameraLiveRtmpCtrl> CREATOR = new Creator<DPCameraLiveRtmpCtrl>() {
            @Override
            public DPCameraLiveRtmpCtrl createFromParcel(Parcel source) {
                return new DPCameraLiveRtmpCtrl(source);
            }

            @Override
            public DPCameraLiveRtmpCtrl[] newArray(int size) {
                return new DPCameraLiveRtmpCtrl[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    public static class DPCameraLiveRtmpStatus extends BaseDataPoint {
        @Index(0)
        public int liveType;//直播类型：1 facebook; 2 youtube; 3 weibo; 4 rtmp
        @Index(1)
        public String url;//rtmp推流地址。示例：rtmp://a.rtmp.youtube.com/live2
        @Index(2)
        public int flag;//状态特征值： 1 准备直播； 2 直播中； 3 直播结束；
        @Index(3)
        public int timestamp;//开始直播的时间戳，其它情况置位0
        @Index(4)
        public int error;//错误特征值： 0 正确； 1 错误；

        @Override
        public String toString() {
            return "DPCameraLiveRtmpStatus{" +
                    "liveType=" + liveType +
                    ", url='" + url + '\'' +
                    ", flag=" + flag +
                    ", timestamp=" + timestamp +
                    ", error=" + error +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.liveType);
            dest.writeString(this.url);
            dest.writeInt(this.flag);
            dest.writeInt(this.timestamp);
            dest.writeInt(this.error);
        }

        public DPCameraLiveRtmpStatus() {
        }

        protected DPCameraLiveRtmpStatus(Parcel in) {
            super(in);
            this.liveType = in.readInt();
            this.url = in.readString();
            this.flag = in.readInt();
            this.timestamp = in.readInt();
            this.error = in.readInt();
        }

        public static final Creator<DPCameraLiveRtmpStatus> CREATOR = new Creator<DPCameraLiveRtmpStatus>() {
            @Override
            public DPCameraLiveRtmpStatus createFromParcel(Parcel source) {
                return new DPCameraLiveRtmpStatus(source);
            }

            @Override
            public DPCameraLiveRtmpStatus[] newArray(int size) {
                return new DPCameraLiveRtmpStatus[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    public static class DPAIService {
        @Index(0)
        public String vid;
        @Index(1)
        public String service_key;
        @Index(2)
        public String service_key_seceret;
    }

    @org.msgpack.annotation.Message
    public static class DPOssRegion {

        @Index(0)
        public String cid;

        @Index(1)
        public int regionType;
    }

    @org.msgpack.annotation.Message
    public static class DPOssService {
        @Index(0)
        public String vid;
        @Index(1)
        public String service_key;
        @Index(2)
        public String service_key_seceret;
    }

    /**
     * {
     * "ret": 0,
     * "msg": "ok",
     * "data": [
     * {
     * "face_id": "LeNvlvZcATymiN5soT8HA959sxRko0Oh2ljdGeST8TrjuUgj0VDgh3uHtzf4MQRkt9ZKMJsyVrr1Jy6scUBqbJJ0LbXDzGpzh3yVzk7dOM5ofG6tm6cyjYmqolHiNRJd",
     * "face_name": "face_1",
     * "coord": "[123, 321],[11, 22]",
     * "account": "test001",
     * "sn": "test1000001",
     * "person_id": 0,
     * "source_image_url": "http://xx.xx.xx/ss/aa/d.jpg",
     * "image_url": "",
     * "group_id": 4,
     * "service_type": 1
     * },
     * {
     * "face_id": "OCTFHIUgzfCOykiIaFOnFSew9083XTJu6yBTHhjSdGtwAELw9tASi9RXlPiF7qQd9mANFr64tS2MPAfaRrzs5GF5SQMacUBcYkYBjBdTIYXCHhu2jO3BFoHnRVDh03K9",
     * "face_name": "face_2",
     * "coord": "[113, 331],[121, 121]",
     * "account": "test001",
     * "sn": "test1000001",
     * "person_id": 0,
     * "source_image_url": "http://xx.xx.xx/ss/aa/d.jpg",
     * "image_url": "",
     * "group_id": 4,
     * "service_type": 1
     * }
     * ]
     * }
     */

    public static class ResponseHeader {
        public int ret;
        public String msg;
    }

    public static class GenericResponse extends ResponseHeader {
        public String data;
    }

    public static class FaceQueryResponse extends ResponseHeader {
        public List<FaceInformation> data;

        @Override
        public String toString() {
            return "FaceQueryResponse{" +
                    "ret=" + ret +
                    ", msg='" + msg + '\'' +
                    ", data=" + data +
                    '}';
        }
    }

//     "face_id": "201710271551094XL1rPiBrkyv3fluSb",
//             "face_name": "就地解决阿胶阿胶阿胶浆",
//             "coord": "[0,567],[593,512]",
//             "account": "18503060168",
//             "sn": "290100000003",
//             "person_id": "2017102715560094KNgILKKdHUY5LJ9Q",
//             "source_image_url": "http://jiafeigou-yf.oss-cn-hangzhou.aliyuncs.com/long/0001/18503060168/AI/290100000003/1524642063_0.jpg?OSSAccessKeyId=xjBdwD1du8lf2wMI\u0026Expires=1524642663\u0026Signature=I1hqJnCeRccXvs9mp9%2BxahMs60o%3D",
//             "image_url": "https://jiafeigou-yf.oss-cn-hangzhou.aliyuncs.com:443/7day/0001/18503060168/AI/290100000003/201710271551094XL1rPiBrkyv3fluSb.jpg?security-token=CAISrAR1q6Ft5B2yfSjIrbmMG9%2Fsq41tgrG8MEGFt0M6VPlEmrLz1zz2IHpPendgAu8ev%2Fo%2FmGpR6PsYlq0rE8ccHZFKnEvtrcY5yxioRqackT%2Fej9Vd%2BmDOewW6Dxr8w7WMAYHQR8%2FcffGAck3NkjQJr5LxaTSlWS7TU%2FiOkoU1VskLeQO6YDFaZrJRPRAwh8IGEnHTOP2xSKWA4AzqAVFvpxB3hE5m9K272bf80BfFi0DgweJnee6TbZGvdJtrJ4wtEYX3ju53f6TM0SFX9l1b%2BuAs1fQcoW%2Bf4InHUwQWoXfJOuHPoNp0N107NOpoGa9NovX9mvpl%2F%2F3d0N6nk00VZbkNC36DFMfiooTNE%2Fj7Mc0iJ%2FSpeSbP09mBO5j6tB5hcHxcblISIoJ5cicoUUR2DXO4Zaas4wLNeRzxCfrHgqM32JR4zlrv4J%2BbKR%2FVEu3AiX5AYsdkNxhvdXxd1Gf6IKgdaF4OIRE1BbecQ4hyYxdD7LLm%2BgbTWmo4likO%2BL%2BcK%2FrdofIYcp6tHMAEg4gcbZpLsmQtUhHmRfWqjUEbMzQ9Ge8RksulMJSkurie27fRM6yUCPEOtFhbfjfM6juDDnJVJCL8oZ9BIlOT4JbW17eescEiQgkv795SDA6CbJN8oRti5KT1t0zLqaihcBDxpjZjo4eIobMisxU0Iq2W5MaJpSPbsXCqZq00pdzaQmUHQ27sIScnnqDJ2SpW%2BUBdyTjqZkwFgFOKznYep2R9GoABXMDXGzsS1SQ6B8NPGUUDrzvJhRc7OSsjbOmpym8Sbzq4LjjDVIdDEZp21ObeMYud",
//             "service_type": 1,
//             "species": 1

    public static class FaceInformation implements Parcelable {


        public String face_id;
        public String face_name;
        public String coord;
        public String account;
        public String sn;
        public String person_id;
        public String source_image_url;
        public String image_url;
        public String service_type;
        public String species;

        @Override
        public String toString() {
            return "FaceInformation{" +
                    "face_id='" + face_id + '\'' +
                    ", face_name='" + face_name + '\'' +
                    ", coord='" + coord + '\'' +
                    ", account='" + account + '\'' +
                    ", sn='" + sn + '\'' +
                    ", person_id='" + person_id + '\'' +
                    ", source_image_url='" + source_image_url + '\'' +
                    ", image_url='" + image_url + '\'' +
                    ", species='" + species + '\'' +
                    ", service_type='" + service_type + '\'' +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.face_id);
            dest.writeString(this.face_name);
            dest.writeString(this.coord);
            dest.writeString(this.account);
            dest.writeString(this.sn);
            dest.writeString(this.person_id);
            dest.writeString(this.source_image_url);
            dest.writeString(this.image_url);
            dest.writeString(this.species);
            dest.writeString(this.service_type);
        }

        public FaceInformation() {
        }

        protected FaceInformation(Parcel in) {
            this.face_id = in.readString();
            this.face_name = in.readString();
            this.coord = in.readString();
            this.account = in.readString();
            this.sn = in.readString();
            this.person_id = in.readString();
            this.source_image_url = in.readString();
            this.image_url = in.readString();
            this.species = in.readString();
            this.service_type = in.readString();
        }

        public static final Parcelable.Creator<FaceInformation> CREATOR = new Parcelable.Creator<FaceInformation>() {
            @Override
            public FaceInformation createFromParcel(Parcel source) {
                return new FaceInformation(source);
            }

            @Override
            public FaceInformation[] newArray(int size) {
                return new FaceInformation[size];
            }
        };
    }

    //rsp=msgpack(total, [[object_type, person_id, person_name, last_time, [face_id1, face_id2, ...]], [object_type, person_id, person_name, last_time, [face_id1, face_id2, ...]], ...])
    @org.msgpack.annotation.Message
    public static class VisitorList implements Parcelable {

        @Index(0)
        public int total;
        @Index(1)
        public List<Visitor> dataList;

        @Override
        public String toString() {
            return "VisitorList{" +
                    "total=" + total +
                    ", dataList=" + dataList +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.total);
            dest.writeTypedList(this.dataList);
        }

        public VisitorList() {
        }

        protected VisitorList(Parcel in) {
            this.total = in.readInt();
            this.dataList = in.createTypedArrayList(Visitor.CREATOR);
        }

        public static final Parcelable.Creator<VisitorList> CREATOR = new Parcelable.Creator<VisitorList>() {
            @Override
            public VisitorList createFromParcel(Parcel source) {
                return new VisitorList(source);
            }

            @Override
            public VisitorList[] newArray(int size) {
                return new VisitorList[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    public static class VisitorDetail implements Parcelable {
        @Index(0)
        public String faceId;
        @Index(1)
        public String imgUrl;
        @Index(2)
        public int ossType;

        @Override
        public String toString() {
            return "VisitorDetail{" +
                    "faceId='" + faceId + '\'' +
                    ", imgUrl='" + imgUrl + '\'' +
                    ", ossType=" + ossType +
                    '}';
        }

        public VisitorDetail() {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.faceId);
            dest.writeString(this.imgUrl);
            dest.writeInt(this.ossType);
        }

        protected VisitorDetail(Parcel in) {
            this.faceId = in.readString();
            this.imgUrl = in.readString();
            this.ossType = in.readInt();
        }

        public static final Creator<VisitorDetail> CREATOR = new Creator<VisitorDetail>() {
            @Override
            public VisitorDetail createFromParcel(Parcel source) {
                return new VisitorDetail(source);
            }

            @Override
            public VisitorDetail[] newArray(int size) {
                return new VisitorDetail[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    public static class Visitor implements Parcelable {

        @Index(0)
        public int objectType;
        @Index(1)
        public String personId;
        @Index(2)
        public String personName;
        @Index(3)
        public long lastTime;
        @Index(4)
        public List<VisitorDetail> detailList;

        @Override
        public String toString() {
            return "Visitor{" +
                    "objectType=" + objectType +
                    ", personId='" + personId + '\'' +
                    ", personName='" + personName + '\'' +
                    ", lastTime=" + lastTime +
                    ", detailList=" + detailList +
                    '}';
        }

        public Visitor() {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.objectType);
            dest.writeString(this.personId);
            dest.writeString(this.personName);
            dest.writeLong(this.lastTime);
            dest.writeTypedList(this.detailList);
        }

        protected Visitor(Parcel in) {
            this.objectType = in.readInt();
            this.personId = in.readString();
            this.personName = in.readString();
            this.lastTime = in.readLong();
            this.detailList = in.createTypedArrayList(VisitorDetail.CREATOR);
        }

        public static final Creator<Visitor> CREATOR = new Creator<Visitor>() {
            @Override
            public Visitor createFromParcel(Parcel source) {
                return new Visitor(source);
            }

            @Override
            public Visitor[] newArray(int size) {
                return new Visitor[size];
            }
        };

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Visitor visitor = (Visitor) o;

            return lastTime == visitor.lastTime;
        }

        @Override
        public int hashCode() {
            return (int) (lastTime ^ (lastTime >>> 32));
        }
    }

    @org.msgpack.annotation.Message
    public static class ReqContent implements Parcelable {
        @Index(0)
        public String uuid;
        @Index(1)
        public long timeSec;

        public ReqContent(String uuid, long timeSec) {
            this.uuid = uuid;
            this.timeSec = timeSec;
        }

        @Override
        public String toString() {
            return "ReqContent{" +
                    "uuid='" + uuid + '\'' +
                    ", timeSec=" + timeSec +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.uuid);
            dest.writeLong(this.timeSec);
        }

        public ReqContent() {
        }

        protected ReqContent(Parcel in) {
            this.uuid = in.readString();
            this.timeSec = in.readLong();
        }

        public static final Parcelable.Creator<ReqContent> CREATOR = new Parcelable.Creator<ReqContent>() {
            @Override
            public ReqContent createFromParcel(Parcel source) {
                return new ReqContent(source);
            }

            @Override
            public ReqContent[] newArray(int size) {
                return new ReqContent[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    public static class StrangerVisitor implements Parcelable {
        @Index(0)
        public String faceId;
        @Index(1)
        public String image_url;
        @Index(2)
        public int ossType;
        @Index(3)
        public long lastTime;


        @Override
        public String toString() {
            return "StrangerVisitor{" +
                    "faceId='" + faceId + '\'' +
                    ", image_url='" + image_url + '\'' +
                    ", ossType=" + ossType +
                    ", lastTime=" + lastTime +
                    '}';
        }

        public StrangerVisitor() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            StrangerVisitor that = (StrangerVisitor) o;

            return lastTime == that.lastTime;
        }

        @Override
        public int hashCode() {
            return (int) (lastTime ^ (lastTime >>> 32));
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.faceId);
            dest.writeString(this.image_url);
            dest.writeInt(this.ossType);
            dest.writeLong(this.lastTime);
        }

        protected StrangerVisitor(Parcel in) {
            this.faceId = in.readString();
            this.image_url = in.readString();
            this.ossType = in.readInt();
            this.lastTime = in.readLong();
        }

        public static final Creator<StrangerVisitor> CREATOR = new Creator<StrangerVisitor>() {
            @Override
            public StrangerVisitor createFromParcel(Parcel source) {
                return new StrangerVisitor(source);
            }

            @Override
            public StrangerVisitor[] newArray(int size) {
                return new StrangerVisitor[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    public static class StrangerVisitorList implements Parcelable {
        @Index(0)
        public int total;
        @Index(1)
        public List<StrangerVisitor> strangerVisitors;

        @Override
        public String toString() {
            return "StrangerVisitorList{" +
                    "total=" + total +
                    ", strangerVisitors=" + strangerVisitors +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.total);
            dest.writeList(this.strangerVisitors);
        }

        public StrangerVisitorList() {
        }

        protected StrangerVisitorList(Parcel in) {
            this.total = in.readInt();
            this.strangerVisitors = new ArrayList<StrangerVisitor>();
            in.readList(this.strangerVisitors, StrangerVisitor.class.getClassLoader());
        }

        public static final Parcelable.Creator<StrangerVisitorList> CREATOR = new Parcelable.Creator<StrangerVisitorList>() {
            @Override
            public StrangerVisitorList createFromParcel(Parcel source) {
                return new StrangerVisitorList(source);
            }

            @Override
            public StrangerVisitorList[] newArray(int size) {
                return new StrangerVisitorList[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    public static class FetchMsgListRsp implements Parcelable {
        //rsp=msgpack(cid, type, id, timeMsec, [505?, 505?, ...])
        @Index(0)
        public String cid;
        @Index(1)
        public int msgType;
        @Index(2)
        public String faceId;
        @Index(3)
        public int timeMsec;
        @Index(4)
        public List<DPHeader> dataList;


        @Override
        public String toString() {
            return "FetchMsgListRsp{" +
                    "cid='" + cid + '\'' +
                    ", msgType=" + msgType +
                    ", faceId='" + faceId + '\'' +
                    ", dataList=" + dataList +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.cid);
            dest.writeInt(this.msgType);
            dest.writeString(this.faceId);
            dest.writeTypedList(this.dataList);
        }

        public FetchMsgListRsp() {
        }

        protected FetchMsgListRsp(Parcel in) {
            this.cid = in.readString();
            this.msgType = in.readInt();
            this.faceId = in.readString();
            this.dataList = in.createTypedArrayList(DPHeader.CREATOR);
        }

        public static final Parcelable.Creator<FetchMsgListRsp> CREATOR = new Parcelable.Creator<FetchMsgListRsp>() {
            @Override
            public FetchMsgListRsp createFromParcel(Parcel source) {
                return new FetchMsgListRsp(source);
            }

            @Override
            public FetchMsgListRsp[] newArray(int size) {
                return new FetchMsgListRsp[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    public static class DPHeader implements Parcelable {
        @Index(0)
        public int msgId;
        @Index(1)
        public long version;
        @Index(2)
        public byte[] bytes;

        @Override
        public String toString() {
            return "DPHeader{" +
                    "msgId=" + msgId +
                    ", seq=" + version +
                    ", bytes=" + Arrays.toString(bytes) +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.msgId);
            dest.writeLong(this.version);
            dest.writeByteArray(this.bytes);
        }

        public DPHeader() {
        }

        protected DPHeader(Parcel in) {
            this.msgId = in.readInt();
            this.version = in.readLong();
            this.bytes = in.createByteArray();
        }

        public static final Creator<DPHeader> CREATOR = new Creator<DPHeader>() {
            @Override
            public DPHeader createFromParcel(Parcel source) {
                return new DPHeader(source);
            }

            @Override
            public DPHeader[] newArray(int size) {
                return new DPHeader[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    public static class FetchMsgListReq {
        //req=msgpack(cid, type, id, timeMsec)
        @Index(0)
        public String cid;
        @Index(1)
        public int msgType;
        @Index(2)
        public String faceId;
        @Index(3)
        public long seq;

        @Override
        public String toString() {
            return "FetchMsgList{" +
                    "cid='" + cid + '\'' +
                    ", msgType=" + msgType +
                    ", faceId='" + faceId + '\'' +
                    ", seq=" + seq +
                    '}';
        }
    }

    @org.msgpack.annotation.Message
    public static class VisitsTimesRsp implements Parcelable {
        @Index(0)
        public String cid;
        @Index(1)
        public int msgType;
        @Index(2)
        public String faceFaceId;
        @Index(3)
        public int count;

        @Override
        public String toString() {
            return "VisitsTimesRsp{" +
                    "cid='" + cid + '\'' +
                    ", msgType=" + msgType +
                    ", faceFaceId='" + faceFaceId + '\'' +
                    ", count=" + count +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.cid);
            dest.writeInt(this.msgType);
            dest.writeString(this.faceFaceId);
            dest.writeInt(this.count);
        }

        public VisitsTimesRsp() {
        }

        protected VisitsTimesRsp(Parcel in) {
            this.cid = in.readString();
            this.msgType = in.readInt();
            this.faceFaceId = in.readString();
            this.count = in.readInt();
        }

        public static final Parcelable.Creator<VisitsTimesRsp> CREATOR = new Parcelable.Creator<VisitsTimesRsp>() {
            @Override
            public VisitsTimesRsp createFromParcel(Parcel source) {
                return new VisitsTimesRsp(source);
            }

            @Override
            public VisitsTimesRsp[] newArray(int size) {
                return new VisitsTimesRsp[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    public static class VisitsTimesReq implements Parcelable {

        @Index(0)
        public String cid;
        @Index(1)
        public int msgType;
        @Index(2)
        public String faceId;

        @Override
        public String toString() {
            return "VisitsTimesReq{" +
                    "cid='" + cid + '\'' +
                    ", msgType=" + msgType +
                    ", faceId='" + faceId + '\'' +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.cid);
            dest.writeInt(this.msgType);
            dest.writeString(this.faceId);
        }

        public VisitsTimesReq() {
        }

        protected VisitsTimesReq(Parcel in) {
            this.cid = in.readString();
            this.msgType = in.readInt();
            this.faceId = in.readString();
        }

        public static final Parcelable.Creator<VisitsTimesReq> CREATOR = new Parcelable.Creator<VisitsTimesReq>() {
            @Override
            public VisitsTimesReq createFromParcel(Parcel source) {
                return new VisitsTimesReq(source);
            }

            @Override
            public VisitsTimesReq[] newArray(int size) {
                return new VisitsTimesReq[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    public static class DelVisitorReq {
        @Index(0)
        public String cid;
        @Index(1)
        public int type;
        @Index(2)
        public String id;

        public DelVisitorReq(String cid, int type, String id, int delMsg) {
            this.cid = cid;
            this.type = type;
            this.id = id;
            this.delMsg = delMsg;
        }

        @Index(3)
        public int delMsg;
    }

    @org.msgpack.annotation.Message
    public static class Unit implements Parcelable {
        @Index(0)
        public short video;
        @Optional
        @Index(1)
        public short mode;


        @Override
        public String toString() {
            return "Unit{" +
                    "video=" + video +
                    ", mode=" + mode +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.video);
            dest.writeInt(this.mode);
        }

        public Unit() {
        }

        protected Unit(Parcel in) {
            this.video = (short) in.readInt();
            this.mode = (short) in.readInt();
        }

        public static final Parcelable.Creator<Unit> CREATOR = new Parcelable.Creator<Unit>() {
            @Override
            public Unit createFromParcel(Parcel source) {
                return new Unit(source);
            }

            @Override
            public Unit[] newArray(int size) {
                return new Unit[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    public static class UniversalDataBaseRsp implements Parcelable {
        @Index(0)
        public int id;
        @Index(1)
        public String caller;
        @Index(2)
        public String callee;
        @Index(3)
        public long seq;
        @Index(4)
        public int way;
        @Index(5)
        public Map<Integer, List<Unit>> dataMap;

        @Override
        public String toString() {
            return "UniversalDataBaseRsp{" +
                    "id=" + id +
                    ", caller='" + caller + '\'' +
                    ", callee='" + callee + '\'' +
                    ", seq=" + seq +
                    ", way=" + way +
                    ", dataMap=" + dataMap +
                    '}';
        }

        public UniversalDataBaseRsp() {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.id);
            dest.writeString(this.caller);
            dest.writeString(this.callee);
            dest.writeLong(this.seq);
            dest.writeInt(this.way);
            dest.writeInt(this.dataMap.size());
            for (Map.Entry<Integer, List<Unit>> entry : this.dataMap.entrySet()) {
                dest.writeValue(entry.getKey());
                dest.writeTypedList(entry.getValue());
            }
        }

        protected UniversalDataBaseRsp(Parcel in) {
            this.id = in.readInt();
            this.caller = in.readString();
            this.callee = in.readString();
            this.seq = in.readLong();
            this.way = in.readInt();
            int dataMapSize = in.readInt();
            this.dataMap = new HashMap<Integer, List<Unit>>(dataMapSize);
            for (int i = 0; i < dataMapSize; i++) {
                Integer key = (Integer) in.readValue(Integer.class.getClassLoader());
                List<Unit> value = in.createTypedArrayList(Unit.CREATOR);
                this.dataMap.put(key, value);
            }
        }

        public static final Creator<UniversalDataBaseRsp> CREATOR = new Creator<UniversalDataBaseRsp>() {
            @Override
            public UniversalDataBaseRsp createFromParcel(Parcel source) {
                return new UniversalDataBaseRsp(source);
            }

            @Override
            public UniversalDataBaseRsp[] newArray(int size) {
                return new UniversalDataBaseRsp[size];
            }
        };
    }

    @org.msgpack.annotation.Message
    public static class AcquaintanceItem {
        @Index(0)
        public String face_id;
        @Index(1)
        public String image_url;
        @Index(2)
        public int oss_type;
    }

    //rsp=msgpack(cid, person_id, [[face_id1, image_url1, oss_type1],  [face_id2, image_url2, oss_type2], ...])
    @org.msgpack.annotation.Message
    public static class AcquaintanceListRsp {
        @Index(0)
        public String cid;
        @Index(1)
        public String person_id;
        @Index(2)
        public List<AcquaintanceItem> acquaintanceItems;
    }

    //req=msgpack(cid, person_id)
    @org.msgpack.annotation.Message
    public static class AcquaintanceListReq {
        @Index(0)
        public String cid;
        @Index(1)
        public String person_id;

        public AcquaintanceListReq(String cid, String person_id) {
            this.cid = cid;
            this.person_id = person_id;
        }
    }

    @Message
    public static class GetRobotServerReq {
        @Index(0)
        public String cid;
        @Index(1)
        public String vid;

        public GetRobotServerReq() {

        }

        public GetRobotServerReq(String cid, String vid) {
            this.cid = cid;
            this.vid = vid;
        }
    }

    @Message
    public static class GetRobotServerRsp {
        @Index(0)
        public String host;
        @Index(1)
        public int port;
    }

    @Message
    public static class Rect4F {
        @Index(0)
        public float left;
        @Index(1)
        public float top;
        @Index(2)
        public float right;
        @Index(3)
        public float bottom;
    }

    @Message
    public static class DPCameraWarnArea extends BaseDataPoint {
//        enable   bool
//        是否开启  rects
//        array
//        侦测区域，如:[[x1,y1,x2,y2],[x1,y1,x2,y2],...]

        @Index(0)
        public boolean enable;
        @Index(1)
        public List<Rect4F> rects;

    }

    @Message
    public static class DpMessage implements Parcelable {
        @Index(0)
        public int msgId;
        @Index(1)
        public long version;
        @Index(2)
        public Value value;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.msgId);
            dest.writeLong(this.version);
            byte[] bytes = DpUtils.pack(value);
            dest.writeInt(bytes == null ? 0 : bytes.length);
            dest.writeByteArray(bytes);
        }

        public DpMessage() {
        }

        protected DpMessage(Parcel in) {
            this.msgId = in.readInt();
            this.version = in.readLong();
            byte[] bytes = new byte[in.readInt()];
            in.readByteArray(bytes);
            this.value = DpUtils.unpack(bytes);
        }

        public static final Parcelable.Creator<DpMessage> CREATOR = new Parcelable.Creator<DpMessage>() {
            @Override
            public DpMessage createFromParcel(Parcel source) {
                return new DpMessage(source);
            }

            @Override
            public DpMessage[] newArray(int size) {
                return new DpMessage[size];
            }
        };
    }
}
