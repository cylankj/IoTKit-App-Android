package com.cylan.jiafeigou.n.view.cam;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.module.JFGDPDevice;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.listener.ILiveStateListener;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamLivePresenterImpl;
import com.cylan.jiafeigou.n.view.activity.CamSettingActivity;
import com.cylan.jiafeigou.n.view.activity.CameraLiveActivity;
import com.cylan.jiafeigou.n.view.activity.SightSettingActivity;
import com.cylan.jiafeigou.n.view.media.NormalMediaFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.DensityUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LiveTimeLayout;
import com.cylan.jiafeigou.widget.flip.FlipLayout;
import com.cylan.jiafeigou.widget.glide.RoundedCornersTransformation;
import com.cylan.jiafeigou.widget.live.ILiveControl;
import com.cylan.jiafeigou.widget.pop.RelativePopupWindow;
import com.cylan.jiafeigou.widget.pop.RoundCardPopup;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;
import com.cylan.jiafeigou.widget.wheel.ex.IData;
import com.cylan.panorama.CameraParam;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_508_CAMERA_STANDBY_FLAG;
import static com.cylan.jiafeigou.misc.JConstant.KEY_CAM_SIGHT_SETTING;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PLAYING;
import static com.cylan.jiafeigou.misc.JFGRules.PlayErr.STOP_MAUNALLY;
import static com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract.TYPE_HISTORY;
import static com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract.TYPE_LIVE;
import static com.cylan.jiafeigou.support.photoselect.helpers.Constants.REQUEST_CODE;

/**
 * A simple {@link Fragment} subclass.
 */
@RuntimePermissions()
public class CameraLiveFragment extends IBaseFragment<CamLiveContract.Presenter>
        implements CamLiveContract.View, View.OnClickListener {


    //    @BindView(R.id.fLayout_live_view_container)
//    FrameLayout fLayoutLiveViewContainer;
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
    CamLiveLandControlLayer swCamLiveControlLayer;


    @BindView(R.id.lLayout_protection)
    FlipLayout portFlipLayout;
    @BindView(R.id.live_time_layout)
    LiveTimeLayout liveTimeLayout;
    @BindView(R.id.imgV_cam_zoom_to_full_screen)
    ImageView imgVCamZoomToFullScreen;
    @BindView(R.id.imv_double_sight)
    ImageView imvDoubleSight;

    private SoftReference<AlertDialog> sdcardPulloutDlg;
    private SoftReference<AlertDialog> sdcardFormatDlg;
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
    private boolean isNormalView;

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
        this.uuid = getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID);
        basePresenter = new CamLivePresenterImpl(this, uuid);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JFGDPDevice device = DataSourceManager.getInstance().getJFGDevice(uuid);
        isNormalView = device != null && !JFGRules.isNeedPanoramicView(device.pid);
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
        //2w显示双排视图  3.1.0功能
//        imvDoubleSight.setVisibility(isNormalView ? View.GONE : View.VISIBLE);
        checkSightDialog(isNormalView);
        ViewUtils.updateViewHeight(fLayoutCamLiveView, isNormalView ? 0.8f : 1.0f);//720*576
        initBottomBtn(false);
        imgVCamSwitchSpeaker.setOnClickListener(this);
        imgVCamTriggerMic.setOnClickListener(this);
        imgVCamTriggerCapture.setOnClickListener(this);
        fLayoutCamLiveView.setOnClickListener(this);
        if (camLiveController == null)
            camLiveController = new CamLiveController(getActivity(), uuid);
        camLiveController.setPresenterRef(basePresenter);
        camLiveController.setLiveAction((ILiveControl) vs_control.inflate());
        camLiveController.setCamLiveControlLayer(swCamLiveControlLayer);
        camLiveController.setScreenZoomer(imgVCamZoomToFullScreen);
        camLiveController.setPortLiveTimeSetter(liveTimeLayout);
        camLiveController.setActivity(getActivity());
        camLiveController.setImgPortMic(imgVCamTriggerMic);
        camLiveController.setImgPortSpeaker(imgVCamSwitchSpeaker);
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
            onDeviceInfoChanged(-1);
        }
        camLiveController.setPortSafeSetter(portFlipLayout);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (basePresenter != null && isVisibleToUser && isResumed() && getActivity() != null && getActivity() instanceof CameraLiveActivity) {
            Bundle bundle = ((CameraLiveActivity) getActivity()).getCurrentBundle();
            int playType = bundle == null ? TYPE_LIVE : bundle.getInt(JConstant.KEY_CAM_LIVE_PAGE_PLAY_TYPE, TYPE_LIVE);
            if (playType == TYPE_LIVE) {
                startLive();
            } else if (playType == TYPE_HISTORY) {
                long time = bundle.getLong(JConstant.KEY_CAM_LIVE_PAGE_PLAY_HISTORY_TIME);
                if (time == 0 && BuildConfig.DEBUG)
                    throw new IllegalArgumentException("play history time is 0");
                startLiveHistory(time);
                ((CameraLiveActivity) getActivity()).setCurrentBundle(null);//使用完，清空
            }
        } else if (isResumed()) {
            basePresenter.stopPlayVideo(basePresenter.getPlayType());
            AppLogger.d("stop play");
        } else {
            AppLogger.d("not ready ");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //更新
        camLiveController.notifyOrientationChange(getResources().getConfiguration().orientation);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (videoView != null) ((View) videoView).setVisibility(View.GONE);
    }

    private void startLive() {
        View old = fLayoutCamLiveView.findViewById(R.id.fLayout_cam_sight_setting);
        AppLogger.d("startPlay: old == null: " + (old == null));
        if (old != null) return;//不用播放
        DpMsgDefine.DPPrimary<Boolean> isStandBY = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_508_CAMERA_STANDBY_FLAG);
        boolean standby = MiscUtils.safeGet(isStandBY, false);
        if (isStandBY != null && standby) return;
        basePresenter.startPlayVideo(TYPE_LIVE);
    }

    private void startLiveHistory(long time) {
        View old = fLayoutCamLiveView.findViewById(R.id.fLayout_cam_sight_setting);
        AppLogger.d("startPlay: old == null: " + (old == null));
        if (old != null) return;//不用播放
        DpMsgDefine.DPPrimary<Boolean> isStandBY = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_508_CAMERA_STANDBY_FLAG);
        boolean standby = MiscUtils.safeGet(isStandBY, false);
        if (standby) return;
        basePresenter.startPlayHistory(time);
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
            Log.d("initBottomBtn", "setClickable: " + enable);
        });
    }

    /**
     * 视角设置
     *
     * @param isNormalCam
     */
    private void checkSightDialog(boolean isNormalCam) {
        if (isNormalCam || basePresenter.isShareDevice()) return;
        boolean isFirstShow = PreferencesUtils.getBoolean(KEY_CAM_SIGHT_SETTING + uuid, true);
        if (!isFirstShow) return;//不是第一次
        View old = fLayoutCamLiveView.findViewById(R.id.fLayout_cam_sight_setting);
        if (old != null) {
            //已经有了
            old.setVisibility(View.VISIBLE);
            return;
        }
        View view = LayoutInflater.from(getContext()).inflate(R.layout.cam_sight_setting_overlay, null);
        fLayoutCamLiveView.addView(view);//最顶
        View layout = fLayoutCamLiveView.findViewById(R.id.fLayout_cam_sight_setting);
        ((TextView) (view.findViewById(R.id.tv_sight_setting_content))).setText(getString(R.string.Tap1_Camera_Overlook) + ": "
                + getString(R.string.Tap1_Camera_OverlookTips));
        view.findViewById(R.id.btn_sight_setting_cancel).setOnClickListener((View v) -> {
            if (layout != null) fLayoutCamLiveView.removeView(layout);
            startLive();
        });
        view.findViewById(R.id.btn_sight_setting_next).setOnClickListener((View v) -> {
            if (layout != null) fLayoutCamLiveView.removeView(layout);
            Intent intent = new Intent(getActivity(), SightSettingActivity.class);
            intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
            startActivity(intent);
        });
        PreferencesUtils.putBoolean(KEY_CAM_SIGHT_SETTING + uuid, false);
    }

    private void initSdcardStateDialog() {
        if (sdcardPulloutDlg == null || sdcardPulloutDlg.get() == null) {
            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.MSG_SD_OFF))
                    .setPositiveButton(getString(R.string.OK), (DialogInterface d, int which) -> {
                        if (basePresenter.getPlayType() != PLAY_STATE_PLAYING)
                            basePresenter.startPlayVideo(TYPE_LIVE);
                    })
                    .setNegativeButton(getString(R.string.CANCEL), null)
                    .create();
            sdcardPulloutDlg = new SoftReference<>(dialog);
        }
    }

    private void initSdcardFormatDialog() {
        if (sdcardFormatDlg == null || sdcardFormatDlg.get() == null) {
            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.Clear_Sdcard_tips6))
                    .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNegativeButton(getString(R.string.CANCEL), null)
                    .create();
            sdcardFormatDlg = new SoftReference<>(dialog);
        }
    }

    /**
     * 根据 待机模式 ,分享用户模式设置一些view的状态
     */
    @Override
    public void onDeviceInfoChanged(long msgId) {
        if (msgId == -1 || msgId == DpMsgMap.ID_508_CAMERA_STANDBY_FLAG) {
            DpMsgDefine.DPPrimary<Boolean> wFlag = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_508_CAMERA_STANDBY_FLAG);
            boolean flag = MiscUtils.safeGet(wFlag, false);
            fLayoutLiveBottomHandleBar.setVisibility(flag ? View.INVISIBLE : View.VISIBLE);
            if (flag) {
                camLiveController.setLoadingState(ILiveControl.STATE_IDLE, null);
                //安全防护状态。
                showFloatFlowView(false, null);
                //需要断开直播
                if (basePresenter != null && basePresenter.getPlayState() == PLAY_STATE_PLAYING) {
                    basePresenter.stopPlayVideo(basePresenter.getPlayType());
                }
            } else {
                startLive();
            }
            setupStandByView(flag);
        }
        if (msgId == DpMsgMap.ID_204_SDCARD_STORAGE) {
            DpMsgDefine.DPSdStatus sdStatus = MiscUtils.safeGet_(DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_204_SDCARD_STORAGE), DpMsgDefine.DPSdStatus.empty);
            //sd卡状态变化，
            camLiveController.updateLiveButtonState(sdStatus != null && sdStatus.hasSdcard);
            if (sdStatus == null || !sdStatus.hasSdcard) {
                AppLogger.d("sdcard 被拔出");
                if (sdcardPulloutDlg != null && sdcardPulloutDlg.get() != null && sdcardPulloutDlg.get().isShowing())
                    return;
                initSdcardStateDialog();
                sdcardPulloutDlg.get().show();
                if (basePresenter.getPlayType() == TYPE_HISTORY) {
                    basePresenter.stopPlayVideo(TYPE_HISTORY);
                }
            }
            AppLogger.e("sdcard数据被清空，唐宽，还没实现");
        }
    }

    private void setupStandByView(boolean flag) {
        //进入待机模式
        View v = fLayoutCamLiveView.findViewById("showSceneView".hashCode());
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
                int index = 0;
                if (videoView != null)
                    index = fLayoutCamLiveView.indexOfChild((View) videoView);//view的上面
                fLayoutCamLiveView.addView(v, index + 1);//最底
                boolean isShareDevice = JFGRules.isShareDevice(uuid);
                TextView tv = (TextView) v.findViewById(R.id.lLayout_standby_jump_setting);
                //分享设备显示：已进入待机状态
                if (isShareDevice) {
                    tv.setText(getString(R.string.Tap1_Camera_Video_Standby));
                    return;
                }
                //非分享设备显示：已进入待机状态，前往开启，和设置点击事件。跳转到设置页面
                tv.setOnClickListener(view -> {
                    Intent intent = new Intent(getActivity(), CamSettingActivity.class);
                    intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                    startActivityForResult(intent, REQUEST_CODE,
                            ActivityOptionsCompat.makeCustomAnimation(getActivity(),
                                    R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
                });
            } else v = viewStandbyRef.get();
        }
        v.setVisibility(flag ? View.VISIBLE : View.GONE);
        AppLogger.i("onDeviceInfoChanged");
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
        camLiveController.setLiveType(basePresenter.getPlayType());
        if (liveListener != null) liveListener.liveStateChange();
        imgVCamSwitchSpeaker.setImageResource(R.drawable.icon_port_speaker_off_selector);
        imgVCamSwitchSpeaker.setTag(R.drawable.icon_port_speaker_off_selector);
        imgVCamTriggerMic.setImageResource(R.drawable.icon_port_mic_off_selector);
        imgVCamTriggerMic.setTag(R.drawable.icon_port_mic_off_selector);
        camLiveController.getImvLandMic().setImageResource(R.drawable.icon_land_mic_off_selector);
        camLiveController.getImvLandMic().setTag(R.drawable.icon_land_mic_off_selector);
        camLiveController.getImvLandSpeaker().setImageResource(R.drawable.icon_land_speaker_off_selector);
        camLiveController.getImvLandSpeaker().setTag(R.drawable.icon_land_speaker_off_selector);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean port = this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (!port) {
            // 加入横屏要处理的代码
            fLayoutLiveBottomHandleBar.setVisibility(View.GONE);
            fLayoutCamLiveMenu.setVisibility(View.GONE);
        } else {
            // 加入竖屏要处理的代码
            fLayoutCamLiveMenu.setVisibility(View.VISIBLE);
            fLayoutLiveBottomHandleBar.setVisibility(View.VISIBLE);
        }
        camLiveController.notifyOrientationChange(this.getResources().getConfiguration().orientation);
        AppLogger.i("onConfigurationChanged");
        updateVideoViewLayoutParameters(null);
        if (tvFlowRef != null && tvFlowRef.get() != null)
            ViewUtils.setMargins(tvFlowRef.get(), 0, (int) getResources().getDimension(port ? R.dimen.x14 : R.dimen.x54),
                    (int) getResources().getDimension(R.dimen.x14), 0);
    }


    /**
     * 初始化流量
     */
    private void showFloatFlowView(boolean show, String content) {
        View v = fLayoutCamLiveView.findViewById("flow".hashCode());
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
                lp.rightMargin = (int) getResources().getDimension(R.dimen.x14);
                lp.topMargin = (int) getResources().getDimension(R.dimen.x14);
                fLayoutCamLiveView.addView(textView, lp);
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
            JFGDPDevice device = DataSourceManager.getInstance().getJFGDevice(uuid);
            if (device == null) {
                AppLogger.e("device is null");
                getActivity().finish();
                return null;
            }
            videoView = VideoViewFactory.CreateRendererExt(!isNormalView,
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
        videoView.config360(CameraParam.getTopPreset());
        return videoView;
    }

    /**
     * 第一次创建view,会导致全屏闪烁一下.
     *
     * @param resolution
     */
    private void updateVideoViewLayoutParameters(JFGMsgVideoResolution resolution) {
        if (resolution != null) fLayoutCamLiveView.setTag(resolution);
        if (resolution == null) {
            Object o = fLayoutCamLiveView.getTag();
            if (o != null && o instanceof JFGMsgVideoResolution) {
                resolution = (JFGMsgVideoResolution) o;
            } else return;//要是resolution为空,就没必要设置了.
        }
        //全屏：高度全屏，横屏：普通view根据分辨率；全景高度=宽度
        int height = getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                ? ViewGroup.LayoutParams.MATCH_PARENT :
                (isNormalView ? (int) (Resources.getSystem().getDisplayMetrics().widthPixels * resolution.height / (float) resolution.width)
                        : Resources.getSystem().getDisplayMetrics().widthPixels);
        height = isNormalView ? height : Resources.getSystem().getDisplayMetrics().widthPixels;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height);

        fLayoutCamLiveView.setLayoutParams(lp);
        View view = fLayoutCamLiveView.findViewById("IVideoView".hashCode());
        if (view == null) {
            fLayoutCamLiveView.addView((View) videoView, 0, lp);
        } else {
            FrameLayout.LayoutParams fLP = (FrameLayout.LayoutParams) view.getLayoutParams();
            fLP.width = ViewGroup.LayoutParams.MATCH_PARENT;
            fLP.height = height;
            view.setLayoutParams(fLP);
        }
        AppLogger.i("updateVideoViewLayoutParameters:" + (view == null));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CameraLiveFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgV_cam_switch_speaker: {
                CameraLiveFragmentPermissionsDispatcher.audioPermissionGrantWithCheck(this);
                CameraLiveFragmentPermissionsDispatcher.audioSettingPermissionGrantWithCheck(this);
                boolean on = isLocalSpeakerOn();
                int sFlag = on ? R.drawable.icon_port_speaker_off_selector : R.drawable.icon_port_speaker_on_selector;
                ((ImageView) view).setImageResource(sFlag);
                view.setTag(sFlag);
                //横屏
                camLiveController.getImvLandSpeaker().setImageResource(on ? R.drawable.icon_land_speaker_off_selector : R.drawable.icon_land_speaker_on_selector);
                camLiveController.getImvLandSpeaker().setTag(on ? R.drawable.icon_land_speaker_off_selector : R.drawable.icon_land_speaker_on_selector);
                if (basePresenter != null) {
                    basePresenter.switchSpeaker();
                }
            }
            break;
            case R.id.imgV_cam_trigger_mic: {
                boolean on = isLocalMicOn();
                int micFlag = on ? R.drawable.icon_port_mic_off_selector : R.drawable.icon_port_mic_on_selector;
                ((ImageView) view).setImageResource(micFlag);
                view.setTag(micFlag);
                camLiveController.getImvLandMic().setImageResource(on ? R.drawable.icon_land_mic_off_selector : R.drawable.icon_land_mic_on_selector);
                camLiveController.getImvLandMic().setTag(on ? R.drawable.icon_land_mic_off_selector : R.drawable.icon_land_mic_on_selector);
                camLiveController.getImvLandSpeaker().setEnabled(on);
                imgVCamSwitchSpeaker.setEnabled(on);
                if (!on) {
                    //同时设置speaker
                    imgVCamSwitchSpeaker.setImageResource(R.drawable.icon_port_speaker_on_selector);
                    imgVCamSwitchSpeaker.setTag(R.drawable.icon_port_speaker_on_selector);
                    camLiveController.getImvLandSpeaker().setImageResource(R.drawable.icon_land_speaker_on_selector);
                    camLiveController.getImvLandSpeaker().setTag(R.drawable.icon_land_speaker_on_selector);
                }
                if (basePresenter != null) {
                    basePresenter.switchMic();
                }
            }
            break;
            case R.id.imgV_cam_trigger_capture:
                if (basePresenter != null) basePresenter.takeSnapShot(false);
                break;
            case R.id.fLayout_cam_live_view:
                if (videoView != null)
                    videoView.performTouch();
                break;
        }
    }

    @Override
    public boolean isLocalMicOn() {
        Object tag = imgVCamTriggerMic.getTag();
        return tag != null && (int) tag == R.drawable.icon_port_mic_on_selector;
    }

    @Override
    public boolean isLocalSpeakerOn() {
        Object tag = imgVCamSwitchSpeaker.getTag();
        return tag != null && (int) tag == R.drawable.icon_port_speaker_on_selector;
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
        initBottomBtn(false);
        camLiveController.setLiveTime(0);
        switch (errId) {//这些errCode 应当写在一个map中.Map<Integer,String>
            case JFGRules.PlayErr.ERR_NERWORK:
                DpMsgDefine.DPPrimary<Boolean> standby = DataSourceManager.getInstance().getValue(uuid, ID_508_CAMERA_STANDBY_FLAG);
                boolean s = MiscUtils.safeGet(standby, false);
                if (s) break;//
                camLiveController.setLoadingState(ILiveControl.STATE_LOADING_FAILED, getString(R.string.OFFLINE_ERR_1), getString(R.string.USER_HELP));
                break;
            case JFGRules.PlayErr.ERR_UNKOWN:
                camLiveController.setLoadingState(ILiveControl.STATE_LOADING_FAILED, getString(R.string.NO_NETWORK_2));
                break;
            case JFGRules.PlayErr.ERR_LOW_FRAME_RATE:
                int net = NetUtils.getJfgNetType(getActivity());
                camLiveController.setLoadingState(ILiveControl.STATE_LOADING_FAILED, getString(R.string.GLOBAL_NO_NETWORK), net == 0 ? getString(R.string.USER_HELP) : null);
                break;
            case JFGRules.PlayErr.ERR_DEVICE_OFFLINE:
            case JError.ErrorVideoPeerNotExist:
//                ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR));
                camLiveController.setLoadingState(ILiveControl.STATE_LOADING_FAILED, getString(R.string.OFFLINE_ERR));
                break;
            case JError.ErrorVideoPeerInConnect:
                //正在直播...
                ToastUtil.showToast(getString(R.string.CONNECTING));
                camLiveController.setLoadingState(ILiveControl.STATE_LOADING_FAILED, getString(R.string.CONNECTING));
                break;
            case STOP_MAUNALLY:
                camLiveController.setLoadingState(ILiveControl.STATE_STOP, null);
                break;
            case JFGRules.PlayErr.ERR_NOT_FLOW:
                camLiveController.setLoadingState(ILiveControl.STATE_LOADING_FAILED, getString(R.string.NETWORK_TIMEOUT));
                break;
            default:
                camLiveController.setLoadingState(ILiveControl.STATE_LOADING_FAILED, getString(R.string.GLOBAL_NO_NETWORK));
                break;
        }
        if (liveListener != null) liveListener.liveStateChange();
    }

    @Override
    public void onTakeSnapShot(Bitmap bitmap) {
        if (bitmap != null) {
            ToastUtil.showPositiveToast(getString(R.string.SAVED_PHOTOS));
            showPopupWindow(bitmap);
        } else {
            ToastUtil.showPositiveToast(getString(R.string.set_failed));
        }
    }

    private void showPopupWindow(Bitmap bitmap) {
        try {
            roundCardPopup = new RoundCardPopup(getContext(), view -> {
                if (bitmap != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    Glide.with(getContext())
                            .load(stream.toByteArray())
                            .placeholder(R.drawable.wonderful_pic_place_holder)
                            .override((int) getResources().getDimension(R.dimen.x44),
                                    (int) getResources().getDimension(R.dimen.x30))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .bitmapTransform(new RoundedCornersTransformation(getContext(), 10, 2))
                            .into(view);
                }
            }, v -> {
                roundCardPopup.dismiss();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                Bundle bundle = new Bundle();
                bundle.putByteArray(JConstant.KEY_SHARE_ELEMENT_BYTE, byteArray);
                NormalMediaFragment fragment = NormalMediaFragment.newInstance(bundle);
                ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(), fragment,
                        android.R.id.content);
                fragment.setCallBack(t -> getActivity().getSupportFragmentManager().popBackStack());
            });
            roundCardPopup.showOnAnchor(imgVCamTriggerCapture, RelativePopupWindow.VerticalPosition.ABOVE, RelativePopupWindow.HorizontalPosition.CENTER);
            basePresenter.startCountForDismissPop();
        } catch (Exception e) {
            AppLogger.e("showPopupWindow: " + e.getLocalizedMessage());
        }
    }

    private RoundCardPopup roundCardPopup;

    @Override
    public void onHistoryLiveStop(int state) {

    }

    @Override
    public void shouldWaitFor(boolean start) {
        camLiveController.setLoadingState(start ? ILiveControl.STATE_LOADING : ILiveControl.STATE_IDLE, null);
    }

    @Override
    public void countdownFinish() {
        roundCardPopup.dismiss();//don't need try catch ,it is wrapped by rxjava
    }

    @Override
    public void onRtcp(JFGMsgVideoRtcp rtcp) {
        String content = MiscUtils.getByteFromBitRate(rtcp.bitRate);
        showFloatFlowView(true, content);
        if (!basePresenter.isShareDevice())
            camLiveController.setLiveTime(rtcp.timestamp == 0 ? System.currentTimeMillis() : rtcp.timestamp * 1000L);
        Log.d("onRtcp", "onRtcp: " + new Gson().toJson(rtcp));
    }

    @Override
    public void onResolution(JFGMsgVideoResolution resolution) throws JfgException {
        JfgCmdInsurance.getCmd().enableRenderSingleRemoteView(true, (View) initVideoView());
        updateVideoViewLayoutParameters(resolution);
    }

    @Override
    public void setPresenter(CamLiveContract.Presenter basePresenter) {
        this.basePresenter = basePresenter;
    }

    @NeedsPermission({Manifest.permission.RECORD_AUDIO})
    public void audioPermissionGrant() {
        if (basePresenter != null) {
            Log.d("NeedsPermission", "audioPermissionGrant");
        }
    }

    @NeedsPermission({Manifest.permission.MODIFY_AUDIO_SETTINGS})
    public void audioSettingPermissionGrant() {
        if (basePresenter != null) {
            Log.d("NeedsPermission", "audioSettingPermissionGrant");
        }
    }

    @OnPermissionDenied({Manifest.permission.RECORD_AUDIO})
    public void audioPermissionDenied() {
        Log.d("OnPermissionDenied", "audioPermissionDenied");
    }

    @OnPermissionDenied({Manifest.permission.MODIFY_AUDIO_SETTINGS})
    public void audioSettingPermissionDenied() {
        Log.d("OnPermissionDenied", "audioSettingPermissionDenied");
    }

}