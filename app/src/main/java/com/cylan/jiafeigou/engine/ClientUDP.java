package com.cylan.jiafeigou.engine;

import android.os.Handler;
import android.os.Message;

import com.cylan.publicApi.Constants;
import com.cylan.publicApi.DswLog;
import com.cylan.jiafeigou.listener.UDPMessageListener;
import com.cylan.jiafeigou.utils.ThreadPoolUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.TimeZone;

public class ClientUDP implements Runnable {
    private static final String TAG = "UDP";
    protected DatagramSocket ms;
    public static final int PORT = 10008;
    public static final int BINDADDR = 10009;
    public static final String INETADDRESS = "255.255.255.255";
    protected boolean exit = false;
    protected DatagramPacket datagramPacket;

    private ArrayList<UDPMessageListener> mObservers = new ArrayList<>();


    public void setUDPMsgListener(UDPMessageListener udpListener) {
        if (!mObservers.contains(udpListener))
            mObservers.add(udpListener);
    }


    public void removeUDPMsgListener(UDPMessageListener udpListener) {
        mCid = "";
        isLoop = false;
        mObservers.remove(udpListener);
    }

    public void exit() {
        exit = true;
    }

    public void close() {
        exit = true;
        if (ms != null) ms.close();
    }


    private ClientUDP() throws InvalidParameterException {
        try {
            ms = new MulticastSocket(BINDADDR);
            ms.setSoTimeout(3000); // 3s
            new Thread(this).start();
        } catch (IOException e) {
            DswLog.ex(e.toString());
        }
    }

    public static ClientUDP getInstance() {
        if (Holder.instance == null) {
            synchronized (ClientUDP.class) {
                if (Holder.instance == null) {
                    Holder.instance = new ClientUDP();
                }
            }
        }
        return Holder.instance;
    }

    private static class Holder {
        private static ClientUDP instance = new ClientUDP();
    }


    @Override
    public void run() {
        byte buf[] = new byte[4096];
        int flag = 0;
        while (!exit) {
            try {
                datagramPacket = new DatagramPacket(buf, buf.length);
                byte[] data = recv();
                JFGCFG_HEADER jfgHeader = new JFGCFG_HEADER(data);

                if (jfgHeader.mMagic != JFG_MSG_MAGIC) {
                    flag |= 0x01;
                    continue;
                }
                if (jfgHeader.mMsgid == JFG_MSG_PONG) {
                    mUdpHandler.obtainMessage(JFG_MSG_PONG, new JFG_PONG(data)).sendToTarget();
                    flag |= 0x02;
                } else if (jfgHeader.mMsgid == JFG_MSG_SET_WIFI_RSP) {
                    mUdpHandler.obtainMessage(JFG_MSG_SET_WIFI_RSP, new JFG_SET_WIFI_RSP(data)).sendToTarget();
                    flag |= 0x04;
                } else if (jfgHeader.mMsgid == JFG_MSG_F_PONG) {
                    mUdpHandler.obtainMessage(JFG_MSG_F_PONG, new JFG_F_PONG(data)).sendToTarget();
                    flag |= 0x08;
                } else if (jfgHeader.mMsgid == JFG_MSG_F_ACK) {
                    mUdpHandler.obtainMessage(JFG_MSG_F_ACK, new JFG_F_ACK(data)).sendToTarget();
                    flag |= 0x10;
                } else if (jfgHeader.mMsgid == JPG_MSG_BELL_PRESS) {
                    mUdpHandler.obtainMessage(JPG_MSG_BELL_PRESS, new JFGCFG_HEADER(data)).sendToTarget();
                    flag |= 0x20;
                }
            } catch (Exception e) {
//                DswLog.ex(e.toString());
            }
        }
        DswLog.e("udp_action_flag: " + flag);
        if (ms != null) ms.close();
    }

    private Handler mUdpHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            try {
                for (int i = 0, size = mObservers.size(); i < size; i++) {
                    switch (msg.what) {
                        case JFG_MSG_PONG:
                            mObservers.get(i).JfgMsgPong((JFG_PONG) msg.obj);
                            break;
                        case JFG_MSG_SET_WIFI_RSP:
                            mObservers.get(i).JfgMsgSetWifiRsp((JFG_SET_WIFI_RSP) msg.obj);
                            break;
                        case JFG_MSG_F_PONG:
                            mObservers.get(i).JfgMsgFPong((JFG_F_PONG) msg.obj);
                            break;
                        case JFG_MSG_F_ACK:
                            mObservers.get(i).JfgMsgFAck((JFG_F_ACK) msg.obj);
                            break;
                        case JPG_MSG_BELL_PRESS:
                            mObservers.get(i).JfgMsgBellPress((JFGCFG_HEADER) msg.obj);
                            break;
                        default:
                            break;
                    }
                }

            } catch (Exception e) {
                DswLog.e("mUdpHandler ecxeption " + e.toString());
            }
            return true;
        }
    });

    public void send(final byte[] data, final InetAddress address, final int port) {
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                    ms.send(packet);
                    print(packet.getData(), "packet send--> ");
                } catch (Exception e) {
                    DswLog.d("packet send exception: " + e.toString());
                }
            }
        });
    }


    public byte[] recv() throws Exception {
        ms.receive(datagramPacket);
        print(datagramPacket.getData(), "packet recv--> ");
        return datagramPacket.getData();
    }

    /**
     * JSON { "cid","xxxx","act": "xxx", "msg": "xxx" }
     */

    // new wifi config
    public static final String JFG_MSG_INIT_CID = "jiafeigou";
    public static final int JFG_MSG_RET_OK = 0x0000;
    public static final int JFG_MSG_RET_ERROR = 0x0001;

    public static final short JFG_MSG_MAGIC = 0x4D4A;
    public static final short JFG_MSG_PING = 0x0000;
    public static final short JFG_MSG_PONG = 0x0001;
    public static final short JFG_MSG_SCAN_WIFI_REQ = 0x0002;
    public static final short JFG_MSG_SCAN_WIFI_RSP = 0x0003;
    public static final short JFG_MSG_SET_WIFI_REQ = 0x0004;
    public static final short JFG_MSG_SET_WIFI_RSP = 0x0005;
    public static final short JFG_MSG_AP_OFF_REQ = 0x0006;
    public static final short JFG_MSG_AP_OFF_RSP = 0x0007;
    public static final short JFG_MSG_HISTORY_VIDEO_REQ = 0x0008;
    public static final short JFG_MSG_HISTORY_VIDEO_RSP = 0x0009;
    public static final short JFG_MSG_WIFI_SWITCH_REQ = 0x000a;
    public static final short JFG_MSG_WIFI_SWITCH_RSP = 0x000b;
    public static final short JFG_MSG_WIFI_STATE_REQ = 0x000c;
    public static final short JFG_MSG_SET_SERVER = 0x000d;
    public static final short JFG_MSG_SET_TIMEZONE = 0x000e;
    public static final short JFG_MSG_WIFI_OPERATION_RESULT = 0x000f;
    public static final short JFG_MSG_SET_LANGUAGE = 0x0010;
    public static final short JFG_MSG_F_ACK = 0x1003;
    public static final short JFG_MSG_F_PING = 0x1004;
    public static final short JFG_MSG_F_PONG = 0x100E;
    public static final short JFG_MSG_F_UPGARDE = 0x1006;
    public static final short JPG_MSG_F_PLAY = 0x1007;
    public static final short JPG_MSG_F_STOP = 0x1008;
    public static final short JPG_MSG_BELL_PRESS = 0x0011;


    public class JFGCFG_HEADER {
        public static final int size4 = 4;
        public static final int size8 = 8;
        public static final int size16 = 16;
        public static final int size32 = 32;
        public static final int size128 = 128;
        public static final int size1024 = 1024;

        public short mMagic;
        public short mMsgid = -1;
        private byte[] tmp = new byte[size16];
        // public byte[] mCid = new byte[16];
        public String mCid;
        ByteBuffer mBuffer = ByteBuffer.allocate(size1024);

        public JFGCFG_HEADER(short msgid, String cid) {
            mMagic = JFG_MSG_MAGIC;
            mMsgid = msgid;
            mCid = cid;
            putBuffer();
        }

        public JFGCFG_HEADER(byte[] buffer) {
            if (buffer.length > size128) {
                mBuffer = ByteBuffer.allocate(buffer.length);
                mBuffer.put(buffer, 0, buffer.length);
            } else {
                mBuffer.put(buffer, 0, buffer.length);
            }
            mBuffer.position(0);

            byte[] shortTmp = new byte[2];
            if (mBuffer.remaining() >= 2) {
                mBuffer.get(shortTmp, 0, 2);
                mMagic = bytesToShort(shortTmp);
            }
            if (mBuffer.remaining() >= 2) {
                mBuffer.get(shortTmp, 0, 2);
                mMsgid = bytesToShort(shortTmp);
            }
            int len = size16;
            if (size16 > mBuffer.remaining()) {
                len = mBuffer.remaining();
            }
            mBuffer.get(tmp, 0, len);
            mCid = new String(tmp).trim();
        }

        private void putBuffer() {
            mBuffer.clear();
            mBuffer.put(shortToBytes(mMagic));
            mBuffer.put(shortToBytes(mMsgid));
            mBuffer.put(mCid.getBytes(), 0, mCid.length());
            int len = size16 - mCid.length();
            if (len > 0) {
                mBuffer.position(mBuffer.position() + len);
            }
        }

        public byte[] getBytes() {
            return mBuffer.array();
        }

        public byte[] shortToBytes(short n) {
            byte[] b = new byte[2];
            b[1] = (byte) (n & 0xff);
            b[0] = (byte) ((n >> 8) & 0xff);
            return b;
        }

        public short bytesToShort(byte[] b) {
            return (short) (b[1] & 0xff | (b[0] & 0xff) << 8);
        }

        public byte[] intToBytes(int n) {
            byte[] b = new byte[4];
            b[3] = (byte) (n & 0xff);
            b[2] = (byte) ((n >> 8) & 0xff);
            b[1] = (byte) ((n >> 16) & 0xff);
            b[0] = (byte) ((n >> 24) & 0xff);
            return b;
        }

        public int bytesToInt(byte[] b) {
            return (int) (b[3] & 0xff | (b[2] & 0xff) << 8 | (b[1] & 0xff) << 16 | (b[0] & 0xff) << 24);
        }
    }

    public class JFG_RESPONSE extends JFGCFG_HEADER {
        public int mError;

        public JFG_RESPONSE(short msgid, String cid) {
            super(msgid, cid);
            mError = JFG_MSG_RET_OK;
        }

        public JFG_RESPONSE(short msgid, String cid, int error) {
            super(msgid, cid);
            mError = error;
            putBuffer();
        }

        public JFG_RESPONSE(byte[] buffer) {
            super(buffer);
            byte[] b = new byte[4];
            int len = 4;
            if (4 > mBuffer.remaining()) {
                len = 4 - mBuffer.remaining();
            }
            mBuffer.get(b, 0, len);
            mError = bytesToInt(b);
        }

        private void putBuffer() {
            super.putBuffer();
            mBuffer.put(intToBytes(mError));
        }
    }

    public class JFG_PING extends JFGCFG_HEADER {
        public JFG_PING(String cid) {
            super(JFG_MSG_PING, cid);
        }

        public JFG_PING(byte[] buffer) {
            super(buffer);
        }
    }

    public class JFG_PONG extends JFGCFG_HEADER {
        public int mNet;

        public JFG_PONG(String cid, int net) {
            super(JFG_MSG_PONG, cid);
            mNet = net;
            putBuffer();
        }

        public JFG_PONG(byte[] buffer) {
            super(buffer);
            byte[] b = new byte[4];
            int len = 4;
            if (4 > mBuffer.remaining()) {
                len = 4 - mBuffer.remaining();
            }
            mBuffer.get(b, 0, len);
            mNet = bytesToInt(b);
        }

        private void putBuffer() {
            super.putBuffer();
            mBuffer.put(intToBytes(mNet));
        }
    }

    public class JFG_SET_WIFI_REQ extends JFGCFG_HEADER {
        public short mSecurity;
        public short mPadding = 0;
        public String mSSID; // 32
        public String mKey; // 32
        public String mAccout;

        public JFG_SET_WIFI_REQ(String cid, short security, String ssid, String key, String accout) {
            super(JFG_MSG_SET_WIFI_REQ, cid);
            mSecurity = security;
            mSSID = ssid;
            mKey = key;
            mAccout = accout;
            putBuffer();
        }

        public JFG_SET_WIFI_REQ(byte[] buffer) {
            super(buffer);
            byte[] b = new byte[2];
            mBuffer.get(b, 0, 2);
            mSecurity = bytesToShort(b);
            mBuffer.get(b, 0, 2);
            mPadding = bytesToShort(b);

            int len = size32;
            if (size32 > mBuffer.remaining()) {
                len = mBuffer.remaining();
            }
            byte[] tmp = new byte[size32];
            mBuffer.get(tmp, 0, len);
            mSSID = new String(tmp).trim();

            if (size32 > mBuffer.remaining()) {
                len = mBuffer.remaining();
            }
            mBuffer.get(tmp, 0, len);
            mKey = new String(tmp).trim();

            if (size32 > mBuffer.remaining()) {
                len = mBuffer.remaining();
            }
            mBuffer.get(tmp, 0, len);
            mAccout = new String(tmp).trim();
        }

        private void putBuffer() {
            super.putBuffer();
            int len = 0;
            mBuffer.put(shortToBytes(mSecurity));
            mBuffer.putShort(mPadding); // padding
            mBuffer.put(mSSID.getBytes(), 0, mSSID.getBytes().length);

            len = size32 - mSSID.getBytes().length;
            if (len > 0) {
                mBuffer.position(mBuffer.position() + len);
            }
            mBuffer.put(mKey.getBytes(), 0, mKey.length());
            len = size32 - mKey.length();
            if (len > 0) {
                mBuffer.position(mBuffer.position() + len);
            }

            mBuffer.put(mAccout.getBytes(), 0, mAccout.getBytes().length);
            len = size32 - mAccout.getBytes().length;
            if (len > 0) {
                mBuffer.position(mBuffer.position() + len);
            }
        }
    }

    public class JFG_SET_WIFI_RSP extends JFG_RESPONSE {

        public JFG_SET_WIFI_RSP(String cid, int error) {
            super(JFG_MSG_SET_WIFI_RSP, cid, error);
        }

        public JFG_SET_WIFI_RSP(byte[] buffer) {
            super(buffer);
        }
    }

    public class JFG_AP_OFF_REQ extends JFGCFG_HEADER {
        public String mNCid;

        public JFG_AP_OFF_REQ(String cid, String ncid) {
            super(JFG_MSG_AP_OFF_REQ, cid);
            mNCid = ncid;
            putBuffer();
        }

        public JFG_AP_OFF_REQ(byte[] buffer) {
            super(buffer);
            int len = size16;
            if (size16 > mBuffer.remaining()) {
                len = mBuffer.remaining();
            }
            byte[] tmp = new byte[size16];
            mBuffer.get(tmp, 0, len);
            mNCid = new String(tmp).trim();
        }

        private void putBuffer() {
            super.putBuffer();
            mBuffer.put(mNCid.getBytes(), 0, mNCid.length());
        }
    }

    public class JFG_AP_OFF_RSP extends JFG_RESPONSE {
        public JFG_AP_OFF_RSP(String cid) {
            super(JFG_MSG_AP_OFF_RSP, cid);
        }

        public JFG_AP_OFF_RSP(String cid, int error) {
            super(JFG_MSG_AP_OFF_RSP, cid, error);
        }

        public JFG_AP_OFF_RSP(byte[] buffer) {
            super(buffer);
        }
    }

    public class JFG_SCAN_WIFI_REQ extends JFGCFG_HEADER {
        public JFG_SCAN_WIFI_REQ(String cid) {
            super(JFG_MSG_SCAN_WIFI_REQ, cid);
        }

        public JFG_SCAN_WIFI_REQ(byte[] buffer) {
            super(buffer);
        }
    }

    public class JFG_SCAN_WIFI_RSP extends JFGCFG_HEADER {
        public short mIndex;
        public short mTotal;
        public short mRssi; // dbm值非负数部分 ,数值越小信号越强。（如果为0则表示该项无法测出）
        public short mSecurity;
        public String mSSID; // 32

        public JFG_SCAN_WIFI_RSP(String cid, short index, short total, short rssi, short security,
                                 String ssid) {
            super(JFG_MSG_SCAN_WIFI_RSP, cid);
            mIndex = index;
            mTotal = total;
            mRssi = rssi;
            mSecurity = security;
            mSSID = ssid;

            putBuffer();
        }

        public JFG_SCAN_WIFI_RSP(byte[] buffer) {
            super(buffer);
            byte[] b = new byte[2];
            mBuffer.get(b, 0, 2);
            mIndex = bytesToShort(b);
            mBuffer.get(b, 0, 2);
            mTotal = bytesToShort(b);
            mBuffer.get(b, 0, 2);
            mRssi = bytesToShort(b);
            mBuffer.get(b, 0, 2);
            mSecurity = bytesToShort(b);
            int len = size32;
            if (size32 > mBuffer.remaining()) {
                len = mBuffer.remaining();
            }
            byte[] tmp = new byte[size32];
            mBuffer.get(tmp, 0, len);
            mSSID = new String(tmp).trim();
        }

        private void putBuffer() {
            super.putBuffer();

            mBuffer.put(shortToBytes(mIndex));
            mBuffer.put(shortToBytes(mTotal));
            mBuffer.put(shortToBytes(mRssi));
            mBuffer.put(shortToBytes(mSecurity));
            mBuffer.put(mSSID.getBytes(), 0, mSSID.getBytes().length);

            int len = size32 - mSSID.getBytes().length;
            if (len > 0) {
                mBuffer.position(mBuffer.position() + len);
            }
        }
    }

    public class JFG_HISTORY_VIDEO_REQ extends JFGCFG_HEADER {
        public JFG_HISTORY_VIDEO_REQ(String cid) {
            super(JFG_MSG_HISTORY_VIDEO_REQ, cid);
        }

        public JFG_HISTORY_VIDEO_REQ(byte[] buffer) {
            super(buffer);
        }
    }

    public class JFG_HISTORY_VIDEO_RSP extends JFGCFG_HEADER {
        public String mWWW;

        public JFG_HISTORY_VIDEO_RSP(String cid, String www) {
            super(JFG_MSG_HISTORY_VIDEO_RSP, cid);
            mWWW = www;

            putBuffer();
        }

        public JFG_HISTORY_VIDEO_RSP(byte[] buffer) {
            super(buffer);
            /*
             * byte[] b = new byte[2]; mBuffer.get(b, 0, 2); short len =
             * bytesToShort(b); byte[] tmp = new byte[len]; if (len >
             * mBuffer.remaining()) { mWWW = null; } else { mBuffer.get(tmp, 0,
             * len); mWWW = new String(tmp); }
             */

            int len = mBuffer.remaining();
            byte[] tmp = new byte[len];
            mBuffer.get(tmp, 0, len);
            mWWW = new String(tmp).trim();
        }

        public final String getUrl() {
            return mWWW;
        }

        private void putBuffer() {
            if (mBuffer.capacity() < 2 + 2 + size32 + 2 + mWWW.length()) {
                mBuffer = ByteBuffer.allocate(2 + 2 + size32 + 2 + mWWW.getBytes().length);
            }
            super.putBuffer();
            // mBuffer.put(shortToBytes((short)mWWW.getBytes().length), 0, 2);
            mBuffer.put(mWWW.getBytes(), 0, mWWW.getBytes().length);
        }
    }

    public class JFG_WIFI_SWITCH_REQ extends JFGCFG_HEADER {
        public int mSwitch;

        public JFG_WIFI_SWITCH_REQ(String cid, int s) {
            super(JFG_MSG_WIFI_SWITCH_REQ, cid);
            mSwitch = s;
            putBuffer();
        }

        public JFG_WIFI_SWITCH_REQ(byte[] buffer) {
            super(buffer);
            byte[] b = new byte[4];
            int len = 4;
            if (4 > mBuffer.remaining()) {
                len = 4 - mBuffer.remaining();
            }
            mBuffer.get(b, 0, len);
            mSwitch = bytesToInt(b);
        }

        private void putBuffer() {
            super.putBuffer();
            mBuffer.put(intToBytes(mSwitch));
        }
    }

    public class JFG_WIFI_SWITCH_RSP extends JFG_RESPONSE {
        public JFG_WIFI_SWITCH_RSP(String cid, int err) {
            super(JFG_MSG_WIFI_SWITCH_RSP, cid, err);
        }

        public JFG_WIFI_SWITCH_RSP(byte[] buffer) {
            super(buffer);
        }
    }

    public class JFG_WIFI_STATE_REQ extends JFGCFG_HEADER {
        public JFG_WIFI_STATE_REQ(String cid) {
            super(JFG_MSG_WIFI_STATE_REQ, cid);
        }

        public JFG_WIFI_STATE_REQ(byte[] buffer) {
            super(buffer);
        }
    }

    public class JFG_SET_SERVER extends JFGCFG_HEADER {
        public String mAddress;
        public String mMsgPackPort;
        public String mPort;

        public JFG_SET_SERVER(String cid, String addr, String msgPackPort, String port) {
            super(JFG_MSG_SET_SERVER, cid);
            mAddress = addr;
            mMsgPackPort = msgPackPort;
            mPort = port;
            putBuffer();
        }

        public JFG_SET_SERVER(byte[] buf) {
            super(buf);

            int len = size32;
            if (size32 > mBuffer.remaining()) {
                len = mBuffer.remaining();
            }

            byte[] tmp = new byte[size32];
            mBuffer.get(tmp, 0, len);
            mAddress = new String(tmp).trim();

            tmp = new byte[size8];
            len = size8;
            if (size8 > mBuffer.remaining()) {
                len = mBuffer.remaining();
            }
            mBuffer.get(tmp, 0, len);
            mMsgPackPort = new String(tmp).trim();

            len = size8;
            if (size8 > mBuffer.remaining()) {
                len = mBuffer.remaining();
            }
            mBuffer.get(tmp, 0, len);
            mPort = new String(tmp).trim();
        }

        private void putBuffer() {
            super.putBuffer();
            int len = 0;
            mBuffer.put(mAddress.getBytes(), 0, mAddress.getBytes().length);
            len = size32 - mAddress.getBytes().length;
            if (len > 0) {
                mBuffer.position(mBuffer.position() + len);
            }

            mBuffer.put(mMsgPackPort.getBytes(), 0, mMsgPackPort.getBytes().length);
            len = size8 - mMsgPackPort.getBytes().length;
            if (len > 0) {
                mBuffer.position(mBuffer.position() + len);
            }

            mBuffer.put(mPort.getBytes(), 0, mPort.getBytes().length);
            len = size8 - mPort.getBytes().length;
            if (len > 0) {
                mBuffer.position(mBuffer.position() + len);
            }
        }
    }

    public class JFG_SET_TIMEZONE extends JFGCFG_HEADER {
        public int mTimezone;

        public JFG_SET_TIMEZONE(String cid, int timezone) {
            super(JFG_MSG_SET_TIMEZONE, cid);
            mTimezone = timezone;
            putBuffer();
        }

        public JFG_SET_TIMEZONE(byte[] buf) {
            super(buf);

            byte[] b = new byte[4];
            int len = 4;
            if (4 > mBuffer.remaining()) {
                len = 4 - mBuffer.remaining();
            }
            mBuffer.get(b, 0, len);
            mTimezone = bytesToInt(b);

        }

        private void putBuffer() {
            super.putBuffer();
            mBuffer.put(intToBytes(mTimezone));

        }
    }


    /**
     * 2014-12-09 hunt
     *
     * @author cylan
     */
    public class JFG_WIFI_OPERATION_RESULT extends JFGCFG_HEADER {
        public int operation_type = -1;

        public JFG_WIFI_OPERATION_RESULT(byte[] buffer) {
            super(buffer);
            // TODO Auto-generated constructor stub
        }

        /**
         * @param cid
         * @param type wifi type
         */
        public JFG_WIFI_OPERATION_RESULT(String cid, int type) {
            super(JFG_MSG_WIFI_OPERATION_RESULT, cid);
            operation_type = type;
            putBuffer();
        }

        private void putBuffer() {
            super.putBuffer();
            mBuffer.put(intToBytes(operation_type));
        }
    }

    public class JFG_SET_LANGUAGE extends JFGCFG_HEADER {
        public int mLanguage;

        public JFG_SET_LANGUAGE(String cid, int language) {
            super(JFG_MSG_SET_LANGUAGE, cid);
            mLanguage = language;
            putBuffer();
        }

        public JFG_SET_LANGUAGE(byte[] buf) {
            super(buf);

            byte[] b = new byte[4];
            int len = 4;
            if (4 > mBuffer.remaining()) {
                len = 4 - mBuffer.remaining();
            }
            mBuffer.get(b, 0, len);
            mLanguage = bytesToInt(b);

        }

        private void putBuffer() {
            super.putBuffer();
            mBuffer.put(intToBytes(mLanguage));

        }
    }

    public class JFG_F_PING extends JFGCFG_HEADER {
        public JFG_F_PING(String cid) {
            super(JFG_MSG_F_PING, cid);
        }

        public JFG_F_PING(byte[] buffer) {
            super(buffer);
        }
    }


    public class JFG_F_PONG extends JFGCFG_HEADER {
        public String mac;//18
        public String version;//16
        public String mIp;
        public int mPort;

        public JFG_F_PONG(byte[] buffer) {
            super(buffer);
            try {
                mIp = datagramPacket.getAddress().getHostAddress();
            } catch (Exception e) {
                DswLog.ex(e.toString());
            }

            mPort = datagramPacket.getPort();

            byte[] macTmp = new byte[18];
            int macLen = 18;
            if (18 > mBuffer.remaining()) {
                macLen = mBuffer.remaining();
            }
            mBuffer.get(macTmp, 0, macLen);
            mac = new String(macTmp).trim();

            byte[] tmp = new byte[size16];
            int len = size16;
            if (size16 > mBuffer.remaining()) {
                len = mBuffer.remaining();
            }
            mBuffer.get(tmp, 0, len);
            version = new String(tmp).trim();

        }

        @Override
        public boolean equals(Object o) {
            return ((JFG_F_PONG) o).mCid.equals(this.mCid);
        }
    }

    public class JFG_F_UPGARDE extends JFGCFG_HEADER {
        public String mMac;//18
        public String mUrl;//256


        public JFG_F_UPGARDE(String cid, String mac, String url) {
            super(JFG_MSG_F_UPGARDE, cid);
            this.mMac = mac;
            this.mUrl = url;
            putBuffer();
        }

        public JFG_F_UPGARDE(byte[] buffer) {
            super(buffer);

            byte[] macTmp = new byte[256];
            int macLen = 256;
            if (256 > mBuffer.remaining()) {
                macLen = mBuffer.remaining();
            }
            mBuffer.get(macTmp, 0, macLen);
            mUrl = new String(macTmp).trim();
        }

        private void putBuffer() {
            super.putBuffer();
            int len = 0;
            mBuffer.put(mMac.getBytes(), 0, mMac.getBytes().length);
            len = 18 - mMac.getBytes().length;
            if (len > 0) {
                mBuffer.position(mBuffer.position() + len);
            }

            mBuffer.put(mUrl.getBytes(), 0, mUrl.getBytes().length);

        }
    }

    public class JFG_F_ACK extends JFGCFG_HEADER {

        public String mMac;//18
        public short mAckmsg;
        public int mResult;

        public JFG_F_ACK(byte[] buffer) {
            super(buffer);

            byte[] charTmp = new byte[18];
            if (mBuffer.remaining() >= 18) {
                mBuffer.get(charTmp, 0, 18);
                mMac = new String(charTmp).trim();
            }


            byte[] shortTmp = new byte[2];
            if (mBuffer.remaining() >= 2) {
                mBuffer.get(shortTmp, 0, 2);
                mAckmsg = bytesToShort(shortTmp);
            }


            byte[] b = new byte[4];
            int len = 4;
            if (4 > mBuffer.remaining()) {
                len = 4 - mBuffer.remaining();
            }
            mBuffer.get(b, 0, len);
            mResult = bytesToInt(b);


        }
    }

    public class JFG_F_PLAY extends JFGCFG_HEADER {
        public String mMac;//18
        public String mUrl;//256


        public JFG_F_PLAY(String cid, String mac, String url) {
            super(JPG_MSG_F_PLAY, cid);
            this.mMac = mac;
            this.mUrl = url;
            putBuffer();
        }

        public JFG_F_PLAY(byte[] buffer) {
            super(buffer);

            byte[] macTmp = new byte[18];
            int macLen = 18;
            if (18 > mBuffer.remaining()) {
                macLen = mBuffer.remaining();
            }
            mBuffer.get(macTmp, 0, macLen);
            mMac = new String(macTmp).trim();

            byte[] urlTmp = new byte[256];
            int urlLength = 256;
            if (256 > mBuffer.remaining()) {
                urlLength = mBuffer.remaining();
            }
            mBuffer.get(urlTmp, 0, urlLength);
            mUrl = new String(urlTmp).trim();

        }

        private void putBuffer() {
            super.putBuffer();
            int len = 0;
            mBuffer.put(mMac.getBytes(), 0, mMac.getBytes().length);
            len = 18 - mMac.getBytes().length;
            if (len > 0) {
                mBuffer.position(mBuffer.position() + len);
            }
            mBuffer.put(mUrl.getBytes(), 0, mUrl.getBytes().length);
            if (256 > mUrl.length()) {
                mBuffer.position(mBuffer.position() + 256 - mUrl.length());
            }
        }
    }


    public class JFG_F_STOP extends JFGCFG_HEADER {
        public String mMac;//18


        public JFG_F_STOP(String cid, String mac) {
            super(JPG_MSG_F_STOP, cid);
            this.mMac = mac;
            putBuffer();
        }

        public JFG_F_STOP(byte[] buffer) {
            super(buffer);

            byte[] macTmp = new byte[18];
            int macLen = 18;
            if (18 > mBuffer.remaining()) {
                macLen = mBuffer.remaining();
            }
            mBuffer.get(macTmp, 0, macLen);
            mMac = new String(macTmp).trim();
        }

        private void putBuffer() {
            super.putBuffer();
            int len = 0;
            mBuffer.put(mMac.getBytes(), 0, mMac.getBytes().length);
            len = 18 - mMac.getBytes().length;
            if (len > 0) {
                mBuffer.position(mBuffer.position() + len);
            }
        }
    }

    private void print(byte[] data, String text) {

        JFGCFG_HEADER header = new JFGCFG_HEADER(data);
        if (header.mMagic == JFG_MSG_MAGIC) {
            switch (header.mMsgid) {
                case JFG_MSG_PING:
                case JFG_MSG_SCAN_WIFI_REQ:
                case JFG_MSG_HISTORY_VIDEO_REQ:
                case JFG_MSG_WIFI_STATE_REQ:
                case JFG_MSG_F_PING:
                case JPG_MSG_BELL_PRESS:
                    DswLog.i(text + " msgid=" + header.mMsgid + " cid=" + header.mCid + " ");
                    break;
                case JFG_MSG_PONG:
                    JFG_PONG jfg_pong = new JFG_PONG(data);
                    DswLog.i(text + " msgid=" + jfg_pong.mMsgid + " cid=" + jfg_pong.mCid
                            + " net=" + jfg_pong.mNet + " ");
                    break;
                case JFG_MSG_SET_WIFI_RSP:
                case JFG_MSG_AP_OFF_RSP:
                case JFG_MSG_WIFI_SWITCH_RSP: {
                    JFG_RESPONSE rsp = new JFG_RESPONSE(data);
                    DswLog.i(text + " msgid=" + rsp.mMsgid + " cid=" + rsp.mCid + " error="
                            + rsp.mError + " ");
                }
                break;
                case JFG_MSG_SCAN_WIFI_RSP:
                    JFG_SCAN_WIFI_RSP jfg_scan_wifi = new JFG_SCAN_WIFI_RSP(data);
                    DswLog.i(text + " msgid=" + jfg_scan_wifi.mMsgid + " cid="
                            + jfg_scan_wifi.mCid + " index=" + jfg_scan_wifi.mIndex + " rssi="
                            + jfg_scan_wifi.mRssi + " security=" + jfg_scan_wifi.mSecurity
                            + " ssid=" + jfg_scan_wifi.mSSID + " ");
                    break;
                case JFG_MSG_SET_WIFI_REQ:
                    JFG_SET_WIFI_REQ jfg_set_wifi = new JFG_SET_WIFI_REQ(data);
                    DswLog.i(text + " msgid=" + jfg_set_wifi.mMsgid + " cid="
                            + jfg_set_wifi.mCid + " security=" + jfg_set_wifi.mSecurity + " ssid="
                            + jfg_set_wifi.mSSID + " key=" + jfg_set_wifi.mKey + " accout="
                            + jfg_set_wifi.mAccout + " ");
                    break;
                case JFG_MSG_AP_OFF_REQ:
                    JFG_AP_OFF_REQ jfg_set_cid_r = new JFG_AP_OFF_REQ(data);
                    DswLog.i(text + " msgid=" + jfg_set_cid_r.mMsgid + " cid="
                            + jfg_set_cid_r.mCid + " ncid=" + jfg_set_cid_r.mNCid + " ");
                    break;
                case JFG_MSG_HISTORY_VIDEO_RSP:
                    JFG_HISTORY_VIDEO_RSP jfg_video_r = new JFG_HISTORY_VIDEO_RSP(data);
                    DswLog.i(text + " msgid=" + jfg_video_r.mMsgid + " cid="
                            + jfg_video_r.mCid + " www=" + jfg_video_r.mWWW + " ");
                    break;
                case JFG_MSG_WIFI_SWITCH_REQ: {
                    JFG_WIFI_SWITCH_REQ req = new JFG_WIFI_SWITCH_REQ(data);
                    DswLog.i(text + " msgid=" + req.mMsgid + " cid=" + req.mCid + " switch="
                            + req.mSwitch + " ");
                }
                break;
                case JFG_MSG_SET_SERVER: {
                    JFG_SET_SERVER req = new JFG_SET_SERVER(data);
                    DswLog.i(text + " msgid=" + req.mMsgid + " cid=" + req.mCid + " addr="
                            + req.mAddress + " port=" + req.mPort + " ");
                }
                break;
                case JFG_MSG_SET_TIMEZONE: {
                    JFG_SET_TIMEZONE req = new JFG_SET_TIMEZONE(data);
                    DswLog.i(text + " msgid=" + req.mMsgid + " cid=" + req.mCid + " timezone="
                            + req.mTimezone);
                }
                break;
                case JFG_MSG_WIFI_OPERATION_RESULT:
                    JFG_WIFI_OPERATION_RESULT res = new JFG_WIFI_OPERATION_RESULT(data);
                    DswLog.i(text + " msgid=" + res.mMsgid + " cid=" + res.mCid + " operation_type="
                            + res.operation_type);
                    break;
                case JFG_MSG_SET_LANGUAGE: {
                    JFG_SET_LANGUAGE req = new JFG_SET_LANGUAGE(data);
                    DswLog.i(text + " msgid=" + req.mMsgid + " cid=" + req.mCid + " language="
                            + req.mLanguage);
                }
                break;
                case JFG_MSG_F_PONG: {
                    JFG_F_PONG req = new JFG_F_PONG(data);
                    DswLog.i(text + " msgid=" + req.mMsgid + " cid=" + req.mCid + " mac="
                            + req.mac + " version=" + req.version + " ip=" + req.mIp + " port=" + req.mPort);
                }
                break;
                case JFG_MSG_F_UPGARDE: {
                    JFG_F_UPGARDE req = new JFG_F_UPGARDE(data);
                    DswLog.i(text + " msgid=" + req.mMsgid + " cid=" + req.mCid + " url="
                            + req.mUrl);
                }
                break;
                case JFG_MSG_F_ACK: {
                    JFG_F_ACK req = new JFG_F_ACK(data);
                    DswLog.i(text + " msgid=" + req.mMsgid + " cid=" + req.mCid + " mMac=" + req.mMac + " mAckmsg="
                            + req.mAckmsg + " mResult=" + req.mResult);
                }
                break;
                case JPG_MSG_F_PLAY: {
                    JFG_F_PLAY req = new JFG_F_PLAY(data);
                    DswLog.i(text + " msgid=" + req.mMsgid + " cid=" + req.mCid + " mMac=" + req.mMac + " url="
                            + req.mUrl);
                }
                break;
                case JPG_MSG_F_STOP: {
                    JFG_F_STOP req = new JFG_F_STOP(data);
                    DswLog.i(text + " msgid=" + req.mMsgid + " cid=" + req.mCid + " mMac=" + req.mMac);
                }
                break;
                default:
                    DswLog.i(text + " err id:" + header.mMsgid + " cid=" + header.mCid + " ");
                    break;
            }
        } else {
            DswLog.i(text + new String(data).trim() + " ");
        }
    }


    private String mCid = null;


    public void setCid(String cid) {
        this.mCid = cid;
    }

    //for control loopfping
    private boolean isLoop = true;

    public void setLoop(boolean loop) {
        isLoop = loop;
    }

    public void sendLoopFPing() {
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isLoop) {
                        sendFPing();
                        Thread.sleep(10000);
                    }
                } catch (InterruptedException e) {
                    DswLog.ex(e.toString());
                }

            }
        });
    }

    public void sendFPing(String... params) {
        try {
            ClientUDP.JFG_F_PING jfgPing = ClientUDP.getInstance().new JFG_F_PING(params.length > 0 ? params[0] : "");
            ClientUDP.getInstance().send(jfgPing.getBytes(), InetAddress.getByName(INETADDRESS), PORT);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }

    public void sendFPlay(JFG_F_PONG pong, String randomPort) {
        try {
            ClientUDP.JFG_F_PLAY jfgfPlay = ClientUDP.getInstance().new JFG_F_PLAY(mCid, pong.mac, String.format("%s:8880", randomPort));
            DswLog.d("udp send fplay ip-->" + pong.mIp + " port-->" + pong.mPort);
            ClientUDP.getInstance().send(jfgfPlay.getBytes(), InetAddress.getByName(pong.mIp), pong.mPort);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }


    public void sendPing() {
        try {
            ClientUDP.JFG_PING jfgPing = ClientUDP.getInstance().new JFG_PING(mCid);
            ClientUDP.getInstance().send(jfgPing.getBytes(), InetAddress.getByName(INETADDRESS), PORT);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }

    public void sendServer(String ip, String port) {
        try {
            ClientUDP.JFG_SET_SERVER jmss = ClientUDP.getInstance().new JFG_SET_SERVER(mCid, ip, port, String.valueOf(Constants.WEB_PORT));
            ClientUDP.getInstance().send(jmss.getBytes(), InetAddress.getByName(INETADDRESS), PORT);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }

    }


    public void sendTimezone() {
        TimeZone mTimeZone = TimeZone.getDefault();
        Integer tz = mTimeZone.getRawOffset() / 1000;
        try {
            ClientUDP.JFG_SET_TIMEZONE jmss = ClientUDP.getInstance().new JFG_SET_TIMEZONE(mCid, tz);
            ClientUDP.getInstance().send(jmss.getBytes(), InetAddress.getByName(INETADDRESS), PORT);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }


    public void sendLanguage(int languagetype) {
        try {
            if (mCid.length() == 12 && mCid.startsWith("3")) {
                ClientUDP.JFG_SET_LANGUAGE jmss = ClientUDP.getInstance().new JFG_SET_LANGUAGE(mCid, languagetype);
                ClientUDP.getInstance().send(jmss.getBytes(), InetAddress.getByName(INETADDRESS), PORT);
            }
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }


    public void sendUpgrade(String url) {
        try {
            ClientUDP.JFG_F_UPGARDE jfgPing = ClientUDP.getInstance().new JFG_F_UPGARDE(mCid, "", url);
            ClientUDP.getInstance().send(jfgPing.getBytes(), InetAddress.getByName(INETADDRESS), PORT);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }

    public void toSendWifi(short type, String ssid, String pwd, String account) {
        try {
            ClientUDP.JFG_SET_WIFI_REQ jfgSetWifiReq = ClientUDP.getInstance().new JFG_SET_WIFI_REQ(mCid, type, ssid, pwd, "");
            ClientUDP.getInstance().send(jfgSetWifiReq.getBytes(), InetAddress.getByName(INETADDRESS), PORT);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }

    public void sendFStop(JFG_F_PONG pong) {
        try {
            ClientUDP.JFG_F_STOP jfgSendFStop = ClientUDP.getInstance().new JFG_F_STOP(mCid, pong.mac);
            DswLog.v("udp send fstop ip-->" + pong.mIp + " port-->" + pong.mPort);
            ClientUDP.getInstance().send(jfgSendFStop.getBytes(), InetAddress.getByName(pong.mIp), pong.mPort);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }


}
