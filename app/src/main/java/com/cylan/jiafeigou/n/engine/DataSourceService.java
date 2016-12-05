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
import com.cylan.jiafeigou.dp.DpParser;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JResultEvent;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.stat.MtaManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


public class DataSourceService extends Service implements AppCallBack {

    static {
        System.loadLibrary("jfgsdk");
        System.loadLibrary("sqlcipher");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        DpParser.getDpParser().registerDpParser();
        GlobalUdpDataSource.getInstance().register();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DpParser.getDpParser().unregisterDpParser();
        GlobalUdpDataSource.getInstance().unregister();
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
        if (RxBus.getCacheInstance().hasObservers()) {
            RxEvent.LocalUdpMsg msg = new RxEvent.LocalUdpMsg();
            msg.time = System.currentTimeMillis();
            msg.ip = s;
            msg.port = (short) i;
            msg.data = bytes;
            RxBus.getCacheInstance().post(msg);
        }
    }

    @Override
    public void OnReportJfgDevices(JFGDevice[] jfgDevices) {
        RxBus.getCacheInstance().postSticky(new RxEvent.DeviceRawList(jfgDevices));
        List<JFGDevice> list = new ArrayList<>();
        for (int i = 0;i<jfgDevices.length;i++){
            list.add(jfgDevices[i]);
        }
        RxBus.getCacheInstance().postSticky(new RxEvent.DeviceList(list));
    }

    @Override
    public void OnUpdateAccount(JFGAccount jfgAccount) {
        JCache.setAccountCache(jfgAccount);
        RxBus.getCacheInstance().postSticky(jfgAccount);
        RxBus.getCacheInstance().postSticky(new RxEvent.GetUserInfo(jfgAccount));
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
        AppLogger.d("OnLocalMessage :"+i);
        RxBus.getCacheInstance().post(i);
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
        AppLogger.d("OnLocalMessage :"+new Gson().toJson(jfgMsgHttpResult));
        if (RxBus.getCacheInstance().hasObservers())
            RxBus.getCacheInstance().post(new RxEvent.GetHttpDoneResult(jfgMsgHttpResult));
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
        AppLogger.d("OnLocalMessage :" + new Gson().toJson(robotoGetDataRsp));
        RxBus.getCacheInstance().post(robotoGetDataRsp);
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
        JCache.onLineStatus = b;
        RxBus.getCacheInstance().post(new RxEvent.LoginRsp(b));
    }

    @Override
    public void OnResult(JFGResult jfgResult) {
        boolean login = false;
        switch (jfgResult.event) {
            case 0:
                RxBus.getCacheInstance().post(new RxEvent.ResultVerifyCode(jfgResult.code));
                break;
            case 1:
                login = jfgResult.code == JError.ErrorOK;//注册成功
                RxBus.getCacheInstance().post(new RxEvent.ResultRegister(jfgResult.code));
                break;
            case 2:
                login = jfgResult.code == JError.ErrorOK;//登陆成功
                RxBus.getCacheInstance().post(new RxEvent.ResultLogin(jfgResult.code));
                break;
            case JResultEvent.JFG_RESULT_BINDDEV:
                //绑定设备
                RxBus.getCacheInstance().postSticky(new RxEvent.BindDeviceEvent(jfgResult));
                break;
        }
        if (login) {
            AfterLoginService.startGetAccountAction(getApplicationContext());
            AfterLoginService.startSaveAccountAction(getApplicationContext());
            AfterLoginService.resumeOfflineRequest();
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
        AppLogger.d("OnRobotSyncData :" + b + " " + s + " " + arrayList);
        RxEvent.JFGRobotSyncData data = new RxEvent.JFGRobotSyncData();
        data.state = b;
        data.identity = s;
        data.dataList = arrayList;
    }

    @Override
    public void OnSendSMSResult(int i, String s) {
        AppLogger.d("OnSendSMSResult :" + i + "," + s);
        if (RxBus.getCacheInstance() != null && RxBus.getCacheInstance().hasObservers())
            RxBus.getCacheInstance().post(new RxEvent.SmsCodeResult(i, s));
    }

    @Override
    public void OnGetFriendListRsp(int i, ArrayList<JFGFriendAccount> arrayList) {
        AppLogger.d("OnLocalMessage :");
        if (RxBus.getCacheInstance() != null && RxBus.getCacheInstance().hasObservers()) {
            RxBus.getCacheInstance().post(new RxEvent.GetFriendList(i, arrayList));
        }
    }

    @Override
    public void OnGetFriendRequestListRsp(int i, ArrayList<JFGFriendRequest> arrayList) {
        AppLogger.d("OnLocalMessage :");
        if (RxBus.getCacheInstance() != null && RxBus.getCacheInstance().hasObservers()) {
            RxBus.getCacheInstance().post(new RxEvent.GetAddReqList(i, arrayList));
        }
    }

    @Override
    public void OnGetFriendInfoRsp(int i, JFGFriendAccount jfgFriendAccount) {
        AppLogger.d("OnLocalMessage :"+new Gson().toJson(jfgFriendAccount));
        RxBus.getCacheInstance().post(new RxEvent.GetFriendInfoCall(i,jfgFriendAccount));
    }

    @Override
    public void OnCheckFriendAccountRsp(int i, String s, String s1, boolean b) {
        AppLogger.d("OnLocalMessage :");
        RxBus.getCacheInstance().post(new RxEvent.CheckAccountCallback(i,s,s1,b));
    }

    @Override
    public void OnShareDeviceRsp(int i, String s, String s1) {
        AppLogger.d("OnShareDeviceRsp :");
        if (RxBus.getCacheInstance() != null && RxBus.getCacheInstance().hasObservers()) {
            RxBus.getCacheInstance().post(new RxEvent.ShareDeviceCallBack(i, s, s1));
        }
    }

    @Override
    public void OnUnShareDeviceRsp(int i, String s, String s1) {
        AppLogger.d("OnUnShareDeviceRsp :");
        RxBus.getCacheInstance().post(new RxEvent.UnshareDeviceCallBack(i,s,s1));
    }

    @Override
    public void OnGetShareListRsp(int i, ArrayList<JFGShareListInfo> arrayList) {
        AppLogger.d("OnGetShareListRsp :");
        RxBus.getCacheInstance().post(new RxEvent.GetShareListCallBack(i, arrayList));
    }

    @Override
    public void OnGetUnShareListByCidRsp(int i, ArrayList<JFGFriendAccount> arrayList) {
        AppLogger.d("OnGetUnShareListByCidRsp :"+arrayList.get(0));
//        RxBus.getCacheInstance().post(new RxEvent.GetHasShareFriendCallBack(i,arrayList));
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
        if (RxBus.getCacheInstance().hasObservers()) {
            RxBus.getCacheInstance().post(new RxEvent.ForgetPwdByMail(s));
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

    @Override
    public void OnCheckDevVersionRsp(boolean b, String s, String s1, String s2, String s3) {

    }

}
