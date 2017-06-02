package com.cylan.jiafeigou.misc.ver;

import android.text.TextUtils;

import com.cylan.entity.jniCall.DevUpgradleInfo;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
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

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by hds on 17-5-28.
 */

public class PanDeviceVersionChecker extends AbstractVersion<PanDeviceVersionChecker.PanVersion> {

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
    public Observable<PanDeviceVersionChecker.PanVersion> startCheck() {
        if (lastCheckTime == 0 || System.currentTimeMillis() - lastCheckTime > 2 * 10 * 1000) {
            lastCheckTime = System.currentTimeMillis();
        } else return Observable.just(PanVersion.NULL);
        if (!checkCondition()) return Observable.just(PanVersion.NULL);
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
                .flatMap(aLong -> RxBus.getCacheInstance().toObservable(PanVersion.class)
                        .subscribeOn(Schedulers.newThread())
                        .filter(ret -> ret != null && TextUtils.equals(uuid, ret.cid)))
                .filter(PanVersion::showVersion)//有新版本
                .flatMap(ret -> {
                    PanVersion oldVersion = getVersionFrom(uuid);
                    ret.setLastShowTime(oldVersion.getLastShowTime());
                    PreferencesUtils.putString(JConstant.KEY_FIRMWARE_CONTENT + uuid, new Gson().toJson(ret));
                    RxBus.getCacheInstance().post(new RxEvent.VersionRsp<>().setVersion(ret)
                            .setUuid(uuid));
                    AppLogger.d("检查到有新固件:" + ret);
                    return Observable.just(ret);
                });
    }

    private PanVersion getVersionFrom(String uuid) {
        final String content = PreferencesUtils.getString(JConstant.KEY_FIRMWARE_CONTENT + uuid);
        if (TextUtils.isEmpty(content)) return PanVersion.NULL;
        try {
            return new Gson().fromJson(content, PanVersion.class);
        } catch (Exception e) {
            return PanVersion.NULL;
        }
    }

    public static class PanVersion extends IVersion.BaseVersion {

        public static PanVersion NULL = new PanVersion();
        private ArrayList<DevUpgradleInfo> list;
        private String cid;
        private String tagVersion;
        private String content;

        public boolean showVersion() {
            Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(cid);
            DpMsgDefine.DPNet dpNet = device.$(201, new DpMsgDefine.DPNet());
            //设备离线就不需要弹出来
            if (!JFGRules.isDeviceOnline(dpNet)) {
                return false;
            }
            //局域网弹出
            if (!MiscUtils.isDeviceInWLAN(cid)) return false;
            //弹框的时间,从弹出算起
            long time = getLastShowTime();
            return list != null && list.size() > 0 && !TextUtils.isEmpty(tagVersion)
                    && (time == 0 || System.currentTimeMillis() - time > 24 * 3600 * 1000);
        }

        public void setList(ArrayList<DevUpgradleInfo> list) {
            this.list = list;
        }

        public void setCid(String cid) {
            this.cid = cid;
        }

        public void setTagVersion(String tagVersion) {
            this.tagVersion = tagVersion;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public ArrayList<DevUpgradleInfo> getList() {
            return list;
        }

        public String getCid() {
            return cid;
        }

        public String getTagVersion() {
            return tagVersion;
        }

        public String getContent() {
            return content;
        }

        @Override
        public String toString() {
            return "PanVersion{" +
                    "list=" + list +
                    ", cid='" + cid + '\'' +
                    ", tagVersion='" + tagVersion + '\'' +
                    ", content='" + content + '\'' +
                    '}';
        }
    }
}
