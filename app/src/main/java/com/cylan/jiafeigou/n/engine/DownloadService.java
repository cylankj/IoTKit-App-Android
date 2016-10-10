package com.cylan.jiafeigou.n.engine;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
    private DownloadManagerPro.TaskBuilder taskBuilder;
    private final Object lock = new Object();
    //callback 集合
    private RemoteCallbackList<IRemoteServiceCallback> iRemoteServiceCallBackList = new RemoteCallbackList<>();

    private IRemoteService.Stub mBinder = new IRemoteService.Stub() {
        public void registerCallback(IRemoteServiceCallback cb) {
            Log.d("DownloadService", "registerCallback:" + cb);
            if (cb != null) iRemoteServiceCallBackList.register(cb);
        }

        public void unregisterCallback(IRemoteServiceCallback cb) {
            Log.d("DownloadService", "unregisterCallback:" + cb);
            if (cb != null) iRemoteServiceCallBackList.unregister(cb);
        }
    };

    public DownloadService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Parcelable parcelable = intent.getParcelableExtra(KEY_PARCELABLE);
        if (parcelable != null && parcelable instanceof UpdateFileBean) {
            UpdateFileBean bean = (UpdateFileBean) parcelable;
            initSomething(bean);
        }
        AppLogger.d("DownloadService: " + parcelable + " " + Thread.currentThread());
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d("DownloadService", "DownloadService: " + rootIntent);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        iRemoteServiceCallBackList.kill();
    }

    private static final int MSG_START_DOWNLOAD = 1;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_DOWNLOAD:
                    Log.d(this.getClass().getSimpleName(), "handler: " + msg.arg1);
                    DownloadManagerPro.getInstance().startDownload(msg.arg1);
                    break;
            }
            return true;
        }
    });

    private void initSomething(final UpdateFileBean bean) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DownloadManagerPro.Config config = new DownloadManagerPro.Config()
                        .setContext(getApplicationContext());
                DownloadManagerPro.getInstance().init(config);
                taskBuilder = new DownloadManagerPro.TaskBuilder();
                taskBuilder.setUrl(bean.url)
                        .setMaxChunks(4)
                        .setSaveName(bean.fileName)
                        .setOverwrite(true)
                        .setSdCardFolderAddress(bean.savePath)
                        .setDownloadManagerListener(DownloadService.this)
                        .setAllowNetType(NetConfig.TYPE_ALL);
                int token = DownloadManagerPro.getInstance().initTask(taskBuilder);
                handler.sendMessageDelayed(handler.obtainMessage(MSG_START_DOWNLOAD, token, 0), 1000);
            }
        }).start();
    }

    @Override
    public void onDownloadStarted(long taskId) {
        synchronized (lock) {
            final int count = iRemoteServiceCallBackList.beginBroadcast();
            for (int i = 0; i < count; i++)
                try {
                    iRemoteServiceCallBackList.getBroadcastItem(i).onDownloadStarted(taskId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            iRemoteServiceCallBackList.finishBroadcast();
            Log.d(this.getClass().getSimpleName(), "onDownloadStarted: " + taskId);
        }
    }

    @Override
    public void onDownloadPaused(long taskId) {
        synchronized (lock) {
            final int count = iRemoteServiceCallBackList.beginBroadcast();
            for (int i = 0; i < count; i++)
                try {
                    iRemoteServiceCallBackList.getBroadcastItem(i).onDownloadPaused(taskId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            iRemoteServiceCallBackList.finishBroadcast();
            Log.d(this.getClass().getSimpleName(), "onDownloadPaused: " + taskId);
        }
    }

    @Override
    public void onDownloadProcess(long taskId, final double percent, long downloadedLength) {
        synchronized (lock) {
            final int count = iRemoteServiceCallBackList.beginBroadcast();
            for (int i = 0; i < count; i++)
                try {
                    iRemoteServiceCallBackList.getBroadcastItem(i).onDownloadProcess(taskId, percent, downloadedLength);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            iRemoteServiceCallBackList.finishBroadcast();
            Log.d(this.getClass().getSimpleName(), "onDownloadProcess: " + taskId + " percent: " + percent);
        }
    }

    @Override
    public void onDownloadFinished(long taskId) {
        synchronized (lock) {
            final int count = iRemoteServiceCallBackList.beginBroadcast();
            for (int i = 0; i < count; i++)
                try {
                    iRemoteServiceCallBackList.getBroadcastItem(i).onDownloadFinished(taskId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            iRemoteServiceCallBackList.finishBroadcast();
            Log.d(this.getClass().getSimpleName(), "onDownloadFinished: " + taskId);
        }
    }

    @Override
    public void onDownloadRebuildStart(long taskId) {
        synchronized (lock) {
            final int count = iRemoteServiceCallBackList.beginBroadcast();
            for (int i = 0; i < count; i++)
                try {
                    iRemoteServiceCallBackList.getBroadcastItem(i).onDownloadRebuildStart(taskId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            iRemoteServiceCallBackList.finishBroadcast();
            Log.d(this.getClass().getSimpleName(), "onDownloadRebuildStart: " + taskId);
        }
    }

    @Override
    public void onDownloadRebuildFinished(long taskId) {
        synchronized (lock) {
            final int count = iRemoteServiceCallBackList.beginBroadcast();
            for (int i = 0; i < count; i++)
                try {
                    iRemoteServiceCallBackList.getBroadcastItem(i).onDownloadRebuildFinished(taskId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            iRemoteServiceCallBackList.finishBroadcast();
            Log.d(this.getClass().getSimpleName(), "onDownloadRebuildFinished: " + taskId);
        }
    }

    @Override
    public void onDownloadCompleted(long taskId) {
        synchronized (lock) {
            final int count = iRemoteServiceCallBackList.beginBroadcast();
            for (int i = 0; i < count; i++)
                try {
                    iRemoteServiceCallBackList.getBroadcastItem(i).onDownloadCompleted(taskId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            iRemoteServiceCallBackList.finishBroadcast();
            Log.d(this.getClass().getSimpleName(), "onDownloadCompleted: " + taskId);
        }
    }

    @Override
    public void onFailedReason(long taskId, FailReason reason) {
        synchronized (lock) {
            final int count = iRemoteServiceCallBackList.beginBroadcast();
            for (int i = 0; i < count; i++)
                try {
                    iRemoteServiceCallBackList.getBroadcastItem(i).onFailedReason(taskId, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            iRemoteServiceCallBackList.finishBroadcast();
            Log.d(this.getClass().getSimpleName(), "taskId: " + taskId + " onFailedReason: " + reason.toString());
        }
    }

}
