package com.cylan.jiafeigou.n.mvp.impl.setting;

/**
 * Created by cylan-hunt on 16-12-3.
 */

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.n.mvp.contract.setting.VideoAutoRecordContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.io.IOException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class VideoAutoRecordPresenterImpl extends AbstractPresenter<VideoAutoRecordContract.View>
        implements VideoAutoRecordContract.Presenter {
    //    private BeanCamInfo beanCamInfo;

    public VideoAutoRecordPresenterImpl(VideoAutoRecordContract.View view,
                                        String uuid) {
        super(view);
    }

    @Override
    public void start() {
        super.start();
        monitorDeviceSyncRsp();
    }

    @Override
    public <T extends DataPoint> void updateInfoReq(T value, long id) {
        Observable.just(value)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    try {
                        DataSourceManager.getInstance().updateValue(uuid, value, (int) id);
                    } catch (IllegalAccessException e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
                }, (Throwable throwable) -> {
                    AppLogger.e(throwable.getLocalizedMessage());
                });
    }

    private void monitorDeviceSyncRsp() {
        Subscription subscribe = RxBus.getCacheInstance().toObservableSticky(RxEvent.DeviceSyncRsp.class)
                .filter(ret -> TextUtils.equals(ret.uuid, uuid))
                .retry()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deviceSyncRsp -> {
                    if (deviceSyncRsp == null || deviceSyncRsp.dpList == null) {
                        return;
                    }
                    try {
                        for (JFGDPMsg msg : deviceSyncRsp.dpList) {
                            if (msg.id == DpMsgMap.ID_204_SDCARD_STORAGE) {
                                DpMsgDefine.DPSdStatus status = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPSdStatus.class);
                                if (status != null) {
                                    getView().onSDCardSync(status);
                                }

                            } else if (msg.id == 305) {
                                boolean recordWatcher = DpUtils.unpackData(msg.packValue, boolean.class);
                                getView().onRecordWatcherSync(recordWatcher);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e.getMessage());
                });
        addStopSubscription(subscribe);
    }
}
