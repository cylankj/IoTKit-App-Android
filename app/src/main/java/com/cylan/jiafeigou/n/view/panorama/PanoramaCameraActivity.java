package com.cylan.jiafeigou.n.view.panorama;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
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

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.JFGCameraDevice;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.SimpleProgressBar;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.SPEED_MODE.AUTO;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.SPEED_MODE.FLUENCY;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.SPEED_MODE.HD;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.SPEED_MODE.NORMAL;

/**
 * Created by yanzhendong on 2017/3/7.
 */
@RuntimePermissions
public class PanoramaCameraActivity extends BaseActivity<PanoramaCameraContact.Presenter> implements PanoramaCameraContact.View {

    @BindView(R.id.act_panorama_camera_banner)
    ViewSwitcher bannerSwitcher;
    @BindView(R.id.imgv_toolbar_right)
    ImageButton setting;
    @BindView(R.id.act_panorama_camera_banner_information_connection_icon)
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
    ImageButton bottomPanelAlbumItem;
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

    private SPEED_MODE speedMode = SPEED_MODE.AUTO;
    private CONNECTION_MODE connectionMode = CONNECTION_MODE.FINE;
    private PANORAMA_VIEW_MODE panoramaViewMode = PANORAMA_VIEW_MODE.MODE_PICTURE;
    private PopupWindow videoPopHint;


    @Override
    public void onShowProperty(JFGCameraDevice device) {
        if (device.charging != null && device.charging.value) {
            bannerChargeIcon.setImageResource(R.drawable.camera720_icon_electricity_charge);
        } else if (device.battery != null) {
            bannerChargeText.setText(device.battery.value + "%");
            int battery = device.battery.value;
            if (battery <= 20) {
                bannerChargeIcon.setImageResource(R.drawable.camera720_icon_electricity_low_power);
            } else if (battery > 20 && battery < 80) {
                bannerChargeIcon.setImageResource(R.drawable.camera720_icon_electricity_charge_half);
            } else if (battery >= 80) {
                bannerChargeIcon.setImageResource(R.drawable.camera720_icon_electricity_charge_full);
            }
        }
    }

    @Override
    public void onViewer() {
        bottomPanelPhotoGraphItem.setEnabled(false);
        bottomPanelMoreItem.setEnabled(false);
        bottomPanelPictureMode.setEnabled(false);
        bottomPanelVideoMode.setEnabled(false);
        loadingBar.setVisibility(View.VISIBLE);
        if (bottomPanelSwitcher.getDisplayedChild() == 1) {
            bottomPanelSwitcher.showPrevious();
        }
    }

    @Override
    public void onDismiss() {

    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
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
        SurfaceView surfaceView = (SurfaceView) VideoViewFactory.CreateRendererExt(false, this, true);
        surfaceView.setId("IVideoView".hashCode());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        surfaceView.setLayoutParams(params);
        videoLiveContainer.addView(surfaceView);
        JfgCmdInsurance.getCmd().enableRenderSingleRemoteView(true, surfaceView);

        //enable views
        bottomPanelSwitcher.setEnabled(true);
        bottomPanelPhotoGraphItem.setEnabled(true);
        bottomPanelPictureMode.setEnabled(true);
        bottomPanelVideoMode.setEnabled(true);
        loadingBar.setVisibility(View.GONE);
        if (panoramaViewMode == PANORAMA_VIEW_MODE.MODE_PICTURE) {
            bottomPanelMoreItem.setEnabled(true);
        } else {
            bottomPanelMoreItem.setEnabled(false);
        }
    }

    @Override
    public void onFlowSpeed(int speed) {
        AppLogger.d("onFlowSpeed:" + MiscUtils.getByteFromBitRate(speed));
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
        AppLogger.d("当前设备已解绑" + mUUID);
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
    protected PanoramaCameraContact.Presenter onCreatePresenter() {
        return new PanoramaPresenter();
    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_panorama_camera;
    }

    @Override
    protected void onStart() {
        super.onStart();
        ViewUtils.setViewPaddingStatusBar(panoramaToolBar);
        connectionMode = CONNECTION_MODE.DEVICE_OFFLINE;
        mPresenter.startViewer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ViewUtils.clearViewPaddingStatusBar(panoramaToolBar);
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
        if (panoramaViewMode == PANORAMA_VIEW_MODE.MODE_VIDEO) {
            onSetShortVideoRecordLayout();
            return true;
        } else {
            return false;
        }
    }

    @OnClick(R.id.act_panorama_bottom_panel_camera_photograph)
    public void clickedBottomPanelPhotoGraphItem() {
        AppLogger.d("clickedBottomPanelPhotoGraphItem");
        if (panoramaViewMode == PANORAMA_VIEW_MODE.MODE_PICTURE) {
            AppLogger.d("将进行拍照");
        } else {
            AppLogger.d("将进行长录像");
            onSetLongVideoRecordLayout();
        }
    }

    @OnClick(R.id.act_panorama_camera_bottom_panel_album)
    public void clickedBottomPanelAlbumItem() {
        AppLogger.d("clickedBottomPanelAlbumItem");
        Intent intent = new Intent(this, PanoramaAlbumActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.act_panorama_camera_bottom_panel_picture)
    public void switchViewerModeToPicture() {
        AppLogger.d("switchViewerModeToPicture");
        hideVideoModePop();
        onSetPictureModeLayout();
    }

    @OnClick(R.id.act_panorama_camera_bottom_panel_video)
    public void switchViewerModeToVideo() {
        AppLogger.d("switchViewerModeToVideo");
        showVideoModePop();
        onSetVideoModeLayout();
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
        PanoramaSettingFragment fragment = PanoramaSettingFragment.newInstance(mUUID);
        ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(), fragment, android.R.id.content);
    }

    @Override
    @OnClick(R.id.tv_top_bar_left)
    public void onBackPressed() {
        if (!hideVideoModePop()) {
            super.onBackPressed();
        }
        AppLogger.d("clickedToolBarBackMenu");
    }

    @OnClick(R.id.act_panorama_camera_banner_bad_net_work_close)
    public void clickedCloseBadNetWorkBanner() {
        AppLogger.d("clickedCloseBadNetWorkBanner");
        onHideBadNetWorkBanner();
    }

    @OnClick(R.id.act_panorama_camera_banner_bad_net_work_configure)
    public void clickedConfigureNetWorkBanner() {
        AppLogger.d("clickedConfigureNetWorkBanner");
        if (connectionMode == CONNECTION_MODE.BAD_NETWORK) {
            AppLogger.d("无网络连接,将进入网络设置界面");
        } else if (connectionMode == CONNECTION_MODE.DEVICE_OFFLINE) {
            AppLogger.d("当前设备离线,可配置 AP 直联模式");

        }
    }


    @OnClick(R.id.act_panorama_camera_quick_menu_item1_mic)
    public void clickedQuickMenuItem1SwitchMic() {
        AppLogger.d("clickedQuickMenuItem1SwitchMic");
        PanoramaCameraActivityPermissionsDispatcher.switchMicroPhoneWithPermissionWithCheck(this);
    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    void switchSpeakerWithPermission() {
        mPresenter.switchSpeaker();
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
        mPresenter.switchMicrophone();
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
        onSwitchSpeedMode(move(speedMode, false));
    }

    private SPEED_MODE move(SPEED_MODE mode, boolean next) {
        switch (mode) {
            case NORMAL:
                return next ? HD : FLUENCY;
            case HD:
                return next ? AUTO : NORMAL;
            case AUTO:
                return next ? FLUENCY : HD;
            case FLUENCY:
                return next ? NORMAL : AUTO;
            default:
                return AUTO;
        }
    }

    @OnClick(R.id.act_panorama_camera_quick_menu_item3_right)
    public void clickedQuickMenuItem3Right() {
        AppLogger.d("clickedQuickMenuItem3Right");
        onSwitchSpeedMode(move(speedMode, true));
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

    public void onSetVideoModeLayout() {
        bottomPanelMoreItem.setEnabled(false);
        panoramaViewMode = PANORAMA_VIEW_MODE.MODE_VIDEO;
        bottomPanelPhotoGraphItem.setImageResource(R.drawable.camera720_icon_short_video_selector);
        animatedPopMenu(false);
    }

    public void onSetPictureModeLayout() {
        bottomPanelMoreItem.setEnabled(true);
        panoramaViewMode = PANORAMA_VIEW_MODE.MODE_PICTURE;
        bottomPanelPhotoGraphItem.setImageResource(R.drawable.camera720_icon_photograph_selector);
    }

    public void onNotifyBannerWithConnectionChanged() {
        switch (connectionMode) {
            case FINE:
                break;
            case DEVICE_OFFLINE:
                onShowDeviceOfflineBanner();
                break;
            case BAD_NETWORK:
                onShowBadNetWorkBanner();
                break;
        }
    }

    public void onSetNoNetWorkLayout() {
        onShowBadNetWorkBanner();
        bottomPanelMoreItem.setEnabled(false);
        bottomPanelPictureMode.setEnabled(false);
        bottomPanelVideoMode.setEnabled(false);
        bottomPanelPhotoGraphItem.setEnabled(false);
    }

    public void onSetShortVideoRecordLayout() {
        bottomPanelPhotoGraphItem.setImageResource(R.drawable.camera720_icon_short_video_selector);
        bottomPanelAlbumItem.setVisibility(View.GONE);
        bottomPanelMoreItem.setVisibility(View.GONE);
        if (bottomPanelSwitcher.getDisplayedChild() == 0) {
            bottomPanelSwitcher.showNext();
        }
    }

    public void onSetLongVideoRecordLayout() {
        bottomPanelPhotoGraphItem.setImageResource(R.drawable.camera720_icon_video_recording_selector);
        bottomPanelAlbumItem.setVisibility(View.GONE);
        bottomPanelMoreItem.setVisibility(View.GONE);
        if (bottomPanelSwitcher.getDisplayedChild() == 0) {
            bottomPanelSwitcher.showNext();
        }
    }

    public void onHideBadNetWorkBanner() {
        AppLogger.d("onHideBadNetWorkBanner");
        int childIndex = bannerSwitcher.getDisplayedChild();
        if (childIndex == 1) {
            bannerSwitcher.showPrevious();
        }
    }

    public void onSwitchSpeedMode(SPEED_MODE mode) {
        this.speedMode = mode;
        switch (speedMode) {
            case AUTO:
                quickMenuItem3TextContent.setText("速率:自动");
                break;
            case FLUENCY:
                quickMenuItem3TextContent.setText("速率:流畅");
                break;
            case NORMAL:
                quickMenuItem3TextContent.setText("速率:标清");
                break;
            case HD:
                quickMenuItem3TextContent.setText("速率:高清");
                break;
        }
    }

    @Override
    public void onSDCardUnMounted() {
        AppLogger.d("未检测到 SD 卡");
    }

    @Override
    public void onSDCardMemoryFull() {
        AppLogger.d("SD 卡内存已满");
    }

    @Override
    public void onDeviceBatteryLow() {
        AppLogger.d("设备电量低");
    }

    @Override
    public void onUpdateRecordTime(int second) {
        AppLogger.d("正在更新录像时间(长视频或者短视频)");
    }

    public void refreshLayout() {

    }
}
