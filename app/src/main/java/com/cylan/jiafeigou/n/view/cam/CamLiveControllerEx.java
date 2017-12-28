package com.cylan.jiafeigou.n.view.cam;

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
import android.view.MotionEvent;
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
import com.cylan.jiafeigou.module.HistoryManager;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.view.activity.SightSettingActivity;
import com.cylan.jiafeigou.n.view.media.NormalMediaFragment;
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
import com.cylan.jiafeigou.widget.wheel.HistoryWheelView;
import com.cylan.panorama.CameraParam;
import com.cylan.panorama.Panoramic360ViewRS;
import com.google.gson.Gson;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

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
import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_FINISH;

/**
 * Created by hds on 17-4-19.
 */

public class CamLiveControllerEx extends RelativeLayout implements ICamLiveLayer,
        View.OnClickListener, HistoryManager.HistoryObserver {
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
    private volatile boolean hasPendingHistoryPlayAction = false;
    private volatile boolean isUserTouchScreen = false;
    private long pendingHistoryPlayTime = -1;
    /**
     * 设备的时区
     */
    private SimpleDateFormat liveTimeDateFormat;
    private Device device;
    private FragmentManager fragmentManager;


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
        imgVCamZoomToFullScreen
                .setOnClickListener(this);
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

    public void onLoadHistoryFailed() {
        btnLoadHistory.setEnabled(true);
        livePlayState = PLAY_STATE_STOP;
        setLoadingState(PLAY_STATE_STOP, null);
        if (presenter.isHistoryEmpty()) {
            ToastUtil.showToast(getResources().getString(R.string.Item_LoadFail));
        }
    }

    public void onHistoryEmpty() {
        presenter.startPlay();
        btnLoadHistory.setEnabled(true);
        ToastUtil.showToast(getResources().getString(R.string.NO_CONTENTS_2));
    }

    public void playHistoryAndSetLiveTime(long playTime) {
        livePlayState = PLAY_STATE_PREPARE;
        setLoadingState(null, null);
        setLiveRectTime(TYPE_HISTORY, playTime, true);
        presenter.startPlayHistory(playTime);
    }

    public void onHistoryReady(Collection<JFGVideo> history) {
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
                playHistoryAndSetLiveTime(playTime / 1000);
            } else {
                Log.d("RePlay", "Replay history no time to play");
            }
        }
        Log.d("onHistoryReady", "onHistoryReady:" + new Gson().toJson(history));
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
            livePlayState = PLAY_STATE_PREPARE;
            setLoadingState(getResources().getString(R.string.VIDEO_REFRESHING), null);
            hasPendingHistoryPlayAction = true;
            presenter.fetchHistoryDataListV2(uuid, (int) (TimeUtils.getTodayEndTime() / 1000), 1, 3);
        }

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
        //issue: 过早 add 进去会导致黑块!!!!!
        liveViewWithThumbnail.setLiveView(videoView);
        liveViewWithThumbnail.setInterActListener(new VideoViewFactory.InterActListener() {
            @Override
            public boolean onSingleTap(float x, float y) {
                performLayoutAnimation(!isLayoutAnimationShowing, true);
                AppLogger.e("点击,需要播放状态");
                return false;
            }

            @Override
            public void onSnapshot(Bitmap bitmap, boolean tag) {
                Log.d("onSnapshot", "onSnapshot: " + (bitmap == null));
                PerformanceUtils.stopTrace("takeShotFromLocalView");
                onCaptureRsp((FragmentActivity) getContext(), bitmap);
                presenter.saveAndShareBitmap(bitmap, true, false);
            }
        });

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
                if (historyWheelHandler == null || presenter.isHistoryEmpty()) {
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
        HistoryManager.getInstance().addHistoryObserver(uuid, this);
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
        if (!ivCamDoorLock.isEnabled()) {
            ivCamDoorLock.setEnabled(hasPingSuccess && !isShareAccount);
        }
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
                    setLoadingState(PLAY_STATE_STOP, null);
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
        setLoadingState(null, null);
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

    @Override
    public void onLivePrepared(int type) {
        livePlayType = type;
        livePlayState = PLAY_STATE_PREPARE;
        setLoadingState(null, null);
        imgVCamZoomToFullScreen.setEnabled(false);//测试用
        int net = NetUtils.getJfgNetType();
        if (net == 2) {
            ToastUtil.showToast(getResources().getString(R.string.LIVE_DATA));
        }
    }

    private boolean isLand() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    @Override
    public void onLiveStart(CamLiveContract.Presenter presenter, Device device) {
        //|直播| 按钮
        performReLayoutAction();
        performLayoutAnimation(true);
        //现在显示的条件就是手动点击其他情况都不显示
        liveViewWithThumbnail.onLiveStart();
        setLoadingState(null, null);
        if (liveLoadingBar.getState() != PLAY_STATE_LOADING_FAILED) {
            liveLoadingBar.setVisibility(INVISIBLE);
        }
    }

    private void setLoadingState(String content, String subContent) {
        int state = livePlayState;
        liveLoadingBar.setState(state, content, subContent);
        if (!TextUtils.isEmpty(content) || !TextUtils.isEmpty(subContent)) {
            liveLoadingBar.setVisibility(VISIBLE);
        }
        switch (livePlayState) {
            case PLAY_STATE_LOADING_FAILED:
            case PLAY_STATE_STOP:
            case PLAY_STATE_PREPARE:
                liveLoadingBar.setVisibility(VISIBLE);
                break;
            case PLAY_STATE_IDLE:
                liveLoadingBar.setVisibility(INVISIBLE);
                break;
        }
    }

    @Override
    public void onLiveStop(CamLiveContract.Presenter presenter, Device device, int errCode) {
        Log.e(TAG, "onLiveStop: " + device.getSn());
        liveViewWithThumbnail.onLiveStop();
        performReLayoutAction();
        performLayoutAnimation(false);
        handlePlayErr(presenter, errCode);
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
                if (livePlayState == PLAY_STATE_PLAYING) {//可能已经失败了,再提示网络连接超时就不正常了
                    livePlayState = PLAY_STATE_LOADING_FAILED;
                    setLoadingState(getContext().getString(R.string.NETWORK_TIMEOUT), getContext().getString(R.string.USER_HELP));
                }
                livePlayState = PLAY_STATE_LOADING_FAILED;
                break;
            case JError.ErrorVideoPeerDisconnect:
                if (livePlayState == PLAY_STATE_PLAYING) {
                    livePlayState = PLAY_STATE_LOADING_FAILED;
                    setLoadingState(getContext().getString(R.string.Device_Disconnected), null);
                }
                livePlayState = PLAY_STATE_LOADING_FAILED;
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
                setLoadingState(getContext().getString(R.string.Historical_Failed), null);
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
                setLoadingState(getContext().getString(R.string.Historical_No), null);
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
                setLoadingState(getContext().getString(R.string.GLOBAL_NO_NETWORK), null);
                break;
        }
    }

    @Override
    public void orientationChanged(CamLiveContract.Presenter presenter, Device device, int orientation) {
        performReLayoutAction();
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
    public void onRtcpCallback(int type, JFGMsgVideoRtcp rtcp) {
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
        historyWheelHandler = getHistoryWheelHandler();
        setLiveRectTime(livePlayType, rtcp.timestamp, false);

    }

    private void setLiveRectTime(int type, long timestamp, boolean focus) {
        //历史视频的时候，使用rtcp自带时间戳。
        if (livePlayType == TYPE_HISTORY && timestamp == 0) {
            return;
        }
        //直播时候，使用本地时间戳。
        //全景的时间戳是0,使用设备的时区
        //wifi狗是格林尼治时间戳,需要-8个时区.
        historyWheelHandler = getHistoryWheelHandler();
        boolean historyLocked = historyWheelHandler.isHistoryLocked();
        Log.d("TYPE_HISTORY", "time: " + timestamp + ",locked:" + historyLocked + ",focus:" + focus);
        if (!historyLocked || focus) {
            setLiveTimeContent(type, timestamp);
            if (type == TYPE_HISTORY) {
                superWheelExt.scrollToPosition(TimeUtils.wrapToLong(timestamp), focus, focus);
            }
        }
    }

    private void setLiveTimeContent(int type, long timestamp) {
        if (JFGRules.hasSDFeature(pid) && !JFGRules.isShareDevice(uuid)) {
            liveTimeLayout.setContent(type, livePlayType == TYPE_LIVE ? 0 : timestamp);
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

    private void reInitHistoryHandler() {
        if (historyWheelHandler == null) {
            historyWheelHandler = new HistoryWheelHandler(superWheelExt, uuid);
            historyWheelHandler.setDatePickerListener((time, state) -> {
                //选择时间,更新时间区域,//wheelView 回调的是毫秒时间, rtcp 回调的是秒,这里要除以1000
                switch (state) {
                    case STATE_FINISH: {
                        setLiveRectTime(TYPE_HISTORY, time, true);
                        presenter.startPlayHistory(time);
                    }
                    break;
                    default: {
                        setLiveTimeContent(TYPE_HISTORY, time);
                    }
                }
            });
        }
    }

    @Override
    public void onLiveDestroy() {
        //1.live view pause
        try {
            liveViewWithThumbnail.getVideoView().onPause();
            liveViewWithThumbnail.getVideoView().onDestroy();
            HistoryManager.getInstance().removeHistoryObserver(uuid);
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
            setLoadingState(null, null);
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
//                showHistoryWheel(false);
                performLayoutAnimation(false);
                //即使网络断开了也要发送 stop
                Subscription subscribe = presenter.stopPlayVideo(JFGRules.PlayErr.ERR_NETWORK).subscribe();
                presenter.addSubscription("presenter.stopPlayVideo(JFGRules.PlayErr.ERR_NETWORK).subscribe()", subscribe);
                handlePlayErr(presenter, JFGRules.PlayErr.ERR_NETWORK);
            } else {
                handlePlayErr(presenter, PLAY_STATE_STOP);
//                showHistoryWheel(true);
            }
        });
    }

    private void changeViewState() {
        // TODO: 2017/8/18 设置为 gone 会导致布局不正确
        liveBottomBannerView.setVisibility(INVISIBLE);
        svSwitchStream.setVisibility(INVISIBLE);
        liveViewWithThumbnail.showFlowView(false, null);
        liveViewWithThumbnail.setThumbnail();
        setHotSeatState(-1, false, false, false, false, false, false);
    }

    @Override
    public void onActivityStart(CamLiveContract.Presenter presenter, Device device) {
        boolean safeIsOpen = device.$(ID_501_CAMERA_ALARM_FLAG, false);
        setFlipped(!safeIsOpen);
        updateLiveViewMode(device.$(509, "1"));
        setHotSeatState(PLAY_STATE_STOP, false, false, false, false, false, false);
    }

    @Override
    public void onActivityResume(CamLiveContract.Presenter presenter, Device device, boolean isUserVisible) {
        final boolean judge = !isSightShown() && !isStandBy();
        Log.d("judge", "judge: " + judge);
        performReLayoutAction();
        performTimeZoneRefresh();
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
        setLoadingState(null, null);
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
        setLoadingState(null, null);
        imgVCamZoomToFullScreen.setEnabled(false);
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


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.imgV_cam_live_land_nav_back:
                if (orientationHandle != null) {
                    orientationHandle.setRequestOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, true);
                }
                // TODO: 2017/8/16 现在需要自动横屏
                break;
            case R.id.imgV_cam_zoom_to_full_screen://点击全屏
                if (orientationHandle != null) {
                    orientationHandle.setRequestOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, true);
                }
                // TODO: 2017/8/16 现在需要自动横屏
                break;
            case R.id.imgV_cam_live_land_play://横屏,左下角播放
                if (playClickListener != null) {
                    playClickListener.onClick(v);
                }
                break;
            case R.id.tv_live://直播中,按钮disable.历史录像:enable
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

    public void setLoadingState(int state, String content) {
        setLoadingState(content, null);
    }


    public void setPlayBtnListener(OnClickListener clickListener) {
        this.playClickListener = clickListener;
    }

    public void setLiveTextClick(OnClickListener liveTextClick) {
        this.liveTextClick = liveTextClick;
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

    @Override
    public void onHistoryChanged(Collection<JFGVideo> history) {
        Log.d(CYLAN_TAG, "历史录像数据发生了变化..");
        superWheelExt.setHistoryFiles(history);
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

    private static final int ANIMATION_DURATION = 250;
    private static final int WAIT_TO_HIDE_DELAY_TIME = 3000;
    private volatile boolean isLayoutAnimationShowing = false;

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
                if (isUserTouchScreen) {
                    postDelayed(this, WAIT_TO_HIDE_DELAY_TIME);
                } else {
                    performLandLayoutAnimation(isLayoutAnimationShowing = false);
                }
            } else {
                performPortLayoutAnimation(isLayoutAnimationShowing = false);
            }
        }
    };

    private boolean canShowLoadingBar() {
        return !isStandBy() && (!isLand() || (livePlayState == PLAY_STATE_LOADING_FAILED || livePlayState == PLAY_STATE_PREPARE));
    }

    private boolean canShowViewModeMenu() {
        return JFGRules.showSwitchModeButton(getDevice().pid) && livePlayState == PLAY_STATE_PLAYING && livePlayType == TYPE_LIVE;
    }

    private boolean canShowStreamSwitcher() {
        return livePlayState == PLAY_STATE_PLAYING && livePlayType == TYPE_LIVE
                && JFGRules.showSdHd(pid, presenter.getDevice().$(207, ""), false);
    }

    private boolean canShowHistoryWheel() {
        return !JFGRules.isShareDevice(device) && JFGRules.hasSDFeature(pid);
    }

    private boolean canShowFlip() {
        return !JFGRules.isShareDevice(device) && JFGRules.hasProtection(device.pid, false);
    }

    private boolean canXunHuanEnable() {
        return livePlayType == TYPE_LIVE && livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid) && enableAutoRotate;
    }

    private boolean canModeSwitchEnable() {
        return livePlayType == TYPE_LIVE && livePlayState == PLAY_STATE_PLAYING && JFGRules.showSwitchModeButton(device.pid);
    }

    private boolean isLivePlaying() {
        return livePlayState == PLAY_STATE_PLAYING;
    }

    private void performLandLayoutAnimation(boolean showLayout) {
        Log.d(TAG, "performLandLayoutAnimation,showLayout:" + showLayout);
        if (showLayout) {
            liveLoadingBar.animate().setDuration(ANIMATION_DURATION).alpha(1).withStartAction(() -> {
                liveLoadingBar.setVisibility(canShowLoadingBar() ? VISIBLE : INVISIBLE
                );//全屏直播门铃 1.需要去掉中间播放按钮
            }).start();
            svSwitchStream.animate().setDuration(ANIMATION_DURATION).alpha(1).translationY(0).withStartAction(() -> {
                svSwitchStream.setVisibility(canShowStreamSwitcher() ? VISIBLE : INVISIBLE);
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
                liveViewModeContainer.setVisibility(canShowViewModeMenu() ? VISIBLE : INVISIBLE);
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
                        liveLoadingBar.setVisibility(canShowLoadingBar() ? VISIBLE : INVISIBLE
                        );
                    }).start();

            svSwitchStream.animate().setDuration(ANIMATION_DURATION).alpha(0).translationY(svSwitchStream.getHeight() / 4)
                    .withStartAction(() -> {
                        svSwitchStream.performSlideAnimation(false);
                    })
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
                liveLoadingBar.setVisibility(canShowLoadingBar() ? VISIBLE : INVISIBLE);
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
            liveLoadingBar.animate().setDuration(ANIMATION_DURATION).alpha(livePlayState == PLAY_STATE_PLAYING ? 0 : 1).translationY(0).withEndAction(() -> {
                liveLoadingBar.setVisibility(canShowLoadingBar() ? VISIBLE : INVISIBLE);
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

    private void performReLayoutAction() {
        livePlayType = presenter.getPlayType();
        livePlayState = presenter.getPlayState();
        boolean isHistory = livePlayType == TYPE_HISTORY;
        boolean isLand = isLand();
        boolean isPlaying = isLivePlaying();
        liveTopBannerView.setVisibility(isLand ? VISIBLE : INVISIBLE);
        bottomControllerContainer.setVisibility(isLand ? INVISIBLE : VISIBLE);
        tvLive.setBackgroundColor(isLand ? Color.TRANSPARENT : Color.WHITE);
        //历史录像显示
        boolean showFlip = canShowFlip();
        layoutLandFlip.setVisibility(showFlip && isLand ? VISIBLE : GONE);
        layoutPortFlip.setVisibility(showFlip && !isLand && isPlaying ? VISIBLE : GONE);
        ViewUtils.setBottomMargin(svSwitchStream, isLand ? (int) getResources().getDimension(R.dimen.y56) : (int) getResources().getDimension(R.dimen.y46));
        //直播
        tvLive.setEnabled(isHistory);
        //显示 昵称
        String alias = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
        imgVCamLiveLandNavBack.setText(alias);
        historyParentContainer.setBackgroundResource(isLand ? R.color.color_4C000000 : android.R.color.transparent);
        imgVCamZoomToFullScreen.setVisibility(isLand ? INVISIBLE : VISIBLE);
        imgVCamLiveLandPlay.setVisibility(isLand ? VISIBLE : GONE);
        liveBottomBannerView.setBackgroundResource(isLand ? android.R.color.transparent : R.drawable.camera_sahdow);
        liveBottomBannerView.setVisibility(isPlaying ? VISIBLE : INVISIBLE);
        historyWheelContainer.setVisibility(canShowHistoryWheel() ? VISIBLE : INVISIBLE);
        historyWheelContainer.setDisplayedChild(presenter.isHistoryEmpty() ? 0 : 1);
        flLoadHistory.setBackgroundResource(isLand ? android.R.color.transparent : R.color.color_F7F8FA);
        vLine.setBackgroundResource(isLand ? android.R.color.transparent : R.color.color_f2f2f2);
        tvLive.setVisibility(historyWheelContainer.getDisplayedChild() == 1 ? VISIBLE : GONE);
        tvLive.setEnabled(isHistory);
        vFlag.setVisibility(historyWheelContainer.getDisplayedChild() == 1 ? VISIBLE : GONE);
        liveViewModeContainer.setVisibility(canShowViewModeMenu() ? VISIBLE : INVISIBLE);
        ivModeXunHuan.setVisibility(JFGRules.showSwitchModeButton(device.pid) ? VISIBLE : INVISIBLE);
        ivModeXunHuan.setEnabled(canXunHuanEnable());
        ivViewModeSwitch.setEnabled(canModeSwitchEnable());
        (imgVCamLiveLandPlay).setImageResource(isPlaying ? R.drawable.icon_landscape_playing : R.drawable.icon_landscape_stop);

        imgVCamTriggerCapture.setEnabled(isPlaying);
        imgVLandCamTriggerCapture.setEnabled(isPlaying);

        imgVCamTriggerMic.setEnabled(isPlaying && !isHistory);
        imgVLandCamTriggerMic.setEnabled(isPlaying && !isHistory);

        imgVCamSwitchSpeaker.setEnabled(isPlaying);
        imgVLandCamSwitchSpeaker.setEnabled(isPlaying);

        imgVCamZoomToFullScreen.setEnabled(isPlaying);
        liveViewWithThumbnail.showFlowView(isPlaying, null);
        svSwitchStream.setVisibility(isPlaying ? View.VISIBLE : GONE);
        liveViewWithThumbnail.setEnabled(true);

        VideoViewFactory.IVideoView videoView = liveViewWithThumbnail.getVideoView();

        if (videoView != null && videoView instanceof Panoramic360ViewRS) {
            try {
                ((Panoramic360ViewRS) videoView).enableAutoRotation(isPlaying && enableAutoRotate);
            } catch (NullPointerException e) {

            }
        }

        if (isLand) {
            //隐藏所有的 showcase
            LiveShowCase.hideHistoryWheelCase((Activity) getContext());
            LiveShowCase.hideHistoryCase((Activity) getContext());
        }

        LayoutParams lp = (LayoutParams) historyParentContainer.getLayoutParams();
        LayoutParams glp = (LayoutParams) liveViewModeContainer.getLayoutParams();
        if (isLand) {
            lp.removeRule(RelativeLayout.BELOW);//remove below rules
            lp.addRule(RelativeLayout.ABOVE, R.id.v_guide);//set above v_guide
//            android:layout_above="@+id/layout_d"
            glp.addRule(RelativeLayout.ABOVE, R.id.layout_e);
            liveViewWithThumbnail.updateLayoutParameters(LayoutParams.MATCH_PARENT, getVideoFinalWidth());
        } else {
            glp.addRule(RelativeLayout.ABOVE, R.id.layout_d);
            lp.removeRule(RelativeLayout.ABOVE);//remove above
            lp.addRule(RelativeLayout.BELOW, R.id.v_guide); //set below v_guide
            updateLiveViewRectHeight(portRatio == -1 ? presenter.getVideoPortHeightRatio() : portRatio);
        }
        historyParentContainer.setLayoutParams(lp);
        liveViewModeContainer.setLayoutParams(glp);
        liveViewWithThumbnail.detectOrientationChanged(!isLand);
        if (!isPlaying) {//显示缩略图
            File file = new File(presenter.getThumbnailKey());
            liveViewWithThumbnail.setThumbnail(getContext(), PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, ""), Uri.fromFile(file));
        }
    }

    private void performTimeZoneRefresh() {
        TimeZone timeZone = JFGRules.getDeviceTimezone(device);
        liveTimeDateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.UK);
        liveTimeDateFormat.setTimeZone(timeZone);
        liveTimeLayout.setTimeZone(timeZone);
        superWheelExt.setTimeZone(timeZone);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isUserTouchScreen = false;
                break;
            default:
                isUserTouchScreen = true;
        }
        return super.onInterceptTouchEvent(ev);
    }
}
