package com.cylan.jiafeigou.n.mvp.impl;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

import com.cylan.jiafeigou.IRemoteService;
import com.cylan.jiafeigou.IRemoteServiceCallback;
import com.cylan.jiafeigou.n.engine.DownloadService;
import com.cylan.jiafeigou.n.mvp.contract.DownloadContract;

/**
 * Created by cylan-hunt on 16-9-29.
 */

public class DownloadContractPresenterImpl extends AbstractPresenter<DownloadContract.View>
        implements DownloadContract.Presenter {

    private IRemoteService mService = null;

    public DownloadContractPresenterImpl(DownloadContract.View view) {
        super(view);
    }

    @Override
    public void startDownload(Parcelable bean) {
        Intent intent = new Intent(getView().getContext().getApplicationContext(), DownloadService.class);
        intent.putExtra(DownloadService.KEY_PARCELABLE, bean);
        getView().getContext().bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
    }

    @Override
    public void stopDownload() {

    }

    @Override
    public void start() {
        super.start();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We want to monitor the service for as long as we are
            // connected to it.
            mService = IRemoteService.Stub.asInterface(service);
            try {
                mService.registerCallback(mCallback);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
                Log.d(this.getClass().getSimpleName(), "RemoteException: " + e);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    @Override
    public void stop() {
        super.stop();
        try {
            if (mService != null)
                mService.unregisterCallback(mCallback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        getView().getContext().unbindService(serviceConnection);
    }

    private IRemoteServiceCallback mCallback = new IRemoteServiceCallback.Stub() {
        @Override
        public void onDownloadStarted(long taskId) throws RemoteException {
            Log.d("IRemoteServiceCallback", "onDownloadStarted:" + taskId);
            if (getView() != null)
                getView().onDownloadStart();
        }

        @Override
        public void onDownloadPaused(long taskId) throws RemoteException {
            Log.d("IRemoteServiceCallback", "onDownloadPaused:" + taskId);
        }

        @Override
        public void onDownloadProcess(long taskId, double percent, long downloadedLength) throws RemoteException {
            Log.d("IRemoteServiceCallback", "onDownloadProcess:" + taskId + " percent:" + percent);
            if (getView() != null)
                getView().onDownloading(percent, downloadedLength);
        }

        @Override
        public void onDownloadFinished(long taskId) throws RemoteException {
            Log.d("IRemoteServiceCallback", "onDownloadFinished:" + taskId);
            if (getView() != null)
                getView().onDownloadFinish();
        }

        @Override
        public void onDownloadRebuildStart(long taskId) throws RemoteException {
            Log.d("IRemoteServiceCallback", "onDownloadRebuildStart:" + taskId);
        }

        @Override
        public void onDownloadRebuildFinished(long taskId) throws RemoteException {
            Log.d("IRemoteServiceCallback", "onDownloadRebuildFinished:" + taskId);
        }

        @Override
        public void onDownloadCompleted(long taskId) throws RemoteException {
            Log.d("IRemoteServiceCallback", "onDownloadCompleted:" + taskId);
        }

        @Override
        public void onFailedReason(long taskId, int reason) throws RemoteException {
            Log.d("IRemoteServiceCallback", "onFailedReason:" + taskId);
            if (getView() != null)
                getView().onDownloadErr(reason);
        }
    };
}
