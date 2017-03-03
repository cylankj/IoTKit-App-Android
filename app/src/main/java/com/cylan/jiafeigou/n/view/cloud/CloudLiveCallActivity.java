package com.cylan.jiafeigou.n.view.cloud;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseDbBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveCallInBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveCallOutBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;

import java.lang.ref.WeakReference;

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
    private boolean isCallIn = false;
    private static WeakReference<CloudMesgBackListener> callBackWeakRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_cloud_live_videochat_connect);
        ButterKnife.bind(this);
        this.uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        this.isCallIn = getIntent().getBooleanExtra("call_in_or_out", false);
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
        if (!isCallIn) {
            presenter.onCloudCallConnettion();
        }
    }

    private void initView() {
        if (isCallIn) {
            rlCallInContainer.setVisibility(View.VISIBLE);
            rootFlVideoContainer.setVisibility(View.GONE);
        } else {
            rlCallInContainer.setVisibility(View.GONE);
            rootFlVideoContainer.setVisibility(View.VISIBLE);
            presenter.countTime();
        }
    }

    @OnClick({R.id.tv_ignore_call, R.id.tv_accept_call, R.id.iv_hang_up})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_ignore_call:
                handlerCallingReuslt(JConstant.CLOUD_IN_CONNECT_FAILED);
                break;
            case R.id.tv_accept_call:
                rlCallInContainer.setVisibility(View.GONE);
                rootFlVideoContainer.setVisibility(View.VISIBLE);
                presenter.onCloudCallConnettion();
                break;
            case R.id.iv_hang_up:
                presenter.stopPlayVideo();
                handlerHangUpResultData();
                break;
        }
    }

    /**
     * 各种点击挂断处理
     */
    private void handlerHangUpResultData() {
        if (presenter.getIsConnectOk()) {
            if (isCallIn) {
                // 呼入连接成功挂断
                handlerCallingReuslt(JConstant.CLOUD_IN_CONNECT_OK);
            } else {
                // 呼出连接成功挂断
                handlerCallingReuslt(JConstant.CLOUD_OUT_CONNECT_OK);
            }
        } else {
            if (isCallIn) {
                // 呼入连接失败挂断
                handlerCallingReuslt(JConstant.CLOUD_IN_CONNECT_FAILED);
            } else {
                // 呼出连接失败挂断
                handlerCallingReuslt(JConstant.CLOUD_OUT_CONNECT_FAILED);
            }
        }
    }

    /**
     * 生成保存的消息bean
     *
     * @param type
     * @param videoLength
     * @param hasConnet
     * @return
     */
    private CloudLiveBaseBean createCloudBackBean(int type, String videoLength, boolean hasConnet) {
        CloudLiveBaseBean newBean = new CloudLiveBaseBean();
        newBean.setType(type);
        newBean.setUserIcon(presenter.getUserIcon());
        //添加到数据库
        CloudLiveBaseDbBean outDbBean = new CloudLiveBaseDbBean();
        outDbBean.setType(type);
        outDbBean.setUuid(uuid);
        switch (type) {
            case 1:
                CloudLiveCallInBean inLeaveBean = new CloudLiveCallInBean();
                inLeaveBean.setVideoLength(videoLength);
                inLeaveBean.setHasConnet(hasConnet);
                inLeaveBean.setVideoTime(presenter.parseTime(System.currentTimeMillis()));
                newBean.setData(inLeaveBean);
                outDbBean.setData(presenter.getSerializedObject(inLeaveBean));
                break;
            case 2:
                CloudLiveCallOutBean outLeaveBean = new CloudLiveCallOutBean();
                outLeaveBean.setVideoLength(videoLength);
                outLeaveBean.setHasConnet(hasConnet);
                outLeaveBean.setVideoTime(presenter.parseTime(System.currentTimeMillis()));
                newBean.setData(outLeaveBean);
                outDbBean.setData(presenter.getSerializedObject(outLeaveBean));
                break;
        }
        presenter.saveIntoDb(outDbBean);
        return newBean;
    }

    /**
     * 响应设备分辨率回调
     *
     * @param resolution
     * @throws JfgException
     */
    @Override
    public void onResolution(JFGMsgVideoResolution resolution) throws JfgException {
        initLocalVideoView();
        initRenderVideoView();
        checkPermission();
    }

    /**
     * 呼叫的结果的处理
     *
     * @param msgId
     */
    @Override
    public void handlerCallingReuslt(int msgId) {
        switch (msgId) {
            case JConstant.CLOUD_IN_CONNECT_TIME_OUT:          // 中控呼入app无应答
                CloudLiveBaseBean inBeanTimeOut = createCloudBackBean(1, "", false);
                if (callBackWeakRef != null && callBackWeakRef.get() != null) {
                    callBackWeakRef.get().onCloudMesgBack(inBeanTimeOut);
                }
                ToastUtil.showToast("未接通");
                break;

            case JConstant.CLOUD_OUT_CONNECT_TIME_OUT:            // 呼出超时
                CloudLiveBaseBean outBeanTimeOut = createCloudBackBean(2, tvVideoTime.getText().toString(), false);
                if (callBackWeakRef != null && callBackWeakRef.get() != null) {
                    callBackWeakRef.get().onCloudMesgBack(outBeanTimeOut);
                }
                ToastUtil.showToast("连接超时");
                break;

            case JConstant.CLOUD_IN_CONNECT_OK: //呼入连接成功 点击挂断按钮
                CloudLiveBaseBean newBean = createCloudBackBean(1, tvVideoTime.getText().toString(), true);
                if (callBackWeakRef != null && callBackWeakRef.get() != null) {
                    callBackWeakRef.get().onCloudMesgBack(newBean);
                }
                break;

            case JConstant.CLOUD_IN_CONNECT_FAILED:
                CloudLiveBaseBean newBeanF = createCloudBackBean(1, "", false);
                if (callBackWeakRef != null && callBackWeakRef.get() != null) {
                    callBackWeakRef.get().onCloudMesgBack(newBeanF);
                }
                break;

            case JConstant.CLOUD_OUT_CONNECT_OK:
                CloudLiveBaseBean outBean = createCloudBackBean(2, tvVideoTime.getText().toString(), true);
                if (callBackWeakRef != null && callBackWeakRef.get() != null) {
                    callBackWeakRef.get().onCloudMesgBack(outBean);
                }
                break;

            case JConstant.CLOUD_OUT_CONNECT_FAILED:
                CloudLiveBaseBean outBeanF = createCloudBackBean(2, "", false);
                if (callBackWeakRef != null && callBackWeakRef.get() != null) {
                    callBackWeakRef.get().onCloudMesgBack(outBeanF);
                }
                break;
        }
        finish();
    }

    /**
     * 回调
     *
     * @param inBeanTimeOut
     */
    private void callBackResult(CloudLiveBaseBean inBeanTimeOut) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("callbackBean", inBeanTimeOut);
        intent.putExtra("callback", bundle);
        setResult(1, intent);
    }

    private void initLocalVideoView() {
        if (mLocalSurfaceView == null) {
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

    public static void setOnCloudMesgBackListener(CloudMesgBackListener listener) {
        callBackWeakRef = new WeakReference<>(listener);
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
        switch (code) {
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

    /**
     * 检测权限
     */
    private void checkPermission() throws JfgException {
        if (ContextCompat.checkSelfPermission(CloudLiveCallActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(CloudLiveCallActivity.this,
                    Manifest.permission.CAMERA)) {
            } else {
                ActivityCompat.requestPermissions(CloudLiveCallActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        1);
            }
        } else {
//            JfgCmdInsurance.getCmd().enableCamera(true,true);
            JfgCmdInsurance.getCmd().enableRenderLocalView(true, mLocalSurfaceView);
            JfgCmdInsurance.getCmd().enableRenderSingleRemoteView(true, mRenderSurfaceView);
            hideLoadingView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            try {
//                JfgCmdInsurance.getCmd().enableCamera(true,true);
                JfgCmdInsurance.getCmd().enableRenderLocalView(true, mLocalSurfaceView);
                JfgCmdInsurance.getCmd().enableRenderSingleRemoteView(true, mRenderSurfaceView);
                hideLoadingView();
            } catch (JfgException e) {
                e.printStackTrace();
            }
        } else {
            ToastUtil.showToast("获取相机权限失败");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stopPlayVideo();
            presenter.stop();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isCallIn) {
            if (presenter.getIsConnectOk()) {
                handlerCallingReuslt(JConstant.CLOUD_IN_CONNECT_OK);
            } else {
                handlerCallingReuslt(JConstant.CLOUD_IN_CONNECT_FAILED);
            }
        } else {
            if (presenter.getIsConnectOk()) {
                handlerCallingReuslt(JConstant.CLOUD_OUT_CONNECT_OK);
            } else {
                handlerCallingReuslt(JConstant.CLOUD_OUT_CONNECT_FAILED);
            }
        }
    }
}
