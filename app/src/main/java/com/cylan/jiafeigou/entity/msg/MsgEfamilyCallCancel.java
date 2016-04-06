package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

/**
 * Created by yangc on 2016/2/23.
 */
public class MsgEfamilyCallCancel extends MsgpackMsg.MsgHeader {
    public MsgEfamilyCallCancel() {
        msgId = MsgpackMsg.EFAML_CALL_CANCEL;
    }
}
