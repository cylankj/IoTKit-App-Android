//
//                                     ******
//                                    ******
//                                   ******  *
//     ***********  *****    @***** ******  **************   ***** **********
//    *************  *****  ****** ****** ****************  *****************
//   *************** ****** *****  *****            ******  *****      ******
//   *****            **********  ******  ********  *****  *****       ******
//   *****             ********* ******  ********** ***** ******      ******
//  *****              ******** ******  ***************** ******      *****   *
//  ******************  ****** ******  ***************** ******      ************
//  *****************   *****  *****   ****************  *****      ****** ******
//   ***************   *****  *****     *************** ******      ******  ****
//                    ******
//                  *******
//
package com.cylan.jiafeigou.n.mvp.impl.home;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.AutoSignIn;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.NotifyManager;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 作者：
 * 创建时间：2016/9/7
 * 描述：
 */
public class HomeSettingPresenterImp extends AbstractPresenter<HomeSettingContract.View>
        implements HomeSettingContract.Presenter {

    private boolean isCheck;
    private JFGAccount userInfo;
    private boolean isOpenLogin;

    public HomeSettingPresenterImp(HomeSettingContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{getAccountInfo()};
    }

    /**
     * 退出登录
     */
    @Override
    public void logOut(String account) {
        BaseApplication.getAppComponent().getSourceManager().logout()
                .subscribeOn(Schedulers.io())
                .subscribe(retAccount -> {
                    BaseApplication.getAppComponent().getCmd().logout();
                    AutoSignIn.getInstance().autoLogout();
                    NotifyManager.getNotifyManager().clearAll();
                    RxBus.getCacheInstance().removeAllStickyEvents();
                    AutoSignIn.getInstance().clearPsw();
                    //emit failed event.
                    //是三方登录获取绑定的手机或者邮箱用于登录页回显
                    if (isOpenLogin) {
                        PreferencesUtils.putString(JConstant.THIRD_RE_SHOW, TextUtils.isEmpty(retAccount.getPhone()) ? (TextUtils.isEmpty(retAccount.getEmail()) ? "" : retAccount.getEmail()) : retAccount.getPhone());
                    }
                    RxBus.getCacheInstance().postSticky(new RxEvent.ResultLogin(JError.ErrorLoginInvalidPass));
                }, AppLogger::e);
    }


    @Override
    public void clearCache() {
        getView().showClearingCacheProgress();
        addSubscription(Observable.just(null)
                .subscribeOn(Schedulers.io())
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
    public String calculateCacheSize() {
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
                        .subscribeOn(Schedulers.io())
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


    private static final int BYTE = 1024;
    private static final int MEGA_BYTE = 1024 * 1024;
    private static final int GIGA_BYTE = 1024 * 1024 * 1024;

    /**
     * desc:转换文件的大小
     *
     * @param byteData
     * @return
     */
    public String FormatFileSize(long byteData) {
        DecimalFormat df = new DecimalFormat("0.0");
        String fileSizeString = "";
        if (byteData < BYTE)
            return byteData + "KB";
        if (byteData >= BYTE && byteData < MEGA_BYTE) {
            return df.format((double) (byteData >>> 10)) + "M";
        }
        if (byteData >= MEGA_BYTE && byteData < GIGA_BYTE) {
            return df.format((double) (byteData >>> 20)) + "G";
        }
        if (byteData >= GIGA_BYTE && byteData < 1024L * GIGA_BYTE) {
            return df.format((double) (byteData >>> 30)) + "T";
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

}
