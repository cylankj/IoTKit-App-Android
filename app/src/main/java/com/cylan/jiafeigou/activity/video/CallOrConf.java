package com.cylan.jiafeigou.activity.video;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.CallMessageCallBack;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.DswLog;
import com.cylan.publicApi.JniPlay;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.activity.main.Help;
import com.cylan.jiafeigou.activity.video.addDevice.AddVideoActivity;
import com.cylan.jiafeigou.activity.video.setting.DeviceSettingActivity;
import com.cylan.jiafeigou.activity.video.setting.SafeProtectActivtiy;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.base.RootActivity;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.engine.ClientUDP;
import com.cylan.jiafeigou.engine.MyService;
import com.cylan.jiafeigou.entity.msg.MsgAudioControl;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.MsgCidGetSetParent;
import com.cylan.jiafeigou.entity.msg.MsgCidSdcardFormat;
import com.cylan.jiafeigou.entity.msg.MsgPush;
import com.cylan.jiafeigou.entity.msg.MsgSyncCidOffline;
import com.cylan.jiafeigou.entity.msg.MsgSyncCidOnline;
import com.cylan.jiafeigou.entity.msg.MsgSyncLogout;
import com.cylan.jiafeigou.entity.msg.MsgSyncSdcard;
import com.cylan.jiafeigou.entity.msg.req.MsgRelayMaskInfoReq;
import com.cylan.jiafeigou.entity.msg.rsp.MsgCidSdcardFormatRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgCidlistRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgRelayMaskInfoRsp;
import com.cylan.jiafeigou.listener.RequestCallback;
import com.cylan.jiafeigou.listener.UDPMessageListener;
import com.cylan.jiafeigou.receiver.HeadsetPlugObserver;
import com.cylan.jiafeigou.receiver.PhoneBroadcastReceiver;
import com.cylan.jiafeigou.utils.AppManager;
import com.cylan.jiafeigou.utils.BitmapUtil;
import com.cylan.jiafeigou.utils.CIDCheck;
import com.cylan.jiafeigou.utils.CacheUtil;
import com.cylan.jiafeigou.utils.DensityUtil;
import com.cylan.jiafeigou.utils.MyImageLoader;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.PathGetter;
import com.cylan.jiafeigou.utils.PermissionChecker;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ThreadPoolUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.Utils;
import com.cylan.jiafeigou.worker.SaveShotPhotoRunnable;
import com.cylan.jiafeigou.worker.SpeakerPhoneWorker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.utils.ImageSizeUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;
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

public class CallOrConf extends RootActivity implements View.OnClickListener, HeadsetPlugObserver.OnHeadsetPlugListener,
        RequestCallback, UDPMessageListener {

    public static final String TAG = "CallOrConf";
    // onActivityResult
    private static final int TO_SET_DEVICE = 0x01;

    public static final String EXPER_MODEL = "EXPER_MODEL";
    // handler
    public final static int MSG_CHANGE_ORIENTATION = 0x01;
    public final static int MSG_CREATECALL_OVER_TIME = 0x02;
    public final static int MSG_PLAYBTN_GONE = 0x03;
    public final static int MSG_LAND_PLAYBAR_DIS = 0x04;
    public final static int MSG_DISCONNECT_DEALY = 0x05;
    public final static int MSG_RECONNECT_VIDEO = 0x06;
    public final static int MSG_SAVE_SHOTPHOTO = 0x07;

    private MsgCidData mData;

    private LinearLayout remoteVideoLayout;
    private RelativeLayout mLandGuideLayout;
    private View mLandTitlebar, mBottom, mLandBottomView;
    private ImageView mLandSetView;
    private TextView mLandTitleView, mHistoryVideo;
    private ImageView mScreenShotView, mMikeView, mVoiceView, mFullScreenView;
    private ImageView mLandPlayView, mLandShotView, mLandVoiceView, mLandMikeView;
    private SurfaceView mRemoteGles20;
    private ImageView mControlBtn;
    private TextView mCodeView;
    private ImageView mCoverView;
    private ViewSwitcher mViewSwitcher;
    private TextView mSubView;
    private TextView mHelpView;
    private boolean isVideoAnswer = false;
    private boolean toggleMike_falg = false;
    private boolean isVoiceOpen = false;
    private boolean isFirst = false;
    /**
     * 是否横屏
     */
    private boolean isLandscape = false;
    /**
     * 记录当前播放按钮状态
     */
    private boolean isPause = true;
    /**
     * 是否分享的摄像头
     */
    private boolean isShareVideo = false;

    private boolean isIntercepLand = false;
    private boolean isIntercepPort = false;
    private boolean isIntercepter = false;
    /**
     * 是否体验账号
     */
    private boolean isExper = false;
    /**
     * 是否已经呼叫
     */
    private boolean isCalled = false;

    private IncomingPhoneReceiver incomingReceiver;

    /**
     * 画面分辨率
     */
    private int width = 0;
    private int height = 0;
    /**
     * 屏幕分辨率
     */
    private int mScreenWidth;
    private int mScreenHeight;

    private HeadsetPlugObserver headsetPlugReceiver;
    private OrientationSensorListener listener;
    private AudioManager audioManager;
    private SensorManager sm;

    private boolean isConnectHeadsetPlug = false;

    private boolean isInitSdcard;

    private boolean isInLan;
    private ClientUDP.JFG_F_PONG mPong;
    private boolean isPlayByLan;

    List<Integer> speedList = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        setContentView(R.layout.activity_call_or_conf);
        MyService.addObserver(this);
        AppManager.getAppManager().addActivity(this);
        mData = (MsgCidData) getIntent().getSerializableExtra(ClientConstants.CIDINFO);
        isExper = getIntent().getBooleanExtra(CallOrConf.EXPER_MODEL, false);
        initView();
        headsetPlugReceiver = new HeadsetPlugObserver(this);
        headsetPlugReceiver.setHeadsetPlugListener(this);
        headsetPlugReceiver.startListen();
        sm = (SensorManager) this.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        listener = new OrientationSensorListener(mChangeOrientationHandler);
        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);
        audioManager = (AudioManager) this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        InitVideoView();
        setBtnEnabled(false);
        if (mData.net != MsgCidData.CID_NET_3G && mData.net != MsgCidData.CID_NET_WIFI) {
            mHistoryVideo.setEnabled(false);
        }
        incomingReceiver = new IncomingPhoneReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PhoneBroadcastReceiver.SEND_ACTION);
        registerReceiver(incomingReceiver, filter);

    }

    private void initView() {
        setRightImageView(this);
        findViewById(R.id.title_cover).getBackground().setAlpha(100);
        MyImageLoader.loadTitlebarImage(this, (ImageView) findViewById(R.id.title_background));
        findViewById(R.id.ico_back).setOnClickListener(this);

        remoteVideoLayout = (LinearLayout) findViewById(R.id.remoteView);
        remoteVideoLayout.setOnClickListener(this);
        mControlBtn = (ImageView) findViewById(R.id.online_control);
        mControlBtn.setOnClickListener(this);
        mLandTitlebar = findViewById(R.id.land_titlebar);
        mBottom = findViewById(R.id.menu_layout);
        mLandBottomView = findViewById(R.id.land_bottom);
        View mLandBackView = findViewById(R.id.ico_land_back);
        mLandSetView = (ImageView) findViewById(R.id.ico_land_set);
        mLandTitleView = (TextView) findViewById(R.id.ico_land_title);
        mVoiceView = (ImageView) findViewById(R.id.voice);
        mMikeView = (ImageView) findViewById(R.id.mike);
        mScreenShotView = (ImageView) findViewById(R.id.screenshot);
        mFullScreenView = (ImageView) findViewById(R.id.fullscreen);
        RelativeLayout mSafeProView = (RelativeLayout) findViewById(R.id.safe_protect_layout);
        mHistoryVideo = (TextView) findViewById(R.id.history_video);
        TextView mNearShareView = (TextView) findViewById(R.id.share);
        mLandVoiceView = (ImageView) findViewById(R.id.ico_land_voice);
        mLandShotView = (ImageView) findViewById(R.id.ico_land_shot);
        ImageView mFullScreenView1 = (ImageView) findViewById(R.id.ico_land_fullscreen);

        mCodeView = (TextView) findViewById(R.id.code_lv);
        mLandPlayView = (ImageView) findViewById(R.id.ico_land_play);
        mLandMikeView = (ImageView) findViewById(R.id.ico_land_mike);
        mCoverView = (ImageView) findViewById(R.id.layout_loading_pic);

        mViewSwitcher = (ViewSwitcher) findViewById(R.id.viewswitcher);
        mViewSwitcher.showNext();

        findViewById(R.id.refresh).setOnClickListener(this);

        mSubView = (TextView) findViewById(R.id.sub);
        mHelpView = (TextView) findViewById(R.id.help);
        mHelpView.setOnClickListener(this);

        mLandMikeView.setOnClickListener(this);

        mLandPlayView.setOnClickListener(this);
        mLandBackView.setOnClickListener(this);
        mMikeView.setOnClickListener(this);
        mScreenShotView.setOnClickListener(this);
        mLandSetView.setOnClickListener(this);
        mLandShotView.setOnClickListener(this);
        mFullScreenView.setOnClickListener(this);
        mFullScreenView1.setOnClickListener(this);
        mVoiceView.setOnClickListener(this);
        mLandVoiceView.setOnClickListener(this);
        mHistoryVideo.setOnClickListener(this);

        mSafeProView.setOnClickListener(this);
        mNearShareView.setOnClickListener(this);

        mScreenWidth = DensityUtil.getScreenWidth(this);
        mScreenHeight = DensityUtil.getScreenHeight(this);
        final String name = mData == null ? "" : mData.mName();
        ((TextView) findViewById(R.id.titleview)).setText(name);
        mLandTitleView.setText(name);

        if (isExper) {
            findViewById(R.id.right_ico).setVisibility(View.GONE);
            mLandSetView.setVisibility(View.GONE);
            findViewById(R.id.quick_menu_layout).setVisibility(View.GONE);
            mScreenShotView.setVisibility(View.INVISIBLE);
            mMikeView.setVisibility(View.INVISIBLE);
            mVoiceView.setVisibility(View.INVISIBLE);
            mLandShotView.setVisibility(View.INVISIBLE);
            mLandMikeView.setVisibility(View.INVISIBLE);
            mLandVoiceView.setVisibility(View.INVISIBLE);
        }

        if (isShareAccount()) {
            isShareVideo = true;
            findViewById(R.id.quick_menu_layout).setVisibility(View.GONE);
            findViewById(R.id.right_ico).setVisibility(View.GONE);
            mLandSetView.setVisibility(View.GONE);
            ((TextView) findViewById(R.id.titleview)).setText(mData.cid);
            mLandTitleView.setText(mData.cid);
        }

        if ((PreferenceUtil.getIsFirstClickAutomaticVideo(this)
                || (mData.sdcard != 0 && mData.err != 0)) && !isExper && !isShareVideo)
            findViewById(R.id.ico_remind).setVisibility(View.VISIBLE);

        if ((mData.net == MsgCidData.CID_NET_3G || mData.net == MsgCidData.CID_NET_WIFI) && !mData.version.equals("") && Utils.isNoSurrortVersion(ClientConstants.UPGRADE_VERSION, mData.version)) {
            showUpgradeDialog();
        }
    }


    @SuppressLint("NewApi")
    private void InitVideoView() {
        mRemoteGles20 = ViERenderer.CreateRenderer(CallOrConf.this, true);
        mRemoteGles20.setDrawingCacheEnabled(true);
        remoteVideoLayout.addView(mRemoteGles20);
        hideTopAndBottom();
    }

    private void setBtnEnabled(Boolean isOpen) {
        mScreenShotView.setEnabled(isOpen);
        mLandShotView.setEnabled(isOpen);
        mVoiceView.setEnabled(isOpen);
        mLandVoiceView.setEnabled(isOpen);
        mMikeView.setEnabled(isOpen);
        mLandMikeView.setEnabled(isOpen);
        mFullScreenView.setEnabled(isOpen);
        if (isExper) {
            mMikeView.setEnabled(false);
            mLandMikeView.setEnabled(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ClientUDP.getInstance().setUDPMsgListener(this);
        ClientUDP.getInstance().setCid(mData.cid);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ImageView safeDot = (ImageView) findViewById(R.id.safe_dot);
        if (PreferenceUtil.getKeyFirstSafe(this)) {
            safeDot.setVisibility(View.GONE);
        }
        isIntercepter = false;
        setPortCoverSize();
        isInitSdcard = false;
        ClientUDP.getInstance().setLoop(true);
        ClientUDP.getInstance().sendLoopFPing();
        StatService.trackBeginPage(this, "Live Video");
        if (!PermissionChecker.isAudioRecordPermissionGrant(this)) {
            showAuthDialog();
        }
    }


    private void makeCall() {
        showLoadingView();
        if (isInLan && mPong != null && isSupportLan(ClientConstants.SUPPORT_LAN_PLAY_VERSION, mPong.version)) {
            int randomPort = Utils.getRandom(10000, 20000);
            ClientUDP.getInstance().sendFPlay(mPong, String.valueOf(randomPort));
            JniPlay.StartFactoryMediaRecv(randomPort);
            isInLan = false;
            isPlayByLan = true;
        } else {
            int netType = (Utils.getNetType(this) == ConnectivityManager.TYPE_WIFI ? 1 : 0);
            if (CacheUtil.readObject(CacheUtil.getCID_RELAYMASKINFO_KEY(mData.cid)) == null) {
                if (mData.net == MsgCidData.CID_NET_3G || mData.net == MsgCidData.CID_NET_WIFI) {
                    JniPlay.SendBytes(new MsgRelayMaskInfoReq(mData.cid).toBytes());
                    return;
                }
            }
            MsgRelayMaskInfoRsp mMsgRelayMaskInfoRsp = (MsgRelayMaskInfoRsp) CacheUtil.readObject(CacheUtil.getCID_RELAYMASKINFO_KEY(mData.cid));
            if (mMsgRelayMaskInfoRsp != null)
                mMsgRelayMaskInfoRsp.callee = mData.cid;
            if (mMsgRelayMaskInfoRsp != null) {
                int relaymask[] = new int[mMsgRelayMaskInfoRsp.mask_list.size()];
                for (int i = 0; i < mMsgRelayMaskInfoRsp.mask_list.size(); i++) {
                    relaymask[i] = mMsgRelayMaskInfoRsp.mask_list.get(i);
                }
                JniPlay.ConnectToPeer(mData.cid, true, netType, false, mData.os, relaymask, false, mData.os == Constants.OS_CAMERA_ANDROID);
            } else {
                JniPlay.ConnectToPeer(mData.cid, true, netType, false, mData.os, new int[0], false, mData.os == Constants.OS_CAMERA_ANDROID);
            }
            isPlayByLan = false;
        }
        mRemoteGles20.setDrawingCacheEnabled(true);
        isCalled = true;

    }

    private void login() {
        mHandler.removeMessages(MSG_CREATECALL_OVER_TIME);
        mHandler.sendEmptyMessageDelayed(MSG_CREATECALL_OVER_TIME, 30000);
        makeCall();
    }

    @Override
    public void onBackPressed() {
        if (isLandscape) {
            isIntercepLand = true;
            setPort();
        } else {
            finish();
        }

    }

    @Override
    protected void onDestroy() {
        try {
            StatService.trackCustomEndEvent(this, TAG, "Live Video");
            ReleaseVideoView();
            mHandler.removeCallbacksAndMessages(null);
            mChangeOrientationHandler.removeCallbacksAndMessages(null);
            headsetPlugReceiver.stopListen();
            listener.removeListener();
            sm.unregisterListener(listener);
            audioManager.abandonAudioFocus(afChangeListener);
            MyService.delObserver(this);
            unregisterReceiver(incomingReceiver);
            AppManager.getAppManager().finishActivity(this);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isIntercepter = true;
        JniPlay.EnableSpeaker(false);
        audioManager.setSpeakerphoneOn(false);
        StatService.trackEndPage(this, "Live Video");
        ClientUDP.getInstance().setLoop(false);
    }


    @Override
    public void onStop() {
        super.onStop();
        isInitSdcard = false;
        if (isVideoAnswer && width != 0 && height != 0) {
            sendPhotoMessage(PathGetter.getCoverPath(CallOrConf.this, mData.cid), false);
        }
        DswLog.e("discount for onStop!");
        discount(false, false);
        ClientUDP.getInstance().removeUDPMsgListener(this);
    }

    public void handleMsg(int msgId, Object obj) {
        try {
            if (CallMessageCallBack.MSG_TO_UI.values()[msgId] != CallMessageCallBack.MSG_TO_UI.MSGPACK_MESSAGE && !AppManager.getAppManager().isActivityTop(this.getClass().getName())) {
                return;
            }
            switch (CallMessageCallBack.MSG_TO_UI.values()[msgId]) {
                case SERVER_DISCONNECTED:
                case RESOLVE_SERVER_FAILED:
                case CONNECT_SERVER_FAILED: {
                    mProgressDialog.dismissDialog();
                    if (isPlayByLan) {
                        return;
                    }
                    if (isVideoAnswer || !isFirst) {
                        if (!isFirst)
                            isFirst = true;
                        mSubView.setText(getString(R.string.OFFLINE_ERR_1) + "(" + msgId + ")");
                    }
                    DswLog.e("discount by CONNECT_SERVER_FAILED!");
                    discount(false, true);
                    break;
                }
                case RECV_DISCONN:
                    if (isPlayByLan) {
                        return;
                    }
                    switch (StringUtils.toInt(new String((byte[]) obj))) {
                        case ClientConstants.CAUSE_PEERDISCONNECT:
                            mSubView.setText(R.string.OFFLINE_ERR);
                            DswLog.e("discount by CAUSE_PEERDISCONNECT!");
                            discount(false, true);
                            break;
                        case ClientConstants.CAUSE_PEERNOTEXIST:
                            mSubView.setText(R.string.OFFLINE_ERR);
                            DswLog.e("discount by CAUSE_PEERNOTEXIST!");
                            discount(false, true);
                            break;
                        case ClientConstants.CAUSE_PEERINCONNECT:
                            mSubView.setText(R.string.CONNECTING);
                            DswLog.e("discount by CAUSE_PEERINCONNECT!");
                            discount(false, true);
                            break;
                        case ClientConstants.CAUSE_CALLER_NOTLOGIN:
                            mSubView.setText(R.string.GLOBAL_NO_NETWORK);
                            DswLog.e("discount by CAUSE_CALLER_NOTLOGIN!");
                            discount(false, true);
                            break;
                        default:
                            mSubView.setText("(" + new String((byte[]) obj) + ")" + getString(R.string.GLOBAL_NO_NETWORK));
                            DswLog.e("discount by RECV_DISCONN DEFAULT!");
                            discount(false, !isVideoAnswer);
                            mHandler.sendEmptyMessageDelayed(MSG_RECONNECT_VIDEO, 3000);
                            break;
                    }

                    break;
                case NOTIFY_RESOLUTION:
                    StatService.trackCustomBeginEvent(this, TAG, "Live Video");
                    JniPlay.StartRendeRemoteView(0, mRemoteGles20);
                    String imageUrl = ImageDownloader.Scheme.FILE.wrap(mData.mThumbPath());
                    MyImageLoader.removeFromCache(imageUrl);
                    setBtnEnabled(true);
                    isVideoAnswer = true;
                    mControlBtn.setVisibility(View.GONE);
                    toggleMike_falg = false;
                    isCalled = false;
                    mSubView.setVisibility(View.GONE);
                    mHandler.removeMessages(MSG_CREATECALL_OVER_TIME);
                    setReslution(new String((byte[]) obj));
                    mCoverView.setVisibility(View.GONE);
                    showRefreshView();
                    mHelpView.setVisibility(View.GONE);
                    break;
                case MSGPACK_MESSAGE: {
                    MsgpackMsg.MsgHeader mMsgHeader = (MsgpackMsg.MsgHeader) obj;
                    if (mMsgHeader.msgId == MsgpackMsg.CLIENT_PUSH) {
                        MsgPush mMsgPush = (MsgPush) mMsgHeader;
                        int pushtype = mMsgPush.push_type;
                        if (pushtype == ClientConstants.PUSH_TYPE_SDCARD_OFF || pushtype == ClientConstants.PUSH_TYPE_SDCARD_ON) {
                            if (mData != null) {
                                if (mData.cid.equals(mMsgPush.cid)) {
                                    mData.sdcard = mMsgPush.push_type == ClientConstants.PUSH_TYPE_SDCARD_OFF ? 0 : 1;
                                    mData.err = mMsgPush.err;
                                }
                            }
                        }

                    } else if (mMsgHeader.msgId == MsgpackMsg.CLIENT_SYNC_SDCARD) {
                        MsgSyncSdcard mMsgSyncSdcard = (MsgSyncSdcard) obj;
                        if (mData != null) {
                            if (mData.cid.equals(mMsgSyncSdcard.caller)) {
                                mData.sdcard = mMsgSyncSdcard.sdcard;
                                mData.err = mMsgSyncSdcard.err;
                            }

                        }
                    } else if (mMsgHeader.msgId == MsgpackMsg.CLIENT_SYNC_CIDONLINE) {
                        MsgSyncCidOnline mMsgSyncCidOnline = (MsgSyncCidOnline) mMsgHeader;
                        String cid = mMsgSyncCidOnline.cid;
                        int net = mMsgSyncCidOnline.net;
                        if (mData != null) {
                            if (mData.cid.equals(cid)) {
                                mData.net = net;
                                mData.name = mMsgSyncCidOnline.name;
                                mData.version = mMsgSyncCidOnline.version;
                                mHistoryVideo.setEnabled(true);
                            }

                        }

                    } else if (mMsgHeader.msgId == MsgpackMsg.CLIENT_SYNC_CIDOFFLINE) {
                        MsgSyncCidOffline mMsgSyncCidOffline = (MsgSyncCidOffline) mMsgHeader;
                        String cid = mMsgSyncCidOffline.cid;
                        if (mData != null) {
                            if (mData.cid.equals(cid)) {
                                mData.net = MsgCidData.CID_NET_OFFLINE;
                                mData.name = "";
                                mHistoryVideo.setEnabled(false);
                            }

                        }

                    } else if (MsgpackMsg.CLIENT_SDCARD_FORMAT_ACK == mMsgHeader.msgId) {
                        mProgressDialog.dismissDialog();
                        MsgCidSdcardFormatRsp mMsgCidSdcardFormatRsp = (MsgCidSdcardFormatRsp) mMsgHeader;
                        int sdcard = mMsgCidSdcardFormatRsp.sdcard;
                        int sdcard_errno = mMsgCidSdcardFormatRsp.err;
                        mData.sdcard = sdcard;
                        mData.err = sdcard_errno;
                        if (AppManager.getAppManager().isActivityTop(this.getClass().getName()) && isInitSdcard) {
                            if (sdcard != 0 && sdcard_errno == 0) {
                                ToastUtil.showSuccessToast(this, getString(R.string.SAVE));
                            } else {
                                ToastUtil.showFailToast(this, getString(R.string.SD_ERR_3));
                            }
                        }
                    } else if (MsgpackMsg.CLIENT_CIDGET_RSP == mMsgHeader.msgId || MsgpackMsg.CLIENT_CIDSET_RSP == mMsgHeader.msgId) {
                        MsgCidGetSetParent mMsgCidGetSetParent = (MsgCidGetSetParent) mMsgHeader;
                        if (Constants.RETOK == mMsgCidGetSetParent.ret) {
                            if (mData.cid.equals(mMsgCidGetSetParent.cid)) {
                                mData.vid = mMsgCidGetSetParent.vid;
                            }
                        }
                    } else if (MsgpackMsg.ID_RELAY_MASK_INFO_RSP == mMsgHeader.msgId) {
                        if (AppManager.getAppManager().isActivityTop(this.getClass().getName())) {
                            MsgRelayMaskInfoRsp mMsgRelayMaskInfoRsp = (MsgRelayMaskInfoRsp) mMsgHeader;
                            CacheUtil.saveObject(mMsgRelayMaskInfoRsp, CacheUtil.getCID_RELAYMASKINFO_KEY(mMsgRelayMaskInfoRsp.caller));
                            if (mMsgRelayMaskInfoRsp.caller.equals(mData.cid)) {
                                if (!isCalled && !isVideoAnswer)
                                    login();
                            }
                        }
                    } else if (MsgpackMsg.BELL_PRESS == mMsgHeader.msgId) {
                        if (!Utils.isRunOnBackground(this)) {
                            mHandler.sendEmptyMessage(MSG_DISCONNECT_DEALY);
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
                }
                break;
                case NOTIFY_RTCP:
                    if (obj.toString() != null) {
                        if (!AppManager.getAppManager().isActivityTop(this.getClass().getName())) {
                            return;
                        }
                        mCodeView.setVisibility(View.VISIBLE);
                        DecimalFormat df = new DecimalFormat("###.0");
                        String[] str = new String((byte[]) obj).split("x");
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
                        mCodeView.setText(liuliang);
                        if (isVideoAnswer) {
                            if (speedList == null)
                                speedList = new ArrayList<>();
                            if (speedList.size() < 10) {
                                speedList.add(ll);
                            } else {
                                Set<Integer> set = new HashSet<>();
                                for (int i = 0; i < 10; i++) {
                                    set.add(speedList.get(i));
                                }
                                if (set.size() == 1) {
                                    DswLog.i("一直没速度，重连");
                                    mSubView.setText("(" + "-3" + ")" + getString(R.string.GLOBAL_NO_NETWORK));
                                    discount(false, true);
                                }
                                speedList.clear();
                            }
                        }
                    }

                    break;
                case TRANSPORT_READY:
                    break;
                case TRANSPORT_FAILED:
                    mSubView.setText("(" + new String((byte[]) obj) + ")" + getString(R.string.GLOBAL_NO_NETWORK));
                    DswLog.e("discount by TRANSPORT_FAILED!");
                    discount(false, !isVideoAnswer);
                    mHandler.sendEmptyMessageDelayed(MSG_RECONNECT_VIDEO, 3000);
                    break;
            }
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }

    @SuppressWarnings("deprecation")
    private void setReslution(String str) {
        if (!StringUtils.isEmptyOrNull(str)) {
            PreferenceUtil.setResolution(CallOrConf.this, str);
            if (str.contains(",")) {
                String[] org = str.split(",");
                String[] res = org[1].split("x");
                width = StringUtils.toInt(res[0]);
                height = StringUtils.toInt(res[1]);
            } else {
                String[] res = str.split("x");
                width = StringUtils.toInt(res[0]);
                height = StringUtils.toInt(res[1]);
            }
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) remoteVideoLayout.getLayoutParams();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) findViewById(R.id.remoteview_layout).getLayoutParams();
            if (!isLandscape && (lp.width != mScreenWidth && lp.height != (mScreenWidth * height) / width)) {
                lp.width = mScreenWidth;
            } else if (isLandscape && (lp.width != mScreenHeight && lp.height != (mScreenHeight * height) / width)) {
                lp.width = mScreenHeight;
            }
            lp.height = (lp.width * height) / width;
            params.height = (lp.width * height) / width;
            remoteVideoLayout.setLayoutParams(lp);
            findViewById(R.id.remoteview_layout).setLayoutParams(params);
        }

    }

    private void ReleaseVideoView() {
        JniPlay.StopRendeRemoteView();
        remoteVideoLayout.removeView(mRemoteGles20);
        mRemoteGles20 = null;
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ico_land_back:
            case R.id.ico_back:
                onBackPressed();
                break;
            case R.id.ico_land_voice:
                hideBarDelay();
            case R.id.voice: {
                if (isVideoAnswer) {
                    if (toggleMike_falg)
                        return;
                    isVoiceOpen = !isVoiceOpen;
                    toggleMute(isVoiceOpen);
                }
            }
            break;
            case R.id.ico_land_mike:
                hideBarDelay();
            case R.id.mike: {
                if (isVideoAnswer) {
                    if (!PermissionChecker.isAudioRecordPermissionGrant(getApplicationContext()) && !toggleMike_falg) {
                        showAuthDialog();
                        return;
                    }
                    toggleMike_falg = !toggleMike_falg;
                    toggleMike(toggleMike_falg);
                    if (toggleMike_falg) {
                        mVoiceView.setEnabled(false);
                        mLandVoiceView.setEnabled(false);
                        isVoiceOpen = true;
                    } else {
                        mVoiceView.setEnabled(true);
                        mLandVoiceView.setEnabled(true);
                    }
                    mVoiceView.setImageResource(R.drawable.btn_open_voice_selector);
                    mLandVoiceView.setImageResource(R.drawable.btn_land_open_voice_selector);
                }
                Utils.disableView(mMikeView);
            }
            break;
            case R.id.ico_land_shot:
                hideBarDelay();
            case R.id.screenshot:
                if (isVideoAnswer) {
                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
                    sendPhotoMessage(PathGetter.getJiaFeiGouPhotos() + df.format(new Date()) + ".png", true);

                }
                break;

            case R.id.ico_land_fullscreen:
                if (isLandscape) {
                    isIntercepLand = true;
                    setPort();
                }
                break;
            case R.id.fullscreen:

                if (isVideoAnswer) {
                    if (width != 0 && height != 0) {
                        if (!isLandscape) {
                            isIntercepPort = true;
                            setLand();
                            mControlBtn.setVisibility(View.GONE);
                            hideBarDelay();
                        }
                    }
                }
                break;
            case R.id.ico_land_set:
            case R.id.right_ico:
                startActivityForResult(new Intent(CallOrConf.this, DeviceSettingActivity.class).putExtra(ClientConstants.CIDINFO, mData), TO_SET_DEVICE);
                findViewById(R.id.ico_remind).setVisibility(View.GONE);
                break;
            case R.id.refresh:
                mViewSwitcher.showNext();
            case R.id.ico_land_play:
                hideBarDelay();
            case R.id.online_control:
                mHandler.removeMessages(MSG_RECONNECT_VIDEO);
                if (!isInLan && !MyApp.getIsLogin()) {
                    if (isVideoAnswer || !isFirst) {
                        isFirst = true;
                        mSubView.setText(R.string.OFFLINE_ERR_1);
                    }
                    DswLog.e("discount by onClick no internet!");
                    discount(false, true);
                    return;
                }

                if (isVideoAnswer) {
                    mControlBtn.setVisibility(isLandscape ? View.GONE : View.VISIBLE);
                    mHandler.removeMessages(MSG_PLAYBTN_GONE);
                    mHandler.sendEmptyMessageDelayed(MSG_PLAYBTN_GONE, 3000);
                    DswLog.e("discount by onClick!");
                    discount(true, false);
                } else {
                    remoteVideoLayout.setVisibility(View.VISIBLE);
                    mLandPlayView.setImageResource(R.drawable.btn_pause);
                    mControlBtn.setImageResource(R.drawable.bg_online_pause_selector);
                    mControlBtn.setVisibility(View.GONE);
                    showLoadingView();
                    login();
                    isPause = false;
                }
                break;
            case R.id.history_video:

                if (mData.sdcard == 0) {
                    ToastUtil.showFailToast(this, getResources().getString(R.string.NO_SDCARD));
                } else {
                    if (mData.err != 0) {
                        final NotifyDialog dialog = new NotifyDialog(this);
                        dialog.setButtonText(R.string.SD_INIT, R.string.CANCEL);
                        dialog.show(R.string.SD_INFO_1, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                switch (v.getId()) {
                                    case R.id.cancel:
                                        dialog.dismiss();
                                        break;
                                    case R.id.confirm:
                                        dialog.dismiss();
                                        if (mData.net == MsgCidData.CID_NET_3G || mData.net == MsgCidData.CID_NET_WIFI) {
                                            formatSdCard();
                                        } else {
                                            ToastUtil.showFailToast(CallOrConf.this, getString(R.string.RET_EUNONLINE_CID));
                                        }
                                        break;
                                }

                            }
                        }, null);
                    } else {
                        startActivity(new Intent(CallOrConf.this, HistoryVideoActivity.class).putExtra(ClientConstants.CIDINFO, mData));
                    }
                }
                break;

            case R.id.remoteView:
                if (isLandscape) {
                    if (mLandTitlebar.isShown()) {
                        mHandler.removeMessages(MSG_LAND_PLAYBAR_DIS);
                        hideTopAndBottom();
                    } else {
                        showTopAndBottom();
                        hideBarDelay();
                    }
                } else {
                    if (mControlBtn.getVisibility() == View.GONE && isVideoAnswer) {
                        mControlBtn.setVisibility(View.VISIBLE);
                        mControlBtn.setImageResource(R.drawable.bg_online_pause_selector);
                        if (!isPause) {
                            mHandler.removeMessages(MSG_PLAYBTN_GONE);
                            mHandler.sendEmptyMessageDelayed(MSG_PLAYBTN_GONE, 3000);
                        }
                    } else if (mControlBtn.getVisibility() == View.VISIBLE && isVideoAnswer && !isPause) {
                        mControlBtn.setVisibility(View.GONE);
                        mHandler.removeMessages(MSG_PLAYBTN_GONE);
                    }

                }

                break;
            case R.id.safe_protect_layout:
                PreferenceUtil.setKeyFirstSafe(this, true);
                startActivity(new Intent(CallOrConf.this, SafeProtectActivtiy.class).putExtra(ClientConstants.CIDINFO, mData));
                break;
            case R.id.share:
                startActivity(new Intent(CallOrConf.this, NearSharedActivity.class).putExtra(ClientConstants.CIDINFO, mData));
                break;
            case R.id.help:
                startActivity(new Intent(CallOrConf.this, Help.class).putExtra(TAG, "flag"));
                break;
        }
    }

    private void formatSdCard() {
        if (!MyApp.getIsLogin()) {
            ToastUtil.showFailToast(CallOrConf.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
            return;
        }
        isInitSdcard = true;
        mProgressDialog.showDialog(getString(R.string.SD_INFO_2));
        MsgCidSdcardFormat mMsgCidSdcardFormat = new MsgCidSdcardFormat(mData.cid);
        MyApp.wsRequest(mMsgCidSdcardFormat.toBytes());
        DswLog.i("send MsgCidSdcardFormat msg-->" + mMsgCidSdcardFormat.toString());

    }


    @SuppressWarnings("deprecation")
    private void toggleMute(boolean state) {
        if (state) {
            audioManager.requestAudioFocus(afChangeListener,
                    AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN);
            mVoiceView.setImageResource(R.drawable.btn_open_voice_selector);
            mLandVoiceView.setImageResource(R.drawable.btn_land_open_voice_selector);
            JniPlay.EnableSpeaker(true);
            JniPlay.EnableMike(false);
            MsgAudioControl msg = new MsgAudioControl(mData.cid);
            msg.mSpeaker = false;
            msg.mMike = true;
            JniPlay.SendBytes(msg.toBytes());
            DswLog.i("send MsgAudioControl-->" + msg.toString());
        } else {
            audioManager.abandonAudioFocus(afChangeListener);
            mVoiceView.setImageResource(R.drawable.btn_close_voice_selector);
            mLandVoiceView.setImageResource(R.drawable.btn_land_close_voice_selector);
            JniPlay.EnableMike(false);
            JniPlay.EnableSpeaker(false);
            MsgAudioControl msg = new MsgAudioControl(mData.cid);
            msg.mSpeaker = false;
            msg.mMike = false;
            JniPlay.SendBytes(msg.toBytes());
            DswLog.i("send MsgAudioControl-->" + msg.toString());
        }
        ThreadPoolUtils.execute(new SpeakerPhoneWorker(isConnectHeadsetPlug, audioManager));
    }

    private void toggleMike(boolean switchcase) {
        if (switchcase) {
            audioManager.requestAudioFocus(afChangeListener,
                    AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN);
            mMikeView.setImageResource(R.drawable.btn_port_mike_open_selector);
            mLandMikeView.setImageResource(R.drawable.btn_mike_open_selector);
            JniPlay.EnableSpeaker(true);
            JniPlay.EnableMike(true);
            MsgAudioControl msg = new MsgAudioControl(mData.cid);
            msg.mSpeaker = true;
            msg.mMike = true;
            JniPlay.SendBytes(msg.toBytes());
            DswLog.i("send MsgAudioControl-->" + msg.toString());
        } else {
            mMikeView.setImageResource(R.drawable.btn_port_mike_close_selector);
            mLandMikeView.setImageResource(R.drawable.btn_mike_close_selector);
            JniPlay.EnableSpeaker(true);
            JniPlay.EnableMike(false);
            MsgAudioControl msg = new MsgAudioControl(mData.cid);
            msg.mSpeaker = false;
            msg.mMike = true;
            JniPlay.SendBytes(msg.toBytes());
            DswLog.i("send MsgAudioControl-->" + msg.toString());
        }

        ToastUtil.showToast(this, switchcase ? getString(R.string.BEGIN_TALKBACK) : getString(R.string.END_TALKBACK), Gravity.CENTER, 3000);
        ThreadPoolUtils.execute(new SpeakerPhoneWorker(isConnectHeadsetPlug, audioManager));
    }

    @Override
    public void JfgMsgPong(ClientUDP.JFG_PONG jfg) {

    }

    @Override
    public void JfgMsgSetWifiRsp(ClientUDP.JFG_RESPONSE rsp) {

    }

    @Override
    public void JfgMsgFPong(ClientUDP.JFG_F_PONG req) {
        if (req.mCid.equals(mData.cid)) {
            DswLog.i("JFG_F_PONG msgid=" + req.mMsgid + " cid=" + req.mCid + " mac=" + req.mac + " version=" + req.version);
            isInLan = true;
            mPong = req;
        }
    }

    @Override
    public void JfgMsgFAck(ClientUDP.JFG_F_ACK ack) {

    }

    @Override
    public void JfgMsgBellPress(ClientUDP.JFGCFG_HEADER data) {
        if (!Utils.isRunOnBackground(this) && MsgCidlistRsp.getInstance().getVideoInfoFromCacheByCid(data.mCid) != null) {
            mHandler.sendEmptyMessage(MSG_DISCONNECT_DEALY);
        }
    }

    void showTopAndBottom() {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Animation top = new TranslateAnimation(0, 0, -mLandTitlebar.getHeight(), 0);
            top.setDuration(350);
            mLandTitlebar.setVisibility(View.VISIBLE);
            mLandTitlebar.startAnimation(top);
            Animation bottom = new TranslateAnimation(0, 0, mLandBottomView.getHeight(), 0);
            bottom.setDuration(350);
            mLandBottomView.startAnimation(bottom);
            mLandBottomView.setVisibility(View.VISIBLE);

            WindowManager.LayoutParams attrs = getWindow().getAttributes();
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(attrs);
        }

    }

    void hideTopAndBottom() {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Animation top = new TranslateAnimation(0, 0, 0, -mLandTitlebar.getHeight());
            top.setDuration(350);
            mLandTitlebar.startAnimation(top);
            mLandTitlebar.setVisibility(View.GONE);
            Animation bottom = new TranslateAnimation(0, 0, 0, mLandBottomView.getHeight());
            bottom.setDuration(350);
            mLandBottomView.startAnimation(bottom);
            mLandBottomView.setVisibility(View.GONE);
        }
    }

    private void sendPhotoMessage(final String name, boolean isShot) {
        byte[] pics = JniPlay.TakeSnapShot(false);
        if (pics != null) {
            Bitmap shotBitmap = BitmapUtil.byte2bitmap(width, height, pics);
            if (!isShot) {
                BitmapUtil.addBitmapToLruCache(name, new ImageView(this), new ImageSize(mScreenWidth, mScreenHeight), shotBitmap);
            }
            ThreadPoolUtils.execute(new SaveShotPhotoRunnable(shotBitmap, name, mHandler, MSG_SAVE_SHOTPHOTO, isShot));
        }
    }


    private void setPort() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        isLandscape = false;
        if (!isExper && !isShareVideo && findViewById(R.id.quick_menu_layout).getVisibility() == View.GONE)
            findViewById(R.id.quick_menu_layout).setVisibility(View.VISIBLE);
        setPortStyle();
        if (!isVideoAnswer && mViewSwitcher.getVisibility() == View.GONE) {
            if (mControlBtn.getVisibility() == View.GONE && !isCalled) {
                mControlBtn.setVisibility(View.VISIBLE);
            }
        }
        if (mLandGuideLayout != null && mLandGuideLayout.getVisibility() == View.VISIBLE)
            mLandGuideLayout.setVisibility(View.GONE);
        Log.d("WEBRTC-JR", "bitmapPauseTime: ");
        if (!isVideoAnswer) {
            mCoverView.setVisibility(View.VISIBLE);
            remoteVideoLayout.setVisibility(View.GONE);
        }
    }

    private void setLand() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        isLandscape = true;
        findViewById(R.id.quick_menu_layout).setVisibility(View.GONE);
        setLandStyle();
        if (PreferenceUtil.getIsFirstVerticalScreen(this)) {
            PreferenceUtil.setIsFirstVerticalScreen(this, false);
            showUseGuide();
        }
    }

    private void setPortStyle() {
        findViewById(R.id.titlebar).setVisibility(View.VISIBLE);
        mLandTitlebar.setVisibility(View.GONE);
        mBottom.setVisibility(View.VISIBLE);
        mLandBottomView.setVisibility(View.GONE);
        setReslution(PreferenceUtil.getResolution(CallOrConf.this));
        setCoverSize();
    }

    private void setLandStyle() {
        findViewById(R.id.titlebar).setVisibility(View.GONE);
        mBottom.setVisibility(View.GONE);
        mLandTitlebar.setVisibility(View.VISIBLE);
        mLandBottomView.setVisibility(View.VISIBLE);
        findViewById(R.id.remoteview_layout).setBackgroundColor(getResources().getColor(R.color.text_color));
        setReslution(PreferenceUtil.getResolution(CallOrConf.this));
        setCoverSize();
    }


    @Override
    public void onHeadsetPlugOn() {
        isConnectHeadsetPlug = true;
        if (isVideoAnswer) {
            audioManager.setSpeakerphoneOn(false);
        }
    }

    @Override
    public void onHeadsetPlugOff() {
        isConnectHeadsetPlug = false;
        if (isVideoAnswer) {
            audioManager.setSpeakerphoneOn(true);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == TO_SET_DEVICE) {
            String handler = data.getStringExtra("handler");
            if (handler.equals("alias")) {
                mData.alias = data.getStringExtra("alias");
                ((TextView) findViewById(R.id.titleview)).setText(mData.mName());
                mLandTitleView.setText(mData.mName());
            } else if (handler.equals("unbindcid")) {
                finish();
            }

        }
    }

    /**
     * @param isShow
     * @param isDis  直播断开
     */
    private void discount(Boolean isShow, boolean isDis) {
        if (mPong != null)
            ClientUDP.getInstance().sendFStop(mPong);
        mHandler.removeMessages(MSG_CREATECALL_OVER_TIME);
        mHandler.removeMessages(MSG_PLAYBTN_GONE);
        mHandler.removeMessages(MSG_RECONNECT_VIDEO);
        setCoverSize();
        showRefreshView();
        JniPlay.DisconnectFromPeer();
        isVideoAnswer = false;
        isCalled = false;
        setBtnEnabled(false);
        isPause = true;
        isPlayByLan = false;

        if (!isShow) {
            if (isDis) {
                mCoverView.setVisibility(View.VISIBLE);
                setOffLineAndDiscountStyle(isDis);
                mCoverView.setImageResource(R.drawable.bg_loading_black);
            } else if (mControlBtn.getVisibility() == View.GONE && !isLandscape) {
                mControlBtn.setVisibility(View.VISIBLE);
            }
        }

        mControlBtn.setImageResource(R.drawable.bg_online_play_selector);
        mLandPlayView.setImageResource(R.drawable.btn_play);

        if (toggleMike_falg) {
            mVoiceView.setImageResource(R.drawable.btn_close_voice_selector);
            mLandVoiceView.setImageResource(R.drawable.btn_land_close_voice_selector);
            mMikeView.setImageResource(R.drawable.btn_port_mike_close_selector);
            mLandMikeView.setImageResource(R.drawable.btn_mike_close_selector);
        } else {
            if (isVoiceOpen) {
                mVoiceView.setImageResource(R.drawable.btn_close_voice_selector);
                mLandVoiceView.setImageResource(R.drawable.btn_land_close_voice_selector);
            }
        }

    }

    /**
     *
     */
    private void setPortCoverSize() {
        if (mCoverView.getVisibility() == View.GONE && !isVideoAnswer) {
            mCoverView.setVisibility(View.VISIBLE);
        }
        if (mData.net != MsgCidData.CID_NET_3G && mData.net != MsgCidData.CID_NET_WIFI) {
            setOffLineAndDiscountStyle(true);
        } else {

            String imageUrl = ImageDownloader.Scheme.FILE.wrap(mData.mThumbPath());
            ImageSize targetSize = ImageSizeUtils.defineTargetSizeForView(new ImageViewAware(new ImageView(this)), new ImageSize(mScreenWidth, mScreenHeight));
            String memoryCacheKey = MemoryCacheUtils.generateKey(imageUrl, targetSize);
            Bitmap bitmap = ImageLoader.getInstance().getMemoryCache().get(memoryCacheKey);
            if (bitmap != null) {
                mCoverView.setImageBitmap(bitmap);
            } else {
                DisplayImageOptions options = new DisplayImageOptions.Builder()
                        .bitmapConfig(Bitmap.Config.RGB_565)
                        .cacheInMemory(true)
                        .showImageOnFail(R.drawable.bg_loading)
                        .build();
                ImageLoader.getInstance().displayImage(imageUrl, mCoverView, options);
            }
            setOffLineAndDiscountStyle(false);
        }

        setCoverSize();

    }

    private void setCoverSize() {
        RelativeLayout.LayoutParams lp;

        if (width != 0 && height != 0) {
            lp = new RelativeLayout.LayoutParams(isLandscape ? mScreenHeight : mScreenWidth, ((isLandscape ? mScreenHeight : mScreenWidth) * height) / width);
        } else {
            lp = new RelativeLayout.LayoutParams(isLandscape ? mScreenHeight : mScreenWidth, ((isLandscape ? mScreenHeight : mScreenWidth) * ((int) getResources().getDimension(R.dimen.y560))) / mScreenWidth);
        }
        mCoverView.setLayoutParams(lp);
    }


    private void hideBarDelay() {
        if (mLandTitlebar.isShown()) {
            mHandler.removeMessages(MSG_LAND_PLAYBAR_DIS);
            mHandler.sendEmptyMessageDelayed(MSG_LAND_PLAYBAR_DIS, 3000);
        }
    }

    private Handler mChangeOrientationHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == MSG_CHANGE_ORIENTATION) {
                int orientation = msg.arg1;

                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) remoteVideoLayout.getLayoutParams();
                if (!isIntercepter) {
                    if (orientation > 225 && orientation < 315) {
                        isIntercepPort = false;
                        if (isVideoAnswer && !isIntercepLand && (width != 0 && height != 0)
                                && (!isLandscape || (lp.width != mScreenHeight && lp.height != (mScreenHeight * height) / width))) {
                            setLand();
                            mControlBtn.setVisibility(View.GONE);
                            hideBarDelay();
                        }
                    } else if ((orientation > 315 && orientation < 360) || (orientation > 0 && orientation < 45)) {
                        isIntercepLand = false;
                        if (isVideoAnswer && !isIntercepPort && (width != 0 && height != 0)
                                && (isLandscape || (lp.width != mScreenWidth && lp.height != (mScreenWidth * height) / width))) {

                            setPort();
                        }
                    }
                }
            }
        }

    };

    private void showUseGuide() {
        mLandGuideLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.view_land_guide, null);
        ImageView mFingerView = (ImageView) mLandGuideLayout.findViewById(R.id.land_guide_finger);
        RelativeLayout view = (RelativeLayout) mLandGuideLayout.findViewById(R.id.land_guide);
        ViewGroup.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        ((RelativeLayout) findViewById(R.id.root)).addView(mLandGuideLayout, params);
        final Animation top = new TranslateAnimation(0, 0, 0, -80);
        top.setRepeatCount(-1);
        top.setRepeatMode(Animation.RESTART);
        top.setDuration(5000);
        mFingerView.startAnimation(top);

        view.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                top.cancel();
                mLandGuideLayout.setVisibility(View.GONE);
                return true;
            }
        });

    }


    private void showLoadingView() {
        mViewSwitcher.setVisibility(View.VISIBLE);
        if (!(mViewSwitcher.getCurrentView() instanceof ProgressBar))
            mViewSwitcher.showNext();
    }

    private void showRefreshView() {
        mViewSwitcher.setVisibility(View.GONE);
        if (mViewSwitcher.getCurrentView() instanceof ProgressBar)
            mViewSwitcher.showPrevious();
    }

    private void setOffLineAndDiscountStyle(boolean isShow) {
        if (isShow) {
            mViewSwitcher.setVisibility(View.VISIBLE);
            if (mViewSwitcher.getCurrentView() instanceof ProgressBar)
                mViewSwitcher.showPrevious();
            mSubView.setVisibility(View.VISIBLE);
            mHelpView.setVisibility(View.VISIBLE);
            mCoverView.setImageResource(R.drawable.bg_loading_black);
            if (mControlBtn.getVisibility() == View.VISIBLE && !isLandscape)
                mControlBtn.setVisibility(View.GONE);

        } else {
            mViewSwitcher.setVisibility(View.GONE);
            if (mViewSwitcher.getCurrentView() instanceof ProgressBar)
                mViewSwitcher.showNext();
            mSubView.setVisibility(View.GONE);
            mHelpView.setVisibility(View.GONE);
            if (mControlBtn.getVisibility() == View.GONE && !isLandscape && !isVideoAnswer)
                mControlBtn.setVisibility(View.VISIBLE);
        }
    }

    private void setRightImageView(View.OnClickListener click) {
        findViewById(R.id.right_ico).setVisibility(View.VISIBLE);
        ((ImageView) findViewById(R.id.right_ico)).setImageResource(R.drawable.btn_online_set_selector);
        findViewById(R.id.right_ico).setOnClickListener(click);
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
            if (flag != null && flag.equals(PhoneBroadcastReceiver.CALL_STATE_RINGING)) {
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                if (audioManager.isWiredHeadsetOn())
                    isConnectHeadsetPlug = true;
                if (audioManager.isSpeakerphoneOn()) {
                    audioManager.setSpeakerphoneOn(false);
                    int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                            currentVolume, AudioManager.STREAM_VOICE_CALL);
                    audioManager.abandonAudioFocus(afChangeListener);
                }
            }
        }
    }

    private void showAuthDialog() {
        final NotifyDialog mDialog = new NotifyDialog(this);
        mDialog.hideNegButton();
        mDialog.setButtonText(R.string.WELL_OK, R.string.CANCEL);
        mDialog.show(String.format(getString(R.string.permission_auth), Utils.getApplicationName(CallOrConf.this), getString(R.string.sound_auth)), new View.OnClickListener() {
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


    private void showUpgradeDialog() {
        final NotifyDialog mDialog = new NotifyDialog(this);
        mDialog.hideNegButton();
        mDialog.setButtonText(R.string.WELL_OK, R.string.CANCEL);
        mDialog.show(R.string.DEVICE_UPGRADE, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                startActivity(new Intent(CallOrConf.this, AddVideoActivity.class));
                finish();

            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CREATECALL_OVER_TIME:
                    mSubView.setText(R.string.NETWORK_TIMEOUT);
                    //discount(false, true);
                    //2.4.5新功能
                    discount(false, !isVideoAnswer);
                    mHandler.sendEmptyMessageDelayed(MSG_RECONNECT_VIDEO, 3000);

                    break;
                case MSG_PLAYBTN_GONE:
                    mControlBtn.setVisibility(View.GONE);
                    break;
                case MSG_LAND_PLAYBAR_DIS:
                    if (CallOrConf.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        if (mLandTitlebar.isShown()) {
                            hideTopAndBottom();
                        }
                    }
                    break;
                case MSG_DISCONNECT_DEALY:
                    discount(false, false);
                    break;
                case MSG_RECONNECT_VIDEO:
                    if (isLandscape) {
                        mControlBtn.performClick();
                    } else {
                        mLandPlayView.performClick();
                    }
                    break;
                case MSG_SAVE_SHOTPHOTO:
                    SaveShotPhotoRunnable.SaveShotPhoto ssp = (SaveShotPhotoRunnable.SaveShotPhoto) msg.obj;
                    if (ssp.isShot) {
                        new ShotPhotoAnimation(CallOrConf.this, ((RelativeLayout) findViewById(R.id.root)));
                        Utils.sendBroad2System(CallOrConf.this, ssp.mPath);
                    }
                    break;
                default:
                    break;
            }
        }
    };


    private boolean isShareAccount() {
        return mData != null && !StringUtils.isEmptyOrNull(mData.share_account);
    }


    private boolean isSupportLan(String compareVersion, String currentVersion) {
        if (!CIDCheck.isUcos(mData.cid))
            return true;
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
