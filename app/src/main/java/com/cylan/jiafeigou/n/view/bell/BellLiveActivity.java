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
import com.cylan.jiafeigou.base.view.CallablePresenter;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.bell.BellLivePresenterImpl;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.bell.DragLayout;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;

import java.lang.ref.WeakReference;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class BellLiveActivity extends BaseFullScreenActivity<BellLiveContract.Presenter>
        implements DragLayout.OnDragReleaseListener, View.OnClickListener
        , BellLiveContract.View {

    @BindView(R.id.fLayout_bell_live_holder)
    FrameLayout fLayoutBellLiveHolder;
    @BindView(R.id.tv_bell_live_flow)
    TextView tvBellLiveFlow;
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

    private ImageView mLandBellLiveSpeaker;
    /**
     * 水平方向的view
     */
    private WeakReference<View> fLayoutLandHolderRef;

    private SurfaceView mSurfaceView;


    private String mNewCallHandle;

    @Override
    protected int getContentViewID() {
        return R.layout.activity_bell_live;
    }

    @Override
    protected void initViewAndListener() {
        ViewUtils.updateViewHeight(fLayoutBellLiveHolder, 0.75f);
        ViewUtils.setViewMarginStatusBar(tvBellLiveFlow);
        dLayoutBellHotSeat.setOnDragReleaseListener(this);
        newCall();
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
            ViewUtils.updateViewHeight(fLayoutBellLiveHolder, 0.75f);
            ViewUtils.setViewMarginStatusBar(tvBellLiveFlow);
            imgvBellLiveSwitchToLand.setVisibility(View.VISIBLE);
        } else {
            ViewUtils.updateViewMatchScreenHeight(fLayoutBellLiveHolder);
            ViewUtils.clearViewMarginStatusBar(tvBellLiveFlow);
            tvBellLiveFlow.bringToFront();
            imgvBellLiveSwitchToLand.setVisibility(View.GONE);
        }

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
                view.findViewById(R.id.tv_bell_live_land_back).setOnClickListener(this);
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
            case R.id.tv_bell_live_land_back:
                super.onBackPressed();
        }
    }

    @Override
    public void onResolution(JFGMsgVideoResolution resolution) throws JfgException {
        initVideoView();
        JfgCmdInsurance.getCmd().enableRenderSingleRemoteView(true, mSurfaceView);
        mBellLiveVideoPicture.setVisibility(View.GONE);
    }

    @Override
    public void onFlowSpeed(int speed) {
        if (tvBellLiveFlow.getVisibility() != View.VISIBLE) {
            tvBellLiveFlow.setVisibility(View.VISIBLE);
        }
        tvBellLiveFlow.setText(String.format(Locale.getDefault(), "%sKb/s", speed));
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
            mLandBellLiveSpeaker.setImageResource(on ? R.drawable.doorbell_icon_talk : R.drawable.doorbell_icon_no_talk);
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
        mNewCallHandle = showAlert(getString(R.string.Tips_New_Call_Coming), getString(R.string.Tips_New_Call_Come_Listen, person), getString(R.string.listen), getString(R.string.ignore));
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
}
