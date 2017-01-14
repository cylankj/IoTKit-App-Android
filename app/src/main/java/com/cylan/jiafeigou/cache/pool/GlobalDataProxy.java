package com.cylan.jiafeigou.cache.pool;

import android.text.TextUtils;
import android.util.Pair;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.dp.IDataPoint;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-12-26.
 */

public class GlobalDataProxy implements IDataProxy {

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
        if (checkAccount())
            dataPointManager.cacheDevice(jfgAccount.getAccount() + uuid, jfgDevice);
    }

    @Override
    public boolean remove(String uuid) {
        if (!checkAccount()) return false;
        return dataPointManager.remove(jfgAccount.getAccount() + uuid);
    }

    @Override
    public JFGDevice fetch(String uuid) {
        if (!checkAccount()) return null;
        return dataPointManager.fetch(jfgAccount.getAccount() + uuid);
    }

    @Override
    public boolean updateJFGDevice(JFGDevice device) {
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
        if (!checkAccount()) return null;
        return dataPointManager.fetchAll(jfgAccount.getAccount());
    }

    private boolean checkAccount() {
        if (jfgAccount == null || TextUtils.isEmpty(jfgAccount.getAccount())) {
//            if (BuildConfig.DEBUG) throw new IllegalArgumentException("account is null");
            //we just clear the cache if the account is null
            dataPointManager.clearAll();
            AppLogger.e("account is null");
            return false;
        }
        return true;
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
    public boolean deleteJFGDevice(String uuid) {
        return dataPointManager.deleteJFGDevice(getJfgAccount().getAccount(), uuid);
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
    public <T> T getValue(String uuid, long id, T defaultValue) {
        if (isSetType(id)) {
            throw new IllegalArgumentException(String.format("id:%s is an array type in the map", id));
        }
        try {
            BaseValue base = dataPointManager.fetchLocal(uuid, id);
            return base == null || base.getValue() == null ? defaultValue : (T) base.getValue();
        } catch (ClassCastException c) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String uuid, long id) {
        try {
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
