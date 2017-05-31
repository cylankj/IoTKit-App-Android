package com.cylan.jiafeigou.misc.ver;

import android.text.TextUtils;

import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by hds on 17-5-28.
 */

public class DeviceVersionChecker extends AbstractVersion<AbstractVersion.AVersion> {


    @Override
    public boolean checkCondition() {
        if (portrait == null) throw new IllegalArgumentException("portrait == null 报错");
        //不支持固件升级
        if (!JFGRules.showFirmware(portrait.getPid())) return false;
        //分享设备
        if (JFGRules.isShareDevice(portrait.getCid())) return false;
        //当前网络不行
        if (NetUtils.getJfgNetType() == 0) return false;
        return true;
    }

    @Override
    public Observable<AVersion> startCheck() {
        final String uuid = portrait.getCid();
        Observable.just("go")
                .subscribeOn(Schedulers.newThread())
                .timeout(5, TimeUnit.SECONDS)
                .flatMap(what -> {
                    long seq;
                    Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(portrait.getCid());
                    final String currentVersion = device.$(207, "");
                    AppLogger.d("current version: " + currentVersion);
                    try {
                        seq = BaseApplication.getAppComponent().getCmd().checkDevVersion(portrait.getPid(), portrait.getCid(),
                                currentVersion);
                    } catch (Exception e) {
                        AppLogger.e("checkNewHardWare:" + e.getLocalizedMessage());
                        seq = -1L;
                    }
                    return Observable.just(seq);
                })
                .flatMap(aLong -> RxBus.getCacheInstance().toObservable(RxEvent.CheckVersionRsp.class)
                        .subscribeOn(Schedulers.newThread())
                        .filter(ret -> ret != null && TextUtils.equals(uuid, ret.uuid))
                        .filter(ret -> {
                            if (!ret.hasNew) {
                                PreferencesUtils.remove(JConstant.KEY_FIRMWARE_CONTENT + uuid);
                            }
                            return ret.hasNew;
                        }))
                .map(ret -> {
                    try {
                        Request request = new Request.Builder()
                                .url(ret.url)
                                .build();
                        Response response = new OkHttpClient().newCall(request).execute();
                        ret.fileSize = response.body().contentLength();
                        ret.fileDir = JConstant.ROOT_DIR;
                        ret.hasNew = true;
                        ret.fileName = "." + uuid + MiscUtils.getFileNameWithoutExn(ret.url);
                        ret.uuid = uuid;
                        ret.preKey = JConstant.KEY_FIRMWARE_CONTENT + uuid;
                        PreferencesUtils.putString(JConstant.KEY_FIRMWARE_CONTENT + uuid, new Gson().toJson(ret));
                        RxBus.getCacheInstance().post(new RxEvent.FirmwareUpdateRsp(uuid));
                        AppLogger.d("检查到有新固件:" + uuid);
                        return ret;
                    } catch (IOException e) {
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                }, throwable -> {
                    if (throwable instanceof TimeoutException) {
                    }
                });
        return null;
    }
}
