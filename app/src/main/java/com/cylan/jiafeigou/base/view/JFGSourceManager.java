package com.cylan.jiafeigou.base.view;


import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGFeedbackInfo;
import com.cylan.entity.jniCall.JFGHistoryVideo;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.jfgapp.interfases.AppCmd;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.IDBHelper;
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

//    void getDevicePropertyByIds(String uuid, long... id);

    /**
     * 刷新设备属性，首页需要
     * ,手动刷
     */
    void syncAllDevicePropertyManually();

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

    void cacheShareList(ArrayList<JFGShareListInfo> arrayList);

    void setLoginState(LogState loginState);

    int getLoginState();

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

    void clear();

    Observable<Account> logout();

    Observable<Device> unBindDevice(String uuid);

    ArrayList<Long> getHisDateList(String uuid);

    void setDBHelper(IDBHelper dbHelper);

    void setPropertyParser(IPropertyParser parser);

    void setAppCmd(AppCmd appCmd);

    void initFromDB();

    void setOnline(boolean online);

    void initAccount();

    void initSubscription();

    <T> void addInterceptor(Long integer, DataSourceManager.Interceptors interceptors);

    void handleSystemNotification(ArrayList<JFGFeedbackInfo> list);
}
