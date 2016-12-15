package com.cylan.jiafeigou.n.view.cam;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.aigestudio.wheelpicker.WheelPicker;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamDelayRecordContract;
import com.cylan.jiafeigou.widget.roundview.RoundedTextureView;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tv.danmaku.ijk.media.exo.IjkExoMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by yzd on 16-12-15.
 */

public class CamDelayRecordActivity extends BaseFullScreenFragmentActivity<CamDelayRecordContract.Presenter>
        implements CamDelayRecordContract.View, IMediaPlayer.OnPreparedListener, RoundedTextureView.SurfaceProvider {

    @BindView(R.id.act_delay_record_video_view)
    RoundedTextureView mRoundedTextureView;
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

    private IjkExoMediaPlayer mMediaPlayer;
    private WeakReference<AlertDialog> mTimeIntervalDialog;
    private WeakReference<AlertDialog> mTimeDurationDialog;
    private int[] mPickItems = new int[]{};
    private int mPickSelection = 0;
    private WheelPicker mPicker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_delay_record);
        ButterKnife.bind(this);
        initViewAndListener();
    }

    private void initViewAndListener() {
        mMediaPlayer = new IjkExoMediaPlayer(this);
        mRoundedTextureView.setSurfaceProvider(this);

        initTimeIntervalDialog();
        initTimeDurationDialog();
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
        mTimeIntervalDialog.get().show();
    }

    @OnClick(R.id.act_delay_record_time_duration)
    public void selectTimeDuration() {
        initTimeDurationDialog();
        mTimeDurationDialog.get().show();
    }

    private void initTimeIntervalDialog() {
        if (mTimeIntervalDialog == null || mTimeIntervalDialog.get() == null) {
            //初始化
            View contentView = View.inflate(this, R.layout.dialog_delay_record_time_interval, null);
            RadioGroup option = (RadioGroup) contentView.findViewById(R.id.dialog_record_rg_option);
            option.setOnCheckedChangeListener((group, checkedId) -> {
                switch (checkedId) {
                    case R.id.dialog_record_rb_20s: {
                        mPickItems = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
                        mPickSelection = 7;//默认是8小时
                        mRecordTimeIntervalOpt.setImageResource(R.drawable.delay_icon_20time);
                    }
                    break;
                    case R.id.dialog_record_rb_60s: {
                        mPickItems = new int[]{4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24};
                        mPickSelection = 4;//默认是8小时
                        mRecordTimeIntervalOpt.setImageResource(R.drawable.delay_icon_60time);
                    }
                    break;
                }
            });
            contentView.findViewById(R.id.dialog_record_time_interval_cancel).setOnClickListener(view -> {
                mTimeIntervalDialog.get().dismiss();
            });
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setView(contentView)
                    .create();

            mTimeIntervalDialog = new WeakReference<>(alertDialog);
        }

    }

    private void initTimeDurationDialog() {
        if (mTimeDurationDialog == null || mTimeDurationDialog.get() == null) {
            View contentView = View.inflate(this, R.layout.dialog_delay_record_time_duration, null);
            contentView.findViewById(R.id.dialog_record_duration_cancel).setOnClickListener(v -> {

            });
            mPicker = (WheelPicker) contentView.findViewById(R.id.dialog_record_duration_picker);
            contentView.findViewById(R.id.dialog_record_duration_ok).setOnClickListener(v -> {
                int position = mPicker.getSelectedItemPosition();


            });
            AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.delay_record_dialog_style)
                    .setCancelable(false)
                    .setView(contentView)
                    .create();
            mTimeDurationDialog = new WeakReference<>(alertDialog);
        }
        mPicker.setData(Arrays.asList(mPickItems));
        mPicker.setSelectedItemPosition(mPickSelection);
    }
}
