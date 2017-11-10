package com.cylan.jiafeigou.n.view.panorama;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.base.module.BaseDeviceInformationFetcher;
import com.cylan.jiafeigou.base.module.BasePanoramaApiHelper;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.module.GlideApp;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BitmapUtils;
import com.cylan.jiafeigou.utils.CamWarnGlideURL;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.google.gson.Gson;
import com.lzy.okserver.download.DownloadManager;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.dp.DpUtils.unpackData;

/**
 * Created by yanzhendong on 2017/5/10.
 */

public class PanoramaDetailPresenter extends BasePresenter<PanoramaDetailContact.View> implements PanoramaDetailContact.Presenter {
    private boolean hasSDCard;

    @Inject
    public PanoramaDetailPresenter(PanoramaDetailContact.View view) {
        super(view);
        Device device = DataSourceManager.getInstance().getDevice(uuid);

        DpMsgDefine.DPSdStatus status = device.$(204, new DpMsgDefine.DPSdStatus());

        hasSDCard = status.hasSdcard;
    }

    @Override
    public void subscribe() {
        super.subscribe();
        subscribeReportMsg();
        subscribeNetwork();
    }

    private void subscribeReportMsg() {
        Subscription subscribe =RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .filter(msg -> TextUtils.equals(msg.uuid, uuid))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    AppLogger.e("收到设备同步消息:" + new Gson().toJson(result));
                    try {
                        for (JFGDPMsg msg : result.dpList) {
                            if (msg.id == 222) {
                                DpMsgDefine.DPSdcardSummary sdcardSummary = null;
                                try {
                                    sdcardSummary = unpackData(msg.packValue, DpMsgDefine.DPSdcardSummary.class);
                                } catch (Exception e) {
                                    AppLogger.e(e.getMessage());
                                }
                                if (sdcardSummary != null && !sdcardSummary.hasSdcard && hasSDCard) {//SDCard 不存在
                                    mView.onReportDeviceError(2004, true);
                                } else if (sdcardSummary != null && sdcardSummary.errCode != 0 /*&& hasSDCard*/) {//SDCard 需要格式化
//                                    mView.onReportDeviceError(2022, true);
                                }
                                hasSDCard = sdcardSummary != null && sdcardSummary.hasSdcard;
                            } else if (msg.id == 204) {
                                // TODO: 2017/8/17 AP 模式下发的是204 消息,需要特殊处理
//                                Device device = DataSourceManager.getInstance().getDevice(uuid);
//                                if (JFGRules.isAPDirect(uuid, device.$(202, ""))) {
                                DpMsgDefine.DPSdStatus status = unpackData(msg.packValue, DpMsgDefine.DPSdStatus.class);
                                if (status != null && !status.hasSdcard && hasSDCard) {//SDCard 不存在
                                    mView.onReportDeviceError(2004, true);
                                } else if (status != null && status.err != 0 /*&& hasSDCard*/) {//SDCard 需要格式化
//                                    mView.onReportDeviceError(2022, true);
                                }
                                hasSDCard = status != null && status.hasSdcard;
//                                }

                            }
                        }
                    } catch (Exception e) {
                        AppLogger.e(e.getMessage());
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
        addStopSubscription(subscribe);
    }

    @Override
    public void delete(PanoramaAlbumContact.PanoramaItem item, int mode, long version) {
        if (mode == 0) {
            DownloadManager.getInstance().removeTask(PanoramaAlbumContact.PanoramaItem.getTaskKey(uuid, item.fileName));
            mView.onDeleteResult(0);
        } else if (mode == 1 || mode == 2) {
            Subscription subscribe =  BasePanoramaApiHelper.getInstance().delete(uuid, 1, 0, Collections.singletonList(item.fileName))
                    .timeout(500, TimeUnit.MILLISECONDS, Observable.just(null))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ret -> {
                        DownloadManager.getInstance().removeTask(PanoramaAlbumContact.PanoramaItem.getTaskKey(uuid, item.fileName));
                        mView.onDeleteResult(0);
                    }, e -> {
                        AppLogger.e(e.getMessage());
                    });
//                    .timeout(10, TimeUnit.SECONDS, Observable.just(null))
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(ret -> {
//                        mView.onDeleteResult(0);
//                    }, e -> {
//                        AppLogger.e(e.getMessage());
//                    });
            addStopSubscription(subscribe);
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
            addStopSubscription(subscribe);
        }
    }

    private void subscribeNetwork() {
        Subscription subscribe =RxBus.getCacheInstance().toObservable(RxEvent.NetConnectionEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    AppLogger.e("监听到网络状态发生变化");
                    BaseDeviceInformationFetcher.getInstance().init(uuid);
                    if (event.mobile != null && event.mobile.isConnected()) {
                        mView.onRefreshConnectionMode(event.mobile.getType());
                    } else if (event.wifi != null && event.wifi.isConnected()) {
                        mView.onRefreshConnectionMode(event.wifi.getType());
                    } else {
                        mView.onRefreshConnectionMode(-1);
                    }
                }, e -> {
                });
        addStopSubscription(subscribe);
    }

    @Override
    public void saveImage(CamWarnGlideURL glideURL, String fileName) {
        GlideApp.with(mView.activity())//注意contxt
                .asBitmap()
                .load(glideURL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                Log.d(TAG, "onResourceReady:" + (resource == null));
                save(resource, fileName);
            }
        })
        ;
    }

    private void save(Bitmap bitmap, String fileName) {
        Observable.just(bitmap)
                .filter(new RxHelper.Filter<>("", bitmap != null))
                .subscribeOn(Schedulers.io())
                .map((Bitmap bMap) -> {
                    String filePath = JConstant.MEDIA_PATH + File.separator;
                    BitmapUtils.saveBitmap2file(bMap, filePath + fileName);
                    MiscUtils.insertImage(filePath, fileName);
                    return true;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(r -> mView.savePicResult(r), AppLogger::e);
    }

    @Override
    public boolean isSaved(String fileName) {
        return new File(JConstant.MEDIA_PATH + File.separator + fileName).exists();
    }

}
