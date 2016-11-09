package com.cylan.jiafeigou.dp;

import android.util.Log;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.rxbus.RxBus;

import org.msgpack.MessagePack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by cylan-hunt on 16-11-8.
 * dp数据解析器
 */

public class DpParser {
    private static final String TAG = "DpParser";
    public final List<Long> seqList = new ArrayList<>();
    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    public static DpParser dpParser;

    private DpParser() {
    }

    public static DpParser getDpParser() {
        if (dpParser == null) {
            dpParser = new DpParser();
        }
        return dpParser;
    }

    public void registerDpParser() {
        compositeSubscription.add(updateJfgDeviceAttr());
        compositeSubscription.add(superParser());
    }

    private Subscription updateJfgDeviceAttr() {
        return RxBus.getDefault().toObservable(RxEvent.DeviceList.class)
                .subscribeOn(Schedulers.newThread())
                .flatMap(new Func1<RxEvent.DeviceList, Observable<List<JFGDevice>>>() {
                    @Override
                    public Observable<List<JFGDevice>> call(RxEvent.DeviceList deviceList) {
                        return deviceList == null || deviceList.jfgDevices == null ? null :
                                Observable.just(deviceList.jfgDevices);
                    }
                })
                .map(new Func1<List<JFGDevice>, Object>() {
                    @Override
                    public Object call(List<JFGDevice> list) {
                        for (int i = 0; i < list.size(); i++) {
                            DpParameters.Builder builder = new DpParameters.Builder();
                            builder.addParam(DpMsgIdMap.IdMap.get(DpMsgConstant.NET), 0);
                            builder.addParam(DpMsgIdMap.IdMap.get(DpMsgConstant.MAC), 0);
                            builder.addParam(DpMsgIdMap.IdMap.get(DpMsgConstant.SDCARD_STATE), 0);
                            builder.addParam(DpMsgIdMap.IdMap.get(DpMsgConstant.SDCARD_STORAGE), 0);
                            builder.addParam(DpMsgIdMap.IdMap.get(DpMsgConstant.CHARGING), 0);
                            builder.addParam(DpMsgIdMap.IdMap.get(DpMsgConstant.BATTERY), 0);
                            builder.addParam(DpMsgIdMap.IdMap.get(DpMsgConstant.APP_VERSION), 0);
                            builder.addParam(DpMsgIdMap.IdMap.get(DpMsgConstant.SYS_VERSION), 0);
                            builder.addParam(DpMsgIdMap.IdMap.get(DpMsgConstant.LED_INDICATOR), 0);
                            builder.addParam(DpMsgIdMap.IdMap.get(DpMsgConstant.UP_TIME), 0);
                            builder.addParam(DpMsgIdMap.IdMap.get(DpMsgConstant.APP_UPLOAD_LOG), 0);
                            builder.addParam(DpMsgIdMap.IdMap.get(DpMsgConstant.DEVICE_UPLOAD_LOG), 0);
                            builder.addParam(DpMsgIdMap.IdMap.get(DpMsgConstant.DEVICE_P2P_VERSION), 0);
                            builder.addParam(DpMsgIdMap.IdMap.get(DpMsgConstant.DEVICE_TIME_ZONE), 0);
                            builder.addParam(DpMsgIdMap.IdMap.get(DpMsgConstant.DEVICE_RTMP), 0);
                            builder.addParam(DpMsgIdMap.IdMap.get(DpMsgConstant.DEVICE_VOLTAGE), 0);
                            builder.addParam(DpMsgIdMap.IdMap.get(DpMsgConstant.DEVICE_MOBILE_NET_PRIORITY), 0);
                            builder.addParam(DpMsgIdMap.IdMap.get(DpMsgConstant.DEVICE_FORMAT_SDCARD), 0);
                            builder.addParam(DpMsgIdMap.IdMap.get(DpMsgConstant.DEVICE_BIND_LOG), 0);
                            builder.addParam(DpMsgIdMap.IdMap.get(DpMsgConstant.SDK_VERSION), 0);
                            AppLogger.i("DpParser: " + builder.toString());
                            long seq = JfgAppCmd.getInstance().robotGetData(list.get(i).uuid, builder.build(), 1, false, 0);
                            seqList.add(seq);
                        }
                        return null;
                    }
                })
                .retry()
                .subscribe();
    }

    private Subscription superParser() {
        return RxBus.getDefault().toObservable(RxEvent.DpDataRsp.class)
                .subscribeOn(Schedulers.computation())
                .filter(notNullFunc)
                .map(new Func1<RxEvent.DpDataRsp, Integer>() {
                    @Override
                    public Integer call(RxEvent.DpDataRsp dpDataRsp) {
                        final String identity = dpDataRsp.robotoGetDataRsp.identity;
                        for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : dpDataRsp.robotoGetDataRsp.map.entrySet()) {
                            final int keyId = entry.getKey();
                            JFGDPMsg dp = entry.getValue().get(0);
                            Class<?> clazz = DpMsgIdClassMap.Id2ClassMap.get(keyId);
                            try {
                                Object o = unpackData(dp.packValue, clazz);
                                Log.d("DpParser", "superParser: " + o + " " + keyId);
                            } catch (Exception e) {
                                AppLogger.e("DpParser :" + keyId + " " + e.getLocalizedMessage());
                            }
                        }
                        return null;
                    }
                })
                //此retry能跳过当前一次的exception
                .retry(exceptionFun)
                .subscribe();
    }

    public void unregisterDpParser() {
        if (compositeSubscription != null)
            compositeSubscription.unsubscribe();
    }

    /**
     * 异常情况下，返回true,将继续订阅
     */
    private Func2<Integer, Throwable, Boolean> exceptionFun = new Func2<Integer, Throwable, Boolean>() {
        @Override
        public Boolean call(Integer integer, Throwable throwable) {
            //此处return true:表示继续订阅，
            AppLogger.e("DpParser: " + throwable.getLocalizedMessage());
            return true;
        }
    };

    /**
     * 非空过滤器
     */
    private Func1<RxEvent.DpDataRsp, Boolean> notNullFunc = new Func1<RxEvent.DpDataRsp, Boolean>() {
        @Override
        public Boolean call(RxEvent.DpDataRsp dpDataRsp) {
            //过滤
            return dpDataRsp != null && dpDataRsp.robotoGetDataRsp != null &&
                    seqList.contains(dpDataRsp.robotoGetDataRsp.seq);
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
}
