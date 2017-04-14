package com.cylan.jiafeigou.misc;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.cylan.jiafeigou.IRemoteService;
import com.cylan.jiafeigou.IRemoteServiceCallback;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.engine.DownloadService;
import com.cylan.jiafeigou.n.mvp.model.UpdateFileBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLConnection;

import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2017/4/1
 * 描述：
 */
public class ClientUpdateManager {

    private static ClientUpdateManager instance;
    private IRemoteService mService = null;

    private NotificationManager notificationManager;
    private Notification notification;
    private RemoteViews contentView;
    private NotificationCompat.Builder cBuilder;
    private NotificationManager nm;
    private UpdateFileBean updateFileBean;
    private String apkPath;
    private Subscription checkLocalSub;

    private ClientUpdateManager() {
    }

    public static ClientUpdateManager getInstance() {
        if (instance == null)
            synchronized (ClientUpdateManager.class) {
                if (instance == null)
                    instance = new ClientUpdateManager();
            }
        return instance;
    }

    public void startDownload(Context context, String url, String newVersion, int upgrade) {
        //启动下载服务 test
        updateFileBean = new UpdateFileBean();
        updateFileBean.url = url;
        updateFileBean.fileName = newVersion;
        updateFileBean.version = "111111";
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //没有sd卡
            updateFileBean.savePath = context.getFilesDir().getAbsolutePath();
        } else {
            updateFileBean.savePath = JConstant.MISC_PATH;
        }
        apkPath = JConstant.MISC_PATH + "/" + updateFileBean.fileName + ".apk";
        checkLocal(apkPath, url, context);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IRemoteService.Stub.asInterface(service);
            try {
                mService.registerCallback(mCallback);
            } catch (RemoteException e) {
                Log.d(this.getClass().getSimpleName(), "RemoteException: " + e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    private IRemoteServiceCallback mCallback = new IRemoteServiceCallback.Stub() {
        @Override
        public void onDownloadStarted(long taskId) throws RemoteException {
            Log.d("IRemoteServiceCallback", "onDownloadStarted:" + taskId);
        }

        @Override
        public void onDownloadPaused(long taskId) throws RemoteException {
            Log.d("IRemoteServiceCallback", "onDownloadPaused:" + taskId);
        }

        @Override
        public void onDownloadProcess(long taskId, double percent, long downloadedLength) throws RemoteException {
            Log.d("IRemoteServiceCallback", "onDownloadProcess:" + taskId + " percent:" + percent);
            // 改变通知栏
        }

        @Override
        public void onDownloadFinished(long taskId) throws RemoteException {
            Log.d("IRemoteServiceCallback", "onDownloadFinished:" + taskId);
        }

        @Override
        public void onDownloadRebuildStart(long taskId) throws RemoteException {
            Log.d("IRemoteServiceCallback", "onDownloadRebuildStart:" + taskId);
        }

        @Override
        public void onDownloadRebuildFinished(long taskId) throws RemoteException {
            Log.d("IRemoteServiceCallback", "onDownloadRebuildFinished:" + taskId);
            AppLogger.d("test_downF:下载完成了");
        }

        @Override
        public void onDownloadCompleted(long taskId) throws RemoteException {
            Log.d("IRemoteServiceCallback", "onDownloadCompleted:" + taskId);
            AppLogger.d("test_downC:下载完成了");
            release(ContextUtils.getContext());
        }

        @Override
        public void onFailedReason(long taskId, int reason) throws RemoteException {
            Log.d("IRemoteServiceCallback", "onFailedReason:" + taskId);
        }
    };

    /**
     * 检测本地是否已经下载了apk
     *
     * @return
     */
    public void checkLocal(String apkPath, String url, Context context) {
        checkLocalSub = Observable.just(url)
                .subscribeOn(Schedulers.newThread())
                .flatMap(new Func1<String, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(String urlPath) {
                        long length = 0;
                        try {
                            if (TextUtils.isEmpty(urlPath)) return Observable.just(false);
                            URL url = new URL(urlPath);
                            URLConnection conn = url.openConnection();//建立连接
                            String headerField = conn.getHeaderField(6);
                            length = conn.getContentLength();
                            AppLogger.d("file name:" + headerField);
                            AppLogger.d("file_length:" + length);
                            //先从本地获取看看是否已下载
                            File file = new File(apkPath);
                            AppLogger.d("local_url:" + file.getAbsolutePath());
                            AppLogger.d("file_length:" + getFileSize(file));
                            AppLogger.d("file_exit:" + file.exists());
                            if (file.exists()) {
                                //包是否完整
                                if (getFileSize(file) == length) {
                                    return Observable.just(true);
                                } else {
                                    boolean delete = file.delete();
                                    AppLogger.d("update_file_del:" + delete);
                                }
                            }
                            return Observable.just(false);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return Observable.just(false);
                        }
                    }
                })
                .subscribe(b -> {
                    if (b) {
                        AppLogger.d("已经瞎子?");
                    } else {
                        //仅wifi环境下升级
                        if (NetUtils.getNetType(ContextUtils.getContext()) == 1) {
                            Intent intent = new Intent(context, DownloadService.class);
                            intent.putExtra(DownloadService.KEY_PARCELABLE, updateFileBean);
                            context.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
                        }
                    }
                }, throwable -> {
                    AppLogger.e("checkLocal" + throwable.getLocalizedMessage());
                });
    }


    /**
     * 获取文件大小
     *
     * @param file
     * @return
     * @throws Exception
     */
    private long getFileSize(File file) {
        long size = 0;
        try {
            if (file.exists()) {
                FileInputStream fis = null;
                fis = new FileInputStream(file);
                size = fis.available();
                AppLogger.d("getF:" + size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 通知栏进度
     */
    public void createNotification() {
        // 获取系统服务来初始化对象
        nm = (NotificationManager) ContextUtils.getContext()
                .getSystemService(Activity.NOTIFICATION_SERVICE);
        cBuilder = new NotificationCompat.Builder(ContextUtils.getContext());
//        cBuilder.setContentIntent(pendingIntent);// 该通知要启动的Intent
        cBuilder.setSmallIcon(R.mipmap.ic_launcher);// 设置顶部状态栏的小图标
        cBuilder.setTicker("正在更新加菲狗");// 在顶部状态栏中的提示信息
        cBuilder.setContentTitle("加菲狗升级程序");// 设置通知中心的标题
        cBuilder.setContentText("正在下载中");// 设置通知中心中的内容
        cBuilder.setWhen(System.currentTimeMillis());
        cBuilder.setAutoCancel(true);
        cBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
    }

    /**
     * 发送通知
     */
    private void sent() {
        notification = cBuilder.build();
        // 发送该通知
        nm.notify(7, notification);
    }

    /**
     * 根据id清除通知
     */
    public void clear() {
        // 取消通知
        nm.cancelAll();
    }


    public void release(Context context) {
        try {
            if (mService != null) {
                mService.unregisterCallback(mCallback);
                mService = null;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        context.unbindService(serviceConnection);

        if (checkLocalSub != null && !checkLocalSub.isUnsubscribed()) {
            checkLocalSub.unsubscribe();
        }
    }

}
