package com.cylan.jiafeigou.cache.db.view;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.module.HistoryFile;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import rx.Observable;

/**
 * Created by yanzhendong on 2017/2/27.
 */

public interface IDBHelper {

    //junk code
    Observable<DPEntity> deleteDPMsgNotConfirm(String uuid, Long version, Integer msgId, DBOption option);

    //junk code
    Observable<DPEntity> deleteDPMsgWithConfirm(String uuid, Long version, Integer msgId, DBOption option);

    Observable<List<DPEntity>> deleteDPMsgWithConfirm(String uuid, Integer msgId, DBOption option);

    Observable<List<DPEntity>> deleteDPMsgNotConfirm(String uuid, Integer msgId, DBOption option);

    Observable<List<DPEntity>> queryUnConfirmDpMsgWithTag(String uuid, Integer msgId, DBAction action);

    Observable<List<DPEntity>> queryUnConfirmDpMsg(String uuid, Integer msgId);

    Observable<List<DPEntity>> markDPMsgWithConfirm(String uuid, Long version, Integer msgId, DBAction action, DBOption option);

    Observable<List<DPEntity>> markDPMsgNotConfirm(String uuid, Long version, Integer msgId, DBAction action, DBOption option);

    Observable<List<DPEntity>> queryDPMsg(String uuid, Long version, Integer msgId, Boolean asc, Integer limit);

    Observable<List<DPEntity>> queryDPMsgByUuid(String uuid);

    Observable<DPEntity> saveOrUpdate(String account, String server, String uuid, Long version, Integer msgId, byte[] bytes, DBAction action, DBState state, DBOption option);

    Observable<DPEntity> saveOrUpdate(String uuid, Long version, Integer msgId, byte[] bytes, DBAction action, DBState state, DBOption option);

    Observable<List<DPEntity>> queryDPMsg(String account, String server, String uuid, Long version, Integer msgId, Boolean asc, Integer limit, DBAction action, DBState state, DBOption option);

    Observable<DPEntity> deleteDPMsgForce(String account, String server, String uuid, Long version, Integer msgId);

    Observable<Account> updateAccount(JFGAccount account);

    Observable<Account> getActiveAccount();

    Observable<Iterable<Device>> updateDevice(JFGDevice[] device);

    Observable<Device> updateDevice(Device device);

    Observable<Device> unBindDeviceNotConfirm(String uuid);

    Observable<Device> unBindDeviceWithConfirm(String uuid);

    Observable<Iterable<Device>> unBindDeviceWithConfirm(Iterable<String> uuids);

    Observable<Device> getDevice(String uuid);

    Observable<List<Device>> markDevice(String account, String server, String uuid, DBAction action, DBState state, DBOption option);

    Observable<List<Device>> getAccountDevice(String account);

    Observable<List<DPEntity>> getAllSavedDPMsgByAccount(String account);

    Observable<List<DPEntity>> getActiveAccountSavedDPMsg();

    Observable<DPEntity> findDPMsg(String uuid, Long version, Integer msgId);

    Observable<Account> logout();

    Observable<Iterable<DPEntity>> saveDPByteInTx(String uuid, Iterable<JFGDPMsg> msgs);

    Observable<Iterable<DPEntity>> saveDPByteInTx(RobotoGetDataRsp dataRsp);

    Observable<List<DPEntity>> queryMultiDpMsg(String account, String server, String uuid, Long version, Long versionMax, List<Integer> msgIdList, Integer limit, DBAction action, DBState state, DBOption option);

    Observable<DPEntity> queryDpMsg(QueryBuilder<DPEntity> builder);

    Observable<List<DPEntity>> queryMultiDpMsg(QueryBuilder<DPEntity> builder);

    QueryBuilder<DPEntity> getDpEntityQueryBuilder();

    DPEntity getProperty(String uuid, int msgId);

    Device getJFGDevice(String uuid);

    Observable<List<HistoryFile>> loadHistoryFile(String uuid, long timeStart, long timeEnd);

    Observable<HistoryFile> saveHistoryFile(HistoryFile historyFile);

    Observable<Boolean> deleteHistoryFile(String uuid, long timeStart, long timeEnd);

    Observable<Void> deleteAllHistoryFile(String uuid);

    void clear(String uuid, Integer msgId);

    void setDataSourceManager(JFGSourceManager manager);

    void setPropertyParser(IPropertyParser parser);
}