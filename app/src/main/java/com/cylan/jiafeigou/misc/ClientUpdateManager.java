package com.cylan.jiafeigou.misc;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.cylan.jiafeigou.IRemoteService;
import com.cylan.jiafeigou.IRemoteServiceCallback;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.engine.DownloadService;
import com.cylan.jiafeigou.n.mvp.model.UpdateFileBean;
import com.cylan.jiafeigou.n.view.misc.UpdateActivity;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;

import java.io.File;

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

    private ClientUpdateManager(){}

    public static ClientUpdateManager getInstance(){
        if (instance == null)
            synchronized (ClientUpdateManager.class) {
                if (instance == null)
                    instance = new ClientUpdateManager();
            }
        return instance;
    }

    public void startDownload(Context context,String url,String newVersion,int upgrade){
        //启动下载服务 test
        updateFileBean = new UpdateFileBean();
        updateFileBean.url = url;
        updateFileBean.fileName = newVersion;
        updateFileBean.version = "111111";
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //TODO　没有sd卡
            updateFileBean.savePath = context.getFilesDir().getAbsolutePath();
        } else {
            updateFileBean.savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
        }

        apkPath = "/mnt/sdcard/"+ updateFileBean.savePath+"/"+ updateFileBean.fileName+".apk";
        if (checkLocal(apkPath)){
            //直接传送APK地址
            ToastUtil.showPositiveToast("已下载");
            RxBus.getCacheInstance().postSticky(new RxEvent.ClientUpgrade(apkPath));
            return;
        }

        //仅wifi环境下升级
        if (NetUtils.getNetType(ContextUtils.getContext()) == 1){
            Intent intent = new Intent(context, DownloadService.class);
            intent.putExtra(DownloadService.KEY_PARCELABLE, updateFileBean);
            context.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
        }
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
//            createNotification();
//            cBuilder.setProgress(100, (int)percent, false);
//            sent();
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
//            cBuilder.setContentText("下载完成").setProgress(0, 0, false);
//            sent();
            //下载完成通知
            RxBus.getCacheInstance().postSticky(new RxEvent.ClientUpgrade(apkPath));
            realse(ContextUtils.getContext());
        }

        @Override
        public void onFailedReason(long taskId, int reason) throws RemoteException {
            Log.d("IRemoteServiceCallback", "onFailedReason:" + taskId);
        }
    };

    /**
     * 检测本地是否已经下载了apk
     * @return
     */
    public boolean checkLocal(String url){
        File file = new File(url);
        if (file.exists()){
            return true;
        }else {
            return false;
        }
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


    public void realse(Context context){
        try {
            if (mService != null){
                mService.unregisterCallback(mCallback);
                mService = null;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        context.unbindService(serviceConnection);
    }

}
