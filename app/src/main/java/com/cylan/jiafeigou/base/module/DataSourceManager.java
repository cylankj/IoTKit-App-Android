package com.cylan.jiafeigou.base.module;


import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.SparseLongArray;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDPMsgCount;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.JFGHistoryVideo;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.entity.jniCall.JFGVideo;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.IDBHelper;
import com.cylan.jiafeigou.cache.video.History;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.JConstant.KEY_ACCOUNT;
import static com.cylan.jiafeigou.misc.JConstant.KEY_ACCOUNT_LOG_STATE;
import static com.cylan.jiafeigou.rx.RxBus.getCacheInstance;

/**
 * Created by yzd on 16-12-28.
 */

public class DataSourceManager implements JFGSourceManager {
    private final String TAG = getClass().getName();

    private IDBHelper dbHelper;
    /**
     * 只缓存当前账号下的数据,一旦注销将会清空所有的缓存,内存缓存方式
     */
    private Map<String, Device> mCachedDeviceMap = new HashMap<>();//和uuid相关的数据缓存
    private Account account;//账号相关的数据全部保存到这里面
    private static DataSourceManager mDataSourceManager;
    private ArrayList<JFGShareListInfo> shareList = new ArrayList<>();
    private Subscription unreadCountFetcher;
    private List<Pair<Integer, String>> rawDeviceOrder = new ArrayList<>();
    /**
     * 未读消息数
     */
    private HashMap<String, SparseLongArray> unreadMap = new HashMap<>();
    @Deprecated
    private boolean isOnline;
    private JFGAccount jfgAccount;


    private DataSourceManager() {
        dbHelper = BaseDBHelper.getInstance();
        dbHelper.getActiveAccount().subscribe(ret -> {
            account = ret;
        }, e -> {
            AppLogger.d(e.getMessage());
            e.printStackTrace();
        });
        initSubscription();
    }

    private void initSubscription() {
        makeCacheGetDataSub();
        makeCacheSyncDataSub();
        makeCacheAccountSub();
        makeCacheDeviceSub();
    }

    public void initFromDB() {//根据需要初始化
        dbHelper.getActiveAccount()
                .observeOn(Schedulers.io())
                .filter(account -> account != null)
                .map(dpAccount -> account = dpAccount)
                .flatMap(dpAccount -> dbHelper.queryDPMsgByUuid(null)
                        .observeOn(Schedulers.io())
                        .map(dpEntities -> {
                            for (DPEntity entity : dpEntities) {
                                dpAccount.setValue(entity.getMsgId(), entity.getVersion(), entity.getBytes(), -1);
                            }
                            getCacheInstance().postSticky(new RxEvent.AccountArrived(dpAccount));
                            return dpAccount;
                        })
                )
                .flatMap(account -> dbHelper.getAccountDevice(account.getAccount()))
                .flatMap(Observable::from)
                .map(device -> {
                    Device dev = create(device.getPid()).fill(device);
                    DBOption.RawDeviceOrderOption option = dev.option(DBOption.RawDeviceOrderOption.class);
                    mCachedDeviceMap.put(device.getUuid(), dev);
                    rawDeviceOrder.add(new Pair<>(option.rawDeviceOrder, dev.getUuid()));
                    return dev;
                })
                .flatMap(device -> dbHelper.queryDPMsgByUuid(device.uuid)
                        .observeOn(Schedulers.io())
                        .map(dpEntities -> {
                            for (DPEntity entity : dpEntities) {
                                device.setValue(entity.getMsgId(), entity.getVersion(), entity.getBytes(), -1);
                            }
                            return dpEntities;
                        }))
                .doOnCompleted(() -> {
                    Collections.sort(rawDeviceOrder, (lhs, rhs) -> lhs.first - rhs.first);
                    getCacheInstance().postSticky(new RxEvent.DevicesArrived(getAllJFGDevice()));
                })
                .subscribe(ret -> {
                }, e -> {
                    //减少不必要的崩溃
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
                });
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
    }

    @Override
    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public <T extends Device> T getJFGDevice(String uuid) {
        Object o = mCachedDeviceMap.get(uuid);
        return (T) o;
    }

    @Override
    public List<Device> getAllJFGDevice() {
        Collections.sort(rawDeviceOrder, (lhs, rhs) -> lhs.first - rhs.first);
        List<Device> result = new ArrayList<>(rawDeviceOrder.size());
        for (Pair<Integer, String> pair : rawDeviceOrder) {
            Device d = mCachedDeviceMap.get(pair.second);
            if (!result.contains(d))
                result.add(d);
            else {
                AppLogger.d("yes list contains d: " + d);
            }
        }
        return result;
    }

    public List<Device> getJFGDeviceByPid(int... pids) {
        if (pids == null) return null;

        List<Device> result = new ArrayList<>();
        for (Map.Entry<String, Device> device : mCachedDeviceMap.entrySet()) {
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
        List<Device> devices = getJFGDeviceByPid(pids);
        for (Device device : devices) {
            result.add(device.uuid);
        }
        return getValueWithAccountCheck(result);
    }

    //主动发起请求,来获取设备所有的属性
    @Override
    public void syncAllJFGDeviceProperty() {
        if (mCachedDeviceMap.size() == 0) return;
        ArrayList<String> uuidList = new ArrayList<>();
        for (Map.Entry<String, Device> entry : mCachedDeviceMap.entrySet()) {
            Device device = mCachedDeviceMap.get(entry.getKey());
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
     * 很暴力地获取
     */

    public void syncDeviceUnreadCount() {
        for (Map.Entry<String, Device> entry : new HashMap<>(mCachedDeviceMap).entrySet()) {
            Device device = entry.getValue();
            if (JFGRules.isCamera(device.pid)) {
                try {
                    JfgCmdInsurance.getCmd().robotCountData(device.uuid, new long[]{505, 222, 512, 401}, 0);
                } catch (Exception e) {
                    AppLogger.e("uuid is null: " + e.getLocalizedMessage());
                }
            }
        }
    }

    private Subscription getUnreadCountFetcherSub() {
        return getCacheInstance().toObservable(RobotoGetDataRsp.class)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .throttleLast(5, TimeUnit.SECONDS)
                .subscribe(rsp -> syncDeviceUnreadCount(), e -> {
                    AppLogger.d(e.getMessage());
                    e.printStackTrace();
                });
    }

    @Override
    public Observable<Account> logout() {
        return dbHelper.logout()
                .map(ret -> {
                    setLoginState(new LogState(LogState.STATE_ACCOUNT_OFF));
                    clear();
                    return ret;
                });
    }

    @Override
    public Observable<Device> unBindDevice(String uuid) {
        return unBindDevices(Collections.singletonList(uuid))
                .filter(devices -> devices != null && devices.iterator().hasNext())
                .flatMap(devices -> Observable.just(devices.iterator().next()));
    }

    @Override
    public void setValue(String uuid, int msgId, byte[] bytes, long version, long seq) {
        Device device = mCachedDeviceMap.get(uuid);
        if (device != null && device.accept(msgId)) {//优先尝试写入device中
            device.setValue(msgId, version, bytes, -seq);
        } else if (account != null && account.accept(msgId)) {//到这里说明无法将数据写入device中,则写入到account中
            account.setValue(msgId, version, bytes, seq);
        }
    }

    @Override
    public void clearValue(String uuid, int msgId) {
        Device device = mCachedDeviceMap.get(uuid);
        if (device != null && device.accept(msgId)) {
            device.clear(msgId);
        } else if (account != null && account.accept(msgId)) {
            account.clear(msgId);
        }
    }

    private Observable<Iterable<Device>> unBindDevices(Iterable<String> uuids) {
        return dbHelper.unBindDeviceWithConfirm(uuids)
                .map(devices -> {
                    if (devices == null) return null;
                    for (Device device : devices) {
                        mCachedDeviceMap.remove(device.getUuid());
                        PreferencesUtils.remove(account.getAccount() + ":" + device.getUuid() + ":" + JConstant.LAST_ENTER_TIME);
                        getCacheInstance().post(new RxEvent.DeviceUnBindedEvent(device.getUuid()));
                    }
                    return devices;
                });
    }

    /**
     * 需要暴力操作。
     * 服务端任务太多，太杂，暂时实现不了。
     * 自力更生
     *
     * @param uuid    区分2.0 3.0设备
     * @param uuid
     * @param version
     * @param asc
     * @param count
     */

//    int queryHistory(String uuid);
    @Override
    public long syncJFGCameraWarn(String uuid, long version, boolean asc, int count) {
        Device device = getJFGDevice(uuid);
        if (device != null) {
            boolean isV2 = TextUtils.isEmpty(device.vid);
            try {
                AppLogger.e("还没实现。。。。。");
//                return  -1;
                return JfgCmdInsurance.getCmd().robotGetDataEx(uuid, asc, version, MiscUtils.getChaosDpList(isV2), 0);

            } catch (Exception e) {
                AppLogger.e("bad ,uuid may be null");
                return -1;
            }
        } else {
            AppLogger.e("bad ,device is null");
            return -1;
        }
    }

    @Override
    public int queryHistory(String uuid) {
        try {
            JfgCmdInsurance.getCmd().getVideoList(uuid);
        } catch (JfgException e) {
            AppLogger.e("uuid is null: " + e.getLocalizedMessage());
        }
        return 0;
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
    public void cacheUnreadCount(long seq, String uuid, ArrayList<JFGDPMsgCount> unreadList) {
        SparseLongArray array = unreadMap.get(uuid);
        if (array == null) {
            array = new SparseLongArray();
        }
        if (unreadList != null) {
            for (JFGDPMsgCount count : unreadList) {
                array.put(count.id, count.count);
            }
            unreadMap.put(uuid, array);
        }
        getCacheInstance().post(new RxEvent.UnreadCount(uuid, seq, unreadList));
    }

    @Override
    public Pair<Integer, Long> getUnreadCount(String uuid, long... ids) {
        if (unreadMap != null && ids != null && ids.length > 0) {
            SparseLongArray array = unreadMap.get(uuid);
            if (array != null) {
                int count = 0;
                long version = 0;
                for (long id : ids) {
                    count += array.get((int) id);
                    try {
                        long v = MiscUtils.getVersion(getValue(uuid, id), false);
                        version = version > v ? version : v;
                    } catch (Exception e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
                }
                return new Pair<>(count, version);
            }
        }
        return null;
    }

    @Override
    public void clear() {
        getCacheInstance().removeAllStickyEvents();
        if (mCachedDeviceMap != null) mCachedDeviceMap.clear();
        isOnline = false;
        account = null;
        jfgAccount = null;
        if (unreadMap != null) unreadMap.clear();
        if (shareList != null) shareList.clear();
        if (unreadCountFetcher != null && unreadCountFetcher.isUnsubscribed())
            unreadCountFetcher.unsubscribe();
    }

    @Override
    public void clearUnread(String uuid, long... ids) {
        try {
            if (ids == null || ids.length == 0) return;
            long[] array = new long[ids.length];
            ArrayList<Long> list = new ArrayList<>();
            for (int i = 0; i < ids.length; i++) {
                array[i] = ids[i];
            }
            JfgCmdInsurance.getCmd().robotCountDataClear(uuid, array, 0);
            boolean result = unreadMap.remove(uuid) != null;
            AppLogger.d("clear unread count：" + result);
        } catch (Exception e) {
        }
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
                if (t.dpMsgVersion >= startVersion && t.dpMsgVersion < endVersion) {
                    result.add(t);
                }
            }
            return result;
        } else return null;
    }

    public void syncJFGDeviceProperty(String uuid) {
        if (TextUtils.isEmpty(uuid) || account == null) return;
        Device device = mCachedDeviceMap.get(uuid);
        if (device != null) {
            ArrayList<JFGDPMsg> parameters = device.getQueryParameters(false);
            AppLogger.d("syncJFGDeviceProperty: " + uuid + " " + new Gson().toJson(parameters));
            try {
                JfgCmdInsurance.getCmd().robotGetData(uuid, parameters, 1, false, 0);
            } catch (JfgException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public Account getAJFGAccount() {
        return account;
    }

    @Override
    public JFGAccount getJFGAccount() {
        if (jfgAccount == null) {
            return new Gson().fromJson(PreferencesUtils.getString(KEY_ACCOUNT), JFGAccount.class);
        }
        return jfgAccount;
    }

    @Override
    public <T extends DataPoint> T getValue(String uuid, long msgId) {
        return getValue(uuid, msgId, -1);
    }

    public <T extends DataPoint> T getValue(String uuid, long msgId, long seq) {
        T result = null;
        Device device = mCachedDeviceMap.get(uuid);
        if (device != null) {
            //这里优先从根据UUID从device中获取数据
            result = device.$(msgId, null);
        }
        if (result == null && account != null) {
            //如果无法从device中获取值,则从account中获取
            result = account.$(msgId, null);
        }
        return getValueWithAccountCheck(result);
    }

    public <T> T getValueWithAccountCheck(T value) {
        return value;
    }


    @Override
    public void cacheShareList(ArrayList<JFGShareListInfo> arrayList) {
        if (shareList == null) shareList = new ArrayList<>();
        shareList.clear();
        shareList.addAll(arrayList);
        Log.d("shareList", "shareList: " + new Gson().toJson(shareList));
        getCacheInstance().post(new RxEvent.GetShareListRsp());
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

    public void setJfgAccount(JFGAccount jfgAccount) {
        this.jfgAccount = jfgAccount;
        if (unreadCountFetcher == null) {
            unreadCountFetcher = getUnreadCountFetcherSub();
        }
        AppLogger.i("setJfgAccount:" + (jfgAccount == null));
        if (jfgAccount != null) {
            PreferencesUtils.putString(KEY_ACCOUNT, new Gson().toJson(jfgAccount));
            getCacheInstance().postSticky(new RxEvent.GetUserInfo(jfgAccount));
        } else PreferencesUtils.putString(KEY_ACCOUNT, "");
    }

    @Override
    public boolean updateJFGDevice(Device device) {
        dbHelper.updateDevice(device).subscribe();
        return true;
    }

    @Override
    public <T extends DataPoint> boolean updateValue(String uuid, T value, int msgId) throws
            IllegalAccessException {
        Device device = getJFGDevice(uuid);
        if (device == null) {
            AppLogger.e("device is null:" + uuid);
            return false;
        }
        boolean update = device.updateValue(msgId, value);
        ArrayList<JFGDPMsg> list = new ArrayList<>();
        JFGDPMsg msg = new JFGDPMsg(msgId, System.currentTimeMillis());
        msg.packValue = value.toBytes();
        list.add(msg);
        try {
            long l = JfgAppCmd.getInstance().robotSetData(uuid, list);
            AppLogger.d("setDataRsp:" + l);
            return true;
        } catch (JfgException e) {
            return false;
        }
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

    /**
     * 不要使用switch case
     *
     * @param pid
     * @return
     */
    private Device create(int pid) {
        Device result = null;
        if (JFGRules.isCamera(pid))
            result = new JFGCameraDevice();
        else if (JFGRules.isBell(pid))
            result = new JFGDoorBellDevice();
        else if (JFGRules.isVRCam(pid))
            result = new JFGCameraDevice();
        else
            result = new Device();
        return result;
    }

    private void makeCacheAccountSub() {
        getCacheInstance().toObservable(RxEvent.SerializeCacheAccountEvent.class)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(event -> dbHelper.updateAccount(event.account)
                        .map(dpAccount -> {
                            this.account = dpAccount;
                            setJfgAccount(event.account);
                            if (jfgAccount != null)
                                setLoginState(new LogState(LogState.STATE_ACCOUNT_ON));
                            else {
                                AppLogger.e("jfgAccount is null");
                            }
                            getCacheInstance().post(account);
                            getCacheInstance().postSticky(new RxEvent.AccountArrived(this.account));
                            BaseDPTaskDispatcher.getInstance().perform();
                            return "";
                        }))
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        AppLogger.d(e.getMessage());
                        e.printStackTrace();
                        makeCacheAccountSub();
                    }

                    @Override
                    public void onNext(String s) {
                        request(1);
                    }

                    @Override
                    public void onStart() {
                        request(1);
                    }
                });
    }

    private void makeCacheDeviceSub() {
        getCacheInstance().toObservable(RxEvent.SerializeCacheDeviceEvent.class)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(event -> {
                    Set<String> result = new TreeSet<>(mCachedDeviceMap.keySet());
                    JFGDevice device;
                    for (int i = 0; i < event.devices.length; i++) {
                        device = event.devices[i];
                        result.remove(device.uuid);
                    }
                    mCachedDeviceMap.clear();
                    rawDeviceOrder.clear();
                    return unBindDevices(result).flatMap(ret -> dbHelper.updateDevice(event.devices));
                })
                .map(devices -> {
                    try {
                        Device dpDevice;
                        ArrayList<JFGDPMsg> parameters;
                        DBOption.RawDeviceOrderOption option;
                        ArrayList<String> uuidList = new ArrayList<>();
                        for (Device device : devices) {
                            dpDevice = create(device.getPid()).fill(device);
                            option = dpDevice.option(DBOption.RawDeviceOrderOption.class);
                            mCachedDeviceMap.put(dpDevice.getUuid(), dpDevice);
                            rawDeviceOrder.add(new Pair<>(option.rawDeviceOrder, dpDevice.getUuid()));
                            parameters = dpDevice.getQueryParameters(false);
                            JfgCmdInsurance.getCmd().robotGetData(dpDevice.getUuid(), parameters, 1, false, 0);
                            AppLogger.d("正在同步设备数据");
                            if (!JFGRules.isShareDevice(device)) {
                                uuidList.add(device.getUuid());
                            }
                        }
                        JfgCmdInsurance.getCmd().getShareList(uuidList);
                        AppLogger.d("正在请求共享账号数据");
                        getCacheInstance().postSticky(new RxEvent.DevicesArrived(getAllJFGDevice()));
                    } catch (JfgException e) {
                        AppLogger.d(e.getMessage());
                        e.printStackTrace();
                    }
                    return "多线程真心麻烦";
                })
                .doOnError(e -> makeCacheDeviceSub())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        AppLogger.d(e.getMessage());
                        e.printStackTrace();
                        makeCacheDeviceSub();
                    }

                    @Override
                    public void onNext(String s) {
                        request(1);
                    }

                    @Override
                    public void onStart() {
                        request(1);
                    }
                });
    }

    private void makeCacheGetDataSub() {
        getCacheInstance().toObservable(RxEvent.SerializeCacheGetDataEvent.class)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(event -> dbHelper.saveDPByteInTx(event.getDataRsp)
                        .map(dpEntities -> {
                            if (event.getDataRsp.map != null) {
                                for (Integer integer : event.getDataRsp.map.keySet()) {//set 类型的每次写入前都要清空,因为无法复用
                                    clearValue(event.getDataRsp.identity, integer);
                                }
                            }
                            for (DPEntity entity : dpEntities) {
                                setValue(event.getDataRsp.identity, entity.getMsgId(), entity.getBytes(), entity.getVersion(), event.getDataRsp.seq);
                            }
                            getCacheInstance().post(event.getDataRsp);
                            return "多线程真心麻烦";
                        })
                )
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        AppLogger.d(e.getMessage());
                        e.printStackTrace();
                        makeCacheGetDataSub();
                    }

                    @Override
                    public void onNext(String s) {
                        request(1);
                    }

                    @Override
                    public void onStart() {
                        request(1);
                    }
                });
    }

    private void makeCacheSyncDataSub() {
        RxBus.getCacheInstance().toObservable(RxEvent.SerializeCacheSyncDataEvent.class)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(event -> dbHelper.saveDPByteInTx(event.s, event.arrayList)
                        .map(dpEntities -> {
                            ArrayList<Long> updateIdList = new ArrayList<>();
                            for (DPEntity entity : dpEntities) {
                                updateIdList.add((long) entity.getMsgId());
                                setValue(event.s, entity.getMsgId(), entity.getBytes(), entity.getVersion(), -1);
                            }
                            RxBus.getCacheInstance().postSticky(new RxEvent.DeviceSyncRsp().setUuid(event.s, updateIdList));
                            return "多线程真是麻烦";
                        }))
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        AppLogger.d(e.getMessage());
                        e.printStackTrace();
                        makeCacheSyncDataSub();
                    }

                    @Override
                    public void onNext(String s) {
                        request(1);
                    }

                    @Override
                    public void onStart() {
                        request(1);
                    }
                });
    }

    public void initAccount() {
        dbHelper.getActiveAccount().subscribe(ret -> this.account = ret, e -> AppLogger.d(e.getMessage()));
    }
}
