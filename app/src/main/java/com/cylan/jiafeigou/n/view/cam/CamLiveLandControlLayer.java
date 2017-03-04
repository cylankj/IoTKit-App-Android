package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.cylan.jiafeigou.n.view.adapter.CamLandHistoryDateAdapter;
import com.cylan.jiafeigou.utils.DensityUtils;
import com.cylan.jiafeigou.widget.LiveTimeLayout;
import com.cylan.jiafeigou.widget.flip.FlipImageView;
import com.cylan.jiafeigou.widget.flip.FlipLayout;
import com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt;

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
public class CamLiveLandControlLayer extends FrameLayout {
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
    FlipLayout flipLandLayout;
    @BindView(R.id.live_time_layout)
    LiveTimeLayout liveTimeLayout;
    @BindView(R.id.fLayout_cam_live_land_top_bar)
    CamLiveLandTopBar fLayoutCamLiveLandTopBar;
    @BindView(R.id.fLayout_cam_live_land_bottom_bar)
    LinearLayout fLayoutCamLiveLandBottomBar;
    @BindView(R.id.rv_land_date_list)
    RecyclerView rvLandDateList;
    @BindView(R.id.fLayout_land_date_container)
    FrameLayout fLayoutLandDateContainer;
    @BindView(R.id.rLayout_cam_live_land_bottom_second)
    View layout;
    @BindView(R.id.tv_cam_live_land_bottom)
    View vLiveRect;

    public CamLiveLandControlLayer(Context context) {
        this(context, null);
    }

    public CamLiveLandControlLayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CamLiveLandControlLayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_cam_live_control_layer, this, true);
        ButterKnife.bind(view);

    }

    public RecyclerView getRvLandDateList() {
        return rvLandDateList;
    }

    public FrameLayout getLandDateContainer() {
        return fLayoutLandDateContainer;
    }

    public void setDateAdapter(CamLandHistoryDateAdapter adapter) {
        if (rvLandDateList.getLayoutManager() == null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            rvLandDateList.setLayoutManager(layoutManager);
        }
        rvLandDateList.setAdapter(adapter);
    }

    public LinearLayout getLiveLandBottomBar() {
        return fLayoutCamLiveLandBottomBar;
    }

    public void setLiveRectClickListener(OnClickListener clickListener) {
        if (clickListener != null)
            liveTimeLayout.setOnClickListener(clickListener);
    }

    public void setLivePlayBtnClickListener(OnClickListener clickListener) {
        if (clickListener != null)
            imgVCamLiveLandPlay.setOnClickListener(clickListener);
    }

    public SuperWheelExt getSwCamLiveWheel() {
        return swCamLiveWheel;
    }

    public TextView getTvCamLivePortLive() {
        return tvCamLivePortLive;
    }

    public CamLiveLandTopBar getCamLiveLandTopBar() {
        return fLayoutCamLiveLandTopBar;
    }

    public ImageView getCamLandImgSpeaker() {
        return fLayoutCamLiveLandTopBar.getImgVCamSwitchSpeaker();
    }

    public ImageView getCamLandImgMic() {
        return fLayoutCamLiveLandTopBar.getImgVCamTriggerMic();
    }

    public ImageView getImgVCamLiveLandPlay() {
        return imgVCamLiveLandPlay;
    }

    public void setOrientation(int bit, int orientation, boolean isShareDevice, boolean hasSdcard, boolean safe) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) setVisibility(VISIBLE);
        imgVCamLiveLandPlay.setVisibility(orientation == Configuration.ORIENTATION_LANDSCAPE ? VISIBLE : GONE);
        fLayoutCamLiveLandTopBar.setVisibility(orientation == Configuration.ORIENTATION_LANDSCAPE ? VISIBLE : GONE);
        fLayoutCamLiveLandBottomBar.setVisibility(orientation == Configuration.ORIENTATION_LANDSCAPE ? VISIBLE : GONE);
        rvLandDateList.setVisibility(orientation == Configuration.ORIENTATION_LANDSCAPE ? VISIBLE : GONE);
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
        //直播|安全防护
        layout.setVisibility(isShareDevice ? GONE : VISIBLE);
        liveTimeLayout.setVisibility(isShareDevice ? GONE : VISIBLE);
        vLiveRect.setVisibility(!isShareDevice && hasSdcard ? VISIBLE : GONE);
        flipLandLayout.setVisibility(!isShareDevice && hasSdcard ? VISIBLE : GONE);
        flipLandLayout.setFlipped(!safe);
    }

    public void updateLiveButton(boolean show) {
        vLiveRect.setVisibility(show ? VISIBLE : GONE);
    }

    public void setTopBarAction(CamLiveLandTopBar.TopBarAction topBarAction) {
        fLayoutCamLiveLandTopBar.setTopBarAction(topBarAction);
    }

    public void setLandSafeClickListener(FlipImageView.OnFlipListener flipListener) {
        if (flipListener != null)
            flipLandLayout.setFlipListener(flipListener);
    }

    public void setLandSafe(boolean safe) {
        flipLandLayout.setFlipped(safe);
    }

    public void showHistoryWheel(boolean port) {
        if (port) {
            liveTimeLayout.setVisibility(GONE);
            flipLandLayout.setVisibility(GONE);
            fLayoutCamLiveLandBottomBar.setVisibility(VISIBLE);
            fLayoutCamLiveLandBottomBar.setTop(0);
            fLayoutCamLiveLandBottomBar.setTranslationY(0);
        }
    }
}
