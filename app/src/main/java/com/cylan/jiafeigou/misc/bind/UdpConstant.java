package com.cylan.jiafeigou.misc.bind;

import android.os.Parcel;
import android.os.Parcelable;

import com.cylan.udpMsgPack.JfgUdpMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * Created by cylan-hunt on 16-11-10.
 */

public class UdpConstant {
    public static final String IP = "255.255.255.255";
    public static final short PORT = 10008;
    public static final String PIP = "192.168.10.255";
    public static final String F_ACK = "f_ack";
    public static final String PING_ACK = "ping_ack";
    public static final String F_PING_ACK = "f_ping_ack";
    public static final String UPGRADE_VERSION = "0.0.0.0";
    public static final String BIND_TAG = "bindTag:";
    public static final String DOORBELL_RING = "doorbell_ring";


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
        public String uuid;
        public String version;
        public int net;
        public String bindCode;
        public int bindFlag = 1;//默认

        @Override
        public String toString() {
            return "UdpDevicePortrait{" +
                    "mac='" + mac + '\'' +
                    ", uuid='" + uuid + '\'' +
                    ", version='" + version + '\'' +
                    ", net=" + net +
                    ", bindCode='" + bindCode + '\'' +
                    ", bindFlag=" + bindFlag +
                    '}';
        }

        public UdpDevicePortrait() {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mac);
            dest.writeString(this.uuid);
            dest.writeString(this.version);
            dest.writeInt(this.net);
            dest.writeString(this.bindCode);
            dest.writeInt(this.bindFlag);
        }

        protected UdpDevicePortrait(Parcel in) {
            this.mac = in.readString();
            this.uuid = in.readString();
            this.version = in.readString();
            this.net = in.readInt();
            this.bindCode = in.readString();
            this.bindFlag = in.readInt();
        }

        public static final Creator<UdpDevicePortrait> CREATOR = new Creator<UdpDevicePortrait>() {
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

    public static final class UpgradeStatus {
        /**
         * {@link IBindResult#UPGRADE_FAILED,IBindResult#UPGRADING,IBindResult#UPGRADE_SUCCESS}
         */
        public int state;

        public UpgradeStatus(int state) {
            this.state = state;
        }

        @Override
        public String toString() {
            return "UpgradeStatus{" +
                    "state=" + state +
                    '}';
        }
    }

    @Message
    public static class UdpFirmwareUpdate extends JfgUdpMsg.UdpHeader {
        @Index(1)
        public String cid;
        @Index(2)
        public String ip;
        @Index(3)
        public int port;
        @Index(4)
        public String url;

        public UdpFirmwareUpdate(String url, String cid, String ip, int port) {
            this.url = url;
            this.cmd = "f_upgrade";
            this.ip = ip;
            this.port = port;
            this.cid = cid;
        }
    }

    @Message
    public static final class FAck extends JfgUdpMsg.UdpRecvHeard {
        @Index(2)
        public int ack;
        @Index(3)
        public int ret;
    }
}
