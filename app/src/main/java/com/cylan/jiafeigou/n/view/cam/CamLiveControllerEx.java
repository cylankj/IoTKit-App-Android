package com.cylan.jiafeigou.n.view.cam;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
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
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.AttributeSet;
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

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.SimpleCache;
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
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.view.activity.SightSettingActivity;
import com.cylan.jiafeigou.n.view.media.NormalMediaFragment;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LiveTimeLayout;
import com.cylan.jiafeigou.widget.Switcher;
import com.cylan.jiafeigou.widget.flip.FlipImageView;
import com.cylan.jiafeigou.widget.flip.FlipLayout;
import com.cylan.jiafeigou.widget.live.ILiveControl;
import com.cylan.jiafeigou.widget.live.LiveControlView;
import com.cylan.jiafeigou.widget.pop.RelativePopupWindow;
import com.cylan.jiafeigou.widget.pop.RoundCardPopup;
import com.cylan.jiafeigou.widget.video.LiveViewWithThumbnail;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;
import com.cylan.jiafeigou.widget.wheel.ex.DataExt;
import com.cylan.jiafeigou.widget.wheel.ex.IData;
import com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt;
import com.cylan.panorama.CameraParam;
import com.cylan.panorama.Panoramic360ViewRS;
import com.daimajia.androidanimations.library.BaseViewAnimator;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_501_CAMERA_ALARM_FLAG;
import static com.cylan.jiafeigou.misc.JConstant.KEY_CAM_SIGHT_SETTING;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_IDLE;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_LOADING_FAILED;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_NET_CHANGED;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PLAYING;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PREPARE;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_STOP;
import static com.cylan.jiafeigou.misc.JFGRules.PlayErr.STOP_MAUNALLY;
import static com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract.TYPE_HISTORY;
import static com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract.TYPE_LIVE;

/**
 * Created by hds on 17-4-19.
 */

public class CamLiveControllerEx extends RelativeLayout implements ICamLiveLayer,
        View.OnClickListener {
    private static final long DAMP_DISTANCE = 2000L;
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
    FrameLayout layoutA;

    //流量
    @BindView(R.id.layout_b)
    TextView layoutB;

    //流量
    @BindView(R.id.vs_wheel)
    ViewSwitcher vsLayoutWheel;

    //loading
    @BindView(R.id.layout_c)
    LiveControlView layoutC;
    @BindView(R.id.layout_port_flip)
    FlipLayout layoutPortFlip;
    @BindView(R.id.live_time_layout)
    LiveTimeLayout liveTimeLayout;
    @BindView(R.id.imgV_cam_zoom_to_full_screen)
    ImageView imgVCamZoomToFullScreen;
    //防护  |直播|时间|   |全屏|
    @BindView(R.id.layout_d)
    FrameLayout layoutD;
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
    FrameLayout layoutE;
    @BindView(R.id.imgV_cam_switch_speaker)
    ImageView imgVCamSwitchSpeaker;
    @BindView(R.id.imgV_cam_trigger_mic)
    ImageView imgVCamTriggerMic;
    @BindView(R.id.imgV_cam_trigger_capture)
    ImageView imgVCamTriggerCapture;
    //|speaker|mic|capture|
    @BindView(R.id.layout_f)
    FrameLayout layoutF;
    @BindView(R.id.sv_switch_stream)
    Switcher svSwitchStream;

    @BindView(R.id.v_live)
    LiveViewWithThumbnail liveViewWithThumbnail;
    @BindView(R.id.sw_cam_live_wheel)
    SuperWheelExt superWheelExt;


    //圆形 柱状 四分一 模式切换
    @BindView(R.id.layout_g)
    FrameLayout layoutG;
    @BindView(R.id.rg_view_mode_switch_parent)
    RadioGroup rbViewModeSwitchParent;
    @BindView(R.id.iv_view_mode_switch)
    ImageView ivViewModeSwitch;
    @BindView(R.id.ll_switch_view_mode)
    LinearLayout ll_view_mode_container;
    @BindView(R.id.imgV_land_cam_switch_xunhuan)
    ImageView ivModeXunHuan;


    private String uuid;
    private static final String TAG = "CamLiveControllerEx";
    private ILiveControl.Action action;


    private float portRatio = -1;
    @Deprecated //boolean 只能表示两种状况,已不适应当前需求,用 pid 来判断更灵活,创建播放 view 也应该用 pid 来判断
    private boolean isNormalView;
    private int livePlayState;
    private int livePlayType;

    private OnClickListener liveTextClick;//直播按钮
    private OnClickListener liveTimeRectListener;
    private OnClickListener playClickListener;
    private RoundCardPopup roundCardPopup;

    private HistoryWheelHandler historyWheelHandler;

    private CamLiveContract.Presenter presenter;
    private int pid;
    private boolean isRSCam;
    private Handler handler = new Handler();
    private boolean needShowSight;

    /**
     * 设备的时区
     */
    private SimpleDateFormat liveTimeDateFormat;
    private VideoViewFactory.IVideoView videoView;
    private Device device;
    private YoYo.YoYoString landAnimationLayoutA;
    private YoYo.YoYoString landAnimationLayoutD;
    private YoYo.YoYoString landAnimationLayoutE;
    private YoYo.YoYoString landAnimationLayoutG;
    private YoYo.YoYoString flowTextAnimation;

    public CamLiveControllerEx(Context context) {
        this(context, null);
    }

    public CamLiveControllerEx(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CamLiveControllerEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        //竖屏 隐藏
        initListener();
    }

    private void initListener() {
//        PerformanceUtils.startTrace("initListener");
        //顶部
        //a.返回,speaker,mic,capture
        Context context = getContext();
        if (context instanceof FragmentActivity) {
            Log.d(TAG, TAG + " context is activity");
            imgVCamLiveLandNavBack.setOnClickListener(this);
            imgVLandCamSwitchSpeaker.setOnClickListener(this);
            imgVLandCamTriggerMic.setOnClickListener(this);
            imgVLandCamTriggerCapture.setOnClickListener(this);
            ivModeXunHuan.setOnClickListener(this);
        }
        //isFriend.流量
        //c.loading
        (layoutC).setAction(this.action);
        //d.time
        imgVCamZoomToFullScreen
                .setOnClickListener(this);
        //e.
        if (vsLayoutWheel.getCurrentView() instanceof FrameLayout) {
            tvLive.setVisibility(GONE);
            vFlag.setVisibility(GONE);
        }
        View vLandPlay = imgVCamLiveLandPlay;
        if (vLandPlay != null) vLandPlay.setOnClickListener(this);
        tvLive.setBackgroundColor(isLand() ? Color.TRANSPARENT : Color.WHITE);
        tvLive.setOnClickListener(this);
        //f
        imgVCamSwitchSpeaker.setOnClickListener(this);
        imgVCamTriggerMic.setOnClickListener(this);
        imgVCamTriggerCapture.setOnClickListener(this);
        btnLoadHistory.setOnClickListener(v -> {
            AppLogger.d("需要手动获取sd卡");
            getSdcardStatus();
        });
//        LayoutTransition transition = new LayoutTransition();
//
//        //使用翻转进入的动画代替默认动画
//        Animator appearAnim = ObjectAnimator
//                .ofFloat(null, "translateY", 90f, 0)
//                .setDuration(transition.getDuration(LayoutTransition.APPEARING));
//        transition.setAnimator(LayoutTransition.APPEARING, appearAnim);
//
//        //使用翻转消失的动画代替默认动画
//        Animator disappearAnim = ObjectAnimator.ofFloat(null, "translationY", 0,
//                90f).setDuration(
//                transition.getDuration(LayoutTransition.DISAPPEARING));
//        transition.setAnimator(LayoutTransition.DISAPPEARING, disappearAnim);
//
////        //使用滑动动画代替默认布局改变的动画
////        //这个动画会让视图滑动进入并短暂地缩小一半，具有平滑和缩放的效果
////        PropertyValuesHolder pvhSlide = PropertyValuesHolder.ofFloat("y", 0, 1);
////
////
////        //这里将上面三个动画综合
////        Animator changingDisappearAnim = ObjectAnimator.ofPropertyValuesHolder(this, pvhSlide);
////        changingDisappearAnim.setDuration(transition.getDuration(LayoutTransition.CHANGE_DISAPPEARING));
////        transition.setAnimator(LayoutTransition.CHANGE_DISAPPEARING,
////                changingDisappearAnim);
//
//        ll_view_mode_container.setLayoutTransition(transition);

        ivModeXunHuan.setEnabled(false);
        ivViewModeSwitch.setEnabled(false);
        ivViewModeSwitch.setOnClickListener(this::toggleSwitchMenu);
        rbViewModeSwitchParent.setOnCheckedChangeListener(this::switchViewMode);

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
        String dpPrimary = device.$(509, "1");
        if ("1".equals(dpPrimary) && JFGRules.hasViewAngle(device.pid)) {
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.SWITCH_VIEW_POP)
                    .setNegativeButton(R.string.CANCEL, null)
                    .setPositiveButton(R.string.OK, (dialog, which) -> {
                        try {
                            BaseApplication.getAppComponent().getSourceManager().updateValue(uuid, new DpMsgDefine.DPPrimary<String>("0"), DpMsgMap.ID_509_CAMERA_MOUNT_MODE);
                        } catch (IllegalAccessException e) {
                            AppLogger.e("err: ");
                        }
                        if (videoView != null && videoView instanceof Panoramic360ViewRS) {
                            rbViewModeSwitchParent.check(getCheckIdByViewMode(((Panoramic360ViewRS) videoView).getDisplayMode()));
                        }
                        rbViewModeSwitchParent.setVisibility(rbViewModeSwitchParent.getVisibility() == VISIBLE ? GONE : VISIBLE);
                    });
        } else if ("0".equals(dpPrimary) || !JFGRules.hasViewAngle(device.pid)) {
            if (videoView != null && videoView instanceof Panoramic360ViewRS) {
                rbViewModeSwitchParent.check(getCheckIdByViewMode(((Panoramic360ViewRS) videoView).getDisplayMode()));
            }
            rbViewModeSwitchParent.setVisibility(rbViewModeSwitchParent.getVisibility() == VISIBLE ? GONE : VISIBLE);

        } else {
            // TODO: 2017/8/18 怎么处理好呢?
            AppLogger.d("怎么处理呢?没有开启平视,但又支持视图切换功能");
        }
//        try {
//            //0:俯视
//            rbtnSightHorizontal.setChecked(TextUtils.equals("1", dpPrimary));
//            rbtnSightVertical.setChecked(TextUtils.equals("0", dpPrimary));
//        } catch (Exception e) {
//        }
    }

    private void switchViewMode(RadioGroup radioGroup, int checkId) {
        switch (checkId) {
            case R.id.rb_view_mode_circular:
                if (videoView != null && videoView instanceof Panoramic360ViewRS) {
                    ((Panoramic360ViewRS) videoView).setDisplayMode(Panoramic360ViewRS.SFM_Normal);
                }
                break;
            case R.id.rb_view_mode_column:
                if (videoView != null && videoView instanceof Panoramic360ViewRS) {
                    ((Panoramic360ViewRS) videoView).setDisplayMode(Panoramic360ViewRS.SFM_Cylinder);
                }
                break;
            case R.id.rb_view_mode_four:
                if (videoView != null && videoView instanceof Panoramic360ViewRS) {
                    ((Panoramic360ViewRS) videoView).setDisplayMode(Panoramic360ViewRS.SFM_Quad);
                }
                break;
        }
        rbViewModeSwitchParent.setVisibility(GONE);
//        if (videoView != null && videoView instanceof Panoramic360ViewRS) {
//            ivModeXunHuan.setEnabled(((Panoramic360ViewRS) videoView).getDisplayMode() == Panoramic360ViewRS.SFM_Cylinder);
//        }
    }

    private void getSdcardStatus() {
        Subscription subscription = Observable.just(new DPEntity()
                .setMsgId(204)
                .setUuid(uuid)
                .setAction(DBAction.QUERY)
                .setVersion(0)
                .setOption(DBOption.SingleQueryOption.ONE_BY_TIME))
                .subscribeOn(Schedulers.io())
                .flatMap(entity -> BaseApplication.getAppComponent().getTaskDispatcher().perform(entity))
                .map(ret -> {
                    try {
                        DpMsgDefine.DPSdStatus sdStatus = ret.getResultResponse();
                        return sdStatus;
                    } catch (Exception e) {
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
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
                        toLoadingHistory();
                    }
                }, throwable -> ToastUtil.showToast(getResources().getString(R.string.NO_SDCARD)));

        presenter.addSubscription("getSdcardStatus", subscription);
    }

    private void toLoadingHistory() {
        AppLogger.d("点击加载历史视频");
        btnLoadHistory.setEnabled(false);
        livePlayState = PLAY_STATE_PREPARE;
        setLoadingState(getResources().getString(R.string.VIDEO_REFRESHING), null);
        Subscription subscription = Observable.just("get")
                .subscribeOn(Schedulers.io())
                .map(ret -> presenter.fetchHistoryDataList())
                .flatMap(aBoolean -> RxBus.getCacheInstance().toObservable(RxEvent.HistoryBack.class)
                        .timeout(30, TimeUnit.SECONDS).first())
                .flatMap(o -> Observable.just(o.isEmpty))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isEmpty -> {
                    AppLogger.d("加载成功:" + isEmpty);
                    presenter.startPlayHistory(DataExt.getInstance().getFlattenMinTime());
                    btnLoadHistory.setEnabled(true);
                    if (isEmpty) {
                        ToastUtil.showToast(getResources().getString(R.string.NO_CONTENTS_2));
                        livePlayState = PLAY_STATE_STOP;
                        setLoadingState(PLAY_STATE_STOP, null);
                        return;
                    }
                    if (vsLayoutWheel.getCurrentView() instanceof ViewGroup) {
                        vsLayoutWheel.showNext();
                        livePlayState = PLAY_STATE_STOP;
                        setLoadingState(PLAY_STATE_STOP, null);
                        tvLive.setVisibility(VISIBLE);
                        vFlag.setVisibility(VISIBLE);
                        AppLogger.d("需要展示 遮罩");
                    }
                }, throwable -> {
                    if (throwable instanceof TimeoutException) {
                        btnLoadHistory.setEnabled(true);
                        livePlayState = PLAY_STATE_STOP;
                        setLoadingState(PLAY_STATE_STOP, null);
                        if (presenter != null
                                && presenter.getHistoryDataProvider() != null
                                && presenter.getHistoryDataProvider().getDataCount() == 0)
                            ToastUtil.showToast(getResources().getString(R.string.Item_LoadFail));
                    }
                });
        presenter.addSubscription("fetchHistoryBy", subscription);
    }

    @Override
    public void initLiveViewRect(float ratio, Rect rect) {
        updateLiveViewRectHeight(ratio);
        liveViewWithThumbnail.post(() -> {
            liveViewWithThumbnail.getLocalVisibleRect(rect);
            AppLogger.d("rect: " + rect);
        });
    }

    @Override
    public void initView(CamLiveContract.Presenter presenter, String uuid) {
        this.presenter = presenter;
        this.uuid = uuid;
        needShowSight = JFGRules.showSight(presenter.getDevice().pid, JFGRules.isShareDevice(uuid));
        //disable 6个view
        setHotSeatState(-1, false, false, false, false, false, false);
        imgVLandCamTriggerCapture.setEnabled(false);
        imgVCamTriggerCapture.setEnabled(false);
        imgVCamZoomToFullScreen.setEnabled(false);
        tvLive.setEnabled(false);
        device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        isRSCam = JFGRules.isRS(device.pid);
        if (device == null) {
            AppLogger.e("device is null");
            return;
        }
        this.pid = device.pid;
        videoView = VideoViewFactory.CreateRendererExt(device.pid, getContext());
        videoView.setInterActListener(new VideoViewFactory.InterActListener() {

            @Override
            public boolean onSingleTap(float x, float y) {
//                camLiveController.tapVideoViewAction();
                onLiveRectTap();
                return true;
            }

            @Override
            public void onSnapshot(Bitmap bitmap, boolean tag) {
                Log.d("onSnapshot", "onSnapshot: " + (bitmap == null));
            }
        });
        liveViewWithThumbnail.setLiveView(videoView);
        updateLiveViewMode(device.$(509, "1"));
        initSightSetting(presenter);
        //分享用户不显示
        boolean showFlip = !presenter.isShareDevice() && JFGRules.hasProtection(device.pid, false);
        layoutPortFlip.setVisibility(showFlip ? VISIBLE : INVISIBLE);
        //要根据设备属性表决定是否显示加载历史视频的按钮
        layoutLandFlip.setVisibility(showFlip && MiscUtils.isLand() ? VISIBLE : GONE);
        vDivider.setVisibility(showFlip && MiscUtils.isLand() ? VISIBLE : GONE);
        //是否显示清晰度切换
        int mode = device.$(513, 0);
        svSwitchStream.setMode(mode);
        svSwitchStream.setSwitcherListener((view, index) -> {
            if (view.getId() == R.id.switch_hd) {
                presenter.switchStreamMode(index)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(ret -> {
                        }, AppLogger::e);
            } else if (view.getId() == R.id.switch_sd) {
                presenter.switchStreamMode(index)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(ret -> {
                        }, AppLogger::e);
            }
            if (MiscUtils.isLand()) {
                removeCallbacks(landHideRunnable);
                postDelayed(landHideRunnable, 3000);
            } else {
                removeCallbacks(portHideRunnable);
//                postDelayed(portHideRunnable, 3000);
            }
        });
        layoutD.setVisibility(livePlayState == PLAY_STATE_PLAYING ? VISIBLE : INVISIBLE);
        layoutG.setVisibility(livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
        ivModeXunHuan.setVisibility(JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
        ivViewModeSwitch.setEnabled(livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid));
        ivModeXunHuan.setEnabled(livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid));
        liveTimeLayout.setVisibility(JFGRules.hasSDFeature(device.pid) ? VISIBLE : INVISIBLE);
        AppLogger.d("需要重置清晰度");
    }

    private void updateCamParam(DpMsgDefine.DpCoordinate coord) {
        try {
            CameraParam cp = new CameraParam(coord.x, coord.y, coord.r, coord.w, coord.h, 180);
            if (cp.cx == 0 && cp.cy == 0 && cp.h == 0) {
                cp = CameraParam.getTopPreset();
            }
            liveViewWithThumbnail.getVideoView().config360(cp);
        } catch (Exception e) {
        }
    }

    public HistoryWheelHandler getHistoryWheelHandler(CamLiveContract.Presenter presenter) {
        reInitHistoryHandler(presenter);
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

        if (!needShowSight || basePresenter.isShareDevice()) return;
        String uuid = basePresenter.getUuid();
        isSightShown = PreferencesUtils.getBoolean(KEY_CAM_SIGHT_SETTING + uuid, true);
        Log.d("initSightSetting", "judge? " + isSightShown);
        if (!isSightShown) return;//不是第一次
        layoutE.setVisibility(INVISIBLE);//需要隐藏历史录像时间轴
        View oldLayout = liveViewWithThumbnail.findViewById(R.id.fLayout_cam_sight_setting);
        if (oldLayout == null) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.cam_sight_setting_overlay, null);
            liveViewWithThumbnail.addView(view);//最顶
            View layout = liveViewWithThumbnail.findViewById(R.id.fLayout_cam_sight_setting);
            ((TextView) (view.findViewById(R.id.tv_sight_setting_content)))
                    .setText(getContext().getString(R.string.Tap1_Camera_Overlook) + ": "
                            + getContext().getString(R.string.Tap1_Camera_OverlookTips));
            view.findViewById(R.id.btn_sight_setting_cancel).setOnClickListener((View v) -> {
                if (layout != null) liveViewWithThumbnail.removeView(layout);
//                basePresenter.startPlay();
                livePlayState = PLAY_STATE_STOP;
                setLoadingState(PLAY_STATE_STOP, null);
                //需要隐藏历史录像时间轴
                boolean showSdcard = JFGRules.showSdcard(basePresenter.getDevice());
                layoutE.setVisibility(showSdcard
                        ? VISIBLE : INVISIBLE);
                vsLayoutWheel.setVisibility(showSdcard ? VISIBLE : INVISIBLE);
            });
            layout.setOnClickListener(v -> AppLogger.d("don't click me"));
            view.findViewById(R.id.btn_sight_setting_next).setOnClickListener((View v) -> {
                liveViewWithThumbnail.removeView(layout);
                Intent intent = new Intent(getContext(), SightSettingActivity.class);
                intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                getContext().startActivity(intent);
            });
            PreferencesUtils.putBoolean(KEY_CAM_SIGHT_SETTING + uuid, false);
        } else {
            //已经添加了
            oldLayout.setVisibility(View.VISIBLE);
        }
        livePlayState = PLAY_STATE_IDLE;
        setLoadingState(null, null);
    }

    /**
     * 视频区域
     */
    private void onLiveRectTap() {
        AppLogger.e("点击,需要播放状态");
        if (isLand()) {
            removeCallbacks(landShowOrHideRunnable);
            post(landShowOrHideRunnable);
        } else {
            if (isStandBy()) {
                post(portHideRunnable);
                return;
            }
            //只有播放的时候才能操作//loading的时候 不能点击|| livePlayState == PLAY_STATE_STOP
            if (livePlayState == PLAY_STATE_PLAYING) {
//                layoutA.setTranslationY(0);
//                layoutD.setTranslationY(0);
//                layoutE.setTranslationY(0);
                boolean toHide = layoutC.isShown();
                if (toHide) {
                    removeCallbacks(portShowRunnable);
                    post(portHideRunnable);
                } else {
                    removeCallbacks(portHideRunnable);
                    post(portShowRunnable);
                }
            }
//            if (!toHide) prepareLayoutDAnimation();
        }
    }

    /**
     * 历史录像条显示逻辑
     *
     * @param show
     */
    private void showHistoryWheel(boolean show) {
        //处理显示逻辑
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        //4.被分享用户不显示
        if (JFGRules.isShareDevice(device)) {
            AppLogger.d("is share device");
            layoutE.setVisibility(INVISIBLE);
            return;
        }
        //3.没有历史录像
        if (superWheelExt.getDataProvider() != null && superWheelExt.getDataProvider().getDataCount() > 0) {
            //显示
            AppLogger.d("has history video");
            layoutE.setVisibility(VISIBLE);
            return;
        }
        layoutE.setVisibility(show ? VISIBLE : INVISIBLE);
    }

    @Override
    public void onLivePrepared(int type) {
        livePlayType = type;
        livePlayState = PLAY_STATE_PREPARE;
        setLoadingState(null, null);
        imgVCamZoomToFullScreen.setEnabled(false);
        int net = NetUtils.getJfgNetType();
        if (net == 2)
            ToastUtil.showToast(getResources().getString(R.string.LIVE_DATA));
    }

    private boolean isLand() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * 3s隐藏
     */
    private void prepareLayoutDAnimation(boolean touchUp) {
        if (MiscUtils.isLand()) {
            removeCallbacks(landHideRunnable);
            if (touchUp) postDelayed(landHideRunnable, 3000);
        } else {
            post(portShowRunnable);
        }
    }

    private Runnable portHideRunnable = new Runnable() {
        @Override
        public void run() {
            setLoadingState(null, null);
            svSwitchStream.setVisibility(GONE);
            layoutC.setVisibility(INVISIBLE);
            Log.d("wahat", "portHideRunnable");
        }
    };

    private Runnable portShowRunnable = new Runnable() {
        @Override
        public void run() {
            layoutD.setVisibility(livePlayState == PLAY_STATE_PLAYING ? VISIBLE : INVISIBLE);
            layoutG.setVisibility(livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
            ivModeXunHuan.setVisibility(JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
            ivViewModeSwitch.setEnabled(livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid));
            ivModeXunHuan.setEnabled(livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid));
            if (layoutD.getAlpha() == 0.0f)
                YoYo.with(Techniques.FadeIn)
                        .duration(200)
                        .playOn(layoutD);
            showHistoryWheel(true);
            removeCallbacks(portHideRunnable);
            postDelayed(portHideRunnable, 3000);
            setLoadingState(null, null);
            svSwitchStream.setVisibility(livePlayState == PLAY_STATE_PLAYING && JFGRules.showSdHd(pid, presenter.getDevice().$(207, ""), false) ? VISIBLE : GONE);
            if (livePlayState == PLAY_STATE_PLAYING) {
                layoutC.setVisibility(VISIBLE);
            }
            Log.d("wahat", "portShowRunnable");
        }
    };


    private Runnable landHideRunnable = new Runnable() {


        @Override
        public void run() {
            if (historyWheelHandler != null && historyWheelHandler.isBusy()) {
                //滑动过程
                postDelayed(this, 3000);
                return;
            }
            setLoadingState(null, null);
            if (livePlayState == PLAY_STATE_PLAYING) {
                layoutC.setVisibility(INVISIBLE);
            } else if (livePlayState == PLAY_STATE_PREPARE) {
                layoutC.setVisibility(VISIBLE);//loading 必须显示
            }
            svSwitchStream.setVisibility(GONE);
            if (landAnimationLayoutA != null) landAnimationLayoutA.stop();
            landAnimationLayoutA = YoYo.with(Techniques.FadeOutUp)
                    .duration(250)
                    .playOn(layoutA);
            if (landAnimationLayoutD != null) landAnimationLayoutD.stop();
            landAnimationLayoutD = YoYo.with(new BaseViewAnimator() {
                @Override
                protected void prepare(View target) {
                    ViewGroup parent = (ViewGroup) target.getParent();
                    int distance = layoutE.getHeight();
                    this.getAnimatorAgent().play(ObjectAnimator.ofFloat(target, "translationY", 0.0F, (float) distance));

                }
            })
                    .duration(250)
                    .playOn(layoutD);
            if (landAnimationLayoutE != null) landAnimationLayoutE.stop();
            landAnimationLayoutE = YoYo.with(Techniques.FadeOutDown)
                    .duration(250)
                    .withListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (!isLand()) {
                                layoutE.setTranslationY(0);
                                layoutE.setAlpha(1);
                            }
                        }
                    })
                    .playOn(layoutE);
            if (landAnimationLayoutG != null) landAnimationLayoutG.stop();
            landAnimationLayoutG = YoYo.with(Techniques.FadeOutDown)
                    .duration(250)
                    .playOn(layoutG);
            if (flowTextAnimation != null) flowTextAnimation.stop();
            flowTextAnimation = YoYo.with(new BaseViewAnimator() {
                @Override
                protected void prepare(View target) {
                    int distance = layoutA.getHeight();
                    this.getAnimatorAgent().play(ObjectAnimator.ofFloat(target, "translationY", 0.0F, -(float) distance));
                }
            })
                    .duration(250)
                    .playOn(liveViewWithThumbnail.getTvLiveFlow());
        }
    };

    private boolean showSdHdBtn() {
        return livePlayState == PLAY_STATE_PLAYING
                && livePlayType == TYPE_LIVE
                && JFGRules.showSdHd(pid, presenter.getDevice().$(207, ""), false);
    }

    private Runnable landShowRunnable = new Runnable() {

        @Override
        public void run() {
            setLoadingState(null, null);
            if (livePlayState == PLAY_STATE_PLAYING) {
                layoutC.setVisibility(INVISIBLE);//全屏直播门铃 1.需要去掉中间播放按钮
            }
            svSwitchStream.setVisibility(showSdHdBtn() ? VISIBLE : GONE);

            YoYo.with(Techniques.FadeInDown)
                    .duration(250)
                    .playOn(layoutA);
            if (!layoutD.isShown())
                layoutD.setVisibility(VISIBLE);//
            if (landAnimationLayoutD != null) landAnimationLayoutD.stop();
            landAnimationLayoutD = YoYo.with(new BaseViewAnimator() {
                @Override
                protected void prepare(View target) {
                    ViewGroup parent = (ViewGroup) target.getParent();
                    int distance = layoutE.getHeight();
                    this.getAnimatorAgent().play(ObjectAnimator.ofFloat(target, "translationY", (float) (distance), 0.0F));
                }
            })
                    .duration(250)
                    .playOn(layoutD);
            if (!layoutE.isShown()) layoutE.setVisibility(VISIBLE);//
            Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
            if (device != null && JFGRules.isShareDevice(device)) {
                vsLayoutWheel.setVisibility(INVISIBLE);
            }
            if (landAnimationLayoutE != null) landAnimationLayoutE.stop();
            landAnimationLayoutE = YoYo.with(Techniques.FadeInUp)
                    .duration(250)
                    .playOn(layoutE);
            if (landAnimationLayoutG != null) landAnimationLayoutG.stop();
            landAnimationLayoutG = YoYo.with(Techniques.FadeInUp)
                    .duration(200)
                    .playOn(layoutG);
            if (flowTextAnimation != null) flowTextAnimation.stop();
            flowTextAnimation = YoYo.with(new BaseViewAnimator() {
                @Override
                protected void prepare(View target) {
                    int distance = layoutA.getHeight();
                    this.getAnimatorAgent().play(ObjectAnimator.ofFloat(target, "translationY", (float) (-distance), 0.0F));
                }
            })
                    .duration(250)
                    .playOn(liveViewWithThumbnail.getTvLiveFlow());
            postDelayed(landHideRunnable, 3000);
        }
    };

    private Runnable landShowOrHideRunnable = new Runnable() {

        @Override
        public void run() {
            float t = layoutA.getTranslationY();
            if (layoutA.getTranslationY() != 0) {
//                if (t == -layoutA.getMeasuredHeight()) {//显示是 fade 动画 不能以相等来判断了
                //显示
                removeCallbacks(landShowRunnable);
                removeCallbacks(landHideRunnable);
                post(landShowRunnable);
                Log.e(TAG, "点击 显示");
//                }
            } else {
                //横屏,隐藏
                removeCallbacks(landShowRunnable);
                removeCallbacks(landHideRunnable);
                post(landHideRunnable);
                Log.e(TAG, "点击 隐藏");
            }
        }
    };

    @Override
    public void onLiveStart(CamLiveContract.Presenter presenter, Device device) {
        livePlayType = presenter.getPlayType();
        livePlayState = PLAY_STATE_PLAYING;
        boolean isPlayHistory = livePlayType == TYPE_HISTORY;
        //左下角直播,竖屏下:左下角按钮已经隐藏
        ((ImageView) imgVCamLiveLandPlay).setImageResource(R.drawable.icon_landscape_playing);
        //|直播| 按钮
        post(portShowRunnable);
        //现在显示的条件就是手动点击其他情况都不显示
        setLoadingState(null, null);
        post(() -> layoutC.setVisibility(INVISIBLE));
        if (tvLive != null) tvLive.setEnabled(isPlayHistory);
        imgVCamTriggerCapture.setEnabled(true);
        imgVLandCamTriggerCapture.setEnabled(true);
        //直播
        tvLive.setEnabled(livePlayType == TYPE_HISTORY);
        liveViewWithThumbnail.onLiveStart();
        imgVCamZoomToFullScreen.setEnabled(true);
        //暂时隐藏吧,用户不喜欢
        post(portHideRunnable);
        layoutG.setVisibility(livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
        ivModeXunHuan.setVisibility(JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
        ivModeXunHuan.setEnabled(livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid));
        ivViewModeSwitch.setEnabled(livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid));
    }


    private void setLoadingState(String content, String subContent) {
        layoutC.setState(livePlayState, content, subContent);
        if (!TextUtils.isEmpty(content) || !TextUtils.isEmpty(subContent))
            layoutC.setVisibility(VISIBLE);
        switch (livePlayState) {
            case PLAY_STATE_LOADING_FAILED:
            case PLAY_STATE_STOP:
            case PLAY_STATE_PREPARE:
                layoutC.setVisibility(VISIBLE);
                break;
            case PLAY_STATE_IDLE:
                layoutC.setVisibility(INVISIBLE);
                break;
        }
    }

    @Override
    public void onLiveStop(CamLiveContract.Presenter presenter, Device device, int errCode) {
        livePlayState = presenter.getPlayState();
        layoutB.setVisibility(GONE);
        imgVCamLiveLandPlay.setImageResource(R.drawable.icon_landscape_stop);
        liveViewWithThumbnail.setEnabled(true);
        liveViewWithThumbnail.showFlowView(false, null);
        imgVCamZoomToFullScreen.setEnabled(false);
        removeCallbacks(portHideRunnable);
        handlePlayErr(presenter, errCode);
        imgVLandCamTriggerCapture.setEnabled(false);
        imgVCamTriggerCapture.setEnabled(false);
        liveViewWithThumbnail.onLiveStop();
        layoutD.setVisibility(livePlayState == PLAY_STATE_PLAYING ? VISIBLE : INVISIBLE);
        layoutG.setVisibility(livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
        ivModeXunHuan.setVisibility(JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
        ivViewModeSwitch.setEnabled(livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid));
        ivModeXunHuan.setEnabled(livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid));
    }

    /**
     * 错误码 需要放在一个Map里面管理
     *
     * @param errCode
     */
    private void handlePlayErr(CamLiveContract.Presenter presenter, int errCode) {
        if (presenter.isDeviceStandby()) return;
        switch (errCode) {//这些errCode 应当写在一个map中.Map<Integer,String>
            case JFGRules.PlayErr.ERR_NETWORK:
                livePlayState = PLAY_STATE_LOADING_FAILED;
                setLoadingState(getContext().getString(R.string.OFFLINE_ERR_1), getContext().getString(R.string.USER_HELP));
                break;
            case JFGRules.PlayErr.ERR_UNKOWN:
                livePlayState = PLAY_STATE_LOADING_FAILED;
                setLoadingState(getContext().getString(R.string.NO_NETWORK_2), null);
                break;
            case JFGRules.PlayErr.ERR_LOW_FRAME_RATE:
                int net = NetUtils.getJfgNetType(getContext());
                livePlayState = PLAY_STATE_LOADING_FAILED;
                setLoadingState(getContext().getString(R.string.GLOBAL_NO_NETWORK), net == 0 ? getContext().getString(R.string.USER_HELP) : null);
                break;
            case STOP_MAUNALLY:
            case PLAY_STATE_STOP:
                livePlayState = PLAY_STATE_STOP;
                setLoadingState(null, null);
                break;
            case JFGRules.PlayErr.ERR_NOT_FLOW:
                livePlayState = PLAY_STATE_LOADING_FAILED;
                setLoadingState(getContext().getString(R.string.NETWORK_TIMEOUT), getContext().getString(R.string.USER_HELP));
                break;
            case JError.ErrorVideoPeerDisconnect:
                livePlayState = PLAY_STATE_LOADING_FAILED;
                setLoadingState(getContext().getString(R.string.Device_Disconnected), null);
                break;
            case JFGRules.PlayErr.ERR_DEVICE_OFFLINE:
                livePlayState = PLAY_STATE_LOADING_FAILED;
                setLoadingState(getContext().getString(R.string.OFFLINE_ERR), getContext().getString(R.string.USER_HELP));
                break;
            case JError.ErrorVideoPeerNotExist:
                livePlayState = PLAY_STATE_LOADING_FAILED;
                setLoadingState(getContext().getString(R.string.OFFLINE_ERR), getContext().getString(R.string.USER_HELP));
                break;
            case JError.ErrorVideoPeerInConnect:
                //正在直播...
                livePlayState = PLAY_STATE_LOADING_FAILED;
                setLoadingState(getContext().getString(R.string.CONNECTING), null);
                break;
            case PLAY_STATE_IDLE:
                livePlayState = PLAY_STATE_IDLE;
                setLoadingState(null, null);
                break;
            case PLAY_STATE_NET_CHANGED:
                livePlayState = PLAY_STATE_PREPARE;
                setLoadingState(null, null);
                break;
            case JError.ErrorSDHistoryAll:
                livePlayState = PLAY_STATE_LOADING_FAILED;
                setLoadingState(getContext().getString(R.string.Historical_Read), null);
                if (getContext() instanceof Activity)
                    AlertDialogManager.getInstance().showDialog((Activity) getContext(),
                            getContext().getString(R.string.Historical_Read),
                            getContext().getString(R.string.Historical_Read),
                            getContext().getString(R.string.OK), (DialogInterface dialog, int which) -> {
                                CamLiveContract.LiveStream prePlayType = presenter.getLiveStream();
                                prePlayType.type = TYPE_LIVE;
                                presenter.updateLiveStream(prePlayType);
                                presenter.startPlay();
                            });
                break;
            case JError.ErrorSDFileIO:
                livePlayState = PLAY_STATE_LOADING_FAILED;
                setLoadingState(getContext().getString(R.string.Historical_Failed), null);
                if (getContext() instanceof Activity)
                    AlertDialogManager.getInstance().showDialog((Activity) getContext(),
                            getContext().getString(R.string.Historical_Failed),
                            getContext().getString(R.string.Historical_Failed),
                            getContext().getString(R.string.OK), (DialogInterface dialog, int which) -> {
                                CamLiveContract.LiveStream prePlayType = presenter.getLiveStream();
                                prePlayType.type = TYPE_LIVE;
                                presenter.updateLiveStream(prePlayType);
                                presenter.startPlay();
                            });
                break;
            case JError.ErrorSDIO:
                livePlayState = PLAY_STATE_LOADING_FAILED;
                setLoadingState(getContext().getString(R.string.Historical_No), null);
                if (getContext() instanceof Activity)
                    AlertDialogManager.getInstance().showDialog((Activity) getContext(),
                            getContext().getString(R.string.Historical_No),
                            getContext().getString(R.string.Historical_No),
                            getContext().getString(R.string.OK), (DialogInterface dialog, int which) -> {
                                CamLiveContract.LiveStream prePlayType = presenter.getLiveStream();
                                prePlayType.type = TYPE_LIVE;
                                presenter.updateLiveStream(prePlayType);
                                presenter.startPlay();
                            });
                break;
            default:
                livePlayState = PLAY_STATE_LOADING_FAILED;
                setLoadingState(getContext().getString(R.string.GLOBAL_NO_NETWORK), null);
                break;
        }
    }

    @Override
    public void orientationChanged(CamLiveContract.Presenter presenter, Device device, int orientation) {
        int playType = presenter.getPlayType();
        boolean isLand = isLand();
        layoutA.setVisibility(isLand ? VISIBLE : INVISIBLE);
        layoutF.setVisibility(isLand ? INVISIBLE : VISIBLE);
        tvLive.setBackgroundColor(isLand ? Color.TRANSPARENT : Color.WHITE);
        if (isLand) {
            //隐藏所有的 showcase
            LiveShowCase.hideHistoryWheelCase((Activity) getContext());
            LiveShowCase.hideHistoryCase((Activity) getContext());
            if (vsLayoutWheel.getCurrentView() instanceof FrameLayout) {
                vsLayoutWheel.getCurrentView().setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }
            vLine.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            if (vsLayoutWheel.getCurrentView() instanceof FrameLayout) {
                tvLive.setVisibility(VISIBLE);
                vFlag.setVisibility(VISIBLE);
            }
            if (device != null && JFGRules.isShareDevice(device)) {
                vsLayoutWheel.setVisibility(INVISIBLE);
            }
            ViewUtils.increaseMargins(svSwitchStream, 0, 0, 0, (int) getResources().getDimension(R.dimen.y10));
        } else {
            if (vsLayoutWheel.getCurrentView() instanceof FrameLayout) {
                tvLive.setVisibility(GONE);
                vFlag.setVisibility(GONE);
            }
            IData dataProvider = presenter.getHistoryDataProvider();
            if (dataProvider != null && dataProvider.getDataCount() != 0) {
                if (vsLayoutWheel.getDisplayedChild() == 0) {
                    vsLayoutWheel.setVisibility(VISIBLE);
                    vsLayoutWheel.showNext();
                }
            } else if (vsLayoutWheel.getCurrentView() instanceof FrameLayout) {
                vsLayoutWheel.getCurrentView().setBackgroundColor(getResources().getColor(R.color.color_F7F8FA));
                vLine.setBackgroundColor(getResources().getColor(R.color.color_f2f2f2));
            }
            ViewUtils.increaseMargins(svSwitchStream, 0, 0, 0, -(int) getResources().getDimension(R.dimen.y10));
        }
        //历史录像显示
        boolean showFlip = !presenter.isShareDevice() && JFGRules.hasProtection(device.pid, false);
        layoutLandFlip.setVisibility(showFlip && isLand ? VISIBLE : GONE);
//        vDivider.setVisibility(showFlip && isLand ? VISIBLE : INVISIBLE);
        liveViewWithThumbnail.detectOrientationChanged(!isLand);

        if (presenter.getPlayState() != PLAY_STATE_PLAYING) {//显示缩略图

            Bitmap bitmap = SimpleCache.getInstance().getSimpleBitmapCache(presenter.getThumbnailKey());
            if (bitmap == null || bitmap.isRecycled()) {
                File file = new File(presenter.getThumbnailKey());
                liveViewWithThumbnail.setThumbnail(getContext(), PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, ""), Uri.fromFile(file));
            } else
                liveViewWithThumbnail.setThumbnail(getContext(), PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, ""), SimpleCache.getInstance().getSimpleBitmapCache(presenter.getThumbnailKey()));
        }

        //直播
        tvLive.setEnabled(playType == TYPE_HISTORY);
        @SuppressLint("WrongViewCast") LayoutParams lp = (LayoutParams) layoutE.getLayoutParams();
        @SuppressLint("WrongViewCast") LayoutParams glp = (LayoutParams) layoutG.getLayoutParams();
        if (isLand) {
            lp.removeRule(3);//remove below rules
            lp.addRule(2, R.id.v_guide);//set above v_guide
//            android:layout_above="@+id/layout_d"
            glp.addRule(RelativeLayout.ABOVE, R.id.layout_e);
            liveViewWithThumbnail.updateLayoutParameters(LayoutParams.MATCH_PARENT, getVideoFinalWidth());
            imgVCamZoomToFullScreen.setVisibility(INVISIBLE);
            layoutD.setBackgroundResource(android.R.color.transparent);
            layoutE.setBackgroundResource(R.color.color_4C000000);
            layoutPortFlip.setVisibility(INVISIBLE);
            //显示 昵称
            String alias = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
            ((TextView) imgVCamLiveLandNavBack)
                    .setText(alias);
            imgVCamLiveLandPlay.setVisibility(VISIBLE);

        } else {
            glp.addRule(RelativeLayout.ABOVE, R.id.layout_d);
            imgVCamLiveLandPlay.setVisibility(GONE);
            lp.removeRule(2);//remove above
            lp.addRule(3, R.id.v_guide); //set below v_guide
            imgVCamZoomToFullScreen.setVisibility(VISIBLE);
            updateLiveViewRectHeight(portRatio == -1 ? presenter.getVideoPortHeightRatio() : portRatio);
            //有条件的.
            if (presenter.getPlayState() == PLAY_STATE_PLAYING) {
                //需要根据设备属性表来决定是否显示或隐藏 portFlip
                layoutPortFlip.setVisibility(showFlip ? VISIBLE : INVISIBLE);
            }
            layoutD.setBackgroundResource(R.drawable.camera_sahdow);
            layoutE.setBackgroundResource(android.R.color.transparent);
        }

        layoutE.setLayoutParams(lp);
        layoutG.setLayoutParams(glp);
        resetAndPrepareNextAnimation(isLand);
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

    private void resetAndPrepareNextAnimation(boolean land) {
        //切换到了横屏,必须先恢复view的位置才能 重新开始动画
        layoutA.setTranslationY(0);
        layoutD.setTranslationY(0);
        layoutE.setTranslationY(0);
        layoutG.setTranslationY(0);
        liveViewWithThumbnail.getTvLiveFlow().setTranslationY(0);
        layoutA.setAlpha(1);
        layoutD.setAlpha(1);
        layoutE.setAlpha(1);
        layoutG.setAlpha(1);
        liveViewWithThumbnail.getTvLiveFlow().setAlpha(1);

        if (land) {
            layoutE.setVisibility(VISIBLE);
            removeCallbacks(portHideRunnable);
            removeCallbacks(landHideRunnable);
            removeCallbacks(landShowRunnable);
            postDelayed(landHideRunnable, 3000);//3s后隐藏
        } else {
            //只有播放是才显示 LayoutD
            layoutD.setVisibility(livePlayState == PLAY_STATE_PLAYING ? VISIBLE : INVISIBLE);
            layoutG.setVisibility(livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
            ivModeXunHuan.setVisibility(JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
            ivViewModeSwitch.setEnabled(livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid));
            ivModeXunHuan.setEnabled(livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid));
            removeCallbacks(portHideRunnable);
            removeCallbacks(landHideRunnable);
            removeCallbacks(landShowRunnable);
            if (livePlayState != PLAY_STATE_PLAYING) {//暂停或 idle 不隐藏
                post(portShowRunnable);
                post(() -> {
                    removeCallbacks(portHideRunnable);
                    removeCallbacks(landHideRunnable);

                });
            }
        }
    }

    @Override
    public void onRtcpCallback(int type, JFGMsgVideoRtcp rtcp) {
        livePlayState = PLAY_STATE_PLAYING;
        String flow = MiscUtils.getByteFromBitRate(rtcp.bitRate);
        liveViewWithThumbnail.showFlowView(true, flow);
        //暴力处理吧,不知道该在哪里隐藏了

        postDelayed(portHideRunnable, 3000);//#118022
//        postDelayed(landHideRunnable, 3000);
        postDelayed(() -> {

        }, 3000);
        //分享账号不显示啊.
        if (JFGRules.isShareDevice(uuid)) return;
//        if (!getHistoryWheelHandler(presenter).isBusy()) {//拖动时间轴时屏蔽 rtcp 时间更新,防止显示异常
        boolean isWheelBusy = historyWheelHandler != null && historyWheelHandler.isBusy();
        Log.d("setLiveRectTime", "isBusy?" + isWheelBusy);
        if (!isWheelBusy) {
            setLiveRectTime(livePlayType, rtcp.timestamp, true);
        }
        //点击事件
        if (liveTimeRectListener == null) {
            liveTimeRectListener = v -> {
                int net = NetUtils.getJfgNetType();
                if (net == 0) {
                    ToastUtil.showNegativeToast(getContext().getString(R.string.NoNetworkTips));
                    return;
                }
                Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
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
                if (historyWheelHandler == null || presenter.getHistoryDataProvider() == null ||
                        presenter.getHistoryDataProvider().getDataCount() == 0) {
                    ToastUtil.showToast(getResources().getString(R.string.History_video_Firstly));
                    return;
                }
                if (historyWheelHandler != null) {
                    ViewUtils.deBounceClick(v);
                    historyWheelHandler.showDatePicker(MiscUtils.isLand());
                }
            };
            (liveTimeLayout).setOnClickListener(liveTimeRectListener);
        }
    }

//    /**
//     * 时间轴刷新不需要 那么频繁
//     */
//    private long fuckTheTime;

    private void setLiveRectTime(int type, long timestamp, boolean useDamp) {
        //全景的时间戳是0,使用设备的时区
        //wifi狗是格林尼治时间戳,需要-8个时区.
        historyWheelHandler = getHistoryWheelHandler(presenter);

        boolean isWheelBusy = historyWheelHandler != null && historyWheelHandler.isBusy();
        boolean shouldUpdateWheelTime = !useDamp ||
                System.currentTimeMillis() - historyWheelHandler.getLastUpdateTime() > DAMP_DISTANCE
                || historyWheelHandler.getNextTimeDistance() > DAMP_DISTANCE;
        Log.d("useDamp", "useDamp:" + useDamp + ",touchDistance:" + (System.currentTimeMillis() - historyWheelHandler.getLastUpdateTime()) + ",nextDistance:" + historyWheelHandler.getNextTimeDistance());
        if (JFGRules.hasSDFeature(pid)) {
            ((LiveTimeLayout) liveTimeLayout).setContent(type, timestamp);
        }
        if (livePlayState == PLAY_STATE_PREPARE) return;
        if (!isWheelBusy && type == TYPE_HISTORY && presenter != null
                && presenter.getPlayState() == PLAY_STATE_PLAYING && shouldUpdateWheelTime) {
            Log.d("TYPE_HISTORY time", "time: " + timestamp);
            historyWheelHandler.setNav2Time(TimeUtils.wrapToLong(timestamp));
        }
    }

    private String getTime(long time) {
        if (liveTimeDateFormat == null)
            liveTimeDateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.UK);
        return liveTimeDateFormat.format(new Date(time));
    }

    public void setFlipListener(FlipImageView.OnFlipListener flipListener) {
        ((FlipLayout) layoutLandFlip).setFlipListener(flipListener);
        ((FlipLayout) layoutPortFlip).setFlipListener(flipListener);
    }

    public void setFlipped(boolean flip) {
        ((FlipLayout) layoutLandFlip).setFlipped(flip);
        ((FlipLayout) layoutPortFlip).setFlipped(flip);
    }


    @Override
    public void onResolutionRsp(JFGMsgVideoResolution resolution) {
        AppLogger.d("收到分辨率消息,正在准备直播");
        try {
            BaseApplication.getAppComponent().getCmd().enableRenderSingleRemoteView(true, (View) liveViewWithThumbnail.getVideoView());
        } catch (JfgException e) {
            AppLogger.e("err:" + MiscUtils.getErr(e));
        }
        Device device = getDevice();
        float ratio = JFGRules.isNeedNormalRadio(device.pid) ? (isLand() ? getLandFillScreen() : (float) resolution.height / resolution.width) :
                isLand() ? (float) Resources.getSystem().getDisplayMetrics().heightPixels /
                        Resources.getSystem().getDisplayMetrics().widthPixels : 1.0f;
        if (portRatio == -1 && !isLand()) portRatio = ratio;
        updateLiveViewRectHeight(ratio);
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

    @Override
    public void onHistoryDataRsp(CamLiveContract.Presenter presenter) {
        showHistoryWheel(true);
        reInitHistoryHandler(presenter);
        Log.d("onHistoryDataRsp", "onHistoryDataRsp");
        historyWheelHandler.dateUpdate();
        historyWheelHandler.setDatePickerListener((time, state) -> {
            //选择时间,更新时间区域
            AppLogger.d("onHistoryDataRsp");
            setLiveRectTime(TYPE_HISTORY, time, false);//wheelView 回调的是毫秒时间, rtcp 回调的是秒,这里要除以1000
        });
        tvCamLiveLandBottom.setVisibility(VISIBLE);
    }

    private void reInitHistoryHandler(CamLiveContract.Presenter presenter) {
        if (historyWheelHandler == null) {
            historyWheelHandler = new HistoryWheelHandler(superWheelExt, presenter);
        }
    }

    @Override
    public void onLiveDestroy() {
        //1.live view pause
        try {
            liveViewWithThumbnail.getVideoView().onPause();
            liveViewWithThumbnail.getVideoView().onDestroy();
        } catch (Exception e) {
        }
    }

    @Override
    public void onDeviceStandByChanged(Device device, OnClickListener clickListener) {
        //设置 standby view相关点击事件
        DpMsgDefine.DPStandby standby = device.$(508, new DpMsgDefine.DPStandby());
        DpMsgDefine.DPNet dpNet = device.$(201, new DpMsgDefine.DPNet());//http://yf.cylan.com.cn:82/redmine/issues/109805
        liveViewWithThumbnail.enableStandbyMode(standby.standby && dpNet.net > 0, clickListener, !TextUtils.isEmpty(device.shareAccount));
        if (standby.standby && JFGRules.isDeviceOnline(dpNet) && !isLand()) {
            post(portHideRunnable);
            setLoadingState(null, null);
        }
        if (!isLand()) {
            if (standby.standby) {
                layoutE.setVisibility(INVISIBLE);
            } else {
                boolean showSdcard = JFGRules.showSdcard(device);
                layoutE.setVisibility(showSdcard ? VISIBLE : INVISIBLE);
                vsLayoutWheel.setVisibility(showSdcard ? VISIBLE : INVISIBLE);
            }
        }
        btnLoadHistory.setEnabled(!standby.standby && device.$(201, new DpMsgDefine.DPNet()).net > 0);
    }

    private boolean isStandBy() {
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        DpMsgDefine.DPStandby standby = device.$(508, new DpMsgDefine.DPStandby());
        return standby.standby;
    }

    @Override
    public void onLoadPreviewBitmap(Bitmap bitmap) {
//        post(() -> liveViewWithThumbnail.setThumbnail(getContext(), PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, ""), bitmap));
    }

    @Override
    public void onCaptureRsp(FragmentActivity activity, Bitmap bitmap) {
        if (MiscUtils.isLand()) return;
        try {
            PerformanceUtils.startTrace("showPopupWindow");
            roundCardPopup = new RoundCardPopup(getContext(), view -> {
                view.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
            }, v -> {
                roundCardPopup.dismiss();
                Bundle bundle = new Bundle();
                bundle.putParcelable(JConstant.KEY_SHARE_ELEMENT_BYTE, bitmap);
                bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
//                if (isNormalView) {
                NormalMediaFragment fragment = NormalMediaFragment.newInstance(bundle);
                ActivityUtils.addFragmentSlideInFromRight(activity.getSupportFragmentManager(), fragment,
                        android.R.id.content);
                fragment.setCallBack(t -> activity.getSupportFragmentManager().popBackStack());
//                } else {
//                    PanoramicViewFragment fragment = PanoramicViewFragment.newInstance(bundle);
//                    ActivityUtils.addFragmentSlideInFromRight(activity.getSupportFragmentManager(), fragment,
//                            android.R.id.content);
//                    fragment.setCallBack(t -> activity.getSupportFragmentManager().popBackStack());
//                }
            });
            roundCardPopup.setAutoDismissTime(5 * 1000L);
            roundCardPopup.showOnAnchor(imgVCamTriggerCapture, RelativePopupWindow.VerticalPosition.ABOVE, RelativePopupWindow.HorizontalPosition.CENTER);
        } catch (Exception e) {
            AppLogger.e("showPopupWindow: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void setLoadingRectAction(ILiveControl.Action action) {
        this.action = action;
        (layoutC).setAction(this.action);
    }

    @Override
    public void onNetworkChanged(CamLiveContract.Presenter presenter, boolean connected) {
        post(() -> {
            changeViewState();
            if (layoutE != null)
                btnLoadHistory.setEnabled(connected);
            if (!connected) {
//                showHistoryWheel(false);
                removeCallbacks(landHideRunnable);  // 取消播放后延时显示的任务
                removeCallbacks(portHideRunnable);  //取消播放后延时显示的任务
                handlePlayErr(presenter, JFGRules.PlayErr.ERR_NETWORK);
            } else {
//                showHistoryWheel(true);
            }
        });
    }

    private void changeViewState() {
        // TODO: 2017/8/18 设置为 gone 会导致布局不正确
//        layoutD.setVisibility(INVISIBLE);
        liveViewWithThumbnail.showFlowView(false, null);
        liveViewWithThumbnail.setThumbnail();
        setHotSeatState(-1, false, false, false, false, false, false);
    }

    @Override
    public void onActivityStart(CamLiveContract.Presenter presenter, Device device) {
        boolean safeIsOpen = device.$(ID_501_CAMERA_ALARM_FLAG, false);
        removeCallbacks(portHideRunnable);
        setFlipped(!safeIsOpen);
        updateLiveViewMode(device.$(509, "1"));
        DpMsgDefine.DPNet net = device.$(201, new DpMsgDefine.DPNet());
        if (!JFGRules.isDeviceOnline(net)) return;//设备离线,不需要显示了
        Bitmap bitmap = SimpleCache.getInstance().getSimpleBitmapCache(presenter.getThumbnailKey());
        if (bitmap == null || bitmap.isRecycled()) {
            File file = new File(presenter.getThumbnailKey());
            liveViewWithThumbnail.setThumbnail(getContext(), PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, ""), Uri.fromFile(file));
        } else
            liveViewWithThumbnail.setThumbnail(getContext(), PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, ""), SimpleCache.getInstance().getSimpleBitmapCache(presenter.getThumbnailKey()));
        TimeZone timeZone = JFGRules.getDeviceTimezone(device);
        liveTimeDateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.UK);
        liveTimeDateFormat.setTimeZone(timeZone);
        AppLogger.d("得到设备时区:" + timeZone.getID() + "," + timeZone.getDisplayName());
        setHotSeatState(PLAY_STATE_STOP, false, false, false, false, false, false);
    }

    @Override
    public void onActivityResume(CamLiveContract.Presenter presenter, Device device, boolean isUserVisible) {
        final boolean judge = !isSightShown() && !isStandBy();
        Log.d("judge", "judge: " + judge);
        handler.postDelayed(() -> {
            livePlayState = judge ? PLAY_STATE_STOP : PLAY_STATE_IDLE;
            setLoadingState(null, null);
            layoutD.setVisibility(!judge ? INVISIBLE : livePlayState == PLAY_STATE_PLAYING ? VISIBLE : INVISIBLE);
            layoutG.setVisibility(livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
            ivModeXunHuan.setVisibility(JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
            ivViewModeSwitch.setEnabled(livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid));
            ivModeXunHuan.setEnabled(livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid));
            boolean online = JFGRules.isDeviceOnline(device.$(201, new DpMsgDefine.DPNet()));
            btnLoadHistory
                    .setEnabled(NetUtils.getJfgNetType() != 0 && online);
            boolean showSdcard = JFGRules.showSdcard(device);
            layoutE.setVisibility(judge && showSdcard ? VISIBLE : INVISIBLE);
            vsLayoutWheel.setVisibility(showSdcard ? VISIBLE : INVISIBLE);
            if (!isUserVisible) return;
        }, 100);
    }

    @Override
    public void updateLiveViewMode(String mode) {
        if (!needShowSight) {
            updateCamParam(presenter.getDevice().$(510, new DpMsgDefine.DpCoordinate()));
        } else {
            liveViewWithThumbnail.getVideoView().config360(TextUtils.equals(mode, "0") ? CameraParam.getTopPreset() : CameraParam.getWallPreset());
        }
        liveViewWithThumbnail.getVideoView().setMode(TextUtils.equals("0", mode) ? 0 : 1);
        liveViewWithThumbnail.getVideoView().detectOrientationChanged();
    }

    private int[] portMicRes = {R.drawable.icon_port_mic_off_selector,
            R.drawable.icon_port_mic_on_selector};
    private int[] landMicRes = {R.drawable.icon_land_mic_off_selector,
            R.drawable.icon_land_mic_on_selector};
    private int[] portSpeakerRes = {R.drawable.icon_port_speaker_off_selector,
            R.drawable.icon_port_speaker_on_selector,
            R.drawable.icon_port_speaker_off_selector, R.drawable.icon_port_speaker_on_selector};
    private int[] landSpeakerRes = {R.drawable.icon_land_speaker_off_selector,
            R.drawable.icon_land_speaker_on_selector,
            R.drawable.icon_land_speaker_off_selector, R.drawable.icon_land_speaker_on_selector};


    /***
     * 三个按钮的状态,不能根据UI的状态来辨别.
     * 反而UI需要根据这个状态来辨别.
     * speaker|mic|capture
     * 用3个byte表示:
     * |0(高位表示1:开,0:关)0(低位表示1:enable,0:disable)|00|00|
     */
    @Override
    public void setHotSeatState(int liveType, boolean speaker,
                                boolean speakerEnable,
                                boolean mic,
                                boolean micEnable,
                                boolean capture, boolean captureEnable) {
        ImageView pMic = (ImageView) imgVCamTriggerMic;
        pMic.setEnabled(micEnable);
        pMic.setImageResource(portMicRes[mic ? 1 : 0]);
        ImageView lMic = (ImageView) imgVLandCamTriggerMic;
        lMic.setEnabled(micEnable);
        lMic.setImageResource(landMicRes[mic ? 1 : 0]);
        //speaker
        ImageView pSpeaker = (ImageView) imgVCamSwitchSpeaker;
        pSpeaker.setEnabled(speakerEnable);
        pSpeaker.setImageResource(portSpeakerRes[speaker ? 1 : 0]);
        ImageView lSpeaker = (ImageView) imgVLandCamSwitchSpeaker;
        lSpeaker.setEnabled(speakerEnable);
        lSpeaker.setImageResource(landSpeakerRes[speaker ? 1 : 0]);
        //capture
        //只有 enable和disable
        ImageView pCapture = (ImageView) imgVCamTriggerCapture;
        pCapture.setEnabled(captureEnable);
        ImageView lCapture = (ImageView) imgVLandCamTriggerCapture;
        lCapture.setEnabled(captureEnable);
        Log.d(TAG, String.format(Locale.getDefault(), "hotSeat:speaker:%s,speakerEnable:%s,mic:%s,micEnable:%s", speaker, speakerEnable, mic, micEnable));
    }

    @Override
    public void setHotSeatListener(OnClickListener micListener,
                                   OnClickListener speakerListener,
                                   OnClickListener captureListener) {
        imgVCamSwitchSpeaker.setOnClickListener(speakerListener);
        imgVLandCamSwitchSpeaker.setOnClickListener(speakerListener);
        imgVCamTriggerMic.setOnClickListener(micListener);
        imgVLandCamTriggerMic.setOnClickListener(micListener);
        imgVCamTriggerCapture.setOnClickListener(captureListener);
        imgVLandCamTriggerCapture.setOnClickListener(captureListener);
    }

    @Override
    public int getMicState() {
        Object o = imgVLandCamTriggerMic.getTag();
        if (o != null && o instanceof Integer) {
            int tag = (int) o;
            switch (tag) {
                case R.drawable.icon_land_mic_on_selector:
                    if (imgVLandCamTriggerMic.isEnabled()) {
                        return 3;
                    } else return 1;
                case R.drawable.icon_land_mic_off_selector:
                    if (imgVLandCamTriggerMic.isEnabled()) {
                        return 2;
                    } else return 0;
            }
        }
        return 0;
    }

    @Override
    public int getSpeakerState() {
        Object o = imgVLandCamSwitchSpeaker.getTag();
        if (o != null && o instanceof Integer) {
            int tag = (int) o;
            switch (tag) {
                case R.drawable.icon_land_speaker_on_selector:
                    if (imgVCamSwitchSpeaker.isEnabled()) {
                        return 3;
                    } else return 1;
                case R.drawable.icon_land_speaker_off_selector:
                    if (imgVCamSwitchSpeaker.isEnabled()) {
                        return 2;
                    } else return 0;
            }
        }
        return 0;
    }

    @Override
    public void resumeGoodFrame() {
        livePlayState = PLAY_STATE_PLAYING;
        setLoadingState(null, null);
        imgVCamZoomToFullScreen.setEnabled(true);
        //0:off-disable,1.on-disable,2.off-enable,3.on-enable
        if (livePlayType == TYPE_HISTORY) {
            imgVLandCamTriggerMic.setEnabled(false);
            imgVCamTriggerMic.setEnabled(false);
        }
        imgVCamTriggerCapture.setEnabled(true);
        imgVLandCamTriggerCapture.setEnabled(true);
        ivModeXunHuan.setEnabled(true);
        ivViewModeSwitch.setEnabled(true);
    }

    @Override
    public void startBadFrame() {
        livePlayState = PLAY_STATE_PREPARE;
        setLoadingState(null, null);
        imgVCamZoomToFullScreen.setEnabled(false);
    }

    @Override
    public void reAssembleHistory(CamLiveContract.Presenter presenter, final long timeTarget) {
        //先loading吧.
        //怎么样,都先开始播放历史录像吧
        boolean fetching = presenter.fetchHistoryDataList();
        if (!fetching) {
            //有历史录像
            reInitHistoryHandler(presenter);
            historyWheelHandler.setNav2Time(timeTarget);
            AppLogger.d("点击播放历史录像:" + timeTarget);
            presenter.startPlayHistory(timeTarget);
            return;
        }
        //获取历史录像ui逻辑
        btnLoadHistory.setEnabled(false);
        livePlayState = PLAY_STATE_PREPARE;
        setLoadingState(getResources().getString(R.string.VIDEO_REFRESHING), null);
        Subscription subscription = Observable.just("get")
                .subscribeOn(Schedulers.io())
                .flatMap(aBoolean -> RxBus.getCacheInstance().toObservable(RxEvent.HistoryBack.class)
                        .timeout(30, TimeUnit.SECONDS).first())
                .flatMap(o -> Observable.just(o.isEmpty))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isEmpty -> {
                    AppLogger.d("加载成功:" + isEmpty);
                    btnLoadHistory.setEnabled(true);
                    if (isEmpty) {
                        ToastUtil.showToast(getResources().getString(R.string.NO_CONTENTS_2));
                        livePlayState = PLAY_STATE_STOP;
                        setLoadingState(PLAY_STATE_STOP, null);
                        return;
                    }
                    if (vsLayoutWheel.getCurrentView() instanceof FrameLayout) {
                        vsLayoutWheel.showNext();
                        livePlayState = PLAY_STATE_STOP;
                        setLoadingState(PLAY_STATE_STOP, null);
                        tvLive.setVisibility(VISIBLE);
                        vFlag.setVisibility(VISIBLE);
                        AppLogger.d("需要展示 遮罩");
                    }
                    HistoryWheelHandler handler = getHistoryWheelHandler(presenter);
                    setLiveRectTime(TYPE_HISTORY, timeTarget / 1000, false);
                    handler.setNav2Time(timeTarget);//2000不一定正确,因为画时间轴需要时间,画出来,才能定位.
                    presenter.startPlayHistory(timeTarget);
                    AppLogger.d("目标历史录像时间?" + timeTarget);
                }, throwable -> {
                    if (throwable instanceof TimeoutException) {
                        btnLoadHistory.setEnabled(true);
                        livePlayState = PLAY_STATE_STOP;
                        setLoadingState(PLAY_STATE_STOP, null);
                        if (presenter != null
                                && presenter.getHistoryDataProvider() != null
                                && presenter.getHistoryDataProvider().getDataCount() == 0)
                            ToastUtil.showToast(getResources().getString(R.string.Item_LoadFail));
                    }
                });
        presenter.addSubscription("fetchHistoryBy", subscription);
    }

    @Override
    public void updateLiveRect(Rect rect) {
        if (liveViewWithThumbnail != null)
            liveViewWithThumbnail.post(() -> {
                liveViewWithThumbnail.getLocalVisibleRect(rect);
                AppLogger.d("rect: " + rect);
            });
    }

    @Override
    public void dpUpdate(JFGDPMsg msg, Device device) {
        if (msg != null && msg.id == 214) {
            TimeZone timeZone = JFGRules.getDeviceTimezone(device);
            liveTimeDateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.UK);
            liveTimeDateFormat.setTimeZone(timeZone);
        }
        if (msg != null && msg.id == 510) {
            try {
                updateCamParam(DpUtils.unpackData(msg.packValue, DpMsgDefine.DpCoordinate.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.imgV_cam_live_land_nav_back:
                post(() -> ViewUtils.setRequestedOrientation((Activity) getContext(),
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
                // TODO: 2017/8/16 现在需要自动横屏
//                postDelayed(() -> ViewUtils.setRequestedOrientation((Activity) getContext(),
//                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED), 3000);
                break;
            case R.id.imgV_cam_zoom_to_full_screen://点击全屏
                post(() -> ViewUtils.setRequestedOrientation((Activity) getContext(),
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
                // TODO: 2017/8/16 现在需要自动横屏
//                postDelayed(() -> ViewUtils.setRequestedOrientation((Activity) getContext(),
//                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED), 3000);

                break;
            case R.id.imgV_cam_live_land_play://横屏,左下角播放
                if (playClickListener != null) playClickListener.onClick(v);
                break;
            case R.id.tv_live://直播中,按钮disable.历史录像:enable
                if (liveTextClick != null) liveTextClick.onClick(v);
                break;
            case R.id.imgV_cam_switch_speaker:
            case R.id.imgV_land_cam_switch_speaker:
                break;
            case R.id.imgV_cam_trigger_mic:
            case R.id.imgV_land_cam_trigger_mic:
                break;
            case R.id.imgV_cam_trigger_capture:
            case R.id.imgV_land_cam_trigger_capture:
                break;
            case R.id.imgV_land_cam_switch_xunhuan:
                switchXunHuanMode();
                break;
        }
    }

    private void switchXunHuanMode() {
        ivModeXunHuan.setSelected(!ivModeXunHuan.isSelected());
        if (videoView != null && videoView instanceof Panoramic360ViewRS) {
            ((Panoramic360ViewRS) videoView).enableAutoRotation(ivModeXunHuan.isSelected());
        }
    }

    public void setLoadingState(int state, String content) {
        setLoadingState(content, null);
    }


    public void setPlayBtnListener(OnClickListener clickListener) {
        this.playClickListener = clickListener;
    }

    public void setLiveTextClick(OnClickListener liveTextClick) {
        this.liveTextClick = liveTextClick;
    }

    public void hideHistoryWheel() {
        IData historyDataProvider = presenter.getHistoryDataProvider();
        if (historyDataProvider != null) {
            historyDataProvider.clean();
        }
        historyWheelHandler.dateUpdate();
    }

    public void showPlayHistoryButton() {
        /**
         * newdoby（1.0.0.457） 已经获取历史视频 当sd卡拔出，客户端点击弹窗中确定后，历史时间轴应消失，同时显示“历史录像”按钮
         * */
        IData historyDataProvider = presenter.getHistoryDataProvider();
        if (historyDataProvider != null) {
            historyDataProvider.clean();
        }
        if (historyWheelHandler != null) {
            historyWheelHandler.dateUpdate();
        }
        vsLayoutWheel.setVisibility(VISIBLE);
        layoutE.setVisibility(VISIBLE);
        if (vsLayoutWheel.getDisplayedChild() == 1) {
            vsLayoutWheel.showPrevious();
        }
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        btnLoadHistory.setEnabled(device.$(201, new DpMsgDefine.DPNet()).net > 0);//设备在线才可点击
        if (isLand()) {
            vsLayoutWheel.getCurrentView().setBackgroundColor(getResources().getColor(android.R.color.transparent));
        } else {
            vsLayoutWheel.getCurrentView().setBackgroundColor(getResources().getColor(R.color.color_F7F8FA));
            vLine.setBackgroundColor(getResources().getColor(R.color.color_f2f2f2));
        }
    }
}
