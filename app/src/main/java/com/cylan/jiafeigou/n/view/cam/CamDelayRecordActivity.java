package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamDelayRecordContract;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.widget.RecordControllerView;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yzd on 16-12-15.
 */

public class CamDelayRecordActivity extends BaseFullScreenFragmentActivity<CamDelayRecordContract.Presenter>
        implements CamDelayRecordContract.View, SurfaceHolder.Callback {

    @BindView(R.id.act_delay_record_video_view)
    SurfaceView mRoundedTextureView;
    @BindView(R.id.act_delay_record_information)
    TextView mRecordInformation;
    @BindView(R.id.act_delay_record_play)
    TextView mRecordPlay;
    @BindView(R.id.act_delay_record_video_overlay)
    View mRecordViewOverlay;
    @BindView(R.id.act_delay_record_time_interval)
    ImageView mRecordTimeIntervalOpt;
    @BindView(R.id.act_delay_record_time_duration)
    ImageView mRecordTimeDurationOpt;
    @BindView(R.id.act_delay_record_controller)
    RecordControllerView mControllerView;
    @BindView(R.id.act_delay_record_again)
    Button mRecordAgain;

    private WeakReference<DelayRecordTimeIntervalDialog> mTimeIntervalDialog;
    private WeakReference<DelayRecordTimeDurationDialog> mTimeDurationDialog;
    private int mRecordMode = 0;
    private int mRecordTime = 24;
    private long mRecordStartTime;
    private long mRecordDuration;
    private BeanCamInfo mCamInfo;
    private TextPaint mPreviewPaint;

    private static final int DELAY_RECORD_SETTING = -1;
    private static final int DELAY_RECORD_PROCESS = -2;
    private static final int DELAY_RECORD_FINISH = -3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_delay_record);
        ButterKnife.bind(this);
        initViewAndListener();
    }

    private void initViewAndListener() {
        basePresenter = new CamDelayRecordContract.Presenter(this);
        mPreviewPaint = new TextPaint();
        mPreviewPaint.setColor(Color.WHITE);
        mCamInfo = getIntent().getParcelableExtra(DelayRecordGuideFragment.KEY_DEVICE_INFO);
        basePresenter.setCamInfo(mCamInfo);
        if (mCamInfo != null && mCamInfo.cameraTimeLapsePhotography != null) {
            mRecordMode = mCamInfo.cameraTimeLapsePhotography.timePeriod;
            mRecordStartTime = mCamInfo.cameraTimeLapsePhotography.timeStart;
            mRecordDuration = mCamInfo.cameraTimeLapsePhotography.timeDuration;
        }
        mRoundedTextureView.getHolder().addCallback(this);
        initTimeIntervalDialog();
        initTimeDurationDialog();
        checkDeviceState();
        refreshRecordTime(DELAY_RECORD_SETTING);
    }

    /**
     * 检查设备是否处于待机状态,未处于待机状态则进行直播预览画面
     */
    private void checkDeviceState() {
        if (mCamInfo != null && mCamInfo.cameraTimeLapsePhotography != null && !mCamInfo.cameraStandbyFlag) {
            startPlayLiveVideo();//设备未处于待机状态,则进行直播预览
        }
    }

    /**
     * 还没有开始延时摄影,先显示实时摄像头数据
     */
    private void startPlayLiveVideo() {
        try {
            JfgCmdInsurance.getCmd().playVideo(mCamInfo.deviceBase.uuid);
        } catch (JfgException e) {
            e.printStackTrace();
        }
    }

    /**
     * 已经开始延时摄影,显示最新的拍照图片
     */
    private void startShowNewPicture() {

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
            mRecordTimeIntervalOpt.setVisibility(View.GONE);
            mRecordTimeDurationOpt.setVisibility(View.GONE);
            mControllerView.setMaxTime(mRecordTime * 60 * 60 * 1000);
            mControllerView.startRecord();
            basePresenter.startRecord(mRecordMode, mRecordStartTime, mRecordDuration);
        } else {
            //结束或者还没开始录制视频
            mRecordTimeIntervalOpt.setVisibility(View.VISIBLE);
            mRecordTimeDurationOpt.setVisibility(View.VISIBLE);
            mControllerView.restoreRecord();
            basePresenter.restoreRecord();
        }
    }

    @OnClick(R.id.act_delay_record_again)
    public void recordAgain() {
        refreshLayout(DELAY_RECORD_SETTING);
    }


    /**
     * 更新时间提示条
     */
    @Override
    public void refreshRecordTime(long time) {
        if (time > 0) {
            //已经开始录制了,则显示剩余时间
            mRecordInformation.setText(getString(R.string.delay_record_hint_information_1, TimeUtils.getHH_MM_Remain(time)));
            mControllerView.setRecordTime(time);
        } else if (time == DELAY_RECORD_SETTING) {
            //还没有开始录制,则现在当前设置的模式
            mRecordInformation.setText(getString(R.string.delay_record_hint_information_0, mRecordMode == 0 ? 60 : 20, mRecordTime));
        } else if (time == DELAY_RECORD_FINISH) {
            mRecordInformation.setText(R.string.delay_record_hint_information_2);
        } else if (time == DELAY_RECORD_PROCESS) {
            mRecordInformation.setText(R.string.delay_record_hint_information_3);
        }
    }

    private void initTimeIntervalDialog() {
        if (mTimeIntervalDialog == null || mTimeIntervalDialog.get() == null) {
            //初始化
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
        switch (state) {
            case DELAY_RECORD_FINISH: {
                mRecordAgain.setVisibility(View.VISIBLE);
                mRecordTimeIntervalOpt.setVisibility(View.GONE);
                mRecordTimeDurationOpt.setVisibility(View.GONE);
                mControllerView.setVisibility(View.GONE);
                refreshRecordTime(DELAY_RECORD_FINISH);
            }
            break;
            case DELAY_RECORD_SETTING: {
                mRecordAgain.setVisibility(View.GONE);
                mRecordTimeIntervalOpt.setVisibility(View.VISIBLE);
                mRecordTimeDurationOpt.setVisibility(View.VISIBLE);
                mControllerView.setVisibility(View.VISIBLE);
                mControllerView.restoreRecord();
                refreshRecordTime(DELAY_RECORD_SETTING);
            }
            break;
            case DELAY_RECORD_PROCESS: {
                mRecordAgain.setVisibility(View.GONE);
                mRecordTimeIntervalOpt.setVisibility(View.GONE);
                mRecordTimeDurationOpt.setVisibility(View.GONE);
                mControllerView.setVisibility(View.GONE);
                mControllerView.restoreRecord();
                refreshRecordTime(DELAY_RECORD_PROCESS);
            }
            break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setDefaultPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onResolution(JFGMsgVideoResolution resolution) throws JfgException {
        JfgCmdInsurance.getCmd().setRenderRemoteView(mRoundedTextureView);
        onReadyToPreview();
    }

    public void onReadyToPreview() {
        mRecordPlay.setVisibility(View.GONE);
        mRecordViewOverlay.setVisibility(View.GONE);
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
}
