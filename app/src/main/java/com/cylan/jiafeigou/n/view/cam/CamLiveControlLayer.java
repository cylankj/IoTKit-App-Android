package com.cylan.jiafeigou.n.view.cam;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.listener.LiveListener;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.flip.FlipImageView;
import com.cylan.jiafeigou.widget.flip.FlipLayout;
import com.cylan.jiafeigou.widget.wheel.ex.IData;
import com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt;
import com.cylan.utils.DensityUtils;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.widget.RelativeLayout.BELOW;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_IDLE;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PLAYING;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PREPARE;
import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_ADSORB;
import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_DRAGGING;
import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_FINISH;

/**
 * 摄像头直播页面
 * <p>
 * |<-   xxx房间        speaker mic takeSnapshot|
 * <p>
 * <p>
 * <p>
 * <p>
 * |播放  | -----------滚动条------- 直播| 安全防护|
 * <p>
 * Created by cylan-hunt on 16-7-27.
 */
public class CamLiveControlLayer extends FrameLayout implements
        SuperWheelExt.WheelRollListener,
        CamLiveLandTopBar.TopBarAction,
        FlipImageView.OnFlipListener,
        LiveListener {
    /**
     * 显示状态:0:隐藏,1:显示,2:淡入,3:淡出,4:slide_out_up,5:slide_out_down
     */
    private int showState;
    private WeakReference<Activity> activityWeakReference;

    @BindView(R.id.lLayout_cam_history_wheel)
    LinearLayout lLayoutCamHistoryWheel;
    @BindView(R.id.sw_cam_live_wheel)
    SuperWheelExt swCamLiveWheel;
    @BindView(R.id.tv_cam_live_port_live)
    TextView tvCamLivePortLive;
    @BindView(R.id.fLayout_cam_live_land_top_bar)
    CamLiveLandTopBar fLayoutCamLiveLandTopBar;
    @BindView(R.id.imgV_cam_live_land_play)
    ImageView imgVCamLiveLandPlay;
    @BindView(R.id.lLayout_protection)
    FlipLayout lLayoutProtection;

    private IData iDataProvider;

    private WeakReference<CamLiveContract.Presenter> presenterRef;

    public CamLiveControlLayer(Context context) {
        this(context, null);
    }

    public CamLiveControlLayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CamLiveControlLayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_cam_live_control_layer, this, true);
        ButterKnife.bind(view);
        swCamLiveWheel.setWheelRollListener(this);
        fLayoutCamLiveLandTopBar.setTopBarAction(this);
        initFlipLayout();
    }

    private void initFlipLayout() {
        //偷懒,这些都应该封装在FlipLayout内部的.
        //设置切换动画
        lLayoutProtection.getFlipImageView().setRotationXEnabled(true);
        lLayoutProtection.getFlipImageView().setDuration(200);
        lLayoutProtection.getFlipImageView().setInterpolator(new DecelerateInterpolator());
        lLayoutProtection.getFlipImageView().setOnFlipListener(this);
        //初始状态
        if (presenterRef != null && presenterRef.get() != null && presenterRef.get().getCamInfo() != null) {
            if (!presenterRef.get().getCamInfo().cameraAlarmFlag) {
                lLayoutProtection.getFlipImageView().setFlipped(true);
            }
        }
        //大区域
        lLayoutProtection.setOnClickListener((View v) ->
                lLayoutProtection.getFlipImageView().performClick());
    }

    public IData getDataProvider() {
        return iDataProvider;
    }

    public void setActivity(Activity activity) {
        this.activityWeakReference = new WeakReference<>(activity);
    }

    public void setupHistoryData(IData dataProvider) {
        this.iDataProvider = dataProvider;
        final long time = System.currentTimeMillis();
        swCamLiveWheel.setDataProvider(dataProvider);
        Log.d("performance", "CamLivePortWheel performance: " + (System.currentTimeMillis() - time));
    }

    /**
     * 改变播放类型文字:{直播,返回}
     *
     * @param liveType
     */
    public void setLiveType(int liveType) {
        tvCamLivePortLive.setText(getResources().getString(liveType == CamLiveContract.TYPE_LIVE ? R.string.Tap1_Camera_VideoLive : R.string.BACK));
    }

    /**
     * 屏幕方向改变.
     */
    public void notifyOrientationChange(final int orientation) {
        post(new Runnable() {
            @Override
            public void run() {
                fLayoutCamLiveLandTopBar.setVisibility(orientation == Configuration.ORIENTATION_LANDSCAPE ? VISIBLE : GONE);
                findViewById(R.id.imgV_cam_live_land_play).setVisibility(orientation == Configuration.ORIENTATION_LANDSCAPE ? VISIBLE : GONE);
                findViewById(R.id.v_divider).setVisibility(orientation == Configuration.ORIENTATION_LANDSCAPE ? VISIBLE : GONE);
                findViewById(R.id.lLayout_protection).setVisibility(orientation == Configuration.ORIENTATION_LANDSCAPE ? VISIBLE : GONE);
                lLayoutCamHistoryWheel.setBackgroundColor(orientation == Configuration.ORIENTATION_LANDSCAPE ? getResources().getColor(R.color.color_4C000000) : Color.TRANSPARENT);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    layoutParams.removeRule(BELOW);
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                } else {
                    layoutParams.addRule(BELOW, R.id.fLayout_cam_live_view);
                    layoutParams.height = DensityUtils.dip2px(52);
                }
                setLayoutParams(layoutParams);
            }
        });
    }

    public void setPresenterRef(CamLiveContract.Presenter presenterRef) {
        this.presenterRef = new WeakReference<>(presenterRef);
    }

    /**
     * @param time :定位到某个时间
     */
    public void setNav2Time(long time) {
        swCamLiveWheel.setPositionByTime(time);
    }

    public long getWheelCurrentFocusTime() {
        return swCamLiveWheel.getCurrentFocusTime();
    }

    /**
     * 根据播放状态更新
     */
    public void updateVisibilityState(boolean show) {
        if (presenterRef != null && presenterRef.get() != null) {
            int count = iDataProvider == null ? 0 : iDataProvider.getDataCount();
            if (count == 0) {
                AppLogger.i("没有历史视频数据,或者没准备好");
                return;
            }
            boolean deviceState = JFGRules.isDeviceOnline(presenterRef.get().getCamInfo().net);

            //播放状态
            int playState = presenterRef.get().getPlayState();
            int orientation = this.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                //横屏 slide_out_up  slide_in_up
            } else {
                //竖屏 ,淡入淡出,
                if (!deviceState) {
                    //设备离线
                    AppLogger.i("设备离线");
                    return;
                }
                setVisibility(show ? VISIBLE : INVISIBLE);
            }
        }
    }

    /**
     * 设置安全防护状态
     *
     * @param state
     */
    public void setProtectionState(boolean state) {
        if (!check())
            return;
        if (lLayoutProtection.getFlipImageView() != null) {
            boolean isFlipped = lLayoutProtection.getFlipImageView().isFlipped();//切换到背面
            if ((isFlipped && state) || (!state && !isFlipped))
                lLayoutProtection.getFlipImageView().performClick();
            lLayoutProtection.getTextView().setText(state ? lLayoutProtection.getContext().getString(R.string.SECURE) : "");
        }
    }

    private boolean check() {
        return activityWeakReference != null && activityWeakReference.get() != null;
    }

    @Override
    public void onTimeUpdate(long time, int state) {
        switch (state) {
            case STATE_DRAGGING:
                Log.d("onTimeUpdate", "STATE_DRAGGING :" + TimeUtils.getTestTime(time));
                break;
            case STATE_ADSORB:
                Log.d("onTimeUpdate", "STATE_ADSORB :" + TimeUtils.getTestTime(time));
                break;
            case STATE_FINISH:
                Log.d("onTimeUpdate", "STATE_FINISH :" + TimeUtils.getTestTime(time));
                if (presenterRef != null && presenterRef.get() != null)
                    presenterRef.get().startPlayHistory(time);
                break;
        }
    }

    @Override
    public void onBack() {
        if (activityWeakReference != null && activityWeakReference.get() != null)
            ViewUtils.setRequestedOrientation(activityWeakReference.get(),
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onSwitchSpeaker() {
        if (presenterRef != null && presenterRef.get() != null)
            presenterRef.get().switchSpeakerMic(false, false, false);
    }

    @Override
    public void onTriggerRecorder() {
        if (presenterRef != null && presenterRef.get() != null)
            presenterRef.get().switchSpeakerMic(false, false, false);
    }

    @Override
    public void onTriggerCapture() {
        if (presenterRef != null && presenterRef.get() != null)
            presenterRef.get().takeSnapShot();
    }

    @OnClick({R.id.imgV_cam_live_land_play,
            R.id.tv_cam_live_port_live})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cam_live_port_live:
                break;
            case R.id.imgV_cam_live_land_play:
                if (presenterRef != null && presenterRef.get() != null) {
                    if (presenterRef.get().getPlayType() == CamLiveContract.TYPE_LIVE) {
                        presenterRef.get().startPlayVideo(presenterRef.get().getPlayType());
                        AppLogger.i(String.format("land play history: %s", "live"));
                    } else {
                        long time = swCamLiveWheel.getCurrentFocusTime();
                        presenterRef.get().startPlayHistory(time);
                        AppLogger.i(String.format("land play history: %s", time));
                    }
                }
                break;
        }
        AppLogger.i(String.format("onClick land play: %s", (presenterRef != null && presenterRef.get() != null)));
    }

    @Override
    public void onClick(FlipImageView view) {
        if (presenterRef != null && presenterRef.get() != null)
            presenterRef.get().saveAlarmFlag(!view.isFlipped());
    }

    @Override
    public void onFlipStart(FlipImageView view) {

    }

    @Override
    public void onFlipEnd(FlipImageView view) {

    }

    @Override
    public void onLiveState(int state) {
        switch (state) {
            case PLAY_STATE_IDLE:
                if (imgVCamLiveLandPlay.getDrawable()
                        != getResources().getDrawable(R.drawable.icon_landscape_stop)) {
                    //do work here
                    imgVCamLiveLandPlay.setImageResource(R.drawable.icon_landscape_stop);
                }
                updateVisibilityState(false);
                break;
            case PLAY_STATE_PREPARE:
            case PLAY_STATE_PLAYING:
                if (imgVCamLiveLandPlay.getDrawable()
                        != getResources().getDrawable(R.drawable.icon_landscape_playing)) {
                    //do work here
                    imgVCamLiveLandPlay.setImageResource(R.drawable.icon_landscape_playing);
                }
                break;
        }
    }
}
