package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.cylan.entity.JfgEvent;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.base.module.BasePanoramaApiHelper;
import com.cylan.jiafeigou.cache.video.History;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.cam.SdCardInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.NetUtils;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION;

/**
 * 作者：zsl
 * 创建时间：2017/2/13
 * 描述：
 */
public class SdCardInfoPresenterImpl extends AbstractPresenter<SdCardInfoContract.View> implements SdCardInfoContract.Presenter {

    private static final long TIMEOUT = 2 * 60L;


    public static class Event {
    }

    public SdCardInfoPresenterImpl(SdCardInfoContract.View view, String uuid) {
        super(view, uuid);
        view.setPresenter(this);
    }


    @Override
    public void start() {
        super.start();
        addSubscription(getSDCardStateMonitor());
    }

    private Subscription getSDCardStateMonitor() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .filter(msg -> TextUtils.equals(msg.uuid, uuid))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deviceSyncRsp -> {
                    boolean hasSDCard = false;
                    AppLogger.e("收到设备同步消息:" + new Gson().toJson(deviceSyncRsp));
                    if (deviceSyncRsp != null && deviceSyncRsp.dpList != null) {
                        for (JFGDPMsg msg : deviceSyncRsp.dpList) {
                            if (msg.id == 204) {
                                DpMsgDefine.DPSdStatus status = null;
                                DpMsgDefine.DPSdStatusInt statusInt = null;
                                try {
                                    status = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPSdStatus.class);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    try {
                                        statusInt = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPSdStatusInt.class);
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                                if (status == null && statusInt != null) {
                                    status = new DpMsgDefine.DPSdStatus();
                                    status.hasSdcard = statusInt.hasSdcard == 1;
                                    status.err = statusInt.err;
                                    status.used = statusInt.used;
                                    status.total = statusInt.total;
                                }
                                hasSDCard = status != null && status.hasSdcard && status.err == 0;
                                if (!hasSDCard) {
                                    mView.showSdPopDialog();
                                }
                                break;
                            } else if (msg.id == 222) {
                                DpMsgDefine.DPSdcardSummary summary = BaseApplication.getAppComponent().getPropertyParser().parser((int) msg.id, msg.packValue, msg.version);
                                hasSDCard = summary != null && summary.errCode == 0 && summary.hasSdcard;
                                if (!hasSDCard) {
                                    mView.showSdPopDialog();
                                }
                                break;
                            }
                        }
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
    }

    @Override
    protected Subscription[] register() {
        return super.register();
    }

    private Subscription robotDataSync() {
        return RxBus.getCacheInstance().toObservable(JfgEvent.RobotoSyncData.class)
                .subscribeOn(Schedulers.newThread())
                .filter(ret -> TextUtils.equals(ret.identity, uuid))
                .flatMap(robotoSyncData -> Observable.from(robotoSyncData.list))
                .filter(ret -> ret.id == 204 && mView != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    try {
                        DpMsgDefine.DPSdStatus status = DpUtils.unpack(ret.packValue, DpMsgDefine.DPSdStatus.class);
                        if (status != null && (!status.hasSdcard || status.err != 0)) {
                            mView.showSdPopDialog();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, AppLogger::e);
    }

    /**
     * 是否有SD卡
     *
     * @return
     */
    @Override
    public boolean getSdcardState() {
        DpMsgDefine.DPSdStatus sdStatus = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid).$(204, new DpMsgDefine.DPSdStatus());
        //sd卡状态
        if (sdStatus != null) {
            if (!sdStatus.hasSdcard && sdStatus.err != 0) {
                //sd初始化失败
                return false;
            }
        }
        if (sdStatus != null && !sdStatus.hasSdcard) {
            return false;
        }
        return true;
    }

    public void clearSDCard() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .mergeWith(BasePanoramaApiHelper.getInstance().sdFormat(uuid).subscribeOn(Schedulers.io())
                        .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)))
                .subscribeOn(Schedulers.io())
                .first(deviceSyncRsp -> {
                    if (!TextUtils.equals(deviceSyncRsp.uuid, uuid)) {
                        return false;
                    }
                    if (deviceSyncRsp.dpList != null && deviceSyncRsp.dpList.size() > 0) {
                        for (JFGDPMsg msg : deviceSyncRsp.dpList) {
                            if (msg.id == 204 || msg.id == 222) {
                                return true;
                            }
                        }
                    }
                    return false;
                })
                .timeout(120, TimeUnit.SECONDS, Observable.create(subscriber -> mView.clearSdResult(2)))
                .map(deviceSyncRsp -> {
                    boolean hasSDCard = false;
                    AppLogger.e("收到设备同步消息:" + new Gson().toJson(deviceSyncRsp));
                    if (deviceSyncRsp != null && deviceSyncRsp.dpList != null) {
                        for (JFGDPMsg msg : deviceSyncRsp.dpList) {
                            if (msg.id == 204) {
                                DpMsgDefine.DPSdStatus status = null;
                                DpMsgDefine.DPSdStatusInt statusInt = null;
                                try {
                                    status = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPSdStatus.class);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    try {
                                        statusInt = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPSdStatusInt.class);
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                                if (status == null && statusInt != null) {
                                    status = new DpMsgDefine.DPSdStatus();
                                    status.hasSdcard = statusInt.hasSdcard == 1;
                                    status.err = statusInt.err;
                                    status.used = statusInt.used;
                                    status.total = statusInt.total;
                                }
                                hasSDCard = status != null && status.hasSdcard && status.err == 0;
                                break;
                            } else if (msg.id == 222) {
                                DpMsgDefine.DPSdcardSummary summary = BaseApplication.getAppComponent().getPropertyParser().parser((int) msg.id, msg.packValue, msg.version);
                                hasSDCard = summary != null && summary.errCode == 0 && summary.hasSdcard;
                                break;
                            }
                        }
                    }
                    if (hasSDCard) {
                        History.getHistory().clearHistoryFile(uuid);
                    }
                    return hasSDCard ? 0 : deviceSyncRsp == null ? 2 : 1;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(Observable.just(2))
                .subscribe(code -> mView.clearSdResult(code), e -> AppLogger.e(e.getMessage()));
        addSubscription(subscribe);
    }

    /**
     * 获取到sd卡的容量
     *
     * @param uuid
     */
    @Override
    public void getSdCapacity(String uuid) {

    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{ConnectivityManager.CONNECTIVITY_ACTION,
                NETWORK_STATE_CHANGED_ACTION};
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        if (mView != null) {
            mView.onNetworkChanged(NetUtils.getJfgNetType() > 0);
        }
    }
}
