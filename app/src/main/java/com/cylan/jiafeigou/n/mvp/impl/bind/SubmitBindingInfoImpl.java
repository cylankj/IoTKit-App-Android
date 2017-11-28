package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.cylan.entity.JfgEnum;
import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
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
import com.cylan.jiafeigou.utils.BindUtils;
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
        this.uuid = uuid;
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
        if (task != null) {
            task.clean();
        }
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
        private UdpConstant.UdpDevicePortrait portrait;
        //        private boolean sendBindInfo;
        private static final int INTERVAL = 3;

        public void clean() {
            if (subscriptionBindResult != null) {
                subscriptionBindResult.unsubscribe();
            }
            if (subscription != null) {
                subscription.unsubscribe();
            }
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
                    .filter(ret -> viewWeakReference.get() != null)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(ret -> {
                        RxBus.getCacheInstance().removeStickyEvent(RxEvent.BindDeviceEvent.class);
                        switch (ret.bindResult) {
                            case JError.ErrorOK: {
                                //绑定成功了
                                viewWeakReference.get().onBindSuccess();
                            }
                            break;
                            case BindUtils.BIND_FAILED: {
                                viewWeakReference.get().onBindFailed();
                            }
                            break;
                            case JError.ErrorCIDBinded: {
                                viewWeakReference.get().onRebindRequired(portrait, ret.reason);
                            }
                            break;
                            case BindUtils.BIND_NULL: {
                                viewWeakReference.get().onBindCidNotExist();
                            }
                            break;
                        }
                        if (subscription != null) {
                            subscription.unsubscribe();
                        }
//                        }
                    }, AppLogger::e);
        }


        private Subscription submitBindDeviceSub() {
            JFGAccount account = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
            String content = PreferencesUtils.getString(JConstant.BINDING_DEVICE);
            portrait = new Gson().fromJson(content, UdpConstant.UdpDevicePortrait.class);
            if (portrait == null || account == null || TextUtils.isEmpty(portrait.uuid)) {
                AppLogger.w("当前情况下无法进行绑定!!!!!");
                return Observable.empty().subscribe();
            }
            return Observable.interval(INTERVAL, TimeUnit.SECONDS, Schedulers.io())
                    .flatMap(aLong -> {
                        if (INTERVAL * aLong * 1000 >= TIME_OUT) {
                            throw new RxEvent.HelperBreaker("timeout");
                        }
                        try {
                            AppLogger.d("设备画像为:" + new Gson().toJson(portrait));
                            int ret = BaseApplication.getAppComponent().getCmd().bindDevice(portrait.uuid, portrait.bindCode, portrait.mac, portrait.bindFlag);
                            AppLogger.d("正在发送绑定请求:" + new Gson().toJson(portrait) + "," + ret);
                            Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(portrait.uuid);
                            if (device != null && device.available()) {
                                //没有 Device 发送查询请求有什么用?
                                ArrayList<JFGDPMsg> params = new ArrayList<>(1);
                                JFGDPMsg msg = new JFGDPMsg(201, 0);
                                params.add(msg);
                                BaseApplication.getAppComponent().getCmd().robotGetData(portrait.uuid, params, 1, false, 0);

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(portrait.uuid);
                        DpMsgDefine.DPNet net;
                        net = device.$(201, new DpMsgDefine.DPNet());
                        AppLogger.d("正在查询设备网络状态:" + new Gson().toJson(net));
                        if (JFGRules.isDeviceOnline(net)) {
                            //成功了
                            // TODO: 2017/8/17 绑定成功了同步所有的属性
                            AppLogger.d("绑定成功了!" + new Gson().toJson(portrait));
                            DataSourceManager.getInstance().syncAllProperty(device.uuid);
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
                                if (viewWeakReference.get() != null) {
                                    viewWeakReference.get().onBindTimeout();
                                }
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
                    .subscribeOn(Schedulers.io())
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
                            if (device.pid == 42) {//康凯斯门铃
                                JFGDPMsg _501 = new JFGDPMsg(501, System.currentTimeMillis());
                                _501.packValue = DpUtils.pack(false);
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
            if (viewWeakReference.get() != null) {
                switch (bindState) {
                    case BIND_SUC: {
                        viewWeakReference.get().onBindSuccess();
                    }
                    break;
                    case BIND_TIME_OUT: {
                        viewWeakReference.get().onBindTimeout();
                    }
                    break;
                }

            }
        }

        @Override
        public void actionPercent(int percent) {
            if (viewWeakReference.get() != null) {
                viewWeakReference.get().onCounting(percent);
            }
        }
    }

}
