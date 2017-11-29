package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.cylan.entity.JfgEnum;
import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
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
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BindHelper;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.TimeZone;

import rx.Emitter;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static com.cylan.jiafeigou.utils.BindUtils.TAG_NET_LOGIN_FLOW;
import static com.cylan.jiafeigou.utils.BindUtils.TAG_NET_RECOVERY_FLOW;

/**
 * Created by cylan-hunt on 16-11-12.
 */

public class SubmitBindingInfoImpl extends AbstractPresenter<SubmitBindingInfoContract.View>
        implements SubmitBindingInfoContract.Presenter {
    private SimulatePercent simulatePercent;
    private SimulatePercent.OnAction onAction = new SimulatePercent.OnAction() {
        @Override
        public void actionDone() {

        }

        @Override
        public void actionPercent(int percent) {
            if (mView != null) {
                mView.onCounting(percent);
            }
        }
    };

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


    /**
     * 绑定成功需要设置一些信息
     */
    private void finalSetSome() {
        Subscription subscribe = Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
            //303,501
            long seq;
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
                seq = BaseApplication.getAppComponent().getCmd().robotSetData(uuid, list);
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (Exception e) {
                e.printStackTrace();
                AppLogger.e(e);
                subscriber.onError(e);
            }
        }).subscribe(ret -> {
        }, e -> {
            e.printStackTrace();
            AppLogger.e(e);
        });
        addDestroySubscription(subscribe);
    }

    @Override
    public void sendBindRequest() {
        JFGAccount account = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
        String content = PreferencesUtils.getString(JConstant.BINDING_DEVICE);
        UdpConstant.UdpDevicePortrait portrait = new Gson().fromJson(content, UdpConstant.UdpDevicePortrait.class);
        AppLogger.d("设备画像为:" + new Gson().toJson(portrait));
        if (portrait == null || account == null || TextUtils.isEmpty(portrait.uuid)) {
            AppLogger.w("当前情况下无法进行绑定!!!!!");
            if (mView != null) {
                mView.onBindFailed();
            }
            return;
        }

        Subscription subscribe = BindHelper.sendBindConfig(portrait.uuid, portrait.bindCode, portrait.mac, portrait.bindFlag)
                .doOnSubscribe(() -> {
                    if (simulatePercent != null) {
                        simulatePercent.stop();
                        simulatePercent.setOnAction(null);
                    }
                    simulatePercent = new SimulatePercent();
                    simulatePercent.setOnAction(onAction);
                    simulatePercent.start();
                })
                .doOnTerminate(() -> {
                    if (simulatePercent != null) {
                        simulatePercent.stop();
                        simulatePercent.setOnAction(null);
                    }
                    simulatePercent = null;
                })
                .flatMap(success -> Observable.fromEmitter((Action1<Emitter<Boolean>>) emitter -> {
                    if (success) {
                        simulatePercent.boost(() -> {
                            emitter.onNext(true);
                            emitter.onCompleted();
                        });
                    } else {
                        emitter.onNext(false);
                        emitter.onCompleted();
                    }
                }, Emitter.BackpressureMode.BUFFER))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    //绑定成功了
                    if (success) {
                        finalSetSome();
                        if (mView != null) {
                            mView.onBindSuccess();
                        }
                    } else {
                        if (mView != null) {
                            mView.onBindTimeout();
                        }
                    }
                }, error -> {
                    error.printStackTrace();
                    AppLogger.e(error);
                    if (error instanceof RxEvent.HelperBreaker) {
                        RxEvent.BindDeviceEvent bindDeviceEvent = (RxEvent.BindDeviceEvent) ((RxEvent.HelperBreaker) error).object;

                        switch (bindDeviceEvent.bindResult) {
                            case BindUtils.BIND_FAILED: {
                                if (mView != null) {
                                    mView.onBindFailed();
                                }
                            }
                            break;
                            case JError.ErrorCIDBinded: {
                                if (mView != null) {
                                    mView.onRebindRequired(portrait, bindDeviceEvent.reason);
                                }
                            }
                            break;
                            case BindUtils.BIND_NULL: {
                                if (mView != null) {
                                    mView.onBindCidNotExist();
                                }
                            }
                            break;
                        }
                    }
                });
        addDestroySubscription(subscribe);
    }
}
