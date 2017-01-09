package com.cylan.jiafeigou.base.view;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jiafeigou.base.module.JFGDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yzd on 16-12-28.
 */

public interface JFGSourceManager {

    <T extends JFGDevice> T getJFGDevice(String uuid);

    List<JFGDevice> getAllJFGDevice();

    void cacheJFGDevices(com.cylan.entity.jniCall.JFGDevice... devices);

    void cacheJFGAccount(JFGAccount account);

    JFGAccount getJFGAccount();

    void cacheRobotoGetDataRsp(RobotoGetDataRsp dataRsp);

    void cacheRobotoSyncData(boolean b, String s, ArrayList<JFGDPMsg> arrayList);

    <T> T getValue(String uuid, long msgId);

    <T> T getValue(String uuid, long msgId, long seq);
}
