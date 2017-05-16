package com.cylan.jiafeigou.n.view.panorama;

import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.ActivityComponent;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.video.PanoramicView720_Ext;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;
import com.cylan.panorama.Panoramic720View;
import com.cylan.player.JFGPlayer;
import com.danikula.videocache.HttpProxyCacheServer;
import com.lzy.okserver.download.DownloadInfo;
import com.lzy.okserver.download.DownloadManager;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by yanzhendong on 2017/3/16.
 */

public class PanoramaDetailActivity extends BaseActivity<PanoramaDetailContact.Presenter> implements JFGPlayer.JFGPlayerCallback {

    @BindView(R.id.act_panorama_detail_content_container)
    FrameLayout panoramaContentContainer;
    @BindView(R.id.act_panorama_detail_bottom_panel_switcher)
    ViewSwitcher panoramaPanelSwitcher;
    @BindView(R.id.act_panorama_detail_bottom_video_menu_seek_bar)
    SeekBar panoramaPanelSeekBar;
    @BindView(R.id.act_panorama_detail_header_title)
    RelativeLayout headerTitleContainer;
    @BindView(R.id.act_panorama_detail_bottom_picture_menu_vr)
    ImageButton bottomPictureMenuVR;
    @BindView(R.id.act_panorama_detail_bottom_video_menu_vr)
    ImageButton bottomVideoMenuVR;
    @BindView(R.id.act_panorama_detail_bottom_picture_menu_gyroscope)
    ImageButton bottomPictureMenuGyroscope;
    @BindView(R.id.act_panorama_detail_bottom_video_menu_gyroscope)
    ImageButton bottomVideoMenuGyroscope;
    @BindView(R.id.act_panorama_detail_bottom_picture_menu_panorama)
    ImageButton bottomPictureMenuMode;
    @BindView(R.id.act_panorama_detail_bottom_video_menu_panorama)
    ImageButton bottomVideoMenuMode;
    @Inject
    HttpProxyCacheServer httpProxy;
    private PanoramicView720_Ext panoramicView720Ext;
    private long player;
    private PanoramaAlbumContact.PanoramaItem panoramaItem;
    private DownloadInfo downloadInfo;


    public static Intent getIntent(Context context, String uuid, PanoramaAlbumContact.PanoramaItem item) {
        Intent intent = new Intent(context, PanoramaDetailActivity.class);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        intent.putExtra("panorama_item", item);
        return intent;
    }

    @Override
    protected void setActivityComponent(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_panorama_detail;
    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        panoramaItem = getIntent().getParcelableExtra("panorama_item");
        panoramaPanelSeekBar.setMax(100);
        initPanoramaView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initPanoramaContent(panoramaItem);
        ViewUtils.setViewPaddingStatusBar(headerTitleContainer);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ViewUtils.clearViewPaddingStatusBar(headerTitleContainer);
        if (player != 0) {
            JFGPlayer.StopRender(player);
            JFGPlayer.Stop(player);
        }
    }

    private void initPanoramaView() {
        panoramicView720Ext = (PanoramicView720_Ext) VideoViewFactory.CreateRendererExt(VideoViewFactory.RENDERER_VIEW_TYPE.TYPE_PANORAMA_720, this, true);
        panoramicView720Ext.configV720();
        panoramicView720Ext.setId("IVideoView".hashCode());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        panoramicView720Ext.setLayoutParams(params);
        panoramaContentContainer.addView(panoramicView720Ext);
        panoramicView720Ext.setDisplayMode(Panoramic720View.DM_Normal);
    }

    private void initPanoramaContent(PanoramaAlbumContact.PanoramaItem panoramaItem) {
        downloadInfo = DownloadManager.getInstance().getDownloadInfo(PanoramaAlbumContact.PanoramaItem.getTaskKey(uuid, panoramaItem.fileName));
        switch (panoramaItem.type) {
            case PanoramaAlbumContact.PanoramaItem.PANORAMA_ITEM_TYPE.TYPE_PICTURE:
                if (panoramaPanelSwitcher.getDisplayedChild() == 0) {
                    panoramaPanelSwitcher.showNext();
                }

                if (downloadInfo != null && downloadInfo.getState() == 4) {
                    panoramicView720Ext.loadImage(downloadInfo.getTargetPath());
                }
                break;
            case PanoramaAlbumContact.PanoramaItem.PANORAMA_ITEM_TYPE.TYPE_VIDEO:
                if (panoramaPanelSwitcher.getDisplayedChild() == 1) {
                    panoramaPanelSwitcher.showPrevious();
                }
                if (downloadInfo != null && downloadInfo.getState() == 4) {
                    player = JFGPlayer.InitPlayer(this);
                    JFGPlayer.Play(player, downloadInfo.getTargetPath());
                    AppLogger.d("播放地址为:" + downloadInfo.getTargetPath());
                }
                break;
        }
        panoramicView720Ext.enableGyro(true);
        panoramicView720Ext.setDisplayMode(Panoramic720View.DM_Normal);
    }

    @OnClick(R.id.act_panorama_detail_bottom_picture_menu_photograph)
    public void clickedPhotograph() {
        AppLogger.d("clickedPhotograph");
        if (panoramicView720Ext != null) {

        }
    }

    @OnClick({R.id.act_panorama_detail_bottom_picture_menu_vr, R.id.act_panorama_detail_bottom_video_menu_vr})
    public void clickedVR() {
        AppLogger.d("clickedVR");
        if (panoramicView720Ext != null) {
            panoramicView720Ext.enableVRMode(!panoramicView720Ext.isVREnabled());
            boolean vrEnabled = panoramicView720Ext.isVREnabled();
            boolean gyroEnabled = panoramicView720Ext.isGyroEnabled();
            bottomPictureMenuVR.setImageResource(vrEnabled ? R.drawable.photos_icon_vr_hl : R.drawable.photos_icon_vr);
            bottomVideoMenuVR.setImageResource(vrEnabled ? R.drawable.video_icon_vr_hl : R.drawable.video_icon_vr);
            bottomPictureMenuGyroscope.setImageResource(gyroEnabled ? R.drawable.photos_icon_gyroscope : R.drawable.photos_icon_manual);
            bottomVideoMenuGyroscope.setImageResource(gyroEnabled ? R.drawable.video_icon_gyroscope : R.drawable.video_icon_manual);
            bottomPictureMenuMode.setEnabled(!vrEnabled);
            bottomVideoMenuMode.setEnabled(!vrEnabled);
        }
    }

    @OnClick({R.id.act_panorama_detail_bottom_picture_menu_panorama, R.id.act_panorama_detail_bottom_video_menu_panorama})
    public void clickedPanorama() {
        AppLogger.d("clickedPanorama");
        if (panoramicView720Ext != null) {
            int displayMode = panoramicView720Ext.getDisplayMode();
            if (displayMode == Panoramic720View.DM_Normal) {
                bottomPictureMenuMode.setImageResource(R.drawable.photos_icon_panorama);
                bottomVideoMenuMode.setImageResource(R.drawable.video_icon_panorama);
                panoramicView720Ext.setDisplayMode(Panoramic720View.DM_Fisheye);
            } else if (displayMode == Panoramic720View.DM_Fisheye) {
                bottomPictureMenuMode.setImageResource(R.drawable.photos_icon_fisheye);
                bottomVideoMenuMode.setImageResource(R.drawable.video_icon_fisheye);
                panoramicView720Ext.setDisplayMode(Panoramic720View.DM_LittlePlanet);
            } else if (displayMode == Panoramic720View.DM_LittlePlanet) {
                bottomPictureMenuMode.setImageResource(R.drawable.photos_icon_asteroid);
                bottomVideoMenuMode.setImageResource(R.drawable.video_icon_asteroid);
                panoramicView720Ext.setDisplayMode(Panoramic720View.DM_Normal);
            }
        }
    }

    @OnClick({R.id.act_panorama_detail_bottom_picture_menu_gyroscope, R.id.act_panorama_detail_bottom_video_menu_gyroscope})
    public void clickedGyroscope() {
        AppLogger.d("clickedGyroscope");
        if (panoramicView720Ext != null) {
            panoramicView720Ext.enableGyro(!panoramicView720Ext.isGyroEnabled());
            boolean gyroEnabled = panoramicView720Ext.isGyroEnabled();
            bottomPictureMenuGyroscope.setImageResource(gyroEnabled ? R.drawable.photos_icon_gyroscope : R.drawable.photos_icon_manual);
            bottomVideoMenuGyroscope.setImageResource(gyroEnabled ? R.drawable.video_icon_gyroscope : R.drawable.video_icon_manual);
        }
    }

    @Override
    public void OnPlayerReady(long l, int i, int i1) {
        AppLogger.d("播放器初始化成功了");
        JFGPlayer.StartRender(player, panoramicView720Ext);
    }

    @Override
    public void OnPlayerFailed(long l) {
        AppLogger.d("播放器初始化失败了");
    }

    @Override
    public void OnPlayerFinish(long l) {
        AppLogger.d("播放完成了");
    }

    @Override
    public void OnUpdateProgress(long l, int i) {
        AppLogger.d("当前播放进度为:" + i);
        panoramaPanelSeekBar.setProgress(i);
    }
}
