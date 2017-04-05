package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.SimulatePercent;
import com.cylan.jiafeigou.n.mvp.contract.bind.SubmitBindingInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.utils.BindUtils.BIND_SUC;
import static com.cylan.jiafeigou.utils.BindUtils.BIND_TIME_OUT;

/**
 * 启动自动轮循的方式更为可靠
 * Created by cylan-hunt on 16-11-12.
 */

public class SubmitBindingInfoContractImpl extends AbstractPresenter<SubmitBindingInfoContract.View>
        implements SubmitBindingInfoContract.Presenter, SimulatePercent.OnAction {

    private SimulatePercent simulatePercent;
    private long startTick;

    public SubmitBindingInfoContractImpl(SubmitBindingInfoContract.View view, String uuid) {
        super(view, uuid);
        view.setPresenter(this);
        simulatePercent = new SimulatePercent();
        simulatePercent.setOnAction(this);
    }

    @Override
    public void startCounting() {
        if (simulatePercent != null)
            simulatePercent.start();
    }

    @Override
    public void endCounting() {
        if (simulatePercent != null)
            simulatePercent.stop();
    }

    @Override
    public void clean() {
    }

    @Override
    public void start() {
        super.start();
        try {
            walkDeviceBindState();
        } catch (IllegalAccessException e) {
            AppLogger.e("err:" + e.getLocalizedMessage());
        }
    }

    private void walkDeviceBindState() throws IllegalAccessException {
        Device device = DataSourceManager.getInstance().getJFGDevice(uuid);
        if (startTick == 0) {
            //可能是覆盖绑定.
            startTick = System.currentTimeMillis();
            //1.可能是覆盖绑定,或者设备列表中已经有了该设备,并且在线状态.
            if (device != null) {
                //2.清空net状态
                DataSourceManager.getInstance().updateValue(uuid, new DpMsgDefine.DPNet(), 201);
            }
        }
        if (System.currentTimeMillis() - startTick >= 90 * 1000) {
            DpMsgDefine.DPNet net = device == null ? new DpMsgDefine.DPNet() : device.$(201, new DpMsgDefine.DPNet());
            if (net.net < 1) {
                //timeout
                mView.bindState(BIND_TIME_OUT);
            } else {
                mView.bindState(BIND_SUC);
            }
            return;
        }
        //3.开启轮循,2s请求一次.

        int netType = NetUtils.getJfgNetType(ContextUtils.getContext());
        AppLogger.d("start repeat get: " + 0 + ",uuid:" + uuid + "netType:" + netType);
        Subscription subscription = Observable.interval(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> {
                    boolean r = System.currentTimeMillis() - startTick < 90 * 1000;
                    if (r) return true;
                    throw new IllegalArgumentException("yes timeout");
                })
                .filter(ret -> mView != null)
                .map(s -> {
                    Device device1 = DataSourceManager.getInstance().getJFGDevice(uuid);
                    if (device1 != null) {
                        DpMsgDefine.DPNet n = device1.$(201, new DpMsgDefine.DPNet());
                        if (n.net < 1) {
                            robotDeviceDataGetRsp();
                        } else {
                            //good?
                            if (simulatePercent != null) simulatePercent.boost();
                            AppLogger.d("yes bingo " + n);
                            unSubscribe("repeat");//完成了,取消订阅.
                        }
                    }
                    return null;
                })
                .subscribe(ret -> {
                }, throwable -> {
                    if (throwable instanceof IllegalArgumentException) {
                        AppLogger.e("err timeout");
                        mView.bindState(BIND_TIME_OUT);
                    }
                });
        addSubscription(subscription, "repeat");
    }

    private void robotDeviceDataGetRsp() {
        Observable.just("get_201_")
                .subscribeOn(Schedulers.newThread())
                .filter(ret -> !TextUtils.isEmpty(uuid))
                .filter(ret -> NetUtils.getJfgNetType(ContextUtils.getContext()) > 0)//有网络,是否还要过滤局域网?
                .map(new SendFunc(uuid))
                .subscribe(robotoGetDataRsp -> {
                    //不在乎那几秒钟.发出去就行了.毕竟这是一个轮训,下一次直接读device
                }, throwable -> AppLogger.e("err:" + throwable.getLocalizedMessage()));
    }


    @Override
    public void actionDone() {
        Subscription subscription = Observable.just(null)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe((Object integer) -> {
                    AppLogger.i("actionDone: " + integer);
                    getView().bindState(BIND_SUC);
                });
        addSubscription(subscription, "actionDone");
    }

    @Override
    public void actionPercent(int percent) {
        Subscription subscription = Observable.just(percent)
                .filter((Integer integer) -> (getView() != null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Integer integer) -> {
                    getView().onCounting(integer);
                });
        addSubscription(subscription, "actionPercent");
    }

    private static final class SendFunc implements Func1<Object, Object> {
        private String uuid;

        public SendFunc(String uuid) {
            this.uuid = uuid;
        }

        @Override
        public Object call(Object o) {
            //需要获取
            ArrayList<JFGDPMsg> list = new ArrayList<>(1);
            JFGDPMsg dp = new JFGDPMsg(201, 0);
            list.add(dp);
            try {
                AppLogger.d("get again");
                return JfgCmdInsurance.getCmd().robotGetData(uuid, list, 1, false, 0);
            } catch (JfgException e) {
                return -1L;
            }
        }
    }
}
