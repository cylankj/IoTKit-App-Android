package com.cylan.publicApi;


import android.content.Context;

import com.google.gson.Gson;

import org.msgpack.MessagePack;
import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.IOException;

import com.cylan.support.DswLog;

/**
 * Created by liuzhiping on 2015/3/23.
 */
public class MsgpackMsg {
    public final static int ID_LOGIN_SERVER = 1;
    public final static int ID_KEEPALIVE = 2; // 心跳
    public final static int ID_KEEPALIVE_RSP = 3; // 心跳回应
    public final static int ID_SETUP = 4;
    public final static int ID_SETUP_RSP = 5;
    public final static int ID_CONNECT = 6;
    public final static int ID_DISCONNECT = 7;
    public final static int ID_NET_CHECK_COMPLETE = 8;
    public final static int ID_AUDIO_CONTROL = 9;
    public final static int ID_REVERSE_CALL = 10; //反向呼叫
    public final static int ID_HISTORY = 11;
    public final static int ID_HISTORY_SETUP = 12;
    public final static int ID_HISTORY_INFO = 13;
    public final static int ID_HISTORY_LIST = 14;
    public final static int ID_HISTORY_LIST_RSP = 15;
    public final static int BELL_PRESS = 16;
    public final static int SERVER_CONFIG = 18;
    public final static int ID_RELAY_SERVER = 19;
    public final static int ID_RELAY_MASK_INFO_REQ = 20;
    public final static int ID_RELAY_MASK_INFO_RSP = 21;
    public final static int ID_SETUP_V2 = 22;
    public final static int ID_HISTORY_SETUP_V2 = 23;
    public final static int ID_SDP = 24;
    public final static int ID_REVERSE_CALL_V2 = 25;
    public final static int ID_BELL_CONNECTED = 26;
    public final static int ID_RESUME = 27; //收到后，重新login
    public final static int REPORT_DATAPACKET_INFO = 28;
    public final static int EFAML_CALL_CLIENT = 29;
    public final static int EFAML_CALL_CLIENT_STATUS = 30;
    public final static int EFAML_CALL_CANCEL = 31;
    public final static int CLIENT_CALL_EFAML_STATUS = 32;

    /**
     * ******************* cameara **************************
     */
    public final static int CID_LOGIN = 100;
    public final static int CID_LOGIN_RSP = 101;
    public final static int LOGOUT = 102;
    public final static int LOGOUT_RSP = 103;
    public final static int CID_SET = 104;
    public final static int CID_SET_RSP = 105;
    public final static int CHECKVERSION = 106;
    public final static int CHECKVERSION_RSP = 107;
    public final static int REBOOT = 108;
    public final static int UPGRADE = 109;
    public final static int REPORT_TEMP = 110;
    public final static int CID_STATUS_SDCARD = 111;
    public final static int CID_STATUS_SDCARD_RSP = 112;
    public final static int CID_PUSH = 113;
    public final static int REPORT_SDCARD = 114;
    public final static int CID_PUSH_ACCOUNT = 115;
    public final static int CID_PUSH_SERVER = 116;
    public final static int CID_PUSH_SCENE_MODE = 117;
    public final static int CID_SDCARD_FORMAT = 118;
    public final static int CID_SDCARD_FORMAT_RSP = 119;
    public final static int CID_PUSH_GET_LOG = 120;
    public final static int LOW_BATTERY_WARNNING = 121;
    public final static int CID_REPORT_RELAYMARK = 124;
    public final static int CID_PUSH_OSS_CONFIG = 128;

    /**
     * ****************************************************************
     */
    public final static int ID_JSON = 200;
    public final static int ID_RTMP_CLIENT_ADDR_REQ = 201;
    public final static int ID_RTMP_CAMERA_ADDR_IND = 202;
    public final static int ID_RTMP_CAMERA_ADDR_REQ = 203;
    public final static int ID_RTMP_SERVER_ADDR_IND = 204;
    public final static int CLIENT_ID = 1000;
    public final static int CLIENT_GET_CODE = CLIENT_ID + 1;
    public final static int CLIENT_GET_CODE_RSP = CLIENT_ID + 2;
    public final static int CLIENT_CHECKCODE_REQ = CLIENT_ID + 3;
    public final static int CLIENT_CHECKCODE_RSP = CLIENT_ID + 4;
    public final static int CLIENT_REGISTER_REQ = CLIENT_ID + 5;
    public final static int CLIENT_REGISTER_RSP = CLIENT_ID + 6;
    public final static int CLIENT_SETPASS_REQ = CLIENT_ID + 7;
    public final static int CLIENT_SETPASS_RSP = CLIENT_ID + 8;
    public final static int CLIENT_SYNC_LOGOUT = CLIENT_ID + 9;
    public final static int CLIENT_CHANGEPASS_REQ = CLIENT_ID + 10;
    public final static int CLIENT_CHANGEPASS_RSP = CLIENT_ID + 11;
    public final static int CLIENT_LOGIN_REQ = CLIENT_ID + 12;
    public final static int CLIENT_LOGIN_RSP = CLIENT_ID + 13;
    public final static int CLIENT_RELOGIN_REQ = CLIENT_ID + 14;
    public final static int CLIENT_RELOGIN_RSP = CLIENT_ID + 15;
    public final static int CLIENT_BINDCID_REQ = CLIENT_ID + 16;
    public final static int CLIENT_BINDCID_RSP = CLIENT_ID + 17;
    public final static int CLIENT_UNBINDCID_REQ = CLIENT_ID + 18;
    public final static int CLIENT_UNBINDCID_RSP = CLIENT_ID + 19;
    public final static int CLIENT_SETCIDALIAS_REQ = CLIENT_ID + 20;
    public final static int CLIENT_SETCIDALIAS_RSP = CLIENT_ID + 21;
    public final static int CLIENT_GETCIDALIAS_REQ = CLIENT_ID + 22;
    public final static int CLIENT_GETCIDALIAS_RSP = CLIENT_ID + 23;
    public final static int CLIENT_GETACCOUNTINFO_REQ = CLIENT_ID + 24;
    public final static int CLIENT_GETACCOUNTINFO_RSP = CLIENT_ID + 25;
    public final static int CLIENT_SETACCOUNTINFO_REQ = CLIENT_ID + 26;
    public final static int CLIENT_SETACCOUNTINFO_RSP = CLIENT_ID + 27;
    public final static int CLIENT_CIDLIST_REQ = CLIENT_ID + 28;
    public final static int CLIENT_CIDLIST_RSP = CLIENT_ID + 29;
    public final static int CLIENT_DELETESCENE_REQ = CLIENT_ID + 30;
    public final static int CLIENT_DELETESCENE_RSP = CLIENT_ID + 31;
    public final static int CLIENT_ENABLESCENE_REQ = CLIENT_ID + 32;
    public final static int CLIENT_ENABLESCENE_RSP = CLIENT_ID + 33;
    public final static int CLIENT_MSGCOUNT_REQ = CLIENT_ID + 34;
    public final static int CLIENT_MSGCOUNT_RSP = CLIENT_ID + 35;
    public final static int CLIENT_MSGLIST_REQ = CLIENT_ID + 36;
    public final static int CLIENT_MSGLIST_RSP = CLIENT_ID + 37;
    public final static int CLIENT_MSGCLEAR_REQ = CLIENT_ID + 38;
    public final static int CLIENT_MSGCLEAR_RSP = CLIENT_ID + 39;
    public final static int CLIENT_MSGIGNORE_REQ = CLIENT_ID + 40;
    public final static int CLIENT_MSGIGNORE_RSP = CLIENT_ID + 41;
    public final static int CLIENT_MSGSYSTEM_REQ = CLIENT_ID + 42;
    public final static int CLIENT_MSGSYSTEM_RSP = CLIENT_ID + 43;
    public final static int CLIENT_MSGDELETE_REQ = CLIENT_ID + 44;
    public final static int CLIENT_MSGDELETE_RSP = CLIENT_ID + 45;
    public final static int CLIENT_SHARE_REQ = CLIENT_ID + 46;
    public final static int CLIENT_SHARE_RSP = CLIENT_ID + 47;
    public final static int CLIENT_UNSHARE_REQ = CLIENT_ID + 48;
    public final static int CLIENT_UNSHARE_RSP = CLIENT_ID + 49;
    public final static int CLIENT_SHARELIST_REQ = CLIENT_ID + 50;
    public final static int CLIENT_SHARELIST_RSP = CLIENT_ID + 51;
    public final static int CLIENT_LOGINBYQQ_REQ = CLIENT_ID + 52;
    public final static int CLIENT_LOGINBYQQ_RSP = CLIENT_ID + 53;
    public final static int CLIENT_LOGINBYSINA_REQ = CLIENT_ID + 54;
    public final static int CLIENT_LOGINBYSINA_RSP = CLIENT_ID + 55;
    public final static int CLIENT_CIDSET_REQ = CLIENT_ID + 56;
    public final static int CLIENT_CIDSET_RSP = CLIENT_ID + 57;
    public final static int CLIENT_CIDGET_REQ = CLIENT_ID + 58;
    public final static int CLIENT_CIDGET_RSP = CLIENT_ID + 59;
    public final static int CLIENT_FORGETPASSBYEMAIL_REQ = CLIENT_ID + 60;
    public final static int CLIENT_FORGETPASSBYEMAIL_RSP = CLIENT_ID + 61;
    public final static int CLIENT_DEMOLIST_REQ = CLIENT_ID + 62;
    public final static int CLIENT_DEMOLIST_RSP = CLIENT_ID + 63;
    public final static int CLIENT_SYNC_CIDONLINE = CLIENT_ID + 64;
    public final static int CLIENT_SYNC_CIDOFFLINE = CLIENT_ID + 65;
    public final static int CLIENT_PUSH = CLIENT_ID + 66;
    public final static int CLIENT_SYNC_SDCARD = REPORT_SDCARD;
    public final static int CLIENT_SDCARD_FORMAT = CID_SDCARD_FORMAT;
    public final static int CLIENT_SDCARD_FORMAT_ACK = CID_SDCARD_FORMAT_RSP; //无ret,msg响应
    public final static int CLIENT_STATUS = CID_STATUS_SDCARD;
    public final static int CLIENT_STATUS_ACK = CLIENT_ID + 67;   //无ret,msg响应
    public final static int CLIENT_SYNC_URL = CLIENT_ID + 68;
    public final static int CLIENT_BELL_CALL_LIST_REQ = CLIENT_ID + 69;
    public final static int CLIENT_BELL_CALL_LIST_RSP = CLIENT_ID + 70;
    public final static int CLIENT_EFAML_GET_ALARM_REQ = CLIENT_ID + 71; //efamily
    public final static int CLIENT_EFAML_GET_ALARM_RSP = CLIENT_ID + 72;
    public final static int CLIENT_EFAML_SET_ALARM_REQ = CLIENT_ID + 73;
    public final static int CLIENT_EFAML_SET_ALARM_RSP = CLIENT_ID + 74;
    public final static int CLIENT_EFAML_LIST_REQ = CLIENT_ID + 75;
    public final static int CLIENT_EFAML_LIST_RSP = CLIENT_ID + 76;
    public final static int CLIENT_EFAML_VOICEMSG_LIST_REQ = CLIENT_ID + 77;
    public final static int CLIENT_EFAML_VOICEMSG_LIST_RSP = CLIENT_ID + 78;
    public final static int CLIENT_EFAML_CLEAR_VOICEMSG_REQ = CLIENT_ID + 79;
    public final static int CLIENT_EFAML_CLEAR_VOICEMSG_RSP = CLIENT_ID + 80;
    public final static int CLIENT_EFAML_MSG_SAFE_LIST_REQ = CLIENT_ID + 81;
    public final static int CLIENT_EFAML_MSG_SAFE_LIST_RSP = CLIENT_ID + 82;
    public final static int CLIENT_SET_TOKEN_REQ = CLIENT_ID + 83;
    public final static int CLIENT_SET_TOKEN_RSP = CLIENT_ID + 84;
    public final static int CLIENT_BELL_CALL_DELETE_REQ = CLIENT_ID + 85;
    public final static int CLIENT_BELL_CALL_DELETE_RSP = CLIENT_ID + 86;
    public final static int CLIENT_HAS_MOBILE_REQ = CLIENT_ID + 87;
    public final static int CLIENT_HAS_MOBILE_RSP = CLIENT_ID + 88;
    public final static int CLIENT_GET_WARN_CONFIG_REQ = CLIENT_ID + 89;
    public final static int CLIENT_GET_WARN_CONFIG_RSP = CLIENT_ID + 90;
    public final static int CLIENT_LOGOUT = CLIENT_ID + 91;
    public final static int CLIENT_MAG_STATUS_LIST_REQ = CLIENT_ID + 92;
    public final static int CLIENT_MAG_STATUS_LIST_RSP = CLIENT_ID + 93;
    public final static int CLIENT_PUSH_SIMPLE_NOTICE = CLIENT_ID + 94;//type=1,2
    public final static int CLIENT_EFAML_SET_BELL_REQ = CLIENT_ID + 95;
    public final static int CLIENT_EFAML_SET_BELL_RSP = CLIENT_ID + 96;
    public final static int CLIENT_EFAML_GET_BELLS_REQ = CLIENT_ID + 97;
    public final static int CLIENT_EFAML_GET_BELLS_RSP = CLIENT_ID + 98;
    public final static int CLIENT_POST = CLIENT_ID + 99;
    public final static int CLIENT_EFAML_MSG_LIST_REQ = CLIENT_ID + 100;
    public final static int CLIENT_EFAML_MSG_LIST_RSP = CLIENT_ID + 101;
    public final static int CLIENT_MAG_SET_WARN_REQ = CLIENT_ID + 102;
    public final static int CLIENT_MAG_SET_WARN_RSP = CLIENT_ID + 103;
    public final static int CLIENT_MAG_GET_WARN_REQ = CLIENT_ID + 104;
    public final static int CLIENT_MAG_GET_WARN_RSP = CLIENT_ID + 105;
    public final static int CLIENT_MAG_GET_INFO = CLIENT_ID + 106;
    public final static int CLIENT_DEL_EFAML_MSG_REQ = CLIENT_ID + 108;
    public final static int CLIENT_DEL_EFAML_MSG_RSP = CLIENT_ID + 109;

    /**
     * *********************** efamily id *****************************
     */
    public final static int EFAML_ID = 2000;
    public final static int EFAML_ACCOUNT_LIST_REQ = EFAML_ID + 1;
    public final static int EFAML_ACCOUNT_LIST_RSP = EFAML_ID + 2;
    public final static int EFAML_PUSH_ALARM = EFAML_ID + 3;
    public final static int EFAML_REPORT_MAGNET_REQ = EFAML_ID + 4;
    public final static int EFAML_REPORT_MAGNET_RSP = EFAML_ID + 5;
    public final static int EFAML_REPORT_IR_REQ = EFAML_ID + 6;
    public final static int EFAML_REPORT_IR_RSP = EFAML_ID + 7;
    public final static int EFAML_REPORT_TH_REQ = EFAML_ID + 8;
    public final static int EFAML_REPORT_TH_RSP = EFAML_ID + 9;
    public final static int EFAML_VOICEMSG_LIST_REQ = EFAML_ID + 10;
    public final static int EFAML_VOICEMSG_LIST_RSP = EFAML_ID + 11;
    public final static int EFAML_VOICEMSG_PLAY_REQ = EFAML_ID + 12;
    public final static int EFAML_VOICEMSG_PLAY_RSP = EFAML_ID + 13;
    public final static int EFAML_BINDCIDLIST_REQ = EFAML_ID + 14;
    public final static int EFAML_BINDCIDLIST_RSP = EFAML_ID + 15;

    @Message
    public static class MsgHeader {
        public MsgHeader() {
            msgId = -1;
            caller = session;
            callee = "";
        }

        @Index(0)
        public int msgId;
        @Index(1)
        public String caller;
        @Index(2)
        public String callee;
        @Ignore
        private static String session = "";

        @Ignore
        public static void setSession(String session) {
            MsgHeader.session = session;
        }

        @Ignore
        public byte[] toBytes() {
            try {
                MessagePack msgpack = new MessagePack();

                return msgpack.write(this);
            } catch (IOException ex) {
                DswLog.ex(ex.toString());
                return null;
            }
        }

        @Ignore
        public String toString() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }

        @Ignore
        public Object fromBytes(byte[] bytes, Context con) {
            MessagePack msgpack = new MessagePack();
            return null;
        }
    }


}
