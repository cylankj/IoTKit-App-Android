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
import android.support.v7.app.AlertDialog;
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
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamLivePresenterImpl;
import com.cylan.jiafeigou.n.view.activity.CamSettingActivity;
import com.cylan.jiafeigou.n.view.mine.HomeMineHelpFragment;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.flip.FlipImageView;
import com.cylan.jiafeigou.widget.live.ILiveControl;
import com.cylan.jiafeigou.widget.wheel.ex.IData;
import com.google.gson.Gson;

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
import rx.android.schedulers.AndroidSchedulers;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_501_CAMERA_ALARM_FLAG;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_IDLE;
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
    private String uuid;
    private boolean isNormalView;
    private SoftReference<AlertDialog> sdcardPulloutDlg;
    private SoftReference<AlertDialog> sdcardFormatDlg;

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
        camLiveControlLayer.initView(basePresenter, uuid);
        camLiveControlLayer.initLiveViewRect(isNormalView ? basePresenter.getVideoPortHeightRatio() : 1.0f, mLiveViewRectInWindow);
        camLiveControlLayer.setLoadingRectAction(new ILiveControl.Action() {
            @Override
            public void clickImage(View view, int state) {
                switch (state) {
                    case PLAY_STATE_LOADING_FAILED:
                    case PLAY_STATE_STOP:
                        //下一步playing
                        CamLiveContract.PrePlayType type = basePresenter.getPrePlayType();
                        if (type.type == TYPE_LIVE) {
                            //不会发生这一幕的.
                            basePresenter.startPlayLive();
                        } else if (type.type == TYPE_HISTORY) {
                            basePresenter.startPlayHistory(type.time * 1000L);
                        }
                        break;
                    case PLAY_STATE_PLAYING:
                        //下一步stop
                        basePresenter.stopPlayVideo(STOP_MAUNALLY);
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
                    new android.app.AlertDialog.Builder(getActivity())
                            .setMessage(getString(R.string.Tap1_Camera_MotionDetection_OffTips))
                            .setPositiveButton(getString(R.string.CARRY_ON), (DialogInterface dialog, int which) -> {
                                DpMsgDefine.DPPrimary<Boolean> wFlag = new DpMsgDefine.DPPrimary<>();
                                wFlag.value = false;
                                basePresenter.updateInfoReq(wFlag, DpMsgMap.ID_501_CAMERA_ALARM_FLAG);
                                camLiveControlLayer.setFlipped(true);
                                ToastUtil.showToast(getString(R.string.SCENE_SAVED));
                            })
                            .setNegativeButton(getString(R.string.CANCEL), null)
                            .show();
                } else {
                    safeIsOpen = device.$(ID_501_CAMERA_ALARM_FLAG, false);
                    DpMsgDefine.DPPrimary<Boolean> safe = new DpMsgDefine.DPPrimary<>(!safeIsOpen);
                    basePresenter.updateInfoReq(safe, ID_501_CAMERA_ALARM_FLAG);
                    camLiveControlLayer.setFlipped(safeIsOpen);
                }
            }
        });
        initCaptureListener();
        initTvTextClick();
    }

    /**
     * |直播|  按钮
     */
    private void initTvTextClick() {
        camLiveControlLayer.setLiveTextClick(v -> {
            CamLiveContract.PrePlayType type = basePresenter.getPrePlayType();
            if (type.type == TYPE_HISTORY) {
                basePresenter.startPlayLive();
            }
        });
    }

    /**
     * 截图按钮
     */
    private void initCaptureListener() {
        camLiveControlLayer.setCaptureListener(v -> {
            int vId = v.getId();
            switch (vId) {
                case R.id.imgV_cam_trigger_capture:
                    basePresenter.takeSnapShot(false);
                    break;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Device device = basePresenter.getDevice();
        DpMsgDefine.DPStandby standby = device.$(508, new DpMsgDefine.DPStandby());
        if (!standby.standby) {
            //开始直播
            CamLiveContract.PrePlayType type = basePresenter.getPrePlayType();
            if (type.type == TYPE_LIVE)
                basePresenter.startPlayLive();
            else if (type.type == TYPE_HISTORY) {
                basePresenter.startPlayHistory(type.time * 1000L);
            }
        } else {
            //show
        }
        camLiveControlLayer.onDeviceStandByChanged(device, v -> jump2Setting());
        camLiveControlLayer.onActivityStart(device);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (basePresenter != null)
            basePresenter.stopPlayVideo(true);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (basePresenter != null && isVisibleToUser && isResumed() && getActivity() != null) {
            Device device = basePresenter.getDevice();
            DpMsgDefine.DPStandby standby = device.$(508, new DpMsgDefine.DPStandby());
            if (standby.standby) return;
            Bundle bundle = getArguments();
            if (getArguments().containsKey(JConstant.KEY_CAM_LIVE_PAGE_PLAY_HISTORY_TIME)) {
                long time = bundle.getLong(JConstant.KEY_CAM_LIVE_PAGE_PLAY_HISTORY_TIME);
                if (time == 0 && BuildConfig.DEBUG)
                    throw new IllegalArgumentException("play history time is 0");
//                startLiveHistory(time);
                AppLogger.e("历史录像");
                getArguments().remove(JConstant.KEY_CAM_LIVE_PAGE_PLAY_HISTORY_TIME);
                return;
            }
            CamLiveContract.PrePlayType prePlayType = basePresenter.getPrePlayType();
            if (prePlayType.type == TYPE_LIVE) {
                basePresenter.startPlayLive();
            } else if (prePlayType.type == TYPE_HISTORY) {
                basePresenter.startPlayHistory(prePlayType.time * 1000L);
            }
        } else if (basePresenter != null && isResumed() && !isVisibleToUser) {
            basePresenter.stopPlayVideo(PLAY_STATE_IDLE);
            AppLogger.d("stop play");
        } else {
            AppLogger.d("not ready ");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (basePresenter != null) {
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        camLiveControlLayer.onLiveDestroy();
    }


    private void initSdcardStateDialog() {
        if (sdcardPulloutDlg == null || sdcardPulloutDlg.get() == null) {
            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.MSG_SD_OFF))
                    .setPositiveButton(getString(R.string.OK), (DialogInterface d, int which) -> {
                        if (basePresenter.getPlayState() != PLAY_STATE_PLAYING)
                            basePresenter.startPlayLive();
                    })
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

    @Override
    public void onDeviceInfoChanged(JFGDPMsg msg) throws IOException {
        int msgId = (int) msg.id;
        if (msgId == DpMsgMap.ID_222_SDCARD_SUMMARY) {
            DpMsgDefine.DPSdcardSummary sdStatus = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPSdcardSummary.class);
            if (sdStatus == null) sdStatus = new DpMsgDefine.DPSdcardSummary();
            if (!sdStatus.hasSdcard) {
                AppLogger.d("sdcard 被拔出");
                if (sdcardPulloutDlg != null && sdcardPulloutDlg.get() != null && sdcardPulloutDlg.get().isShowing())
                    return;
                if (!getUserVisibleHint()) {
                    AppLogger.d("隐藏了，sd卡更新");
                    return;
                }
                initSdcardStateDialog();
                sdcardPulloutDlg.get().show();
                if (basePresenter.getPlayType() == TYPE_HISTORY) {
                    basePresenter.stopPlayVideo(TYPE_HISTORY);
                }
            }
            AppLogger.e("sdcard数据被清空，唐宽，还没实现");
        }
        if (msgId == DpMsgMap.ID_508_CAMERA_STANDBY_FLAG) {
            camLiveControlLayer.onDeviceStandByChanged(basePresenter.getDevice(), v -> jump2Setting());
        }
        if (msgId == DpMsgMap.ID_218_DEVICE_FORMAT_SDCARD) {
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
            initSdcardFormatDialog();
        }
        if (msgId == 509) {
            Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
            String _509 = device.$(509, "0");
            camLiveControlLayer.updateLiveViewMode(_509);
        }
    }


    @Override
    public void onLivePrepare(int type) {
        camLiveControlLayer.onLivePrepared();
    }

    @Override
    public void onLiveStarted(int type) {
        if (getView() != null) getView().setKeepScreenOn(true);
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        camLiveControlLayer.onLiveStart(basePresenter, device);
        camLiveControlLayer.setMicSpeakerListener(mic -> {
            int tag = camLiveControlLayer.getMicState();
            handleSwitchMic(tag);
        }, speaker -> {
            int tag = camLiveControlLayer.getSpeakerState();
            handleSwitchSpeaker(tag);
        });
        camLiveControlLayer.setPlayBtnListener(v -> {
            CamLiveContract.PrePlayType prePlayType = basePresenter.getPrePlayType();
            if (prePlayType.type == TYPE_LIVE) return;
            if (basePresenter.getPlayState() == PLAY_STATE_PREPARE)
                return;
            if (basePresenter.getPlayState() == PLAY_STATE_PLAYING) {
                basePresenter.stopPlayVideo(STOP_MAUNALLY);
                ((ImageView) v).setImageResource(R.drawable.icon_landscape_stop);
                camLiveControlLayer.setLoadingState(PLAY_STATE_STOP, null);
            } else if (prePlayType.type == TYPE_HISTORY) {
                basePresenter.startPlayHistory(prePlayType.time * 1000L);
                ((ImageView) v).setImageResource(R.drawable.icon_landscape_playing);
            }
        });
    }

    /**
     * 没有0,1两种状态
     * 0:off-disable,1.on-disable,2.off-enable,3.on-enable
     *
     * @param tag 2: 3:
     */
    private void handleSwitchMic(int tag) {
        basePresenter.switchMic()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    Log.d("handleSwitchMic", "handleSwitchMic:" + tag);
                    //表示设置结果,设置成功才需要改变view 图标
                    if (ret) {
                        //设置成功,更新下一状态
                        camLiveControlLayer.setMicSpeakerState(tag == 2 ? 3 : 2,
                                tag == 2 ? 3 : camLiveControlLayer.getSpeakerState());
                    } else {
                    }
                }, AppLogger::e);
    }

    /**
     * 没有0,1两种状态
     * 0:off-disable,1.on-disable,2.off-enable,3.on-enable
     *
     * @param tag 2: 3:
     */
    private void handleSwitchSpeaker(int tag) {
        basePresenter.switchSpeaker()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    Log.d("handleSwitchSpeaker", "handleSwitchSpeaker:" + tag);
                    //表示设置结果,设置成功才需要改变view 图标
                    if (ret) {
                        //设置成功,更新下一状态
                        int mic = basePresenter.getPlayType() == TYPE_HISTORY ? 0 : camLiveControlLayer.getMicState();
                        camLiveControlLayer.setMicSpeakerState(mic,
                                tag == 2 ? 3 : 2);
                    } else {
                    }
                }, AppLogger::e);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
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
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
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
        return camLiveControlLayer.getMicState() == 3;
    }

    /**
     * 0:off-disable,1.on-disable,2.off-enable,3.on-enable
     */
    @Override
    public boolean isLocalSpeakerOn() {
        return camLiveControlLayer.getSpeakerState() == 3;
    }

    @Override
    public void onHistoryDataRsp(IData dataStack) {
        camLiveControlLayer.onHistoryDataRsp(basePresenter);
    }

    @Override
    public void onLiveStop(int playType, int errId) {
        if (getView() != null)
            getView().setKeepScreenOn(false);
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        if (getView() != null)
            getView().postDelayed(() -> camLiveControlLayer.onLiveStop(basePresenter, device, errId), 500);
    }

    @Override
    public void onTakeSnapShot(Bitmap bitmap) {
        if (bitmap == null) {
            if (getView() != null)
                getView().post(() -> ToastUtil.showNegativeToast(getString(R.string.set_failed)));
            return;
        }
        PerformanceUtils.startTrace("takeSnapShot_pre");
        camLiveControlLayer.post(() -> camLiveControlLayer.onCaptureRsp(getActivity(), bitmap));
        PerformanceUtils.stopTrace("takeSnapShot_pre");
        PerformanceUtils.stopTrace("takeSnapShot");
    }


    @Override
    public void onPreviewResourceReady(Bitmap bitmap) {
        camLiveControlLayer.onLoadPreviewBitmap(bitmap);
    }


    @Override
    public void onHistoryLiveStop(int state) {

    }

    @Override
    public void shouldWaitFor(boolean start) {
        if (getView() != null) getView().post(() -> {
            if (start) {
                camLiveControlLayer.onLivePrepared();
            } else {
                camLiveControlLayer.resumeGoodFrame();
            }
        });
        Log.d("shouldWaitFor", "shouldWaitFor: " + start);
    }

    @Override
    public void onRtcp(JFGMsgVideoRtcp rtcp) {
        camLiveControlLayer.onRtcpCallback(basePresenter.getPlayType(), rtcp);
        Log.d("onRtcp", "onRtcp: " + new Gson().toJson(rtcp));
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
    public void showAudioRecordPermission_() {
//        int sFlag = R.drawable.icon_port_speaker_on_selector;
//        imgVCamSwitchSpeaker.setImageResource(sFlag);
//        imgVCamSwitchSpeaker.setTag(sFlag);
//        //横屏
//        camLiveController.getImvLandSpeaker().setImageResource(R.drawable.icon_land_speaker_on_selector);
//        camLiveController.getImvLandSpeaker().setTag(R.drawable.icon_land_speaker_on_selector);
//        if (basePresenter != null) {
//            basePresenter.switchSpeaker();
//        }
    }

    @NeedsPermission({Manifest.permission.RECORD_AUDIO})
    public void showAudioRecordPermission() {
//        imgVCamTriggerMic.setImageResource(R.drawable.icon_port_mic_on_selector);
//        imgVCamTriggerMic.setTag(R.drawable.icon_port_mic_on_selector);
//        camLiveController.getImvLandMic().setImageResource(R.drawable.icon_land_mic_on_selector);
//        camLiveController.getImvLandMic().setTag(R.drawable.icon_land_mic_on_selector);
//        camLiveController.getImvLandSpeaker().setEnabled(false);
//        imgVCamSwitchSpeaker.setEnabled(false);
//        //同时设置speaker
//        imgVCamSwitchSpeaker.setImageResource(R.drawable.icon_port_speaker_on_selector);
//        imgVCamSwitchSpeaker.setTag(R.drawable.icon_port_speaker_on_selector);
//        camLiveController.getImvLandSpeaker().setImageResource(R.drawable.icon_land_speaker_on_selector);
//        camLiveController.getImvLandSpeaker().setTag(R.drawable.icon_land_speaker_on_selector);
//        if (basePresenter != null) {
//            basePresenter.switchMic();
//        }
    }


    @OnPermissionDenied({Manifest.permission.RECORD_AUDIO})
    public void audioRecordPermissionDenied() {
        ToastUtil.showNegativeToast(getString(R.string.permission_auth, getString(R.string.sound_auth), ""));
    }

    @Override
    public void onNetworkChanged(boolean connected) {
        camLiveControlLayer.onNetworkChanged(connected);
    }

    @Override
    public boolean isUserVisible() {
        return getUserVisibleHint();
    }

    @OnNeverAskAgain({Manifest.permission.RECORD_AUDIO})
    public void audioRecordPermissionNeverAsk() {
        ToastUtil.showNegativeToast(getString(R.string.permission_auth, getString(R.string.sound_auth), ""));
    }

    @OnShowRationale({Manifest.permission.RECORD_AUDIO})
    public void audioRecordPermissionRational(PermissionRequest request) {
        ToastUtil.showNegativeToast(getString(R.string.permission_auth, getString(R.string.sound_auth), ""));
    }

    private AlertDialog firmwareDialog;

    @Override
    public void hardwareResult(RxEvent.CheckDevVersionRsp rsp) {
        if (rsp.hasNew) {
            Device device = basePresenter.getDevice();
            DpMsgDefine.DPNet net = device.$(201, new DpMsgDefine.DPNet());
            if (!JFGRules.isDeviceOnline(net)) return;//离线不显示
            if (firmwareDialog != null && firmwareDialog.isShowing()) return;
            if (firmwareDialog == null) {
                firmwareDialog = new AlertDialog.Builder(getActivity())
                        .setMessage(getString(R.string.Tap1_Device_UpgradeTips))
                        .setPositiveButton(getString(R.string.OK), (DialogInterface dialog, int which) -> {
                            Bundle bundle = new Bundle();
                            bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                            bundle.putSerializable("version_content", rsp);
                            AppLogger.d("使用activity");
                            FirmwareFragment hardwareUpdateFragment = FirmwareFragment.newInstance(bundle);
                            ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                                    hardwareUpdateFragment, android.R.id.content);
                        })
                        .setNegativeButton(getString(R.string.CANCEL), null)
                        .create();
            }
            firmwareDialog.show();
            AppLogger.e("新固件");
        }
    }

    @Override
    public void onHistoryDateListUpdate(ArrayList<Long> dateList) {

    }
}