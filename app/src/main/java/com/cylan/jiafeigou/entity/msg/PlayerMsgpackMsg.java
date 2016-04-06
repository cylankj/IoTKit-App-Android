package com.cylan.jiafeigou.entity.msg;


import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import cylan.log.DswLog;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.entity.msg.rsp.LoginRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgBellCallListRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgBindCidRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgCidSdcardFormatRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgCidlistRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgClientEfamlGetBellsRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgClientEfamlMsgListRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgClientHasMobileRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgClientMagGetWarnRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgClientMagSetWarnRsq;
import com.cylan.jiafeigou.entity.msg.rsp.MsgClientMagStatusListRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgEfamilyListRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgEfamilyMsgSafeListRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgEfamilyVoicemsgListRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgEnableSceneRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgForgetPassByEmailRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgGetCodeRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgHistoryListRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgMsgClearRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgMsgCountRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgMsgDeleteRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgMsgListRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgMsgSystemRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgRelayMaskInfoRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgSetCidAliasRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgShareListRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgShareRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgUnbindCidRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgUnshareRsp;

import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;

import java.io.IOException;

public class PlayerMsgpackMsg {

    private static PlayerMsgpackMsg instance;

    private String[] strs = null;

    private MsgpackMsg.MsgHeader readHeader(MessagePack msgpack, byte[] bytes, Class c)
            throws IOException {
        RspMsgHeader header = msgpack.read(bytes, RspMsgHeader.class);
        header.msg = strs[header.ret + 1];
        return header.ret == Constants.RETOK ? (MsgpackMsg.MsgHeader) msgpack.read(bytes, c) : header;
    }

    private PlayerMsgpackMsg() {
        if (strs == null) {
            strs = MyApp.getContext().getResources().getStringArray(R.array.error_msg);
        }
    }


    public static PlayerMsgpackMsg getInstance() {
        if (instance == null) {
            synchronized (PlayerMsgpackMsg.class) {
                if (instance == null) {
                    instance = new PlayerMsgpackMsg();
                }
            }
        }
        return instance;
    }


    public MsgpackMsg.MsgHeader fromBytes(byte[] bytes) {
        MessagePack msgpack = new MessagePack();
        MsgpackMsg.MsgHeader header = null;
        try {
            header = msgpack.read(bytes, MsgpackMsg.MsgHeader.class);
            switch (header.msgId) {
                case MsgpackMsg.ID_HISTORY_LIST_RSP: {
                    return msgpack.read(bytes, MsgHistoryListRsp.class);
                }
                case MsgpackMsg.BELL_PRESS: {
                    return msgpack.read(bytes, MsgClientBellPress.class);
                }
                case MsgpackMsg.SERVER_CONFIG: {
                    try {
                        return msgpack.read(bytes, MsgServerConfig.class);
                    } catch (MessageTypeException e) {
                        return msgpack.read(bytes, MsgServers.class);
                    }
                }
                case MsgpackMsg.ID_LOGIN_SERVER: {
                    return msgpack.read(bytes, MsgLoginServers.class);
                }
                case MsgpackMsg.ID_HISTORY_INFO: {
                    return msgpack.read(bytes, MsgHistoryInfo.class);
                }
                case MsgpackMsg.CLIENT_CHANGEPASS_RSP:
                case MsgpackMsg.CLIENT_LOGINBYQQ_RSP:
                case MsgpackMsg.CLIENT_LOGINBYSINA_RSP:
                case MsgpackMsg.CLIENT_SETPASS_RSP:
                case MsgpackMsg.CLIENT_LOGIN_RSP:
                case MsgpackMsg.CLIENT_RELOGIN_RSP:
                case MsgpackMsg.CLIENT_REGISTER_RSP: {
                    return readHeader(msgpack, bytes, LoginRsp.class);
                }
                case MsgpackMsg.CLIENT_GET_CODE_RSP: {
                    return readHeader(msgpack, bytes, MsgGetCodeRsp.class);
                }
                case MsgpackMsg.CLIENT_FORGETPASSBYEMAIL_RSP: {
                    return readHeader(msgpack, bytes, MsgForgetPassByEmailRsp.class);
                }
                case MsgpackMsg.CLIENT_PUSH: {
                    return msgpack.read(bytes, MsgPush.class);
                }
                case MsgpackMsg.CLIENT_SYNC_SDCARD: {
                    return msgpack.read(bytes, MsgSyncSdcard.class);
                }
                case MsgpackMsg.CLIENT_SYNC_CIDOFFLINE: {
                    return msgpack.read(bytes, MsgSyncCidOffline.class);
                }

                case MsgpackMsg.CLIENT_SYNC_CIDONLINE: {
                    return msgpack.read(bytes, MsgSyncCidOnline.class);
                }
                case MsgpackMsg.CLIENT_SETCIDALIAS_RSP: {
                    return readHeader(msgpack, bytes, MsgSetCidAliasRsp.class);
                }
                case MsgpackMsg.CLIENT_CIDLIST_RSP: {
                    return readHeader(msgpack, bytes, MsgCidlistRsp.class);
                }
                case MsgpackMsg.CLIENT_UNBINDCID_RSP: {
                    return readHeader(msgpack, bytes, MsgUnbindCidRsp.class);
                }
                case MsgpackMsg.CLIENT_BINDCID_RSP: {
                    return readHeader(msgpack, bytes, MsgBindCidRsp.class);
                }
                case MsgpackMsg.CLIENT_ENABLESCENE_RSP: {
                    return readHeader(msgpack, bytes, MsgEnableSceneRsp.class);
                }
                case MsgpackMsg.CLIENT_DELETESCENE_RSP: {
                    return readHeader(msgpack, bytes, RspMsgHeader.class);
                }
                case MsgpackMsg.CLIENT_SETACCOUNTINFO_RSP:
                case MsgpackMsg.CLIENT_GETACCOUNTINFO_RSP: {
                    return readHeader(msgpack, bytes, AccountInfo.class);
                }
                case MsgpackMsg.CLIENT_STATUS_ACK: {
                    return msgpack.read(bytes, MsgStatusSdcardToClient.class);
                }
                case MsgpackMsg.CLIENT_SDCARD_FORMAT_ACK: {
                    return msgpack.read(bytes, MsgCidSdcardFormatRsp.class);
                }

                case MsgpackMsg.CLIENT_SHARELIST_RSP: {
                    return readHeader(msgpack, bytes, MsgShareListRsp.class);
                }
                case MsgpackMsg.CLIENT_SHARE_RSP: {
                    return readHeader(msgpack, bytes, MsgShareRsp.class);
                }
                case MsgpackMsg.CLIENT_UNSHARE_RSP: {
                    return readHeader(msgpack, bytes, MsgUnshareRsp.class);
                }
                case MsgpackMsg.CLIENT_EFAML_VOICEMSG_LIST_RSP: {
                    return readHeader(msgpack, bytes, MsgEfamilyVoicemsgListRsp.class);
                }
                case MsgpackMsg.CLIENT_MSGDELETE_RSP: {
                    return readHeader(msgpack, bytes, MsgMsgDeleteRsp.class);
                }
                case MsgpackMsg.CLIENT_MSGCOUNT_RSP: {
                    return readHeader(msgpack, bytes, MsgMsgCountRsp.class);
                }
                case MsgpackMsg.CLIENT_MSGCLEAR_RSP: {
                    return readHeader(msgpack, bytes, MsgMsgClearRsp.class);
                }
                case MsgpackMsg.CLIENT_MSGLIST_RSP: {
                    return readHeader(msgpack, bytes, MsgMsgListRsp.class);
                }
                case MsgpackMsg.CLIENT_EFAML_MSG_SAFE_LIST_RSP: {
                    return readHeader(msgpack, bytes, MsgEfamilyMsgSafeListRsp.class);
                }
                case MsgpackMsg.CLIENT_MSGSYSTEM_RSP: {
                    return readHeader(msgpack, bytes, MsgMsgSystemRsp.class);
                }
                case MsgpackMsg.CLIENT_EFAML_LIST_RSP: {
                    return readHeader(msgpack, bytes, MsgEfamilyListRsp.class);
                }

                case MsgpackMsg.CLIENT_SYNC_LOGOUT: {
                    return msgpack.read(bytes, MsgSyncLogout.class);
                }
                case MsgpackMsg.CLIENT_CIDGET_RSP:
                case MsgpackMsg.CLIENT_CIDSET_RSP:
                case MsgpackMsg.CID_SET_RSP: {
                    return readHeader(msgpack, bytes, MsgCidGetSetParent.class);
                }
                case MsgpackMsg.CLIENT_EFAML_SET_ALARM_RSP:
                case MsgpackMsg.CLIENT_EFAML_GET_ALARM_RSP: {
                    return readHeader(msgpack, bytes, MsgEfamlGetSetAlarmParent.class);
                }
                case MsgpackMsg.ID_RELAY_SERVER: {
                    return msgpack.read(bytes, MsgRelayServer.class);
                }
                case MsgpackMsg.ID_RELAY_MASK_INFO_RSP: {
                    return msgpack.read(bytes, MsgRelayMaskInfoRsp.class);
                }
                case MsgpackMsg.CLIENT_BELL_CALL_LIST_RSP: {
                    return readHeader(msgpack, bytes, MsgBellCallListRsp.class);
                }
                case MsgpackMsg.ID_BELL_CONNECTED: {
                    return msgpack.read(bytes, MsgIdBellConnected.class);
                }
                case MsgpackMsg.CID_PUSH_GET_LOG:
                case MsgpackMsg.ID_RESUME: {
                    return msgpack.read(bytes, MsgpackMsg.MsgHeader.class);
                }
                case MsgpackMsg.CLIENT_HAS_MOBILE_RSP: {
                    return msgpack.read(bytes, MsgClientHasMobileRsp.class);
                }
                case MsgpackMsg.CLIENT_DEL_EFAML_MSG_RSP:
                case MsgpackMsg.CLIENT_BELL_CALL_DELETE_RSP:
                case MsgpackMsg.CLIENT_MSGIGNORE_RSP:
                case MsgpackMsg.CLIENT_EFAML_SET_BELL_RSP: {
                    return msgpack.read(bytes, RspMsgHeader.class);
                }
                case MsgpackMsg.CLIENT_EFAML_GET_BELLS_RSP: {
                    return msgpack.read(bytes, MsgClientEfamlGetBellsRsp.class);
                }
                case MsgpackMsg.CLIENT_PUSH_SIMPLE_NOTICE: {
                    return msgpack.read(bytes, MsgClientPushSimpleNotice.class);
                }
                case MsgpackMsg.CLIENT_EFAML_MSG_LIST_RSP: {
                    return msgpack.read(bytes, MsgClientEfamlMsgListRsp.class);
                }
                case MsgpackMsg.CID_PUSH_OSS_CONFIG: {
                    return msgpack.read(bytes, CidPushOssConfig.class);
                }
                case MsgpackMsg.EFAML_CALL_CLIENT: {
                    return msgpack.read(bytes, MsgEFamilyCallClient.class);
                }
                case MsgpackMsg.CLIENT_MAG_STATUS_LIST_RSP: {
                    return msgpack.read(bytes, MsgClientMagStatusListRsp.class);
                }
                case MsgpackMsg.CLIENT_MAG_SET_WARN_RSP: {
                    return msgpack.read(bytes, MsgClientMagSetWarnRsq.class);
                }
                case MsgpackMsg.CLIENT_MAG_GET_WARN_RSP: {
                    return msgpack.read(bytes, MsgClientMagGetWarnRsp.class);
                }
                case MsgpackMsg.EFAML_CALL_CANCEL: {
                    return msgpack.read(bytes, MsgEfamilyCallCancel.class);
                }
            }
        } catch (Exception ex) {
            DswLog.e("msg pack is err: " + (header == null ? -1 : header.msgId));
            DswLog.ex(ex.toString());
        }
        return null;


    }

}
