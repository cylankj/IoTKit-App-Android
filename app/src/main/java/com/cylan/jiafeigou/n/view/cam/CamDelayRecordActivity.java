package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamDelayRecordContract;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.widget.RecordControllerView;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yzd on 16-12-15.
 */

public class CamDelayRecordActivity extends BaseFullScreenFragmentActivity<CamDelayRecordContract.Presenter>
        implements CamDelayRecordContract.View, SurfaceHolder.Callback {

    @BindView(R.id.act_delay_record_video_view_container)
    FrameLayout mVideoViewContainer;
    @BindView(R.id.act_delay_record_information)
    TextView mRecordInformation;
    @BindView(R.id.act_delay_record_play)
    TextView mRecordPlay;
    @BindView(R.id.act_delay_record_video_preview)
    ImageView mRecordPreview;
    @BindView(R.id.act_delay_record_time_interval)
    ImageView mRecordTimeIntervalOpt;
    @BindView(R.id.act_delay_record_time_duration)
    ImageView mRecordTimeDurationOpt;
    @BindView(R.id.act_delay_record_controller)
    RecordControllerView mControllerView;
    @BindView(R.id.act_delay_record_again)
    Button mRecordAgain;


    SurfaceView mRoundedTextureView;
    private WeakReference<DelayRecordTimeIntervalDialog> mTimeIntervalDialog;
    private WeakReference<DelayRecordTimeDurationDialog> mTimeDurationDialog;
    private int mRecordMode = 0;
    private int mRecordTime = 24;
    private long mRecordStartTime = -1;
    private long mRecordDuration = -1;
    private BeanCamInfo mCamInfo;
    private TextPaint mPreviewPaint;

    private static final int DELAY_RECORD_SETTING = -1;
    private static final int DELAY_RECORD_PROCESS = -2;
    private static final int DELAY_RECORD_FINISH = -3;
    private static final int DELAY_RECORD_PREVIEW = -4;
    private static final int DELAY_RECORD_RECORDING = -5;


    private int mDelayRecordState = DELAY_RECORD_SETTING;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_delay_record);
        ButterKnife.bind(this);
        initViewAndListener();
    }

    private void initViewAndListener() {
        mCamInfo = getIntent().getExtras().getParcelable(JConstant.KEY_DEVICE_ITEM_BUNDLE);
        basePresenter = new CamDelayRecordContract.Presenter(this);
        if (mCamInfo != null) {
            basePresenter.setCamInfo(mCamInfo);
            mCamInfo.deviceBase = getIntent().getParcelableExtra(DelayRecordGuideFragment.KEY_DEVICE_INFO);
            if (mCamInfo.cameraTimeLapsePhotography != null) {
                mRecordMode = mCamInfo.cameraTimeLapsePhotography.timePeriod;
                mRecordStartTime = mCamInfo.cameraTimeLapsePhotography.timeStart;
                mRecordDuration = mCamInfo.cameraTimeLapsePhotography.timeDuration;
            }
        }
        mPreviewPaint = new TextPaint();
        mPreviewPaint.setColor(Color.WHITE);
        initTimeIntervalDialog();
        initTimeDurationDialog();
        initVideoView();
    }


    /**
     * 检查设备是否处于待机状态,未处于待机状态则进行直播预览画面
     */
    private void showPreviewOrRecord() {
        if (mCamInfo != null && !mCamInfo.cameraStandbyFlag
                && (mDelayRecordState == DELAY_RECORD_SETTING || mDelayRecordState == DELAY_RECORD_PREVIEW) && mRecordStartTime == -1) {
            startPreview();//设备未处于待机状态,且未开始延时摄影,则进行直播预览
        } else if (mCamInfo != null && !mCamInfo.cameraStandbyFlag
                && (mDelayRecordState == DELAY_RECORD_SETTING || mDelayRecordState == DELAY_RECORD_RECORDING) && mRecordStartTime != -1) {
            startPreview();//因为现在没有数据,所以暂时显示直播预览画面
            startRecord();//现在还没有数据
        }
    }

    /**
     * 还没有开始延时摄影,先显示实时摄像头数据
     */
    private void startPreview() {
        try {
            JfgCmdInsurance.getCmd().playVideo(mCamInfo.deviceBase.uuid);
        } catch (JfgException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void setPresenter(CamDelayRecordContract.Presenter presenter) {
        basePresenter = presenter;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    @OnClick(R.id.act_delay_record_back)
    public void onBackPressed() {
        super.onBackPressed();
    }

    @OnClick(R.id.act_delay_record_time_interval)
    public void selectTimeInterval() {
        initTimeIntervalDialog();
        DelayRecordTimeIntervalDialog dialog = mTimeIntervalDialog.get();
        dialog.setValue(mRecordMode);
        dialog.show(getSupportFragmentManager(), DelayRecordTimeIntervalDialog.class.getName());
    }

    @OnClick(R.id.act_delay_record_time_duration)
    public void selectTimeDuration() {
        initTimeDurationDialog();
        DelayRecordTimeDurationDialog dialog = mTimeDurationDialog.get();
        dialog.setValue(mRecordMode);
        dialog.show(getSupportFragmentManager(), DelayRecordTimeDurationDialog.class.getName());
    }

    @OnClick(R.id.act_delay_record_controller)
    public void controller() {
        if (!mControllerView.isRecording()) {
            //开始录制视频
            startRecord();

        } else {
            //结束或者还没开始录制视频
            startPreview();
            basePresenter.restoreRecord();
        }
    }

    @OnClick(R.id.act_delay_record_again)
    public void recordAgain() {
        refreshLayout(DELAY_RECORD_PREVIEW);
    }


    /**
     * 更新时间提示条
     */
    @Override
    public void refreshRecordTime(long time) {
        if (time > 0) {
            //已经开始录制了,则显示剩余时间
            String remain = getString(R.string.Tap1_CameraFun_Timelapse_Countdown) +
                    TimeUtils.getHH_MM_Remain(mRecordDuration - time);
            mRecordInformation.setText(remain);
            mControllerView.setRecordTime(time);
        } else if (time == DELAY_RECORD_SETTING || time == DELAY_RECORD_PREVIEW) {
            //还没有开始录制,则现在当前设置的模式
            String content = getString(R.string.Tap1_CameraFun_Timelapse_Interval) + (mRecordMode == 0 ? 60 : 20)
                    + " " +
                    getString(R.string.Tap1_CameraFun_Timelapse_RecordTime) + (mRecordTime);
            mRecordInformation.setText(content);
        } else if (time == DELAY_RECORD_FINISH) {
            //录制并合成完成视频
            mRecordInformation.setText(R.string.Tap1_CameraFun_Timelapse_SynthesisTips);
        } else if (time == DELAY_RECORD_PROCESS) {
            //已完成录制,正在进行视频合成
            mRecordInformation.setText(R.string.delay_record_hint_information_3);
        } else if (time == DELAY_RECORD_RECORDING) {
            //yet do nothing
        }
    }

    private void initTimeIntervalDialog() {
        if (mTimeIntervalDialog == null || mTimeIntervalDialog.get() == null) {
            DelayRecordTimeIntervalDialog dialog = DelayRecordTimeIntervalDialog.newInstance(null);
            dialog.setAction(this::setTimeIntervalOption);
            mTimeIntervalDialog = new WeakReference<>(dialog);
        }
    }

    private void initTimeDurationDialog() {
        if (mTimeDurationDialog == null || mTimeDurationDialog.get() == null) {
            DelayRecordTimeDurationDialog dialog = DelayRecordTimeDurationDialog.newInstance(null);
            dialog.setAction(this::setTimeDurationOK);
            mTimeDurationDialog = new WeakReference<>(dialog);
        }
    }

    private void setTimeDurationOK(int id, Object value) {
        mRecordTime = (int) value;
        refreshRecordTime(DELAY_RECORD_SETTING);
    }

    private void setTimeIntervalOption(int id, Object value) {
        switch (id) {
            case R.id.dialog_record_rb_20s: {
                mRecordTimeIntervalOpt.setImageResource(R.drawable.delay_icon_20time);
                mRecordMode = 1;
                mRecordTime = 8;
                refreshRecordTime(DELAY_RECORD_SETTING);
            }
            break;
            case R.id.dialog_record_rb_60s: {
                mRecordTimeIntervalOpt.setImageResource(R.drawable.delay_icon_60time);
                mRecordMode = 0;
                mRecordTime = 24;
                refreshRecordTime(DELAY_RECORD_SETTING);
            }
            break;
        }
    }

    @Override
    public void onRecordFinished() {
        refreshLayout(DELAY_RECORD_PROCESS);
    }

    public void refreshLayout(int state) {
        switch (mDelayRecordState = state) {
            case DELAY_RECORD_FINISH: {
                mRecordAgain.setVisibility(View.VISIBLE);
                mRecordTimeIntervalOpt.setVisibility(View.GONE);
                mRecordTimeDurationOpt.setVisibility(View.GONE);
                mControllerView.setVisibility(View.GONE);
                mRecordPlay.setVisibility(View.VISIBLE);
                mRecordPreview.setVisibility(View.VISIBLE);
                refreshRecordTime(DELAY_RECORD_FINISH);
            }
            break;
            case DELAY_RECORD_SETTING: {
                mRecordAgain.setVisibility(View.GONE);
                mRecordTimeIntervalOpt.setVisibility(View.VISIBLE);
                mRecordTimeDurationOpt.setVisibility(View.VISIBLE);
                mControllerView.setVisibility(View.VISIBLE);
                mControllerView.restoreRecord();
                mRecordPlay.setVisibility(View.VISIBLE);
                mRecordPreview.setVisibility(View.VISIBLE);
                refreshRecordTime(DELAY_RECORD_SETTING);
            }
            break;
            case DELAY_RECORD_PROCESS: {
                mRecordAgain.setVisibility(View.GONE);
                mRecordTimeIntervalOpt.setVisibility(View.GONE);
                mRecordTimeDurationOpt.setVisibility(View.GONE);
                mControllerView.setVisibility(View.GONE);
                mRecordPlay.setVisibility(View.VISIBLE);
                mRecordPreview.setVisibility(View.VISIBLE);
                refreshRecordTime(DELAY_RECORD_PROCESS);
            }
            break;
            case DELAY_RECORD_PREVIEW: {
                mRecordAgain.setVisibility(View.GONE);
                mRecordTimeIntervalOpt.setVisibility(View.VISIBLE);
                mRecordTimeDurationOpt.setVisibility(View.VISIBLE);
                mControllerView.setVisibility(View.VISIBLE);
                mRecordPlay.setVisibility(View.GONE);
                mRecordPreview.setVisibility(View.GONE);
                refreshRecordTime(DELAY_RECORD_PREVIEW);
            }
            break;
            case DELAY_RECORD_RECORDING: {
                mRecordAgain.setVisibility(View.GONE);
                mRecordTimeIntervalOpt.setVisibility(View.GONE);
                mRecordTimeDurationOpt.setVisibility(View.GONE);
                mControllerView.setVisibility(View.VISIBLE);
                mRecordPlay.setVisibility(View.GONE);
                mRecordPreview.setVisibility(View.GONE);
                refreshRecordTime(DELAY_RECORD_RECORDING);
            }
            break;
        }
    }

    private void startRecord() {
        refreshLayout(DELAY_RECORD_RECORDING);
        if (mRecordDuration == -1) mRecordDuration = mRecordTime * 60 * 60 * 1000;
        mControllerView.setMaxTime(mRecordDuration);
        if (mRecordStartTime == -1) mRecordStartTime = System.currentTimeMillis();
        mControllerView.setRecordTime(System.currentTimeMillis() - mRecordStartTime);
        mControllerView.startRecord();
        basePresenter.startRecord(mRecordMode, mRecordStartTime, mRecordDuration);
    }


    @Override
    public void onResolution(JFGMsgVideoResolution resolution) throws JfgException {
        if (mDelayRecordState != DELAY_RECORD_RECORDING)//just for test
            refreshLayout(DELAY_RECORD_PREVIEW);
        mRoundedTextureView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        mRoundedTextureView.getHolder().setFormat(PixelFormat.OPAQUE);
        JfgCmdInsurance.getCmd().setRenderRemoteView(mRoundedTextureView);
    }

    /**
     * 这个方法必须在holder已经创建了之后才能调用
     */
    public void setDefaultPreview() {
        SurfaceHolder holder = mRoundedTextureView.getHolder();
        int width = holder.getSurfaceFrame().right - holder.getSurfaceFrame().left;
        int height = holder.getSurfaceFrame().bottom - holder.getSurfaceFrame().top;
        Canvas canvas = holder.lockCanvas();
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.delay_record_overlay);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), new Rect(0, 0, width, height), mPreviewPaint);
        holder.unlockCanvasAndPost(canvas);
    }

    /**
     * 初始化videoView
     *
     * @return
     */
    private void initVideoView() {
        if (mRoundedTextureView == null) {
            int pid = basePresenter.getCamInfo().deviceBase.pid;
            mRoundedTextureView = (SurfaceView) VideoViewFactory.CreateRendererExt(JFGRules.isNeedPanoramicView(pid),
                    getContext(), true);
            mRoundedTextureView.getHolder().addCallback(this);
            mRoundedTextureView.setId("IVideoView".hashCode());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mRoundedTextureView.setLayoutParams(params);
            mVideoViewContainer.removeAllViews();
            mVideoViewContainer.addView(mRoundedTextureView);
        }
        AppLogger.i("initVideoView");
    }

    private void refreshSurfaceView() {
        switch (mDelayRecordState) {
            case DELAY_RECORD_SETTING: {
                setDefaultPreview();
                showPreviewOrRecord();
            }
            break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPreviewAndRedord();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLayout(DELAY_RECORD_SETTING);
    }

    private void stopPreview() {
        if (mCamInfo != null && mCamInfo.deviceBase != null) {
            try {
                JfgCmdInsurance.getCmd().stopPlay(mCamInfo.deviceBase.uuid);
            } catch (JfgException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopPreviewAndRedord() {
        stopPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        refreshSurfaceView();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
