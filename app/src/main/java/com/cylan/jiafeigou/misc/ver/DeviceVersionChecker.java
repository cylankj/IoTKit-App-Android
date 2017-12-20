package com.cylan.jiafeigou.misc.ver;

import android.text.TextUtils;

import com.cylan.entity.jniCall.DevUpgradeInfo;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.module.SubscriptionSupervisor;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by hds on 17-5-28.
 */

public class DeviceVersionChecker extends AbstractVersion<AbstractVersion.BinVersion> {


    @Override
    public boolean checkCondition() {
        if (portrait == null) {
            throw new IllegalArgumentException("portrait == null 报错");
        }
        //不支持固件升级
        if (!JFGRules.showFirmware(portrait.getPid(), false)) {
            return false;
        }
        //分享设备
        if (JFGRules.isShareDevice(portrait.getCid())) {
            return false;
        }
        //当前网络不行
        if (NetUtils.getJfgNetType() == 0) {
            return false;
        }
        return true;
    }

    @Override
    public void startCheck() {
        //测试要求去掉一个小时检测一次的限制
//        if (lastCheckTime == 0 || System.currentTimeMillis() - lastCheckTime > 60 * 1000) {
//            lastCheckTime = System.currentTimeMillis();
//        } else {
//            return;
//        }
        final String uuid = portrait.getCid();

        Subscription subscribe = Observable.just("go").subscribeOn(Schedulers.io())
                .timeout(5, TimeUnit.SECONDS)
                .flatMap(what -> {
                    long seq;
                    Device device = DataSourceManager.getInstance().getDevice(portrait.getCid());
                    final String currentVersion = device.$(207, "");
                    AppLogger.d("current version: " + currentVersion);
                    try {
                        seq = Command.getInstance()
                                .checkDevVersion(device.pid, device.getUuid(), currentVersion);
                        Command.getInstance().CheckTagDeviceVersion(uuid);
                    } catch (Exception e) {
                        AppLogger.e("checkNewHardWare:" + e.getLocalizedMessage());
                        seq = -1L;
                    }
                    return Observable.just(seq);
                })
                .flatMap(aLong -> RxBus.getCacheInstance().toObservable(RxEvent.VersionRsp.class)
                        .subscribeOn(Schedulers.io())
                        .filter(ret -> ret != null && TextUtils.equals(uuid, ret.getUuid())))
                .flatMap(ret -> {
                    setBinVersion(ret.getVersion());
                    BinVersion oldVersion = getVersionFrom(uuid);
                    long time = oldVersion.getLastShowTime();
                    oldVersion = ret.getVersion();
                    oldVersion.setLastShowTime(time);
                    oldVersion.setTotalSize(totalSize(oldVersion));
                    Device device = DataSourceManager.getInstance().getDevice(portrait.getCid());
                    final String newVersion = binVersion.getTagVersion();
                    final String currentVersion = device.$(207, "");
                    if (BindUtils.versionCompare(newVersion, currentVersion) > 0) {
                        PreferencesUtils.putString(JConstant.KEY_FIRMWARE_CONTENT + uuid, new Gson().toJson(oldVersion));
                        setBinVersion(oldVersion);
                        finalShow();
                    } else {
                        PreferencesUtils.putString(JConstant.KEY_FIRMWARE_CONTENT + uuid, "");
                    }
                    return Observable.just(ret.getVersion());
//                    BinVersion oldVersion = getVersionFrom(uuid);
//                    long time = oldVersion.getLastShowTime();
//                    oldVersion = ret.getVersion();
//                    oldVersion.setLastShowTime(time);
//                    oldVersion.setTotalSize(totalSize(oldVersion));
//                    PreferencesUtils.putString(JConstant.KEY_FIRMWARE_CONTENT + uuid, new Gson().toJson(oldVersion));
//                    setBinVersion(oldVersion);
//                    finalShow();
//                    return Observable.just(ret.getVersion());
                })
                .subscribe(ret -> {
                }, AppLogger::e);
        SubscriptionSupervisor.subscribe(this, SubscriptionSupervisor.CATEGORY_DEFAULT, "DeviceVersionChecker.startCheck", subscribe);
    }

    @Override
    public void finalShow() {
        if (showCondition == null || showCondition.show()) {
            //弹框的时间,从弹出算起
            long time = binVersion.getLastShowTime();
            ArrayList<DevUpgradeInfo> list = binVersion.getList();
            final String tagVersion = binVersion.getTagVersion();
            boolean shouldShow = list != null && list.size() > 0 && !TextUtils.isEmpty(tagVersion)
                    && (time == 0 || System.currentTimeMillis() - time > 24 * 3600 * 1000);
            if (shouldShow) {
                RxBus.getCacheInstance().post(binVersion);
                AppLogger.d("检查到有新固件:" + binVersion);
            }
        }
    }
}
