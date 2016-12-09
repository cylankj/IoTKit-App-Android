package com.cylan.jiafeigou.dp;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.cache.JCache;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.mvp.model.BaseBean;
import com.cylan.jiafeigou.n.mvp.model.param.CamParam;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-11-28.
 */

public class DpCam extends DpBase<CamParam> {

    private static final String TAG = "DpCam";

    public Subscription[] register() {
        return new Subscription[]{camAttrSub()};
    }

    @Override
    public void clear() {

    }

    private Subscription camAttrSub() {
        return Observable.zip(monitorJFGAccount(), monitorDeviceRawList(),
                new Func2<JFGAccount, RxEvent.DeviceRawList, List<JFGDevice>>() {
                    @Override
                    public List<JFGDevice> call(JFGAccount account,
                                                RxEvent.DeviceRawList deviceRawList) {
                        AppLogger.i(TAG + " cam yes jfgAccount is ready and deviceList is ready too");
                        return Arrays.asList(deviceRawList.devices);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .map(new Func1<List<JFGDevice>, Object>() {
                    @Override
                    public Object call(List<JFGDevice> list) {
                        for (int i = 0; i < list.size(); i++) {
                            //过滤摄像头设备
                            if (JFGRules.isCamera(list.get(i).pid))
                                assembleBase(list.get(i));
                            long seq = JfgAppCmd.getInstance().robotGetData(list.get(i).uuid, getParameters(), 1, false, 0);
                        }
                        return null;
                    }
                })
                .retry(new RxHelper.RxException<>(TAG + " camAttrSub"))
                .subscribe();
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
//        flatMsg.cache(JCache.getAccountCache().getAccount(), dpDevice);
    }

    private Observable<JFGAccount> monitorJFGAccount() {
        return RxBus.getCacheInstance().toObservableSticky(JFGAccount.class);
    }

    private Observable<RxEvent.DeviceRawList> monitorDeviceRawList() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceRawList.class);
    }

    @Override
    protected ArrayList<JFGDPMsg> getParameters() {
        return new CamParam().queryParameters(null);
    }
}
