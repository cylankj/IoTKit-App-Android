package com.cylan.jiafeigou.n.mvp.impl.cloud;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.cylan.jiafeigou.ICloudLiveService;
import com.cylan.jiafeigou.n.engine.CloudLiveService;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveCallInContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * 作者：zsl
 * 创建时间：2016/10/19
 * 描述：
 */
public class CloudLiveCallInPresenterImp extends AbstractPresenter<CloudLiveCallInContract.View> implements CloudLiveCallInContract.Presenter{

    private ICloudLiveService iCloudLiveService;

    public CloudLiveCallInPresenterImp(CloudLiveCallInContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void setVideoTalkFinishFlag(boolean isFinish) {
        try {
            iCloudLiveService.setIgnoreFlag(isFinish);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setVideoTalkFinishResultData(String data) {
        try {
            iCloudLiveService.setIgnoreResultData(data);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void bindService() {
        Intent serviceIntent = new Intent(getView().getContext(), CloudLiveService.class);
        getView().getContext().bindService(serviceIntent,conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        if (conn != null){
            getView().getContext().unbindService(conn);
        }
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iCloudLiveService = ICloudLiveService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            iCloudLiveService = null;
        }
    };
}
