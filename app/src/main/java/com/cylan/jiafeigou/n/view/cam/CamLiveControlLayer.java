package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.widget.LiveTimeLayout;
import com.cylan.jiafeigou.widget.flip.FlipLayout;
import com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt;
import com.cylan.utils.DensityUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.widget.RelativeLayout.BELOW;

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
public class CamLiveControlLayer extends FrameLayout {
    /**
     * 显示状态:0:隐藏,1:显示,2:淡入,3:淡出,4:slide_out_up,5:slide_out_down
     */
    @BindView(R.id.lLayout_cam_history_wheel)
    LinearLayout lLayoutCamHistoryWheel;
    @BindView(R.id.sw_cam_live_wheel)
    SuperWheelExt swCamLiveWheel;
    @BindView(R.id.tv_cam_live_port_live)
    TextView tvCamLivePortLive;
    @BindView(R.id.imgV_cam_live_land_play)
    ImageView imgVCamLiveLandPlay;
    @BindView(R.id.lLayout_protection)
    FlipLayout lLayoutProtection;
    @BindView(R.id.live_time_layout)
    LiveTimeLayout liveTimeLayout;
    @BindView(R.id.fLayout_cam_live_land_top_bar)
    CamLiveLandTopBar fLayoutCamLiveLandTopBar;
    @BindView(R.id.fLayout_cam_live_land_bottom_bar)
    LinearLayout fLayoutCamLiveLandBottomBar;

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

    }

    public LinearLayout getLiveLandBottomBar() {
        return fLayoutCamLiveLandBottomBar;
    }

    public LiveTimeLayout getLiveTimeLayout() {
        return liveTimeLayout;
    }

    public LinearLayout getlLayoutCamHistoryWheel() {
        return lLayoutCamHistoryWheel;
    }

    public SuperWheelExt getSwCamLiveWheel() {
        return swCamLiveWheel;
    }

    public TextView getTvCamLivePortLive() {
        return tvCamLivePortLive;
    }

    public CamLiveLandTopBar getfLayoutCamLiveLandTopBar() {
        return fLayoutCamLiveLandTopBar;
    }

    public ImageView getImgVCamLiveLandPlay() {
        return imgVCamLiveLandPlay;
    }

    public FlipLayout getFlipLayout() {
        return lLayoutProtection;
    }

    public void setOrientation(int orientation) {
        post(new Runnable() {
            @Override
            public void run() {
                setVisibility(VISIBLE);
                fLayoutCamLiveLandTopBar.setVisibility(orientation == Configuration.ORIENTATION_LANDSCAPE ? VISIBLE : GONE);
                findViewById(R.id.imgV_cam_live_land_play).setVisibility(orientation == Configuration.ORIENTATION_LANDSCAPE ? VISIBLE : GONE);
                findViewById(R.id.v_divider).setVisibility(orientation == Configuration.ORIENTATION_LANDSCAPE ? VISIBLE : GONE);
                findViewById(R.id.lLayout_protection).setVisibility(orientation == Configuration.ORIENTATION_LANDSCAPE ? VISIBLE : GONE);
                liveTimeLayout.setVisibility(orientation == Configuration.ORIENTATION_LANDSCAPE ? VISIBLE : GONE);
                fLayoutCamLiveLandTopBar.setVisibility(orientation == Configuration.ORIENTATION_LANDSCAPE ? VISIBLE : GONE);
                fLayoutCamLiveLandBottomBar.setVisibility(orientation == Configuration.ORIENTATION_LANDSCAPE ? VISIBLE : GONE);
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

    public void setTopBarAction(CamLiveLandTopBar.TopBarAction topBarAction) {
        fLayoutCamLiveLandTopBar.setTopBarAction(topBarAction);
    }
}
