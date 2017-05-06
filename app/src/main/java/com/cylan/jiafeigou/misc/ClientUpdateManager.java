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
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PackageUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

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
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

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


    private Map<String, DownloadListener> listenerHashMap = new HashMap<>();
    private Map<String, Action1<String>> actionMap = new HashMap<>();

    /**
     * 下载文件
     *
     * @param url         文件url
     * @param destFileDir 存储目标目录
     */
    public void downLoadFile(String url, String fileName, final String destFileDir, DownloadListener l) {
        if (listenerHashMap.get(fileName) != null) return;
        listenerHashMap.put(fileName, l);
        if (actionMap.get(fileName) != null) {
            listenerHashMap.put(fileName, l);
            return;
        }
        Action1<String> action1 = o -> {
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
                        DownloadListener listener = listenerHashMap.get(fileName);
                        if (listener != null) {
                            listener.finished(file);
                        }
                        listenerHashMap.remove(fileName);
                        actionMap.remove(fileName);
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
                    DownloadListener listener = listenerHashMap.get(fileName);
                    try {
                        long total = response.body().contentLength();
                        if (listener != null) listener.start(total);
                        Log.d(TAG, "total------>" + total);
                        long current = 0;
                        is = response.body().byteStream();
                        fos = new FileOutputStream(file);
                        while ((len = is.read(buf)) != -1) {
                            current += len;
                            fos.write(buf, 0, len);
                            Log.d(TAG, "current------>" + current);
                            listener = listenerHashMap.get(fileName);
                            if (listener != null) listener.process(current, total);
                        }
                        fos.flush();
                        listener = listenerHashMap.get(fileName);
                        if (listener != null)
                            listener.finished(new File(destFileDir, fileName));
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                        listener = listenerHashMap.get(fileName);
                        if (listener != null)
                            listener.failed(e);
                        actionMap.remove(fileName);
                        listenerHashMap.remove(fileName);
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
        };
        actionMap.put(fileName, action1);
        Observable.just("go").
                subscribeOn(Schedulers.newThread())
                .subscribe(action1, AppLogger::e);
    }

    /**
     * 下载文件
     */
    public void downLoadFile(RxEvent.CheckDevVersionRsp rsp, DownloadListener l) {
        Log.d(TAG, "开始下载: " + rsp);
        if (rsp == null) return;
        if (listenerHashMap.get(rsp.fileName) != null) return;
        listenerHashMap.put(rsp.fileName, l);
        if (actionMap.get(rsp.fileName) != null) {
            listenerHashMap.put(rsp.fileName, l);
            return;
        }
        Action1<String> action1 = o -> {
            final File file = new File(rsp.fileDir, rsp.fileName);
            new File(rsp.fileDir).mkdir();
            if (file.exists()) {
                try {
                    Request request = new Request.Builder()
                            .url(rsp.url)
                            .build();
                    Response response = new OkHttpClient().newCall(request).execute();
                    long fileSize = response.body().contentLength();
                    AppLogger.d("文件大小:" + fileSize);
                    if (fileSize == file.length()) {
                        AppLogger.d("文件存在,完整");
                        DownloadListener listener = listenerHashMap.get(rsp.fileName);
                        if (listener != null) {
                            listener.finished(file);
                            rsp.downloadState = JConstant.D.SUCCESS;
                            rsp.lastUpdateTime = System.currentTimeMillis();
                            updateInfo(rsp.uuid, rsp);
                        }
                        listenerHashMap.remove(rsp.fileName);
                        actionMap.remove(rsp.fileName);
                        return;
                    }
                } catch (IOException e) {
                    AppLogger.e("err:" + MiscUtils.getErr(e));
                }
            }
            //文件失败了
            FileUtils.delete(JConstant.MISC_PATH, rsp.fileDir + File.separator + rsp.fileName);
            final Request request = new Request.Builder().url(rsp.url).build();
            final Call call = new OkHttpClient().newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, e.toString());
                    FileUtils.delete(JConstant.MISC_PATH, rsp.fileDir + File.separator + rsp.fileName);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    InputStream is = null;
                    byte[] buf = new byte[2048];
                    int len = 0;
                    FileOutputStream fos = null;
                    DownloadListener listener = listenerHashMap.get(rsp.fileName);
                    try {
                        long total = response.body().contentLength();
                        if (listener != null) {
                            listener.start(total);
                            rsp.downloadState = JConstant.D.DOWNLOADING;
                            rsp.lastUpdateTime = System.currentTimeMillis();
                            updateInfo(rsp.uuid, rsp);
                        }
                        Log.d(TAG, "total------>" + total);
                        long current = 0;
                        is = response.body().byteStream();
                        fos = new FileOutputStream(file);
                        while ((len = is.read(buf)) != -1) {
                            current += len;
                            fos.write(buf, 0, len);
                            Log.d(TAG, "current------>" + current);
                            listener = listenerHashMap.get(rsp.fileName);
                            if (listener != null) {
                                listener.process(current, total);
                                rsp.downloadState = JConstant.D.DOWNLOADING;
                                rsp.lastUpdateTime = System.currentTimeMillis();
                                updateInfo(rsp.uuid, rsp);
                            }
                        }
                        fos.flush();
                        listener = listenerHashMap.get(rsp.fileName);
                        if (listener != null) {
                            listener.finished(new File(rsp.fileDir, rsp.fileName));
                            rsp.downloadState = JConstant.D.SUCCESS;
                            rsp.lastUpdateTime = System.currentTimeMillis();
                            updateInfo(rsp.uuid, rsp);
                        }
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                        listener = listenerHashMap.get(rsp.fileName);
                        if (listener != null) {
                            listener.failed(e);
                            rsp.downloadState = JConstant.D.FAILED;
                            rsp.lastUpdateTime = System.currentTimeMillis();
                            updateInfo(rsp.uuid, rsp);
                        }
                        actionMap.remove(rsp.fileName);
                        listenerHashMap.remove(rsp.fileName);
                        FileUtils.delete(JConstant.MISC_PATH, rsp.fileDir + File.separator + rsp.fileName);
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
        };
        actionMap.put(rsp.fileName, action1);
        Observable.just("go").
                subscribeOn(Schedulers.newThread())
                .subscribe(action1, AppLogger::e);
    }

    private Gson gson = new Gson();

    private void updateInfo(String uuid, RxEvent.CheckDevVersionRsp checkDevVersionRsp) {
        PreferencesUtils.putString(JConstant.KEY_FIRMWARE_CONTENT + uuid, gson.toJson(checkDevVersionRsp));
    }

    public interface DownloadListener {

        void start(long totalByte);

        void failed(Throwable throwable);

        void finished(File file);

        void process(long currentByte, long totalByte);
    }
}
