package com.cylan.jiafeigou.n.mvp.impl.home;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.google.gson.Gson;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/7
 * 描述：
 */
public class HomeSettingPresenterImp extends AbstractPresenter<HomeSettingContract.View>
        implements HomeSettingContract.Presenter {

    private boolean isCheck;
    private JFGAccount userInfo;

    public HomeSettingPresenterImp(HomeSettingContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{getAccountInfo()};
    }

    @Override
    public void clearCache() {
        getView().showClearingCacheProgress();
        addSubscription(Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .map(o -> {
                    File directory = getCacheDirectory(ContextUtils.getContext(), "");
                    deleteCacheFile(directory);
                    return null;
                })
                .delay(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    getView().hideClearingCacheProgress();
                    File directory = getCacheDirectory(ContextUtils.getContext(), "");
                    if (directory.getTotalSpace() == directory.getFreeSpace()) {
                        getView().clearNoCache();
                    } else {
                        getView().clearFinish();
                    }
                }, AppLogger::e), "clearCache");
    }

    /**
     * 删除所有的缓存文件
     *
     * @param directory
     */
    private void deleteCacheFile(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                if (item.isDirectory()) {
                    deleteCacheFile(item);
                } else {
                    item.delete();
                }
            }
        }
    }

    @Override
    public void calculateCacheSize() {
        if (getView() != null) {
            getView().showLoadCacheSizeProgress();
        }
        addSubscription(rx.Observable.just(null)
                .subscribeOn(Schedulers.io())
                .map(o -> {
                    long cacheSize = 0l;
                    //getContent 更换
                    File directory = getCacheDirectory(ContextUtils.getContext(), "");
                    if (directory.exists()) {
                        try {
                            cacheSize = getFolderSize(directory);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        cacheSize = 0l;
                    }
                    return FormatFileSize(cacheSize);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(size -> {
                    getView().hideLoadCacheSizeProgress();
                    getView().setCacheSize(size);
                }, AppLogger::e), "calculateCacheSize");
    }

    @Override
    public boolean getNegation() {
        isCheck = !isCheck;
        return isCheck;
    }

    /**
     * 更改状态
     *
     * @param isChick
     * @param key
     */
    @Override
    public void savaSwitchState(boolean isChick, final String key) {
        addSubscription(rx.Observable.just(isChick)
                        .subscribeOn(Schedulers.newThread())
                        .subscribe(aBoolean -> chooseWhichSet(aBoolean, key), AppLogger::e),
                "savaSwitchState");
    }

    /**
     * 选择哪个开关
     *
     * @param aBoolean
     * @param key
     */
    private void chooseWhichSet(Boolean aBoolean, String key) {
        switch (key) {
            case JConstant.RECEIVE_MESSAGE_NOTIFICATION:
                userInfo.resetFlag();
                userInfo.setEnablePush(aBoolean);
                try {
                    BaseApplication.getAppComponent().getCmd().setAccount(userInfo);
                    BaseApplication.getAppComponent().getSourceManager().setJfgAccount(userInfo);
                } catch (JfgException e) {
                    e.printStackTrace();
                }
                break;

            case JConstant.OPEN_VOICE:
                userInfo.resetFlag();
                userInfo.setEnableSound(aBoolean);
                try {
                    BaseApplication.getAppComponent().getCmd().setAccount(userInfo);
                } catch (JfgException e) {
                    e.printStackTrace();
                }
                break;

            case JConstant.OPEN_SHAKE:
                userInfo.resetFlag();
                userInfo.setEnableVibrate(aBoolean);
                try {
                    BaseApplication.getAppComponent().getCmd().setAccount(userInfo);
                } catch (JfgException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * 获取到用户信息 设置通知等的开启和关闭
     *
     * @return
     */
    @Override
    public Subscription getAccountInfo() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getUserInfo -> {
                    if (getUserInfo != null && getView() != null) {
                        getView().initSwitchState(getUserInfo);
                        userInfo = getUserInfo.jfgAccount;
                    }
                }, AppLogger::e);
    }


    /**
     * desc:转换文件的大小
     *
     * @param fileS
     * @return
     */
    public String FormatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("0.0");
        String fileSizeString = "";
        if (fileS == 0) {
            fileSizeString = "0.0M";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    /**
     * desc：获取应用专属缓存目录
     *
     * @param context 上下文
     * @param type    文件夹类型 可以为空，为空则返回API得到的一级目录
     * @return 缓存文件夹 如果没有SD卡或SD卡有问题则返回内存缓存目录，否则优先返回SD卡缓存目录
     */
    public static File getCacheDirectory(Context context, String type) {
        File appCacheDir = getInternalCacheDirectory(context, type);
        if (appCacheDir == null) {
            appCacheDir = getExternalCacheDirectory(context, type);
        }

        if (appCacheDir == null) {
            Log.e("getCacheDirectory", "getCacheDirectory fail ,the reason is mobile phone unknown exception !");
        } else {
            if (!appCacheDir.exists() && !appCacheDir.mkdirs()) {
                Log.e("getCacheDirectory", "getCacheDirectory fail ,the reason is make directory fail !");
            }
        }
        return appCacheDir;
    }

    /**
     * desc:获取到外部缓存路径
     *
     * @param context
     * @param type
     * @return
     */
    public static File getExternalCacheDirectory(Context context, String type) {
        File appCacheDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            if (TextUtils.isEmpty(type)) {
                appCacheDir = context.getExternalCacheDir();
            } else {
                appCacheDir = context.getExternalFilesDir(type);
            }

            if (appCacheDir == null) {// 有些手机需要通过自定义目录
                appCacheDir = new File(Environment.getExternalStorageDirectory(), "Android/data/" + context.getPackageName() + "/cache/" + type);
            }

            if (appCacheDir == null) {
                Log.e("getExternalDirectory", "getExternalDirectory fail ,the reason is sdCard unknown exception !");
            } else {
                if (!appCacheDir.exists() && !appCacheDir.mkdirs()) {
                    Log.e("getExternalDirectory", "getExternalDirectory fail ,the reason is make directory fail !");
                }
            }
        } else {
            Log.e("getExternalDirectory", "getExternalDirectory fail ,the reason is sdCard nonexistence or sdCard mount fail !");
        }
        return appCacheDir;
    }

    /**
     * desc:获取到内存缓存路径
     *
     * @param context
     * @param type
     * @return
     */
    public static File getInternalCacheDirectory(Context context, String type) {
        File appCacheDir = null;
        if (TextUtils.isEmpty(type)) {
            appCacheDir = context.getCacheDir();// /data/data/app_package_name/cache
        } else {
            appCacheDir = new File(context.getFilesDir(), type);// /data/data/app_package_name/files/type
        }

        if (!appCacheDir.exists() && !appCacheDir.mkdirs()) {
            Log.e("getInternalDirectory", "getInternalDirectory fail ,the reason is make directory fail !");
        }
        return appCacheDir;
    }

    /**
     * desc:计算缓存的大小
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static long getFolderSize(File file) throws Exception {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                // 如果下面还有文件
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    @Override
    public void refreshWechat() {
        getOpenID();
    }

    private boolean access;

    private void getOpenID() {
        if (access) return;
        access = true;
        UMShareAPI.get(mView.getContext())
                .getPlatformInfo((Activity) mView.getContext(), SHARE_MEDIA.WEIXIN, new UMAuthListener() {
                    @Override
                    public void onStart(SHARE_MEDIA share_media) {
                        Log.d("getOpenID", "onStart: ");
                    }

                    @Override
                    public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
                        Log.d("getOpenID", "getOpenID: " + new Gson().toJson(map));
                    }

                    @Override
                    public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                        Log.d("getOpenID", "onError: " + throwable);
                    }

                    @Override
                    public void onCancel(SHARE_MEDIA share_media, int i) {
                        Log.d("getOpenID", "onCancel: ");
                    }
                });
//        // 通过WXAPIFactory工厂，获取IWXAPI的实例
//        IWXAPI api = WXAPIFactory.createWXAPI(ContextUtils.getContext(), "wx3081bcdae8a842cf", true);
//        api.handleIntent(getIntent(), this);
//        // 将该app注册到微信
//        api.registerApp("wx3081bcdae8a842cf");
//
//        //d93676ab7db1876c06800dee3f33fbc2
//        //wx3081bcdae8a842cf
//        String appId = PackageUtils.getMetaString(ContextUtils.getContext(), "weChatAppKey");
//        String appSecret = PackageUtils.getMetaString(ContextUtils.getContext(), "weChatAppSecret");
//        // APP_ID和APP_Secret在微信开发平台添加应用的时候会生成，grant_type 用默认的"authorization_code"即可.
//        String urlStr = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" +
//                appId + "&secret=" + "d93676ab7db1876c06800dee3f33fbc2" +
//                "&code=" + code + "&grant_type=authorization_code";
//
//        Request request = new Request.Builder()
//                .url(urlStr)
//                .build();
//        try {
//            Response response = new OkHttpClient().newCall(request).execute();
//            response.body().contentLength();
//        } catch (IOException e) {
//        }
    }
}
