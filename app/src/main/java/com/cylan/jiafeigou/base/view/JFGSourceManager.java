package com.cylan.jiafeigou.base.view;


import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGFeedbackInfo;
import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.entity.jniCall.JFGHistoryVideo;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.module.PanoramaEvent;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DataPoint;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by yzd on 16-12-28.
 */

public interface JFGSourceManager {

    Device getDevice(String uuid);

    List<Device> getAllDevice();

    Account getAccount();

    JFGAccount getJFGAccount();

    <T> T getValue(String uuid, long msgId, T defaultValue);

    List<Device> getDevicesByPid(int... pids);

    List<String> getDeviceUuidByPid(int... pids);

    void syncDeviceProperty(String uuid);

    /**
     * 获取某几个
     *
     * @param uuid
     * @param pids
     */
    void syncDeviceProperty(String uuid, int... pids);

    /**
     * 刷新设备属性，首页需要
     * ,手动刷
     */
    void syncAllProperty();

    /**
     * 刷新设备属性，首页需要
     * ,手动刷
     */
    boolean syncAllProperty(String uuid, int... excludeMsgIds);

    void syncHomeProperty();

    boolean isOnline();

    /**
     * 该设备是否已经被分享给其他用户
     *
     * @param uuid
     * @return
     */
    boolean isDeviceSharedTo(String uuid);

    /**
     * 依赖account
     *
     * @return
     */
    ArrayList<JFGShareListInfo> getShareList();

    JFGShareListInfo getShareListByCid(String cid);

    void cacheShareList(ArrayList<JFGShareListInfo> arrayList);
    int getStorageType();

    void setStorageType(int type);

    void setJfgAccount(JFGAccount jfgAccount);

    boolean updateDevice(Device device);

    /**
     * 本地和远程
     *
     * @param uuid
     * @param value
     * @param msgId
     * @param <T>
     * @return
     * @throws IllegalAccessException
     */
    <T extends DataPoint> boolean updateValue(String uuid, T value, int msgId) throws IllegalAccessException;

    <T extends DataPoint> boolean updateValue(String uuid, List<T> value) throws IllegalAccessException;

    <T extends DataPoint> boolean clearValue(String uuid, int... msgId) throws IllegalAccessException;

    boolean deleteByVersions(String uuid, long id, ArrayList<Long> versions);

    /**
     * 查询单个摄像头的报警消息：默认为{505,222}
     *
     * @param uuid
     */
    long syncJFGCameraWarn(String uuid, long version, boolean asc, int count);

    Observable<Boolean> queryHistory(String uuid);

    void cacheHistoryDataList(JFGHistoryVideo historyVideo);

    void cacheHistoryDataList(byte[] rawV2Data);

    void clear();

    Observable<Account> logout();

    Observable<String> unBindDevice(String uuid);

    ArrayList<Long> getHisDateList(String uuid);

    void initFromDB();

    void setOnline(boolean online);

    void initAccount();

    void initSubscription();

    <T> void addInterceptor(Long integer, DataSourceManager.Interceptors interceptors);

    void handleSystemNotification(ArrayList<JFGFeedbackInfo> list);

    ArrayList<JFGFriendAccount> getFriendsList();

    void setFriendsList(ArrayList<JFGFriendAccount> list);

    void setFriendsReqList(ArrayList<JFGFriendRequest> list);

    ArrayList<JFGFriendRequest> getFriendsReqList();


    void pushDeviceState(String uuid, PanoramaEvent.MsgVideoStatusRsp videoStatusRsp);

    void removeDeviceState(String uuid);

    Object getDeviceState(String uuid);
}
