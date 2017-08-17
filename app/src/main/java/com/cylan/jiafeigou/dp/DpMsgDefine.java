package com.cylan.jiafeigou.dp;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;
import org.msgpack.annotation.Optional;

import java.util.TimeZone;

/**
 * Created by cylan-hunt on 16-11-9.
 */

/**
 * @Deprecated msgPack 将升级到0.8 版本,不以反射的方式来解析了,将会通过静态方法的方式
 */
public class DpMsgDefine {
    @Message
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

    @Message
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

    @Message
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

    @Message
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
    @Message
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
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            DPSdcardSummary summary = (DPSdcardSummary) o;

            if (hasSdcard != summary.hasSdcard) return false;
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

    @Message
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

    @Message
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

    @Message
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

    @Message
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
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            DPAlarm dpAlarm = (DPAlarm) o;

            if (time != dpAlarm.time) return false;
            if (isRecording != dpAlarm.isRecording) return false;
            if (fileIndex != dpAlarm.fileIndex) return false;
            if (ossType != dpAlarm.ossType) return false;
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
        }

        protected DPAlarm(Parcel in) {
            super(in);
            this.time = in.readInt();
            this.isRecording = in.readInt();
            this.fileIndex = in.readInt();
            this.ossType = in.readInt();
            this.tly = in.readString();
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

    @Message//504
    public static final class DPNotificationInfo extends BaseDataPoint implements Parcelable {
        @Index(0)
        public int notification;
        @Index(1)
        public int duration;

        @Ignore
        public static DPNotificationInfo empty = new DPNotificationInfo();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DPNotificationInfo that = (DPNotificationInfo) o;

            if (notification != that.notification) return false;
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

    @Message
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

    @Message
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

    @Message
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

    @Message
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
            if (loadBean == null)
                loadBean = new DPWonderItem();
            loadBean.msgType = TYPE_LOAD;
            return loadBean;
        }

        public static DPWonderItem getNoMoreTypeBean() {
            if (nomore == null)
                nomore = new DPWonderItem();
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

    @Message
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

    @Message
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

    @Message
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
    @Message
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

    @Message
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

    @Message
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

    @Message
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

    @Message
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

//    @Message
//    public static class DPWarnInterval extends BaseDataPoint {
//
//        @Index(0)
//        public int sec;
//
//        @Override
//        public int describeContents() {
//            return 0;
//        }
//
//        @Override
//        public void writeToParcel(Parcel dest, int flags) {
//            super.writeToParcel(dest, flags);
//            dest.writeInt(this.sec);
//        }
//
//        public DPWarnInterval() {
//        }
//
//        protected DPWarnInterval(Parcel in) {
//            super(in);
//            this.sec = in.readInt();
//        }
//
//        public static final Creator<DPWarnInterval> CREATOR = new Creator<DPWarnInterval>() {
//            @Override
//            public DPWarnInterval createFromParcel(Parcel source) {
//                return new DPWarnInterval(source);
//            }
//
//            @Override
//            public DPWarnInterval[] newArray(int size) {
//                return new DPWarnInterval[size];
//            }
//        };
//    }
//
//
//    @Message
//    @Deprecated
//    public static class DPCameraObjectDetect extends BaseDataPoint {
//
//        @Index(0)
//        public int[] objects = new int[]{};
//
//        @Override
//        public int describeContents() {
//            return 0;
//        }
//
//        @Override
//        public void writeToParcel(Parcel dest, int flags) {
//            super.writeToParcel(dest, flags);
//            dest.writeIntArray(this.objects);
//        }
//
//        public DPCameraObjectDetect() {
//        }
//
//        @Override
//        public byte[] toBytes() {
//            return DpUtils.pack(objects);
//        }
//
//        protected DPCameraObjectDetect(Parcel in) {
//            super(in);
//            this.objects = in.createIntArray();
//        }
//
//        public static final Creator<DPCameraObjectDetect> CREATOR = new Creator<DPCameraObjectDetect>() {
//            @Override
//            public DPCameraObjectDetect createFromParcel(Parcel source) {
//                return new DPCameraObjectDetect(source);
//            }
//
//            @Override
//            public DPCameraObjectDetect[] newArray(int size) {
//                return new DPCameraObjectDetect[size];
//            }
//        };
//    }
}
