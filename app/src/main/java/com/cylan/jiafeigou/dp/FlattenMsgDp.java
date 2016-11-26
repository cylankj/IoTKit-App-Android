package com.cylan.jiafeigou.dp;

import android.text.TextUtils;

import com.cylan.jiafeigou.n.mvp.model.BaseBean;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by cylan-hunt on 16-11-17.
 */

public class FlattenMsgDp implements IFlat {

    /**
     * <账号,uuid></>
     */
    private Map<String, ArrayList<String>> accountUUidMap = new HashMap<>();

    /**
     * <uuid,基本信息></>
     */
    private Map<String, BaseBean> baseDpDeviceMap = new HashMap<>();
    /**
     * uuid,msgId,Object
     */
    private Map<String, ArrayList<DpMsgDefine.DpMsg>> simpleMap = new HashMap<>();

    @Override
    public void cache(String account, String uuid) {
        ArrayList<String> uuidList = accountUUidMap.get(account);
        if (uuidList == null) {
            uuidList = new ArrayList<>();
        }
        if (!uuidList.contains(uuid))
            uuidList.add(uuid);
        uuidList = new ArrayList<>(new HashSet<>(uuidList));
        accountUUidMap.put(account, uuidList);
    }

    @Override
    public void cache(String account, BaseBean dpDevice) {
        ArrayList<String> uuidList = accountUUidMap.get(account);
        if (uuidList == null) {
            uuidList = new ArrayList<>();
        }
        if (!uuidList.contains(dpDevice.uuid)) {
            uuidList.add(dpDevice.uuid);
            accountUUidMap.put(account, uuidList);
        }
        baseDpDeviceMap.put(dpDevice.uuid, dpDevice);
    }

    @Override
    public void clean() {
        accountUUidMap.clear();
    }


    /**
     * 获取当前账号下的设备uuid
     *
     * @param account
     * @return
     */
    @Override
    public ArrayList<String> getUuidList(String account) {
        return accountUUidMap.get(account);
    }

    @Override
    public DpMsgDefine.DpWrap removeMsg(String account, String uuid) {
        return null;
    }


    @Override
    public DpMsgDefine.DpWrap getDevice(String account, String uuid) {
        exception(account, uuid);
        BaseBean baseDpDevice = baseDpDeviceMap.get(uuid);
        ArrayList<DpMsgDefine.DpMsg> dpMsgList = simpleMap.get(uuid);
        DpMsgDefine.DpWrap wrap = new DpMsgDefine.DpWrap();
        wrap.baseDpDevice = baseDpDevice;
        wrap.baseDpMsgList = dpMsgList;
        return wrap;
    }

    @Override
    public void cache(String account, String uuid, DpMsgDefine.DpMsg msg) {
        exception(account, uuid);
        DpMsgDefine.DpWrap wrap = getDevice(account, uuid);
        ArrayList<DpMsgDefine.DpMsg> list = wrap.baseDpMsgList;
        if (list == null)
            list = new ArrayList<>();
        if (!list.contains(msg)) {
            list.add(msg);
        }
        simpleMap.put(uuid, list);
    }


    @Override
    public ArrayList<DpMsgDefine.DpWrap> getAllDevices(String account) {
        if (TextUtils.isEmpty(account)) {
            AppLogger.i("account is null");
            return null;
        }
        ArrayList<DpMsgDefine.DpWrap> finalList = new ArrayList<>();
        ArrayList<String> uuidList = accountUUidMap.get(account);
        if (uuidList == null) {
            AppLogger.e("uuidList is null: " + account);
            return null;
        }
        for (String uuid : uuidList) {
            finalList.add(getDevice(account, uuid));
        }
        return finalList;
    }

    private void exception(String account, String uuid) {
        if (TextUtils.isEmpty(account)
                || TextUtils.isEmpty(uuid)) {
            AppLogger.e("wrong: " + account + " " + uuid);
        }
    }

}
