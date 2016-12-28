package com.cylan.jiafeigou.n.view.bell;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.BaseFullScreenActivity;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.bell.BellLivePresenterImpl;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.bell.DragLayout;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;

import java.lang.ref.WeakReference;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class BellLiveActivity extends BaseFullScreenActivity<BellLiveContract.Presenter, BellLiveContract.View>
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

    @Override
    protected int getContentViewID() {
        return R.layout.activity_bell_live;
    }

    @Override
    protected void initViewAndListener() {
        ViewUtils.updateViewHeight(fLayoutBellLiveHolder, 0.75f);
        ViewUtils.setViewMarginStatusBar(tvBellLiveFlow);
        dLayoutBellHotSeat.setOnDragReleaseListener(this);
    }

    @Override
    protected BellLiveContract.Presenter onCreatePresenter() {
        return new BellLivePresenterImpl();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //大大的蛋疼
        String callWay = getIntent().getStringExtra(JConstant.BELL_CALL_WAY);
        Object extra = getIntent().getParcelableExtra(JConstant.BELL_CALL_WAY_EXTRA);
        if (extra == null) extra = getIntent().getSerializableExtra(JConstant.BELL_CALL_WAY_EXTRA);
        Object extra1 = getIntent().getParcelableExtra(JConstant.KEY_DEVICE_ITEM_BUNDLE);
        mPresenter.onBellCall(callWay, extra, extra1);
    }

    @Override
    public void onScreenRotationChanged(boolean land) {
        handleScreenUpdate(!land);
        getWindow().getDecorView().post(() -> handleSystemBar(!land, 100));
    }

    @Override
    protected void onPrepareToExit(Action action) {
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
            View view = LayoutInflater.from(getApplicationContext())
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
        AppLogger.d("pick up? " + (side == 1));
        if (side == 0) {
            mPresenter.onDismiss();
            finishExt();
            return;
        }
        dLayoutBellHotSeat.setVisibility(View.GONE);
        fLayoutBellAfterLive.setVisibility(View.VISIBLE);
        mPresenter.onPickup();
    }

    @Override
    public void onLoginState(int state) {

    }


    @OnClick({R.id.imgv_bell_live_capture,
            R.id.imgv_bell_live_hang_up,
            R.id.imgv_bell_live_speaker,
            R.id.imgv_bell_live_switch_to_land})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgv_bell_live_capture:
                mPresenter.onCapture();
                break;
            case R.id.imgv_bell_live_hang_up:
                if (mPresenter != null)
                    mPresenter.onDismiss();
                finishExt();
                break;
            case R.id.imgv_bell_live_switch_to_land:
                initLandView();
                ViewUtils.setRequestedOrientation(this,
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case R.id.imgv_bell_live_speaker:
            case R.id.imgv_bell_live_land_mic:
                mPresenter.onSwitchSpeaker();
                break;
            case R.id.imgv_bell_live_land_capture:
                mPresenter.onCapture();
                break;
            case R.id.imgv_bell_live_land_hangup:
                Toast.makeText(getViewContext(), "hangup", Toast.LENGTH_SHORT).show();
                finishExt();
                break;
        }
    }

    @Override
    public void onResolution(JFGMsgVideoResolution resolution) throws JfgException {
        initVideoView();
        JfgCmdInsurance.getCmd().setRenderRemoteView(mSurfaceView);
        mBellLiveVideoPicture.setVisibility(View.GONE);
    }

    @Override
    public void onFlowSpeed(int speed) {
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
            mLandBellLiveSpeaker.setImageResource(on ? R.drawable.icon_mic_on : R.drawable.icon_mic_off);
        imgvBellLiveSpeaker.setImageResource(on ? R.drawable.icon_mic_on : R.drawable.icon_mic_off);
    }

    /**
     * 初始化videoView
     *
     * @return
     */
    private void initVideoView() {
        if (mSurfaceView == null) {
            mSurfaceView = (SurfaceView) VideoViewFactory.CreateRendererExt(false,
                    getViewContext(), true);
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


}
