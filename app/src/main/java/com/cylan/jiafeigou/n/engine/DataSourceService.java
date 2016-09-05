package com.cylan.jiafeigou.n.engine;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;

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
import com.cylan.entity.jniCall.RobotMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jfgapp.interfases.AppCallBack;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.stat.MtaManager;

import java.util.ArrayList;


public class DataSourceService extends Service implements AppCallBack {
    private Handler workHandler;

    static {
        System.loadLibrary("jfgsdk");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initLogUtil();
        initHandler();
        workHandler.sendEmptyMessageDelayed(0, 1000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void initNative() {
        AppLogger.d("let's go initNative:");
        MtaManager.customEvent(this, "DataSourceService", "NativeInit");
    }


    /**
     * 初始化日志
     */
    private void initLogUtil() {

    }


    private void initHandler() {
        HandlerThread workThread = new HandlerThread("work");
        workThread.start();
        AppLogger.i("start workThread !");
        workHandler = new Handler(workThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        //init jni
                        workHandler.sendEmptyMessage(1);
                        break;
                    case 1:
                        break;

                }
                return true;
            }
        });
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
    public void OnRobotCountDataRsp(long l, ArrayList<JFGDPMsgCount> arrayList) {

    }

    @Override
    public void OnRobotDelDataRsp(long l, int i) {

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
}
