package com.cylan.jiafeigou.base.view;


import android.util.Pair;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDPMsgCount;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.JFGHistoryVideo;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.entity.jniCall.JFGVideo;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jiafeigou.base.module.JFGDPAccount;
import com.cylan.jiafeigou.base.module.JFGDPDevice;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.dp.DataPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yzd on 16-12-28.
 */

public interface JFGSourceManager {

    <T extends JFGDPDevice> T getJFGDevice(String uuid);

    JFGDevice getRawJFGDevice(String uuid);

    List<JFGDPDevice> getAllJFGDevice();

    ArrayList<JFGDevice> getAllRawJFGDeviceList();

    boolean updateRawDevice(JFGDevice device);

    /**
     * 删除设备，解绑设备使用,一般等待服务器返回结果再调用
     *
     * @param uuid
     * @return
     */
    boolean delLocalJFGDevice(String uuid);

    boolean delRemoteJFGDevice(String uuid);

    void cacheJFGDevices(com.cylan.entity.jniCall.JFGDevice... devices);

    void cacheJFGAccount(com.cylan.entity.jniCall.JFGAccount account);

    JFGDPAccount getAJFGAccount();

    JFGAccount getJFGAccount();

    void cacheRobotoGetDataRsp(RobotoGetDataRsp dataRsp);

    void cacheRobotoSyncData(boolean b, String s, ArrayList<JFGDPMsg> arrayList);

    <T extends DataPoint> T getValue(String uuid, long msgId);

//    <T extends DataPoint> T getValueSafe(String uuid, long msgId, Object defaultValue);

    <T extends DataPoint> T getValue(String uuid, long msgId, long seq);

    List<JFGDPDevice> getJFGDeviceByPid(int... pids);

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

    boolean updateJFGDevice(JFGDPDevice device);

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

    boolean deleteByVersions(String uuid, long id, ArrayList<Long> versions);

    /**
     * 获取设备的所有最新的报警消息；1.用于展示最新的报警消息列表，2.根据本地的时间戳游标显示有xx条新消息。
     *
     * @param ignoreShareDevice
     */
    void syncAllJFGCameraWarnMsg(boolean ignoreShareDevice);

    /**
     * 查询单个摄像头的报警消息：默认为{505,222}
     *
     * @param uuid
     */
    long syncJFGCameraWarn(String uuid, boolean asc, int count);

    void queryHistory(String uuid);

    void cacheHistoryDataList(JFGHistoryVideo historyVideo);

    ArrayList<JFGVideo> getHistoryList(String uuid);

    void cacheUnreadCount(long seq, String uuid, ArrayList<JFGDPMsgCount> unreadList);

    Pair<Integer, Long> getUnreadCount(String uuid, long... ids);

    void clear();

    void clearUnread(String uuid, long... ids);

    void syncDeviceUnreadCount();
}
