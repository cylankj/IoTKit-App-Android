package com.cylan.jiafeigou.n.view.panorama;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.ActivityComponent;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.view.bell.LBatteryWarnDialog;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.photoselect.CircleImageView;
import com.cylan.jiafeigou.utils.BitmapUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.SimpleProgressBar;
import com.cylan.jiafeigou.widget.video.PanoramicView720_Ext;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;
import com.cylan.panorama.CommonPanoramicView;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static com.cylan.jiafeigou.R.id.act_panorama_camera_banner_information_connection_icon;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_202_MAC;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_205_CHARGING;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_206_BATTERY;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.CONNECTION_MODE.FINE;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.PANORAMA_RECORD_MODE.MODE_LONG;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.PANORAMA_RECORD_MODE.MODE_NONE;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.PANORAMA_RECORD_MODE.MODE_SHORT;

/**
 * Created by yanzhendong on 2017/3/7.
 */
@RuntimePermissions
public class PanoramaCameraActivity extends BaseActivity<PanoramaCameraContact.Presenter> implements PanoramaCameraContact.View, CommonPanoramicView.PanoramaEventListener {

    @BindView(R.id.act_panorama_camera_banner)
    ViewSwitcher bannerSwitcher;
    @BindView(R.id.imgv_toolbar_right)
    ImageButton setting;
    @BindView(act_panorama_camera_banner_information_connection_icon)
    ImageView bannerConnectionIcon;
    @BindView(R.id.act_panorama_camera_banner_information_connection_text)
    TextView bannerConnectionText;
    @BindView(R.id.act_panorama_camera_banner_information_charge_icon)
    ImageView bannerChargeIcon;
    @BindView(R.id.act_panorama_camera_banner_information_charge_text)
    TextView bannerChargeText;
    @BindView(R.id.act_panorama_camera_toolbar)
    FrameLayout panoramaToolBar;
    @BindView(R.id.act_panorama_camera_quick_menu_item3_content)
    TextView quickMenuItem3TextContent;
    @BindView(R.id.act_panorama_camera_quick_menu)
    LinearLayout panoramaCameraQuickMenu;
    @BindView(R.id.act_panorama_camera_flow_speed)
    TextView liveFlowSpeedText;
    @BindView(R.id.act_panorama_camera_banner_bad_net_work_configure)
    TextView bannerWarmingTitle;
    @BindView(R.id.act_panorama_camera_quick_menu_item1_mic)
    ImageView quickMenuItem1Mic;
    @BindView(R.id.act_panorama_camera_quick_menu_item2_voice)
    ImageView quickMenuItem2Voice;
    @BindView(R.id.act_panorama_camera_bottom_panel_picture)
    RadioButton bottomPanelPictureMode;
    @BindView(R.id.act_panorama_camera_bottom_panel_video)
    RadioButton bottomPanelVideoMode;
    @BindView(R.id.act_panorama_camera_bottom_panel_more)
    ImageButton bottomPanelMoreItem;
    @BindView(R.id.act_panorama_bottom_panel_camera_photograph)
    ImageButton bottomPanelPhotoGraphItem;
    @BindView(R.id.act_panorama_camera_bottom_panel_album)
    CircleImageView bottomPanelAlbumItem;
    @BindView(R.id.act_panorama_camera_bottom_panel_switcher_menu_item)
    RadioGroup bottomPanelSwitcherItem1ViewMode;
    @BindView(R.id.act_panorama_camera_bottom_panel_switcher_menu)
    ViewSwitcher bottomPanelSwitcher;
    @BindView(R.id.act_panorama_camera_bottom_panel_switcher_menu_information)
    RelativeLayout bottomPanelSwitcherItem2Information;
    @BindView(R.id.act_panorama_camera_bottom_panel_switcher_menu_information_record_time)
    TextView bottomPanelSwitcherItem2TimeText;
    @BindView(R.id.act_panorama_camera_bottom_panel_switcher_menu_information_red_dot)
    ImageView bottomPanelSwitcherItem2DotIndicator;
    @BindView(R.id.act_panorama_camera_video_container)
    FrameLayout videoLiveContainer;
    @BindView(R.id.act_panorama_camera_loading)
    SimpleProgressBar loadingBar;
    @BindView(R.id.act_panorama_camera_bottom_count_down_line)
    View bottomCountDownLine;
    @BindView(R.id.tv_top_bar_left)
    Button topLeftMenu;

    @SPEED_MODE
    private int speedMode = SPEED_MODE.AUTO;
    @CONNECTION_MODE
    private int connectionMode = FINE;
    @PANORAMA_VIEW_MODE
    private int panoramaViewMode = PANORAMA_VIEW_MODE.MODE_PICTURE;
    @PANORAMA_RECORD_MODE
    private int panoramaRecordMode = PANORAMA_RECORD_MODE.MODE_NONE;
    private PopupWindow videoPopHint;
    private PanoramicView720_Ext surfaceView;
    private ConnectionDialog connectionDialog;
    private LBatteryWarnDialog lBatteryWarnDialog;
    private AlertDialog mobileAlert;
    private boolean allowMobile = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    @Override
    public void onShowProperty(Device device) {
        int battery = device.$(ID_206_BATTERY, 0);
        bannerChargeText.setText(battery + "%");
        if (battery <= 20) {
            bannerChargeIcon.setImageResource(R.drawable.camera720_icon_electricity_low_power);
        } else if (battery > 20 && battery < 80) {
            bannerChargeIcon.setImageResource(R.drawable.camera720_icon_electricity_charge_half);
        } else if (battery >= 80) {
            bannerChargeIcon.setImageResource(R.drawable.camera720_icon_electricity_charge_full);
        }
        boolean charging = device.$(ID_205_CHARGING, false);
        if (charging) {
            bannerChargeIcon.setImageResource(R.drawable.camera720_icon_electricity_charge);
        }
        String mac = device.$(ID_202_MAC, "");
        if (mac != null) {
            String routerMac = NetUtils.getRouterMacAddress(getApplication());
            if (TextUtils.equals(mac, routerMac)) {
                //AP 模式
                bannerConnectionIcon.setImageResource(R.drawable.camera720_icon_ap);
                bannerConnectionText.setText(R.string.Tap1_OutdoorMode);
            } else {
                //非 AP 模式
                bannerConnectionIcon.setImageResource(R.drawable.camera720_icon_wifi);
                bannerConnectionText.setText(R.string.wifi_connection);
            }
        }
    }

    @Override
    public void onViewer() {
        liveFlowSpeedText.setText("0K/s");
        loadingBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDismiss() {
        if (surfaceView != null) {
            surfaceView.onDestroy();
        }
    }

    public void showLoading(String msg) {
        if (LoadingDialog.isShowing(getSupportFragmentManager())) {
            LoadingDialog.dismissLoading(getSupportFragmentManager());
        }
        LoadingDialog.showLoading(getSupportFragmentManager(), msg);
    }

    public void dismissLoading() {
        if (LoadingDialog.isShowing(getSupportFragmentManager())) {
            LoadingDialog.dismissLoading(getSupportFragmentManager());
        }
    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        if (FileUtils.isFileExist(JConstant.PANORAMA_MEDIA_THUMB_PATH + "/" + uuid + ".jpg")) {
            Glide.with(this).load(JConstant.PANORAMA_MEDIA_THUMB_PATH + "/" + uuid + ".jpg").into(bottomPanelAlbumItem);
        }
        Device device = sourceManager.getDevice(uuid);
        String alias = device.getAlias();
        topLeftMenu.setText(TextUtils.isEmpty(alias) ? getString(R.string._720PanoramicCamera) : alias);

        bottomPanelPhotoGraphItem.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    if (panoramaRecordMode == MODE_SHORT) {
                        AppLogger.d("录制短视频结束了");
                        bottomPanelPhotoGraphItem.setEnabled(false);
                        presenter.stopVideoRecord(PANORAMA_RECORD_MODE.MODE_SHORT);
                    }
                    break;
            }
            return false;
        });

    }

    @Override
    public void onSpeaker(boolean on) {
        quickMenuItem2Voice.setImageResource(on ? R.drawable.camera720_icon_voice_selector : R.drawable.camera720_icon_no_voice_selector);
    }

    @Override
    public void onMicrophone(boolean on) {
        quickMenuItem1Mic.setImageResource(on ? R.drawable.camera720_icon_talk_selector : R.drawable.camera720_icon_no_talk_selector);
    }

    @Override
    public void onResolution(JFGMsgVideoResolution resolution) throws JfgException {
        if (surfaceView == null) {
            surfaceView = (PanoramicView720_Ext) VideoViewFactory.CreateRendererExt(VideoViewFactory.RENDERER_VIEW_TYPE.TYPE_PANORAMA_720, this, true);
            surfaceView.configV720();
            surfaceView.setId("IVideoView".hashCode());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            surfaceView.setLayoutParams(params);
            surfaceView.setEventListener(this);
            videoLiveContainer.addView(surfaceView);

        }
        appCmd.enableRenderSingleRemoteView(true, surfaceView);

        loadingBar.setVisibility(View.GONE);
        if (panoramaViewMode == PANORAMA_VIEW_MODE.MODE_PICTURE) {
            bottomPanelMoreItem.setEnabled(presenter.isApiAvailable());
        } else {
            bottomPanelMoreItem.setEnabled(false);
        }
    }

    @Override
    public void onDisableControllerView() {
        bottomPanelSwitcher.setEnabled(false);
        bottomPanelPhotoGraphItem.setEnabled(false);
        bottomPanelPictureMode.setEnabled(false);
        bottomPanelVideoMode.setEnabled(false);
        bottomPanelMoreItem.setEnabled(false);
    }

    @Override
    public void onEnableControllerView() {
        bottomPanelSwitcher.setEnabled(true);
        bottomPanelPhotoGraphItem.setEnabled(true);
        bottomPanelPictureMode.setEnabled(true);
        bottomPanelVideoMode.setEnabled(true);
        bottomPanelMoreItem.setEnabled(true);
    }

    @Override
    public void onShowPreviewPicture(String picture) {

    }

    @Override
    public void onFlowSpeed(int speed) {
        liveFlowSpeedText.setText(MiscUtils.getByteFromBitRate(speed));
    }

    @Override
    public void onConnectDeviceTimeOut() {
        onShowBadNetWorkBanner();
    }

    @Override
    public void onVideoDisconnect(int code) {
        onShowDeviceOfflineBanner();
    }

    @Override
    public void onDeviceUnBind() {
        AppLogger.d("当前设备已解绑" + uuid);
        presenter.cancelViewer();
        AlertDialog.Builder builder = AlertDialogManager.getInstance().getCustomDialog(getActivityContext());
        builder.setPositiveButton(getString(R.string.OK), (dialog, which) -> {
            finish();
            Intent intent = new Intent(this, NewHomeActivity.class);
            startActivity(intent);
        })
                .setMessage(getString(R.string.Tap1_device_deleted));
        AlertDialogManager.getInstance().showDialog("onDeviceUnBind", this, builder);
    }

    @Override
    public void onLoading(boolean loading) {

    }

    @Override
    public void onShowVideoPreviewPicture(String picture) {

    }

    @Override
    public void hasNoAudioPermission() {

    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_panorama_camera;
    }

    @Override
    protected void onStart() {
        super.onStart();
        ViewUtils.setViewPaddingStatusBar(panoramaToolBar);

        int netType = NetUtils.getNetType(this);
        if (netType == ConnectivityManager.TYPE_MOBILE && !allowMobile) {
            onNetWorkChangedToMobile();
        } else {
            presenter.startViewer();
        }
        setViewModeAndRecordLayout();
        presenter.checkAndInitRecord();
        onDisableControllerView();
    }

    public void setViewModeAndRecordLayout() {
        if (bottomPanelSwitcher.getDisplayedChild() == 1) {//1
            if (panoramaViewMode == PANORAMA_VIEW_MODE.MODE_PICTURE || panoramaRecordMode == MODE_NONE) {
                bottomPanelSwitcher.showPrevious();
            }
        } else {//0
            if (panoramaViewMode == PANORAMA_VIEW_MODE.MODE_VIDEO && panoramaRecordMode != MODE_NONE) {
                bottomPanelSwitcher.showNext();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ViewUtils.clearViewPaddingStatusBar(panoramaToolBar);
        presenter.dismiss();
    }

    @Override
    protected void setActivityComponent(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    public boolean muteAudio(boolean bMute) {
        boolean isSuccess;
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (bMute) {
            int result = am.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            isSuccess = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        } else {
            int result = am.abandonAudioFocus(null);
            isSuccess = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        }
        AppLogger.e("pauseMusic bMute=" + bMute + " result=" + isSuccess);
        return isSuccess;
    }


    @OnClick(R.id.act_panorama_camera_bottom_panel_more)
    public void clickedBottomPanelMoreItem() {
        AppLogger.d("clickedBottomPanelMoreItem");
        if (panoramaCameraQuickMenu.getVisibility() == View.VISIBLE) {
            animatedPopMenu(false);
        } else if (panoramaCameraQuickMenu.getVisibility() == View.GONE) {
            animatedPopMenu(true);
        }

    }

    private void animatedPopMenu(boolean show) {
        if (show) {
            panoramaCameraQuickMenu.animate().alpha(1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    panoramaCameraQuickMenu.setVisibility(View.VISIBLE);
                }
            }).start();
        } else {
            panoramaCameraQuickMenu.animate().alpha(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    panoramaCameraQuickMenu.setVisibility(View.GONE);
                }
            }).start();
        }
    }

    @OnLongClick(R.id.act_panorama_bottom_panel_camera_photograph)
    public boolean longClickedBottomPanelPhotoGraphItem() {
        AppLogger.d("longClickedBottomPanelPhotoGraphItem");
        if (panoramaViewMode == PANORAMA_VIEW_MODE.MODE_VIDEO && panoramaRecordMode == PANORAMA_RECORD_MODE.MODE_NONE) {
            presenter.startVideoRecord(PANORAMA_RECORD_MODE.MODE_SHORT);
        }
        hideVideoModePop();
        return true;
    }

    @OnClick(R.id.act_panorama_bottom_panel_camera_photograph)
    public void clickedBottomPanelPhotoGraphItem() {

        if (panoramaViewMode == PANORAMA_VIEW_MODE.MODE_PICTURE) {
            AppLogger.d("将进行拍照");
            bottomPanelPhotoGraphItem.setEnabled(false);//防止重复点击
            presenter.makePhotograph();
        } else if (panoramaRecordMode == PANORAMA_RECORD_MODE.MODE_NONE) {
            bottomPanelPhotoGraphItem.setEnabled(false);//防止重复点击
            presenter.startVideoRecord(panoramaRecordMode = PANORAMA_RECORD_MODE.MODE_LONG);
        } else if (panoramaRecordMode == PANORAMA_RECORD_MODE.MODE_LONG) {
            showLoading("视频处理中,请稍后");
            bottomPanelPhotoGraphItem.setEnabled(false);//防止重复点击
            presenter.stopVideoRecord(panoramaRecordMode);
        }
        //此时下面两个选项不允许切换,因为操作还未完成
        bottomPanelPictureMode.setEnabled(false);
        bottomPanelVideoMode.setEnabled(false);
        hideVideoModePop();
    }

    @OnClick(R.id.act_panorama_camera_bottom_panel_album)
    public void clickedBottomPanelAlbumItem() {
        AppLogger.d("clickedBottomPanelAlbumItem");
        Intent intent = new Intent(this, PanoramaAlbumActivity.class);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        startActivity(intent);
    }

    @OnClick(R.id.act_panorama_camera_bottom_panel_picture)
    public void switchViewerModeToPicture() {
        AppLogger.d("switchViewerModeToPicture");
        hideVideoModePop();
        onSetViewModeLayout(PANORAMA_VIEW_MODE.MODE_PICTURE);
    }

    @OnClick(R.id.act_panorama_camera_bottom_panel_video)
    public void switchViewerModeToVideo() {
        AppLogger.d("switchViewerModeToVideo");
        if (PreferencesUtils.getBoolean(JConstant.KEY_PANORAMA_POP_HINT, true)) {//只提示一次
            PreferencesUtils.putBoolean(JConstant.KEY_PANORAMA_POP_HINT, false);
            showVideoModePop();
        }
        onSetViewModeLayout(PANORAMA_VIEW_MODE.MODE_VIDEO);
    }

    public void showVideoModePop() {
        if (videoPopHint == null) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_panorama_pop_hint, null);
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            videoPopHint = new PopupWindow(view, view.getMeasuredWidth(), view.getMeasuredHeight());
            videoPopHint.setFocusable(false);
            videoPopHint.setOutsideTouchable(false);
            videoPopHint.setTouchable(false);
            videoPopHint.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        int xPos = (int) ((videoPopHint.getWidth() - bottomPanelVideoMode.getWidth()) / 2 + getResources().getDimension(R.dimen.y2));
        int yPos = (int) (videoPopHint.getHeight() + getResources().getDimension(R.dimen.y2) + bottomPanelVideoMode.getHeight());
        videoPopHint.showAsDropDown(bottomPanelVideoMode, -xPos, -yPos);
    }

    public boolean hideVideoModePop() {
        if (videoPopHint != null && videoPopHint.isShowing()) {
            videoPopHint.dismiss();
            return true;
        }
        return false;
    }

    @OnClick(R.id.imgv_toolbar_right)
    public void clickedToolBarSettingMenu() {
        AppLogger.d("clickedSettingMenu");
        hideVideoModePop();
        presenter.dismiss();
        startActivity(new Intent(this, PanoramaSettingActivity.class));
    }

    @Override
    @OnClick(R.id.tv_top_bar_left)
    public void onBackPressed() {
        if (!hideVideoModePop()) {
            super.onBackPressed();
        }
        AppLogger.d("clickedToolBarBackMenu");
    }

    @Override
    protected void onPrepareToExit(Action action) {
        presenter.dismiss();
        if (surfaceView != null) {
            surfaceView.onPause();
            videoLiveContainer.removeAllViews();
            surfaceView = null;
            muteAudio(false);
        }
        action.actionDone();
    }

    @OnClick(R.id.act_panorama_camera_banner_bad_net_work_close)
    public void clickedCloseBadNetWorkBanner() {
        AppLogger.d("clickedCloseBadNetWorkBanner");
        onHideBadNetWorkBanner();
    }

    @OnClick(R.id.act_panorama_camera_banner_bad_net_work_configure)
    public void clickedConfigureNetWorkBanner() {
        AppLogger.d("clickedConfigureNetWorkBanner");
        showConfigConnectionDialog();
    }


    @OnClick(R.id.act_panorama_camera_quick_menu_item1_mic)
    public void clickedQuickMenuItem1SwitchMic() {
        AppLogger.d("clickedQuickMenuItem1SwitchMic");
        PanoramaCameraActivityPermissionsDispatcher.switchMicroPhoneWithPermissionWithCheck(this);
    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    void switchSpeakerWithPermission() {
        presenter.switchSpeaker();
    }

    @OnShowRationale(Manifest.permission.RECORD_AUDIO)
    public void showEnablePermissionAlert(PermissionRequest request) {
        AppLogger.d("需要提醒用户给 APP 授权");
    }

    @OnPermissionDenied(Manifest.permission.RECORD_AUDIO)
    public void onNoAudioPermission() {
        AppLogger.d("没有控制扬声器和麦克风的权限");
    }


    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    void switchMicroPhoneWithPermission() {
        presenter.switchMicrophone();
    }

    @OnClick(R.id.act_panorama_camera_quick_menu_item2_voice)
    public void clickedQuickMenuItem2SwitchVoice() {
        AppLogger.d("clickedQuickMenuItem2SwitchVoice");
        PanoramaCameraActivityPermissionsDispatcher.switchSpeakerWithPermissionWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PanoramaCameraActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnClick(R.id.act_panorama_camera_quick_menu_item3_left)
    public void clickedQuickMenuItem3Left() {
        AppLogger.d("clickedQuickMenuItem3Left");
        presenter.switchVideoResolution(move(speedMode, false));
    }

    private int move(@SPEED_MODE int mode, boolean next) {
        switch (mode) {
            case SPEED_MODE.NORMAL:
                return next ? SPEED_MODE.HD : SPEED_MODE.HD;
            case SPEED_MODE.HD:
                return next ? SPEED_MODE.NORMAL : SPEED_MODE.NORMAL;
            case SPEED_MODE.AUTO:
                return next ? SPEED_MODE.NORMAL : SPEED_MODE.HD;
            case SPEED_MODE.FLUENCY:
                return next ? SPEED_MODE.NORMAL : SPEED_MODE.HD;
            default:
                return SPEED_MODE.HD;
        }
    }

    @OnClick(R.id.act_panorama_camera_quick_menu_item3_right)
    public void clickedQuickMenuItem3Right() {
        AppLogger.d("clickedQuickMenuItem3Right");
        presenter.switchVideoResolution(move(speedMode, true));
    }

    public void onShowBadNetWorkBanner() {
        AppLogger.d("onShowBadNetWorkBanner");
        int childIndex = bannerSwitcher.getDisplayedChild();
        if (childIndex == 0) {
            bannerSwitcher.showNext();
        }
        bannerWarmingTitle.setText("无网络连接，请检查网络设置");
    }

    public void onShowDeviceOfflineBanner() {
        AppLogger.d("onShowBadNetWorkBanner");
        int childIndex = bannerSwitcher.getDisplayedChild();
        if (childIndex == 0) {
            bannerSwitcher.showNext();
        }
        bannerWarmingTitle.setText("设备离线，请重新配置连接>>");
    }

    public void onSetViewModeLayout(@PANORAMA_VIEW_MODE int mode) {
        panoramaRecordMode = MODE_NONE;
        bottomPanelAlbumItem.setVisibility(View.VISIBLE);
        bottomPanelMoreItem.setVisibility(View.VISIBLE);
        bottomPanelPhotoGraphItem.setEnabled(true);
        bottomCountDownLine.setVisibility(View.GONE);
        bottomPanelPictureMode.setEnabled(true);
        bottomPanelVideoMode.setEnabled(true);
        if (bottomPanelSwitcher.getDisplayedChild() == 1) {
            bottomPanelSwitcher.showPrevious();
        }

        switch (panoramaViewMode = mode) {
            case PANORAMA_VIEW_MODE.MODE_VIDEO:
                bottomPanelMoreItem.setEnabled(false);
                bottomPanelPhotoGraphItem.setImageResource(R.drawable.camera720_icon_short_video_selector);
                animatedPopMenu(false);
                break;
            case PANORAMA_VIEW_MODE.MODE_PICTURE:
                bottomPanelMoreItem.setEnabled(presenter.isApiAvailable());
                bottomPanelPhotoGraphItem.setImageResource(R.drawable.camera720_icon_photograph_selector);
                break;
        }
    }

    public void onSetVideoRecordLayout(@PANORAMA_RECORD_MODE int mode) {
        panoramaViewMode = PANORAMA_VIEW_MODE.MODE_VIDEO;
        bottomPanelAlbumItem.setVisibility(View.GONE);
        bottomPanelMoreItem.setVisibility(View.GONE);
        bottomPanelPhotoGraphItem.setEnabled(true);
        bottomPanelSwitcherItem1ViewMode.check(R.id.act_panorama_camera_bottom_panel_video);
        if (bottomPanelSwitcher.getDisplayedChild() == 0) {
            bottomPanelSwitcher.showNext();
        }
        switch (panoramaRecordMode = mode) {
            case MODE_LONG:
                bottomPanelSwitcherItem2DotIndicator.setVisibility(View.VISIBLE);
                bottomCountDownLine.setVisibility(View.GONE);
                bottomPanelPhotoGraphItem.setImageResource(R.drawable.camera720_icon_video_recording_selector);
                break;
            case MODE_SHORT:
                bottomPanelSwitcherItem2DotIndicator.setVisibility(View.GONE);
                bottomCountDownLine.setVisibility(View.VISIBLE);
                bottomCountDownLine.setScaleX(1.0F);
                bottomPanelPhotoGraphItem.setImageResource(R.drawable.camera720_icon_short_video_selector);
                break;
        }
    }

    public void onHideBadNetWorkBanner() {
        AppLogger.d("onHideBadNetWorkBanner");
        int childIndex = bannerSwitcher.getDisplayedChild();
        if (childIndex == 1) {
            bannerSwitcher.showPrevious();
        }
    }

    @Override
    public void onBellBatteryDrainOut() {
        if (JFGRules.isShareDevice(uuid)) return;
        if (lBatteryWarnDialog == null)
            lBatteryWarnDialog = LBatteryWarnDialog.newInstance(null);
        if (!lBatteryWarnDialog.isAdded())
            lBatteryWarnDialog.show(getSupportFragmentManager(), "lBattery");
    }

    @Override
    public void onNetWorkChangedToMobile() {
        AppLogger.d("正在使用移动网络,请注意流量");
        if (mobileAlert == null) {
            mobileAlert = new AlertDialog.Builder(this)
                    .setMessage(R.string.Tap1_Firmware_DataTips)
                    .setPositiveButton(R.string.OK, (dialog, which) -> {
                        presenter.startViewer();
                    })
                    .setNegativeButton(R.string.CANCEL, null)
                    .create();
        }
        if (!mobileAlert.isShowing() && !allowMobile) {
            mobileAlert.show();
        }
    }

    @Override
    public void onNetWorkChangedToWiFi() {
        AppLogger.d("正在使用 WiFi 网络,可以放心观看");
        if (mobileAlert != null && mobileAlert.isShowing()) {
            mobileAlert.dismiss();
            presenter.startViewer();
        }
    }

    public void showConfigConnectionDialog() {
        if (connectionDialog == null) {
            connectionDialog = ConnectionDialog.newInstance(uuid);
        }
        if (!connectionDialog.isVisible()) {
            connectionDialog.show(getSupportFragmentManager(), ConnectionDialog.class.getSimpleName());
        }
    }

    public void onSwitchSpeedMode(@SPEED_MODE int mode) {
        switch (this.speedMode = mode) {
            case SPEED_MODE.AUTO:
                quickMenuItem3TextContent.setText("速率:自动");
                break;
            case SPEED_MODE.FLUENCY:
                quickMenuItem3TextContent.setText("速率:流畅");
                break;
            case SPEED_MODE.NORMAL:
                quickMenuItem3TextContent.setText("速率:标清");
                break;
            case SPEED_MODE.HD:
                quickMenuItem3TextContent.setText("速率:高清");
                break;
        }
    }

    @Override
    public void onRefreshVideoRecordUI(int second, @PANORAMA_RECORD_MODE int type) {
        switch (type) {
            case PANORAMA_RECORD_MODE.MODE_LONG:
                bottomPanelSwitcherItem2TimeText.setText(TimeUtils.getHHMMSS(second * 1000L));
                int visibility = bottomPanelSwitcherItem2DotIndicator.getVisibility();
                bottomPanelSwitcherItem2DotIndicator.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;
            case PANORAMA_RECORD_MODE.MODE_SHORT:
                int countDown = 8 - second;
                bottomPanelSwitcherItem2TimeText.setText(countDown + "S");
                bottomCountDownLine.animate().scaleX(countDown * 1.0F / 8).start();
                break;
            default:
                break;
        }
    }

    @Override
    public void onMakePhotoGraphSuccess() {
        bottomPanelPhotoGraphItem.setEnabled(true);
        bottomPanelPictureMode.setEnabled(true);
        bottomPanelVideoMode.setEnabled(true);
        ToastUtil.showPositiveToast("拍照成功!");
        //这里不去加载设备上的缩略图,因为可能是在公网环境下,无法加载缩略图
        if (surfaceView != null) {
            surfaceView.takeSnapshot(true);
        }
    }

    @Override
    public void onStartVideoRecordSuccess(int type) {
        switch (this.panoramaRecordMode = type) {
            case PANORAMA_RECORD_MODE.MODE_LONG:
                panoramaViewMode = PANORAMA_VIEW_MODE.MODE_VIDEO;
                bottomPanelPhotoGraphItem.setEnabled(true);
                onSetVideoRecordLayout(MODE_LONG);
                break;
            case PANORAMA_RECORD_MODE.MODE_SHORT:
                onSetVideoRecordLayout(MODE_SHORT);
                break;
        }
        setting.setEnabled(false);//开始录制后设置不可点击
    }

    @Override
    public void onStartVideoRecordError(@PANORAMA_RECORD_MODE int type, int ret) {
        onReportError(ret);
        switch (this.panoramaRecordMode = type) {
            case PANORAMA_RECORD_MODE.MODE_LONG:
                ToastUtil.showNegativeToast("开始录制长视频失败");
                break;
            case PANORAMA_RECORD_MODE.MODE_SHORT:
                ToastUtil.showNegativeToast("开始录制短视频失败");
                break;
        }
        onSetViewModeLayout(PANORAMA_VIEW_MODE.MODE_VIDEO);
    }

    @Override
    public void onStopVideoRecordSuccess(@PANORAMA_RECORD_MODE int type) {
        switch (type) {
            case PANORAMA_RECORD_MODE.MODE_LONG:
                bottomPanelSwitcherItem2DotIndicator.setVisibility(View.VISIBLE);
                ToastUtil.showPositiveToast("录制长视频成功了!");
                break;
            case PANORAMA_RECORD_MODE.MODE_SHORT:
                ToastUtil.showPositiveToast("短视频录制成功");
                break;
        }
        dismissLoading();
        onSetViewModeLayout(PANORAMA_VIEW_MODE.MODE_VIDEO);
        setting.setEnabled(true);
    }

    @Override
    public void onStopVideoRecordError(int type, int ret) {
        dismissLoading();
        AppLogger.d("结束录制视频失败");
        if (ret == -2) {
            //录像时间低于3秒
            ToastUtil.showNegativeToast("低于3秒的录像将不会保存");
        } else {
            ToastUtil.showNegativeToast("录制视频失败了!");
        }
        onSetViewModeLayout(PANORAMA_VIEW_MODE.MODE_VIDEO);
        setting.setEnabled(true);
    }

    @Override
    public void onReportError(int err) {
        switch (err) {
            case 150://低电量
                AppLogger.d("设备电量过低");
                bottomPanelPhotoGraphItem.setEnabled(true);
                ToastUtil.showNegativeToast("设备电量过低");
                break;
            case 2003://sd 卡没有容量
                AppLogger.d("SD 卡内存已满");
                bottomPanelPhotoGraphItem.setEnabled(true);
                ToastUtil.showNegativeToast("SD卡存储空间已满");
                break;
            case 2004://没有 sd 卡
                AppLogger.d("未检测到 SD 卡");
                bottomPanelPhotoGraphItem.setEnabled(true);
                ToastUtil.showNegativeToast("未检测到SD卡");
                break;
            case 2007://正在录像
                break;
            case 2008://sd 卡正在格式化
                break;
            case 2022://sd卡识别失败，需要格式化
                break;
            case -1:
                break;
        }
        bottomPanelPhotoGraphItem.setEnabled(true);
    }

    @Override
    public void onSingleTap(float v, float v1) {

    }

    @Override
    public void onSnapshot(Bitmap bitmap, boolean b) {
        bottomPanelAlbumItem.setImageBitmap(bitmap);
        BitmapUtils.saveBitmap2file(bitmap, JConstant.PANORAMA_MEDIA_THUMB_PATH + "/" + uuid + ".jpg");
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(bottomPanelAlbumItem, "scaleX", 1.0f, 1.2f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(bottomPanelAlbumItem, "scaleY", 1.0f, 1.2f, 1.0f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(200);
        set.start();
    }
}
