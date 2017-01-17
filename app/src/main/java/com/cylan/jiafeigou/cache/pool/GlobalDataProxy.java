package com.cylan.jiafeigou.cache.pool;

import android.text.TextUtils;
import android.util.Pair;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.dp.IDataPoint;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cylan-hunt on 16-12-26.
 */

public class GlobalDataProxy implements IDataProxy {

    private static GlobalDataProxy instance;
    private JFGAccount jfgAccount;
    private IDataPoint dataPointManager;
    /**
     * <String(account),ArrayList<...></>></>
     * 根据账号
     */
    private Map<String, ArrayList<JFGShareListInfo>> shareListMap = new HashMap<>();
    private boolean isOnline;

    private GlobalDataProxy() {
    }

    public static GlobalDataProxy getInstance() {
        if (instance == null) {
            synchronized (GlobalDataProxy.class) {
                if (instance == null) instance = new GlobalDataProxy();
            }
        }
        return instance;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setJfgAccount(JFGAccount jfgAccount) {
        this.jfgAccount = jfgAccount;
        AppLogger.i("setJfgAccount:" + (jfgAccount == null));

    }

    public JFGAccount getJfgAccount() {
        return jfgAccount;
    }

    public void setDataPointManager(IDataPoint dataPointManager) {
        this.dataPointManager = dataPointManager;
    }

    @Override
    public void cacheDevice(String uuid, JFGDevice jfgDevice) {
        if (checkAccount() && dataPointManager != null)
            dataPointManager.cacheDevice(jfgAccount.getAccount() + uuid, jfgDevice);
    }

    @Override
    public boolean remove(String uuid) {
        if (!checkAccount()) return false;
        return dataPointManager != null && dataPointManager.remove(jfgAccount.getAccount() + uuid);
    }

    @Override
    public JFGDevice fetch(String uuid) {
        if (!checkAccount() || dataPointManager == null) return null;
        return dataPointManager.fetch(jfgAccount.getAccount() + uuid);
    }

    @Override
    public void cacheShareList(ArrayList<JFGShareListInfo> arrayList) {
        if (checkAccount()) {
            shareListMap.put(jfgAccount.getAccount(), arrayList);
            RxBus.getCacheInstance().post(new RxEvent.GetShareListRsp());
        }
    }

    @Override
    public boolean isDeviceShared(String uuid) {
        if (checkAccount()) {
            ArrayList<JFGShareListInfo> listInfos = shareListMap.get(jfgAccount.getAccount());
            int size = listInfos == null ? 0 : listInfos.size();
            for (int i = 0; i < size; i++) {
                JFGShareListInfo info = listInfos.get(i);
                if (TextUtils.equals(uuid, info.cid)) {
                    return info.friends != null && info.friends.size() > 0;
                }
            }
        }
        return false;
    }

    @Override
    public ArrayList<JFGShareListInfo> getShareList() {
        if (jfgAccount == null || TextUtils.isEmpty(jfgAccount.getAccount()))
            return null;
        return this.shareListMap.get(jfgAccount.getAccount());
    }

    @Override
    public boolean updateJFGDevice(JFGDevice device) {
        if (dataPointManager == null) return false;
        boolean r = dataPointManager.updateJFGDevice(getJfgAccount().getAccount(), device);
        //需要修改
        try {
            JfgCmdInsurance.getCmd().setAliasByCid(device.uuid, device.alias);
        } catch (JfgException e) {
            r = false;
        }
        AppLogger.i("r:" + r);
        return r;
    }

    @Override
    public ArrayList<JFGDevice> fetchAll() {
        if (!checkAccount() || dataPointManager == null) return null;
        return dataPointManager.fetchAll(jfgAccount.getAccount());
    }

    private boolean checkAccount() {
        if (jfgAccount == null || TextUtils.isEmpty(jfgAccount.getAccount())) {
//            if (BuildConfig.DEBUG) throw new IllegalArgumentException("account is null");
            //we just clear the cache if the account is null
//            if (dataPointManager != null) dataPointManager.clearAll();
            AppLogger.e("account is null");
            return false;
        }
        return true;
    }

    @Override
    public boolean insert(String uuid, BaseValue baseValue) {
        return dataPointManager != null && dataPointManager.insert(uuid, baseValue);
    }

    @Override
    public boolean update(String uuid, BaseValue baseValue, boolean sync) {
        return dataPointManager != null && dataPointManager.update(uuid, baseValue, sync);
    }

    @Override
    public boolean deleteAll(String uuid) {
        return dataPointManager != null && dataPointManager.deleteAll(uuid);
    }

    @Override
    public boolean deleteJFGDevice(String uuid) {
        return dataPointManager != null && dataPointManager.deleteJFGDevice(getJfgAccount().getAccount(), uuid);
    }

    @Override
    public Object delete(String uuid, long id) {
        if (dataPointManager == null) return null;
        return dataPointManager.delete(uuid, id);
    }

    @Override
    public Object delete(String uuid, long id, long version) {
        if (dataPointManager == null) return null;
        return dataPointManager.delete(uuid, id, version);
    }

    @Override
    public BaseValue fetchLocal(String uuid, long id) {
        if (dataPointManager == null) return null;
        return dataPointManager.fetchLocal(uuid, id);
    }

    @Override
    public boolean deleteAll(String uuid, long id, ArrayList<Long> versions) {
        return dataPointManager != null && dataPointManager.deleteAll(uuid, id, versions);
    }

    @Override
    public ArrayList<BaseValue> fetchLocalList(String uuid, long id) {
        if (dataPointManager == null) return null;
        return dataPointManager.fetchLocalList(uuid, id);
    }

    @Override
    public boolean isSetType(long id) {
        return dataPointManager != null && dataPointManager.isSetType(id);
    }

    @Override
    public Pair<Integer, BaseValue> fetchUnreadCount(String uuid, long id) throws JfgException {
        if (dataPointManager == null) return null;
        return dataPointManager.fetchUnreadCount(uuid, id);
    }

    @Override
    public boolean markAsRead(String uuid, long id) throws JfgException {
        return dataPointManager != null && dataPointManager.markAsRead(uuid, id);
    }

    @Override
    public long robotGetData(String peer, ArrayList<JFGDPMsg> queryDps, int limit, boolean asc, int timeoutMs) throws JfgException {
        if (dataPointManager == null) return -1;
        return dataPointManager.robotGetData(peer, queryDps, limit, asc, timeoutMs);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String uuid, long id, T defaultValue) {
        if (isSetType(id)) {
            throw new IllegalArgumentException(String.format("id:%s is an array type in the map", id));
        }
        try {
            if (dataPointManager == null) return null;
            BaseValue base = dataPointManager.fetchLocal(uuid, id);
            return base == null || base.getValue() == null ? defaultValue : (T) base.getValue();
        } catch (ClassCastException c) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String uuid, long id) {
        try {
            if (dataPointManager == null) return null;
            T result;
            if (isSetType(id)) {
                result = (T) dataPointManager.fetchLocalList(uuid, id);
            } else {
                result = (T) dataPointManager.fetchLocal(uuid, id);
            }
            return result;
        } catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        }
    }
}
