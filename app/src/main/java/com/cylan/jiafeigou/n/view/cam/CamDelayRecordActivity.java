package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamDelayRecordContract;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.widget.RecordControllerView;
import com.cylan.jiafeigou.widget.roundview.RoundedTextureView;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tv.danmaku.ijk.media.exo.IjkExoMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by yzd on 16-12-15.
 */

public class CamDelayRecordActivity extends BaseFullScreenFragmentActivity<CamDelayRecordContract.Presenter>
        implements CamDelayRecordContract.View, IMediaPlayer.OnPreparedListener, RoundedTextureView.SurfaceProvider, SurfaceHolder.Callback {

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

    private IjkExoMediaPlayer mMediaPlayer;
    private WeakReference<DelayRecordTimeIntervalDialog> mTimeIntervalDialog;
    private WeakReference<DelayRecordTimeDurationDialog> mTimeDurationDialog;
    private int mRecordMode = 0;
    private BeanCamInfo mCamInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_delay_record);
        ButterKnife.bind(this);
        initViewAndListener();
    }

    private void initViewAndListener() {
        mCamInfo = getIntent().getParcelableExtra(DelayRecordGuideFragment.KEY_DEVICE_INFO);
        if (mCamInfo.cameraTimeLapsePhotography != null)
            mRecordMode = mCamInfo.cameraTimeLapsePhotography.timePeriod;
        mMediaPlayer = new IjkExoMediaPlayer(this);
//        mRoundedTextureView.setSurfaceProvider(this);
        mRoundedTextureView.getHolder().addCallback(this);
        initTimeIntervalDialog();
        initTimeDurationDialog();
        checkDeviceState();
    }

    /**
     * 检查设备是否处于待机状态,未处于待机状态则进行直播预览画面
     */
    private void checkDeviceState() {
        if (mCamInfo.cameraTimeLapsePhotography != null && !mCamInfo.cameraStandbyFlag) {
            startPlayLiveVideo();//设备未处于待机状态,则进行直播预览
        }
    }

    /**
     * 还没有开始延时摄影,先显示实时摄像头数据
     */
    private void startPlayLiveVideo() {
        try {
            JfgCmdInsurance.getCmd().setRenderRemoteView(mRoundedTextureView);
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
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        mMediaPlayer.start();
    }

    @Override
    public void onSurfaceCreated(SurfaceTexture surface) {
        mMediaPlayer.setSurface(new Surface(surface));
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
        if (mControllerView.isChecked()) {
            //结束或者还没开始录制视频
            mRecordTimeIntervalOpt.setVisibility(View.VISIBLE);
            mRecordTimeDurationOpt.setVisibility(View.VISIBLE);
        } else {
            //开始录制视频
            mRecordTimeIntervalOpt.setVisibility(View.GONE);
            mRecordTimeDurationOpt.setVisibility(View.GONE);
        }
        mControllerView.setChecked(!mControllerView.isChecked());
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
    }

    private void setTimeIntervalOption(int id, Object value) {
        switch (id) {
            case R.id.dialog_record_rb_20s: {
                mRecordTimeIntervalOpt.setImageResource(R.drawable.delay_icon_20time);
                mRecordMode = 1;
            }
            break;
            case R.id.dialog_record_rb_60s: {
                mRecordTimeIntervalOpt.setImageResource(R.drawable.delay_icon_60time);
                mRecordMode = 0;
            }
            break;
        }
    }

    public void onRecordFinished() {
        mRecordAgain.setVisibility(View.VISIBLE);
        mRecordTimeIntervalOpt.setVisibility(View.GONE);
        mRecordTimeDurationOpt.setVisibility(View.GONE);
        mControllerView.setVisibility(View.GONE);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mMediaPlayer.reset();
        mMediaPlayer.setDisplay(holder);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setDataSource("http://yf.cylan.com.cn:82/Garfield/1045020208160b9706425470.mp4");
        mMediaPlayer.prepareAsync();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
