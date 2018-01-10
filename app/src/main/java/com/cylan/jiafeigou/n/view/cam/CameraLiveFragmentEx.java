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
import android.widget.RadioButton;
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
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.module.CameraLiveHelper;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamLivePresenterImpl;
import com.cylan.jiafeigou.n.view.activity.CamSettingActivity;
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
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.cylan.jiafeigou.misc.JConstant.CYLAN_TAG;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_IDLE;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_NET_CHANGED;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_STOP;
import static com.cylan.jiafeigou.misc.JFGRules.PlayErr.STOP_MAUNALLY;
import static com.cylan.jiafeigou.support.photoselect.helpers.Constants.REQUEST_CODE;
import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_FINISH;

/**
 * A simple {@link Fragment} subclass.
 */
@RuntimePermissions()
public class CameraLiveFragmentEx extends IBaseFragment<CamLiveContract.Presenter> implements CamLiveContract.View, CameraMessageSender.MessageObserver,
        Switcher.SwitcherListener, VideoViewFactory.InterActListener, ILiveControl.Action, FlipImageView.OnFlipListener, HistoryWheelHandler.DatePickerListener {
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
    private static final String TAG = "CameraLiveFragmentEx";
    private RoundCardPopup roundCardPopup;
    private HistoryWheelHandler historyWheelHandler;
    private CamLiveContract.Presenter presenter;
    private int pid;
    private boolean isRSCam;
    private boolean isShareAccount = false;
    /**
     * 设备的时区
     */
    private SimpleDateFormat liveTimeDateFormat;
    private Device device;
    private boolean hasMicFeature;
    private boolean hasDoorLock;
    private boolean isLocalOnline = false;
    private VideoViewFactory.IVideoView videoView;
    private CameraLiveViewModel cameraLiveViewModel = new CameraLiveViewModel();
    private CameraMenuViewModel cameraMenuViewModel = new CameraMenuViewModel();
    private CameraMessageSender cameraMessageSender = new CameraMessageSender();
    private boolean isNormalView;
    private MyEventListener eventListener;

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
        liveLoadingBar.postDelayed(backgroundCheckerRunnable, 700);
        presenter.performStopVideoAction(true);
    }

    private boolean isReallyVisibleToUser() {
        return getUserVisibleHint() && isResumed() && getActivity() != null && presenter != null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isReallyVisibleToUser()) {
            performReLayoutAction();
        } else if (presenter != null) {
            presenter.performStopVideoAction(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        liveLoadingBar.removeCallbacks(backgroundCheckerRunnable);
        if (isReallyVisibleToUser()) {
            performReLayoutAction();
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
        return willExit && onBackPressed();
    }

    private void enableSensor(boolean enable) {
        boolean autoRotateOn = (Settings.System.getInt(getContext().getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
        //检查系统是否开启自动旋转
        if (autoRotateOn && enable) {
            AppLogger.d("耗电大户");
            eventListener.enable();
        } else if (eventListener != null) {
            eventListener.disable();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        performReLayoutAction();
        performLayoutAnimation(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        CameraLiveFragmentExPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onVideoPlayStopped(boolean live) {
        Log.d(CameraLiveHelper.TAG, "onLiveStop: " + device.getSn());
        enableSensor(false);
        liveLoadingBar.changeToPlaying(canShowLoadingBar());
        liveLoadingBar.setKeepScreenOn(false);
        performReLayoutAction();
        performLayoutAnimation(false);
    }

    @Override
    public void onPlayErrorWaitForPlayCompletedTimeout() {
        Log.d(CameraLiveHelper.TAG, "onPlayErrorWaitForPlayCompletedTimeout");
        liveLoadingBar.changeToLoadingError(true, getContext().getString(R.string.CONNECTING), null);
    }

    @Override
    public void onUpdateVideoLoading(boolean showLoading) {
        Log.d(CameraLiveHelper.TAG, "onUpdateVideoLoading:" + showLoading);
        if (showLoading) {
            liveLoadingBar.changeToLoading(canShowLoadingBar());
        } else {
            liveLoadingBar.hideLoading();
        }
        imgVCamZoomToFullScreen.setEnabled(!showLoading);
    }

    @Override
    public void onPlayErrorUnKnowPlayError(int errorCode) {
        Log.d(CameraLiveHelper.TAG, "onPlayErrorUnKnowPlayError:" + errorCode);
        handlePlayErr(errorCode);
    }

    @Override
    public void onPlayErrorInConnecting() {
        Log.d(CameraLiveHelper.TAG, "onPlayErrorInConnecting");
        liveLoadingBar.changeToLoadingError(true, ContextUtils.getContext().getString(R.string.Device_Disconnected), null);
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
        if (!isLivePlaying()) {
            liveLoadingBar.changeToPlaying(true);
            performLayoutContentAction();
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

    @Override
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
    public void onHistoryLoadFinished() {

    }

    @Override
    public void onDeviceUnBind() {
        AppLogger.d("当前设备已解绑");
        presenter.performStopVideoAction(true);
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
    public void onHistoryEmpty() {
        Log.d(CameraLiveHelper.TAG, "没有历史录像视频...");
        presenter.performPlayVideoAction(true, 0);
        btnLoadHistory.setEnabled(true);
        ToastUtil.showToast(getResources().getString(R.string.NO_CONTENTS_2));
    }

    @Override
    public void onHistoryReady(Collection<JFGVideo> history) {
        Log.d(CYLAN_TAG, "历史录像视频数据已就绪");
        superWheelExt.setHistoryFiles(history);
        performReLayoutAction();
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
        liveLoadingBar.changeToPlaying(canShowLoadingBar() && !isStandBy);
    }

    @Override
    public void onPlayErrorFirstSight() {
        Log.d(CameraLiveHelper.TAG, "第一次使用全景模式");
        performReLayoutAction();
        liveLoadingBar.changeToPlaying(false);
    }

    @Override
    public void onPlayErrorNoNetwork() {
        Log.d(CameraLiveHelper.TAG, "无网络连接");
        performReLayoutAction();
        liveLoadingBar.changeToLoadingError(true, ContextUtils.getContext().getString(R.string.OFFLINE_ERR_1), ContextUtils.getContext().getString(R.string.USER_HELP));
    }

    @Override
    public void onPlayErrorDeviceOffLine() {
        if (BuildConfig.DEBUG) {
            Log.d(CameraLiveHelper.TAG, "设备离线了");
        }
        performReLayoutAction();
        liveLoadingBar.changeToLoadingError(true, ContextUtils.getContext().getString(R.string.OFFLINE_ERR), getContext().getString(R.string.USER_HELP));
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
        liveLoadingBar.setKeepScreenOn(true);//需要保持屏幕常亮
        //|直播| 按钮
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
    }

    @Override
    public void onPlayFrameResumeGood() {
        Log.d(CameraLiveHelper.TAG, "当前帧率已恢复正常");
        liveLoadingBar.changeToPause(false);
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

    public boolean onBackPressed() {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            this.eventListener.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, true);
            return true;
        } else {
            liveViewWithThumbnail.showVideoView(isReallyVisibleToUser() && !isNormalView);
            presenter.performStopVideoAction(true);
            return false;
        }
    }

    @Override
    public void onReceiveMessage(CameraMessageSender.Message message) {

    }

    @Override
    public void onPickDate(long time, int state) {
        //选择时间,更新时间区域,//wheelView 回调的是毫秒时间, rtcp 回调的是秒,这里要除以1000
        if (isLand()) {
            performLayoutAnimation(true, true);
        }
        switch (state) {
            case STATE_FINISH: {
                setLiveRectTime(time, true);
                presenter.performPlayVideoAction(false, time);
            }
            break;
            default: {
                setLiveTimeContent(time);
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

    private Runnable backgroundCheckerRunnable = () -> {
        if (BaseApplication.getPauseViewCount() == 0) {
            //APP 进入了后台,需要停止直播播放,7.0 以上onStop 会延迟10秒,所以不能在 onStop 里停止直播,
            if (presenter != null) {
                presenter.performStopVideoAction(false);
            }
        }
    };

    private void initListener() {
        //isFriend.流量
        //c.loading
        (liveLoadingBar).setAction(this);
        ivViewModeSwitch.setOnClickListener(this::toggleSwitchMenu);
        rbViewModeSwitchParent.setOnCheckedChangeListener(this::switchViewMode);
        svSwitchStream.setSwitcherListener(this);
        liveViewWithThumbnail.setInterActListener(this);
        (layoutLandFlip).setFlipListener(this);
        (layoutPortFlip).setFlipListener(this);
        historyWheelHandler.setDatePickerListener(this);
    }

    public Device getDevice() {
        if (device == null) {
            device = DataSourceManager.getInstance().getDevice(uuid);
        }
        return device;
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

    private void toggleSwitchMenu(View view) {
        //平视,1.平视.0俯视.默认平视
        String dpPrimary = getDevice().$(509, "1");
        if ("1".equals(dpPrimary) && JFGRules.hasViewAngle(getDevice().pid)) {
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.SWITCH_VIEW_POP)
                    .setNegativeButton(R.string.CANCEL, null)
                    .setPositiveButton(R.string.OK, (dialog, which) -> {
                        try {
                            DataSourceManager.getInstance().updateValue(uuid, new DpMsgDefine.DPPrimary<String>("0"), DpMsgMap.ID_509_CAMERA_MOUNT_MODE);
                        } catch (IllegalAccessException e) {
                            AppLogger.e("err: ");
                        }
                        rbViewModeSwitchParent.setVisibility(rbViewModeSwitchParent.getVisibility() == VISIBLE ? GONE : VISIBLE);

                        if (videoView != null && videoView instanceof Panoramic360ViewRS && rbViewModeSwitchParent.getVisibility() == VISIBLE) {
                            ((Panoramic360ViewRS) videoView).setMountMode(Panoramic360ViewRS.MountMode.TOP);
                            ((RadioButton) rbViewModeSwitchParent.findViewById(getCheckIdByViewMode(((Panoramic360ViewRS) videoView).getDisplayMode()))).setChecked(true);
                            rbViewModeSwitchParent.setVisibility(VISIBLE);
                            if (!isLand()) {
                                ((Panoramic360ViewRS) videoView).enableAutoRotation(false);
                            }
                        }

                    }).show();
        } else if ("0".equals(dpPrimary)) {
            rbViewModeSwitchParent.setVisibility(rbViewModeSwitchParent.getVisibility() == VISIBLE ? GONE : VISIBLE);
            if (videoView != null && videoView instanceof Panoramic360ViewRS && rbViewModeSwitchParent.getVisibility() == VISIBLE) {
//                ((Panoramic360ViewRS) videoView).setMountMode(Panoramic360ViewRS.MountMode.TOP);
                ((RadioButton) rbViewModeSwitchParent.findViewById(getCheckIdByViewMode(((Panoramic360ViewRS) videoView).getDisplayMode()))).setChecked(true);
                rbViewModeSwitchParent.setVisibility(VISIBLE);
            }

        } else if ("1".equals(dpPrimary) && !JFGRules.hasViewAngle(device.pid)) {
            // TODO: 2017/8/18 怎么处理好呢?
            rbViewModeSwitchParent.setVisibility(rbViewModeSwitchParent.getVisibility() == VISIBLE ? GONE : VISIBLE);
            if (videoView != null && videoView instanceof Panoramic360ViewRS && rbViewModeSwitchParent.getVisibility() == VISIBLE) {
                ((Panoramic360ViewRS) videoView).setMountMode(Panoramic360ViewRS.MountMode.TOP);
                if (rbViewModeSwitchParent.getCheckedRadioButtonId() == -1) {
                    ((RadioButton) rbViewModeSwitchParent.findViewById(getCheckIdByViewMode(((Panoramic360ViewRS) videoView).getDisplayMode()))).setChecked(true);
                    rbViewModeSwitchParent.setVisibility(VISIBLE);
                }
            }
            AppLogger.d("当前视图不支持视角切换,但又支持视图切换,强制开始平视视图");
        } else if (!JFGRules.hasViewAngle(device.pid)) {
            rbViewModeSwitchParent.setVisibility(rbViewModeSwitchParent.getVisibility() == VISIBLE ? GONE : VISIBLE);
            if (videoView != null && videoView instanceof Panoramic360ViewRS && rbViewModeSwitchParent.getVisibility() == VISIBLE) {
                ((Panoramic360ViewRS) videoView).setMountMode(Panoramic360ViewRS.MountMode.TOP);
                ((RadioButton) rbViewModeSwitchParent.findViewById(getCheckIdByViewMode(((Panoramic360ViewRS) videoView).getDisplayMode()))).setChecked(true);
                rbViewModeSwitchParent.setVisibility(VISIBLE);
            }
        }
    }

    void switchViewMode(RadioGroup radioGroup, int checkId) {
        ((RadioButton) radioGroup.findViewById(checkId)).setChecked(true);
        switch (checkId) {
            case R.id.rb_view_mode_circular:
                if (videoView != null && videoView instanceof Panoramic360ViewRS) {
                    ((Panoramic360ViewRS) videoView).setDisplayMode(Panoramic360ViewRS.SFM_Normal);
                    ivModeXunHuan.setEnabled(enableAutoRotate);
                    enableAutoRotate(isLand() && enableAutoRotate);
                    AppLogger.d("正在切换到圆形视图");
                }
                break;
            case R.id.rb_view_mode_column:
                if (videoView != null && videoView instanceof Panoramic360ViewRS) {
                    ((Panoramic360ViewRS) videoView).setDisplayMode(Panoramic360ViewRS.SFM_Cylinder);
                    ivModeXunHuan.setEnabled(enableAutoRotate = false);
                    enableAutoRotate(enableAutoRotate);
                    AppLogger.d("正在切换到柱状视图");
                }
                break;
            case R.id.rb_view_mode_four:
                if (videoView != null && videoView instanceof Panoramic360ViewRS) {
                    ((Panoramic360ViewRS) videoView).setDisplayMode(Panoramic360ViewRS.SFM_Quad);
                    ivModeXunHuan.setEnabled(enableAutoRotate = false);
                    enableAutoRotate(enableAutoRotate);
                    AppLogger.d("正在切换到四合一视图");
                }
                break;
        }
        rbViewModeSwitchParent.setVisibility(GONE);
    }

    private void getSdcardStatus() {
        Subscription subscription = Observable.just(new DPEntity()
                .setMsgId(204)
                .setUuid(uuid)
                .setAction(DBAction.QUERY)
                .setVersion(0)
                .setOption(DBOption.SingleQueryOption.ONE_BY_TIME))
                .subscribeOn(Schedulers.io())
                .flatMap(entity -> BaseDPTaskDispatcher.getInstance().perform(entity))
                .map(ret -> {
                    try {
                        DpMsgDefine.DPSdStatus sdStatus = ret.getResultResponse();
                        return sdStatus;
                    } catch (Exception e) {
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .first()
                .subscribe(status -> {

                    if (status == null) {
                        ToastUtil.showToast(getResources().getString(R.string.NO_SDCARD));
                    } else {
                        if (!status.hasSdcard) {
                            ToastUtil.showToast(getResources().getString(R.string.NO_SDCARD));
                            return;
                        }
                        if (status.err != 0) {
                            ToastUtil.showToast(getResources().getString(R.string.VIDEO_SD_DESC));
                            return;
                        }
                        performLoadHistoryAndPlay(-1);
                    }
                }, throwable -> ToastUtil.showToast(getResources().getString(R.string.NO_SDCARD)));

        presenter.addSubscription("getSdcardStatus", subscription);
    }


    public void playHistoryAndSetLiveTime(long playTime) {
        liveLoadingBar.changeToLoading(canShowLoadingBar());
        presenter.performPlayVideoAction(false, playTime);
        setLiveRectTime(playTime, true);
    }

    public void performLoadHistoryAndPlay(long playTime) {
        AppLogger.d("点击加载历史视频");
        //这里需要判断是否已经是加载过历史视频了,虽然这个有局限性
        if (historyWheelContainer.getDisplayedChild() == 1) {
            playHistoryAndSetLiveTime(playTime);
        } else {
            btnLoadHistory.setEnabled(false);
            liveLoadingBar.changeToLoading(canShowLoadingBar(), getResources().getString(R.string.VIDEO_REFRESHING), null);
            presenter.fetchHistoryDataListV2(uuid, (int) (TimeUtils.getTodayEndTime() / 1000), 1, 3, playTime);
        }
    }

    public void initView(CamLiveContract.Presenter presenter, String uuid) {
        //竖屏 隐藏
        this.presenter = presenter;
        this.uuid = uuid;
        this.device = DataSourceManager.getInstance().getDevice(uuid);
        this.isRSCam = JFGRules.isRS(device.pid);
        this.hasMicFeature = JFGRules.hasMicFeature(device.pid);
        this.hasDoorLock = JFGRules.hasDoorLock(device.pid);
        this.isShareAccount = !TextUtils.isEmpty(device.shareAccount);
        this.pid = device.pid;
        this.historyWheelHandler = new HistoryWheelHandler(superWheelExt, uuid);
        isNormalView = device != null && !JFGRules.isNeedPanoramicView(device.pid);
        //disable 6个view
        initListener();
        performReLayoutAction();
        enableAutoRotate(false);
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

    /**
     * 错误码 需要放在一个Map里面管理
     *
     * @param errCode
     */
    private void handlePlayErr(int errCode) {
        switch (errCode) {//这些errCode 应当写在一个map中.Map<Integer,String>
            case JFGRules.PlayErr.ERR_NETWORK:
                liveLoadingBar.changeToLoadingError(true, ContextUtils.getContext().getString(R.string.OFFLINE_ERR_1), ContextUtils.getContext().getString(R.string.USER_HELP));
                break;
            case JFGRules.PlayErr.ERR_UNKOWN:
                liveLoadingBar.changeToLoadingError(true, ContextUtils.getContext().getString(R.string.NO_NETWORK_2), null);
                break;
            case JFGRules.PlayErr.ERR_LOW_FRAME_RATE:
                int net = NetUtils.getJfgNetType(getContext());
                liveLoadingBar.changeToLoadingError(true, ContextUtils.getContext().getString(R.string.GLOBAL_NO_NETWORK), net == 0 ? ContextUtils.getContext().getString(R.string.USER_HELP) : null);
                break;
            case STOP_MAUNALLY:
            case PLAY_STATE_STOP:
                liveLoadingBar.changeToPlaying(true);
                break;
            case JFGRules.PlayErr.ERR_NOT_FLOW:
                if (isLivePlaying()) {//可能已经失败了,再提示网络连接超时就不正常了
                    liveLoadingBar.changeToLoadingError(true, ContextUtils.getContext().getString(R.string.NETWORK_TIMEOUT), ContextUtils.getContext().getString(R.string.USER_HELP));
                }
                break;
            case JError.ErrorVideoPeerDisconnect:
                if (isLivePlaying()) {
                    liveLoadingBar.changeToLoadingError(true, ContextUtils.getContext().getString(R.string.Device_Disconnected), null);
                }
                break;
            case JFGRules.PlayErr.ERR_DEVICE_OFFLINE:
                liveLoadingBar.changeToLoadingError(true, getContext().getString(R.string.OFFLINE_ERR), getContext().getString(R.string.USER_HELP));
                break;
            case JError.ErrorVideoPeerNotExist:
                liveLoadingBar.changeToLoadingError(true, getContext().getString(R.string.OFFLINE_ERR), getContext().getString(R.string.USER_HELP));
                break;
            case JError.ErrorVideoPeerInConnect:
                //正在直播...
                liveLoadingBar.changeToLoadingError(true, getContext().getString(R.string.CONNECTING), null);
                break;
            case PLAY_STATE_IDLE:
                liveLoadingBar.hideLoading();
                break;
            case PLAY_STATE_NET_CHANGED:
                liveLoadingBar.changeToLoading(true);
                break;
            case JError.ErrorSDHistoryAll:
                liveLoadingBar.changeToLoadingError(true, getContext().getString(R.string.Historical_Read), null);
                if (getContext() instanceof Activity) {
                    AlertDialogManager.getInstance().showDialog((Activity) getContext(),
                            getContext().getString(R.string.Historical_Read),
                            getContext().getString(R.string.Historical_Read),
                            getContext().getString(R.string.OK), (DialogInterface dialog, int which) -> {
                                presenter.performPlayVideoAction(true, 0);
                            });
                }
                break;
            case JError.ErrorSDFileIO:
                liveLoadingBar.changeToLoadingError(true, getContext().getString(R.string.Historical_Failed), null);
                if (getContext() instanceof Activity) {
                    AlertDialogManager.getInstance().showDialog((Activity) getContext(),
                            getContext().getString(R.string.Historical_Failed),
                            getContext().getString(R.string.Historical_Failed),
                            getContext().getString(R.string.OK), (DialogInterface dialog, int which) -> {
                                presenter.performPlayVideoAction(true, 0);
                            });
                }
                break;
            case JError.ErrorSDIO:
                liveLoadingBar.changeToLoadingError(true, getContext().getString(R.string.Historical_No), null);
                if (getContext() instanceof Activity) {
                    AlertDialogManager.getInstance().showDialog((Activity) getContext(),
                            getContext().getString(R.string.Historical_No),
                            getContext().getString(R.string.Historical_No),
                            getContext().getString(R.string.OK), (DialogInterface dialog, int which) -> {
                                presenter.performPlayVideoAction(true, 0);
                            });
                }
                break;
            default:
                liveLoadingBar.changeToLoadingError(true, getContext().getString(R.string.GLOBAL_NO_NETWORK), null);
                break;
        }
    }

    private void setLiveRectTime(long timestamp, boolean focus) {
        //历史视频的时候，使用rtcp自带时间戳。
        if (!isLive() && timestamp == 0) {
            return;
        }
        //直播时候，使用本地时间戳。
        //全景的时间戳是0,使用设备的时区
        //wifi狗是格林尼治时间戳,需要-8个时区.
        boolean historyLocked = historyWheelHandler.isHistoryLocked();
        Log.d("TYPE_HISTORY", "time: " + timestamp + ",locked:" + historyLocked + ",focus:" + focus);
        if (!historyLocked || focus) {
            setLiveTimeContent(timestamp);
            if (!isLive()) {
                superWheelExt.scrollToPosition(TimeUtils.wrapToLong(timestamp), focus, focus);
            }
        }
    }

    private void setLiveTimeContent(long timestamp) {
        if (JFGRules.hasSDFeature(pid) && !JFGRules.isShareDevice(uuid)) {
            liveTimeLayout.setContent(isLive() ? CamLiveContract.TYPE_LIVE : CamLiveContract.TYPE_HISTORY, isLive() ? 0 : timestamp);
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
        if (isLivePlaying()) {
            presenter.performStopVideoAction(true);
            ((ImageView) v).setImageResource(R.drawable.icon_landscape_stop);
        } else {
            ((ImageView) v).setImageResource(R.drawable.icon_landscape_playing);
            presenter.performPlayVideoAction();
        }
    }

    @OnClick(R.id.tv_live)
    public void onLiveClick() {
        if (canPlayVideoNow()) {
            presenter.performPlayVideoAction(true, 0);
            AppLogger.i("TextView click start play!");
        }
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
        boolean switchModeButton = JFGRules.showSwitchModeButton(device.pid);
        if (!switchModeButton) {
            this.enableAutoRotate = false;
            return;
        }
        this.enableAutoRotate = enableAutoRotate;
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
        getSdcardStatus();
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
        // TODO: 2017/8/23 摇一摇
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
        if (BuildConfig.DEBUG) {
            Log.d(CameraLiveHelper.TAG, "onDeviceNetChanged,net is:" + net + " isLocalOnline:" + isLocalOnline);
        }
        this.isLocalOnline = isLocalOnline;
        performLayoutEnableAction();
    }

    @Override
    public void onUpdateAlarmOpenChanged(boolean alarmOpened) {
        if (BuildConfig.DEBUG) {
            Log.d(CameraLiveHelper.TAG, "onUpdateAlarmOpenChanged,isAlarmOpened:" + alarmOpened);
        }
        setFlipped(!alarmOpened);
    }

    @Override
    public void onChangeSafeProtectionErrorAutoRecordClosed() {
        Log.d(CameraLiveHelper.TAG, "onChangeSafeProtectionErrorAutoRecordClosed");
        AlertDialogManager.getInstance().showDialog((Activity) getContext(),
                getContext().getString(R.string.Tap1_Camera_MotionDetection_OffTips),
                getContext().getString(R.string.Tap1_Camera_MotionDetection_OffTips),
                getContext().getString(R.string.CARRY_ON), (DialogInterface dialog, int which) -> {
                    presenter.performChangeSafeProtection(1);
                    //关闭移动侦测的同时也关闭自动录像
                    setFlipped(true);
                    ToastUtil.showToast(getContext().getString(R.string.SCENE_SAVED));
                    if (isLand()) {
                        ViewUtils.setSystemUiVisibility(liveLoadingBar, false);
                    }
                }, getContext().getString(R.string.CANCEL), (dialog, which) -> {
                    if (isLand()) {
                        ViewUtils.setSystemUiVisibility(liveLoadingBar, false);
                    }
                });
    }

    @Override
    public void onChangeSafeProtectionErrorNeedConfirm() {
        Log.d(CameraLiveHelper.TAG, "onChangeSafeProtectionErrorAutoRecordClosed");
        AlertDialogManager.getInstance().showDialog((Activity) getContext(), "safeIsOpen", getContext().getString(R.string.Detection_Pop),
                getContext().getString(R.string.OK), (dialog, which) -> {
                    presenter.performChangeSafeProtection(1);
                    setFlipped(true);
                    if (isLand()) {
                        ViewUtils.setSystemUiVisibility(liveLoadingBar, false);
                    }
                }, getContext().getString(R.string.CANCEL), (dialog, which) -> {
                    if (isLand()) {
                        ViewUtils.setSystemUiVisibility(liveLoadingBar, false);
                    }
                }, false);
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
        performLayoutAnimation(!isLayoutAnimationShowing, true);
        return false;
    }

    @Override
    public void onSnapshot(Bitmap bitmap, boolean tag) {
        if (BuildConfig.DEBUG) {
            Log.d("onSnapshot", "onSnapshot: " + (bitmap == null));
        }
    }

    @Override
    public void clickImage(View view, int state) {
        boolean livePlaying = isLivePlaying();
        if (livePlaying) {
            presenter.performStopVideoAction(true);
        } else {
            presenter.performPlayVideoAction();
        }
        AppLogger.i("clickImage:" + state);
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
        return isLive() && isLivePlaying() && JFGRules.showSwitchModeButton(device.pid)
                && videoView != null && videoView instanceof Panoramic360ViewRS
                && ((Panoramic360ViewRS) videoView).getDisplayMode() == Panoramic360ViewRS.SFM_Cylinder;
    }

    private boolean canModeSwitchEnable() {
        return isLive() && isLivePlaying() && JFGRules.showSwitchModeButton(device.pid);
    }

    private boolean canLoadHistoryEnable() {
        return !isStandBy() && JFGRules.isDeviceOnline(uuid);
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
        Log.d(TAG, "performLandLayoutAnimation,showLayout:" + showLayout);
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

            svSwitchStream.animate().setDuration(ANIMATION_DURATION).alpha(0).translationY(svSwitchStream.getHeight() / 4)
                    .withStartAction(() -> {
                        svSwitchStream.performSlideAnimation(false);
                    })
                    .withEndAction(() -> {
                        svSwitchStream.setVisibility(INVISIBLE);
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

            liveViewModeContainer.animate().setDuration(ANIMATION_DURATION).alpha(0).translationY(liveViewModeContainer.getHeight() / 4).withEndAction(() -> {
                ivViewModeSwitch.setEnabled(isLive() && isLivePlaying() && JFGRules.showSwitchModeButton(device.pid));
                liveViewModeContainer.setVisibility(INVISIBLE);
            }).start();
            liveViewWithThumbnail.getTvLiveFlow().animate().setDuration(ANIMATION_DURATION).translationY(-liveTopBannerView.getHeight()).withEndAction(() -> {

            }).start();
        }
    }

    private void performPortLayoutAnimation(boolean showLayout) {
        Log.d(TAG, "performPortLayoutAnimation,showLayout:" + showLayout);
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
            svSwitchStream.animate().setDuration(ANIMATION_DURATION).alpha(0).translationY(0).withStartAction(() -> {
                svSwitchStream.performSlideAnimation(false);
            }).withEndAction(() -> {
                svSwitchStream.setVisibility(INVISIBLE);
            }).start();

            liveViewModeContainer.animate().setDuration(ANIMATION_DURATION).alpha(0).translationY(0).withEndAction(() -> {
                ivViewModeSwitch.setEnabled(canModeSwitchEnable());
                liveViewModeContainer.setVisibility(INVISIBLE);
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
        liveTopBannerView.setVisibility(isLand ? VISIBLE : INVISIBLE);
        bottomControllerContainer.setVisibility(isLand ? INVISIBLE : VISIBLE);
        layoutLandFlip.setVisibility(showFlip && isLand ? VISIBLE : GONE);
        layoutPortFlip.setVisibility(showFlip && !isLand && isLivePlaying() ? VISIBLE : GONE);
        imgVCamZoomToFullScreen.setVisibility(isLand ? INVISIBLE : VISIBLE);
        imgVCamLiveLandPlay.setVisibility(isLand ? VISIBLE : GONE);
        liveBottomBannerView.setVisibility(isLivePlaying() ? VISIBLE : INVISIBLE);
        historyWheelContainer.setVisibility(showHistoryWheel ? VISIBLE : INVISIBLE);
        tvLive.setVisibility(historyWheelContainer.getDisplayedChild() == 1 || !isHistoryEmpty() ? VISIBLE : GONE);
        liveViewModeContainer.setVisibility(canShowViewModeMenu() ? VISIBLE : INVISIBLE);
        vFlag.setVisibility(historyWheelContainer.getDisplayedChild() == 1 || !isHistoryEmpty() ? VISIBLE : GONE);
        ivModeXunHuan.setVisibility(JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
        svSwitchStream.setVisibility(canShowStreamSwitcher() ? VISIBLE : GONE);
        imgVCamTriggerMic.setVisibility(hasMicFeature ? VISIBLE : GONE);
        imgVLandCamTriggerMic.setVisibility(hasMicFeature ? VISIBLE : GONE);
        ivCamDoorLock.setVisibility(hasDoorLock && !isShareAccount ? VISIBLE : GONE);
        ivViewModeSwitch.setVisibility(JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
        vDivider.setVisibility(showFlip && MiscUtils.isLand() ? VISIBLE : GONE);
        liveViewWithThumbnail.getTvLiveFlow().setVisibility(isLivePlaying() ? VISIBLE : GONE);
        liveLoadingBar.showOrHide(canShowLoadingBar());
        historyParentContainer.setVisibility(canShowHistoryWheel() ? VISIBLE : INVISIBLE);
    }

    private void performLayoutEnableAction() {
        boolean isHistory = !isLive();
        boolean isPlaying = isLivePlaying();
        //直播
        tvLive.setEnabled(isHistory);
        ivModeXunHuan.setEnabled(canXunHuanEnable());
        ivViewModeSwitch.setEnabled(canModeSwitchEnable());
        imgVCamTriggerCapture.setEnabled(isPlaying);
        imgVLandCamTriggerCapture.setEnabled(isPlaying);
        imgVCamTriggerMic.setEnabled(isPlaying && !isHistory);
        imgVLandCamTriggerMic.setEnabled(isPlaying && !isHistory);
        imgVCamSwitchSpeaker.setEnabled(isPlaying);
        imgVLandCamSwitchSpeaker.setEnabled(isPlaying);
        ivCamDoorLock.setEnabled(isDoorLockEnable());
        imgVCamZoomToFullScreen.setEnabled(isPlaying);
        liveViewWithThumbnail.setEnabled(true);
        btnLoadHistory.setEnabled(canLoadHistoryEnable());
    }

    private boolean isDoorLockEnable() {
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
        liveViewWithThumbnail.showFlowView(isLivePlaying(), null);

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
        decideFlippedContent();
        decideLiveThumbContent();
        decideLiveViewMode();
        decideFirstSightSetting();
        decideDeviceTimezone();
        decideHistoryShowCase();
        decidePendingResumeAction();
        enableAutoRotate(enableAutoRotate);
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
                performLoadHistoryAndPlay(playTime);
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
    }

    private void decideFlippedContent() {
        Boolean alarmOpen = device.$(DpMsgMap.ID_501_CAMERA_ALARM_FLAG, false);
        setFlipped(!alarmOpen);
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