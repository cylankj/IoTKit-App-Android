package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.listener.ILiveStateListener;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LiveTimeSetter;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.DatePickerDialogFragment;
import com.cylan.jiafeigou.widget.flip.FlipImageView;
import com.cylan.jiafeigou.widget.flip.ISafeStateSetter;
import com.cylan.jiafeigou.widget.live.ILiveControl;
import com.cylan.jiafeigou.widget.wheel.ex.IData;
import com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt;
import com.cylan.utils.NetUtils;

import java.lang.ref.WeakReference;

import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_IDLE;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PLAYING;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PREPARE;
import static com.cylan.jiafeigou.widget.live.ILiveControl.STATE_IDLE;
import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_ADSORB;
import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_DRAGGING;
import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_FINISH;

/**
 * 此类包含了所有 控制view播放的按钮{中间loading区域,安全防护,直播时间,全屏切换,}
 * Created by cylan-hunt on 16-12-23.
 */

public class CamLiveController implements
        SuperWheelExt.WheelRollListener,
        CamLiveLandTopBar.TopBarAction,
        FlipImageView.OnFlipListener,
        View.OnClickListener {
    private WeakReference<FragmentActivity> activityWeakReference;
    private WeakReference<DatePickerDialogFragment> datePickerRef;
    private IData iDataProvider;
    //横屏竖屏的时候,不一样,需要切换.
    private ISafeStateSetter iSafeStateSetterPort, iSafeStateSetterLand;
    private LiveTimeSetter liveTimeSetterPort, liveTimeSetterLand;
    private WeakReference<CamLiveContract.Presenter> presenterRef;
    //    /**
//     * 播放,暂停,loading,播放失败提示按钮.
//     */
    private WeakReference<ILiveControl> iLiveActionViewRef;

    //播放控制层面.
    private CamLiveControlLayer camLiveControlLayer;
    private Context context;
    private static final String TAG = "CamLiveController";
    private String uuid;

    public CamLiveController(Context context, String uuid) {
        this.context = context;
        this.uuid = uuid;
    }

    public void setCamLiveControlLayer(CamLiveControlLayer camLiveControlLayer) {
        this.camLiveControlLayer = camLiveControlLayer;
        this.camLiveControlLayer.setTopBarAction(this);
        this.camLiveControlLayer.getImgVCamLiveLandPlay().setOnClickListener(this);
        this.camLiveControlLayer.getLiveTimeLayout().setOnClickListener(this);
        this.camLiveControlLayer.getTvCamLivePortLive().setOnClickListener(this);
    }

    public IData getDataProvider() {
        return iDataProvider;
    }

    public void setActivity(FragmentActivity activity) {
        this.activityWeakReference = new WeakReference<>(activity);
    }

    /**
     * 中间 loading 区域
     *
     * @param iLiveAction
     */
    public void setLiveAction(ILiveControl iLiveAction) {
        this.iLiveActionViewRef = new WeakReference<>(iLiveAction);
        initLiveControlView();
    }

    /**
     * 中间白色 loading 播放 暂停 按钮
     */
    private void initLiveControlView() {
        iLiveActionViewRef.get().setAction(new ILiveControl.Action() {
            @Override
            public void clickImage(int curState) {
                switch (curState) {
                    case ILiveControl.STATE_LOADING_FAILED:
                    case ILiveControl.STATE_STOP:
                        //下一步playing
                        if (presenterRef != null && presenterRef.get() != null)
                            presenterRef.get().startPlayVideo(presenterRef.get().getPlayType());
                        break;
                    case ILiveControl.STATE_PLAYING:
                        //下一步stop
                        if (presenterRef != null && presenterRef.get() != null) {
                            presenterRef.get().stopPlayVideo(presenterRef.get().getPlayType());
                        }
                        break;
                }
                AppLogger.i("clickImage:" + curState);
            }

            @Override
            public void clickText() {

            }
        });
    }

    /**
     * |图标|安全防护
     *
     * @param setter
     */
    public void setPortSafeSetter(ISafeStateSetter setter) {
        this.iSafeStateSetterPort = setter;
        iSafeStateSetterPort.setFlipListener(this);
        boolean safe = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_501_CAMERA_ALARM_FLAG, false);
        //true:绿色,false:setFlipped(true)
        iSafeStateSetterPort.setFlipped(!safe);
        Log.d(TAG, "setFlip: " + safe + " " + uuid);
    }

    /**
     * |图标|安全防护
     *
     * @param setter
     */
    private void setLandSafeSetter(ISafeStateSetter setter) {
        this.iSafeStateSetterLand = setter;
    }

    /**
     * 中间黑色半透明区域 |直播|5/16 23:30|
     *
     * @param setter
     */
    public void setPortLiveTimeSetter(LiveTimeSetter setter) {
        liveTimeSetterPort = setter;
        ((View) liveTimeSetterPort).setOnClickListener(this);
    }

    /**
     * 横屏
     *
     * @param setter
     */
    private void setLandLiveTimeSetter(LiveTimeSetter setter) {
        this.liveTimeSetterLand = setter;
    }

    public void setupHistoryData(IData dataProvider) {
        this.iDataProvider = dataProvider;
        final long time = System.currentTimeMillis();
        camLiveControlLayer.getSwCamLiveWheel().setDataProvider(dataProvider);
        camLiveControlLayer.getSwCamLiveWheel().setWheelRollListener(this);
        Log.d("performance", "CamLivePortWheel performance: " + (System.currentTimeMillis() - time));
    }

    /**
     * loading区域
     *
     * @param state
     * @param content
     */
    public void setLoadingState(int state, String content) {
        if (iLiveActionViewRef != null && iLiveActionViewRef.get() != null)
            iLiveActionViewRef.get().setState(state, content);
    }

    /**
     * 改变播放类型文字:{直播,返回}
     *
     * @param liveType
     */
    public void setLiveType(int liveType) {
        camLiveControlLayer.getTvCamLivePortLive().setText(context.getResources()
                .getString(liveType == CamLiveContract.TYPE_LIVE ? R.string.Tap1_Camera_VideoLive : R.string.BACK));
    }

    /**
     * 屏幕方向改变.
     */
    public void notifyOrientationChange(final int orientation) {
        camLiveControlLayer.setOrientation(orientation);
        boolean land = orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (land && liveTimeSetterLand == null) {
            liveTimeSetterLand = camLiveControlLayer.getLiveTimeLayout();
        }
        if (land && iSafeStateSetterLand == null) {
            //安全防护
            setLandSafeSetter(camLiveControlLayer.getFlipLayout());
            iSafeStateSetterLand.setFlipListener(this);
            boolean safe = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_501_CAMERA_ALARM_FLAG, false);
            iSafeStateSetterLand.setFlipped(safe);
        }//显示或者隐藏
        if (liveTimeSetterLand != null) liveTimeSetterLand.setVisibility(land);
        if (iSafeStateSetterLand != null) iSafeStateSetterLand.setVisibility(land);
        if (liveTimeSetterPort != null && presenterRef.get().getPlayState() == PLAY_STATE_PLAYING)
            liveTimeSetterPort.setVisibility(!land);
        if (iSafeStateSetterPort != null) iSafeStateSetterPort.setVisibility(!land);
        AppLogger.i("orientation: " + orientation);
    }

    public void setPresenterRef(CamLiveContract.Presenter presenterRef) {
        this.presenterRef = new WeakReference<>(presenterRef);
    }

    /**
     * @param time :定位到某个时间
     */
    public void setNav2Time(long time) {
        camLiveControlLayer.getSwCamLiveWheel().setPositionByTime(time);
    }

    private long getWheelCurrentFocusTime() {
        return camLiveControlLayer.getSwCamLiveWheel().getCurrentFocusTime();
    }

    /**
     * 判断操作栏的动画模式
     */
    public void tapVideoViewAction() {
        boolean show = false;
        if (iLiveActionViewRef != null && iLiveActionViewRef.get() != null) {
//            int state = iLiveActionViewRef.get().getState();
            boolean land = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
            if (land) {//横屏不显示?
//                state = STATE_IDLE;
                //上下滑动,进场动画.
                AnimatorUtils.slideAuto(camLiveControlLayer.getLiveLandBottomBar(), false);
                AnimatorUtils.slideAuto(camLiveControlLayer.getCamLiveLandTopBar(), true);
                setLoadingState(STATE_IDLE, null);
            } else {
                //某些限制条件,不需要显示
                if (presenterRef.get().needShowHistoryWheelView()) {
                    camLiveControlLayer.setVisibility(camLiveControlLayer.isShown() ? View.VISIBLE : View.INVISIBLE);
//                    show = iLiveActionViewRef.get() instanceof View && ((View) iLiveActionViewRef.get()).isShown();
//                    camLiveControlLayer.getLiveLandBottomBar().setVisibility(!show ? View.VISIBLE : View.INVISIBLE);
                }
                setLoadingState(iLiveActionViewRef.get().getState(), null);
            }
        }
        AppLogger.i("tap");
    }

    /**
     * 根据播放状态更新
     */
    public void updateVisibilityState(boolean show) {
        if (presenterRef != null && presenterRef.get() != null) {
            int count = iDataProvider == null ? 0 : iDataProvider.getDataCount();
            if (count == 0) {
                AppLogger.i("没有历史视频数据,或者没准备好");
                return;
            }
            DpMsgDefine.DPNet net = GlobalDataProxy.getInstance().getValue(uuid,
                    DpMsgMap.ID_201_NET, null);
            boolean deviceState = JFGRules.isDeviceOnline(net);
            //播放状态
            int playState = presenterRef.get().getPlayState();
            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                //横屏 slide_out_up  slide_in_up
            } else {
                //竖屏 ,淡入淡出,
                if (!deviceState) {
                    //设备离线
                    AppLogger.i("设备离线");
                    return;
                }

            }
        }
    }

    /**
     * 设置安全防护状态
     *
     * @param state
     */
    public void setProtectionState(boolean state) {
        if (!check())
            return;
        //这个state可根据不同模式
        if (iSafeStateSetterPort != null) iSafeStateSetterPort.setState(state);
        if (iSafeStateSetterLand != null) iSafeStateSetterLand.setState(state);
    }

    /**
     * @param time
     */
    public void setLiveTime(long time) {
        if (activityWeakReference != null && activityWeakReference.get() != null) {
            int playType = presenterRef.get().getPlayType();
            boolean land = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
            boolean show = !presenterRef.get().isShareDevice()
                    && playType != CamLiveContract.TYPE_NONE
                    && presenterRef.get().getPlayState() == PLAY_STATE_PLAYING;
            if (land) {
                if (liveTimeSetterLand != null)
                    liveTimeSetterLand.setVisibility(show);
                if (liveTimeSetterLand != null) liveTimeSetterLand.setContent(playType,
                        playType == CamLiveContract.TYPE_LIVE ? System.currentTimeMillis() : time);
            } else {
                if (liveTimeSetterPort != null)
                    liveTimeSetterPort.setVisibility(show);
                if (liveTimeSetterPort != null)
                    liveTimeSetterPort.setContent(playType,
                            playType == CamLiveContract.TYPE_LIVE ? System.currentTimeMillis() : time);
            }
        }
        AppLogger.i("playState: " + presenterRef.get().getPlayState());
    }

    /**
     * 横竖屏幕切换
     */
    public void setScreenZoomer(View view) {
        view.setOnClickListener(this);
    }

    private boolean check() {
        return activityWeakReference != null && activityWeakReference.get() != null;
    }

    @Override
    public void onWheelTimeUpdate(long time, int state) {
        switch (state) {
            case STATE_DRAGGING:
                Log.d("onTimeUpdate", "STATE_DRAGGING :" + TimeUtils.getTestTime(time));
                break;
            case STATE_ADSORB:
                Log.d("onTimeUpdate", "STATE_ADSORB :" + TimeUtils.getTestTime(time));
                break;
            case STATE_FINISH:
                Log.d("onTimeUpdate", "STATE_FINISH :" + TimeUtils.getTestTime(time));
                if (presenterRef != null && presenterRef.get() != null)
                    presenterRef.get().startPlayHistory(time);
                break;
        }
    }

    @Override
    public void onBack() {
        if (activityWeakReference != null && activityWeakReference.get() != null)
            ViewUtils.setRequestedOrientation(activityWeakReference.get(),
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onSwitchSpeaker() {
        if (presenterRef != null && presenterRef.get() != null)
            presenterRef.get().switchSpeakerMic(false, false, false);
    }

    @Override
    public void onTriggerRecorder() {
        if (presenterRef != null && presenterRef.get() != null)
            presenterRef.get().switchSpeakerMic(false, false, false);
    }

    @Override
    public void onTriggerCapture() {
        if (presenterRef != null && presenterRef.get() != null)
            presenterRef.get().takeSnapShot();
    }

    //    @OnClick({R.id.imgV_cam_live_land_play,
//            R.id.live_time_layout,
//            R.id.imgV_cam_zoom_to_full_screen,
//            R.id.tv_cam_live_port_live})
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cam_live_port_live:
                break;
            case R.id.imgV_cam_live_land_play:
                if (presenterRef != null && presenterRef.get() != null) {
                    if (presenterRef.get().getPlayType() == CamLiveContract.TYPE_LIVE) {
                        presenterRef.get().startPlayVideo(presenterRef.get().getPlayType());
                        AppLogger.i(String.format("land play history: %s", "live"));
                    } else {
                        long time = camLiveControlLayer.getSwCamLiveWheel()
                                .getCurrentFocusTime();
                        presenterRef.get().startPlayHistory(time);
                        AppLogger.i(String.format("land play history: %s", time));
                    }
                }
            case R.id.live_time_layout:
                clickLiveTimeRect(view);
                break;
            case R.id.imgV_cam_zoom_to_full_screen://全屏
                if (activityWeakReference != null && activityWeakReference.get() != null) {
                    if (presenterRef.get().getPlayState() != JConstant.PLAY_STATE_PLAYING)
                        return;
                    ViewUtils.setRequestedOrientation(activityWeakReference.get(),
                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                break;
        }
        AppLogger.i(String.format("onClick land play: %s", (presenterRef != null && presenterRef.get() != null)));
    }


    /**
     * 检查
     */
    private void clickLiveTimeRect(View v) {
        ViewUtils.deBounceClick(v);
        if (NetUtils.getJfgNetType(context) == 0 || presenterRef.get() == null) {
            AppLogger.d("no net work");
            return;
        }
        DpMsgDefine.DPNet net = GlobalDataProxy.getInstance().getValue(uuid,
                DpMsgMap.ID_201_NET, null);
        if (net != null &&
                net.net == 0) {
            AppLogger.d("device is offline");
            return;
        }
        DpMsgDefine.DPSdStatus status = GlobalDataProxy.getInstance().getValue(uuid,
                DpMsgMap.ID_204_SDCARD_STORAGE, null);
        if (status != null && !status.hasSdcard) {
            //没有sd卡
            ToastUtil.showToast(context.getString(R.string.Tap1_Camera_NoSDCardTips));
            AppLogger.d("no sdcard");
            return;
        }
        if (iDataProvider == null || iDataProvider.getDataCount() == 0) {
            AppLogger.d("history data is not prepared");
            ToastUtil.showToast("没有历史视频...自己加的,,,别点了");
            return;
        }
        boolean land = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        //竖屏显示对话框,横屏显示测推
        if (land) showLandDatePicker();
        else showPortDatePicker();
    }

    private void showPortDatePicker() {
        if (datePickerRef == null || datePickerRef.get() == null) {
            Bundle bundle = new Bundle();
            bundle.putString(BaseDialog.KEY_TITLE, context.getString(R.string.TIME));
            DatePickerDialogFragment.newInstance(bundle);
            datePickerRef = new WeakReference<>(DatePickerDialogFragment.newInstance(bundle));
            datePickerRef.get().setAction((int id, Object value) -> {
                if (value != null && value instanceof Long) {
                    AppLogger.d("date pick: " + TimeUtils.getSpecifiedDate((Long) value));
                    setNav2Time((Long) value);
                    presenterRef.get().startPlayHistory((Long) value);
                }
            });
        }
        datePickerRef.get().setTimeFocus(getWheelCurrentFocusTime());
        datePickerRef.get().setDateMap(presenterRef.get().getFlattenDateMap());
        datePickerRef.get().show(activityWeakReference.get().getSupportFragmentManager(),
                "DatePickerDialogFragment");
    }

    private void showLandDatePicker() {
        int visibility = camLiveControlLayer.getLandDateContainer().getVisibility();
        if (visibility == View.GONE) {
            camLiveControlLayer.getLandDateContainer().setVisibility(View.INVISIBLE);
        }
        float x = camLiveControlLayer.getLandDateContainer().getX();
        float left = camLiveControlLayer.getLandDateContainer().getLeft();
        float translateX = camLiveControlLayer.getLandDateContainer().getTranslationX();
        if (x == left && camLiveControlLayer.getLandDateContainer().isShown())
            AnimatorUtils.slideOutRight(camLiveControlLayer.getLandDateContainer());
        else if (translateX + left == x
                || x == left + translateX
                || !camLiveControlLayer.getLandDateContainer().isShown())
            AnimatorUtils.slideInRight(camLiveControlLayer.getLandDateContainer());
    }

    @Override
    public void onClick(FlipImageView view) {
        boolean land = view.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        AppLogger.i("land: " + land + " " + (!view.isFlipped()));
        if (presenterRef != null && presenterRef.get() != null)
            presenterRef.get().updateInfoReq(!view.isFlipped(), DpMsgMap.ID_501_CAMERA_ALARM_FLAG);
    }

    @Override
    public void onFlipStart(FlipImageView view) {

    }

    @Override
    public void onFlipEnd(FlipImageView view) {

    }

    public ILiveStateListener getLiveStateListener() {
        return liveStateListener;
    }

    /**
     * 注册一个播放状态
     */
    private ILiveStateListener liveStateListener = new ILiveStateListener() {
        @Override
        public void liveStateChange() {
            if (activityWeakReference == null || activityWeakReference.get() == null)
                return;
            int state = presenterRef.get().getPlayState();
            switch (state) {
                case PLAY_STATE_IDLE:
                    if (camLiveControlLayer.getImgVCamLiveLandPlay().getDrawable()
                            != context.getResources().getDrawable(R.drawable.icon_landscape_stop)) {
                        //do work here
                        camLiveControlLayer.getImgVCamLiveLandPlay().setImageResource(R.drawable.icon_landscape_stop);
                    }
                    updateVisibilityState(false);
                    break;
                case PLAY_STATE_PREPARE:
                case PLAY_STATE_PLAYING:
                    if (camLiveControlLayer.getImgVCamLiveLandPlay().getDrawable()
                            != context.getResources().getDrawable(R.drawable.icon_landscape_playing)) {
                        //do work here
                        camLiveControlLayer.getImgVCamLiveLandPlay().setImageResource(R.drawable.icon_landscape_playing);
                    }
                    break;
            }
            AppLogger.i("state: " + state);
        }
    };

}
