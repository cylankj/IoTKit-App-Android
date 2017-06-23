package com.cylan.jiafeigou.base.module;

import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.toolsfinal.io.Charsets;
import com.google.gson.Gson;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.http.RealResponseBody;
import okio.Buffer;
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
    private String uuid;

    public static BasePanoramaApiHelper getInstance() {
        return apiHelper;
    }

    @Inject
    public BasePanoramaApiHelper() {
        apiHelper = this;
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    if (deviceInformation != null && deviceInformation.ip != null) {
                        //动态 host
                        HttpUrl httpUrl = chain.request().url().newBuilder().host(deviceInformation.ip).build();
                        request = request.newBuilder().url(httpUrl).build();
                    }
                    AppLogger.e("http请求为:" + request.toString());
                    Response proceed = chain.proceed(request);
                    String string = proceed.body().string();
                    AppLogger.e("http 请求返回的结果:" + new Gson().toJson(string));
                    return proceed.newBuilder().body(new RealResponseBody(proceed.headers(), new Buffer().writeString(string, Charsets.UTF_8))).build();
                })
                .build();
        this.httpApi = new Retrofit.Builder().client(okHttpClient)
                .baseUrl("http://192.168.10.2/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(IHttpApi.class);

        RxBus.getCacheInstance().toObservable(RxEvent.FetchDeviceInformation.class)
                .observeOn(Schedulers.io())
                .subscribe(fetchEvent -> {
                    if (fetchEvent.success) {
                        deviceInformation = BaseDeviceInformationFetcher.getInstance().getDeviceInformation();
                        if (deviceInformation != null && deviceInformation.ip != null) {
                            RxBus.getCacheInstance().postSticky(RxEvent.PanoramaApiAvailable.API_HTTP);
                        } else {
                            if (BaseApplication.isOnline()) {
                                forwardHelper = BaseForwardHelper.getInstance();
                                deviceInformation = null;
                                RxBus.getCacheInstance().postSticky(RxEvent.PanoramaApiAvailable.API_FORWARD);
                            } else {
                                RxBus.getCacheInstance().postSticky(RxEvent.PanoramaApiAvailable.API_NOT_AVAILABLE);
                            }
                        }
                    } else {
                        deviceInformation = null;
                        RxBus.getCacheInstance().removeStickyEvent(RxEvent.PanoramaApiAvailable.class);//扫描开始了
                    }
                }, AppLogger::e);
    }

    public String getDeviceIp() {
        return deviceInformation == null ? null : "http://" + deviceInformation.ip;
    }

    public String getFilePath(String fileName) {
        return File.separator + BaseApplication.getAppComponent().getSourceManager().getAccount().getAccount() + File.separator + uuid + File.separator + fileName;
    }

    public Observable<RxEvent.PanoramaApiAvailable> monitorPanoramaApi() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.PanoramaApiAvailable.class);
    }

    /**
     * 使用这个类之前必须调用这个方法进行初始化,否则会出异常
     */
    public void init(String uuid) {
        this.uuid = uuid;
        RxBus.getCacheInstance().removeStickyEvent(RxEvent.PanoramaApiAvailable.class);
        BaseDeviceInformationFetcher.getInstance().init(uuid);
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
                .filter(panoramaApiAvailable -> {
                    AppLogger.d("当前使用的 API 类型为:" + panoramaApiAvailable.ApiType);
                    return panoramaApiAvailable.ApiType >= 0;
                })
                .first()
                .observeOn(Schedulers.io());
    }

    public void download(String fileName, DownloadPercent.DownloadListener listener) {
//        DownloadPercentManager.getInstance().download(deviceInformation.uuid, fileName, listener);
    }

    /**
     * @param deleteType -1:全部删除;0:反向删除,即选中的不删除;1:正向删除,即选中的删除;
     */

    public Observable<PanoramaEvent.MsgFileRsp> delete(int deleteType, List<String> files) {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.delete(deleteType, files) : forwardHelper.sendForward(uuid, 3, new PanoramaEvent.MsgFileReq(files, deleteType)));
    }

    public Observable<PanoramaEvent.MsgFileListRsp> getFileList(int beginTime, int endTime, int count) {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getFileList(beginTime, endTime, count) : forwardHelper.sendForward(uuid, 5, new PanoramaEvent.MsgFileListReq(beginTime, endTime, count)));
    }

    public Observable<PanoramaEvent.MsgFileRsp> snapShot() {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.snapShot() : forwardHelper.sendForward(uuid, 7, null));
    }

    public Observable<PanoramaEvent.MsgRsp> startRec(int videoType) {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.startRec(videoType) : forwardHelper.sendForward(uuid, 9, videoType));
    }

    public Observable<PanoramaEvent.MsgFileRsp> stopRec(int videoType) {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.stopRec(videoType) : forwardHelper.sendForward(uuid, 11, videoType));
    }

    public Observable<PanoramaEvent.MsgVideoStatusRsp> getRecStatus() {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getRecStatus() : forwardHelper.sendForward(uuid, 13, null));
    }

    public Observable<PanoramaEvent.MsgSdInfoRsp> sdFormat() {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.sdFormat().map(ret -> {
            //更新设备属性
            DpMsgDefine.DPSdStatus status = new DpMsgDefine.DPSdStatus();
            status.err = ret.sdcard_recogntion;
            status.hasSdcard = ret.sdIsExist == 1;
            status.used = ret.storage_used;
            status.total = ret.storage;
            Device device = DataSourceManager.getInstance().getDevice(uuid);
            if (device.available()) {
                DPEntity property = device.getProperty(204);
                if (property == null) {
                    property = device.getEmptyProperry(204);
                }
                property.setValue(status, DpUtils.pack(status), property.getVersion());
                device.updateProperty(204, property);
            }
            return ret;
        }) : forwardHelper.setDataPoint(uuid, 218));
    }

    public Observable<PanoramaEvent.MsgSdInfoRsp> getSdInfo() {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getSdInfo().map(ret -> {
            //更新设备属性
            DpMsgDefine.DPSdStatus status = new DpMsgDefine.DPSdStatus();
            status.err = ret.sdcard_recogntion;
            status.hasSdcard = ret.sdIsExist == 1;
            status.used = ret.storage_used;
            status.total = ret.storage;
            Device device = DataSourceManager.getInstance().getDevice(uuid);
            if (device.available()) {
                DPEntity property = device.getProperty(204);
                if (property == null) {
                    property = device.getEmptyProperry(204);
                }
                property.setValue(status, DpUtils.pack(status), property.getVersion());
                device.updateProperty(204, property);
            }
            return ret;
        }) : forwardHelper.sendDataPoint(uuid, 204));
    }

    public Observable<PanoramaEvent.MsgPowerLineRsp> getPowerLine() {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getPowerLine().map(ret -> {
            //更新设备属性
            Device device = DataSourceManager.getInstance().getDevice(uuid);
            if (device.available()) {
                DPEntity property = device.getProperty(205);
                if (property == null) {
                    property = device.getEmptyProperry(205);
                }
                property.setValue(new DpMsgDefine.DPPrimary<>(ret.powerline == 1), DpUtils.pack(ret.powerline == 1), property.getVersion());
                device.updateProperty(205, property);
            }
            return ret;
        }) : forwardHelper.sendDataPoint(uuid, 205));
    }

    public Observable<PanoramaEvent.MsgBatteryRsp> getBattery() {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getBattery().map(ret -> {
            //更新设备属性
            Device device = DataSourceManager.getInstance().getDevice(uuid);
            if (device.available()) {
                DPEntity property = device.getProperty(206);
                if (property == null) {
                    property = device.getEmptyProperry(206);
                }
                property.setValue(new DpMsgDefine.DPPrimary<>(ret.battery), DpUtils.pack(ret.battery), property.getVersion());
                device.updateProperty(206, property);
            }
            return ret;
        }) : forwardHelper.sendDataPoint(uuid, 206));
    }

    public Observable<PanoramaEvent.MsgRsp> setLogo(int logType) {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.setLogo(logType) : forwardHelper.sendForward(uuid, 15, logType));
    }

    public Observable<PanoramaEvent.MsgRsp> setResolution(int videoStandard) {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.setResolution(videoStandard) : forwardHelper.sendForward(uuid, 17, null));
    }

    public Observable<PanoramaEvent.MsgLogoRsp> getLogo() {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getLogo() : forwardHelper.sendForward(uuid, 16, null));
    }

    public Observable<PanoramaEvent.MsgResolutionRsp> getResolution() {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getResolution() : forwardHelper.sendForward(uuid, 21, null));
    }

    public Observable<PanoramaEvent.MsgUpgradeStatusRsp> getUpgradeStatus() {
        return getAvailableApi().flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getUpgradeStatus() : forwardHelper.sendDataPoint(uuid, 228));
    }
}
