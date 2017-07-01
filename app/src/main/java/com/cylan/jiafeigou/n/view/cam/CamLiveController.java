//package com.cylan.jiafeigou.n.view.cam;
//
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.pm.ActivityInfo;
//import android.content.res.Configuration;
//import android.os.Bundle;
//import android.support.v4.app.FragmentActivity;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.View;
//import android.widget.ImageView;
//
//import com.cylan.jiafeigou.R;
//import com.cylan.jiafeigou.cache.db.module.Device;
//import com.cylan.jiafeigou.cache.db.module.HistoryFile;
//import com.cylan.jiafeigou.dp.DpMsgDefine;
//import com.cylan.jiafeigou.dp.DpMsgMap;
//import com.cylan.jiafeigou.misc.JConstant;
//import com.cylan.jiafeigou.misc.JFGRules;
//import com.cylan.jiafeigou.misc.listener.ILiveStateListener;
//import com.cylan.jiafeigou.n.base.BaseApplication;
//import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
//import com.cylan.jiafeigou.n.view.adapter.CamLandHistoryDateAdapter;
//import com.cylan.jiafeigou.n.view.mine.HomeMineHelpFragment;
//import com.cylan.jiafeigou.support.log.AppLogger;
//import com.cylan.jiafeigou.utils.ActivityUtils;
//import com.cylan.jiafeigou.utils.AnimatorUtils;
//import com.cylan.jiafeigou.utils.ContextUtils;
//import com.cylan.jiafeigou.utils.MiscUtils;
//import com.cylan.jiafeigou.utils.NetUtils;
//import com.cylan.jiafeigou.utils.TimeUtils;
//import com.cylan.jiafeigou.utils.ToastUtil;
//import com.cylan.jiafeigou.utils.ViewUtils;
//import com.cylan.jiafeigou.widget.LiveTimeSetter;
//import com.cylan.jiafeigou.widget.dialog.BaseDialog;
//import com.cylan.jiafeigou.widget.dialog.DatePickerDialogFragment;
//import com.cylan.jiafeigou.widget.flip.FlipImageView;
//import com.cylan.jiafeigou.widget.flip.ISafeStateSetter;
//import com.cylan.jiafeigou.widget.live.ILiveControl;
//import com.cylan.jiafeigou.widget.wheel.ex.IData;
//import com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt;
//
//import java.lang.ref.WeakReference;
//import java.util.ArrayList;
//import java.util.Collections;
//
//import rx.android.schedulers.AndroidSchedulers;
//import rx.schedulers.Schedulers;
//
//import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_IDLE;
//import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PLAYING;
//import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PREPARE;
//import static com.cylan.jiafeigou.misc.JFGRules.PlayErr.STOP_MAUNALLY;
//import static com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract.TYPE_HISTORY;
//import static com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract.TYPE_LIVE;
//import static com.cylan.jiafeigou.widget.live.ILiveControl.STATE_IDLE;
//import static com.cylan.jiafeigou.widget.live.ILiveControl.STATE_PLAYING;
//import static com.cylan.jiafeigou.widget.live.ILiveControl.STATE_STOP;
//import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_ADSORB;
//import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_DRAGGING;
//import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_FINISH;
//
///**
// * 此类包含了所有 控制view播放的按钮{中间loading区域,安全防护,直播时间,全屏切换,}
// * Created by cylan-hunt on 16-12-23.
// */
//
//public class CamLiveController implements
//        SuperWheelExt.WheelRollListener,
//        CamLiveLandTopBar.TopBarAction,
//        FlipImageView.OnFlipListener,
//        View.OnClickListener {
//    private WeakReference<FragmentActivity> activityWeakReference;
//    private WeakReference<DatePickerDialogFragment> datePickerRef;
//    private IData iDataProvider;
//    //横屏竖屏的时候,不一样,需要切换.
//    private ISafeStateSetter iSafeStateSetterPort;
//    private LiveTimeSetter liveTimeSetterPort;
//    private WeakReference<CamLiveContract.Presenter> presenterRef;
//    private ImageView imgPortMic, imgPortSpeaker;
//    private WeakReference<HomeMineHelpFragment> helpPageFragment;
//
//    public void setImgPortMic(ImageView imgPortMic) {
//        this.imgPortMic = imgPortMic;
//    }
//
//    public void setImgPortSpeaker(ImageView imgPortSpeaker) {
//        this.imgPortSpeaker = imgPortSpeaker;
//    }
//
//    public ImageView getImvLandSpeaker() {
//        return camLiveControlLayer.getCamLandImgSpeaker();
//    }
//
//    public ImageView getImvLandMic() {
//        return camLiveControlLayer.getCamLandImgMic();
//    }
//
//    //    /**
////     * 播放,暂停,loading,播放失败提示按钮.
////     */
//    private WeakReference<ILiveControl> iLiveActionViewRef;
//
//    private Context context;
//    private static final String TAG = "CamLiveController";
//    private String uuid;
////    private View.OnClickListener alertListener;
//
//    public CamLiveController(Context context, String uuid) {
//        this.context = context;
//        this.uuid = uuid;
//    }
//
//    public void setCamLiveControlLayer(CamLiveLandControlLayer camLiveControlLayer) {
//        this.camLiveControlLayer = camLiveControlLayer;
//        this.camLiveControlLayer.setTopBarAction(this);
//        this.camLiveControlLayer.setLivePlayBtnClickListener(this);
//        this.camLiveControlLayer.setLiveRectClickListener(this);
//        this.camLiveControlLayer.getTvCamLivePortLive().setOnClickListener(this);
//    }
//
//    public IData getDataProvider() {
//        return iDataProvider;
//    }
//
//    public void setActivity(FragmentActivity activity) {
//        this.activityWeakReference = new WeakReference<>(activity);
//    }
//
//    /**
//     * 中间 loading 区域
//     *
//     * @param iLiveAction
//     */
//    public void setLiveAction(ILiveControl iLiveAction) {
//        this.iLiveActionViewRef = new WeakReference<>(iLiveAction);
//        initLiveControlView();
//    }
//
//    /**
//     * 中间白色 loading 播放 暂停 按钮
//     */
//    private void initLiveControlView() {
//        iLiveActionViewRef.get().setState(STATE_IDLE, null);
//        iLiveActionViewRef.get().setAction(new ILiveControl.Action() {
//            @Override
//            public void clickImage(int curState) {
//                switch (curState) {
//                    case ILiveControl.PLAY_STATE_LOADING_FAILED:
//                    case STATE_STOP:
//                        //下一步playing
//                        if (presenterRef != null && presenterRef.get() != null)
//                            presenterRef.get().startPlay(presenterRef.get().getPlayType());
//                        break;
//                    case STATE_PLAYING:
//                        //下一步stop
//                        if (presenterRef != null && presenterRef.get() != null) {
//                            presenterRef.get().setStopReason(STOP_MAUNALLY);
//                            presenterRef.get().stopPlayVideo(presenterRef.get().getPlayType());
//                        }
//                        break;
//                }
//                AppLogger.i("clickImage:" + curState);
//            }
//
//            @Override
//            public void clickText() {
//
//            }
//
//            @Override
//            public void clickHelp() {
//                if (NetUtils.isNetworkAvailable(ContextUtils.getContext())) {
//                    ToastUtil.showNegativeToast(ContextUtils.getContext().getString(R.string.OFFLINE_ERR_1));
//                    return;
//                }
//                if (helpPageFragment == null || helpPageFragment.get() == null) {
//                    helpPageFragment = new WeakReference<>(HomeMineHelpFragment.newInstance(null));
//                }
//                if (helpPageFragment != null && helpPageFragment.get() != null && helpPageFragment.get().isResumed())
//                    return;
//                if (context != null && context instanceof AppCompatActivity)
//                    ActivityUtils.addFragmentSlideInFromRight(((AppCompatActivity) context).getSupportFragmentManager(), helpPageFragment.get(),
//                            android.R.id.content);
//            }
//        });
//    }
//
//    /**
//     * |图标|安全防护
//     *
//     * @param setter
//     */
//    public void setPortSafeSetter(ISafeStateSetter setter) {
//        this.iSafeStateSetterPort = setter;
//        iSafeStateSetterPort.setFlipListener(this);
//        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
//        boolean safe = device.$(DpMsgMap.ID_501_CAMERA_ALARM_FLAG, false);
//        iSafeStateSetterPort.setFlipped(!safe);
//        Log.d(TAG, "setFlip: " + safe + " " + uuid);
//        if (presenterRef.get() != null && JFGRules.isShareDevice(uuid)) {
//            setter.setVisibility(false);
//            return;
//        }
//    }
//
//    /**
//     * 中间黑色半透明区域 |直播|5/16 23:30|
//     *
//     * @param setter
//     */
//    public void setPortLiveTimeSetter(LiveTimeSetter setter) {
//        liveTimeSetterPort = setter;
//        if (presenterRef != null && presenterRef.get() != null && presenterRef.get().isShareDevice()) {
//            liveTimeSetterPort.setVisibility(false);
//        }
//        ((View) liveTimeSetterPort).setOnClickListener(this);
//    }
//
//
//    public void setupHistoryData(IData dataProvider) {
//        this.iDataProvider = dataProvider;
//        final long time = System.currentTimeMillis();
//        camLiveControlLayer.getSwCamLiveWheel().setDataProvider(dataProvider);
//        camLiveControlLayer.getSwCamLiveWheel().setWheelRollListener(this);
//        updateLiveButtonState(dataProvider != null && dataProvider.getDataCount() > 0);
//        Log.d("performance", "CamLivePortWheel performance: " + (System.currentTimeMillis() - time));
//    }
//
//    public void updateLiveButtonState(boolean show) {
//        camLiveControlLayer.updateLiveButton(show);
//    }
//
//    /**
//     * loading区域
//     *
//     * @param state
//     * @param content
//     */
//    public void setLoadingState(int state, String content) {
//        setLoadingState(state, content, null);
//    }
//
//    /**
//     * loading区域
//     *
//     * @param state
//     * @param content
//     */
//    public void setLoadingState(int state, String content, String help) {
//        int playType = presenterRef.get().getPlayType();
//        if (playType != TYPE_HISTORY) {
//            if (state == STATE_PLAYING || state == STATE_STOP)
//                state = STATE_IDLE;//根据原型,直播没有暂停
//        }
//        if (iLiveActionViewRef != null && iLiveActionViewRef.get() != null) {
//            if (!TextUtils.isEmpty(help))
//                iLiveActionViewRef.get().setState(state, content, help);//兼容使用帮助
//            else iLiveActionViewRef.get().setState(state, content);
//        }
//    }
//
//    /**
//     * 改变播放类型文字:{直播,返回}
//     *
//     * @param liveType
//     */
//    public void setLiveType(int liveType, int orientation) {
//        camLiveControlLayer.getTvCamLivePortLive().setEnabled(liveType != TYPE_LIVE);
//        camLiveControlLayer.getTvCamLivePortLive().setAlpha(liveType != TYPE_LIVE ? 1.f : 0.6f);
//        boolean showPlayBtn = Configuration.ORIENTATION_LANDSCAPE == orientation && liveType == TYPE_HISTORY;
//        camLiveControlLayer.getImgVCamLiveLandPlay().setVisibility(showPlayBtn ? View.VISIBLE : View.GONE);
//        //开始直播,根据条件,显示 安全防护.
//        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
//        if (iSafeStateSetterPort != null)
//            iSafeStateSetterPort.setVisibility(device != null && TextUtils.isEmpty(device.shareAccount));
//    }
//
//    /**
//     * 屏幕方向改变.
//     */
//    public void notifyOrientationChange(final int orientation) {
//        boolean land = orientation == Configuration.ORIENTATION_LANDSCAPE;
//        boolean isShareDevice = presenterRef != null && presenterRef.get() != null && presenterRef.get().isShareDevice();
//        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
//        DpMsgDefine.DPSdStatus sd = device.$(204, new DpMsgDefine.DPSdStatus());
//        boolean sdCardStatus = sd.hasSdcard && sd.err == 0;
//        boolean safe = device.$(501, false);
//        //竖屏事件区域 考虑 分享账号，sd卡
//        if (liveTimeSetterPort != null && presenterRef.get().getPlayState() == PLAY_STATE_PLAYING) {
//            liveTimeSetterPort.setVisibility(!land && !isShareDevice);
//        }
//        if (iSafeStateSetterPort != null)
//            iSafeStateSetterPort.setVisibility(!land && !isShareDevice && presenterRef.get().getPlayState() == PLAY_STATE_PLAYING);
//        //全屏底部区域
//        camLiveControlLayer.setOrientation(presenterRef.get().getPlayType(), orientation, isShareDevice, sdCardStatus, safe);
//        //安全防护
//        camLiveControlLayer.setLandSafeClickListener(this);
//        AppLogger.i("orientation: " + orientation);
//    }
//
//    public void setPresenterRef(CamLiveContract.Presenter presenterRef) {
//        this.presenterRef = new WeakReference<>(presenterRef);
//    }
//
//    /**
//     * @param time :定位到某个时间
//     */
//    public void setNav2Time(long time) {
//        camLiveControlLayer.post(() -> camLiveControlLayer.getSwCamLiveWheel().setPositionByTime(time));
//    }
//
//    private long getWheelCurrentFocusTime() {
//        return camLiveControlLayer.getSwCamLiveWheel().getCurrentFocusTime();
//    }
//
//    /**
//     * 判断操作栏的动画模式
//     */
//    public void tapVideoViewAction() {
//        if (iLiveActionViewRef != null && iLiveActionViewRef.get() != null) {
//            boolean land = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
//            if (land) {//横屏不显示?
//                //上下滑动,进场动画.
//                AnimatorUtils.slideAuto(camLiveControlLayer.getLiveLandBottomBar(), false);
//                AnimatorUtils.slideAuto(camLiveControlLayer.getCamLiveLandTopBar(), true);
//                setLoadingState(STATE_IDLE, null);
//                slideLandDatePickView();
//            } else {
//                //某些限制条件,不需要显示
//                if (presenterRef.get().needShowHistoryWheelView()) {
//                    camLiveControlLayer.setVisibility(camLiveControlLayer.isShown() ? View.INVISIBLE : View.VISIBLE);
//                    camLiveControlLayer.showHistoryWheel(true);
//                }
//                setLoadingState(iLiveActionViewRef.get().getState(), null);
//            }
//        }
//        AppLogger.i("tap: " + (iLiveActionViewRef == null || iLiveActionViewRef.get() == null));
//    }
//
//    /**
//     * 根据播放状态更新
//     */
//    public void updateVisibilityState(boolean show) {
//        if (presenterRef != null && presenterRef.get() != null) {
//            int count = iDataProvider == null ? 0 : iDataProvider.getDataCount();
//            if (count == 0) {
//                AppLogger.i("没有历史视频数据,或者没准备好");
//                return;
//            }
//            Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
//            DpMsgDefine.DPNet net = device.$(DpMsgMap.ID_201_NET, new DpMsgDefine.DPNet());
//            boolean deviceState = JFGRules.isDeviceOnline(net);
//            //播放状态
//            int orientation = context.getResources().getConfiguration().orientation;
//            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                //横屏 slide_out_up  slide_in_up
//            } else {
//                //竖屏 ,淡入淡出,
//                if (!deviceState) {
//                    //设备离线
//                    AppLogger.i("设备离线");
//                    return;
//                }
//
//            }
//        }
//    }
//
//    public void onLiveStop() {
//        //开始直播,根据条件,显示 安全防护.
//        iSafeStateSetterPort.setVisibility(false);
//    }
//
//    /**
//     * @param time
//     */
//    public void setLiveTime(long time) {
//        if (activityWeakReference != null && activityWeakReference.get() != null) {
//            int playType = presenterRef.get().getPlayType();
//            boolean land = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
//            boolean show = !presenterRef.get().isShareDevice()
//                    && playType != CamLiveContract.TYPE_NONE
//                    && presenterRef.get().getPlayState() == PLAY_STATE_PLAYING;
//            if (land) {
//                camLiveControlLayer.getLiveTimeLayout().setVisibility(show);
//                camLiveControlLayer.getLiveTimeLayout().setContent(playType,
//                        playType == TYPE_LIVE ? System.currentTimeMillis() : time);
//            } else {
//                if (liveTimeSetterPort != null)
//                    liveTimeSetterPort.setVisibility(show);
//                if (liveTimeSetterPort != null)
//                    liveTimeSetterPort.setContent(playType,
//                            playType == TYPE_LIVE ? System.currentTimeMillis() : time);
//            }
//        }
//        AppLogger.i("playState: " + presenterRef.get().getPlayState());
//    }
//
//    /**
//     * 横竖屏幕切换
//     */
//    public void setScreenZoomer(View view) {
//        view.setOnClickListener(this);
//    }
//
//    @Override
//    public void onWheelTimeUpdate(long time, int state) {
//        switch (state) {
//            case STATE_DRAGGING:
//                Log.d("onTimeUpdate", "STATE_DRAGGING :" + TimeUtils.getTestTime(time));
//                break;
//            case STATE_ADSORB:
//                Log.d("onTimeUpdate", "STATE_ADSORB :" + TimeUtils.getTestTime(time));
//                break;
//            case STATE_FINISH:
//                Log.d("onTimeUpdate", "STATE_FINISH :" + TimeUtils.getTestTime(time));
//                if (presenterRef != null && presenterRef.get() != null)
//                    presenterRef.get().startPlayHistory(time);
//                break;
//        }
//    }
//
//    @Override
//    public void onBack(View view) {
//        if (activityWeakReference != null && activityWeakReference.get() != null)
//            ViewUtils.setRequestedOrientation(activityWeakReference.get(),
//                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//    }
//
//    @Override
//    public void onSwitchSpeaker(View view) {
//        if (presenterRef != null && presenterRef.get() != null) {
//            int bit = presenterRef.get().getLocalMicSpeakerBit();
//            boolean flag = bit == 1 || bit == 3;
//            ((ImageView) view).setImageResource(flag ?
//                    R.drawable.icon_land_speaker_off_selector : R.drawable.icon_land_speaker_on_selector);
//            view.setTag(flag ?
//                    R.drawable.icon_land_speaker_off_selector : R.drawable.icon_land_speaker_on_selector);
//            imgPortSpeaker.setImageResource(flag ?
//                    R.drawable.icon_port_speaker_off_selector : R.drawable.icon_port_speaker_on_selector);
//            imgPortSpeaker.setTag(flag ?
//                    R.drawable.icon_port_speaker_off_selector : R.drawable.icon_port_speaker_on_selector);
//            presenterRef.get().switchSpeaker();
//        }
//    }
//
//    //横屏mic设置
//    @Override
//    public void onTriggerMic(View view) {
//        if (presenterRef != null && presenterRef.get() != null) {
//            int bit = presenterRef.get().getLocalMicSpeakerBit();
//            boolean flag = bit >= 2;
//            ((ImageView) view).setImageResource(flag ?
//                    R.drawable.icon_land_mic_off_selector : R.drawable.icon_land_mic_on_selector);
//            view.setTag(flag ?
//                    R.drawable.icon_land_mic_off_selector : R.drawable.icon_land_mic_on_selector);
//            imgPortMic.setImageResource(flag ?
//                    R.drawable.icon_port_mic_off_selector : R.drawable.icon_port_mic_on_selector);
//            imgPortMic.setTag(flag ?
//                    R.drawable.icon_port_mic_off_selector : R.drawable.icon_port_mic_on_selector);
//            if (!flag) {
//                imgPortSpeaker.setImageResource(R.drawable.icon_port_speaker_on_selector);
//                imgPortSpeaker.setTag(R.drawable.icon_port_speaker_on_selector);
//                camLiveControlLayer.getCamLandImgSpeaker().setImageResource(R.drawable.icon_land_speaker_on_selector);
//                camLiveControlLayer.getCamLandImgSpeaker().setTag(R.drawable.icon_land_speaker_on_selector);
//            }
//            camLiveControlLayer.getCamLandImgSpeaker().setEnabled(flag);
//            imgPortSpeaker.setEnabled(flag);
//            presenterRef.get().switchMic();
//        }
//    }
//
//    @Override
//    public void onTriggerCapture(View view) {
//        if (presenterRef != null && presenterRef.get() != null)
//            presenterRef.get().takeSnapShot(false);
//    }
//
//    @Override
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.tv_cam_live_port_live:
//                //只有看历史录像的时候才能点击
//                if (presenterRef != null && presenterRef.get() != null) {
//                    AppLogger.d("click live btn: " + presenterRef.get().getPlayType());
//                    presenterRef.get().stopPlayVideo(TYPE_HISTORY);
//                    presenterRef.get().startPlay(TYPE_LIVE);
//                }
//                break;
//            case R.id.imgV_cam_live_land_play:
//                if (presenterRef != null && presenterRef.get() != null) {
//                    if (presenterRef.get().getPlayType() == TYPE_LIVE) {
//                        if (presenterRef.get().getPlayState() == PLAY_STATE_PLAYING) {
//                            presenterRef.get().stopPlayVideo(presenterRef.get().getPlayType());
//                        } else
//                            presenterRef.get().startPlay(presenterRef.get().getPlayType());
//                        AppLogger.i(String.format("land play history: %account", presenterRef.get().getPlayType()));
//                    } else {
//                        long time = camLiveControlLayer.getSwCamLiveWheel()
//                                .getCurrentFocusTime();
//                        presenterRef.get().startPlayHistory(time);
//                        AppLogger.i(String.format("land play history: %account", time));
//                    }
//                }
//                break;
//            case R.id.live_time_layout:
//                clickLiveTimeRect(view);
//                break;
//            case R.id.imgV_cam_zoom_to_full_screen://全屏
//                if (activityWeakReference != null && activityWeakReference.get() != null) {
//                    if (presenterRef.get().getPlayState() != JConstant.PLAY_STATE_PLAYING)
//                        return;
//                    ViewUtils.setRequestedOrientation(activityWeakReference.get(),
//                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//                }
//                break;
//            case R.id.tv_item_content:
//                Object tag = view.getTag();
//                Log.d(TAG, "land click: " + tag);
//                if (tag != null && tag instanceof Long && adapter != null) {
//                    adapter.setCurrentFocusTime((Long) tag);
//                    setNav2Time((Long) tag);
//                }
//                break;
//        }
//        AppLogger.i(String.format("onClick play: %account", (presenterRef != null && presenterRef.get() != null)));
//    }
//
//
//    /**
//     * 检查
//     */
//    private void clickLiveTimeRect(View v) {
//        ViewUtils.deBounceClick(v);
//        if (NetUtils.getJfgNetType(context) == 0 || presenterRef.get() == null) {
//            AppLogger.d("no net work");
//            return;
//        }
//        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
//        DpMsgDefine.DPNet net = device.$(DpMsgMap.ID_201_NET, new DpMsgDefine.DPNet());
//        if (net != null &&
//                net.net == 0) {
//            AppLogger.d("device is offline");
//            return;
//        }
//        DpMsgDefine.DPSdStatus status = device.$(DpMsgMap.ID_204_SDCARD_STORAGE, new DpMsgDefine.DPSdStatus());
//        if (status == null || !status.hasSdcard) {
//            //没有sd卡
//            ToastUtil.showToast(context.getString(R.string.Tap1_Camera_NoSDCardTips));
//            AppLogger.d("no sdcard");
//            return;
//        }
//        if (iDataProvider == null || iDataProvider.getDataCount() == 0) {
////            ToastUtil.showToast(context.getString(R.string.NO_SDCARD));
//            AppLogger.d("history data is not prepared");
//            return;
//        }
//        boolean land = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
//        //竖屏显示对话框,横屏显示测推
//        if (land) {
//            initLandDatePickerView();
//            slideLandDatePickView();
//        } else showPortDatePicker();
//    }
//
//    private void showPortDatePicker() {
//        if (datePickerRef == null || datePickerRef.get() == null) {
//            Bundle bundle = new Bundle();
//            bundle.putString(BaseDialog.KEY_TITLE, context.getString(R.string.TIME));
//            DatePickerDialogFragment.newInstance(bundle);
//            datePickerRef = new WeakReference<>(DatePickerDialogFragment.newInstance(bundle));
//            datePickerRef.get().setAction((int id, Object value) -> {
//                if (value != null && value instanceof Long) {
//                    AppLogger.d("date pick: " + TimeUtils.getSpecifiedDate((Long) value));
//                    loadSelectedDay(TimeUtils.getSpecificDayStartTime((Long) value));
//                }
//            });
//        }
//        datePickerRef.get().setTimeFocus(getWheelCurrentFocusTime());
//        datePickerRef.get().setDateList(presenterRef.get().getFlattenDateList());
//        datePickerRef.get().show(activityWeakReference.get().getSupportFragmentManager(),
//                "DatePickerDialogFragment");
//    }
//
//    /**
//     * datePicker 选中某一选项后,重新加载这一天的数据,此处以后可以拓展为从服务器请求数据
//     *
//     * @param timeStart
//     */
//    private void loadSelectedDay(long timeStart) {
//        if (presenterRef != null && presenterRef.get() != null) {
//            presenterRef.get().assembleTheDay(timeStart / 1000L)
//                    .subscribeOn(Schedulers.io())
//                    .filter(iData -> iData != null)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .doOnCompleted(() -> {
//                        AppLogger.d("reLoad hisData: good");
//                    })
//                    .subscribe(iData -> {
//                        setupHistoryData(iData);
//                        HistoryFile historyFile = iData.getMaxHistoryFile();
//                        if (historyFile != null) {
//                            setNav2Time(historyFile.time * 1000L);
//                            presenterRef.get().startPlayHistory(historyFile.time * 1000L);
//                            AppLogger.d("找到历史录像?" + historyFile);
//                        }
//                    }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
//        }
//
//    }
//
//    private CamLandHistoryDateAdapter adapter;
//
//    /**
//     * 判断,slide_in 或者slide_out
//     */
//    private void slideLandDatePickView() {
//        float x = camLiveControlLayer.getLandDateContainer().getX();
//        float left = camLiveControlLayer.getLandDateContainer().getLeft();
//        if (x == left && camLiveControlLayer.getLandDateContainer().isShown())
//            AnimatorUtils.slideOutRight(camLiveControlLayer.getLandDateContainer());
//    }
//
//    /**
//     * 视图初始化
//     */
//    private void initLandDatePickerView() {
//        int visibility = camLiveControlLayer.getLandDateContainer().getVisibility();
//        if (visibility == View.GONE) {
//            camLiveControlLayer.getLandDateContainer().setVisibility(View.INVISIBLE);
//        }
//        if (presenterRef == null || presenterRef.get() == null || presenterRef.get().getFlattenDateList() == null ||
//                presenterRef.get().getFlattenDateList().isEmpty()) return;
//        if (adapter == null)
//            adapter = new CamLandHistoryDateAdapter(context, null, R.layout.layout_cam_history_land_list);
//        adapter.clear();
//        ArrayList<Long> dateStartList = presenterRef.get().getFlattenDateList();
//        Collections.sort(dateStartList, Collections.reverseOrder());//来一个降序
//        Log.d(TAG, "sort: " + dateStartList);
//        adapter.addAll(dateStartList);
//        adapter.setItemClickListener(this);
//        adapter.setCurrentFocusTime(getWheelCurrentFocusTime());
//        camLiveControlLayer.setDateAdapter(adapter);
//    }
//
//    @Override
//    public void onClick(FlipImageView view) {
//        boolean land = view.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
//        AppLogger.i("land: " + land + " " + (!view.isFlipped()));
//        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
//        boolean aFlag = device.$(DpMsgMap.ID_501_CAMERA_ALARM_FLAG, false);
////        int aVideo = device.$(DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD, -1);
//        if (aFlag) {//已开启自动录像和移动侦测
//            getAlertDialogFrag().show();
//            AppLogger.d("关闭移动侦测将关闭自动录像功能");
//        } else if (presenterRef != null && presenterRef.get() != null) {
//            DpMsgDefine.DPPrimary<Boolean> flipped = new DpMsgDefine.DPPrimary<>();
//            flipped.value = !view.isFlipped();
//            presenterRef.get().updateInfoReq(flipped, DpMsgMap.ID_501_CAMERA_ALARM_FLAG);
//        }
//    }
//
//    @Override
//    public void onFlipStart(FlipImageView view) {
//
//    }
//
//    @Override
//    public void onFlipEnd(FlipImageView view) {
//
//    }
//
//    public ILiveStateListener getLiveStateListener() {
//        return liveStateListener;
//    }
//
//    /**
//     * 注册一个播放状态
//     */
//    private ILiveStateListener liveStateListener = new ILiveStateListener() {
//        @Override
//        public void liveStateChange() {
//            if (activityWeakReference == null || activityWeakReference.get() == null)
//                return;
//            int state = presenterRef.get().getPlayState();
//            switch (state) {
//                case PLAY_STATE_IDLE:
//                    if (camLiveControlLayer.getImgVCamLiveLandPlay().getDrawable()
//                            != context.getResources().getDrawable(R.drawable.icon_landscape_stop)) {
//                        //do work here
//                        camLiveControlLayer.getImgVCamLiveLandPlay().setImageResource(R.drawable.icon_landscape_stop);
//                    }
//                    updateVisibilityState(false);
//                    break;
//                case PLAY_STATE_PREPARE:
//                case PLAY_STATE_PLAYING:
//                    //do work here
//                    camLiveControlLayer.getImgVCamLiveLandPlay().setImageResource(R.drawable.icon_landscape_playing);
//                    break;
//            }
//            AppLogger.i("state: " + state);
//        }
//    };
//
//    private AlertDialog getAlertDialogFrag() {
//        return new AlertDialog.Builder(context)
//                .setMessage(context.getString(R.string.Tap1_Camera_MotionDetection_OffTips))
//                .setNegativeButton(context.getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
//                    camLiveControlLayer.setLandSafe(false);
//                    if (iSafeStateSetterPort != null) iSafeStateSetterPort.setFlipped(false);
//                })
//                .setPositiveButton(context.getString(R.string.CARRY_ON), (DialogInterface dialog, int which) -> {
//                    if (presenterRef != null && presenterRef.get() != null) {
//                        DpMsgDefine.DPPrimary<Boolean> flag = new DpMsgDefine.DPPrimary<>();
//                        flag.value = false;
//                        presenterRef.get().updateInfoReq(flag, DpMsgMap.ID_501_CAMERA_ALARM_FLAG);
//                    }
//                    if (presenterRef != null && presenterRef.get() != null) {
//                        DpMsgDefine.DPPrimary<Integer> flag = new DpMsgDefine.DPPrimary<>();
//                        flag.value = 2;
//                        presenterRef.get().updateInfoReq(flag, DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD);
//                    }
//                    camLiveControlLayer.setLandSafe(true);
//                    if (iSafeStateSetterPort != null) iSafeStateSetterPort.setFlipped(true);
//                })
//                .setCancelable(false)
//                .create();
//    }
//}
