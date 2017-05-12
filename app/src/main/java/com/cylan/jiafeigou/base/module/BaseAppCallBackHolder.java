package com.cylan.jiafeigou.base.module;

import android.util.Log;

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
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import static com.cylan.jiafeigou.dp.DpUtils.unpackData;

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
//        AppLogger.d("OnLocalMessage :" + s + ",i:" + i);
        RxBus.getCacheInstance().post(new RxEvent.LocalUdpMsg(System.currentTimeMillis(), s, (short) i, bytes));
    }

    @Override
    public void OnReportJfgDevices(JFGDevice[] jfgDevices) {
        AppLogger.d("OnReportJfgDevices" + gson.toJson(jfgDevices));
        RxBus.getCacheInstance().post(new RxEvent.SerializeCacheDeviceEvent(jfgDevices));
    }

    @Override
    public void OnUpdateAccount(JFGAccount jfgAccount) {
        AppLogger.d("OnUpdateAccount :" + gson.toJson(jfgAccount));
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
        Log.d("", "OnVideoNotifyRTCP" + gson.toJson(jfgMsgVideoRtcp));
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
        AppLogger.d("OnRobotGetDataExRsp :" + s + "," + gson.toJson(arrayList));
    }

    @Override
    public void OnRobotSetDataRsp(long l, String uuid, ArrayList<JFGDPMsgRet> arrayList) {
        AppLogger.d("OnRobotSetDataRsp :" + l + gson.toJson(arrayList));
        RxBus.getCacheInstance().post(new RxEvent.SetDataRsp(l, uuid, arrayList));
    }

    @Override
    public void OnRobotGetDataTimeout(long l, String s) {
        AppLogger.d("OnRobotGetDataTimeout :" + l + ":" + s);
    }

    @Override
    public ArrayList<JFGDPMsg> OnQuerySavedDatapoint(String s, ArrayList<JFGDPMsg> arrayList) {
        AppLogger.e("这是一个bug");
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
        AppLogger.i("jfgResult:" + jfgResult.code);
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
        AppLogger.d("OnGetFeedbackRsp :" + ListUtils.getSize(arrayList));
        RxBus.getCacheInstance().postSticky(new RxEvent.GetFeedBackRsp(i, arrayList));
    }

    @Override
    public void OnCheckDevVersionRsp(boolean b, String s, String s1, String s2, String s3, String s4) {
        AppLogger.d("OnCheckDevVersionRsp :" + b + ":" + s + ":" + s1 + ":" + s2 + ":" + s3);
//        b = true;
//        s = "http://yf.cylan.com.cn:82/Garfield/JFG2W/3.0.0/3.0.0.1000/201704261515/hi.bin";
//        s1 = "3.0.0";
//        s2 = "你好";
//        s3 = "xx";
        RxBus.getCacheInstance().post(new RxEvent.CheckVersionRsp(b, s, s1, s2, s3).setUuid(s4).setSeq(0));
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
            PanoramaEvent.MsgForward rawRspMsg = DpUtils.unpackData(bytes, PanoramaEvent.MsgForward.class);
            RxBus.getCacheInstance().post(rawRspMsg);
            AppLogger.d("收到服务器透传消息:" + new Gson().toJson(rawRspMsg));
            AppLogger.e("尝试解析服务器透传消息:" + new Gson().toJson((Object) parse(rawRspMsg)));
        } catch (IOException e) {
            e.printStackTrace();
            AppLogger.e("解析服务器透传消息失败");
        }
    }

    private <T> T parse(PanoramaEvent.MsgForward forward) {
        AppLogger.d("收到服务器的透传消息:" + new Gson().toJson(forward));
        try {
            switch (forward.type) {
                case 1:       //TYPE_FILE_DOWNLOAD_REQ          = 1   下载请求
                case 2:       //TYPE_FILE_DOWNLOAD_RSP          = 2   下载响应
                case 3:       //TYPE_FILE_DELETE_REQ            = 3   删除请求
                case 4:       //TYPE_FILE_DELETE_RSP            = 4   删除响应
                    return (T) unpackData(forward.msg, PanoramaEvent.MsgFileRsp.class);
                case 5:       //TYPE_FILE_LIST_REQ              = 5   列表请求
                case 6:       //TYPE_FILE_LIST_RSP              = 6   列表响应
                    return (T) unpackData(forward.msg, PanoramaEvent.MsgFileListRsp.class);
                case 7:       //TYPE_TAKE_PICTURE_REQ           = 7   拍照请求
                case 8:       //TYPE_TAKE_PICTURE_RSP           = 8   拍照响应
                    PanoramaEvent.TP tp = unpackData(forward.msg, PanoramaEvent.TP.class);
                    PanoramaEvent.MsgFileRsp fileRsp = new PanoramaEvent.MsgFileRsp();
                    fileRsp.ret = tp.ret;
                    fileRsp.files = Collections.singletonList(tp.pitcure);
                    return (T) fileRsp;
                case 9:       //TYPE_VIDEO_BEGIN_REQ            = 9   开始录像请求
                case 10:      //TYPE_VIDEO_BEGIN_RSP            = 10  开始录像响应
                    return (T) unpackData(forward.msg, PanoramaEvent.MsgRsp.class);
                case 11:      //TYPE_VIDEO_END_REQ              = 11  停止录像请求
                case 12:      //TYPE_VIDEO_END_RSP              = 12  停止录像响应
                    PanoramaEvent.TP sp = unpackData(forward.msg, PanoramaEvent.TP.class);
                    PanoramaEvent.MsgFileRsp frsp = new PanoramaEvent.MsgFileRsp();
                    frsp.ret = sp.ret;
                    frsp.files = Collections.singletonList(sp.pitcure);
                    return (T) frsp;
                case 13:      //TYPE_VIDEO_STATUS_REQ           = 13  查询录像状态请求
                case 14:      //TYPE_VIDEO_STATUS_RSP           = 14  查询录像状态响应
                    return (T) unpackData(forward.msg, PanoramaEvent.MsgVideoStatusRsp.class);
                case 15:      //TYPE_FILE_LOGO_REQ              = 15  设置水印请求
                case 16:      //TYPE_FILE_LOGO_RSP              = 16  设置水印响应
                    return (T) unpackData(forward.msg, PanoramaEvent.MsgRsp.class);
                case 17:      //TYPE_FILE_RESOLUTION_REQ        = 17  设置视频分辨率请求
                case 18:      //TYPE_FILE_RESOLUTION_RSP        = 18  视频分辨率响应
                    return (T) unpackData(forward.msg, PanoramaEvent.MsgResolutionRsp.class);
                case 20:      //TYPE_FILE_GET_LOGO_RSP          = 20  查询水印响应
                    return (T) unpackData(forward.msg, PanoramaEvent.MsgLogoRsp.class);
                case 21:      //TYPE_FILE_GET_RESOLUTION_REQ    = 21  查询视频分辨率请求
                case 22:      //TYPE_FILE_GET_RESOLUTION_RSP    = 22  查询视频分辨率响应
                    return (T) unpackData(forward.msg, PanoramaEvent.MsgResolutionRsp.class);
            }
        } catch (Exception e) {
            AppLogger.e(e.getMessage());
        }
        return null;
    }

    @Override
    public void OnMultiShareDevices(int i, String s, String s1) {
        AppLogger.d(String.format(Locale.getDefault(), "check OnMultiShareDevices:%d,%s,%s", i, s, s1));
    }

    @Override
    public void OnCheckClientVersion(int i, String s, int i1) {
        RxBus.getCacheInstance().post(new RxEvent.ClientCheckVersion(i, s, i1));
    }

    @Override
    public void OnRobotCountMultiDataRsp(long l, Object o) {
        AppLogger.d("OnRobotCountMultiDataRsp:" + o.toString());
    }

    @Override
    public void OnRobotGetMultiDataRsp(long l, Object o) {
        AppLogger.d("OnRobotGetMultiDataRsp:" + l + ":" + o);
    }

    @Override
    public void OnGetAdPolicyRsp(int i, long l, String s, String s1) {
        AppLogger.d(String.format("OnGetAdPolicyRsp:ret:%s,time:%s,picUrl:%s,tagUrl:%s", i, l, s, s1));
    }
}
