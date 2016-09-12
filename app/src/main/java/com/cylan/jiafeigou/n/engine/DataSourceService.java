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
import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.entity.jniCall.JFGHistoryVideo;
import com.cylan.entity.jniCall.JFGHistoryVideoErrorInfo;
import com.cylan.entity.jniCall.JFGMessageInfo;
import com.cylan.entity.jniCall.JFGMsgHttpResult;
import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.entity.jniCall.JFGServerCfg;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.entity.jniCall.RobotMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jfgapp.interfases.AppCallBack;
import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.stat.MtaManager;

import java.util.ArrayList;


public class DataSourceService extends Service implements AppCallBack {

    static {
        System.loadLibrary("jfgsdk");
        System.loadLibrary("sqlcipher");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initNative();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void initNative() {
        try {
            JfgAppCmd.initJfgAppCmd(this, this, JConstant.LOG_PATH);
        } catch (PackageManager.NameNotFoundException e) {
            AppLogger.d("let's go err:" + e.getLocalizedMessage());
        }
        AppLogger.d("let's go initNative:");
        MtaManager.customEvent(this, "DataSourceService", "NativeInit");
    }


    @Override
    public void OnLocalMessage(String s, int i, byte[] bytes) {

    }

    @Override
    public void OnReportJfgDevices(JFGDevice[] jfgDevices) {

    }

    @Override
    public void OnUpdateAccount(JFGAccount jfgAccount) {

    }

    @Override
    public void OnUpdateHistoryVideoList(JFGHistoryVideo jfgHistoryVideo) {

    }

    @Override
    public void OnUpdateHistoryErrorCode(JFGHistoryVideoErrorInfo jfgHistoryVideoErrorInfo) {

    }

    @Override
    public void OnServerPushMessage(JFGMessageInfo jfgMessageInfo) {

    }

    @Override
    public void OnServerConfig(JFGServerCfg jfgServerCfg) {

    }

    @Override
    public void OnLogoutByServer(int i) {

    }

    @Override
    public void OnVideoDisconnect(JFGMsgVideoDisconn jfgMsgVideoDisconn) {

    }

    @Override
    public void OnVideoNotifyResolution(JFGMsgVideoResolution jfgMsgVideoResolution) {

    }

    @Override
    public void OnVideoNotifyRTCP(JFGMsgVideoRtcp jfgMsgVideoRtcp) {

    }

    @Override
    public void OnHttpDone(JFGMsgHttpResult jfgMsgHttpResult) {

    }

    @Override
    public void OnRobotTransmitMsg(RobotMsg robotMsg) {

    }

    @Override
    public void OnRobotMsgAck(int i) {

    }

    @Override
    public void OnRobotGetDataRsp(RobotoGetDataRsp robotoGetDataRsp) {

    }

    @Override
    public void OnRobotSetDataRsp(long l, ArrayList<JFGDPMsgRet> arrayList) {

    }

    @Override
    public void OnRobotGetDataTimeout(long l) {

    }

    @Override
    public ArrayList<JFGDPMsg> OnQuerySavedDatapoint(String s, ArrayList<JFGDPMsg> arrayList) {
        return null;
    }

    @Override
    public void OnlineStatus(boolean b) {

    }

    @Override
    public void OnResult(int i, int i1) {

    }

    @Override
    public void OnDoorBellCall(JFGDoorBellCaller jfgDoorBellCaller) {

    }

    @Override
    public void OnOtherClientAnswerCall() {

    }

    @Override
    public void OnRobotCountDataRsp(long l, String s, ArrayList<JFGDPMsgCount> arrayList) {

    }

    @Override
    public void OnRobotDelDataRsp(long l, String s, int i) {

    }

    @Override
    public void OnRobotSyncData(boolean b, String s, ArrayList<JFGDPMsg> arrayList) {

    }

    @Override
    public void OnSendSMSResult(int i, String s) {

    }

    @Override
    public void OnGetFriendListRsp(int i, ArrayList<JFGFriendAccount> arrayList) {

    }

    @Override
    public void OnGetFriendRequestListRsp(int i, ArrayList<JFGFriendRequest> arrayList) {

    }

    @Override
    public void OnGetFriendInfoRsp(int i, JFGFriendAccount jfgFriendAccount) {

    }

    @Override
    public void OnCheckFriendAccountRsp(int i, String s, String s1, boolean b) {

    }

    @Override
    public void OnShareDeviceRsp(int i, String s, String s1) {

    }

    @Override
    public void OnUnShareDeviceRsp(int i, String s, String s1) {

    }

    @Override
    public void OnGetShareListRsp(int i, ArrayList<JFGShareListInfo> arrayList) {

    }

    @Override
    public void OnGetUnShareListByCidRsp(int i, ArrayList<JFGFriendAccount> arrayList) {

    }

    @Override
    public void OnUpdateNTP(long l) {

    }

    @Override
    public void OnEfamilyMsg(byte[] bytes) {

    }
}
