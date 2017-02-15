package com.cylan.jiafeigou.n.view.bell;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.BaseFullScreenActivity;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.module.JFGDPDevice;
import com.cylan.jiafeigou.base.view.CallablePresenter;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.bell.BellLivePresenterImpl;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.bell.DragLayout;
import com.cylan.jiafeigou.widget.live.ILiveControl;
import com.cylan.jiafeigou.widget.live.LivePlayControlView;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;

import java.lang.ref.WeakReference;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class BellLiveActivity extends BaseFullScreenActivity<BellLiveContract.Presenter>
        implements DragLayout.OnDragReleaseListener, View.OnClickListener
        , BellLiveContract.View, ILiveControl.Action {

    @BindView(R.id.fLayout_bell_live_holder)
    FrameLayout fLayoutBellLiveHolder;
    @BindView(R.id.tv_bell_live_flow)
    TextView mBellFlow;
    @BindView(R.id.imgv_bell_live_switch_to_land)
    ImageView imgvBellLiveSwitchToLand;
    @BindView(R.id.dLayout_bell_hot_seat)
    DragLayout dLayoutBellHotSeat;
    @BindView(R.id.imgv_bell_live_capture)
    ImageView imgvBellLiveCapture;
    @BindView(R.id.imgv_bell_live_hang_up)
    ImageView imgvBellLiveHangUp;
    @BindView(R.id.imgv_bell_live_speaker)
    ImageView imgvBellLiveSpeaker;
    @BindView(R.id.fLayout_bell_after_live)
    FrameLayout fLayoutBellAfterLive;
    @BindView(R.id.act_bell_live_video_picture)
    ImageView mBellLiveVideoPicture;
    @BindView(R.id.act_bell_live_video_view_container)
    FrameLayout mVideoViewContainer;
    @BindView(R.id.act_bell_live_video_play_controller)
    ILiveControl mVideoPlayController;
    @BindView(R.id.act_bell_live_back)
    TextView mBellLiveBack;

    private ImageView mLandBellLiveSpeaker;
    /**
     * 水平方向的view
     */
    private WeakReference<View> fLayoutLandHolderRef;

    private SurfaceView mSurfaceView;


    private String mNewCallHandle;

    private String mLiveTitle = "宝宝的房间";

    private boolean isLandMode = false;


    @Override
    protected int getContentViewID() {
        return R.layout.activity_bell_live;
    }

    @Override
    protected void initViewAndListener() {
        JFGDPDevice device = DataSourceManager.getInstance().getJFGDevice(mUUID);
        if (device != null) {
            mLiveTitle = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
        }
        ViewUtils.updateViewHeight(fLayoutBellLiveHolder, 0.75f);
        dLayoutBellHotSeat.setOnDragReleaseListener(this);
        mVideoPlayController.setAction(this);
        fLayoutBellLiveHolder.setOnClickListener(view -> {
            if (!isLandMode) {
                handlePortClick();
            }
        });
        newCall();
        //三秒后隐藏状态栏
        mVideoViewContainer.removeCallbacks(mHideStatusBarAction);
        mVideoViewContainer.postDelayed(mHideStatusBarAction, 3000);
    }

    private void handlePortClick() {
        int visibility = mVideoViewContainer.getSystemUiVisibility();
        if (visibility != View.SYSTEM_UI_FLAG_VISIBLE) {//说明状态栏被隐藏了,则显示三秒后隐藏
            setNormalBackMargin();
            mVideoViewContainer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            mVideoViewContainer.removeCallbacks(mHideStatusBarAction);
            mVideoViewContainer.postDelayed(mHideStatusBarAction, 3000);
        } else {//说明状态栏没有隐藏,则直接隐藏
            hideStatusBar();
        }
    }

    private Runnable mHideStatusBarAction = this::hideStatusBar;

    public void hideStatusBar() {
        mVideoViewContainer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setHideBackMargin();
    }

    private void setNormalBackMargin() {
        mBellLiveBack.setVisibility(View.VISIBLE);
        mBellLiveBack.animate().setDuration(200).translationY(0);
        mBellFlow.animate().setDuration(200).translationY(0);
    }

    private void setHideBackMargin() {
        mBellLiveBack.setVisibility(View.VISIBLE);
        mBellLiveBack.animate().setDuration(200).translationY(-getResources().getDimension(R.dimen.y21));
        mBellFlow.animate().setDuration(200).translationY(-getResources().getDimension(R.dimen.y20));
    }

    @Override
    protected BellLiveContract.Presenter onCreatePresenter() {
        return new BellLivePresenterImpl();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        newCall();
    }

    private void newCall() {
        mVideoPlayController.setState(LivePlayControlView.STATE_LOADING, null);
        imgvBellLiveCapture.setEnabled(false);
        imgvBellLiveSpeaker.setEnabled(false);
        String extra = getIntent().getStringExtra(JConstant.VIEW_CALL_WAY_EXTRA);
        long time = getIntent().getLongExtra(JConstant.VIEW_CALL_WAY_TIME, System.currentTimeMillis());
        CallablePresenter.Caller caller = new CallablePresenter.Caller();
        caller.caller = mUUID;
        caller.picture = extra;
        caller.callTime = time;
        mPresenter.newCall(caller);
        if (mSurfaceView != null && mSurfaceView instanceof GLSurfaceView) {
            ((GLSurfaceView) mSurfaceView).onResume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mSurfaceView != null && mSurfaceView instanceof GLSurfaceView) {
            ((GLSurfaceView) mSurfaceView).onPause();
            mVideoViewContainer.removeAllViews();
            mSurfaceView = null;
        }
    }

    @Override
    public void onScreenRotationChanged(boolean land) {
        isLandMode = land;

        handleScreenUpdate(!land);
        getWindow().getDecorView().post(() -> handleSystemBar(!land, 100));
    }

    @Override
    protected void onPrepareToExit(Action action) {
        mPresenter.dismiss();
        finishExt();
        action.actionDone();
    }

    private void handleScreenUpdate(final boolean port) {
        initLandView();
        fLayoutLandHolderRef.get()
                .setVisibility(port ? View.GONE : View.VISIBLE);
        if (port) {
            mBellLiveBack.removeCallbacks(mHideStatusBarAction);
            hideStatusBar();
            setHideBackMargin();
            ViewUtils.updateViewHeight(fLayoutBellLiveHolder, 0.75f);
            mBellLiveBack.setText(null);
            imgvBellLiveSwitchToLand.setVisibility(View.VISIBLE);
        } else {
            setHideBackMargin();
            ViewUtils.updateViewMatchScreenHeight(fLayoutBellLiveHolder);
            mBellLiveBack.setText(mLiveTitle);
            imgvBellLiveSwitchToLand.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.act_bell_live_back)
    public void bellBack() {
        super.onBackPressed();
    }


    /**
     * 初始化 Layer层view，横屏全屏时候，需要在上层
     */
    private void initLandView() {
        if (fLayoutLandHolderRef == null || fLayoutLandHolderRef.get() == null) {
            View view = LayoutInflater.from(getAppContext())
                    .inflate(R.layout.layout_bell_live_land_layer, null);
            if (view != null) {
                fLayoutLandHolderRef = new WeakReference<>(view);
                view.findViewById(R.id.imgv_bell_live_land_capture)
                        .setOnClickListener(this);
                view.findViewById(R.id.imgv_bell_live_land_hangup)
                        .setOnClickListener(this);
                mLandBellLiveSpeaker = (ImageView) view.findViewById(R.id.imgv_bell_live_land_mic);
                mLandBellLiveSpeaker.setOnClickListener(this);
            }
        }
        View v = fLayoutBellLiveHolder.findViewById(R.id.fLayout_bell_live_land_layer);
        if (v == null) {
            fLayoutBellLiveHolder.addView(fLayoutLandHolderRef.get());
        }

    }

    @Override
    public void onRelease(int side) {
        if (side == 0) {
            mPresenter.dismiss();
        } else {
            mPresenter.pickup();
        }
    }

    @Override
    public void onLoginState(int state) {

    }


    @OnClick({R.id.imgv_bell_live_capture,
            R.id.imgv_bell_live_hang_up,
            R.id.imgv_bell_live_speaker,
            R.id.imgv_bell_live_switch_to_land,
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgv_bell_live_capture:
            case R.id.imgv_bell_live_land_capture:
                mPresenter.capture();
                break;
            case R.id.imgv_bell_live_speaker:
            case R.id.imgv_bell_live_land_mic:
                mPresenter.switchSpeaker();
                break;
            case R.id.imgv_bell_live_land_hangup:
            case R.id.imgv_bell_live_hang_up:
                mPresenter.dismiss();

                break;
            case R.id.imgv_bell_live_switch_to_land:
                initLandView();
                ViewUtils.setRequestedOrientation(this,
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
        }
    }

    @Override
    public void onResolution(JFGMsgVideoResolution resolution) throws JfgException {
        initVideoView();
        JfgCmdInsurance.getCmd().enableRenderSingleRemoteView(true, mSurfaceView);
        mBellLiveVideoPicture.setVisibility(View.GONE);
        mVideoPlayController.setState(ILiveControl.STATE_IDLE, null);
        imgvBellLiveCapture.setEnabled(true);
        imgvBellLiveSpeaker.setEnabled(true);
    }

    @Override
    public void onFlowSpeed(int speed) {
        if (mBellFlow.getVisibility() != View.VISIBLE) {
            mBellFlow.setVisibility(View.VISIBLE);
        }
        mBellFlow.setText(String.format(Locale.getDefault(), "%sKb/s", speed));
    }

    @Override
    public void onLiveStop(int errId) {

    }

    @Override
    public void onListen() {
        dLayoutBellHotSeat.setVisibility(View.VISIBLE);
        fLayoutBellAfterLive.setVisibility(View.GONE);
        mBellLiveVideoPicture.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConnectDeviceTimeOut() {
        ToastUtil.showNegativeToast("连接门铃超时");
        mPresenter.dismiss();
    }

    @Override
    public void onPreviewPicture(String URL) {
        mBellLiveVideoPicture.setVisibility(View.VISIBLE);
        Glide.with(this).load(URL).into(mBellLiveVideoPicture);
    }


    public void onViewer() {
        dLayoutBellHotSeat.setVisibility(View.GONE);
        fLayoutBellAfterLive.setVisibility(View.VISIBLE);

    }


    @Override
    public void onSpeaker(boolean on) {
        if (mLandBellLiveSpeaker != null)
            mLandBellLiveSpeaker.setImageResource(on ? R.drawable.doorbell_icon_landscape_talk : R.drawable.doorbell_icon_landscape_no_talk);
        imgvBellLiveSpeaker.setImageResource(on ? R.drawable.doorbell_icon_talk : R.drawable.doorbell_icon_no_talk);
    }

    @Override
    public String onResolveViewLaunchType() {
        return getIntent().getStringExtra(JConstant.VIEW_CALL_WAY);
    }

    /**
     * 初始化videoView
     *
     * @return
     */
    private void initVideoView() {
        if (mSurfaceView == null) {
            mSurfaceView = (SurfaceView) VideoViewFactory.CreateRendererExt(false,
                    getAppContext(), true);
            mSurfaceView.setId("IVideoView".hashCode());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mSurfaceView.setLayoutParams(params);
            mVideoViewContainer.removeAllViews();
            mVideoViewContainer.addView(mSurfaceView);
        }
        AppLogger.i("initVideoView");
        mSurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        mSurfaceView.getHolder().setFormat(PixelFormat.OPAQUE);
    }


    @Override
    public void onPickup() {
        dLayoutBellHotSeat.setVisibility(View.GONE);
        fLayoutBellAfterLive.setVisibility(View.VISIBLE);

    }

    @Override
    public void onDismiss() {
        finishExt();
    }

    @Override
    public void onCallAnswerInOther() {
        finishExt();
        ToastUtil.showNegativeToast(getString(R.string.Tips_Call_Answer_In_Other));
    }

    @Override
    public void onNewCallWhenInLive(String person) {
        mNewCallHandle = showAlert(getString(R.string.Tips_New_Call_Coming), getString(R.string.Tips_New_Call_Come_Listen, person), getString(R.string.ANSWER), getString(R.string.IGNORE));
    }

    @Override
    public void onViewAction(int action, String handler, Object extra) {
        if (TextUtils.equals(mNewCallHandle, handler)) {
            switch (action) {
                case VIEW_ACTION_OK:
                    mPresenter.pickup();
                    break;
                case VIEW_ACTION_CANCEL:

                    break;
            }
        }
    }

    @Override
    public void clickImage(int state) {
        switch (state) {
            case ILiveControl.STATE_LOADING_FAILED:
                mPresenter.pickup();
                break;
        }
    }

    @Override
    public void clickText() {
    }
}
