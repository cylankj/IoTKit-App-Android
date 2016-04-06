package com.cylan.jiafeigou.activity.doorbell;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.activity.video.ShotPhotoAnimation;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.base.RootActivity;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.engine.ClientUDP;
import com.cylan.jiafeigou.entity.msg.MsgAudioControl;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.MsgIdBellConnected;
import com.cylan.jiafeigou.entity.msg.MsgSyncLogout;
import com.cylan.jiafeigou.entity.msg.rsp.MsgRelayMaskInfoRsp;
import com.cylan.jiafeigou.listener.UDPMessageListener;
import com.cylan.jiafeigou.receiver.DeviceConnectedReceiver;
import com.cylan.jiafeigou.receiver.HeadsetPlugObserver;
import com.cylan.jiafeigou.receiver.HomeWatcherReveiver;
import com.cylan.jiafeigou.receiver.PhoneBroadcastReceiver;
import com.cylan.jiafeigou.utils.AppManager;
import com.cylan.jiafeigou.utils.BitmapUtil;
import com.cylan.jiafeigou.utils.CacheUtil;
import com.cylan.jiafeigou.utils.DensityUtil;
import com.cylan.jiafeigou.utils.NotificationUtil;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.PathGetter;
import com.cylan.jiafeigou.utils.PermissionChecker;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ThreadPoolUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.Utils;
import com.cylan.jiafeigou.widget.ViewDrag;
import com.cylan.jiafeigou.worker.SaveShotPhotoRunnable;
import com.cylan.jiafeigou.worker.SpeakerPhoneWorker;
import com.cylan.publicApi.CallMessageCallBack;
import com.cylan.publicApi.JniPlay;
import com.cylan.publicApi.MsgpackMsg;
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

import cylan.log.DswLog;
import cylan.uil.core.ImageLoader;
import cylan.uil.core.assist.FailReason;
import cylan.uil.core.listener.ImageLoadingListener;

public class DoorBellCalledActivity extends RootActivity implements OnClickListener,
        HeadsetPlugObserver.OnHeadsetPlugListener, ViewDrag.Drag2RightOrLeftListener, UDPMessageListener {

    private final String TAG = "DoorBellCalledActivity";
    private final String MTATAG = "DoorBell";

    /**
     * handler msg
     */
    protected static final int HANDLER_CREATECALL_OVERYTIME = 0x01;
    protected static final int HANDLER_NORESPONSE_15S = 0x02;
    protected static final int HANDLER_PLAYSOUND_15S = 0x03;
    private static final int HANDLER_DELAY_FINISH = 0x04;
    private static final int HANDLER_SHOT_PHOTO = 0x05;
    private static final int HANDLER_TRY_TO_LOAD_PHOTO = 0x06;

    private AudioManager audioManager;

    protected TextView mCalledTitle;
    private RelativeLayout mVideoLayout;
    protected ImageView mLoadingView;
    protected ImageView mHangUpView;
    protected ImageView mTalk;
    protected ImageView mShot;
    private SurfaceView mSurfaceViewOutDoorScen;
    private FrameLayout viewHolderFrameLayout;
    protected View mAnswerView;
    protected View mActiveLookView;
    private ImageView mTempView;
    /**
     * this is the handlebar margins
     */
    protected float mViewMargin = 0;
    protected MsgCidData cidData;

    private HeadsetPlugObserver headsetPlugReceiver;
    private boolean isConnected;
    private boolean isOpenSound = false;
    private boolean isOpenTalk = false;
    private boolean isConnectHeadsetPlug;
    private static boolean saveOpenTalkState = false;
    private static boolean saveOpenSoundState = false;


    private int remoteWidth;
    private int remoteHeight;

    /**
     * called timer*
     */
    private Timer timer = null;
    private int timelength;
    private SimpleDateFormat mSimpleTimeFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
    private MediaPlayer mPlayer;
    private Vibrator vibrator;

    private NotifyDialog mTryDialog;
    private IncomingPhoneReceiver inComingReceiver = null;
    private HomeWatcherReveiver homeWatcherReveiver = null;
    private DeviceConnectedReceiver connectedReceiver = null;

    private PowerManager.WakeLock wakeLock;

    List<Integer> speedList = null;

    private MsgRelayMaskInfoRsp mMemoryMsgRelayMaskInfoRsp = null;
    private MsgRelayMaskInfoRsp mDiskMsgRelayMaskInfoRsp = null;

    private IPlayOrStop mManager;

    private ClientUDP.JFG_F_PONG mPong;
    private boolean isLan;
    private boolean isPlayByLan;


    public static Intent getIntent(Context ctx, MsgCidData info, long time, boolean isActive) {
        return new Intent(ctx, DoorBellCalledActivity.class)
                .putExtra(ClientConstants.CIDINFO, info)
                .putExtra(ClientConstants.DOOR_BELL_TIME, time)
                .putExtra(ClientConstants.IS_ACTIVE_CHECK_BELL, isActive)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        cidData = (MsgCidData) getIntent().getSerializableExtra(ClientConstants.CIDINFO);
        long mBellPressTime = getIntent().getLongExtra(ClientConstants.DOOR_BELL_TIME, 0);
        DswLog.i(TAG + "--onCreate--" + " getIntent-->" + getIntent());
        if (mBellPressTime != 0 && !Utils.isRecentBellCall(this, mBellPressTime)) {
            notifycation(new Intent(DoorBellCalledActivity.this, DoorBellActivity.class).putExtra(ClientConstants.CIDINFO, cidData).addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS));
            DswLog.ex("what the hell");
            finish();
            return;
        }
        setContentView(R.layout.activity_doorbell_called);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mViewMargin = getResources().getDimension(R.dimen.doorbell_handlebar_margin);
        headsetPlugReceiver = new HeadsetPlugObserver(this);
        headsetPlugReceiver.setHeadsetPlugListener(this);
        headsetPlugReceiver.startListen();
        InitView();
        InitSurfaceView();

        mHandler.sendEmptyMessageDelayed(HANDLER_NORESPONSE_15S, 15000);

        inComingReceiver = new IncomingPhoneReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PhoneBroadcastReceiver.SEND_ACTION);
        registerReceiver(inComingReceiver, filter);
        registerConnectedReceiver();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
        wakeLock.acquire();

        setResolution(ClientConstants.DOORBELL_RESOLUTION);

        boolean isActive = getIntent().getBooleanExtra(ClientConstants.IS_ACTIVE_CHECK_BELL, false);
        if (isActive) {
            mHandler.removeMessages(HANDLER_NORESPONSE_15S);
            mAnswerView.setVisibility(View.GONE);
            mActiveLookView.setVisibility(View.VISIBLE);
            makeCall();
        } else {
            if (getIntent().getBooleanExtra(ClientConstants.IS_CALLED_FROM_FACETIME, false)) {
                dragRight();
                return;
            }
            playCalledSound();
            loadImage();
        }

    }


    @Override
    public void cancelWarmNotifycation() {
    }

    protected void playCalledSound() {
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audio.getRingerMode();
        if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
            vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            long[] pattern = {100, 1000, 100, 1000};
            vibrator.vibrate(pattern, 2);
            mHandler.sendEmptyMessageDelayed(HANDLER_PLAYSOUND_15S, 15000);
        } else if (ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            mHandler.sendEmptyMessageDelayed(HANDLER_PLAYSOUND_15S, 15000);
            mPlayer = MediaPlayer.create(this, R.raw.doorbell_called);
            mPlayer.setLooping(true);
            mPlayer.start();
        }
    }


    private void InitView() {
        mCalledTitle = (TextView) findViewById(R.id.title);
        mCalledTitle.setText(String.format(getString(R.string.doorbell_title), cidData.mName(), getString(R.string.DOOR_ANSWERING)));
        mVideoLayout = (RelativeLayout) findViewById(R.id.video_layout);
        mHangUpView = (ImageView) findViewById(R.id.doorbell_discount);
        mHangUpView.setOnClickListener(this);
        mTalk = (ImageView) findViewById(R.id.doorbell_talkback);
        mTalk.setOnClickListener(this);
        mTalk.setEnabled(false);
        mShot = (ImageView) findViewById(R.id.doorbell_screenshot);
        mShot.setOnClickListener(this);
        mShot.setEnabled(false);
        mLoadingView = (ImageView) findViewById(R.id.loading);
        mAnswerView = findViewById(R.id.layout_doorbell_answer);
        mActiveLookView = findViewById(R.id.layout_doorbell_activelook);
        mTempView = (ImageView) findViewById(R.id.temp_page);
        ViewDrag mViewDrag = (ViewDrag) findViewById(R.id.layout_doorbell_drag);
        mViewDrag.setDrag2RightOrLeftListening(this);
    }

    private void InitSurfaceView() {
        mSurfaceViewOutDoorScen = ViERenderer.CreateRenderer(this, true);
        viewHolderFrameLayout = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        viewHolderFrameLayout.addView(mSurfaceViewOutDoorScen);
        viewHolderFrameLayout.setBackgroundColor(getResources().getColor(R.color.white));
        mVideoLayout.addView(viewHolderFrameLayout, 1);
        mSurfaceViewOutDoorScen.setLayoutParams(params);
        viewHolderFrameLayout.setVisibility(View.INVISIBLE);
    }


    @Override
    protected void onStart() {
        super.onStart();
        DswLog.i(TAG + "--onStart--");
        ClientUDP.getInstance().setUDPMsgListener(this);
        ClientUDP.getInstance().setCid(cidData.cid);
    }


    private void startCall() {
        mActiveLookView.setVisibility(View.VISIBLE);
        mActiveLookView.setAlpha(0.0f);
        ValueAnimator mAnswerAlpha = ObjectAnimator.ofFloat(mActiveLookView, "alpha", 0.0f, 1.0f);
        ValueAnimator mDisconnectAlpha = ObjectAnimator.ofFloat(mAnswerView, "alpha", 1.0f, 0.0f);
        AnimatorSet animAnswer = new AnimatorSet();
        animAnswer.setDuration(200);
        animAnswer.playTogether(mAnswerAlpha, mDisconnectAlpha);
        animAnswer.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                mAnswerView.setVisibility(View.GONE);
                makeCall();
            }
        });
        animAnswer.start();

    }


    protected void loadImage() {
        if (cidData.mThumbPath() == null || cidData.mThumbPath().equals(""))
            return;
        ImageLoader.getInstance().loadImage(cidData.mThumbPath(), new ImageLoadingListener() {

            @Override
            public void onLoadingStarted(String s, View view) {
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                if (mHandler.hasMessages(HANDLER_NORESPONSE_15S) && !isConnected) {
                    mHandler.sendEmptyMessageDelayed(HANDLER_TRY_TO_LOAD_PHOTO, 1000);
                }
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                if (mTempView != null)
                    mTempView.setImageBitmap(bitmap);
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
            }
        });
    }


    private void playLoadingAnim() {
        mLoadingView.setVisibility(View.VISIBLE);
        RotateAnimation mRotateAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateAnimation.setRepeatCount(Animation.INFINITE);
        mRotateAnimation.setInterpolator(new LinearInterpolator());
        mRotateAnimation.setRepeatMode(Animation.RESTART);
        mRotateAnimation.setDuration(600);
        mLoadingView.startAnimation(mRotateAnimation);
    }

    private void stopLoadingAnim() {
        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.GONE);
            mLoadingView.clearAnimation();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.doorbell_discount:
                // finish this activity,go back to the doorbell detail page
                mHandler.removeMessages(HANDLER_NORESPONSE_15S);
                mFinish();
                break;
            case R.id.doorbell_screenshot:
                if (isConnected) {
                    mShot.setEnabled(false);
                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
                    byte[] pics = JniPlay.TakeSnapShot(false);
                    if (pics != null && remoteWidth > 0 && remoteHeight > 0) {
                        Bitmap mBitmap = BitmapUtil.byte2bitmap(remoteWidth, remoteHeight, pics);
                        String path = PathGetter.getJiaFeiGouPhotos() + df.format(new Date()) + ".png";
                        ThreadPoolUtils.execute(new SaveShotPhotoRunnable(mBitmap, path, mHandler, HANDLER_SHOT_PHOTO));
                    }
                    mShot.setEnabled(true);
                }

                break;
            case R.id.doorbell_talkback:
                if (isConnected) {
                    final boolean isAudioPermissionGrant
                            = PermissionChecker.isAudioRecordPermissionGrant(getApplicationContext());
                    if (!isAudioPermissionGrant && !saveOpenTalkState) {
                        showAuthDialog();
                        return;
                    }
                    mTalk.setEnabled(false);
                    isOpenTalk = !saveOpenTalkState;
                    if (saveOpenTalkState) {
                        toggleTalk(false);
                    } else {
                        toggleTalk(isOpenTalk);
                    }
                    mTalk.setEnabled(true);
                    DswLog.i("RecordChecker permission: " + isAudioPermissionGrant);
                }


                break;
            default:
                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        DswLog.i(TAG + "--onResume--");
        registerHomeKeyReciver(this);
        ClientUDP.getInstance().setLoop(true);
        ClientUDP.getInstance().sendLoopFPing();
    }

    @Override
    protected void onPause() {
        super.onPause();
        DswLog.i(TAG + "--onPause--");
        unRegisterHomeKeyReciver();
        ClientUDP.getInstance().setLoop(false);
    }


    @Override
    protected void onDestroy() {
        DswLog.i(TAG + "--onDestroy--");
        try {
            saveOpenSoundState = false;
            saveOpenTalkState = false;

            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }

            stop();

            mHandler.removeCallbacksAndMessages(null);

            if (audioManager != null)
                audioManager.abandonAudioFocus(afChangeListener);

            if (headsetPlugReceiver != null) {
                headsetPlugReceiver.stopListen();
            }

            ClientUDP.getInstance().removeUDPMsgListener(this);

            stopLoadingAnim();

            if (timer != null) {
                timer.cancel();
                timer = null;
            }

            if (inComingReceiver != null)
                unregisterReceiver(inComingReceiver);

            unRegisterConnectedReceiver();

            stopMediaplayerOrVibrator();
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }

        super.onDestroy();

    }

    @Override
    protected void onStop() {
        super.onStop();
        DswLog.i(TAG + "--onStop--");
        // mFinish();
    }

    protected void makeCall() {
        playLoadingAnim();
        mHandler.removeMessages(HANDLER_CREATECALL_OVERYTIME);
        mHandler.sendEmptyMessageDelayed(HANDLER_CREATECALL_OVERYTIME, 30000);
        mCalledTitle.setText(String.format(getString(R.string.doorbell_title), cidData.mName(), getString(R.string.DOOR_ANSWERING)));
        timelength = 0;
        if (isLan && mPong != null && isSupportLan(ClientConstants.SUPPORT_LAN_PLAY_BELL_VERSION, mPong.version)) {
            mManager = new LanCallOut(ClientUDP.getInstance(), mPong);
            isLan = false;
            isPlayByLan = true;
        } else {
            mManager = new WIFICallOut(this, getRelayMaskInfoRsp(), cidData);
            isLan = false;
            isPlayByLan = false;
        }
        mManager.makeCall();
    }

    @Override
    public void handleMsg(int msgId, Object param) {
        switch (CallMessageCallBack.MSG_TO_UI.values()[msgId]) {
            case SERVER_DISCONNECTED:
            case RESOLVE_SERVER_FAILED:
            case CONNECT_SERVER_FAILED:
                stopLoadingAnim();
                if (isPlayByLan) {
                    return;
                }
                discountPrompt(-1, getString(R.string.NO_NETWORK_DOOR) + "(" + msgId + ")");
                break;
            case CONNECT_SERVER_SUCCESS:
                break;
            case RECV_DISCONN:
                stopLoadingAnim();
                if (isPlayByLan) {
                    return;
                }
                int id = StringUtils.toInt(new String((byte[]) param));
                switch (StringUtils.toInt(id)) {
                    case ClientConstants.CAUSE_CALLER_NOTLOGIN:
                    case ClientConstants.CAUSE_PEERDISCONNECT:
                        discountPrompt(id, getString(R.string.NO_NETWORK_DOOR) + "(" + id + ")");
                        break;
                    case ClientConstants.CAUSE_PEERNOTEXIST:
                        discountPrompt(id, getString(R.string.NO_NETWORK_DOOR) + "(" + id + ")");
                        break;
                    case ClientConstants.CAUSE_PEERINCONNECT:

                        ToastUtil.showFailToast(DoorBellCalledActivity.this, getString(R.string.OTHERS_LOOKING));
                        mHandler.sendEmptyMessageDelayed(HANDLER_DELAY_FINISH, 1000);

                        break;
                    case ClientConstants.CAUSE_BELL_CONNECTED:
                        discountPrompt(id, getString(R.string.OTHERS_LOOKING) + "(" + id + ")");
                        break;
                    default:
                        break;
                }
                break;


            case NOTIFY_RESOLUTION:

                JniPlay.StartRendeRemoteView(0, mSurfaceViewOutDoorScen);
                if (getIntent().getLongExtra(ClientConstants.DOOR_BELL_TIME, 0) == 0) {
                    StatService.trackCustomBeginEvent(this, MTATAG, "Doorbell Calling");
                } else {
                    StatService.trackCustomBeginEvent(this, MTATAG, "Doorbell Called");
                }
                mHandler.removeMessages(HANDLER_CREATECALL_OVERYTIME);
                mHandler.removeMessages(HANDLER_PLAYSOUND_15S);
                mHandler.removeMessages(HANDLER_TRY_TO_LOAD_PHOTO);
                String params = new String((byte[]) param);
                isConnected = true;
                mTalk.setEnabled(true);
                mShot.setEnabled(true);
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                timer = new Timer(true);
                timer.schedule(new Ta(), 1000, 1000); // timeTask
                mCalledTitle.setText(String.format(getString(R.string.doorbell_title), cidData.mName(), mSimpleTimeFormat.format(new Date(timelength * 1000))));
                if (isConnected) {
                    if (isOpenTalk) {
                        return;
                    }
                    if (saveOpenSoundState) {
                        toggleSound(false);
                    } else {
                        isOpenSound = !isOpenSound;
                        toggleSound(isOpenSound);
                    }
                    if (saveOpenTalkState) {
                        toggleTalk(true);
                    }

                }
                stopLoadingAnim();
                if (!StringUtils.isEmptyOrNull(params)) {
                    viewHolderFrameLayout.setVisibility(View.VISIBLE);
                    setResolution(params);
                }
                break;

            case NOTIFY_RTCP:
                if (param.toString() != null && isConnected) {
                    findViewById(R.id.speed).setVisibility(View.VISIBLE);
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
                    ((TextView) findViewById(R.id.speed)).setText(liuliang);
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
                                discountPrompt(3, getString(R.string.NO_NETWORK_DOOR) + "(" + -3 + ")");
                            }
                            speedList.clear();
                        }
                    }
                }


                break;
            case TRANSPORT_READY:
                break;
            case MSGPACK_MESSAGE:
                MsgpackMsg.MsgHeader mMsgHeader = (MsgpackMsg.MsgHeader) param;
                if (MsgpackMsg.ID_RELAY_MASK_INFO_RSP == mMsgHeader.msgId) {
                    if (AppManager.getAppManager().isActivityTop(this.getClass().getName())) {
                        if (mMsgHeader.caller.equals(cidData.cid)) {
                            mMemoryMsgRelayMaskInfoRsp = (MsgRelayMaskInfoRsp) mMsgHeader;
                            CacheUtil.saveObject(mMemoryMsgRelayMaskInfoRsp, CacheUtil.getCID_RELAYMASKINFO_KEY(mMemoryMsgRelayMaskInfoRsp.caller));
                            if (!isConnected)
                                makeCall();
                        }
                    }
                } else if (MsgpackMsg.ID_BELL_CONNECTED == mMsgHeader.msgId) {
                    MsgIdBellConnected mMsgIdBellConnected = (MsgIdBellConnected) mMsgHeader;
                    if (mMsgIdBellConnected.caller.equals(cidData.cid)) {
                        mHandler.sendEmptyMessageDelayed(HANDLER_DELAY_FINISH, 500);
                        ToastUtil.showFailToast(this, getString(R.string.DOOR_OTHER_LISTENED));
                    }
                } else if (MsgpackMsg.CLIENT_SYNC_LOGOUT == mMsgHeader.msgId) {
                    MsgSyncLogout mMsgSyncLogout = (MsgSyncLogout) mMsgHeader;
                    MyApp.logout(this);
                    if ((mMsgSyncLogout.trigger_id == MsgpackMsg.CLIENT_CHANGEPASS_REQ) || (mMsgSyncLogout.trigger_id == MsgpackMsg.CLIENT_SETPASS_REQ)) {
                        MyApp.showForceNotifyDialog(this, getString(R.string.PWD_CHANGED));
                    } else {
                        MyApp.showForceNotifyDialog(this, getString(R.string.RET_ESESSION_NOT_EXIST));
                    }
                }
                break;

        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getIntent().getLongExtra(ClientConstants.DOOR_BELL_TIME, 0) == 0) {
            StatService.trackCustomEndEvent(this, MTATAG, "Doorbell Calling");
        } else {
            StatService.trackCustomEndEvent(this, MTATAG, "Doorbell Called");
        }
        JniPlay.DisconnectFromPeer();
    }

    @SuppressWarnings("deprecation")
    void toggleSound(boolean isOpenSound) {
        if (isOpenSound) {
            saveOpenSoundState = false;
            audioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN);

            JniPlay.EnableSpeaker(true);
            JniPlay.EnableMike(false);
            MsgAudioControl msg = new MsgAudioControl(cidData.cid);
            msg.mSpeaker = false;
            msg.mMike = true;
            JniPlay.SendBytes(msg.toBytes());

            DswLog.i("send toggleSound MsgAudioControl-->" + msg.toString());

        } else {
            saveOpenSoundState = true;

            JniPlay.EnableMike(false);
            JniPlay.EnableSpeaker(false);
            MsgAudioControl msg = new MsgAudioControl(cidData.cid);
            msg.mSpeaker = false;
            msg.mMike = false;
            JniPlay.SendBytes(msg.toBytes());
            DswLog.i("send toggleSound MsgAudioControl-->" + msg.toString());
            audioManager.abandonAudioFocus(afChangeListener);
        }
        ThreadPoolUtils.execute(new SpeakerPhoneWorker(isConnectHeadsetPlug, audioManager));
    }

    void toggleTalk(boolean isOpenTalk) {
        if (isOpenTalk) {
            saveOpenTalkState = true;
            audioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN);
            mTalk.setImageResource(R.drawable.btn_doorbell_talkback);
            JniPlay.EnableSpeaker(true);
            JniPlay.EnableMike(true);
            MsgAudioControl msg = new MsgAudioControl(cidData.cid);
            msg.mSpeaker = true;
            msg.mMike = true;
            JniPlay.SendBytes(msg.toBytes());
            DswLog.i("send toggleTalk MsgAudioControl-->" + msg.toString());

        } else {
            saveOpenTalkState = false;
            mTalk.setImageResource(R.drawable.btn_doorbell_talkback_allowed);
            JniPlay.EnableSpeaker(true);
            JniPlay.EnableMike(false);
            MsgAudioControl msg = new MsgAudioControl(cidData.cid);
            msg.mSpeaker = false;
            msg.mMike = true;
            JniPlay.SendBytes(msg.toBytes());
            DswLog.i("send toggleTalk MsgAudioControl-->" + msg.toString());
            audioManager.abandonAudioFocus(afChangeListener);
        }
        ThreadPoolUtils.execute(new SpeakerPhoneWorker(isConnectHeadsetPlug, audioManager));
    }


    private AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                audioManager.abandonAudioFocus(afChangeListener);
            }

        }
    };

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
    public void dragRight() {
        mHandler.removeMessages(HANDLER_NORESPONSE_15S);
        stopMediaplayerOrVibrator();
        startCall();
    }

    @Override
    public void dragLeft() {
        mFinish();
        mHandler.removeMessages(HANDLER_NORESPONSE_15S);
        stopMediaplayerOrVibrator();
    }

    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_NORESPONSE_15S:
                    notifycation(new Intent(DoorBellCalledActivity.this, DoorBellActivity.class).putExtra(ClientConstants.CIDINFO, cidData).addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS));

                    mFinish();
                    break;
                case HANDLER_CREATECALL_OVERYTIME:
                    stopLoadingAnim();
                    discountPrompt(-1, getString(R.string.GLOBAL_NO_NETWORK));

                    break;
                case HANDLER_PLAYSOUND_15S:
                    stopMediaplayerOrVibrator();
                    break;
                case HANDLER_DELAY_FINISH:
                    mHandler.removeMessages(HANDLER_DELAY_FINISH);
                    mFinish();
                    break;
                case HANDLER_SHOT_PHOTO:
                    SaveShotPhotoRunnable.SaveShotPhoto ssp = (SaveShotPhotoRunnable.SaveShotPhoto) msg.obj;
                    new ShotPhotoAnimation(DoorBellCalledActivity.this, ((RelativeLayout) findViewById(R.id.root)));
                    Utils.sendBroad2System(DoorBellCalledActivity.this, ssp.mPath);
                    break;
                case HANDLER_TRY_TO_LOAD_PHOTO:
                    loadImage();
                    break;
            }
            super.handleMessage(msg);
        }
    };


    private void stopMediaplayerOrVibrator() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
        } else if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.cancel();
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
        if (req.mCid.equals(cidData.cid)) {
            mPong = req;
            isLan = true;
        }
    }

    @Override
    public void JfgMsgFAck(ClientUDP.JFG_F_ACK ack) {

    }

    @Override
    public void JfgMsgBellPress(ClientUDP.JFGCFG_HEADER data) {

    }

    private class Ta extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() { // UI thread
                @Override
                public void run() {
                    timelength++;
                    mCalledTitle.setText(String.format(getString(R.string.doorbell_title), cidData.mName(), Utils.parse2Time(timelength)));
                }
            });

        }

    }

    protected void mFinish() {
        stop();
        AppManager.getAppManager().finishActivity(this);
    }

    protected void discountPrompt(int msgid, String str) {
        // ToastUtil.getToastUtilInstance().showFailToast(DoorBellCalledActivity.this, str);
        stop();
        isConnected = false;
        showTryDialog(str);
        if (timer != null)
            timer.cancel();
        mCalledTitle.setText(String.format(getString(R.string.doorbell_title), cidData.mName(), getString(R.string.DOOR_ANSWERING)));
        isOpenSound = false;
        isOpenTalk = false;
        mTalk.setEnabled(false);
        mShot.setEnabled(false);
        isPlayByLan = false;

    }

    private void showTryDialog(String title) {
        if (mTryDialog == null)
            mTryDialog = new NotifyDialog(this);
        mTryDialog.setButtonText(R.string.TRY_AGAIN, R.string.CANCEL);
        mTryDialog.setCancelable(false);
        mTryDialog.setCanceledOnTouchOutside(false);
        mTryDialog.show(title, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mTryDialog.dismiss();
                        makeCall();
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


    private void notifycation(Intent intent) {
        NotificationUtil.notifycation(this,
                Utils.getRandom(0, 1000),
                R.drawable.icon_notify,
                Utils.getApplicationName(this),
                String.format("%s:%s", cidData.mName(), this.getString(R.string.someone_call)),
                true,
                true,
                intent
        );
        sendBroadcast(new Intent(ClientConstants.ACTION));
    }

    private class IncomingPhoneReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String flag = intent.getStringExtra(PhoneBroadcastReceiver.TAG);
            if (flag.equals(PhoneBroadcastReceiver.CALL_STATE_RINGING)) {
                Log.d(TAG, "Incoming phone");
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

    private void registerHomeKeyReciver(Activity activity) {
        homeWatcherReveiver = new HomeWatcherReveiver(activity);
        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(homeWatcherReveiver, filter);
    }

    private void unRegisterHomeKeyReciver() {
        if (homeWatcherReveiver != null) {
            unregisterReceiver(homeWatcherReveiver);
        }
    }

    private void registerConnectedReceiver() {
        connectedReceiver = new DeviceConnectedReceiver(mHandler, HANDLER_DELAY_FINISH);
        IntentFilter filter = new IntentFilter("ID_BELL_CONNECTED");
        registerReceiver(connectedReceiver, filter);
    }

    private void unRegisterConnectedReceiver() {
        if (connectedReceiver != null)
            unregisterReceiver(connectedReceiver);
    }


    private void showAuthDialog() {
        final NotifyDialog mDialog = new NotifyDialog(this);
        mDialog.hideNegButton();
        mDialog.setButtonText(R.string.WELL_OK, R.string.CANCEL);
        mDialog.show(String.format(getString(R.string.permission_auth), Utils.getApplicationName(DoorBellCalledActivity.this), getString(R.string.sound_auth)), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();

            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
    }

    private MsgRelayMaskInfoRsp getRelayMaskInfoRsp() {
        if (mMemoryMsgRelayMaskInfoRsp != null)
            return mMemoryMsgRelayMaskInfoRsp;
        if (mDiskMsgRelayMaskInfoRsp != null) {
            return mDiskMsgRelayMaskInfoRsp;
        } else {
            mDiskMsgRelayMaskInfoRsp = (MsgRelayMaskInfoRsp) CacheUtil.readObject(CacheUtil.getCID_RELAYMASKINFO_KEY(cidData.cid));
            if (mDiskMsgRelayMaskInfoRsp != null) {
                return mDiskMsgRelayMaskInfoRsp;
            }
        }
        return null;
    }


    private void setResolution(String params) {
        if (params.contains(",")) {
            String[] org = params.split(",");
            String[] res = org[1].split("x");
            remoteWidth = StringUtils.toInt(res[0]);
            remoteHeight = StringUtils.toInt(res[1]);
        } else {
            String[] res = params.split("x");
            remoteWidth = StringUtils.toInt(res[0]);
            remoteHeight = StringUtils.toInt(res[1]);
        }
        LayoutParams lp = (RelativeLayout.LayoutParams) viewHolderFrameLayout.getLayoutParams();

        lp.width = DensityUtil.getScreenWidth(this);
        lp.height = (lp.width * remoteHeight) / remoteWidth;
        viewHolderFrameLayout.setLayoutParams(lp);

        LayoutParams mmSurfaceViewLayoutParams = (RelativeLayout.LayoutParams) mVideoLayout.getLayoutParams();

        mmSurfaceViewLayoutParams.width = DensityUtil.getScreenWidth(this);
        mmSurfaceViewLayoutParams.height = (mmSurfaceViewLayoutParams.width * remoteHeight) / remoteWidth;
        mVideoLayout.setLayoutParams(mmSurfaceViewLayoutParams);
    }


    private void stop() {
        if (mManager != null) {
            mManager.stop();
            isLan = false;
            mManager = null;
        }
    }


    private boolean isSupportLan(String compareVersion, String currentVersion) {
        if (compareVersion.equals(currentVersion))
            return true;
        String[] compare = compareVersion.split("\\.");
        String[] current = currentVersion.split("\\.");
        if (compare.length == 4 && current.length == 4) {
            for (int i = 0; i < current.length; i++) {
                if (Integer.parseInt(compare[i]) > Integer.parseInt(current[i])) {
                    return false;
                } else if (Integer.parseInt(compare[i]) < Integer.parseInt(current[i])) {
                    return true;
                } else {
                    continue;
                }
            }
        }
        return false;
    }

}
