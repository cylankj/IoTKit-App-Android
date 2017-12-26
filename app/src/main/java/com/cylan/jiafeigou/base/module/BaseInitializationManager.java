package com.cylan.jiafeigou.base.module;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.dagger.annotation.ContextLife;
import com.cylan.jiafeigou.dagger.annotation.Named;
import com.cylan.jiafeigou.push.PushResultReceiver;
import com.cylan.jiafeigou.push.google.QuickstartPreferences;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.OptionsImpl;
import com.cylan.jiafeigou.support.block.impl.BlockCanary;
import com.cylan.jiafeigou.support.block.impl.BlockCanaryContext;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.stat.BugMonitor;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PackageUtils;
import com.squareup.leakcanary.LeakCanary;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareConfig;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by yanzhendong on 2017/4/13.
 */
@Singleton
public final class BaseInitializationManager {
    private CompositeSubscription compositeSubscription;
    private Context appContext;
    private String crashPath;
    private BaseBellCallEventListener bellCallEventListener;
    private PushResultReceiver pushReceiver;
    private UMShareConfig config;

    @Inject
    public BaseInitializationManager(@ContextLife Context context,
                                     @Named("CrashPath") String crashPath,
                                     BaseBellCallEventListener listener
    ) {
        this.appContext = context;
        this.crashPath = crashPath;
        this.bellCallEventListener = listener;
    }

    public void initialization() {
        Log.d("initialization", "initialization," + Thread.currentThread());
        PerformanceUtils.startTrace("initialization");
        enableDebugOptions();
        initBugMonitor();
        initBlockCanary();
        initLeakCanary();
        initGlobalSubscription();
        initPushResult();
        initUmengSdk();
        RxBus.getCacheInstance().postSticky(RxEvent.GlobalInitFinishEvent.INSTANCE);
        PerformanceUtils.stopTrace("initialization");
    }
    private void initUmengSdk() {
        Config.DEBUG = true;
        PlatformConfig.setWeixin(PackageUtils.getMetaString(ContextUtils.getContext(), "weChatAppId"),
                PackageUtils.getMetaString(ContextUtils.getContext(), "weChatAppSecret"));
        PlatformConfig.setQQZone(PackageUtils.getMetaString(ContextUtils.getContext(), "qqAppId"),
                PackageUtils.getMetaString(ContextUtils.getContext(), "qqAppKey"));
        AppLogger.d("sina:" + PackageUtils.getMetaString(ContextUtils.getContext(), "sinaAppKey"));
        AppLogger.d("sina:" + PackageUtils.getMetaString(ContextUtils.getContext(), "sinaAppSecret"));
        PlatformConfig.setSinaWeibo(PackageUtils.getMetaString(ContextUtils.getContext(), "sinaAppKey"),
                PackageUtils.getMetaString(ContextUtils.getContext(), "sinaAppSecret"),
                "https://api.weibo.com/oauth2/default.html");
        PlatformConfig.setTwitter(PackageUtils.getMetaString(ContextUtils.getContext(), "TWITTER_APP_KEY"),
                PackageUtils.getMetaString(ContextUtils.getContext(), "TWITTER_APP_SECRET"));
        config = new UMShareConfig();
        config.isNeedAuthOnGetUserInfo(true);
        config.isOpenShareEditActivity(true);
        config.setSinaAuthType(UMShareConfig.AUTH_TYPE_SSO);
        config.setFacebookAuthType(UMShareConfig.AUTH_TYPE_SSO);
        UMShareAPI.get(appContext).setShareConfig(config);
    }


    public UMShareConfig getConfig() {
        return config;
    }

    private void initPushResult() {
        IntentFilter intentFilter = new IntentFilter(QuickstartPreferences.SENT_TOKEN_TO_SERVER);
        intentFilter.addAction(QuickstartPreferences.SENT_TOKEN_TO_SERVER);
        intentFilter.addAction(QuickstartPreferences.PUSH_MESSAGE_RESULT);
        intentFilter.addAction(QuickstartPreferences.REGISTRATION_COMPLETE);
        intentFilter.addAction(QuickstartPreferences.PUSH_TOKEN);
        if (pushReceiver == null) {
            pushReceiver = new PushResultReceiver();
        }
        try {
            LocalBroadcastManager.getInstance(ContextUtils.getContext())
                    .registerReceiver(pushReceiver, intentFilter);
        } catch (Exception e) {
            Log.d("BaseInitialization", "initPushResultFailed:" + MiscUtils.getErr(e));
        }
    }

    private void initGlobalSubscription() {
        if (compositeSubscription != null) {
            compositeSubscription.unsubscribe();
        }
        compositeSubscription = new CompositeSubscription();
        compositeSubscription.add(bellCallEventListener.initSubscription());
    }

    public void clean() {
        //全局订阅者资源释放
        if (!compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
            compositeSubscription = null;
        }
    }

    private void initBugMonitor() {
        BugMonitor.init(ContextUtils.getContext());
    }

    private void initBlockCanary() {
        BlockCanary.install(appContext, new BlockCanaryContext()).start();
    }

    private void initLeakCanary() {
        if (BuildConfig.DEBUG) {
            LeakCanary.install((Application) appContext);
        }
    }

    private void enableDebugOptions() {
        OptionsImpl.enableCrashHandler(appContext, crashPath);
//        OptionsImpl.enableStrictMode();
    }
}
