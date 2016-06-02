package com.cylan.jiafeigou.n.engine;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;

import com.cylan.entity.JfgEnum;
import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGCameraSettings;
import com.cylan.entity.jniCall.JFGDelMsg;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.JFGDoorBellCaller;
import com.cylan.entity.jniCall.JFGDoorSensorSettings;
import com.cylan.entity.jniCall.JFGHistoryVideo;
import com.cylan.entity.jniCall.JFGHistoryVideoErrorInfo;
import com.cylan.entity.jniCall.JFGMessage;
import com.cylan.entity.jniCall.JFGMessageInfo;
import com.cylan.entity.jniCall.JFGMsgHttpResult;
import com.cylan.entity.jniCall.JFGMsgTransportFail;
import com.cylan.entity.jniCall.JFGMsgTransportReady;
import com.cylan.entity.jniCall.JFGMsgVideoRecvCall;
import com.cylan.entity.jniCall.JFGMsgVideoRecvDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.entity.jniCall.JFGServerCfg;
import com.cylan.entity.jniCall.JFGStatus;
import com.cylan.entity.jniCall.RobotMsg;
import com.cylan.interfaces.JniCallBack;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.cylan.jiafeigou.support.stat.MtaManager;
import com.cylan.sdkjni.JfgCmd;
import com.superlog.LogLevel;
import com.superlog.SLog;

import java.util.ArrayList;


public class DataSourceService extends Service implements JniCallBack {
    private HandlerThread workThread;
    private Handler workHandler;
    private String logPath;
    JfgCmd cmd;
    RxBus rxBus;

    static {
        System.loadLibrary("jfgsdk");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initNative();
        rxBus = RxBus.getInstance();
        cmd = JfgCmd.getJfgCmd(this);
        logPath = Environment.getExternalStorageDirectory().getPath() + "/JFG";
        initLogUtil(logPath);
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
        SLog.d("let's go initNative:");
        MtaManager.customEvent(this, "DataSourceService", "NativeInit");
    }


    /**
     * 初始化日志
     *
     * @param path
     */
    private void initLogUtil(String path) {
        SLog.init("JFG")//自定义
                .setLogDir(path) //保存到此目录下
                .setWriteToFile(true)//写入到文件
                .setDebug(BuildConfig.DEBUG)//调试模式
                .setLogLevel(LogLevel.INFO);//写入级别,info 以下不写入文件
    }


    private void initHandler() {
        workThread = new HandlerThread("work");
        workThread.start();
        SLog.i("start workThread !");

        workHandler = new Handler(workThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        //init jni
                        boolean initResult = cmd.initJni(logPath, true, DataSourceService.this);
                        workHandler.sendEmptyMessage(1);
                        break;
                    case 1:
                        cmd.connectServer(JfgEnum.ServerAddr.YF);

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
    public void OnReprotJfgDevices(JFGDevice[] jfgDevices) {
        if (rxBus.hasObservers()) {
            rxBus.send(jfgDevices);
        }
    }

    @Override
    public void OnUpdateDevStatus(JFGStatus jfgStatus) {

    }

    @Override
    public void OnServerConnected() {
        SLog.i("OnServerConnected");
        workHandler.removeMessages(1);
        if (rxBus.hasObservers()) {
            rxBus.send("test");
        }


    }

    @Override
    public void OnServerDisconnected() {
        SLog.i("OnServerDisconnected");
        workHandler.sendEmptyMessageDelayed(1, 3000); //re connect server
    }

    @Override
    public void OnRecvCall(JFGMsgVideoRecvCall jfgMsgVideoRecvCall) {

    }

    @Override
    public void OnRecvDisconnect(JFGMsgVideoRecvDisconn jfgMsgVideoRecvDisconn) {

    }

    @Override
    public void OnTransportReady(JFGMsgTransportReady jfgMsgTransportReady) {

    }

    @Override
    public void OnTransportFail(JFGMsgTransportFail jfgMsgTransportFail) {

    }

    @Override
    public void OnNotifyResolution(JFGMsgVideoResolution jfgMsgVideoResolution) {

    }

    @Override
    public void OnNotifyRTCP(JFGMsgVideoRtcp jfgMsgVideoRtcp) {

    }

    @Override
    public void OnHttpDone(JFGMsgHttpResult jfgMsgHttpResult) {

    }

    @Override
    public void OnLoginResult(int i, String s) {

    }

    @Override
    public void OnUpdateAccount(JFGAccount jfgAccount) {

    }

    @Override
    public void OnUpdateCameraSettings(JFGCameraSettings jfgCameraSettings) {

    }

    @Override
    public void OnUpdateDoorSensorSettings(JFGDoorSensorSettings jfgDoorSensorSettings) {

    }

    @Override
    public void OnUpdateHistoryVideoList(JFGHistoryVideo jfgHistoryVideo) {

    }

    @Override
    public void OnServerConfig(JFGServerCfg jfgServerCfg) {

    }

    @Override
    public void OnUpdateMessage(ArrayList<JFGMessage> arrayList) {

    }

    @Override
    public void OnUpdateMessageInfoByCid(ArrayList<JFGMessageInfo> arrayList) {

    }

    @Override
    public void OnUpdateHistoryErrorCode(JFGHistoryVideoErrorInfo jfgHistoryVideoErrorInfo) {

    }

    @Override
    public void OnServerPushMessage(JFGMessageInfo jfgMessageInfo) {

    }

    @Override
    public void OnUpdateDelMsg(JFGDelMsg jfgDelMsg) {

    }

    @Override
    public void OnDoorBellCall(JFGDoorBellCaller jfgDoorBellCaller) {

    }

    @Override
    public void OnUpdateCallerInfos(ArrayList<JFGDoorBellCaller> arrayList) {

    }

    @Override
    public void OnError(int i) {

    }

    @Override
    public void OnUpdateDelCallerInfo() {

    }

    @Override
    public void OnOtherClientAnswerCall() {

    }

    @Override
    public void OnMustReLogin() {

    }

    @Override
    public void OnLogoutByServer(int i) {

    }

    @Override
    public void OnRobotTransmitMsg(RobotMsg robotMsg) {

    }

    @Override
    public void OnRobotMsgAck(int i) {

    }

    @Override
    public void OnBindResult(int i) {

    }

    @Override
    public void OnReprotSmsPhone(String s) {

    }
}
