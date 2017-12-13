package com.cylan.jiafeigou.n.view.cam;

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
import android.support.v4.app.FragmentManager;
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
import android.widget.RadioButton;
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
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.view.activity.SightSettingActivity;
import com.cylan.jiafeigou.n.view.media.NormalMediaFragment;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.APObserver;
import com.cylan.jiafeigou.utils.ActivityUtils;
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
import com.cylan.jiafeigou.widget.wheel.ex.DataExt;
import com.cylan.jiafeigou.widget.wheel.ex.IData;
import com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt;
import com.cylan.panorama.CameraParam;
import com.cylan.panorama.Panoramic360ViewRS;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_501_CAMERA_ALARM_FLAG;
import static com.cylan.jiafeigou.misc.JConstant.CYLAN_TAG;
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
    FrameLayout liveTopBannerView;
    //流量
    @BindView(R.id.layout_b)
    TextView liveFlowText;
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
    SuperWheelExt superWheelExt;


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
    private boolean enableAutoRotate = true;

    private String uuid;
    private static final String TAG = "CamLiveControllerEx";
    private ILiveControl.Action action;
    private float portRatio = -1;
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
    private boolean isShareAccount = false;
    private boolean hasPingSuccess = false;
    private Handler handler = new Handler();
    private boolean needShowSight;

    /**
     * 设备的时区
     */
    private SimpleDateFormat liveTimeDateFormat;
    private Device device;
    private FragmentManager fragmentManager;
    private static final long ANIMATION_DURATION = 250L;
    private static final long WAIT_TO_HIDE_DELAY_TIME = 3000L;


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
        (liveLoadingBar).setAction(this.action);
        //d.time
        imgVCamZoomToFullScreen.setOnClickListener(this);
        //e.
        if (historyWheelContainer.getCurrentView() instanceof FrameLayout) {
            tvLive.setVisibility(GONE);
            vFlag.setVisibility(GONE);
        }
        View vLandPlay = imgVCamLiveLandPlay;
        if (vLandPlay != null) {
            vLandPlay.setOnClickListener(this);
        }
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

                        VideoViewFactory.IVideoView videoView = liveViewWithThumbnail.getVideoView();
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
            VideoViewFactory.IVideoView videoView = liveViewWithThumbnail.getVideoView();
            if (videoView != null && videoView instanceof Panoramic360ViewRS && rbViewModeSwitchParent.getVisibility() == VISIBLE) {
//                ((Panoramic360ViewRS) videoView).setMountMode(Panoramic360ViewRS.MountMode.TOP);
                ((RadioButton) rbViewModeSwitchParent.findViewById(getCheckIdByViewMode(((Panoramic360ViewRS) videoView).getDisplayMode()))).setChecked(true);
                rbViewModeSwitchParent.setVisibility(VISIBLE);
            }

        } else if ("1".equals(dpPrimary) && !JFGRules.hasViewAngle(device.pid)) {
            // TODO: 2017/8/18 怎么处理好呢?
            rbViewModeSwitchParent.setVisibility(rbViewModeSwitchParent.getVisibility() == VISIBLE ? GONE : VISIBLE);
            VideoViewFactory.IVideoView videoView = liveViewWithThumbnail.getVideoView();
            if (videoView != null && videoView instanceof Panoramic360ViewRS && rbViewModeSwitchParent.getVisibility() == VISIBLE) {
                ((Panoramic360ViewRS) videoView).setMountMode(Panoramic360ViewRS.MountMode.TOP);
                if (rbViewModeSwitchParent.getCheckedRadioButtonId() == -1) {
                    ((RadioButton) rbViewModeSwitchParent.findViewById(getCheckIdByViewMode(((Panoramic360ViewRS) videoView).getDisplayMode()))).setChecked(true);
                    rbViewModeSwitchParent.setVisibility(VISIBLE);

//                    rbViewModeSwitchParent.check(getCheckIdByViewMode(((Panoramic360ViewRS) videoView).getDisplayMode()));
                }
            }
            AppLogger.d("当前视图不支持视角切换,但又支持视图切换,强制开始平视视图");
        } else if (!JFGRules.hasViewAngle(device.pid)) {
            rbViewModeSwitchParent.setVisibility(rbViewModeSwitchParent.getVisibility() == VISIBLE ? GONE : VISIBLE);
            VideoViewFactory.IVideoView videoView = liveViewWithThumbnail.getVideoView();
            if (videoView != null && videoView instanceof Panoramic360ViewRS && rbViewModeSwitchParent.getVisibility() == VISIBLE) {
                ((Panoramic360ViewRS) videoView).setMountMode(Panoramic360ViewRS.MountMode.TOP);
                ((RadioButton) rbViewModeSwitchParent.findViewById(getCheckIdByViewMode(((Panoramic360ViewRS) videoView).getDisplayMode()))).setChecked(true);
                rbViewModeSwitchParent.setVisibility(VISIBLE);
            }
        }
    }

    private void switchViewMode(RadioGroup radioGroup, int checkId) {
        ((RadioButton) radioGroup.findViewById(checkId)).setChecked(true);
        VideoViewFactory.IVideoView videoView = liveViewWithThumbnail.getVideoView();
        switch (checkId) {
            case R.id.rb_view_mode_circular:
                if (videoView != null && videoView instanceof Panoramic360ViewRS) {
                    ((Panoramic360ViewRS) videoView).setDisplayMode(Panoramic360ViewRS.SFM_Normal);
                    enableAutoRotate = isLand() && enableAutoRotate;
                    ivModeXunHuan.setEnabled(enableAutoRotate);
                    ((Panoramic360ViewRS) videoView).enableAutoRotation(enableAutoRotate);
                    AppLogger.d("正在切换到圆形视图");
                }
                break;
            case R.id.rb_view_mode_column:
                if (videoView != null && videoView instanceof Panoramic360ViewRS) {
                    ((Panoramic360ViewRS) videoView).setDisplayMode(Panoramic360ViewRS.SFM_Cylinder);
                    ivModeXunHuan.setEnabled(enableAutoRotate = false);
                    AppLogger.d("正在切换到柱状视图");
                }
                break;
            case R.id.rb_view_mode_four:
                if (videoView != null && videoView instanceof Panoramic360ViewRS) {
                    ((Panoramic360ViewRS) videoView).setDisplayMode(Panoramic360ViewRS.SFM_Quad);
                    ivModeXunHuan.setEnabled(enableAutoRotate = false);
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
        performLiveControllerViewAction(getResources().getString(R.string.VIDEO_REFRESHING), null);
        Subscription subscription = Observable.just("get")
                .subscribeOn(Schedulers.io())
                .map(ret -> presenter.fetchHistoryDataList())
                .flatMap(aBoolean -> RxBus.getCacheInstance().toObservable(RxEvent.HistoryBack.class)
                        .timeout(30, TimeUnit.SECONDS).first())
                .flatMap(o -> Observable.just(o.isEmpty))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isEmpty -> {
                    AppLogger.d("加载成功: isEmpty" + isEmpty);
                    if (!isEmpty) {
                        try {
                            presenter.startPlayHistory(DataExt.getInstance().getFlattenMinTime());
                        } catch (Throwable throwable) {
                            AppLogger.e("err:" + MiscUtils.getErr(throwable));
                        }
                    } else {
                        btnLoadHistory.setEnabled(true);
                        ToastUtil.showToast(getResources().getString(R.string.NO_CONTENTS_2));
                        presenter.startPlay();
//                        livePlayState = PLAY_STATE_STOP;
//                        performLiveControllerViewAction(PLAY_STATE_STOP, null);
                        return;
                    }
                    if (historyWheelContainer.getCurrentView() instanceof ViewGroup) {
                        historyWheelContainer.showNext();
                        livePlayState = PLAY_STATE_STOP;
                        performLiveControllerViewAction(PLAY_STATE_STOP, null);
                        tvLive.setVisibility(VISIBLE);
                        vFlag.setVisibility(VISIBLE);
                        AppLogger.d("需要展示 遮罩");
                    }
                }, throwable -> {
                    boolean timeout = throwable instanceof TimeoutException;
                    if (timeout) {
                        btnLoadHistory.setEnabled(true);
                        livePlayState = PLAY_STATE_STOP;
                        performLiveControllerViewAction(PLAY_STATE_STOP, null);
                        if (presenter != null
                                && presenter.getHistoryDataProvider() != null
                                && presenter.getHistoryDataProvider().getDataCount() == 0) {
                            ToastUtil.showToast(getResources().getString(R.string.Item_LoadFail));
                        }
                    }
                    AppLogger.e("err:" + MiscUtils.getErr(throwable) + ",timeout?" + timeout);
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
        this.device = DataSourceManager.getInstance().getDevice(uuid);
        this.isRSCam = JFGRules.isRS(device.pid);
        this.isShareAccount = !TextUtils.isEmpty(device.shareAccount);
        this.pid = device.pid;
        VideoViewFactory.IVideoView videoView = VideoViewFactory.CreateRendererExt(device.pid, getContext());
        if (!JFGRules.showSwitchModeButton(device.pid) && videoView instanceof Panoramic360ViewRS) {
            ((Panoramic360ViewRS) videoView).enableAutoRotation(false);
        }
        videoView.setInterActListener(new VideoViewFactory.InterActListener() {

            @Override
            public boolean onSingleTap(float x, float y) {
                performLayoutAnimation(true);
//                onLiveRectTap();
                return true;
            }

            @Override
            public void onSnapshot(Bitmap bitmap, boolean tag) {
                Log.d("onSnapshot", "onSnapshot: " + (bitmap == null));
                PerformanceUtils.stopTrace("takeShotFromLocalView");
                onCaptureRsp((FragmentActivity) getContext(), bitmap);
                presenter.saveAndShareBitmap(bitmap, true);
            }
        });
        //issue: 过早 add 进去会导致黑块!!!!!
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
        svSwitchStream.setVisibility(presenter.getPlayState() == JConstant.PLAY_STATE_PLAYING ? VISIBLE : GONE);
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
            performLayoutAnimation(true);
        });
        liveBottomBannerView.setVisibility(livePlayState == PLAY_STATE_PLAYING ? VISIBLE : INVISIBLE);
        liveViewModeContainer.setVisibility(livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
        ivModeXunHuan.setVisibility(JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
        ivViewModeSwitch.setVisibility(JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
        if (!JFGRules.showSwitchModeButton(device.pid) && videoView != null && videoView instanceof Panoramic360ViewRS) {
            ((Panoramic360ViewRS) videoView).enableAutoRotation(false);
        }
        ivViewModeSwitch.setEnabled(livePlayType == TYPE_LIVE && livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid));
        ivModeXunHuan.setEnabled(livePlayType == TYPE_LIVE && livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid) && enableAutoRotate);
        // TODO: 2017/9/4 此时还没有 rtcp 过来,在这里设置可见性过早,会有一个空的圆圈
//        liveTimeLayout.setVisibility(JFGRules.hasSDFeature(device.pid) && !JFGRules.isShareDevice(uuid) ? VISIBLE : INVISIBLE);

        // TODO: 2017/10/11
        boolean hasMicFeature = JFGRules.hasMicFeature(device.pid);
        boolean hasDoorLock = JFGRules.hasDoorLock(device.pid);
        boolean isShareAccount = !TextUtils.isEmpty(device.shareAccount);
        imgVCamTriggerMic.setVisibility(hasMicFeature ? VISIBLE : GONE);
        imgVLandCamTriggerMic.setVisibility(hasMicFeature ? VISIBLE : GONE);
        ivCamDoorLock.setVisibility(hasDoorLock && !isShareAccount ? VISIBLE : GONE);
        imgVCamTriggerMic.setImageResource(hasDoorLock ? portBellMicRes[0] : portMicRes[0]);

        updateDoorLock();

        if (JFGRules.shouldObserverAP()) {//需要监听是否局域网在线
            Subscription subscribe = APObserver.scan(uuid)
                    .timeout(5, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ret -> {
                        updateDoorLockFromPing(true);
                    }, e -> {
                        e.printStackTrace();
                        AppLogger.e(e);
                        updateDoorLockFromPing(false);
                    });
            presenter.addSubscription("CamLiveControllerEx.APObserver.scan", subscribe);
        }
        AppLogger.w("需要重置清晰度");
    }

    public void updateDoorLock() {
        //无网络连接或者设备离线不可点击,局域网在线可点击,
        DpMsgDefine.DPNet net = device.$(201, new DpMsgDefine.DPNet());
        boolean noNet = NetUtils.getNetType(getContext()) == -1;

        if (!JFGRules.isDeviceOnline(net) || noNet || isShareAccount) {
            ivCamDoorLock.setEnabled(false);
        }
    }

    public void updateDoorLockFromPing(boolean pingSuccess) {
        this.hasPingSuccess = pingSuccess;
        ivCamDoorLock.setEnabled(hasPingSuccess && !isShareAccount);
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

        if (!needShowSight || basePresenter.isShareDevice()) {
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
                    livePlayState = PLAY_STATE_STOP;
                    performLiveControllerViewAction(PLAY_STATE_STOP, null);
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
            oldLayout.setVisibility(View.VISIBLE);
        }
        livePlayState = PLAY_STATE_IDLE;
        performLiveControllerViewAction(null, null);
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
        if (superWheelExt.getDataProvider() != null && superWheelExt.getDataProvider().getDataCount() > 0) {
            //显示
            AppLogger.d("has history video");
            historyParentContainer.setVisibility(VISIBLE);
            return;
        }
        historyParentContainer.setVisibility(show ? VISIBLE : GONE);
    }

    @Override
    public void onLivePrepared(int type) {
        livePlayType = type;
        livePlayState = PLAY_STATE_PREPARE;
        performLiveControllerViewAction(null, null);
        imgVCamZoomToFullScreen.setEnabled(false);//测试用
        int net = NetUtils.getJfgNetType();
        if (net == 2) {
            ToastUtil.showToast(getResources().getString(R.string.LIVE_DATA));
        }
    }

    private boolean isLand() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public boolean isActionBarHide() {
        return liveTopBannerView.getAlpha() == 0 || !liveTopBannerView.isShown()
                || liveBottomBannerView.getAlpha() == 0 || !liveBottomBannerView.isShown();
    }

    private boolean showSdHdBtn() {
        return livePlayState == PLAY_STATE_PLAYING
                && livePlayType == TYPE_LIVE
                && JFGRules.showSdHd(pid, presenter.getDevice().$(207, ""), false);
    }

    @Override
    public void onLiveStart(CamLiveContract.Presenter presenter, Device device) {
        livePlayType = presenter.getPlayType();
        livePlayState = PLAY_STATE_PLAYING;
        boolean isPlayHistory = livePlayType == TYPE_HISTORY;
        //左下角直播,竖屏下:左下角按钮已经隐藏
        (imgVCamLiveLandPlay).setImageResource(R.drawable.icon_landscape_playing);
        performLiveControllerViewAction(null, null);
        if (tvLive != null) {
            tvLive.setEnabled(isPlayHistory);
        }
        imgVCamTriggerCapture.setEnabled(true);
        imgVLandCamTriggerCapture.setEnabled(true);
        //直播
        tvLive.setEnabled(livePlayType == TYPE_HISTORY);
        liveViewWithThumbnail.onLiveStart();
        imgVCamZoomToFullScreen.setEnabled(true);
        //分享用户不显示
        boolean showFlip = !presenter.isShareDevice() && JFGRules.hasProtection(device.pid, false);
        boolean land = MiscUtils.isLand();
        layoutPortFlip.setVisibility(showFlip && !land ? VISIBLE : INVISIBLE);
        //要根据设备属性表决定是否显示加载历史视频的按钮
        layoutLandFlip.setVisibility(showFlip && land ? VISIBLE : GONE);
        VideoViewFactory.IVideoView videoView = liveViewWithThumbnail.getVideoView();
        if (!JFGRules.showSwitchModeButton(device.pid) && videoView != null && videoView instanceof Panoramic360ViewRS) {
            ((Panoramic360ViewRS) videoView).enableAutoRotation(enableAutoRotate = false);
        }
        performLayoutAnimation(false);
    }


    @Override
    public void onLiveStop(CamLiveContract.Presenter presenter, Device device, int errCode) {
        VideoViewFactory.IVideoView videoView = liveViewWithThumbnail.getVideoView();
        if (videoView != null && videoView instanceof Panoramic360ViewRS) {
            try {
                ((Panoramic360ViewRS) videoView).enableAutoRotation(enableAutoRotate = false);
            } catch (NullPointerException e) {
            }
        }
        livePlayState = presenter.getPlayState();
        imgVCamLiveLandPlay.setImageResource(R.drawable.icon_landscape_stop);
        liveViewWithThumbnail.setEnabled(true);
        liveViewWithThumbnail.showFlowView(false, null);
        imgVCamZoomToFullScreen.setEnabled(false);
        imgVLandCamTriggerCapture.setEnabled(false);
        imgVCamTriggerCapture.setEnabled(false);
        liveViewWithThumbnail.onLiveStop();
        handlePlayErr(presenter, errCode);
        performLayoutAnimation(false);
    }

    /**
     * 错误码 需要放在一个Map里面管理
     *
     * @param errCode
     */
    private void handlePlayErr(CamLiveContract.Presenter presenter, int errCode) {
        if (presenter.isDeviceStandby()) {
            return;
        }
        switch (errCode) {//这些errCode 应当写在一个map中.Map<Integer,String>
            case JFGRules.PlayErr.ERR_NETWORK:
                livePlayState = PLAY_STATE_LOADING_FAILED;
                performLiveControllerViewAction(getContext().getString(R.string.OFFLINE_ERR_1), getContext().getString(R.string.USER_HELP));
                break;
            case JFGRules.PlayErr.ERR_UNKOWN:
                livePlayState = PLAY_STATE_LOADING_FAILED;
                performLiveControllerViewAction(getContext().getString(R.string.NO_NETWORK_2), null);
                break;
            case JFGRules.PlayErr.ERR_LOW_FRAME_RATE:
                int net = NetUtils.getJfgNetType(getContext());
                livePlayState = PLAY_STATE_LOADING_FAILED;
                performLiveControllerViewAction(getContext().getString(R.string.GLOBAL_NO_NETWORK), net == 0 ? getContext().getString(R.string.USER_HELP) : null);
                break;
            case STOP_MAUNALLY:
            case PLAY_STATE_STOP:
                livePlayState = PLAY_STATE_STOP;
                performLiveControllerViewAction(null, null);
                break;
            case JFGRules.PlayErr.ERR_NOT_FLOW:
                livePlayState = PLAY_STATE_LOADING_FAILED;
                performLiveControllerViewAction(getContext().getString(R.string.NETWORK_TIMEOUT), getContext().getString(R.string.USER_HELP));
                break;
            case JError.ErrorVideoPeerDisconnect:
                livePlayState = PLAY_STATE_LOADING_FAILED;
                performLiveControllerViewAction(getContext().getString(R.string.Device_Disconnected), null);
                break;
            case JFGRules.PlayErr.ERR_DEVICE_OFFLINE:
                livePlayState = PLAY_STATE_LOADING_FAILED;
                performLiveControllerViewAction(getContext().getString(R.string.OFFLINE_ERR), getContext().getString(R.string.USER_HELP));
                break;
            case JError.ErrorVideoPeerNotExist:
                livePlayState = PLAY_STATE_LOADING_FAILED;
                performLiveControllerViewAction(getContext().getString(R.string.OFFLINE_ERR), getContext().getString(R.string.USER_HELP));
                break;
            case JError.ErrorVideoPeerInConnect:
                //正在直播...
                livePlayState = PLAY_STATE_LOADING_FAILED;
                performLiveControllerViewAction(getContext().getString(R.string.CONNECTING), null);
                break;
            case PLAY_STATE_IDLE:
                livePlayState = PLAY_STATE_IDLE;
                performLiveControllerViewAction(null, null);
                break;
            case PLAY_STATE_NET_CHANGED:
                livePlayState = PLAY_STATE_PREPARE;
                performLiveControllerViewAction(null, null);
                break;
            case JError.ErrorSDHistoryAll:
                livePlayState = PLAY_STATE_LOADING_FAILED;
                performLiveControllerViewAction(getContext().getString(R.string.Historical_Read), null);
                if (getContext() instanceof Activity) {
                    AlertDialogManager.getInstance().showDialog((Activity) getContext(),
                            getContext().getString(R.string.Historical_Read),
                            getContext().getString(R.string.Historical_Read),
                            getContext().getString(R.string.OK), (DialogInterface dialog, int which) -> {
                                CamLiveContract.LiveStream prePlayType = presenter.getLiveStream();
                                prePlayType.type = TYPE_LIVE;
                                presenter.updateLiveStream(prePlayType);
                                presenter.startPlay();
                            });
                }
                break;
            case JError.ErrorSDFileIO:
                livePlayState = PLAY_STATE_LOADING_FAILED;
                performLiveControllerViewAction(getContext().getString(R.string.Historical_Failed), null);
                if (getContext() instanceof Activity) {
                    AlertDialogManager.getInstance().showDialog((Activity) getContext(),
                            getContext().getString(R.string.Historical_Failed),
                            getContext().getString(R.string.Historical_Failed),
                            getContext().getString(R.string.OK), (DialogInterface dialog, int which) -> {
                                CamLiveContract.LiveStream prePlayType = presenter.getLiveStream();
                                prePlayType.type = TYPE_LIVE;
                                presenter.updateLiveStream(prePlayType);
                                presenter.startPlay();
                            });
                }
                break;
            case JError.ErrorSDIO:
                livePlayState = PLAY_STATE_LOADING_FAILED;
                performLiveControllerViewAction(getContext().getString(R.string.Historical_No), null);
                if (getContext() instanceof Activity) {
                    AlertDialogManager.getInstance().showDialog((Activity) getContext(),
                            getContext().getString(R.string.Historical_No),
                            getContext().getString(R.string.Historical_No),
                            getContext().getString(R.string.OK), (DialogInterface dialog, int which) -> {
                                CamLiveContract.LiveStream prePlayType = presenter.getLiveStream();
                                prePlayType.type = TYPE_LIVE;
                                presenter.updateLiveStream(prePlayType);
                                presenter.startPlay();
                            });
                }
                break;
            default:
                livePlayState = PLAY_STATE_LOADING_FAILED;
                performLiveControllerViewAction(getContext().getString(R.string.GLOBAL_NO_NETWORK), null);
                break;
        }
    }

    @Override
    public void orientationChanged(CamLiveContract.Presenter presenter, Device device, int orientation) {
        boolean isLand = isLand();
        performReLayoutAction(isLand);
        performLayoutAnimation(true);
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

    @Override
    public void onRtcpCallback(int type, JFGMsgVideoRtcp rtcp, boolean ignoreTimeStamp) {
        livePlayState = PLAY_STATE_PLAYING;
        String flow = MiscUtils.getByteFromBitRate(rtcp.bitRate);
        liveViewWithThumbnail.showFlowView(true, flow);
        //暴力处理吧,不知道该在哪里隐藏了
        boolean land = isLand();
//        if (!land) {
//            postDelayed(portHideRunnable, 3000);//#118022
//        }
        //分享账号不显示啊.
//        if (JFGRules.isShareDevice(uuid)) return;
        historyWheelHandler = getHistoryWheelHandler(presenter);
        boolean isWheelBusy = historyWheelHandler.isBusy();
        if (!isWheelBusy && (livePlayType == TYPE_LIVE || !ignoreTimeStamp)) {
            setLiveRectTime(livePlayType, rtcp.timestamp);
        }
        //点击事件
        if (liveTimeRectListener == null) {
            liveTimeRectListener = v -> {
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

    private void setLiveRectTime(int type, long timestamp) {
        //历史视频的时候，使用rtcp自带时间戳。
        if (livePlayType == TYPE_HISTORY && timestamp == 0) {
            return;
        }
        //直播时候，使用本地时间戳。
//        if (livePlayType == TYPE_LIVE && timestamp != 0) return;
        //全景的时间戳是0,使用设备的时区
        //wifi狗是格林尼治时间戳,需要-8个时区.
        historyWheelHandler = getHistoryWheelHandler(presenter);
        boolean isWheelBusy = historyWheelHandler.isBusy();
        //拖动的时候，拒绝外部设置时间。
        if (!isWheelBusy && JFGRules.hasSDFeature(pid) && !JFGRules.isShareDevice(uuid)) {
            liveTimeLayout.setContent(type, timestamp);
        }
        if (type == TYPE_HISTORY && presenter != null
                && presenter.getPlayState() == PLAY_STATE_PLAYING) {
            Log.d("TYPE_HISTORY time", "time: " + timestamp);
            historyWheelHandler.setNav2Time(TimeUtils.wrapToLong(timestamp));
        }
    }

    public void setFlipListener(FlipImageView.OnFlipListener flipListener) {
        (layoutLandFlip).setFlipListener(flipListener);
        (layoutPortFlip).setFlipListener(flipListener);
    }

    public void setFlipped(boolean flip) {
        (layoutLandFlip).setFlipped(flip);
        (layoutPortFlip).setFlipped(flip);
    }


    @Override
    public void onResolutionRsp(JFGMsgVideoResolution resolution) {
        AppLogger.d("收到分辨率消息,正在准备直播");
        try {
            Command.getInstance().enableRenderSingleRemoteView(true, (View) liveViewWithThumbnail.getVideoView());
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
        updateLiveViewRectHeight(ratio);
        ivModeXunHuan.setEnabled(livePlayType == TYPE_LIVE && livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid) && enableAutoRotate);
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

    @Override
    public void onHistoryDataRsp(CamLiveContract.Presenter presenter) {
        showHistoryWheel(true);
        reInitHistoryHandler(presenter);
        Log.d("onHistoryDataRsp", "onHistoryDataRsp");
        historyWheelHandler.dateUpdate();
        historyWheelHandler.setDatePickerListener((time, state) -> {
            //选择时间,更新时间区域,//wheelView 回调的是毫秒时间, rtcp 回调的是秒,这里要除以1000
            setLiveRectTime(TYPE_HISTORY, time);
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
        liveViewWithThumbnail.enableStandbyMode(standby.standby && dpNet.net > 0, clickListener, isShareAccount);
        boolean online = JFGRules.isDeviceOnline(dpNet);
        boolean noNet = NetUtils.getNetType(getContext()) == -1;
        if (standby.standby && online && !isLand()) {
//            post(portHideRunnable);
//            post(landHideRunnable);
            performLayoutAnimation(false);
            performLiveControllerViewAction(null, null);
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

    @Override
    public void onLoadPreviewBitmap(Bitmap bitmap) {
//        post(() -> liveViewWithThumbnail.setThumbnail(getContext(), PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, ""), bitmap));
    }

    @Override
    public void onCaptureRsp(FragmentActivity activity, Bitmap bitmap) {
        if (MiscUtils.isLand() || activity == null || activity.isFinishing()) {
            return;
        }
        try {
            PerformanceUtils.startTrace("showPopupWindow");
            roundCardPopup = new RoundCardPopup(getContext(), view -> view.setImageDrawable(new BitmapDrawable(getResources(), bitmap)), v -> {
                roundCardPopup.dismiss();
                Bundle bundle = new Bundle();
                bundle.putParcelable(JConstant.KEY_SHARE_ELEMENT_BYTE, bitmap);
                bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                NormalMediaFragment fragment = NormalMediaFragment.newInstance(bundle);
                ActivityUtils.addFragmentSlideInFromRight(activity.getSupportFragmentManager(), fragment,
                        android.R.id.content);
                fragment.setCallBack(t -> activity.getSupportFragmentManager().popBackStack());
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
        (liveLoadingBar).setAction(this.action);
    }

    @Override
    public void onNetworkChanged(CamLiveContract.Presenter presenter, boolean connected) {
        post(() -> {
            changeViewState();
            if (historyParentContainer != null) {
                btnLoadHistory.setEnabled(connected);
            }
            if (!connected) {
                handlePlayErr(presenter, JFGRules.PlayErr.ERR_NETWORK);
            } else {
                showHistoryWheel(true);
            }
        });
    }

    private void changeViewState() {
        // TODO: 2017/8/18 设置为 gone 会导致布局不正确
        liveViewWithThumbnail.showFlowView(false, null);
        liveViewWithThumbnail.setThumbnail();
        setHotSeatState(-1, false, false, false, false, false, false);
    }

    @Override
    public void onActivityStart(CamLiveContract.Presenter presenter, Device device) {
        boolean safeIsOpen = device.$(ID_501_CAMERA_ALARM_FLAG, false);
        performLayoutAnimation(true);
        setFlipped(!safeIsOpen);
        updateLiveViewMode(device.$(509, "1"));
        DpMsgDefine.DPNet net = device.$(201, new DpMsgDefine.DPNet());
//        if (!JFGRules.isDeviceOnline(net)) return;//设备离线,不需要显示了
//        Bitmap bitmap = SimpleCache.getInstance().getSimpleBitmapCache(presenter.getThumbnailKey());
//        if (bitmap == null || bitmap.isRecycled()) {
        File file = new File(presenter.getThumbnailKey());
        liveViewWithThumbnail.setThumbnail(getContext(), PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, ""), Uri.fromFile(file));
//        } else
//            liveViewWithThumbnail.setThumbnail(getContext(), PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, ""), SimpleCache.getInstance().getSimpleBitmapCache(presenter.getThumbnailKey()));
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
            performLiveControllerViewAction(null, null);
            liveBottomBannerView.setVisibility(!judge ? INVISIBLE : livePlayState == PLAY_STATE_PLAYING ? VISIBLE : INVISIBLE);
            liveViewModeContainer.setVisibility(livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
            ivModeXunHuan.setVisibility(JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
            ivViewModeSwitch.setVisibility(JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
            VideoViewFactory.IVideoView videoView = liveViewWithThumbnail.getVideoView();
            if (!JFGRules.showSwitchModeButton(device.pid) && videoView != null && videoView instanceof Panoramic360ViewRS) {
                ((Panoramic360ViewRS) videoView).enableAutoRotation(false);
            }
            ivViewModeSwitch.setEnabled(livePlayType == TYPE_LIVE && livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid));
            ivModeXunHuan.setEnabled(livePlayType == TYPE_LIVE && livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid) && enableAutoRotate);
            boolean online = JFGRules.isDeviceOnline(device.$(201, new DpMsgDefine.DPNet()));
            btnLoadHistory
                    .setEnabled(NetUtils.getJfgNetType() != 0 && online);
            boolean showSdcard = JFGRules.showSdcard(device);
            historyParentContainer.setVisibility(judge && showSdcard ? VISIBLE : GONE);
            historyWheelContainer.setVisibility(showSdcard ? VISIBLE : INVISIBLE);
            if (!isUserVisible) {
                return;
            }
        }, 100);
    }

    @Override
    public void updateLiveViewMode(String mode) {
        if (device.pid == 39 || device.pid == 49) {
            mode = "0";
        }
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
    private int[] portBellMicRes = {R.drawable.door_bell_no_talk_selector, R.drawable.door_bell_talk_selector};
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
        boolean hasDoorLock = JFGRules.hasDoorLock(getDevice().pid);
        boolean isShareAccount = !TextUtils.isEmpty(getDevice().shareAccount);
        pMic.setImageResource(hasDoorLock && !isShareAccount ? portBellMicRes[mic ? 1 : 0] : portMicRes[mic ? 1 : 0]);
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
                    } else {
                        return 1;
                    }
                case R.drawable.icon_land_mic_off_selector:
                    if (imgVLandCamTriggerMic.isEnabled()) {
                        return 2;
                    } else {
                        return 0;
                    }
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
                    } else {
                        return 1;
                    }
                case R.drawable.icon_land_speaker_off_selector:
                    if (imgVCamSwitchSpeaker.isEnabled()) {
                        return 2;
                    } else {
                        return 0;
                    }
            }
        }
        return 0;
    }

    @Override
    public void resumeGoodFrame() {
        livePlayState = PLAY_STATE_PLAYING;
        performLiveControllerViewAction(null, null);
        imgVCamZoomToFullScreen.setEnabled(true);
        //0:off-disable,1.on-disable,2.off-enable,3.on-enable
        if (livePlayType == TYPE_HISTORY) {
            imgVLandCamTriggerMic.setEnabled(false);
            imgVCamTriggerMic.setEnabled(false);
        }
        imgVCamTriggerCapture.setEnabled(true);
        imgVLandCamTriggerCapture.setEnabled(true);
        ivModeXunHuan.setEnabled(livePlayType == TYPE_LIVE && livePlayState == PLAY_STATE_PLAYING && enableAutoRotate);
        ivViewModeSwitch.setEnabled(livePlayType == TYPE_LIVE);

    }

    @Override
    public void startBadFrame() {
        livePlayState = PLAY_STATE_PREPARE;
        performLiveControllerViewAction(null, null);
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
        performLiveControllerViewAction(getResources().getString(R.string.VIDEO_REFRESHING), null);
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
                        performLiveControllerViewAction(PLAY_STATE_STOP, null);
                        return;
                    }
                    if (historyWheelContainer.getCurrentView() instanceof FrameLayout) {
                        historyWheelContainer.showNext();
                        livePlayState = PLAY_STATE_STOP;
                        performLiveControllerViewAction(PLAY_STATE_STOP, null);
                        tvLive.setVisibility(VISIBLE);
                        vFlag.setVisibility(VISIBLE);
                        AppLogger.d("需要展示 遮罩");
                    }
                    HistoryWheelHandler handler = getHistoryWheelHandler(presenter);
                    setLiveRectTime(TYPE_HISTORY, timeTarget / 1000);
                    handler.setNav2Time(timeTarget);//2000不一定正确,因为画时间轴需要时间,画出来,才能定位.
                    presenter.startPlayHistory(timeTarget);
                    AppLogger.d("目标历史录像时间?" + timeTarget);
                }, throwable -> {
                    if (throwable instanceof TimeoutException) {
                        btnLoadHistory.setEnabled(true);
                        livePlayState = PLAY_STATE_STOP;
                        performLiveControllerViewAction(PLAY_STATE_STOP, null);
                        if (presenter.getHistoryDataProvider() != null
                                && presenter.getHistoryDataProvider().getDataCount() == 0) {
                            ToastUtil.showToast(getResources().getString(R.string.Item_LoadFail));
                        }
                    }
                });
        presenter.addSubscription("fetchHistoryBy", subscription);
    }

    @Override
    public void updateLiveRect(Rect rect) {
        if (liveViewWithThumbnail != null) {
            liveViewWithThumbnail.post(() -> {
                liveViewWithThumbnail.getLocalVisibleRect(rect);
                AppLogger.d("rect: " + rect);
            });
        }
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
//                if (MiscUtils.isLand() && isActionBarHide()) {
//                    return;
//                }
                if (orientationHandle != null) {
                    orientationHandle.setRequestOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, true);
                }

                // TODO: 2017/8/16 现在需要自动横屏
//                postDelayed(() -> ViewUtils.setRequestedOrientation((Activity) getContext(),
//                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED), 3000);
                break;
            case R.id.imgV_cam_zoom_to_full_screen://点击全屏
//                post(() -> ViewUtils.setRequestedOrientation((Activity) getContext(),
//                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
                if (orientationHandle != null) {
                    orientationHandle.setRequestOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, true);
                }
                // TODO: 2017/8/16 现在需要自动横屏
//                postDelayed(() -> ViewUtils.setRequestedOrientation((Activity) getContext(),
//                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED), 3000);

                break;
            case R.id.imgV_cam_live_land_play://横屏,左下角播放
//                if (MiscUtils.isLand() && isActionBarHide()) {
//                    return;
//                }
                if (playClickListener != null) {
                    playClickListener.onClick(v);
                }
                break;
            case R.id.tv_live://直播中,按钮disable.历史录像:enable
//                if (MiscUtils.isLand() && isActionBarHide()) {
//                    return;
//                }
                if (liveTextClick != null) {
                    liveTextClick.onClick(v);
                }
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
    }

    public void performLiveControllerViewAction(int state, String content) {
        performLiveControllerViewAction(content, null);
    }


    public void setPlayBtnListener(OnClickListener clickListener) {
        this.playClickListener = clickListener;
    }

    public void setLiveTextClick(OnClickListener liveTextClick) {
        this.liveTextClick = liveTextClick;
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
            VideoViewFactory.IVideoView videoView = liveViewWithThumbnail.getVideoView();
            if (videoView != null && videoView instanceof PanoramicView360RS_Ext) {
                ((PanoramicView360RS_Ext) videoView).enableAutoRotation(true);
                ((PanoramicView360RS_Ext) videoView).phoneShook();
            }
        }
    }

    public boolean isShakeEnable() {
        return ivModeXunHuan.isSelected() && ivModeXunHuan.isEnabled();
    }

    public void setFragmentManager(FragmentManager childFragmentManager) {
        this.fragmentManager = childFragmentManager;
    }

    public void updateDeviceNet() {
        updateDoorLock();
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
        doorLockDialog.show(fragmentManager, "CamLiveControllerEx.onDoorLockClick");
    }

    private void performLayoutAnimation(boolean showLayout) {
        performLayoutAnimation(showLayout, true);
    }

    private void performLayoutAnimation(boolean showLayout, boolean autoHide) {
        removeCallbacks(showLayoutAnimationRunnable);
        removeCallbacks(hideLayoutAnimationRunnable);
        post(showLayout ? showLayoutAnimationRunnable : hideLayoutAnimationRunnable);
        if (showLayout && autoHide) {
            postDelayed(hideLayoutAnimationRunnable, WAIT_TO_HIDE_DELAY_TIME);
        }
    }


    //显示 View 的 runnable
    private Runnable showLayoutAnimationRunnable = () -> {
        if (isLand()) {
            performLandLayoutAnimation(true);
        } else {
            performPortLayoutAnimation(true);
        }
    };
    //隐藏 view 的 runnable
    private Runnable hideLayoutAnimationRunnable = () -> {
        if (isLand()) {
            performLandLayoutAnimation(false);
        } else {
            performPortLayoutAnimation(false);
        }
    };

    private void performLandLayoutAnimation(boolean showLayout) {
        Log.d(TAG, "performLandLayoutAnimation,showLayout:" + showLayout);
        if (showLayout) {
            liveLoadingBar.animate().setDuration(ANIMATION_DURATION).alpha(1).withStartAction(() -> {
                liveLoadingBar.setVisibility(livePlayState == PLAY_STATE_LOADING_FAILED
                        || livePlayState == PLAY_STATE_PREPARE ? VISIBLE : INVISIBLE
                );//全屏直播门铃 1.需要去掉中间播放按钮
            }).start();
            svSwitchStream.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {
                svSwitchStream.setVisibility(showSdHdBtn() ? VISIBLE : INVISIBLE);
            }).start();

            liveTopBannerView.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {
                liveTopBannerView.setVisibility(VISIBLE);
                ivModeXunHuan.setVisibility(JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
                ivModeXunHuan.setEnabled(livePlayType == TYPE_LIVE
                        && livePlayState == PLAY_STATE_PLAYING
                        && JFGRules.showSwitchModeButton(device.pid)
                        && enableAutoRotate
                );
            }).start();

            liveBottomBannerView.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {
                liveBottomBannerView.setVisibility(livePlayState == PLAY_STATE_PLAYING ? VISIBLE : INVISIBLE);
            }).withEndAction(() -> {
                liveBottomBannerView.setVisibility(livePlayState == PLAY_STATE_PLAYING ? VISIBLE : INVISIBLE);
            }).start();

            historyParentContainer.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {
                historyParentContainer.setVisibility(VISIBLE);
                historyWheelContainer.setVisibility(JFGRules.isShareDevice(uuid) ? INVISIBLE : VISIBLE);
            }).start();

            liveViewModeContainer.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {
                ivViewModeSwitch.setEnabled(livePlayType == TYPE_LIVE && livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid));
                liveViewModeContainer.setVisibility(JFGRules.showSwitchModeButton(getDevice().pid)
                        && livePlayState == PLAY_STATE_PLAYING
                        && livePlayType == TYPE_LIVE
                        ? VISIBLE : INVISIBLE
                );
            }).start();

            liveViewWithThumbnail.getTvLiveFlow().animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {
                liveViewWithThumbnail.getTvLiveFlow().setVisibility(livePlayState == PLAY_STATE_PLAYING ? VISIBLE : INVISIBLE);

            }).start();
        } else {

            liveLoadingBar.animate().setDuration(ANIMATION_DURATION).alpha(
                    livePlayState == PLAY_STATE_LOADING_FAILED
                            || livePlayState == PLAY_STATE_PREPARE ? 1 : 0
            )
                    .translationY(livePlayState == PLAY_STATE_LOADING_FAILED
                            || livePlayState == PLAY_STATE_PREPARE ? 1 : 0)
                    .withEndAction(() -> {
                        liveLoadingBar.setVisibility(livePlayState == PLAY_STATE_LOADING_FAILED
                                || livePlayState == PLAY_STATE_PREPARE
                                ? VISIBLE : INVISIBLE
                        );
                    }).start();

            svSwitchStream.animate().setDuration(ANIMATION_DURATION).alpha(0).translationY(svSwitchStream.getHeight() / 4)
                    .withEndAction(() -> {
                        svSwitchStream.setVisibility(INVISIBLE);
                    }).start();

            liveTopBannerView.animate().setDuration(ANIMATION_DURATION).alpha(0).translationY(-liveTopBannerView.getHeight() / 4).withEndAction(() -> {
                ivModeXunHuan.setEnabled(livePlayType == TYPE_LIVE && livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid) && enableAutoRotate);
                liveTopBannerView.setVisibility(INVISIBLE);
            }).start();

            liveBottomBannerView.animate().setDuration(ANIMATION_DURATION).translationY(historyParentContainer.getHeight()).withStartAction(() -> {
                liveBottomBannerView.setVisibility(livePlayState == PLAY_STATE_PLAYING ? VISIBLE : INVISIBLE);
            }).withEndAction(() -> {
                liveBottomBannerView.setVisibility(livePlayState == PLAY_STATE_PLAYING ? VISIBLE : INVISIBLE);
            }).start();

            historyParentContainer.animate().setDuration(ANIMATION_DURATION).alpha(0).translationY(historyParentContainer.getHeight() / 4).withEndAction(() -> {
                historyParentContainer.setVisibility(INVISIBLE);
            }).start();

            liveViewModeContainer.animate().setDuration(ANIMATION_DURATION).alpha(0).translationY(liveViewModeContainer.getHeight() / 4).withEndAction(() -> {
                ivViewModeSwitch.setEnabled(livePlayType == TYPE_LIVE && livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid));
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
                liveLoadingBar.setVisibility(VISIBLE);
            }).start();

            liveViewModeContainer.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {
                liveViewModeContainer.setVisibility(livePlayState == PLAY_STATE_PLAYING
                        && livePlayType == TYPE_LIVE
                        && JFGRules.showSwitchModeButton(device.pid)
                        ? VISIBLE : INVISIBLE
                );
                ivViewModeSwitch.setEnabled(livePlayType == TYPE_LIVE
                        && livePlayState == PLAY_STATE_PLAYING
                        && JFGRules.showSwitchModeButton(device.pid)
                );
            }).start();

            liveBottomBannerView.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {
                liveBottomBannerView.setVisibility(livePlayState == PLAY_STATE_PLAYING ? VISIBLE : INVISIBLE);
            }).withEndAction(() -> {
                liveBottomBannerView.setVisibility(livePlayState == PLAY_STATE_PLAYING ? VISIBLE : INVISIBLE);
            }).start();

            svSwitchStream.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {
                svSwitchStream.setVisibility(livePlayState == PLAY_STATE_PLAYING && JFGRules.showSdHd(pid, presenter.getDevice().$(207, ""), false) ? VISIBLE : INVISIBLE);
            }).start();

            historyParentContainer.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {
                historyParentContainer.setVisibility(JFGRules.isShareDevice(uuid) ? INVISIBLE : VISIBLE);
            }).start();

        } else {
            liveLoadingBar.animate().setDuration(ANIMATION_DURATION).alpha(livePlayState == PLAY_STATE_PLAYING ? 0 : 1).translationY(0).withEndAction(() -> {
                liveLoadingBar.setVisibility(livePlayState == PLAY_STATE_PLAYING ? INVISIBLE : VISIBLE);
            }).start();
            svSwitchStream.animate().setDuration(ANIMATION_DURATION).alpha(0).translationY(0).withEndAction(() -> {
                svSwitchStream.setVisibility(INVISIBLE);
            }).start();

            liveViewModeContainer.animate().setDuration(ANIMATION_DURATION).alpha(0).translationY(0).withEndAction(() -> {
                ivViewModeSwitch.setEnabled(livePlayType == TYPE_LIVE
                        && livePlayState == PLAY_STATE_PLAYING
                        && JFGRules.showSwitchModeButton(device.pid)
                );
                liveViewModeContainer.setVisibility(INVISIBLE);
            }).start();

            liveBottomBannerView.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0)
                    .withStartAction(() -> {
                        liveBottomBannerView.setVisibility(livePlayState == PLAY_STATE_PLAYING ? VISIBLE : INVISIBLE);
                    })
                    .withEndAction(() -> {
                        liveBottomBannerView.setVisibility(livePlayState == PLAY_STATE_PLAYING ? VISIBLE : INVISIBLE);
                    })
                    .start();
            historyParentContainer.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {

            }).start();
        }
    }

    private void performReLayoutAction(boolean isLand) {
        int playType = presenter.getPlayType();
        liveTopBannerView.setVisibility(isLand ? VISIBLE : INVISIBLE);
        bottomControllerContainer.setVisibility(isLand ? INVISIBLE : VISIBLE);
        tvLive.setBackgroundColor(isLand ? Color.TRANSPARENT : Color.WHITE);
        if (isLand) {
            //隐藏所有的 showcase
            LiveShowCase.hideHistoryWheelCase((Activity) getContext());
            LiveShowCase.hideHistoryCase((Activity) getContext());
            if (historyWheelContainer.getCurrentView() instanceof FrameLayout) {
                historyWheelContainer.getCurrentView().setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }
            vLine.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            if (historyWheelContainer.getCurrentView() instanceof FrameLayout) {
                tvLive.setVisibility(VISIBLE);
                vFlag.setVisibility(VISIBLE);
            }
            if (device != null && JFGRules.isShareDevice(device)) {
                historyWheelContainer.setVisibility(INVISIBLE);
            }
            //需要判断是否已近是播放状态
//            svSwitchStream.setVisibility(playType == PLAY_STATE_PLAYING ? VISIBLE : GONE);
            ViewUtils.increaseMargins(svSwitchStream, 0, 0, 0, (int) getResources().getDimension(R.dimen.y10));
        } else {
            tvLive.setVisibility(historyWheelContainer.getCurrentView() instanceof FrameLayout ? GONE : VISIBLE);
            IData dataProvider = presenter.getHistoryDataProvider();
            if (dataProvider != null && dataProvider.getDataCount() != 0) {
                if (historyWheelContainer.getDisplayedChild() == 0) {
                    historyWheelContainer.setVisibility(VISIBLE);
                    historyWheelContainer.showNext();
                }
            } else if (historyWheelContainer.getCurrentView() instanceof FrameLayout) {
                historyWheelContainer.getCurrentView().setBackgroundColor(getResources().getColor(R.color.color_F7F8FA));
                vLine.setBackgroundColor(getResources().getColor(R.color.color_f2f2f2));
            }
            ViewUtils.increaseMargins(svSwitchStream, 0, 0, 0, -(int) getResources().getDimension(R.dimen.y10));
        }
        //历史录像显示
        boolean showFlip = !presenter.isShareDevice() && JFGRules.hasProtection(device.pid, false);
        layoutLandFlip.setVisibility(showFlip && isLand ? VISIBLE : GONE);
        liveViewWithThumbnail.detectOrientationChanged(!isLand);

        if (presenter.getPlayState() != PLAY_STATE_PLAYING) {//显示缩略图

            File file = new File(presenter.getThumbnailKey());
            liveViewWithThumbnail.setThumbnail(getContext(), PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, ""), Uri.fromFile(file));
        }

        //直播
        tvLive.setEnabled(playType == TYPE_HISTORY);
        @SuppressLint("WrongViewCast") LayoutParams lp = (LayoutParams) historyParentContainer.getLayoutParams();
        @SuppressLint("WrongViewCast") LayoutParams glp = (LayoutParams) liveViewModeContainer.getLayoutParams();
        if (isLand) {
            lp.removeRule(3);//remove below rules
            lp.addRule(2, R.id.v_guide);//set above v_guide
            glp.addRule(RelativeLayout.ABOVE, R.id.layout_e);
            liveViewWithThumbnail.updateLayoutParameters(LayoutParams.MATCH_PARENT, getVideoFinalWidth());
            imgVCamZoomToFullScreen.setVisibility(INVISIBLE);
            liveBottomBannerView.setBackgroundResource(android.R.color.transparent);
            historyParentContainer.setBackgroundResource(R.color.color_4C000000);
            layoutPortFlip.setVisibility(INVISIBLE);
            //显示 昵称
            String alias = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
            imgVCamLiveLandNavBack.setText(alias);
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
            liveBottomBannerView.setBackgroundResource(R.drawable.camera_sahdow);
            historyParentContainer.setBackgroundResource(android.R.color.transparent);
        }

        historyParentContainer.setLayoutParams(lp);
        liveViewModeContainer.setLayoutParams(glp);
    }

    private void performLiveControllerViewAction(String content, String subContent) {
        liveLoadingBar.setState(livePlayState, content, subContent);
        performLayoutAnimation(false);
    }

    private void performLayoutVisibilityAction(boolean isLand) {
    }
}
