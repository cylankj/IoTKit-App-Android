package com.cylan.jiafeigou.dp;

import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.cache.JCache;
import com.cylan.jiafeigou.misc.JConstant;
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

import org.msgpack.MessagePack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_2_CLASS_MAP;


/**
 * Created by cylan-hunt on 16-11-16.
 */

public class DpDeviceAssembler implements IParser {

    private static DpDeviceAssembler instance;
    /**
     * 请求序列
     */
    private final List<Long> seqList = new ArrayList<>();

    private IFlat flatMsg;

    private DpDeviceAssembler() {
        flatMsg = new FlattenMsgDp();
    }

    public static DpDeviceAssembler getInstance() {
        if (instance == null)
            instance = new DpDeviceAssembler();
        return instance;
    }

    @Override
    public Subscription[] register() {
        Subscription[] subscriptions = {
                simpleBulkSubSend2Ui(),
                deviceListSub(),
                deviceDpSub(),
        };
        return subscriptions;
    }

    private Subscription simpleBulkSubSend2Ui() {
        return RxBus.getCacheInstance().toObservable(RxUiEvent.QueryBulkDevice.class)
                .filter(new RxHelper.Filter<>(JCache.getAccountCache() != null
                        && !TextUtils.isEmpty(JCache.getAccountCache().getAccount())))
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
        DpMsgDefine.BaseDpDevice dpDevice = new DpMsgDefine.BaseDpDevice();
        dpDevice.alias = device.alias;
        dpDevice.pid = device.pid;
        dpDevice.shareAccount = device.shareAccount;
        dpDevice.sn = device.sn;
        dpDevice.uuid = device.uuid;
        flatMsg.cache(JCache.getAccountCache().getAccount(), dpDevice);
    }

    /**
     * 从dataSource来的消息
     *
     * @return
     */
    private Subscription deviceListSub() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.DeviceRawList.class)
                //账号不为空
                .map(new Func1<RxEvent.DeviceRawList, RxEvent.DeviceRawList>() {
                    @Override
                    public RxEvent.DeviceRawList call(RxEvent.DeviceRawList deviceRawList) {
                        AppLogger.i("wait_for_account_end: " + (JCache.getAccountCache() == null));
                        return deviceRawList;
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .delay(500, TimeUnit.MILLISECONDS)
                .flatMap(new Func1<RxEvent.DeviceRawList, Observable<List<JFGDevice>>>() {
                    @Override
                    public Observable<List<JFGDevice>> call(RxEvent.DeviceRawList deviceList) {
                        return Observable.just(Arrays.asList(deviceList.devices));
                    }
                })
                .map(new Func1<List<JFGDevice>, Object>() {
                    @Override
                    public Object call(List<JFGDevice> list) {
                        for (int i = 0; i < list.size(); i++) {
                            assembleBase(list.get(i));
                            final int pid = list.get(i).pid;
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
                            AppLogger.i("req: " + list.get(i).uuid);
                            long seq = JfgAppCmd.getInstance().robotGetData(list.get(i).uuid, baseParam.queryParameters(null), 1, false, 0);
                            seqList.add(seq);
                        }
                        return null;
                    }
                })
                .retry(new RxHelper.RxException<>("deviceListSub"))
                .subscribe();
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
                        for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : dpDataRsp.map.entrySet()) {
                            JFGDPMsg dp = entry.getValue() != null
                                    && entry.getValue().size() > 0 ? entry.getValue().get(0) : null;
                            final int keyId = entry.getKey();
                            Class<?> clazz = ID_2_CLASS_MAP.get(keyId);
                            if (clazz == null || dp == null) {
                                continue;
                            }
                            Log.d("superParser", "superParser: " + keyId + " " + clazz);
                            Object o = null;
                            try {
                                o = unpackData(dp.packValue, clazz);
                            } catch (Exception e) {
                                AppLogger.e("DpParser:" + keyId + " " + e.getLocalizedMessage());
                            }
                            try {
                                flatMsg.cache(JCache.getAccountCache().getAccount(),
                                        identity,
                                        wrap(o, keyId, dp.version));
                                Log.d("DpParser", "superParser: " + keyId + " " + o);
                            } catch (Exception e) {
                                AppLogger.e("DpParser:" + keyId + " " + e.getLocalizedMessage());
                            }
                        }
                        if (seqList.size() == 0) {
                            //完成所有设备更新
                            RxUiEvent.BulkDeviceList cacheList = new RxUiEvent.BulkDeviceList();
                            cacheList.allDevices = flatMsg.getAllDevices(JCache.getAccountCache().getAccount());
                            RxBus.getUiInstance().postSticky(cacheList);
                        }
                        return null;
                    }
                })
                //此retry能跳过当前一次的exception
                .retry(new RxHelper.RxException<>("deviceDpSub"))
                .subscribe();
    }

    private DpMsgDefine.BaseDpMsg wrap(Object o, int msgId, long version) {
        DpMsgDefine.BaseDpMsg base = new DpMsgDefine.BaseDpMsg();
        base.version = version;
        base.msgId = msgId;
        base.o = o;
        return base;
    }

    /**
     * 非空过滤器
     */
    private Func1<RobotoGetDataRsp, Boolean> notNullFunc = new Func1<RobotoGetDataRsp, Boolean>() {
        @Override
        public Boolean call(RobotoGetDataRsp dpDataRsp) {
            boolean good = (dpDataRsp != null
                    && seqList.remove(dpDataRsp.seq)//包含了此次请求
                    && JCache.getAccountCache() != null);
            AppLogger.i("false? " + good);
            //过滤
            return good;
        }
    };

    /**
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    private <T> T unpackData(byte[] data, Class<T> clazz) throws IOException {
        MessagePack ms = new MessagePack();
        return ms.read(data, clazz);
    }

    @Override
    public void unregister() {

    }
}