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
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
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

import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_LOADING_FAILED;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PLAYING;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_STOP;
import static com.cylan.jiafeigou.misc.JFGRules.PlayErr.STOP_MAUNALLY;
import static com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract.TYPE_HISTORY;
import static com.cylan.jiafeigou.support.photoselect.helpers.Constants.REQUEST_CODE;
import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_ADSORB;
import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_DRAGGING;
import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_FINISH;

/**
 * A simple {@link Fragment} subclass.
 */
@RuntimePermissions()
public class CameraLiveFragmentEx extends IBaseFragment<CamLiveContract.Presenter>
        implements CamLiveContract.View, BaseDialog.BaseDialogAction {

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
        camLiveControlLayer.initView(uuid);
        camLiveControlLayer.initLiveViewRect(isNormalView ? basePresenter.getVideoPortHeightRatio() : 1.0f, mLiveViewRectInWindow);
        camLiveControlLayer.setLoadingRectAction(new ILiveControl.Action() {
            @Override
            public void clickImage(View view, int state) {
                switch (state) {
                    case PLAY_STATE_LOADING_FAILED:
                    case PLAY_STATE_STOP:
                        //下一步playing
                        basePresenter.startPlayLive();
                        break;
                    case PLAY_STATE_PLAYING:
                        //下一步stop
                        basePresenter.setStopReason(STOP_MAUNALLY);
                        basePresenter.stopPlayVideo(basePresenter.getPlayType());
                        break;
                }
                AppLogger.i("clickImage:" + state);
            }

            @Override
            public void clickText(View view) {

            }

            @Override
            public void clickHelp(View view) {
                if (NetUtils.isNetworkAvailable(ContextUtils.getContext())) {
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
    }

    @Override
    public void onStart() {
        super.onStart();
        Device device = basePresenter.getDevice();
        DpMsgDefine.DPStandby standby = device.$(508, new DpMsgDefine.DPStandby());
        if (!standby.standby) {
            //开始直播
            basePresenter.startPlayLive();
        } else {
            //show
        }
        camLiveControlLayer.onDeviceStandByChanged(device, v -> jump2Setting());
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (basePresenter != null)
            basePresenter.stopPlayVideo(basePresenter.getPlayType());
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (basePresenter != null && isVisibleToUser && isResumed() && getActivity() != null) {
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
            AppLogger.e("直播");
            basePresenter.startPlayLive();
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
//            basePresenter.fetchHistoryDataList();
//            //每天检测一次新固件
//            basePresenter.checkNewHardWare();
            //非待机模式

        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        camLiveControlLayer.onLiveDestroy();
    }

//    private void startLive() {
//        View old = fLayoutCamLiveView.findViewById(R.id.fLayout_cam_sight_setting);
//        AppLogger.d("startPlay: old == null: " + (old == null));
//        if (old != null) return;//不用播放
//        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
//        DpMsgDefine.DPStandby isStandBY = device.$(DpMsgMap.ID_508_CAMERA_STANDBY_FLAG, new DpMsgDefine.DPStandby());
//        if (isStandBY != null && isStandBY.standby) return;
//        if (!getUserVisibleHint()) return;//看不见，就不需要播放了。
//        if (basePresenter.getPlayState() != PLAY_STATE_PLAYING) {
//            CamLiveContract.PrePlayType type = basePresenter.getPrePlayType();
//            if (type.type == TYPE_LIVE) {
//                basePresenter.startPlayLive(TYPE_LIVE);
//            } else {
//                startLiveHistory(type.time * 1000L);
//            }
//        }
//    }
//
//    /**
//     * @param time :毫秒
//     */
//    private void startLiveHistory(long time) {
//        View old = fLayoutCamLiveView.findViewById(R.id.fLayout_cam_sight_setting);
//        AppLogger.d("startPlay: old == null: " + (old == null));
//        if (old != null) return;//不用播放
//        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
//        DpMsgDefine.DPStandby isStandBY = device.$(508, new DpMsgDefine.DPStandby());
//        if (isStandBY == null || isStandBY.standby) return;
//        basePresenter.startPlayHistory(time);
//    }

//    /**
//     * 初始化三个按钮{扬声器,mic,截图}
//     *
//     * @param enable
//     */
//    private void initBottomBtn(final boolean enable) {
//        imgVCamSwitchSpeaker.post(() -> {
//            imgVCamSwitchSpeaker.setEnabled(enable);
//            //历史录像,不需要Mic
//            imgVCamTriggerMic.setEnabled(enable && basePresenter.getPlayType() == TYPE_LIVE);
//            imgVCamTriggerCapture.setEnabled(enable);
//            Log.d("initBottomBtn", "setClickable: " + enable);
//        });
//    }

//    /**
//     * 视角设置
//     *
//     * @param isNormalCam
//     */
//    private void checkSightDialog(boolean isNormalCam) {
//        if (isNormalCam || basePresenter.isShareDevice()) return;
//        boolean isFirstShow = PreferencesUtils.getBoolean(KEY_CAM_SIGHT_SETTING + uuid, true);
//        if (!isFirstShow) return;//不是第一次
//        View old = fLayoutCamLiveView.findViewById(R.id.fLayout_cam_sight_setting);
//        if (old != null) {
//            //已经有了
//            old.setVisibility(View.VISIBLE);
//            return;
//        }
//        View view = LayoutInflater.from(getContext()).inflate(R.layout.cam_sight_setting_overlay, null);
//        fLayoutCamLiveView.addView(view);//最顶
//        View layout = fLayoutCamLiveView.findViewById(R.id.fLayout_cam_sight_setting);
//        ((TextView) (view.findViewById(R.id.tv_sight_setting_content))).setText(getString(R.string.Tap1_Camera_Overlook) + ": "
//                + getString(R.string.Tap1_Camera_OverlookTips));
//        view.findViewById(R.id.btn_sight_setting_cancel).setOnClickListener((View v) -> {
//            if (layout != null) fLayoutCamLiveView.removeView(layout);
//            startLive();
//        });
//        layout.setOnClickListener(v -> AppLogger.d("don't click me"));
//        view.findViewById(R.id.btn_sight_setting_next).setOnClickListener((View v) -> {
//            if (layout != null) fLayoutCamLiveView.removeView(layout);
//            Intent intent = new Intent(getActivity(), SightSettingActivity.class);
//            intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
//            startActivity(intent);
//        });
//        PreferencesUtils.putBoolean(KEY_CAM_SIGHT_SETTING + uuid, false);
//    }

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
//            vLive.getVideoView().config360(TextUtils.equals(_509, "0") ? CameraParam.getTopPreset() : CameraParam.getWallPreset());
//            vLive.getVideoView().setMode(TextUtils.equals("0", _509) ? 0 : 1);
//            vLive.getVideoView().detectOrientationChanged();
        }
    }

//    /**
//     * 展示 待机模式view
//     *
//     * @param flag
//     */
//    private void setupStandByView(boolean flag) {
//        if (flag)
//            vLive.enableStandbyMode(true, v -> {
//                Intent intent = new Intent(getActivity(), CamSettingActivity.class);
//                intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
//                startActivityForResult(intent, REQUEST_CODE,
//                        ActivityOptionsCompat.makeCustomAnimation(getActivity(),
//                                R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
//            }, JFGRules.isShareDevice(uuid));
//        else vLive.enableStandbyMode(false, null, false);
//    }

    @Override
    public void onLivePrepare(int type) {
        camLiveControlLayer.onLivePrepared();
    }

    @Override
    public void onLiveStarted(int type) {
        if (getView() != null) getView().setKeepScreenOn(true);
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        camLiveControlLayer.onLiveStart(basePresenter, device);
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

    @Override
    public boolean isLocalMicOn() {
//        Object tag = imgVCamTriggerMic.getTag();
//        return tag != null && (int) tag == R.drawable.icon_port_mic_on_selector;
        return false;
    }

    @Override
    public boolean isLocalSpeakerOn() {
//        Object tag = imgVCamSwitchSpeaker.getTag();
///        return tag != null && (int) tag == R.drawable.icon_port_speaker_on_selector;
        return false;
    }

    @Override
    public void onHistoryDataRsp(IData dataStack) {
        camLiveControlLayer.onHistoryDataRsp(dataStack, (long time, int state) -> {
            switch (state) {
                case STATE_DRAGGING:
                    Log.d("onTimeUpdate", "STATE_DRAGGING :" + TimeUtils.getTestTime(time));
                    break;
                case STATE_ADSORB:
                    Log.d("onTimeUpdate", "STATE_ADSORB :" + TimeUtils.getTestTime(time));
                    break;
                case STATE_FINISH:
                    Log.d("onTimeUpdate", "STATE_FINISH :" + TimeUtils.getTestTime(time));
                    basePresenter.startPlayHistory(time);
                    break;
            }
        });
    }

    @Override
    public void onLiveStop(int playType, int errId) {
        if (getView() != null)
            getView().setKeepScreenOn(false);
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        camLiveControlLayer.onLiveStop(basePresenter, device, errId);
    }

    @Override
    public void onTakeSnapShot(Bitmap bitmap) {
        if (bitmap == null) {
            if (getView() != null)
                getView().post(() -> ToastUtil.showNegativeToast(getString(R.string.set_failed)));
            return;
        }
        PerformanceUtils.startTrace("takeSnapShot_pre");
//        if (getView() != null) getView().post(() -> showPopupWindow(bitmap));
        camLiveControlLayer.onCaptureRsp(getActivity(), bitmap);
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
//        camLiveController.setLoadingState(start ? ILiveControl.STATE_LOADING : ILiveControl.STATE_IDLE, null);
    }

    @Override
    public void countdownFinish() {
//        roundCardPopup.dismiss();//don't need try catch ,it is wrapped by rxjava
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
//
//            Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_KEY);
//            if (f == null) {
//                Bundle bundle = new Bundle();
//                bundle.putString(BaseDialog.KEY_TITLE, getString(R.string.Tap1_Device_UpgradeTips));
//                bundle.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, getString(R.string.CANCEL));
//                bundle.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, getString(R.string.OK));
//                bundle.putBoolean(SimpleDialogFragment.KEY_TOUCH_OUT_SIDE_DISMISS, false);
//                SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(bundle);
//                dialogFragment.setValue(rsp);
//                dialogFragment.setAction(this);
//                dialogFragment.show(getActivity().getSupportFragmentManager(), DIALOG_KEY);
//            }
            AppLogger.e("新固件");
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
            FirmwareFragment hardwareUpdateFragment = FirmwareFragment.newInstance(bundle);
            ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                    hardwareUpdateFragment, android.R.id.content);
        }
    }
}