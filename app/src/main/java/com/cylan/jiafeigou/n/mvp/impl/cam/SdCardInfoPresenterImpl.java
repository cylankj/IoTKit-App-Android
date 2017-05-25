package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
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
                .flatMap(seq -> handleClearSDCardResponse(seq, uuid))
                .timeout(120, TimeUnit.SECONDS, Observable.just(null))
                .map(success -> {
                    if (success) {
                        History.getHistory().clearHistoryFile(uuid);
                    }
                    return success;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    mView.clearSdResult(success == null ? 2 : (success ? 0 : 1));
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
        addSubscription(subscribe);
    }

    private Observable<Boolean> handleClearSDCardResponse(long seq, String uuid) {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .filter(rsp -> TextUtils.equals(rsp.uuid, uuid))
                .map(rsp -> {
                    if (rsp != null && rsp.dpList != null && rsp.dpList.size() > 0) {
                        for (JFGDPMsg msg : rsp.dpList) {
                            if (msg.id == 204) {
                                DpMsgDefine.DPSdStatus status = BaseApplication.getAppComponent().getPropertyParser().parser((int) msg.id, msg.packValue, msg.version);
                                return status != null && status.err == 0 && status.hasSdcard;
                            } else if (msg.id == 222) {
                                DpMsgDefine.DPSdcardSummary summary = BaseApplication.getAppComponent().getPropertyParser().parser((int) msg.id, msg.packValue, msg.version);
                                return summary != null && summary.hasSdcard && summary.errCode == 0;
                            }
                        }
                    }
                    return false;
                })
                .first(has -> has)
                .first();
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
