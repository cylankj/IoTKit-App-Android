package com.cylan.jiafeigou.base.module;


import android.app.PendingIntent;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Pair;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.JFGFeedbackInfo;
import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.entity.jniCall.JFGHistoryVideo;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.ex.JfgException;
import com.cylan.jfgapp.interfases.AppCmd;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.IDBHelper;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.video.History;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.AutoSignIn;
import com.cylan.jiafeigou.misc.INotify;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.NotifyManager;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.task.FetchFeedbackTask;
import com.cylan.jiafeigou.n.task.FetchFriendsTask;
import com.cylan.jiafeigou.n.task.SysUnreadCountTask;
import com.cylan.jiafeigou.n.view.activity.CameraLiveActivity;
import com.cylan.jiafeigou.n.view.bell.DoorBellHomeActivity;
import com.cylan.jiafeigou.n.view.mine.FeedbackActivity;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.OptionsImpl;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.RandomUtils;
import com.google.gson.Gson;

import org.msgpack.MessagePack;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.JConstant.KEY_ACCOUNT_LOG_STATE;
import static com.cylan.jiafeigou.rx.RxBus.getCacheInstance;

/**
 * Created by yzd on 16-12-28.
 */

public class DataSourceManager implements JFGSourceManager {
    private final String TAG = getClass().getName();

    private IDBHelper dbHelper;
    private IPropertyParser propertyParser;
    private AppCmd appCmd;
    private int storageType;
    private LogState logState;
    private Map<String, Object> deviceState = new HashMap<>();
    /**
     * 只缓存当前账号下的数据,一旦注销将会清空所有的缓存,内存缓存方式
     */
    private Map<String, Device> mCachedDeviceMap = new ConcurrentHashMap<>();//和uuid相关的数据缓存
    private Account account;//账号相关的数据全部保存到这里面
    private ArrayList<JFGShareListInfo> shareList = new ArrayList<>();
    private List<Pair<Integer, String>> rawDeviceOrder = new ArrayList<>();
    private ArrayList<JFGFriendAccount> friendBeanArrayList;
    private ArrayList<JFGFriendRequest> friendsReqBeanArrayList;

    @Deprecated
    private boolean isOnline = true;
    private JFGAccount jfgAccount;

    private HashMap<Long, Interceptors> dpSeqRspInterceptor = new HashMap<>();
    private static DataSourceManager instance;
    private int loginType;

    public static DataSourceManager getInstance() {
        return instance;
    }

    public DataSourceManager() {
        instance = this;
    }

    public void initFromDB() {//根据需要初始化
        dbHelper.getActiveAccount()
                .observeOn(Schedulers.io())
                .filter(account -> account != null)
                .map(dpAccount -> {
                    account = dpAccount;
                    int anInt = PreferencesUtils.getInt(KEY_ACCOUNT_LOG_STATE, LogState.STATE_NONE);
                    if (anInt == LogState.STATE_ACCOUNT_ON) {
                        account.setOnline(true);
                    } else {
                        account.setOnline(false);
                    }
                    return account;
                })
                .map(dpAccount -> {
                    RxEvent.AccountArrived accountArrived = new RxEvent.AccountArrived(dpAccount);
                    accountArrived.jfgAccount = new Gson().fromJson(dpAccount.getAccountJson(), JFGAccount.class);
                    getCacheInstance().postSticky(accountArrived);
                    AppLogger.d("正在从数据库初始化...");
                    return dpAccount;
                })
                .flatMap(account -> dbHelper.getAccountDevice(account.getAccount()))
                .flatMap(Observable::from)
                .map(device -> {
                    DBOption.DeviceOption option = device.option(DBOption.DeviceOption.class);
                    mCachedDeviceMap.put(device.getUuid(), device);
                    rawDeviceOrder.add(new Pair<>(option.rawDeviceOrder, device.getUuid()));
                    return device;
                })
                .flatMap(device -> dbHelper.queryDPMsgByUuid(device.uuid)
                        .map(ret -> {
                            if (ret != null) {
                                DataPoint dataPoint;
                                for (DPEntity dpEntity : ret) {
                                    dataPoint = propertyParser.parser(dpEntity.getMsgId(), dpEntity.getBytes(), dpEntity.getVersion());
                                    dpEntity.setValue(dataPoint, dpEntity.getBytes(), dpEntity.getVersion());
                                    device.updateProperty(dpEntity.getMsgId(), dpEntity);
                                }
                            }
                            return ret;
                        }))
                .doOnCompleted(() -> {
                    Collections.sort(rawDeviceOrder, (lhs, rhs) -> lhs.first - rhs.first);
                    getCacheInstance().post(new RxEvent.DevicesArrived(getAllDevice()));
//                    queryForwardInformation();
                })
                .subscribe(ret -> {
                }, e -> {
                    //减少不必要的崩溃
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
                });
    }

    @Override
    public ArrayList<JFGFriendAccount> getFriendsList() {
        return this.friendBeanArrayList;
    }

    @Override
    public void setFriendsList(ArrayList<JFGFriendAccount> list) {
        this.friendBeanArrayList = list;
        RxBus.getCacheInstance().post(new RxEvent.GetFriendList());
    }

    @Override
    public void setFriendsReqList(ArrayList<JFGFriendRequest> list) {
        friendsReqBeanArrayList = list;
        RxBus.getCacheInstance().postSticky(new RxEvent.GetAddReqList());
    }

    @Override
    public ArrayList<JFGFriendRequest> getFriendsReqList() {
        return friendsReqBeanArrayList;
    }

    @Override
    public void pushDeviceState(String uuid, PanoramaEvent.MsgVideoStatusRsp videoStatusRsp) {
        deviceState.put(uuid, videoStatusRsp);
        RxBus.getCacheInstance().post(RxEvent.DeviceRecordStateChanged.INSTANCE);
    }

    @Override
    public void removeDeviceState(String uuid) {
        Object remove = deviceState.remove(uuid);
        if (remove != null) {
            RxBus.getCacheInstance().post(RxEvent.DeviceRecordStateChanged.INSTANCE);
        }
    }

    @Override
    public Object getDeviceState(String uuid) {
        return deviceState.get(uuid);
    }

    @Override
    public int getLoginType() {
        return loginType;
    }

    private void queryForwardInformation() {
        //这里尝试从 udp 获取设备属性
        try {
            List<String> dstArray = new ArrayList<>();
            ArrayList<JFGDPMsg> queryParameters = null;
            for (Map.Entry<String, Device> entry : mCachedDeviceMap.entrySet()) {
                dstArray.add(entry.getKey());
                queryParameters = BasePropertyParser.getInstance().getAllQueryParameters();
            }
            AppLogger.e("所有的设备属性为:" + new Gson().toJson(queryParameters));
            MessagePack messagePack = new MessagePack();
            messagePack.register(JFGDPMsg.class);
            int seq = RandomUtils.getRandom(Integer.MAX_VALUE);
            PanoramaEvent.MsgForward forward = new PanoramaEvent.MsgForward();
            forward.dst = dstArray;
            forward.mCaller = "";
            forward.mCallee = "";
            forward.mId = 20006;
            forward.isAck = 1;
            forward.type = 51;
            forward.mSeq = seq;
            forward.msg = messagePack.write(queryParameters);
            AppLogger.d("正在向设备发送透传消息:" + new Gson().toJson(forward));
            appCmd.sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, messagePack.write(forward));
            appCmd.sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, messagePack.write(forward));
        } catch (Exception e) {
            AppLogger.e(e.getMessage());
        }
    }


    @Deprecated
    public void setOnline(boolean online) {
        isOnline = online;
        if (account != null && !online) {
            account.setOnline(false);
        }

    }

    @Override
    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public Device getDevice(String uuid) {
        Device device = mCachedDeviceMap == null ? new Device() : mCachedDeviceMap.get(uuid);
        if (device == null) {
            device = new Device();
            device.setPropertyParser(propertyParser);
        }
        return device;//给一个默认的 device, 防止出现空指针
    }

    @Override
    public List<Device> getAllDevice() {
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

    public List<Device> getDevicesByPid(int... pids) {
        if (pids == null) return null;

        List<Device> result = new ArrayList<>();
        for (Map.Entry<String, Device> device : mCachedDeviceMap.entrySet()) {
            for (int pid : pids) {
                if (device.getValue().pid == pid) {
                    result.add(getDevice(device.getKey()));
                    break;
                }
            }
        }
        return getValueWithAccountCheck(result);
    }

    public List<String> getDeviceUuidByPid(int... pids) {
        if (pids == null) return null;
        List<String> result = new ArrayList<>();
        List<Device> devices = getDevicesByPid(pids);
        for (Device device : devices) {
            result.add(device.uuid);
        }
        return getValueWithAccountCheck(result);
    }

    //主动发起请求,来获取设备所有的属性
    @Override
    public void syncAllDevicePropertyManually() {
        if (mCachedDeviceMap.size() == 0) return;
        ArrayList<String> uuidList = new ArrayList<>();
        for (Map.Entry<String, Device> entry : mCachedDeviceMap.entrySet()) {
            Device device = mCachedDeviceMap.get(entry.getKey());
            syncDeviceProperty(entry.getKey());
            //非分享设备需要一些属性
            if (!JFGRules.isShareDevice(device)) {
                uuidList.add(device.uuid);
            }
        }
        /**
         * 设备分享列表
         */
        appCmd.getShareList(uuidList);
    }


    @Override
    public Observable<Account> logout() {
        clear();
        setLoginState(new LogState(LogState.STATE_ACCOUNT_OFF));
        return dbHelper.logout();
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

    @Override
    public void setAppCmd(AppCmd appCmd) {
        this.appCmd = appCmd;
    }

    private Observable<Iterable<Device>> unBindDevices(Iterable<String> uuids) {
        return dbHelper.unBindDeviceWithConfirm(uuids)
                .map(devices -> {
                    for (Device uuid : devices) {
                        AppLogger.d("设备已解绑:" + uuid);
                        mCachedDeviceMap.remove(uuid.uuid);
                        String mac = uuid.$(DpMsgMap.ID_202_MAC, "");
                        if (TextUtils.isEmpty(mac)) {
                            mac = PreferencesUtils.getString(JConstant.KEY_DEVICE_MAC + uuid.uuid);
                        }
                        JFGRules.switchApModel(mac, uuid.uuid, 1).subscribe(ret -> {
                            if (ret) {
                                AppLogger.d("睿视删除设备起 AP 成功了!");
                            }
                        }, e -> {
                            AppLogger.e("unBindDevices,Error:" + e.getMessage());
                        });
                        FileUtils.deleteFile(JConstant.PANORAMA_MEDIA_PATH + File.separator + uuid.uuid);
                        getCacheInstance().post(new RxEvent.DeviceUnBindedEvent(uuid.uuid));
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
        Device device = getDevice(uuid);
        if (device != null) {
            try {
                AppLogger.d(String.format(Locale.getDefault(), "uuid:%s,version:%s,asc:%s,count:%s", uuid, version, asc, count));
                return appCmd.robotGetDataEx(uuid, asc, version, new long[]{505L, 222L, 512}, 0);
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
        return Observable.just(History.getHistory().queryHistory(getDevice(uuid)));
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

    public void syncDeviceProperty(String uuid) {
        if (TextUtils.isEmpty(uuid) || account == null) return;
        Device device = mCachedDeviceMap.get(uuid);
        if (device != null) {
            ArrayList<JFGDPMsg> parameters = device.getQueryParams();
            try {
                appCmd.robotGetData(uuid, parameters, 1, false, 0);
            } catch (JfgException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public Account getAccount() {
        return account == null ? new Account() : account;
    }

    @Override
    public JFGAccount getJFGAccount() {
        if (jfgAccount == null && account != null) {
            return new Gson().fromJson(account.getAccountJson(), JFGAccount.class);
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
        getCacheInstance().post(new RxEvent.GetShareListRsp());
    }

    @Override
    public boolean isDeviceSharedTo(String uuid) {
        int size = shareList == null ? 0 : shareList.size();
        for (int i = 0; i < size; i++) {
            JFGShareListInfo info = shareList.get(i);
            if (info != null && TextUtils.equals(uuid, info.cid)) {
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
    public JFGShareListInfo getShareListByCid(String cid) {
        if (shareList != null) {
            for (JFGShareListInfo listInfo : shareList) {
                if (TextUtils.equals(cid, listInfo.cid)) {
                    return listInfo;
                }
            }
        }
        return null;
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
        if (this.account != null) this.account.setAccount(jfgAccount);
        AppLogger.i("setJfgAccount:" + (jfgAccount == null));
    }

    @Override
    public boolean updateDevice(Device device) {
        dbHelper.updateDevice(device).subscribe(ret -> {
        }, AppLogger::e);
        return true;
    }

    @Override
    public <T extends DataPoint> boolean updateValue(String uuid, T value, int msgId) throws
            IllegalAccessException {
        ArrayList<T> list = new ArrayList<>();
        value.setMsgId(msgId);
        value.setVersion(System.currentTimeMillis());
        list.add(value);
        return updateValue(uuid, list);
    }

    @Override
    public <T extends DataPoint> boolean updateValue(String uuid, List<T> value) throws IllegalAccessException {
        Observable.just("update")
                .subscribeOn(Schedulers.newThread())
                .subscribe(s -> {
                    Device device = getDevice(uuid);
                    if (device == null) {
                        AppLogger.e("device is null:" + uuid);
                        return;
                    }
                    try {
                        ArrayList<JFGDPMsg> list = new ArrayList<>();
                        for (DataPoint data : value) {
                            boolean result = device.setValue((int) data.getMsgId(), data);
                            AppLogger.d("update dp:" + result + " " + data.getMsgId());
                            JFGDPMsg jfgdpMsg = new JFGDPMsg(data.getMsgId(), System.currentTimeMillis());
                            jfgdpMsg.packValue = data.toBytes();
                            list.add(jfgdpMsg);
                        }
                        List<IDPEntity> multiUpdateList = MiscUtils.msgList(DBAction.MULTI_UPDATE, uuid, getAccount().getAccount(), OptionsImpl.getServer(), list);
                        BaseApplication.getAppComponent().getTaskDispatcher().perform(multiUpdateList)
                                .subscribeOn(Schedulers.io())
                                .doOnError(AppLogger::e)
                                .subscribe(ret -> {
                                }, AppLogger::e);
                    } catch (Exception e) {
                        AppLogger.e("err:" + MiscUtils.getErr(e));
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
                    Device device = getDevice(uuid);
                    if (device != null) {
                        ArrayList<JFGDPMsg> list = new ArrayList<>();
                        for (int msgId : msgIdList) {
                            device.setValue(msgId, null);
                            JFGDPMsg jfgdpMsg = new JFGDPMsg(msgId, 0);
                            jfgdpMsg.packValue = DpUtils.pack(0);
                            list.add(jfgdpMsg);
                        }
                        try {
                            List<IDPEntity> multiUpdateList = MiscUtils.msgList(DBAction.MULTI_UPDATE, uuid, getAccount().getAccount(), OptionsImpl.getServer(), list);
                            BaseApplication.getAppComponent().getTaskDispatcher().perform(multiUpdateList)
                                    .subscribeOn(Schedulers.io())
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

    @Override
    public int getLoginState() {
        JFGAccount account = this.getJFGAccount();
        if (account == null || TextUtils.isEmpty(account.getAccount())) {
            return 0;//无账号
        } else {
            return PreferencesUtils.getInt(KEY_ACCOUNT_LOG_STATE, 0);
        }
    }


    @Override
    public int getStorageType() {
        return this.storageType;
    }

    @Override
    public void setStorageType(int type) {
        this.storageType = type;
    }

    private Subscription makeCacheAccountSub() {
        return getCacheInstance().toObservable(RxEvent.SerializeCacheAccountEvent.class)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(event -> dbHelper.updateAccount(event.account)
                        .map(dpAccount -> {
                            this.account = dpAccount;
                            this.account.setOnline(true);
                            setJfgAccount(event.account);
                            if (jfgAccount != null) {
                                setLoginState(new LogState(LogState.STATE_ACCOUNT_ON));
                            } else {
                                AppLogger.e("jfgAccount is null");
                            }
                            getCacheInstance().post(account);
                            RxEvent.AccountArrived accountArrived = new RxEvent.AccountArrived(this.account);
                            accountArrived.jfgAccount = event.account;
                            getCacheInstance().postSticky(accountArrived);
                            return "";
                        }))
                .retry((i, e) -> true)
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        AppLogger.d(e.getMessage());
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

    private Subscription makeCacheDeviceSub() {
        return getCacheInstance().toObservable(RxEvent.SerializeCacheDeviceEvent.class)
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
                    AppLogger.d("已删除的设备数:" + result.size());
                    return dbHelper.updateDevice(event.devices).flatMap(dpDevice -> unBindDevices(result).map(ret -> dpDevice));
                })
                .map(devices -> {
                    try {
                        ArrayList<JFGDPMsg> parameters;
                        DBOption.DeviceOption option;
                        mCachedDeviceMap.clear();
                        rawDeviceOrder.clear();
                        ArrayList<String> uuidList = new ArrayList<>();
                        synchronized (DataSourceManager.class) {
                            mCachedDeviceMap.clear();
                            rawDeviceOrder.clear();
                            for (Device device : devices) {
                                option = device.option(DBOption.DeviceOption.class);
                                mCachedDeviceMap.put(device.getUuid(), device);
                                rawDeviceOrder.add(new Pair<>(option.rawDeviceOrder, device.getUuid()));
                                if (!JFGRules.isShareDevice(device)) {
                                    uuidList.add(device.getUuid());
                                }
                            }
                        }
                        Device device;
                        for (Map.Entry<String, Device> entry : mCachedDeviceMap.entrySet()) {
                            device = entry.getValue();
                            parameters = device.getQueryParams();
                            AppLogger.d("正在查询设备属性:" + new Gson().toJson(parameters));
                            appCmd.robotGetData(device.getUuid(), parameters, 1, false, 0);
                        }
                        appCmd.getShareList(uuidList);
                        getCacheInstance().post(new RxEvent.DevicesArrived(getAllDevice()));
                    } catch (JfgException e) {
                        AppLogger.d(e.getMessage());
                        e.printStackTrace();
                    }
                    return "多线程真心麻烦";
                })
                .retry((i, e) -> true)
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        AppLogger.d(e.getMessage());
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

    private Subscription makeCacheGetDataSub() {
        return getCacheInstance().toObservable(RxEvent.SerializeCacheGetDataEvent.class)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(event -> {
                    long seq = event.getDataRsp == null ? -1 : event.getDataRsp.seq;
                    if (dpSeqRspInterceptor.containsKey(seq)) {
                        Interceptors interceptors = dpSeqRspInterceptor.get(seq);
                        interceptors.handleInterception(event.getDataRsp);
                        dpSeqRspInterceptor.remove(seq);
                    }
                    return dbHelper.saveDPByteInTx(event.getDataRsp).map(dpEntities -> event);
                })
                .retry((i, e) -> true)
                .subscribe(new Subscriber<RxEvent.SerializeCacheGetDataEvent>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        AppLogger.e(e.getMessage());
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

    private Subscription makeCacheSyncDataSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SerializeCacheSyncDataEvent.class)
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
                            AppLogger.d("收到设备同步消息");
                            handleSystemNotification(event.arrayList, event.s);
                            return "多线程真是麻烦";
                        }))
                .retry((i, e) -> true)
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        AppLogger.d(e.getMessage());
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

    @Override
    public void handleSystemNotification(ArrayList<JFGFeedbackInfo> list) {
        if (!BaseApplication.isBackground()) return;
        if (ListUtils.isEmpty(list)) return;
        if (getAccount() == null || !getJFGAccount().isEnablePush())
            return;
        INotify.NotifyBean bean = new INotify.NotifyBean();
        bean.sound = getAccount() != null && getAccount().getEnableSound();
        bean.vibrate = getAccount() != null && getJFGAccount().isEnableVibrate();
        bean.time = System.currentTimeMillis();
        bean.resId = R.mipmap.ic_launcher;
        bean.notificationId = "account".hashCode();
        bean.content = ContextUtils.getContext().getString(R.string.app_name);
        bean.subContent = list.get(list.size() - 1).msg;
        final Intent intent = new Intent(ContextUtils.getContext(), FeedbackActivity.class);
        bean.pendingIntent = PendingIntent.getActivity(ContextUtils.getContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotifyManager.getNotifyManager().sendNotify(bean);
    }

    /**
     * 简单发起一个通知
     *
     * @param arrayList
     */
    private void handleSystemNotification(ArrayList<JFGDPMsg> arrayList, String uuid) {
        if (getAccount() == null || !getJFGAccount().isEnablePush())
            return;
        Device device = getDevice(uuid);
        //需要考虑,app进入后台.
        if (device != null && !TextUtils.isEmpty(device.account)) {
            ArrayList<JFGDPMsg> list = new ArrayList<>(arrayList);
            for (int i = 0; i < ListUtils.getSize(list); i++) {
                long msgId = list.get(i).id;
                JFGDPMsg msg = list.get(i);
                if (msgId == 505 || msgId == 512 || msgId == 222) {
                    if (!BaseApplication.isBackground()) {
                        continue;
                    }
                    //cam 1001 1002  1003
                    try {
                        List<IDPEntity> idpEntities = new MiscUtils.DPEntityBuilder()
                                .add(DBAction.SIMPLE_MULTI_QUERY, uuid, 1001, 0, true).add(DBAction.SIMPLE_MULTI_QUERY, uuid, 1002, 0, true).add(DBAction.SIMPLE_MULTI_QUERY, uuid, 1003, 0, true).build();
                        BaseApplication.getAppComponent().getTaskDispatcher().perform(idpEntities)
                                .subscribeOn(Schedulers.newThread())
                                .subscribe(baseDPTaskResult -> {
                                    Device dd = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
                                    String alias = TextUtils.isEmpty(dd.alias) ? dd.uuid : dd.alias;
                                    DPEntity entity = MiscUtils.getMaxVersionEntity(dd.getProperty(1001), dd.getProperty(1002), dd.getProperty(1003));
                                    INotify.NotifyBean bean = new INotify.NotifyBean();
                                    bean.sound = getAccount() != null && getAccount().getEnableSound();
                                    bean.vibrate = getAccount() != null && getJFGAccount().isEnableVibrate();
                                    bean.time = System.currentTimeMillis();
                                    bean.resId = R.mipmap.ic_launcher;
                                    bean.notificationId = (uuid + "cam").hashCode();
                                    bean.content = alias;
                                    int count = entity.getValue(0);
                                    bean.subContent = ContextUtils.getContext().getString(R.string.receive_new_news, count > 99 ? "99+" : count);
                                    final Intent intent = new Intent(ContextUtils.getContext(), CameraLiveActivity.class);
                                    intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                                    intent.putExtra(JConstant.KEY_JUMP_TO_MESSAGE, JConstant.KEY_JUMP_TO_MESSAGE);
                                    bean.pendingIntent = PendingIntent.getActivity(ContextUtils.getContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
                                    NotifyManager.getNotifyManager().sendNotify(bean);
                                }, AppLogger::e);
                    } catch (Exception e) {
                        AppLogger.e("err:" + MiscUtils.getErr(e));
                    }
                } else if (msgId == 401) {
                    DpMsgDefine.DPBellCallRecord dataPoint = propertyParser.parser((int) msgId, msg.packValue, msg.version);
                    if (dataPoint.isOK == 1) return; //已接听了,不需要发送通知了
                    AppLogger.d("may fire a notification: " + msgId);
                    //for bell 1004 1005
                    INotify.NotifyBean bean = new INotify.NotifyBean();
                    try {
                        List<IDPEntity> idpEntities = new MiscUtils.DPEntityBuilder()
                                .add(DBAction.SIMPLE_MULTI_QUERY, uuid, 1004, 0, true)
                                .add(DBAction.SIMPLE_MULTI_QUERY, uuid, 1005, 0, true).build();
                        BaseApplication.getAppComponent().getTaskDispatcher().perform(idpEntities)
                                .subscribeOn(Schedulers.newThread())
                                .subscribe(baseDPTaskResult -> {
                                    Device dd = getDevice(uuid);
                                    DPEntity entity = MiscUtils.getMaxVersionEntity(dd.getProperty(1004), dd.getProperty(1005));
                                    Intent intent = new Intent(ContextUtils.getContext(), DoorBellHomeActivity.class);
                                    intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                                    int count = entity.getValue(0);
                                    final String title = count == 0 ? ContextUtils.getContext().getString(R.string.app_name) :
                                            String.format(ContextUtils.getContext().getString(R.string.app_name) + "(%s%s)", count, ContextUtils.getContext().getString(R.string.DOOR_NOT_CONNECT));
                                    final String subTitle = count == 0 ?
                                            ContextUtils.getContext().getString(R.string.Slogan) : ContextUtils.getContext().getString(R.string.EFAMILY_MISSED_CALL);
                                    bean.time = System.currentTimeMillis();
                                    bean.resId = R.mipmap.ic_launcher;
                                    bean.pendingIntent = PendingIntent.getActivity(ContextUtils.getContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
                                    bean.time = entity.getVersion();
                                    bean.notificationId = (uuid + "bell").hashCode();
                                    bean.content = title;
                                    bean.subContent = subTitle;
                                    bean.sound = getAccount() != null && getAccount().getEnableSound();
                                    bean.vibrate = getAccount() != null && getJFGAccount().isEnableVibrate();
                                    NotifyManager.getNotifyManager().sendNotify(bean);
                                }, throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()));
                    } catch (Exception e) {
                        AppLogger.e("err:" + MiscUtils.getErr(e));
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
        makeAccountSub();
    }

    /**
     * 账号刷新了.需要更新账号相关的信息
     *
     * @return
     */
    private Subscription makeAccountSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.AccountArrived.class)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.newThread())
                //可能是本地的
                .filter(ret -> isOnline())
                .subscribe(ret -> {
                    try {
                        AutoSignIn.SignType signType = AutoSignIn.getInstance().getSignType();
                        loginType = signType == null ? 0 : signType.type;
                        Observable.just(new FetchFeedbackTask(), new FetchFriendsTask(), new SysUnreadCountTask())
                                .subscribeOn(Schedulers.newThread())
                                .subscribe(objectAction1 -> objectAction1.call(""), AppLogger::e);
                    } catch (Exception e) {

                    }
                }, AppLogger::e);
    }

    @Override
    public void addInterceptor(Long value, DataSourceManager.Interceptors interceptors) {
        dpSeqRspInterceptor.put(value, interceptors);
    }

    public interface Interceptors<T> {
        /**
         * 这里必须是一个同步方法
         *
         * @param data
         * @return
         */
        void handleInterception(T data);
    }
}
