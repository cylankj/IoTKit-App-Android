package com.cylan.jiafeigou.n.base;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.cylan.ext.opt.DebugOptionsImpl;
import com.cylan.jiafeigou.DaemonReceiver1;
import com.cylan.jiafeigou.DaemonReceiver2;
import com.cylan.jiafeigou.DaemonService1;
import com.cylan.jiafeigou.DaemonService2;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.engine.DataSourceService;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.block.impl.BlockCanary;
import com.cylan.jiafeigou.support.block.impl.BlockCanaryContext;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.stat.BugMonitor;
import com.cylan.jiafeigou.support.stat.MtaManager;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.HandlerThreadUtils;
import com.cylan.jiafeigou.utils.PathGetter;
import com.cylan.jiafeigou.utils.ProcessUtils;
import com.danikula.videocache.HttpProxyCacheServer;
import com.facebook.FacebookSdk;
import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiClient;
import com.huawei.hms.support.api.push.HuaweiPush;
import com.marswin89.marsdaemon.DaemonClient;
import com.marswin89.marsdaemon.DaemonConfigurations;
import com.squareup.leakcanary.LeakCanary;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import io.fabric.sdk.android.Fabric;

/**
 * Created by hunt on 16-5-14.
 */
public class BaseApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks, HuaweiApiClient.ConnectionCallbacks, HuaweiApiClient.OnConnectionFailedListener {

    private static final String TAG = "BaseApplication";
    private HttpProxyCacheServer proxy;

    private static final String TWITTER_KEY = "kCEeFDWzz5xHi8Ej9Wx6FWqRL";
    private static final String TWITTER_SECRET = "Ih4rUwyhKreoHqzd9BeIseAKHoNRszi2rT2udlMz6ssq9LeXw5";

    private DaemonClient mDaemonClient;
    private HuaweiApiClient client;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mDaemonClient = new DaemonClient(createDaemonConfigurations());
        mDaemonClient.onAttachBaseContext(base);
    }


    private DaemonConfigurations createDaemonConfigurations() {
        DaemonConfigurations.DaemonConfiguration configuration1 = new DaemonConfigurations.DaemonConfiguration(
                getPackageName() + ":process1",
                DaemonService1.class.getCanonicalName(),
                DaemonReceiver1.class.getCanonicalName());
        DaemonConfigurations.DaemonConfiguration configuration2 = new DaemonConfigurations.DaemonConfiguration(
                getPackageName() + ":process2",
                DaemonService2.class.getCanonicalName(),
                DaemonReceiver2.class.getCanonicalName());
        DaemonConfigurations.DaemonListener listener = new MyDaemonListener();
        //return new DaemonConfigurations(configuration1, configuration2);//listener can be null
        return new DaemonConfigurations(configuration1, configuration2, listener);
    }

    @Override
    public void onConnected() {
        AppLogger.d("华为推送连接成功");
        HuaweiPush.HuaweiPushApi.getToken(client).setResultCallback(result -> {
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        AppLogger.d("onConnectionSuspended" + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        AppLogger.d("华为推送连接失败" + result.getErrorCode());
    }


    class MyDaemonListener implements DaemonConfigurations.DaemonListener {
        @Override
        public void onPersistentStart(Context context) {
        }

        @Override
        public void onDaemonAssistantStart(Context context) {
        }

        @Override
        public void onWatchDaemonDaed() {
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        GlobalDataProxy.getInstance().setLoginState(new LogState(LogState.STATE_ACCOUNT_OFF));
        enableDebugOptions();
        MtaManager.init(getApplicationContext(), true);
        //每一个新的进程启动时，都会调用onCreate方法。
//        if (TextUtils.equals(ProcessUtils.myProcessName(getApplicationContext()), getPackageName())) {
        Log.d("BaseApplication", "BaseApplication..." + ProcessUtils.myProcessName(getApplicationContext()));
        startService(new Intent(getApplicationContext(), DataSourceService.class));
        initBlockCanary();
        initBugMonitor();
        registerBootComplete();
//        }
        initLeakCanary();
        registerActivityLifecycleCallbacks(this);

        initTwitter();
        initFaceBook();
        initHuaweiPushSDK();
    }

    private void initHuaweiPushSDK() {
        AppLogger.d("正在初始化华为推送SDK");
        client = new HuaweiApiClient.Builder(this)
                .addApi(HuaweiPush.PUSH_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        client.connect();
    }

    private void initFaceBook() {
        HandlerThreadUtils.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                FacebookSdk.sdkInitialize(getApplicationContext());
            }
        });
    }

    private void initTwitter() {
        HandlerThreadUtils.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
                Fabric.with(getApplicationContext(), new TwitterCore(authConfig), new TweetComposer());
            }
        });
    }


    private void initLeakCanary() {
        HandlerThreadUtils.post(new Runnable() {
            @Override
            public void run() {
                LeakCanary.install(BaseApplication.this);
            }
        });
    }

    private void initBlockCanary() {
        HandlerThreadUtils.post(new Runnable() {
            @Override
            public void run() {
                AppLogger.d("initBlockCanary");
                //BlockCanary
                BlockCanary.install(ContextUtils.getContext(), new BlockCanaryContext()).start();
            }
        });
    }

    private void initBugMonitor() {
        HandlerThreadUtils.post(new Runnable() {
            @Override
            public void run() {
                //bugLy
                BugMonitor.init(ContextUtils.getContext());
            }
        });
    }

    /**
     * 注册启动监听广播
     */
    private void registerBootComplete() {
        try {
            BootCompletedReceiver receiver = new BootCompletedReceiver();
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
            registerReceiver(receiver, intentFilter);
            Log.d(TAG, "bootComplete");
        } catch (Exception e) {
            Log.d(TAG, "bootComplete: e: " + e.toString());
        }
    }

    private void enableDebugOptions() {
        DebugOptionsImpl.enableCrashHandler(this, PathGetter.createPath(JConstant.CRASH_PATH));

//        DebugOptionsImpl.enableStrictMode();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (client != null && client.isConnected()) {
            client.disconnect();
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                //should release some resource
                Log.d(TAG, "onTrimMemory: " + level);
//                shouldKillBellCallProcess();
                RxBus.getCacheInstance().post(new RxEvent.AppHideEvent());
//                JfgCmdInsurance.getCmd().closeDataBase();
                break;
        }
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "onLowMemory: ");
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        AppLogger.i("life:onActivityCreated: " + activity.getClass().getSimpleName() + " " + savedInstanceState);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        AppLogger.i("life:onActivityStarted " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        AppLogger.i("life:onActivityResumed " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityPaused(Activity activity) {
        AppLogger.i("life:onActivityPaused " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        AppLogger.i("life:onActivityStopped " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        AppLogger.i("life:onActivitySaveInstanceState " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        AppLogger.i("life:onActivityDestroyed " + activity.getClass().getSimpleName());
    }


    public static class BootCompletedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!ProcessUtils.isServiceRunning(context, DataSourceService.class)) {
                AppLogger.i("start DataSourceService");
                context.startService(new Intent(context, DataSourceService.class));
            }
        }
    }

    public static HttpProxyCacheServer getProxy(Context context) {
        BaseApplication app = (BaseApplication) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer.Builder(this).maxCacheSize(Long.MAX_VALUE).maxCacheFilesCount(Integer.MAX_VALUE).build();
    }


}
