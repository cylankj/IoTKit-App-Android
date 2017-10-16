package com.cylan.jiafeigou.n.view.record;

import android.os.Bundle;
import android.support.v7.widget.CardView;
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
import com.cylan.jiafeigou.base.injector.component.FragmentComponent;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamDelayRecordContract;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.RecordControllerView;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by yzd on 16-12-15.
 */

public class DelayRecordMainFragment extends BaseFragment<CamDelayRecordContract.Presenter>
        implements CamDelayRecordContract.View {
    @BindView(R.id.header_delay_record_back)
    ImageView mHeaderBack;
    @BindView(R.id.fragment_delay_record_video_view_container)
    FrameLayout mVideoViewContainer;
    @BindView(R.id.fragment_delay_record_video_preview)
    ImageView mVideoPreview;
    @BindView(R.id.fragment_delay_record_play)
    TextView mPlay;
    @BindView(R.id.fragment_delay_record_information)
    TextView mInformationText;
    @BindView(R.id.act_delay_record_video_container)
    CardView mVideoContainer;
    @BindView(R.id.fragment_delay_record_time_interval)
    ImageView mTimeInterval;
    @BindView(R.id.fragment_delay_record_controller)
    RecordControllerView mController;
    @BindView(R.id.fragment_delay_record_again)
    Button mPlayAgain;
    @BindView(R.id.fragment_delay_record_time_duration)
    ImageView mTimeDuration;
    @BindView(R.id.fragment_delay_record_video_overlay)
    View mVideoOverlay;

    @BindView(R.id.header_delay_record_container)
    ViewGroup mHeaderContainer;
    private WeakReference<DelayRecordTimeIntervalDialog> mTimeIntervalDialog;
    private WeakReference<DelayRecordTimeDurationDialog> mTimeDurationDialog;
    private int mRecordBeginTime = -1;
    private int mRecordTimeCycle = -1;
    private int mRecordTimeDuration = -1;
    private int mRecordStatus = -1;

    private boolean isStandBy = false;

    private static final int DELAY_RECORD_SETTING = -1;
    private static final int DELAY_RECORD_PROCESS = -2;
    private static final int DELAY_RECORD_FINISH = -3;
    private static final int DELAY_RECORD_PREVIEW = -4;
    private static final int DELAY_RECORD_RECORDING = -5;


    private int mDelayRecordState = DELAY_RECORD_SETTING;

    public static DelayRecordMainFragment newInstance(String uuid) {
        DelayRecordMainFragment fragment = new DelayRecordMainFragment();
        Bundle bundle = new Bundle();
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getContentViewID() {
        return R.layout.fragment_delay_record;
    }

    @Override
    protected void initViewAndListener() {
        initTimeIntervalDialog();
        initTimeDurationDialog();
        ViewUtils.setViewMarginStatusBar(mHeaderContainer);
        setPreferenceValues();
    }

    private void setPreferenceValues() {
        //无论以哪种方式进入该页面,都不应该再显示引导页了
        PreferencesUtils.putBoolean(JConstant.KEY_DELAY_RECORD_GUIDE, false);
    }


    @OnClick(R.id.fragment_delay_record_time_interval)
    public void selectTimeInterval() {
        initTimeIntervalDialog();
        DelayRecordTimeIntervalDialog dialog = mTimeIntervalDialog.get();
        dialog.setValue(mRecordTimeCycle);
        dialog.show(getChildFragmentManager(), DelayRecordTimeIntervalDialog.class.getName());
    }

    @OnClick(R.id.fragment_delay_record_time_duration)
    public void selectTimeDuration() {
        initTimeDurationDialog();
        DelayRecordTimeDurationDialog dialog = mTimeDurationDialog.get();
        dialog.setValue(mRecordTimeCycle);
        dialog.show(getChildFragmentManager(), DelayRecordTimeDurationDialog.class.getName());
    }

    @OnClick(R.id.fragment_delay_record_controller)
    public void controller() {
        if (!mController.isRecording()) {
            //开始录制视频
            startRecord();
        } else {
            //结束或者还没开始录制视频
//            presenter.startViewer();
            presenter.restoreRecord();
        }
    }

    @OnClick(R.id.header_delay_record_back)
    public void back() {
        getActivity().onBackPressed();
    }

    @OnClick(R.id.fragment_delay_record_again)
    public void recordAgain() {
        refreshLayout();
    }


    /**
     * 更新时间提示条
     */
    @Override
    public void refreshRecordTime(long time) {
        if (time > 0) {
            //已经开始录制了,则显示剩余时间
            String remain = getString(R.string.Tap1_CameraFun_Timelapse_Countdown) +
                    TimeUtils.getHH_MM_Remain(mRecordTimeDuration - time);
            mInformationText.setText(remain);
            mController.setRecordTime(time);
        } else if (time == DELAY_RECORD_SETTING || time == DELAY_RECORD_PREVIEW) {
            //还没有开始录制,则现在当前设置的模式
            String content = getString(R.string.Tap1_CameraFun_Timelapse_Interval) + mRecordTimeCycle
                    + " " +
                    getString(R.string.Tap1_CameraFun_Timelapse_RecordTime) + (mRecordTimeDuration / 3600);
            mInformationText.setText(content);
        } else if (time == DELAY_RECORD_FINISH) {
            //录制并合成完成视频
            mInformationText.setText(R.string.Tap1_CameraFun_Timelapse_SynthesisTips);
        } else if (time == DELAY_RECORD_PROCESS) {
            //已完成录制,正在进行视频合成
            mInformationText.setText(R.string.Tap1_CameraFun_Timelapse_Synthesis);
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
        mRecordTimeDuration = (int) value * 3600;
        refreshRecordTime(DELAY_RECORD_SETTING);
    }

    @Override
    public void onMarkRecordInformation(int interval, int recordDuration, int remainTime) {

    }

    private void setTimeIntervalOption(int id, Object value) {
        switch (id) {
            case R.id.dialog_record_rb_20s: {
                mTimeInterval.setImageResource(R.drawable.delay_icon_20time);
                mRecordTimeCycle = 20;
                mRecordTimeDuration = 8 * 60 * 60;
                refreshRecordTime(DELAY_RECORD_SETTING);
            }
            break;
            case R.id.dialog_record_rb_60s: {
                mTimeDuration.setImageResource(R.drawable.delay_icon_60time);
                mRecordTimeCycle = 60;
                mRecordTimeDuration = 24 * 60 * 60;
                refreshRecordTime(DELAY_RECORD_SETTING);
            }
            break;
        }
    }

    @Override
    public void onRecordFinished() {
        refreshLayout();
    }

    public void refreshLayout() {
        if (mRecordStatus == 2) {//视频已经合成完成
            mPlayAgain.setVisibility(View.VISIBLE);
            mTimeInterval.setVisibility(View.GONE);
            mTimeInterval.setVisibility(View.GONE);
            mController.setVisibility(View.GONE);
            mPlay.setVisibility(View.GONE);
            mVideoPreview.setVisibility(View.VISIBLE);
            mVideoOverlay.setVisibility(View.VISIBLE);
            refreshRecordTime(DELAY_RECORD_FINISH);
        } else if (mRecordStatus == 1 && (mRecordBeginTime + mRecordTimeDuration) * 1000L < System.currentTimeMillis()) {//正在合成视频
            mPlayAgain.setVisibility(View.GONE);
            mTimeInterval.setVisibility(View.GONE);
            mTimeDuration.setVisibility(View.GONE);
            mController.setVisibility(View.GONE);
            mPlay.setVisibility(View.VISIBLE);
            mVideoPreview.setVisibility(View.VISIBLE);
            mVideoOverlay.setVisibility(View.VISIBLE);
            refreshRecordTime(DELAY_RECORD_PROCESS);

        } else if (mRecordStatus == 1) {//正在录制中
            mPlayAgain.setVisibility(View.GONE);
            mTimeInterval.setVisibility(View.GONE);
            mTimeDuration.setVisibility(View.GONE);
            mController.setVisibility(View.VISIBLE);
            mPlay.setVisibility(View.GONE);
            mVideoPreview.setVisibility(View.VISIBLE);
            mVideoOverlay.setVisibility(View.GONE);
            refreshRecordTime(DELAY_RECORD_RECORDING);
        } else if (mRecordStatus == 0) {//视频被终止

        } else if (mRecordStatus == -2) {//未开始且未待机
            mPlayAgain.setVisibility(View.GONE);
            mTimeInterval.setVisibility(View.VISIBLE);
            mTimeDuration.setVisibility(View.VISIBLE);
            mController.setVisibility(View.VISIBLE);
            mPlay.setVisibility(View.GONE);
            mVideoPreview.setVisibility(View.GONE);
            mVideoOverlay.setVisibility(View.GONE);
            mController.restoreRecord();
            refreshRecordTime(DELAY_RECORD_PREVIEW);
        }
    }

    private void startRecord() {
        mController.setMaxTime(mRecordTimeDuration);
        mController.setRecordTime(System.currentTimeMillis() / 1000 - mRecordBeginTime);
        mController.startRecord();
        presenter.startRecord(mRecordTimeCycle, (int) (System.currentTimeMillis() / 1000), mRecordTimeDuration);
        refreshLayout();
    }


    @Override
    public void onViewer() {

    }

    @Override
    public void onDismiss() {

    }

    @Override
    public void onSpeaker(boolean on) {

    }

    @Override
    public void onMicrophone(boolean on) {

    }

    @Override
    public void onResolution(JFGMsgVideoResolution resolution) throws JfgException {

        SurfaceView surfaceView = presenter.getViewerInstance();
        mVideoViewContainer.removeAllViews();
        mVideoViewContainer.addView(surfaceView);
        appCmd.enableRenderSingleRemoteView(true, surfaceView);
        mRecordStatus = -2;
        refreshLayout();
    }

    @Override
    public void onFlowSpeed(int speed) {
    }

    @Override
    public void onConnectDeviceTimeOut() {

    }

    @Override
    public void onVideoDisconnect(int code) {

    }

    @Override
    public void onDeviceUnBind() {

    }

    @Override
    public void onLoading(boolean loading) {

    }

    @Override
    public void onShowVideoPreviewPicture(String picture) {

    }

    @Override
    public void hasNoAudioPermission() {

    }

    @Override
    public void onResume() {
        super.onResume();
        refreshLayout();
    }

    @Override
    protected void onEnterAnimationFinished() {
        refreshLayout();
    }

    @Override
    protected void setFragmentComponent(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        ViewUtils.clearViewMarginStatusBar(mHeaderContainer);
    }

    @Override
    public void onShowProperty(Device device) {
//        if (!device.camera_time_lapse_photography.isNull()) {
//            mRecordBeginTime = device.camera_time_lapse_photography.timeStart;
//            mRecordTimeCycle = device.camera_time_lapse_photography.timePeriod;
//            mRecordTimeDuration = device.camera_time_lapse_photography.timeDuration;
//            mRecordStatus = device.camera_time_lapse_photography.status;
//        }


        refreshLayout();
        if (!isStandBy && mRecordStatus < 1) {
            presenter.startViewer();
        }
    }
}
