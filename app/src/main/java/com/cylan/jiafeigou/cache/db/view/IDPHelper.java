package com.cylan.jiafeigou.cache.db.view;

import com.cylan.jiafeigou.cache.db.module.DPEntity;

import java.util.List;

import rx.Observable;

/**
 * Created by yanzhendong on 2017/2/27.
 */

public interface IDPHelper {

    Observable saveDPByte(String uuid, Long version, Integer msgId, byte[] bytes);


    //junk code
    Observable<DPEntity> deleteDPMsgNotConfirm(String uuid, Long version, Integer msgId);

    //junk code
    Observable<DPEntity> deleteDPMsgWithConfirm(String uuid, Long version, Integer msgId);

    Observable<Boolean> deleteDPMsgWithConfirm(String uuid, Integer msgId);

    Observable<List<DPEntity>> queryUnConfirmDpMsgWithTag(String uuid, Integer msgId, IDPAction action);

    Observable<List<DPEntity>> queryUnConfirmDpMsg(String uuid, Integer msgId);

    Observable<List<DPEntity>> markDPMsgWithConfirm(String uuid, Long version, Integer msgId, IDPAction action);

    Observable<List<DPEntity>> markDPMsgNotConfirm(String uuid, Long version, Integer msgId, IDPAction action);

    Observable<List<DPEntity>> queryDPMsg(String uuid, Long version, Integer msgId, Boolean asc, Integer limit);


    Observable<DPEntity> saveDpMsg(String account, String server, String uuid, Long version, Integer msgId, byte[] bytes, String action, String state);

    Observable<DPEntity> saveOrUpdate(String account, String server, String uuid, Long version, Integer msgId, byte[] bytes, String action, String state);

    Observable<List<DPEntity>> queryDPMsg(String account, String server, String uuid, Long version, Integer msgId, Boolean asc, Integer limit, IDPAction action, IDPState state);

    Observable<List<DPEntity>> markDPMsg(String account, String server, String uuid, Long version, Integer msgId, IDPAction action, IDPState state);

    Observable<DPEntity> deleteDPMsgForce(String account, String server, String uuid, Long version, Integer msgId);
}
