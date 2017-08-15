package com.cylan.jiafeigou.n.view.panorama;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.ActivityComponent;
import com.cylan.jiafeigou.base.module.BaseDeviceInformationFetcher;
import com.cylan.jiafeigou.base.module.BasePanoramaApiHelper;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.misc.ApFilter;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.share.ShareManager;
import com.cylan.jiafeigou.utils.BitmapUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PanoramaThumbURL;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.video.PanoramicView720_Ext;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;
import com.cylan.panorama.CommonPanoramicView;
import com.cylan.panorama.Panoramic720View;
import com.cylan.player.JFGPlayer;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.danikula.videocache.HttpProxyCacheServer;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okserver.download.DownloadInfo;
import com.lzy.okserver.download.DownloadManager;
import com.lzy.okserver.download.db.DownloadDBManager;
import com.lzy.okserver.listener.DownloadListener;

import java.io.File;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/3/16.
 */

public class PanoramaDetailActivity extends BaseActivity<PanoramaDetailContact.Presenter> implements JFGPlayer.JFGPlayerCallback, PanoramaDetailContact.View, CommonPanoramicView.PanoramaEventListener, SeekBar.OnSeekBarChangeListener {

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
    @BindView(R.id.act_panorama_detail_bottom_video_menu_play)
    ImageButton bottomVideoMenuPlay;
    @Inject
    HttpProxyCacheServer httpProxy;
    private PanoramicView720_Ext panoramicView720Ext;
    private long player = 0;
    private PanoramaAlbumContact.PanoramaItem panoramaItem;
    private DownloadInfo downloadInfo;
    private PopupWindow morePopMenu;
    private TextView download;
    private int mode;//0 :仅本地 1:设备 2:本机+设备 3:fromMessageList
    private View deleted;
    private boolean looper = true;
    private boolean isPlay = false;
    private CamMessageBean bean;

    private DetailDataAdapter adapter;
    private Subscription subscribe;
    private boolean isSetPadding = false;


    public static Intent getIntentFromMessage(Context context, String uuid, CamMessageBean bean, int position, int index) {
        PanoramaAlbumContact.PanoramaItem item = null;
        if (bean.alarmMsg != null) {
            bean.alarmMsg.isRecording = 0;//全部当成图片处理
            if (bean.alarmMsg.isRecording == 1) {
                // TODO: 2017/8/10 视频
                item = new PanoramaAlbumContact.PanoramaItem(bean.alarmMsg.time + "_8.mp4");
            } else {
                // TODO: 2017/8/10 图片
                item = new PanoramaAlbumContact.PanoramaItem(bean.alarmMsg.time + ".jpg");
            }
        }
        Intent intent = new Intent(context, PanoramaDetailActivity.class);
        intent.putExtra("panorama_position", position);
        intent.putExtra("panorama_mode", 3);
        intent.putExtra("panorama_item", item);
        intent.putExtra("cam_bean", bean);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        return intent;
    }

    public static Intent getIntent(Context context, String uuid, PanoramaAlbumContact.PanoramaItem item, int mode, int position) {
        Intent intent = new Intent(context, PanoramaDetailActivity.class);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        intent.putExtra("panorama_item", item);
        intent.putExtra("panorama_mode", mode);
        intent.putExtra("panorama_position", position);
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

        refreshControllerView(false);

        Drawable progressDrawable = panoramaPanelSeekBar.getProgressDrawable().mutate();
        Drawable drawable = DrawableCompat.wrap(progressDrawable);
        DrawableCompat.setTint(drawable, getResources().getColor(R.color.color_white));
        panoramaPanelSeekBar.setProgressDrawable(drawable);
        Drawable mutate = panoramaPanelSeekBar.getThumb().mutate();
        Drawable wrap = DrawableCompat.wrap(mutate);
        DrawableCompat.setTint(wrap, getResources().getColor(R.color.color_white));
        panoramaPanelSeekBar.setThumb(wrap);
        panoramaPanelSeekBar.requestLayout();
        panoramaPanelSeekBar.setOnSeekBarChangeListener(this);

        panoramaItem = getIntent().getParcelableExtra("panorama_item");
        mode = getIntent().getIntExtra("panorama_mode", 2);
        this.bean = getIntent().getParcelableExtra("cam_bean");
        topBack.setText(TimeUtils.get1224(panoramaItem.time * 1000L));
        initPanoramaView();

//        panoramaContentContainer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onSingleTap(0, 0);
//            }
//        });
    }

    private Observable<RxEvent.FetchDeviceInformation> getConnection() {
        return RxBus.getCacheInstance().toObservable(RxEvent.FetchDeviceInformation.class)
                .first(ret -> ret.success)
                .doOnSubscribe(() -> BaseDeviceInformationFetcher.getInstance().init(uuid))
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE && !isSetPadding) {
            isSetPadding = true;
            ViewUtils.setViewPaddingStatusBar(headerTitleContainer);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isSetPadding) {
            isSetPadding = false;
            ViewUtils.clearViewPaddingStatusBar(headerTitleContainer);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (panoramicView720Ext != null) {
            panoramicView720Ext.onResume();
        } else {
            initPanoramaView();
        }
        looper = true;
        initPanoramaContent(panoramaItem);
    }


    @Override
    protected void onPause() {
        headerTitleContainer.setTranslationY(0);
        headerTitleContainer.setAlpha(1);
        panoramaPanelSwitcher.setTranslationY(0);
        panoramaPanelSwitcher.setAlpha(1);
        super.onPause();
        if (panoramicView720Ext != null) {
            panoramicView720Ext.onPause();
        }
        looper = false;
        releasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (panoramicView720Ext != null) {
            panoramicView720Ext.onDestroy();
            panoramicView720Ext = null;
        }
    }

    @Override
    public void onScreenRotationChanged(boolean land) {
        super.onScreenRotationChanged(land);
        initViewLayoutParams(land);
    }

    private void initViewLayoutParams(boolean land) {
        headerTitleContainer.setTranslationY(0);
        headerTitleContainer.setAlpha(1);
        panoramaPanelSwitcher.setTranslationY(0);
        panoramaPanelSwitcher.setAlpha(1);
        if (land) {
            if (isSetPadding) {
                isSetPadding = false;
                ViewUtils.clearViewPaddingStatusBar(headerTitleContainer);
            }
//            popPictureVrTips.setVisibility(View.GONE);
//            ViewUtils.setSystemUiVisibility(headerTitleContainer, false);
            headerTitleContainer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        } else {
//            ViewUtils.setSystemUiVisibility(headerTitleContainer, true);
            headerTitleContainer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            if (!isSetPadding) {
                isSetPadding = true;
                ViewUtils.setViewPaddingStatusBar(headerTitleContainer);
            }
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


        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) bottomPictureMenuGyroscope.getLayoutParams();
        lp.setMarginEnd((int) getResources().getDimension(R.dimen.panorama_detail_panel_picture_menu_margin_slide));
        bottomPictureMenuGyroscope.setLayoutParams(lp);
        lp = (ConstraintLayout.LayoutParams) bottomPictureMenuPicture.getLayoutParams();
        lp.setMarginStart((int) getResources().getDimension(R.dimen.panorama_detail_panel_picture_menu_margin_slide));
        bottomPictureMenuPicture.setLayoutParams(lp);

    }

    private void initPanoramaView() {
        panoramicView720Ext = (PanoramicView720_Ext) VideoViewFactory.CreateRendererExt(VideoViewFactory.RENDERER_VIEW_TYPE.TYPE_PANORAMA_720, this, true);
        panoramicView720Ext.setId("IVideoView".hashCode());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        panoramicView720Ext.setLayoutParams(params);
        panoramaContentContainer.addView(panoramicView720Ext);
        bottomPictureMenuGyroscope.setImageResource(R.drawable.photos_icon_manual_selector);
        bottomVideoMenuGyroscope.setImageResource(R.drawable.video_icon_manual_selector);
        bottomPictureMenuMode.setImageResource(R.drawable.photos_icon_fisheye_selector);
        bottomVideoMenuMode.setImageResource(R.drawable.video_icon_fisheye_selector);
        panoramicView720Ext.setEventListener(this);
        panoramicView720Ext.setDisplayMode(Panoramic720View.DM_Fisheye);
        panoramicView720Ext.enableGyro(false);
        panoramicView720Ext.configV720();
        panoramicView720Ext.setZOrderOnTop(true);
        panoramicView720Ext.setZOrderMediaOverlay(true);
    }


    private void initPanoramaContent(PanoramaAlbumContact.PanoramaItem panoramaItem) {
        LoadingDialog.dismissLoading();
        downloadInfo = DownloadManager.getInstance().getDownloadInfo(PanoramaAlbumContact.PanoramaItem.getTaskKey(uuid, panoramaItem.fileName));
        switch (panoramaItem.type) {
            case PanoramaAlbumContact.PanoramaItem.PANORAMA_ITEM_TYPE.TYPE_PICTURE: {
                if (panoramaPanelSwitcher.getDisplayedChild() == 0) {
                    panoramaPanelSwitcher.showNext();
                }

                if (downloadInfo != null && downloadInfo.getState() == DownloadManager.FINISH) {
                    Schedulers.io().createWorker().schedule(() -> panoramicView720Ext.loadImage(downloadInfo.getTargetPath()));
                    refreshControllerView(true);
                } else if (bean != null) {
                    Glide.with(this)
                            .load(MiscUtils.getCamWarnUrl(uuid, bean, bean.alarmMsg.fileIndex))
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .skipMemoryCache(true)
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    refreshControllerView(true);
                                    panoramicView720Ext.loadImage(resource);
                                    LoadingDialog.dismissLoading();
//                                        panoramicView720Ext.postDelayed(() -> LoadingDialog.dismissLoading(getSupportFragmentManager()), 1000);
                                }

                                @Override
                                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                    super.onLoadFailed(e, errorDrawable);
                                    LoadingDialog.dismissLoading();
//                                        panoramicView720Ext.postDelayed(() -> LoadingDialog.dismissLoading(getSupportFragmentManager()), 1000);
                                }

                                @Override
                                public void onStart() {
                                    super.onStart();
                                    LoadingDialog.showLoading(PanoramaDetailActivity.this, getString(R.string.LOADING), false, null);
                                }
                            });
                } else {
                    if (subscribe != null && !subscribe.isUnsubscribed()) {
                        subscribe.unsubscribe();
                    }
                    subscribe = getConnection().subscribe(ret -> {
                        String deviceIp = BasePanoramaApiHelper.getInstance().getDeviceIp();
                        if (!TextUtils.isEmpty(deviceIp) && bean == null) {
                            Glide.with(this)
                                    .load(deviceIp + "/images/" + panoramaItem.fileName)
                                    .thumbnail(Glide.with(this).load("http://" + deviceIp + "/thumb/" + panoramaItem.fileName.split("\\.")[0] + ".thumb"))
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(new SimpleTarget<GlideDrawable>() {
                                        @Override
                                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                            refreshControllerView(true);
                                            Schedulers.io().createWorker().schedule(() -> panoramicView720Ext.loadImage(BitmapUtils.drawableToBitmap(resource)));
                                            LoadingDialog.dismissLoading();
                                        }

                                        @Override
                                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                            super.onLoadFailed(e, errorDrawable);
                                            LoadingDialog.dismissLoading();
                                        }

                                        @Override
                                        public void onStart() {
                                            super.onStart();
                                            LoadingDialog.showLoading(PanoramaDetailActivity.this, getString(R.string.LOADING), false, null);
                                        }
                                    });
                        }

                    }, e -> {
                        AppLogger.e(e.getMessage());
                    });
                }
                break;
            }
            case PanoramaAlbumContact.PanoramaItem.PANORAMA_ITEM_TYPE.TYPE_VIDEO: {
                if (panoramaPanelSwitcher.getDisplayedChild() == 1) {
                    panoramaPanelSwitcher.showPrevious();
                }
                bottomVideoMenuPlay.setImageResource(R.drawable.icon_play_selector);
                if (downloadInfo != null && downloadInfo.getState() == 4) {
                    LoadingDialog.showLoading(this, getString(R.string.LOADING), false, null);
                    refreshControllerView(false);
                    initPlayerAndPlay(downloadInfo.getTargetPath());
                } else {
                    if (subscribe != null && !subscribe.isUnsubscribed()) {
                        subscribe.unsubscribe();
                    }
                    subscribe = getConnection().subscribe(ret -> {
                        String deviceIp = BasePanoramaApiHelper.getInstance().getDeviceIp();
                        if (bean != null) {
                            if (deviceIp != null) {
                                LoadingDialog.showLoading(this, getString(R.string.LOADING), false, null);
                                refreshControllerView(false);
                                initPlayerAndPlay(deviceIp + "/md/" + panoramaItem.fileName);
                            } else {
                                AppLogger.d("当前网络状况下无法播放");
                                refreshControllerView(false);
                                loadPreview();

                            }
                        } else {
                            if (deviceIp != null) {
                                LoadingDialog.showLoading(this, getString(R.string.LOADING), false, null);
                                refreshControllerView(false);
                                initPlayerAndPlay(deviceIp + "/images/" + panoramaItem.fileName);
                            } else {
                                AppLogger.d("当前网络状况下无法播放");
                                refreshControllerView(false);
                                loadPreview();
                            }
                        }
                    }, e -> {
                        AppLogger.e(e.getMessage());
                    });
                }
                break;
            }
        }
        panoramicView720Ext.setDisplayMode(Panoramic720View.DM_Fisheye);
        panoramaPanelSeekBar.setMax(panoramaItem.duration);
        panoramicView720Ext.enableGyro(false);
        bottomPictureMenuGyroscope.setImageResource(R.drawable.photos_icon_manual_selector);
        bottomVideoMenuGyroscope.setImageResource(R.drawable.video_icon_manual_selector);
    }

    private void initPlayerAndPlay(String path) {
        Schedulers.io().createWorker().schedule(() -> {
            if (player == 0) {
                player = JFGPlayer.InitPlayer(this);
            }
            JFGPlayer.Play(player, path);
        });
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
        PreferencesUtils.putBoolean(JConstant.SHOW_VR_MODE_TIPS, false);
        popPictureVrTips.setVisibility(View.INVISIBLE);
    }

    @OnClick({R.id.act_panorama_detail_bottom_picture_menu_vr, R.id.act_panorama_detail_bottom_video_menu_vr})
    public void clickedVR() {
        AppLogger.d("clickedVR");
        if (panoramicView720Ext != null) {
            panoramicView720Ext.enableVRMode(!panoramicView720Ext.isVREnabled());
            boolean vrEnabled = panoramicView720Ext.isVREnabled();
            if (!vrEnabled) panoramicView720Ext.setDisplayMode(Panoramic720View.DM_Fisheye);//默认双鱼眼
            panoramicView720Ext.enableGyro(vrEnabled || panoramicView720Ext.isGyroEnabled());
            bottomPictureMenuVR.setImageResource(vrEnabled ? R.drawable.photos_icon_vr_hl : R.drawable.photos_icon_vr_selector);
            bottomVideoMenuVR.setImageResource(vrEnabled ? R.drawable.video_icon_vr_hl : R.drawable.video_icon_vr_selector);
            bottomPictureMenuGyroscope.setImageResource(panoramicView720Ext.isGyroEnabled() ? R.drawable.photos_icon_gyroscope_selector : R.drawable.photos_icon_manual_selector);
            bottomVideoMenuGyroscope.setImageResource(panoramicView720Ext.isGyroEnabled() ? R.drawable.video_icon_gyroscope_selector : R.drawable.video_icon_manual_selector);
            bottomPictureMenuGyroscope.setEnabled(!vrEnabled);
            bottomVideoMenuGyroscope.setEnabled(!vrEnabled);
            bottomPictureMenuMode.setImageResource(R.drawable.photos_icon_panorama_selector);
            bottomVideoMenuMode.setImageResource(R.drawable.video_icon_panorama_selector);
            bottomPictureMenuMode.setEnabled(!vrEnabled);
            bottomVideoMenuMode.setEnabled(!vrEnabled);
            boolean show = PreferencesUtils.getBoolean(JConstant.SHOW_VR_MODE_TIPS, true);
            if (show && vrEnabled) {
                popPictureVrTips.setVisibility(View.VISIBLE);
            }
            if (!MiscUtils.isLand() && vrEnabled)
                ViewUtils.setRequestedOrientation(this, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @OnClick({R.id.act_panorama_detail_bottom_picture_menu_panorama, R.id.act_panorama_detail_bottom_video_menu_panorama})
    public void clickedPanorama() {
        AppLogger.d("clickedPanorama");
        if (panoramicView720Ext != null) {
            int displayMode = panoramicView720Ext.getDisplayMode();
            if (displayMode == Panoramic720View.DM_Normal) {
                bottomPictureMenuMode.setImageResource(R.drawable.photos_icon_asteroid_selector);
                bottomVideoMenuMode.setImageResource(R.drawable.video_icon_asteroid_selector);
                panoramicView720Ext.setDisplayMode(Panoramic720View.DM_LittlePlanet);
            } else if (displayMode == Panoramic720View.DM_Fisheye) {
                bottomPictureMenuMode.setImageResource(R.drawable.photos_icon_panorama_selector);
                bottomVideoMenuMode.setImageResource(R.drawable.video_icon_panorama_selector);
                panoramicView720Ext.setDisplayMode(Panoramic720View.DM_Normal);
            } else if (displayMode == Panoramic720View.DM_LittlePlanet) {
                panoramicView720Ext.setDisplayMode(Panoramic720View.DM_Fisheye);
                bottomPictureMenuMode.setImageResource(R.drawable.photos_icon_fisheye_selector);
                bottomVideoMenuMode.setImageResource(R.drawable.video_icon_fisheye_selector);
            }
        }
    }

    @OnClick({R.id.act_panorama_detail_bottom_picture_menu_gyroscope, R.id.act_panorama_detail_bottom_video_menu_gyroscope})
    public void clickedGyroscope() {
        AppLogger.d("clickedGyroscope");
        if (panoramicView720Ext != null) {
            panoramicView720Ext.enableGyro(!panoramicView720Ext.isGyroEnabled());
            boolean gyroEnabled = panoramicView720Ext.isGyroEnabled();
            bottomPictureMenuGyroscope.setImageResource(gyroEnabled ? R.drawable.photos_icon_gyroscope_selector : R.drawable.photos_icon_manual_selector);
            bottomVideoMenuGyroscope.setImageResource(gyroEnabled ? R.drawable.video_icon_gyroscope_selector : R.drawable.video_icon_manual_selector);
        }
    }

    @OnClick({R.id.act_panorama_detail_bottom_video_menu_photograph, R.id.act_panorama_detail_bottom_picture_menu_photograph})
    public void screenShot() {
        AppLogger.d("clickedPhotograph");
        Schedulers.io().createWorker().schedule(() -> {
            if (panoramicView720Ext != null) {
                panoramicView720Ext.takeSnapshot(true);
            }
        });
    }

    private void releasePlayer() {
        AppLogger.d("正在释放播放器!!!");
        looper = false;
        if (player != 0) {
            long address = player;
            player = 0;
            JFGPlayer.Stop(address);
            JFGPlayer.Release(address);
        }
        player = 0;
    }

    @OnClick(R.id.act_panorama_detail_toolbar_share)
    public void clickedShare() {
        AppLogger.d("点击的分享菜单");
        dismissDialogs();

        if (!NetUtils.isNetworkAvailable(this)) {
            ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR_1));
        } else if (bean != null && bean.alarmMsg.isRecording == 0) {
            releasePlayer();
            Glide.with(this)
                    .load(MiscUtils.getCamWarnUrl(uuid, bean, bean.alarmMsg.fileIndex))
                    .downloadOnly(new SimpleTarget<File>() {
                        @Override
                        public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                            ShareManager.byH5(PanoramaDetailActivity.this)
                                    .withFile(resource.getAbsolutePath())
                                    .withItem(panoramaItem)
                                    .withThumb(resource.getAbsolutePath())
                                    .withUuid(uuid)
                                    .share();
                        }
                    });
        } else if (panoramaItem.duration > 8) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.Tap1_Share_NoLonger8STips)
                    .setCancelable(false)
                    .setPositiveButton(R.string.OK, null)
                    .show();
        } else if (downloadInfo == null || (downloadInfo.getState() != DownloadManager.FINISH && downloadInfo.getState() != DownloadManager.DOWNLOADING)) {
            //视频还未下载完成
            new AlertDialog.Builder(this)
                    .setMessage(R.string.Download_Then_Share)
                    .setCancelable(false)
                    .setPositiveButton(R.string.OK, null)
                    .show();
            AppLogger.d("视频还未下载完成");

        } else if (downloadInfo != null && downloadInfo.getState() == DownloadManager.DOWNLOADING) {
            ToastUtil.showNegativeToast(getString(R.string.Downloading));
        } else if (ApFilter.isAPMode(uuid)) {
            ToastUtil.showNegativeToast(getString(R.string.NoNetworkTips));
        } else {
            releasePlayer();
            new PanoramaThumbURL(uuid, panoramaItem.fileName).fetchFile(filePath -> {
                ShareManager.byH5(PanoramaDetailActivity.this)
                        .withFile(downloadInfo.getTargetPath())
                        .withItem(panoramaItem)
                        .withThumb(filePath)
                        .withUuid(uuid)
                        .share();
            });
        }
    }

    private void dismissDialogs() {
        if (morePopMenu != null) {
            morePopMenu.dismiss();
        }
    }

    @OnClick(R.id.act_panorama_detail_toolbar_more)
    public void clickedMore() {
        AppLogger.d("点击了更多菜单");
        if (morePopMenu == null) {
            View contentView = LayoutInflater.from(this).inflate(R.layout.item_panorama_more, null);
            contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            download = (TextView) contentView.findViewById(R.id.panorama_detail_more_download);
            deleted = contentView.findViewById(R.id.panorama_detail_more_delete);
            download.setOnClickListener(v -> {
                if (downloadInfo != null && downloadInfo.getState() == DownloadManager.DOWNLOADING) {
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setMessage(R.string.Tap1_Album_CancelDownloadTips)
                            .setPositiveButton(R.string.OK, (dialog, which) -> {
                                downloadInfo.setListener(null);
                                DownloadManager.getInstance().stopTask(downloadInfo.getTaskKey());
                                download.setText(R.string.Tap1_Album_Download);
                                download.setEnabled(true);
                            })
                            .setNegativeButton(R.string.CANCEL, null)
                            .show();
                } else {
                    processDownload();
                }
            });
            deleted.setOnClickListener(v -> {
                deleteWithAlert();
            });
            morePopMenu = new PopupWindow(contentView, contentView.getMeasuredWidth(), contentView.getMeasuredHeight());
            morePopMenu.setOutsideTouchable(true);
            morePopMenu.setFocusable(true);
            morePopMenu.setBackgroundDrawable(new ColorDrawable(0));
        }
        if (!morePopMenu.isShowing()) {
            if (bean != null && bean.alarmMsg != null && bean.alarmMsg.isRecording == 0) {
                download.setText(R.string.Tap1_Album_Downloaded);
                download.setEnabled(false);
            } else if (downloadInfo == null || (downloadInfo.getState() != DownloadManager.FINISH && downloadInfo.getState() != DownloadManager.DOWNLOADING)) {
                download.setText(R.string.Tap1_Album_Download);
            } else if (downloadInfo.getState() == DownloadManager.FINISH) {
                download.setText(R.string.Tap1_Album_Downloaded);
                download.setEnabled(false);
            } else {
                download.setText((int) (downloadInfo.getProgress() * 100) + "%");
                downloadInfo.setListener(listener);
            }
            morePopMenu.showAtLocation(topMenuMore, Gravity.RIGHT | Gravity.TOP, getResources().getDimensionPixelOffset(R.dimen.y15), getResources().getDimensionPixelOffset(R.dimen.y30));
        }
    }

    private void deleteWithAlert() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.PHOTO_DELE_POP)
                .setNegativeButton(R.string.CANCEL, null)
                .setPositiveButton(R.string.DELETE, (dialog, which) -> {
                    if (deleted != null) {
                        deleted.setEnabled(false);
                    }
                    presenter.delete(panoramaItem, mode, bean == null || bean.alarmMsg == null ? 0 : bean.alarmMsg.version);
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
                String targetPath = downloadInfo.getTargetPath();
                if (FileUtils.isFileExist(targetPath)) {
                    download.setText(R.string.Tap1_Album_Downloaded);
                    download.setEnabled(false);
                } else {
                    download.setText(R.string.Tap1_Album_Download);
                    download.setEnabled(true);
                    DownloadManager.getInstance().removeTask(downloadInfo.getTaskKey());
                    downloadInfo.setState(DownloadManager.NONE);
                    DownloadDBManager.INSTANCE.update(downloadInfo);
                }
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

        if (bean != null) {
            String deviceIp = BasePanoramaApiHelper.getInstance().getDeviceIp();
            if (deviceIp != null) {
                GetRequest request = OkGo.get(deviceIp + "/md/" + panoramaItem.fileName);
                String taskKey = PanoramaAlbumContact.PanoramaItem.getTaskKey(uuid, panoramaItem.fileName);
                DownloadInfo downloadInfo = DownloadManager.getInstance().getDownloadInfo(taskKey);
                if (downloadInfo != null) {
                    downloadInfo.setRequest(request);
                    downloadInfo.setUrl(request.getBaseUrl());
                    DownloadDBManager.INSTANCE.replace(downloadInfo);
                }
                DownloadManager.getInstance().addTask(taskKey, request, listener);
                this.downloadInfo = DownloadManager.getInstance().getDownloadInfo(PanoramaAlbumContact.PanoramaItem.getTaskKey(uuid, panoramaItem.fileName));
            } else {
                AppLogger.d("非家居模式不能进行下载");
            }
        } else {
            String deviceIp = BasePanoramaApiHelper.getInstance().getDeviceIp();
            if (deviceIp != null) {
                GetRequest request = OkGo.get(deviceIp + "/images/" + panoramaItem.fileName);
                String taskKey = PanoramaAlbumContact.PanoramaItem.getTaskKey(uuid, panoramaItem.fileName);
                DownloadInfo downloadInfo = DownloadManager.getInstance().getDownloadInfo(taskKey);
                if (downloadInfo != null) {
                    downloadInfo.setRequest(request);
                    downloadInfo.setUrl(request.getBaseUrl());
                    DownloadDBManager.INSTANCE.replace(downloadInfo);
                }
                DownloadManager.getInstance().addTask(taskKey, request, listener);
                this.downloadInfo = DownloadManager.getInstance().getDownloadInfo(PanoramaAlbumContact.PanoramaItem.getTaskKey(uuid, panoramaItem.fileName));
            } else {
                AppLogger.d("非家居模式不能进行下载");
            }
        }
    }

    @Override
    public void OnPlayerReady(long l, int i, int i1, int i2) {
        AppLogger.d("播放器初始化成功了" + i + "," + i1 + "," + i2);
        JFGPlayer.StartRender(player, panoramicView720Ext);
        isPlay = true;
        runOnUiThread(() -> {
            panoramaPanelSeekBar.setMax(i2);
            bottomVideoMenuPlay.setImageResource(R.drawable.icon_suspend_selector);
            refreshControllerView(true);
            LoadingDialog.dismissLoading();
        });
    }


    private void loadPreview() {
        String deviceIp = BasePanoramaApiHelper.getInstance().getDeviceIp();
        if (!TextUtils.isEmpty(deviceIp) && bean == null) {
            Glide.with(this).load("http://" + deviceIp + "/thumb/" + panoramaItem.fileName.split("\\.")[0] + ".thumb")
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new SimpleTarget<GlideDrawable>() {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            Schedulers.io().createWorker().schedule(() -> panoramicView720Ext.loadImage(BitmapUtils.drawableToBitmap(resource)));
//                            refreshControllerView(true);
                        }
                    });
        } else if (bean != null) {
            Glide.with(this)
                    .load(MiscUtils.getCamWarnUrl(uuid, bean, bean.alarmMsg.fileIndex))
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(true)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                            refreshControllerView(true);
                            panoramicView720Ext.loadImage(resource);
                        }
                    });
        }

        bottomVideoMenuPlay.setImageResource(R.drawable.icon_play_selector);
        LoadingDialog.dismissLoading();
    }

    @Override
    public void OnPlayerFailed(long l) {
        AppLogger.d("播放器初始化失败了");
        runOnUiThread(this::loadPreview);

    }

    @Override
    public void OnPlayerFinish(long l) {
        AppLogger.d("播放完成了");
        isPlay = false;
        if (looper) {
            runOnUiThread(() -> initPanoramaContent(panoramaItem));
        }
    }

    @Override
    public void OnUpdateProgress(long l, int i) {
        AppLogger.d("当前播放进度为:" + i + "," + l);
        runOnUiThread(() -> updateProgress(i, panoramaPanelSeekBar.getMax()));
    }

    @Override
    public void onDeleteResult(int code) {
        if (deleted != null) {
            deleted.setEnabled(true);
        }
//        if (code == 0) {
//            ToastUtil.showPositiveToast(getstr);
//        } else if (code == -1) {
//            ToastUtil.showNegativeToast("本地已删除,设备端删除失败");
//        }
        if (code == 0) {
            RxEvent.DeletePanoramaItem deletePanoramaItem = new RxEvent.DeletePanoramaItem();
            deletePanoramaItem.position = getIntent().getIntExtra("panorama_position", 0);
            RxBus.getCacheInstance().post(deletePanoramaItem);
            RxBus.getCacheInstance().postSticky(new RxEvent.ClearDataEvent(505));
            finish();
        } else {
            AppLogger.e("删除失败了");
        }
    }

    @Override
    public void onReportDeviceError(int i, boolean b) {
        if (i == 2004) {
            if (BasePanoramaApiHelper.getInstance().getDeviceIp() != null) {
                new AlertDialog.Builder(this)
                        .setMessage(R.string.MSG_SD_OFF)
                        .setCancelable(false)
                        .setPositiveButton(R.string.OK, (dialog, which) -> {
                            if (downloadInfo == null || downloadInfo.getState() != DownloadManager.FINISH) {
                                finish();
                            }
                        })
                        .show();
            }
        }
    }

    @Override
    public void onRefreshConnectionMode(int type) {
        if (type == -1) {
            //无网络连接
            ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1));
            if (panoramaItem.type == PanoramaAlbumContact.PanoramaItem.PANORAMA_ITEM_TYPE.TYPE_VIDEO) {
                refreshControllerView(false);
            }
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            if (panoramaItem.type == PanoramaAlbumContact.PanoramaItem.PANORAMA_ITEM_TYPE.TYPE_VIDEO) {
                initPanoramaContent(panoramaItem);
            }
        } else if (type == ConnectivityManager.TYPE_MOBILE) {

        }
    }

    @Override
    public void onSingleTap(float v, float v1) {
        float translationY = headerTitleContainer.getTranslationY();
        int orientation = getResources().getConfiguration().orientation;

//        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        if (translationY != 0) {
            headerTitleContainer.postDelayed(() -> headerTitleContainer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE), 200);
        } else {
            headerTitleContainer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
//        }


        YoYo.with(headerTitleContainer.getTranslationY() != 0 ? Techniques.SlideInDown : Techniques.SlideOutUp).duration(200)
                .playOn(headerTitleContainer);
        YoYo.with(panoramaPanelSwitcher.getTranslationY() != 0 ? Techniques.SlideInUp : Techniques.SlideOutDown).duration(200)
                .playOn(panoramaPanelSwitcher);
    }

    @Override
    public void onSnapshot(Bitmap bitmap, boolean b) {
        ToastUtil.showPositiveToast(getString(R.string.SAVED_PHOTOS));
        Schedulers.io().createWorker().schedule(() -> BitmapUtils.saveBitmap2file(bitmap, JConstant.MEDIA_PATH + "/" + System.currentTimeMillis() / 1000 + ".jpg"));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateProgress(seekBar.getProgress(), seekBar.getMax());
    }

    private void updateProgress(int progress, int max) {
        LoadingDialog.dismissLoading();
        panoramaPanelSeekBar.setProgress(progress);
        bottomVideoMenuPlayTime.setText(String.format("%s/%s", TimeUtils.getMM_SS(progress), TimeUtils.getMM_SS(max)));
    }

    @OnClick(R.id.act_panorama_detail_bottom_video_menu_play)
    public void play() {
        if (player != 0) {
            if (isPlay) {
                JFGPlayer.Pause(player);
                bottomVideoMenuPlay.setImageResource(R.drawable.icon_play_selector);
                isPlay = false;
            } else {
                JFGPlayer.Resume(player);
                isPlay = true;
                bottomVideoMenuPlay.setImageResource(R.drawable.icon_suspend_selector);

            }
        } else {
            AppLogger.d("播放器初始化失败");
            initPanoramaContent(panoramaItem);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        updateProgress(seekBar.getProgress(), seekBar.getMax());
        Schedulers.io().createWorker().schedule(() -> {
            AppLogger.e("正在 seek");
            if (player != 0) {
                JFGPlayer.Seek(player, seekBar.getProgress());
            }
        });
    }

    private void refreshControllerView(boolean enable) {
        topMenuShare.setEnabled(enable);
        topMenuMore.setEnabled(enable);
        bottomVideoMenuPlay.setEnabled(enable);
        bottomVideoMenuPicture.setEnabled(enable);
        bottomPictureMenuPicture.setEnabled(enable);
        bottomPictureMenuVR.setEnabled(enable);
        bottomVideoMenuVR.setEnabled(enable);
        bottomVideoMenuMode.setEnabled(enable);
        bottomPictureMenuMode.setEnabled(enable);
        bottomPictureMenuGyroscope.setEnabled(enable);
        bottomVideoMenuGyroscope.setEnabled(enable);
        panoramaPanelSeekBar.setEnabled(enable);
        bottomVideoMenuPlayTime.setEnabled(enable);
        if (!enable) {
            panoramaPanelSeekBar.setProgress(0);
            bottomVideoMenuPlayTime.setText(String.format("%s/%s", TimeUtils.getMM_SS(0), TimeUtils.getMM_SS(panoramaPanelSeekBar.getMax())));
        }
    }
}
