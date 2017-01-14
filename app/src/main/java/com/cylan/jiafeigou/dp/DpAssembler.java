package com.cylan.jiafeigou.dp;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDevice;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;


/**
 * Created by cylan-hunt on 16-11-16.
 *
 * @deprecated 请使用 {@link com.cylan.jiafeigou.cache.pool.GlobalDataProxy}
 */
@Deprecated
public class DpAssembler implements IParser {
    private static final String TAG = "DpAssembler:";

    private static DpAssembler instance;


    private DpAssembler() {
    }

    private HashMap<String, HashMap<String, Long>> seqMap = new HashMap<>();


    public static DpAssembler getInstance() {
        if (instance == null)
            instance = new DpAssembler();
        return instance;
    }

    @Override
    public Subscription[] register() {
        return new Subscription[]{
                deviceListSub()
        };
    }


    private void getHistoryData(String uuid) {
        RxEvent.JFGHistoryVideoReq req = new RxEvent.JFGHistoryVideoReq();
        req.uuid = uuid;
        RxBus.getCacheInstance().post(req);
    }

    private Observable<JFGAccount> monitorJFGAccount() {
        return RxBus.getCacheInstance().toObservableSticky(JFGAccount.class)
                .map((JFGAccount account) -> {
                    AppLogger.i("JFGAccount.class");
                    return account;
                });
    }

    private Observable<RxEvent.DeviceRawList> monitorDeviceRawList() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceRawList.class)
                .map((RxEvent.DeviceRawList deviceRawList) -> {
                    AppLogger.i("DeviceRawList.class: ");
                    return deviceRawList;
                });
    }

    /**
     * 从dataSource来的消息
     * 账号与设备列表两个信息都收到之后,才查询
     *
     * @return
     */
    private Subscription deviceListSub() {
        return Observable.zip(monitorJFGAccount(), monitorDeviceRawList(),
                (JFGAccount account, RxEvent.DeviceRawList deviceRawList) -> {
                    AppLogger.i(TAG + " yes jfgAccount is ready and deviceList is ready too");
                    return Arrays.asList(deviceRawList.devices);
                })
                .subscribeOn(Schedulers.newThread())
                .map((List<JFGDevice> list) -> {
                    HashMap<String, Long> map = new HashMap<>();
                    for (int i = 0; i < list.size(); i++) {
                        if (merger(list.get(i).pid) == null) continue;
                        GlobalDataProxy.getInstance().cacheDevice(list.get(i).uuid, list.get(i));
                        getUnreadMsg(list.get(i));
                        getHistoryData(list.get(i).uuid);
                        final int pid = list.get(i).pid;
                        BaseParam baseParam = merger(pid);
                        long seq = 0;
                        try {
                            seq = JfgCmdInsurance.getCmd().robotGetData(list.get(i).uuid, baseParam.queryParameters(null), 1, false, 0);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                        map.put(list.get(i).uuid, seq);
                        AppLogger.i(TAG + " req: " + list.get(i).uuid);
                    }
                    seqMap.put("deviceListSub", map);
                    return null;
                })
                .retry(new RxHelper.RxException<>(TAG + " deviceListSub"))
                .subscribe();
    }

    private void getUnreadMsg(JFGDevice device) {
        if (JFGRules.isCamera(device.pid))
            try {
                GlobalDataProxy.getInstance().fetchUnreadCount(device.uuid, DpMsgMap.ID_505_CAMERA_ALARM_MSG);
            } catch (JfgException e) {
                AppLogger.e("" + e.getLocalizedMessage());
            }
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

    @Override
    public void clear() {
    }
}