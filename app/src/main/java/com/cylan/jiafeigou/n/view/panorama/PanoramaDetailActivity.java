package com.cylan.jiafeigou.n.view.panorama;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.ActivityComponent;
import com.cylan.jiafeigou.base.module.BasePanoramaApiHelper;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.share.ShareActivity;
import com.cylan.jiafeigou.support.share.ShareConstant;
import com.cylan.jiafeigou.utils.BitmapUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PanoramaThumbURL;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.video.PanoramicView720_Ext;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;
import com.cylan.panorama.CommonPanoramicView;
import com.cylan.panorama.Panoramic720View;
import com.cylan.player.JFGPlayer;
import com.danikula.videocache.HttpProxyCacheServer;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okserver.download.DownloadInfo;
import com.lzy.okserver.download.DownloadManager;
import com.lzy.okserver.listener.DownloadListener;
import com.umeng.socialize.UMShareAPI;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by yanzhendong on 2017/3/16.
 */

public class PanoramaDetailActivity extends BaseActivity<PanoramaDetailContact.Presenter> implements JFGPlayer.JFGPlayerCallback, PanoramaDetailContact.View, CommonPanoramicView.PanoramaEventListener {

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
    @BindView(R.id.act_panorama_detail_toolbar_more)
    ImageButton topMenuMore;
    @BindView(R.id.act_panorama_detail_toolbar_share)
    ImageButton topMenuShare;
    @BindView(R.id.tv_top_bar_left)
    Button topBack;
    @BindView(R.id.act_panorama_detail_bottom_video_menu_container)
    LinearLayout bottomMenuItemContainer;
    @BindView(R.id.act_panorama_detail_bottom_picture_menu_photograph)
    ImageButton bottomPictureMenuPicture;
    @BindView(R.id.act_panorama_detail_pop_picture_vr_tips)
    RelativeLayout popPictureVrTips;
    @BindView(R.id.act_panorama_detail_bottom_video_menu_photograph)
    ImageButton bottomVideoMenuPicture;
    @BindView(R.id.act_panorama_detail_bottom_video_menu_time_title)
    TextView bottomVideoMenuPlayTime;
    @Inject
    HttpProxyCacheServer httpProxy;
    private PanoramicView720_Ext panoramicView720Ext;
    private long player;
    private PanoramaAlbumContact.PanoramaItem panoramaItem;
    private DownloadInfo downloadInfo;
    private PopupWindow morePopMenu;
    private TextView download;
    private int mode;
    private View deleted;

    public static Intent getIntent(Context context, String uuid, PanoramaAlbumContact.PanoramaItem item, int mode) {
        Intent intent = new Intent(context, PanoramaDetailActivity.class);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        intent.putExtra("panorama_item", item);
        intent.putExtra("panorama_mode", mode);
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
        mode = getIntent().getIntExtra("panorama_mode", 2);
        topBack.setText(TimeUtils.getTimeSpecial(panoramaItem.time * 1000L));
        initPanoramaView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ViewUtils.setViewPaddingStatusBar(headerTitleContainer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initPanoramaContent(panoramaItem);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != 0) {
            JFGPlayer.StopRender(player);
            JFGPlayer.Stop(player);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        ViewUtils.clearViewPaddingStatusBar(headerTitleContainer);

    }

    @Override
    public void onScreenRotationChanged(boolean land) {
        super.onScreenRotationChanged(land);
        initViewLayoutParams(land);
    }

    private void initViewLayoutParams(boolean land) {
        if (land) {
            ViewUtils.clearViewMarginStatusBar(headerTitleContainer);
            headerTitleContainer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            headerTitleContainer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            ViewUtils.setViewMarginStatusBar(headerTitleContainer);
        }
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) panoramaPanelSwitcher.getLayoutParams();
        params.height = ((int) getResources().getDimension(R.dimen.panorama_detail_panel_height));
        panoramaPanelSwitcher.setLayoutParams(params);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) bottomVideoMenuGyroscope.getLayoutParams();
        layoutParams.setMarginEnd((int) getResources().getDimension(R.dimen.panorama_detail_panel_video_menu_last_margin));
        bottomVideoMenuGyroscope.setLayoutParams(layoutParams);
        layoutParams = (LinearLayout.LayoutParams) bottomVideoMenuMode.getLayoutParams();
        layoutParams.setMarginEnd((int) getResources().getDimension(R.dimen.panorama_detail_panel_video_menu_item_margin));
        bottomVideoMenuMode.setLayoutParams(layoutParams);
        layoutParams = (LinearLayout.LayoutParams) bottomVideoMenuVR.getLayoutParams();
        layoutParams.setMarginEnd((int) getResources().getDimension(R.dimen.panorama_detail_panel_video_menu_item_margin));
        bottomVideoMenuVR.setLayoutParams(layoutParams);
        layoutParams = (LinearLayout.LayoutParams) bottomVideoMenuPicture.getLayoutParams();
        layoutParams.setMarginEnd((int) getResources().getDimension(R.dimen.panorama_detail_panel_video_menu_item_margin));
        bottomVideoMenuPicture.setLayoutParams(layoutParams);
        params = (RelativeLayout.LayoutParams) bottomMenuItemContainer.getLayoutParams();
        params.bottomMargin = (int) getResources().getDimension(R.dimen.panorama_detail_panel_video_menu_margin_bottom);
        bottomMenuItemContainer.setLayoutParams(params);
        layoutParams = (LinearLayout.LayoutParams) bottomPictureMenuPicture.getLayoutParams();
        layoutParams.setMarginStart((int) getResources().getDimension(R.dimen.panorama_detail_panel_picture_menu_margin_slide));
        bottomPictureMenuPicture.setLayoutParams(layoutParams);
        layoutParams = (LinearLayout.LayoutParams) bottomPictureMenuVR.getLayoutParams();
        layoutParams.setMarginStart((int) getResources().getDimension(R.dimen.panorama_detail_panel_picture_menu_item_margin));
        bottomPictureMenuVR.setLayoutParams(layoutParams);
        layoutParams = (LinearLayout.LayoutParams) bottomPictureMenuMode.getLayoutParams();
        layoutParams.setMarginStart((int) getResources().getDimension(R.dimen.panorama_detail_panel_picture_menu_item_margin));
        bottomPictureMenuMode.setLayoutParams(layoutParams);
        layoutParams = (LinearLayout.LayoutParams) bottomPictureMenuGyroscope.getLayoutParams();
        layoutParams.setMarginStart((int) getResources().getDimension(R.dimen.panorama_detail_panel_picture_menu_item_margin));
        layoutParams.setMarginEnd((int) getResources().getDimension(R.dimen.panorama_detail_panel_picture_menu_margin_slide));
        bottomPictureMenuGyroscope.setLayoutParams(layoutParams);
    }

    private void initPanoramaView() {
        panoramicView720Ext = (PanoramicView720_Ext) VideoViewFactory.CreateRendererExt(VideoViewFactory.RENDERER_VIEW_TYPE.TYPE_PANORAMA_720, this, true);
        panoramicView720Ext.configV720();
        panoramicView720Ext.setId("IVideoView".hashCode());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        panoramicView720Ext.setLayoutParams(params);
        panoramaContentContainer.addView(panoramicView720Ext);
        panoramicView720Ext.setEventListener(this);
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
                } else {
                    String deviceIp = BasePanoramaApiHelper.getInstance().getDeviceIp();
                    if (deviceIp != null) {
                        player = JFGPlayer.InitPlayer(this);
                        JFGPlayer.Play(player, deviceIp + "/images/" + panoramaItem.fileName);
                    } else {
                        AppLogger.d("当前网络状况下无法播放");
                    }
                }


                break;
        }
        panoramicView720Ext.enableGyro(true);
        panoramicView720Ext.setDisplayMode(Panoramic720View.DM_Normal);
        panoramaPanelSeekBar.setMax(panoramaItem.duration);
    }

    @OnClick(R.id.act_panorama_detail_bottom_picture_menu_photograph)
    public void clickedPhotograph() {
        AppLogger.d("clickedPhotograph");
        if (panoramicView720Ext != null) {

        }
    }

    @OnClick(R.id.tv_top_bar_left)
    public void clickedBack() {
        if (downloadInfo != null) {
            if (downloadInfo.getListener() == listener) {
                downloadInfo.setListener(null);
            }
        }
        onBackPressed();
    }

    @OnClick(R.id.act_panorama_detail_pop_picture_close)
    public void closePopPictureTips() {
        popPictureVrTips.setVisibility(View.INVISIBLE);
    }

    @OnClick({R.id.act_panorama_detail_bottom_picture_menu_vr, R.id.act_panorama_detail_bottom_video_menu_vr})
    public void clickedVR() {
        AppLogger.d("clickedVR");
        if (panoramicView720Ext != null) {
            panoramicView720Ext.enableVRMode(!panoramicView720Ext.isVREnabled());
            boolean vrEnabled = panoramicView720Ext.isVREnabled();
            panoramicView720Ext.enableGyro(vrEnabled || panoramicView720Ext.isGyroEnabled());
            bottomPictureMenuVR.setImageResource(vrEnabled ? R.drawable.photos_icon_vr_hl : R.drawable.photos_icon_vr);
            bottomVideoMenuVR.setImageResource(vrEnabled ? R.drawable.video_icon_vr_hl : R.drawable.video_icon_vr);
            bottomPictureMenuGyroscope.setImageResource(panoramicView720Ext.isGyroEnabled() ? R.drawable.photos_icon_gyroscope : R.drawable.photos_icon_manual);
            bottomVideoMenuGyroscope.setImageResource(panoramicView720Ext.isGyroEnabled() ? R.drawable.video_icon_gyroscope : R.drawable.video_icon_manual);
            bottomPictureMenuGyroscope.setEnabled(!vrEnabled);
            bottomVideoMenuGyroscope.setEnabled(!vrEnabled);
            bottomPictureMenuMode.setImageResource(R.drawable.photos_icon_panorama);
            bottomVideoMenuMode.setImageResource(R.drawable.video_icon_panorama);
            bottomPictureMenuMode.setEnabled(!vrEnabled);
            bottomVideoMenuMode.setEnabled(!vrEnabled);
            int orientation = getResources().getConfiguration().orientation;
            if (orientation != Configuration.ORIENTATION_LANDSCAPE && vrEnabled) {
                popPictureVrTips.setVisibility(View.VISIBLE);
            }
        }
    }

    @OnClick({R.id.act_panorama_detail_bottom_picture_menu_panorama, R.id.act_panorama_detail_bottom_video_menu_panorama})
    public void clickedPanorama() {
        AppLogger.d("clickedPanorama");
        if (panoramicView720Ext != null) {
            int displayMode = panoramicView720Ext.getDisplayMode();
            if (displayMode == Panoramic720View.DM_Normal) {
                panoramicView720Ext.setDisplayMode(Panoramic720View.DM_Fisheye);
                bottomPictureMenuMode.setImageResource(R.drawable.photos_icon_fisheye);
                bottomVideoMenuMode.setImageResource(R.drawable.video_icon_fisheye);
            } else if (displayMode == Panoramic720View.DM_Fisheye) {
                bottomPictureMenuMode.setImageResource(R.drawable.photos_icon_asteroid);
                bottomVideoMenuMode.setImageResource(R.drawable.video_icon_asteroid);
                panoramicView720Ext.setDisplayMode(Panoramic720View.DM_LittlePlanet);
            } else if (displayMode == Panoramic720View.DM_LittlePlanet) {
                bottomPictureMenuMode.setImageResource(R.drawable.photos_icon_panorama);
                bottomVideoMenuMode.setImageResource(R.drawable.video_icon_panorama);
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

    @OnClick({R.id.act_panorama_detail_bottom_video_menu_photograph, R.id.act_panorama_detail_bottom_picture_menu_photograph})
    public void screenShot() {
        if (panoramicView720Ext != null) {
            panoramicView720Ext.takeSnapshot(true);
        }
    }

    @OnClick(R.id.act_panorama_detail_toolbar_share)
    public void clickedShare() {
        AppLogger.d("点击的分享菜单");
        if (!NetUtils.isNetworkAvailable(this)) {
            ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR_1));
        } else if (panoramaItem.duration > 8) {
            ToastUtil.showNegativeToast(getString(R.string.Tap1_Share_NoLonger8STips));
        } else {
            AppLogger.d("点击了分享按钮");
            dismissDialogs();
            JFGPlayer.Stop(player);
            new PanoramaThumbURL(uuid, panoramaItem.fileName).fetchFile(filePath -> {
                Intent intent = new Intent(this, ShareActivity.class);
                intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                intent.putExtra(ShareConstant.SHARE_CONTENT_H5_WITH_UPLOAD_EXTRA_SHARE_ITEM, panoramaItem);
                intent.putExtra(ShareConstant.SHARE_CONTENT_H5_WITH_UPLOAD_EXTRA_FILE_PATH, downloadInfo.getTargetPath());
                intent.putExtra(ShareConstant.SHARE_CONTENT, ShareConstant.SHARE_CONTENT_H5_WITH_UPLOAD);
                intent.putExtra(ShareConstant.SHARE_CONTENT_H5_WITH_UPLOAD_EXTRA_THUMB_PATH, filePath);
                startActivity(intent);
            });

        }
    }

    private void dismissDialogs() {
        if (morePopMenu != null) {
            morePopMenu.dismiss();
        }
    }

    public void onShareToH5(View view) {

    }


    @OnClick(R.id.act_panorama_detail_toolbar_more)
    public void clickedMore() {
        AppLogger.d("点击了更多菜单");
        if (morePopMenu == null) {
            morePopMenu = new PopupWindow(this);
            View contentView = LayoutInflater.from(this).inflate(R.layout.item_panorama_more, null);
            contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            download = (TextView) contentView.findViewById(R.id.panorama_detail_more_download);
            deleted = contentView.findViewById(R.id.panorama_detail_more_delete);
            download.setOnClickListener(v -> {
                processDownload();
            });
            deleted.setOnClickListener(v -> {
                deleteWithAlert();
            });
            morePopMenu.setContentView(contentView);
            morePopMenu.setOutsideTouchable(true);
            morePopMenu.setFocusable(true);
            morePopMenu.setBackgroundDrawable(new ColorDrawable(0));
        }
        if (!morePopMenu.isShowing()) {
            if (downloadInfo == null) {
                download.setText(R.string.Tap1_Album_Download);
            } else if (downloadInfo.getState() == 4) {
                download.setText(R.string.FINISHED);
            } else {
                download.setText((int) (downloadInfo.getProgress() * 100) + "%");
                downloadInfo.setListener(listener);
            }
            morePopMenu.showAtLocation(topMenuMore, Gravity.RIGHT | Gravity.TOP, getResources().getDimensionPixelOffset(R.dimen.y15), getResources().getDimensionPixelOffset(R.dimen.y30));
        }
    }

    private void deleteWithAlert() {
        new AlertDialog.Builder(this)
                .setMessage(mode == 0 ? R.string.Tips_SureDelete : R.string.Tap1_DeletedCameraNCellphoneFileTips)
                .setNegativeButton(R.string.CANCEL, null)
                .setPositiveButton(R.string.DELETE, (dialog, which) -> {
                    if (deleted != null) {
                        deleted.setEnabled(false);
                    }
                    presenter.delete(panoramaItem, mode);
                })
                .show();
        AppLogger.d("将进行删除");

    }

    private DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(DownloadInfo downloadInfo) {
            if (download != null) {
                download.setText((int) (downloadInfo.getProgress() * 100) + "%");
            }
        }

        @Override
        public void onFinish(DownloadInfo downloadInfo) {
            if (download != null) {
                download.setText(R.string.FINISHED);
                download.setEnabled(false);
            }
        }

        @Override
        public void onError(DownloadInfo downloadInfo, String s, Exception e) {
            if (download != null) {
                download.setText(R.string.Tap1_Album_Download);
                download.setEnabled(true);
            }
        }
    };

    private void processDownload() {
        AppLogger.d("将进行下载");
        String deviceIp = BasePanoramaApiHelper.getInstance().getDeviceIp();
        if (deviceIp != null) {
            String taskKey = PanoramaAlbumContact.PanoramaItem.getTaskKey(uuid, panoramaItem.fileName);
            GetRequest request = OkGo.get(deviceIp + "/images/" + panoramaItem.fileName);
            DownloadManager.getInstance().addTask(taskKey, request, listener);
            downloadInfo = DownloadManager.getInstance().getDownloadInfo(PanoramaAlbumContact.PanoramaItem.getTaskKey(uuid, panoramaItem.fileName));
            if (download != null) {
                download.setEnabled(false);
            }
        } else {
            AppLogger.d("非家居模式不能进行下载");
        }
    }

    @Override
    public void OnPlayerReady(long l, int i, int i1, int i2) {
        AppLogger.d("播放器初始化成功了" + i + "," + i1 + "," + i2);
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
        AppLogger.d("当前播放进度为:" + i + "," + l);
        panoramaPanelSeekBar.setProgress(i / 1000);
    }

    @Override
    public void onDeleteResult(int code) {
        if (deleted != null) {
            deleted.setEnabled(true);
        }
        if (code == 0) {
            ToastUtil.showPositiveToast("删除成功");
        } else if (code == -1) {
            ToastUtil.showNegativeToast("本地已删除,设备端删除失败");
        }
        finish();
    }

    @Override
    public void onSingleTap(float v, float v1) {

    }

    @Override
    public void onSnapshot(Bitmap bitmap, boolean b) {
        BitmapUtils.saveBitmap2file(bitmap, JConstant.MEDIA_PATH + "/" + System.currentTimeMillis() / 1000 + ".jpg");
        ToastUtil.showPositiveToast("截图成功");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }
}
