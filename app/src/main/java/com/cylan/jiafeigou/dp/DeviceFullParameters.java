package com.cylan.jiafeigou.dp;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDPMsgCount;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.model.param.BaseParam;
import com.cylan.jiafeigou.n.mvp.model.param.BellParam;
import com.cylan.jiafeigou.n.mvp.model.param.CamParam;
import com.cylan.jiafeigou.n.mvp.model.param.EfamilyParam;
import com.cylan.jiafeigou.n.mvp.model.param.MagParam;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * Created by cylan-hunt on 16-11-16.
 */
public class DeviceFullParameters {
    private static final String TAG = "DeviceFullParameters:";
    private Subscription subscription;
    private static DeviceFullParameters deviceFullParameters;

    private DeviceFullParameters() {
    }

    public static DeviceFullParameters getInstance() {
        if (deviceFullParameters == null) {
            synchronized (DeviceFullParameters.class) {
                if (deviceFullParameters == null) deviceFullParameters = new DeviceFullParameters();
            }
        }
        return deviceFullParameters;
    }

    /**
     * 摄像头是否有未读消息
     */
    private void getDeviceUnreadMsg(ArrayList<JFGDevice> devices) {
        if (subscription == null || subscription.isUnsubscribed())
            subscription = handleUnreadMessageCount();
        Observable.just(devices)
                .subscribeOn(Schedulers.io())
                .subscribe(d -> {
                    if (devices == null || devices.size() == 0)
                        return;
                    for (JFGDevice device : devices) {
                        int pid = device.pid;
                        if (JFGRules.isCamera(pid)) {
                            try {
                                GlobalDataProxy.getInstance().fetchUnreadCount(device.uuid, DpMsgMap.ID_505_CAMERA_ALARM_MSG);
                            } catch (JfgException e) {
                                AppLogger.e("" + e.getLocalizedMessage());
                            }
                        }
                    }
                }, throwable -> {
                    AppLogger.e("get cam unread msg");
                });
    }

    /**
     * 未读消息数,只是一个数字.
     * 应该在报警消息池中取,如果没有新的报警消息,需要重新获取.
     *
     * @return
     */
    private Subscription handleUnreadMessageCount() {
        return RxBus.getCacheInstance().toObservable(RxEvent.UnreadCount.class)
                .subscribeOn(Schedulers.newThread())
                .map((RxEvent.UnreadCount unreadCount) -> {
                    String uuid = unreadCount.uuid;
                    for (JFGDPMsgCount msg : unreadCount.msgList) {
                        int id = msg.id;
                        BaseValue base = GlobalDataProxy.getInstance().fetchLocal(uuid, id, true);
                        if (base == null) {
                            AppLogger.e(String.format(Locale.getDefault(), "no id:%d BaseValue", id));
                            continue;
                        }
                        if (msg.count == 0) continue;
                        Pair<Integer, BaseValue> pair = new Pair<>(msg.count, base);
                        GlobalDataProxy.getInstance().cacheUnread(uuid + id, pair);
                        Log.d(TAG, "handleUnreadMessageCount:" + pair);
                    }
                    return null;
                })
                .retry(new RxHelper.RxException<>("handleUnreadMessageCount"))
                .subscribe();
    }

    /**
     * 请求响应
     *
     * @param robotoGetDataRsp
     */
    public void fullDataPointAssembler(RobotoGetDataRsp robotoGetDataRsp) {
        Observable.just(robotoGetDataRsp)
                .subscribeOn(Schedulers.computation())
                .map(new Func1<RobotoGetDataRsp, Integer>() {
                    @Override
                    public Integer call(RobotoGetDataRsp dpDataRsp) {
                        final String identity = dpDataRsp.identity;
                        Log.d(TAG, "fullDataPointAssembler: " + identity);
                        boolean needNotify = false;
                        boolean isNotEmpty = false;
                        for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : dpDataRsp.map.entrySet()) {
                            if (entry.getValue() == null) continue;
                            for (JFGDPMsg dp : entry.getValue()) {
                                try {
                                    if (dp == null) continue;
                                    Object o = DpUtils.unpackData(dp.packValue, DpMsgMap.ID_2_CLASS_MAP.get((int) dp.id));
                                    if (o != null && o instanceof DataPoint) {
                                        ((DataPoint) o).id = dp.id;
                                        ((DataPoint) o).version = dp.version;
                                    }
                                    BaseValue value = new BaseValue();
                                    value.setId(dp.id);
                                    value.setVersion(dp.version);
                                    value.setValue(o);
                                    boolean changed = DataPointManager.getInstance().putValue(identity, value);
                                    needNotify |= changed;
                                    isNotEmpty = true;
                                    Log.d(TAG, "put: " + dp.id + " " + changed + " " + needNotify);
                                } catch (Exception e) {
                                    AppLogger.i("dp is null: " + dp.id + ".." + e.getLocalizedMessage());
                                }
                            }
                        }
                        //表明有数据更新并且存到内存中.
                        if (isNotEmpty) {
                            RxBus.getCacheInstance().post(dpDataRsp);
                        }
                        return null;
                    }
                })
                //此retry能跳过当前一次的exception
                .retry(new RxHelper.RxException<>(TAG + "deviceDpSub"))
                .subscribe();
    }

    /**
     * 服务器推送
     *
     * @param b
     * @param identity
     * @param arrayList
     */
    public void assembleFullParameters(boolean b, final String identity, ArrayList<JFGDPMsg> arrayList) {
        Observable.just(arrayList)
                .subscribeOn(Schedulers.io())
                .map(jfgdpMsgs -> {
                    Map<String, BaseValue> updatedItems = new HashMap<>();
                    for (JFGDPMsg jfg : jfgdpMsgs) {
                        try {
                            BaseValue base = new BaseValue();
                            base.setId(jfg.id);
                            base.setVersion(jfg.version);
                            base.setValue(DpUtils.unpackData(jfg.packValue, DpMsgMap.ID_2_CLASS_MAP.get((int) jfg.id)));
                            boolean result = GlobalDataProxy.getInstance().update(identity, base, false);
                            if (result) updatedItems.put(identity, base);
                        } catch (Exception e) {
                            AppLogger.e("" + jfg.id + " " + e.getLocalizedMessage());
                        }
                    }
                    AppLogger.i("robotSyc:" + updatedItems.keySet());
                    return updatedItems;
                })
                .filter((Map<String, BaseValue> map) -> (map.size() > 0))
                .map((Map<String, BaseValue> map) -> {
                    for (String uuid : map.keySet()) {
                        RxEvent.DataPoolUpdate data = new RxEvent.DataPoolUpdate();
                        data.id = (int) map.get(uuid).getId();
                        data.uuid = uuid;
                        data.value = map.get(uuid);
                        RxBus.getCacheInstance().post(data);
                    }
                    return null;
                })
                .retry(new RxHelper.RxException<>("robotDataSyncSub:"))
                .subscribe();
    }

    /**
     * 设备对应的分享信息
     *
     * @param devices
     */
    private void getDeviceShareState(ArrayList<JFGDevice> devices) {
        Observable.just(devices)
                .subscribeOn(Schedulers.io())
                .subscribe(d -> {
                    if (devices == null || devices.size() == 0)
                        return;
                    ArrayList<String> uuidList = new ArrayList<>();
                    for (JFGDevice device : devices) {
                        if (device != null && TextUtils.isEmpty(device.shareAccount))
                            uuidList.add(device.uuid);
                    }
                    JfgCmdInsurance.getCmd().getShareList(uuidList);
                }, throwable -> {
                    AppLogger.e("get cam unread msg");
                });
    }

    /**
     * 设备的每一个属性
     *
     * @param allDevice
     */
    public void getDeviceFullParameters(JFGDevice... allDevice) {
        if (allDevice == null || allDevice.length == 0) return;
        ArrayList<JFGDevice> devices = new ArrayList<>();
        for (JFGDevice d : allDevice) {
            devices.add(d);
        }
        getDeviceUnreadMsg(devices);
        getDeviceShareState(devices);
        Observable.just(devices)
                .subscribeOn(Schedulers.newThread())
                .subscribe(d -> {
                    for (JFGDevice device : devices) {
                        String uuid = device.uuid;
                        int pid = device.pid;
                        if (merger(pid) == null) continue;
                        BaseParam baseParam = merger(pid);
                        try {
                            long seq = JfgCmdInsurance.getCmd().robotGetData(uuid, baseParam.queryParameters(null), 1, false, 0);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                        AppLogger.i(TAG + " req: " + uuid);
                    }
                }, throwable -> {
                    AppLogger.e("get full parameters err:" + throwable.getLocalizedMessage());
                });
    }

    /**
     * 根据不同的pid来组合不同的请求体
     *
     * @param pid
     * @return
     */
    private BaseParam merger(int pid) {
        BaseParam baseParam = null;
        switch (pid) {//摄像头全部属性
            case JConstant.OS_CAMERA_ANDROID:
            case JConstant.OS_CAMERA_UCOS:
            case JConstant.OS_CAMERA_UCOS_V2:
            case JConstant.OS_CAMERA_UCOS_V3:
            case JConstant.OS_CAMERA_ANDROID_4G:
            case JConstant.OS_CAMERA_CC3200:
            case JConstant.OS_CAMERA_PANORAMA_HAISI:
            case JConstant.OS_CAMERA_PANORAMA_QIAOAN:
            case JConstant.OS_CAMERA_PANORAMA_GUOKE:
                baseParam = new CamParam();
                break;
            case JConstant.OS_EFAML:
                baseParam = new EfamilyParam();
                break;
            case JConstant.OS_MAGNET:
                baseParam = new MagParam();
                break;
            case JConstant.OS_DOOR_BELL:
                baseParam = new BellParam();
                break;
        }
        return baseParam;
    }

}