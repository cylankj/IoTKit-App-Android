package com.cylan.jiafeigou.engine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.CallMessageCallBack;
import com.cylan.publicApi.Config;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.DswLog;
import com.cylan.publicApi.Function;
import com.cylan.publicApi.JniPlay;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.publicApi.NetUtils;
import com.cylan.jiafeigou.activity.doorbell.DoorBellCalledActivity;
import com.cylan.jiafeigou.activity.efamily.facetime.FaceTimeActivity;
import com.cylan.jiafeigou.activity.efamily.facetime.FaceTimeCallingActivity;
import com.cylan.jiafeigou.activity.efamily.magnetic.MagneticActivity;
import com.cylan.jiafeigou.activity.message.MessageActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.entity.msg.CidPushOssConfig;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.MsgClientBellPress;
import com.cylan.jiafeigou.entity.msg.MsgEFamilyCallClient;
import com.cylan.jiafeigou.entity.msg.MsgLoginServers;
import com.cylan.jiafeigou.entity.msg.MsgPush;
import com.cylan.jiafeigou.entity.msg.MsgRelayServer;
import com.cylan.jiafeigou.entity.msg.MsgSceneData;
import com.cylan.jiafeigou.entity.msg.MsgServerConfig;
import com.cylan.jiafeigou.entity.msg.MsgServers;
import com.cylan.jiafeigou.entity.msg.PlayerMsgpackMsg;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.ClientReloginReq;
import com.cylan.jiafeigou.entity.msg.req.MsgBindCidReq;
import com.cylan.jiafeigou.entity.msg.rsp.LoginRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgCidlistRsp;
import com.cylan.jiafeigou.listener.RequestCallback;
import com.cylan.jiafeigou.listener.UDPMessageListener;
import com.cylan.jiafeigou.receiver.BootCompletedReceiver;
import com.cylan.jiafeigou.utils.AppManager;
import com.cylan.jiafeigou.utils.CacheUtil;
import com.cylan.jiafeigou.utils.DaemonUtil;
import com.cylan.jiafeigou.utils.NotificationUtil;
import com.cylan.jiafeigou.utils.OEMConf;
import com.cylan.jiafeigou.utils.PathGetter;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.ThreadPoolUtils;
import com.cylan.jiafeigou.utils.Utils;

import java.util.ArrayList;

public class MyService extends Service implements UDPMessageListener {

    private static final String TAG = "MyService";

    private static final int MSG_RECONNECT = 0x00;
    private static final int MSG_DATA = 0x01;
    private static final int MSG_SEND_DELAY = 0x02;

    private static boolean isConnectServer = false;
    private static boolean isLogin = false;
    private static int msgID = 3;


    private static WifiInfo CONNECT_NET = null;
    private static int mNum;
    private static int faceTimeNum = 1;

    private ClientUDP mScanUdpManager = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        DswLog.d(TAG + "onCreate");
        initConnect();
        DaemonUtil daemon = DaemonUtil.getInstance();
        daemon.init(this.getApplicationContext(), false, this.getClass().getName());
        mScanUdpManager = ClientUDP.getInstance();
        mScanUdpManager.setUDPMsgListener(this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void setWakeUpAlarm(Context context) {
        Intent send = new Intent(context, BootCompletedReceiver.class);
        send.setAction(ClientConstants.ACTION_PULL_SERVICE);
        PendingIntent pend = PendingIntent.getBroadcast(context, 0, send, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60 * 1000, pend);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG + "onDestroy");
        mScanUdpManager.removeUDPMsgListener(this);
        mScanUdpManager.close();
    }

    private void initConnect() {
        JniPlay.NativeInit(this, false, PathGetter.getSmartCallPath());
        MsgpackMsg.MsgHeader.setSession(PreferenceUtil.getSessionId(this));
        JniPlay.ConnectToServer(PreferenceUtil.getIP(this), PreferenceUtil.getPort(this));
    }

    public static void connectServer(Context ctx) {
        JniPlay.ConnectToServer(PreferenceUtil.getIP(ctx), PreferenceUtil.getPort(ctx));
    }

    private void pingNet() {
        try {
            ThreadPoolUtils.execute(new Runnable() {
                @Override
                public void run() {
                    DswLog.e(Constants.PREFIX0 + NetUtils.pingNetwork(Config.ADDR));
                    DswLog.e(Constants.PREFIX1 + NetUtils.sPingNetwork(Config.ADDR));
                    DswLog.e(Constants.PREFIX2 + NetUtils.isInternetAvailable(Constants.HTTP_QQ));
                    DswLog.e(Constants.PREFIX3 + NetUtils.sPingNetwork(Constants._QQ));
                }
            });
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }

    private void handleMsg(int msg, byte[] obj) {
        try {
            MsgpackMsg.MsgHeader mMsgHeader = null;
            switch (CallMessageCallBack.MSG_TO_UI.values()[msg]) {
                case CONNECT_SERVER_SUCCESS:
                    DswLog.e("MSG_CONNECT_SERVER_SUCCESS");
                    mHandler.removeMessages(MSG_RECONNECT);
                    isConnectServer = true;
                    msgID = msg;
                    CONNECT_NET = ((WifiManager) this.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
                    if (!PreferenceUtil.getIsLogout(this)) {
                        reLogin();
                    }
                    break;
                case SERVER_DISCONNECTED:
                case RESOLVE_SERVER_FAILED:
                case CONNECT_SERVER_FAILED:
                    DswLog.e("MSG_CONNECT_SERVER_FAILED");
                    msgID = msg;
                    isConnectServer = false;
                    isLogin = false;
                    mHandler.removeMessages(MSG_RECONNECT);
                    mHandler.sendEmptyMessageDelayed(MSG_RECONNECT, 3000);
                    pingNet();
                    break;
                case MSGPACK_MESSAGE:
                    mMsgHeader = PlayerMsgpackMsg.getInstance().fromBytes(obj);
                    if (mMsgHeader == null)
                        return;

                    int msgId = mMsgHeader.msgId;

                    if (msgId == MsgpackMsg.ID_RELAY_SERVER) {
                        MsgRelayServer mMsgRelayServer = (MsgRelayServer) mMsgHeader;
                        setRelayAddr(mMsgRelayServer);
                    }

                    if (msgId == MsgpackMsg.ID_RESUME) {
                        reLogin();
                    }

                    if (msgId == MsgpackMsg.SERVER_CONFIG) {
                        setServerConfig(mMsgHeader);
                    }
                    if (msgId == MsgpackMsg.ID_LOGIN_SERVER) {
                        setILoginServer(mMsgHeader);
                    }

                    if (msgId == MsgpackMsg.CLIENT_BINDCID_RSP) {
                        removeBindCidCache(mMsgHeader);
                    }

                    if (MsgpackMsg.CLIENT_PUSH == msgId || MsgpackMsg.BELL_PRESS == msgId
                            || MsgpackMsg.EFAML_CALL_CLIENT == msgId) {
                        push(mMsgHeader);
                    }

                    if (msgId == MsgpackMsg.CID_PUSH_OSS_CONFIG) {
                        setOssConfig((CidPushOssConfig) mMsgHeader);
                    }

                    if ((msgId == MsgpackMsg.CLIENT_LOGIN_RSP || msgId == MsgpackMsg.CLIENT_RELOGIN_RSP
                            || msgId == MsgpackMsg.CLIENT_SETPASS_RSP || msgId == MsgpackMsg.CLIENT_REGISTER_RSP
                            || msgId == MsgpackMsg.CLIENT_LOGINBYQQ_RSP || msgId == MsgpackMsg.CLIENT_LOGINBYSINA_RSP)
                            && ((RspMsgHeader) (mMsgHeader)).ret == Constants.RETOK) {

                        sendBindCidMsg();

                        setWakeUpAlarm(this);

                        isLogin = true;

                        UnSendQueue.getInstance().sendQuenueMsg();

                        MyApp.initConfig(MyService.this, ((LoginRsp) (mMsgHeader)));

                        if (msgId == MsgpackMsg.CLIENT_LOGINBYQQ_RSP || msgId == MsgpackMsg.CLIENT_LOGINBYSINA_RSP) {
                            saveLoginType(msgId);
                        }

                        if (msgId == MsgpackMsg.CLIENT_LOGIN_RSP) {
                            PreferenceUtil.setIsLoginType(this, true);
                        }
                    }
                    if (msgId == MsgpackMsg.ID_BELL_CONNECTED) {
                        mHandler.sendEmptyMessageDelayed(MSG_SEND_DELAY, 2000);
                    }

                    if (msgId == MsgpackMsg.CLIENT_SYNC_LOGOUT) {
                        PreferenceUtil.setIsLogout(this, true);
                    }

                    break;
                case NTP_UPDATE:
                    setNtpTime(new String(obj));
                    break;

            }
            Message message = mHandler.obtainMessage(MSG_DATA);
            message.arg1 = msg;
            message.obj = (CallMessageCallBack.MSG_TO_UI.values()[msg] == CallMessageCallBack.MSG_TO_UI.MSGPACK_MESSAGE) ? mMsgHeader : obj;

            mHandler.sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
            DswLog.ex(e.toString());
        }

    }

    private void setNtpTime(String time) {
        long ntpTime = Integer.parseInt(time);
        int timeDiff = (int) (ntpTime - System.currentTimeMillis() / 1000);
        PreferenceUtil.setKeyNtpTimeDiff(this, timeDiff);
    }

    private void setOssConfig(CidPushOssConfig config) {
        PreferenceUtil.setOssUrl(this, "http://" + config.hostname + "/" + config.bucket + "/%1$s/%2$s&OSSAccessKeyId=" + config.AccessID);
        PreferenceUtil.setOssTypeKey(this, config.type);
    }

    private void removeBindCidCache(MsgpackMsg.MsgHeader mMsgHeader) {
        MsgBindCidReq mMsgBindCidReq = (MsgBindCidReq) CacheUtil.readObject(CacheUtil.getADD_DEVICE_CACHE());
        if (mMsgBindCidReq != null && mMsgBindCidReq.cid.equals(mMsgHeader.caller)) {
            CacheUtil.remove(CacheUtil.getADD_DEVICE_CACHE());
        }
    }

    private void sendBindCidMsg() {
        MsgBindCidReq mMsgBindCidReq = (MsgBindCidReq) CacheUtil.readObject(CacheUtil.getADD_DEVICE_CACHE());
        if (!isLogin && mMsgBindCidReq != null) {
            mMsgBindCidReq.msgId = MsgpackMsg.CLIENT_BINDCID_REQ;
            JniPlay.SendBytes(mMsgBindCidReq.toBytes());
            DswLog.i("send MsgBindCidReq from server msg-->" + mMsgBindCidReq.toString());
        }
    }

    private void saveLoginType(int msgId) {
        PreferenceUtil.setOtherLoginType(this, msgId == MsgpackMsg.CLIENT_LOGINBYQQ_RSP ? 0 : 1);
        PreferenceUtil.setIsOtherLoginType(this, true);
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RECONNECT:
                    if (!Utils.isNetworkConnected(MyService.this.getApplicationContext())) {
                        handleMsg(3, null);
                    } else {
                        connectServer(MyService.this.getApplicationContext());
                    }
                    break;
                case MSG_DATA:
                    CallbackManager.getInstance().notifyObservers(msg.arg1, msg.obj);
                    break;
                case MSG_SEND_DELAY:
                    Intent intent = new Intent("ID_BELL_CONNECTED");
                    sendBroadcast(intent);
                    break;
            }
            return true;
        }
    });


    private void reLogin() {
        if (isLogin || !isConnectServer)
            return;
        final String bindPhone = PreferenceUtil.getBindingPhone(this);
        String sessid = PreferenceUtil.getSessionId(this);
        if (!sessid.equals("")) {
            ClientReloginReq clientReloginReq = new ClientReloginReq();
            clientReloginReq.language_type = Utils.getLanguageType(this);
            clientReloginReq.account = bindPhone;
            clientReloginReq.pass = "";
            clientReloginReq.os = Constants.OS_ANDROID_PHONE;
            clientReloginReq.version = Function.getVersion(this);
            clientReloginReq.sys_version = android.os.Build.VERSION.RELEASE;
            clientReloginReq.model = android.os.Build.BRAND + "-" + android.os.Build.MODEL;
            clientReloginReq.net = Utils.getNetType(this);
            clientReloginReq.name = Utils.getNetName(this);
            clientReloginReq.time = System.currentTimeMillis() / 1000;
            clientReloginReq.bundleId = Utils.getBundleId(this);
            clientReloginReq.device_token = Utils.getIMEI(this);
            clientReloginReq.alias = "";
            clientReloginReq.register_type = Constants.REGISTER_TYPE_PHONE;
            clientReloginReq.code = "";
            clientReloginReq.newpass = "";
            clientReloginReq.sessid = sessid;
            clientReloginReq.oem = OEMConf.getOEM();
            JniPlay.SendBytes(clientReloginReq.toBytes());
        }
    }


    public static void addObserver(RequestCallback observer) {
        CallbackManager.getInstance().addObserver(observer);
    }

    public static void delObserver(RequestCallback observer) {
        CallbackManager.getInstance().delObserver(observer);
    }

    public static void addQuene(byte[] str) {
        UnSendQueue.getInstance().addQuene(str);
    }

    private void push(MsgpackMsg.MsgHeader header) {
        if (header.msgId == MsgpackMsg.CLIENT_PUSH) {
            MsgPush mMsgPush = (MsgPush) header;
            if ((mMsgPush.push_type == ClientConstants.PUSH_TYPE_WARN || mMsgPush.push_type == ClientConstants.PUSH_TYPE_SYSTEM)
                    && Utils.isRunOnBackground(this)) {
                NotificationUtil.cancelNotifycationById(this, ClientConstants.WARM_NOTIFY_FLAG);
                mNum += 1;
                NotificationUtil.notifycation(this,
                        ClientConstants.WARM_NOTIFY_FLAG,
                        R.drawable.icon_notify,
                        Utils.getApplicationName(this),
                        String.format(getString(R.string.receive_new_news), mNum),
                        PreferenceUtil.getKeySetIsOpenVoice(this),
                        PreferenceUtil.getKeySetIsOpenVibrate(this),
                        new Intent(this, MessageActivity.class).addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS));
            } else if (mMsgPush.push_type == ClientConstants.PUSH_TYPE_MAGNET_ON
                    && Utils.isRunOnBackground(this)) {
                notifyMagOnOff(true, mMsgPush.cid);
            } else if (mMsgPush.push_type == ClientConstants.PUSH_TYPE_MAGNET_OFF
                    && Utils.isRunOnBackground(this)) {
                notifyMagOnOff(false, mMsgPush.cid);
            }
        } else if (MsgpackMsg.BELL_PRESS == header.msgId) {
            MsgClientBellPress mMsgClientBellPress = (MsgClientBellPress) header;
            doorbellPress(mMsgClientBellPress);
            //2016-03-31
            DswLog.e("some one is visiting...");
        } else if (MsgpackMsg.EFAML_CALL_CLIENT == header.msgId) {
            if (AppManager.getAppManager().isActivityTop(FaceTimeActivity.class.getName())
                    || AppManager.getAppManager().isActivityTop(FaceTimeCallingActivity.class.getName())
                    || AppManager.getAppManager().isActivityTop(DoorBellCalledActivity.class.getName())) {
                return;
            }
            MsgEFamilyCallClient eFamilyCallClient = (MsgEFamilyCallClient) header;
            MsgCidData info = MsgCidlistRsp.getInstance().getVideoInfoFromCacheByCid(eFamilyCallClient.caller);
            if (info == null) {
                return;
            }
            faceTimeNum += 1;
            startActivity(new Intent(getApplicationContext(), FaceTimeActivity.class)
                    .putExtra(ClientConstants.CIDINFO, info)
                    .putExtra(ClientConstants.FACE_TIME_TIME, eFamilyCallClient.time)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS));
        }
    }

    public static void setNum(int mNum) {
        MyService.mNum = mNum;
    }

    public static void setFaceTimeNum(int mNum) {
        MyService.faceTimeNum = mNum;
    }

    public static int getFaceTimeNum() {
        return MyService.faceTimeNum;
    }

    public static boolean getIsLogin() {
        return isLogin;
    }

    public static boolean getIsConnectServer() {
        return isConnectServer;
    }

    public static WifiInfo getConnectNet() {
        return CONNECT_NET;
    }

    public static int getMsgID() {
        return msgID;
    }

    private void setServerConfig(MsgpackMsg.MsgHeader mMsgHeader) {
        MsgServers mMsgServer = (MsgServers) mMsgHeader;
        if (mMsgServer.mServers.size() > 0) {
            String ip = mMsgServer.mServers.get(0).ip;
            int port = mMsgServer.mServers.get(0).port;
            DswLog.v("httpserver ip----->" + ip + "----old ip--->" + Constants.WEB_ADDR + "----port----->" + port + "----old port---->" + Constants.WEB_PORT);
            if (!ip.equals(Constants.WEB_ADDR) || port != (Constants.WEB_PORT)) {
                Constants.WEB_ADDR = ip;
                Constants.WEB_PORT = port;
            }
            if (mMsgServer instanceof MsgServerConfig) {
                JniPlay.SetHeartbeatInterval(((MsgServerConfig) mMsgServer).heartbeat);
            }

        }
    }

    private void setILoginServer(MsgpackMsg.MsgHeader mMsgHeader) {
        MsgLoginServers mMsgLoginServers = (MsgLoginServers) mMsgHeader;
        if (mMsgLoginServers.mServers.size() > 0) {
            String ip = mMsgLoginServers.mServers.get(0).ip;
            int port = mMsgLoginServers.mServers.get(0).port;
            DswLog.v("loginserver ip----->" + ip + "----old ip--->" + PreferenceUtil.getIP(this) + "----port----->" + port + "----old port---->" + PreferenceUtil.getPort(this));
            if (!ip.equals(PreferenceUtil.getIP(getApplicationContext())) || port != (PreferenceUtil.getPort(this))) {
                PreferenceUtil.setIP(this, ip);
                PreferenceUtil.setPort(this, port);
                Constants.ADDR = ip;
                Constants.CONFERENCE_PORT = port;
                JniPlay.DisconnectFromServer();
            }

        }
    }

    private void setRelayAddr(MsgRelayServer mMsgRelayServer) {
        MsgRelayServer server = new MsgRelayServer();
        server.iplist = new ArrayList<>();
        server.portlist = new ArrayList<>();
        if (mMsgRelayServer.iplist.size() > 0) {
            for (String ip : mMsgRelayServer.iplist) {
                server.iplist.add(ip);
            }
        }
        if (mMsgRelayServer.portlist.size() > 0) {
            for (int port : mMsgRelayServer.portlist) {
                server.portlist.add(port);
            }
        }
        JniPlay.UpdateRelayServer(server.toBytes());
    }

    private void notifyMagOnOff(boolean on_off, String cid) {
        if (MsgCidlistRsp.getInstance().isSomeoneMode(cid, MsgSceneData.MODE_HOME_IN))
            return;
        if (PreferenceUtil.getKeyMagWarnRsp(this)
                || MsgCidlistRsp.getInstance().isSomeoneMode(cid, MsgSceneData.MODE_HOME_OUT)) {
            mNum++;
            MsgCidData info = MsgCidlistRsp.getInstance().getVideoInfoFromCacheByCid(cid);
            NotificationUtil.cancelNotifycationById(this, ClientConstants.MAG_WARN_NOTIFY_FLAG);
            NotificationUtil.notifycation(this,
                    ClientConstants.MAG_WARN_NOTIFY_FLAG,
                    R.drawable.icon_notify,
                    Utils.getApplicationName(this),
                    String.format(on_off ? getString(R.string.MAGNETISM_ON_PUSH) : getString(R.string.MAGNETISM_OFF_PUSH), info.mName()) + "(" + mNum + ")",
                    PreferenceUtil.getKeySetIsOpenVoice(this),
                    PreferenceUtil.getKeySetIsOpenVibrate(this),
                    new Intent(this, MagneticActivity.class).putExtra(ClientConstants.CIDINFO, info)
                            .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS));
        }
    }

    private void doorbellPress(MsgClientBellPress mMsgClientBellPress) {
        if (AppManager.getAppManager().isActivityTop(FaceTimeActivity.class.getName())
                || AppManager.getAppManager().isActivityTop(FaceTimeCallingActivity.class.getName())
                || AppManager.getAppManager().isActivityTop(DoorBellCalledActivity.class.getName())) {
            DswLog.ex("AppManager.getAppManager().isActivityTop");
            return;
        }
        MsgCidData info = MsgCidlistRsp.getInstance().getVideoInfoFromCacheByCid(mMsgClientBellPress.caller);
        if (info == null) {
            DswLog.i("mMsgCidlistRsp is NULL");
            return;
        }
        info.url = mMsgClientBellPress.url;
        startActivity(DoorBellCalledActivity.getIntent(getApplicationContext(), info, mMsgClientBellPress.time, false));
    }


    @Override
    public void JfgMsgPong(ClientUDP.JFG_PONG jfg) {

    }

    @Override
    public void JfgMsgSetWifiRsp(ClientUDP.JFG_RESPONSE rsp) {

    }

    @Override
    public void JfgMsgFPong(ClientUDP.JFG_F_PONG req) {

    }

    @Override
    public void JfgMsgFAck(ClientUDP.JFG_F_ACK ack) {

    }

    @Override
    public void JfgMsgBellPress(ClientUDP.JFGCFG_HEADER data) {
        MsgClientBellPress mMsgClientBellPress = new MsgClientBellPress();
        mMsgClientBellPress.caller = data.mCid;
        mMsgClientBellPress.time = 0;
        if (!PreferenceUtil.getIsLogout(this)) {
            doorbellPress(mMsgClientBellPress);
        }
    }
}
