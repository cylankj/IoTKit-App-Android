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
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.module.GlideApp;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PackageUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.io.File;

import rx.Observable;
import rx.Subscription;
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
    private static boolean hasAdsChecked = false;

    public static boolean hasAdsChecked() {
        return hasAdsChecked;
    }

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
            Observable.create((Observable.OnSubscribe<String>) subscriber -> {
                Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.AdsRsp.class)
                        .observeOn(Schedulers.io())
                        .subscribe(adsRsp -> {
                            AppLogger.w("广告啊:" + adsRsp);
                            AdsDescription adsDescription = convert(adsRsp);
                            try {
                                String string = PreferencesUtils.getString(JConstant.KEY_ADD_DESC + JFGRules.getLanguageType(), null);
                                Gson gson = new Gson();
                                if (!TextUtils.isEmpty(string)) {
                                    AdsDescription description = gson.fromJson(string, AdsDescription.class);
                                    if (description != null && TextUtils.equals(description.url, adsRsp.picUrl)) {
                                        subscriber.onNext("");
                                        subscriber.onCompleted();
                                    }
                                } else {
                                    GlideApp.with(ContextUtils.getContext()).downloadOnly().load(adsDescription.url)
                                            .signature(new ObjectKey(String.valueOf(adsDescription.expireTime)))
                                            .into(new SimpleTarget<File>() {
                                                @Override
                                                public void onResourceReady(File resource, Transition<? super File> transition) {
                                                    PreferencesUtils.putString(JConstant.KEY_ADD_DESC + JFGRules.getLanguageType(), gson.toJson(adsDescription));
                                                    subscriber.onNext("");
                                                    subscriber.onCompleted();
                                                }
                                            });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                AppLogger.e(e);
                                if (!subscriber.isUnsubscribed()) {
                                    subscriber.onError(e);
                                }
                            }
                        }, e -> {
                            e.printStackTrace();
                            AppLogger.e(MiscUtils.getErr(e));
                        });
                subscriber.add(subscribe);
                try {
                    Command.getInstance().GetAdPolicy(JFGRules.getLanguageType(ContextUtils.getContext()),
                            PackageUtils.getAppVersionName(ContextUtils.getContext()),
                            getResolutionForAds());
                } catch (JfgException e) {
                    e.printStackTrace();
                    AppLogger.e(e);
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.io())
                    .subscribe(ret -> {
                    }, Throwable::printStackTrace);
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
                        AppLogger.w("广告下载失败://表示找不到. ");
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
        hasAdsChecked = true;
        return Observable.create((Observable.OnSubscribe<AdsDescription>) subscriber -> {
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
                subscriber.onNext(null);
                subscriber.onCompleted();
                return;
            }

            try {
                AdsStrategy.AdsDescription description = new Gson().fromJson(content, AdsStrategy.AdsDescription.class);
                if (description == null) {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } else if (description.showCount > 2) {
                    //不需要主动删,因为服务器不会帮忙处理.需要本地记录显示过的.
//                      PreferencesUtils.remove(JConstant.KEY_ADD_DESC);
                    AdsStrategy.getStrategy().fetchAds();
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } else if (System.currentTimeMillis() / 1000 > description.expireTime) {
                    //PreferencesUtils.remove(JConstant.KEY_ADD_DESC);
                    AdsStrategy.getStrategy().fetchAds();
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } else {
                    subscriber.onNext(description);
                    subscriber.onCompleted();
                }
            } catch (Exception e) {
                AdsStrategy.getStrategy().fetchAds();
            }
        }).subscribeOn(Schedulers.io());
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
