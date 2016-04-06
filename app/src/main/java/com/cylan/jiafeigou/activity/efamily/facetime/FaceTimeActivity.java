package com.cylan.jiafeigou.activity.efamily.facetime;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.CallMessageCallBack;
import cylan.log.DswLog;
import com.cylan.publicApi.JniPlay;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.activity.doorbell.DoorBellCalledActivity;
import com.cylan.jiafeigou.activity.efamily.main.ClientCallEFamlStatusListener;
import com.cylan.jiafeigou.activity.efamily.main.FaceTimeCallbackListener;
import com.cylan.jiafeigou.activity.main.MyVideos;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.engine.ClientUDP;
import com.cylan.jiafeigou.engine.MyService;
import com.cylan.jiafeigou.entity.msg.MsgAudioControl;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.MsgClientBellPress;
import com.cylan.jiafeigou.entity.msg.MsgIdBellConnected;
import com.cylan.jiafeigou.entity.msg.MsgSyncLogout;
import com.cylan.jiafeigou.entity.msg.req.MsgRelayMaskInfoReq;
import com.cylan.jiafeigou.entity.msg.rsp.MsgCidlistRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgRelayMaskInfoRsp;
import com.cylan.jiafeigou.listener.UDPMessageListener;
import com.cylan.jiafeigou.receiver.DeviceConnectedReceiver;
import com.cylan.jiafeigou.receiver.HeadsetPlugObserver;
import com.cylan.jiafeigou.receiver.PhoneBroadcastReceiver;
import com.cylan.jiafeigou.utils.AppManager;
import com.cylan.jiafeigou.utils.CacheUtil;
import com.cylan.jiafeigou.utils.DensityUtil;
import com.cylan.jiafeigou.utils.NotificationUtil;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ThreadPoolUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.Utils;
import com.cylan.jiafeigou.worker.SpeakerPhoneWorker;
import com.tencent.stat.StatService;

import org.webrtc.videoengine.ViERenderer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by yangc on 2015/12/8.
 *
 */
public class FaceTimeActivity extends BaseActivity implements View.OnClickListener,
        HeadsetPlugObserver.OnHeadsetPlugListener, UDPMessageListener {

    private static final String MTATAG = "FaceTime";

    private RelativeLayout mRmoteLayout;
    protected RelativeLayout mLocalLayout;
    protected RelativeLayout mLoadingAnimLayout;
    protected Button mIngorn;
    protected Button mAnwser;
    private TextView mTitle;
    private ImageView loadingAnim;
    protected ImageView bg_load;

    protected MsgCidData cidData;
    private NotifyDialog mTryDialog;

    protected SurfaceView mLocalView;
    private SurfaceView mRemoteView;

    private MsgRelayMaskInfoRsp mMemoryMsgRelayMaskInfoRsp = null;
    private MsgRelayMaskInfoRsp mDiskMsgRelayMaskInfoRsp = null;

    private Timer timer;
    private AudioManager audioManager;
    private IncomingPhoneReceiver inComingReceiver;
    private DeviceConnectedReceiver connectedReceiver = null;
    private HeadsetPlugObserver headsetPlugReceiver;
    protected ClientCallEFamlStatusListener statusListener = null;
    private static FaceTimeCallbackListener callback;

    private MediaPlayer mPlayer;
    private Vibrator vibrator;

    private int timelength;
    private SimpleDateFormat mSimpleTimeFormat = new SimpleDateFormat(
            "mm:ss", Locale.getDefault());
    private List<Integer> speedList;
    protected boolean isConnected = false;
    private boolean isConnectHeadsetPlug;
    private int recentCall = 0;

    protected static final int VIDEO_CALLING_TIMEOUT = 30 * 1000;

    protected static final int MSG_NO_RESPONSE = 0x01;
    protected static final int MSG_NO_ANSWER = 0x03;
    private static final int MSG_DELAY_FINISH = 0x04;
    private static final int MSG_CONNECT_TIMEOUT = 0x06;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        Intent intent = getIntent();
        cidData = (MsgCidData) intent.getSerializableExtra(ClientConstants.CIDINFO);
        recentCall = intent.getIntExtra(ClientConstants.FACE_TIME_TIME, 0);
        setContentView(R.layout.activity_facetime);
        initView();
        initSurfaceView();
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(afChangeListener,
                AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN);
        loadingAnim();
        mHandler.sendEmptyMessageDelayed(MSG_NO_RESPONSE, VIDEO_CALLING_TIMEOUT);

        inComingReceiver = new IncomingPhoneReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PhoneBroadcastReceiver.SEND_ACTION);
        registerReceiver(inComingReceiver, filter);

        registerConnectedReceiver();

        headsetPlugReceiver = new HeadsetPlugObserver(this);
        headsetPlugReceiver.setHeadsetPlugListener(this);
        headsetPlugReceiver.startListen();

        if (recentCall != 0){
            playCalledSound();
            TextView callingName = (TextView) findViewById(R.id.face_time_calling_name);
            callingName.setText(cidData.mName());
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(params);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            mLocalLayout.setVisibility(View.GONE);
        }
    }

    private void initView() {
        setBaseTitlebarVisbitly(false);
        mLocalLayout = (RelativeLayout) findViewById(R.id.face_local_view);
        mRmoteLayout = (RelativeLayout) findViewById(R.id.face_remote_view);
        mIngorn = (Button) findViewById(R.id.face_time_ignore);
        mAnwser = (Button) findViewById(R.id.face_time_answer);
        mTitle = (TextView) findViewById(R.id.face_title);
        mTitle.setText(R.string.EFAMILY_VIDEOCALLING1);
        loadingAnim = (ImageView) findViewById(R.id.face_loading_anim);
        mLoadingAnimLayout = (RelativeLayout) findViewById(R.id.face_load_layout);
        bg_load = (ImageView) findViewById(R.id.face_load_bg);
        bg_load.setVisibility(View.VISIBLE);

        mIngorn.setOnClickListener(this);
        mAnwser.setOnClickListener(this);
    }

    private void initSurfaceView() {
        mRemoteView = ViERenderer.CreateRenderer(this, true);
        mRemoteView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        mRmoteLayout.addView(mRemoteView);

        mLocalView = ViERenderer.CreateRenderer(this, true);
        mLocalLayout.addView(mLocalView);
        mLocalView.setZOrderOnTop(true);
        mLocalView.setZOrderMediaOverlay(true);
    }

    protected void scaleWidth() {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.width = DensityUtil.getScreenHeight(this) * 3 / 4;
        getWindow().setAttributes(params);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

    }

    @Override
    protected void onResume() {
        super.onResume();
        hideStatusBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ClientUDP.getInstance().setUDPMsgListener(this);
        ClientUDP.getInstance().setCid(cidData.cid);
    }

    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NO_RESPONSE:
                    notifycation(new Intent(new Intent(FaceTimeActivity.this, MyVideos.class)
                            .putExtra(ClientConstants.CIDINFO, cidData)
                            .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)));
                    if (callback != null)
                        callback.missCallByOverTime(true, cidData.cid);
                    mFinish();
                    break;
                case MSG_NO_ANSWER:
                    if (callback != null)
                        callback.missCallByOverTime(false, cidData.cid);
                    if (statusListener != null)
                        statusListener.missCallByOverTime();
                    ToastUtil.showToast(getApplicationContext(), getString(R.string.EFAMILY_NO_ANSWER), Gravity.CENTER, 1500);
                    mFinish();
                    break;
                case MSG_DELAY_FINISH:
                    mHandler.removeMessages(MSG_DELAY_FINISH);
                    stopMediaplayerOrVibrator();
                    mFinish();
                    break;
                case MSG_CONNECT_TIMEOUT:
                    ToastUtil.showToast(getApplicationContext(), getString(R.string.NO_NETWORK_DOOR), Gravity.CENTER, 1500);
                    if (callback != null)
                        callback.missCallByOverTime(true, cidData.cid);
                    mFinish();
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.face_time_ignore:
                if (statusListener != null)
                    statusListener.haveAnswered(timelength);
                sendCallStatus();
                mFinish();
                break;
            case R.id.face_time_answer:
                mHandler.removeMessages(MSG_NO_RESPONSE);
                mHandler.sendEmptyMessageDelayed(MSG_CONNECT_TIMEOUT, VIDEO_CALLING_TIMEOUT);
                try {
                    JniPlay.StartCamera(true);
                    JniPlay.StartRendeLocalView(mLocalView);
                } catch (Exception e) {
                    showAuthDialog();
                }
                makeCall();
                mAnwser.setVisibility(View.GONE);
                mIngorn.setText(getString(R.string.DOOR_STOPPED));
                MyService.setFaceTimeNum(0);
                NotificationUtil.cancelNotifycationById(this, ClientConstants.FACE_TIME_NOTIFY_FLAG);
                stopMediaplayerOrVibrator();
                break;
        }
    }

    @Override
    public void handleMsg(int msg, Object param) {
        super.handleMsg(msg, param);
        switch (CallMessageCallBack.MSG_TO_UI.values()[msg]) {
            case RESOLVE_SERVER_FAILED:
            case CONNECT_SERVER_FAILED:
            case SERVER_DISCONNECTED:
                stopAnim();
                discountPrompt(-1, getString(R.string.NO_NETWORK_DOOR) );
                break;
            case CONNECT_SERVER_SUCCESS:
                break;
            case RECV_DISCONN:
                stopAnim();
                mHandler.removeMessages(MSG_NO_ANSWER);
                int id = StringUtils.toInt(new String((byte[]) param));
                switch (StringUtils.toInt(id)) {
                    case ClientConstants.CAUSE_CALLER_NOTLOGIN:
                    case ClientConstants.CAUSE_PEERDISCONNECT:
                        discountPrompt(id, getString(R.string.NO_NETWORK_DOOR));
                        break;
                    case ClientConstants.CAUSE_PEERNOTEXIST:
                        discountPrompt(id, getString(R.string.NO_NETWORK_DOOR));
                        break;
                    case ClientConstants.CAUSE_PEERINCONNECT:
                        ToastUtil.showFailToast(FaceTimeActivity.this, getString(R.string.EFAMILY_CALL_BUSY));
                        mHandler.sendEmptyMessageDelayed(MSG_DELAY_FINISH, 1000);
                        break;
                    case ClientConstants.CAUSE_BELL_CONNECTED:
                        discountPrompt(id, getString(R.string.OTHERS_LOOKING));
                        break;
                    default:
                        sendCallStatus();
                        mFinish();
                        break;
                }
                break;
            case NOTIFY_RESOLUTION:
                isConnected = true;
                JniPlay.StartRendeRemoteView(0, mRemoteView);
                setLocalViewPoistion();
                if (recentCall == 0)
                    StatService.trackCustomBeginEvent(this, MTATAG, "FaceTimeCalling");
                else
                    StatService.trackCustomEndEvent(this, MTATAG, "FaceTimeCalled");
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                timer = new Timer(true);
                timer.schedule(new Ta(), 1000, 1000);
                mTitle.setText(mSimpleTimeFormat.format(new Date(timelength * 1000)));
                stopAnim();
                mHandler.removeMessages(MSG_NO_RESPONSE);
                mHandler.removeMessages(MSG_NO_ANSWER);
                mHandler.removeMessages(MSG_CONNECT_TIMEOUT);
                if (isConnected) {
                    openVoiceAndMike();
                }
                String params = new String((byte[]) param);
                Log.d("FaceTime", "localView:width-->" + mLocalLayout.getWidth()
                        + " height-->" + mLocalLayout.getHeight() + "remoteView:width-->"
                        + mRemoteView.getWidth() + "height-->" + mRemoteView.getHeight()
                        + " params-->" + params + " fixedSize-->" );
                if (!StringUtils.isEmptyOrNull(params)) {
                    setResolution(params);
                }
                break;
            case NOTIFY_RTCP:
                if (param.toString() != null) {
                    findViewById(R.id.face_time_speed).setVisibility(View.VISIBLE);
                    DecimalFormat df = new DecimalFormat("###.0");
                    String[] str = new String((byte[]) param).split("x");
                    int ll = StringUtils.toInt(str[2]);
                    StringBuilder liuliang = new StringBuilder();
                    liuliang.append((StringUtils.toInt(str[1]) / 8));
                    liuliang.append("K/s ");
                    if (ll > 1024) {
                        liuliang.append(df.format((double) ll / 1024.0)).append("M");
                    } else if (ll > (1024 * 1024)) {
                        liuliang.append(df.format((double) ll / 1024 / 1024.0)).append("G");
                    } else {
                        liuliang.append(ll).append("KB");
                    }
                    ((TextView) findViewById(R.id.face_time_speed)).setText(liuliang);
                    if (isConnected) {
                        if (speedList == null)
                            speedList = new ArrayList<>();
                        if (speedList.size() < 10) {
                            speedList.add(ll);
                        } else {
                            Set<Integer> set = new HashSet();
                            for (int i = 0; i < 10; i++) {
                                set.add(speedList.get(i));
                            }
                            if (set.size() == 1) {
                                DswLog.i("一直没速度，重连");
                                discountPrompt(-3, getString(R.string.NO_NETWORK_DOOR) );
                            }
                            speedList.clear();
                        }
                    }
                }
                break;
            case MSGPACK_MESSAGE:
                MsgpackMsg.MsgHeader mMsgHeader = (MsgpackMsg.MsgHeader) param;
                if (MsgpackMsg.ID_RELAY_MASK_INFO_RSP == mMsgHeader.msgId) {
                    if (AppManager.getAppManager().isActivityTop(this.getClass().getName())) {
                        if (mMsgHeader.caller.equals(cidData.cid)) {
                            mMemoryMsgRelayMaskInfoRsp = (MsgRelayMaskInfoRsp) mMsgHeader;
                            CacheUtil.saveObject(mMemoryMsgRelayMaskInfoRsp, CacheUtil.getCID_RELAYMASKINFO_KEY(mMemoryMsgRelayMaskInfoRsp.caller));
                            makeCall();
                        }
                    }
                }
                if (MsgpackMsg.BELL_PRESS == mMsgHeader.msgId) {
                    MsgClientBellPress bellPress = (MsgClientBellPress) mMsgHeader;
                    MsgCidData info = MsgCidlistRsp.getInstance().getVideoInfoFromCacheByCid(bellPress.caller);
                    showDialog(info, bellPress);
                } else if (MsgpackMsg.ID_BELL_CONNECTED == mMsgHeader.msgId) {
                    MsgIdBellConnected mMsgIdBellConnected = (MsgIdBellConnected) mMsgHeader;
                    if (mMsgIdBellConnected.caller.equals(cidData.cid)) {
                        mHandler.sendEmptyMessageDelayed(MSG_DELAY_FINISH, 1000);
                        ToastUtil.showFailToast(this, getString(R.string.DOOR_OTHER_LISTENED));
                    }
                }else if (MsgpackMsg.EFAML_CALL_CANCEL == mMsgHeader.msgId) {
                    if (callback != null)
                        callback.missCallByCancel(true, cidData.cid);
                    mFinish();
                }else if (MsgpackMsg.CLIENT_SYNC_LOGOUT == mMsgHeader.msgId){
                    MsgSyncLogout mMsgSyncLogout = (MsgSyncLogout) mMsgHeader;
                    MyApp.logout(this);
                    if ((mMsgSyncLogout.trigger_id == MsgpackMsg.CLIENT_CHANGEPASS_REQ) || (mMsgSyncLogout.trigger_id == MsgpackMsg.CLIENT_SETPASS_REQ)) {
                        MyApp.showForceNotifyDialog(this, getString(R.string.PWD_CHANGED));
                    } else {
                        MyApp.showForceNotifyDialog(this, getString(R.string.RET_ESESSION_NOT_EXIST));
                    }
                }
                break;
            default:
                break;
        }
    }

    protected void makeCall() {
        MsgRelayMaskInfoRsp mMaskInfo = getRelayMaskInfoRsp();
        if (mMaskInfo == null) {
            JniPlay.SendBytes(new MsgRelayMaskInfoReq(cidData.cid).toBytes());
            return;
        }
        int netType = (Utils.getNetType(this) == ConnectivityManager.TYPE_WIFI ? 1 : 0);
        mMaskInfo.callee = cidData.cid;
        if (mMaskInfo != null) {
            int relaymask[] = new int[mMaskInfo.mask_list.size()];
            for (int i = 0; i < mMaskInfo.mask_list.size(); i++) {
                relaymask[i] = mMaskInfo.mask_list.get(i);
            }
            JniPlay.ConnectToPeer(cidData.cid, true, netType, false, cidData.os, relaymask, false, true);
        } else {
            JniPlay.ConnectToPeer(cidData.cid, true, netType, false, cidData.os, new int[0], false, true);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (recentCall == 0)
            StatService.trackCustomEndEvent(this, MTATAG, "FaceTimeCalling");
        else
            StatService.trackCustomEndEvent(this, MTATAG, "FaceTimeCalled");
        JniPlay.DisconnectFromPeer();
        stopAnim();
    }

    private MsgRelayMaskInfoRsp getRelayMaskInfoRsp() {
        if (mMemoryMsgRelayMaskInfoRsp != null)
            return mMemoryMsgRelayMaskInfoRsp;
        if (mDiskMsgRelayMaskInfoRsp != null) {
            return mDiskMsgRelayMaskInfoRsp;
        } else {
            mDiskMsgRelayMaskInfoRsp = (MsgRelayMaskInfoRsp) CacheUtil
                    .readObject(CacheUtil.getCID_RELAYMASKINFO_KEY(cidData.cid));
            if (mDiskMsgRelayMaskInfoRsp != null)
                return mDiskMsgRelayMaskInfoRsp;
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (JniPlay.IsInCall()) {
            JniPlay.DisconnectFromPeer();
            closeVoiceAndMike();
        }

        if (inComingReceiver != null)
            unregisterReceiver(inComingReceiver);

        unRegisterConnectedReceiver();

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        mHandler.removeCallbacksAndMessages(null);

        if (audioManager != null)
            audioManager.abandonAudioFocus(afChangeListener);

        if (headsetPlugReceiver != null) {
            headsetPlugReceiver.stopListen();
        }
        if (mPlayer != null)
            mPlayer.release();

        ClientUDP.getInstance().removeUDPMsgListener(this);
    }

    @Override
    public void onHeadsetPlugOn() {
        isConnectHeadsetPlug = true;
        if (isConnected) {
            audioManager.setSpeakerphoneOn(false);
        }
    }

    @Override
    public void onHeadsetPlugOff() {
        isConnectHeadsetPlug = false;
        if (isConnected) {
            audioManager.setSpeakerphoneOn(true);
        }
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
        MsgClientBellPress bellPress = new MsgClientBellPress();
        bellPress.caller = data.mCid;
        bellPress.time = 0;
        MsgCidData info = MsgCidlistRsp.getInstance().getVideoInfoFromCacheByCid(bellPress.caller);
        if (!PreferenceUtil.getIsLogout(this))
            showDialog(info, bellPress);
    }

    private class Ta extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() { // UI thread
                @Override
                public void run() {
                    timelength++;
                    mTitle.setText(mSimpleTimeFormat.format(new Date(timelength * 1000)));
                }
            });

        }

    }

    protected void loadingAnim() {
        mLoadingAnimLayout.setVisibility(View.VISIBLE);
        RotateAnimation animation = new RotateAnimation(0f, 360f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        animation.setRepeatCount(RotateAnimation.INFINITE);
        animation.setDuration(1000);
        animation.setRepeatMode(Animation.RESTART);
        animation.setInterpolator(new LinearInterpolator());
        loadingAnim.startAnimation(animation);
    }

    private void stopAnim() {
        if (loadingAnim != null) {
            mLoadingAnimLayout.setVisibility(View.GONE);
            loadingAnim.clearAnimation();
            findViewById(R.id.face_time_calling_name).setVisibility(View.GONE);
        }
    }

    private void setLocalViewPoistion() {
        if (mLocalLayout.getVisibility() == View.GONE)
            mLocalLayout.setVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mLocalLayout.getLayoutParams();
        params.height = DensityUtil.dip2px(this, 133);
        params.width = DensityUtil.dip2px(this, 100);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.setMargins(DensityUtil.getScreenWidth(this) - params.width,
                DensityUtil.getScreenHeight(this) - params.height, 24, 20);
        mLocalLayout.setLayoutParams(params);
        mLocalLayout.setPadding(2, 2, 2, 2);
        mLocalLayout.setBackgroundResource(R.drawable.facetime_corner);
        bg_load.setVisibility(View.INVISIBLE);

        if (recentCall == 0){
            mLocalLayout.scrollTo(0, 0);
            WindowManager.LayoutParams windowParams = getWindow().getAttributes();
            windowParams.width = DensityUtil.getScreenWidth(this);
            getWindow().setAttributes(windowParams);
        }
    }

    protected void discountPrompt(int msgid, String str) {
        isConnected = false;
        JniPlay.StopCamera();
        JniPlay.DisconnectFromPeer();
        showTryDialog(msgid, str);
    }

    private void showTryDialog(int msgId, String title) {
        if (mTryDialog == null)
            mTryDialog = new NotifyDialog(this);
        mTryDialog.setButtonText(R.string.TRY_AGAIN, R.string.CANCEL);
        mTryDialog.setCancelable(false);
        mTryDialog.setCanceledOnTouchOutside(false);
        mTryDialog.show(msgId, title, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mTryDialog.dismiss();
                        JniPlay.StartCamera(true);
                        JniPlay.StartRendeLocalView(mLocalView);
                        makeCall();
                        hideStatusBar();
                    }
                }
                , new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mTryDialog.dismiss();
                        mFinish();

                    }

                }

        );
    }

    protected void mFinish() {
        stopAnim();
        stopMediaplayerOrVibrator();
        AppManager.getAppManager().finishActivity(this);
    }

    private void notifycation(Intent intent) {
        NotificationUtil.notifycation(this,
                ClientConstants.FACE_TIME_NOTIFY_FLAG,
                R.drawable.icon_notify,
                Utils.getApplicationName(this),
                String.format("%s:%s", cidData.mName(), this.getString(R.string.EFAMILY_MISSED)
                        + "(" + MyService.getFaceTimeNum() + ")"),
                true,
                true,
                intent
        );
        sendBroadcast(new Intent(ClientConstants.ACTION));
    }

    protected void showAuthDialog() {
        final NotifyDialog mDialog = new NotifyDialog(this);
        mDialog.hideNegButton();
        mDialog.setButtonText(R.string.WELL_OK, R.string.CANCEL);
        mDialog.show(String.format(getString(R.string.permission_auth),
                Utils.getApplicationName(FaceTimeActivity.this),
                getString(R.string.sound_auth)), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                mFinish();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
    }

    private void openVoiceAndMike() {
        JniPlay.EnableSpeaker(true);
        JniPlay.EnableMike(true);
        MsgAudioControl msg = new MsgAudioControl(cidData.cid);
        msg.mSpeaker = true;
        msg.mMike = true;
        JniPlay.SendBytes(msg.toBytes());
        ThreadPoolUtils.execute(new SpeakerPhoneWorker(isConnectHeadsetPlug, audioManager));
    }

    private void closeVoiceAndMike() {
        JniPlay.EnableMike(false);
        JniPlay.EnableSpeaker(false);
        MsgAudioControl msg = new MsgAudioControl(cidData.cid);
        msg.mSpeaker = false;
        msg.mMike = false;
        JniPlay.SendBytes(msg.toBytes());
        ThreadPoolUtils.execute(new SpeakerPhoneWorker(isConnectHeadsetPlug, audioManager));
        audioManager.abandonAudioFocus(afChangeListener);
    }

    private AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                audioManager.abandonAudioFocus(afChangeListener);
            }

        }
    };

    private class IncomingPhoneReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String flag = intent.getStringExtra(PhoneBroadcastReceiver.TAG);
            if (flag.equals(PhoneBroadcastReceiver.CALL_STATE_RINGING)) {
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                isConnectHeadsetPlug = true;
                if (audioManager.isSpeakerphoneOn()) {
                    audioManager.setSpeakerphoneOn(false);
                    audioManager.abandonAudioFocus(afChangeListener);
                }
            } else {
                mFinish();
            }

        }

    }

    protected void playCalledSound() {
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audio.getRingerMode();
        if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
            vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            long[] pattern = {100, 1000, 100, 1000};
            vibrator.vibrate(pattern, 2);
        } else if (ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            mPlayer = MediaPlayer.create(this, R.raw.facetime_called);
            mPlayer.setLooping(true);
            mPlayer.start();
        }
    }

    private void stopMediaplayerOrVibrator() {
        if (mPlayer != null && mPlayer.isPlaying()){
            mPlayer.stop();
        } else if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.cancel();
        }
    }

    private void showDialog(final MsgCidData info, final MsgClientBellPress bellPress) {
        final NotifyDialog dialog = new NotifyDialog(this);
        dialog.setButtonText(R.string.EFAMILY_IGNORE, R.string.DOOR_BELL_LOOK);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show(String.format("%s:%s", info.mName(), this.getString(R.string.someone_call)),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        hideStatusBar();
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getApplicationContext(), DoorBellCalledActivity.class)
                                .putExtra(ClientConstants.CIDINFO, info)
                                .putExtra(ClientConstants.DOOR_BELL_TIME, bellPress.time)
                                .putExtra(ClientConstants.IS_CALLED_FROM_FACETIME, true)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS));
                        dialog.dismiss();
                        mFinish();
                    }
                });
    }

    protected void setResolution(String params) {
        int remoteWidth;
        int remoteHeight;
        if (params.contains(",")){
            String[] org = params.split(",");
            String[] res = org[1].split("x");
            remoteWidth = StringUtils.toInt(res[0]);
            remoteHeight = StringUtils.toInt(res[1]);
        }else {
            String[] res = params.split("x");
            remoteWidth = StringUtils.toInt(res[0]);
            remoteHeight = StringUtils.toInt(res[1]);
        }

        ViewGroup.LayoutParams remoteParams1 = mRemoteView.getLayoutParams();
        remoteParams1.height = DensityUtil.getScreenHeight(this) + 3;
        remoteParams1.width = (remoteParams1.height * remoteWidth) / remoteHeight;
        mRemoteView.setLayoutParams(remoteParams1);
        hideStatusBar();
    }

    protected void hideStatusBar(){
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public static void setOnEFamlCallbackListener(FaceTimeCallbackListener listener){
        callback = listener;
    }

    public void setOnCallEFamlStatusListener(ClientCallEFamlStatusListener listener){
        statusListener = listener;
    }

    private void registerConnectedReceiver(){
        connectedReceiver = new DeviceConnectedReceiver(mHandler, MSG_DELAY_FINISH);
        IntentFilter filter = new IntentFilter("ID_BELL_CONNECTED");
        registerReceiver(connectedReceiver, filter);
    }

    private void unRegisterConnectedReceiver(){
        if (connectedReceiver != null)
            unregisterReceiver(connectedReceiver);
    }

    private void sendCallStatus(){
        if (callback != null){
            if (recentCall == 0 ){
                if (timelength == 0)
                    callback.missCallByCancel(false, cidData.cid);
                else
                    callback.haveAnswered(false, timelength, cidData.cid);
            }
            else{
                if (timelength == 0)
                    callback.missCallByCancel(true, cidData.cid);
                else
                    callback.haveAnswered(true, timelength, cidData.cid);
            }
        }
    }

}
