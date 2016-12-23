package com.cylan.jiafeigou.n.view.bell;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.bell.BellLivePresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.BeanBellInfo;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.bell.DragLayout;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;

import java.lang.ref.WeakReference;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BellLiveActivity extends ProcessActivity
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

    @BindView(R.id.act_bell_live_video_view_container)
    FrameLayout mVideoViewContainer;

    /**
     * 水平方向的view
     */
    private WeakReference<View> fLayoutLandHolderRef;

    private BellLiveContract.Presenter presenter;
    private SurfaceView mSurfaceView;
    private BeanBellInfo mBellInfo;
    private boolean mIsMikeOn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bell_live);
        ButterKnife.bind(this);
        initPresenter();
        initView();
        initTextData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.start();
        showListenOrViewerView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        final int orientation = this.getResources().getConfiguration().orientation;
        switch (orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                // 加入横屏要处理的代码
                handleScreenUpdate(false);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                // 加入竖屏要处理的代码
                handleScreenUpdate(true);
                break;
        }
        final boolean isLandScape = this.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                handleSystemBar(!isLandScape, 100);
            }
        });
    }

    @Override
    public void onBackPressed() {
        final boolean isLandScape = this.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
        if (isLandScape) {
            ViewUtils.setRequestedOrientation(this, Configuration.ORIENTATION_PORTRAIT);
            return;
        }
        finishExt();
    }


    private void handleScreenUpdate(final boolean port) {
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
        initLandView();
        fLayoutLandHolderRef.get()
                .setVisibility(port ? View.GONE : View.VISIBLE);
    }

    private void initView() {
        ViewUtils.updateViewHeight(fLayoutBellLiveHolder, 0.75f);
        ViewUtils.setViewMarginStatusBar(tvBellLiveFlow);
        dLayoutBellHotSeat.setOnDragReleaseListener(this);
    }

    private void initTextData() {
        Intent intent = getIntent();
        final String content = intent.getStringExtra("text");
        AppLogger.d("content: " + content);
    }

    private void initPresenter() {
        basePresenter = new BellLivePresenterImpl(this);
        mBellInfo = getIntent().getParcelableExtra(JConstant.KEY_DEVICE_ITEM_BUNDLE);
        mBellInfo.deviceBase = getIntent().getParcelableExtra("extra");
        presenter.setBellInfo(mBellInfo);

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
                view.findViewById(R.id.imgv_bell_live_land_mic)
                        .setOnClickListener(this);
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
            presenter.onDismiss();
            finishExt();
            return;
        }
        dLayoutBellHotSeat.setVisibility(View.GONE);
        fLayoutBellAfterLive.setVisibility(View.VISIBLE);
        presenter.onPickup();
    }

    @Override
    public void onLoginState(int state) {

    }

    @Override
    public void setPresenter(BellLiveContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @OnClick({R.id.imgv_bell_live_capture,
            R.id.imgv_bell_live_hang_up,
            R.id.imgv_bell_live_speaker,
            R.id.imgv_bell_live_switch_to_land})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgv_bell_live_capture:
                presenter.onCapture();
                break;
            case R.id.imgv_bell_live_hang_up:
                if (presenter != null)
                    presenter.onDismiss();
                finishExt();
                break;
            case R.id.imgv_bell_live_speaker:
                mIsMikeOn = !mIsMikeOn;
                imgvBellLiveSpeaker.setImageResource(mIsMikeOn ? R.drawable.icon_mic_on : R.drawable.icon_mic_off);
                presenter.onMike(mIsMikeOn ? 1 : 0);
                break;
            case R.id.imgv_bell_live_switch_to_land:
                initLandView();
                ViewUtils.setRequestedOrientation(this,
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case R.id.imgv_bell_live_land_mic:
                break;
            case R.id.imgv_bell_live_land_capture:
                presenter.onCapture();
                break;
            case R.id.imgv_bell_live_land_hangup:
                Toast.makeText(getContext(), "hangup", Toast.LENGTH_SHORT).show();
                finishExt();
                break;
        }
    }

    public void showListenOrViewerView() {
        String callWay = getIntent().getStringExtra(JConstant.BELL_CALL_WAY);
        switch (callWay) {
            case JConstant.BELL_CALL_WAY_VIEWER: {
                dLayoutBellHotSeat.setVisibility(View.GONE);
                fLayoutBellAfterLive.setVisibility(View.VISIBLE);
                presenter.onPickup();
            }
            break;
            case JConstant.BELL_CALL_WAY_LISTEN: {
                dLayoutBellHotSeat.setVisibility(View.VISIBLE);
                fLayoutBellAfterLive.setVisibility(View.GONE);
            }
            break;
        }

    }

    @Override
    public void onResolution(JFGMsgVideoResolution resolution) throws JfgException {
        initVideoView();
        JfgCmdInsurance.getCmd().setRenderRemoteView(mSurfaceView);
        presenter.onMike(mIsMikeOn ? 1 : 0);
    }

    @Override
    public void onFlowSpeedRefresh(int speed) {
        tvBellLiveFlow.setText(String.format(Locale.getDefault(), "%sKb/s", speed));
    }

    /**
     * 初始化videoView
     *
     * @return
     */
    private void initVideoView() {
        if (mSurfaceView == null) {
            int pid = presenter.getBellInfo().deviceBase.pid;
            mSurfaceView = (SurfaceView) VideoViewFactory.CreateRendererExt(JFGRules.isNeedPanoramicView(pid),
                    getContext(), true);
            mSurfaceView.setId("IVideoView".hashCode());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mSurfaceView.setLayoutParams(params);
            mVideoViewContainer.removeAllViews();
            mVideoViewContainer.addView(mSurfaceView);
        }
        AppLogger.i("initVideoView");
    }


}
