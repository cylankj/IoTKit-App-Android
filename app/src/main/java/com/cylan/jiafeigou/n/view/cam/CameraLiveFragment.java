package com.cylan.jiafeigou.n.view.cam;


import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.view.misc.LiveBottomBarAnimDelegate;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraLiveFragment extends Fragment {


    @BindView(R.id.fLayout_live_view_container)
    FrameLayout fLayoutLiveViewContainer;
    @BindView(R.id.vs_progress)
    ViewStub vsProgress;
    @BindView(R.id.fLayout_cam_live_protection_flip)
    FrameLayout fLayoutCamLiveProtectionFlip;
    @BindView(R.id.tv_cam_live_protection)
    TextView tvCamLiveProtection;
    @BindView(R.id.tv_cam_live)
    TextView tvCamLive;
    @BindView(R.id.imgV_cam_switch_speaker)
    ImageView imgVCamSwitchSpeaker;
    @BindView(R.id.imgV_cam_trigger_recorder)
    ImageView imgVCamTriggerRecorder;
    @BindView(R.id.imgV_cam_trigger_capture)
    ImageView imgVCamTriggerCapture;
    @BindView(R.id.imgV_cam_zoom_to_full_screen)
    ImageView imgVCamZoomToFullScreen;
    @BindView(R.id.tv_cam_show_timeline)
    TextView tvCamShowTimeline;
    @BindView(R.id.fLayout_cam_live_view)
    FrameLayout fLayoutCamLiveView;
    @BindView(R.id.fLayout_live_port_bottom_handle_bar)
    FrameLayout fLayoutLiveBottomHandleBar;
    @BindView(R.id.fLayout_cam_live_menu)
    FrameLayout fLayoutCamLiveMenu;

    private WeakReference<LiveBottomBarAnimDelegate> liveBottomBarAnimDelegateWeakReference;



    public CameraLiveFragment() {
        // Required empty public constructor
    }

    public static CameraLiveFragment newInstance(Bundle bundle) {
        CameraLiveFragment fragment = new CameraLiveFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera_live, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.updateViewHeight(fLayoutCamLiveView, 0.75f);
        animateBottomBar(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 加入横屏要处理的代码
            fLayoutLiveBottomHandleBar.setVisibility(View.GONE);
            fLayoutCamLiveMenu.setVisibility(View.GONE);
            ViewUtils.updateViewMatchScreenHeight(fLayoutCamLiveView);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 加入竖屏要处理的代码
            fLayoutCamLiveMenu.setVisibility(View.VISIBLE);
            fLayoutLiveBottomHandleBar.setVisibility(View.VISIBLE);
            ViewUtils.updateViewHeight(fLayoutCamLiveView, 0.75f);
        }
    }

    @OnClick({R.id.imgV_cam_switch_speaker,
            R.id.imgV_cam_trigger_recorder,
            R.id.imgV_cam_trigger_capture,
            R.id.imgV_cam_zoom_to_full_screen,
            R.id.tv_cam_show_timeline,
            R.id.fLayout_cam_live_view})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgV_cam_switch_speaker:
                break;
            case R.id.imgV_cam_trigger_recorder:
                break;
            case R.id.imgV_cam_trigger_capture:
                break;
            case R.id.imgV_cam_zoom_to_full_screen:
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case R.id.tv_cam_show_timeline:
                break;
            case R.id.fLayout_cam_live_view:
                animateBottomBar(false);
                break;
        }
    }



    /**
     * 检查
     */
    private void checkBottomAnimation() {
        if (liveBottomBarAnimDelegateWeakReference == null || liveBottomBarAnimDelegateWeakReference.get() == null)
            liveBottomBarAnimDelegateWeakReference = new WeakReference<>(new LiveBottomBarAnimDelegate(fLayoutLiveBottomHandleBar));
    }

    private void animateBottomBar(boolean auto) {
        checkBottomAnimation();
        liveBottomBarAnimDelegateWeakReference.get()
                .startAnimation(auto);
    }

}
