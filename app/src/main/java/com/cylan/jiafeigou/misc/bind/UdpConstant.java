package com.cylan.jiafeigou.misc.bind;

import android.os.Parcel;
import android.os.Parcelable;

import com.cylan.udpMsgPack.JfgUdpMsg;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cylan-hunt on 16-11-10.
 */

public class UdpConstant {
    public static final String IP = "255.255.255.255";
    public static final short PORT = 10008;
    public static final String PING_ACK = "ping_ack";
    public static final String F_PING_ACK = "f_ping_ack";

    /**
     * 绑定的设备画像
     */
    public static final String KEY_BINDE_DEVICE_PORTRAIT = "key_binde_device_portrait";
    public static final Map<Class<?>, Object> udpObjectMap = new HashMap<>();

    public static final class PingAckT {
        public long time;
        public JfgUdpMsg.PingAck pingAck;

        public PingAckT(long time, JfgUdpMsg.PingAck pingAck) {
            this.time = time;
            this.pingAck = pingAck;
        }
    }

    public static final class FPingAckT {
        public long time;
        public JfgUdpMsg.FPingAck fPingAck;

        public FPingAckT(long time, JfgUdpMsg.FPingAck fPingAck) {
            this.time = time;
            this.fPingAck = fPingAck;
        }
    }

    /**
     * 用来包含ping_ack,fping_ack消息内容的类.
     */
    public static final class UdpDevicePortrait implements Parcelable {
        public String mac;
        public String cid;
        public String version;
        public int net;

        @Override
        public String toString() {
            return "UdpDevicePortrait{" +
                    "mac='" + mac + '\'' +
                    ", cid='" + cid + '\'' +
                    ", version='" + version + '\'' +
                    ", net=" + net +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mac);
            dest.writeString(this.cid);
            dest.writeString(this.version);
            dest.writeInt(this.net);
        }

        public UdpDevicePortrait() {
        }

        protected UdpDevicePortrait(Parcel in) {
            this.mac = in.readString();
            this.cid = in.readString();
            this.version = in.readString();
            this.net = in.readInt();
        }

        public static final Parcelable.Creator<UdpDevicePortrait> CREATOR = new Parcelable.Creator<UdpDevicePortrait>() {
            @Override
            public UdpDevicePortrait createFromParcel(Parcel source) {
                return new UdpDevicePortrait(source);
            }

            @Override
            public UdpDevicePortrait[] newArray(int size) {
                return new UdpDevicePortrait[size];
            }
        };
    }
}
