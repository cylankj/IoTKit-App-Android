package com.cylan.jiafeigou.n.view.bell;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.BaseFullScreenActivity;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.view.CallablePresenter;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.INotify;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.NotifyManager;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.bell.BellLivePresenterImpl;
import com.cylan.jiafeigou.n.view.media.NormalMediaFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.bell.DragLayout;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;
import com.cylan.jiafeigou.widget.glide.RoundedCornersTransformation;
import com.cylan.jiafeigou.widget.live.ILiveControl;
import com.cylan.jiafeigou.widget.pop.RelativePopupWindow;
import com.cylan.jiafeigou.widget.pop.RoundCardPopup;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

import butterknife.BindView;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.PermissionUtils;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class BellLiveActivity extends BaseFullScreenActivity<BellLiveContract.Presenter>
        implements DragLayout.OnDragReleaseListener, View.OnClickListener
        , BellLiveContract.View, ILiveControl.Action {

    @BindView(R.id.fLayout_bell_live_holder)
    FrameLayout fLayoutBellLiveHolder;
    @BindView(R.id.tv_bell_live_flow)
    TextView mBellFlow;
    @BindView(R.id.imgv_bell_live_switch_to_land)
    ImageView imgvBellLiveSwitchToLand;
    @BindView(R.id.dLayout_bell_hot_seat)
    DragLayout dLayoutBellHotSeat;
    @BindView(R.id.imgv_bell_live_capture)
    ImageView imgvBellLiveCapture;
    @BindView(R.id.imgv_bell_live_hang_up)
    ImageView imgvBellLiveHangUp;
    @BindView(R.id.imgv_bell_live_speaker)
    ImageView imgvBellLiveSpeaker;
    @BindView(R.id.fLayout_bell_after_live)
    FrameLayout fLayoutBellAfterLive;
    @BindView(R.id.act_bell_live_video_picture)
    ImageView mBellLiveVideoPicture;
    @BindView(R.id.act_bell_live_video_view_container)
    FrameLayout mVideoViewContainer;
    @BindView(R.id.act_bell_live_video_play_controller)
    ILiveControl mVideoPlayController;
    @BindView(R.id.act_bell_live_back)
    TextView mBellLiveBack;
    @BindView(R.id.view_bell_handle)
    ImageView mBellhandle;
    private ImageView mLandBellLiveSpeaker;
    /**
     * 水平方向的view
     */
    private WeakReference<View> fLayoutLandHolderRef;

    private SurfaceView mSurfaceView;

    private String mNewCallHandle;

    private String mLiveTitle = "宝宝的房间";

    private boolean isLandMode = false;
    //    private boolean isLanchFromBellCall = false;
    private MediaPlayer mediaPlayer;
    private RoundCardPopup roundCardPopup;


    @Override
    protected int getContentViewID() {
        return R.layout.activity_bell_live;
    }

    @Override
    protected void initViewAndListener() {
        //锁屏状态下显示门铃呼叫
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |               //这个在锁屏状态下
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        registSreenStatusReceiver();
        Device device = DataSourceManager.getInstance().getJFGDevice(mUUID);
        if (device != null) {
            mLiveTitle = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
        }
        ViewUtils.updateViewHeight(fLayoutBellLiveHolder, 0.75f);
        dLayoutBellHotSeat.setOnDragReleaseListener(this);
        mVideoPlayController.setAction(this);
        fLayoutBellLiveHolder.setOnClickListener(view -> {
            if (!isLandMode) {
                handlePortClick();
            }
        });
    }

    private void initHeadSetEventReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headsetPlugReceiver, intentFilter);
    }

    private BroadcastReceiver headsetPlugReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.option.HEADSET_PLUG".equals(action)) {
                if (intent.hasExtra("state")) {
                    if (intent.getIntExtra("state", 0) == 0) {
                        BellLiveActivityPermissionsDispatcher.handleHeadsetDisconnectedWithCheck(BellLiveActivity.this);
                    } else if (intent.getIntExtra("state", 0) == 1) {
                        AppLogger.e("耳机已连接");
                        handleHeadsetConnected();
//                        BellLiveActivityPermissionsDispatcher.handleHeadsetConnectedWithCheck(BellLiveActivity.this);
                    }
                }
            }
        }

    };

    @NeedsPermission(Manifest.permission.MODIFY_AUDIO_SETTINGS)
    void handleHeadsetConnected() {
        AudioManager manager = (AudioManager) getSystemService(AUDIO_SERVICE);
        manager.setSpeakerphoneOn(false);

    }

    @OnPermissionDenied(Manifest.permission.MODIFY_AUDIO_SETTINGS)
    void onModifyAudioSettingFaild() {
        AppLogger.d("切换模式失败");
    }

    @NeedsPermission(Manifest.permission.MODIFY_AUDIO_SETTINGS)
    void handleHeadsetDisconnected() {

    }

    private void handlePortClick() {
        int visibility = mVideoViewContainer.getSystemUiVisibility();
        if (visibility != View.SYSTEM_UI_FLAG_VISIBLE) {//说明状态栏被隐藏了,则显示三秒后隐藏
            setNormalBackMargin();
        } else {//说明状态栏没有隐藏,则直接隐藏
            hideStatusBar();
        }
    }

    private Runnable mHideStatusBarAction = this::hideStatusBar;

    public void hideStatusBar() {
        mVideoViewContainer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setHideBackMargin();
    }

    private void setNormalBackMargin() {
        mBellLiveBack.setVisibility(View.VISIBLE);
        mBellLiveBack.animate().setDuration(200).translationY(0);
        mBellFlow.animate().setDuration(200).translationY(0);
        mVideoViewContainer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        mVideoViewContainer.removeCallbacks(mHideStatusBarAction);
        mVideoViewContainer.postDelayed(mHideStatusBarAction, 3000);
    }

    private void setHideBackMargin() {
        mBellLiveBack.setVisibility(View.VISIBLE);
        mBellLiveBack.animate().setDuration(200).translationY(-getResources().getDimension(R.dimen.y21));
        mBellFlow.animate().setDuration(200).translationY(-getResources().getDimension(R.dimen.y20));
    }

    @Override
    protected BellLiveContract.Presenter onCreatePresenter() {
        return new BellLivePresenterImpl();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        setIntent(intent);//直接無視新的呼叫
//        newCall();
    }

    private void newCall() {
        String extra = getIntent().getStringExtra(JConstant.VIEW_CALL_WAY_EXTRA);
        long time = getIntent().getLongExtra(JConstant.VIEW_CALL_WAY_TIME, System.currentTimeMillis());
        CallablePresenter.Caller caller = new CallablePresenter.Caller();
        caller.caller = mUUID;
        caller.picture = extra;
        caller.callTime = time;
        mPresenter.newCall(caller);
//        if (TextUtils.equals(onResolveViewLaunchType(), JConstant.VIEW_CALL_WAY_VIEWER)) {
//            isLanchFromBellCall = false;
//        } else if (TextUtils.equals(onResolveViewLaunchType(), JConstant.VIEW_CALL_WAY_LISTEN)) {
//            isLanchFromBellCall = true;
//        }

        onSpeaker(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initHeadSetEventReceiver();
        muteAudio(true);
        setNormalBackMargin();
        mVideoViewContainer.removeCallbacks(mHideStatusBarAction);
        mVideoViewContainer.postDelayed(mHideStatusBarAction, 3000);
        newCall();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        if (mSurfaceView != null && mSurfaceView instanceof GLSurfaceView) {
            ((GLSurfaceView) mSurfaceView).onPause();
            mVideoViewContainer.removeAllViews();
            mSurfaceView = null;
            finish();
        }
        muteAudio(false);
        clearHeadSetEventReceiver();
    }

    private void clearHeadSetEventReceiver() {
        if (headsetPlugReceiver != null) {
            unregisterReceiver(headsetPlugReceiver);
        }
    }

    @Override
    public void onScreenRotationChanged(boolean land) {
        isLandMode = land;
        handleScreenUpdate(!land);
        getWindow().getDecorView().post(() -> handleSystemBar(!land, 100));
    }

    @Override
    protected void onPrepareToExit(Action action) {
        mPresenter.dismiss();
        finishExt();
        action.actionDone();
    }

    private void handleScreenUpdate(final boolean port) {
        initLandView();
        fLayoutLandHolderRef.get()
                .setVisibility(port ? View.GONE : View.VISIBLE);
        if (port) {
            mBellLiveBack.removeCallbacks(mHideStatusBarAction);
            hideStatusBar();
            setHideBackMargin();
            ViewUtils.updateViewHeight(fLayoutBellLiveHolder, 0.75f);
            mBellLiveBack.setText(null);
            imgvBellLiveSwitchToLand.setVisibility(View.VISIBLE);
        } else {
            setHideBackMargin();
            ViewUtils.updateViewMatchScreenHeight(fLayoutBellLiveHolder);
            mBellLiveBack.setText(mLiveTitle);
            imgvBellLiveSwitchToLand.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.act_bell_live_back)
    public void bellBack() {
        super.onBackPressed();
    }


    /**
     * 初始化 Layer层view，横屏全屏时候，需要在上层
     */
    private void initLandView() {
        if (fLayoutLandHolderRef == null || fLayoutLandHolderRef.get() == null) {
            View view = LayoutInflater.from(getAppContext())
                    .inflate(R.layout.layout_bell_live_land_layer, null);
            if (view != null) {
                fLayoutLandHolderRef = new WeakReference<>(view);
                view.findViewById(R.id.imgv_bell_live_land_capture)
                        .setOnClickListener(this);
                view.findViewById(R.id.imgv_bell_live_land_hangup)
                        .setOnClickListener(this);
                mLandBellLiveSpeaker = (ImageView) view.findViewById(R.id.imgv_bell_live_land_mic);
                mLandBellLiveSpeaker.setOnClickListener(this);
            }
        }
        View v = fLayoutBellLiveHolder.findViewById(R.id.fLayout_bell_live_land_layer);
        if (v == null) {
            fLayoutBellLiveHolder.addView(fLayoutLandHolderRef.get());
        }

    }

    @Override
    public void onRelease(int side) {
        if (side == 0) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mPresenter.dismiss();
        } else {
            if (mediaPlayer != null && mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mPresenter.pickup();
        }
    }

    @Override
    public void onLoginState(int state) {

    }


    @OnClick({R.id.imgv_bell_live_capture,
            R.id.imgv_bell_live_hang_up,
            R.id.imgv_bell_live_speaker,
            R.id.imgv_bell_live_switch_to_land,
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgv_bell_live_capture:
            case R.id.imgv_bell_live_land_capture:
                BellLiveActivityPermissionsDispatcher.captureWithStoragePermissionWithCheck(this);
                break;
            case R.id.imgv_bell_live_speaker:
            case R.id.imgv_bell_live_land_mic:
                BellLiveActivityPermissionsDispatcher.switchSpeakerWithPermissionWithCheck(this);
                break;
            case R.id.imgv_bell_live_land_hangup:
            case R.id.imgv_bell_live_hang_up:
                mPresenter.dismiss();

                break;
            case R.id.imgv_bell_live_switch_to_land:
                initLandView();
                ViewUtils.setRequestedOrientation(this,
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
        }
    }

    @Override
    public void onResolution(JFGMsgVideoResolution resolution) throws JfgException {
        initVideoView();
        JfgCmdInsurance.getCmd().enableRenderSingleRemoteView(true, mSurfaceView);
        mBellLiveVideoPicture.setVisibility(View.GONE);
        mVideoPlayController.setState(ILiveControl.STATE_IDLE, null);
        imgvBellLiveCapture.setEnabled(true);
        imgvBellLiveSpeaker.setEnabled(true);
        imgvBellLiveSwitchToLand.setEnabled(true);
        imgvBellLiveSwitchToLand.setVisibility(View.VISIBLE);
        if (!mPresenter.checkAudio(1)) {
            mPresenter.switchMicrophone();
        }
//        if (isLanchFromBellCall) {
//            BellLiveActivityPermissionsDispatcher.switchSpeakerWithPermissionWithCheck(this);
//        }
    }

    @Override
    public void onFlowSpeed(int speed) {
        if (mBellFlow.getVisibility() != View.VISIBLE) {
            mBellFlow.setVisibility(View.VISIBLE);
        }
        mBellFlow.setText(MiscUtils.getByteFromBitRate(speed));
    }

    @Override
    public void onLiveStop(int errId) {

    }

    @Override
    public void onTakeSnapShotSuccess(Bitmap bitmap) {
        if (bitmap != null) {
            ToastUtil.showPositiveToast(getString(R.string.SAVED_PHOTOS));
            showPopupWindow(bitmap);
        } else {
            ToastUtil.showPositiveToast(getString(R.string.set_failed));
        }
    }

    private void showPopupWindow(Bitmap bitmap) {
        try {
            roundCardPopup = new RoundCardPopup(this, view -> {
                if (bitmap != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    Glide.with(this)
                            .load(stream.toByteArray())
                            .placeholder(R.drawable.wonderful_pic_place_holder)
                            .override((int) getResources().getDimension(R.dimen.x44),
                                    (int) getResources().getDimension(R.dimen.x30))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .bitmapTransform(new RoundedCornersTransformation(this, 10, 2))
                            .into(view);
                }
            }, v -> {
                roundCardPopup.dismiss();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                Bundle bundle = new Bundle();
                bundle.putByteArray(JConstant.KEY_SHARE_ELEMENT_BYTE, byteArray);
                NormalMediaFragment fragment = NormalMediaFragment.newInstance(bundle);
                ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(), fragment,
                        android.R.id.content);
                fragment.setCallBack(t -> getSupportFragmentManager().popBackStack());
            });
            roundCardPopup.setAutoDismissTime(3000);
            roundCardPopup.showOnAnchor(imgvBellLiveCapture, RelativePopupWindow.VerticalPosition.ABOVE, RelativePopupWindow.HorizontalPosition.CENTER);
        } catch (Exception e) {
            AppLogger.e("showPopupWindow: " + e.getLocalizedMessage());
        }

    }

    @Override
    public void onTakeSnapShotFailed() {
        ToastUtil.showPositiveToast(getString(R.string.set_failed));
    }

    @Override
    public void onDeviceUnBind() {
        AppLogger.d("当前设备已解绑");
        mPresenter.cancelViewer();
        new AlertDialog.Builder(this).setCancelable(false)
                .setPositiveButton(getString(R.string.OK), (dialog, which) -> {
                    finish();
                    Intent intent = new Intent(this, NewHomeActivity.class);
                    startActivity(intent);
                })
                .setMessage(getString(R.string.Tap1_device_deleted))
                .show();
    }

    @Override
    public void onLoading(boolean loading) {
        if (loading) {
            mVideoPlayController.setState(ILiveControl.STATE_LOADING, null);
        } else {
            mVideoPlayController.setState(ILiveControl.STATE_IDLE, null);
        }
    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    void switchSpeakerWithPermission() {
        mPresenter.switchSpeaker();
    }

    @OnPermissionDenied(Manifest.permission.RECORD_AUDIO)
    public void hasNoAudioPermission() {
        Bundle args = new Bundle();
        args.putString(BaseDialog.KEY_TITLE, "");
        args.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, getString(R.string.OK));
        args.putString(SimpleDialogFragment.KEY_CONTENT_CONTENT, getString(R.string.permission_auth, "", getString(R.string.PERMISSION_AUDIO)));
        SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(args);
        dialogFragment.setAction((id, value) -> {

        });
        dialogFragment.show(getSupportFragmentManager(), "audio_permission");
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void captureWithStoragePermission() {
        mPresenter.capture();
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void hasNoStoragePermission() {
        //为删除dialog设置提示信息
        Bundle args = new Bundle();
        args.putString(BaseDialog.KEY_TITLE, "");
        args.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, getString(R.string.OK));
        args.putString(SimpleDialogFragment.KEY_CONTENT_CONTENT, getString(R.string.permission_auth, "", getString(R.string.PERMISSION_STORAGE)));
        SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(args);
        dialogFragment.show(getSupportFragmentManager(), "storage_permission");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            BellLiveActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onListen() {
        dLayoutBellHotSeat.setVisibility(View.VISIBLE);
        fLayoutBellAfterLive.setVisibility(View.GONE);
        imgvBellLiveSwitchToLand.setEnabled(false);
        playSoundEffect();
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        mBellhandle.startAnimation(shake);
    }

    @Override
    public void onNewCallTimeOut() {
//        dismissAlert();
        mVideoPlayController.setState(ILiveControl.STATE_LOADING_FAILED, getString(R.string.Item_ConnectionFail));
        INotify.NotifyBean notify = new INotify.NotifyBean();
        Device device = DataSourceManager.getInstance().getJFGDevice(mUUID);
        int count = 0;
        if (device != null) {
            DPEntity entity = MiscUtils.getMaxVersionEntity(device.getProperty(1004), device.getProperty(1005));
            if (entity != null) {
                count = entity.getValue(0);
            }
        }
        notify.count = count;
        Intent intent = new Intent(this, BellLiveActivity.class);
        intent.putExtra(JConstant.VIEW_CALL_WAY, JConstant.VIEW_CALL_WAY_VIEWER);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, mUUID);
        notify.pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotifyManager.getNotifyManager().sendNotify(notify);
        onDismiss();
    }

    @Override
    public void onVideoDisconnect(int code) {
//        imgvBellLiveCapture.setEnabled(false);
//        imgvBellLiveSpeaker.setEnabled(false);
//        imgvBellLiveSwitchToLand.setEnabled(false);
        switch (code) {
            case JError.ErrorVideoPeerInConnect://其他端在查看
                mVideoPlayController.setState(ILiveControl.STATE_LOADING_FAILED, getString(R.string.CONNECTING));
                break;
            case JError.ErrorVideoPeerNotExist://对端不在线
                mVideoPlayController.setState(ILiveControl.STATE_LOADING_FAILED, getString(R.string.OFFLINE_ERR));
                break;
            case JError.ErrorVideoNotLogin://本端未登录
                mVideoPlayController.setState(ILiveControl.STATE_LOADING_FAILED, getString(R.string.OFFLINE_ERR));
                break;
            case JError.ErrorVideoPeerDisconnect://对端断开
                mVideoPlayController.setState(ILiveControl.STATE_LOADING_FAILED, getString(R.string.OFFLINE_ERR));
                break;
            case JError.ErrorP2PSocket:
                mVideoPlayController.setState(ILiveControl.STATE_LOADING_FAILED, getString(R.string.OFFLINE_ERR));
                break;
            case BAD_NET_WORK:
                mVideoPlayController.setState(ILiveControl.STATE_LOADING_FAILED, getString(R.string.OFFLINE_ERR_1));
                break;
            case BAD_FRAME_RATE:
                mVideoPlayController.setState(ILiveControl.STATE_LOADING_FAILED, getString(R.string.OFFLINE_ERR));
                break;
            default:
                mVideoPlayController.setState(ILiveControl.STATE_LOADING_FAILED, "");
        }
    }

    @Override
    public void onConnectDeviceTimeOut() {
        ToastUtil.showNegativeToast(getString(R.string.NETWORK_TIMEOUT));
        mPresenter.dismiss();
    }

    @Override
    public void onPreviewPicture(String URL) {
        mVideoPlayController.setState(ILiveControl.STATE_IDLE, null);
        mBellLiveVideoPicture.setVisibility(View.VISIBLE);
        Glide.with(this).load(URL).
                placeholder(R.drawable.default_diagram_mask)
                .into(mBellLiveVideoPicture);
    }


    public void onViewer() {
        AudioManager manager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int mode = manager.getMode();
        if (mode != AudioManager.MODE_NORMAL) {
            handleHeadsetConnected();
        }
        mVideoPlayController.setState(ILiveControl.STATE_LOADING, null);
        imgvBellLiveCapture.setEnabled(false);
        imgvBellLiveSpeaker.setEnabled(false);
        imgvBellLiveSwitchToLand.setEnabled(false);
        dLayoutBellHotSeat.setVisibility(View.GONE);
        fLayoutBellAfterLive.setVisibility(View.VISIBLE);

    }


    @Override
    public void onSpeaker(boolean on) {
        boolean hasSelfPermissions = PermissionUtils.hasSelfPermissions(this, Manifest.permission.RECORD_AUDIO);
        if (mLandBellLiveSpeaker != null) {
            mLandBellLiveSpeaker.setImageResource(on && hasSelfPermissions ? R.drawable.doorbell_icon_landscape_talk : R.drawable.doorbell_icon_landscape_no_talk);
        }
        imgvBellLiveSpeaker.setImageResource(on && hasSelfPermissions ? R.drawable.icon_port_voice_on_selector : R.drawable.icon_port_voice_off_selector);
    }

    @Override
    public void onMicrophone(boolean on) {

    }

    @Override
    public String onResolveViewLaunchType() {
        return getIntent().getStringExtra(JConstant.VIEW_CALL_WAY);
    }

    /**
     * 初始化videoView
     *
     * @return
     */
    private void initVideoView() {
        if (mSurfaceView == null) {
            mSurfaceView = (SurfaceView) VideoViewFactory.CreateRendererExt(false,
                    getAppContext(), true);
            mSurfaceView.setId("IVideoView".hashCode());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mSurfaceView.setLayoutParams(params);
            mVideoViewContainer.removeAllViews();
            mVideoViewContainer.addView(mSurfaceView);
        }
        AppLogger.i("initVideoView");
        mSurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        mSurfaceView.getHolder().setFormat(PixelFormat.OPAQUE);
    }


    @Override
    public void onPickup() {
        dLayoutBellHotSeat.setVisibility(View.GONE);
        fLayoutBellAfterLive.setVisibility(View.VISIBLE);

    }

    @Override
    public void onDismiss() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        finishExt();
    }

    @Override
    public void onCallAnswerInOther() {
        ToastUtil.showNegativeToast(getString(R.string.DOOR_OTHER_LISTENED));
    }

    @Override
    public void onNewCallWhenInLive(String person) {
        mNewCallHandle = showAlert(getString(R.string.Tap1_Index_Tips_Newvisitor), getString(R.string.Tap1_Index_Tips_Newvisitor), getString(R.string.EFAMILY_CALL_ANSWER), getString(R.string.IGNORE));
    }

    @Override
    public void onViewAction(int action, String handler, Object extra) {
        if (TextUtils.equals(mNewCallHandle, handler)) {
            switch (action) {
                case VIEW_ACTION_OK:
                    mPresenter.startViewer();
                    break;
                case VIEW_ACTION_CANCEL:

                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void clickImage(int state) {
        switch (state) {
            case ILiveControl.STATE_LOADING_FAILED:
                mPresenter.startViewer();
                mVideoPlayController.setState(ILiveControl.STATE_LOADING, "");
                break;
        }
    }

    @Override
    public void clickText() {
    }

    @Override
    public void clickHelp() {

    }

    @Override
    public void playSoundEffect() {
        mediaPlayer = MediaPlayer.create(this, R.raw.doorbell_called);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    private final AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            AppLogger.e("onAudioFocusChange focusChange = " + focusChange);
            //被其他App切换时，把当前自己的音乐停止
        }
    };

    @Override
    public boolean muteAudio(boolean bMute) {
        boolean isSuccess = false;
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (bMute) {
            int result = am.requestAudioFocus(mOnAudioFocusChangeListener,
                    AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            isSuccess = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        } else {
            int result = am.abandonAudioFocus(mOnAudioFocusChangeListener);
            isSuccess = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        }
        AppLogger.e("pauseMusic bMute=" + bMute + " result=" + isSuccess);
        return isSuccess;
    }

    private void registSreenStatusReceiver() {
        mScreenStatusReceiver = new ScreenStatusReceiver();
        IntentFilter screenStatusIF = new IntentFilter();
        screenStatusIF.addAction(Intent.ACTION_SCREEN_ON);
        screenStatusIF.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenStatusReceiver, screenStatusIF);
    }

    private ScreenStatusReceiver mScreenStatusReceiver;

    class ScreenStatusReceiver extends BroadcastReceiver {
        String SCREEN_ON = "android.intent.action.SCREEN_ON";
        String SCREEN_OFF = "android.intent.action.SCREEN_OFF";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (SCREEN_ON.equals(intent.getAction())) {

            } else if (SCREEN_OFF.equals(intent.getAction())) {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mScreenStatusReceiver);
    }
}
