package com.cylan.jiafeigou.n.view.cam;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
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
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.SimpleCache;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.listener.ILiveStateListener;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamLivePresenterImpl;
import com.cylan.jiafeigou.n.view.activity.CamSettingActivity;
import com.cylan.jiafeigou.n.view.activity.CameraLiveActivity;
import com.cylan.jiafeigou.n.view.activity.SightSettingActivity;
import com.cylan.jiafeigou.n.view.media.NormalMediaFragment;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LiveTimeLayout;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;
import com.cylan.jiafeigou.widget.flip.FlipLayout;
import com.cylan.jiafeigou.widget.glide.RoundedCornersTransformation;
import com.cylan.jiafeigou.widget.live.ILiveControl;
import com.cylan.jiafeigou.widget.pop.RelativePopupWindow;
import com.cylan.jiafeigou.widget.pop.RoundCardPopup;
import com.cylan.jiafeigou.widget.video.LiveViewWithThumbnail;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;
import com.cylan.jiafeigou.widget.wheel.ex.IData;
import com.cylan.panorama.CameraParam;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

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
        implements CamLiveContract.View, View.OnClickListener, BaseDialog.BaseDialogAction {


    //    @BindView(R.msgId.fLayout_live_view_container)
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

    @BindView(R.id.v_live)
    LiveViewWithThumbnail vLive;
    public Rect mLiveViewRectInWindow = new Rect();
    private SoftReference<AlertDialog> sdcardPulloutDlg;
    private SoftReference<AlertDialog> sdcardFormatDlg;
    private CamLiveController camLiveController;
    //    /**
//     * 直播状态监听
//     */
    private ILiveStateListener liveListener;

    private String uuid;
    private boolean isNormalView;
    private static final String DIALOG_KEY = "dialogFragment";

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
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
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
        initLiveView();
        checkSightDialog(isNormalView);
        ViewUtils.updateViewHeight(fLayoutCamLiveView, getScaleSizeFactor());//720*576
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

    private float getScaleSizeFactor() {
        if (!isNormalView)
            return 1.f;
        float r = PreferencesUtils.getFloat(JConstant.KEY_UUID_RESOLUTION + uuid, 0.8f);
        Log.d("getScaleSizeFactor", "getScaleSizeFactor: " + r);
        if (r == 0.0f) return 0.8f;
        return r;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void initLiveView() {
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        if (device == null) {
            AppLogger.e("device is null");
            getActivity().finish();
            return;
        }
        VideoViewFactory.IVideoView videoView = VideoViewFactory.CreateRendererExt(!isNormalView,
                getContext(), true);
        videoView.setInterActListener(new VideoViewFactory.InterActListener() {

            @Override
            public boolean onSingleTap(float x, float y) {
                camLiveController.tapVideoViewAction();
                return true;
            }

            @Override
            public void onSnapshot(Bitmap bitmap, boolean tag) {
                Log.d("onSnapshot", "onSnapshot: " + (bitmap == null));
            }
        });
        String _509 = device.$(509, "1");
        videoView.config360(TextUtils.equals(_509, "0") ? CameraParam.getTopPreset() : CameraParam.getWallPreset());
        videoView.setMode(TextUtils.equals("0", _509) ? 0 : 1);
        videoView.detectOrientationChanged();
        vLive.setLiveView(videoView);
        if (SimpleCache.getInstance().getSimpleBitmapCache(basePresenter.getThumbnailKey()) == null) {
            File file = new File(basePresenter.getThumbnailKey());
            vLive.setThumbnail(getContext(), PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, ""), Uri.fromFile(file));
        } else
            vLive.setThumbnail(getContext(), PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, ""), SimpleCache.getInstance().getSimpleBitmapCache(basePresenter.getThumbnailKey()));
        vLive.post(() -> vLive.getLocalVisibleRect(mLiveViewRectInWindow));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (basePresenter != null)
            basePresenter.stopPlayVideo(basePresenter.getPlayType());
        if (vLive != null && vLive.getVideoView() != null)
            vLive.getVideoView().onPause();
    }

    @Override
    public void onStart() {
        super.onStart();

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
        if (basePresenter != null) {
            basePresenter.fetchHistoryDataList();
            //非待机模式
            onDeviceInfoChanged(-1);

            //每天检测一次新固件
            basePresenter.checkNewHardWare();
        }
        camLiveController.setPortSafeSetter(portFlipLayout);
        //更新
        camLiveController.notifyOrientationChange(getResources().getConfiguration().orientation);
        if (vLive != null && vLive.getVideoView() != null) {
            vLive.getVideoView().onResume();
            Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
            String dpPrimary = device.$(509, "0");//0:俯视
            vLive.getVideoView().setMode(TextUtils.equals(dpPrimary, "0") ? 0 : 1);
            vLive.getVideoView().config360(TextUtils.equals(dpPrimary, "0") ? CameraParam.getTopPreset() : CameraParam.getWallPreset());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (vLive != null && vLive.getVideoView() != null) {
            vLive.getVideoView().onDestroy();
        }
    }

    private void startLive() {
        View old = fLayoutCamLiveView.findViewById(R.id.fLayout_cam_sight_setting);
        AppLogger.d("startPlay: old == null: " + (old == null));
        if (old != null) return;//不用播放
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        DpMsgDefine.DPStandby isStandBY = device.$(DpMsgMap.ID_508_CAMERA_STANDBY_FLAG, new DpMsgDefine.DPStandby());
        if (isStandBY != null && isStandBY.standby) return;
        if (!getUserVisibleHint()) return;//看不见，就不需要播放了。
        basePresenter.startPlayVideo(TYPE_LIVE);
    }

    private void startLiveHistory(long time) {
        View old = fLayoutCamLiveView.findViewById(R.id.fLayout_cam_sight_setting);
        AppLogger.d("startPlay: old == null: " + (old == null));
        if (old != null) return;//不用播放
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        DpMsgDefine.DPStandby isStandBY = device.$(508, new DpMsgDefine.DPStandby());
        if (isStandBY == null || isStandBY.standby) return;
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
        layout.setOnClickListener(v -> AppLogger.d("don't click me"));
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
                    .setPositiveButton(getString(R.string.OK), null)
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
            Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
            DpMsgDefine.DPStandby isStandBY = device.$(DpMsgMap.ID_508_CAMERA_STANDBY_FLAG, new DpMsgDefine.DPStandby());
            boolean flag = isStandBY.standby;
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
    }

    @Override
    public void onDeviceInfoChanged(JFGDPMsg msg) throws IOException {
        int msgId = (int) msg.id;
        if (msgId == DpMsgMap.ID_222_SDCARD_SUMMARY) {
            DpMsgDefine.DPSdcardSummary sdStatus = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPSdcardSummary.class);
            if (sdStatus == null) sdStatus = new DpMsgDefine.DPSdcardSummary();
            //sd卡状态变化，
            camLiveController.updateLiveButtonState(sdStatus.hasSdcard);
            if (!sdStatus.hasSdcard) {
                AppLogger.d("sdcard 被拔出");
                if (sdcardPulloutDlg != null && sdcardPulloutDlg.get() != null && sdcardPulloutDlg.get().isShowing())
                    return;
                if (!getUserVisibleHint()) {
                    AppLogger.d("隐藏了，sd卡更新");
                    return;
                }
                if (basePresenter.getPlayType() == PLAY_STATE_PLAYING) {
                    initSdcardStateDialog();
                    sdcardPulloutDlg.get().show();
                    if (basePresenter.getPlayType() == TYPE_HISTORY) {
                        basePresenter.stopPlayVideo(TYPE_HISTORY);
                    }
                }
            }
            AppLogger.e("sdcard数据被清空，唐宽，还没实现");
        }
        if (msgId == DpMsgMap.ID_508_CAMERA_STANDBY_FLAG) {
            onDeviceInfoChanged(msgId);
        }
        if (msgId == DpMsgMap.ID_218_DEVICE_FORMAT_SDCARD) {
//            DpMsgDefine.DpSdcardFormatRsp formatRsp = DpUtils.unpackData(msg.packValue, DpMsgDefine.DpSdcardFormatRsp.class);
//            if (formatRsp == null) formatRsp = DpMsgDefine.EMPTY.SDCARD_FORMAT_RSP;
            if (!getUserVisibleHint()) {
                AppLogger.d("隐藏了，sd卡被格式化");
                return;
            }
            if (basePresenter.getPlayType() != TYPE_HISTORY)
                return;
            if (sdcardFormatDlg != null && sdcardFormatDlg.get() != null && sdcardFormatDlg.get().isShowing())
                return;
            if (sdcardPulloutDlg != null && sdcardPulloutDlg.get() != null && sdcardPulloutDlg.get().isShowing()) {
                sdcardPulloutDlg.get().dismiss();//其他对话框要隐藏。
            }
//            if(formatRsp)
            initSdcardFormatDialog();
        }
        if (msgId == 509) {
            Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
            String _509 = device.$(509, "0");
            vLive.getVideoView().config360(TextUtils.equals(_509, "0") ? CameraParam.getTopPreset() : CameraParam.getWallPreset());
            vLive.getVideoView().setMode(TextUtils.equals("0", _509) ? 0 : 1);
            vLive.getVideoView().detectOrientationChanged();
        }
    }

    /**
     * 展示 待机模式view
     *
     * @param flag
     */
    private void setupStandByView(boolean flag) {
        if (flag)
            vLive.enableStandbyMode(true, v -> {
                Intent intent = new Intent(getActivity(), CamSettingActivity.class);
                intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                startActivityForResult(intent, REQUEST_CODE,
                        ActivityOptionsCompat.makeCustomAnimation(getActivity(),
                                R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
            }, JFGRules.isShareDevice(uuid));
        else vLive.enableStandbyMode(false, null, false);
    }

    @Override
    public void onLivePrepare(int type) {
        camLiveController.setLoadingState(ILiveControl.STATE_LOADING, null);
        AppLogger.i("onLivePrepare");
        if (liveListener != null) liveListener.liveStateChange();
    }

    @Override
    public void onLiveStarted(int type) {
        vLive.onLiveStart();
        camLiveController.setLoadingState(ILiveControl.STATE_PLAYING, null);
        AppLogger.i("onLiveStarted");
        if (getView() != null)
            getView().setKeepScreenOn(true);
        initBottomBtn(true);
        camLiveController.setLiveType(basePresenter.getPlayType(), getResources().getConfiguration().orientation);
        if (liveListener != null) liveListener.liveStateChange();
        imgVCamSwitchSpeaker.setImageResource(R.drawable.icon_port_speaker_off_selector);
        imgVCamSwitchSpeaker.setTag(R.drawable.icon_port_speaker_off_selector);
        imgVCamTriggerMic.setImageResource(R.drawable.icon_port_mic_off_selector);
        imgVCamTriggerMic.setTag(R.drawable.icon_port_mic_off_selector);
        camLiveController.getImvLandMic().setImageResource(R.drawable.icon_land_mic_off_selector);
        camLiveController.getImvLandMic().setTag(R.drawable.icon_land_mic_off_selector);
        camLiveController.getImvLandSpeaker().setImageResource(R.drawable.icon_land_speaker_off_selector);
        camLiveController.getImvLandSpeaker().setTag(R.drawable.icon_land_speaker_off_selector);
        imgVCamZoomToFullScreen.setVisibility(View.VISIBLE);
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
        vLive.detectOrientationChanged(port);
    }


    /**
     * 初始化流量
     */
    private void showFloatFlowView(boolean show, String content) {
        vLive.showFlowView(show, content);
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
        int height = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                ? ViewGroup.LayoutParams.MATCH_PARENT :
                (isNormalView ? (int) (Resources.getSystem().getDisplayMetrics().widthPixels * resolution.height / (float) resolution.width)
                        : Resources.getSystem().getDisplayMetrics().widthPixels);
        height = isNormalView ? height : (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ?
                Resources.getSystem().getDisplayMetrics().heightPixels
                : Resources.getSystem().getDisplayMetrics().widthPixels);
        ViewGroup.LayoutParams lp = fLayoutCamLiveView.getLayoutParams();
        if (lp == null)
            lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = height;
        fLayoutCamLiveView.setLayoutParams(lp);
        vLive.updateLayoutParameters(height, ViewGroup.LayoutParams.MATCH_PARENT);
        vLive.getVideoView().detectOrientationChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CameraLiveFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        if (permissions.length == 1) {
            AppLogger.d("permission:" + permissions + " " + grantResults);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgV_cam_switch_speaker: {
                boolean on = isLocalSpeakerOn();
                if (!on) {
                    MediaRecorder mRecorder = null;
                    try {
                        mRecorder = new MediaRecorder();
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mRecorder.release();
                    } catch (Exception e) {
                        AppLogger.d(e.getMessage());
                        audioRecordPermissionDenied();
                        if (mRecorder != null) {
                            mRecorder.release();
                        }
                        return;
                    }
                    CameraLiveFragmentPermissionsDispatcher.showAudioRecordPermission_WithCheck(this);
                    return;
                }
                int sFlag = R.drawable.icon_port_speaker_off_selector;
                imgVCamSwitchSpeaker.setImageResource(sFlag);
                imgVCamSwitchSpeaker.setTag(sFlag);
                //横屏
                camLiveController.getImvLandSpeaker().setImageResource(R.drawable.icon_land_speaker_off_selector);
                camLiveController.getImvLandSpeaker().setTag(R.drawable.icon_land_speaker_off_selector);
                if (basePresenter != null) {
                    basePresenter.switchSpeaker();
                }
            }
            break;
            case R.id.imgV_cam_trigger_mic: {
//                CameraLiveFragmentPermissionsDispatcher.s
                boolean on = isLocalMicOn();
                if (!on) {
                    MediaRecorder mRecorder = null;
                    try {
                        mRecorder = new MediaRecorder();
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mRecorder.release();
                    } catch (Exception e) {
                        AppLogger.d(e.getMessage());
                        audioRecordPermissionDenied();
                        if (mRecorder != null) {
                            mRecorder.release();
                        }
                        return;
                    }
                    CameraLiveFragmentPermissionsDispatcher.showAudioRecordPermissionWithCheck(this);
                    return;
                }
                imgVCamTriggerMic.setImageResource(R.drawable.icon_port_mic_off_selector);
                imgVCamTriggerMic.setTag(R.drawable.icon_port_mic_off_selector);
                camLiveController.getImvLandMic().setImageResource(R.drawable.icon_land_mic_off_selector);
                camLiveController.getImvLandMic().setTag(R.drawable.icon_land_mic_off_selector);
                camLiveController.getImvLandSpeaker().setEnabled(true);
                imgVCamSwitchSpeaker.setEnabled(true);
                //同时设置speaker
                imgVCamSwitchSpeaker.setImageResource(R.drawable.icon_port_speaker_off_selector);
                imgVCamSwitchSpeaker.setTag(R.drawable.icon_port_speaker_off_selector);
                camLiveController.getImvLandSpeaker().setImageResource(R.drawable.icon_land_speaker_off_selector);
                camLiveController.getImvLandSpeaker().setTag(R.drawable.icon_land_speaker_off_selector);
                if (basePresenter != null) {
                    basePresenter.switchMic();
                }
            }
            break;
            case R.id.imgV_cam_trigger_capture:
                PerformanceUtils.startTrace("takeSnapShot");
                if (basePresenter != null) basePresenter.takeSnapShot(false);
                break;
            case R.id.fLayout_cam_live_view:
                vLive.performTouch();
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
//        vLive.onLiveStop();
        if (vLive.isShowStandby()) {
            AppLogger.d("stand by is reEnabled");
            return;
        }
        showFloatFlowView(false, null);
        initBottomBtn(false);
        camLiveController.setLiveTime(0);
        switch (errId) {//这些errCode 应当写在一个map中.Map<Integer,String>
            case JFGRules.PlayErr.ERR_NERWORK:
                Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
                DpMsgDefine.DPStandby isStandBY = device.$(508, new DpMsgDefine.DPStandby());
                if (isStandBY == null || isStandBY.standby) break;//
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
                camLiveController.setLoadingState(ILiveControl.STATE_LOADING_FAILED, getString(R.string.OFFLINE_ERR), getString(R.string.USER_HELP));
                break;
            case JError.ErrorVideoPeerInConnect:
                //正在直播...
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
        vLive.onLiveStop();
        imgVCamZoomToFullScreen.setVisibility(View.GONE);
        AppLogger.d("onLiveStop");
    }

    @Override
    public void onTakeSnapShot(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            showPopupWindow(filePath);
        } else {
            ToastUtil.showPositiveToast(getString(R.string.set_failed));
        }
        PerformanceUtils.stopTrace("takeSnapShot");
    }

    @Override
    public void onPreviewResourceReady(Bitmap bitmap) {
        if (isVisible()) {
//            vLive.setThumbnail(getContext(), PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, ""), bitmap);
        }
    }

    private void showPopupWindow(String filePath) {
        try {
            roundCardPopup = new RoundCardPopup(getContext(), view -> {
                if (!TextUtils.isEmpty(filePath)) {
                    Glide.with(getContext())
                            .load(filePath)
                            .placeholder(R.drawable.wonderful_pic_place_holder)
                            .override((int) getResources().getDimension(R.dimen.x44), (int) getResources().getDimension(R.dimen.x30))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .bitmapTransform(new RoundedCornersTransformation(getContext(), 10, 2))
                            .into(view);
                }
            }, v -> {
                roundCardPopup.dismiss();
                Bundle bundle = new Bundle();
                bundle.putString(JConstant.KEY_SHARE_ELEMENT_BYTE, filePath);
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
        BaseApplication.getAppComponent().getCmd().enableRenderSingleRemoteView(true, (View) vLive.getVideoView());
        updateVideoViewLayoutParameters(resolution);
        if (resolution != null && resolution.height > 0 && resolution.width > 0) {
            PreferencesUtils.putFloat(JConstant.KEY_UUID_RESOLUTION + uuid, (float) resolution.height / resolution.width);
        }
    }

    @Override
    public void setPresenter(CamLiveContract.Presenter basePresenter) {
        this.basePresenter = basePresenter;
    }

    @NeedsPermission({Manifest.permission.RECORD_AUDIO})
    public void showAudioRecordPermission_() {
        int sFlag = R.drawable.icon_port_speaker_on_selector;
        imgVCamSwitchSpeaker.setImageResource(sFlag);
        imgVCamSwitchSpeaker.setTag(sFlag);
        //横屏
        camLiveController.getImvLandSpeaker().setImageResource(R.drawable.icon_land_speaker_on_selector);
        camLiveController.getImvLandSpeaker().setTag(R.drawable.icon_land_speaker_on_selector);
        if (basePresenter != null) {
            basePresenter.switchSpeaker();
        }
    }

    @NeedsPermission({Manifest.permission.RECORD_AUDIO})
    public void showAudioRecordPermission() {
        imgVCamTriggerMic.setImageResource(R.drawable.icon_port_mic_on_selector);
        imgVCamTriggerMic.setTag(R.drawable.icon_port_mic_on_selector);
        camLiveController.getImvLandMic().setImageResource(R.drawable.icon_land_mic_on_selector);
        camLiveController.getImvLandMic().setTag(R.drawable.icon_land_mic_on_selector);
        camLiveController.getImvLandSpeaker().setEnabled(false);
        imgVCamSwitchSpeaker.setEnabled(false);
        //同时设置speaker
        imgVCamSwitchSpeaker.setImageResource(R.drawable.icon_port_speaker_on_selector);
        imgVCamSwitchSpeaker.setTag(R.drawable.icon_port_speaker_on_selector);
        camLiveController.getImvLandSpeaker().setImageResource(R.drawable.icon_land_speaker_on_selector);
        camLiveController.getImvLandSpeaker().setTag(R.drawable.icon_land_speaker_on_selector);
        if (basePresenter != null) {
            basePresenter.switchMic();
        }
    }


    @OnPermissionDenied({Manifest.permission.RECORD_AUDIO})
    public void audioRecordPermissionDenied() {
        ToastUtil.showNegativeToast(getString(R.string.permission_auth, getString(R.string.sound_auth), ""));
    }

    @OnNeverAskAgain({Manifest.permission.RECORD_AUDIO})
    public void audioRecordPermissionNeverAsk() {
        ToastUtil.showNegativeToast(getString(R.string.permission_auth, getString(R.string.sound_auth), ""));
    }

    @OnShowRationale({Manifest.permission.RECORD_AUDIO})
    public void audioRecordPermissionRational(PermissionRequest request) {
        ToastUtil.showNegativeToast(getString(R.string.permission_auth, getString(R.string.sound_auth), ""));
    }

    @Override
    public void hardwareResult(RxEvent.CheckDevVersionRsp rsp) {
        if (rsp.hasNew) {
            Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_KEY);
            if (f == null) {
                Bundle bundle = new Bundle();
                bundle.putString(BaseDialog.KEY_TITLE, getString(R.string.Tap1_Device_UpgradeTips));
                bundle.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, getString(R.string.CANCEL));
                bundle.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, getString(R.string.OK));
                bundle.putBoolean(SimpleDialogFragment.KEY_TOUCH_OUT_SIDE_DISMISS, false);
                SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(bundle);
                dialogFragment.setValue(rsp);
                dialogFragment.setAction(this);
                dialogFragment.show(getActivity().getSupportFragmentManager(), DIALOG_KEY);
            }
        }
    }

    @Override
    public void onHistoryDateListUpdate(ArrayList<Long> dateList) {

    }

    @Override
    public void onDialogAction(int id, Object value) {
        if (id == R.id.tv_dialog_btn_right) {
            Bundle bundle = new Bundle();
            bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
            bundle.putSerializable("version_content", (RxEvent.CheckDevVersionRsp) value);
            HardwareUpdateFragment hardwareUpdateFragment = HardwareUpdateFragment.newInstance(bundle);
            ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                    hardwareUpdateFragment, android.R.id.content);
        }
    }
}