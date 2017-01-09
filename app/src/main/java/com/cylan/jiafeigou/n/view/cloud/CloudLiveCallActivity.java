package com.cylan.jiafeigou.n.view.cloud;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveCallContract;
import com.cylan.jiafeigou.n.mvp.impl.cloud.CloudLiveCallPresenterImp;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.softkeyboard.util.ViewUtil;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.live.ILiveControl;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2017/1/6
 * 描述：
 */
public class CloudLiveCallActivity extends AppCompatActivity implements CloudLiveCallContract.View {

    @BindView(R.id.iv_call_user_image_head)
    ImageView ivCallUserImageHead;
    @BindView(R.id.ll_top_layout)
    LinearLayout llTopLayout;
    @BindView(R.id.tv_ignore_call)
    TextView tvIgnoreCall;
    @BindView(R.id.tv_accept_call)
    TextView tvAcceptCall;
    @BindView(R.id.tv_video_time)
    Chronometer tvVideoTime;
    @BindView(R.id.tv_connet_text)
    TextView tvConnetText;
    @BindView(R.id.tv_loading)
    TextView tvLoading;
    @BindView(R.id.iv_hang_up)
    ImageView ivHangUp;
    @BindView(R.id.ll_myself_video)
    LinearLayout llMyselfVideo;
    @BindView(R.id.root_fl_video)
    FrameLayout rootFlVideoContainer;
    @BindView(R.id.rl_call_in_container)
    RelativeLayout rlCallInContainer;
    @BindView(R.id.fl_video_container)
    FrameLayout flVideoContainer;

    private CloudLiveCallContract.Presenter presenter;
    private SurfaceView mRenderSurfaceView;
    private SurfaceView mLocalSurfaceView;
    private String uuid;
    private boolean callInOrOut = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_cloud_live_videochat_connect);
        ButterKnife.bind(this);
        this.uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        this.callInOrOut = getIntent().getBooleanExtra("call_in_or_out", false);
        initPresenter();
    }

    private void initPresenter() {
        presenter = new CloudLiveCallPresenterImp(this, uuid);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (presenter != null) presenter.start();
        initView();
        if (!callInOrOut){
            presenter.onCloudCallOut();
        }
    }

    private void initView() {
        if (callInOrOut) {
            rlCallInContainer.setVisibility(View.VISIBLE);
            rootFlVideoContainer.setVisibility(View.GONE);
        } else {
            rlCallInContainer.setVisibility(View.GONE);
            rootFlVideoContainer.setVisibility(View.VISIBLE);
        }
    }

    @OnClick({R.id.tv_ignore_call, R.id.tv_accept_call, R.id.iv_hang_up})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_ignore_call:
                finish();
                break;
            case R.id.tv_accept_call:
                rlCallInContainer.setVisibility(View.GONE);
                rootFlVideoContainer.setVisibility(View.VISIBLE);
                presenter.onCloudCallOut();
                break;
            case R.id.iv_hang_up:
                finish();
                break;
        }
    }

    /**
     * 响应设备分辨率回调
     * @param resolution
     * @throws JfgException
     */
    @Override
    public void onResolution(JFGMsgVideoResolution resolution) throws JfgException {
        hideLoadingView();
        initLocalVideoView();
        initRenderVideoView();
        JfgCmdInsurance.getCmd().setRenderLocalView(mLocalSurfaceView);
        JfgCmdInsurance.getCmd().setRenderRemoteView(mRenderSurfaceView);
    }

    private void initLocalVideoView() {
        if (mLocalSurfaceView == null){
            mLocalSurfaceView = (SurfaceView) VideoViewFactory.CreateRendererExt(false,
                    ContextUtils.getContext(), true);
            mLocalSurfaceView.setId("IVideoView".hashCode());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.width = ViewUtils.dp2px(86);
            params.height = ViewUtils.dp2px(132);
            mLocalSurfaceView.setLayoutParams(params);
            llMyselfVideo.removeAllViews();
            llMyselfVideo.addView(mLocalSurfaceView);
        }
        AppLogger.i("initRenderVideoView");
        mLocalSurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        mLocalSurfaceView.getHolder().setFormat(PixelFormat.OPAQUE);
    }

    private void initRenderVideoView() {
        if (mRenderSurfaceView == null) {
            mRenderSurfaceView = (SurfaceView) VideoViewFactory.CreateRendererExt(false,
                    ContextUtils.getContext(), true);
            mRenderSurfaceView.setId("IVideoView".hashCode());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mRenderSurfaceView.setLayoutParams(params);
            flVideoContainer.removeAllViews();
            flVideoContainer.addView(mRenderSurfaceView);
        }
        AppLogger.i("initRenderVideoView");
        mRenderSurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        mRenderSurfaceView.getHolder().setFormat(PixelFormat.OPAQUE);

    }

    @Override
    public void setPresenter(CloudLiveCallContract.Presenter presenter) {

    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public void showLoadingView() {
        tvLoading.setVisibility(View.VISIBLE);
        tvConnetText.setVisibility(View.VISIBLE);
        tvVideoTime.setVisibility(View.INVISIBLE);
        tvVideoTime.stop();
        llMyselfVideo.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideLoadingView() {
        tvLoading.setVisibility(View.INVISIBLE);
        tvConnetText.setVisibility(View.INVISIBLE);
        tvVideoTime.setVisibility(View.VISIBLE);
        tvVideoTime.setBase(SystemClock.elapsedRealtime());
        tvVideoTime.start();
        llMyselfVideo.setVisibility(View.VISIBLE);
    }

    @Override
    public void setLoadingText(String text) {
        tvLoading.setText(text);
    }

    @Override
    public void onLiveStop(int code) {
        switch (code){
            case JFGRules.PlayErr.ERR_NERWORK:
                ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR_1));
                break;
            case JFGRules.PlayErr.ERR_UNKOWN:
                ToastUtil.showNegativeToast("出错了");
                break;
            case JFGRules.PlayErr.ERR_LOW_FRAME_RATE:
                ToastUtil.showNegativeToast("\"帧率太低,不足以播放,重试\"");
                break;
            case JFGRules.PlayErr.ERR_DEVICE_OFFLINE:
            case JError.ErrorVideoPeerNotExist:
                ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR));
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (presenter != null){
            presenter.stopPlayVideo();
            presenter.stop();
        }
    }
}
