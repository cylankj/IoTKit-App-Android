package com.cylan.jiafeigou.dp;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.cache.JCache;
import com.cylan.jiafeigou.misc.Convertor;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.model.BaseBean;
import com.cylan.jiafeigou.n.mvp.model.param.BaseParam;
import com.cylan.jiafeigou.n.mvp.model.param.BellParam;
import com.cylan.jiafeigou.n.mvp.model.param.CamParam;
import com.cylan.jiafeigou.n.mvp.model.param.EfamilyParam;
import com.cylan.jiafeigou.n.mvp.model.param.MagParam;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.rx.RxUiEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_2_CLASS_MAP;


/**
 * Created by cylan-hunt on 16-11-16.
 */

public class DpAssembler implements IParser {
    private static final String TAG = "DpAssembler:";

    private static DpAssembler instance;

    private IFlat flatMsg;

    private DpAssembler() {
        flatMsg = new FlattenMsgDp();
    }

    private HashMap<Long, Long> seqSequence = new HashMap<>();

    public static DpAssembler getInstance() {
        if (instance == null)
            instance = new DpAssembler();
        return instance;
    }

    @Override
    public Subscription[] register() {
        Subscription[] subscriptions = {
                simpleBulkSubSend2Ui(),
                deviceListSub(),
                deviceDpSub(),
                updateDpMsg(),
        };
        return subscriptions;
    }

    /**
     * 作为流水线的下游,更新所有的数据.
     *
     * @return
     */
    private Subscription updateDpMsg() {

        return RxBus.getCacheInstance().toObservable(RxEvent.JfgDpMsgUpdate.class)
                .subscribeOn(Schedulers.computation())
                .filter(new RxHelper.Filter<RxEvent.JfgDpMsgUpdate>(TAG + "updateDpMsg",
                        JCache.getAccountCache() != null && !TextUtils.isEmpty(JCache.getAccountCache().getAccount())))
                .flatMap(new Func1<RxEvent.JfgDpMsgUpdate, Observable<Pair<DpMsgDefine.DpMsg, String>>>() {
                    @Override
                    public Observable<Pair<DpMsgDefine.DpMsg, String>> call(RxEvent.JfgDpMsgUpdate jfgDpMsgUpdate) {
                        return Observable.just(new Pair<>(jfgDpMsgUpdate.dpMsg, jfgDpMsgUpdate.uuid));
                    }
                })
                .map(new Func1<Pair<DpMsgDefine.DpMsg, String>, Object>() {
                    @Override
                    public Object call(Pair<DpMsgDefine.DpMsg, String> arrayListStringPair) {
                        //拿出对应uuid的所有属性
                        DpMsgDefine.DpWrap deviceDetailsCache = flatMsg.getDevice(JCache.getAccountCache().getAccount(),
                                arrayListStringPair.second);
                        if (deviceDetailsCache == null || deviceDetailsCache.baseDpMsgList == null) {
                            AppLogger.e("deviceDetailsCache is null");
                            return null;
                        }
                        for (DpMsgDefine.DpMsg dpMsg : deviceDetailsCache.baseDpMsgList) {
                            if (dpMsg.msgId == arrayListStringPair.first.msgId) {
                                //hit
                                if (arrayListStringPair.first.version > dpMsg.version) {

                                    AppLogger.i("update attr: " + dpMsg + " -->" + arrayListStringPair.first);
                                    break;
                                }
                            }
                        }
                        return null;
                    }
                })
                .retry(new RxHelper.ExceptionFun<>(TAG + "updateDpMsg"))
                .subscribe();
    }

    /**
     * 由于一个账号绑定的设备量不是很多{0,100},100个设备也是逆天了.
     * 批量请求,批量更新
     *
     * @return
     */
    private Subscription simpleBulkSubSend2Ui() {
        return RxBus.getCacheInstance().toObservable(RxUiEvent.QueryBulkDevice.class)
                .filter(new Func1<RxUiEvent.QueryBulkDevice, Boolean>() {
                    @Override
                    public Boolean call(RxUiEvent.QueryBulkDevice queryBulkDevice) {
                        AppLogger.i(TAG + " simpleBulkSubSend2Ui: " + new Gson().toJson(JCache.getAccountCache()));
                        return JCache.getAccountCache() != null;
                    }
                })
                .map(new Func1<RxUiEvent.QueryBulkDevice, Object>() {
                    @Override
                    public Object call(RxUiEvent.QueryBulkDevice queryBulkDevice) {
                        RxUiEvent.BulkDeviceList cacheList = new RxUiEvent.BulkDeviceList();
                        cacheList.allDevices = flatMsg.getAllDevices(JCache.getAccountCache().getAccount());
                        RxBus.getUiInstance().postSticky(cacheList);
                        return null;
                    }
                }).subscribe();
    }

    /**
     * 设备的基本属性,不按常规出牌.
     *
     * @param device
     */
    private void assembleBase(JFGDevice device) {
        BaseBean dpDevice = new BaseBean();
        dpDevice.alias = device.alias;
        dpDevice.pid = device.pid;
        dpDevice.shareAccount = device.shareAccount;
        dpDevice.sn = device.sn;
        dpDevice.uuid = device.uuid;
        flatMsg.cache(JCache.getAccountCache().getAccount(), dpDevice);
    }

    private Observable<JFGAccount> monitorJFGAccount() {
        return RxBus.getCacheInstance().toObservableSticky(JFGAccount.class);
    }

    private Observable<RxEvent.DeviceRawList> monitorDeviceRawList() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceRawList.class);
    }

    /**
     * 从dataSource来的消息
     *
     * @return
     */
    private Subscription deviceListSub() {
        return Observable.zip(monitorJFGAccount(), monitorDeviceRawList(),
                new Func2<JFGAccount, RxEvent.DeviceRawList, List<JFGDevice>>() {
                    @Override
                    public List<JFGDevice> call(JFGAccount account, RxEvent.DeviceRawList deviceRawList) {
                        AppLogger.i(TAG + " yes jfgAccount is ready and deviceList is ready too");
                        return Arrays.asList(deviceRawList.devices);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .map(new Func1<List<JFGDevice>, Object>() {
                    @Override
                    public Object call(List<JFGDevice> list) {
                        for (int i = 0; i < list.size(); i++) {
                            assembleBase(list.get(i));
                            final int pid = list.get(i).pid;
                            BaseParam baseParam = merger(pid);
                            long seq = JfgAppCmd.getInstance().robotGetData(list.get(i).uuid, baseParam.queryParameters(null), 1, false, 0);
                            AppLogger.i(TAG + " req: " + list.get(i).uuid);
                            seqSequence.put(seq, seq);
                        }
                        return null;
                    }
                })
                .retry(new RxHelper.RxException<>(TAG + " deviceListSub"))
                .subscribe();
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

    /**
     * 这个订阅者是设备所有的dp消息处理中心.
     *
     * @return
     */
    private Subscription deviceDpSub() {
        return RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                .subscribeOn(Schedulers.computation())
                .filter(notNullFunc)
                .map(new Func1<RobotoGetDataRsp, Integer>() {
                    @Override
                    public Integer call(RobotoGetDataRsp dpDataRsp) {
                        final String identity = dpDataRsp.identity;
                        Log.d(TAG, "dpDataRsp: " + identity);
                        for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : dpDataRsp.map.entrySet()) {
                            JFGDPMsg dp = entry.getValue() != null
                                    && entry.getValue().size() > 0 ? entry.getValue().get(0) : null;
                            final int keyId = entry.getKey();
                            if (keyId == DpMsgMap.ID_505_CAMERA_ALARM_MSG || dp == null) {
                                //报警消息
                                assembleCamAlarmMsg(identity, entry.getValue());
                                continue;
                            }
                            assembleMiscMsg(identity, dp, keyId);
                        }
                        if (seqSequence.containsKey(dpDataRsp.seq)) {
                            //这次请求是,设备更新
                            sendSingleDeviceInfo(identity);
                            seqSequence.remove(dpDataRsp.seq);
                        } else {
                            AppLogger.i("not contains key: " + dpDataRsp.seq + " " + seqSequence);
                        }
                        return null;
                    }
                })
                //此retry能跳过当前一次的exception
                .retry(new RxHelper.RxException<>(TAG + "deviceDpSub"))
                .subscribe();
    }

    /**
     * 摄像头的报警图片是批量返回的.
     *
     * @param uuid
     * @param msgs
     */
    private void assembleCamAlarmMsg(String uuid, ArrayList<JFGDPMsg> msgs) {
        RxEvent.JfgAlarmMsg msg = new RxEvent.JfgAlarmMsg();
        msg.uuid = uuid;
        if (msg.jfgdpMsgs == null)
            msg.jfgdpMsgs = new ArrayList<>();
        if (msgs == null) {
            RxBus.getCacheInstance().post(msg);
            return;
        }
        for (JFGDPMsg jfgdpMsg : msgs) {
            try {
                DpMsgDefine.AlarmMsg o = DpUtils.unpackData(jfgdpMsg.packValue,
                        DpMsgDefine.AlarmMsg.class);
                DpMsgDefine.DpMsg dpMsg = new DpMsgDefine.DpMsg();
                dpMsg.msgId = (int) jfgdpMsg.id;
                dpMsg.version = jfgdpMsg.version;
                dpMsg.o = o;
                msg.jfgdpMsgs.add(dpMsg);
            } catch (Exception e) {
                AppLogger.e("assembleCamAlarmMsg: " + e.getLocalizedMessage());
            }
        }
        flatMsg.cache(JCache.getAccountCache().getAccount(), uuid, msg.jfgdpMsgs);
        RxBus.getCacheInstance().post(msg);
    }

    /**
     * 组装零散的消息
     *
     * @param identity
     * @param dp
     * @param keyId
     */
    private void assembleMiscMsg(String identity, JFGDPMsg dp, int keyId) {
        if (dp == null) {
            AppLogger.e("dp is null: " + keyId);
            return;
        }
        try {
            Class<?> clazz = ID_2_CLASS_MAP.get(keyId);
            Object o = DpUtils.unpackData(dp.packValue, clazz);
            if (o == null) {
                AppLogger.e("o is null" + keyId);
                return;
            }
            flatMsg.cache(JCache.getAccountCache().getAccount(),
                    identity,
                    Convertor.convert(o, keyId, dp.version));
            Log.d(TAG, "superParser: " + keyId + " " + o);
        } catch (Exception e) {
            AppLogger.e(TAG + keyId + " " + e.getLocalizedMessage());
        }
    }

    private void sendSingleDeviceInfo(String uuid) {
        RxUiEvent.SingleDevice singleDevice = new RxUiEvent.SingleDevice();
        singleDevice.dpMsg = flatMsg.getDevice(JCache.getAccountCache().getAccount(),
                uuid);
        if (singleDevice.dpMsg == null) {
            AppLogger.i(TAG + "DpParser:null ");
            return;
        }
        AppLogger.i(TAG + singleDevice);
        RxBus.getUiInstance().post(singleDevice);
    }

    /**
     * 非空过滤器
     */
    private Func1<RobotoGetDataRsp, Boolean> notNullFunc = new Func1<RobotoGetDataRsp, Boolean>() {
        @Override
        public Boolean call(RobotoGetDataRsp dpDataRsp) {

            boolean good = (dpDataRsp != null
//                    && seqMap.containsKey(dpDataRsp.seq)//包含了此次请求
                    && JCache.getAccountCache() != null);
            AppLogger.i(TAG + "false? " + (dpDataRsp != null) + " " + (JCache.getAccountCache() != null) + " " + good);
            if (dpDataRsp != null) {
//                seqMap.remove(dpDataRsp.seq);
            }
            //过滤
            return good;
        }
    };


    @Override
    public void unregister() {

    }
}