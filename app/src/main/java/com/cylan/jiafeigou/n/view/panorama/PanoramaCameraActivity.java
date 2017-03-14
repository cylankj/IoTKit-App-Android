package com.cylan.jiafeigou.n.view.panorama;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
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
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;

import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.SPEED_MODE.AUTO;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.SPEED_MODE.FLUENCY;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.SPEED_MODE.HD;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.SPEED_MODE.NORMAL;

/**
 * Created by yanzhendong on 2017/3/7.
 */

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


    private SPEED_MODE speedMode = SPEED_MODE.AUTO;
    private CONNECTION_MODE connectionMode = CONNECTION_MODE.FINE;
    private PopupWindow videoPopHint;


    @Override
    public void onShowProperty(Device device) {

    }

    @Override
    public void onViewer() {

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
        SurfaceView surfaceView = mPresenter.getViewerInstance();
        JfgCmdInsurance.getCmd().enableRenderSingleRemoteView(true, surfaceView);
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
        onNotifyBannerWithConnectionChanged();
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
            panoramaCameraQuickMenu.animate().alpha(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    panoramaCameraQuickMenu.setVisibility(View.GONE);
                }
            }).start();
        } else if (panoramaCameraQuickMenu.getVisibility() == View.GONE) {
            panoramaCameraQuickMenu.animate().alpha(1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    panoramaCameraQuickMenu.setVisibility(View.VISIBLE);
                }
            }).start();
        }
    }

    @OnLongClick(R.id.act_panorama_bottom_panel_camera_photograph)
    public boolean longClickedBottomPanelPhotoGraphItem() {
        AppLogger.d("longClickedBottomPanelPhotoGraphItem");
        return false;
    }

    @OnClick(R.id.act_panorama_bottom_panel_camera_photograph)
    public void clickedBottomPanelPhotoGraphItem() {
        AppLogger.d("clickedBottomPanelPhotoGraphItem");

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
    }

    @OnClick(R.id.act_panorama_camera_quick_menu_item2_voice)
    public void clickedQuickMenuItem2SwitchVoice() {
        AppLogger.d("clickedQuickMenuItem2SwitchVoice");
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
        bottomPanelPhotoGraphItem.setImageResource(R.drawable.camera720_icon_short_video_selector);
    }

    public void onSetPictureModeLayout() {
        bottomPanelMoreItem.setEnabled(true);
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

    public void onSetGenericLayout() {
        bottomPanelPhotoGraphItem.setImageResource(R.drawable.camera720_icon_photograph_selector);
        bottomPanelAlbumItem.setVisibility(View.VISIBLE);
        bottomPanelMoreItem.setVisibility(View.VISIBLE);
        if (bottomPanelSwitcher.getDisplayedChild() == 1) {
            bottomPanelSwitcher.showPrevious();
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

}
