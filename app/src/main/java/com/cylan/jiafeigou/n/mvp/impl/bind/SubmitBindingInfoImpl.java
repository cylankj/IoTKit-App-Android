package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.cylan.entity.JfgEnum;
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
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.utils.BindUtils.BIND_SUC;
import static com.cylan.jiafeigou.utils.BindUtils.BIND_TIME_OUT;
import static com.cylan.jiafeigou.utils.BindUtils.TAG_NET_FINAL_FLOW;
import static com.cylan.jiafeigou.utils.BindUtils.TAG_NET_LOGIN_FLOW;
import static com.cylan.jiafeigou.utils.BindUtils.TAG_NET_RECOVERY_FLOW;

/**
 * Created by cylan-hunt on 16-11-12.
 */

public class SubmitBindingInfoImpl extends AbstractPresenter<SubmitBindingInfoContract.View>
        implements SubmitBindingInfoContract.Presenter {
    private BindResultTask task;

    public SubmitBindingInfoImpl(SubmitBindingInfoContract.View view, String uuid) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{ConnectivityManager.CONNECTIVITY_ACTION};
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        super.onNetworkChanged(context, intent);
        int net = NetUtils.getJfgNetType();
        if (net != 0) {
            AppLogger.d("网络恢复了:" + NetUtils.getNetName(ContextUtils.getContext()));
            BaseApplication.getAppComponent().getCmd().reportEnvChange(JfgEnum.ENVENT_TYPE.ENV_NETWORK_LOST);
            BaseApplication.getAppComponent().getCmd().reportEnvChange(JfgEnum.ENVENT_TYPE.ENV_NETWORK_CONNECTED);
            PerformanceUtils.stopTrace(TAG_NET_RECOVERY_FLOW);
            PerformanceUtils.startTrace(TAG_NET_LOGIN_FLOW);
        }
    }

    @Override
    public void clean() {
        RxBus.getCacheInstance().removeStickyEvent(RxEvent.BindDeviceEvent.class);
        if (task != null) task.clean();
    }

    @Override
    public void start() {
        super.start();
        if (task == null) {
            task = new BindResultTask(uuid, mView, this);
            task.call(uuid);
        } else {
            if (task.getBindState() == BIND_TIME_OUT) {
                //超时
            } else if (task.getBindState() == BIND_SUC) {
                //失败
            } else {
                task.setListener(mView);
            }
        }
    }


    private static final class BindResultTask implements Action1<String>, SimulatePercent.OnAction {
        private WeakReference<SubmitBindingInfoContract.View> viewWeakReference;
        private WeakReference<SubmitBindingInfoContract.Presenter> presenterWeakReference;
        private SimulatePercent simulatePercent;
        private int bindState;
        private Subscription subscription;
        private Subscription subscriptionBindResult;
        private static final long TIME_OUT = 90 * 1000;
        private String uuid;

        private boolean sendBindInfo;
        private static final int INTERVAL = 3;

        public void clean() {
            if (subscriptionBindResult != null) subscriptionBindResult.unsubscribe();
            if (subscription != null) subscription.unsubscribe();
        }

        public int getBindState() {
            return bindState;
        }

        public BindResultTask(String uuid, SubmitBindingInfoContract.View v, SubmitBindingInfoContract.Presenter p) {
            this.uuid = uuid;
            this.viewWeakReference = new WeakReference<>(v);
            this.presenterWeakReference = new WeakReference<>(p);
            simulatePercent = new SimulatePercent();
            simulatePercent.setOnAction(this);
            simulatePercent.start();
        }

        public void setListener(SubmitBindingInfoContract.View view) {
            this.viewWeakReference = new WeakReference<>(view);
        }

        @Override
        public void call(String s) {
            clean();
            subscription = submitBindDeviceSub();
            subscriptionBindResult = bindResultSub();
        }


        /**
         * 绑定结果
         *
         * @return
         */
        private Subscription bindResultSub() {
            return RxBus.getCacheInstance().toObservableSticky(RxEvent.BindDeviceEvent.class)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .filter(ret -> viewWeakReference.get() != null)
                    .subscribe(ret -> {
                        if (ret.bindResult != 0) {//0表示正常绑定
                            RxBus.getCacheInstance().removeStickyEvent(RxEvent.BindDeviceEvent.class);
                            viewWeakReference.get().bindState(ret.bindResult);
                            if (subscription != null) subscription.unsubscribe();
                        }
                    }, AppLogger::e);
        }

        private Subscription submitBindDeviceSub() {
            return Observable.interval(INTERVAL, TimeUnit.SECONDS, Schedulers.newThread())
                    .flatMap(aLong -> {
                        if (INTERVAL * aLong * 1000 >= TIME_OUT) {
                            throw new RxEvent.HelperBreaker("timeout");
                        }
                        JFGAccount account = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
                        if (account != null && !sendBindInfo) {
                            try {
                                String content = PreferencesUtils.getString(JConstant.BINDING_DEVICE);
                                UdpConstant.UdpDevicePortrait portrait = new Gson().fromJson(content, UdpConstant.UdpDevicePortrait.class);
                                if (portrait != null) {
                                    int ret = BaseApplication.getAppComponent().getCmd().bindDevice(portrait.uuid, portrait.bindCode, portrait.mac, portrait.bindFlag);
                                    AppLogger.d("正在发送绑定请求:" + new Gson().toJson(portrait) + "," + ret);
                                    if (ret != 0) {
                                        AppLogger.d("客户端登录失败.需要不断尝试");
                                    } else {
                                        sendBindInfo = true;
                                        PerformanceUtils.stopTrace(TAG_NET_LOGIN_FLOW);
                                        PerformanceUtils.startTrace(TAG_NET_FINAL_FLOW);
                                    }
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
                            bindState = BIND_SUC;
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
                                finalSetSome();
                                simulatePercent.boost();
                                PerformanceUtils.stopTrace(TAG_NET_FINAL_FLOW);
                            } else {
                                //timeout失败
                                bindState = BIND_TIME_OUT;
                                simulatePercent.stop();
                                if (viewWeakReference.get() != null)
                                    viewWeakReference.get().bindState(bindState);
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
        public void actionDone() {
            if (viewWeakReference.get() != null) viewWeakReference.get().bindState(bindState);
        }

        @Override
        public void actionPercent(int percent) {
            if (viewWeakReference.get() != null) viewWeakReference.get().onCounting(percent);
        }
    }

}
