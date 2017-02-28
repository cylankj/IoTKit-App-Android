package com.cylan.jiafeigou.cache.pool;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.dp.DeviceFullParameters;
import com.cylan.jiafeigou.dp.IDataPoint;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.util.ArrayList;

import static com.cylan.jiafeigou.misc.JConstant.KEY_ACCOUNT;
import static com.cylan.jiafeigou.misc.JConstant.KEY_ACCOUNT_LOG_STATE;

/**
 * Created by cylan-hunt on 16-12-26.
 */

public class GlobalDataProxy implements IDataProxy {
    //    private DeviceFullParameters deviceFullParameters;
    private static GlobalDataProxy instance;
    private JFGAccount jfgAccount;
    private IDataPoint dataPointManager;
    /**
     * <String(cid),ArrayList<...></>></>
     * 根据账号
     */
    private ArrayList<JFGShareListInfo> shareList = new ArrayList<>();
    @Deprecated
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

    public void cacheRobotoSyncData(boolean b, String s, ArrayList<JFGDPMsg> arrayList) {
        DeviceFullParameters.getInstance().assembleFullParameters(b, s, arrayList);
    }

    @Deprecated
    public void setOnline(boolean online) {
        isOnline = online;
    }

    @Deprecated
    public boolean isOnline() {
        return isOnline;
    }


    public void setLoginState(LogState loginState) {
        PreferencesUtils.putInt(KEY_ACCOUNT_LOG_STATE, loginState.state);
        if (loginState.state == LogState.STATE_NONE) {
            if (dataPointManager != null) dataPointManager.clear();
            setJfgAccount(null);
        } else if (loginState.state == LogState.STATE_ACCOUNT_OFF) {
            if (dataPointManager != null) dataPointManager.clear();
        } else {

        }
        AppLogger.i("logState update: " + loginState.state);
    }

    public int getLoginState() {
        JFGAccount account = getJfgAccount();
        if (account == null || TextUtils.isEmpty(account.getAccount())) {
            return 0;//无账号
        } else {
            return PreferencesUtils.getInt(KEY_ACCOUNT_LOG_STATE, 0);
        }
    }

    public void setJfgAccount(JFGAccount jfgAccount) {
        this.jfgAccount = jfgAccount;
        AppLogger.i("setJfgAccount:" + (jfgAccount == null));
        if (jfgAccount != null) {
            PreferencesUtils.putString(KEY_ACCOUNT, new Gson().toJson(jfgAccount));
        } else PreferencesUtils.putString(KEY_ACCOUNT, "");
        RxBus.getCacheInstance().post(jfgAccount);
    }

    public JFGAccount getJfgAccount() {
        if (jfgAccount == null || TextUtils.isEmpty(jfgAccount.getAccount())) {
            try {
                String content = PreferencesUtils.getString(KEY_ACCOUNT);
                return jfgAccount = new Gson().fromJson(content, JFGAccount.class);
            } catch (Exception e) {
                return null;
            }
        }
        return jfgAccount;
    }

    public void setDataPointManager(IDataPoint dataPointManager) {
        this.dataPointManager = dataPointManager;
    }

    @Override
    public void cacheDevice(JFGDevice... device) {
        if (device != null)
            for (JFGDevice d : device) {
                Log.d("debug", "debug: " + new Gson().toJson(d));
            }
        dataPointManager.cacheDevice(device);
        DeviceFullParameters.getInstance().getDeviceFullParameters(device);
        RxBus.getCacheInstance().postSticky(new RxEvent.DeviceListUpdate());
    }

    @Override
    public void robotGetDataRsp(RobotoGetDataRsp dataRsp) {
        DeviceFullParameters.getInstance().fullDataPointAssembler(dataRsp);
    }

    @Override
    public boolean remove(String uuid) {
        return dataPointManager != null && dataPointManager.remove(uuid);
    }

    @Override
    public JFGDevice fetch(String uuid) {
        if (dataPointManager == null) return null;
        return dataPointManager.fetch(uuid);
    }

    @Override
    public void cacheShareList(ArrayList<JFGShareListInfo> arrayList) {
        if (shareList == null) shareList = new ArrayList<>();
        shareList.clear();
        shareList.addAll(arrayList);
        RxBus.getCacheInstance().post(new RxEvent.GetShareListRsp());
    }

    @Override
    public void cacheUnread(String uuid, Pair<Integer, BaseValue> pair) {
        if (dataPointManager != null) dataPointManager.cacheUnread(uuid, pair);
    }

    @Override
    public boolean isDeviceSharedTo(String uuid) {
        int size = shareList == null ? 0 : shareList.size();
        for (int i = 0; i < size; i++) {
            JFGShareListInfo info = shareList.get(i);
            if (TextUtils.equals(uuid, info.cid)) {
                return info.friends != null && info.friends.size() > 0;
            }
        }
        return false;
    }

    @Override
    public ArrayList<JFGShareListInfo> getShareList() {
        return shareList;
    }

    @Override
    public int updateJFGDevice(JFGDevice device) {
        if (dataPointManager == null) return -1;
        dataPointManager.updateJFGDevice(device);
        //需要修改
        try {
            return JfgCmdInsurance.getCmd().setAliasByCid(device.uuid, device.alias);
        } catch (JfgException e) {
            return -1;
        }
    }

    @Override
    public ArrayList<JFGDevice> fetchAll() {
        if (dataPointManager == null) return null;
        return dataPointManager.fetchAll();
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
        return dataPointManager != null && dataPointManager.deleteJFGDevice(uuid);
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
    public BaseValue fetchLocal(String uuid, long id, boolean topOne) {
        if (dataPointManager == null) return null;
        return dataPointManager.fetchLocal(uuid, id, topOne);
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
    public long robotGetDataReq(String peer, ArrayList<JFGDPMsg> queryDps, int limit, boolean asc, int timeoutMs) throws JfgException {
        if (dataPointManager == null) return -1;
        return dataPointManager.robotGetData(peer, queryDps, limit, asc, timeoutMs);
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T getValue(String uuid, long id, T defaultValue) {
        if (isSetType(id)) {
            throw new IllegalArgumentException(String.format("id:%s is an array type in the map", id));
        }
        try {
            if (dataPointManager == null) return null;
            BaseValue base = dataPointManager.fetchLocal(uuid, id);
            Log.d("getPackValue", "getPackValue:" + id + " base:" + base);
            return base == null || base.getValue() == null ? defaultValue : base.getValue();
        } catch (Exception c) {
            Log.e("getPackValue", "getPackValue:" + id + " base:" + c.getLocalizedMessage());
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
