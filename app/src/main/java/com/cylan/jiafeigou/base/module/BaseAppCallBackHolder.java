package com.cylan.jiafeigou.base.module;

import android.text.TextUtils;

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
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.ClientUpdateManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by yanzhendong on 2017/4/14.
 */

public class BaseAppCallBackHolder implements AppCallBack {
    /**
     * 不要在这个类里做复杂的逻辑处理,所有的消息都应该以 RxBus 发送出去,在对应的地方再做处理
     */
    private Gson gson = new Gson();

    @Override
    public void OnLocalMessage(String s, int i, byte[] bytes) {
        AppLogger.d("OnLocalMessage :" + s + ",i:" + i);
        RxBus.getCacheInstance().post(new RxEvent.LocalUdpMsg(System.currentTimeMillis(), s, (short) i, bytes));
    }

    @Override
    public void OnReportJfgDevices(JFGDevice[] jfgDevices) {
        AppLogger.d("OnReportJfgDevices" + gson.toJson(jfgDevices));
        RxBus.getCacheInstance().post(new RxEvent.SerializeCacheDeviceEvent(jfgDevices));
    }

    @Override
    public void OnUpdateAccount(JFGAccount jfgAccount) {
        AppLogger.d("OnUpdateAccount :" + jfgAccount.getEmail());
        RxBus.getCacheInstance().post(new RxEvent.SerializeCacheAccountEvent(jfgAccount));
    }

    @Override
    public void OnUpdateHistoryVideoList(JFGHistoryVideo jfgHistoryVideo) {
        AppLogger.d("OnUpdateHistoryVideoList :" + jfgHistoryVideo.list.size());
        BaseApplication.getAppComponent().getSourceManager().cacheHistoryDataList(jfgHistoryVideo);
    }

    @Override
    public void OnUpdateHistoryErrorCode(JFGHistoryVideoErrorInfo jfgHistoryVideoErrorInfo) {
        AppLogger.d("OnUpdateHistoryErrorCode :" + gson.toJson(jfgHistoryVideoErrorInfo));
        RxBus.getCacheInstance().post(jfgHistoryVideoErrorInfo);
    }

    @Override
    public void OnServerConfig(JFGServerCfg jfgServerCfg) {
        AppLogger.d("OnServerConfig :" + gson.toJson(jfgServerCfg));
        RxBus.getCacheInstance().post(jfgServerCfg);
    }

    @Override
    public void OnLogoutByServer(int i) {
        AppLogger.d("OnLogoutByServer:" + i);
        RxBus.getCacheInstance().post(new RxEvent.PwdHasResetEvent(i));
        BaseApplication.getAppComponent().getSourceManager().setLoginState(new LogState(LogState.STATE_ACCOUNT_OFF));
    }

    @Override
    public void OnVideoDisconnect(JFGMsgVideoDisconn jfgMsgVideoDisconn) {
        AppLogger.d("OnVideoDisconnect :" + gson.toJson(jfgMsgVideoDisconn));
        RxBus.getCacheInstance().post(jfgMsgVideoDisconn);
    }

    @Override
    public void OnVideoNotifyResolution(JFGMsgVideoResolution jfgMsgVideoResolution) {
        AppLogger.d("OnVideoNotifyResolution" + jfgMsgVideoResolution.peer);
        RxBus.getCacheInstance().post(jfgMsgVideoResolution);
    }

    @Override
    public void OnVideoNotifyRTCP(JFGMsgVideoRtcp jfgMsgVideoRtcp) {
        AppLogger.d("OnVideoNotifyRTCP" + gson.toJson(jfgMsgVideoRtcp));
        RxBus.getCacheInstance().post(jfgMsgVideoRtcp);
    }

    @Override
    public void OnHttpDone(JFGMsgHttpResult jfgMsgHttpResult) {
        AppLogger.d("OnHttpDone :" + gson.toJson(jfgMsgHttpResult));
        RxBus.getCacheInstance().post(jfgMsgHttpResult);
    }

    @Override
    public void OnRobotTransmitMsg(RobotMsg robotMsg) {
        AppLogger.d("OnRobotTransmitMsg :" + gson.toJson(robotMsg));
        RxBus.getCacheInstance().post(robotMsg);
    }

    @Override
    public void OnRobotMsgAck(int i) {
        AppLogger.d("OnRobotMsgAck :" + i);
    }

    @Override
    public void OnRobotGetDataRsp(RobotoGetDataRsp robotoGetDataRsp) {
        AppLogger.d("OnRobotGetDataRsp :" + gson.toJson(robotoGetDataRsp));
        RxBus.getCacheInstance().post(new RxEvent.SerializeCacheGetDataEvent(robotoGetDataRsp));
    }

    @Override
    public void OnRobotGetDataExRsp(long l, String s, ArrayList<JFGDPMsg> arrayList) {
        RobotoGetDataRsp robotoGetDataRsp = new RobotoGetDataRsp();
        robotoGetDataRsp.identity = s;
        robotoGetDataRsp.seq = l;
        robotoGetDataRsp.put(-1, arrayList);//key在这种情况下无用
        RxBus.getCacheInstance().post(new RxEvent.SerializeCacheGetDataEvent(robotoGetDataRsp));
        AppLogger.d("OnRobotGetDataExRsp :" + gson.toJson(arrayList));
    }

    @Override
    public void OnRobotSetDataRsp(long l, String uuid, ArrayList<JFGDPMsgRet> arrayList) {
        AppLogger.d("OnRobotSetDataRsp :" + l + gson.toJson(arrayList));
        RxBus.getCacheInstance().post(new RxEvent.SetDataRsp(l, arrayList));
    }

    @Override
    public void OnRobotGetDataTimeout(long l, String s) {
        AppLogger.d("OnRobotGetDataTimeout :" + l + ":" + s);
    }

    @Override
    public ArrayList<JFGDPMsg> OnQuerySavedDatapoint(String s, ArrayList<JFGDPMsg> arrayList) {
        return null;
    }

    @Override
    public void OnlineStatus(boolean b) {
        AppLogger.d("OnlineStatus :" + b);
        RxBus.getCacheInstance().post(new RxEvent.OnlineStatusRsp(b));
        BaseApplication.getAppComponent().getSourceManager().setOnline(b);//设置用户在线信息
    }

    @Override
    public void OnResult(JFGResult jfgResult) {
        RxBus.getCacheInstance().post(jfgResult);
        AppLogger.i("jfgResult:");
    }

    @Override
    public void OnDoorBellCall(JFGDoorBellCaller jfgDoorBellCaller) {
        AppLogger.d("OnDoorBellCall :" + gson.toJson(jfgDoorBellCaller));
        RxBus.getCacheInstance().post(new RxEvent.BellCallEvent(jfgDoorBellCaller, false));
    }

    @Override
    public void OnOtherClientAnswerCall(String s) {
        AppLogger.d("OnOtherClientAnswerCall:" + s);
        RxBus.getCacheInstance().post(new RxEvent.CallResponse(false));
    }

    @Override
    public void OnRobotCountDataRsp(long l, String s, ArrayList<JFGDPMsgCount> arrayList) {
//        BaseApplication.getAppComponent().getSourceManager().cacheUnreadCount(l, s, arrayList);
        AppLogger.d("OnRobotCountDataRsp :" + l + ":" + s + "");
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
        AppLogger.d("OnShareDeviceRsp :" + i + ":" + s + ":" + s1);
        RxBus.getCacheInstance().post(new RxEvent.ShareDeviceCallBack(i, s, s1));
    }

    @Override
    public void OnUnShareDeviceRsp(int i, String s, String s1) {
        AppLogger.d("OnUnShareDeviceRsp :");
        RxBus.getCacheInstance().post(new RxEvent.UnshareDeviceCallBack(i, s, s1));
    }

    @Override
    public void OnGetShareListRsp(int i, ArrayList<JFGShareListInfo> arrayList) {
        AppLogger.d("OnGetShareListRsp :" + i);
        RxBus.getCacheInstance().post(new RxEvent.GetShareListCallBack(i, arrayList));
        BaseApplication.getAppComponent().getSourceManager().cacheShareList(arrayList);
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
        RxBus.getCacheInstance().post(new RxEvent.ForgetPwdByMail(s));
    }

    @Override
    public void OnGetAliasByCidRsp(int i, String s) {
        AppLogger.d("OnGetAliasByCidRsp :" + i + ":" + s);
    }

    @Override
    public void OnGetFeedbackRsp(int i, ArrayList<JFGFeedbackInfo> arrayList) {
        AppLogger.d("OnGetFeedbackRsp :" + i);
        RxBus.getCacheInstance().post(new RxEvent.GetFeedBackRsp(i, arrayList));
    }

    @Override
    public void OnCheckDevVersionRsp(boolean b, String s, String s1, String s2, String s3) {
        AppLogger.d("OnCheckDevVersionRsp :" + b + ":" + s + ":" + s1 + ":" + s2 + ":" + s3);
        RxBus.getCacheInstance().post(new RxEvent.CheckDevVersionRsp(b, s, s1, s2, s3));
    }

    @Override
    public void OnNotifyStorageType(int i) {
        AppLogger.d("OnNotifyStorageType:" + i);
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
            AppLogger.d("OnForwardData:" + gson.toJson(rawRspMsg));
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
        // 客户端升级测试
        if (!TextUtils.isEmpty(s)) {
//            String url = "http://121.15.220.150/imtt.dd.qq.com/16891/AE6502757AE91F88EE91D985D5AAE5AD.apk?mkey=58eb4c2e30b4058c&f=2409&c=0&fsname=com.cylan.jiafeigou_2.4.9.5296_20170228.apk&csr=1bbd&p=.apk";
            String url = "http://d.app8h.com/d1/966/5708/Clever%20Dog.apk";
            ClientUpdateManager.getInstance().startDownload(getApplicationContext(), url, "3.2.0", 1);
        }
    }

    @Override
    public void OnRobotCountMultiDataRsp(long l, Object o) {
        AppLogger.d("OnRobotCountMultiDataRsp:" + o.toString());
    }

    @Override
    public void OnRobotGetMultiDataRsp(long l, Object o) {
        AppLogger.d("OnRobotGetMultiDataRsp:" + l + ":" + o);
    }
}
