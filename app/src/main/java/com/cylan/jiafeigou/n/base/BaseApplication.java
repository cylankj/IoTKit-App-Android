package com.cylan.jiafeigou.n.base;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.engine.DaemonService;
import com.cylan.jiafeigou.n.engine.DataSourceService;
import com.cylan.jiafeigou.support.DebugOptionsImpl;
import com.cylan.jiafeigou.support.block.impl.BlockCanary;
import com.cylan.jiafeigou.support.block.impl.BlockCanaryContext;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.stat.BugMonitor;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PathGetter;
import com.cylan.jiafeigou.utils.SuperSpUtils;
import com.cylan.utils.HandlerThreadUtils;
import com.cylan.utils.ProcessUtils;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by hunt on 16-5-14.
 */
public class BaseApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "BaseApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        enableDebugOptions();
        startService(new Intent(getApplicationContext(), DaemonService.class));
        //每一个新的进程启动时，都会调用onCreate方法。
        if (TextUtils.equals(ProcessUtils.myProcessName(getApplicationContext()), getPackageName())) {
            Log.d("BaseApplication", "BaseApplication..." + ProcessUtils.myProcessName(getApplicationContext()));
            initBlockCanary();
            initBugMonitor();
            registerBootComplete();
        }
        initLeakCanary();
        registerActivityLifecycleCallbacks(this);
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
        DebugOptionsImpl options = new DebugOptionsImpl("test");
        options.enableCrashHandler(this, PathGetter.createPath(JConstant.CRASH_PATH));
        options.enableStrictMode();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                //should release some resource
                Log.d(TAG, "onTrimMemory: " + level);
                shouldKillBellCallProcess();
                break;
        }
    }

    /**
     * 进入后台，应该杀掉呼叫页面的进程
     */
    private void shouldKillBellCallProcess() {
        final int processId = SuperSpUtils.getInstance(getApplicationContext())
                .getAppPreferences().getInt(JConstant.KEY_BELL_CALL_PROCESS_ID, JConstant.INVALID_PROCESS);
        final int isForeground = SuperSpUtils.getInstance(getApplicationContext())
                .getAppPreferences().getInt(JConstant.KEY_BELL_CALL_PROCESS_IS_FOREGROUND, 0);
        if (processId != JConstant.INVALID_PROCESS && isForeground == 0) {
            AppLogger.d("kill processId: " + processId);
            android.os.Process.killProcess(processId);
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
            AppLogger.i("start DataSourceService");
            if (!ProcessUtils.isServiceRunning(context, DataSourceService.class))
                context.startService(new Intent(context, DataSourceService.class));
        }
    }
}
