package com.cylan.jiafeigou.n.view.cam;


import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.view.misc.LandLiveBarAnimDelegate;
import com.cylan.jiafeigou.n.view.misc.LiveBottomBarAnimDelegate;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.wheel.SDataStack;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraLiveFragment extends Fragment implements CamLandLiveAction,
        CamLiveContract.View {


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

    WeakReference<View> fLayoutLandScapeViewHolderRef;
    @BindView(R.id.sw_cam_port_wheel)
    CamLivePortWheel swCamPortWheel;
    private WeakReference<LiveBottomBarAnimDelegate> liveBottomBarAnimDelegateWeakReference;
    private WeakReference<CamLandLiveLayerInterface> landLiveLayerViewActionWeakReference;
    private CamLandLiveLayerInterface camLandLiveLayerInterface;


    private CamLiveContract.Presenter presenter;

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
    public void onPause() {
        super.onPause();
        if (camLandLiveLayerInterface != null)
            camLandLiveLayerInterface.destroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        swCamPortWheel.postDelayed(new Runnable() {
            @Override
            public void run() {
                presenter.fetchHistoryData();
            }
        }, 3000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null)
            presenter.stop();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 加入横屏要处理的代码
            fLayoutLiveBottomHandleBar.setVisibility(View.GONE);
            fLayoutCamLiveMenu.setVisibility(View.GONE);
            ViewUtils.updateViewMatchScreenHeight(fLayoutCamLiveView);
            showLandLayerView(true);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 加入竖屏要处理的代码
            fLayoutCamLiveMenu.setVisibility(View.VISIBLE);
            fLayoutLiveBottomHandleBar.setVisibility(View.VISIBLE);
            ViewUtils.updateViewHeight(fLayoutCamLiveView, 0.75f);
            showLandLayerView(false);
        }
    }

    private void showLandLayerView(boolean show) {
        if (show) {
            initLandView();
            if (fLayoutLandScapeViewHolderRef != null && fLayoutLandScapeViewHolderRef.get() != null)
                fLayoutLandScapeViewHolderRef.get().setVisibility(View.VISIBLE);
        } else {
            if (fLayoutLandScapeViewHolderRef != null && fLayoutLandScapeViewHolderRef.get() != null)
                fLayoutLandScapeViewHolderRef.get().setVisibility(View.GONE);
        }
        if (show) {
            initLandLiveLayerViewAction();
        }
    }

    private void initLandLiveLayerViewAction() {
        final long time = System.currentTimeMillis();
        if (landLiveLayerViewActionWeakReference == null
                || landLiveLayerViewActionWeakReference.get() == null) {
            camLandLiveLayerInterface = new LandLiveLayerViewAction(fLayoutLandScapeViewHolderRef.get(), new CamLandLiveLayerViewBundle());
            landLiveLayerViewActionWeakReference =
                    new WeakReference<>(camLandLiveLayerInterface);
            landLiveLayerViewActionWeakReference.get().setCamLandLiveAction(this);
        }
        Log.d("performance", "initLandLiveLayerViewAction performance: " + (System.currentTimeMillis() - time));
    }

    /**
     * 初始化 Layer层view，横屏全屏时候，需要在上层
     */
    private void initLandView() {
        if (fLayoutLandScapeViewHolderRef == null || fLayoutLandScapeViewHolderRef.get() == null) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_camera_live_land_top_layer, null);
            if (view != null) {
                fLayoutLandScapeViewHolderRef = new WeakReference<>(view);
            }
        }
        View view = fLayoutLiveViewContainer.findViewById(R.id.fLayout_cam_live_land_layer);
        if (view == null) {
            fLayoutLiveViewContainer.addView(fLayoutLandScapeViewHolderRef.get());
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
                ViewUtils.setRequestedOrientation(getActivity(), ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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

    @Override
    public void onLive() {

    }

    @Override
    public void onLandPlay(int state) {
        Toast.makeText(getContext(), "play: ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLandBack() {
        Toast.makeText(getContext(), "onBack: ", Toast.LENGTH_SHORT).show();
        ViewUtils.setRequestedOrientation(getActivity(), ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onLandSwitchSpeaker(int state) {
        Toast.makeText(getContext(), "onSwitchSpeaker: ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLandSwitchRecorder(int state) {
        Toast.makeText(getContext(), "onSwitchRecorder: ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLandCapture() {
        Toast.makeText(getContext(), "onCapture: ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onHistoryDataRsp(SDataStack dataStack) {
        swCamPortWheel.loading(false);
        swCamPortWheel.setupHistoryData(dataStack);
        setCamLandLiveHistory(dataStack);
    }

    private void setCamLandLiveHistory(SDataStack dataStack) {
        if (fLayoutLandScapeViewHolderRef == null)
            return;
        landLiveLayerViewActionWeakReference.get().setupHistoryTimeSet(dataStack);
    }

    @Override
    public void setPresenter(CamLiveContract.Presenter presenter) {
        this.presenter = presenter;
    }


    private static class LandLiveLayerViewAction implements CamLandLiveLayerInterface,
            CamLiveLandWheel.WheelUpdateListener,
            CamLiveLandTopBar.TopBarAction {
        WeakReference<View> viewWeakReference;
        CamLandLiveLayerViewBundle bundle;
        ImageView imgvPlay;
        LandLiveBarAnimDelegate landLiveBarAnimDelegate;
        CamLandLiveAction camLandLiveAction;
        CamLiveLandWheel camLiveLandWheel;
        CamLiveLandTopBar camLiveLandTopBar;

        /**
         * updateBundle
         *
         * @param bundle
         */
        public void setBundle(CamLandLiveLayerViewBundle bundle) {
            this.bundle = bundle;
            setup();
        }

        public void setCamLandLiveAction(CamLandLiveAction camLandLiveAction) {
            this.camLandLiveAction = camLandLiveAction;
        }

        private static final int[] playId = {R.drawable.icon_landscape_play,
                R.drawable.icon_landscape_pause};

        public LandLiveLayerViewAction(View view,
                                       CamLandLiveLayerViewBundle bundle) {
            this.viewWeakReference = new WeakReference<>(view);
            this.bundle = bundle;
            setup();
            landLiveBarAnimDelegate = new LandLiveBarAnimDelegate(view.findViewById(R.id.fLayout_cam_live_land_top_bar),
                    view.findViewById(R.id.rLayout_cam_live_land_bottom_bar));
            landLiveBarAnimDelegate.startAnimation(true);
        }

        private void setup() {
            viewWeakReference.get().findViewById(R.id.fLayout_cam_live_land_layer)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            landLiveBarAnimDelegate.startAnimation(false);
                        }
                    });
            imgvPlay = (ImageView) viewWeakReference.get().findViewById(R.id.imgV_cam_live_land_play);
            imgvPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            camLiveLandWheel = (CamLiveLandWheel) viewWeakReference.get().findViewById(R.id.cv_cam_land_live_wheel);
            camLiveLandWheel.setWheelUpdateListener(this);
            camLiveLandTopBar = (CamLiveLandTopBar) viewWeakReference.get().findViewById(R.id.fLayout_cam_live_land_top_bar);
            camLiveLandTopBar.setTopBarAction(this);
        }


        @Override
        public void updateLandPlayBtn(int state) {
            if (imgvPlay != null && (state == 0 || state == 1)) {
                imgvPlay.setImageResource(playId[state]);
            }
        }

        @Override
        public void setupHistoryTimeSet(SDataStack dataStack) {
            if (camLiveLandWheel != null)
                camLiveLandWheel.setupHistoryData(dataStack);
        }


        @Override
        public void destroy() {
            if (landLiveBarAnimDelegate != null)
                landLiveBarAnimDelegate.destroy();
        }

        @Override
        public void onSettleFinish() {
        }

        @Override
        public void onBack() {
            if (camLandLiveAction != null) camLandLiveAction.onLandBack();
        }

        @Override
        public void onSwitchSpeaker() {
            if (camLandLiveAction != null) camLandLiveAction.onLandSwitchSpeaker(0);
        }

        @Override
        public void onTriggerRecorder() {
            if (camLandLiveAction != null) camLandLiveAction.onLandSwitchRecorder(0);
        }

        @Override
        public void onTriggerCapture() {
            if (camLandLiveAction != null) camLandLiveAction.onLandCapture();
        }
    }


    public static class CamLandLiveLayerViewBundle {

        public String title;
        /**
         * -1： disable  1:open speaker  0: close speaker
         */
        public int speakerState;
        /**
         * -1： disable  1:open recorder 0: close recorder
         */
        public int recorderState;
        /**
         * -1： disable  1:enable
         */
        public int captureState;
    }
}


interface CamLandLiveLayerInterface {
    /**
     * 0：pause 1:playing
     *
     * @param state
     */
    void updateLandPlayBtn(int state);

    void setCamLandLiveAction(CamLandLiveAction action);

    void setupHistoryTimeSet(SDataStack timeSet);

    void destroy();
}

interface CamLandLiveAction {

    void onLive();


    void onLandPlay(int state);

    void onLandBack();

    void onLandSwitchSpeaker(int state);

    void onLandSwitchRecorder(int state);

    void onLandCapture();

}