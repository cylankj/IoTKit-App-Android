package com.cylan.jiafeigou.cache.pool;

import android.text.TextUtils;
import android.util.Pair;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.dp.IDataPoint;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by cylan-hunt on 16-12-26.
 */

public class GlobalDataProxy implements IDataPool {

    private HashMap<String, JFGDevice> jfgDeviceMap = new HashMap<>();
    private static GlobalDataProxy instance;
    private JFGAccount jfgAccount;
    private IDataPoint dataPointManager;
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
    }

    public JFGAccount getJfgAccount() {
        return jfgAccount;
    }

    public void setDataPointManager(IDataPoint dataPointManager) {
        this.dataPointManager = dataPointManager;
    }

    @Override
    public void cacheDevice(JFGDevice jfgDevice) {
        checkAccount();
        jfgDeviceMap.put(jfgAccount.getAccount() + jfgDevice.uuid, jfgDevice);
    }

    @Override
    public JFGDevice fetch(String uuid) {
        checkAccount();
        return jfgDeviceMap.get(jfgAccount.getAccount() + uuid);
    }

    private void checkAccount() {
        if (jfgAccount == null || TextUtils.isEmpty(jfgAccount.getAccount())) {
            if (BuildConfig.DEBUG) throw new IllegalArgumentException("account is null");
            AppLogger.e("account is null");
        }
    }

    @Override
    public boolean insert(String uuid, BaseValue baseValue) {
        return dataPointManager.insert(uuid, baseValue);
    }

    @Override
    public boolean update(String uuid, BaseValue baseValue, boolean sync) {
        return dataPointManager.update(uuid, baseValue, sync);
    }

    @Override
    public boolean deleteAll(String uuid) {
        return dataPointManager.deleteAll(uuid);
    }

    @Override
    public Object delete(String uuid, long id) {
        return dataPointManager.delete(uuid, id);
    }

    @Override
    public Object delete(String uuid, long id, long version) {
        return dataPointManager.delete(uuid, id, version);
    }

    @Override
    public BaseValue fetchLocal(String uuid, long id) {
        return dataPointManager.fetchLocal(uuid, id);
    }

    @Override
    public boolean deleteAll(String uuid, long id, ArrayList<Long> versions) {
        return dataPointManager.deleteAll(uuid, id, versions);
    }

    @Override
    public ArrayList<BaseValue> fetchLocalList(String uuid, long id) {
        return dataPointManager.fetchLocalList(uuid, id);
    }

    @Override
    public boolean isSetType(long id) {
        return dataPointManager.isSetType(id);
    }

    @Override
    public Pair<Integer, BaseValue> fetchUnreadCount(String uuid, long id) throws JfgException {
        return dataPointManager.fetchUnreadCount(uuid, id);
    }

    @Override
    public boolean markAsRead(String uuid, long id) throws JfgException {
        return dataPointManager.markAsRead(uuid, id);
    }

    @Override
    public long robotGetData(String peer, ArrayList<JFGDPMsg> queryDps, int limit, boolean asc, int timeoutMs) throws JfgException {
        return dataPointManager.robotGetData(peer, queryDps, limit, asc, timeoutMs);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String uuid, long id) {
        if (isSetType(id)) {
            throw new IllegalArgumentException(String.format("id:%s is an array type in the map", id));
        }
        try {
            BaseValue base = dataPointManager.fetchLocal(uuid, id);
            return base == null || base.getValue() == null ? null : (T) base.getValue();
        } catch (ClassCastException c) {
            return null;
        }
    }
}
