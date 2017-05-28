package com.cylan.jiafeigou.n.engine;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.ClientUpdateManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PackageUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.n.engine.AfterLoginService.GooglePlayCheckResult.NEW_VERSION;
import static com.cylan.jiafeigou.n.engine.AfterLoginService.GooglePlayCheckResult.NO_VERSION;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - setDevice intent actions, extra parameters and static
 * helper methods.
 * 登陆成功后，需要刷新一些缓存，数据，都在这里做。
 */
public class AfterLoginService extends IntentService {

    private static final String TAG = "KEY";
    /**
     * 保存账号密码，登陆成功后保存。
     */
    public static final String ACTION_SAVE_ACCOUNT = "action_save_account";
    public static final String ACTION_GET_ACCOUNT = "action_get_account";
    public static final String ACTION_SYN_OFFLINE_REQ = "action_offline_req";

    public static final String ACTION_CHECK_VERSION = "action_check_version";

    private static long clientVersionCheck = 0;

    public AfterLoginService() {
        super("AfterLoginService");
    }

    public static void startSaveAccountAction(Context context) {
        Intent intent = new Intent(context, AfterLoginService.class);
        intent.putExtra(TAG, ACTION_SAVE_ACCOUNT);
        context.startService(intent);
    }

    public static void startGetAccountAction(Context context) {
        Intent intent = new Intent(context, AfterLoginService.class);
        intent.putExtra(TAG, ACTION_GET_ACCOUNT);
        context.startService(intent);
    }

    /**
     * 恢复离线时候,加入请求队列的消息
     */
    public static void resumeOfflineRequest() {
        Intent intent = new Intent(ContextUtils.getContext(), AfterLoginService.class);
        intent.putExtra(TAG, ACTION_SYN_OFFLINE_REQ);
        ContextUtils.getContext().startService(intent);
    }

    /**
     * 恢复离线时候,加入请求队列的消息
     */
    public static void resumeTryCheckVersion() {
        if (clientVersionCheck == 0 || System.currentTimeMillis() - clientVersionCheck > 5 * 60 * 1000L) {
            clientVersionCheck = System.currentTimeMillis();
        } else return;
        Intent intent = new Intent(ContextUtils.getContext(), AfterLoginService.class);
        intent.putExtra(TAG, ACTION_CHECK_VERSION);
        ContextUtils.getContext().startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getStringExtra(TAG);
            AppLogger.i("AfterLoginService: " + action + ",looper: " + (Looper.myLooper() == Looper.getMainLooper()));
            if (TextUtils.equals(action, ACTION_SAVE_ACCOUNT)) {
            } else if (TextUtils.equals(action, ACTION_GET_ACCOUNT)) {
                BaseApplication.getAppComponent().getCmd().getAccount();
            } else if (TextUtils.equals(action, ACTION_SYN_OFFLINE_REQ)) {
                Observable.just("go and do something")
                        .subscribeOn(Schedulers.newThread())
                        .subscribe(s -> {
                            try {
                                String content = PreferencesUtils.getString(JConstant.BINDING_DEVICE);
                                UdpConstant.UdpDevicePortrait portrait = new Gson().fromJson(content, UdpConstant.UdpDevicePortrait.class);
                                if (portrait != null) {
                                    BaseApplication.getAppComponent().getCmd().bindDevice(portrait.uuid, portrait.bindCode, portrait.mac, portrait.bindFlag);
                                    //设备上线后,需要设置时区.
                                }
                            } catch (Exception e) {
                                AppLogger.d("err: " + e.getLocalizedMessage());
                            }
                        }, throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()));
            } else if (TextUtils.equals(action, ACTION_CHECK_VERSION)) {
                Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
                AppLogger.d("尝试检查版本");
                checkVersion();
            }
        }
    }

    @IntDef({
            NO_VERSION,
            NEW_VERSION,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface GooglePlayCheckResult {
        int NO_VERSION = 100;
        int NEW_VERSION = 200;
    }

    /**
     * 从Google Play检查
     *
     * @return
     */
    private Observable<RxEvent.ClientCheckVersion> checkVersionFromGooglePlay() {
        return MiscUtils.getAppVersionFromGooglePlay()
                .subscribeOn(Schedulers.newThread())
                .flatMap(gVersion -> {
                    String v = PackageUtils.getAppVersionName(ContextUtils.getContext());
                    AppLogger.d("有没有?" + v + ",gV: " + gVersion);
                    //有新包
                    RxEvent.ClientCheckVersion version = new RxEvent.ClientCheckVersion(0, null, 1);
                    if (BindUtils.versionCompare(gVersion, v) > 0) {
                        version.ret = NEW_VERSION;
                    } else {
                        version.ret = NO_VERSION;
                        version.forceUpgrade = 0;
                    }
                    return Observable.just(version);
                });
    }

    /**
     * 从8小时检查
     *
     * @return
     */
    private Observable<RxEvent.ClientCheckVersion> checkVersionFrom8Hour() {
        return Observable.just("check_version")
                .subscribeOn(Schedulers.newThread())
                .delay(3, TimeUnit.SECONDS)
                .timeout(10, TimeUnit.SECONDS)
                .filter(s -> {
                    int netType = NetUtils.getJfgNetType(ContextUtils.getContext());
                    return netType == 1;//wifi
                })
                .flatMap(s -> {
                    int req = -1;
                    try {
                        String vid = PackageUtils.getMetaString(ContextUtils.getContext(), "vId");
                        req = BaseApplication.getAppComponent().getCmd().checkClientVersion(vid);
                    } catch (JfgException e) {
                        AppLogger.e("check_version failed:" + MiscUtils.getErr(e));
                    }
                    return Observable.just(req);
                })
                .filter(ret -> ret >= 0)
                .flatMap(integer -> RxBus.getCacheInstance().toObservable(RxEvent.ClientCheckVersion.class)
                        .flatMap(clientCheckVersion -> {
                            throw new RxEvent.HelperBreaker(clientCheckVersion);
                        }));
    }

    private void checkVersion() {
        int netType = NetUtils.getJfgNetType(ContextUtils.getContext());
        if (netType != 1)//wifi
            return;
        if (MiscUtils.isGooglePlayServiceAvailable()) {
            //google play 可用 只走google play
            checkVersionFromGooglePlay()
                    .subscribeOn(Schedulers.newThread())
                    .filter(ret -> ret != null)
                    .subscribe(ret -> {
                        AppLogger.d("google play检查版本结果?" + ret);
                        if (ret.ret == NEW_VERSION) {
                            RxBus.getCacheInstance().postSticky(new RxEvent.ApkDownload("")
                                    .setRsp(new RxEvent.CheckVersionRsp(true, "", "", "", ""))
                                    .setUpdateType(RxEvent.UpdateType.GOOGLE_PLAY));
                        }
                    }, AppLogger::e);
        } else {
            checkVersionFrom8Hour().subscribeOn(Schedulers.newThread())
                    .subscribe(ret -> {
                    }, throwable -> {//让整条订阅连结束
                        if (throwable instanceof RxEvent.HelperBreaker) {
                            if (((RxEvent.HelperBreaker) throwable).object != null && ((RxEvent.HelperBreaker) throwable).object instanceof RxEvent.ClientCheckVersion)
                                checkRsp((RxEvent.ClientCheckVersion) ((RxEvent.HelperBreaker) throwable).object);
                        }
                    });
        }
    }

    private void checkRsp(final RxEvent.ClientCheckVersion clientCheckVersion) {
        AppLogger.d("check_version result: " + clientCheckVersion);
//                                    clientCheckVersion.result = "VRJz6f";
//                                    2iYjQr
        if (TextUtils.isEmpty(clientCheckVersion.result))
            return;
        String result = clientCheckVersion.result.replace("http://yun.app8h.com/s?id=", "");
        String finalUrl = JConstant.assembleUrl(result, getApplicationContext().getPackageName());
        Request.Builder requestBuilder = new Request.Builder().url(finalUrl);
        requestBuilder.method("GET", null);
        OkHttpClient client = new OkHttpClient();
        client.newCall(requestBuilder.build())
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        AppLogger.e("check_version what the hell?" + MiscUtils.getErr(e));
                        PreferencesUtils.remove(JConstant.KEY_CLIENT_UPDATE_DESC);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        dealClient(response, clientCheckVersion);
                    }
                });
    }

    private void dealClient(Response response, RxEvent.ClientCheckVersion clientCheckVersion) {
        //不需要那么频繁地检查.
        try {
            String result = response.body().string();
            AppLogger.d("check_version result: " + result);
            JSONObject jsonObject = new JSONObject(result);
            final String url = jsonObject.getString("url");
            final String versionName = jsonObject.getString("version");
            final String shortVersion = jsonObject.getString("shortversion");
            final String desc = jsonObject.getString("desc");
            int currentAppVersionCode = PackageUtils.getAppVersionCode(ContextUtils.getContext());
            try {
                //1.版本检测
                if (currentAppVersionCode >= Integer.parseInt(shortVersion)) {
                    AppLogger.d("本地版本较高不需要升级");
                    PreferencesUtils.remove(JConstant.KEY_CLIENT_UPDATE_DESC);
                    FileUtils.deleteFile(JConstant.MISC_PATH + File.separator + versionName + ".apk");
                    return;
                }
            } catch (Exception e) {
            }
            final RxEvent.CheckVersionRsp rsp = new RxEvent.CheckVersionRsp(true,
                    url, versionName, desc, "");
            rsp.forceUpdate = clientCheckVersion.forceUpgrade;
            rsp.fileDir = JConstant.ROOT_DIR;
            rsp.fileSize = getFileSizeFromUrl(url);
            rsp.fileName = versionName + ".apk";
            rsp.preKey = JConstant.KEY_CLIENT_UPDATE_DESC;
            //2.文件大小
            File file = new File(rsp.fileDir, rsp.fileName);
            if (file.exists() && file.length() == rsp.fileSize) {
                //文件已经下载好
                AppLogger.d("文件已经下载好");
                rsp.downloadState = JConstant.D.SUCCESS;
                PreferencesUtils.putString(JConstant.KEY_CLIENT_UPDATE_DESC, new Gson().toJson(rsp));
                RxBus.getCacheInstance().postSticky(new RxEvent.ApkDownload(rsp.fileDir + File.separator + rsp.fileName).setRsp(rsp)
                        .setUpdateType(RxEvent.UpdateType._8HOUR));
                return;
            }
            ClientUpdateManager.getInstance().downLoadFile(rsp, new ClientUpdateManager.DownloadListener() {
                @Override
                public void start(long totalByte) {

                }

                @Override
                public void failed(Throwable throwable) {

                }

                @Override
                public void finished(File file) {
                    RxBus.getCacheInstance().postSticky(new RxEvent.ApkDownload(file.getAbsolutePath()).setRsp(rsp));
                }

                @Override
                public void process(long currentByte, long totalByte) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private long getFileSizeFromUrl(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = new OkHttpClient().newCall(request).execute();
            return response.body().contentLength();
        } catch (IOException e) {
            return 0;
        }
    }
}