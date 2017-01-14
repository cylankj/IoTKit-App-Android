package com.cylan.jiafeigou.dp;

import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.base.module.JFGDevice;
import com.cylan.jiafeigou.n.mvp.model.BaseBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by cylan-hunt on 16-11-17.
 * @deprecated 请使用 {@link com.cylan.jiafeigou.cache.pool.GlobalDataProxy}
 */
@Deprecated
public class FlattenMsgDp implements IFlat {

    /**
     * <账号,uuid></>
     */
    private Map<String, ArrayList<String>> accountUUidMap = new HashMap<>();

    /**
     * <account+uuid,基本信息></>
     */
    private Map<String, BaseBean> baseDpDeviceMap = new HashMap<>();

    /**
     * account+uuid,msgId,Object
     */
    private Map<String, ArrayList<DpMsgDefine.DpMsg>> simpleMap = new HashMap<>();
    /**
     * account+uuid:报警消息
     */
    private Map<String, ArrayList<DpMsgDefine.DpMsg>> alarmMsg = new HashMap<>();

    @Override
    public void cache(String account, String uuid) {
        //某个账号下所有的uuid
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
        //某个账号下所有的uuid
        ArrayList<String> uuidList = accountUUidMap.get(account);
        if (uuidList == null) {
            uuidList = new ArrayList<>();
        }
        if (!uuidList.contains(dpDevice.uuid)) {
            uuidList.add(dpDevice.uuid);
            accountUUidMap.put(account, uuidList);
        }
        baseDpDeviceMap.put(account + dpDevice.uuid, dpDevice);
    }

    @Override
    public void cacheJFGDevice(String account, JFGDevice device) {
        //某个账号下所有的uuid
        ArrayList<String> uuidList = accountUUidMap.get(account);
        if (uuidList == null) {
            uuidList = new ArrayList<>();
        }
        if (!uuidList.contains(device.uuid)) {
            uuidList.add(device.uuid);
            accountUUidMap.put(account, uuidList);
        }
    }


    @Override
    public void cache(String account, String uuid, ArrayList<DpMsgDefine.DpMsg> jfgdpMsgs) {
        exception(account, uuid);
        alarmMsg.put(account + uuid, jfgdpMsgs);
    }

    @Override
    public void rm(String account) {
        if (!accountUUidMap.containsKey(account)) {
            AppLogger.e("not contains account");
            return;
        }
        List<String> uuidList = accountUUidMap.get(account);
        if (uuidList != null) {
            for (String uuid : uuidList) {
                simpleMap.remove(account + uuid);
                baseDpDeviceMap.remove(account + uuid);
                alarmMsg.remove(account + uuid);
            }
        }
        accountUUidMap.remove(account);
    }

    @Override
    public void rm(String account, String uuid) {
        exception(account, uuid);
        simpleMap.remove(account + uuid);
        baseDpDeviceMap.remove(account + uuid);
        alarmMsg.remove(account + uuid);
    }

    @Override
    public void clean() {
        accountUUidMap.clear();
        baseDpDeviceMap.clear();
        simpleMap.clear();
        alarmMsg.clear();
    }

    @Override
    public void update(String account, String uuid, DpMsgDefine.DpMsg msg) {
        exception(account, uuid);
        DpMsgDefine.DpWrap wrap = getDevice(account, uuid);
        ArrayList<DpMsgDefine.DpMsg> list = wrap.baseDpMsgList;
        if (list == null)
            list = new ArrayList<>();
        int index = list.indexOf(msg);
        if (index != -1) {
            list.set(index, msg);
        } else {
            list.add(msg);
        }
        Log.d("FlattenMsgDp", "setDevice?: " + (index != -1) + "," + new Gson().toJson(msg));
        simpleMap.put(account + uuid, list);
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
        BaseBean baseDpDevice = baseDpDeviceMap.get(account + uuid);
        ArrayList<DpMsgDefine.DpMsg> dpMsgList = simpleMap.get(account + uuid);
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
        simpleMap.put(account + uuid, list);
    }


    @Override
    public ArrayList<DpMsgDefine.DpWrap> getAllDevices(String account) {
        ArrayList<DpMsgDefine.DpWrap> result = new ArrayList<>();
        if (TextUtils.isEmpty(account)) {
            AppLogger.i("account is null");
            return result;
        }
        ArrayList<String> uuidList = accountUUidMap.get(account);
        if (uuidList == null) {
            AppLogger.e("uuidList is null: " + account);
            return result;
        }
        for (String uuid : uuidList) {
            result.add(getDevice(account, uuid));
        }
        return result;
    }

    private void exception(String account, String uuid) {
        if (TextUtils.isEmpty(account)
                || TextUtils.isEmpty(uuid)
                || !accountUUidMap.containsKey(account)) {
            AppLogger.e("wrong: " + account + " " + uuid);
        }
    }

}
