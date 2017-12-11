package com.cylan.jiafeigou.n.base;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.JfgEnum;
import com.cylan.jiafeigou.MyObjectBox;
import com.cylan.jiafeigou.base.module.BaseInitializationManager;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.dagger.component.AppComponent;
import com.cylan.jiafeigou.dagger.component.DaggerAppComponent;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.n.engine.AppServices;
import com.cylan.jiafeigou.n.engine.GlobalResetPwdSource;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.server.cache.Device;
import com.cylan.jiafeigou.server.cache.PropertyItem;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.hook.HookHelper;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ProcessUtils;
import com.danikula.videocache.HttpProxyCacheServer;
import com.lzy.okgo.OkGo;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasFragmentInjector;
import dagger.android.support.HasSupportFragmentInjector;
import io.objectbox.Box;
import io.objectbox.BoxStore;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by hunt on 16-5-14.
 */
public class BaseApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks, HasActivityInjector, HasSupportFragmentInjector, HasFragmentInjector {

    @Inject
    DispatchingAndroidInjector<Activity> activityDispatchingAndroidInjector;
    @Inject
    DispatchingAndroidInjector<Fragment> supportFragmentDispatchingAndroidInjector;
    @Inject
    DispatchingAndroidInjector<android.app.Fragment> fragmentDispatchingAndroidInjector;
    @Inject
    BaseInitializationManager initializationManager;


    private static final String TAG = "BaseApplication";


    //    private static AppComponent appComponent;
    private static int viewCount = 0;
    private static BoxStore boxStore;

    private static Box<PropertyItem> propertyItemBox;

    private static Box<Device> deviceBox;
    private volatile boolean needToInject = true;
    private static AppComponent appComponent;

    public static BoxStore getBoxStore() {
        return boxStore;
    }

    //
    public static Box<PropertyItem> getPropertyItemBox() {
        return propertyItemBox;
    }

    public static Box<Device> getDeviceBox() {
        return deviceBox;
    }

    @Override
    public void onCreate() {
        if (TextUtils.equals(ProcessUtils.myProcessName(this), getPackageName())) {
            injectIfNecessary();
        }
        super.onCreate();

        //这是主进程
        if (TextUtils.equals(ProcessUtils.myProcessName(this), getApplicationContext().getPackageName())) {
            //设计师不需要这个固定通知栏.20170531
//            startService(new Intent(this, WakeupService.class));
            PreferencesUtils.init(getApplicationContext());
            PerformanceUtils.startTrace("appInit");
            viewCount = 0;
            //Dagger2 依赖注入
            boxStore = MyObjectBox.builder().androidContext(this).buildDefault();
            DataSourceManager.getInstance();//以后会去掉 datasource
            propertyItemBox = boxStore.boxFor(PropertyItem.class);
            deviceBox = boxStore.boxFor(Device.class);
            OkGo.init(this);
            initializationManager.initialization();

            //每一个新的进程启动时，都会调用onCreate方法。
            //Dagger2 依赖注入,初始化全局资源
            registerActivityLifecycleCallbacks(this);
            PerformanceUtils.stopTrace("appInit");
            PerformanceUtils.startTrace("app2SmartCall");
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        AppLogger.d("进程已被销毁!!!!");
        initializationManager.clean();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
//            HookHelper.hookInstrument();
            HookHelper.hookAMS();
//            HookHelper.hookPackageManager(base);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                //should release some resource
                Log.d(TAG, "onTrimMemory: " + level);
                RxBus.getCacheInstance().post(new RxEvent.AppHideEvent());
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
        viewCount++;
        GlobalResetPwdSource.getInstance().currentActivity(activity);

        RxBus.getCacheInstance().post(new RxEvent.ActivityStartEvent());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        AppLogger.i("life:onActivityResumed " + activity.getClass().getSimpleName());
        cancelReportTask();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        AppLogger.i("life:onActivityPaused " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        AppLogger.i("life:onActivityStopped " + activity.getClass().getSimpleName());
        viewCount--;
        if (viewCount == 0) {
            GlobalResetPwdSource.getInstance().currentActivity(null);
            prepareReportTask();
        }
    }

    private Subscription reportTask;

    /**
     * 退出后台3分钟,将向sdkReport网络状态
     */
    private void prepareReportTask() {
        if (Command.getInstance() != null) {
            if (reportTask != null) {
                reportTask.unsubscribe();
            }
            reportTask = Observable.just("report")
                    .subscribeOn(Schedulers.io())
                    .delay(3, TimeUnit.MINUTES)
                    .subscribe(ret -> {
                        AppLogger.d("timeout for report");
                        Command.getInstance().reportEnvChange(JfgEnum.ENVENT_TYPE.ENV_NETWORK_CONNECTED);
                    }, throwable -> {
                        AppLogger.d("timeout for report");
                        Command.getInstance().reportEnvChange(JfgEnum.ENVENT_TYPE.ENV_NETWORK_CONNECTED);
                    });
        }
    }

    private void cancelReportTask() {
        if (reportTask != null) {
            reportTask.unsubscribe();
        }
        Command.getInstance().reportEnvChange(JfgEnum.ENVENT_TYPE.ENV_ONTOP);
    }

    public static boolean isBackground() {
        return viewCount == 0;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
//        AppLogger.i("life:onActivitySaveInstanceState " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
//        AppLogger.i("life:onActivityDestroyed " + activity.getClass().getSimpleName());
    }

    public static HttpProxyCacheServer getProxy() {
        return appComponent.getHttpProxyCacheServer();
    }

    public static AppComponent getAppComponent() {
        return appComponent;
    }

    public static boolean isOnline() {
        return false;
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return activityDispatchingAndroidInjector;
    }

    @Override
    public AndroidInjector<android.app.Fragment> fragmentInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return supportFragmentDispatchingAndroidInjector;
    }

    /**
     * Lazily injects the {@link DaggerApplication}'s members. Injection cannot be performed in {@link
     * Application#onCreate()} since {@link android.content.ContentProvider}s' {@link
     * android.content.ContentProvider#onCreate() onCreate()} method will be called first and might
     * need injected members on the application. Injection is not performed in the the constructor, as
     * that may result in members-injection methods being called before the constructor has completed,
     * allowing for a partially-constructed instance to escape.
     */
    private void injectIfNecessary() {
        if (needToInject) {
            synchronized (this) {
                if (needToInject) {
                    appComponent = DaggerAppComponent.builder().application(this).build();
                    appComponent.inject(this);
                    if (needToInject) {
                        throw new IllegalStateException(
                                "The AndroidInjector returned from applicationInjector() did not inject the "
                                        + "DaggerApplication");
                    }
                }
            }
        }
    }

    @Inject
    void setInjected() {
        needToInject = false;
    }


}
