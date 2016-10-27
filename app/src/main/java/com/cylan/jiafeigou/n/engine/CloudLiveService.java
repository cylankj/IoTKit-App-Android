package com.cylan.jiafeigou.n.engine;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.cylan.jiafeigou.ICloudLiveService;

/**
 * 作者：zsl
 * 创建时间：2016/10/14
 * 描述：
 */
public class CloudLiveService extends Service {

    private String hangUpResult = null;
    private boolean hangUpFlag;

    private String ignoreResult = null;
    private boolean ignoreFlag;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private ICloudLiveService.Stub mBinder = new ICloudLiveService.Stub() {
        @Override
        public void setHangUpFlag(boolean isHangUp) throws RemoteException {
            hangUpFlag = isHangUp;
        }

        @Override
        public boolean getHangUpFlag() throws RemoteException {
            return hangUpFlag;
        }

        @Override
        public void setHangUpResultData(String obj) throws RemoteException {
            hangUpResult = obj;
        }

        @Override
        public String getHangUpResultData() throws RemoteException {
            return hangUpResult;
        }

        @Override
        public void setIgnoreFlag(boolean isIgnore) throws RemoteException {
            ignoreFlag = isIgnore;
        }

        @Override
        public boolean getIgnoreFlag() throws RemoteException {
            return ignoreFlag;
        }

        @Override
        public void setIgnoreResultData(String obj) throws RemoteException {
            ignoreResult = obj;
        }

        @Override
        public String getIgnoreResultData() throws RemoteException {
            return ignoreResult;
        }
    };

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
