package com.cylan.jiafeigou.misc;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.cylan.jiafeigou.IRemoteService;
import com.cylan.jiafeigou.IRemoteServiceCallback;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PackageUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2017/4/1
 * 描述：
 */
public class ClientUpdateManager {
    private static final String TAG = "ClientUpdateManager";
    private static ClientUpdateManager instance;
    private IRemoteService mService = null;

    private NotificationManager notificationManager;
    private Notification notification;
    private RemoteViews contentView;
    private NotificationCompat.Builder cBuilder;
    private NotificationManager nm;
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
            try {
                mService.registerCallback(mCallback);
            } catch (RemoteException e) {
                Log.d(this.getClass().getSimpleName(), "RemoteException: " + e);
            }
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


    /**
     * @param url
     * @param versionName
     * @param versionCode:
     */
    public void enqueue(String url, String versionName, String versionCode, DownloadListener downloadListener) {
        try {
            int currentVersionCode = PackageUtils.getAppVersionCode(ContextUtils.getContext());
            int remoteVersionCode = Integer.parseInt(versionCode);
            if (currentVersionCode >= remoteVersionCode) {
                AppLogger.d("不需要升级");
                return;
            }
        } catch (Exception e) {
            AppLogger.e("err:" + MiscUtils.getErr(e));
        }
        downLoadFile(url, versionName + ".apk", JConstant.MISC_PATH, downloadListener);
    }


    private Map<String, DownloadListener> map = new HashMap<>();

    /**
     * 下载文件
     *
     * @param url         文件url
     * @param destFileDir 存储目标目录
     */
    public void downLoadFile(String url, String fileName, final String destFileDir, DownloadListener downloadListener) {

        

        final File file = new File(destFileDir, fileName);
        new File(destFileDir).mkdir();
        if (file.exists()) {
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Response response = new OkHttpClient().newCall(request).execute();
                long fileSize = response.body().contentLength();
                AppLogger.d("文件大小:" + fileSize);
                if (fileSize == file.length()) {
                    AppLogger.d("文件存在,完整");
                    if (downloadListener != null) downloadListener.finished(file);
                    return;
                }
                FileUtils.delete(JConstant.MISC_PATH, destFileDir + File.separator + fileName);
            } catch (IOException e) {
                AppLogger.e("err:" + MiscUtils.getErr(e));
            }
        }
        final Request request = new Request.Builder().url(url).build();
        final Call call = new OkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, e.toString());
                FileUtils.delete(JConstant.MISC_PATH, destFileDir + File.separator + fileName);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                if (downloadListener != null) downloadListener.start();
                try {
                    long total = response.body().contentLength();
                    Log.d(TAG, "total------>" + total);
                    long current = 0;
                    is = response.body().byteStream();
                    fos = new FileOutputStream(file);
                    while ((len = is.read(buf)) != -1) {
                        current += len;
                        fos.write(buf, 0, len);
                        Log.d(TAG, "current------>" + current);
                        if (downloadListener != null) downloadListener.process(current, total);
                    }
                    fos.flush();
                    if (downloadListener != null)
                        downloadListener.finished(new File(destFileDir, fileName));
                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                    if (downloadListener != null)
                        downloadListener.failed(e);
                    FileUtils.delete(JConstant.MISC_PATH, destFileDir + File.separator + fileName);
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        Log.d(TAG, e.toString());
                    }
                }
            }
        });
    }

    public interface DownloadListener {

        void start();

        void failed(Throwable throwable);

        void finished(File file);

        void process(long currentByte, long totalByte);
    }
}
