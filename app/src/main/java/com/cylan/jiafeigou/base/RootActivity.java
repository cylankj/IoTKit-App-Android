package com.cylan.jiafeigou.base;

import android.app.Activity;
import android.os.Bundle;

import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.engine.MyService;
import com.cylan.jiafeigou.entity.msg.HttpResult;
import com.cylan.jiafeigou.listener.RequestCallback;
import com.cylan.jiafeigou.listener.ServerMessage;
import com.cylan.jiafeigou.utils.AppManager;
import com.cylan.jiafeigou.utils.NotificationUtil;
import com.cylan.jiafeigou.utils.ProgressDialogUtil;
import com.cylan.jiafeigou.utils.SafeChecker;
import com.cylan.publicApi.CallMessageCallBack;
import com.cylan.publicApi.MsgpackMsg;
import com.google.gson.Gson;

import org.msgpack.MessagePack;

import java.io.IOException;

import cylan.log.DswLog;

public class RootActivity extends Activity implements RequestCallback, ServerMessage {

    protected ProgressDialogUtil mProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyService.addObserver(this);
        AppManager.getAppManager().addActivity(this);
        init();
        mProgressDialog = new ProgressDialogUtil(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cancelWarmNotifycation();
    }

    public void cancelWarmNotifycation() {
        NotificationUtil.cancelNotifycationById(this, ClientConstants.WARM_NOTIFY_FLAG);
        MyService.setNum(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyService.delObserver(this);
        AppManager.getAppManager().finishActivity(this);
    }

    @Override
    public void handleMsg(int msg, Object param) {
        switch (CallMessageCallBack.MSG_TO_UI.values()[msg]) {
            case CONNECT_SERVER_SUCCESS:
                connectServer();
                break;
            case SERVER_DISCONNECTED:
            case RESOLVE_SERVER_FAILED:
            case CONNECT_SERVER_FAILED:
                disconnectServer();
                break;
            case HTTP_DONE:
                try {
                    MessagePack mMessage = new MessagePack();
                    HttpResult mResult = mMessage.read((byte[]) param, HttpResult.class);
                    Gson gson = new Gson();
                    DswLog.e("HttpResult--->" + gson.toJson(mResult));
                    httpDone(mResult);
                } catch (IOException e) {
                    DswLog.ex(e.toString());
                }
                break;
            case MSGPACK_MESSAGE:
//                if (((MsgpackMsg.MsgHeader) param).msgId == MsgpackMsg.CLIENT_SYNC_LOGOUT) {
//                    MsgSyncLogout mMsgSyncLogout = (MsgSyncLogout) param;
//                    MyApp.logout(this);
//                    if ((mMsgSyncLogout.trigger_id == MsgpackMsg.CLIENT_CHANGEPASS_REQ)
//                            || (mMsgSyncLogout.trigger_id == MsgpackMsg.CLIENT_SETPASS_REQ)) {
//                        MyApp.showForceNotifyDialog(this, getString(R.string.PWD_CHANGED));
//                    } else {
//                        MyApp.showForceNotifyDialog(this, getString(R.string.RET_ESESSION_NOT_EXIST));
//                    }
//                } else {
                    handleMsgpackMsg(msg, (MsgpackMsg.MsgHeader) param);
//                }
                break;

        }

    }


    @Override
    public void connectServer() {

    }

    @Override
    public void disconnectServer() {

    }

    @Override
    public void httpDone(HttpResult result) {
    }

    @Override
    public void handleMsgpackMsg(int msg, MsgpackMsg.MsgHeader msgpackMsg) {

    }

    private void init() {
        SafeChecker checker = new SafeChecker(this.getApplicationContext());
        if (checker.isOk()) {
            AppManager.getAppManager().finishAllActivity();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    }


}
