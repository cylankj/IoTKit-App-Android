package com.cylan.jiafeigou.base.module;


import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGHistoryVideo;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.entity.jniCall.JFGVideo;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.cache.db.impl.BaseDPHelper;
import com.cylan.jiafeigou.cache.video.History;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.cylan.jiafeigou.misc.JConstant.KEY_ACCOUNT;
import static com.cylan.jiafeigou.misc.JConstant.KEY_ACCOUNT_LOG_STATE;

/**
 * Created by yzd on 16-12-28.
 */

public class DataSourceManager implements JFGSourceManager {
    private final String TAG = getClass().getName();
    /**
     * 只缓存当前账号下的数据,一旦注销将会清空所有的缓存,内存缓存方式
     */
    private Map<String, JFGDPDevice> mCachedDeviceMap = new HashMap<>();//和uuid相关的数据缓存
    private JFGDPAccount mJFGAccount;//账号相关的数据全部保存到这里面
    private static DataSourceManager mDataSourceManager;
    private ArrayList<JFGShareListInfo> shareList = new ArrayList<>();
    @Deprecated
    private boolean isOnline;

    private DataSourceManager() {
    }

    public static DataSourceManager getInstance() {
        if (mDataSourceManager == null) {
            synchronized (DataSourceManager.class) {
                if (mDataSourceManager == null) {
                    mDataSourceManager = new DataSourceManager();
                }
            }
        }
        return mDataSourceManager;
    }

    @Deprecated
    public void setOnline(boolean online) {
        isOnline = online;
//        if (!(getLoginState = online)) {//没有登录的话则清除所有的缓存
//            mCachedDeviceMap.clear();
//            mJFGAccount = null;
//        }
        //什么也不做,防止程序崩溃
    }

    @Override
    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public <T extends JFGDPDevice> T getJFGDevice(String uuid) {
        Object o = mCachedDeviceMap.get(uuid);

        return (T) o;

//        if (device == null&& BuildConfig.DEBUG) throw new IllegalArgumentException("天啊,它真的发生了,你是不是又在乱传参数???");
//        return device == null ? null : getValueWithAccountCheck((T) device.$());
    }

    @Override
    public List<JFGDPDevice> getAllJFGDevice() {
        List<JFGDPDevice> result = new ArrayList<>(mCachedDeviceMap.size());
        for (Map.Entry<String, ? extends DataPoint> entry : mCachedDeviceMap.entrySet()) {
            result.add(getJFGDevice(entry.getKey()));
        }
        return getValueWithAccountCheck(result);
    }

    @Override
    public boolean delLocalJFGDevice(String uuid) {
        boolean result = mCachedDeviceMap.remove(uuid) != null;
        AppLogger.d("unbind dev: " + result + " " + uuid);
        return result;
    }

    @Override
    public boolean delRemoteJFGDevice(String uuid) {
        try {
            JfgCmdInsurance.getCmd().unBindDevice(uuid);
            return true;
        } catch (JfgException e) {
            return false;
        }
    }

    public List<JFGDPDevice> getJFGDeviceByPid(int... pids) {
        if (pids == null) return null;

        List<JFGDPDevice> result = new ArrayList<>();
        for (Map.Entry<String, JFGDPDevice> device : mCachedDeviceMap.entrySet()) {
            for (int pid : pids) {
                if (device.getValue().pid == pid) {
                    result.add(getJFGDevice(device.getKey()));
                    break;
                }
            }
        }
        return getValueWithAccountCheck(result);
    }

    public List<String> getJFGDeviceUUIDByPid(int... pids) {
        if (pids == null) return null;
        List<String> result = new ArrayList<>();
        List<JFGDPDevice> devices = getJFGDeviceByPid(pids);
        for (JFGDPDevice device : devices) {
            result.add(device.uuid);
        }
        return getValueWithAccountCheck(result);
    }

    @Override
    public void cacheJFGDevices(com.cylan.entity.jniCall.JFGDevice... devices) {
        for (com.cylan.entity.jniCall.JFGDevice device : devices) {
            Log.d("uuid", "uuid: " + new Gson().toJson(device));
            JFGDPDevice temp = mCachedDeviceMap.get(device.uuid);
            if (temp != null) {//已经存在了,则更新即可
                temp.setDevice(device);
            } else {//不存在,则添加
                JFGDPDevice jfgDevice = create(device);
                if (jfgDevice != null) mCachedDeviceMap.put(device.uuid, jfgDevice);
            }
        }
        syncAllJFGDeviceProperty();
    }

    @Override
    public void cacheJFGAccount(com.cylan.entity.jniCall.JFGAccount account) {
        if (jfgAccount != null)
            DataSourceManager.getInstance().setLoginState(new LogState(LogState.STATE_ACCOUNT_ON));
        else {
            AppLogger.e("jfgAccount is null");
        }
        setJfgAccount(account);
        mJFGAccount = new JFGDPAccount().setAccount(account);
        syncAllJFGDeviceProperty();
        RxBus.getCacheInstance().postSticky(new RxEvent.GetUserInfo(jfgAccount));
    }


    //主动发起请求,来获取设备所有的属性
    @Override
    public void syncAllJFGDeviceProperty() {
        if (mCachedDeviceMap.size() == 0) return;
        ArrayList<String> uuidList = new ArrayList<>();
        for (Map.Entry<String, JFGDPDevice> entry : mCachedDeviceMap.entrySet()) {
            JFGDPDevice device = mCachedDeviceMap.get(entry.getKey());
            syncJFGDeviceProperty(entry.getKey());
            //非分享设备需要一些属性
            if (!JFGRules.isShareDevice(device)) {
                uuidList.add(device.uuid);
            }
        }
        /**
         * 设备分享列表
         */
        JfgCmdInsurance.getCmd().getShareList(uuidList);
//        syncAllJFGCameraWarnMsg(true);
    }

    /**
     * 获取所有的报警消息{505,222}，1：保证有最新的报警消息，2.用于显示xx条新消息。
     *
     * @param ignoreShareDevice:忽略分享账号，一般都为true
     */
    @Override
    public void syncAllJFGCameraWarnMsg(boolean ignoreShareDevice) {
        for (Map.Entry<String, JFGDPDevice> entry : mCachedDeviceMap.entrySet()) {
            JFGDPDevice device = mCachedDeviceMap.get(entry.getKey());
            if (JFGRules.isShareDevice(device) && ignoreShareDevice) continue;
            syncJFGCameraWarn(entry.getKey(), false, 100);
        }
    }

    /**
     * 需要暴力操作。
     * 服务端任务太多，太杂，暂时实现不了。
     * 自力更生
     *
     * @param uuid
     */
    @Override
    public long syncJFGCameraWarn(String uuid, boolean asc, int count) {
        ArrayList<JFGDPMsg> list = MiscUtils.createGetCameraWarnMsgDp();
        try {
            return JfgCmdInsurance.getCmd().robotGetData(uuid, list, count, false, 0);
        } catch (JfgException e) {
            AppLogger.e("uuid is null");
            return 0L;
        }
    }

    @Override
    public void queryHistory(String uuid) {
        try {
            JfgCmdInsurance.getCmd().getVideoList(uuid);
        } catch (JfgException e) {
            AppLogger.e("uuid is null: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void cacheHistoryDataList(JFGHistoryVideo historyVideo) {
        History.getHistory().cacheHistoryDataList(historyVideo);
    }

    @Override
    public ArrayList<JFGVideo> getHistoryList(String uuid) {
        return History.getHistory().getHistoryList(uuid);
    }

    @Override
    public <T extends DataPoint> List<T> getValueBetween(String uuid, long msgId, long startVersion, long endVersion) {
        List<T> result = new ArrayList<>();
        Object origin = getValue(uuid, msgId);
        if (origin == null)
            return result;
        if (origin instanceof DpMsgDefine.DPSet) {
            DpMsgDefine.DPSet<T> set = (DpMsgDefine.DPSet<T>) origin;
            if (set.value == null) return result;
            for (T t : set.value) {
                if (t.version >= startVersion && t.version < endVersion) {
                    result.add(t);
                }
            }
            return result;
        } else return null;
    }

    public void syncJFGDeviceProperty(String uuid) {
        if (TextUtils.isEmpty(uuid) || mJFGAccount == null) return;
        JFGDPDevice device = mCachedDeviceMap.get(uuid);

        if (device != null) {
            ArrayList<JFGDPMsg> parameters = device.getQueryParameters(false);
            try {
                JfgCmdInsurance.getCmd().robotGetData(uuid, parameters, 1, false, 0);
            } catch (JfgException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public JFGDPAccount getAJFGAccount() {
        return mJFGAccount;
    }

    @Override
    public JFGAccount getJFGAccount() {
        return jfgAccount;
    }

    @Override
    public void cacheRobotoGetDataRsp(RobotoGetDataRsp dataRsp) {
        final String identity = dataRsp.identity;
        JFGDPDevice device = mCachedDeviceMap.get(identity);
        boolean changed = false;
        for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : dataRsp.map.entrySet()) {
            if (entry.getValue() == null) continue;
            changed = false;
            for (JFGDPMsg dp : entry.getValue()) {
                BaseDPHelper.getInstance().saveDPByte(identity, dp.version, (int) dp.id, dp.packValue).subscribe();
                if (device != null) {//优先尝试写入device中
                    changed |= device.setValue(dp, dataRsp.seq);
                    continue;
                }
                if (mJFGAccount != null) {//到这里说明无法将数据写入device中,则写入到account中
                    changed |= mJFGAccount.setValue(dp, dataRsp.seq);
                    if (changed) mJFGAccount.version = System.currentTimeMillis();
                }
            }

            //每一个响应都需要被通知,即使没有数据变化,以免客户端无限等待
            RxEvent.GetDataResponse response = new RxEvent.GetDataResponse();
            response.changed = changed;
            response.seq = dataRsp.seq;
            response.msgId = entry.getKey();
            RxBus.getCacheInstance().post(response);
        }

        RxEvent.ParseResponseCompleted completed = new RxEvent.ParseResponseCompleted();
        completed.seq = dataRsp.seq;
        completed.uuid = dataRsp.identity;
        RxBus.getCacheInstance().post(completed);
        if (changed) {
            long version = System.currentTimeMillis();
            if (device != null) device.version = version;
            else if (mJFGAccount != null) mJFGAccount.version = version;
        }
        RxBus.getCacheInstance().post(dataRsp);
    }

    @Override
    public void cacheRobotoSyncData(boolean b, String s, ArrayList<JFGDPMsg> arrayList) {
        JFGDPDevice device = mCachedDeviceMap.get(s);
        if (device != null) {
//            boolean changed = false;//
            ArrayList<Long> updateIdList = new ArrayList<>();
            for (JFGDPMsg msg : arrayList) {
//                changed |= device.setValue(msg);
                device.setValue(msg);
                updateIdList.add(msg.id);
            }
//            if (changed) {//消息量不大，尽可刷新
            device.version = System.currentTimeMillis();
            RxBus.getCacheInstance().postSticky(new RxEvent.DeviceSyncRsp().setUuid(s, updateIdList));
//            }
        }
    }

    @Override
    public <T extends DataPoint> T getValue(String uuid, long msgId) {
        return getValue(uuid, msgId, -1);
    }

    @Override
    public <T extends DataPoint> T getValueSafe(String uuid, long msgId, Object defaultValue) {
        T object = getValue(uuid, msgId, -1);
        if (object == null) {
            if (defaultValue instanceof Set) {
                return (T) new DpMsgDefine.DPSet(new TreeSet());
            } else if (!(defaultValue instanceof DataPoint)) {
                return (T) new DpMsgDefine.DPPrimary(defaultValue);
            } else {
                return (T) defaultValue;
            }
        } else return object;
    }

    public <T extends DataPoint> T getValue(String uuid, long msgId, long seq) {
        T result = null;
        JFGDPDevice device = mCachedDeviceMap.get(uuid);
        if (device != null) {
            //这里优先从根据UUID从device中获取数据
            result = device.$().getValue(msgId, seq);
        }
        if (result == null && mJFGAccount != null) {
            //如果无法从device中获取值,则从account中获取
            result = mJFGAccount.$().getValue(msgId, seq);
        }
        return getValueWithAccountCheck(result);
    }

    public <T> T getValueWithAccountCheck(T value) {
//        if (mJFGAccount == null || !getLoginState) {
//            return null;
//        }
        return value;
    }


    private JFGDPDevice create(com.cylan.entity.jniCall.JFGDevice device) {
        //摄像头设备
        if (JFGRules.isCamera(device.pid)) {
            return new JFGCameraDevice().setDevice(device);
        }
        //门铃设备
        if (JFGRules.isBell(device.pid))
            return new JFGDoorBellDevice().setDevice(device);
        return new JFGDPDevice() {
        };
    }

    @Override
    public void cacheShareList(ArrayList<JFGShareListInfo> arrayList) {
        if (shareList == null) shareList = new ArrayList<>();
        shareList.clear();
        shareList.addAll(arrayList);
        Log.d("shareList", "shareList: " + new Gson().toJson(shareList));
        RxBus.getCacheInstance().post(new RxEvent.GetShareListRsp());
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

    public void setLoginState(LogState loginState) {
        PreferencesUtils.putInt(KEY_ACCOUNT_LOG_STATE, loginState.state);
        if (loginState.state == LogState.STATE_NONE) {
//            shareList.clear();
            setJfgAccount(null);
        } else if (loginState.state == LogState.STATE_ACCOUNT_OFF) {
//            shareList.clear();
        } else {

        }
        AppLogger.i("logState update: " + loginState.state);
    }

    private JFGAccount jfgAccount;

    public void setJfgAccount(JFGAccount jfgAccount) {
        this.jfgAccount = jfgAccount;
        if (jfgAccount != null)
            this.mJFGAccount = new JFGDPAccount().setAccount(jfgAccount);
        else this.mJFGAccount = null;
        AppLogger.i("setJfgAccount:" + (jfgAccount == null));
        if (jfgAccount != null) {
            PreferencesUtils.putString(KEY_ACCOUNT, new Gson().toJson(jfgAccount));
        } else PreferencesUtils.putString(KEY_ACCOUNT, "");
        RxBus.getCacheInstance().post(jfgAccount);
    }

    @Override
    public boolean updateJFGDevice(JFGDPDevice device) {
        JFGDPDevice temp = mCachedDeviceMap.get(device.uuid);
        if (temp != null) {
            //先删除
            mCachedDeviceMap.remove(device.uuid);
            if (BuildConfig.DEBUG) AppLogger.i("更新设备属性");
            return mCachedDeviceMap.put(device.uuid, device) != null;
        }
        return false;
    }

    @Override
    public <T extends DataPoint> boolean updateValue(String uuid, T value, int msgId) throws IllegalAccessException {
        JFGDPDevice device = getJFGDevice(uuid);
        if (device == null) {
            AppLogger.e("device is null:" + uuid);
            return false;
        }
        return device.updateValue(msgId, value);
    }

    @Override
    public boolean deleteByVersions(String uuid, long id, ArrayList<Long> versions) {
        return false;
    }

    public int getLoginState() {
        JFGAccount account = this.getJFGAccount();
        if (account == null || TextUtils.isEmpty(account.getAccount())) {
            return 0;//无账号
        } else {
            return PreferencesUtils.getInt(KEY_ACCOUNT_LOG_STATE, 0);
        }
    }


}
