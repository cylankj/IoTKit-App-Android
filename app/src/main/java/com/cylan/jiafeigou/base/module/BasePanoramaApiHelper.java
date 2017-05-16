package com.cylan.jiafeigou.base.module;

import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/5/8.
 */

@Singleton
public class BasePanoramaApiHelper {
    private IHttpApi httpApi;
    private BaseForwardHelper forwardHelper;
    private DeviceInformation deviceInformation;
    private static BasePanoramaApiHelper apiHelper;

    public static BasePanoramaApiHelper getInstance() {
        return apiHelper;
    }

    @Inject
    public BasePanoramaApiHelper() {
        apiHelper = this;
        RxBus.getCacheInstance().toObservable(RxEvent.FetchDeviceInformation.class)
                .observeOn(Schedulers.io())
                .subscribe(fetchEvent -> {
                    if (fetchEvent.success) {
                        deviceInformation = BaseDeviceInformationFetcher.getInstance().getDeviceInformation();
                        if (deviceInformation != null && deviceInformation.ip != null) {
                            Retrofit retrofit = new Retrofit.Builder().client(BaseApplication.getAppComponent().getOkHttpClient())
                                    .baseUrl("http://" + deviceInformation.ip)
                                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build();
                            httpApi = retrofit.create(IHttpApi.class);
                            RxBus.getCacheInstance().postSticky(RxEvent.PanoramaApiAvailable.API_HTTP);
                        } else {
                            forwardHelper = BaseForwardHelper.getInstance();
                            deviceInformation = null;
                            RxBus.getCacheInstance().postSticky(RxEvent.PanoramaApiAvailable.API_FORWARD);
                        }
                    } else {
                        httpApi = null;
                        deviceInformation = null;
                        RxBus.getCacheInstance().removeStickyEvent(RxEvent.PanoramaApiAvailable.class);
                    }
                }, AppLogger::e);
    }

    public String getDeviceIp() {
        return deviceInformation == null ? null : "http://" + deviceInformation.ip;
    }

    public Observable<RxEvent.PanoramaApiAvailable> monitorPanoramaApi() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.PanoramaApiAvailable.class);
    }

    /**
     * 使用这个类之前必须调用这个方法进行初始化,否则会出异常
     */
    public void init(String uuid) {
        BaseDeviceInformationFetcher.getInstance().init(uuid);
        BaseForwardHelper.getInstance().init(uuid);
    }

    public Observable<String> loadPicture(String origin) {
        return Observable.create((Observable.OnSubscribe<String>) subscriber -> Glide.with(BaseApplication.getAppComponent().getAppContext())
                .load("http://" + (deviceInformation == null ? null : deviceInformation.ip) + "/images/" + origin)
                .downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                        subscriber.onNext(resource.getAbsolutePath());
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        subscriber.onError(e);
                    }
                }))
                .subscribeOn(AndroidSchedulers.mainThread());
    }

    private Observable<RxEvent.PanoramaApiAvailable> getAvailableApi() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.PanoramaApiAvailable.class)
                .first()
                .map(panoramaApiAvailable -> {
                    AppLogger.d("当前使用的 API 类型为:" + panoramaApiAvailable.ApiType);
                    return panoramaApiAvailable;
                })
                .observeOn(Schedulers.io());
    }

    public void download(String fileName, DownloadPercent.DownloadListener listener) {
//        DownloadPercentManager.getInstance().download(deviceInformation.uuid, fileName, listener);
    }

    /**
     * @param deleteType -1:全部删除;0:反向删除,即选中的不删除;1:正向删除,即选中的删除;
     */

    public Observable<PanoramaEvent.MsgFileRsp> delete(int deleteType, List<String> files) {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.delete(deleteType, files) : forwardHelper.sendForward(3, new PanoramaEvent.MsgFileReq(files, deleteType)));
    }

    public Observable<PanoramaEvent.MsgFileListRsp> getFileList(int beginTime, int endTime, int count) {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getFileList(beginTime, endTime, count) : forwardHelper.sendForward(5, new PanoramaEvent.MsgFileListReq(beginTime, endTime, count)));
    }

    public Observable<PanoramaEvent.MsgFileRsp> snapShot() {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.snapShot() : forwardHelper.sendForward(7, null));
    }

    public Observable<PanoramaEvent.MsgRsp> startRec(int videoType) {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.startRec(videoType) : forwardHelper.sendForward(9, videoType));
    }

    public Observable<PanoramaEvent.MsgFileRsp> stopRec(int videoType) {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.stopRec(videoType) : forwardHelper.sendForward(11, videoType));
    }

    public Observable<PanoramaEvent.MsgVideoStatusRsp> getRecStatus() {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getRecStatus() : forwardHelper.sendForward(13, null));
    }

    public Observable<PanoramaEvent.MsgSdInfoRsp> sdFormat() {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.sdFormat() : forwardHelper.empty());
    }

    public Observable<PanoramaEvent.MsgSdInfoRsp> getSdInfo() {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getSdInfo() : forwardHelper.sendDataPoint(204));
    }

    public Observable<PanoramaEvent.MsgPowerLineRsp> getPowerLine() {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getPowerLine() : forwardHelper.empty());
    }

    public Observable<PanoramaEvent.MsgBatteryRsp> getBattery() {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getBattery() : forwardHelper.sendDataPoint(206));
    }

    public Observable<PanoramaEvent.MsgRsp> setLogo(int logType) {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.setLogo(logType) : forwardHelper.sendForward(15, logType));
    }

    public Observable<PanoramaEvent.MsgRsp> setResolution(int videoStandard) {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.setResolution(videoStandard) : forwardHelper.sendForward(17, null));
    }

    public Observable<PanoramaEvent.MsgLogoRsp> getLogo() {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getLogo() : forwardHelper.sendForward(16, null));
    }

    public Observable<PanoramaEvent.MsgResolutionRsp> getResolution() {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getResolution() : forwardHelper.sendForward(21, null));
    }
}
