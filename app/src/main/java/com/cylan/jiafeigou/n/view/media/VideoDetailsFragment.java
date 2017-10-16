package com.cylan.jiafeigou.n.view.media;

/**
 * Created by cylan-hunt on 16-9-7.
 */

import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.transition.Transition;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.view.adapter.TransitionListenerAdapter;
import com.cylan.jiafeigou.rx.event.EventFactory;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.GlideNetVideoUtils;
import com.cylan.jiafeigou.widget.SimpleProgressBar;
import com.cylan.jiafeigou.widget.dialog.VideoMoreDialog;

import java.util.Formatter;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscription;
import tv.danmaku.ijk.media.exo.IjkExoMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;

import static com.cylan.jiafeigou.rx.RxBus.getCacheInstance;

public class VideoDetailsFragment extends PicDetailsFragment implements SeekBar.OnSeekBarChangeListener, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnInfoListener, IMediaPlayer.OnCompletionListener, SurfaceHolder.Callback {

    @BindView(R.id.vv_play_video)
    SurfaceView vvPlayVideo;
    @BindView(R.id.vv_play_loading)
    SimpleProgressBar vvLoading;
    @BindView(R.id.vv_play)
    ImageView vvPlay;
    @BindView(R.id.vv_play_time)
    TextView vvPlayTime;
    @BindView(R.id.vv_seekbar)
    SeekBar vvSeekBar;
    @BindView(R.id.vv_total_time)
    TextView vvTotalTime;
    @BindView(R.id.vv_full_screen)
    ImageView vvFullScreen;
    @BindView(R.id.vv_more)
    ImageView vvMore;
    private IjkExoMediaPlayer mMediaPlayer;

    private boolean isEntering = true;
    private Subscription mSubscribe;
    private Subscription mPlaySubscribe;
    private boolean started = false;
    private String mMediaURL;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private long mVideoPosition;
    private boolean mSurfaceCreated = false;

    private boolean mAnimateEnd = true;

    public static VideoDetailsFragment newInstance(int position, int startingPosition, final String url) {
        Bundle args = new Bundle();
        args.putInt(ARG_MEDIA_POSITION, position);
        args.putInt(ARG_MEDIA_START_POSITION, startingPosition);
        args.putString(KEY_MEDIA_URL, url);
        args.putInt(ARG_MEDIA_TYPE, 1);
        VideoDetailsFragment fragment = new VideoDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutID() {
        return R.layout.layout_fragment_media_video_details;
    }

    @Override
    protected void loadMedia(final String mediaUrl) {
        mMediaURL = mediaUrl;
        if (mediaUrl.startsWith("http://")) {
            GlideNetVideoUtils.loadNetVideo(getContext(), mediaUrl, detailsAlbumImage, this::startPostponedEnterTransition);
        } else {
            super.loadMedia(mediaUrl);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mVideoPosition = savedInstanceState.getLong("videoPosition");
        }
    }

    public void startPlay() {
        vvPlayVideo.post(() -> {
            AppLogger.e("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            mMediaPlayer.reset();
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnInfoListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setDataSource(mMediaURL);
            mMediaPlayer.setDisplay(vvPlayVideo.getHolder());
            mMediaPlayer.prepareAsync();
        });
    }

    @Override
    protected void initView() {
        vvSeekBar.setOnSeekBarChangeListener(this);
        vvPlayVideo.getHolder().addCallback(this);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        mMediaPlayer = new IjkExoMediaPlayer(getContext());
        mSubscribe = getCacheInstance().toObservable(EventFactory.HideVideoViewEvent.class).subscribe(hideVideoViewEvent -> {
            if (mSubscribe != null && !mSubscribe.isUnsubscribed()) {
                mSubscribe.unsubscribe();
            }
            mMediaPlayer.stop();
            mMediaPlayer.release();
            detailsAlbumImage.setVisibility(View.VISIBLE);
            vvPlayVideo.setVisibility(View.INVISIBLE);
            vvLoading.setVisibility(View.INVISIBLE);
        }, AppLogger::e);
        if (mStartPosition == mPosition) {
            mAnimateEnd = false;
            getActivity().getWindow().getSharedElementEnterTransition().addListener(new TransitionListenerAdapter() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    if (!mAnimateEnd) {
                        vvPlayVideo.post(VideoDetailsFragment.this::startLoading);
                    }
                    mAnimateEnd = true;
                }
            });
        }
    }

    private void startLoading() {
        vvPlayVideo.setVisibility(View.VISIBLE);
        vvLoading.setVisibility(View.VISIBLE);

    }

    @Override
    public void onPause() {
        super.onPause();
        updatePausePlay();
        isEntering = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePausePlay();
        Log.d("AAAAAA", "" + this);
    }


    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private void updateTime() {
        vvSeekBar.postDelayed(() -> {
            if (mMediaPlayer.isPlaying()) {
                int position = (int) mMediaPlayer.getCurrentPosition();
                vvPlayTime.setText(stringForTime(position));
                vvSeekBar.setProgress(position);
                updateTime();
            }
        }, 200);
    }

    @OnClick({
            R.id.vv_play,
            R.id.vv_full_screen,
            R.id.vv_more})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.vv_play:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                } else {
                    mMediaPlayer.start();
                    updateTime();
                }
                updatePausePlay();
                break;
            case R.id.vv_full_screen:
                fullScreen();
                break;
            case R.id.vv_more:
                showMoreDialog();
                break;
        }
    }

    private void fullScreen() {
        mVideoPosition = mMediaPlayer.getCurrentPosition();
        mMediaPlayer.reset();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private void showMoreDialog() {
        VideoMoreDialog.newInstance(null).show(getFragmentManager(), VideoMoreDialog.class.getName());
    }

    private void updatePausePlay() {
        if (mMediaPlayer.isPlaying()) {
            vvPlay.setImageResource(R.drawable.wonderful__video_suspend);
        } else {
            vvPlay.setImageResource(R.drawable.wonderful_icon_video_play);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("videoPosition", mVideoPosition);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mMediaPlayer.seekTo(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        AppLogger.e(mVideoPosition + "AADDD");
        mMediaPlayer.start();
        if (mVideoPosition > 0) {
            mMediaPlayer.seekTo(mVideoPosition);
            mVideoPosition = 0;
        }
        vvPlayVideo.post(() -> {
            vvLoading.dismiss();
            vvLoading.setVisibility(View.INVISIBLE);
            detailsAlbumImage.setVisibility(View.INVISIBLE);
        });
        vvSeekBar.setMax((int) iMediaPlayer.getDuration());
        vvSeekBar.setProgress((int) iMediaPlayer.getCurrentPosition());
        String totalTime = stringForTime((int) iMediaPlayer.getDuration());
        vvTotalTime.setText(totalTime);
        vvPlay.setImageResource(R.drawable.wonderful__video_suspend);
        updateTime();
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int i1) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                vvLoading.setVisibility(View.VISIBLE);
                vvLoading.run();
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                vvLoading.setVisibility(View.INVISIBLE);
                vvLoading.dismiss();
                break;
        }
        return false;
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        mVideoPosition = 0;
        vvSeekBar.setProgress(0);
        vvPlayTime.setText(stringForTime(0));
        mMediaPlayer.seekTo(0);
        mMediaPlayer.pause();
        updatePausePlay();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceCreated = true;
        if (getUserVisibleHint() && getActivity().getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            AppLogger.e("surfaceCreated");
            vvPlayVideo.postDelayed(this::startPlay, 2000);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceCreated = false;
        mVideoPosition = mMediaPlayer.getCurrentPosition();
        mMediaPlayer.reset();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisable && isPrepared && mAnimateEnd) {
            startLoading();
            if (mSurfaceCreated) {
                startPlay();
            }
        } else if (!isVisble && isPrepared && mAnimateEnd) {
            mVideoPosition = mMediaPlayer.getCurrentPosition();
            mMediaPlayer.reset();
        }

    }
}