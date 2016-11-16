package com.cylan.jiafeigou.dp;

import android.util.Log;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDevBaseValue;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jfgapp.jni.JfgAppCmd;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    /**
     * 序列对应的对象
     */
    private final Map<Long, JFGDevice> deviceMap = new HashMap<>();

    private final Map<String, JFGDevice> cacheJfgDeviceMap = new HashMap<>();

    private DpDeviceAssembler() {
    }

    public static DpDeviceAssembler getInstance() {
        if (instance == null)
            instance = new DpDeviceAssembler();
        return instance;
    }

    @Override
    public Subscription[] register() {
        Subscription[] subscriptions = {simpleBulkSubSend2Ui(),
                deviceListSub(),
                deviceDpSub(),
        };
        return subscriptions;
    }

    private Subscription simpleBulkSubSend2Ui() {
        return RxBus.getCacheInstance().toObservable(RxUiEvent.QueryBulkDevice.class)
                .map(new Func1<RxUiEvent.QueryBulkDevice, Object>() {
                    @Override
                    public Object call(RxUiEvent.QueryBulkDevice queryBulkDevice) {
                        RxUiEvent.BulkDeviceList cacheList = new RxUiEvent.BulkDeviceList();
                        Iterator<String> iterator = cacheJfgDeviceMap.keySet().iterator();
                        while (iterator.hasNext()) {
                            cacheList.bulkList.add(cacheJfgDeviceMap.get(iterator.next()));
                        }
                        if (cacheList.bulkList.size() == 0)
                            return null;//do nothing
                        RxBus.getUiInstance().post(cacheList);
                        return null;
                    }
                }).subscribe();
    }

    /**
     * 从dataSource来的消息
     *
     * @return
     */
    private Subscription deviceListSub() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.DeviceRawList.class)
                .filter(new Func1<RxEvent.DeviceRawList, Boolean>() {
                    @Override
                    public Boolean call(RxEvent.DeviceRawList deviceRawList) {
                        return deviceRawList.devices != null && deviceRawList.devices.length > 0;
                    }
                })
                .subscribeOn(Schedulers.newThread())
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
                            DpParameters.Builder builder = new DpParameters.Builder();
                            builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgConstant.NET), 0);
                            builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgConstant.MAC), 0);
                            builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgConstant.SDCARD_STATE), 0);
                            builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgConstant.SDCARD_STORAGE), 0);
                            builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgConstant.CHARGING), 0);
                            builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgConstant.BATTERY), 0);
                            builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgConstant.APP_VERSION), 0);
                            builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgConstant.SYS_VERSION), 0);
                            builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgConstant.LED_INDICATOR), 0);
                            builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgConstant.UP_TIME), 0);
                            builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgConstant.APP_UPLOAD_LOG), 0);
                            builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgConstant.DEVICE_UPLOAD_LOG), 0);
                            builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgConstant.DEVICE_P2P_VERSION), 0);
                            builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgConstant.DEVICE_TIME_ZONE), 0);
                            builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgConstant.DEVICE_RTMP), 0);
                            builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgConstant.DEVICE_VOLTAGE), 0);
                            builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgConstant.DEVICE_MOBILE_NET_PRIORITY), 0);
                            builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgConstant.DEVICE_FORMAT_SDCARD), 0);
                            builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgConstant.DEVICE_BIND_LOG), 0);
                            builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgConstant.SDK_VERSION), 0);
                            long seq = JfgAppCmd.getInstance().robotGetData(list.get(i).uuid, builder.build(), 1, false, 0);
                            seqList.add(seq);
                            deviceMap.put(seq, list.get(i));
                        }
                        return null;
                    }
                })
                .retry(new RxHelper.RxException<>("deviceDpParser"))
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
                        Log.d("DpParser", "DpParser: " + identity);
                        JFGDevice device = deviceMap.get(dpDataRsp.seq);
                        for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : dpDataRsp.map.entrySet()) {
                            JFGDPMsg dp = entry.getValue() != null
                                    && entry.getValue().size() > 0 ? entry.getValue().get(0) : null;
                            final int keyId = entry.getKey();
                            Class<?> clazz = ID_2_CLASS_MAP.get(keyId);
                            if (clazz == null || dp == null) {
                                continue;
                            }
                            try {
                                Object o = unpackData(dp.packValue, clazz);
                                assembleJFGDevice(device,
                                        keyId,
                                        o);
                                Log.d("DpParser", "superParser: " + clazz + " " + identity + " " + o + " " + keyId);
                            } catch (IOException e) {
                                AppLogger.e("DpParser unpack err:" + keyId + " " + e.getLocalizedMessage());
                            } catch (ClassCastException e) {
                                AppLogger.e("DpParser class cast err:" + keyId + " " + e.getLocalizedMessage());
                            }
                        }
                        AppLogger.i(new Gson().toJson(device));
                        //yes...
                        cacheJfgDeviceMap.put(identity, device);
                        //得到最终数据,应该先做缓存.
                        //发射出去,应该是一种sticky模式
                        RxUiEvent.BulkDeviceList cacheList = new RxUiEvent.BulkDeviceList();
                        cacheList.bulkList.add(device);
                        RxBus.getUiInstance().post(cacheList);
                        return null;
                    }
                })
                //此retry能跳过当前一次的exception
                .retry(new RxHelper.RxException<>("deviceDpParser"))
                .subscribe();
    }


    /**
     * 非空过滤器
     */
    private Func1<RobotoGetDataRsp, Boolean> notNullFunc = new Func1<RobotoGetDataRsp, Boolean>() {
        @Override
        public Boolean call(RobotoGetDataRsp dpDataRsp) {
            AppLogger.i("false? " + (dpDataRsp != null
                    && seqList.contains(dpDataRsp.seq)));
            //过滤
            return dpDataRsp != null
                    && seqList.contains(dpDataRsp.seq);
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


    private void assembleJFGDevice(JFGDevice jfgDevice, final int key, Object o) throws ClassCastException {
        if (jfgDevice.base == null) {
            jfgDevice.base = new JFGDevBaseValue();
        }
        switch (key) {
            case 201: {
                DpMsgDefine.MsgNet net = (DpMsgDefine.MsgNet) o;
                jfgDevice.base.netType = net.net;
                jfgDevice.base.netName = net.ssid;
            }
            break;
            case 202: {
                jfgDevice.base.mac = (String) o;
            }
            break;
            case 203: {
                jfgDevice.base.hasSDCard = (boolean) o;
            }
            break;
            case 204: {
                DpMsgDefine.SdStatus status = (DpMsgDefine.SdStatus) o;
                jfgDevice.base.sdcardTotalCapacity = status.total;
                jfgDevice.base.sdcardErrorCode = status.err;
                jfgDevice.base.sdcardUsedCapacity = status.used;
            }
            break;
            case 205: {
                jfgDevice.base.charging = (boolean) o;
            }
            break;
            case 206: {
                jfgDevice.base.battery = (int) o;
            }
            break;
            case 207: {
                jfgDevice.base.version = (String) o;
            }
            break;
            case 208: {
                jfgDevice.base.sysVersion = (String) o;
            }
            break;
            case 209: {
                Log.d("DpDeviceAssembler", "DpDeviceAssembler:209: " + o);
                jfgDevice.base.ledModel = (int) o;
            }
            break;
            case 210: {
            }
            break;
            case 211: {
            }
            break;
            case 212: {
            }
            break;
            case 213: {

            }
            break;
            case 214: {
                jfgDevice.base.strTimeZone = ((DpMsgDefine.MsgTimeZone) o).timezone;
                jfgDevice.base.intTimeZone = ((DpMsgDefine.MsgTimeZone) o).offset;
            }
            break;
            case 215: {
            }
            break;
            case 216: {
            }
            break;
            case 217: {
                jfgDevice.base.priorityMobleNet = (boolean) o;
            }
            break;
            case 218: {
            }
            break;
            case 219: {
            }
            break;
            case 220:

                break;
        }
    }
}
