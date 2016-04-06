package com.cylan.jiafeigou.activity.doorbell;

import android.content.Context;
import android.net.ConnectivityManager;

import com.cylan.publicApi.Constants;
import com.cylan.publicApi.JniPlay;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.req.MsgRelayMaskInfoReq;
import com.cylan.jiafeigou.entity.msg.rsp.MsgRelayMaskInfoRsp;
import com.cylan.jiafeigou.utils.Utils;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2015-12-28
 * Time: 18:17
 */

public class WIFICallOut implements IPlayOrStop {

    private MsgCidData mInfo;
    private MsgRelayMaskInfoRsp mRsp;
    private Context mContext;

    public WIFICallOut(Context ctx, MsgRelayMaskInfoRsp mInfoRsp, MsgCidData info) {
        this.mContext = ctx;
        this.mRsp = mInfoRsp;
        this.mInfo = info;
    }

    @Override
    public void makeCall() {
        if (mRsp == null) {
            JniPlay.SendBytes(new MsgRelayMaskInfoReq(mInfo.cid).toBytes());
            return;
        }
        int netType = (Utils.getNetType(mContext) == ConnectivityManager.TYPE_WIFI ? 1 : 0);
        boolean fastp2p = mInfo.os != Constants.OS_DOOR_BELL_V2;
        if (mRsp != null)
            mRsp.callee = mInfo.cid;
        if (mRsp != null) {
            int relaymask[] = new int[mRsp.mask_list.size()];
            for (int i = 0; i < mRsp.mask_list.size(); i++) {
                relaymask[i] = mRsp.mask_list.get(i);
            }
            JniPlay.ConnectToPeer(mInfo.cid, true, netType, false, mInfo.os, relaymask, false, fastp2p);
        } else {
            JniPlay.ConnectToPeer(mInfo.cid, true, netType, false, mInfo.os, new int[0], false, fastp2p);
        }
    }

    @Override
    public void stop() {
        JniPlay.DisconnectFromPeer();
    }
}
