package com.cylan.jiafeigou.ads;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.cylan.ex.JfgException;
import com.cylan.jfgapp.interfases.AppCmd;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.module.GlideApp;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PackageUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.io.File;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
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
        if (strategy == null) {
            strategy = new AdsStrategy();
        }
        return strategy;
    }

    public void fetchAds() {
        if (System.currentTimeMillis() - lastFetchTime > PERIOD) {
            lastFetchTime = System.currentTimeMillis();
            //获取失败需要重新获取
            Log.d("AdsStrategy", "'AdsStrategy'");
            Observable.just("go and get ads")
                    .subscribeOn(Schedulers.io())
                    .map(s -> {
                        AppCmd cmd =  Command.getInstance();
                        if (cmd != null) {//必须要等待sdk init完成,也即是load dex完成后,才能调用.
                            RxBus.getCacheInstance().toObservableSticky(RxEvent.GlobalInitFinishEvent.class)
                                    .first()
                                    .subscribeOn(Schedulers.io())
                                    .delay(10, TimeUnit.SECONDS)
                                    .subscribe(ret -> {
                                        Log.d("AdsStrategy", "初始化成功'AdsStrategy,手动结束'");
                                        throw new RxEvent.HelperBreaker();
                                    }, throwable -> {
                                        //手动结束订阅
                                        try {
                                            cmd.GetAdPolicy(JFGRules.getLanguageType(ContextUtils.getContext()),
                                                    PackageUtils.getAppVersionName(ContextUtils.getContext()),
                                                    getResolutionForAds());
                                            AppLogger.w("开始获取广告");
                                            RxBus.getCacheInstance().removeStickyEvent(AdsStrategy.AdsDescription.class);
                                        } catch (JfgException e) {
                                            if (BuildConfig.DEBUG) {
                                                throw new IllegalArgumentException("出错了");
                                            }
                                        }
                                    });
                        }
                        return null;
                    })
                    .flatMap(ret -> RxBus.getCacheInstance().toObservableSticky(RxEvent.AdsRsp.class)
                            .first()
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .flatMap(adsRsp -> {
                                AppLogger.w("广告啊:" + adsRsp);
                                String content = PreferencesUtils.getString(JConstant.KEY_ADD_DESC + JFGRules.getLanguageType(), "");
                                if (!TextUtils.isEmpty(content)) {
                                    //判断是否同一个广告
                                    try {
                                        AdsStrategy.AdsDescription description = new Gson().fromJson(content, AdsStrategy.AdsDescription.class);
                                        if (!TextUtils.equals(description.url, adsRsp.picUrl)) {
                                            try2DownloadAds(convert(adsRsp));
                                        }
                                    } catch (Exception e) {
                                        try2DownloadAds(convert(adsRsp));
                                    }
                                } else {
                                    try2DownloadAds(convert(adsRsp));
                                }
                                return null;
                            }))
                    .subscribe(ret -> {
                    }, AppLogger::e);
        }
    }

    private void try2DownloadAds(AdsDescription description) {
        //没有可以先后台下载,必须在主线程调用
        if (description == null || TextUtils.isEmpty(description.url)) {
            return;
        }
        GlideApp.with(ContextUtils.getContext())
                .downloadOnly()
                .load(description.url)
                .signature(new ObjectKey(String.valueOf(description.expireTime)))
                .into(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, Transition<? super File> transition) {
                        AppLogger.w("广告下载成功: ");
                        PreferencesUtils.putString(JConstant.KEY_ADD_DESC + JFGRules.getLanguageType(), new Gson().toJson(description));
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        AppLogger.w("广告下载失败://表示找不到. " );
                        //表示找不到.
                    }
                });
    }

    private AdsDescription convert(RxEvent.AdsRsp adsRsp) {
        AdsDescription description = new AdsDescription();
        description.url = adsRsp.picUrl;
        description.tagUrl = adsRsp.tagUrl;
        description.expireTime = adsRsp.time;
        return description;
    }

    /**
     * 是否显示广告
     *
     * @return
     */
    public Observable<AdsStrategy.AdsDescription> needShowAds() {
        return Observable.just("check and get ads")
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    //1.广告页仅在加菲狗版本、中国大陆地区(简体中文)版本显示，其余版本屏蔽。
                    //2.在广告投放时间期限内，每个用户看到的广告展示次数最多为三次，广告展示次数满三次后不再显示。
//                    int l = JFGRules.getLanguageType(ContextUtils.getContext());
//                    if (l != JFGRules.LANGUAGE_TYPE_SIMPLE_CHINESE) {
//                        //非简体中文
//                        return null;
//                    }
                    String content = PreferencesUtils.getString(JConstant.KEY_ADD_DESC + JFGRules.getLanguageType(), "");
                    if (TextUtils.isEmpty(content)) {
                        AdsStrategy.getStrategy().fetchAds();
                        return null;
                    }
                    try {
                        AdsStrategy.AdsDescription description = new Gson().fromJson(content, AdsStrategy.AdsDescription.class);
                        if (description != null) {
                            //展示两次
                            if (description.showCount > 2) {
                                //不需要主动删,因为服务器不会帮忙处理.需要本地记录显示过的.
//                                PreferencesUtils.remove(JConstant.KEY_ADD_DESC);
                                AdsStrategy.getStrategy().fetchAds();
                                return null;
                            }
                            //过期了
                            if (System.currentTimeMillis() / 1000 > description.expireTime) {
//                                PreferencesUtils.remove(JConstant.KEY_ADD_DESC);
                                AdsStrategy.getStrategy().fetchAds();
                                return null;
                            }
                            return description;
                        } else {
                            return null;
                        }
                    } catch (Exception e) {
                        AdsStrategy.getStrategy().fetchAds();
                        return null;
                    }
                });
    }

    public static class AdsDescription implements Parcelable {
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
    private static final int[] hArray = new int[]{800, 1280, 1920, 2550};

    private static String getResolutionForAds() {
        int w = Resources.getSystem().getDisplayMetrics().widthPixels;
        int h = Resources.getSystem().getDisplayMetrics().heightPixels;
        int i;
        for (i = wArray.length - 1; i >= 0; i--) {
            if (w >= wArray[i]) {
                break;
            }
        }
        AppLogger.d("取到分辨率?" + wArray[i] + ":" + hArray[i]);
        return wArray[i] + "x" + hArray[i];
    }
}
