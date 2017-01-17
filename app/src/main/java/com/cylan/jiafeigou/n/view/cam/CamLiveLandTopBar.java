package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamLiveLandTopBar extends FrameLayout {

    @BindView(R.id.imgV_cam_live_land_nav_back)
    TextView imgVCamLiveLandNavBack;
    @BindView(R.id.imgV_land_cam_switch_speaker)
    ImageView imgVCamSwitchSpeaker;
    @BindView(R.id.imgV_land_cam_trigger_mic)
    ImageView imgVCamTriggerRecorder;
    @BindView(R.id.imgV_land_cam_trigger_capture)
    ImageView imgVCamTriggerCapture;

    public CamLiveLandTopBar(Context context) {
        this(context, null);
    }

    public CamLiveLandTopBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CamLiveLandTopBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_cam_land_live_top_bar, this, true);
        ButterKnife.bind(view);
    }

    @OnClick({R.id.imgV_cam_live_land_nav_back,
            R.id.imgV_land_cam_switch_speaker,
            R.id.imgV_land_cam_trigger_mic,
            R.id.imgV_land_cam_trigger_capture})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgV_cam_live_land_nav_back:
                if (topBarAction != null)
                    topBarAction.onBack(view);
                break;
            case R.id.imgV_land_cam_switch_speaker:
                if (topBarAction != null)
                    topBarAction.onSwitchSpeaker(view);
                break;
            case R.id.imgV_land_cam_trigger_mic:
                if (topBarAction != null)
                    topBarAction.onTriggerMic(view);
                break;
            case R.id.imgV_land_cam_trigger_capture:
                if (topBarAction != null)
                    topBarAction.onTriggerCapture(view);
                break;
        }
    }

    private TopBarAction topBarAction;

    public void setTopBarAction(TopBarAction topBarAction) {
        this.topBarAction = topBarAction;
    }

    public interface TopBarAction {
        void onBack(View view);

        void onSwitchSpeaker(View view);

        void onTriggerMic(View view);

        void onTriggerCapture(View view);
    }
}
