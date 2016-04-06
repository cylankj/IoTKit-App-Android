package com.cylan.jiafeigou.engine;

import com.cylan.jiafeigou.entity.msg.MsgClientPost;
import com.cylan.jiafeigou.entity.msg.MsgStatusSdcardToCid;
import com.cylan.jiafeigou.entity.msg.req.MsgUnbindCidReq;

import java.util.List;

/**
 * author：hebin on 2015/10/26 15:04
 * email：hebin@cylan.com.cn
 */
public class RequestMessage {

    /**
     * 解绑设备
     *
     * @param cid
     * @return MsgUnbindCidReq
     */
    public static MsgUnbindCidReq getMsgUnbindCidReq(String cid) {
        MsgUnbindCidReq msgUnbindCidReq = new MsgUnbindCidReq("", "");
        msgUnbindCidReq.cid = cid;
        return msgUnbindCidReq;
    }

    /**
     * 设备信息
     *
     * @param cid
     * @return MsgUnbindCidReq
     */
    public static MsgStatusSdcardToCid getDeviceInfo(String cid) {
        MsgStatusSdcardToCid mMsgStatusSdcardToCid = new MsgStatusSdcardToCid("", cid);
        mMsgStatusSdcardToCid.cid = cid;
        return mMsgStatusSdcardToCid;
    }


    /**
     * 绑定设备和设置wifi时，客户端提交信息
     *
     * @param type
     * @param list
     * @return MsgClientPost
     */
    public static MsgClientPost getMsgClientPost(String callee, int type, List<String> list) {
        MsgClientPost post = new MsgClientPost(callee);
        post.postType = type;
        post.array = list;
        return post;
    }
}
