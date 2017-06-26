package com.cylan.jiafeigou.base.module;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.cylan.jfgapp.interfases.AppCmd;
import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.base.injector.lifecycle.ContextLife;
import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.db.view.IDBHelper;
import com.cylan.jiafeigou.cache.db.view.IDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.view.IDPTaskFactory;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.pty.IProperty;
import com.cylan.jiafeigou.push.PushResultReceiver;
import com.cylan.jiafeigou.push.google.QuickstartPreferences;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.OptionsImpl;
import com.cylan.jiafeigou.support.block.impl.BlockCanary;
import com.cylan.jiafeigou.support.block.impl.BlockCanaryContext;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.stat.BugMonitor;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PackageUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okserver.download.DownloadManager;
import com.squareup.leakcanary.LeakCanary;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareConfig;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import rx.android.schedulers.AndroidSchedulers;
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
    private BaseUdpMsgParser udpParser;
    private BaseBellCallEventListener bellCallEventListener;
    private PushResultReceiver pushReceiver;
    private BaseDeviceInformationFetcher deviceInformationFetcher;
    private BasePanoramaApiHelper apiHelper;
    private BaseForwardHelper forwardHelper;
    private IProperty iProperty;
    private boolean hasInitFinished = false;

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
                                     BaseUdpMsgParser udpParser,
                                     BaseBellCallEventListener listener,
                                     BasePanoramaApiHelper apiHelper,
                                     BaseDeviceInformationFetcher fetcher,
                                     BaseForwardHelper forwardHelper,
                                     IProperty iProperty
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
        this.deviceInformationFetcher = fetcher;
        this.apiHelper = apiHelper;
        this.forwardHelper = forwardHelper;
        this.iProperty = iProperty;
    }

    public void initialization() {
        hasInitFinished = false;
        initOKGo();
        enableDebugOptions();
        initSourceManager();
        initDBHelper();
        initTaskDispatcher();
        initAppCmd();
        initBugMonitor();
        initBlockCanary();
        initLeakCanary();
        initGlobalSubscription();
        initDialogManager();
        initPushResult();
        initDeviceInformationFetcher();
        this.iProperty.initialize();
        initUmengSdk();
        hasInitFinished = true;
        RxBus.getCacheInstance().postSticky(RxEvent.GlobalInitFinishEvent.INSTANCE);
    }

    private void initOKGo() {
        AndroidSchedulers.mainThread().createWorker().schedule(() -> {
            OkGo.init((Application) appContext);
            DownloadManager.getInstance();
        });
    }

    private void initUmengSdk() {
        Config.DEBUG = true;
        PlatformConfig.setWeixin(PackageUtils.getMetaString(ContextUtils.getContext(), "weChatAppId"),
                PackageUtils.getMetaString(ContextUtils.getContext(), "weChatAppSecret"));
        PlatformConfig.setQQZone(PackageUtils.getMetaString(ContextUtils.getContext(), "qqAppId"),
                PackageUtils.getMetaString(ContextUtils.getContext(), "qqAppKey"));
        PlatformConfig.setSinaWeibo(PackageUtils.getMetaString(ContextUtils.getContext(), "sinaAppKey"),
                PackageUtils.getMetaString(ContextUtils.getContext(), "sinaAppSecret"),
                "https://api.weibo.com/oauth2/default.html");
        PlatformConfig.setTwitter(PackageUtils.getMetaString(ContextUtils.getContext(), "twitterAppKey"),
                PackageUtils.getMetaString(ContextUtils.getContext(), "twitterAppSecret"));
        UMShareConfig config = new UMShareConfig();
        config.isNeedAuthOnGetUserInfo(true);
        config.isOpenShareEditActivity(true);
        config.setSinaAuthType(UMShareConfig.AUTH_TYPE_SSO);
        config.setFacebookAuthType(UMShareConfig.AUTH_TYPE_SSO);
        UMShareAPI.get(appContext).setShareConfig(config);
    }

    private void initDeviceInformationFetcher() {
        forwardHelper.setAppCmd(appCmd);
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

    private void initDialogManager() {

    }

    public boolean isHasInitFinished() {
        return hasInitFinished;
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
            System.loadLibrary("jfgsdk");
            appCmd.setCallBack(callBackHolder);
            appCmd.initNativeParam(vid, vkey, serverAddress, JConstant.ROOT_DIR);
            appCmd.enableLog(true, logPath);
        } catch (Exception e) {
            AppLogger.e("初始化出现错误!!!" + e.getMessage() + "vid:" + vid + ",vkey:" + vkey + ",serverAddress:" + serverAddress + ",logPath:" + logPath);
        }
    }

    private void initBugMonitor() {
        BugMonitor.init(ContextUtils.getContext());
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

    public void observeInitFinish() {
        if (hasInitFinished) {
            RxBus.getCacheInstance().postSticky(RxEvent.GlobalInitFinishEvent.INSTANCE);
        }
    }
}
