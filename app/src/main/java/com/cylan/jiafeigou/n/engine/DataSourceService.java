package com.cylan.jiafeigou.n.engine;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDPMsgCount;
import com.cylan.entity.jniCall.JFGDPMsgRet;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.JFGDoorBellCaller;
import com.cylan.entity.jniCall.JFGFeedbackInfo;
import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.entity.jniCall.JFGHistoryVideo;
import com.cylan.entity.jniCall.JFGHistoryVideoErrorInfo;
import com.cylan.entity.jniCall.JFGMsgHttpResult;
import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.entity.jniCall.JFGResult;
import com.cylan.entity.jniCall.JFGServerCfg;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.entity.jniCall.RobotMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jfgapp.interfases.AppCallBack;
import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.cache.JCache;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JfgCmdEnsurance;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.rxbus.IEventBus;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.cylan.jiafeigou.support.stat.MtaManager;
import com.google.gson.Gson;

import java.util.ArrayList;


public class DataSourceService extends Service implements AppCallBack {

    static {
        System.loadLibrary("jfgsdk");
        System.loadLibrary("sqlcipher");
    }

    private IEventBus eventBus;

    @Override
    public void onCreate() {
        super.onCreate();
        eventBus = RxBus.getDefault();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initNative();
        return START_STICKY;
    }

    private void initNative() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JfgAppCmd.initJfgAppCmd(getApplicationContext(), DataSourceService.this,
                            JConstant.LOG_PATH);
                } catch (PackageManager.NameNotFoundException e) {
                    AppLogger.d("let's go err:" + e.getLocalizedMessage());
                }
                AppLogger.d("let's go initNative:");
                MtaManager.customEvent(getApplicationContext(), "DataSourceService", "NativeInit");
            }
        }).start();
    }


    @Override
    public void OnLocalMessage(String s, int i, byte[] bytes) {
        AppLogger.d("OnLocalMessage :" + s + ",i:" + i);
    }

    @Override
    public void OnReportJfgDevices(JFGDevice[] jfgDevices) {
        AppLogger.d("OnLocalMessage :" + jfgDevices);
    }

    @Override
    public void OnUpdateAccount(JFGAccount jfgAccount) {
        AppLogger.d("OnLocalMessage :" + new Gson().toJson(jfgAccount));
        if (eventBus != null && eventBus.hasObservers()) {
            eventBus.post(new RxEvent.GetUserInfo(jfgAccount));
        }
    }

    @Override
    public void OnUpdateHistoryVideoList(JFGHistoryVideo jfgHistoryVideo) {
        AppLogger.d("OnLocalMessage :");
    }

    @Override
    public void OnUpdateHistoryErrorCode(JFGHistoryVideoErrorInfo jfgHistoryVideoErrorInfo) {
        AppLogger.d("OnLocalMessage :");
    }


    @Override
    public void OnServerConfig(JFGServerCfg jfgServerCfg) {
        AppLogger.d("OnLocalMessage :");
    }

    @Override
    public void OnLogoutByServer(int i) {
        AppLogger.d("OnLocalMessage :");
    }

    @Override
    public void OnVideoDisconnect(JFGMsgVideoDisconn jfgMsgVideoDisconn) {
        AppLogger.d("OnLocalMessage :");
    }

    @Override
    public void OnVideoNotifyResolution(JFGMsgVideoResolution jfgMsgVideoResolution) {
        AppLogger.d("OnLocalMessage :");
    }

    @Override
    public void OnVideoNotifyRTCP(JFGMsgVideoRtcp jfgMsgVideoRtcp) {
        AppLogger.d("OnLocalMessage :");
    }

    @Override
    public void OnHttpDone(JFGMsgHttpResult jfgMsgHttpResult) {
        AppLogger.d("OnLocalMessage :");
        if (eventBus != null && eventBus.hasObservers())
            eventBus.post(new RxEvent.GetHttpDoneResult(jfgMsgHttpResult));
    }

    @Override
    public void OnRobotTransmitMsg(RobotMsg robotMsg) {
        AppLogger.d("OnLocalMessage :");
    }

    @Override
    public void OnRobotMsgAck(int i) {
        AppLogger.d("OnLocalMessage :");
    }

    @Override
    public void OnRobotGetDataRsp(RobotoGetDataRsp robotoGetDataRsp) {
        AppLogger.d("OnLocalMessage :");
    }

    @Override
    public void OnRobotSetDataRsp(long l, ArrayList<JFGDPMsgRet> arrayList) {
        AppLogger.d("OnLocalMessage :");
    }

    @Override
    public void OnRobotGetDataTimeout(long l) {
        AppLogger.d("OnLocalMessage :");
    }

    @Override
    public ArrayList<JFGDPMsg> OnQuerySavedDatapoint(String s, ArrayList<JFGDPMsg> arrayList) {
        return null;
    }

    @Override
    public void OnlineStatus(boolean b) {
        AppLogger.d("OnlineStatus :" + b);
        JCache.isOnline = b;
        RxBus.getDefault().post(new RxEvent.LoginRsp(b));
    }

    @Override
    public void OnResult(JFGResult jfgResult) {
        boolean login = false;
        switch (jfgResult.event) {
            case 0:
                eventBus.post(new RxEvent.ResultVerifyCode(jfgResult.code));
                break;
            case 1:
                login = jfgResult.code == JError.ErrorOK;
                eventBus.post(new RxEvent.ResultRegister(jfgResult.code));
                break;
            case 2:
                login = jfgResult.code == JError.ErrorOK;
                eventBus.post(new RxEvent.ResultLogin(jfgResult.code));
                break;
        }
        if (login) {
            JfgCmdEnsurance.getCmd().getAccount();
            AppLogger.i("get account");
        }
        AppLogger.i("jfgResult:[event:" + jfgResult.event + ",code:" + jfgResult.code + "]");
    }

    @Override
    public void OnDoorBellCall(JFGDoorBellCaller jfgDoorBellCaller) {
        AppLogger.d("OnLocalMessage :");
    }

    @Override
    public void OnOtherClientAnswerCall() {
        AppLogger.d("OnLocalMessage :");
    }

    @Override
    public void OnRobotCountDataRsp(long l, String s, ArrayList<JFGDPMsgCount> arrayList) {
        AppLogger.d("OnLocalMessage :");
    }

    @Override
    public void OnRobotDelDataRsp(long l, String s, int i) {
        AppLogger.d("OnLocalMessage :");
    }

    @Override
    public void OnRobotSyncData(boolean b, String s, ArrayList<JFGDPMsg> arrayList) {
        AppLogger.d("OnLocalMessage :");
    }

    @Override
    public void OnSendSMSResult(int i, String s) {
        AppLogger.d("OnSendSMSResult :" + i + "," + s);
        if (eventBus != null && eventBus.hasObservers())
            eventBus.post(new RxEvent.SmsCodeResult(i, s));
    }

    @Override
    public void OnGetFriendListRsp(int i, ArrayList<JFGFriendAccount> arrayList) {
        AppLogger.d("OnLocalMessage :");
        if (eventBus != null && eventBus.hasObservers()) {
            eventBus.post(new RxEvent.GetFriendList(i, arrayList));
        }
    }

    @Override
    public void OnGetFriendRequestListRsp(int i, ArrayList<JFGFriendRequest> arrayList) {
        AppLogger.d("OnLocalMessage :");
        if (eventBus != null && eventBus.hasObservers()) {
            eventBus.post(new RxEvent.GetAddReqList(i, arrayList));
        }
    }

    @Override
    public void OnGetFriendInfoRsp(int i, JFGFriendAccount jfgFriendAccount) {
        AppLogger.d("OnLocalMessage :");
    }

    @Override
    public void OnCheckFriendAccountRsp(int i, String s, String s1, boolean b) {
        AppLogger.d("OnLocalMessage :");
    }

    @Override
    public void OnShareDeviceRsp(int i, String s, String s1) {
        AppLogger.d("OnShareDeviceRsp :");
        if (eventBus != null && eventBus.hasObservers()) {
            eventBus.post(new RxEvent.ShareDeviceCallBack(i,s,s1));
        }
    }

    @Override
    public void OnUnShareDeviceRsp(int i, String s, String s1) {
        AppLogger.d("OnUnShareDeviceRsp :");
    }

    @Override
    public void OnGetShareListRsp(int i, ArrayList<JFGShareListInfo> arrayList) {
        AppLogger.d("OnGetShareListRsp :");
        if (eventBus != null && eventBus.hasObservers()) {
            eventBus.post(new RxEvent.GetShareDeviceList(i, arrayList));
        }

    }

    @Override
    public void OnGetUnShareListByCidRsp(int i, ArrayList<JFGFriendAccount> arrayList) {
        AppLogger.d("OnGetUnShareListByCidRsp :");
    }

    @Override
    public void OnUpdateNTP(long l) {
        AppLogger.d("OnUpdateNTP :" + l);
    }

    @Override
    public void OnEfamilyMsg(byte[] bytes) {
        AppLogger.d("OnEfamilyMsg :");
    }

    @Override
    public void OnForgetPassByEmailRsp(int i, String s) {
        AppLogger.d("OnForgetPassByEmailRsp :" + s);
        if (eventBus.hasObservers()) {
            eventBus.post(new RxEvent.ForgetPwdByMail(s));
        }
    }

    @Override
    public void OnGetAliasByCidRsp(int i, String s) {
        AppLogger.d("OnGetAliasByCidRsp :");
    }

    @Override
    public void OnGetFeedbackRsp(int i, ArrayList<JFGFeedbackInfo> arrayList) {
        AppLogger.d("OnGetFeedbackRsp :");
    }
}
