
package com.cylan.jiafeigou.base;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.os.StrictMode;
import android.view.View;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.activity.SmartCall;
import com.cylan.jiafeigou.block.BlockCanary;
import com.cylan.jiafeigou.block.BlockCanaryContext;
import com.cylan.jiafeigou.engine.CallbackManager;
import com.cylan.jiafeigou.engine.MyService;
import com.cylan.jiafeigou.engine.UnSendQueue;
import com.cylan.jiafeigou.entity.msg.rsp.LoginRsp;
import com.cylan.jiafeigou.utils.AppManager;
import com.cylan.jiafeigou.utils.NotificationUtil;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.OEMConf;
import com.cylan.jiafeigou.utils.PathGetter;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.CrashHandler;
import com.cylan.publicApi.JniPlay;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.support.DswLog;
import com.cylan.utils.PackageUtils;
import com.tencent.stat.StatService;

import support.stat.bugly.Bugly;
import support.stat.mta.MtaManager;
import support.uil.cache.disc.naming.Md5FileNameGenerator;
import support.uil.core.ImageLoader;
import support.uil.core.ImageLoaderConfiguration;
import support.uil.core.assist.QueueProcessingType;
import support.uil.utils.L;

public class MyApp extends Application {

//    private static FinalBitmap mLoader;

    static {
        System.loadLibrary("media-engine-jni");
    }

    private static Application instance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        debugDetect();
        instance = this;
        Bugly.init(this, BuildConfig.DEBUG, PackageUtils.getAppVersionName(this));
        CrashHandler.getInstance(null).init(getApplicationContext());
        initDswLog();
        initBlockCanary();
        MtaManager.init(this, BuildConfig.DEBUG);
        StatService.trackCustomEvent(this, "App onCreate", "");
        OEMConf.LoadConf(this);
        initServerConfig();
        initImageLoaderConfig();
        JniPlay.SetHttpRoot(PathGetter.getUpgradePath());
        CallbackManager.getInstance().clearObserver();
        UnSendQueue.getInstance().clear();
    }

    private void debugDetect() {
        if (!BuildConfig.DEBUG)
            return;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyDialog()
                .build());
    }

    private void initDswLog() {
        DswLog.debug = com.cylan.jiafeigou.BuildConfig.DEBUG;
        DswLog.setRootDir(Constants.ROOT_DIR);
    }

    private void initBlockCanary() {
        BlockCanary.install(this, new BlockCanaryContext()).start();
    }

    public static Application getContext() {
        return instance;
    }

//    private FinalBitmap initFinalBitmap() {
//        mLoader = FinalBitmap.create(this);
//        mLoader.configBitmapLoadThreadSize(5);
//        mLoader.configDiskCachePath(PathGetter.getImgPath());
//        mLoader.configMemoryCacheSize(5);
//        mLoader.configRecycleImmediately(true);
//        return mLoader;
//    }

    private void initImageLoaderConfig() {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb
                .memoryCacheSize((int) (Runtime.getRuntime().maxMemory() / 1024) / 6)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        ImageLoader.getInstance().init(config);
        L.writeDebugLogs(BuildConfig.DEBUG);
    }


//    public static FinalBitmap getFinalBitmap() {
//        return mLoader;
//    }

    public static boolean getIsLogin() {
        return MyService.getIsLogin();
    }

    public static boolean getIsConnectServer() {
        return MyService.getIsConnectServer();
    }


    public static void logout(Context context) {
        PreferenceUtil.setIsLogout(context, true);
        if (PreferenceUtil.getIsOtherLoginType(context)) {
            PreferenceUtil.setIsOtherLoginType(context, false);
            PreferenceUtil.setBindingPhone(context, "");
            PreferenceUtil.setThirDswLoginPicUrl(context, "");
            PreferenceUtil.setOtherLoginType(context, -1);
        }
        if (PreferenceUtil.getIsLoginType(context)) {
            PreferenceUtil.setIsLoginType(context, false);
        }
        PreferenceUtil.cleanSessionId(context);
        MsgpackMsg.MsgHeader.setSession("");
        CallbackManager.getInstance().clearObserver();
        NotificationUtil.cancelAllNotifycation(context);
    }

    public static void showForceNotifyDialog(final Context context, String msg) {
        final NotifyDialog dialog = new NotifyDialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setButtonText(R.string.OK, R.string.CANCEL);
        dialog.hideNegButton();
        dialog.show(msg, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.confirm:
                        dialog.dismiss();
                        MyApp.logout(context);
                        JniPlay.DisconnectFromServer();
                        startActivityToSmartCall(context);
                        break;
                }

            }
        }, null);
    }


    public static void startActivityToSmartCall(Context context) {
        AppManager.getAppManager().finishAllActivity();
        Intent intent = new Intent(context, SmartCall.class);
        context.startActivity(intent);
    }


    private void initServerConfig() {
        if (PreferenceUtil.getIP(this).isEmpty()) {
            PreferenceUtil.setIP(this, Constants.ADDR);
        }
        if (PreferenceUtil.getPort(this) == 0) {
            PreferenceUtil.setPort(this, Constants.CONFERENCE_PORT);
        }
    }


    public static boolean wsRequest(byte[] str) {
        if (str == null)
            return false;
        if (!MyService.getIsLogin()) {
            MyService.addQuene(str);
            return false;
        }
        JniPlay.SendBytes(str);
        return true;
    }


    public static int getMsgID() {
        return MyService.getMsgID();
    }

    public static WifiInfo getConnectNet() {
        return MyService.getConnectNet();
    }

    public static void setUpdateUrl(Context ctx, String url) {

        if (url.contains("id=")) {
            String id = url.substring(url.indexOf("id=") + 3);
            PreferenceUtil.setCheckVersionUrl(ctx, id);
        }
        if (url.contains("/s")) {
            PreferenceUtil.setCheckVersion(ctx, url.substring(0, url.indexOf("/s")));
        }
        if (!StringUtils.isEmptyOrNull(url)) {
            PreferenceUtil.setDownloadAddressUrl(ctx, url);
        }
    }

    public static void initConfig(Context ctx, LoginRsp rsp) {
        PreferenceUtil.setBindingPhone(ctx, rsp.account);
        PreferenceUtil.setIsUpgrade(ctx, rsp.is_upgrade);
        PreferenceUtil.setKeyMsgCount(ctx, rsp.msg_count);
        MyApp.setUpdateUrl(ctx, rsp.url);
        PreferenceUtil.setSessionId(ctx, rsp.sessid);
        MsgpackMsg.MsgHeader.setSession(rsp.sessid);
        PreferenceUtil.setIsSafeLogin(ctx, rsp.is_safe == 1);
        JniPlay.SetLocalID(rsp.account, rsp.sessid);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        DswLog.i("onTrimMemory, level-->" + level);
        if (level > TRIM_MEMORY_MODERATE) {
            releaseMemory();
        }
    }

    public static void releaseMemory() {
//        mLoader.clearCache();
        ImageLoader.getInstance().clearMemoryCache();
//        AppManager.getAppManager().finishAllActivity();     //////finish 所有activity产生类是闪退的现象，暂时屏蔽 by yangc
    }


}