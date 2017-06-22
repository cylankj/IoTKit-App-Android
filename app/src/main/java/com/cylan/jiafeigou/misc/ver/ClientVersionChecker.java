package com.cylan.jiafeigou.misc.ver;

import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JConstant;
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
import com.lzy.okgo.OkGo;
import com.lzy.okgo.request.BaseRequest;
import com.lzy.okserver.download.DownloadInfo;
import com.lzy.okserver.download.DownloadManager;
import com.lzy.okserver.download.db.DownloadDBManager;
import com.lzy.okserver.listener.DownloadListener;

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
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.ver.ClientVersionChecker.GooglePlayCheckResult.NEW_VERSION;
import static com.cylan.jiafeigou.misc.ver.ClientVersionChecker.GooglePlayCheckResult.NO_VERSION;

/**
 * Created by hds on 17-5-28.
 */

public class ClientVersionChecker implements IVersion<ClientVersionChecker.CVersion> {

    @Override
    public boolean checkCondition() {
        //当前网络不行
        if (NetUtils.getJfgNetType() == 0) return false;
        int netType = NetUtils.getJfgNetType(ContextUtils.getContext());
        if (netType != 1)//wifi
            return false;
        return true;
    }

    @Override
    public void startCheck() {
        if (checkCondition())
            checkVersion();
    }

    @Override
    public void finalShow() {
    }

    public static final class CVersion extends IVersion.BaseVersion {

        private long fileSize;
        public int forceUpdate;
        public
        @RxEvent.UpdateType
        int updateType;

        public void setUpdateType(@RxEvent.UpdateType int updateType) {
            this.updateType = updateType;
        }

        public
        @RxEvent.UpdateType
        int getUpdateType() {
            return updateType;
        }

        public void setForceUpdate(int forceUpdate) {
            this.forceUpdate = forceUpdate;
        }

        public int getForceUpdate() {
            return forceUpdate;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
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
                    if (TextUtils.isEmpty(gVersion)) gVersion = "1.0.0";
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
        if (MiscUtils.isGooglePlayServiceAvailable()) {
            //google play 可用 只走google play
            checkVersionFromGooglePlay()
                    .subscribeOn(Schedulers.newThread())
                    .filter(ret -> ret != null)
                    .doOnError(ret -> checkVersionFrom8Hours())
                    .subscribe(ret -> {
                        AppLogger.d("google play检查版本结果?" + ret);
                        if (ret.ret == NEW_VERSION) {
                            RxBus.getCacheInstance().postSticky(new RxEvent.ApkDownload("")
                                    .setUpdateType(RxEvent.UpdateType.GOOGLE_PLAY));
                        } else {
                            checkVersionFrom8Hours();
                        }
                    }, AppLogger::e);
        } else {
            checkVersionFrom8Hours();
        }
    }

    private void checkVersionFrom8Hours() {
        AppLogger.d("走8小时");
        checkVersionFrom8Hour().subscribeOn(Schedulers.newThread())
                .subscribe(ret -> {
                }, throwable -> {//让整条订阅连结束
                    if (throwable instanceof RxEvent.HelperBreaker) {
                        if (((RxEvent.HelperBreaker) throwable).object != null && ((RxEvent.HelperBreaker) throwable).object instanceof RxEvent.ClientCheckVersion)
                            checkRsp((RxEvent.ClientCheckVersion) ((RxEvent.HelperBreaker) throwable).object);
                    }
                });
    }

    private void checkRsp(final RxEvent.ClientCheckVersion clientCheckVersion) {
        AppLogger.d("check_version result: " + clientCheckVersion);
//                                    clientCheckVersion.result = "VRJz6f";
//                                    2iYjQr
        if (TextUtils.isEmpty(clientCheckVersion.result))
            return;
        final String result = clientCheckVersion.result.replace("http://yun.app8h.com/s?id=", "");
        final String finalUrl = JConstant.assembleUrl(result, ContextUtils.getContext().getPackageName());
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

            final String result = response.body().string();
            AppLogger.d("check_version result: " + result);
            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject.has("ret") && jsonObject.getInt("ret") != 0) return;
            final String url = jsonObject.getString("url");
            final String versionName = jsonObject.getString("version");
            final String shortVersion = jsonObject.getString("shortversion");
            final String desc = jsonObject.getString("desc");
            final int forceUpate = clientCheckVersion.forceUpgrade;
            CVersion cVersion = new CVersion();
            cVersion.setDesc(desc);
            cVersion.setFileName(versionName + ".apk");
            cVersion.setSaveDir(JConstant.MISC_PATH);
            cVersion.setUrl(url);
            cVersion.setFileSize(response.body().contentLength());
            cVersion.setVersionCode(Integer.parseInt(shortVersion));
            int currentAppVersionCode = PackageUtils.getAppVersionCode(ContextUtils.getContext());
            try {
                //1.版本检测
                if (currentAppVersionCode >= cVersion.getVersionCode()) {
                    AppLogger.d("本地版本较高不需要升级");
                    PreferencesUtils.remove(JConstant.KEY_CLIENT_UPDATE_DESC);
                    FileUtils.deleteFile(JConstant.MISC_PATH + File.separator + versionName + ".apk");
                    return;
                }
            } catch (Exception e) {
            }
            //2.文件大小
            File file = new File(JConstant.MISC_PATH, versionName + ".apk");
            if (file.exists() && file.length() == response.body().contentLength()) {
                //文件已经下载好
                AppLogger.d("文件已经下载好");
                PreferencesUtils.putString(JConstant.KEY_CLIENT_UPDATE_DESC, new Gson().toJson(cVersion));
                RxBus.getCacheInstance().postSticky(new RxEvent.ApkDownload(cVersion.getSaveDir() + File.separator + cVersion.getFileName())
                        .setForceUpdate(forceUpate)
                        .setUpdateType(RxEvent.UpdateType._8HOUR));
                return;
            }
            if (subscription != null) subscription.unsubscribe();
            subscription = Observable.just(url)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Task(forceUpate, cVersion), AppLogger::e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int writeHelper;
    private Subscription subscription;

    private class Task implements Action1<String> {
        private int forceUpdate;
        private CVersion cVersion;

        public Task(int forceUpdate, CVersion version) {
            this.forceUpdate = forceUpdate;
            this.cVersion = version;
        }

        @Override
        public void call(String url) {
            writeHelper = 0;
            new File(cVersion.getSaveDir()).mkdirs();
            BaseRequest request = OkGo.get(url);
            DownloadInfo info = DownloadManager.getInstance().getDownloadInfo(url);
            final DownloadListener listener = new DownloadListener() {
                @Override
                public void onProgress(DownloadInfo downloadInfo) {
                    writeHelper++;
                    if (writeHelper % 5 == 0) AppLogger.d("下载进度?" + downloadInfo.getProgress());
                }

                @Override
                public void onFinish(DownloadInfo downloadInfo) {
                    AppLogger.d("下载进度,完成:" + downloadInfo.getTargetPath());
                    RxBus.getCacheInstance().postSticky(new RxEvent.ApkDownload(cVersion.getSaveDir() + File.separator + cVersion.getFileName())
                            .setForceUpdate(forceUpdate)
                            .setUpdateType(RxEvent.UpdateType._8HOUR));
                }

                @Override
                public void onError(DownloadInfo downloadInfo, String s, Exception e) {
                    AppLogger.d("下载进度,失败了:" + MiscUtils.getErr(e));
                }
            };
            if (info != null) {
                info.setListener(listener);
                DownloadDBManager.INSTANCE.delete(url);
                DownloadManager.getInstance().removeTask(url);
            }
            DownloadManager.getInstance().setTargetFolder(JConstant.UPDATE_FILE_PATH);
            DownloadManager.getInstance().addTask(url, request, listener);
        }
    }
}
