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

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by hds on 17-5-28.
 */

public class PanDeviceVersionChecker extends AbstractVersion<AbstractVersion.BinVersion> {

    private static long lastCheckTime = 0;

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
    public Observable<BinVersion> startCheck() {
        if (lastCheckTime == 0 || System.currentTimeMillis() - lastCheckTime > 2 * 10 * 1000) {
            lastCheckTime = System.currentTimeMillis();
        } else return Observable.just(BinVersion.NULL);
        if (!checkCondition()) return Observable.just(BinVersion.NULL);
        final String uuid = portrait.getCid();
        return Observable.just("go").subscribeOn(Schedulers.newThread())
                .timeout(5, TimeUnit.SECONDS)
                .flatMap(what -> {
                    long seq;
                    Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(portrait.getCid());
                    final String currentVersion = device.$(207, "");
                    AppLogger.d("current version: " + currentVersion);
                    try {
                        seq = BaseApplication.getAppComponent().getCmd()
                                .CheckTagDeviceVersion(portrait.getCid());
                    } catch (Exception e) {
                        AppLogger.e("checkNewHardWare:" + e.getLocalizedMessage());
                        seq = -1L;
                    }
                    return Observable.just(seq);
                })
                .flatMap(aLong -> RxBus.getCacheInstance().toObservable(RxEvent.VersionRsp.class)
                        .subscribeOn(Schedulers.newThread())
                        .filter(ret -> ret != null && TextUtils.equals(uuid, ret.getUuid())))
                .filter(ret -> ret.getVersion().showVersion())//有新版本
                .flatMap(ret -> {
                    BinVersion oldVersion = getVersionFrom(uuid);
                    long time = oldVersion.getLastShowTime();
                    oldVersion = ret.getVersion();
                    oldVersion.setLastShowTime(time);
                    oldVersion.setTotalSize(totalSize(oldVersion));
                    PreferencesUtils.putString(JConstant.KEY_FIRMWARE_CONTENT + uuid, new Gson().toJson(oldVersion));
                    RxBus.getCacheInstance().post(oldVersion);
                    AppLogger.d("检查到有新固件:" + oldVersion);
                    return Observable.just(ret.getVersion());
                });
    }

    private long totalSize(BinVersion version) {
        if (version == null || version.getList() == null) return 0;
        int count = version.getList().size();
        long size = 0;
        for (int i = 0; i < count; i++) {
            size += MiscUtils.getFileSizeFromUrl(version.getList().get(i).url);
        }
        return size;
    }

    private BinVersion getVersionFrom(String uuid) {
        final String content = PreferencesUtils.getString(JConstant.KEY_FIRMWARE_CONTENT + uuid);
        if (TextUtils.isEmpty(content)) return BinVersion.NULL;
        try {
            return new Gson().fromJson(content, BinVersion.class);
        } catch (Exception e) {
            return BinVersion.NULL;
        }
    }


}
