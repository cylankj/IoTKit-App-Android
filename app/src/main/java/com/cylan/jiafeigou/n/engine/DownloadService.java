package com.cylan.jiafeigou.n.engine;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.cylan.jiafeigou.IRemoteService;
import com.cylan.jiafeigou.IRemoteServiceCallback;
import com.cylan.jiafeigou.n.mvp.model.UpdateFileBean;
import com.cylan.jiafeigou.support.download.core.DownloadManagerPro;
import com.cylan.jiafeigou.support.download.net.NetConfig;
import com.cylan.jiafeigou.support.download.report.listener.DownloadManagerListener;
import com.cylan.jiafeigou.support.download.report.listener.FailReason;
import com.cylan.jiafeigou.support.log.AppLogger;

/**
 * 这个Service跑在一个独立的进程。 ：download
 */
public class DownloadService extends Service implements DownloadManagerListener {

    public static final String KEY_PARCELABLE = "key_parcel";
    private final static String url = "http://le-apk.wdjcdn.com/7/24/1703183cbee0b57a38079d004d72f247.apk";
    private DownloadManagerPro.TaskBuilder taskBuilder;

    //callback 集合
    private RemoteCallbackList<IRemoteServiceCallback> iRemoteServiceCallBackList = new RemoteCallbackList<>();

    private IRemoteService.Stub mBinder = new IRemoteService.Stub() {
        public void registerCallback(IRemoteServiceCallback cb) {
            if (cb != null) iRemoteServiceCallBackList.register(cb);
        }

        public void unregisterCallback(IRemoteServiceCallback cb) {
            if (cb != null) iRemoteServiceCallBackList.unregister(cb);
        }
    };

    public DownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d("DownloadService", "DownloadService: " + rootIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Parcelable parcelable = intent.getParcelableExtra("KEY_PARCELABLE");
        if (parcelable != null && parcelable instanceof UpdateFileBean) {
            UpdateFileBean bean = (UpdateFileBean) parcelable;
            initSomething(bean);
            AppLogger.d("DownloadService: " + bean.toString());
        }
        AppLogger.d("DownloadService: " + parcelable);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        iRemoteServiceCallBackList.kill();
    }

    private void initSomething(final UpdateFileBean bean) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DownloadManagerPro.Config config = new DownloadManagerPro.Config()
                        .setContext(getApplicationContext());
                DownloadManagerPro.getInstance().init(config);
                taskBuilder = new DownloadManagerPro.TaskBuilder();
                taskBuilder.setUrl(url)
                        .setMaxChunks(4)
                        .setSaveName(bean.fileName)
                        .setOverwrite(true)
                        .setSdCardFolderAddress(bean.savePath)
                        .setDownloadManagerListener(DownloadService.this)
                        .setAllowNetType(NetConfig.TYPE_ALL);
                int token = DownloadManagerPro.getInstance().initTask(taskBuilder);
                DownloadManagerPro.getInstance().startDownload(token);
            }
        }).start();
    }

    @Override
    public void onDownloadStarted(long taskId) {
        final int count = iRemoteServiceCallBackList.getRegisteredCallbackCount();
        for (int i = 0; i < count; i++)
            try {
                iRemoteServiceCallBackList.getBroadcastItem(i).onDownloadStarted(taskId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        Log.d(this.getClass().getSimpleName(), "onDownloadStarted: " + taskId);
    }

    @Override
    public void onDownloadPaused(long taskId) {
        final int count = iRemoteServiceCallBackList.getRegisteredCallbackCount();
        for (int i = 0; i < count; i++)
            try {
                iRemoteServiceCallBackList.getBroadcastItem(i).onDownloadPaused(taskId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        Log.d(this.getClass().getSimpleName(), "onDownloadPaused: " + taskId);
    }

    @Override
    public void onDownloadProcess(long taskId, final double percent, long downloadedLength) {
        final int count = iRemoteServiceCallBackList.getRegisteredCallbackCount();
        for (int i = 0; i < count; i++)
            try {
                iRemoteServiceCallBackList.getBroadcastItem(i).onDownloadProcess(taskId, percent, downloadedLength);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        Log.d(this.getClass().getSimpleName(), "onDownloadProcess: " + taskId + " percent: " + percent);
    }

    @Override
    public void onDownloadFinished(long taskId) {
        final int count = iRemoteServiceCallBackList.getRegisteredCallbackCount();
        for (int i = 0; i < count; i++)
            try {
                iRemoteServiceCallBackList.getBroadcastItem(i).onDownloadFinished(taskId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        Log.d(this.getClass().getSimpleName(), "onDownloadFinished: " + taskId);
    }

    @Override
    public void onDownloadRebuildStart(long taskId) {
        final int count = iRemoteServiceCallBackList.getRegisteredCallbackCount();
        for (int i = 0; i < count; i++)
            try {
                iRemoteServiceCallBackList.getBroadcastItem(i).onDownloadRebuildStart(taskId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        Log.d(this.getClass().getSimpleName(), "onDownloadRebuildStart: " + taskId);
    }

    @Override
    public void onDownloadRebuildFinished(long taskId) {
        final int count = iRemoteServiceCallBackList.getRegisteredCallbackCount();
        for (int i = 0; i < count; i++)
            try {
                iRemoteServiceCallBackList.getBroadcastItem(i).onDownloadRebuildFinished(taskId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        Log.d(this.getClass().getSimpleName(), "onDownloadRebuildFinished: " + taskId);
    }

    @Override
    public void onDownloadCompleted(long taskId) {
        final int count = iRemoteServiceCallBackList.getRegisteredCallbackCount();
        for (int i = 0; i < count; i++)
            try {
                iRemoteServiceCallBackList.getBroadcastItem(i).onDownloadCompleted(taskId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        Log.d(this.getClass().getSimpleName(), "onDownloadCompleted: " + taskId);
    }

    @Override
    public void onFailedReason(long taskId, FailReason reason) {
        final int count = iRemoteServiceCallBackList.getRegisteredCallbackCount();
        for (int i = 0; i < count; i++)
            try {
                iRemoteServiceCallBackList.getBroadcastItem(i).onFailedReason(taskId, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        Log.d(this.getClass().getSimpleName(), "taskId: " + taskId + " onFailedReason: " + reason.toString());
    }

}
