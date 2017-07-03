package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.SimulatePercent;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.bind.SubmitBindingInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.utils.BindUtils.BIND_SUC;
import static com.cylan.jiafeigou.utils.BindUtils.BIND_TIME_OUT;

/**
 * Created by cylan-hunt on 16-11-12.
 */

public class SubmitBindingInfoImpl extends AbstractPresenter<SubmitBindingInfoContract.View>
        implements SubmitBindingInfoContract.Presenter, SimulatePercent.OnAction {
    private static final long TIME_OUT = 90 * 1000;
    private SimulatePercent simulatePercent;
    private static int bindResult;
    private static long startTick;

    public SubmitBindingInfoImpl(SubmitBindingInfoContract.View view, String uuid) {
        super(view);
        view.setPresenter(this);
        simulatePercent = new SimulatePercent();
        simulatePercent.setOnAction(this);
        startTick = 0;
        bindResult = BindUtils.BIND_PREPARED;
        startTick = 0;
    }

    @Override
    public void startCounting() {
        if (simulatePercent != null)
            simulatePercent.start();
    }

    @Override
    public void endCounting() {
        if (simulatePercent != null) {
            simulatePercent.boost();
        }
    }

    @Override
    public void clean() {
        RxBus.getCacheInstance().removeStickyEvent(RxEvent.BindDeviceEvent.class);
    }

    @Override
    public void start() {
        super.start();
        if (bindResult == BIND_TIME_OUT) {
            return;
        }
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        if (startTick == 0) {//可能是覆盖绑定.
            startTick = System.currentTimeMillis();
            //1.可能是覆盖绑定,或者设备列表中已经有了该设备,并且在线状态.
            if (device.available()) {
                //2.清空net状态
                device.setValue(201, new DpMsgDefine.DPNet());//先清空
            }
        }
        if (System.currentTimeMillis() - startTick > TIME_OUT) {
            //timeout
            mView.bindState(bindResult = BIND_TIME_OUT);
            return;
        }
        //3.重新获取,
        DpMsgDefine.DPNet net = device == null ? null : device.$(201, new DpMsgDefine.DPNet());
        if (device != null && !TextUtils.isEmpty(uuid) && net != null && net.net > 0) {
            //4.net数据可能已经被更新了(重新进入该页面时候使用.)
            endCounting();
            AppLogger.d("finish? ;" + net);
            return;
        }
        //超时
        if (bindResult == BindUtils.BIND_PREPARED) {
            bindResult = BindUtils.BIND_ING;
            addSubscription(submitBindDeviceSub(), "submitBindDeviceSub");
        }
        if (bindResult == BindUtils.BIND_ING) {
            if (simulatePercent != null) simulatePercent.resume();
        }
        addSubscription(bindResultSub(), "bindResultSub");
    }

    /**
     * 绑定结果
     *
     * @return
     */
    private Subscription bindResultSub() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.BindDeviceEvent.class)
                .subscribeOn(AndroidSchedulers.mainThread())
                .filter(ret -> mView != null)
                .subscribe(ret -> {
                    if (ret.bindResult != 0) {//0表示正常绑定
                        RxBus.getCacheInstance().removeStickyEvent(RxEvent.BindDeviceEvent.class);
                        mView.bindState(ret.bindResult);
                        unSubscribe("submitBindDeviceSub");
                    }
                }, AppLogger::e);
    }

    private boolean sendBindInfo;
    private static final int INTERVAL = 3;

    private Subscription submitBindDeviceSub() {
        final long timeout = Math.min(TIME_OUT - (System.currentTimeMillis() - startTick), TIME_OUT);
        return Observable.interval(INTERVAL, TimeUnit.SECONDS, Schedulers.newThread())
                .flatMap(aLong -> {
                    if (INTERVAL * aLong * 1000 >= timeout) {
                        throw new RxEvent.HelperBreaker("timeout");
                    }
                    JFGAccount account = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
                    if (account != null && !sendBindInfo) {
                        sendBindInfo = true;
                        try {
                            String content = PreferencesUtils.getString(JConstant.BINDING_DEVICE);
                            UdpConstant.UdpDevicePortrait portrait = new Gson().fromJson(content, UdpConstant.UdpDevicePortrait.class);
                            if (portrait != null) {
                                BaseApplication.getAppComponent().getCmd().bindDevice(portrait.uuid, portrait.bindCode, portrait.mac, portrait.bindFlag);
                                AppLogger.d("正在发送绑定请求:" + new Gson().toJson(portrait));
                            }
                        } catch (Exception e) {
                            AppLogger.d("err: " + e.getLocalizedMessage());
                        }
                    }
                    //
                    ArrayList<JFGDPMsg> params = new ArrayList<>(1);
                    JFGDPMsg msg = new JFGDPMsg(201, 0);
                    params.add(msg);
                    try {
                        BaseApplication.getAppComponent().getCmd().robotGetData(uuid, params, 1, false, 0);
                    } catch (JfgException e) {
                        AppLogger.d(e.getMessage());
                    }
                    Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
                    DpMsgDefine.DPNet net = null;
                    if (device != null) {
                        net = device.$(201, new DpMsgDefine.DPNet());
                    }
                    AppLogger.d("正在查询设备网络状态:" + new Gson().toJson(net));
                    if (JFGRules.isDeviceOnline(net)) {
                        //成功了
                        throw new RxEvent.HelperBreaker("good");
                    }
                    return null;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                }, throwable -> {
                    if (throwable instanceof RxEvent.HelperBreaker) {
                        if (TextUtils.equals(throwable.getMessage(), "good")) {
                            AppLogger.d("绑定成功,网络状态为:");
                            bindResult = BIND_SUC;
                            endCounting();
                            finalSetSome();
                        } else {
                            //timeout失败
                            mView.bindState(bindResult = BIND_TIME_OUT);
                            AppLogger.e("绑定设备超时");
                        }
                    }
                });
    }

    /**
     * 绑定成功需要设置一些信息
     */
    private void finalSetSome() {
        Observable.just("go")
                .subscribeOn(Schedulers.newThread())
                .subscribe(s -> {
                    Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
                    //303,501
                    try {
                        DpMsgDefine.DPTimeZone timeZone = new DpMsgDefine.DPTimeZone();
                        timeZone.offset = TimeZone.getDefault().getRawOffset() / 1000;
                        timeZone.timezone = TimeZone.getDefault().getID();
                        ArrayList<JFGDPMsg> list = new ArrayList<>();
                        JFGDPMsg _timeZone = new JFGDPMsg(214, System.currentTimeMillis());
                        _timeZone.packValue = timeZone.toBytes();
                        boolean isRs = JFGRules.isRS(device.pid);
                        if (isRs) {
                            JFGDPMsg _303 = new JFGDPMsg(303, System.currentTimeMillis());
                            _303.packValue = DpUtils.pack(2);
                            JFGDPMsg _501 = new JFGDPMsg(501, System.currentTimeMillis());
                            _501.packValue = DpUtils.pack(false);
                            list.add(_303);
                            list.add(_501);
                        }
                        list.add(_timeZone);
                        AppLogger.d("设置睿视属性?" + isRs);
                        BaseApplication.getAppComponent().getCmd().robotSetData(uuid, list);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, AppLogger::e);
    }

    @Override
    public void stop() {
        super.stop();
        if (bindResult == BindUtils.BIND_ING) {
            if (simulatePercent != null) simulatePercent.resume();
        } else {
            if (simulatePercent != null)
                simulatePercent.stop();
        }
    }


    @Override
    public void actionDone() {
        Subscription subscription = Observable.just(null)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe((Object integer) -> {
                    AppLogger.i("actionDone: ");
                    getView().bindState(bindResult);
                }, AppLogger::e);
        addSubscription(subscription, "actionDone");
    }

    @Override
    public void actionPercent(int percent) {
        Subscription subscription = Observable.just(percent)
                .filter((Integer integer) -> (getView() != null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Integer integer) -> {
                    getView().onCounting(integer);
                }, AppLogger::e);
        addSubscription(subscription, "actionPercent");
    }

}
