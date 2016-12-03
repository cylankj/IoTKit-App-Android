package com.cylan.jiafeigou.n.view.cam;


import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
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

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamLivePresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.view.misc.LandLiveBarAnimDelegate;
import com.cylan.jiafeigou.n.view.misc.LiveBottomBarAnimDelegate;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;
import com.cylan.jiafeigou.widget.wheel.SDataStack;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraLiveFragment extends IBaseFragment<CamLiveContract.Presenter>
        implements CamLandLiveAction, CamLiveContract.View {


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
    @BindView(R.id.fLayout_live_port_bottom_control_bar)
    FrameLayout fLayoutLiveBottomHandleBar;
    @BindView(R.id.fLayout_cam_live_menu)
    FrameLayout fLayoutCamLiveMenu;

    WeakReference<View> fLayoutLandScapeControlLayerRef;
    @BindView(R.id.sw_cam_port_wheel)
    CamLivePortWheel swCamPortWheel;
    private WeakReference<LiveBottomBarAnimDelegate> liveBottomBarAnimDelegateWeakReference;
    private WeakReference<CamLandLiveLayerInterface> landLiveLayerViewActionWeakReference;
    private CamLandLiveLayerInterface camLandLiveLayerInterface;

    private VideoViewFactory.IVideoView videoView;

    public CameraLiveFragment() {
        // Required empty public constructor
    }

    public static CameraLiveFragment newInstance(Bundle bundle) {
        CameraLiveFragment fragment = new CameraLiveFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        basePresenter = new CamLivePresenterImpl(this, (DeviceBean) getArguments().getParcelable(JConstant.KEY_DEVICE_ITEM_BUNDLE));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        if (basePresenter != null)
            basePresenter.stopPlayVideo();
        if (camLandLiveLayerInterface != null)
            camLandLiveLayerInterface.destroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (basePresenter != null) {
            basePresenter.fetchCamInfo(basePresenter.getCamInfo().deviceBase.uuid);
            basePresenter.fetchHistoryData();
            basePresenter.startPlayVideo();
            showLoading(basePresenter.getCamInfo().net != null
                    && basePresenter.getCamInfo().net.net != 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        showLoading(false);
    }

    private void showLoading(boolean show) {
        if (vsProgress.getInflatedId() == View.NO_ID) {
            vsProgress.setInflatedId("vsProgress".hashCode());
            vsProgress.inflate();
        }
        vsProgress.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
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

    /**
     * 初始化videoView
     *
     * @return
     */
    private VideoViewFactory.IVideoView initVideoView() {
        if (videoView == null) {
            int pid = basePresenter.getCamInfo().deviceBase.pid;
            videoView = VideoViewFactory.CreateRendererExt(JFGRules.isNeedPanoramicView(pid),
                    getContext(), true);
            ((View) videoView).setId("IVideoView".hashCode());

        }
        return videoView;
    }

    private void updateVideoViewLayoutParameters(JFGMsgVideoResolution resolution) {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                Resources.getSystem().getDisplayMetrics().widthPixels);
        View view = fLayoutLiveViewContainer.findViewById("IVideoView".hashCode());
        if (view == null) {
            fLayoutLiveViewContainer.addView((View) videoView, 0, lp);
        } else {
            view.setLayoutParams(lp);
        }
    }

    /**
     * 显示全屏
     *
     * @param show
     */
    private void showLandLayerView(boolean show) {
        if (show) {
            initLandControlLayer();
            if (fLayoutLandScapeControlLayerRef != null && fLayoutLandScapeControlLayerRef.get() != null)
                fLayoutLandScapeControlLayerRef.get().setVisibility(View.VISIBLE);
        } else {
            if (fLayoutLandScapeControlLayerRef != null && fLayoutLandScapeControlLayerRef.get() != null)
                fLayoutLandScapeControlLayerRef.get().setVisibility(View.GONE);
        }
        if (show) {
            initLandLiveLayerViewAction();
        }
    }

    private void initLandLiveLayerViewAction() {
        final long time = System.currentTimeMillis();
        if (landLiveLayerViewActionWeakReference == null
                || landLiveLayerViewActionWeakReference.get() == null) {
            camLandLiveLayerInterface = new LandLiveLayerViewAction(fLayoutLandScapeControlLayerRef.get(), new CamLandLiveLayerViewBundle());
            landLiveLayerViewActionWeakReference =
                    new WeakReference<>(camLandLiveLayerInterface);
            landLiveLayerViewActionWeakReference.get().setCamLandLiveAction(this);
        }
        Log.d("performance", "initLandLiveLayerViewAction performance: " + (System.currentTimeMillis() - time));
    }

    /**
     * 初始化 Layer层view，横屏全屏时候，需要在上层
     */
    private void initLandControlLayer() {
        if (fLayoutLandScapeControlLayerRef == null || fLayoutLandScapeControlLayerRef.get() == null) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_camera_live_land_control_layer, null);
            if (view != null) {
                fLayoutLandScapeControlLayerRef = new WeakReference<>(view);
            }
        }
        View view = fLayoutLiveViewContainer.findViewById(R.id.fLayout_cam_live_land_control_layer);
        if (view == null) {
            fLayoutLiveViewContainer.addView(fLayoutLandScapeControlLayerRef.get());
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
        swCamPortWheel.setupHistoryData(dataStack);
        setCamLandLiveHistory(dataStack);
    }

    @Override
    public void onFailed(int id) {
        showLoading(false);
        switch (id) {
            case JFGRules.PlayErr.ERR_NERWORK:
                ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR_1));
                break;
            case JFGRules.PlayErr.ERR_UNKOWN:
                ToastUtil.showNegativeToast("出错了");
                break;
            case JFGRules.PlayErr.ERR_LOW_FRAME_RATE:
                ToastUtil.showNegativeToast("帧率太低,不足以播放,重试");
                break;
            case JFGRules.PlayErr.ERR_DEVICE_OFFLINE:
                ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR));
                break;
        }
    }

    @Override
    public void onRtcp(JFGMsgVideoRtcp rtcp) {

    }

    @Override
    public void onResolution(JFGMsgVideoResolution resolution) {
        showLoading(false);
        JfgCmdInsurance.getCmd().setRenderRemoteView((View) initVideoView());
        updateVideoViewLayoutParameters(resolution);
    }

    @Override
    public void onDeviceStandBy(boolean state) {
        //进入待机模式
        if (basePresenter == null) {
            AppLogger.e("basePresenter is null");
            return;
        }
        if (state) {
            basePresenter.stopPlayVideo();
            showLoading(false);
            if (basePresenter.getPlayState() != 0) {
                //处于非播放状态

            }
        } else {
            if (getActivity().getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                ViewUtils.setRequestedOrientation(getActivity(), ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    }

    private void setCamLandLiveHistory(SDataStack dataStack) {
        if (fLayoutLandScapeControlLayerRef == null)
            return;
        landLiveLayerViewActionWeakReference.get().setupHistoryTimeSet(dataStack);
    }

    @Override
    public void setPresenter(CamLiveContract.Presenter basePresenter) {
        this.basePresenter = basePresenter;
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
            viewWeakReference.get().findViewById(R.id.fLayout_cam_live_land_control_layer)
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