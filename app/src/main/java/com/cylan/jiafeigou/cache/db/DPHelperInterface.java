package com.cylan.jiafeigou.cache.db;

import rx.Observable;

/**
 * Created by yanzhendong on 2017/2/27.
 */

public interface DPHelperInterface {

    Observable saveDPByte(String uuid, long version, int msgId, byte[] bytes);


    //junk code
    Observable deleteDPMsgNotConfirm(String uuid, long version, int msgId);

    //junk code
    Observable deleteDPMsgWithConfirm(String uuid, long version, int msgId);

    Observable deleteDPMsgWithConfirm(String uuid, int msgId);

    Observable queryUnConfirmDpMsgWithTag(String uuid, int msgId, String tag);

    Observable queryDPMsg(String uuid, long version, int msgId, boolean asc, int limit);
}
