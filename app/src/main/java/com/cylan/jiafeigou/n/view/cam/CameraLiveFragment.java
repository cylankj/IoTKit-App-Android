package com.cylan.jiafeigou.n.view.cam;


import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.listener.ILiveStateListener;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamLivePresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LiveTimeLayout;
import com.cylan.jiafeigou.widget.flip.FlipLayout;
import com.cylan.jiafeigou.widget.live.ILiveControl;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;
import com.cylan.jiafeigou.widget.wheel.ex.IData;
import com.cylan.utils.DensityUtils;
import com.cylan.utils.NetUtils;
import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.Locale;

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
    ViewStub vs_control;//中间loading那个view
    @BindView(R.id.imgV_cam_switch_speaker)
    ImageView imgVCamSwitchSpeaker;
    @BindView(R.id.imgV_cam_trigger_mic)
    ImageView imgVCamTriggerMic;
    @BindView(R.id.imgV_cam_trigger_capture)
    ImageView imgVCamTriggerCapture;
    @BindView(R.id.fLayout_cam_live_view)
    FrameLayout fLayoutCamLiveView;
    //
    // 安全防护   直播|5/16 12:30   全屏
    //
    @BindView(R.id.fLayout_live_port_bottom_control_bar)
    FrameLayout fLayoutLiveBottomHandleBar;
    //
    // 扬声器 mic 截图
    //
    @BindView(R.id.fLayout_cam_live_menu)
    FrameLayout fLayoutCamLiveMenu;

    @BindView(R.id.cam_live_control_layout)
    CamLiveControlLayer swCamLiveControlLayer;


    @BindView(R.id.lLayout_protection)
    FlipLayout portFlipLayout;
    @BindView(R.id.live_time_layout)
    LiveTimeLayout liveTimeLayout;
    @BindView(R.id.imgV_cam_zoom_to_full_screen)
    ImageView imgVCamZoomToFullScreen;

    private CamLiveController camLiveController;
    //    /**
//     * 直播状态监听
//     */
    private ILiveStateListener liveListener;
    /**
     * |安全防护|----直播|5/16 16:30|---|
     */
    private VideoViewFactory.IVideoView videoView;
    //流量显示
    private WeakReference<TextView> tvFlowRef;
    /**
     * 待机模式的view:"已进入待机模式,前往打开"
     */
    private WeakReference<View> viewStandbyRef;

    private String uuid;

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
        DeviceBean bean = getArguments().getParcelable(JConstant.KEY_DEVICE_ITEM_BUNDLE);
        this.uuid = bean.uuid;
        basePresenter = new CamLivePresenterImpl(this, bean.uuid);
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
        initBottomBtn(false);
        camLiveController = new CamLiveController(getContext());
        camLiveController.setPresenterRef(basePresenter);
        camLiveController.setLiveAction((ILiveControl) vs_control.inflate());
        camLiveController.setCamLiveControlLayer(swCamLiveControlLayer);
        camLiveController.setScreenZoomer(imgVCamZoomToFullScreen);
        camLiveController.setPortSafeSetter(portFlipLayout);
        camLiveController.setPortLiveTimeSetter(liveTimeLayout);
        camLiveController.setActivity(getActivity());
        liveListener = camLiveController.getLiveStateListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (basePresenter != null)
            basePresenter.stopPlayVideo(basePresenter.getPlayType());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (basePresenter != null) {
            basePresenter.fetchHistoryDataList();
            //非待机模式
            boolean flag = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_508_CAMERA_STANDBY_FLAG, false);
            if (!flag) {
                basePresenter.startPlayVideo(basePresenter.getPlayType());
            } else {
                onDeviceStandBy(true);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * 初始化三个按钮{扬声器,mic,截图}
     *
     * @param enable
     */
    private void initBottomBtn(final boolean enable) {
        imgVCamSwitchSpeaker.post(() -> {
            imgVCamSwitchSpeaker.setEnabled(enable);
            imgVCamTriggerMic.setEnabled(enable);
            imgVCamTriggerCapture.setEnabled(enable);
        });
    }


    /**
     * 根据 待机模式 ,分享用户模式设置一些view的状态
     */
    @Override
    public void onDeviceStandBy(boolean flag) {
        camLiveController.setLoadingState(ILiveControl.STATE_IDLE, null);
        showFloatFlowView(false, null);
        //进入待机模式
        View v = fLayoutLiveViewContainer.findViewById("showSceneView".hashCode());
        if (v == null && !flag) {
            return;
        }
        if (v == null) {
            if (viewStandbyRef == null || viewStandbyRef.get() == null) {
                long time = System.currentTimeMillis();
                v = LayoutInflater.from(getContext()).inflate(R.layout.layout_fragment_cam_live_standby, null, false);
                v.setId("showSceneView".hashCode());
                viewStandbyRef = new WeakReference<>(v);
                Log.d("showSceneView", "showSceneView: " + (System.currentTimeMillis() - time));
                fLayoutLiveViewContainer.addView(v);
            } else v = viewStandbyRef.get();
        }
        v.setVisibility(flag ? View.VISIBLE : View.GONE);
        AppLogger.i("onDeviceStandBy");
    }

    @Override
    public void onLivePrepare(int type) {
        camLiveController.setLoadingState(ILiveControl.STATE_LOADING, null);
        AppLogger.i("onLivePrepare");
        if (liveListener != null) liveListener.liveStateChange();
    }

    @Override
    public void onLiveStarted(int type) {
        camLiveController.setLoadingState(ILiveControl.STATE_PLAYING, null);
        AppLogger.i("onLiveStarted");
        if (getView() != null)
            getView().setKeepScreenOn(true);
        initBottomBtn(true);
        imgVCamSwitchSpeaker.performClick();
        imgVCamTriggerMic.performClick();
        camLiveController.setLiveType(basePresenter.getPlayType());
        if (liveListener != null) liveListener.liveStateChange();
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
//            ViewUtils.updateViewHeight(fLayoutCamLiveView, 0.75f);
        }
        camLiveController.notifyOrientationChange(this.getResources().getConfiguration().orientation);
        AppLogger.i("onConfigurationChanged");
        updateVideoViewLayoutParameters(null);
    }

    /**
     * 初始化流量
     */
    private void showFloatFlowView(boolean show, String content) {
        View v = fLayoutLiveViewContainer.findViewById("flow".hashCode());
        if (!show && v == null)
            return;
        if (!show && v.isShown()) {
            v.setVisibility(View.GONE);
            return;
        }
        if (show && v != null && !v.isShown()) {
            v.setVisibility(View.VISIBLE);
        }
        if (v == null) {
            if (tvFlowRef == null || tvFlowRef.get() == null) {
                TextView textView = new TextView(getContext());
                textView.setBackground(getResources().getDrawable(R.drawable.flow_bg));
                textView.setId("flow".hashCode());
                textView.setTextColor(getResources().getColor(R.color.color_white));
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(DensityUtils.dip2px(60),
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.END);
                textView.setGravity(Gravity.CENTER);
                lp.setMargins(10, 60, 10, 10);
                lp.setMarginEnd(10);
                fLayoutLiveViewContainer.addView(textView, lp);
                tvFlowRef = new WeakReference<>(textView);
            }
            v = tvFlowRef.get();
        }
        ((TextView) v).setText(content);
    }

    /**
     * 初始化videoView
     *
     * @return
     */
    private VideoViewFactory.IVideoView initVideoView() {
        AppLogger.i("initVideoView:" + (videoView == null));
        if (videoView == null) {
            JFGDevice device = GlobalDataProxy.getInstance().fetch(uuid);
            if (device == null) {
                AppLogger.e("device is null");
                getActivity().finish();
                return null;
            }
            int pid = device.pid;
            videoView = VideoViewFactory.CreateRendererExt(JFGRules.isNeedPanoramicView(pid),
                    getContext(), true);
            ((View) videoView).setId("IVideoView".hashCode());
            videoView.setInterActListener(new VideoViewFactory.InterActListener() {

                @Override
                public boolean onSingleTap(float x, float y) {
                    camLiveController.tapVideoViewAction();
                    return true;
                }

                @Override
                public void onSnapshot(Bitmap bitmap, boolean tag) {

                }
            });
        }
        return videoView;
    }

    /**
     * 第一次创建view,会导致全屏闪烁一下.
     *
     * @param resolution
     */
    private void updateVideoViewLayoutParameters(JFGMsgVideoResolution resolution) {
        if (resolution != null) fLayoutLiveViewContainer.setTag(resolution);
        if (resolution == null) {
            Object o = fLayoutLiveViewContainer.getTag();
            if (o != null && o instanceof JFGMsgVideoResolution) {
                resolution = (JFGMsgVideoResolution) o;
            } else return;//要是resolution为空,就没必要设置了.
        }
        int height = getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                ? ViewGroup.LayoutParams.MATCH_PARENT : (int) (Resources.getSystem().getDisplayMetrics().widthPixels * resolution.height / (float) resolution.width);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height);
        View view = fLayoutLiveViewContainer.findViewById("IVideoView".hashCode());
        if (view == null) {
            fLayoutLiveViewContainer.addView((View) videoView, 0, lp);
        } else {
            view.setLayoutParams(lp);
            ViewUtils.updateViewHeight(fLayoutCamLiveView, resolution.height / (float) resolution.width);
        }
        AppLogger.i("updateVideoViewLayoutParameters:" + (view == null));
    }


    @OnClick({R.id.imgV_cam_switch_speaker,
            R.id.imgV_cam_trigger_mic,
            R.id.imgV_cam_trigger_capture,
            R.id.fLayout_cam_live_view})
    public void onClick(View view) {
        if (NetUtils.getJfgNetType(getContext()) == 0)
            return;
        ViewUtils.deBounceClick(view);
        switch (view.getId()) {
            case R.id.imgV_cam_switch_speaker:
                if (basePresenter != null) {
                    basePresenter.switchSpeakerMic(false, !basePresenter.getSpeakerFlag(), basePresenter.getMicFlag());
                    ((ImageView) view).setImageResource(basePresenter.getSpeakerFlag() ? R.drawable.icon_speaker_normal_port_off : R.drawable.icon_speaker_normal_port_on);
                }
                break;
            case R.id.imgV_cam_trigger_mic:
                if (basePresenter != null) {
                    basePresenter.switchSpeakerMic(false, basePresenter.getSpeakerFlag(), !basePresenter.getMicFlag());
                    ((ImageView) view).setImageResource(basePresenter.getMicFlag() ? R.drawable.btn_video_retry : R.drawable.icon_record);
                }
                break;
            case R.id.imgV_cam_trigger_capture:
                if (basePresenter != null) basePresenter.takeSnapShot();
                break;
            case R.id.fLayout_cam_live_view:
                if (videoView != null)
                    videoView.performTouch();
                break;
        }
    }


    @Override
    public void onClickLive() {
    }

    @Override
    public void onClickLandPlay(int state) {
        Toast.makeText(getContext(), "play: ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClickLandBack() {
        Toast.makeText(getContext(), "onBack: ", Toast.LENGTH_SHORT).show();
        ViewUtils.setRequestedOrientation(getActivity(), ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onClickLandSwitchSpeaker(int state) {
        Toast.makeText(getContext(), "onSwitchSpeaker: ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClickLandSwitchRecorder(int state) {
        Toast.makeText(getContext(), "onSwitchRecorder: ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClickLandCapture() {
        Toast.makeText(getContext(), "onCapture: ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onHistoryDataRsp(IData dataStack) {
        if (dataStack.getDataCount() > 0) {
            //显示按钮
            camLiveController.setLiveTime(System.currentTimeMillis());
        }
        camLiveController.setupHistoryData(dataStack);
    }

    @Override
    public void onLiveStop(int playType, int errId) {
        if (getView() != null)
            getView().setKeepScreenOn(false);
        showFloatFlowView(false, null);
        camLiveController.setLiveTime(0);
        switch (errId) {//这些errCode 应当写在一个map中.Map<Integer,String>
            case JFGRules.PlayErr.ERR_NERWORK:
                ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR_1));
                break;
            case JFGRules.PlayErr.ERR_UNKOWN:
                camLiveController.setLoadingState(ILiveControl.STATE_LOADING_FAILED, "出错了");
                break;
            case JFGRules.PlayErr.ERR_LOW_FRAME_RATE:
                camLiveController.setLoadingState(ILiveControl.STATE_LOADING_FAILED, "\"帧率太低,不足以播放,重试\"");
                break;
            case JFGRules.PlayErr.ERR_DEVICE_OFFLINE:
            case JError.ErrorVideoPeerNotExist:
                ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR));
                camLiveController.setLoadingState(ILiveControl.STATE_LOADING_FAILED, getString(R.string.OFFLINE_ERR));
                break;
            case JError.ErrorVideoPeerInConnect:
                //正在直播...
                ToastUtil.showToast(getString(R.string.CONNECTING));
                camLiveController.setLoadingState(ILiveControl.STATE_IDLE, null);
                break;
            default:
                camLiveController.setLoadingState(ILiveControl.STATE_STOP, null);
                break;
        }
        if (liveListener != null) liveListener.liveStateChange();
    }

    @Override
    public void onTakeSnapShot(boolean state) {
        if (state) {
            ToastUtil.showPositiveToast(getString(R.string.SAVED_PHOTOS));
        } else {
            ToastUtil.showPositiveToast(getString(R.string.set_failed));
        }
    }

    @Override
    public void onBeanInfoUpdate(final BeanCamInfo info) {
        if (getView() != null && isResumed()) {
            getView().post(() -> {
                camLiveController.setProtectionState(info.cameraAlarmFlag);
                camLiveController.setProtectionState(info.cameraAlarmFlag);
            });
            if (info.deviceBase != null && !TextUtils.isEmpty(info.deviceBase.shareAccount)) {
                //分享账号,不显示
                camLiveController.setLiveTime(0);
            }
        }
    }

    @Override
    public void onHistoryLiveStop(int state) {

    }

    @Override
    public void onRtcp(JFGMsgVideoRtcp rtcp) {
        String content = String.format(Locale.getDefault(), "%sKb/s", rtcp.bitRate);
        showFloatFlowView(true, content);
        if (!basePresenter.isShareDevice())
            camLiveController.setLiveTime(rtcp.timestamp == 0 ? System.currentTimeMillis() : rtcp.timestamp * 1000L);
        Log.d("onRtcp", "onRtcp: " + new Gson().toJson(rtcp));
    }

    @Override
    public void onResolution(JFGMsgVideoResolution resolution) throws JfgException {
        JfgCmdInsurance.getCmd().setRenderRemoteView((View) initVideoView());
        updateVideoViewLayoutParameters(resolution);
    }

    @Override
    public void setPresenter(CamLiveContract.Presenter basePresenter) {
        this.basePresenter = basePresenter;
    }
}


interface CamLandLiveAction {

    void onClickLive();

    void onClickLandPlay(int state);

    void onClickLandBack();

    void onClickLandSwitchSpeaker(int state);

    void onClickLandSwitchRecorder(int state);

    void onClickLandCapture();

}