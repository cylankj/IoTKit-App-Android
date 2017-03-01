package com.cylan.jiafeigou.base.view;


import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jiafeigou.base.module.JFGDPAccount;
import com.cylan.jiafeigou.base.module.JFGDPDevice;
import com.cylan.jiafeigou.dp.DataPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yzd on 16-12-28.
 */

public interface JFGSourceManager {

    <T extends JFGDPDevice> T getJFGDevice(String uuid);

    List<JFGDPDevice> getAllJFGDevice();

    void cacheJFGDevices(com.cylan.entity.jniCall.JFGDevice... devices);

    void cacheJFGAccount(com.cylan.entity.jniCall.JFGAccount account);

    JFGDPAccount getJFGAccount();

    void cacheRobotoGetDataRsp(RobotoGetDataRsp dataRsp);

    void cacheRobotoSyncData(boolean b, String s, ArrayList<JFGDPMsg> arrayList);

    <T extends DataPoint> T getValue(String uuid, long msgId);

    <T extends DataPoint> T getValue(String uuid, long msgId, long seq);

    List<JFGDPDevice> getJFGDeviceByPid(int... pids);

    List<String> getJFGDeviceUUIDByPid(int... pids);

    void syncJFGDeviceProperty(String uuid);

    void syncAllJFGDeviceProperty();

    <T extends DataPoint> List<T> getValueBetween(String uuid, long msgId, long startVersion, long endVersion);

    boolean isOnline();
}
