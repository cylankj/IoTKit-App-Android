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
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
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

import com.cylan.entity.jniCall.JFGDPMsg;
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
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.module.CameraLiveHelper;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.module.DPTimeZone;
import com.cylan.jiafeigou.module.HistoryManager;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
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
import com.cylan.jiafeigou.utils.APObserver;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

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
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_501_CAMERA_ALARM_FLAG;
import static com.cylan.jiafeigou.misc.JConstant.CYLAN_TAG;
import static com.cylan.jiafeigou.misc.JConstant.KEY_CAM_SIGHT_SETTING;
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
        HistoryManager.HistoryObserver, Switcher.SwitcherListener, VideoViewFactory.InterActListener, ILiveControl.Action, FlipImageView.OnFlipListener {
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
    private float portRatio = -1;
    private RoundCardPopup roundCardPopup;
    private HistoryWheelHandler historyWheelHandler;
    private CamLiveContract.Presenter presenter;
    private int pid;
    private boolean isRSCam;
    private boolean isShareAccount = false;
    private boolean hasPingSuccess = false;
    private boolean needShowSight;
    private volatile boolean hasPendingHistoryPlayAction = false;
    private long pendingHistoryPlayTime = -1;
    /**
     * 设备的时区
     */
    private SimpleDateFormat liveTimeDateFormat;
    private Device device;
    private boolean hasMicFeature;
    private boolean hasDoorLock;
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
        Device device = getDevice();
        isNormalView = device != null && !JFGRules.isNeedPanoramicView(device.pid);
        eventListener = new MyEventListener(getActivity());
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
        updateLiveViewRectHeight(isNormalView ? presenter.getVideoPortHeightRatio() : 1.0f);
        setOrientationHandle(eventListener::setRequestedOrientation);
    }

    @Override
    public void onPause() {
        super.onPause();
        liveLoadingBar.postDelayed(backgroundCheckerRunnable, 700);
        enableSensor(false);
        presenter.performStopVideoAction();
    }

    private boolean isReallyVisibleToUser() {
        return getUserVisibleHint() && isResumed() && getActivity() != null && presenter != null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isReallyVisibleToUser()) {
            performLazyContentInitAction();
        } else if (presenter != null) {
            presenter.performStopVideoAction();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        liveLoadingBar.removeCallbacks(backgroundCheckerRunnable);
        if (isReallyVisibleToUser()) {
            performLazyContentInitAction();
        }
    }

    private void performLazyContentInitAction() {
        // TODO: 2017/8/16 直播页需要自动横屏了
        performLayoutContentAction();
        performTimeZoneRefresh();
        //直播成功之后，才触发sensor.
        Bundle bundle = getArguments();
        if (bundle != null && presenter != null) {
            long playTime = bundle.getLong(JConstant.KEY_CAM_LIVE_PAGE_PLAY_HISTORY_TIME, -1);
            bundle.remove(JConstant.KEY_CAM_LIVE_PAGE_PLAY_HISTORY_TIME);
            if (playTime > 0 && CameraLiveHelper.canPlayVideoNow(uuid)) {
                AppLogger.d("需要定位到时间轴:" + playTime);
                performLoadHistoryAndPlay(playTime);
            }
        }
    }

    @Override
    public void onDestroyView() {
        //1.live view pause
        try {
            if (videoView != null) {
                videoView.onPause();
                videoView.onDestroy();
            }
            HistoryManager.getInstance().removeHistoryObserver(uuid);
        } catch (Exception e) {
        }
        super.onDestroyView();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public boolean performBackIntercept(boolean willExit) {
        return isVisible && isPrepared && willExit && onBackPressed();
    }

    @Override
    public void onDeviceInfoChanged(JFGDPMsg msg) throws IOException {
        int msgId = (int) msg.id;
        if (msgId == DpMsgMap.ID_222_SDCARD_SUMMARY) {
            DpMsgDefine.DPSdcardSummary sdStatus = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPSdcardSummary.class);
            if (!JFGRules.hasSdcard(sdStatus)) {
                AppLogger.d("sdcard 被拔出");
                showPlayHistoryButton();
                if (!getUserVisibleHint() || JFGRules.isShareDevice(uuid)) {
                    AppLogger.d("隐藏了，sd卡更新");
                    return;
                }
                getAlertDialogManager().showDialog(getActivity(), getString(R.string.MSG_SD_OFF),
                        getString(R.string.MSG_SD_OFF),
                        getString(R.string.OK), (DialogInterface d, int which) -> {
                            boolean videoPlaying = CameraLiveHelper.isVideoPlaying(presenter.getCameraLiveAction());
                            if (!videoPlaying && CameraLiveHelper.canPlayVideoNow(uuid)) {
                                presenter.performPlayVideoAction(true, 0);
                            }

                        });
                if (!CameraLiveHelper.isLive(presenter.getCameraLiveAction())) {
                    presenter.performStopVideoAction();
                }
                AppLogger.e("sdcard数据被清空，唐宽，还没实现");
            }
        }

        if (msgId == DpMsgMap.ID_508_CAMERA_STANDBY_FLAG) {
            DpMsgDefine.DPStandby standby = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPStandby.class);
            if (standby != null && standby.standby) {
                presenter.performStopVideoAction();
            } else {
                if (isLivePlaying()) {
                    //恢复播放
                    if (CameraLiveHelper.canPlayVideoNow(uuid)) {
                        presenter.performPlayVideoAction();
                    }
                }
            }
            onDeviceStandByChanged(presenter.getDevice(), v -> jump2Setting());
        }
        if (msgId == DpMsgMap.ID_218_DEVICE_FORMAT_SDCARD) {
            if (!getUserVisibleHint()) {
                AppLogger.d("隐藏了，sd卡被格式化");
                return;
            }
            if (isLive()) {
                return;
            }
            getAlertDialogManager().showDialog(getActivity(), getString(R.string.Clear_Sdcard_tips6),
                    getString(R.string.Clear_Sdcard_tips6),
                    getString(R.string.OK), null,
                    getString(R.string.CANCEL), null);
        }
        if (msgId == 509) {
            Device device = DataSourceManager.getInstance().getDevice(uuid());
            String _509 = device.$(509, "1");
            if (device.pid == 39 || device.pid == 49) {
                _509 = "0";
            }
            updateLiveViewMode(_509);
        }
        if (msgId == DpMsgMap.ID_201_NET) {
            updateDeviceNet();
        }
        if (msg != null && msg.id == 214) {
            TimeZone timeZone = JFGRules.getDeviceTimezone(device);
            liveTimeDateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.UK);
            liveTimeDateFormat.setTimeZone(timeZone);
            liveTimeLayout.setTimeZone(timeZone);
            superWheelExt.setTimeZone(timeZone);
        }
        if (msg != null && msg.id == 510) {
            try {
                updateCamParam(DpUtils.unpackData(msg.packValue, DpMsgDefine.DpCoordinate.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    private void jump2Setting() {
        Intent intent = new Intent(getContext(), CamSettingActivity.class);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid());
        startActivityForResult(intent, REQUEST_CODE,
                ActivityOptionsCompat.makeCustomAnimation(getActivity(), R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
        AppLogger.d("跳转到使用帮助");
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
        historyWheelHandler = getHistoryWheelHandler();
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
            if (!videoView.isShown()) {
                videoView.setVisibility(VISIBLE);
            }
            Command.getInstance().enableRenderSingleRemoteView(true, videoView);
        } catch (JfgException e) {
            AppLogger.e("err:" + MiscUtils.getErr(e));
        }
        Device device = getDevice();
        float ratio = JFGRules.isNeedNormalRadio(device.pid) ? (isLand() ? getLandFillScreen() : (float) resolution.height / resolution.width) :
                isLand() ? (float) Resources.getSystem().getDisplayMetrics().heightPixels /
                        Resources.getSystem().getDisplayMetrics().widthPixels : 1.0f;
        if (portRatio == -1 && !isLand()) {
            portRatio = ratio;
        }
        performReLayoutAction();
        updateLiveViewRectHeight(portRatio);
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
    public boolean isUserVisible() {
        return getUserVisibleHint();
    }

    @Override
    public void onBatteryDrainOut() {
        //当前页面才显示
        if (!isAdded() || !isUserVisible()) {
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
        if (getUserVisibleHint() && isResumed() && getActivity() != null) {
            //这里是个异步的,显示的条件是当前 fragment 可见
            AppLogger.d(" //这里是个异步的,显示的条件是当前 fragment 可见");
            if (!MiscUtils.isLand()) {
                LiveShowCase.showHistoryWheelCase(getActivity(), historyParentContainer);
                LiveShowCase.showHistoryCase((Activity) getContext(), imgVCamZoomToFullScreen);
            }
        }
    }

    @Override
    public void onDeviceUnBind() {
        AppLogger.d("当前设备已解绑");
        presenter.performStopVideoAction();
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
        hasPendingHistoryPlayAction = false;
        presenter.performPlayVideoAction(true, 0);
        btnLoadHistory.setEnabled(true);
        ToastUtil.showToast(getResources().getString(R.string.NO_CONTENTS_2));
    }

    @Override
    public void onHistoryReady(Collection<JFGVideo> history) {
        Log.d(CYLAN_TAG, "历史录像视频数据已就绪");
        historyWheelContainer.setDisplayedChild(1);
        tvLive.setVisibility(VISIBLE);
        vFlag.setVisibility(VISIBLE);
        showHistoryWheel(true);
        reInitHistoryHandler();
        superWheelExt.setHistoryFiles(history);
        tvCamLiveLandBottom.setVisibility(VISIBLE);
        if (hasPendingHistoryPlayAction) {
            hasPendingHistoryPlayAction = false;
            long playTime = -1;
            if (pendingHistoryPlayTime > 0) {
                playTime = pendingHistoryPlayTime;
                pendingHistoryPlayTime = -1;
            } else {
                JFGVideo jfgVideo = null;
                if (history != null && history.size() > 0) {
                    jfgVideo = history.iterator().next();
                }
                if (jfgVideo != null) {
                    playTime = jfgVideo.beginTime * 1000L;
                }
            }
            if (playTime > 0) {
                Log.d("RePlay", "Replay history");
                playHistoryAndSetLiveTime(playTime);
            } else {
                Log.d("RePlay", "Replay history no time to play");
            }
        }
        Log.d("onHistoryReady", "onHistoryReady:" + new Gson().toJson(history));
    }

    @Override
    public void onLoadHistoryFailed() {
        Log.d(CameraLiveHelper.TAG, "加载历史录像失败了");
        hasPendingHistoryPlayAction = false;
        btnLoadHistory.setEnabled(true);
        liveLoadingBar.changeToPlaying(canShowLoadingBar());
        if (presenter.isHistoryEmpty()) {
            ToastUtil.showToast(getResources().getString(R.string.Item_LoadFail));
        }
    }

    @Override
    public void onPlayErrorStandBy() {
        Log.d(CameraLiveHelper.TAG, "设备已设置为待机模式");
        performReLayoutAction();
    }

    @Override
    public void onPlayErrorFirstSight() {
        Log.d(CameraLiveHelper.TAG, "第一次使用全景模式");
        performReLayoutAction();
    }

    @Override
    public void onPlayErrorNoNetwork() {
        Log.d(CameraLiveHelper.TAG, "无网络连接");
        liveLoadingBar.changeToLoadingError(true, ContextUtils.getContext().getString(R.string.OFFLINE_ERR_1), ContextUtils.getContext().getString(R.string.USER_HELP));
        performReLayoutAction();
    }

    @Override
    public void onPlayErrorDeviceOffLine() {
        if (BuildConfig.DEBUG) {
            Log.d(CameraLiveHelper.TAG, "设备离线了");
        }
        liveLoadingBar.changeToLoadingError(true, ContextUtils.getContext().getString(R.string.OFFLINE_ERR), getContext().getString(R.string.USER_HELP));
        performReLayoutAction();
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
        liveLoadingBar.changeToLoadingError(true, getContext().getString(R.string.NETWORK_TIMEOUT), getContext().getString(R.string.USER_HELP));
        performReLayoutAction();
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
        showPlayHistoryButton();
        if (!getUserVisibleHint() || JFGRules.isShareDevice(uuid)) {
            AppLogger.d("隐藏了，sd卡更新");
            return;
        }
        getAlertDialogManager().showDialog(getActivity(), getString(R.string.MSG_SD_OFF),
                getString(R.string.MSG_SD_OFF),
                getString(R.string.OK), (DialogInterface d, int which) -> {
                    if (!isLivePlaying() && CameraLiveHelper.canPlayVideoNow(uuid)) {
                        presenter.performPlayVideoAction(true, 0);
                    }

                });
        if (!isLive()) {
            presenter.performStopVideoAction();
        }
        AppLogger.e("sdcard数据被清空，唐宽，还没实现");
    }

    @Override
    public void onDeviceSDCardFormat() {
        Log.d(CameraLiveHelper.TAG, "onDeviceSDCardFormat");
    }

    @Override
    public void onUpdateLiveViewMode(String _509) {
        Log.d(CameraLiveHelper.TAG, "onUpdateLiveViewMode:" + _509);
    }

    @Override
    public void onDeviceTimeZoneChanged(DPTimeZone timeZone) {
        Log.d(CameraLiveHelper.TAG, "onDeviceTimeZoneChanged:" + timeZone);
        performTimeZoneRefresh();
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
            AppLogger.d("用户按下了返回键,需要手动停止播放直播,Bug:Android 7.0 以上 stop 延迟调用");
            if (videoView != null) {
                liveViewWithThumbnail.showVideoView(false);
            }
            presenter.performStopVideoAction();
            return false;
        }
    }

    @Override
    public void onReceiveMessage(CameraMessageSender.Message message) {

    }

    class MyEventListener extends com.cylan.jiafeigou.misc.OrientationListener {

        private boolean isShake = false;

        private volatile int orientation = -1;

        private int customOrientation = -1;

        public int getOrientation() {
            return orientation;
        }

        public int getCustomOrientation() {
            return customOrientation;
        }

        public void setCustomOrientation(int customOrientation) {
            this.customOrientation = customOrientation;
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
                if (presenter != null && isUserVisible() && isResumed() && getActivity() != null && isLivePlaying()) {
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

            if (presenter != null && isUserVisible() && isResumed() && getActivity() != null && isLivePlaying()) {
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
                presenter.performStopVideoAction();
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
            this.hasPendingHistoryPlayAction = true;
            this.pendingHistoryPlayTime = playTime;
            btnLoadHistory.setEnabled(false);
            liveLoadingBar.changeToLoading(canShowLoadingBar(), getResources().getString(R.string.VIDEO_REFRESHING), null);
            hasPendingHistoryPlayAction = true;
            presenter.fetchHistoryDataListV2(uuid, (int) (TimeUtils.getTodayEndTime() / 1000), 1, 3);
        }
    }

    public void initView(CamLiveContract.Presenter presenter, String uuid) {
        //竖屏 隐藏
        this.presenter = presenter;
        this.uuid = uuid;
        this.device = DataSourceManager.getInstance().getDevice(uuid);
        this.isRSCam = JFGRules.isRS(device.pid);
        this.needShowSight = JFGRules.showSight(presenter.getDevice().pid, JFGRules.isShareDevice(uuid));
        this.hasMicFeature = JFGRules.hasMicFeature(device.pid);
        this.hasDoorLock = JFGRules.hasDoorLock(device.pid);
        this.isShareAccount = !TextUtils.isEmpty(device.shareAccount);
        this.pid = device.pid;
        //disable 6个view
        initListener();
        performReLayoutAction();
        videoView = VideoViewFactory.CreateRendererExt(device.pid, getContext());
        if (!JFGRules.showSwitchModeButton(device.pid) && videoView instanceof Panoramic360ViewRS) {
            ((Panoramic360ViewRS) videoView).enableAutoRotation(false);
        }
        //issue: 过早 add 进去会导致黑块!!!!!
        liveViewWithThumbnail.setLiveView(videoView, uuid);

        updateLiveViewMode(device.$(509, "1"));
        initSightSetting(presenter);
        updateDoorLock();
        if (JFGRules.shouldObserverAP()) {//需要监听是否局域网在线
            Subscription subscribe = APObserver.scan(uuid)
                    .timeout(5, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ret -> {
                        hasPingSuccess = true;
                        updateDoorLock();
                    }, e -> {
                        e.printStackTrace();
                        AppLogger.e(e);
                        hasPingSuccess = false;
                        updateDoorLock();
                    });
            presenter.addSubscription("CameraLiveFragmentEx.APObserver.scan", subscribe);
        }
        AppLogger.w("需要重置清晰度");
        HistoryManager.getInstance().addHistoryObserver(uuid, this);
    }

    public void updateDoorLock() {
        //无网络连接或者设备离线不可点击,局域网在线可点击,
        DpMsgDefine.DPNet net = device.$(201, new DpMsgDefine.DPNet());
        boolean deviceOnline = JFGRules.isDeviceOnline(net);
        boolean noNet = NetUtils.getNetType(getContext()) == -1;
        if ((!deviceOnline && !hasPingSuccess) || noNet || isShareAccount) {
            ivCamDoorLock.setEnabled(false);
        }
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

    public HistoryWheelHandler getHistoryWheelHandler() {
        reInitHistoryHandler();
        return historyWheelHandler;
    }

    private boolean isSightShown;

    public boolean isSightShown() {
        return isSightShown;
    }

    /**
     * 全景视角设置
     */
    private void initSightSetting(CamLiveContract.Presenter basePresenter) {

        if (!needShowSight || JFGRules.isShareDevice(uuid)) {
            return;
        }
        String uuid = basePresenter.getUuid();
        isSightShown = PreferencesUtils.getBoolean(KEY_CAM_SIGHT_SETTING + uuid, true);
        Log.d("initSightSetting", "judge? " + isSightShown);
        if (!isSightShown) {
            return;//不是第一次
        }
        historyParentContainer.setVisibility(GONE);//需要隐藏历史录像时间轴
        View oldLayout = liveViewWithThumbnail.findViewById(R.id.fLayout_cam_sight_setting);
        if (oldLayout == null) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.cam_sight_setting_overlay, null);
            liveViewWithThumbnail.addView(view);//最顶
            View layout = liveViewWithThumbnail.findViewById(R.id.fLayout_cam_sight_setting);
            ((TextView) (view.findViewById(R.id.tv_sight_setting_content)))
                    .setText(getContext().getString(R.string.Tap1_Camera_Overlook) + ": "
                            + getContext().getString(R.string.Tap1_Camera_OverlookTips));
            view.findViewById(R.id.btn_sight_setting_cancel).setOnClickListener((View v) -> {
                if (layout != null) {
                    liveViewWithThumbnail.removeView(layout);
                }
//                basePresenter.startPlay();
                if (!isStandBy()) {
                    liveLoadingBar.changeToPlaying(canShowLoadingBar());
                }
                //需要隐藏历史录像时间轴
                boolean showSdcard = JFGRules.showSdcard(basePresenter.getDevice());
                historyParentContainer.setVisibility(showSdcard
                        ? VISIBLE : GONE);
                historyWheelContainer.setVisibility(showSdcard ? VISIBLE : INVISIBLE);
                isSightShown = false;
            });
            layout.setOnClickListener(v -> AppLogger.d("don't click me"));
            view.findViewById(R.id.btn_sight_setting_next).setOnClickListener((View v) -> {
                liveViewWithThumbnail.removeView(layout);
                Intent intent = new Intent(getContext(), SightSettingActivity.class);
                intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                getContext().startActivity(intent);
                isSightShown = false;
            });
            PreferencesUtils.putBoolean(KEY_CAM_SIGHT_SETTING + uuid, false);
        } else {
            //已经添加了
            oldLayout.setVisibility(VISIBLE);
        }
        liveLoadingBar.hideLoading();
    }

    /**
     * 历史录像条显示逻辑
     *
     * @param show
     */
    private void showHistoryWheel(boolean show) {
        //处理显示逻辑
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        //4.被分享用户不显示
        if (JFGRules.isShareDevice(device)) {
            AppLogger.d("is share device");
            historyParentContainer.setVisibility(GONE);
            return;
        }
        //3.没有历史录像
        if (/*superWheelExt.getDataProvider() != null && superWheelExt.getDataProvider().getDataCount() > 0*/ superWheelExt.getHistoryCount() > 0) {
            //显示
            AppLogger.d("has history video");
            historyParentContainer.setVisibility(VISIBLE);
            return;
        }
        historyParentContainer.setVisibility(show ? VISIBLE : GONE);
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

    private int getVideoFinalWidth() {
        if (MiscUtils.isLand()) {
            //横屏需要区分睿视
            // TODO: 2017/8/17 #118156 Android（1.1.0.535）睿视设备 OS81的鱼缸效果不正确 全屏时，不做4:3的比例 而是图像是满屏效果/(ㄒoㄒ)/~~
            if (JFGRules.isRoundRadio(device.pid)) {
                return ViewGroup.LayoutParams.MATCH_PARENT;
            } else {
                // TODO: 2017/8/18 再说吧
                if (isRSCam && device.getPid() != 81) {
                    //保持4:3
                    Log.d("isRSCam", "isRSCam....");
                    return (int) (Resources.getSystem().getDisplayMetrics().heightPixels * (float) 4 / 3);
                }
            }
            return ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            //竖屏 match
            return ViewGroup.LayoutParams.MATCH_PARENT;
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
        historyWheelHandler = getHistoryWheelHandler();
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

    private float getLandFillScreen() {
        return (float) Resources.getSystem().getDisplayMetrics().heightPixels /
                Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    /**
     * 分辨率 (float)h/w
     *
     * @param ratio
     */
    private void updateLiveViewRectHeight(float ratio) {
        liveViewWithThumbnail.updateLayoutParameters((int) (Resources.getSystem().getDisplayMetrics().widthPixels * ratio),
                getVideoFinalWidth());
    }

    public LiveViewWithThumbnail getLiveViewWithThumbnail() {
        return liveViewWithThumbnail;
    }

    private void reInitHistoryHandler() {
        if (historyWheelHandler == null) {
            historyWheelHandler = new HistoryWheelHandler(superWheelExt, uuid);
            historyWheelHandler.setDatePickerListener((time, state) -> {
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
            });
        }
    }

    public void onDeviceStandByChanged(Device device, View.OnClickListener clickListener) {
        //设置 standby view相关点击事件
        DpMsgDefine.DPStandby standby = device.$(508, new DpMsgDefine.DPStandby());
        DpMsgDefine.DPNet dpNet = device.$(201, new DpMsgDefine.DPNet());//http://yf.cylan.com.cn:82/redmine/issues/109805
        liveViewWithThumbnail.enableStandbyMode(standby.standby && dpNet.net > 0, clickListener, isShareAccount);
        boolean online = JFGRules.isDeviceOnline(dpNet);
        if (standby.standby && online && !isLand()) {
            liveLoadingBar.hideLoading();
        }
        if (!isLand()) {
            if (standby.standby) {
                historyParentContainer.setVisibility(GONE);
            } else {
                boolean showSdcard = JFGRules.showSdcard(device);
                historyParentContainer.setVisibility(showSdcard ? VISIBLE : GONE);
                historyWheelContainer.setVisibility(showSdcard ? VISIBLE : INVISIBLE);
            }
        }
        updateDoorLock();
        btnLoadHistory.setEnabled(!standby.standby && online);
    }

    private boolean isStandBy() {
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        DpMsgDefine.DPStandby standby = device.$(508, new DpMsgDefine.DPStandby());
        return standby.standby;
    }

    public void updateLiveViewMode(String mode) {
        if (device.pid == 39 || device.pid == 49) {
            mode = "0";
        }
        if (!needShowSight) {
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
            presenter.performStopVideoAction();
            ((ImageView) v).setImageResource(R.drawable.icon_landscape_stop);
        } else {
            ((ImageView) v).setImageResource(R.drawable.icon_landscape_playing);
            presenter.performPlayVideoAction();
        }
    }

    @OnClick(R.id.tv_live)
    public void onLiveClick() {
        if (CameraLiveHelper.canPlayVideoNow(uuid)) {
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
        this.enableAutoRotate = enableAutoRotate;
        if (videoView != null && videoView instanceof Panoramic360ViewRS) {
            try {
                ((Panoramic360ViewRS) videoView).enableAutoRotation(isLivePlaying() && isLive() && enableAutoRotate);
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

    public void showPlayHistoryButton() {
        historyWheelContainer.setVisibility(VISIBLE);
        historyParentContainer.setVisibility(VISIBLE);
        if (historyWheelContainer.getDisplayedChild() == 1) {
            historyWheelContainer.showPrevious();
        }
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        btnLoadHistory.setEnabled(device.$(201, new DpMsgDefine.DPNet()).net > 0);//设备在线才可点击
        if (isLand()) {
            historyWheelContainer.getCurrentView().setBackgroundColor(getResources().getColor(android.R.color.transparent));
        } else {
            historyWheelContainer.getCurrentView().setBackgroundColor(getResources().getColor(R.color.color_F7F8FA));
            vLine.setBackgroundColor(getResources().getColor(R.color.color_f2f2f2));
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

    public void updateDeviceNet() {
        updateDoorLock();
    }

    @Override
    public void onHistoryChanged(Collection<JFGVideo> history) {
        if (BuildConfig.DEBUG) {
            Log.d(CameraLiveHelper.TAG, "历史录像数据发生了变化..");
        }
        superWheelExt.setHistoryFiles(history);
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
        if (BuildConfig.DEBUG) {
            Log.d(CameraLiveHelper.TAG, "点击,需要播放状态");
        }
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
            presenter.performStopVideoAction();
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
        getContext().startActivity(intent, ActivityOptionsCompat.makeCustomAnimation(getContext(), R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
    }

    @Override
    public void clickText(View view) {

    }

    @Override
    public void onClick(FlipImageView view) {
        Device device = presenter.getDevice();
        DpMsgDefine.DPSdStatus dpSdStatus = device.$(204, new DpMsgDefine.DPSdStatus());
        int oldOption = device.$(ID_303_DEVICE_AUTO_VIDEO_RECORD, -1);
        boolean safeIsOpen = device.$(ID_501_CAMERA_ALARM_FLAG, false);
        //先判断是否关闭了自动录像,关闭了提示 :若关闭，“侦测到异常时”将不启用录像

        //若自动录像未关闭 则提示:关闭“移动侦测”，将停止“侦测报警录像”

        //无卡不需要显示 //oldOption 不等于2 说明没有关闭自动录像则提示:关闭“移动侦测”，将停止“侦测报警录像”
        if (oldOption == 0 && safeIsOpen && dpSdStatus.hasSdcard && dpSdStatus.err == 0) {
            AlertDialogManager.getInstance().showDialog((Activity) getContext(),
                    getContext().getString(R.string.Tap1_Camera_MotionDetection_OffTips),
                    getContext().getString(R.string.Tap1_Camera_MotionDetection_OffTips),
                    getContext().getString(R.string.CARRY_ON), (DialogInterface dialog, int which) -> {
                        DpMsgDefine.DPPrimary<Boolean> wFlag = new DpMsgDefine.DPPrimary<>();
                        wFlag.value = false;
                        presenter.updateInfoReq(wFlag, DpMsgMap.ID_501_CAMERA_ALARM_FLAG);
                        //关闭移动侦测的同时也关闭自动录像
                        setFlipped(true);
                        ToastUtil.showToast(getContext().getString(R.string.SCENE_SAVED));
                        if (MiscUtils.isLand()) {
                            ((BaseFullScreenFragmentActivity) getContext())
                                    .showSystemBar(false, 500);
                        }
                    }, getContext().getString(R.string.CANCEL), (dialog, which) -> {
                        if (MiscUtils.isLand()) {
                            ((BaseFullScreenFragmentActivity) getContext())
                                    .showSystemBar(false, 500);
                        }
                    });
        } else {
            safeIsOpen = device.$(ID_501_CAMERA_ALARM_FLAG, false);
            if (safeIsOpen) {
                AlertDialogManager.getInstance().showDialog((Activity) getContext(), "safeIsOpen", getContext().getString(R.string.Detection_Pop),
                        getContext().getString(R.string.OK), (dialog, which) -> {
                            DpMsgDefine.DPPrimary<Boolean> safe = new DpMsgDefine.DPPrimary<>(false);
                            presenter.updateInfoReq(safe, ID_501_CAMERA_ALARM_FLAG);
                            setFlipped(true);
                            if (MiscUtils.isLand()) {
                                ((BaseFullScreenFragmentActivity) getContext())
                                        .showSystemBar(false, 500);
                            }
                        }, getContext().getString(R.string.CANCEL), (dialog, which) -> {
                            if (MiscUtils.isLand()) {
                                ((BaseFullScreenFragmentActivity) getContext())
                                        .showSystemBar(false, 500);
                            }
                        }, false);
            } else {
                DpMsgDefine.DPPrimary<Boolean> safe = new DpMsgDefine.DPPrimary<>(true);
                presenter.updateInfoReq(safe, ID_501_CAMERA_ALARM_FLAG);
                setFlipped(false);
            }
        }
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
        DoorLockDialog doorLockDialog = DoorLockDialog.Companion.newInstance(uuid);
        doorLockDialog.setAction((id, value) -> {
            if (id == R.id.ok) {
                presenter.openDoorLock((String) value);
            }
        });
        doorLockDialog.show(getFragmentManager(), "CameraLiveFragmentEx.onDoorLockClick");
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
        return !isStandBy() && (isLoading() || hasPendingHistoryPlayAction || !isLand());
    }

    private boolean canHideLoadingBar() {
        return !isLoading() && !hasPendingHistoryPlayAction && isLivePlaying() || isStandBy();
    }

    private boolean canShowViewModeMenu() {
        return JFGRules.showSwitchModeButton(getDevice().pid) && isLivePlaying() && isLive();
    }

    private boolean canShowStreamSwitcher() {
        return isLivePlaying() && isLive() && JFGRules.showSdHd(pid, presenter.getDevice().$(207, ""), false);
    }

    private boolean canShowHistoryWheel() {
        return !JFGRules.isShareDevice(device) && JFGRules.hasSDFeature(pid);
    }

    private boolean canShowFlip() {
        return !JFGRules.isShareDevice(device) && JFGRules.hasProtection(device.pid, false);
    }

    private boolean canXunHuanEnable() {
        return isLive() && isLivePlaying() && JFGRules.showSwitchModeButton(device.pid) && enableAutoRotate;
    }

    private boolean canModeSwitchEnable() {
        return isLive() && isLivePlaying() && JFGRules.showSwitchModeButton(device.pid);
    }

    private boolean canShowBottomBannerView() {
        return isLivePlaying();
    }

    private boolean isLivePlaying() {
        return CameraLiveHelper.isVideoPlaying(presenter.getCameraLiveAction());
    }

    private boolean isLive() {
        return CameraLiveHelper.isLive(presenter.getCameraLiveAction());
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
            liveLoadingBar.animate().setDuration(ANIMATION_DURATION).alpha(isLoading() ? 1 : 0)
                    .translationY(isLoading() ? 1 : 0)
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
        liveTopBannerView.setVisibility(isLand ? VISIBLE : INVISIBLE);
        bottomControllerContainer.setVisibility(isLand ? INVISIBLE : VISIBLE);
        layoutLandFlip.setVisibility(showFlip && isLand ? VISIBLE : GONE);
        layoutPortFlip.setVisibility(showFlip && !isLand && isLivePlaying() ? VISIBLE : GONE);
        imgVCamZoomToFullScreen.setVisibility(isLand ? INVISIBLE : VISIBLE);
        imgVCamLiveLandPlay.setVisibility(isLand ? VISIBLE : GONE);
        liveBottomBannerView.setVisibility(isLivePlaying() ? VISIBLE : INVISIBLE);
        historyWheelContainer.setVisibility(canShowHistoryWheel() ? VISIBLE : INVISIBLE);
        tvLive.setVisibility(historyWheelContainer.getDisplayedChild() == 1 ? VISIBLE : GONE);
        liveViewModeContainer.setVisibility(canShowViewModeMenu() ? VISIBLE : INVISIBLE);
        vFlag.setVisibility(historyWheelContainer.getDisplayedChild() == 1 ? VISIBLE : GONE);
        ivModeXunHuan.setVisibility(JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
        svSwitchStream.setVisibility(canShowStreamSwitcher() ? VISIBLE : GONE);
        imgVCamTriggerMic.setVisibility(hasMicFeature ? VISIBLE : GONE);
        imgVLandCamTriggerMic.setVisibility(hasMicFeature ? VISIBLE : GONE);
        ivCamDoorLock.setVisibility(hasDoorLock && !isShareAccount ? VISIBLE : GONE);
        ivViewModeSwitch.setVisibility(JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
        vDivider.setVisibility(showFlip && MiscUtils.isLand() ? VISIBLE : GONE);
        liveViewWithThumbnail.getTvLiveFlow().setVisibility(isLivePlaying() ? VISIBLE : GONE);
        liveLoadingBar.showOrHide(canShowLoadingBar());
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
        imgVCamZoomToFullScreen.setEnabled(isPlaying);
        liveViewWithThumbnail.setEnabled(true);
    }

    private void performLayoutContentAction() {
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
        historyWheelContainer.setDisplayedChild(presenter.isHistoryEmpty() ? 0 : 1);
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
        if (isLand) {
            lp.removeRule(RelativeLayout.BELOW);//remove below rules
            lp.addRule(RelativeLayout.ABOVE, R.id.v_guide);//set above v_guide
//            android:layout_above="@+id/layout_d"
            glp.addRule(RelativeLayout.ABOVE, R.id.layout_e);
            liveViewWithThumbnail.updateLayoutParameters(RelativeLayout.LayoutParams.MATCH_PARENT, getVideoFinalWidth());
        } else {
            glp.addRule(RelativeLayout.ABOVE, R.id.layout_d);
            lp.removeRule(RelativeLayout.ABOVE);//remove above
            lp.addRule(RelativeLayout.BELOW, R.id.v_guide); //set below v_guide
            updateLiveViewRectHeight(portRatio == -1 ? presenter.getVideoPortHeightRatio() : portRatio);
        }
        historyParentContainer.setLayoutParams(lp);
        liveViewModeContainer.setLayoutParams(glp);
        liveViewWithThumbnail.detectOrientationChanged(!isLand);
        boolean safeIsOpen = device.$(ID_501_CAMERA_ALARM_FLAG, false);
        setFlipped(!safeIsOpen);
        decideFlippedContent();
        decideLiveThumbContent();
        decideLiveViewMode();
        enableAutoRotate(enableAutoRotate);
    }

    private void decideLiveViewMode() {
        updateLiveViewMode(device.$(509, "1"));
    }

    private void decideFlippedContent() {
        boolean safeIsOpen = device.$(ID_501_CAMERA_ALARM_FLAG, false);
        setFlipped(!safeIsOpen);
    }

    private void decideLiveThumbContent() {
        boolean no_net = NetUtils.getJfgNetType() == 0;
        btnLoadHistory.setEnabled(!no_net);
        if (no_net) {
            liveViewWithThumbnail.showBlackBackground();
            return;
        }
        if (!isLivePlaying() && isResumed() && isUserVisible() && presenter != null) {//显示缩略图
            presenter.performLoadLiveThumbPicture();
            return;
        }
        if (isLivePlaying()) {
            liveViewWithThumbnail.hideThumbPicture();
        }
    }

    private void performReLayoutAction() {
        performLayoutVisibilityAction();
        performLayoutEnableAction();
        performLayoutContentAction();
    }

    private void performTimeZoneRefresh() {
        TimeZone timeZone = JFGRules.getDeviceTimezone(device);
        liveTimeDateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.UK);
        liveTimeDateFormat.setTimeZone(timeZone);
        liveTimeLayout.setTimeZone(timeZone);
        superWheelExt.setTimeZone(timeZone);
    }

    private boolean isLoading() {
        return CameraLiveHelper.isVideoLoading(presenter.getCameraLiveAction());
    }
}