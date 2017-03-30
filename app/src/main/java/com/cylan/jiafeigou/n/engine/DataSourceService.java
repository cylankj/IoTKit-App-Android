package com.cylan.jiafeigou.n.engine;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.support.annotation.Nullable;

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
import com.cylan.ex.JfgException;
import com.cylan.jfgapp.interfases.AppCallBack;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.module.PanoramaEvent;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.AutoSignIn;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JResultEvent;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.OptionsImpl;
import com.cylan.jiafeigou.support.Security;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.HandlerThreadUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.udpMsgPack.JfgUdpMsg;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class DataSourceService extends Service implements AppCallBack {
    //这里用 services 避免 APP进入后台被挂起

    static {
        System.loadLibrary("jfgsdk");
    }

    public DataSourceService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        initNative();
        GlobalUdpDataSource.getInstance().register();
        GlobalBellCallSource.getInstance().register();
        GlobalResetPwdSource.getInstance().register();
    }


    public void onDestroy() {
        GlobalUdpDataSource.getInstance().unregister();
        GlobalBellCallSource.getInstance().unRegister();
        GlobalResetPwdSource.getInstance().unRegister();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void initNative() {

        HandlerThreadUtils.clean();
        HandlerThreadUtils.postAtFrontOfQueue(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
            try {
                String trimPackageName = JFGRules.getTrimPackageName();
                //研发平台下才能使用额外配置的服务器地址.不检查服务器地址格式.
                String vid = Security.getVId(trimPackageName);
                String vKey = Security.getVKey(trimPackageName);
                JfgCmdInsurance.getCmd().setCallBack(DataSourceService.this);
                JfgCmdInsurance.getCmd().initNativeParam(vid, vKey, OptionsImpl.getServer());
                JfgCmdInsurance.getCmd().enableLog(true, JConstant.LOG_PATH);
                AppLogger.d("sdk version:" + JfgCmdInsurance.getCmd().getSdkVersion());
            } catch (Exception e) {
                AppLogger.d("let's go err:" + e.getLocalizedMessage());
            }
            try2autoLogin();
            AppLogger.d("let's go initNative:");
            try {
                JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
            } catch (JfgException e) {
                e.printStackTrace();
            }

        });
    }

    private void try2autoLogin() {
        AutoSignIn.getInstance().autoLogin()
                .flatMap(integer -> {
                    AppLogger.d("integer: " + integer);
                    if (integer == 0) {
                        PreferencesUtils.putInt(JConstant.IS_lOGINED, 1);
                        PreferencesUtils.putBoolean(JConstant.AUTO_SIGNIN_TAB, true);
                        RxBus.getCacheInstance().toObservableSticky(RxEvent.ResultLogin.class)
                                .subscribeOn(Schedulers.newThread())
                                .timeout(5, TimeUnit.SECONDS, Observable.just("autoSign in timeout")
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .map(s -> {
                                            AppLogger.d("net type: " + NetUtils.getNetType(ContextUtils.getContext()));
                                            if (NetUtils.getNetType(ContextUtils.getContext()) == -1) {
                                                RxBus.getCacheInstance().postSticky(new RxEvent.ResultLogin(JError.NoNet));
                                            } else {
                                                if (!PreferencesUtils.getBoolean(JConstant.AUTO_lOGIN_PWD_ERR, false)) {
                                                    RxBus.getCacheInstance().postSticky(new RxEvent.ResultLogin(JError.LoginTimeOut));
                                                }
                                                PreferencesUtils.putBoolean(JConstant.AUTO_lOGIN_PWD_ERR, false);
                                            }
                                            DataSourceManager.getInstance().initFromDB();
                                            return null;
                                        }))
                                .subscribe();
                    } else if (integer == -1) {
                        //emit failed event.
                        PreferencesUtils.putInt(JConstant.IS_lOGINED, 0);
                        RxBus.getCacheInstance().postSticky(new RxEvent.ResultLogin(JError.StartLoginPage));
                    }
                    return null;
                })
                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                .subscribe();
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
        for (JFGDevice device : jfgDevices) {
            AppLogger.d("OnReportJfgDevices" + new Gson().toJson(device));
        }
        RxBus.getCacheInstance().post(new RxEvent.SerializeCacheDeviceEvent(jfgDevices));
    }

    @Override
    public void OnUpdateAccount(JFGAccount jfgAccount) {
        AppLogger.d("OnUpdateAccount :" + jfgAccount.getPhotoUrl());
        RxBus.getCacheInstance().post(new RxEvent.SerializeCacheAccountEvent(jfgAccount));
    }

    @Override
    public void OnUpdateHistoryVideoList(JFGHistoryVideo jfgHistoryVideo) {
        AppLogger.d("OnUpdateHistoryVideoList :" + jfgHistoryVideo.list.size());
        DataSourceManager.getInstance().cacheHistoryDataList(jfgHistoryVideo);
    }

    @Override
    public void OnUpdateHistoryErrorCode(JFGHistoryVideoErrorInfo jfgHistoryVideoErrorInfo) {
        AppLogger.d("OnUpdateHistoryErrorCode :");
    }

    @Override
    public void OnServerConfig(JFGServerCfg jfgServerCfg) {
        AppLogger.d("OnServerConfig :");
    }

    @Override
    public void OnLogoutByServer(int i) {
        AppLogger.d("OnLocalMessage hh:" + i);
        RxBus.getCacheInstance().post(new RxEvent.PwdHasResetEvent(i));
        DataSourceManager.getInstance().setLoginState(new LogState(LogState.STATE_ACCOUNT_OFF));
    }

    @Override
    public void OnVideoDisconnect(JFGMsgVideoDisconn jfgMsgVideoDisconn) {
        RxBus.getCacheInstance().post(jfgMsgVideoDisconn);
        AppLogger.d("OnVideoDisconnect :" + new Gson().toJson(jfgMsgVideoDisconn));
    }

    @Override
    public void OnVideoNotifyResolution(JFGMsgVideoResolution jfgMsgVideoResolution) {
        AppLogger.d("OnVideoNotifyResolution" + jfgMsgVideoResolution.peer);
        RxBus.getCacheInstance().post(jfgMsgVideoResolution);
    }

    @Override
    public void OnVideoNotifyRTCP(JFGMsgVideoRtcp jfgMsgVideoRtcp) {
        RxBus.getCacheInstance().post(jfgMsgVideoRtcp);
    }

    @Override
    public void OnHttpDone(JFGMsgHttpResult jfgMsgHttpResult) {
        AppLogger.d("OnLocalMessage :" + new Gson().toJson(jfgMsgHttpResult));
        RxBus.getCacheInstance().post(jfgMsgHttpResult);
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
        AppLogger.d("OnRobotGetDataRsp :" + new Gson().toJson(robotoGetDataRsp));
        RxBus.getCacheInstance().post(new RxEvent.SerializeCacheGetDataEvent(robotoGetDataRsp));
    }

    @Override
    public void OnRobotGetDataExRsp(long l, String s, ArrayList<JFGDPMsg> arrayList) {
        long time = System.currentTimeMillis();
        //parse to RobotoGetDataRsp
        RobotoGetDataRsp robotoGetDataRsp = new RobotoGetDataRsp();
        robotoGetDataRsp.identity = s;
        robotoGetDataRsp.seq = l;
        robotoGetDataRsp.map = new HashMap<>();
        for (JFGDPMsg msg : arrayList) {
            ArrayList<JFGDPMsg> list = robotoGetDataRsp.map.get((int) msg.id);//
            if (list == null) {
                list = new ArrayList<>();
                robotoGetDataRsp.map.put((int) msg.id, list);
            }
            list.add(msg);
        }
        RxBus.getCacheInstance().post(new RxEvent.SerializeCacheGetDataEvent(robotoGetDataRsp));
        AppLogger.d("OnRobotGetDataExRsp performance:" + (System.currentTimeMillis() - time));
        AppLogger.d("OnRobotGetDataExRsp :" + new Gson().toJson(arrayList));
    }

    @Override
    public void OnRobotSetDataRsp(long l, String uuid, ArrayList<JFGDPMsgRet> arrayList) {
        AppLogger.d("OnRobotSetDataRsp :" + l + new Gson().toJson(arrayList));
        RxBus.getCacheInstance().post(new RxEvent.SetDataRsp(l, arrayList));
        RxBus.getCacheInstance().post(new RxEvent.SdcardClearReqRsp(l, arrayList));
    }

    @Override
    public void OnRobotGetDataTimeout(long l, String s) {
        AppLogger.d("OnRobotGetDataTimeout :");
    }

//    @Override
//    public void OnRobotSetDataRsp(long l, ArrayList<JFGDPMsgRet> arrayList) {
//        AppLogger.d("OnRobotSetDataRsp :" + l + new Gson().toJson(arrayList));
//        RxBus.getCacheInstance().post(new RxEvent.SetDataRsp(l, arrayList));
//        RxBus.getCacheInstance().post(new RxEvent.SdcardClearReqRsp(l, arrayList));
//    }
//
//    @Override
//    public void OnRobotGetDataTimeout(long l) {
//        AppLogger.d("OnRobotGetDataTimeout :");
//    }

    @Override
    public ArrayList<JFGDPMsg> OnQuerySavedDatapoint(String s, ArrayList<JFGDPMsg> arrayList) {
        return null;
    }

    @Override
    public void OnlineStatus(boolean b) {
        AppLogger.d("OnlineStatus :" + b);
        RxBus.getCacheInstance().post(new RxEvent.OnlineStatusRsp(b));
        DataSourceManager.getInstance().setOnline(b);//设置用户在线信息
    }

    @Override
    public void OnResult(JFGResult jfgResult) {
        RxBus.getCacheInstance().post(jfgResult);
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
                RxBus.getCacheInstance().postSticky(new RxEvent.ResultLogin(jfgResult.code));
                RxBus.getCacheInstance().post(new RxEvent.ResultUserLogin(jfgResult.code));
                break;
            case JResultEvent.JFG_RESULT_BINDDEV:
                //绑定设备
                RxBus.getCacheInstance().postSticky(new RxEvent.BindDeviceEvent(jfgResult.code));
                break;
            case JResultEvent.JFG_RESULT_UNBINDDEV:
                RxBus.getCacheInstance().post(new RxEvent.UnBindDeviceEvent(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_CHANGE_PASS:
                RxBus.getCacheInstance().post(new RxEvent.ChangePwdBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_RESET_PASS:
                RxBus.getCacheInstance().post(new RxEvent.ResetPwdBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_ADD_FRIEND:
                RxBus.getCacheInstance().post(new RxEvent.AddFriendBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_CONSENT_ADD_FRIEND:
                RxBus.getCacheInstance().post(new RxEvent.ConsentAddFriendBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_DEL_FRIEND:
                RxBus.getCacheInstance().post(new RxEvent.DelFriendBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_SETPWD_WITH_BINDACCOUNT:
                RxBus.getCacheInstance().post(new RxEvent.OpenLogInSetPwdBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_SEND_FEEDBACK:
                RxBus.getCacheInstance().post(new RxEvent.SendFeekBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_DEL_FRIEND_ADD_REQ:
                RxBus.getCacheInstance().post(new RxEvent.DeleteAddReqBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_CHECK_REGISTER:
                RxBus.getCacheInstance().post(new RxEvent.CheckRegsiterBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_SET_DEVICE_ALIAS:
                RxBus.getCacheInstance().post(new RxEvent.SetAlias(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_SET_FRIEND_MARKNAME:
                RxBus.getCacheInstance().post(new RxEvent.SetFriendMarkNameBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_UPDATE_ACCOUNT:
                RxBus.getCacheInstance().post(new RxEvent.RessetPhoneBack(jfgResult));
                break;
        }
        if (login) {
            AfterLoginService.startGetAccountAction(ContextUtils.getContext());
            AfterLoginService.startSaveAccountAction(ContextUtils.getContext());
            AfterLoginService.resumeOfflineRequest();
        }
        AppLogger.i("jfgResult:[event:" + jfgResult.event + ",code:" + jfgResult.code + ",seq:" + jfgResult.seq + "]");
    }

    @Override
    public void OnDoorBellCall(JFGDoorBellCaller jfgDoorBellCaller) {
        AppLogger.d("OnDoorBellCall :");
        RxBus.getCacheInstance().post(new RxEvent.BellCallEvent(jfgDoorBellCaller));
    }

    @Override
    public void OnOtherClientAnswerCall(String s) {
        AppLogger.d("OnOtherClientAnswerCall");
        RxBus.getCacheInstance().post(new RxEvent.CallResponse(false));
    }

    @Override
    public void OnRobotCountDataRsp(long l, String s, ArrayList<JFGDPMsgCount> arrayList) {
//        DataSourceManager.getInstance().cacheUnreadCount(l, s, arrayList);
//        AppLogger.d("OnRobotCountDataRsp :");
    }

    @Override
    public void OnRobotDelDataRsp(long l, String s, int i) {
        AppLogger.d("OnRobotDelDataRsp :" + l + " uuid:" + s + " i:" + i);
        RxBus.getCacheInstance().post(new RxEvent.DeleteDataRsp(l, s, i));
    }

    @Override
    public void OnRobotSyncData(boolean b, String s, ArrayList<JFGDPMsg> arrayList) {
        AppLogger.d("OnRobotSyncData :" + b + " " + s + " " + new Gson().toJson(arrayList));
        RxBus.getCacheInstance().post(new RxEvent.SerializeCacheSyncDataEvent(b, s, arrayList));
        RxBus.getCacheInstance().post(new RxEvent.SdcardClearFinishRsp(b, s, arrayList));
    }

    @Override
    public void OnSendSMSResult(int i, String s) {
        AppLogger.d("OnSendSMSResult :" + i + "," + s);
        RxBus.getCacheInstance().post(new RxEvent.SmsCodeResult(i, s));
    }

    @Override
    public void OnGetFriendListRsp(int i, ArrayList<JFGFriendAccount> arrayList) {
        AppLogger.d("OnLocalMessage :" + arrayList.size());
        RxBus.getCacheInstance().post(new RxEvent.GetFriendList(i, arrayList));
    }

    @Override
    public void OnGetFriendRequestListRsp(int i, ArrayList<JFGFriendRequest> arrayList) {
        AppLogger.d("OnLocalMessage:" + arrayList.size());
        RxBus.getCacheInstance().post(new RxEvent.GetAddReqList(i, arrayList));
    }

    @Override
    public void OnGetFriendInfoRsp(int i, JFGFriendAccount jfgFriendAccount) {
        AppLogger.d("OnLocalMessage :" + new Gson().toJson(jfgFriendAccount));
        RxBus.getCacheInstance().post(new RxEvent.GetFriendInfoCall(i, jfgFriendAccount));
    }

    @Override
    public void OnCheckFriendAccountRsp(int i, String s, String s1, boolean b) {
        AppLogger.d("OnLocalMessage :");
        RxBus.getCacheInstance().post(new RxEvent.CheckAccountCallback(i, s, s1, b));
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
        RxBus.getCacheInstance().post(new RxEvent.UnshareDeviceCallBack(i, s, s1));
    }

    @Override
    public void OnGetShareListRsp(int i, ArrayList<JFGShareListInfo> arrayList) {
        AppLogger.d("OnGetShareListRsp :");
        RxBus.getCacheInstance().post(new RxEvent.GetShareListCallBack(i, arrayList));
        DataSourceManager.getInstance().cacheShareList(arrayList);
    }

    @Override
    public void OnGetUnShareListByCidRsp(int i, ArrayList<JFGFriendAccount> arrayList) {
        AppLogger.d("OnGetUnShareListByCidRsp :");
        RxBus.getCacheInstance().post(new RxEvent.GetHasShareFriendCallBack(i, arrayList));
    }

    @Override
    public void OnUpdateNTP(int l) {
        AppLogger.d("OnUpdateNTP :" + l);
        PreferencesUtils.putInt(JConstant.KEY_NTP_INTERVAL, (int) (System.currentTimeMillis() / 1000 - l));
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
        if (RxBus.getCacheInstance().hasObservers()) {
            RxBus.getCacheInstance().post(new RxEvent.GetFeedBackRsp(i, arrayList));
        }
    }

    @Override
    public void OnCheckDevVersionRsp(boolean b, String s, String s1, String s2, String s3) {
        AppLogger.d("OnCheckDevVersionRsp :");
        if (RxBus.getCacheInstance().hasObservers()) {
            RxBus.getCacheInstance().post(new RxEvent.CheckDevVersionRsp(b, s, s1, s2, s3));
        }
    }

    @Override
    public void OnNotifyStorageType(int i) {
        AppLogger.d("I:" + i);
    }

    @Override
    public void OnBindDevRsp(int i, String s) {
        AppLogger.d("onBindDev: " + i + " uuid:" + s);
        RxBus.getCacheInstance().postSticky(new RxEvent.BindDeviceEvent(i, s));
        PreferencesUtils.putString(JConstant.BINDING_DEVICE, "");
    }

    @Override
    public void OnUnBindDevRsp(int i, String s) {
        AppLogger.d(String.format(Locale.getDefault(), "OnUnBindDevRsp:%d,%s", i, s));
    }

    @Override
    public void OnGetVideoShareUrl(String s) {
        AppLogger.d(String.format(Locale.getDefault(), "OnGetVideoShareUrl:%s", s));
    }

    @Override
    public void OnForwardData(byte[] bytes) {
        try {
            PanoramaEvent.RawRspMsg rawRspMsg = DpUtils.unpackData(bytes, PanoramaEvent.RawRspMsg.class);
            RxBus.getCacheInstance().post(rawRspMsg);
            AppLogger.d("OnForwardData:" + new Gson().toJson(rawRspMsg));
        } catch (IOException e) {
            e.printStackTrace();
            AppLogger.e("OnForwardData:解析局域网消息失败!!!");
        }
    }

    @Override
    public void OnMultiShareDevices(int i, String s, String s1) {
        AppLogger.d(String.format(Locale.getDefault(), "check OnMultiShareDevices:%d,%s,%s", i, s, s1));
    }

    @Override
    public void OnCheckClientVersion(int i, String s, int i1) {
        AppLogger.d(String.format(Locale.getDefault(), "check version:%d,%s,%d", i, s, i1));
    }
}
