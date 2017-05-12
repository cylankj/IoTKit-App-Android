package com.cylan.jiafeigou.base.module;

import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.toolsfinal.io.Charsets;
import com.google.gson.Gson;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

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
    private DeviceInformation deviceInformation;
    private static BasePanoramaApiHelper apiHelper;

    public static BasePanoramaApiHelper getInstance() {
        return apiHelper;
    }

    @Inject
    public BasePanoramaApiHelper() {
        apiHelper = this;
        RxBus.getCacheInstance().toObservable(DeviceInformation.class)
                .observeOn(Schedulers.io())
                .retry()
                .subscribe(info -> {
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .addInterceptor(chain -> {
                                Request request = chain.request();
                                Response proceed = chain.proceed(request);
                                String string = proceed.body().string();
                                Response build = proceed.newBuilder().body(new RealResponseBody(proceed.headers(), new Buffer().writeString(string, Charsets.UTF_8))).build();
                                AppLogger.e("http 请求返回的结果:" + new Gson().toJson(string));
                                return build;
                            })
                            .build();
                    Retrofit retrofit = new Retrofit.Builder().client(okHttpClient)
                            .baseUrl("http://" + info.ip)
                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create())
                            .addConverterFactory(ImageFileConverterFactory.create())
                            .build();
                    deviceInformation = BaseDeviceInformationFetcher.getInstance().getDeviceInformation();
                    httpApi = retrofit.create(IHttpApi.class);
                    RxBus.getCacheInstance().postSticky(new RxEvent.HttpApiArrived(httpApi));
                }, AppLogger::e);

        RxBus.getCacheInstance().toObservable(RxEvent.FetchDeviceInformation.class)
                .filter(ret -> !ret.success)
                .observeOn(Schedulers.io())
                .subscribe(ret -> {
                    httpApi = null;
                    deviceInformation = null;
                    RxBus.getCacheInstance().removeStickyEvent(RxEvent.HttpApiArrived.class);
                }, AppLogger::e);
    }

    public String getDeviceIp() {
        return deviceInformation == null ? null : "http://" + deviceInformation.ip;
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

    public Observable<IHttpApi> getHttpApi() {
        return BaseDeviceInformationFetcher.getInstance().fetchDeviceInformation()
                .flatMap(ret -> RxBus.getCacheInstance().toObservableSticky(RxEvent.HttpApiArrived.class).first())
                .timeout(5, TimeUnit.SECONDS)
                .map(ret -> httpApi)
                .observeOn(Schedulers.io());
    }

    public Observable<BaseForwardHelper> getForwardHelper() {
        return BaseForwardHelper.getInstance().getApi();
    }


    public Observable<File> download(String fileName) {
        return null;
    }


    public Observable<File> getThumbPicture(String thumb) {
        return httpApi == null && BaseApplication.isOnline() ?
                getForwardHelper().flatMap(BaseForwardHelper::empty) : getHttpApi().flatMap(api -> api.getThumbPicture(thumb));
    }

    /**
     * @param deleteType -1:全部删除;0:反向删除,即选中的不删除;1:正向删除,即选中的删除;
     */

    public Observable<PanoramaEvent.MsgFileRsp> delete(int deleteType, List<String> files) {
        return httpApi == null && BaseApplication.isOnline() ?
                getForwardHelper().flatMap(forward -> forward.sendForward(3, new PanoramaEvent.MsgFileReq(files, deleteType))) :
                getHttpApi().flatMap(api -> api.delete(deleteType, files));
    }

    public Observable<PanoramaEvent.MsgFileListRsp> getFileList(int beginTime, int endTime, int count) {
        return httpApi == null ?
                getForwardHelper().flatMap(forward -> forward.sendForward(5, new PanoramaEvent.MsgFileListReq(beginTime, endTime, count))) :
                getHttpApi().flatMap(api -> api.getFileList(beginTime, endTime, count));
    }

    public Observable<PanoramaEvent.MsgFileRsp> snapShot() {
        return httpApi == null && BaseApplication.isOnline() ?
                getForwardHelper().flatMap(forward -> forward.sendForward(7, null)) :
                getHttpApi().flatMap(IHttpApi::snapShot);
    }

    public Observable<PanoramaEvent.MsgRsp> startRec(int videoType) {
        return httpApi == null && BaseApplication.isOnline() ?
                getForwardHelper().flatMap(forward -> forward.sendForward(9, new PanoramaEvent.MsgReq(videoType))) :
                getHttpApi().flatMap(api -> api.startRec(videoType));
    }

    public Observable<PanoramaEvent.MsgFileRsp> stopRec(int videoType) {
        return httpApi == null && BaseApplication.isOnline() ?
                getForwardHelper().flatMap(forward -> forward.sendForward(11, new PanoramaEvent.MsgReq(videoType))) :
                getHttpApi().flatMap(api -> api.stopRec(videoType));
    }

    public Observable<PanoramaEvent.MsgVideoStatusRsp> getRecStatus() {
        return httpApi == null && BaseApplication.isOnline() ?
                getForwardHelper().flatMap(forward -> forward.sendForward(13, null)) :
                getHttpApi().flatMap(IHttpApi::getRecStatus);
    }

    public Observable<PanoramaEvent.MsgSdInfoRsp> sdFormat() {
        return httpApi == null && BaseApplication.isOnline() ?
                getForwardHelper().flatMap(BaseForwardHelper::empty) :
                getHttpApi().flatMap(api -> sdFormat());
    }

    public Observable<PanoramaEvent.MsgSdInfoRsp> getSdInfo() {
        return httpApi == null && BaseApplication.isOnline() ?
                getForwardHelper().flatMap(forward -> forward.sendDataPoint(204)) :
                getHttpApi().flatMap(IHttpApi::getSdInfo);
    }

    public Observable<PanoramaEvent.MsgPowerLineRsp> getPowerLine() {
        return httpApi == null && BaseApplication.isOnline() ?
                getForwardHelper().flatMap(BaseForwardHelper::empty) :
                getHttpApi().flatMap(IHttpApi::getPowerLine);
    }

    public Observable<PanoramaEvent.MsgBatteryRsp> getBattery() {
        return httpApi == null && BaseApplication.isOnline() ?
                getForwardHelper().flatMap(forward -> forward.sendDataPoint(206)) :
                getHttpApi().flatMap(IHttpApi::getBattery);
    }

    public Observable<PanoramaEvent.MsgRsp> setLogo(int logType) {
        return httpApi == null && BaseApplication.isOnline() ?
                getForwardHelper().flatMap(forward -> forward.sendForward(15, new PanoramaEvent.MsgReq(logType))) :
                getHttpApi().flatMap(api -> api.setLogo(logType));
    }

    public Observable<PanoramaEvent.MsgRsp> setResolution(int videoStandard) {
        return httpApi == null && BaseApplication.isOnline() ?
                getForwardHelper().flatMap(forward -> forward.sendForward(17, null)) :
                getHttpApi().flatMap(api -> api.setResolution(videoStandard));
    }

    public Observable<PanoramaEvent.MsgLogoRsp> getLogo() {
        return httpApi == null && BaseApplication.isOnline() ?
                getForwardHelper().flatMap(forward -> forward.sendForward(16, null)) :
                getHttpApi().flatMap(IHttpApi::getLogo);
    }

    public Observable<PanoramaEvent.MsgResolutionRsp> getResolution() {
        return httpApi == null && BaseApplication.isOnline() ?
                getForwardHelper().flatMap(forward -> forward.sendForward(21, null)) :
                getHttpApi().flatMap(IHttpApi::getResolution);
    }
}
