package com.cylan.jiafeigou.base.module;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JFGRules;
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
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.dp.DpUtils.unpackData;

/**
 * Created by yanzhendong on 2017/5/8.
 */

@Singleton
public class BasePanoramaApiHelper {
    private IHttpApi httpApi;
    private static BasePanoramaApiHelper apiHelper;

    public static BasePanoramaApiHelper getInstance() {
        return apiHelper;
    }

    @Inject
    public BasePanoramaApiHelper() {
        apiHelper = this;
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    DeviceInformation deviceInformation = BaseDeviceInformationFetcher.getInstance().getDeviceInformation();
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
    }

    public String getDeviceIp() {
        DeviceInformation deviceInformation = BaseDeviceInformationFetcher.getInstance().getDeviceInformation();
        return deviceInformation == null || deviceInformation.ip == null ? null : "http://" + deviceInformation.ip;
    }

    public String getFilePath(String uuid, String fileName) {
        return File.separator + BaseApplication.getAppComponent().getSourceManager().getAccount().getAccount() + File.separator + uuid + File.separator + fileName;
    }

    private Observable<RxEvent.PanoramaApiAvailable> getAvailableApi(String uuid) {
        if (!JFGRules.isPan720(DataSourceManager.getInstance().getDevice(uuid).pid)) {
            return Observable.just(RxEvent.PanoramaApiAvailable.API_FORWARD);
        } else {
            return RxBus.getCacheInstance().toObservableSticky(RxEvent.FetchDeviceInformation.class)
                    .first(fetchDeviceInformation -> fetchDeviceInformation.success)
                    .map(ret -> {
                        DeviceInformation deviceInformation = BaseDeviceInformationFetcher.getInstance().getDeviceInformation();
                        if (deviceInformation != null && !TextUtils.isEmpty(deviceInformation.uuid) &&
                                TextUtils.equals(deviceInformation.uuid, uuid) && !TextUtils.isEmpty(deviceInformation.ip)) {
                            return RxEvent.PanoramaApiAvailable.API_HTTP;
                        }
                        return RxEvent.PanoramaApiAvailable.API_FORWARD;
                    })
                    .timeout(5, TimeUnit.SECONDS, Observable.just(RxEvent.PanoramaApiAvailable.API_FORWARD))
                    .observeOn(Schedulers.io());
        }
    }

    public void download(String fileName, DownloadPercent.DownloadListener listener) {
//        DownloadPercentManager.getInstance().download(deviceInformation.uuid, fileName, listener);
    }

    /**
     * @param deleteType -1:全部删除;0:反向删除,即选中的不删除;1:正向删除,即选中的删除;
     */

    public Observable<PanoramaEvent.MsgFileRsp> delete(String uuid, int deleteType, int enwarn, List<String> files) {
        return getAvailableApi(uuid).flatMap(apiType -> apiType.ApiType == 0 ? httpApi.delete(deleteType, enwarn, files) : BaseForwardHelper.getInstance().sendForward(uuid, 3, new PanoramaEvent.MsgFileReq(files, deleteType, enwarn)));
    }

    public Observable<PanoramaEvent.MsgFileListRsp> getFileList(String uuid, int beginTime, int endTime, int count) {
//        return getAvailableApi(uuid).flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getFileList(beginTime, endTime, count) : BaseForwardHelper.getInstance().sendForward(uuid, 5, new PanoramaEvent.MsgFileListReq(beginTime, endTime, count)));
        return getAvailableApi(uuid).flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getFileList(beginTime, endTime, count) : Observable.empty());
    }

    public Observable<PanoramaEvent.MsgFileRsp> snapShot(String uuid) {
        return getAvailableApi(uuid).flatMap(apiType -> apiType.ApiType == 0 ? httpApi.snapShot() : BaseForwardHelper.getInstance().sendForward(uuid, 7, null));
    }

    public Observable<PanoramaEvent.MsgRsp> startRec(String uuid, int videoType) {
        return getAvailableApi(uuid).flatMap(apiType -> apiType.ApiType == 0 ? httpApi.startRec(videoType) : BaseForwardHelper.getInstance().sendForward(uuid, 9, videoType));
    }

    public Observable<PanoramaEvent.MsgFileRsp> stopRec(String uuid, int videoType) {
        return getAvailableApi(uuid).flatMap(apiType -> apiType.ApiType == 0 ? httpApi.stopRec(videoType) : BaseForwardHelper.getInstance().sendForward(uuid, 11, videoType));
    }

    public Observable<PanoramaEvent.MsgVideoStatusRsp> getRecStatus(String uuid) {
        return getAvailableApi(uuid).flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getRecStatus() : BaseForwardHelper.getInstance().sendForward(uuid, 13, null));
    }

    public Observable<PanoramaEvent.MsgSdInfoRsp> sdFormat(String uuid) {
        return getAvailableApi(uuid).flatMap(apiType -> apiType.ApiType == 0 ? httpApi.sdFormat().map(ret -> {
                    //更新设备属性
                    DpMsgDefine.DPSdStatus status = new DpMsgDefine.DPSdStatus();
                    status.err = ret.sdcard_recogntion;
                    status.hasSdcard = ret.sdIsExist;
                    status.used = ret.storage_used;
                    status.total = ret.storage;
                    Device device = DataSourceManager.getInstance().getDevice(uuid);
                    if (device.available()) {
                        DPEntity property = device.getProperty(204);
                        if (property == null) {
                            property = device.getEmptyProperty(204);
                        }
                        property.setValue(status, DpUtils.pack(status), property.getVersion());
                        device.updateProperty(204, property);
                    }
                    return ret;
                }) : BaseForwardHelper.getInstance().setDataPoint(uuid, 218, 0)
                        .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                                .filter(rsp -> TextUtils.equals(rsp.uuid, uuid))
                                .first(robotoSyncData -> {
                                    if (robotoSyncData.dpList != null) {
                                        for (JFGDPMsg msg : robotoSyncData.dpList) {
                                            if (msg.id == 203) {
                                                AppLogger.d("收到了203 消息回复");
                                                return true;
                                            }
                                        }
                                    }
                                    return false;
                                })
                                .map(robotoSyncData -> {
                                    try {
                                        for (JFGDPMsg msg : robotoSyncData.dpList) {
                                            if (msg.id == 203) {
                                                DpMsgDefine.DPSdStatus status = null;
                                                try {
                                                    status = unpackData(msg.packValue, DpMsgDefine.DPSdStatus.class);
                                                } catch (Exception e) {
                                                    DpMsgDefine.DPSdStatusInt statusInt = unpackData(msg.packValue, DpMsgDefine.DPSdStatusInt.class);
                                                    status = new DpMsgDefine.DPSdStatus();
                                                    status.total = statusInt.total;
                                                    status.used = statusInt.used;
                                                    status.err = statusInt.err;
                                                    status.hasSdcard = statusInt.hasSdcard == 1;
                                                }
                                                PanoramaEvent.MsgSdInfoRsp sdInfoRsp = new PanoramaEvent.MsgSdInfoRsp();
                                                sdInfoRsp.sdIsExist = status.hasSdcard;
                                                sdInfoRsp.sdcard_recogntion = status.err;
                                                sdInfoRsp.storage = status.total;
                                                sdInfoRsp.storage_used = status.used;
                                                AppLogger.d("rsp:" + new Gson().toJson(sdInfoRsp));
                                                return sdInfoRsp;
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                })
                        )
        );
    }

    public Observable<PanoramaEvent.MsgSdInfoRsp> getSdInfo(String uuid) {
        return getAvailableApi(uuid).flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getSdInfo().map(ret -> {
            //更新设备属性
            DpMsgDefine.DPSdStatus status = new DpMsgDefine.DPSdStatus();
            status.err = ret.sdcard_recogntion;
            status.hasSdcard = ret.sdIsExist;
            status.used = ret.storage_used;
            status.total = ret.storage;
            Device device = DataSourceManager.getInstance().getDevice(uuid);
            if (device.available()) {
                DPEntity property = device.getProperty(204);
                if (property == null) {
                    property = device.getEmptyProperty(204);
                }
                property.setValue(status, DpUtils.pack(status), property.getVersion());
                device.updateProperty(204, property);
            }
            return ret;
        }) : BaseForwardHelper.getInstance().sendDataPoint(uuid, 204, 1));
    }

    public Observable<PanoramaEvent.MsgPowerLineRsp> getPowerLine(String uuid) {
        return getAvailableApi(uuid).flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getPowerLine().map(ret -> {
            //更新设备属性
            Device device = DataSourceManager.getInstance().getDevice(uuid);
            if (device.available()) {
                DPEntity property = device.getProperty(205);
                if (property == null) {
                    property = device.getEmptyProperty(205);
                }
                property.setValue(new DpMsgDefine.DPPrimary<>(ret.powerline == 1), DpUtils.pack(ret.powerline == 1), property.getVersion());
                device.updateProperty(205, property);
            }
            return ret;
        }) : BaseForwardHelper.getInstance().sendDataPoint(uuid, 205, 1));
    }

    public Observable<PanoramaEvent.MsgBatteryRsp> getBattery(String uuid) {
        return getAvailableApi(uuid).flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getBattery().map(ret -> {
            //更新设备属性
            Device device = DataSourceManager.getInstance().getDevice(uuid);
            if (device.available()) {
                DPEntity property = device.getProperty(206);
                if (property == null) {
                    property = device.getEmptyProperty(206);
                }
                property.setValue(new DpMsgDefine.DPPrimary<>(ret.battery), DpUtils.pack(ret.battery), property.getVersion());
                device.updateProperty(206, property);
            }
            return ret;
        }) : BaseForwardHelper.getInstance().sendDataPoint(uuid, 206, 1));
    }

    public Observable<PanoramaEvent.MsgRsp> setLogo(String uuid, int logType) {
        return getAvailableApi(uuid).flatMap(apiType -> apiType.ApiType == 0 ? httpApi.setLogo(logType) : BaseForwardHelper.getInstance().sendForward(uuid, 15, logType));
    }

    public Observable<PanoramaEvent.MsgRsp> setResolution(String uuid, int videoStandard) {
        return getAvailableApi(uuid).flatMap(apiType -> apiType.ApiType == 0 ? httpApi.setResolution(videoStandard) : BaseForwardHelper.getInstance().sendForward(uuid, 17, null));
    }

    public Observable<PanoramaEvent.MsgLogoRsp> getLogo(String uuid) {
        return getAvailableApi(uuid).flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getLogo() : BaseForwardHelper.getInstance().sendForward(uuid, 16, null));
    }

    public Observable<PanoramaEvent.MsgResolutionRsp> getResolution(String uuid) {
        return getAvailableApi(uuid).flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getResolution() : BaseForwardHelper.getInstance().sendForward(uuid, 21, null));
    }

    public Observable<PanoramaEvent.MsgUpgradeStatusRsp> getUpgradeStatus(String uuid) {
        return getAvailableApi(uuid).flatMap(apiType -> apiType.ApiType == 0 ? httpApi.getUpgradeStatus() : BaseForwardHelper.getInstance().sendDataPoint(uuid, 228, 1));
    }
}
