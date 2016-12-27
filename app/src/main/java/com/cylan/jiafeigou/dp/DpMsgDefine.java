package com.cylan.jiafeigou.dp;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.cylan.annotation.DpBase;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.n.mvp.model.BaseBean;
import com.cylan.jiafeigou.utils.ParcelableUtils;
import com.google.gson.Gson;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-11-9.
 */


public class DpMsgDefine {


    @Message
    public static final class MsgNet extends BaseDataPoint implements Parcelable {
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

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.net);
            dest.writeString(this.ssid);
        }

        public MsgNet() {
        }

        protected MsgNet(Parcel in) {
            this.net = in.readInt();
            this.ssid = in.readString();
        }

        public static final Creator<MsgNet> CREATOR = new Creator<MsgNet>() {
            @Override
            public MsgNet createFromParcel(Parcel source) {
                return new MsgNet(source);
            }

            @Override
            public MsgNet[] newArray(int size) {
                return new MsgNet[size];
            }
        };
    }

    @Message
    public static final class MsgTimeZone extends BaseDataPoint implements Parcelable {
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

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.timezone);
            dest.writeInt(this.offset);
        }

        public MsgTimeZone() {
        }

        protected MsgTimeZone(Parcel in) {
            this.timezone = in.readString();
            this.offset = in.readInt();
        }

        public static final Creator<MsgTimeZone> CREATOR = new Creator<MsgTimeZone>() {
            @Override
            public MsgTimeZone createFromParcel(Parcel source) {
                return new MsgTimeZone(source);
            }

            @Override
            public MsgTimeZone[] newArray(int size) {
                return new MsgTimeZone[size];
            }
        };
    }

    @Message
    public static final class BindLog extends BaseDataPoint implements Parcelable {
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
            dest.writeByte(this.isBind ? (byte) 1 : (byte) 0);
            dest.writeString(this.account);
            dest.writeString(this.oldAccount);
        }

        public BindLog() {
        }

        protected BindLog(Parcel in) {
            this.isBind = in.readByte() != 0;
            this.account = in.readString();
            this.oldAccount = in.readString();
        }

        public static final Creator<BindLog> CREATOR = new Creator<BindLog>() {
            @Override
            public BindLog createFromParcel(Parcel source) {
                return new BindLog(source);
            }

            @Override
            public BindLog[] newArray(int size) {
                return new BindLog[size];
            }
        };

        @Override
        public String toString() {
            return "BindLog{" +
                    "isBind=" + isBind +
                    ", account='" + account + '\'' +
                    ", oldAccount='" + oldAccount + '\'' +
                    '}';
        }
    }

    @Message
    public static final class SdStatus extends BaseDataPoint implements Parcelable {
        @Index(0)
        public long total;
        @Index(1)
        public long used;
        @Index(2)
        public int err;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.total);
            dest.writeLong(this.used);
            dest.writeInt(this.err);
        }

        public SdStatus() {
        }

        protected SdStatus(Parcel in) {
            this.total = in.readLong();
            this.used = in.readLong();
            this.err = in.readInt();
        }

        public static final Creator<SdStatus> CREATOR = new Creator<SdStatus>() {
            @Override
            public SdStatus createFromParcel(Parcel source) {
                return new SdStatus(source);
            }

            @Override
            public SdStatus[] newArray(int size) {
                return new SdStatus[size];
            }
        };

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
    public static final class AlarmInfo extends BaseDataPoint implements Parcelable {
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
            dest.writeInt(this.timeStart);
            dest.writeInt(this.timeEnd);
            dest.writeInt(this.day);
        }

        public AlarmInfo() {
        }

        protected AlarmInfo(Parcel in) {
            this.timeStart = in.readInt();
            this.timeEnd = in.readInt();
            this.day = in.readInt();
        }

        public static final Creator<AlarmInfo> CREATOR = new Creator<AlarmInfo>() {
            @Override
            public AlarmInfo createFromParcel(Parcel source) {
                return new AlarmInfo(source);
            }

            @Override
            public AlarmInfo[] newArray(int size) {
                return new AlarmInfo[size];
            }
        };

        @Override
        public String toString() {
            return "AlarmInfo{" +
                    "timeStart=" + timeStart +
                    ", timeEnd=" + timeEnd +
                    ", duration=" + day +
                    '}';
        }
    }

    @Message
    public static final class AlarmMsg extends BaseDataPoint implements Parcelable {//505 报警消息
        @Index(0)
        public int time;
        @Index(1)
        public int isRecording;
        @Index(2)
        public int fileIndex;
        @Index(3)
        public int type;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.time);
            dest.writeInt(this.isRecording);
            dest.writeInt(this.fileIndex);
            dest.writeInt(this.type);
        }

        public AlarmMsg() {
        }

        @Override
        public String toString() {
            return "AlarmMsg{" +
                    "time=" + time +
                    ", isRecording=" + isRecording +
                    ", fileIndex=" + fileIndex +
                    ", type=" + type +
                    '}';
        }

        protected AlarmMsg(Parcel in) {
            this.time = in.readInt();
            this.isRecording = in.readInt();
            this.fileIndex = in.readInt();
            this.type = in.readInt();
        }

        public static final Creator<AlarmMsg> CREATOR = new Creator<AlarmMsg>() {
            @Override
            public AlarmMsg createFromParcel(Parcel source) {
                return new AlarmMsg(source);
            }

            @Override
            public AlarmMsg[] newArray(int size) {
                return new AlarmMsg[size];
            }
        };
    }

    @Message//504
    public static final class NotificationInfo extends BaseDataPoint implements Parcelable {
        @Index(0)
        public int notification;
        @Index(1)
        public int duration;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            NotificationInfo that = (NotificationInfo) o;

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
            dest.writeInt(this.notification);
            dest.writeInt(this.duration);
        }

        @Override
        public String toString() {
            return "NotificationInfo{" +
                    "notification=" + notification +
                    ", duration=" + duration +
                    '}';
        }

        public NotificationInfo() {
        }

        protected NotificationInfo(Parcel in) {
            this.notification = in.readInt();
            this.duration = in.readInt();
        }

        public static final Creator<NotificationInfo> CREATOR = new Creator<NotificationInfo>() {
            @Override
            public NotificationInfo createFromParcel(Parcel source) {
                return new NotificationInfo(source);
            }

            @Override
            public NotificationInfo[] newArray(int size) {
                return new NotificationInfo[size];
            }
        };
    }

    @Message
    public static final class TimeLapse extends BaseDataPoint implements Parcelable {
        @Index(0)
        public int timeStart;
        @Index(1)
        public int timePeriod;
        @Index(2)
        public int timeDuration;
        @Index(3)
        public int status;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.timeStart);
            dest.writeInt(this.timePeriod);
            dest.writeInt(this.timeDuration);
            dest.writeInt(this.status);
        }

        public TimeLapse() {
        }

        protected TimeLapse(Parcel in) {
            this.timeStart = in.readInt();
            this.timePeriod = in.readInt();
            this.timeDuration = in.readInt();
            this.status = in.readInt();
        }

        public static final Creator<TimeLapse> CREATOR = new Creator<TimeLapse>() {
            @Override
            public TimeLapse createFromParcel(Parcel source) {
                return new TimeLapse(source);
            }

            @Override
            public TimeLapse[] newArray(int size) {
                return new TimeLapse[size];
            }
        };

        @Override
        public String toString() {
            return "TimeLapse{" +
                    "timeStart=" + timeStart +
                    ", timePeriod=" + timePeriod +
                    ", timeDuration=" + timeDuration +
                    ", status=" + status +
                    '}';
        }
    }

    @Message
    public static final class CamCoord extends BaseDataPoint implements Parcelable {
        @Index(0)
        public int x;
        @Index(1)
        public int y;
        @Index(2)
        public int r;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.x);
            dest.writeInt(this.y);
            dest.writeInt(this.r);
        }

        public CamCoord() {
        }

        protected CamCoord(Parcel in) {
            this.x = in.readInt();
            this.y = in.readInt();
            this.r = in.readInt();
        }

        public static final Creator<CamCoord> CREATOR = new Creator<CamCoord>() {
            @Override
            public CamCoord createFromParcel(Parcel source) {
                return new CamCoord(source);
            }

            @Override
            public CamCoord[] newArray(int size) {
                return new CamCoord[size];
            }
        };

        @Override
        public String toString() {
            return "CamCoord{" +
                    "x=" + x +
                    ", y=" + y +
                    ", r=" + r +
                    '}';
        }
    }

    @Message
    public static final class BellCallState extends BaseDataPoint implements Parcelable {

        @Index(0)
        public int state;

        @Index(1)
        public int time;

        @Index(2)
        public int duration;

        @Index(3)
        public int type;

        public BellCallState() {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.state);
            dest.writeInt(this.time);
            dest.writeInt(this.duration);
            dest.writeInt(this.type);
        }

        protected BellCallState(Parcel in) {
            this.state = in.readInt();
            this.time = in.readInt();
            this.duration = in.readInt();
            this.type = in.readInt();
        }

        public static final Creator<BellCallState> CREATOR = new Creator<BellCallState>() {
            @Override
            public BellCallState createFromParcel(Parcel source) {
                return new BellCallState(source);
            }

            @Override
            public BellCallState[] newArray(int size) {
                return new BellCallState[size];
            }
        };

        @Override
        public String toString() {
            return "BellCallState{" +
                    "state=" + state +
                    ", time=" + time +
                    ", duration=" + duration +
                    ", type=" + type +
                    '}';
        }
    }

    @DpBase
    public static class DpMsg implements Parcelable {
        public int msgId;
        public long version;
        public Object o;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DpMsg dpMsg = (DpMsg) o;

            return msgId == dpMsg.msgId;

        }

        @Override
        public int hashCode() {
            return msgId;
        }

        @Override
        public String toString() {
            return "DpMsg{" +
                    "msgId=" + msgId +
                    ", version=" + version +
                    ", o=" + new Gson().toJson(o) +
                    '}';
        }

        public DpMsg() {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.msgId);
            dest.writeLong(this.version);
            //一定要实现
            Class<?> clazz = DpMsgMap.ID_2_CLASS_MAP.get(msgId);
            if (this.o == null) {
                Log.d("DpMsgDefine", "write: " + msgId);
            }
            try {
                ParcelableUtils.write(clazz, dest, this.o, flags);
            } catch (Exception e) {
                if (BuildConfig.DEBUG)
                    throw new IllegalArgumentException(e.getLocalizedMessage());
            }
        }

        protected DpMsg(Parcel in) {
            this.msgId = in.readInt();
            this.version = in.readLong();
            try {
                Class<?> clazz = DpMsgMap.ID_2_CLASS_MAP.get(msgId);
                this.o = ParcelableUtils.read(clazz, in);
            } catch (Exception e) {
                if (BuildConfig.DEBUG)
                    throw new IllegalArgumentException(e.getLocalizedMessage());
            }
            Log.d("DpMsgDefine", "read:  type:" + msgId + " " + o);
        }

        public static final Creator<DpMsg> CREATOR = new Creator<DpMsg>() {
            @Override
            public DpMsg createFromParcel(Parcel source) {
                return new DpMsg(source);
            }

            @Override
            public DpMsg[] newArray(int size) {
                return new DpMsg[size];
            }
        };
    }


    public static class DpWrap implements Parcelable {
        public BaseBean baseDpDevice;
        public ArrayList<DpMsg> baseDpMsgList;

        @Override
        public String toString() {
            return "DpWrap{" +
                    "baseDpDevice=" + baseDpDevice +
                    ", baseDpMsgList=" + baseDpMsgList +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.baseDpDevice, flags);
            dest.writeTypedList(this.baseDpMsgList);
        }

        public DpWrap() {
        }

        protected DpWrap(Parcel in) {
            this.baseDpDevice = in.readParcelable(BaseBean.class.getClassLoader());
            this.baseDpMsgList = in.createTypedArrayList(DpMsg.CREATOR);
        }

        public static final Creator<DpWrap> CREATOR = new Creator<DpWrap>() {
            @Override
            public DpWrap createFromParcel(Parcel source) {
                return new DpWrap(source);
            }

            @Override
            public DpWrap[] newArray(int size) {
                return new DpWrap[size];
            }
        };
    }


}
