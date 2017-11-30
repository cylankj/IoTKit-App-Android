package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.BaseDeviceInformationFetcher;
import com.cylan.jiafeigou.base.module.BasePanoramaApiHelper;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.video.History;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.udpMsgPack.JfgUdpMsg;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamSettingPresenterImpl extends AbstractPresenter<CamSettingContract.View> implements
        CamSettingContract.Presenter {
    private static final int[] autoRecordMode = {
            R.string.RECORD_MODE,
            R.string.RECORD_MODE_1,
            R.string.RECORD_MODE_2
    };

    public CamSettingPresenterImpl(CamSettingContract.View view, String uuid) {
        super(view);
        DataSourceManager.getInstance().syncDeviceProperty(uuid, 204);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                robotDataSync(),
                robotDeviceDataSync(),
//                onClearSdReqBack(),
                getDeviceUnBindSub()
        };
    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{ConnectivityManager.CONNECTIVITY_ACTION};
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
            BaseDeviceInformationFetcher.getInstance().init(uuid);
            ConnectivityStatus status = ReactiveNetwork.getConnectivityStatus(context);
            Observable.just(status)
                    .throttleFirst(500, TimeUnit.MILLISECONDS)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(connectivityStatus ->
                                    getView().onNetworkChanged(connectivityStatus != null && connectivityStatus.state >= 0),
                            throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()));
        }
    }

    @Override
    public void start() {
        super.start();
        DataSourceManager.getInstance().syncAllProperty(uuid, 204, 222);
        getView().deviceUpdate(getDevice());
        if (JFGRules.isPan720(getDevice().pid)) {
            BaseDeviceInformationFetcher.getInstance().init(uuid);
            BasePanoramaApiHelper.getInstance().getSdInfo(uuid)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        getView().deviceUpdate(getDevice());
                    }, e -> {
                        AppLogger.e(e.getMessage());
                    });
        }
    }

    /**
     * robot同步数据
     *
     * @return
     */
    private Subscription robotDataSync() {
        return RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                .filter((RobotoGetDataRsp jfgRobotSyncData) -> (
                        getView() != null && TextUtils.equals(uuid, jfgRobotSyncData.identity)
                ))
                .observeOn(AndroidSchedulers.mainThread())
                .map((RobotoGetDataRsp update) -> {
                    getView().deviceUpdate(DataSourceManager.getInstance().getDevice(uuid));
                    return null;
                })
                .subscribe(ret -> {
                }, throwable -> AppLogger.e("err: " + MiscUtils.getErr(throwable)));
    }

    /**
     * robot同步数据
     *
     * @return
     */
    private Subscription robotDeviceDataSync() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .filter(jfgRobotSyncData -> (
                        ListUtils.getSize(jfgRobotSyncData.dpList) > 0 &&
                                getView() != null && TextUtils.equals(uuid, jfgRobotSyncData.uuid)
                ))
                .flatMap(ret -> Observable.from(ret.dpList))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                .subscribe(msg -> {
                    try {
                        mView.deviceUpdate(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, throwable -> AppLogger.e("err: " + MiscUtils.getErr(throwable)));
    }

    @Override
    public String getDetailsSubTitle(Context context, boolean hasSdcard, int err) {
        //sd卡状态
//        if (hasSdcard && err != 0) {
//            //sd初始化失败时候显示
//            return context.getString(R.string.SD_INIT_ERR, err);
//        }
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        return device != null && TextUtils.isEmpty(device.alias) ?
                device.uuid : (device != null ? device.alias : "");
    }

    //    一：关闭移动侦测
//1.关闭移动侦测后，下方显示：关闭
//
//    二：打开移动侦测
//1.当设备报警开启周一至周日，时间段为0:00-23:59 下方应显示：每天 全天提示
//3.当设备报警开启周一至周五，时间段为0:00-00:00 下方应显示：工作日 00:00-次日0:00
//            4.当设备报警天数开启周六、周日，时间段为01:00-16:00 下方应显示：周末 01:00-16:00
//            5.当设备报警天数开启周六、周日，时间段为0:00-23:59 下方应显示：工作日 全天提示
//6.当设备报警天数开启周一、周五、周六，时间段为02:00-19:00 下方应显示：周一、周五、周六 02:00-19:00
//            7.当设备报警天数开启周一、周五、周六，时间段为0:00-23:59 下方应显示：周一、周五、周六 全天提示
//
//    天数总结：
//            1.周一至周日显示：每天
//2.周一至周五显示 ：工作日
//3.周六日显示：周末
//4.周一、周六显示：每周一、周六
//
//    时间总结：
//            1.0:00-23：59 显示：全天提示
//2.0：00-0:23:28 显示：0：00-0:23:28
//            3.0:00-0:00显示0:00-次日0:00
//            4:12:01-12:00显示：12:01-次日12:00
    @Override
    public String getAlarmSubTitle(Context context) {
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        boolean f = device.$(DpMsgMap.ID_501_CAMERA_ALARM_FLAG, false);
        DpMsgDefine.DPAlarmInfo info = device.$(DpMsgMap.ID_502_CAMERA_ALARM_INFO, new DpMsgDefine.DPAlarmInfo());
        return MiscUtils.getChaosTime(context, info, f);
    }


    @Override
    public String getAutoRecordTitle(Context context) {
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        boolean isRs = JFGRules.isRS(device.pid);
        int deviceAutoVideoRecord = device.$(DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD,
                isRs ? 2 : -1);
        DpMsgDefine.DPSdStatus sdStatus = device.$(DpMsgMap.ID_204_SDCARD_STORAGE, new DpMsgDefine.DPSdStatus());
        if (sdStatus == null || !sdStatus.hasSdcard || sdStatus.err != 0) {
            return "";
        }
        if (deviceAutoVideoRecord > 2 || deviceAutoVideoRecord < 0) {
            deviceAutoVideoRecord = 0;
        }
        boolean alarmFlag = device.$(DpMsgMap.ID_501_CAMERA_ALARM_FLAG, false);
        if (!alarmFlag && deviceAutoVideoRecord == 0)//不开启,默认不选择
        {
            return "";
        }
        return context.getString(autoRecordMode[deviceAutoVideoRecord]);
    }

    @Override
    public void enableAp() {
        AppLogger.e("还没实现");
    }

    @Override
    public <T extends DataPoint> void updateInfoReq(T value, long id) {
        Subscription subscription = Observable.just(value)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    AppLogger.i("save initSubscription: " + id + " " + value);
                    try {
                        DataSourceManager.getInstance().updateValue(uuid, value, (int) id);
                    } catch (IllegalAccessException e) {
                        AppLogger.e("err:" + e.getLocalizedMessage());
                    }
                    AppLogger.i("save end: " + id + " " + value);
                }, (Throwable throwable) -> AppLogger.e(throwable.getLocalizedMessage()));
        addSubscription(subscription, "updateInfoReq" + id);
    }

    @Override
    public <T extends DataPoint> void updateInfoReq(List<T> value) {
        Subscription subscription = Observable.just(value)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    AppLogger.i("save initSubscription: " + " " + value);
                    try {
                        DataSourceManager.getInstance().updateValue(uuid, value);
                    } catch (IllegalAccessException e) {
                        AppLogger.e("err:" + e.getLocalizedMessage());
                    }
                    AppLogger.i("save end:" + value);
                }, (Throwable throwable) -> AppLogger.e(throwable.getLocalizedMessage()));
        addSubscription(subscription, "updateInfoReq_ex");
    }

    @Override
    public void unbindDevice() {
        Subscription subscribe = Observable.just(new DPEntity()
                .setUuid(uuid)
                .setAction(DBAction.UNBIND))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(item -> BaseDPTaskDispatcher.getInstance().perform(item))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> mView.unbindDeviceRsp(rsp.getResultCode()), e -> {
                    if (e instanceof TimeoutException) {
                        mView.unbindDeviceRsp(-1);
                    }
                    AppLogger.e("err: " + MiscUtils.getErr(e));
                }, () -> {
                });
        addSubscription(subscribe, "unbindDevice");
    }

    @Override
    public Observable<Boolean> switchApModel(int model) {
        final String mac = getDevice().$(202, "");
        if (TextUtils.isEmpty(mac)) {
            AppLogger.d("mac为空");
            return Observable.just(false);
        }
        return Observable.just(model)
                .subscribeOn(Schedulers.io())
                .flatMap(s -> {
                    try {
                        for (int i = 0; i < 3; i++) {
                            JfgUdpMsg.UdpSetApReq req = new JfgUdpMsg.UdpSetApReq(uuid, mac);
                            req.model = s;
                            Command.getInstance().sendLocalMessage(UdpConstant.IP,
                                    UdpConstant.PORT, req.toBytes());
                        }
                        AppLogger.d("send UdpSetApReq :" + uuid + "," + mac);
                    } catch (JfgException e) {
                    }
                    return Observable.just(s);
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg.class)
                        .subscribeOn(Schedulers.io())
                        .timeout(10, TimeUnit.SECONDS))//原型说10s
                .timeout(10, TimeUnit.SECONDS)
                .flatMap(localUdpMsg -> {
                    try {
                        JfgUdpMsg.UdpHeader header = DpUtils.unpackData(localUdpMsg.data, JfgUdpMsg.UdpHeader.class);
                        if (header != null && TextUtils.equals(header.cmd, "set_ap_rsp")) {
                            return Observable.just(localUdpMsg.data);
                        }
                    } catch (Exception e) {
                    }
                    return Observable.just(null);
                })
                .filter(ret -> ret != null)
                .filter(ret -> {
                    try {
                        UdpConstant.SetApRsp rsp = DpUtils.unpackData(ret, UdpConstant.SetApRsp.class);
                        return (rsp != null && TextUtils.equals(rsp.cid, uuid));
                    } catch (IOException e) {
                        return false;
                    }
                }).take(1)
                .flatMap(bytes -> Observable.just(true));
    }

    @Override
    public void addSub(Subscription subscription, String tag) {
        addSubscription(subscription, tag);
    }

    //这块代码 没有经过验证
//    @Override
//    public void clearSdcard() {
//        Observable.just("clear")
//                .subscribeOn(Schedulers.io())
//                .map(s -> {
//                    try {
//                        ArrayList<JFGDPMsg> ipList = new ArrayList<JFGDPMsg>();
//                        JFGDPMsg mesg = new JFGDPMsg(DpMsgMap.ID_218_DEVICE_FORMAT_SDCARD, 0);
//                        mesg.packValue = DpUtils.pack(0);
//                        ipList.add(mesg);
//                        long ret = Command.getInstance().robotSetData(uuid, ipList);
//                    } catch (Exception e) {
//                        AppLogger.e("format sd： " + e.getLocalizedMessage());
//                    }
//                    return null;
//                })
//                .flatMap(result -> RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
//                        .subscribeOn(Schedulers.io())
//                        .filter(ret -> mView != null && TextUtils.equals(ret.uuid, uuid))
//                        .flatMap(ret -> Observable.from(ret.dpList))
//                        .filter(msg -> msg.id == 203)
//                        .timeout(120, TimeUnit.SECONDS))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(ret -> {
//                    try {
//                        DpMsgDefine.DPSdStatus status = DpUtils.unpackData(ret.packValue, DpMsgDefine.DPSdStatus.class, new DpMsgDefine.DPSdStatus());
//                        if (status != null && status.hasSdcard && status.err == 0) {
//                            mView.clearSdResult(0);
//                        } else {
//                            //失败
//                            mView.clearSdResult(1);
//                        }
//                    } catch (Exception e) {
//                    }
//                }, throwable -> {
//                    if (throwable instanceof TimeoutException) {
//                        mView.clearSdResult(1);
//                    }
//                });
//    }
    @Override
    public void clearSdcard() {

        Subscription subscribe = BasePanoramaApiHelper.getInstance().sdFormat(uuid)
                .timeout(120, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    if (ret != null && ret.sdIsExist && ret.sdcard_recogntion == 0) {
                        History.getHistory().clearHistoryFile(uuid);
                        mView.clearSdResult(0);
//                        return hasSDCard ? 0 : deviceSyncRsp == null ? 2 : 1;
                    } else {
                        mView.clearSdResult(1);
//                        mView.onSDFormatResult(-1);
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                    mView.clearSdResult(2);
                });
        addSubscription(subscribe);

//        rx.Observable.just(null)
//                .subscribeOn(Schedulers.io())
//                .subscribe((Object o) -> {
//                    try {
//                        ArrayList<JFGDPMsg> ipList = new ArrayList<JFGDPMsg>();
//                        JFGDPMsg mesg = new JFGDPMsg(DpMsgMap.ID_218_DEVICE_FORMAT_SDCARD, 0);
//                        mesg.packValue = DpUtils.pack(0);
//                        ipList.add(mesg);
//                        Command.getInstance().robotSetData(uuid, ipList);
//                    } catch (Exception e) {
//                        AppLogger.e("format sd： " + e.getLocalizedMessage());
//                    }
//                }, AppLogger::e);
    }

//    @Override
//    public Subscription clearSdcardReqBack() {
//        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
//                .subscribeOn(Schedulers.io())
//                .flatMap(rsp -> {
//                    if (rsp != null && rsp.dpList.size() > 0) {
//                        for (JFGDPMsg dp : rsp.dpList) {
//                            try {
//                                if (dp.id == 203 && TextUtils.equals(uuid, rsp.uuid)) {
//                                    DpMsgDefine.DPSdStatus sdStatus = DpUtils.unpackData(dp.packValue, DpMsgDefine.DPSdStatus.class);
//                                    return Observable.just(sdStatus);
//                                }
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                                return Observable.just(null);
//                            }
//                        }
//                    }
//                    return Observable.just(null);
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(o -> {
//                    if (o != null) {
//                        //清空SD卡提示
//                        if (isInitSd) {
//                            getView().clearSdResult(0);
//                            isInitSd = false;
//                        }
//
//                    }
//                }, AppLogger::e);
//    }

//    @Override
//    public Subscription onClearSdReqBack() {
//        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
//                .subscribeOn(Schedulers.io())
//                .filter(ret -> mView != null && TextUtils.equals(ret.uuid, uuid))
//                .map(ret -> ret.dpList)
//                .flatMap(Observable::from)
//                .filter(msg -> msg.id == 203)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(result -> {
//                    DpMsgDefine.DPSdStatus status = null;
//                    try {
//                        try {
//                            status = DpUtils.unpackData(result.packValue, DpMsgDefine.DPSdStatus.class);
//
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            status = new DpMsgDefine.DPSdStatus();
//                            DpMsgDefine.DPSdStatusInt statusInt = DpUtils.unpackData(result.packValue, DpMsgDefine.DPSdStatusInt.class);
//                            status.err = statusInt.err;
//                            status.hasSdcard = statusInt.hasSdcard == 1;
//                            status.used = statusInt.used;
//                            status.total = statusInt.total;
//                        }
//                    } catch (Exception e) {
//
//                    }
//                    if (status != null) {
//                        getView().clearSdResult(status.hasSdcard && status.err == 0 ? 0 : 1);
//                    } else {
//                        getView().clearSdResult(1);
//                    }
//                }, AppLogger::e);
//    }

    @Override
    public void clearBellRecord(String uuid) {
        AppLogger.d("删除uuid下所有401dp");
        Subscription subscribe = Observable.just(new DPEntity()
                .setMsgId(DpMsgMap.ID_401_BELL_CALL_STATE)
                .setUuid(uuid)
                .setAction(DBAction.CLEARED))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(ret -> BaseDPTaskDispatcher.getInstance().perform(ret))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                    if (rsp.getResultCode() == 0) {//删除成功
                        mView.onClearBellRecordSuccess();
                        RxBus.getCacheInstance().postSticky(new RxEvent.ClearDataEvent(DpMsgMap.ID_401_BELL_CALL_STATE));
                        AppLogger.d("清空呼叫记录成功!");
                    } else {
                        mView.onClearBellRecordFailed();
                        AppLogger.d("清空呼叫记录失败");
                    }
                }, e -> {
                    mView.onClearBellRecordFailed();
                    AppLogger.d(e.getMessage());
                    AppLogger.d("清空呼叫记录失败!");
                });
        addSubscription(subscribe);
    }


    private Subscription getDeviceUnBindSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceUnBindedEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(event -> TextUtils.equals(event.uuid, uuid))
                .subscribe(event -> {
                    if (mView != null) {
                        mView.onDeviceUnBind();
                    }
                }, e -> AppLogger.d(e.getMessage()));
    }

}
