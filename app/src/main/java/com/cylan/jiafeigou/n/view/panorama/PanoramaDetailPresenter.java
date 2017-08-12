package com.cylan.jiafeigou.n.view.panorama;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.base.module.BasePanoramaApiHelper;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;
import com.lzy.okserver.download.DownloadManager;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.dp.DpUtils.unpackData;

/**
 * Created by yanzhendong on 2017/5/10.
 */

public class PanoramaDetailPresenter extends BasePresenter<PanoramaDetailContact.View> implements PanoramaDetailContact.Presenter {

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
        registerSubscription(getReportMsgSub(), getNetWorkMonitorSub());
    }

    private Subscription getReportMsgSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .filter(msg -> TextUtils.equals(msg.uuid, uuid))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    AppLogger.e("收到设备同步消息:" + new Gson().toJson(result));
                    try {
                        for (JFGDPMsg msg : result.dpList) {
                            if (msg.id == 222) {
                                DpMsgDefine.DPSdStatus status = null;
                                try {
                                    status = unpackData(msg.packValue, DpMsgDefine.DPSdStatus.class);
                                } catch (Exception e) {
                                    DpMsgDefine.DPSdStatusInt statusInt = unpackData(msg.packValue, DpMsgDefine.DPSdStatusInt.class);
                                    status.total = statusInt.total;
                                    status.err = statusInt.err;
                                    status.used = statusInt.used;
                                    status.hasSdcard = statusInt.hasSdcard == 1;
                                }
                                if (status != null && !status.hasSdcard) {//SDCard 不存在
                                    mView.onReportDeviceError(2004, true);
                                } else if (status != null && status.err != 0) {//SDCard 需要格式化
                                    mView.onReportDeviceError(2022, true);
                                }
                            }
                        }
                    } catch (Exception e) {
                        AppLogger.e(e.getMessage());
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
    }

    @Override
    public void delete(PanoramaAlbumContact.PanoramaItem item, int mode, long version) {
        if (mode == 0) {
            DownloadManager.getInstance().removeTask(PanoramaAlbumContact.PanoramaItem.getTaskKey(uuid, item.fileName));
            mView.onDeleteResult(0);
        } else if (mode == 1 || mode == 2) {
            Subscription subscribe = BasePanoramaApiHelper.getInstance().delete(uuid, 1, 0, Collections.singletonList(item.fileName))
                    .timeout(10, TimeUnit.SECONDS, Observable.just(null))
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .map(ret -> {
                        DownloadManager.getInstance().removeTask(PanoramaAlbumContact.PanoramaItem.getTaskKey(uuid, item.fileName));
                        return ret;
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ret -> {
                        if (ret != null && ret.ret == 0) {//删除成功
                            mView.onDeleteResult(0);
                        } else {
                            mView.onDeleteResult(-1);//本地删除了,设备删除失败
                        }
                    }, e -> {
                        AppLogger.e(e.getMessage());
                    });
            registerSubscription(subscribe);
        } else if (mode == 3) {
            // TODO: 2017/8/3  
            Subscription subscribe = Observable.just(version)
                    .observeOn(Schedulers.io())
                    .map(ver -> new DPEntity()
                            .setUuid(uuid)
                            .setVersion(version)
                            .setAction(DBAction.DELETED)
                            .setMsgId(DpMsgMap.ID_505_CAMERA_ALARM_MSG))
                    .flatMap(this::perform)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ret -> {
                        if (ret.getResultCode() == 0) {//成功了
                            mView.onDeleteResult(0);
                        } else {
                            mView.onDeleteResult(-1);
                        }
                    }, e -> {
                        e.printStackTrace();
                        AppLogger.d(e.getMessage());
                    });
            registerSubscription(subscribe);

        }
    }

    private Subscription getNetWorkMonitorSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.NetConnectionEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    AppLogger.e("监听到网络状态发生变化");
                    if (event.mobile != null && event.mobile.isConnected()) {
                        mView.onRefreshConnectionMode(event.mobile.getType());
                    } else if (event.wifi != null && event.wifi.isConnected()) {
                        mView.onRefreshConnectionMode(event.wifi.getType());
                    } else {
                        mView.onRefreshConnectionMode(-1);
                    }
                }, e -> {
                });
    }
}
