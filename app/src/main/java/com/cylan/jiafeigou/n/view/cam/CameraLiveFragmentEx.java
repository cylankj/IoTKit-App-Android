package com.cylan.jiafeigou.n.view.cam;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamLivePresenterImpl;
import com.cylan.jiafeigou.n.view.activity.CamSettingActivity;
import com.cylan.jiafeigou.n.view.firmware.FirmwareUpdateActivity;
import com.cylan.jiafeigou.n.view.mine.HomeMineHelpActivity;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.flip.FlipImageView;
import com.cylan.jiafeigou.widget.live.ILiveControl;
import com.cylan.jiafeigou.widget.wheel.ex.IData;
import com.google.gson.Gson;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_501_CAMERA_ALARM_FLAG;
import static com.cylan.jiafeigou.misc.JConstant.KEY_CAM_SIGHT_SETTING;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_LOADING_FAILED;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PLAYING;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_STOP;
import static com.cylan.jiafeigou.misc.JFGRules.PlayErr.STOP_MAUNALLY;
import static com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract.TYPE_HISTORY;
import static com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract.TYPE_LIVE;
import static com.cylan.jiafeigou.support.photoselect.helpers.Constants.REQUEST_CODE;

/**
 * A simple {@link Fragment} subclass.
 */
@RuntimePermissions()
public class CameraLiveFragmentEx extends IBaseFragment<CamLiveContract.Presenter>
        implements CamLiveContract.View {

    public Rect mLiveViewRectInWindow = new Rect();
    @BindView(R.id.cam_live_control_layer)
    CamLiveControllerEx camLiveControlLayer;
    private boolean isNormalView;
    private MyEventListener eventListener;

    public CameraLiveFragmentEx() {
        // Required empty public constructor
    }

    public static CameraLiveFragmentEx newInstance(Bundle bundle) {
        CameraLiveFragmentEx fragment = new CameraLiveFragmentEx();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        basePresenter = new CamLivePresenterImpl(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Device device = getDevice();
        isNormalView = device != null && !JFGRules.isNeedPanoramicView(device.pid);
        eventListener = new MyEventListener(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        camLiveControlLayer.initView(basePresenter, getUuid());
        camLiveControlLayer.initLiveViewRect(isNormalView ? basePresenter.getVideoPortHeightRatio() : 1.0f, mLiveViewRectInWindow);
        camLiveControlLayer.setLoadingRectAction(new ILiveControl.Action() {
            @Override
            public void clickImage(View view, int state) {
                switch (state) {
                    case PLAY_STATE_LOADING_FAILED:
                    case PLAY_STATE_STOP:
                        CamLiveContract.LiveStream prePlayType = basePresenter.getLiveStream();
                        if (accept()) {  // 减少if 层次
                            if (prePlayType.type == TYPE_HISTORY) {
                                basePresenter.startPlayHistory(prePlayType.time * 1000L);
                            } else if (prePlayType.type == TYPE_LIVE) {
                                basePresenter.startPlay();
                            }
                        }
                        break;
                    case PLAY_STATE_PLAYING:
                        //下一步stop
                        basePresenter.stopPlayVideo(STOP_MAUNALLY).subscribe(ret -> {
//                            camLiveControlLayer.getLiveViewWithThumbnail().getVideoView().takeSnapshot(true);
                        }, AppLogger::e);
                        break;
                }
                AppLogger.i("clickImage:" + state);
            }

            @Override
            public void clickText(View view) {

            }

            @Override
            public void clickHelp(View view) {
                if (NetUtils.getJfgNetType() == 0) {
                    ToastUtil.showNegativeToast(ContextUtils.getContext().getString(R.string.OFFLINE_ERR_1));
                    return;
                }
                Intent intent = new Intent(getContext(), HomeMineHelpActivity.class);
                intent.putExtra(JConstant.KEY_SHOW_SUGGESTION, JConstant.KEY_SHOW_SUGGESTION);
                startActivity(intent, ActivityOptionsCompat.makeCustomAnimation(getContext(),
                        R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
            }
        });
        camLiveControlLayer.setFlipListener(new FlipImageView.FlipListener() {
            @Override
            public void onClick(FlipImageView view) {
                if (camLiveControlLayer.isActionBarHide() && MiscUtils.isLand()) {
                    return;//动画过程中
                }
                Device device = basePresenter.getDevice();
                DpMsgDefine.DPSdStatus dpSdStatus = device.$(204, new DpMsgDefine.DPSdStatus());
                int oldOption = device.$(ID_303_DEVICE_AUTO_VIDEO_RECORD, -1);
                boolean safeIsOpen = device.$(ID_501_CAMERA_ALARM_FLAG, false);


                //先判断是否关闭了自动录像,关闭了提示 :若关闭，“侦测到异常时”将不启用录像

                //若自动录像未关闭 则提示:关闭“移动侦测”，将停止“侦测报警录像”


                //无卡不需要显示 //oldOption 不等于2 说明没有关闭自动录像则提示:关闭“移动侦测”，将停止“侦测报警录像”
                if (oldOption == 0 && safeIsOpen && dpSdStatus.hasSdcard && dpSdStatus.err == 0) {
                    AlertDialogManager.getInstance().showDialog(getActivity(),
                            getString(R.string.Tap1_Camera_MotionDetection_OffTips),
                            getString(R.string.Tap1_Camera_MotionDetection_OffTips),
                            getString(R.string.CARRY_ON), (DialogInterface dialog, int which) -> {
                                DpMsgDefine.DPPrimary<Boolean> wFlag = new DpMsgDefine.DPPrimary<>();
                                wFlag.value = false;
                                basePresenter.updateInfoReq(wFlag, DpMsgMap.ID_501_CAMERA_ALARM_FLAG);

//                                //关闭移动侦测的同时也关闭自动录像
//                                DpMsgDefine.DPPrimary<Integer> record = new DpMsgDefine.DPPrimary<>(2);
//                                basePresenter.updateInfoReq(record, DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD);

                                camLiveControlLayer.setFlipped(true);
                                ToastUtil.showToast(getString(R.string.SCENE_SAVED));
                                if (MiscUtils.isLand()) {
                                    ((BaseFullScreenFragmentActivity) getActivity())
                                            .showSystemBar(false, 500);
                                }
                            }, getString(R.string.CANCEL), (dialog, which) -> {
                                if (MiscUtils.isLand()) {
                                    ((BaseFullScreenFragmentActivity) getActivity())
                                            .showSystemBar(false, 500);
                                }
                            });
                } else {
                    safeIsOpen = device.$(ID_501_CAMERA_ALARM_FLAG, false);
                    if (safeIsOpen) {
                        AlertDialogManager.getInstance().showDialog(getActivity(), "safeIsOpen", getString(R.string.Detection_Pop),
                                getString(R.string.OK), (dialog, which) -> {
                                    DpMsgDefine.DPPrimary<Boolean> safe = new DpMsgDefine.DPPrimary<>(false);
                                    basePresenter.updateInfoReq(safe, ID_501_CAMERA_ALARM_FLAG);
                                    camLiveControlLayer.setFlipped(true);
                                    if (MiscUtils.isLand()) {
                                        ((BaseFullScreenFragmentActivity) getActivity())
                                                .showSystemBar(false, 500);
                                    }
                                }, getString(R.string.CANCEL), (dialog, which) -> {
                                    if (MiscUtils.isLand()) {
                                        ((BaseFullScreenFragmentActivity) getActivity())
                                                .showSystemBar(false, 500);
                                    }
                                }, false);
                    } else {
                        DpMsgDefine.DPPrimary<Boolean> safe = new DpMsgDefine.DPPrimary<>(true);
                        basePresenter.updateInfoReq(safe, ID_501_CAMERA_ALARM_FLAG);
                        camLiveControlLayer.setFlipped(false);
                    }
                }
            }
        });
        initTvTextClick();

        camLiveControlLayer.setOrientationHandle(eventListener::setRequestedOrientation);
    }

    /**
     * |直播|  按钮
     */
    private void initTvTextClick() {
        camLiveControlLayer.setLiveTextClick(v -> {
            CamLiveContract.LiveStream type = basePresenter.getLiveStream();
            if (type.type == TYPE_HISTORY && accept()) {
                type.type = TYPE_LIVE;
                basePresenter.updateLiveStream(type);
                AppLogger.i("TextView click start play!");
                basePresenter.startPlay();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("isResumed", "start isResumed: " + getUserVisibleHint());
        Device device = basePresenter.getDevice();
        camLiveControlLayer.onActivityStart(basePresenter, device);
    }

    @Override
    public void onPause() {
        super.onPause();
//        basePresenter.saveHotSeatState();
        enableSensor(false);
        if (basePresenter != null) {
            basePresenter.stopPlayVideo(true).subscribe(ret -> {
//                camLiveControlLayer.getLiveViewWithThumbnail().getVideoView().takeSnapshot(true);
            }, AppLogger::e);
        }
    }

//    @Override
//    public void onStop() {
//        super.onStop();
//        enableSensor(false);
//        if (basePresenter != null)
//            basePresenter.stopPlayVideo(true).subscribe(ret -> {
//            }, AppLogger::e);
//    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (basePresenter != null && isVisibleToUser && isResumed() && getActivity() != null) {
//            camLiveControlLayer.showUseCase();
            // TODO: 2017/8/16 直播页需要自动横屏了
            //直播成功之后，才触发sensor.
//            ViewUtils.setRequestedOrientation(getActivity(), ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            Device device = basePresenter.getDevice();
            DpMsgDefine.DPStandby standby = device.$(508, new DpMsgDefine.DPStandby());
            if (standby.standby) {
                return;
            }
            Bundle bundle = getArguments();
            if (getArguments().containsKey(JConstant.KEY_CAM_LIVE_PAGE_PLAY_HISTORY_TIME)) {
                long time = bundle.getLong(JConstant.KEY_CAM_LIVE_PAGE_PLAY_HISTORY_TIME);
                AppLogger.d("需要定位到时间轴:" + time);
                if (time == 0 && BuildConfig.DEBUG) {
                    throw new IllegalArgumentException("play history time is 0");
                }
                getArguments().remove(JConstant.KEY_CAM_LIVE_PAGE_PLAY_HISTORY_TIME);
                //满足条件才需要播放
                if (!judge()) {
                    return;
                }
                if (String.valueOf(time).length() != String.valueOf(System.currentTimeMillis()).length()) {
                    time = time * 1000L;//确保是毫秒
                }
                camLiveControlLayer.reAssembleHistory(basePresenter, time);
            }
//            basePresenter.startPlay();
        } else if (basePresenter != null && isResumed() && !isVisibleToUser) {
            basePresenter.stopPlayVideo(PLAY_STATE_STOP).subscribe(ret -> {
//                camLiveControlLayer.getLiveViewWithThumbnail().getVideoView().takeSnapshot(true);
            }, AppLogger::e);
            AppLogger.d("stop play");
        } else {
            AppLogger.d("not ready :" + "isResumed?" + isResumed());
        }
    }

    private boolean accept() {
        Intent intent = getActivity().getIntent();
        if (!judge() || intent != null && intent.hasExtra(JConstant.KEY_JUMP_TO_MESSAGE)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("isResumed", "isResumed: " + getUserVisibleHint());
        camLiveControlLayer.onActivityResume(basePresenter, BaseApplication.getAppComponent()
                .getSourceManager().getDevice(getUuid()), isUserVisible());
        if (basePresenter != null) {
            if (!judge() || basePresenter.getLiveStream().playState == PLAY_STATE_STOP) {
                return;//还没开始播放
            }
//            basePresenter.restoreHotSeatState();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        camLiveControlLayer.onLiveDestroy();
    }

    @Override
    public void onDeviceInfoChanged(JFGDPMsg msg) throws IOException {
        int msgId = (int) msg.id;
        if (msgId == DpMsgMap.ID_222_SDCARD_SUMMARY) {
            DpMsgDefine.DPSdcardSummary sdStatus = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPSdcardSummary.class);
            if (!JFGRules.hasSdcard(sdStatus)) {
                AppLogger.d("sdcard 被拔出");
                camLiveControlLayer.showPlayHistoryButton();
                if (!getUserVisibleHint() || basePresenter.isShareDevice()) {
                    AppLogger.d("隐藏了，sd卡更新");
                    return;
                }
                getAlertDialogManager().showDialog(getActivity(), getString(R.string.MSG_SD_OFF),
                        getString(R.string.MSG_SD_OFF),
                        getString(R.string.OK), (DialogInterface d, int which) -> {
                            if (basePresenter.getPlayState() != PLAY_STATE_PLAYING) {
                                if (accept()) {
                                    basePresenter.startPlay();
                                }
                            }

                        });
                if (basePresenter.getPlayType() == TYPE_HISTORY) {
                    basePresenter.stopPlayVideo(TYPE_HISTORY).subscribe(ret -> {
//                        camLiveControlLayer.getLiveViewWithThumbnail().getVideoView().takeSnapshot(true);
                    }, AppLogger::e);
                }
                AppLogger.e("sdcard数据被清空，唐宽，还没实现");
            }
        }

        if (msgId == DpMsgMap.ID_508_CAMERA_STANDBY_FLAG) {
            DpMsgDefine.DPStandby standby = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPStandby.class);
            if (standby != null && standby.standby) {
                basePresenter.stopPlayVideo(JFGRules.PlayErr.STOP_MAUNALLY)
                        .subscribe(ret -> {
//                            camLiveControlLayer.getLiveViewWithThumbnail().getVideoView().takeSnapshot(true);
                        }, AppLogger::e);
            } else {
                if (basePresenter.getLiveStream().playState != JConstant.PLAY_STATE_PLAYING) {
                    CamLiveContract.LiveStream stream = basePresenter.getLiveStream();
                    //恢复播放
                    if (accept()) { // 简化if else 代码 // modify lxh
                        if (stream.type == TYPE_LIVE) {
                            basePresenter.startPlay();
                        } else {
                            basePresenter.startPlayHistory(stream.time);
                        }
                    }
                }
            }
            camLiveControlLayer.onDeviceStandByChanged(basePresenter.getDevice(), v -> jump2Setting());
        }
        if (msgId == DpMsgMap.ID_218_DEVICE_FORMAT_SDCARD) {
            if (!getUserVisibleHint()) {
                AppLogger.d("隐藏了，sd卡被格式化");
                return;
            }
            if (basePresenter.getPlayType() != TYPE_HISTORY) {
                return;
            }
            getAlertDialogManager().showDialog(getActivity(), getString(R.string.Clear_Sdcard_tips6),
                    getString(R.string.Clear_Sdcard_tips6),
                    getString(R.string.OK), null,
                    getString(R.string.CANCEL), null);
        }
        if (msgId == 509) {
            Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(getUuid());
            String _509 = device.$(509, "1");
            if (device.pid == 39 || device.pid == 49) {
                _509 = "0";
            }
            camLiveControlLayer.updateLiveViewMode(_509);
        }
        camLiveControlLayer.dpUpdate(msg, getDevice());
    }


    @Override
    public void onLivePrepare(int type) {
        if (getView() != null) {
            getView().post(() -> camLiveControlLayer.onLivePrepared(type));
        }
    }

    private void enableSensor(boolean enable) {
        boolean autoRotateOn = (Settings.System.getInt(getContext().getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
        //检查系统是否开启自动旋转
        if (autoRotateOn && enable) {
            AppLogger.d("耗电大户");
            eventListener.enable();
        } else if (eventListener != null) {
            eventListener.disable();
        }
    }

    @Override
    public void onLiveStarted(int type) {
        enableSensor(true);
        if (getView() != null) {
            getView().setKeepScreenOn(true);
        }
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(getUuid());
        camLiveControlLayer.onLiveStart(basePresenter, device);
        camLiveControlLayer.setHotSeatListener(mic -> CameraLiveFragmentExPermissionsDispatcher.audioRecordPermissionGrant_MicWithCheck(this),
                speaker -> CameraLiveFragmentExPermissionsDispatcher.audioRecordPermissionGrant_SpeakerWithCheck(this),
                capture -> {
                    int vId = capture.getId();
                    switch (vId) {
                        case R.id.imgV_cam_trigger_capture:
                        case R.id.imgV_land_cam_trigger_capture:
                            if (MiscUtils.isLand() && camLiveControlLayer.isActionBarHide()) {
                                return;
                            }
                            if (camLiveControlLayer != null && camLiveControlLayer.getLiveViewWithThumbnail() != null &&
                                    camLiveControlLayer.getLiveViewWithThumbnail().getVideoView() != null) {
                                camLiveControlLayer.getLiveViewWithThumbnail().getVideoView()
                                        .takeSnapshot(true);
                            }
                            PerformanceUtils.startTrace("takeShotFromLocalView");
//                            basePresenter.takeSnapShot(true);
                            break;
                    }
                });
        camLiveControlLayer.setPlayBtnListener(v -> {
            if (MiscUtils.isLand() && camLiveControlLayer.isActionBarHide()) {
                return;
            }
            CamLiveContract.LiveStream prePlayType = basePresenter.getLiveStream();
            if (prePlayType.playState == PLAY_STATE_PLAYING) {
                // 暂停
                basePresenter.stopPlayVideo(STOP_MAUNALLY).subscribe(ret -> {
//                    camLiveControlLayer.getLiveViewWithThumbnail().getVideoView().takeSnapshot(true);
                }, AppLogger::e);
                ((ImageView) v).setImageResource(R.drawable.icon_landscape_stop);
            } else {
                AppLogger.i("start play!!");
                if (prePlayType.type == TYPE_HISTORY) {
                    basePresenter.startPlayHistory(prePlayType.time * 1000L);
                } else {
                    basePresenter.startPlay();
                }
                ((ImageView) v).setImageResource(R.drawable.icon_landscape_playing);
            }
        });

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(getUuid());
        camLiveControlLayer.orientationChanged(basePresenter, device, this.getResources().getConfiguration().orientation);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CameraLiveFragmentExPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        if (permissions.length == 1) {
            AppLogger.d("permission:" + permissions + " " + grantResults);
        }
    }

    private void jump2Setting() {
        //跳转到...
        Intent intent = new Intent(getContext(), CamSettingActivity.class);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, getUuid());
        ((Activity) getContext()).startActivityForResult(intent, REQUEST_CODE,
                ActivityOptionsCompat.makeCustomAnimation(getActivity(),
                        R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
        AppLogger.d("跳转到使用帮助");
    }

    /**
     * 0:off-disable,1.on-disable,2.off-enable,3.on-enable
     */
    @Override
    public boolean isLocalMicOn() {
        return camLiveControlLayer.getMicState() == 3 ||
                camLiveControlLayer.getMicState() == 1;
    }

    /**
     * 0:off-disable,1.on-disable,2.off-enable,3.on-enable
     */
    @Override
    public boolean isLocalSpeakerOn() {
        return camLiveControlLayer.getSpeakerState() == 3
                || camLiveControlLayer.getSpeakerState() == 1;
    }


    @Override
    public boolean judge() {
        //待机模式
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(getUuid());
        camLiveControlLayer.onDeviceStandByChanged(device, v -> jump2Setting());
        if (basePresenter.isDeviceStandby()) {
            return false;
        }
        //全景,首次使用模式
        boolean sightShow = PreferencesUtils.getBoolean(KEY_CAM_SIGHT_SETTING + getUuid(), false);
        if (sightShow) {
            return false;
        }
        //手机数据
//        if (NetUtils.getJfgNetType() == 2 && !ALLOW_PLAY_WITH_MOBILE_NET) {
//            ALLOW_PLAY_WITH_MOBILE_NET = true;
//            //显示遮罩层
//            camLiveControlLayer.showMobileDataCover(basePresenter);
//            return false;
//        }
        return true;
    }

    @Override
    public void onHistoryDataRsp(IData dataStack) {
        camLiveControlLayer.onHistoryDataRsp(basePresenter);
    }

    @Override
    public void onLiveStop(int playType, int errId) {
        enableSensor(false);
        if (!isAdded()) {
            return;
        }
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(getUuid());
        if (getView() != null) {
            getView().postDelayed(() -> {
                if (getView() != null) {
                    getView().setKeepScreenOn(false);
                }
                camLiveControlLayer.onLiveStop(basePresenter, device, errId);
            }, 500);
        }
    }

    @Override
    public void onTakeSnapShot(Bitmap bitmap) {
        if (bitmap == null) {
            if (getView() != null) {
                getView().post(() -> ToastUtil.showNegativeToast(getString(R.string.set_failed)));
            }
            return;
        }
        if (MiscUtils.isLand()) {
            ToastUtil.showNegativeToast(getString(R.string.SAVED_PHOTOS));
            return;//横屏 不需要弹窗.
        }
        PerformanceUtils.startTrace("takeSnapShot_pre");
        camLiveControlLayer.post(() -> camLiveControlLayer.onCaptureRsp(getActivity(), bitmap));
        PerformanceUtils.stopTrace("takeSnapShot_pre");
        PerformanceUtils.stopTrace("takeSnapShot");
    }


    @Override
    public void onPreviewResourceReady(Bitmap bitmap) {
        //手动暂停时,需要加载
        camLiveControlLayer.onLoadPreviewBitmap(bitmap);
    }


    @Override
    public void onHistoryLiveStop(int state) {

    }

    @Override
    public void shouldWaitFor(boolean start) {
        if (getView() != null) {
            getView().post(() -> {
                if (start) {
                    //停止声音
                    camLiveControlLayer.startBadFrame();
                } else {
                    //恢复按钮状态
                    camLiveControlLayer.resumeGoodFrame();
                }
            });
        }
    }

    @Override
    public void showFirmwareDialog() {
        getAlertDialogManager().showDialog(getActivity(),
                getString(R.string.Tap1_Device_UpgradeTips), getString(R.string.Tap1_Device_UpgradeTips),
                getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    Intent intent = new Intent(getActivity(), FirmwareUpdateActivity.class);
                    intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, getUuid());
                    startActivity(intent);
                }, getString(R.string.CANCEL), null);
    }

    @Override
    public void onRtcp(JFGMsgVideoRtcp rtcp, boolean ignoreTimeStamp) {
        AppLogger.d("onRtcp: " + new Gson().toJson(rtcp));
        camLiveControlLayer.onRtcpCallback(basePresenter.getPlayType(), rtcp, false);
    }

    @Override
    public void onResolution(JFGMsgVideoResolution resolution) throws JfgException {
        camLiveControlLayer.onResolutionRsp(resolution);
        camLiveControlLayer.updateLiveRect(mLiveViewRectInWindow);
    }

    @Override
    public void setPresenter(CamLiveContract.Presenter basePresenter) {
        this.basePresenter = basePresenter;
    }

    @NeedsPermission({Manifest.permission.RECORD_AUDIO})
    public void audioRecordPermissionGrant_Speaker() {
        if (basePresenter.getPlayState() == PLAY_STATE_PLAYING) {
            basePresenter.switchSpeaker();
        }
    }

    @NeedsPermission({Manifest.permission.RECORD_AUDIO})
    public void audioRecordPermissionGrant_Mic() {
        if (basePresenter.getPlayState() == PLAY_STATE_PLAYING) {
            basePresenter.switchMic();
        }
    }

    @NeedsPermission({Manifest.permission.RECORD_AUDIO})
    public void audioRecordPermissionGrantProgramCheck() {

    }

    @Override
    @OnPermissionDenied({Manifest.permission.RECORD_AUDIO})
    public void audioRecordPermissionDenied() {
        if (!isAdded() || getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        getView().post(() -> getAlertDialogManager().showDialog(getActivity(),
                "RECORD_AUDIO", getString(R.string.permission_auth, getString(R.string.sound_auth)),
                getString(R.string.OK), null));
    }

    @OnNeverAskAgain({Manifest.permission.RECORD_AUDIO})
    public void audioRecordPermissionNeverAsk() {
        audioRecordPermissionDenied();
    }

    @OnShowRationale({Manifest.permission.RECORD_AUDIO})
    public void audioRecordPermissionRational(PermissionRequest request) {
        audioRecordPermissionDenied();
    }

    @Override
    public void onNetworkChanged(boolean connected) {
        camLiveControlLayer.onNetworkChanged(basePresenter, connected);
    }

    @Override
    public boolean isUserVisible() {
        return getUserVisibleHint();
    }

    @Override
    public void switchHotSeat(boolean speaker, boolean speakerEnable,
                              boolean mic, boolean micEnable,
                              boolean capture, boolean captureEnable) {
        camLiveControlLayer.postDelayed(() -> camLiveControlLayer.setHotSeatState(
                basePresenter.getPlayType(), speaker, speakerEnable, mic, micEnable, capture, captureEnable), 100);
    }

    @Override
    public void onAudioPermissionCheck() {
        if (!isAdded()) {
            return;
        }
//        CameraLiveFragmentExPermissionsDispatcher.audioRecordPermissionGrantProgramCheckWithCheck(this);
    }

    @Override
    public void onBatteryDrainOut() {
        //当前页面才显示
        if (!isAdded() || !isUserVisible()) {
            return;
        }
        Device device = DataSourceManager.getInstance().getDevice(getUuid());
        if (device.available() && JFGRules.hasBatteryNotify(device.pid)) {
            AlertDialogManager.getInstance().showDialog(getActivity(),
                    "onBatteryDrainOut", getString(R.string.Tap1_LowPower),
                    getString(R.string.OK), null, false);
        }
    }

    @Override
    public void onHistoryLoadFinished() {
        if (getUserVisibleHint() && isResumed() && getActivity() != null) {
            //这里是个异步的,显示的条件是当前 fragment 可见
            AppLogger.d(" //这里是个异步的,显示的条件是当前 fragment 可见");
            if (!MiscUtils.isLand()) {
                LiveShowCase.showHistoryWheelCase(getActivity(), camLiveControlLayer.findViewById(R.id.layout_e));
                LiveShowCase.showHistoryCase((Activity) getContext(), camLiveControlLayer.findViewById(R.id.imgV_cam_zoom_to_full_screen));
            }
//            basePresenter.startPlay();//加载完成,不需要播放了.
        }
    }

    @Override
    public void onDeviceUnBind() {
        AppLogger.d("当前设备已解绑");
        basePresenter.stopPlayVideo(STOP_MAUNALLY).subscribe(ret -> {
//            camLiveControlLayer.getLiveViewWithThumbnail().getVideoView().takeSnapshot(true);
        }, AppLogger::e);
        AlertDialogManager.getInstance().showDialog(getActivity(), getString(R.string.Tap1_device_deleted), getString(R.string.Tap1_device_deleted),
                getString(R.string.OK), (dialog, which) -> {
                    getActivity().finish();
                    Intent intent = new Intent(getContext(), NewHomeActivity.class);
                    startActivity(intent);
                }, false);
    }

    public void removeVideoView() {
        if (!isUserVisible() && camLiveControlLayer != null)//可以解决退出activity,TransitionAnimation，出现黑屏
        {
            camLiveControlLayer.removeAllViews();
        }
    }


    public boolean onBackPressed() {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            this.eventListener.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, true);
        } else {

            AppLogger.d("用户按下了返回键,需要手动停止播放直播,Bug:Android 7.0 以上 onStop 延迟调用");
            basePresenter.stopPlayVideo(true).subscribe(ret -> {
//            camLiveControlLayer.getLiveViewWithThumbnail().getVideoView().takeSnapshot(true);
            }, AppLogger::e);
        }
        return false;
    }

//    @Override
//    public void onHistoryDateListUpdate(ArrayList<Long> dateList) {
//
//    }


    class MyEventListener extends com.cylan.jiafeigou.misc.OrientationListener {

        private boolean isShake = false;

        private volatile int orientation = -1;

        private int customOrientation = -1;

        public int getOrientation() {
            return orientation;
        }

        public int getCustomOrientation() {
            return customOrientation;
        }

        public void setCustomOrientation(int customOrientation) {
            this.customOrientation = customOrientation;
        }

        public MyEventListener(Context context) {
            super(context);
        }

        public MyEventListener(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onSensorChanged(int sensor, float[] values) {
            super.onSensorChanged(sensor, values);

            float x = values[0];
            float y = values[1];
            float z = values[2];
            if ((Math.abs(x) > 17 || Math.abs(y) > 17 || Math
                    .abs(z) > 17) && !isShake) {
                // TODO: 2016/10/19 实现摇动逻辑, 摇动后进行震动
                if (basePresenter != null && isUserVisible() && isResumed() && getActivity() != null && basePresenter.getPlayState() == PLAY_STATE_PLAYING) {
                    if (camLiveControlLayer.isShakeEnable()) {
                        isShake = true;
                        camLiveControlLayer.onShake();
                        camLiveControlLayer.postDelayed(() -> {
                            // TODO: 2017/8/31 摇一摇后重置 customOrientation
                            isShake = false;
                            customOrientation = -1;
                        }, 2000);//2秒只允许摇一摇一次
                    }


                }
            }
        }


        @Override
        public void onOrientationChanged(int orientation) {

            // TODO: 2017/8/30 只能从一个方向旋转到另一个方向,不能从一个方向旋转回自己的方向

            if (((orientation >= 0) && (orientation < 45)) || (orientation > 315)) {//设置竖屏
//                    Log.d(TAG, "设置竖屏");
                this.orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

            } else if (orientation > 225 && orientation < 315) { //设置横屏
//                Log.d(TAG, "设置横屏");
                this.orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

            } else if (orientation > 45 && orientation < 135) {// 设置反向横屏
//                Log.d(TAG, "反向横屏");
                this.orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;

            } else if (orientation > 135 && orientation < 225) {
                this.orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
//                Log.d(TAG, "反向竖屏");
            }

            if (isShake) {
                return;
            }

            if (basePresenter != null && isUserVisible() && isResumed() && getActivity() != null && basePresenter.getPlayState() == PLAY_STATE_PLAYING) {
                // TODO: 2017/8/24 摇一摇开启后不允许自动转屏
                if (!camLiveControlLayer.isShakeEnable()) {
                    setRequestedOrientation(this.orientation, false);
                }
            }

        }

        public void setRequestedOrientation(int requestedOrientation, boolean fromUser) {
            if (fromUser) {
                customOrientation = orientation;
                ViewUtils.setRequestedOrientation(getActivity(), requestedOrientation);
            } else {


                if (customOrientation != requestedOrientation) {
                    customOrientation = -1;

                    if (requestedOrientation != getActivity().getRequestedOrientation()) {
                        ViewUtils.setRequestedOrientation(getActivity(), requestedOrientation);
                    }

                }


            }

        }

    }
}