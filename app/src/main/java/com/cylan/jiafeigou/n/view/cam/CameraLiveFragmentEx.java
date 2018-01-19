package com.cylan.jiafeigou.n.view.cam;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.entity.jniCall.JFGVideo;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.module.CameraLiveHelper;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamLivePresenterImpl;
import com.cylan.jiafeigou.n.view.activity.CamSettingActivity;
import com.cylan.jiafeigou.n.view.activity.CameraLiveActivity;
import com.cylan.jiafeigou.n.view.activity.SightSettingActivity;
import com.cylan.jiafeigou.n.view.firmware.FirmwareUpdateActivity;
import com.cylan.jiafeigou.n.view.media.NormalMediaFragment;
import com.cylan.jiafeigou.n.view.mine.HomeMineHelpActivity;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LiveTimeLayout;
import com.cylan.jiafeigou.widget.Switcher;
import com.cylan.jiafeigou.widget.dialog.DoorLockDialog;
import com.cylan.jiafeigou.widget.flip.FlipImageView;
import com.cylan.jiafeigou.widget.flip.FlipLayout;
import com.cylan.jiafeigou.widget.live.ILiveControl;
import com.cylan.jiafeigou.widget.live.LiveControlView;
import com.cylan.jiafeigou.widget.pop.RelativePopupWindow;
import com.cylan.jiafeigou.widget.pop.RoundCardPopup;
import com.cylan.jiafeigou.widget.video.LiveViewWithThumbnail;
import com.cylan.jiafeigou.widget.video.PanoramicView360RS_Ext;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;
import com.cylan.jiafeigou.widget.wheel.HistoryWheelView;
import com.cylan.panorama.CameraParam;
import com.cylan.panorama.Panoramic360ViewRS;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.cylan.jiafeigou.misc.JConstant.CYLAN_TAG;
import static com.cylan.jiafeigou.support.photoselect.helpers.Constants.REQUEST_CODE;
import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_FINISH;

/**
 * A simple {@link Fragment} subclass.
 */
@RuntimePermissions()
public class CameraLiveFragmentEx extends IBaseFragment<CamLiveContract.Presenter> implements CamLiveContract.View, CameraMessageSender.MessageObserver,
        Switcher.SwitcherListener, VideoViewFactory.InterActListener, ILiveControl.Action, FlipImageView.OnFlipListener, HistoryWheelHandler.DatePickerListener {
    @BindView(R.id.cam_live_control_layer)
    RelativeLayout camLiveControlLayer;
    @BindView(R.id.imgV_cam_live_land_nav_back)
    TextView imgVCamLiveLandNavBack;
    @BindView(R.id.imgV_land_cam_switch_speaker)
    ImageView imgVLandCamSwitchSpeaker;
    @BindView(R.id.imgV_land_cam_trigger_mic)
    ImageView imgVLandCamTriggerMic;
    @BindView(R.id.imgV_land_cam_trigger_capture)
    ImageView imgVLandCamTriggerCapture;
    //横屏 top bar
    @BindView(R.id.layout_a)
    FrameLayout liveTopBannerView;
    //流量
    @BindView(R.id.vs_wheel)
    ViewSwitcher historyWheelContainer;
    //loading
    @BindView(R.id.layout_c)
    LiveControlView liveLoadingBar;
    @BindView(R.id.layout_port_flip)
    FlipLayout layoutPortFlip;
    @BindView(R.id.live_time_layout)
    LiveTimeLayout liveTimeLayout;
    @BindView(R.id.imgV_cam_zoom_to_full_screen)
    ImageView imgVCamZoomToFullScreen;
    //防护  |直播|时间|   |全屏|
    @BindView(R.id.layout_d)
    FrameLayout liveBottomBannerView;
    @BindView(R.id.imgV_cam_live_land_play)
    ImageView imgVCamLiveLandPlay;
    @BindView(R.id.btn_load_history)
    TextView btnLoadHistory;
    @BindView(R.id.v_flag)
    View vFlag;
    @BindView(R.id.tv_live)
    TextView tvLive;
    @BindView(R.id.v_divider)
    View vDivider;
    @BindView(R.id.tv_cam_live_land_bottom)
    LinearLayout tvCamLiveLandBottom;
    @BindView(R.id.layout_land_flip)
    FlipLayout layoutLandFlip;
    @BindView(R.id.v_line)
    View vLine;
    //历史录像条
    @BindView(R.id.layout_e)
    RelativeLayout historyParentContainer;
    @BindView(R.id.imgV_cam_switch_speaker)
    ImageView imgVCamSwitchSpeaker;
    @BindView(R.id.imgV_cam_trigger_mic)
    ImageView imgVCamTriggerMic;
    @BindView(R.id.imgV_cam_trigger_capture)
    ImageView imgVCamTriggerCapture;
    //|speaker|mic|capture|
    @BindView(R.id.layout_f)
    FrameLayout bottomControllerContainer;
    @BindView(R.id.sv_switch_stream)
    Switcher svSwitchStream;
    @BindView(R.id.v_live)
    LiveViewWithThumbnail liveViewWithThumbnail;
    @BindView(R.id.sw_cam_live_wheel)
    HistoryWheelView superWheelExt;
    //圆形 柱状 四分一 模式切换
    @BindView(R.id.layout_g)
    FrameLayout liveViewModeContainer;
    @BindView(R.id.rg_view_mode_switch_parent)
    RadioGroup rbViewModeSwitchParent;
    @BindView(R.id.iv_view_mode_switch)
    ImageView ivViewModeSwitch;
    @BindView(R.id.ll_switch_view_mode)
    LinearLayout ll_view_mode_container;
    @BindView(R.id.imgV_land_cam_switch_xunhuan)
    ImageView ivModeXunHuan;
    @BindView(R.id.imgV_cam_door_look)
    ImageView ivCamDoorLock;
    @BindView(R.id.fl_load_history)
    FrameLayout flLoadHistory;
    private boolean enableAutoRotate = false;
    private String uuid;
    private static final String TAG = "CameraLiveFragmentEx:";
    private RoundCardPopup roundCardPopup;
    private HistoryWheelHandler historyWheelHandler;
    private CamLiveContract.Presenter presenter;
    private int pid;
    private boolean isShareAccount = false;
    /**
     * 设备的时区
     */
    private SimpleDateFormat liveTimeDateFormat;
    private Device device;
    private boolean hasMicFeature;
    private boolean hasDoorLock;
    private boolean isLocalOnline = false;
    private boolean hasPendingFinishAction = false;
    private VideoViewFactory.IVideoView videoView;
    private CameraLiveViewModel cameraLiveViewModel = new CameraLiveViewModel();
    private CameraMenuViewModel cameraMenuViewModel = new CameraMenuViewModel();
    private CameraMessageSender cameraMessageSender = new CameraMessageSender();
    private MyEventListener eventListener;

    private Runnable backgroundCheckerRunnable = () -> {
        if (BaseApplication.getPauseViewCount() == 0) {
            //APP 进入了后台,需要停止直播播放,7.0 以上onStop 会延迟10秒,所以不能在 onStop 里停止直播,
            if (presenter != null) {
                Log.d(CameraLiveHelper.TAG, "APP 进入了后台,需要停止直播播放,7.0 以上onStop 会延迟10秒,所以不能在 onStop 里停止直播,");
                presenter.performStopVideoAction(true);
            }
        }
    };


    public CameraLiveFragmentEx() {
        // Required empty public constructor
    }

    public static CameraLiveFragmentEx newInstance(Bundle bundle) {
        CameraLiveFragmentEx fragment = new CameraLiveFragmentEx();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        presenter = new CamLivePresenterImpl(this);
        cameraMessageSender.observeMessages(this);
        cameraLiveViewModel.attachMessageSender(cameraMessageSender);
        cameraMenuViewModel.attachMessageSender(cameraMessageSender);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventListener = new MyEventListener(getActivity());
        setOrientationHandle(eventListener::setRequestedOrientation);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera_live, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //2w显示双排视图  3.1.0功能
        initView(presenter, uuid());
    }

    @Override
    public void onPause() {
        super.onPause();
        liveLoadingBar.removeCallbacks(backgroundCheckerRunnable);
        liveLoadingBar.postDelayed(backgroundCheckerRunnable, 700);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (BuildConfig.DEBUG) {
            Log.d(CameraLiveHelper.TAG, "View 生命周期OnStop已经调用了");
        }
        liveLoadingBar.removeCallbacks(backgroundCheckerRunnable);
        presenter.performStopVideoAction(true);
    }

    private boolean isReallyVisibleToUser() {
        return getUserVisibleHint() && isResumed() && getActivity() != null && presenter != null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isReallyVisibleToUser()) {
            presenter.performCheckVideoPlayError();
        } else if (presenter != null) {
            presenter.performStopVideoAction(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hasPendingFinishAction = false;
        liveLoadingBar.removeCallbacks(backgroundCheckerRunnable);
        if (isReallyVisibleToUser()) {
            presenter.performCheckVideoPlayError();
        }
    }

    @Override
    public void onDestroyView() {
        //1.live view pause
        liveViewWithThumbnail.destroyVideoView();
        super.onDestroyView();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public boolean performBackIntercept(boolean willExit) {
        return onBackPressed(willExit);
    }

    private void enableSensor(boolean enable) {
        boolean autoRotateOn = (Settings.System.getInt(ContextUtils.getContext().getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
        //检查系统是否开启自动旋转
        if (autoRotateOn && enable) {
            AppLogger.d("耗电大户");
            if (eventListener != null) {
                eventListener.enable();
            }
        } else if (eventListener != null) {
            eventListener.disable();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        performReLayoutAction();
        performLayoutAnimation(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        CameraLiveFragmentExPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onVideoPlayStopped(boolean live) {
        Log.d(CameraLiveHelper.TAG, "onLiveStop: " + device.getSn());
        enableSensor(false);
        performReLayoutAction();
        if (isNoPlayError()) {
            liveLoadingBar.changeToPlaying(canShowLoadingBar());
        }
        if (hasPendingFinishAction) {
            hasPendingFinishAction = false;
            CameraLiveActivity activity = (CameraLiveActivity) getActivity();
            if (activity != null) {
                activity.finishExt();
            }
        }
    }

    private boolean isNoPlayError() {
        return presenter != null && presenter.isNoPlayError();
    }

    @Override
    public void onPlayErrorWaitForPlayCompletedTimeout() {
        Log.d(CameraLiveHelper.TAG, "onPlayErrorWaitForPlayCompletedTimeout");
        performReLayoutAction();
        liveLoadingBar.changeToLoadingError(true, getContext().getString(R.string.CONNECTING), null);
    }

    @Override
    public void onPlayErrorUnKnowPlayError(int errorCode) {
        Log.d(CameraLiveHelper.TAG, "onPlayErrorUnKnowPlayError:" + errorCode);
        liveLoadingBar.changeToLoadingError(true, ContextUtils.getContext().getString(R.string.NO_NETWORK_2), null);
    }

    @Override
    public void onPlayErrorInConnecting() {
        Log.d(CameraLiveHelper.TAG, "onPlayErrorInConnecting");
        liveLoadingBar.changeToLoadingError(true, ContextUtils.getContext().getString(R.string.CONNECTING), null); //正在直播...
    }

    @Override
    public void onVideoPlayActionCompleted() {
        Log.d(CameraLiveHelper.TAG, "onVideoPlayActionCompleted");
        performReLayoutAction();
        performLayoutAnimation(false);
        liveLoadingBar.changeToPause(false);

    }

    @Override
    public void onCaptureFinished(Bitmap bitmap) {
        boolean land = isLand();
        boolean finished = getActivity() == null || getActivity().isFinishing();
        FragmentActivity activity = getActivity();
        if (BuildConfig.DEBUG) {
            Log.d(CameraLiveHelper.TAG, "onCaptureFinished,has bitmap:" + (bitmap != null) + ",isLand:" + land + ",activity finished:" + finished);
        }
        if (bitmap == null) {
            ToastUtil.showNegativeToast(getString(R.string.set_failed));
            return;
        }
        if (land) {
            ToastUtil.showNegativeToast(getString(R.string.SAVED_PHOTOS));
            return;//横屏 不需要弹窗.
        }
        if (!finished) {
            roundCardPopup = new RoundCardPopup(getActivity(), view -> view.setImageDrawable(new BitmapDrawable(getResources(), bitmap)), v -> {
                roundCardPopup.dismiss();
                Bundle bundle = new Bundle();
                bundle.putParcelable(JConstant.KEY_SHARE_ELEMENT_BYTE, bitmap);
                bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                NormalMediaFragment fragment = NormalMediaFragment.newInstance(bundle);
                ActivityUtils.addFragmentSlideInFromRight(activity.getSupportFragmentManager(), fragment, android.R.id.content);
                fragment.setCallBack(t -> activity.getSupportFragmentManager().popBackStack());
            });
            roundCardPopup.setAutoDismissTime(5 * 1000L);
            roundCardPopup.showOnAnchor(imgVCamTriggerCapture, RelativePopupWindow.VerticalPosition.ABOVE, RelativePopupWindow.HorizontalPosition.CENTER);
        }
    }

    @Override
    public void onUpdateNormalThumbPicture(Bitmap bitmap) {
        if (BuildConfig.DEBUG) {
            Log.d(CameraLiveHelper.TAG, "onUpdateNormalThumbPicture,has bitmap:" + (bitmap != null));
        }
        liveViewWithThumbnail.showLiveThumbPicture(bitmap, true);
    }

    @Override
    public void onUpdatePanoramaThumbPicture(Bitmap bitmap) {
        if (BuildConfig.DEBUG) {
            Log.d(CameraLiveHelper.TAG, "onUpdatePanoramaThumbPicture,has bitmap:" + (bitmap != null));
        }
        liveViewWithThumbnail.showLiveThumbPicture(bitmap, false);
    }

    @Override
    public void onNetworkResumeGood() {
        if (BuildConfig.DEBUG) {
            Log.d(CameraLiveHelper.TAG, "onNetworkResumeGood");
        }
        performReLayoutAction();
        if (!isLivePlaying()) {
            liveLoadingBar.changeToPlaying(true);
        }
    }

    @Override
    public void showFirmwareDialog() {
        getAlertDialogManager().showDialog(getActivity(),
                getString(R.string.Tap1_Device_UpgradeTips), getString(R.string.Tap1_Device_UpgradeTips),
                getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    Intent intent = new Intent(getActivity(), FirmwareUpdateActivity.class);
                    intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid());
                    startActivity(intent);
                }, getString(R.string.CANCEL), null);
    }

    @Override
    public void onRtcp(JFGMsgVideoRtcp rtcp) {
        AppLogger.d("onRtcp: " + new Gson().toJson(rtcp));
        String flow = MiscUtils.getByteFromBitRate(rtcp.bitRate);
        liveViewWithThumbnail.showFlowView(true, flow);
        setLiveRectTime(rtcp.timestamp, false);
    }

    @Override
    public void onResolution(JFGMsgVideoResolution resolution) throws JfgException {
        AppLogger.d("收到分辨率消息,正在准备直播");
        if (BuildConfig.DEBUG) {
            Log.d(CameraLiveHelper.TAG, "收到分辨率消息,正在准备直播");
        }
        try {
            View videoView = (View) this.videoView;
            if (videoView != null) {
                videoView.setVisibility(VISIBLE);
                this.videoView.detectOrientationChanged();
                Command.getInstance().enableRenderSingleRemoteView(true, videoView);
            }
        } catch (JfgException e) {
            AppLogger.e("err:" + MiscUtils.getErr(e));
        }
    }

    @NeedsPermission({Manifest.permission.RECORD_AUDIO})
    public void audioRecordPermissionGrant_Speaker() {
        if (isLivePlaying()) {
            if (CameraLiveHelper.checkAudioPermission()) {
                presenter.performChangeSpeakerAction();
            } else {
                Log.d(CameraLiveHelper.TAG, "audioRecordPermissionGrant_Speaker没有声音权限权限");
            }
        }
    }

    @NeedsPermission({Manifest.permission.RECORD_AUDIO})
    public void audioRecordPermissionGrant_Mic() {
        if (isLivePlaying()) {
            if (CameraLiveHelper.checkAudioPermission()) {
                presenter.performChangeMicrophoneAction();
            } else {
                Log.d(CameraLiveHelper.TAG, "audioRecordPermissionGrant_Mic没有声音权限权限");
            }
        }
    }

    @NeedsPermission({Manifest.permission.RECORD_AUDIO})
    public void audioRecordPermissionGrantProgramCheck() {
    }

    @OnPermissionDenied({Manifest.permission.RECORD_AUDIO})
    public void audioRecordPermissionDenied() {
        if (!isAdded() || getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        getView().post(() -> getAlertDialogManager().showDialog(getActivity(),
                "RECORD_AUDIO", getString(R.string.permission_auth, getString(R.string.sound_auth)),
                getString(R.string.OK), null));
    }

    @OnNeverAskAgain({Manifest.permission.RECORD_AUDIO})
    public void audioRecordPermissionNeverAsk() {
        audioRecordPermissionDenied();
    }

    @OnShowRationale({Manifest.permission.RECORD_AUDIO})
    public void audioRecordPermissionRational(PermissionRequest request) {
        audioRecordPermissionDenied();
    }

    @Override
    public void onBatteryDrainOut() {
        //当前页面才显示
        if (!isAdded() || !getUserVisibleHint()) {
            return;
        }
        Device device = DataSourceManager.getInstance().getDevice(uuid());
        if (device.available() && JFGRules.hasBatteryNotify(device.pid)) {
            AlertDialogManager.getInstance().showDialog(getActivity(),
                    "onBatteryDrainOut", getString(R.string.Tap1_LowPower),
                    getString(R.string.OK), null, false);
        }
    }

    @Override
    public void onDeviceUnBind() {
        AppLogger.d("当前设备已解绑");
        AlertDialogManager.getInstance().showDialog(getActivity(), getString(R.string.Tap1_device_deleted), getString(R.string.Tap1_device_deleted),
                getString(R.string.OK), (dialog, which) -> {
                    getActivity().finish();
                    Intent intent = new Intent(getContext(), NewHomeActivity.class);
                    startActivity(intent);
                }, false);
    }

    @Override
    public void onOpenDoorError() {
        AppLogger.d("开门失败");
        ToastUtil.showFailureToast(getString(R.string.DOOR_OPEN_FAIL));
    }

    @Override
    public void onOpenDoorSuccess() {
        AppLogger.d("开门成功");
        ToastUtil.showToast(getString(R.string.OPEN_DOOR_SUCCE_MSG));
    }

    @Override
    public void onOpenDoorPasswordError() {
        Log.d(CameraLiveHelper.TAG, "开门密码错误");
        ToastUtil.showToast(getString(R.string.DOOR_WRONG_PSW));
    }

    @Override
    public void onHistoryReached(Collection<JFGVideo> history, boolean isHistoryEmpty) {
        Log.d(CYLAN_TAG, "历史录像视频数据已就绪");
        superWheelExt.setHistoryFiles(history);
        performReLayoutAction();
        if (isHistoryEmpty) {
            ToastUtil.showToast(getResources().getString(R.string.NO_CONTENTS_2));
        }
    }

    @Override
    public void onLoadHistoryFailed() {
        Log.d(CameraLiveHelper.TAG, "加载历史录像失败了");
        btnLoadHistory.setEnabled(true);
        liveLoadingBar.changeToPlaying(canShowLoadingBar());
        if (presenter.isHistoryEmpty()) {
            ToastUtil.showToast(getResources().getString(R.string.Item_LoadFail));
        }
    }

    @Override
    public void onDeviceStandByChanged(boolean isStandBy) {
        Log.d(CameraLiveHelper.TAG, "设备已设置为待机模式");
        performReLayoutAction();
        if (isStandBy) {
            liveLoadingBar.changeToNone();
        } else {
            liveLoadingBar.changeToPlaying(canShowLoadingBar());
        }
    }

    @Override
    public void onPlayErrorFirstSight() {
        Log.d(CameraLiveHelper.TAG, "第一次使用全景模式");
        performReLayoutAction();
        liveLoadingBar.changeToNone();
    }

    @Override
    public void onPlayErrorNoNetwork() {
        Log.d(CameraLiveHelper.TAG, "无网络连接");
        performReLayoutAction();
        liveLoadingBar.changeToLoadingError(true, ContextUtils.getContext().getString(R.string.OFFLINE_ERR_1), ContextUtils.getContext().getString(R.string.USER_HELP));
    }

    @Override
    public void onDeviceChangedToOffLine() {
        Log.d(CameraLiveHelper.TAG, "onDeviceChangedToOffLine");
        performReLayoutAction();
        liveLoadingBar.changeToLoadingError(true, ContextUtils.getContext().getString(R.string.OFFLINE_ERR), ContextUtils.getContext().getString(R.string.USER_HELP));

    }

    @Override
    public void onDeviceChangedToOnline() {
        Log.d(CameraLiveHelper.TAG, "onDeviceChangedToOnline");
        performReLayoutAction();
        if (isLivePlaying()) {
            liveLoadingBar.changeToPause(false);
        } else {
            liveLoadingBar.changeToPlaying(true);
        }
    }

    @Override
    public void onPlayErrorException() {
        if (BuildConfig.DEBUG) {
            Log.d(CameraLiveHelper.TAG, "播放出现异常");
        }
    }

    @Override
    public void onPlayErrorWaitForPlayCompleted() {
        if (BuildConfig.DEBUG) {
            Log.d(CameraLiveHelper.TAG, "已经发起了播放请求,正在等待播放结果,超时或者播放成功?");
        }
        enableSensor(true);
        //|直播| 按钮
        performLayoutVisibilityAction();
        performLayoutEnableAction();
        //现在显示的条件就是手动点击其他情况都不显示
        liveLoadingBar.changeToLoading(canShowLoadingBar());
        imgVCamZoomToFullScreen.setEnabled(false);//测试用
        int net = NetUtils.getJfgNetType();
        if (net == 2) {
            ToastUtil.showToast(getResources().getString(R.string.LIVE_DATA));
        }
    }

    @Override
    public void onPlayErrorLowFrameRate() {
        Log.d(CameraLiveHelper.TAG, "当前帧率低");
        liveLoadingBar.changeToLoading(canShowLoadingBar());
        performLayoutEnableAction();
    }

    @Override
    public void onPlayFrameResumeGood() {
        Log.d(CameraLiveHelper.TAG, "当前帧率已恢复正常");
        liveLoadingBar.changeToPause(false);
        performLayoutEnableAction();
    }

    @Override
    public void onPlayErrorBadFrameRate() {
        Log.d(CameraLiveHelper.TAG, "当前帧率加载失败了");
        performReLayoutAction();
        liveLoadingBar.changeToLoadingError(true, getContext().getString(R.string.NETWORK_TIMEOUT), getContext().getString(R.string.USER_HELP));
    }

    @Override
    public void onUpdateBottomMenuEnable(boolean microphoneEnable, boolean speakerEnable, boolean doorLockEnable, boolean captureEnable) {
        Log.d(CameraLiveHelper.TAG, "micEnable:" + microphoneEnable +
                ",speakerEnable:" + speakerEnable +
                ",doorLockEnable:" + doorLockEnable +
                ",captureEnable:" + captureEnable);

        imgVCamTriggerMic.setEnabled(microphoneEnable);
        imgVLandCamTriggerMic.setEnabled(microphoneEnable);

        imgVCamSwitchSpeaker.setEnabled(speakerEnable);
        imgVLandCamSwitchSpeaker.setEnabled(speakerEnable);

        imgVCamTriggerCapture.setEnabled(captureEnable);
        imgVLandCamTriggerCapture.setEnabled(captureEnable);

        ivCamDoorLock.setEnabled(doorLockEnable);
    }

    @Override
    public void onUpdateBottomMenuOn(boolean speakerOn, boolean microphoneOn) {
        Log.d(CameraLiveHelper.TAG, "speakerOn:" + speakerOn + ",microphoneOn:" + microphoneOn);
        imgVCamSwitchSpeaker.setImageResource(portSpeakerRes[speakerOn ? 1 : 0]);
        imgVLandCamSwitchSpeaker.setImageResource(landSpeakerRes[speakerOn ? 1 : 0]);
        imgVCamTriggerMic.setImageResource(hasDoorLock && !isShareAccount ? portBellMicRes[microphoneOn ? 1 : 0] : portMicRes[microphoneOn ? 1 : 0]);
        imgVLandCamTriggerMic.setImageResource(landMicRes[microphoneOn ? 1 : 0]);
    }

    @Override
    public void onDeviceSDCardOut() {
        Log.d(CameraLiveHelper.TAG, "onDeviceSDCardOut");
        performReLayoutAction();
        if (isReallyVisibleToUser() && !JFGRules.isShareDevice(uuid)) {
            getAlertDialogManager().showDialog(getActivity(), getString(R.string.MSG_SD_OFF), getString(R.string.MSG_SD_OFF), getString(R.string.OK),
                    (DialogInterface d, int which) -> {
                        if (isLivePlaying() && !isLive() && canPlayVideoNow()) {
                            presenter.performPlayVideoAction(true, 0);
                        }

                    });
        }
    }

    @Override
    public void onDeviceSDCardFormat() {
        Log.d(CameraLiveHelper.TAG, "onDeviceSDCardFormat");
        if (isReallyVisibleToUser() && isLivePlaying()) {
            getAlertDialogManager().showDialog(getActivity(), getString(R.string.Clear_Sdcard_tips6),
                    getString(R.string.Clear_Sdcard_tips6),
                    getString(R.string.OK), null,
                    getString(R.string.CANCEL), null);
        }
    }

    @Override
    public void onUpdateLiveViewMode(String _509) {
        Log.d(CameraLiveHelper.TAG, "onUpdateLiveViewMode:" + _509);
        Device device = DataSourceManager.getInstance().getDevice(uuid());
        if (device.pid == 39 || device.pid == 49) {
            _509 = "0";
        }
        updateLiveViewMode(_509);
    }

    @Override
    public void onDeviceTimeZoneChanged(int rawOffset) {
        Log.d(CameraLiveHelper.TAG, "onDeviceTimeZoneChanged:" + rawOffset);
        if (liveTimeDateFormat == null) {
            liveTimeDateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.UK);
        }
        TimeZone zone = TimeZone.getDefault();
        zone.setRawOffset(rawOffset);
        liveTimeDateFormat.setTimeZone(zone);
        liveTimeLayout.setTimeZone(zone);
        superWheelExt.setTimeZone(zone);
    }

    @Override
    public void onUpdateCameraCoordinate(DpMsgDefine.DpCoordinate dpCoordinate) {
        Log.d(CameraLiveHelper.TAG, "onUpdateCameraCoordinate:" + dpCoordinate);
        updateCamParam(dpCoordinate);
    }

    public boolean onBackPressed(boolean willExit) {
        if (!willExit) {
            return false;
        }
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            this.eventListener.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, true);
            return true;
        } else {
            if (!isReallyVisibleToUser()) {
                liveViewWithThumbnail.showVideoView(false);
            }
            presenter.performStopVideoAction(hasPendingFinishAction = true);
            return true;
        }
    }

    @Override
    public void onReceiveMessage(CameraMessageSender.Message message) {

    }

    @Override
    public void onPickDate(long time, int state) {
        //选择时间,更新时间区域,//wheelView 回调的是毫秒时间, rtcp 回调的是秒,这里要除以1000
        if (isLand()) {
            performLayoutAnimation(true);
        }
        switch (state) {
            case STATE_FINISH: {
                setLiveRectTime(time, true);
                presenter.performPlayVideoAction(false, time);
            }
            break;
            default: {
                setLiveTimeContent(false, time);
            }
        }
    }

    class MyEventListener extends com.cylan.jiafeigou.misc.OrientationListener {
        private boolean isShake = false;
        private volatile int orientation = -1;
        private int customOrientation = -1;

        public int getOrientation() {
            return orientation;
        }

        public MyEventListener(Context context) {
            super(context);
        }

        public MyEventListener(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onSensorChanged(int sensor, float[] values) {
            super.onSensorChanged(sensor, values);
            float x = values[0];
            float y = values[1];
            float z = values[2];
            if ((Math.abs(x) > 17 || Math.abs(y) > 17 || Math
                    .abs(z) > 17) && !isShake) {
                // TODO: 2016/10/19 实现摇动逻辑, 摇动后进行震动
                if (isReallyVisibleToUser() && isLivePlaying()) {
                    if (isShakeEnable()) {
                        isShake = true;
                        onShake();
                        liveLoadingBar.postDelayed(() -> {
                            // TODO: 2017/8/31 摇一摇后重置 customOrientation
                            isShake = false;
                            customOrientation = -1;
                        }, 2000);//2秒只允许摇一摇一次
                    }
                }
            }
        }


        @Override
        public void onOrientationChanged(int o) {
            // TODO: 2017/8/30 只能从一个方向旋转到另一个方向,不能从一个方向旋转回自己的方向
            if (((o >= 0) && (o < 45)) || (o > 315)) {//设置竖屏
//                    Log.d(TAG, "设置竖屏");
                this.orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

            } else if (o > 225 && o < 315) { //设置横屏
//                Log.d(TAG, "设置横屏");
                this.orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

            } else if (o > 45 && o < 135) {// 设置反向横屏
//                Log.d(TAG, "反向横屏");
                this.orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;

            } else if (o > 135 && o < 225) {
                this.orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
//                Log.d(TAG, "反向竖屏");
            }
            if (isShake) {
                return;
            }

            if (isReallyVisibleToUser() && isLivePlaying()) {
                // TODO: 2017/8/24 摇一摇开启后不允许自动转屏
                if (!isShakeEnable()) {
                    setRequestedOrientation(this.orientation, false);
                }
            }
        }

        public void setRequestedOrientation(int requestedOrientation, boolean fromUser) {
            if (fromUser) {
                customOrientation = orientation;
                ViewUtils.setRequestedOrientation(getActivity(), requestedOrientation);
            } else {
                if (customOrientation != requestedOrientation) {
                    customOrientation = -1;
                    if (requestedOrientation != getActivity().getRequestedOrientation()) {
                        ViewUtils.setRequestedOrientation(getActivity(), requestedOrientation);
                    }
                }
            }
        }
    }

    private void initListener() {
        //isFriend.流量
        //c.loading
        (liveLoadingBar).setAction(this);
        ivViewModeSwitch.setOnClickListener(this::toggleViewMode);  //平视,1.平视.0俯视.默认平视
        rbViewModeSwitchParent.setOnCheckedChangeListener(this::switchViewMode);
        svSwitchStream.setSwitcherListener(this);
        liveViewWithThumbnail.setInterActListener(this);
        (layoutLandFlip).setFlipListener(this);
        (layoutPortFlip).setFlipListener(this);
        historyWheelHandler.setDatePickerListener(this);
    }

    private void toggleViewMode(View view) {
        if (rbViewModeSwitchParent.getVisibility() == View.VISIBLE) {
            rbViewModeSwitchParent.setVisibility(GONE);
        } else {
            presenter.performViewModeChecker(getDisplayMode());
        }
    }

    private int getDisplayMode() {
        int viewDisplayMode = Panoramic360ViewRS.SFM_Normal;
        if (videoView != null && videoView instanceof Panoramic360ViewRS) {
            viewDisplayMode = ((Panoramic360ViewRS) videoView).getDisplayMode();
        }
        viewDisplayMode = presenter != null ? presenter.getDisplayMode() : viewDisplayMode;
        return viewDisplayMode;
    }

    private int getMountMode() {
        return presenter == null ? 0 : presenter.getMountMode();
    }

    private CameraParam getCameraParam() {
        return presenter == null ? CameraParam.getTopPreset() : presenter.getCameraParam();
    }

    private void performChangeViewDisplayMode(int displayMode, boolean show) {
        rbViewModeSwitchParent.setVisibility(show ? VISIBLE : GONE);
        rbViewModeSwitchParent.check(getCheckIdByViewMode(displayMode));
        if (videoView != null && videoView instanceof Panoramic360ViewRS) {
            Panoramic360ViewRS panoramic360ViewRS = (Panoramic360ViewRS) this.videoView;
            if (displayMode != Panoramic360ViewRS.SFM_Normal) {
                panoramic360ViewRS.setMountMode(Panoramic360ViewRS.MountMode.TOP);
            }
//            else {
//                int mountMode = getMountMode();
//                videoView.setMode(mountMode);
//            }
            panoramic360ViewRS.setDisplayMode(displayMode);
        }
//        CameraParam cameraParam = getCameraParam();
//        if (videoView != null) {
//            videoView.config360(cameraParam);
//            videoView.detectOrientationChanged();
//        }
//        if (device.pid == 39 || device.pid == 49) {
//            mode = "0";
//        }
//        if (!canShowFirstSight()) {
//            updateCamParam(presenter.getDevice().$(510, new DpMsgDefine.DpCoordinate()));
//        } else {
//            if (videoView != null) {
//                videoView.config360(TextUtils.equals(mode, "0") ? CameraParam.getTopPreset() : CameraParam.getWallPreset());
//            }
//        }
//        if (videoView != null) {
//            videoView.setMode(TextUtils.equals("0", mode) ? 0 : 1);
//            videoView.detectOrientationChanged();
//        }
        enableAutoRotate(enableAutoRotate = displayMode == Panoramic360ViewRS.SFM_Normal && enableAutoRotate);
        if (show) {
            performLayoutAnimation(true);
        }
    }


    private int getCheckIdByViewMode(int viewMode) {
        switch (viewMode) {
            case Panoramic360ViewRS.SFM_Cylinder:
                return R.id.rb_view_mode_column;
            case Panoramic360ViewRS.SFM_Normal:
                return R.id.rb_view_mode_circular;
            case Panoramic360ViewRS.SFM_Quad:
                return R.id.rb_view_mode_four;
        }
        return R.id.rb_view_mode_circular;
    }

    private int getViewModeByCheckId(int checkId) {
        switch (checkId) {
            case R.id.rb_view_mode_column:
                return Panoramic360ViewRS.SFM_Cylinder;
            case R.id.rb_view_mode_circular:
                return Panoramic360ViewRS.SFM_Normal;
            case R.id.rb_view_mode_four:
                return Panoramic360ViewRS.SFM_Quad;
            default:
                return Panoramic360ViewRS.SFM_Normal;
        }
    }

    void switchViewMode(RadioGroup radioGroup, int checkId) {
        int checkIdByViewMode = getCheckIdByViewMode(getDisplayMode());
        if (checkId != checkIdByViewMode) {
            presenter.performViewModeChecker(getViewModeByCheckId(checkId));
        }
    }

    public void initView(CamLiveContract.Presenter presenter, String uuid) {
        //竖屏 隐藏
        this.presenter = presenter;
        this.uuid = uuid;
        this.device = DataSourceManager.getInstance().getDevice(uuid);
        this.hasMicFeature = JFGRules.hasMicFeature(device.pid);
        this.hasDoorLock = JFGRules.hasDoorLock(device.pid);
        this.isShareAccount = !TextUtils.isEmpty(device.shareAccount);
        this.pid = device.pid;
        this.historyWheelHandler = new HistoryWheelHandler(superWheelExt, uuid);
        //disable 6个view
        initListener();
        performReLayoutAction();
        presenter.performLocalNetworkPingAction();
    }

    private void updateCamParam(DpMsgDefine.DpCoordinate coord) {
        try {
            CameraParam cp = new CameraParam(coord.x, coord.y, coord.r, coord.w, coord.h, 180);
            if (cp.cx == 0 && cp.cy == 0 && cp.h == 0) {
                cp = CameraParam.getTopPreset();
            }
            if (videoView != null) {
                videoView.config360(cp);
            }
        } catch (Exception e) {
        }
    }

    private boolean isLand() {
        return ContextUtils.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private void setLiveRectTime(long timestamp, boolean focus) {
        //历史视频的时候，使用rtcp自带时间戳。
        //直播时候，使用本地时间戳。
        //全景的时间戳是0,使用设备的时区
        boolean historyLooked = superWheelExt.isLocked();
        boolean live = isLive();
        if (!live && !historyLooked && timestamp != 0 || focus) {
            superWheelExt.scrollToPosition(TimeUtils.wrapToLong(timestamp), focus, focus);
        }
        setLiveTimeContent(live, historyLooked ? liveTimeLayout.lastDisplayTime : timestamp);
    }

    private void setLiveTimeContent(boolean live, long timestamp) {
        if (JFGRules.hasSDFeature(pid) && !JFGRules.isShareDevice(uuid)) {
            liveTimeLayout.setContent(live || timestamp == 0 ? CamLiveContract.TYPE_LIVE : CamLiveContract.TYPE_HISTORY, isLive() ? 0 : timestamp);
        }
    }

    public void setFlipped(boolean flip) {
        (layoutLandFlip).setFlipped(flip);
        (layoutPortFlip).setFlipped(flip);
    }

    private boolean isStandBy() {
        return presenter != null && presenter.isStandBy();
    }

    public void updateLiveViewMode(String mode) {
        if (device.pid == 39 || device.pid == 49) {
            mode = "0";
        }
        if (!canShowFirstSight()) {
            updateCamParam(presenter.getDevice().$(510, new DpMsgDefine.DpCoordinate()));
        } else {
            if (videoView != null) {
                videoView.config360(TextUtils.equals(mode, "0") ? CameraParam.getTopPreset() : CameraParam.getWallPreset());
            }
        }
        if (videoView != null) {
            videoView.setMode(TextUtils.equals("0", mode) ? 0 : 1);
            videoView.detectOrientationChanged();
        }
    }

    private int[] portMicRes = {R.drawable.icon_port_mic_off_selector,
            R.drawable.icon_port_mic_on_selector};
    private int[] portBellMicRes = {R.drawable.door_bell_no_talk_selector, R.drawable.door_bell_talk_selector};
    private int[] landMicRes = {R.drawable.icon_land_mic_off_selector,
            R.drawable.icon_land_mic_on_selector};
    private int[] portSpeakerRes = {R.drawable.icon_port_speaker_off_selector,
            R.drawable.icon_port_speaker_on_selector,
            R.drawable.icon_port_speaker_off_selector, R.drawable.icon_port_speaker_on_selector};
    private int[] landSpeakerRes = {R.drawable.icon_land_speaker_off_selector,
            R.drawable.icon_land_speaker_on_selector,
            R.drawable.icon_land_speaker_off_selector, R.drawable.icon_land_speaker_on_selector};

    @OnClick(R.id.imgV_cam_live_land_nav_back)
    public void onLiveLandBackClick() {
        if (orientationHandle != null) {
            orientationHandle.setRequestOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, true);
        }
    }

    @OnClick(R.id.imgV_cam_zoom_to_full_screen)
    public void onZoomToFullScreenClick() {
        if (orientationHandle != null) {
            orientationHandle.setRequestOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, true);
        }
    }

    @OnClick(R.id.imgV_cam_live_land_play)
    public void onLandLivePlayClick(View v) {//横屏,左下角播放
        performTogglePlayingAction();
    }

    private void performTogglePlayingAction() {
        boolean livePlaying = isLivePlaying();
        if (livePlaying) {
            imgVCamLiveLandPlay.setImageResource(R.drawable.icon_landscape_stop);
            presenter.performStopVideoAction(true);
        } else {
            imgVCamLiveLandPlay.setImageResource(R.drawable.icon_landscape_playing);
            presenter.performPlayVideoAction();
        }
        AppLogger.d(CameraLiveHelper.TAG + ":performTogglePlayingAction:isPlaying:" + livePlaying);
    }

    @OnClick(R.id.tv_live)
    public void onLiveClick() {
        presenter.performPlayVideoAction(true, 0);
        AppLogger.i("TextView click start play!");
    }

    @OnClick({R.id.imgV_cam_switch_speaker, R.id.imgV_land_cam_switch_speaker})
    public void onTriggerSpeakerClick() {
        CameraLiveFragmentExPermissionsDispatcher.audioRecordPermissionGrant_SpeakerWithCheck(this);
    }

    @OnClick({R.id.imgV_cam_trigger_mic, R.id.imgV_land_cam_trigger_mic})
    public void onTriggerMicClick() {
        CameraLiveFragmentExPermissionsDispatcher.audioRecordPermissionGrant_MicWithCheck(this);
    }

    @OnClick({R.id.imgV_cam_trigger_capture, R.id.imgV_land_cam_trigger_capture})
    public void onTriggerCaptureClick() {
        presenter.performLivePictureCaptureSaveAction(true);
    }

    @OnClick(R.id.imgV_land_cam_switch_xunhuan)
    public void onSwitchXunHuanClick() {
        ivModeXunHuan.setSelected(enableAutoRotate = !ivModeXunHuan.isSelected());
        enableAutoRotate(enableAutoRotate);
    }

    private void enableAutoRotate(boolean enableAutoRotate) {
        ivModeXunHuan.setEnabled(enableAutoRotate);
        boolean switchModeButton = JFGRules.showSwitchModeButton(device.pid);
        if (!switchModeButton) {
            this.enableAutoRotate = false;
            return;
        }
        if (isLand()) {
            this.enableAutoRotate = enableAutoRotate;
        }
        if (videoView != null && videoView instanceof Panoramic360ViewRS) {
            try {
                ((Panoramic360ViewRS) videoView).enableAutoRotation(isLivePlaying() && isLive() && enableAutoRotate && isLand());
            } catch (NullPointerException e) {
            }
        }
    }

    @OnClick(R.id.btn_load_history)
    public void onLoadHistoryClick() {
        AppLogger.d("需要手动获取sd卡");
        presenter.performHistoryPlayAndCheckerAction(0);
    }

    @OnClick(R.id.live_time_layout)
    public void onLiveTimeLayoutClick(View v) {
        int net = NetUtils.getJfgNetType();
        if (net == 0) {
            ToastUtil.showNegativeToast(getContext().getString(R.string.NoNetworkTips));
            return;
        }
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        if (!JFGRules.isDeviceOnline(device.$(201, new DpMsgDefine.DPNet()))) {
            ToastUtil.showNegativeToast(getContext().getString(R.string.OFFLINE_ERR));
            return;
        }
        DpMsgDefine.DPSdStatus status = device.$(204, new DpMsgDefine.DPSdStatus());
        if (status.hasSdcard && status.err != 0) {
            ToastUtil.showNegativeToast(getContext().getString(R.string.VIDEO_SD_DESC));
            return;
        }
        if (!status.hasSdcard || status.err != 0) {
            ToastUtil.showToast(getContext().getString(R.string.NO_SDCARD));
            return;
        }
        if (historyWheelHandler == null || presenter.isHistoryEmpty()) {
            ToastUtil.showToast(getResources().getString(R.string.History_video_Firstly));
            return;
        }
        if (historyWheelHandler != null) {
            ViewUtils.deBounceClick(v);
            historyWheelHandler.showDatePicker(MiscUtils.isLand());
        }
    }

    public void onShake() {
        Log.i(CYLAN_TAG, "我需要摇一摇");
        if (isShakeEnable()) {
            if (videoView != null && videoView instanceof PanoramicView360RS_Ext) {
                ((PanoramicView360RS_Ext) videoView).enableAutoRotation(true);
                ((PanoramicView360RS_Ext) videoView).phoneShook();
            }
        }
    }

    public boolean isShakeEnable() {
        return ivModeXunHuan.isSelected() && ivModeXunHuan.isEnabled();
    }

    @Override
    public void onDeviceNetChanged(DpMsgDefine.DPNet net, boolean isLocalOnline) {
        AppLogger.d(CameraLiveHelper.TAG + ":onDeviceNetChanged,net is:" + net + " isLocalOnline:" + isLocalOnline);
        this.isLocalOnline = isLocalOnline;
        performLayoutEnableAction();
    }

    @Override
    public void onUpdateAlarmOpenChanged(boolean alarmOpened) {
        AppLogger.d(CameraLiveHelper.TAG + ":onUpdateAlarmOpenChanged,isAlarmOpened:" + alarmOpened);
        setFlipped(!alarmOpened);
    }

    @Override
    public void onChangeSafeProtectionErrorAutoRecordClosed() {
        AppLogger.d(CameraLiveHelper.TAG + ":onChangeSafeProtectionErrorAutoRecordClosed");
        AlertDialogManager.getInstance().showDialog(getActivity(),
                getString(R.string.Tap1_Camera_MotionDetection_OffTips),
                getString(R.string.Tap1_Camera_MotionDetection_OffTips),
                getString(R.string.CARRY_ON), (DialogInterface dialog, int which) -> {
                    presenter.performChangeSafeProtection(1);
                    //关闭移动侦测的同时也关闭自动录像
                    ToastUtil.showToast(getString(R.string.SCENE_SAVED));
                    if (isLand()) {
                        ViewUtils.setSystemUiVisibility(liveLoadingBar, false);
                    }
                }, getString(R.string.CANCEL), (dialog, which) -> {
                    if (isLand()) {
                        ViewUtils.setSystemUiVisibility(liveLoadingBar, false);
                    }
                });
    }

    @Override
    public void onChangeSafeProtectionErrorNeedConfirm() {
        AppLogger.d(CameraLiveHelper.TAG + ":onChangeSafeProtectionErrorAutoRecordClosed");
        AlertDialogManager.getInstance().showDialog((Activity) getContext(), "safeIsOpen", getString(R.string.Detection_Pop),
                getString(R.string.OK), (dialog, which) -> {
                    presenter.performChangeSafeProtection(1);
                    if (isLand()) {
                        ViewUtils.setSystemUiVisibility(liveLoadingBar, false);
                    }
                }, getString(R.string.CANCEL), (dialog, which) -> {
                    if (isLand()) {
                        ViewUtils.setSystemUiVisibility(liveLoadingBar, false);
                    }
                }, false);
    }

    @Override
    public void onPlayErrorSDFileIO() {
        AppLogger.d(CameraLiveHelper.TAG + ":onPlayErrorSDFileIO");
        liveLoadingBar.changeToLoadingError(true, getString(R.string.Historical_Failed), null);
        AlertDialogManager.getInstance().showDialog(getActivity(), getString(R.string.Historical_Failed),
                getString(R.string.Historical_Failed), getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    presenter.performPlayVideoAction(true, 0);
                });
    }

    @Override
    public void onPlayErrorSDHistoryAll() {
        AppLogger.d(CameraLiveHelper.TAG + ":onPlayErrorSDHistoryAll");
        liveLoadingBar.changeToLoadingError(true, getString(R.string.Historical_Read), null);
        AlertDialogManager.getInstance().showDialog(getActivity(), getString(R.string.Historical_Read),
                getString(R.string.Historical_Read), getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    presenter.performPlayVideoAction(true, 0);
                });
    }

    @Override
    public void onPlayErrorSDIO() {
        AppLogger.d(CameraLiveHelper.TAG + ":onPlayErrorSDIO");
        liveLoadingBar.changeToLoadingError(true, getString(R.string.Historical_No), null);
        AlertDialogManager.getInstance().showDialog(getActivity(), getString(R.string.Historical_No),
                getString(R.string.Historical_No), getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    presenter.performPlayVideoAction(true, 0);
                });
    }

    @Override
    public void onPlayErrorVideoPeerDisconnect() {
        AppLogger.d(CameraLiveHelper.TAG + ":onPlayErrorVideoPeerDisconnect");
        if (isLivePlaying()) {
            liveLoadingBar.changeToLoadingError(true, ContextUtils.getContext().getString(R.string.Device_Disconnected), null);
        }
    }

    @Override
    public void onPlayErrorVideoPeerNotExist() {
        AppLogger.d(CameraLiveHelper.TAG + ":onPlayErrorVideoPeerNotExist");
        liveLoadingBar.changeToLoadingError(true, getString(R.string.OFFLINE_ERR), getContext().getString(R.string.USER_HELP));
    }

    @Override
    public void onViewModeAvailable(int displayMode) {
        AppLogger.d(CameraLiveHelper.TAG + ":onViewModeAvailable:" + displayMode);
        performChangeViewDisplayMode(displayMode, true);
    }

    @Override
    public void onViewModeHangError() {
        AppLogger.d(CameraLiveHelper.TAG + ":onViewModeHangError");
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.SWITCH_VIEW_POP)
                .setNegativeButton(R.string.CANCEL, null)
                .setPositiveButton(R.string.OK, (dialog, which) -> {
                    presenter.updateInfoReq(new DpMsgDefine.DPPrimary<>("0"), DpMsgMap.ID_509_CAMERA_MOUNT_MODE);
                    onViewModeAvailable(getDisplayMode());
                }).show();
    }

    @Override
    public void onViewModeNotSupportError() {
        AppLogger.d(CameraLiveHelper.TAG + ":onViewModeNotSupportError");
    }

    @Override
    public void onViewModeForceHangError(int displayMode) {
        Log.d(CameraLiveHelper.TAG, "onViewModeForceHangError");
        presenter.updateInfoReq(new DpMsgDefine.DPPrimary<>("0"), DpMsgMap.ID_509_CAMERA_MOUNT_MODE);
        performChangeViewDisplayMode(displayMode, true);
        AppLogger.d("当前视图不支持视角切换,但又支持视图切换,强制开始平视视图");
    }

    @Override
    public void onVideoPlayPrepared(boolean live) {
        AppLogger.d(CameraLiveHelper.TAG + ":onVideoPlayPrepared,live:" + live);
        performLayoutEnableAction();
        liveLoadingBar.changeToLoading(true);
    }

    @Override
    public void onPlayErrorNoError() {
        AppLogger.d(CameraLiveHelper.TAG + ":onPlayErrorNoError");
        performReLayoutAction();
        liveLoadingBar.changeToPlaying(canShowLoadingBar());
    }

    @Override
    public void onPlayErrorWaitForFetchHistoryCompleted() {
        AppLogger.d(CameraLiveHelper.TAG + ":onPlayErrorWaitForFetchHistoryCompleted");
        performReLayoutAction();
        liveLoadingBar.changeToLoading(true, ContextUtils.getContext().getString(R.string.LOADING), null);
    }

    @Override
    public void onSafeProtectionChanged(boolean safeProtectionOpened) {
        AppLogger.d(CameraLiveHelper.TAG + ":onSafeProtectionChanged:" + safeProtectionOpened);
        setFlipped(!safeProtectionOpened);
    }

    @Override
    public void onStreamModeChanged(int mode) {
        AppLogger.d(CameraLiveHelper.TAG + ":onStreamModeChanged");
        svSwitchStream.setMode(mode);
    }

    @Override
    public void onHistoryCheckerErrorNoSDCard() {
        AppLogger.d(CameraLiveHelper.TAG + ":onHistoryCheckerErrorNoSDCard");
        ToastUtil.showToast(getString(R.string.NO_SDCARD));
        performLayoutEnableAction();
        if (!isLivePlaying()) {
            liveLoadingBar.changeToPlaying(canShowLoadingBar());
        } else if (isNoPlayError()) {
            liveLoadingBar.changeToPause(!canHideLoadingBar());
        }
    }

    @Override
    public void onHistoryCheckerErrorSDCardInitRequired(int errorCode) {
        AppLogger.d(CameraLiveHelper.TAG + ":onHistoryCheckerErrorSDCardInitRequired,errorCode is:" + errorCode);
        ToastUtil.showToast(getString(R.string.VIDEO_SD_DESC));
        performLayoutEnableAction();
        if (!isLivePlaying()) {
            liveLoadingBar.changeToPlaying(canShowLoadingBar());
        } else if (isNoPlayError()) {
            liveLoadingBar.changeToPause(!canHideLoadingBar());
        }
    }

    @Override
    public void onLoadHistoryPrepared(long playTime, boolean isHistoryCheckerRequired) {
        AppLogger.d(CameraLiveHelper.TAG + ":onLoadHistoryPrepared");
        performLayoutEnableAction();
        liveLoadingBar.changeToLoading(true, isHistoryCheckerRequired ? getString(R.string.LOADING) : null, null);
    }

    @Override
    public void onVideoPlayTypeChanged(boolean isLive) {
        AppLogger.d(CameraLiveHelper.TAG + ":onVideoPlayTypeChanged isLive:" + isLive);
        performReLayoutAction();
        liveLoadingBar.showOrHide(!canHideLoadingBar());
    }

    @Override
    public void switcher(View view, int mode) {
        if (view.getId() != R.id.sv_switch_stream) {
            presenter.performChangeStreamModeAction(mode);
        }
        performLayoutAnimation(true);
    }

    @Override
    public boolean onSingleTap(float x, float y) {
        performLayoutAnimation(!isLayoutAnimationShowing);
        return false;
    }

    @Override
    public void onSnapshot(Bitmap bitmap, boolean tag) {
        AppLogger.d(TAG + "onSnapshot: " + (bitmap == null));
    }

    @Override
    public void clickImage(View view, int state) {
        performTogglePlayingAction();
    }

    @Override
    public void clickHelp(View view) {
        if (NetUtils.getJfgNetType() == 0) {
            ToastUtil.showNegativeToast(ContextUtils.getContext().getString(R.string.OFFLINE_ERR_1));
            return;
        }
        Intent intent = new Intent(getContext(), HomeMineHelpActivity.class);
        intent.putExtra(JConstant.KEY_SHOW_SUGGESTION, JConstant.KEY_SHOW_SUGGESTION);
        startActivity(intent, ActivityOptionsCompat.makeCustomAnimation(getContext(), R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
    }

    @Override
    public void clickText(View view) {
    }

    @Override
    public void onClick(FlipImageView view) {
        presenter.performChangeSafeProtection(0);
    }

    @Override
    public void onFlipStart(FlipImageView view) {
    }

    @Override
    public void onFlipEnd(FlipImageView view) {
    }

    interface OrientationHandle {
        void setRequestOrientation(int orientation, boolean fromUser);
    }

    private OrientationHandle orientationHandle;

    public void setOrientationHandle(OrientationHandle handle) {
        this.orientationHandle = handle;
    }

    @OnClick(R.id.imgV_cam_door_look)
    public void onDoorLockClick() {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            DoorLockDialog doorLockDialog = DoorLockDialog.Companion.newInstance(uuid);
            doorLockDialog.setAction((id, value) -> {
                if (id == R.id.ok) {
                    presenter.openDoorLock((String) value);
                }
            });
            doorLockDialog.show(fragmentManager, "CameraLiveFragmentEx.onDoorLockClick");
        }
    }

    private static final int ANIMATION_DURATION = 250;
    private static final int WAIT_TO_HIDE_DELAY_TIME = 3000;
    private volatile boolean isLayoutAnimationShowing = false;

    private void performLayoutAnimation(boolean showLayout) {
        performLayoutAnimation(showLayout, true);
    }

    private void performLayoutAnimation(boolean showLayout, boolean autoHide) {
        liveLoadingBar.removeCallbacks(showLayoutAnimationRunnable);
        liveLoadingBar.removeCallbacks(hideLayoutAnimationRunnable);
        liveLoadingBar.post(showLayout ? showLayoutAnimationRunnable : hideLayoutAnimationRunnable);
        if (showLayout && autoHide) {
            liveLoadingBar.postDelayed(hideLayoutAnimationRunnable, WAIT_TO_HIDE_DELAY_TIME);
        }
    }

    //显示 View 的 runnable
    private Runnable showLayoutAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            if (isLand()) {
                performLandLayoutAnimation(isLayoutAnimationShowing = true);
            } else {
                performPortLayoutAnimation(isLayoutAnimationShowing = true);
            }
        }
    };
    //隐藏 view 的 runnable
    private Runnable hideLayoutAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            if (isLand()) {
                performLandLayoutAnimation(isLayoutAnimationShowing = false);
            } else {
                performPortLayoutAnimation(isLayoutAnimationShowing = false);
            }
        }
    };

    private boolean canShowLoadingBar() {
        return presenter != null && presenter.canShowLoadingBar();
    }

    private boolean canHideLoadingBar() {
        return presenter != null && presenter.canHideLoadingBar();
    }

    private boolean canShowViewModeMenu() {
        return presenter != null && presenter.canShowViewModeMenu();
    }

    private boolean canShowStreamSwitcher() {
        return presenter != null && presenter.canShowStreamSwitcher();
    }

    private boolean canShowHistoryWheel() {
        return presenter != null && presenter.canShowHistoryWheel();
    }

    private boolean canShowFlip() {
        return presenter != null && presenter.canShowFlip();
    }

    private boolean canShowFirstSight() {
        return presenter != null && presenter.canShowFirstSight();
    }

    private boolean canShowHistoryCase() {
        return presenter != null && presenter.canShowHistoryCase();
    }

    private boolean canXunHuanEnable() {
        return presenter != null && presenter.canXunHuanEnable();
    }

    private boolean canModeSwitchEnable() {
        return presenter != null && presenter.canModeSwitchEnable();
    }

    private boolean canLoadHistoryEnable() {
        return presenter != null && presenter.canLoadHistoryEnable();
    }

    private boolean canPlayVideoNow() {
        return presenter != null && presenter.canPlayVideoNow();
    }

    private boolean isLivePlaying() {
        return presenter != null && presenter.isLivePlaying();
    }

    private boolean isLive() {
        return presenter != null && presenter.isLive();
    }

    private void performLandLayoutAnimation(boolean showLayout) {
        AppLogger.d(TAG + "performLandLayoutAnimation,showLayout:" + showLayout);
        if (showLayout) {
            liveLoadingBar.animate().setDuration(ANIMATION_DURATION).alpha(1).withStartAction(() -> {
                liveLoadingBar.showOrHide(canShowLoadingBar());//全屏直播门铃 1.需要去掉中间播放按钮
            }).start();
            svSwitchStream.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {
                svSwitchStream.setVisibility(canShowStreamSwitcher() ? VISIBLE : INVISIBLE);
            }).start();

            liveTopBannerView.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {
                liveTopBannerView.setVisibility(VISIBLE);
                ivModeXunHuan.setVisibility(JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
                ivModeXunHuan.setEnabled(canXunHuanEnable());
            }).start();

            liveBottomBannerView.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {
                liveBottomBannerView.setVisibility(isLivePlaying() ? VISIBLE : INVISIBLE);
            }).withEndAction(() -> {
                liveBottomBannerView.setVisibility(isLivePlaying() ? VISIBLE : INVISIBLE);
            }).start();

            historyParentContainer.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {
                historyParentContainer.setVisibility(VISIBLE);
                historyWheelContainer.setVisibility(canShowHistoryWheel() ? VISIBLE : INVISIBLE);
            }).start();

            liveViewModeContainer.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {
                ivViewModeSwitch.setEnabled(canModeSwitchEnable());
                liveViewModeContainer.setVisibility(canShowViewModeMenu() ? VISIBLE : INVISIBLE);
            }).start();

            liveViewWithThumbnail.getTvLiveFlow().animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {
                liveViewWithThumbnail.getTvLiveFlow().setVisibility(isLivePlaying() ? VISIBLE : INVISIBLE);

            }).start();
        } else {
            liveLoadingBar.animate().setDuration(ANIMATION_DURATION).alpha(canHideLoadingBar() ? 0 : 1)
                    .translationY(canHideLoadingBar() ? 1 : 0)
                    .withEndAction(() -> {
                        liveLoadingBar.showOrHide(!canHideLoadingBar());
                    }).start();

            svSwitchStream.animate().setDuration(ANIMATION_DURATION).alpha(canHideStreamSwitcher() ? 0 : 1).translationY(svSwitchStream.getHeight() / 4)
                    .withStartAction(() -> {
                        svSwitchStream.performSlideAnimation(false);
                    })
                    .withEndAction(() -> {
                        svSwitchStream.setVisibility(canHideStreamSwitcher() ? INVISIBLE : VISIBLE);
                    }).start();

            liveTopBannerView.animate().setDuration(ANIMATION_DURATION).alpha(0).translationY(-liveTopBannerView.getHeight() / 4).withEndAction(() -> {
                ivModeXunHuan.setEnabled(isLive() && isLivePlaying() && JFGRules.showSwitchModeButton(device.pid) && enableAutoRotate);
                liveTopBannerView.setVisibility(INVISIBLE);
            }).start();

            liveBottomBannerView.animate().setDuration(ANIMATION_DURATION).translationY(historyParentContainer.getHeight()).withStartAction(() -> {
                liveBottomBannerView.setVisibility(isLivePlaying() ? VISIBLE : INVISIBLE);
            }).withEndAction(() -> {
                liveBottomBannerView.setVisibility(isLivePlaying() ? VISIBLE : INVISIBLE);
            }).start();

            historyParentContainer.animate().setDuration(ANIMATION_DURATION).alpha(0).translationY(historyParentContainer.getHeight() / 4).withEndAction(() -> {
                historyParentContainer.setVisibility(INVISIBLE);
            }).start();

            liveViewModeContainer.animate().setDuration(ANIMATION_DURATION).alpha(canHideViewMode() ? 0 : 1).translationY(liveViewModeContainer.getHeight() / 4).withEndAction(() -> {
                ivViewModeSwitch.setEnabled(isLive() && isLivePlaying() && JFGRules.showSwitchModeButton(device.pid));
                liveViewModeContainer.setVisibility(canHideViewMode() ? INVISIBLE : VISIBLE);
                rbViewModeSwitchParent.setVisibility(INVISIBLE);
            }).start();
            liveViewWithThumbnail.getTvLiveFlow().animate().setDuration(ANIMATION_DURATION).translationY(-liveTopBannerView.getHeight()).withEndAction(() -> {

            }).start();
        }
    }

    private boolean canHideViewMode() {
        return presenter != null && presenter.canHideViewModeMenu();
    }

    private boolean canHideStreamSwitcher() {
        return presenter != null && presenter.canHideStreamSwitcher();
    }

    private void performPortLayoutAnimation(boolean showLayout) {
        AppLogger.d(TAG + "performPortLayoutAnimation,showLayout:" + showLayout);
        if (showLayout) {
            liveLoadingBar.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {
                liveLoadingBar.showOrHide(canShowLoadingBar());
            }).start();

            liveViewModeContainer.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {
                liveViewModeContainer.setVisibility(canShowViewModeMenu() ? VISIBLE : INVISIBLE);
                ivViewModeSwitch.setEnabled(canModeSwitchEnable());
            }).start();


            liveBottomBannerView.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {
                liveBottomBannerView.setVisibility(isLivePlaying() ? VISIBLE : INVISIBLE);
            }).withEndAction(() -> {
                liveBottomBannerView.setVisibility(isLivePlaying() ? VISIBLE : INVISIBLE);
            }).start();

            svSwitchStream.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {
                svSwitchStream.setVisibility(canShowStreamSwitcher() ? VISIBLE : INVISIBLE);
            }).start();

            historyParentContainer.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {
                historyParentContainer.setVisibility(JFGRules.isShareDevice(uuid) ? INVISIBLE : VISIBLE);
            }).start();

        } else {
            liveLoadingBar.animate().setDuration(ANIMATION_DURATION).alpha(isLivePlaying() ? 0 : 1).translationY(0).withEndAction(() -> {
                liveLoadingBar.showOrHide(!canHideLoadingBar());
            }).start();
            svSwitchStream.animate().setDuration(ANIMATION_DURATION).alpha(canHideStreamSwitcher() ? 0 : 1).translationY(0).withStartAction(() -> {
                svSwitchStream.performSlideAnimation(false);
            }).withEndAction(() -> {
                svSwitchStream.setVisibility(canHideStreamSwitcher() ? INVISIBLE : VISIBLE);
            }).start();

            liveViewModeContainer.animate().setDuration(ANIMATION_DURATION).alpha(canHideViewMode() ? 0 : 1).translationY(0).withEndAction(() -> {
                ivViewModeSwitch.setEnabled(canModeSwitchEnable());
                liveViewModeContainer.setVisibility(canHideViewMode() ? INVISIBLE : VISIBLE);
                rbViewModeSwitchParent.setVisibility(INVISIBLE);
            }).start();

            liveBottomBannerView.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0)
                    .withStartAction(() -> {
                        liveBottomBannerView.setVisibility(isLivePlaying() ? VISIBLE : INVISIBLE);
                    })
                    .withEndAction(() -> {
                        liveBottomBannerView.setVisibility(isLivePlaying() ? VISIBLE : INVISIBLE);
                    })
                    .start();
            historyParentContainer.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {

            }).start();
        }
    }

    private void performLayoutVisibilityAction() {
        boolean isLand = isLand();
        boolean showFlip = canShowFlip();
        boolean showHistoryWheel = canShowHistoryWheel();
        boolean canShowMicrophone = canShowMicrophone();
        boolean canShowDoorLook = canShowDoorLook();
        boolean canShowXunHuan = canShowXunHuan();
        liveTopBannerView.setVisibility(isLand ? VISIBLE : INVISIBLE);
        bottomControllerContainer.setVisibility(isLand ? INVISIBLE : VISIBLE);
        layoutLandFlip.setVisibility(showFlip && isLand ? VISIBLE : GONE);
        layoutPortFlip.setVisibility(showFlip && !isLand && isLivePlaying() ? VISIBLE : GONE);
        imgVCamZoomToFullScreen.setVisibility(isLand ? INVISIBLE : VISIBLE);
        imgVCamLiveLandPlay.setVisibility(isLand ? VISIBLE : GONE);
        liveBottomBannerView.setVisibility(isLivePlaying() ? VISIBLE : INVISIBLE);
        historyWheelContainer.setVisibility(showHistoryWheel ? VISIBLE : INVISIBLE);
        tvLive.setVisibility(historyWheelContainer.getDisplayedChild() == 1 || !isHistoryEmpty() ? VISIBLE : GONE);
        vFlag.setVisibility(historyWheelContainer.getDisplayedChild() == 1 || !isHistoryEmpty() ? VISIBLE : GONE);
        ivModeXunHuan.setVisibility(canShowXunHuan ? VISIBLE : INVISIBLE);
        imgVCamTriggerMic.setVisibility(canShowMicrophone ? VISIBLE : GONE);
        imgVLandCamTriggerMic.setVisibility(canShowMicrophone ? VISIBLE : GONE);
        ivCamDoorLock.setVisibility(canShowDoorLook ? VISIBLE : GONE);
        ivViewModeSwitch.setVisibility(JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
        vDivider.setVisibility(showFlip && MiscUtils.isLand() ? VISIBLE : GONE);
        liveViewWithThumbnail.getTvLiveFlow().setVisibility(isLivePlaying() ? VISIBLE : INVISIBLE);
        liveLoadingBar.showOrHide(!canHideLoadingBar());
        historyParentContainer.setVisibility(canShowHistoryWheel() ? VISIBLE : INVISIBLE);

        //菜单View默认隐藏
        svSwitchStream.setVisibility(canShowStreamSwitcher() ? VISIBLE : INVISIBLE);
        liveViewModeContainer.setVisibility(canShowViewModeMenu() ? VISIBLE : INVISIBLE);
    }

    private boolean canShowXunHuan() {
        return presenter != null && presenter.canShowXunHuan();
    }

    private boolean canShowDoorLook() {
        return presenter != null && presenter.canShowDoorLock();
    }

    private boolean canShowMicrophone() {
        return presenter != null && presenter.canShowMicrophone();
    }

    private void performLayoutEnableAction() {
        boolean isHistory = !isLive();
        boolean isPlaying = isLivePlaying();
        boolean canCaptureEnable = canCaptureEnable();
        boolean canMicrophoneEnable = canMicrophoneEnable();
        boolean canSpeakerEnable = canSpeakerEnable();
        tvLive.setEnabled(isHistory);
        ivModeXunHuan.setEnabled(canXunHuanEnable());
        ivViewModeSwitch.setEnabled(canModeSwitchEnable());
        imgVCamTriggerCapture.setEnabled(canCaptureEnable);
        imgVLandCamTriggerCapture.setEnabled(canCaptureEnable);
        imgVCamTriggerMic.setEnabled(canMicrophoneEnable);
        imgVLandCamTriggerMic.setEnabled(canMicrophoneEnable);
        imgVCamSwitchSpeaker.setEnabled(canSpeakerEnable);
        imgVLandCamSwitchSpeaker.setEnabled(canCaptureEnable);
        ivCamDoorLock.setEnabled(canDoorLockEnable());
        imgVCamZoomToFullScreen.setEnabled(isPlaying);
        liveViewWithThumbnail.setEnabled(true);
        btnLoadHistory.setEnabled(canLoadHistoryEnable());
        svSwitchStream.setEnabled(canStreamSwitcherEnable());
    }

    private boolean canSpeakerEnable() {
        return presenter != null && presenter.canSpeakerEnable();
    }

    private boolean canMicrophoneEnable() {
        return presenter != null && presenter.canMicrophoneEnable();
    }

    private boolean canCaptureEnable() {
        return presenter != null && presenter.canCaptureEnable();
    }

    private boolean canStreamSwitcherEnable() {
        return presenter != null && presenter.canStreamSwitcherEnable();
    }

    private boolean canDoorLockEnable() {
        return presenter != null && presenter.canDoorLockEnable();
    }

    private void performLayoutContentAction() {
        if (videoView == null) {
            videoView = VideoViewFactory.CreateRendererExt(device.pid, getContext());
            liveViewWithThumbnail.setLiveView(videoView, uuid);
        }
        boolean isLand = isLand();
        imgVCamTriggerMic.setImageResource(hasDoorLock ? portBellMicRes[0] : portMicRes[0]);
        tvLive.setBackgroundColor(isLand ? Color.TRANSPARENT : Color.WHITE);
        //历史录像显示
        ViewUtils.setBottomMargin(svSwitchStream, isLand ? (int) getResources().getDimension(R.dimen.y56) : (int) getResources().getDimension(R.dimen.y46));
        //显示 昵称
        String alias = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
        imgVCamLiveLandNavBack.setText(alias);
        historyParentContainer.setBackgroundResource(isLand ? R.color.color_4C000000 : android.R.color.transparent);
        liveBottomBannerView.setBackgroundResource(isLand ? android.R.color.transparent : R.drawable.camera_sahdow);
        historyWheelContainer.setDisplayedChild(isHistoryEmpty() ? 0 : 1);
        flLoadHistory.setBackgroundResource(isLand ? android.R.color.transparent : R.color.color_F7F8FA);
        vLine.setBackgroundResource(isLand ? android.R.color.transparent : R.color.color_f2f2f2);
        (imgVCamLiveLandPlay).setImageResource(isLivePlaying() ? R.drawable.icon_landscape_playing : R.drawable.icon_landscape_stop);
        liveViewWithThumbnail.showFlowView(isLivePlaying(), "0K/s");

        //是否显示清晰度切换
        int mode = device.$(513, 0);
        svSwitchStream.setMode(mode);

        if (isLand) {
            //隐藏所有的 showcase
            LiveShowCase.hideHistoryWheelCase((Activity) getContext());
            LiveShowCase.hideHistoryCase((Activity) getContext());
        }

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) historyParentContainer.getLayoutParams();
        RelativeLayout.LayoutParams glp = (RelativeLayout.LayoutParams) liveViewModeContainer.getLayoutParams();
        float liveViewRadio = presenter == null ? 1.0F : presenter.getVideoPortHeightRatio(isLand);
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        if (isLand) {
            lp.removeRule(RelativeLayout.BELOW);//remove below rules
            lp.addRule(RelativeLayout.ABOVE, R.id.v_guide);//set above v_guide
//            android:layout_above="@+id/layout_d"
            glp.addRule(RelativeLayout.ABOVE, R.id.layout_e);
            liveViewWithThumbnail.updateLayoutParameters(RelativeLayout.LayoutParams.MATCH_PARENT, (int) (displayMetrics.heightPixels / liveViewRadio));
        } else {
            glp.addRule(RelativeLayout.ABOVE, R.id.layout_d);
            lp.removeRule(RelativeLayout.ABOVE);//remove above
            lp.addRule(RelativeLayout.BELOW, R.id.v_guide); //set below v_guide
            liveViewWithThumbnail.updateLayoutParameters((int) (displayMetrics.widthPixels * liveViewRadio), RelativeLayout.LayoutParams.MATCH_PARENT);
        }
        historyParentContainer.setLayoutParams(lp);
        liveViewModeContainer.setLayoutParams(glp);
        liveViewWithThumbnail.detectOrientationChanged(!isLand);
        camLiveControlLayer.setKeepScreenOn(isLivePlaying());
        decideFlippedContent();
        decideLiveThumbContent();
        decideLiveViewMode();
        decideFirstSightSetting();
        decideDeviceTimezone();
        decideHistoryShowCase();
        decidePendingResumeAction();
        decideStreamModeSetting();
    }

    private void decideStreamModeSetting() {
        onStreamModeChanged(getStreamMode());
    }

    private int getStreamMode() {
        return presenter == null ? 0 : presenter.getStreamMode();
    }

    private boolean isHistoryEmpty() {
        return presenter != null && presenter.isHistoryEmpty();
    }

    private void decidePendingResumeAction() {
        Bundle bundle = getArguments();
        if (bundle != null && presenter != null) {
            long playTime = bundle.getLong(JConstant.KEY_CAM_LIVE_PAGE_PLAY_HISTORY_TIME, -1);
            bundle.remove(JConstant.KEY_CAM_LIVE_PAGE_PLAY_HISTORY_TIME);
            if (playTime > 0 && canPlayVideoNow()) {
                AppLogger.d("需要定位到时间轴:" + playTime);
                presenter.performHistoryPlayAndCheckerAction(playTime);
            }
        }
    }

    private void decideHistoryShowCase() {
        boolean canShowHistoryCase = canShowHistoryCase();
        Fragment historyShowCaseFragment = getFragmentManager().findFragmentByTag(HistoryWheelShowCaseFragment.class.getSimpleName());
        if (isReallyVisibleToUser()) {
            if (!MiscUtils.isLand() && canShowHistoryCase) {
                LiveShowCase.showHistoryWheelCase(getActivity(), historyParentContainer);
                LiveShowCase.showHistoryCase((Activity) getContext(), tvLive);
            }
        } else {
            if (historyShowCaseFragment != null) {
                getFragmentManager().beginTransaction().remove(historyShowCaseFragment).commitAllowingStateLoss();
            }
        }
    }

    private void decideDeviceTimezone() {
        TimeZone timezone = JFGRules.getDeviceTimezone(device);
        onDeviceTimeZoneChanged(timezone.getRawOffset());
    }

    private void decideFirstSightSetting() {
        boolean canShowFirstSight = canShowFirstSight();
        View sightLayer = liveViewWithThumbnail.findViewById(R.id.fLayout_cam_sight_setting);
        if (!canShowFirstSight) {
            liveViewWithThumbnail.removeView(sightLayer);
            return;
        }
        liveLoadingBar.changeToPlaying(false);
        if (sightLayer != null) {
            sightLayer.setVisibility(VISIBLE);
            return;
        }
        LayoutInflater.from(getContext()).inflate(R.layout.cam_sight_setting_overlay, liveViewWithThumbnail);
        ((TextView) (liveViewWithThumbnail.findViewById(R.id.tv_sight_setting_content))).setText(getString(R.string.Tap1_Camera_Overlook) + ": " + getString(R.string.Tap1_Camera_OverlookTips));
        liveViewWithThumbnail.findViewById(R.id.btn_sight_setting_cancel).setOnClickListener(this::onSightSettingCancelClicked);
        liveViewWithThumbnail.findViewById(R.id.btn_sight_setting_next).setOnClickListener(this::onSightSettingNextClicked);
    }

    private void onSightSettingNextClicked(View view) {
        presenter.performResetFirstSight();
        Intent intent = new Intent(getContext(), SightSettingActivity.class);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        startActivity(intent);
    }

    private void onSightSettingCancelClicked(View view) {
        presenter.performResetFirstSight();
        performReLayoutAction();
    }

    private void decideLiveViewMode() {
        updateLiveViewMode(device.$(509, "1"));
        performChangeViewDisplayMode(getDisplayMode(), false);
    }

    private void decideFlippedContent() {
        performRefreshSafeProtectionSetting();
    }

    private void performRefreshSafeProtectionSetting() {
        onUpdateAlarmOpenChanged(isSafeProtectionOpen());
    }

    private boolean isSafeProtectionOpen() {
        return presenter != null && presenter.isSafeProtectionOpened();
    }

    private void decideLiveThumbContent() {
        boolean no_net = NetUtils.getJfgNetType() == 0;
        boolean standBy = isStandBy();
        liveViewWithThumbnail.enableStandbyMode(standBy, this::onStandByClicked, JFGRules.isShareDevice(uuid));
        if (standBy) {
            liveLoadingBar.changeToPlaying(false);
            return;
        }
        if (no_net) {
            liveViewWithThumbnail.showBlackBackground();
            return;
        }
        if (!isLivePlaying() && isReallyVisibleToUser()) {//显示缩略图
            presenter.performLoadLiveThumbPicture();
            return;
        }
        if (isLivePlaying() && isReallyVisibleToUser()) {
            liveViewWithThumbnail.hideThumbPicture();
        }
    }

    private void onStandByClicked(View view) {
        Intent intent = new Intent(getContext(), CamSettingActivity.class);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid());
        startActivityForResult(intent, REQUEST_CODE, ActivityOptionsCompat.makeCustomAnimation(getActivity(), R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
        AppLogger.d("跳转到使用帮助");
    }

    private void performReLayoutAction() {
        performLayoutVisibilityAction();
        performLayoutEnableAction();
        performLayoutContentAction();
    }
}