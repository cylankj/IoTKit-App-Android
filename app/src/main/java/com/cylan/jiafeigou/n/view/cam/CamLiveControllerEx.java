package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.widget.flip.FlipImageView;
import com.cylan.jiafeigou.widget.flip.FlipLayout;
import com.cylan.jiafeigou.widget.live.ILiveControl;
import com.cylan.jiafeigou.widget.live.LivePlayControlView;
import com.cylan.jiafeigou.widget.wheel.ex.IData;

import static com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract.TYPE_HISTORY;

/**
 * Created by hds on 17-4-19.
 */

public class CamLiveControllerEx extends RelativeLayout implements ICamLiveLayer,
        View.OnClickListener, FlipImageView.OnFlipListener,
        ILiveControl.Action {

    private static final String TAG = "CamLiveControllerEx";

    //横屏 top bar
    private View layoutA;
    //流量
    private View layoutB;
    //loading
    private View layoutC;
    //防护  |直播|时间|   |全屏|
    private View layoutD;
    //历史录像条
    private View layoutE;
    //|speaker|mic|capture|
    private View layoutF;
    //横屏 侧滑日历
    private View layoutG;

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
        layoutC = findViewById(R.id.layout_c);
        layoutD = findViewById(R.id.layout_d);
        layoutE = findViewById(R.id.layout_e);
        layoutF = findViewById(R.id.layout_f);
        layoutG = findViewById(R.id.layout_g);
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
        ((LivePlayControlView) layoutC).setAction(this);
        //d.time
        ((FlipLayout) layoutD.findViewById(R.id.layout_port_flip))
                .setFlipListener(this);
        layoutD.findViewById(R.id.live_time_layout).setOnClickListener(this);
        layoutD.findViewById(R.id.imgV_cam_zoom_to_full_screen)
                .setOnClickListener(this);
        //e.
        layoutE.findViewById(R.id.imgV_cam_live_land_play).setOnClickListener(this);
        layoutE.findViewById(R.id.tv_live).setOnClickListener(this);
        ((FlipLayout) layoutE.findViewById(R.id.layout_land_flip)).setFlipListener(this);
        //f
        layoutF.findViewById(R.id.imgV_cam_switch_speaker).setOnClickListener(this);
        layoutF.findViewById(R.id.imgV_cam_trigger_mic).setOnClickListener(this);
        layoutF.findViewById(R.id.imgV_cam_trigger_capture).setOnClickListener(this);
//        PerformanceUtils.stopTrace("initListener");
    }

    @Override
    public void onLivePrepared() {

    }

    private boolean isLand() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    @Override
    public void onLiveStart(CamLiveContract.Presenter presenter, Device device) {
        boolean isPlayHistory = presenter.getPlayState() == TYPE_HISTORY;
        //左下角直播
        ((ImageView) layoutE.findViewById(R.id.imgV_cam_live_land_play))
                .setImageResource(R.drawable.icon_landscape_stop);
        //|直播| 按钮
        layoutE.findViewById(R.id.tv_live).setEnabled(isPlayHistory);
        layoutF.findViewById(R.id.imgV_cam_switch_speaker).setEnabled(true);
        layoutF.findViewById(R.id.imgV_cam_trigger_mic).setEnabled(true);
        layoutF.findViewById(R.id.imgV_cam_trigger_capture).setEnabled(true);
        layoutF.findViewById(R.id.imgV_land_cam_switch_speaker).setEnabled(true);
        layoutF.findViewById(R.id.imgV_land_cam_trigger_mic).setEnabled(true);
        layoutF.findViewById(R.id.imgV_land_cam_trigger_capture).setEnabled(true);

    }

    @Override
    public void onLiveStop(CamLiveContract.Presenter presenter, Device device, int errCode) {
        layoutB.setVisibility(GONE);
        ((ImageView) layoutE.findViewById(R.id.imgV_cam_live_land_play))
                .setImageResource(R.drawable.icon_landscape_playing);
        layoutF.findViewById(R.id.imgV_cam_switch_speaker).setEnabled(false);
        layoutF.findViewById(R.id.imgV_cam_trigger_mic).setEnabled(false);
        layoutF.findViewById(R.id.imgV_cam_trigger_capture).setEnabled(false);
        layoutF.findViewById(R.id.imgV_land_cam_switch_speaker).setEnabled(false);
        layoutF.findViewById(R.id.imgV_land_cam_trigger_mic).setEnabled(false);
        layoutF.findViewById(R.id.imgV_land_cam_trigger_capture).setEnabled(false);

    }

    @Override
    public void orientationChanged(CamLiveContract.Presenter presenter, Device device, int orientation) {
        boolean isLand = isLand();
        layoutA.setVisibility(isLand ? VISIBLE : GONE);
//        layoutD.findViewById(R.id.layout_port_flip).setVisibility();
        layoutF.setVisibility(isLand ? GONE : VISIBLE);
        layoutE.findViewById(R.id.imgV_cam_live_land_play).setVisibility(isLand ? VISIBLE : GONE);
        layoutE.findViewById(R.id.layout_land_flip).setVisibility(isLand ? VISIBLE : GONE);
        layoutE.findViewById(R.id.v_divider).setVisibility(isLand ? VISIBLE : GONE);
    }

    @Override
    public void onRtcpCallback(JFGMsgVideoRtcp rtcp) {

    }

    @Override
    public void onHistoryDataRsp(IData data) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.imgV_cam_live_land_nav_back:
                break;
            case R.id.imgV_cam_zoom_to_full_screen://点击全屏
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

    @Override
    public void onClick(FlipImageView view) {

    }

    @Override
    public void onFlipStart(FlipImageView view) {

    }

    @Override
    public void onFlipEnd(FlipImageView view) {

    }

    @Override
    public void clickImage(int state) {

    }

    @Override
    public void clickText() {

    }

    @Override
    public void clickHelp() {

    }
}
