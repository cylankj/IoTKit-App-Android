package com.cylan.jiafeigou.base.module;

import android.app.Application;
import android.content.Context;

import com.cylan.jfgapp.interfases.AppCmd;
import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.base.injector.lifecycle.ContextLife;
import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.db.view.IDBHelper;
import com.cylan.jiafeigou.cache.db.view.IDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.view.IDPTaskFactory;
import com.cylan.jiafeigou.n.engine.TryLogin;
import com.cylan.jiafeigou.support.OptionsImpl;
import com.cylan.jiafeigou.support.block.impl.BlockCanary;
import com.cylan.jiafeigou.support.block.impl.BlockCanaryContext;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by yanzhendong on 2017/4/13.
 */
@Singleton
public final class BaseInitializationManager {
    private CompositeSubscription compositeSubscription;
    private JFGSourceManager manager;
    private IDBHelper helper;
    private IPropertyParser parser;
    private IDPTaskFactory factory;
    private IDPTaskDispatcher dispatcher;
    private BaseAppCallBackHolder callBackHolder;
    private AppCmd appCmd;
    private String vkey;
    private String vid;
    private String serverAddress;
    private String logPath;
    private Context appContext;
    private String crashPath;
    private BaseJFGResultParser resultParser;
    private BaseGlobalUdpParser udpParser;
    private BaseBellCallEventListener bellCallEventListener;

    @Inject
    public BaseInitializationManager(JFGSourceManager manager,
                                     IDBHelper helper,
                                     IPropertyParser parser,
                                     IDPTaskFactory factory,
                                     IDPTaskDispatcher dispatcher,
                                     BaseAppCallBackHolder callBackHolder,
                                     AppCmd appCmd,
                                     @Named("VKey") String vkey,
                                     @Named("Vid") String vid,
                                     @Named("ServerAddress") String serverAddress,
                                     @Named("LogPath") String logPath,
                                     BaseJFGResultParser resultParser,
                                     @ContextLife Context context,
                                     @Named("CrashPath") String crashPath,
                                     BaseGlobalUdpParser udpParser,
                                     BaseBellCallEventListener listener
    ) {
        compositeSubscription = new CompositeSubscription();
        this.manager = manager;
        this.helper = helper;
        this.parser = parser;
        this.factory = factory;
        this.dispatcher = dispatcher;
        this.callBackHolder = callBackHolder;
        this.appCmd = appCmd;
        this.vkey = vkey;
        this.vid = vid;
        this.serverAddress = serverAddress;
        this.logPath = logPath;
        this.resultParser = resultParser;
        this.appContext = context;
        this.crashPath = crashPath;
        this.udpParser = udpParser;
        this.bellCallEventListener = listener;
    }

    public void initialization() {
        System.loadLibrary("jfgsdk");
        enableDebugOptions();
        initSourceManager();
        initDBHelper();
        initTaskDispatcher();
        initAppCmd();
        initBugMonitor();
        initBlockCanary();
        initLeakCanary();
        initGlobalSubscription();

        TryLogin.tryLogin();//只有等所有资源初始化完成之后才能走 login 流程
    }

    private void initGlobalSubscription() {
        compositeSubscription.add(resultParser.initSubscription());
        compositeSubscription.add(udpParser.initSubscription());
        compositeSubscription.add(bellCallEventListener.initSubscription());
    }

    public void clean() {
        //全局订阅者资源释放
        if (compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
            CompositeSubscription compositeSubscription = null;
        }
        JFGSourceManager manager = null;
        IDBHelper helper = null;
        IPropertyParser parser = null;
        IDPTaskFactory factory = null;
        IDPTaskDispatcher dispatcher = null;
        BaseAppCallBackHolder callBackHolder = null;

        /*appCmd 资源释放*/
        appCmd.releaseApi();
        JfgAppCmd appCmd = null;
        String vkey = null;
        String vid = null;
        String serverAddress = null;
        String logPath = null;
        BaseJFGResultParser resultParser = null;
    }

    private void initSourceManager() {
        manager.setPropertyParser(parser);
        manager.setDBHelper(helper);
        manager.setAppCmd(appCmd);
        manager.initSubscription();
    }

    private void initDBHelper() {
        helper.setDataSourceManager(manager);
        helper.setPropertyParser(parser);
    }

    private void initTaskDispatcher() {
        dispatcher.setPropertyParser(parser);
        dispatcher.setDBHelper(helper);
        dispatcher.setSourceManager(manager);
        dispatcher.setTaskFactory(factory);
        dispatcher.setAppCmd(appCmd);
    }

    private void initAppCmd() {
        try {
            appCmd.setCallBack(callBackHolder);
            appCmd.initNativeParam(vid, vkey, serverAddress);
            appCmd.enableLog(true, logPath);
        } catch (Exception e) {
            AppLogger.e("初始化出现错误!!!" + e.getMessage());
        }
    }

    private void initBugMonitor() {
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(appContext);
        strategy.setAppChannel(BuildConfig.DEBUG ? "DEBUG" : "CLEVER_DOG");
        CrashReport.initCrashReport(appContext, strategy);
        com.tencent.bugly.Bugly.enable = BuildConfig.DEBUG;
    }

    private void initBlockCanary() {
        BlockCanary.install(appContext, new BlockCanaryContext()).start();
    }

    private void initLeakCanary() {
        LeakCanary.install((Application) appContext);
    }

    private void enableDebugOptions() {
        OptionsImpl.enableCrashHandler(appContext, crashPath);
    }
}
