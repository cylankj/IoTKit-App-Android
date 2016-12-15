package com.cylan.jiafeigou.n.view.cam;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamDelayRecordContract;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
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
        implements CamDelayRecordContract.View, IMediaPlayer.OnPreparedListener, RoundedTextureView.SurfaceProvider {

    @BindView(R.id.act_delay_record_video_view)
    RoundedTextureView mRoundedTextureView;

    private IjkExoMediaPlayer mMediaPlayer;
    private WeakReference<AlertDialog> mTimeIntervalDialog;
    private WeakReference<AlertDialog> mTimeDurationDialog;

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
        if (mTimeIntervalDialog == null || mTimeIntervalDialog.get() == null) {
            //初始化
            View contentView = View.inflate(this, R.layout.dialog_delay_record_time_interval, null);
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setView(contentView)
                    .create();

            mTimeIntervalDialog = new WeakReference<>(alertDialog);
        }
        mTimeIntervalDialog.get().show();
    }

    @OnClick(R.id.act_delay_record_time_duration)
    public void selectTimeDuration() {
        if (mTimeDurationDialog == null || mTimeDurationDialog.get() == null) {
            View contentView = View.inflate(this, R.layout.dialog_delay_record_time_duration, null);
            AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.delay_record_dialog_style)
                    .setCancelable(false)
                    .setView(contentView)
                    .create();
            mTimeDurationDialog = new WeakReference<>(alertDialog);
        }
        mTimeDurationDialog.get().show();
    }


    public static class TimeIntervalFragment extends BaseDialog {

    }
}
