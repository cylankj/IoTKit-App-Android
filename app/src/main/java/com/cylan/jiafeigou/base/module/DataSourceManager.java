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
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.video.History;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.INotify;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.NotifyManager;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.module.AppCallbackSupervisor;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.view.activity.CameraLiveActivity;
import com.cylan.jiafeigou.n.view.mine.FeedbackActivity;
import com.cylan.jiafeigou.n.view.misc.MapSubscription;
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
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.JConstant.KEY_ACCOUNT_LOG_STATE;
import static com.cylan.jiafeigou.rx.RxBus.getCacheInstance;

/**
 * @Deprecated 不推荐使用缓存了, 很多问题,每次直接查询服务器就行了
 * 即将删除
 */
public class DataSourceManager implements JFGSourceManager {
    private final String TAG = getClass().getName();

    private int storageType;
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
    private MapSubscription mapSubscription = new MapSubscription();
    @Deprecated
    private boolean isOnline = true;
    private JFGAccount jfgAccount;

    private HashMap<Long, Interceptors> dpSeqRspInterceptor = new HashMap<>();
    private static DataSourceManager instance;

    public static DataSourceManager getInstance() {
        if (instance == null) {
            synchronized (DataSourceManager.class) {
                if (instance == null) {
                    instance = new DataSourceManager();
                }
            }
        }
        return instance;
    }

    public DataSourceManager() {
        instance = this;
        initSubscription();
    }

    @Override
    public void initFromDB() {//根据需要初始化
        BaseDBHelper.getInstance().getActiveAccount()
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
                    AppLogger.w("正在从数据库初始化...");
                    return dpAccount;
                })
                .flatMap(account -> BaseDBHelper.getInstance().getAccountDevice(account.getAccount()))
                .flatMap(Observable::from)
                .map(device -> {
                    DBOption.DeviceOption option = device.option(DBOption.DeviceOption.class);
                    mCachedDeviceMap.put(device.getUuid(), device);
                    rawDeviceOrder.add(new Pair<>(option.rawDeviceOrder, device.getUuid()));
                    return device;
                })
                .flatMap(device -> BaseDBHelper.getInstance().queryDPMsgByUuid(device.uuid)
                        .map(ret -> {
                            if (ret != null) {
                                DataPoint dataPoint;
                                for (DPEntity dpEntity : ret) {
                                    dataPoint = BasePropertyParser.getInstance().parser(dpEntity.getMsgId(), dpEntity.getBytes(), dpEntity.getVersion());
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
        if (videoStatusRsp.videoType == 2) {//1 短视频,3 报警录像,都忽略掉
            deviceState.put(uuid, videoStatusRsp);
            RxBus.getCacheInstance().post(RxEvent.DeviceRecordStateChanged.INSTANCE);
        }
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

    private void queryForwardInformation() {
        //这里尝试从 udp 获取设备属性
        try {
            List<String> dstArray = new ArrayList<>();
            ArrayList<JFGDPMsg> queryParameters = null;
            for (Map.Entry<String, Device> entry : mCachedDeviceMap.entrySet()) {
                dstArray.add(entry.getKey());
                queryParameters = BasePropertyParser.getInstance().getAllQueryParameters();
            }
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
            if (BuildConfig.DEBUG) {
                AppLogger.w("所有的设备属性为:" + new Gson().toJson(queryParameters));
                AppLogger.w("正在向设备发送透传消息:" + new Gson().toJson(forward));
            }
            Command.getInstance().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, messagePack.write(forward));
            Command.getInstance().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, messagePack.write(forward));
        } catch (Exception e) {
            AppLogger.e(e.getMessage());
        }
    }


    @Override
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
        Device device = mCachedDeviceMap == null || TextUtils.isEmpty(uuid) ? new Device() : mCachedDeviceMap.get(uuid);

//        if (device == null) {
//            com.cylan.jiafeigou.server.cache.Device device1 = BaseApplication.getDeviceBox().get(Long.parseLong(uuid));
//            device = device1 == null ? null : device1.cast();
//        }

        if (device == null) {
            device = new Device();
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
            if (!result.contains(d)) {
                result.add(d);
            } else {
                AppLogger.w("yes list contains d: " + d);
            }
        }

        return result;
    }

    @Override
    public List<Device> getDevicesByPid(int... pids) {
        if (pids == null) {
            return null;
        }

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

    @Override
    public List<String> getDeviceUuidByPid(int... pids) {
        if (pids == null) {
            return null;
        }
        List<String> result = new ArrayList<>();
        List<Device> devices = getDevicesByPid(pids);
        for (Device device : devices) {
            result.add(device.uuid);
        }
        return getValueWithAccountCheck(result);
    }

    //主动发起请求,来获取设备所有的属性
    @Override
    public void syncAllProperty() {
        if (mCachedDeviceMap.size() == 0) {
            return;
        }
        ArrayList<String> uuidList = new ArrayList<>();
        if (mCachedDeviceMap.size() == 0) {
            return;
        }
        HashMap<String, JFGDPMsg[]> map = new HashMap<>();
        List<HashMap<String, JFGDPMsg[]>> mapList = new ArrayList<>();
        int deviceCount = 0;
        for (Map.Entry<String, Device> entry : mCachedDeviceMap.entrySet()) {
            Device device = mCachedDeviceMap.get(entry.getKey());
            //非分享设备需要一些属性
            if (!JFGRules.isShareDevice(device)) {
                uuidList.add(device.uuid);
            }
            final String uuid = device.uuid;
            if (TextUtils.isEmpty(uuid) || account == null) {
                return;
            }
            ArrayList<JFGDPMsg> parameters = device.getQueryParams();
            if (parameters == null || parameters.size() == 0) {
                continue;
            }
            JFGDPMsg[] array = new JFGDPMsg[parameters.size()];
            for (int i = 0; i < parameters.size(); i++) {
                //非常丑的方式过滤掉 实时dp
                if (parameters.get(i).id == 204) {
                    parameters.get(i).id = 201;
                }
                array[i] = parameters.get(i);
            }
            map.put(uuid, array);
            if (deviceCount % 6 == 0) {
                mapList.add(map);
                map = new HashMap<>();
            }
            deviceCount++;
        }
        try {
            for (int i = 0; i < ListUtils.getSize(mapList); i++) {
                Command.getInstance().robotGetMultiData(mapList.get(i), 1, false, 0);
            }
            AppLogger.w("多查询");
        } catch (Exception e) {
            e.printStackTrace();
        }
        /**
         * 设备分享列表
         */
//        Command.getInstance().getShareList(uuidList);
    }

    @Override
    public boolean syncAllProperty(String uuid, int... excludeMsgIds) {
        if (mCachedDeviceMap.size() == 0) {
            return false;
        }
        Device device = mCachedDeviceMap.get(uuid);
        if (device == null) {
            return false;
        }
        ArrayList<String> uuidList = new ArrayList<>();
        HashMap<String, JFGDPMsg[]> map = new HashMap<>();
        //非分享设备需要一些属性
        if (!JFGRules.isShareDevice(device)) {
            uuidList.add(device.uuid);
        }
        if (TextUtils.isEmpty(uuid) || account == null) {
            return false;
        }
        ArrayList<JFGDPMsg> parameters = device.getQueryParams();
        JFGDPMsg[] array = new JFGDPMsg[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            int msgId = (int) parameters.get(i).id;
            if (MiscUtils.arrayContains(excludeMsgIds, msgId)) {
                array[i] = new JFGDPMsg(201, 0);//201是占位符号
            } else {
                array[i] = parameters.get(i);
            }
        }
        map.put(uuid, array);
        try {
            for (Map.Entry<String, JFGDPMsg[]> entry : map.entrySet()) {
                //超过512会崩溃
                HashMap<String, JFGDPMsg[]> hashMap = new HashMap<>();
                hashMap.put(entry.getKey(), entry.getValue());
                Command.getInstance().robotGetMultiData(hashMap, 1, false, 0);
                AppLogger.w("多查询");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /**
         * 设备分享列表
         */
//        Command.getInstance().getShareList(uuidList);
        return true;
    }

    @Override
    public void syncHomeProperty() {
        if (mCachedDeviceMap.size() == 0) {
            return;
        }
        ArrayList<String> uuidList = new ArrayList<>();
        for (Map.Entry<String, Device> entry : mCachedDeviceMap.entrySet()) {
            HashMap<String, JFGDPMsg[]> map = new HashMap<>();
            Device device = mCachedDeviceMap.get(entry.getKey());
            final String uuid = device.uuid;
            if (TextUtils.isEmpty(uuid) || account == null) {
                break;
            }
            uuidList.add(device.uuid);

            ArrayList<JFGDPMsg> parameters = device.getQueryParameters(device.pid, DPProperty.LEVEL_HOME);
            if (parameters == null || parameters.size() == 0) {
                continue;
            }
            JFGDPMsg[] array = new JFGDPMsg[parameters.size()];
            for (int i = 0; i < parameters.size(); i++) {
                array[i] = parameters.get(i);
            }
            map.put(uuid, array);
            try {
                Command.getInstance().robotGetMultiData(map, 1, false, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Command.getInstance().getShareList(uuidList);
    }


    @Override
    public Observable<Account> logout() {
        clear();
        return BaseDBHelper.getInstance().logout();
    }

    @Override
    public Observable<String> unBindDevice(String uuid) {
        return unBindDevices(Collections.singletonList(uuid))
                .filter(devices -> devices != null && devices.iterator().hasNext())
                .flatMap(devices -> Observable.just(devices.iterator().next()));
    }

    @Override
    public ArrayList<Long> getHisDateList(String uuid) {
        return History.getHistory().getDateList(uuid);
    }


    private Observable<Iterable<String>> unBindDevices(Iterable<String> uuids) {
        return Observable.just(uuids)
                .map(devices -> {
                    for (String uuid : devices) {
                        AppLogger.w("设备已解绑:" + uuid);
                        Device device = mCachedDeviceMap.remove(uuid);
                        String mac = null;
                        if (device != null && device.available()) {
                            mac = device.$(DpMsgMap.ID_202_MAC, "");
                        }
                        if (TextUtils.isEmpty(mac)) {
                            mac = PreferencesUtils.getString(JConstant.KEY_DEVICE_MAC + uuid);
                        }
                        JFGRules.switchApModel(mac, uuid, 1).subscribe(ret -> {
                            if (ret) {
                                AppLogger.w("睿视删除设备起 AP 成功了!");
                            }
                        }, e -> {
                            AppLogger.e("unBindDevices,Error:" + e.getMessage());
                        });
                        FileUtils.deleteFile(JConstant.MEDIA_PATH + File.separator + uuid);
                        final String pre = PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid);
                        FileUtils.deleteFile(pre);
                        getCacheInstance().post(new RxEvent.DeviceUnBindedEvent(uuid));
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

    @Override
    public long syncJFGCameraWarn(String uuid, long version, boolean asc, int count) {
        Device device = getDevice(uuid);
        if (device != null) {
            try {
                AppLogger.w(String.format(Locale.getDefault(), "uuid:%s,version:%s,asc:%s,count:%s", uuid, version, asc, count));
                return Command.getInstance().robotGetDataEx(uuid, asc, version, new long[]{505L, 222L, 512L, 401L}, 0);
            } catch (Exception e) {
                AppLogger.w("bad ,uuid may be null");
                return -1;
            }
        } else {
            AppLogger.w("bad ,device is null");
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
    public void cacheHistoryDataList(byte[] rawV2Data) {
        History.getHistory().cacheHistoryDataList(rawV2Data);
    }

    @Override
    public void clear() {
        getCacheInstance().removeAllStickyEvents();
        isOnline = false;
        account = null;
        jfgAccount = null;
//        if (unreadMap != null) unreadMap.clearLocal();
        if (shareList != null) {
            shareList.clear();
        }
        if (rawDeviceOrder != null) {
            rawDeviceOrder.clear();
        }
        if (mCachedDeviceMap != null) {
            mCachedDeviceMap.clear();
        }
    }

    @Override
    public void syncDeviceProperty(String uuid) {
        if (TextUtils.isEmpty(uuid) || account == null) {
            return;
        }
        Device device = mCachedDeviceMap.get(uuid);
        if (device != null) {
            ArrayList<JFGDPMsg> parameters = device.getQueryParams();
            try {
                Command.getInstance().robotGetData(uuid, parameters, 1, false, 0);
            } catch (JfgException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void syncDeviceProperty(String uuid, int... pids) {
        if (TextUtils.isEmpty(uuid) || account == null || pids == null) {
            return;
        }
        Device device = mCachedDeviceMap.get(uuid);
        if (device != null) {
            ArrayList<JFGDPMsg> parameters = new ArrayList<>();
            for (int pid : pids) {
                JFGDPMsg msg = new JFGDPMsg(pid, 0);
                parameters.add(msg);
            }
            try {
                Command.getInstance().robotGetData(uuid, parameters, 1, false, 0);
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
        if (shareList == null) {
            shareList = new ArrayList<>();
        }
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

    @Override
    public void setJfgAccount(JFGAccount jfgAccount) {
        this.jfgAccount = jfgAccount;
        if (this.account != null) {
            this.account.setAccount(jfgAccount);
        }
        AppLogger.w("setJfgAccount:" + (jfgAccount == null));
    }

    @Override
    public boolean updateDevice(Device device) {
        BaseDBHelper.getInstance().updateDevice(device).subscribe(ret -> {
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
                .subscribeOn(Schedulers.io())
                .subscribe(s -> {
                    Device device = getDevice(uuid);
                    if (device == null) {
                        AppLogger.w("device is null:" + uuid);
                        return;
                    }
                    try {
                        ArrayList<JFGDPMsg> list = new ArrayList<>();
                        for (DataPoint data : value) {
                            boolean result = device.setValue((int) data.getMsgId(), data);
                            AppLogger.w("update dp:" + result + " " + data.getMsgId());
                            JFGDPMsg jfgdpMsg = new JFGDPMsg(data.getMsgId(), System.currentTimeMillis());

                            jfgdpMsg.packValue = data.toBytes();
                            list.add(jfgdpMsg);

                        }
                        List<IDPEntity> multiUpdateList = MiscUtils.msgList(DBAction.MULTI_UPDATE, uuid, getAccount().getAccount(), OptionsImpl.getServer(), list);
                        BaseDPTaskDispatcher.getInstance().perform(multiUpdateList)
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
                .subscribeOn(Schedulers.io())
                .subscribe(s -> {
                    if (msgIdList == null || msgIdList.length == 0) {
                        return;
                    }
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
                            BaseDPTaskDispatcher.getInstance().perform(multiUpdateList)
                                    .subscribe(ret -> {
                                        for (int i : msgIdList) {
                                            device.updateProperty(i, null);
                                        }
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
    public int getStorageType() {
        return this.storageType;
    }

    @Override
    public void setStorageType(int type) {
        this.storageType = type;
    }

    private Subscription makeCacheAccountSub() {
        return AppCallbackSupervisor.INSTANCE.observe(JFGAccount.class)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(event -> BaseDBHelper.getInstance().updateAccount(event)
                        .map(dpAccount -> {
                            this.account = dpAccount;
                            this.account.setOnline(true);
                            setJfgAccount(event);
                            RxEvent.AccountArrived accountArrived = new RxEvent.AccountArrived(this.account);
                            accountArrived.jfgAccount = event;
                            if (!BaseApplication.isBackground()) {
                                getCacheInstance().post(account);
                                getCacheInstance().postSticky(accountArrived);
                            }
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
        return AppCallbackSupervisor.INSTANCE.observe(AppCallbackSupervisor.ReportDeviceEvent.class)
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .flatMap(event -> {
                    Set<String> result = new TreeSet<>(mCachedDeviceMap.keySet());
                    JFGDevice device;
                    for (int i = 0; i < event.getDevices().length; i++) {
                        device = event.getDevices()[i];
                        result.remove(device.uuid);
                    }
                    AppLogger.w("已删除的设备数:" + result.size());
                    return BaseDBHelper.getInstance().updateDevice(event.getDevices()).map(devices -> new Pair<>(devices, result));
                })
                .subscribe(new Action1<Pair<Iterable<Device>, Set<String>>>() {
                    @Override
                    public void call(Pair<Iterable<Device>, Set<String>> iterableSetPair) {
                        try {
                            unBindDevices(iterableSetPair.second).subscribe();
                        } catch (Exception e) {
                            AppLogger.e(e);
                        }
                        try {
                            DBOption.DeviceOption option;
                            rawDeviceOrder.clear();
                            ArrayList<String> uuidList = new ArrayList<>();
                            synchronized (DataSourceManager.class) {
                                rawDeviceOrder.clear();
                                for (Device device : iterableSetPair.first) {
                                    option = device.option(DBOption.DeviceOption.class);
                                    Device dev = mCachedDeviceMap.get(device.getUuid());
                                    if (dev == null) {
                                        mCachedDeviceMap.put(device.getUuid(), device);
                                    } else {
                                        dev.setDevice(device);
                                    }
                                    rawDeviceOrder.add(new Pair<>(option.rawDeviceOrder, device.getUuid()));
                                    if (!JFGRules.isShareDevice(device)) {
                                        uuidList.add(device.getUuid());
                                    }
                                }
                            }
                            syncHomeProperty();
                            if (!BaseApplication.isBackground()) {
                                getCacheInstance().post(new RxEvent.DevicesArrived(getAllDevice()));
                            }
                        } catch (Exception e) {
                            AppLogger.d(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e(throwable);
                    }
                });

    }


    private Subscription makeCacheGetDataSub() {
        return AppCallbackSupervisor.INSTANCE.observe(RobotoGetDataRsp.class)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(event -> {
                    long seq = event == null ? -1 : event.seq;
                    if (dpSeqRspInterceptor.containsKey(seq)) {
                        Interceptors interceptors = dpSeqRspInterceptor.get(seq);
                        interceptors.handleInterception(event);
                        dpSeqRspInterceptor.remove(seq);
                    }
                    return BaseDBHelper.getInstance().saveDPByteInTx(event).map(dpEntities -> event);
                })
                .retry((i, e) -> true)
                .subscribe(new Subscriber<RobotoGetDataRsp>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        AppLogger.e(e.getMessage());
                    }

                    @Override
                    public void onNext(RobotoGetDataRsp event) {
                        RxBus.getCacheInstance().post(event);
                        request(1);
                    }

                    @Override
                    public void onStart() {
                        request(1);
                    }
                });
    }

    private Subscription makeCacheSyncDataSub() {
        return AppCallbackSupervisor.INSTANCE.observe(AppCallbackSupervisor.RobotSyncDataEvent.class)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(event -> BaseDBHelper.getInstance().saveDPByteInTx(event.getUuid(), event.getDpList())
                        .map(dpEntities -> {
                            ArrayList<Long> updateIdList = new ArrayList<>();
                            for (DPEntity entity : dpEntities) {
                                updateIdList.add((long) entity.getMsgId());
                            }
                            if (!BaseApplication.isBackground()) {
                                RxBus.getCacheInstance().postSticky(new RxEvent.DeviceSyncRsp().setUuid(event.getUuid(), updateIdList, event.getDpList()));
                            }
                            AppLogger.w("收到设备同步消息:" + event.getDpList());
                            handleSystemNotification(event.getDpList(), event.getUuid());
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
        if (!BaseApplication.isBackground()) {
            return;
        }
        if (ListUtils.isEmpty(list)) {
            return;
        }
        if (getAccount() == null || !getJFGAccount().isEnablePush()) {
            return;
        }
        INotify.NotifyBean bean = new INotify.NotifyBean();
        bean.sound = getAccount() != null && getAccount().getEnableSound();
        bean.vibrate = getAccount() != null && getJFGAccount().isEnableVibrate();
        bean.time = System.currentTimeMillis();
        bean.resId = R.mipmap.ic_launcher;
        bean.notificationId = "account".hashCode();
        bean.content = ContextUtils.getContext().getString(R.string.app_name);
        bean.subContent = list.get(list.size() - 1).msg;
        final Intent intent = new Intent(ContextUtils.getContext(), FeedbackActivity.class);
        bean.pendingIntent = PendingIntent.getActivity(ContextUtils.getContext(), bean.notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotifyManager.getNotifyManager().sendNotify(bean);
    }

    /**
     * 简单发起一个通知
     *
     * @param arrayList
     */
    private int[] notifyIDs = new int[]{401, 222, 40, 505, 512, 526};

    private void handleSystemNotification(ArrayList<JFGDPMsg> arrayList, String uuid) {
        Device device = getDevice(uuid);
        //需要考虑,app进入后台.
        if (device == null || TextUtils.isEmpty(device.account)) {
            return;
        }
        ArrayList<JFGDPMsg> list = new ArrayList<>(arrayList);
        boolean shouldRefreshUnReadCount = false;

        shouldFor:
        for (int i = 0; i < ListUtils.getSize(list); i++) {
            long msgId = list.get(i).id;
            for (int notifyID : notifyIDs) {
                if (notifyID == msgId) {
                    shouldRefreshUnReadCount = true;
                    break shouldFor;
                }
            }
        }
        if (shouldRefreshUnReadCount) {
            List<IDPEntity> idpEntities = new MiscUtils.DPEntityBuilder()
                    .add(DBAction.SIMPLE_MULTI_QUERY, uuid, 1001, 0, true)
                    .add(DBAction.SIMPLE_MULTI_QUERY, uuid, 1002, 0, true)
                    .add(DBAction.SIMPLE_MULTI_QUERY, uuid, 1003, 0, true)
                    .add(DBAction.SIMPLE_MULTI_QUERY, uuid, 1004, 0, true)
                    .add(DBAction.SIMPLE_MULTI_QUERY, uuid, 1005, 0, true)
                    .add(DBAction.SIMPLE_MULTI_QUERY, uuid, 1006, 0, true)
                    .build();
            BaseDPTaskDispatcher.getInstance().perform(idpEntities)
                    .subscribeOn(Schedulers.io())
                    .subscribe(baseDPTaskResult -> {
                        if (getAccount() == null || !getJFGAccount().isEnablePush()) {
                            return;
                        }
                        Device dd = DataSourceManager.getInstance().getDevice(uuid);
                        String alias = TextUtils.isEmpty(dd.alias) ? dd.uuid : dd.alias;
                        DPEntity entity = MiscUtils.getMaxVersionEntity(
                                dd.getProperty(1001),
                                dd.getProperty(1002),
                                dd.getProperty(1003),
                                dd.getProperty(1004),
                                dd.getProperty(1005),
                                dd.getProperty(1006)
                        );
                        int count = dd.$(1001, 0) +
                                dd.$(1002, 0) +
                                dd.$(1003, 0) +
                                dd.$(1004, 0) +
                                dd.$(1005, 0) +
                                dd.$(1006, 0);
                        if (count == 0) {
                            return;
                        }
                        boolean isBell = JFGRules.isBell(dd.pid);
                        String content;
                        String subContent;
                        if (isBell) {
                            JFGDPMsg msg = list.get(0);
                            if (msg.id == 401) {
                                DpMsgDefine.DPBellCallRecord bellCallRecord = DpUtils.unpackDataWithoutThrow(msg.packValue, DpMsgDefine.DPBellCallRecord.class, new DpMsgDefine.DPBellCallRecord());
                                if (bellCallRecord != null && bellCallRecord.isOK == 1) {
                                    return;
                                }
                            }
                            content = String.format(ContextUtils.getContext().getString(R.string.app_name) + "(%s%s)", count, ContextUtils.getContext().getString(R.string.DOOR_NOT_CONNECT));
                            subContent = ContextUtils.getContext().getString(R.string.EFAMILY_MISSED_CALL);
                        } else {
                            content = alias;
                            subContent = ContextUtils.getContext().getString(R.string.receive_new_news, count > 99 ? "99+" : count);
                        }
                        INotify.NotifyBean bean = new INotify.NotifyBean();
                        final Intent intent = new Intent(ContextUtils.getContext(), CameraLiveActivity.class);
                        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                        intent.putExtra(JConstant.KEY_JUMP_TO_MESSAGE, JConstant.KEY_JUMP_TO_MESSAGE);
                        bean.pendingIntent = PendingIntent.getActivity(ContextUtils.getContext(), bean.notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        bean.sound = getAccount() != null && getAccount().getEnableSound();
                        bean.vibrate = getAccount() != null && getJFGAccount().isEnableVibrate();
                        bean.time = System.currentTimeMillis();
                        bean.resId = R.mipmap.ic_launcher;
                        bean.notificationId = (uuid + "camera").hashCode();
                        bean.time = entity.getVersion();
                        bean.content = content;
                        bean.subContent = subContent;
                        NotifyManager.getNotifyManager().sendNotify(bean);
                    }, throwable -> AppLogger.e(MiscUtils.getErr(throwable)));
        }
    }

    @Override
    public void initAccount() {
        BaseDBHelper.getInstance().getActiveAccount().subscribe(ret -> this.account = ret, e -> AppLogger.d(e.getMessage()));
    }

    @Override
    public void initSubscription() {
        if (mapSubscription != null) {
            mapSubscription.clear();
        }
        if (mapSubscription == null) {
            mapSubscription = new MapSubscription();
        }
        mapSubscription.add(makeCacheGetDataSub(), "makeCacheGetDataSub");
        mapSubscription.add(makeCacheSyncDataSub(), "makeCacheSyncDataSub");
        mapSubscription.add(makeCacheAccountSub(), "makeCacheAccountSub");
        mapSubscription.add(makeCacheDeviceSub(), "makeCacheDeviceSub");
        mapSubscription.add(makeAccountSub(), "makeAccountSub");
    }

    /**
     * 账号刷新了.需要更新账号相关的信息
     *
     * @return
     */

    private Subscription makeAccountSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.AccountArrived.class)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                //可能是本地的
                .filter(ret -> isOnline())
                .subscribe(ret -> {
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
