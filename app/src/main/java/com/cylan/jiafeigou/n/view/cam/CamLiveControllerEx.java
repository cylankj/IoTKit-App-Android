package com.cylan.jiafeigou.n.view.cam;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.view.media.NormalMediaFragment;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LiveTimeLayout;
import com.cylan.jiafeigou.widget.flip.FlipImageView;
import com.cylan.jiafeigou.widget.flip.FlipLayout;
import com.cylan.jiafeigou.widget.live.ILiveControl;
import com.cylan.jiafeigou.widget.live.LivePlayControlView;
import com.cylan.jiafeigou.widget.pop.RelativePopupWindow;
import com.cylan.jiafeigou.widget.pop.RoundCardPopup;
import com.cylan.jiafeigou.widget.video.LiveViewWithThumbnail;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;
import com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt;
import com.cylan.panorama.CameraParam;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_501_CAMERA_ALARM_FLAG;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_IDLE;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_LOADING_FAILED;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PLAYING;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PREPARE;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_STOP;
import static com.cylan.jiafeigou.misc.JFGRules.PlayErr.STOP_MAUNALLY;
import static com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract.TYPE_HISTORY;

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
    private LivePlayControlView layoutC;
    //防护  |直播|时间|   |全屏|
    private View layoutD;
    //历史录像条
    private View layoutE;
    //|speaker|mic|capture|
    private View layoutF;
    //横屏 侧滑日历
    private View layoutG;

    private boolean isNormalView;

    private SuperWheelExt superWheelExt;
    private OnClickListener liveTimeRectClick;
    private OnClickListener liveTimeRectListener;
    private RoundCardPopup roundCardPopup;
    private LiveViewWithThumbnail liveViewWithThumbnail;

    private HistoryWheelHandler historyWheelHandler;

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
        layoutC = (LivePlayControlView) findViewById(R.id.layout_c);
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
        layoutD.findViewById(R.id.live_time_layout).setOnClickListener(this);
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
    public void initView(String uuid) {
        this.uuid = uuid;
        //disable 6个view
        findViewById(R.id.imgV_land_cam_switch_speaker).setEnabled(false);
        findViewById(R.id.imgV_land_cam_trigger_mic).setEnabled(false);
        findViewById(R.id.imgV_land_cam_trigger_capture).setEnabled(false);
        findViewById(R.id.imgV_cam_switch_speaker).setEnabled(false);
        findViewById(R.id.imgV_cam_trigger_mic).setEnabled(false);
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
        videoView.detectOrientationChanged();
        liveViewWithThumbnail.setLiveView(videoView);
    }

    /**
     * 视频区域
     */
    private void onLiveRectTap() {
        AppLogger.e("点击");
        if (isLand()) {
            float t = layoutA.getTranslationY();
            Log.d("xxxxxxxxxxx", "t: " + t);
            if (layoutA.getTranslationY() != 0) {
                if (t == -layoutA.getMeasuredHeight()) {
                    //显示
                    layoutA.removeCallbacks(landShowRunnable);
                    layoutA.removeCallbacks(landHideRunnable);
                    layoutA.post(landShowRunnable);
                    layoutA.postDelayed(landHideRunnable, 3000);
                }
            } else {
                //横屏,隐藏
                layoutA.removeCallbacks(landShowRunnable);
                layoutA.removeCallbacks(landHideRunnable);
                layoutA.post(landHideRunnable);
            }
        } else {
            if (isStandBy()) {
                post(portHideRunnable);
                return;
            }
            layoutA.setTranslationY(0);
            layoutD.setTranslationY(0);
            layoutE.setTranslationY(0);
            boolean toHide = layoutD.isShown();
            layoutD.setVisibility(toHide ? INVISIBLE : VISIBLE);
            showHistoryWheel(!toHide);
            if (!toHide) prepareLayoutDAnimation();
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
    public void onLivePrepared() {
        layoutC.setState(PLAY_STATE_PREPARE, null);
    }

    private boolean isLand() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * 3s隐藏
     */
    private void prepareLayoutDAnimation() {
        layoutD.removeCallbacks(portHideRunnable);
        layoutD.postDelayed(portHideRunnable, 3000);
    }

    private Runnable portHideRunnable = new Runnable() {
        @Override
        public void run() {
            layoutD.setVisibility(INVISIBLE);
            showHistoryWheel(false);
        }
    };
    private Runnable landHideRunnable = new Runnable() {
        @Override
        public void run() {
            AnimatorUtils.slideOut(layoutA, true);
            AnimatorUtils.slideOut(layoutD, false);
            AnimatorUtils.slideOut(layoutE, false);
        }
    };
    private Runnable landShowRunnable = new Runnable() {
        @Override
        public void run() {
            AnimatorUtils.slideIn(layoutA, true);
            AnimatorUtils.slideIn(layoutD, false);
            AnimatorUtils.slideIn(layoutE, false);
        }
    };

    private void removeViewAnimation(View view, Runnable runnable) {
        view.removeCallbacks(runnable);
    }

    @Override
    public void onLiveStart(CamLiveContract.Presenter presenter, Device device) {
        int playState = presenter.getPlayState();
        int playType = presenter.getPlayType();
        boolean isPlayHistory = playType == TYPE_HISTORY;
        //左下角直播
        ((ImageView) layoutE.findViewById(R.id.imgV_cam_live_land_play))
                .setImageResource(R.drawable.icon_landscape_stop);
        //|直播| 按钮
        layoutE.findViewById(R.id.tv_live).setEnabled(isPlayHistory);
        findViewById(R.id.imgV_land_cam_switch_speaker).setEnabled(true);
        findViewById(R.id.imgV_land_cam_trigger_mic).setEnabled(!isPlayHistory);//历史录像,disable
        findViewById(R.id.imgV_land_cam_trigger_capture).setEnabled(true);
        findViewById(R.id.imgV_cam_switch_speaker).setEnabled(true);
        findViewById(R.id.imgV_cam_trigger_mic).setEnabled(!isPlayHistory);
        findViewById(R.id.imgV_cam_trigger_capture).setEnabled(true);
        //直播
        findViewById(R.id.tv_live).setEnabled(playType == TYPE_HISTORY);
        layoutC.setState(PLAY_STATE_PLAYING, null);
        liveViewWithThumbnail.onLiveStart();
        findViewById(R.id.imgV_cam_zoom_to_full_screen).setEnabled(true);
        layoutD.setVisibility(VISIBLE);
    }

    @Override
    public void onLiveStop(CamLiveContract.Presenter presenter, Device device, int errCode) {
        layoutB.setVisibility(GONE);
        ((ImageView) layoutE.findViewById(R.id.imgV_cam_live_land_play))
                .setImageResource(R.drawable.icon_landscape_playing);
        findViewById(R.id.imgV_land_cam_switch_speaker).setEnabled(true);
        findViewById(R.id.imgV_land_cam_trigger_mic).setEnabled(true);
        findViewById(R.id.imgV_land_cam_trigger_capture).setEnabled(true);
        findViewById(R.id.imgV_cam_switch_speaker).setEnabled(true);
        findViewById(R.id.imgV_cam_trigger_mic).setEnabled(true);
        findViewById(R.id.imgV_cam_trigger_capture).setEnabled(true);
        findViewById(R.id.v_live).setEnabled(true);
        liveViewWithThumbnail.onLiveStop();
        liveViewWithThumbnail.showFlowView(false, null);
        findViewById(R.id.imgV_cam_zoom_to_full_screen).setEnabled(false);
        handlePlayErr(errCode);
    }

    private void handlePlayErr(int errCode) {
        switch (errCode) {//这些errCode 应当写在一个map中.Map<Integer,String>
            case JFGRules.PlayErr.ERR_NERWORK:
                Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
                DpMsgDefine.DPStandby isStandBY = device.$(508, new DpMsgDefine.DPStandby());
                if (isStandBY == null || isStandBY.standby) break;//
                layoutC.setState(PLAY_STATE_LOADING_FAILED, getContext().getString(R.string.OFFLINE_ERR_1), getContext().getString(R.string.USER_HELP));
                break;
            case JFGRules.PlayErr.ERR_UNKOWN:
                layoutC.setState(PLAY_STATE_LOADING_FAILED, getContext().getString(R.string.NO_NETWORK_2));
                break;
            case JFGRules.PlayErr.ERR_LOW_FRAME_RATE:
                int net = NetUtils.getJfgNetType(getContext());
                layoutC.setState(PLAY_STATE_LOADING_FAILED, getContext().getString(R.string.GLOBAL_NO_NETWORK), net == 0 ? getContext().getString(R.string.USER_HELP) : null);
                break;
            case JFGRules.PlayErr.ERR_DEVICE_OFFLINE:
            case JError.ErrorVideoPeerNotExist:
                layoutC.setState(PLAY_STATE_LOADING_FAILED, getContext().getString(R.string.OFFLINE_ERR), getContext().getString(R.string.USER_HELP));
                break;
            case JError.ErrorVideoPeerInConnect:
                //正在直播...
                layoutC.setState(PLAY_STATE_LOADING_FAILED, getContext().getString(R.string.CONNECTING));
                break;
            case STOP_MAUNALLY:
                layoutC.setState(PLAY_STATE_STOP, null);
                break;
            case JFGRules.PlayErr.ERR_NOT_FLOW:
                layoutC.setState(PLAY_STATE_LOADING_FAILED, getContext().getString(R.string.NETWORK_TIMEOUT));
                break;
            default:
                layoutC.setState(PLAY_STATE_LOADING_FAILED, getContext().getString(R.string.GLOBAL_NO_NETWORK));
                break;
        }
    }

    @Override
    public void orientationChanged(CamLiveContract.Presenter presenter, Device device, int orientation) {
        int playState = presenter.getPlayState();
        int playType = presenter.getPlayType();
        boolean isLand = isLand();
        layoutA.setVisibility(isLand ? VISIBLE : GONE);
        layoutF.setVisibility(isLand ? GONE : VISIBLE);
        //历史录像显示
        findViewById(R.id.imgV_cam_live_land_play).setVisibility(playType == TYPE_HISTORY && isLand ? VISIBLE : GONE);
        findViewById(R.id.layout_land_flip).setVisibility(isLand ? VISIBLE : GONE);
        findViewById(R.id.v_divider).setVisibility(isLand ? VISIBLE : GONE);
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
        } else {
            lp.removeRule(2);//remove above
            lp.addRule(3, R.id.v_guide); //set below v_guide
            findViewById(R.id.imgV_cam_zoom_to_full_screen).setVisibility(VISIBLE);
            float ratio = isNormalView ? presenter.getVideoPortHeightRatio() : 1.0f;
            updateLiveViewRectHeight(ratio);
            //有条件的.
            if (presenter.getPlayState() == PLAY_STATE_PLAYING)
                findViewById(R.id.layout_port_flip).setVisibility(VISIBLE);
            layoutD.setBackgroundResource(R.drawable.camera_sahdow);
            layoutE.setBackgroundResource(android.R.color.transparent);
            if (historyWheelHandler != null) historyWheelHandler.onBackPress();
        }
        findViewById(R.id.v_divider).setVisibility(isLand ? VISIBLE : GONE);
        findViewById(R.id.layout_e).setLayoutParams(lp);
        resetAndPrepareNextAnimation(isLand);
    }

    private void resetAndPrepareNextAnimation(boolean land) {
        //切换到了横屏
        layoutA.setTranslationY(0);
        layoutD.setTranslationY(0);
        layoutE.setTranslationY(0);
        if (land) {
            removeViewAnimation(layoutD, portHideRunnable);
            removeViewAnimation(layoutD, landHideRunnable);
            removeViewAnimation(layoutD, landShowRunnable);
            layoutD.postDelayed(landHideRunnable, 3000);//3s后隐藏
        } else {
            removeViewAnimation(layoutD, portHideRunnable);
            removeViewAnimation(layoutD, landHideRunnable);
            removeViewAnimation(layoutD, landShowRunnable);
            layoutD.postDelayed(portHideRunnable, 3000);
        }
    }

    @Override
    public void onRtcpCallback(int type, JFGMsgVideoRtcp rtcp) {
        String flow = MiscUtils.getByteFromBitRate(rtcp.bitRate);
        liveViewWithThumbnail.showFlowView(true, flow);
        //分享账号不显示啊.
        if (JFGRules.isShareDevice(uuid)) return;
        String content = String.format(getContext().getString(type == 1 ? R.string.Tap1_Camera_VideoLive : R.string.Tap1_Camera_Playback)
                + "|%s", TimeUtils.getHistoryTime1(rtcp.timestamp * 1000L));
        ((LiveTimeLayout) layoutD.findViewById(R.id.live_time_layout))
                .setContent(content);
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
                if (!status.hasSdcard || status.err != 0) {
                    ToastUtil.showNegativeToast(getContext().getString(R.string.has_not_sdcard));
                    return;
                }
                if (historyWheelHandler != null)
                    historyWheelHandler.showDatePicker(MiscUtils.isLand());
            };
            (layoutD.findViewById(R.id.live_time_layout)).setOnClickListener(liveTimeRectListener);
        }
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
        updateLiveViewRectHeight(ratio);
    }

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
            layoutC.setState(PLAY_STATE_IDLE, null);
        }
    }

    private boolean isStandBy() {
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        DpMsgDefine.DPStandby standby = device.$(508, new DpMsgDefine.DPStandby());
        return standby.standby;
    }

    @Override
    public void onLoadPreviewBitmap(Bitmap bitmap) {
//        if (isVisible()) {
//            vLive.post(() -> vLive.setThumbnail(getContext(), PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, ""), bitmap));
//        }
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
    public void onNetworkChanged(boolean connected) {
        if (!connected) {
            post(() -> showHistoryWheel(false));
        }
    }

    @Override
    public void onActivityStart(Device device) {
        boolean safeIsOpen = device.$(ID_501_CAMERA_ALARM_FLAG, false);
        setFlipped(!safeIsOpen);
        updateLiveViewMode(device.$(509, "0"));
    }

    @Override
    public void setCaptureListener(OnClickListener captureListener) {
        findViewById(R.id.imgV_cam_trigger_capture).setOnClickListener(captureListener);
        findViewById(R.id.imgV_land_cam_trigger_capture).setOnClickListener(captureListener);
    }

    @Override
    public void updateLiveViewMode(String mode) {
        liveViewWithThumbnail.getVideoView().config360(TextUtils.equals(mode, "0") ? CameraParam.getTopPreset() : CameraParam.getWallPreset());
        liveViewWithThumbnail.getVideoView().setMode(TextUtils.equals("0", mode) ? 0 : 1);
        liveViewWithThumbnail.getVideoView().detectOrientationChanged();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.imgV_cam_live_land_nav_back:
                ViewUtils.setRequestedOrientation((Activity) getContext(),
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case R.id.imgV_cam_zoom_to_full_screen://点击全屏
                ViewUtils.setRequestedOrientation((Activity) getContext(),
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case R.id.live_time_layout://时间区域
                handleTimeRectClick();
                break;
            case R.id.imgV_cam_live_land_play://横屏,左下角播放
                break;
            case R.id.tv_live://直播中,按钮disable.历史录像:enable
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

    private void handleTimeRectClick() {

        if (isLand()) {
            //弹窗
        } else {
            //右边侧滑进来
        }
    }


}
