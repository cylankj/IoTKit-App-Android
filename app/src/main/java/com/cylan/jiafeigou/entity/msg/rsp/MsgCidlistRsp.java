package com.cylan.jiafeigou.entity.msg.rsp;

import com.cylan.publicApi.Constants;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.RelatedbellBean;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.MsgSceneData;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.utils.CacheUtil;
import com.cylan.jiafeigou.utils.StringUtils;

import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:57
 */
@Message
public class MsgCidlistRsp extends RspMsgHeader implements Serializable {

    @Index(5)
    public int vid;
    @Index(6)
    public List<MsgSceneData> data;

    @Ignore
    private static MsgCidlistRsp instance = null;

    @Ignore
    public static MsgCidlistRsp getInstance() {
        if (instance == null) {
            Serializable obj = CacheUtil.readObject(CacheUtil.getCID_LIST_KEY());
            if (obj == null) { // App第一次启动，文件不存在，则新建之
                obj = new MsgCidlistRsp();
                ((MsgCidlistRsp) obj).data = new ArrayList<>();
                CacheUtil.saveObject(obj, CacheUtil.getCID_LIST_KEY());
            }
            instance = (MsgCidlistRsp) obj;
        }
        return instance;
    }

    @Ignore
    public void setCidList(MsgCidlistRsp mMsgCidlistRsp) {
        instance = mMsgCidlistRsp;
        CacheUtil.saveObject(mMsgCidlistRsp, CacheUtil.getCID_LIST_KEY());
    }


    @Ignore
    public MsgSceneData getEnableMsgSceneData() {
        for (int i = 0, size = data.size(); i < size; i++) {
            if (data.get(i).enable == ClientConstants.ENABLE_SCENE) {
                return data.get(i);
            }
        }
        return null;
    }

    @Ignore
    public int getEnableSceneIndex() {
        for (int i = 0, size = data.size(); i < size; i++) {
            if (data.get(i).enable == ClientConstants.ENABLE_SCENE) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 是某个模式
     *
     * @param cid
     * @return
     */
    @Ignore
    public boolean isSomeoneMode(String cid, int mode) {
        MsgCidlistRsp rsp = getInstance();
        if (rsp == null)
            return false;
        for (int i = 0; i < rsp.data.size(); i++) {
            MsgSceneData mScencInfo = rsp.data.get(i);
            for (int j = 0; j < mScencInfo.data.size(); j++) {
                if (mScencInfo.data.get(j).cid.equals(cid)) {
                    if (mScencInfo.mode == mode) {
                        return true;
                    }
                    break;
                }
            }
        }
        return false;
    }

    @Ignore
    public String getSceneName(int mSceneid) {
        MsgCidlistRsp rsp = getInstance();
        for (int i = 0; i < rsp.data.size(); i++) {
            MsgSceneData mScencInfo = rsp.data.get(i);
            if (mScencInfo.scene_id == mSceneid) {
                return mScencInfo.scene_name;
            }
        }
        return "";

    }

    @Ignore
    public MsgCidData getVideoInfoByCid(String cid) {
        MsgCidlistRsp rsp = getInstance();
        MsgSceneData mScencInfo;
        MsgCidData mVideoInfo;
        for (int i = 0, scencSize = rsp.data.size(); i < scencSize; i++) {
            mScencInfo = rsp.data.get(i);
            for (int j = 0; j < mScencInfo.data.size(); j++) {
                mVideoInfo = mScencInfo.data.get(j);
                if (mVideoInfo.cid.equals(cid)) {
                    return mVideoInfo;
                }
            }
        }
        return null;
    }

    @Ignore
    public MsgCidData getVideoInfoFromCacheByCid(String cid) {
        MsgCidData info = getVideoInfoByCid(cid);
        if (info == null) {
            MsgCidlistRsp mMsgCidlistRsp = (MsgCidlistRsp) CacheUtil.readObject(CacheUtil.getCID_LIST_KEY());
            if (mMsgCidlistRsp != null) {
                for (int i = 0, size = mMsgCidlistRsp.data.size(); i < size; i++) {
                    MsgSceneData data = mMsgCidlistRsp.data.get(i);
                    for (int j = 0; j < data.data.size(); j++) {
                        MsgCidData mCidData = data.data.get(j);
                        if (mCidData.cid.equals(cid)) {
                            return mCidData;
                        }
                    }
                }
            }
        }
        return info;
    }

    @Ignore
    public List<RelatedbellBean> getDoorbellList() {
        MsgCidlistRsp rsp = getInstance();
        if (rsp == null)
            return null;
        List<RelatedbellBean> list = new ArrayList<>();
        MsgSceneData mScencInfo = null;
        MsgCidData mVideoInfo = null;
        for (int i = 0, scencSize = rsp.data.size(); i < scencSize; i++) {
            mScencInfo = rsp.data.get(i);
            for (int j = 0; j < mScencInfo.data.size(); j++) {
                mVideoInfo = mScencInfo.data.get(j);
                if (Constants.isDoorbellByOS(mVideoInfo.os) && StringUtils.isEmptyOrNull(mVideoInfo.share_account)) {
                    RelatedbellBean bean = new RelatedbellBean();
                    bean.setInfo(mVideoInfo);
                    bean.setChoose(false);
                    list.add(bean);
                }
            }
        }
        return list;
    }

    @Ignore
    public boolean isHasBindDeviceByCid(String suffix) {
        MsgCidlistRsp rsp = getInstance();
        if (rsp == null)
            return false;
        for (int i = 0; i < rsp.data.size(); i++) {
            MsgSceneData mScencInfo = rsp.data.get(i);
            for (int j = 0; j < mScencInfo.data.size(); j++) {
                MsgCidData mVideoInfo = mScencInfo.data.get(j);
                if (mVideoInfo.cid.endsWith(suffix)) {
                    return true;
                }
            }
        }
        return false;
    }

}
