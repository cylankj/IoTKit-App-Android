package com.cylan.jiafeigou.n.mvp.model;

import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AESUtil;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by hds on 17-6-6.
 */

public class LocalWifiInfo {
    private String ssid;
    private String pwd;

    public String getSsid() {
        return ssid;
    }

    public String getPwd() {
        return pwd;
    }

    public LocalWifiInfo setSsid(String ssid) {
        this.ssid = ssid;
        return this;
    }

    public LocalWifiInfo setPwd(String pwd) {
        this.pwd = pwd;
        return this;
    }

    @Override
    public String toString() {
        return "LocalWifiInfo{" +
                "ssid='" + ssid + '\'' +
                ", pwd='" + pwd + '\'' +
                '}';
    }

    public static final class Saver {
        private static final String secretKey = "wth";
        private static Saver saver;

        public static Saver getSaver() {
            if (saver == null) {
                saver = new Saver();
            }
            return saver;
        }

        private Map<String, LocalWifiInfo> wifiInfoMap;

        public Map<String, LocalWifiInfo> getWifiInfoMap() {
            return wifiInfoMap;
        }

        private Saver() {
        }

        public void addOrUpdateInfo(LocalWifiInfo localWifiInfo) {
            if (wifiInfoMap == null) {
                wifiInfoMap = new HashMap<>();
            }
            wifiInfoMap.put(localWifiInfo.getSsid(), localWifiInfo);
            Observable.just("save")
                    .subscribeOn(Schedulers.io())
                    .subscribe(ret -> {
                        try {
                            PreferencesUtils.putString(secretKey, AESUtil.encrypt(new Gson().toJson(saver)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, AppLogger::e);
        }

        public Observable<LocalWifiInfo> getInfo(String ssid) {
            return Observable.just(ssid)
                    .subscribeOn(Schedulers.io())
                    .flatMap(s -> {
                        if (wifiInfoMap != null) {
                            return Observable.just(wifiInfoMap.get(s));
                        }
                        final String content = PreferencesUtils.getString(secretKey);
                        try {
                            final String de = AESUtil.decrypt(content);
                            Saver saver = new Gson().fromJson(de, Saver.class);
                            wifiInfoMap = saver.getWifiInfoMap();
                            return Observable.just(wifiInfoMap.get(s));
                        } catch (Exception e) {
                            PreferencesUtils.remove(secretKey);
                            return Observable.just(null);
                        }
                    });
        }

        public Observable<Map<String, LocalWifiInfo>> getMap() {
            return Observable.just(null)
                    .subscribeOn(Schedulers.io())
                    .flatMap(s -> {
                        if (wifiInfoMap != null) {
                            return Observable.just(wifiInfoMap);
                        }
                        final String content = PreferencesUtils.getString(secretKey);
                        try {
                            final String de = AESUtil.decrypt(content);
                            Saver saver = new Gson().fromJson(de, Saver.class);
                            wifiInfoMap = saver.getWifiInfoMap();
                            return Observable.just(wifiInfoMap);
                        } catch (Exception e) {
                            PreferencesUtils.remove(secretKey);
                            return Observable.just(null);
                        }
                    });
        }

        @Override
        public String toString() {
            return "Saver{" +
                    "wifiInfoMap=" + wifiInfoMap +
                    '}';
        }
    }
}
