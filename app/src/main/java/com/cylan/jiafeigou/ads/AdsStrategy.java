package com.cylan.jiafeigou.ads;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.cylan.ex.JfgException;
import com.cylan.jfgapp.interfases.AppCmd;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PackageUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by hds on 17-5-26.
 */

public class AdsStrategy {

    /**
     * 需要改成一个小时
     */
    private static final long PERIOD = 2 * 10 * 1000;
    private static long lastFetchTime = 0;
    private static AdsStrategy strategy;

    public static AdsStrategy getStrategy() {
        if (strategy == null) strategy = new AdsStrategy();
        return strategy;
    }

    public void fetchAds() {
        if (System.currentTimeMillis() - lastFetchTime > PERIOD) {
            lastFetchTime = System.currentTimeMillis();
            //获取失败需要重新获取
            Log.d("AdsStrategy", "'AdsStrategy'");
            Observable.just("go and get ads")
                    .subscribeOn(Schedulers.newThread())
                    .map(s -> {
                        AppCmd cmd = BaseApplication.getAppComponent().getCmd();
                        if (cmd != null) {//必须要等待sdk init完成,也即是load dex完成后,才能调用.
                            RxBus.getCacheInstance().toObservableSticky(RxEvent.GlobalInitFinishEvent.class)
                                    .first()
                                    .subscribeOn(Schedulers.newThread())
                                    .delay(3, TimeUnit.SECONDS)
                                    .subscribe(ret -> {
                                        Log.d("AdsStrategy", "初始化成功'AdsStrategy,手动结束'");
                                        throw new RxEvent.HelperBreaker();
                                    }, throwable -> {
                                        //手动结束订阅
//                                        try {
//                                            cmd.GetAdPolicy(JFGRules.getLanguageType(ContextUtils.getContext()),
//                                                    PackageUtils.getAppVersionName(ContextUtils.getContext()),
//                                                    getResolutionForAds());
//                                            AppLogger.d("开始获取广告");
//                                            RxBus.getCacheInstance().removeStickyEvent(AdsStrategy.AdsDescription.class);
//                                        } catch (JfgException e) {
//                                            if (BuildConfig.DEBUG)
//                                                throw new IllegalArgumentException("出错了");
//                                        }
                                    });
                        }
                        return null;
                    })
                    .flatMap(ret -> RxBus.getCacheInstance().toObservableSticky(AdsStrategy.AdsDescription.class)
                            .first()
                            .flatMap(adsDescription -> {
                                AppLogger.d("广告啊:" + adsDescription);
                                return null;
                            }))
                    .subscribe(ret -> {
                    }, AppLogger::e);
        }
    }

    public static class AdsDescription implements Parcelable{
        /**
         * 图片链接
         */
        public String url;
        /**
         * 用来标记点击
         */
        public String tagUrl;
        /**
         * 有效期
         */
        public long expireTime;
        /**
         * 展示次数
         */
        public int showCount;

        @Override
        public String toString() {
            return "AdsDescription{" +
                    "url='" + url + '\'' +
                    ", tagUrl='" + tagUrl + '\'' +
                    ", expireTime=" + expireTime +
                    ", showCount=" + showCount +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.url);
            dest.writeString(this.tagUrl);
            dest.writeLong(this.expireTime);
            dest.writeInt(this.showCount);
        }

        public AdsDescription() {
        }

        protected AdsDescription(Parcel in) {
            this.url = in.readString();
            this.tagUrl = in.readString();
            this.expireTime = in.readLong();
            this.showCount = in.readInt();
        }

        public static final Creator<AdsDescription> CREATOR = new Creator<AdsDescription>() {
            @Override
            public AdsDescription createFromParcel(Parcel source) {
                return new AdsDescription(source);
            }

            @Override
            public AdsDescription[] newArray(int size) {
                return new AdsDescription[size];
            }
        };
    }

    private static final int[] wArray = new int[]{480, 768, 1080, 1440};
    private static final int[] hArray = new int[]{800, 1280, 1920, 2500};

    private static String getResolutionForAds() {
        int w = Resources.getSystem().getDisplayMetrics().widthPixels;
        int h = Resources.getSystem().getDisplayMetrics().heightPixels;
        int i = 0;
        for (i = 0; i < wArray.length; i++) {
            if (w > wArray[i])
                break;
        }
        AppLogger.d("取到分辨率?" + wArray[i]);
        return wArray[i] + "x" + hArray[i];
    }
}
