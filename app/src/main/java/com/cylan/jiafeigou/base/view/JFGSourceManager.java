package com.cylan.jiafeigou.base.view;


import android.util.Pair;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsgCount;
import com.cylan.entity.jniCall.JFGHistoryVideo;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.entity.jniCall.JFGVideo;
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

    <T extends Device> T getJFGDevice(String uuid);

    List<Device> getAllJFGDevice();

    Account getAJFGAccount();

    JFGAccount getJFGAccount();

    <T extends DataPoint> T getValue(String uuid, long msgId);

    List<Device> getJFGDeviceByPid(int... pids);

    List<String> getJFGDeviceUUIDByPid(int... pids);

    void syncJFGDeviceProperty(String uuid);

    /**
     * 刷新设备属性，首页需要
     */
    void syncAllJFGDeviceProperty();

    <T extends DataPoint> List<T> getValueBetween(String uuid, long msgId, long startVersion, long endVersion);

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

    void setJfgAccount(JFGAccount jfgAccount);

    boolean updateJFGDevice(Device device);

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

    boolean deleteByVersions(String uuid, long id, ArrayList<Long> versions);

    /**
     * 获取设备的所有最新的报警消息；1.用于展示最新的报警消息列表，2.根据本地的时间戳游标显示有xx条新消息。
     *
     * @param ignoreShareDevice
     */
//    void syncAllJFGCameraWarnMsg(boolean ignoreShareDevice);

    /**
     * 查询单个摄像头的报警消息：默认为{505,222}
     *
     * @param uuid
     */
    long syncJFGCameraWarn(String uuid, long version, boolean asc, int count);

    int queryHistory(String uuid);

    void cacheHistoryDataList(JFGHistoryVideo historyVideo);

    ArrayList<JFGVideo> getHistoryList(String uuid);

    void cacheUnreadCount(long seq, String uuid, ArrayList<JFGDPMsgCount> unreadList);

    Pair<Integer, Long> getUnreadCount(String uuid, long... ids);

    void clear();

    void clearUnread(String uuid, long... ids);

    void syncDeviceUnreadCount();

    Observable<Account> logout();

    Observable<Device> unBindDevice(String uuid);

    void setValue(String uuid, int msgId, byte[] bytes, long version, long seq);

    void clearValue(String uuid, int msgId);
}
