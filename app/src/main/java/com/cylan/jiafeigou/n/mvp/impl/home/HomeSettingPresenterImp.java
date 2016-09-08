package com.cylan.jiafeigou.n.mvp.impl.home;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.n.mvp.contract.home.HomeSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.sina.weibo.sdk.utils.LogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/7
 * 描述：
 */
public class HomeSettingPresenterImp extends AbstractPresenter<HomeSettingContract.View> implements HomeSettingContract.Presenter {

    private Subscription calculateSubscription;
    private Subscription clearCacheSubscription;

    public HomeSettingPresenterImp(HomeSettingContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void clearCache() {
        getView().showClearingCacheProgress();
        clearCacheSubscription = Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .map(new Func1<Object, Object>() {
                    @Override
                    public Object call(Object o) {
                        File directory = getCacheDirectory(getView().getContext(),"");
                        if (directory != null && directory.exists() && directory.isDirectory()) {
                            for (File item : directory.listFiles()) {
                                item.delete();
                            }
                        }
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        getView().hideClearingCacheProgress();
                        File directory = getCacheDirectory(getView().getContext(),"");
                        if(directory.getTotalSpace() == directory.getFreeSpace()){
                            getView().clearNoCache();
                        }else {
                            getView().clearFinish();
                        }
                    }
                });

    }

    @Override
    public void calculateCacheSize() {

        calculateSubscription = Observable.just(null)
                .subscribeOn(Schedulers.computation())
                .map(new Func1<Object, String>() {
                    @Override
                    public String call(Object o) {

                        getView().showLoadCacheSizeProgress();
                        long cacheSize = 0l;
                        File directory = getCacheDirectory(getView().getContext(),"");
                        if(directory.exists()){
                            try {
                                cacheSize = getFolderSize(directory);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else {
                            cacheSize = 0l;
                        }
                        return FormetFileSize(cacheSize);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String size) {
                        getView().hideLoadCacheSizeProgress();
                        getView().setCacheSize(size);
                    }
                });
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        if(calculateSubscription != null){
            calculateSubscription.unsubscribe();
        }
        if(clearCacheSubscription != null){
            clearCacheSubscription.unsubscribe();
        }
    }

    /**
     * desc:转换文件的大小
     * @param fileS
     * @return
     */
    public String FormetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if(fileS == 0){
            fileSizeString = "0.0MB";
        } else if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    /**
     * desc：获取应用专属缓存目录
     * @param context 上下文
     * @param type 文件夹类型 可以为空，为空则返回API得到的一级目录
     * @return 缓存文件夹 如果没有SD卡或SD卡有问题则返回内存缓存目录，否则优先返回SD卡缓存目录
     */
    public static File getCacheDirectory(Context context, String type) {
        File appCacheDir = getInternalCacheDirectory(context,type);
        if (appCacheDir == null){
            appCacheDir = getExternalCacheDirectory(context,type);
        }

        if (appCacheDir == null){
            Log.e("getCacheDirectory","getCacheDirectory fail ,the reason is mobile phone unknown exception !");
        }else {
            if (!appCacheDir.exists()&&!appCacheDir.mkdirs()){
                Log.e("getCacheDirectory","getCacheDirectory fail ,the reason is make directory fail !");
            }
        }
        return appCacheDir;
    }

    /**
     * desc:获取到外部缓存路径
     * @param context
     * @param type
     * @return
     */
    public static File getExternalCacheDirectory(Context context,String type) {
        File appCacheDir = null;
        if( Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            if (TextUtils.isEmpty(type)){
                appCacheDir = context.getExternalCacheDir();
            }else {
                appCacheDir = context.getExternalFilesDir(type);
            }

            if (appCacheDir == null){// 有些手机需要通过自定义目录
                appCacheDir = new File(Environment.getExternalStorageDirectory(),"Android/data/"+context.getPackageName()+"/cache/"+type);
            }

            if (appCacheDir == null){
                Log.e("getExternalDirectory","getExternalDirectory fail ,the reason is sdCard unknown exception !");
            }else {
                if (!appCacheDir.exists()&&!appCacheDir.mkdirs()){
                    Log.e("getExternalDirectory","getExternalDirectory fail ,the reason is make directory fail !");
                }
            }
        }else {
            Log.e("getExternalDirectory","getExternalDirectory fail ,the reason is sdCard nonexistence or sdCard mount fail !");
        }
        return appCacheDir;
    }

    /**
     * desc:获取到内存缓存路径
     * @param context
     * @param type
     * @return
     */
    public static File getInternalCacheDirectory(Context context,String type) {
        File appCacheDir = null;
        if (TextUtils.isEmpty(type)){
            appCacheDir = context.getCacheDir();// /data/data/app_package_name/cache
        }else {
            appCacheDir = new File(context.getFilesDir(),type);// /data/data/app_package_name/files/type
        }

        if (!appCacheDir.exists()&&!appCacheDir.mkdirs()){
            Log.e("getInternalDirectory","getInternalDirectory fail ,the reason is make directory fail !");
        }
        return appCacheDir;
    }

    /**
     * desc:计算缓存的大小
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
