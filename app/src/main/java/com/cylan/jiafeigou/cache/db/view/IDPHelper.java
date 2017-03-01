package com.cylan.jiafeigou.cache.db.view;

import com.cylan.jiafeigou.cache.db.module.DBAction;
import com.cylan.jiafeigou.cache.db.module.DPCache;

import java.util.List;

import rx.Observable;

/**
 * Created by yanzhendong on 2017/2/27.
 */

public interface IDPHelper {

    Observable saveDPByte(String uuid, Long version, Integer msgId, byte[] bytes);


    //junk code
    Observable deleteDPMsgNotConfirm(String uuid, Long version, Integer msgId);

    //junk code
    Observable deleteDPMsgWithConfirm(String uuid, Long version, Integer msgId);

    Observable deleteDPMsgWithConfirm(String uuid, Integer msgId);

    Observable queryUnConfirmDpMsgWithTag(String uuid, Integer msgId, DBAction tag);

    Observable<List<DPCache>> queryUnConfirmDpMsg(String uuid, Integer msgId);

    Observable<List<DPCache>> markDPMsgWithConfirm(String uuid, Long version, Integer msgId, DBAction tag);

    Observable<List<DPCache>> markDPMsgNotConfirm(String uuid, Long version, Integer msgId, DBAction tag);

    Observable queryDPMsg(String uuid, Long version, Integer msgId, Boolean asc, Integer limit);


    Observable<DPCache> saveDpMsg(String account, String server, String uuid, Long version, Integer msgId, byte[] bytes, DBAction... dbActions);

    Observable<List<DPCache>> queryDPMsg(String account, String server, String uuid, Long version, Integer msgId, Boolean asc, Integer limit, DBAction... dbActions);

    Observable<List<DPCache>> markDPMsg(String account, String server, String uuid, Long version, Integer msgId, DBAction... markedAction);

}
