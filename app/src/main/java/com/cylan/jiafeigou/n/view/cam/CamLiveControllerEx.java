package com.cylan.jiafeigou.n.view.cam;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.SimpleCache;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.view.activity.SightSettingActivity;
import com.cylan.jiafeigou.n.view.media.NormalMediaFragment;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LiveTimeLayout;
import com.cylan.jiafeigou.widget.flip.FlipImageView;
import com.cylan.jiafeigou.widget.flip.FlipLayout;
import com.cylan.jiafeigou.widget.live.ILiveControl;
import com.cylan.jiafeigou.widget.live.LiveControlView;
import com.cylan.jiafeigou.widget.pop.RelativePopupWindow;
import com.cylan.jiafeigou.widget.pop.RoundCardPopup;
import com.cylan.jiafeigou.widget.video.LiveViewWithThumbnail;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;
import com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt;
import com.cylan.panorama.CameraParam;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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
import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_FINISH;

/**
 * Created by hds on 17-4-19.
 */

public class CamLiveControllerEx extends RelativeLayout implements ICamLiveLayer,
        View.OnClickListener {
    private String uuid;
    private static final String TAG = "CamLiveControllerEx";
    private ILiveControl.Action action;
    //横屏 top bar
    private View layoutA;
    //流量
    private View layoutB;
    //loading
    private LiveControlView layoutC;
    //防护  |直播|时间|   |全屏|
    private View layoutD;
    //历史录像条
    private View layoutE;
    //|speaker|mic|capture|
    private View layoutF;
    //横屏 侧滑日历
    private View layoutG;

    private boolean isNormalView;
    private int livePlayState;
    private int livePlayType;
    private SuperWheelExt superWheelExt;
    private OnClickListener liveTextClick;//直播按钮
    private OnClickListener liveTimeRectListener;
    private OnClickListener playClickListener;
    private RoundCardPopup roundCardPopup;
    private LiveViewWithThumbnail liveViewWithThumbnail;

    private HistoryWheelHandler historyWheelHandler;

    /**
     * 设备的时区
     */
    private TimeZone mDeviceTimezone;

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
        //竖屏 隐藏
        layoutA = findViewById(R.id.layout_a);
        layoutB = findViewById(R.id.layout_b);
        layoutC = (LiveControlView) findViewById(R.id.layout_c);
        layoutD = findViewById(R.id.layout_d);
        layoutE = findViewById(R.id.layout_e);
        layoutF = findViewById(R.id.layout_f);
        layoutG = findViewById(R.id.layout_g);
        liveViewWithThumbnail = (LiveViewWithThumbnail) findViewById(R.id.v_live);
        superWheelExt = (SuperWheelExt) findViewById(R.id.sw_cam_live_wheel);
        initListener();
    }

    private void initListener() {
//        PerformanceUtils.startTrace("initListener");
        //顶部
        //a.返回,speaker,mic,capture
        Context context = getContext();
        if (context instanceof FragmentActivity) {
            Log.d(TAG, TAG + " context is activity");
            layoutA.findViewById(R.id.imgV_cam_live_land_nav_back).setOnClickListener(this);
            layoutA.findViewById(R.id.imgV_land_cam_switch_speaker).setOnClickListener(this);
            layoutA.findViewById(R.id.imgV_land_cam_trigger_mic).setOnClickListener(this);
            layoutA.findViewById(R.id.imgV_land_cam_trigger_capture).setOnClickListener(this);
        }
        //b.流量
        //c.loading
        (layoutC).setAction(this.action);
        //d.time
//        ((FlipLayout) layoutD.findViewById(R.id.layout_port_flip))
//                .setFlipListener(this);
        layoutD.findViewById(R.id.imgV_cam_zoom_to_full_screen)
                .setOnClickListener(this);
        //e.
        layoutE.findViewById(R.id.imgV_cam_live_land_play).setOnClickListener(this);
        layoutE.findViewById(R.id.tv_live).setOnClickListener(this);
//        ((FlipLayout) layoutE.findViewById(R.id.layout_land_flip)).setFlipListener(this);
        //f
        layoutF.findViewById(R.id.imgV_cam_switch_speaker).setOnClickListener(this);
        layoutF.findViewById(R.id.imgV_cam_trigger_mic).setOnClickListener(this);
        layoutF.findViewById(R.id.imgV_cam_trigger_capture).setOnClickListener(this);
//        PerformanceUtils.stopTrace("initListener");
    }

    @Override
    public void initLiveViewRect(float ratio, Rect rect) {
        updateLiveViewRectHeight(ratio);
        liveViewWithThumbnail.post(() -> liveViewWithThumbnail.getLocalVisibleRect(rect));
    }

    @Override
    public void initView(CamLiveContract.Presenter presenter, String uuid) {
        this.uuid = uuid;
        //disable 6个view
        setHotSeatState(-1, false, false, false, false, false, false);
        findViewById(R.id.imgV_land_cam_trigger_capture).setEnabled(false);
        findViewById(R.id.imgV_cam_trigger_capture).setEnabled(false);
        findViewById(R.id.tv_live).setEnabled(false);
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        if (device == null) {
            AppLogger.e("device is null");
            return;
        }
        isNormalView = !JFGRules.isNeedPanoramicView(device.pid);
        VideoViewFactory.IVideoView videoView = VideoViewFactory.CreateRendererExt(!isNormalView,
                getContext(), true);
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
        String _509 = device.$(509, "1");
        videoView.config360(TextUtils.equals(_509, "0") ? CameraParam.getTopPreset() : CameraParam.getWallPreset());
        videoView.setMode(TextUtils.equals("0", _509) ? 0 : 1);
        liveViewWithThumbnail.setLiveView(videoView);
        initSightSetting(presenter);
        //分享用户不显示
        boolean showFlip = !presenter.isShareDevice();
        findViewById(R.id.layout_port_flip).setVisibility(showFlip ? VISIBLE : INVISIBLE);
        findViewById(R.id.layout_land_flip).setVisibility(showFlip && MiscUtils.isLand() ? VISIBLE : GONE);
        findViewById(R.id.v_divider).setVisibility(showFlip && MiscUtils.isLand() ? VISIBLE : GONE);
    }

    public boolean isSightSettingShow() {
        return isSightSettingShow;
    }

    private boolean isSightSettingShow = false;

    /**
     * 全景视角设置
     */
    private void initSightSetting(CamLiveContract.Presenter basePresenter) {
        if (isNormalView || basePresenter.isShareDevice()) return;
        String uuid = basePresenter.getUuid();
        boolean isFirstShow = PreferencesUtils.getBoolean(KEY_CAM_SIGHT_SETTING + uuid, true);
        if (!isFirstShow) return;//不是第一次
        View oldLayout = liveViewWithThumbnail.findViewById(R.id.fLayout_cam_sight_setting);
        if (oldLayout == null) {
            isSightSettingShow = true;
            layoutC.setVisibility(INVISIBLE);
            View view = LayoutInflater.from(getContext()).inflate(R.layout.cam_sight_setting_overlay, null);
            liveViewWithThumbnail.addView(view);//最顶
            View layout = liveViewWithThumbnail.findViewById(R.id.fLayout_cam_sight_setting);
            ((TextView) (view.findViewById(R.id.tv_sight_setting_content)))
                    .setText(getContext().getString(R.string.Tap1_Camera_Overlook) + ": "
                            + getContext().getString(R.string.Tap1_Camera_OverlookTips));
            view.findViewById(R.id.btn_sight_setting_cancel).setOnClickListener((View v) -> {
                if (layout != null) liveViewWithThumbnail.removeView(layout);
                basePresenter.startPlay();
                isSightSettingShow = false;
            });
            layout.setOnClickListener(v -> AppLogger.d("don't click me"));
            view.findViewById(R.id.btn_sight_setting_next).setOnClickListener((View v) -> {
                liveViewWithThumbnail.removeView(layout);
                Intent intent = new Intent(getContext(), SightSettingActivity.class);
                intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                getContext().startActivity(intent);
                isSightSettingShow = false;
            });
            PreferencesUtils.putBoolean(KEY_CAM_SIGHT_SETTING + uuid, false);
        } else {
            //已经添加了
            oldLayout.setVisibility(View.VISIBLE);
            layoutC.setVisibility(INVISIBLE);
        }
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
            //只有播放的时候才能操作//loading的时候 不能点击
            if (livePlayState == PLAY_STATE_PLAYING) {
                layoutA.setTranslationY(0);
                layoutD.setTranslationY(0);
                layoutE.setTranslationY(0);
                boolean toHide = layoutD.isShown();
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
        if (!show) {
            layoutE.setVisibility(INVISIBLE);
            return;
        } else layoutE.setVisibility(VISIBLE);
        //处理显示逻辑
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        //1.sd
        DpMsgDefine.DPSdStatus status = device.$(204, new DpMsgDefine.DPSdStatus());
        if (!status.hasSdcard || status.err != 0) {
            //隐藏
            layoutE.setVisibility(INVISIBLE);
            return;
        }
        //2.手机无网络
        int net = NetUtils.getJfgNetType();
        if (net == 0) {
            //隐藏
            layoutE.setVisibility(INVISIBLE);
            return;
        }
        //3.没有历史录像
        if (superWheelExt.getDataProvider() != null && superWheelExt.getDataProvider().getDataCount() > 0) {
            //显示
        } else {
            layoutE.setVisibility(INVISIBLE);
            return;
        }
        //4.被分享用户不显示
        if (JFGRules.isShareDevice(device)) {
            layoutE.setVisibility(INVISIBLE);
        }
        //5.设备离线
        if (!JFGRules.isDeviceOnline(device.$(201, new DpMsgDefine.DPNet()))) {
            layoutE.setVisibility(INVISIBLE);
        }
    }

    @Override
    public void initHotRect() {

    }

    @Override
    public void onLivePrepared(int type) {
        livePlayType = type;
        livePlayState = PLAY_STATE_PREPARE;
        setLoadingState(null, null, true);
        findViewById(R.id.imgV_cam_zoom_to_full_screen).setEnabled(false);
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
            layoutD.setVisibility(INVISIBLE);
            showHistoryWheel(false);
            setLoadingState(null, null);
            if (livePlayState == PLAY_STATE_PLAYING) {
                layoutC.setVisibility(INVISIBLE);
            }
        }
    };
    private Runnable portShowRunnable = new Runnable() {
        @Override
        public void run() {
            layoutD.setVisibility(VISIBLE);
            showHistoryWheel(true);
            removeCallbacks(portHideRunnable);
            postDelayed(portHideRunnable, 3000);
            setLoadingState(null, null);
            //应该直接getPlayType.
            if (livePlayType == TYPE_HISTORY && livePlayState == PLAY_STATE_PLAYING) {
                layoutC.setVisibility(VISIBLE);
            } else if (livePlayType == TYPE_LIVE && livePlayState == PLAY_STATE_PLAYING) {
                layoutC.setVisibility(INVISIBLE);
            }
        }
    };

    private Runnable landHideRunnable = new Runnable() {
        @Override
        public void run() {
            setLoadingState(null, null);
            if (livePlayState == PLAY_STATE_PLAYING) {
                layoutC.setVisibility(INVISIBLE);
            }
            YoYo.with(Techniques.SlideOutUp)
                    .duration(200)
                    .playOn(layoutA);
            YoYo.with(Techniques.SlideOutDown)
                    .duration(200)
                    .playOn(layoutD);
            YoYo.with(Techniques.SlideOutDown)
                    .duration(200)
                    .playOn(layoutE);
        }
    };

    private Runnable landShowRunnable = new Runnable() {
        @Override
        public void run() {
            setLoadingState(null, null);
            if (livePlayType == TYPE_HISTORY && livePlayState == PLAY_STATE_PLAYING) {
                layoutC.setVisibility(VISIBLE);
            }
            post(() -> {
                YoYo.with(Techniques.SlideInDown)
                        .duration(250)
                        .playOn(layoutA);
                YoYo.with(Techniques.SlideInUp)
                        .duration(250)
                        .playOn(layoutD);
                YoYo.with(Techniques.SlideInUp)
                        .duration(250)
                        .playOn(layoutE);
                postDelayed(landHideRunnable, 3000);
            });
        }
    };

    private Runnable landShowOrHideRunnable = new Runnable() {

        @Override
        public void run() {
            float t = layoutA.getTranslationY();
            if (layoutA.getTranslationY() != 0) {
                if (t == -layoutA.getMeasuredHeight()) {
                    //显示
                    removeCallbacks(landShowRunnable);
                    removeCallbacks(landHideRunnable);
                    post(landShowRunnable);
                    Log.e(TAG, "点击 显示");
                }
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
        //左下角直播
        ((ImageView) layoutE.findViewById(R.id.imgV_cam_live_land_play))
                .setImageResource(R.drawable.icon_landscape_playing);
        //|直播| 按钮
        layoutE.findViewById(R.id.tv_live).setEnabled(isPlayHistory);
        findViewById(R.id.imgV_cam_trigger_capture).setEnabled(true);
        findViewById(R.id.imgV_land_cam_trigger_capture).setEnabled(true);
        //直播
        findViewById(R.id.tv_live).setEnabled(livePlayType == TYPE_HISTORY);
        setLoadingState(null, null, false);
        liveViewWithThumbnail.onLiveStart();
        findViewById(R.id.imgV_cam_zoom_to_full_screen).setEnabled(true);
        post(portShowRunnable);
        findViewById(R.id.imgV_cam_live_land_play).setEnabled(livePlayType == TYPE_HISTORY);
    }

    private void setLoadingState(String content, String subContent, boolean forceShowOrHide) {
        layoutC.setState(livePlayState, content, subContent);
        layoutC.setVisibility(forceShowOrHide ? VISIBLE : INVISIBLE);
    }

    private void setLoadingState(String content, String subContent) {
        layoutC.setState(livePlayState, content, subContent);
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        DpMsgDefine.DPStandby isStandBY = device.$(508, new DpMsgDefine.DPStandby());
        if (isStandBY.standby) {
            layoutC.setVisibility(INVISIBLE);
            return;
        }
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
        ((ImageView) layoutE.findViewById(R.id.imgV_cam_live_land_play))
                .setImageResource(R.drawable.icon_landscape_stop);
        findViewById(R.id.v_live).setEnabled(true);
        liveViewWithThumbnail.showFlowView(false, null);
        findViewById(R.id.imgV_cam_zoom_to_full_screen).setEnabled(false);
        handlePlayErr(presenter, errCode);
        findViewById(R.id.imgV_land_cam_trigger_capture).setEnabled(false);
        findViewById(R.id.imgV_cam_trigger_capture).setEnabled(false);
        post(portHideRunnable);
        post(() -> liveViewWithThumbnail.onLiveStop());
    }

    /**
     * 错误码 需要放在一个Map里面管理
     *
     * @param errCode
     */
    private void handlePlayErr(CamLiveContract.Presenter presenter, int errCode) {
        switch (errCode) {//这些errCode 应当写在一个map中.Map<Integer,String>
            case JFGRules.PlayErr.ERR_NETWORK:
                livePlayState = PLAY_STATE_LOADING_FAILED;
                setLoadingState(getContext().getString(R.string.OFFLINE_ERR_1), getContext().getString(R.string.USER_HELP), true);
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
        layoutA.setVisibility(isLand ? VISIBLE : GONE);
        layoutF.setVisibility(isLand ? GONE : VISIBLE);
        //历史录像显示
        boolean showFlip = !presenter.isShareDevice();
        findViewById(R.id.layout_land_flip).setVisibility(showFlip && isLand ? VISIBLE : GONE);
        findViewById(R.id.v_divider).setVisibility(showFlip && isLand ? VISIBLE : GONE);
        liveViewWithThumbnail.detectOrientationChanged(!isLand);
        //直播
        findViewById(R.id.tv_live).setEnabled(playType == TYPE_HISTORY);
        RelativeLayout.LayoutParams lp = (LayoutParams) findViewById(R.id.layout_e).getLayoutParams();
        if (isLand) {
            lp.removeRule(3);//remove below rules
            lp.addRule(2, R.id.v_guide);//set above v_guide
            liveViewWithThumbnail.updateLayoutParameters(LayoutParams.MATCH_PARENT);
            findViewById(R.id.imgV_cam_zoom_to_full_screen).setVisibility(GONE);
            layoutD.setBackgroundResource(android.R.color.transparent);
            layoutE.setBackgroundResource(R.color.color_4C000000);
            findViewById(R.id.layout_port_flip).setVisibility(GONE);
            //显示 昵称
            String alias = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
            ((TextView) findViewById(R.id.imgV_cam_live_land_nav_back))
                    .setText(alias);
            findViewById(R.id.imgV_cam_live_land_play).setVisibility(VISIBLE);
        } else {
            findViewById(R.id.imgV_cam_live_land_play).setVisibility(GONE);
            lp.removeRule(2);//remove above
            lp.addRule(3, R.id.v_guide); //set below v_guide
            findViewById(R.id.imgV_cam_zoom_to_full_screen).setVisibility(VISIBLE);
            float ratio = isNormalView ? presenter.getVideoPortHeightRatio() : 1.0f;
            updateLiveViewRectHeight(ratio);
            //有条件的.
            if (presenter.getPlayState() == PLAY_STATE_PLAYING)
                findViewById(R.id.layout_port_flip).setVisibility(showFlip ? VISIBLE : INVISIBLE);
            layoutD.setBackgroundResource(R.drawable.camera_sahdow);
            layoutE.setBackgroundResource(android.R.color.transparent);
            layoutG.setVisibility(GONE);
            if (historyWheelHandler != null) historyWheelHandler.onBackPress();
        }
        findViewById(R.id.v_divider).setVisibility(isLand ? VISIBLE : GONE);
        findViewById(R.id.layout_e).setLayoutParams(lp);
        resetAndPrepareNextAnimation(isLand);
    }

    private void resetAndPrepareNextAnimation(boolean land) {
        //切换到了横屏,必须先恢复view的位置才能 重新开始动画
        layoutA.setTranslationY(0);
        layoutD.setTranslationY(0);
        layoutE.setTranslationY(0);
        if (land) {
            layoutE.setVisibility(VISIBLE);
            removeCallbacks(portHideRunnable);
            removeCallbacks(landHideRunnable);
            removeCallbacks(landShowRunnable);
            postDelayed(landHideRunnable, 3000);//3s后隐藏
        } else {
            removeCallbacks(portHideRunnable);
            removeCallbacks(landHideRunnable);
            removeCallbacks(landShowRunnable);
            postDelayed(portHideRunnable, 3000);
        }
    }

    @Override
    public void onRtcpCallback(int type, JFGMsgVideoRtcp rtcp) {
        livePlayState = PLAY_STATE_PLAYING;
        String flow = MiscUtils.getByteFromBitRate(rtcp.bitRate);
        liveViewWithThumbnail.showFlowView(true, flow);
        //分享账号不显示啊.
        if (JFGRules.isShareDevice(uuid)) return;
        setLiveRectTime(livePlayType, rtcp.timestamp);
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
                    ToastUtil.showNegativeToast(getContext().getString(R.string.has_not_sdcard));
                    return;
                }
                if (historyWheelHandler != null) {
                    ViewUtils.deBounceClick(v);
                    historyWheelHandler.showDatePicker(MiscUtils.isLand());
                }
            };
            (layoutD.findViewById(R.id.live_time_layout)).setOnClickListener(liveTimeRectListener);
        }
    }


    private void setLiveRectTime(int type, long timestamp) {
        //需要考虑设备的时区.他娘的.
        if (mDeviceTimezone == null) {
            mDeviceTimezone = TimeZone.getDefault();
        }
        //全景的时间戳是0,使用设备的时区
        //wifi狗是格林尼治时间戳,需要-8个时区.
        if (timestamp == 0) timestamp = System.currentTimeMillis() / 1000;
        String content = String.format(getContext().getString(type == 1 ? R.string.Tap1_Camera_VideoLive : R.string.Tap1_Camera_Playback)
                + "|%s", getTime(mDeviceTimezone, timestamp * 1000L));
        ((LiveTimeLayout) layoutD.findViewById(R.id.live_time_layout))
                .setContent(content);
    }

    private String getTime(TimeZone timeZone, long time) {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd HH:mm", Locale.UK);
        format.setTimeZone(timeZone);
        return format.format(new Date(time));
    }

    public void setFlipListener(FlipImageView.OnFlipListener flipListener) {
        ((FlipLayout) findViewById(R.id.layout_land_flip)).setFlipListener(flipListener);
        ((FlipLayout) findViewById(R.id.layout_port_flip)).setFlipListener(flipListener);
    }

    public void setFlipped(boolean flip) {
        ((FlipLayout) findViewById(R.id.layout_land_flip)).setFlipped(flip);
        ((FlipLayout) findViewById(R.id.layout_port_flip)).setFlipped(flip);
    }


    @Override
    public void onResolutionRsp(JFGMsgVideoResolution resolution) {
        try {
            BaseApplication.getAppComponent().getCmd().enableRenderSingleRemoteView(true, (View) liveViewWithThumbnail.getVideoView());
        } catch (JfgException e) {
            AppLogger.e("err:" + MiscUtils.getErr(e));
        }
        float ratio = isNormalView ? (float) resolution.height / resolution.width : 1.0f;
        if (isLand()) {
            //需要考虑下横屏,match_parent
            ratio = (float) Resources.getSystem().getDisplayMetrics().heightPixels /
                    Resources.getSystem().getDisplayMetrics().widthPixels;
        }
        updateLiveViewRectHeight(ratio);
    }

    /**
     * 分辨率 (float)h/w
     *
     * @param ratio
     */
    private void updateLiveViewRectHeight(float ratio) {
        liveViewWithThumbnail.updateLayoutParameters((int) (Resources.getSystem().getDisplayMetrics().widthPixels * ratio));
    }

    @Override
    public void onHistoryDataRsp(CamLiveContract.Presenter presenter) {
        showHistoryWheel(true);
        if (historyWheelHandler == null) {
            historyWheelHandler = new HistoryWheelHandler((ViewGroup) layoutG, superWheelExt, presenter);
        }
        historyWheelHandler.dateUpdate();
        historyWheelHandler.setDatePickerListener((time, state) -> {
            //选择时间,更新时间区域
            post(() -> {
                setLiveRectTime(livePlayType, time);
                prepareLayoutDAnimation(state == STATE_FINISH);
            });
        });
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
        liveViewWithThumbnail.enableStandbyMode(standby.standby, clickListener, !TextUtils.isEmpty(device.shareAccount));
        if (standby.standby && !isLand()) {
            post(portHideRunnable);
            setLoadingState(null, null, false);
        }
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
        try {
            PerformanceUtils.startTrace("showPopupWindow");
            roundCardPopup = new RoundCardPopup(getContext(), view -> {
                view.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
            }, v -> {
                roundCardPopup.dismiss();
                Bundle bundle = new Bundle();
                bundle.putParcelable(JConstant.KEY_SHARE_ELEMENT_BYTE, bitmap);
                NormalMediaFragment fragment = NormalMediaFragment.newInstance(bundle);
                ActivityUtils.addFragmentSlideInFromRight(activity.getSupportFragmentManager(), fragment,
                        android.R.id.content);
                fragment.setCallBack(t -> activity.getSupportFragmentManager().popBackStack());
            });
            roundCardPopup.setAutoDismissTime(5 * 1000L);
            roundCardPopup.showOnAnchor(findViewById(R.id.imgV_cam_trigger_capture), RelativePopupWindow.VerticalPosition.ABOVE, RelativePopupWindow.HorizontalPosition.CENTER);
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
        if (!connected) {
            post(() -> {
                showHistoryWheel(false);
                handlePlayErr(presenter, JFGRules.PlayErr.ERR_NETWORK);
            });
        }
    }

    @Override
    public void onActivityStart(CamLiveContract.Presenter presenter, Device device) {
        boolean safeIsOpen = device.$(ID_501_CAMERA_ALARM_FLAG, false);
        setFlipped(!safeIsOpen);
        updateLiveViewMode(device.$(509, "0"));
        DpMsgDefine.DPNet net = device.$(201, new DpMsgDefine.DPNet());
        if (!JFGRules.isDeviceOnline(net)) return;//设备离线,不需要显示了
        Bitmap bitmap = SimpleCache.getInstance().getSimpleBitmapCache(presenter.getThumbnailKey());
        if (bitmap == null || bitmap.isRecycled()) {
            File file = new File(presenter.getThumbnailKey());
            liveViewWithThumbnail.setThumbnail(getContext(), PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, ""), Uri.fromFile(file));
        } else
            liveViewWithThumbnail.setThumbnail(getContext(), PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, ""), SimpleCache.getInstance().getSimpleBitmapCache(presenter.getThumbnailKey()));
        mDeviceTimezone = JFGRules.getDeviceTimezone(device);
        AppLogger.d("得到设备时区:" + mDeviceTimezone.getID());
    }

    @Override
    public void updateLiveViewMode(String mode) {
        liveViewWithThumbnail.getVideoView().config360(TextUtils.equals(mode, "0") ? CameraParam.getTopPreset() : CameraParam.getWallPreset());
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
        ImageView pMic = (ImageView) findViewById(R.id.imgV_cam_trigger_mic);
        pMic.setEnabled(micEnable);
        pMic.setImageResource(portMicRes[mic ? 1 : 0]);
        ImageView lMic = (ImageView) findViewById(R.id.imgV_land_cam_trigger_mic);
        lMic.setEnabled(micEnable);
        lMic.setImageResource(landMicRes[mic ? 1 : 0]);
        //speaker
        ImageView pSpeaker = (ImageView) findViewById(R.id.imgV_cam_switch_speaker);
        pSpeaker.setEnabled(speakerEnable);
        pSpeaker.setImageResource(portSpeakerRes[speaker ? 1 : 0]);
        ImageView lSpeaker = (ImageView) findViewById(R.id.imgV_land_cam_switch_speaker);
        lSpeaker.setEnabled(speakerEnable);
        lSpeaker.setImageResource(landSpeakerRes[speaker ? 1 : 0]);
        //capture
        //只有 enable和disable
        ImageView pCapture = (ImageView) findViewById(R.id.imgV_cam_trigger_capture);
        pCapture.setEnabled(captureEnable);
        ImageView lCapture = (ImageView) findViewById(R.id.imgV_land_cam_trigger_capture);
        lCapture.setEnabled(captureEnable);
        Log.d(TAG, String.format(Locale.getDefault(), "hotSeat:speaker:%s,speakerEnable:%s,mic:%s,micEnable:%s", speaker, speakerEnable, mic, micEnable));
    }

    @Override
    public void setHotSeatListener(OnClickListener micListener,
                                   OnClickListener speakerListener,
                                   OnClickListener captureListener) {
        findViewById(R.id.imgV_cam_switch_speaker).setOnClickListener(speakerListener);
        findViewById(R.id.imgV_land_cam_switch_speaker).setOnClickListener(speakerListener);
        findViewById(R.id.imgV_cam_trigger_mic).setOnClickListener(micListener);
        findViewById(R.id.imgV_land_cam_trigger_mic).setOnClickListener(micListener);
        findViewById(R.id.imgV_cam_trigger_capture).setOnClickListener(captureListener);
        findViewById(R.id.imgV_land_cam_trigger_capture).setOnClickListener(captureListener);
    }

    @Override
    public int getMicState() {
        Object o = findViewById(R.id.imgV_land_cam_trigger_mic).getTag();
        if (o != null && o instanceof Integer) {
            int tag = (int) o;
            switch (tag) {
                case R.drawable.icon_land_mic_on_selector:
                    if (findViewById(R.id.imgV_land_cam_trigger_mic).isEnabled()) {
                        return 3;
                    } else return 1;
                case R.drawable.icon_land_mic_off_selector:
                    if (findViewById(R.id.imgV_land_cam_trigger_mic).isEnabled()) {
                        return 2;
                    } else return 0;
            }
        }
        return 0;
    }

    @Override
    public int getSpeakerState() {
        Object o = findViewById(R.id.imgV_land_cam_switch_speaker).getTag();
        if (o != null && o instanceof Integer) {
            int tag = (int) o;
            switch (tag) {
                case R.drawable.icon_land_speaker_on_selector:
                    if (findViewById(R.id.imgV_cam_switch_speaker).isEnabled()) {
                        return 3;
                    } else return 1;
                case R.drawable.icon_land_speaker_off_selector:
                    if (findViewById(R.id.imgV_cam_switch_speaker).isEnabled()) {
                        return 2;
                    } else return 0;
            }
        }
        return 0;
    }

    @Override
    public void resumeGoodFrame() {
        livePlayState = PLAY_STATE_PLAYING;
        setLoadingState(null, null, false);
        findViewById(R.id.imgV_cam_zoom_to_full_screen).setEnabled(true);
        //0:off-disable,1.on-disable,2.off-enable,3.on-enable
        if (livePlayType == TYPE_HISTORY) {
            findViewById(R.id.imgV_land_cam_trigger_mic).setEnabled(false);
            findViewById(R.id.imgV_cam_trigger_mic).setEnabled(false);
        }
        findViewById(R.id.imgV_cam_trigger_capture).setEnabled(true);
        findViewById(R.id.imgV_land_cam_trigger_capture).setEnabled(true);
    }

    @Override
    public void startBadFrame() {
        livePlayState = PLAY_STATE_PREPARE;
        setLoadingState(null, null, true);
        findViewById(R.id.imgV_cam_zoom_to_full_screen).setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.imgV_cam_live_land_nav_back:
                post(() -> ViewUtils.setRequestedOrientation((Activity) getContext(),
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
                break;
            case R.id.imgV_cam_zoom_to_full_screen://点击全屏
                post(() -> ViewUtils.setRequestedOrientation((Activity) getContext(),
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
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

}
