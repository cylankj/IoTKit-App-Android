package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.cylan.entity.JfgEvent;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDPMsgRet;
import com.cylan.jiafeigou.cache.video.History;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.cam.SdCardInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.NetUtils;

import java.util.ArrayList;
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

    public SdCardInfoPresenterImpl(SdCardInfoContract.View view, String uuid) {
        super(view, uuid);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
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
        Subscription subscribe = Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                ArrayList<JFGDPMsg> params = new ArrayList<JFGDPMsg>();
                JFGDPMsg msg = new JFGDPMsg(DpMsgMap.ID_218_DEVICE_FORMAT_SDCARD, 0);
                msg.packValue = DpUtils.pack(0);
                params.add(msg);
                long seq = BaseApplication.getAppComponent().getCmd().robotSetData(uuid, params);
                AppLogger.d("正在格式化 SDCard:Seq 为:" + seq);
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
                AppLogger.e("err_sd: " + e.getLocalizedMessage());
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class).first(deviceSyncRsp -> {
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
                }))
                .timeout(120, TimeUnit.SECONDS, Observable.just(null))
                .map(deviceSyncRsp -> {
                    boolean hasSDCard = false;
                    if (deviceSyncRsp != null && deviceSyncRsp.dpList != null) {
                        for (JFGDPMsg msg : deviceSyncRsp.dpList) {
                            if (msg.id == 204) {
                                DpMsgDefine.DPSdStatus status = BaseApplication.getAppComponent().getPropertyParser().parser((int) msg.id, msg.packValue, msg.version);
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
                .subscribe(code -> mView.clearSdResult(code), e -> AppLogger.e(e.getMessage()));
        addSubscription(subscribe);
    }

    private Observable<Integer> handleClearSDCardResponse(long seq, String uuid) {
        return Observable.zip(RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp.class).first(rsp -> rsp.seq == seq),
                RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class).first(deviceSyncRsp -> {
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
                , (setDataRsp, deviceSyncRsp) -> {
                    boolean setSuccess = false;
                    boolean hasSDCard = false;
                    if (setDataRsp.rets != null) {
                        for (JFGDPMsgRet msgRet : setDataRsp.rets) {
                            if (msgRet.id == 218) {
                                setSuccess = msgRet.ret == 0;
                                break;
                            }
                        }
                    }
                    if (deviceSyncRsp.dpList != null) {
                        for (JFGDPMsg msg : deviceSyncRsp.dpList) {
                            if (msg.id == 204) {
                                DpMsgDefine.DPSdStatus status = BaseApplication.getAppComponent().getPropertyParser().parser((int) msg.id, msg.packValue, msg.version);
                                hasSDCard = status != null && status.hasSdcard && status.err == 0;
                                break;
                            } else if (msg.id == 222) {
                                DpMsgDefine.DPSdcardSummary summary = BaseApplication.getAppComponent().getPropertyParser().parser((int) msg.id, msg.packValue, msg.version);
                                hasSDCard = summary != null && summary.errCode == 0 && summary.hasSdcard;
                                break;
                            }
                        }
                    }
                    return setSuccess && hasSDCard ? 0 : 1;
                }
        );
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
