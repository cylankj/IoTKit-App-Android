package com.cylan.jiafeigou.base.module;


import android.app.PendingIntent;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.JFGHistoryVideo;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.module.tasks.DPSimpleMultiQueryTask;
import com.cylan.jiafeigou.cache.db.module.tasks.DPUpdateTask;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.IDBHelper;
import com.cylan.jiafeigou.cache.video.History;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.INotify;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.NotifyManager;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.view.activity.CameraLiveActivity;
import com.cylan.jiafeigou.n.view.bell.BellLiveActivity;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.OptionsImpl;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
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
    private IPropertyParser propertyParser;
    /**
     * 只缓存当前账号下的数据,一旦注销将会清空所有的缓存,内存缓存方式
     */
    private Map<String, Device> mCachedDeviceMap = new HashMap<>();//和uuid相关的数据缓存
    private Account account;//账号相关的数据全部保存到这里面
    private ArrayList<JFGShareListInfo> shareList = new ArrayList<>();
    private List<Pair<Integer, String>> rawDeviceOrder = new ArrayList<>();
    @Deprecated
    private boolean isOnline;
    private JFGAccount jfgAccount;

    public void initFromDB() {//根据需要初始化
        dbHelper.getActiveAccount()
                .observeOn(Schedulers.io())
                .filter(account -> account != null)
                .map(dpAccount -> {
                    account = dpAccount;
                    account.setOnline(false);
                    return account;
                })
                .map(dpAccount -> {
                    getCacheInstance().postSticky(new RxEvent.AccountArrived(dpAccount));
                    return dpAccount;
                })
                .flatMap(account -> dbHelper.getAccountDevice(account.getAccount()))
                .flatMap(Observable::from)
                .map(device -> {
                    DBOption.RawDeviceOrderOption option = device.option(DBOption.RawDeviceOrderOption.class);
                    mCachedDeviceMap.put(device.getUuid(), device);
                    rawDeviceOrder.add(new Pair<>(option.rawDeviceOrder, device.getUuid()));
                    return device;
                })
                .flatMap(device -> dbHelper.queryDPMsgByUuid(device.uuid)
                        .flatMap(new Func1<List<DPEntity>, Observable<List<DPEntity>>>() {
                            @Override
                            public Observable<List<DPEntity>> call(List<DPEntity> ret) {
                                if (ret != null) {
                                    DataPoint dataPoint;
                                    for (DPEntity dpEntity : ret) {
                                        dataPoint = propertyParser.parser(dpEntity.getMsgId(), dpEntity.getBytes(), dpEntity.getVersion());
                                        dpEntity.setValue(dataPoint, dpEntity.getBytes(), dpEntity.getVersion());
                                        device.updateProperty(dpEntity.getMsgId(), dpEntity);
                                    }
                                }
                                return Observable.just(ret);
                            }
                        }))
                .doOnCompleted(() -> {
                    Collections.sort(rawDeviceOrder, (lhs, rhs) -> lhs.first - rhs.first);
                    getCacheInstance().post(new RxEvent.DevicesArrived(getAllJFGDevice()));
                })
                .subscribe(ret -> {
                }, e -> {
                    //减少不必要的崩溃
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
                });
    }

    @Deprecated
    public void setOnline(boolean online) {
        isOnline = online;
        if (account != null) {
            account.setOnline(online);
        }
    }

    @Override
    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public Device getJFGDevice(String uuid) {
        Device device = mCachedDeviceMap.get(uuid);
        if (device == null) {
            for (Pair<Integer, String> pair : rawDeviceOrder) {
                if (pair.second.equals(uuid)) {
                    return new Device();
                }
            }
        }
        return device;
    }

    @Override
    public List<Device> getAllJFGDevice() {
        Collections.sort(rawDeviceOrder, (lhs, rhs) -> lhs.first - rhs.first);
        List<Device> result = new ArrayList<>(rawDeviceOrder.size());
        List<Pair<Integer, String>> copyList = new ArrayList<>(rawDeviceOrder);
        for (Pair<Integer, String> pair : copyList) {
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
    public void syncAllJFGDevicePropertyManually() {
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
    public ArrayList<Long> getHisDateList(String uuid) {
        return History.getHistory().getDateList(uuid);
    }

    @Override
    public void setDBHelper(IDBHelper dbHelper) {
        this.dbHelper = dbHelper;

    }

    @Override
    public void setPropertyParser(IPropertyParser parser) {
        this.propertyParser = parser;
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
            try {
                AppLogger.d(String.format(Locale.getDefault(), "uuid:%s,version:%s,asc:%s,count:%s", uuid, version, asc, count));
                return JfgCmdInsurance.getCmd().robotGetDataEx(uuid, asc, version, new long[]{505L, 222L, 512}, 0);
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
    public Observable<Boolean> queryHistory(String uuid) {
        return Observable.just(History.getHistory().queryHistory(getJFGDevice(uuid)));
    }

    @Override
    public void cacheHistoryDataList(JFGHistoryVideo historyVideo) {
        History.getHistory().cacheHistoryDataList(historyVideo);
    }

    @Override
    public void clear() {
        getCacheInstance().removeAllStickyEvents();
        isOnline = false;
        account = null;
        jfgAccount = null;
//        if (unreadMap != null) unreadMap.clearLocal();
        if (shareList != null) shareList.clear();
        if (rawDeviceOrder != null) rawDeviceOrder.clear();
        if (mCachedDeviceMap != null) mCachedDeviceMap.clear();
    }

    public void syncJFGDeviceProperty(String uuid) {
        if (TextUtils.isEmpty(uuid) || account == null) return;
        Device device = mCachedDeviceMap.get(uuid);
        if (device != null) {
            ArrayList<JFGDPMsg> parameters = device.getQueryParams();
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
    @Deprecated //无法获取值
    public <T> T getValue(String uuid, long msgId, T defaultValue) {
        T result = null;
        Device device = mCachedDeviceMap.get(uuid);
        if (device != null) {
            //这里优先从根据UUID从device中获取数据
            result = device.$((int) msgId, defaultValue);
        }
        if (result == null && account != null) {
            //如果无法从device中获取值,则从account中获取
            result = account.$((int) msgId, defaultValue);
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
//            shareList.clearLocal();
            setJfgAccount(null);
        } else if (loginState.state == LogState.STATE_ACCOUNT_OFF) {
//            shareList.clearLocal();
        } else {

        }
        AppLogger.i("logState update: " + loginState.state);
    }

    public void setJfgAccount(JFGAccount jfgAccount) {
        this.jfgAccount = jfgAccount;
        AppLogger.i("setJfgAccount:" + (jfgAccount == null));
        if (jfgAccount != null) {
            PreferencesUtils.putString(KEY_ACCOUNT, new Gson().toJson(jfgAccount));
            getCacheInstance().postSticky(new RxEvent.GetUserInfo(jfgAccount));
        } else PreferencesUtils.putString(KEY_ACCOUNT, "");
    }

    @Override
    public boolean updateJFGDevice(Device device) {
        dbHelper.updateDevice(device).subscribe(ret -> {
        }, AppLogger::e);
        return true;
    }

    @Override
    public <T extends DataPoint> boolean updateValue(String uuid, T value, int msgId) throws
            IllegalAccessException {
        ArrayList<T> list = new ArrayList<>();
        value.msgId = msgId;
        value.version = System.currentTimeMillis();
        list.add(value);
        return updateValue(uuid, list);
    }

    @Override
    public <T extends DataPoint> boolean updateValue(String uuid, List<T> value) throws IllegalAccessException {
        Observable.just("update")
                .subscribeOn(Schedulers.newThread())
                .subscribe(s -> {
                    Device device = getJFGDevice(uuid);
                    if (device == null) {
                        AppLogger.e("device is null:" + uuid);
                        return;
                    }
                    try {
                        ArrayList<JFGDPMsg> list = new ArrayList<>();
                        for (DataPoint data : value) {
                            device.setValue((int) data.msgId, data);
                            JFGDPMsg jfgdpMsg = new JFGDPMsg(data.msgId, System.currentTimeMillis());
                            jfgdpMsg.packValue = data.toBytes();
                            list.add(jfgdpMsg);
                        }
                        new DPUpdateTask().init(MiscUtils.msgList(uuid, getAJFGAccount().getAccount(), OptionsImpl.getServer(), list)).performLocal()
                                .subscribeOn(Schedulers.io())
                                .doOnError(throwable -> AppLogger.e("err:" + throwable.getLocalizedMessage()))
                                .subscribe(ret -> {
                                }, throwable -> AppLogger.e("err:" + throwable.getLocalizedMessage()));
                    } catch (Exception e) {
                    }
                }, throwable -> AppLogger.e("err:" + throwable.getLocalizedMessage()));
        return true;
    }

    @Override
    public <T extends DataPoint> boolean clearValue(String uuid, int... msgIdList) throws IllegalAccessException {
        Observable.just("update")
                .subscribeOn(Schedulers.newThread())
                .subscribe(s -> {
                    if (msgIdList == null || msgIdList.length == 0) return;
                    Device device = getJFGDevice(uuid);
                    if (device != null) {
                        ArrayList<JFGDPMsg> list = new ArrayList<>();
                        for (int msgId : msgIdList) {
                            device.setValue(msgId, null);
                            JFGDPMsg jfgdpMsg = new JFGDPMsg(msgId, 0);
                            jfgdpMsg.packValue = DpUtils.pack(0);
                            list.add(jfgdpMsg);
                        }
                        try {
                            new DPUpdateTask().init(MiscUtils.msgList(uuid, getAJFGAccount().getAccount(), OptionsImpl.getServer(), list))
                                    .performLocal()
                                    .subscribeOn(Schedulers.io())
                                    .doOnError(throwable -> AppLogger.e("err:" + throwable.getLocalizedMessage()))
                                    .subscribe(ret -> {
                                    }, throwable -> AppLogger.e("err:" + throwable.getLocalizedMessage()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, throwable -> AppLogger.e("err:" + throwable.getLocalizedMessage()));
        return false;
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

    private void makeCacheAccountSub() {
        getCacheInstance().toObservable(RxEvent.SerializeCacheAccountEvent.class)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(event -> dbHelper.updateAccount(event.account)
                        .map(dpAccount -> {
                            this.account = dpAccount;
                            this.account.setOnline(true);
                            setJfgAccount(event.account);
                            if (jfgAccount != null)
                                setLoginState(new LogState(LogState.STATE_ACCOUNT_ON));
                            else {
                                AppLogger.e("jfgAccount is null");
                            }
                            getCacheInstance().post(account);
                            getCacheInstance().postSticky(new RxEvent.AccountArrived(this.account));
//                            BaseDPTaskDispatcher.getInstance().perform();
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
                        ArrayList<JFGDPMsg> parameters;
                        DBOption.RawDeviceOrderOption option;
                        ArrayList<String> uuidList = new ArrayList<>();
                        for (Device device : devices) {
                            option = device.option(DBOption.RawDeviceOrderOption.class);
                            mCachedDeviceMap.put(device.getUuid(), device);
                            rawDeviceOrder.add(new Pair<>(option.rawDeviceOrder, device.getUuid()));
                            parameters = device.getQueryParams();
                            AppLogger.d("QueryParams:" + new Gson().toJson(parameters));
                            JfgCmdInsurance.getCmd().robotGetData(device.getUuid(), parameters, 1, false, 0);
                            AppLogger.d("正在同步设备数据");
                            if (!JFGRules.isShareDevice(device)) {
                                uuidList.add(device.getUuid());
                            }
                        }
                        JfgCmdInsurance.getCmd().getShareList(uuidList);
                        AppLogger.d("正在请求共享账号数据");
                        getCacheInstance().post(new RxEvent.DevicesArrived(getAllJFGDevice()));
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
                .flatMap(event -> dbHelper.saveDPByteInTx(event.getDataRsp).map(dpEntities -> event))
                .subscribe(new Subscriber<RxEvent.SerializeCacheGetDataEvent>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        AppLogger.e(e.getMessage());
                        e.printStackTrace();
                        makeCacheGetDataSub();
                    }

                    @Override
                    public void onNext(RxEvent.SerializeCacheGetDataEvent event) {
                        RxBus.getCacheInstance().post(event.getDataRsp);
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
                            }
                            RxBus.getCacheInstance().postSticky(new RxEvent.DeviceSyncRsp().setUuid(event.s, updateIdList, event.arrayList));
                            AppLogger.d("正在同步数据");
                            handleSystemNotification(event.arrayList, event.s);
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

    /**
     * 简单发起一个通知
     *
     * @param arrayList
     */
    private void handleSystemNotification(ArrayList<JFGDPMsg> arrayList, String uuid) {
        Device device = getJFGDevice(uuid);
        //需要考虑,app进入后台.
        if (device != null && !TextUtils.isEmpty(device.account)) {
            ArrayList<JFGDPMsg> list = new ArrayList<>(arrayList);
            for (int i = 0; i < ListUtils.getSize(list); i++) {
                long msgId = list.get(i).id;
                if (msgId == 505 || msgId == 512 || msgId == 222) {
                    AppLogger.d("may fire a notification: " + msgId);
                    //cam 1001 1002  1003
                    INotify.NotifyBean bean = new INotify.NotifyBean();
                    bean.resId = R.drawable.icon_home_doorbell_online;
                    try {
                        AppLogger.d("通知栏..." + list.get(i));
                        bean.time = list.get(i).version;
                        bean.resId = R.mipmap.ic_launcher;
                        NotifyManager.getNotifyManager().sendNotify(bean);
                        AppLogger.e("报警消息来了,但是未读数,跟不上.");
                        DPSimpleMultiQueryTask task = new DPSimpleMultiQueryTask()
                                .init(new MiscUtils.DPEntityBuilder()
                                        .add(uuid, 1001, 0, true)
                                        .add(uuid, 1002, 0, true)
                                        .add(uuid, 1003, 0, true)
                                        .build());
                        task.run().subscribeOn(Schedulers.newThread())
                                .subscribe(baseDPTaskResult -> {
                                    Device dd = BaseApplication.getAppComponent().getSourceManager().getJFGDevice(uuid);
                                    String alias = TextUtils.isEmpty(dd.alias) ? dd.uuid : dd.alias;
                                    DPEntity entity = MiscUtils.getMaxVersionEntity(dd.getProperty(1001), dd.getProperty(1002), dd.getProperty(1003));
                                    bean.time = entity.getVersion();
                                    bean.resId = R.mipmap.ic_launcher;
                                    bean.notificationId = (uuid + "cam").hashCode();
                                    bean.content = alias;
                                    int count = entity.getValue(0);
                                    bean.subContent = ContextUtils.getContext().getString(R.string.receive_new_news, count > 99 ? "99+" : count);
                                    final Intent intent = new Intent(ContextUtils.getContext(), CameraLiveActivity.class);
                                    intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                                    intent.putExtra("jump_to_message", "jump_to_message");
                                    bean.pendingIntent = PendingIntent.getActivity(ContextUtils.getContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
                                    NotifyManager.getNotifyManager().sendNotify(bean);
                                }, AppLogger::e);
                    } catch (Exception e) {

                    }
                } else if (msgId == 401) {
                    AppLogger.d("may fire a notification: " + msgId);
                    //for bell 1004 1005
                    INotify.NotifyBean bean = new INotify.NotifyBean();
                    bean.resId = R.drawable.icon_home_doorbell_online;
                    try {
                        DPSimpleMultiQueryTask task = new DPSimpleMultiQueryTask()
                                .init(new MiscUtils.DPEntityBuilder()
                                        .add(uuid, 1004, 0, true)
                                        .add(uuid, 1005, 0, true)
                                        .build());
                        task.run().subscribeOn(Schedulers.newThread())
                                .subscribe(baseDPTaskResult -> {
                                    Device dd = getJFGDevice(uuid);
                                    DPEntity entity = MiscUtils.getMaxVersionEntity(dd.getProperty(1004), dd.getProperty(1005));
                                    AppLogger.d("通知栏..." + entity);
                                    Intent intent = new Intent(ContextUtils.getContext(), BellLiveActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra(JConstant.VIEW_CALL_WAY, JConstant.VIEW_CALL_WAY_VIEWER);
                                    intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                                    int count = entity.getValue(0);
                                    final String title = count == 0 ? ContextUtils.getContext().getString(R.string.app_name) :
                                            String.format(ContextUtils.getContext().getString(R.string.app_name) + "(%s%s)", count, ContextUtils.getContext().getString(R.string.DOOR_NOT_CONNECT));
                                    final String subTitle = count == 0 ?
                                            ContextUtils.getContext().getString(R.string.Slogan) : ContextUtils.getContext().getString(R.string.EFAMILY_MISSED_CALL);
                                    bean.time = entity.getVersion();
                                    bean.resId = R.mipmap.ic_launcher;
                                    bean.pendingIntent = PendingIntent.getActivity(ContextUtils.getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                    bean.time = entity.getVersion();
                                    bean.resId = R.mipmap.ic_launcher;
                                    bean.notificationId = (uuid + "bell").hashCode();
                                    bean.content = title;
                                    bean.subContent = subTitle;
                                    NotifyManager.getNotifyManager().sendNotify(bean);
                                }, throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void initAccount() {
        dbHelper.getActiveAccount().subscribe(ret -> this.account = ret, e -> AppLogger.d(e.getMessage()));
    }

    @Override
    public void initSubscription() {
        makeCacheGetDataSub();
        makeCacheSyncDataSub();
        makeCacheAccountSub();
        makeCacheDeviceSub();
    }
}
