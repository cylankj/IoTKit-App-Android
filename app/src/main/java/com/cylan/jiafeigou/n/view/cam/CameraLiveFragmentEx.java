package com.cylan.jiafeigou.n.view.cam;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamLivePresenterImpl;
import com.cylan.jiafeigou.n.view.activity.CamSettingActivity;
import com.cylan.jiafeigou.n.view.firmware.FirmwareUpdateActivity;
import com.cylan.jiafeigou.n.view.mine.HomeMineHelpFragment;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
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
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PREPARE;
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
                        if (prePlayType.type == TYPE_HISTORY) {
                            if (accept()) {
                                basePresenter.startPlayHistory(prePlayType.time * 1000L);
                            }
                        } else if (prePlayType.type == TYPE_LIVE) {
                            if (accept()) {
                                basePresenter.startPlay();
                            }
                        }
                        break;
                    case PLAY_STATE_PLAYING:
                        //下一步stop
                        basePresenter.stopPlayVideo(STOP_MAUNALLY).subscribe(ret -> {
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
                Bundle bundle = new Bundle();
                bundle.putString(JConstant.KEY_SHOW_SUGGESTION, JConstant.KEY_SHOW_SUGGESTION);
                ActivityUtils.addFragmentSlideInFromRight(((AppCompatActivity) getContext()).getSupportFragmentManager(),
                        HomeMineHelpFragment.newInstance(bundle),
                        android.R.id.content);
            }
        });
        camLiveControlLayer.setFlipListener(new FlipImageView.FlipListener() {
            @Override
            public void onClick(FlipImageView view) {
                Device device = basePresenter.getDevice();
                DpMsgDefine.DPSdStatus dpSdStatus = device.$(204, new DpMsgDefine.DPSdStatus());
                int oldOption = device.$(ID_303_DEVICE_AUTO_VIDEO_RECORD, -1);
                boolean safeIsOpen = device.$(ID_501_CAMERA_ALARM_FLAG, false);
                //无卡不需要显示
                if (oldOption == 0 && safeIsOpen && dpSdStatus.hasSdcard && dpSdStatus.err == 0) {
                    AlertDialogManager.getInstance().showDialog(getActivity(),
                            getString(R.string.Tap1_Camera_MotionDetection_OffTips),
                            getString(R.string.Tap1_Camera_MotionDetection_OffTips),
                            getString(R.string.CARRY_ON), (DialogInterface dialog, int which) -> {
                                DpMsgDefine.DPPrimary<Boolean> wFlag = new DpMsgDefine.DPPrimary<>();
                                wFlag.value = false;
                                basePresenter.updateInfoReq(wFlag, DpMsgMap.ID_501_CAMERA_ALARM_FLAG);
                                camLiveControlLayer.setFlipped(true);
                                ToastUtil.showToast(getString(R.string.SCENE_SAVED));
                            }, getString(R.string.CANCEL), null);
                } else {
                    safeIsOpen = device.$(ID_501_CAMERA_ALARM_FLAG, false);
                    DpMsgDefine.DPPrimary<Boolean> safe = new DpMsgDefine.DPPrimary<>(!safeIsOpen);
                    basePresenter.updateInfoReq(safe, ID_501_CAMERA_ALARM_FLAG);
                    camLiveControlLayer.setFlipped(safeIsOpen);
                }
            }
        });
        initTvTextClick();
    }

    /**
     * |直播|  按钮
     */
    private void initTvTextClick() {
        camLiveControlLayer.setLiveTextClick(v -> {
            CamLiveContract.LiveStream type = basePresenter.getLiveStream();
            if (type.type == TYPE_HISTORY) {
                type.type = TYPE_LIVE;
                basePresenter.updateLiveStream(type);
                if (accept()) {
                    basePresenter.startPlay();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("isResumed", "start isResumed: " + getUserVisibleHint());
        Device device = basePresenter.getDevice();
        camLiveControlLayer.onActivityStart(basePresenter, device);
        //不需要自动播放了
        if (judge()) {
            //显示按钮
        }
        //        basePresenter.startPlay();
        if (getUserVisibleHint())
            camLiveControlLayer.showUseCase();
    }

    @Override
    public void onPause() {
        super.onPause();
        basePresenter.saveHotSeatState();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (basePresenter != null)
            basePresenter.stopPlayVideo(true).subscribe(ret -> {
            }, AppLogger::e);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (basePresenter != null && isVisibleToUser && isResumed() && getActivity() != null) {
            camLiveControlLayer.showUseCase();
            Device device = basePresenter.getDevice();
            DpMsgDefine.DPStandby standby = device.$(508, new DpMsgDefine.DPStandby());
            if (standby.standby) return;
            Bundle bundle = getArguments();
            if (getArguments().containsKey(JConstant.KEY_CAM_LIVE_PAGE_PLAY_HISTORY_TIME)) {
                long time = bundle.getLong(JConstant.KEY_CAM_LIVE_PAGE_PLAY_HISTORY_TIME);
                AppLogger.d("需要定位到时间轴");
                if (time == 0 && BuildConfig.DEBUG)
                    throw new IllegalArgumentException("play history time is 0");
                getArguments().remove(JConstant.KEY_CAM_LIVE_PAGE_PLAY_HISTORY_TIME);
                //满足条件才需要播放
                if (!judge())
                    return;
                if (String.valueOf(time).length() != String.valueOf(System.currentTimeMillis()).length()) {
                    time = time * 1000L;//确保是毫秒
                }
                camLiveControlLayer.reAssembleHistory(basePresenter, time);
            }
//            basePresenter.startPlay();
        } else if (basePresenter != null && isResumed() && !isVisibleToUser) {
            basePresenter.stopPlayVideo(PLAY_STATE_STOP).subscribe(ret -> {
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
        } else return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("isResumed", "isResumed: " + getUserVisibleHint());
        camLiveControlLayer.onActivityResume(basePresenter, BaseApplication.getAppComponent()
                .getSourceManager().getDevice(getUuid()));
        if (basePresenter != null) {
            if (!judge() || basePresenter.getLiveStream().playState == PLAY_STATE_STOP)
                return;//还没开始播放
            basePresenter.restoreHotSeatState();
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
            if (sdStatus == null) sdStatus = new DpMsgDefine.DPSdcardSummary();
            if (!sdStatus.hasSdcard) {
                AppLogger.d("sdcard 被拔出");
                if (!getUserVisibleHint() || basePresenter.isShareDevice()) {
                    AppLogger.d("隐藏了，sd卡更新");
                    return;
                }
                getAlertDialogManager().showDialog(getActivity(), getString(R.string.MSG_SD_OFF),
                        getString(R.string.MSG_SD_OFF),
                        getString(R.string.OK), (DialogInterface d, int which) -> {
                            if (basePresenter.getPlayState() != PLAY_STATE_PLAYING)
                                if (accept()) {
                                    basePresenter.startPlay();
                                }
                        });
                if (basePresenter.getPlayType() == TYPE_HISTORY) {
                    basePresenter.stopPlayVideo(TYPE_HISTORY).subscribe(ret -> {
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
                        }, AppLogger::e);
            } else {
                if (basePresenter.getLiveStream().playState != JConstant.PLAY_STATE_PLAYING) {
                    CamLiveContract.LiveStream stream = basePresenter.getLiveStream();
                    //恢复播放
                    if (stream.type == TYPE_HISTORY) {
                        if (accept()) {
                            basePresenter.startPlayHistory(stream.time);
                        }
                    } else {
                        if (accept()) {
                            basePresenter.startPlay();
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
            if (basePresenter.getPlayType() != TYPE_HISTORY)
                return;
            getAlertDialogManager().showDialog(getActivity(), getString(R.string.Clear_Sdcard_tips6),
                    getString(R.string.Clear_Sdcard_tips6),
                    getString(R.string.OK), null,
                    getString(R.string.CANCEL), null);
        }
        if (msgId == 509) {
            Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(getUuid());
            String _509 = device.$(509, "1");
            camLiveControlLayer.updateLiveViewMode(_509);
        }
    }


    @Override
    public void onLivePrepare(int type) {
        if (getView() != null) getView().post(() -> camLiveControlLayer.onLivePrepared(type));
    }

    @Override
    public void onLiveStarted(int type) {
        if (getView() != null) getView().setKeepScreenOn(true);
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(getUuid());
        camLiveControlLayer.onLiveStart(basePresenter, device);
        camLiveControlLayer.setHotSeatListener(mic -> CameraLiveFragmentExPermissionsDispatcher.audioRecordPermissionGrant_MicWithCheck(this),
                speaker -> CameraLiveFragmentExPermissionsDispatcher.audioRecordPermissionGrant_SpeakerWithCheck(this),
                capture -> {
                    int vId = capture.getId();
                    switch (vId) {
                        case R.id.imgV_cam_trigger_capture:
                        case R.id.imgV_land_cam_trigger_capture:
                            basePresenter.takeSnapShot(true);
                            break;
                    }
                });
        camLiveControlLayer.setPlayBtnListener(v -> {
            CamLiveContract.LiveStream prePlayType = basePresenter.getLiveStream();
            if (prePlayType.type == TYPE_LIVE) return;
            if (basePresenter.getPlayState() == PLAY_STATE_PREPARE)
                return;
            if (basePresenter.getPlayState() == PLAY_STATE_PLAYING) {
                basePresenter.stopPlayVideo(STOP_MAUNALLY).subscribe(ret -> {
                }, AppLogger::e);
                ((ImageView) v).setImageResource(R.drawable.icon_landscape_stop);
                camLiveControlLayer.setLoadingState(PLAY_STATE_STOP, null);
            } else if (prePlayType.type == TYPE_HISTORY) {
                if (accept()) {
                    basePresenter.startPlayHistory(prePlayType.time * 1000L);
                    ((ImageView) v).setImageResource(R.drawable.icon_landscape_playing);
                }
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
        if (basePresenter.isDeviceStandby()) {
            Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(getUuid());
            camLiveControlLayer.onDeviceStandByChanged(device, v -> jump2Setting());
            return false;
        }
        //全景,首次使用模式
        boolean sightShow = PreferencesUtils.getBoolean(KEY_CAM_SIGHT_SETTING + getUuid(), false);
        if (sightShow)
            return false;
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
        if (!isAdded()) return;
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(getUuid());
        if (getView() != null)
            getView().postDelayed(() -> {
                if (getView() != null)
                    getView().setKeepScreenOn(false);
                camLiveControlLayer.onLiveStop(basePresenter, device, errId);
            }, 500);
    }

    @Override
    public void onTakeSnapShot(Bitmap bitmap) {
        if (bitmap == null) {
            if (getView() != null)
                getView().post(() -> ToastUtil.showNegativeToast(getString(R.string.set_failed)));
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
        if (getView() != null) getView().post(() -> {
            if (start) {
                //停止声音
                camLiveControlLayer.startBadFrame();
            } else {
                //恢复按钮状态
                camLiveControlLayer.resumeGoodFrame();
            }
        });
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
    public void onRtcp(JFGMsgVideoRtcp rtcp) {
        Log.d("onRtcp", "onRtcp: " + new Gson().toJson(rtcp));
        camLiveControlLayer.onRtcpCallback(basePresenter.getPlayType(), rtcp);
    }

    @Override
    public void onResolution(JFGMsgVideoResolution resolution) throws JfgException {
        camLiveControlLayer.onResolutionRsp(resolution);
    }

    @Override
    public void setPresenter(CamLiveContract.Presenter basePresenter) {
        this.basePresenter = basePresenter;
    }

    @NeedsPermission({Manifest.permission.RECORD_AUDIO})
    public void audioRecordPermissionGrant_Speaker() {
        basePresenter.switchSpeaker();
    }

    @NeedsPermission({Manifest.permission.RECORD_AUDIO})
    public void audioRecordPermissionGrant_Mic() {
        basePresenter.switchMic();
    }

    @NeedsPermission({Manifest.permission.RECORD_AUDIO})
    public void audioRecordPermissionGrantProgramCheck() {

    }

    @OnPermissionDenied({Manifest.permission.RECORD_AUDIO})
    public void audioRecordPermissionDenied() {
        if (!isAdded() || getActivity() == null || getActivity().isFinishing()) return;
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
        if (!isAdded()) return;
//        CameraLiveFragmentExPermissionsDispatcher.audioRecordPermissionGrantProgramCheckWithCheck(this);
    }

    @Override
    public void onBatteryDrainOut() {
        if (!isAdded()) return;
        AlertDialogManager.getInstance().showDialog(getActivity(),
                "onBatteryDrainOut", getString(R.string.Tap1_LowPower),
                getString(R.string.OK), null, false);
    }


//    @Override
//    public void onHistoryDateListUpdate(ArrayList<Long> dateList) {
//
//    }
}