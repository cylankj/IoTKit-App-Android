package com.cylan.jiafeigou.n.view.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.transition.Transition;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.model.MediaBean;
import com.cylan.jiafeigou.n.view.adapter.MediaDetailPagerAdapter;
import com.cylan.jiafeigou.n.view.adapter.TransitionListenerAdapter;
import com.cylan.jiafeigou.n.view.home.ShareDialogFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.SimpleProgressBar;
import com.cylan.jiafeigou.widget.dialog.VideoMoreDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import tv.danmaku.ijk.media.exo.IjkExoMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;


public class MediaActivity extends AppCompatActivity implements IMediaPlayer.OnPreparedListener, IMediaPlayer.OnInfoListener, IMediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener, ViewPager.OnPageChangeListener {


    @BindView(R.id.act_media_pager)
    ViewPager mMediaPager;
    @BindView(R.id.act_media_header_title)
    TextView mHeaderTitle;
    @BindView(R.id.act_media_header_opt_delete)
    ImageView mHeaderDelete;
    @BindView(R.id.act_media_header_opt_download)
    ImageView mHeaderDownload;
    @BindView(R.id.act_media_header_opt_share)
    ImageView mHeaderShare;
    @BindView(R.id.act_media_header)
    FrameLayout mHeaderContainer;
    @BindView(R.id.act_media_picture_opt_download)
    ImageView mPictureDownload;
    @BindView(R.id.act_media_picture_opt_share)
    ImageView mPictureShare;
    @BindView(R.id.act_media_picture_opt_collection)
    ImageView mPictureCollection;
    @BindView(R.id.act_media_pic_option)
    FrameLayout mPictureFooterContainer;
    @BindView(R.id.act_media_video_opt_play)
    ImageView mVideoPlay;
    @BindView(R.id.act_media_video_opt_play_time)
    TextView mVideoPlayTime;
    @BindView(R.id.act_media_video_opt_seek_bar)
    SeekBar mVideoSeekBar;
    @BindView(R.id.act_media_video_opt_total_time)
    TextView mVideoTotalTime;
    @BindView(R.id.act_media_video_opt_full_screen)
    ImageView mVideoFullScreen;
    @BindView(R.id.act_media_video_opt_more)
    ImageView mVideoMore;
    @BindView(R.id.act_media_video_option)
    FrameLayout mVideoFooterContainer;
    @BindView(R.id.act_media_footer)
    FrameLayout mFooterContainer;
    @BindView(R.id.act_media_header_opt_container)
    LinearLayout mHeaderOptContainer;
    @BindView(R.id.act_media_root_view)
    FrameLayout mContentRootView;
    @BindView(R.id.act_media_header_back)
    ImageView mHeaderBack;
    SurfaceView mVideoView;
    SimpleProgressBar mVideoLoadingBar;
    private IjkExoMediaPlayer mMediaPlayer;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    private long mVideoPlayPosition;
    private int mStartPosition;
    private int mCurrentPosition;
    private ArrayList<MediaBean> mMediaList;
    private int mCurrentViewType;
    private static final String STATE_CURRENT_PAGE_POSITION = "state_current_page_position";
    private MediaDetailPagerAdapter mAdapter;
    private View mPhotoView;
    private View mPagerContentView;
    private boolean mEnterAnimationFinished = false;
    private boolean mIsReadToPlay = false;
    private WeakReference<VideoMoreDialog> mMoreDialog;
    private WeakReference<ShareDialogFragment> mShareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
        }
        //数据初始化
        mCurrentPosition = mStartPosition = getIntent().getIntExtra(JConstant.KEY_SHARED_ELEMENT_STARTED_POSITION, 0);
        mMediaList = getIntent().getParcelableArrayListExtra(JConstant.KEY_SHARED_ELEMENT_LIST);
        mCurrentViewType = mMediaList.get(mStartPosition).mediaType;
        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(STATE_CURRENT_PAGE_POSITION);
        }
        initViewAndListener();
        initShareElementCallback();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_PAGE_POSITION, mCurrentPosition);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initShareElementCallback() {
        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                names.clear();
                sharedElements.clear();
                if (mPhotoView != null) {
                    names.add(mPhotoView.getTransitionName());
                    sharedElements.put(mPhotoView.getTransitionName(), mPhotoView);
                }
            }
        });
    }

    private void initViewAndListener() {
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        ViewUtils.setViewMarginStatusBar(mHeaderContainer);

        getWindow().getSharedElementEnterTransition().addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                getWindow().getSharedElementEnterTransition().removeListener(this);
                mEnterAnimationFinished = true;
                animateHeaderAndFooter(true, true);
                if (mCurrentViewType == MediaBean.TYPE_VIDEO) {
                    startPlayVideo();
                }
            }
        });

        mMediaPlayer = new IjkExoMediaPlayer(this);
        mVideoSeekBar.setOnSeekBarChangeListener(this);
        mAdapter = new MediaDetailPagerAdapter(mMediaList, mStartPosition) {
            @Override
            public void setPrimaryItem(ViewGroup container, int position, Object object) {
                super.setPrimaryItem(container, position, object);
                if (mCurrentViewType == MediaBean.TYPE_VIDEO && mPagerContentView != object) {
                    mPagerContentView = (View) object;
                    ViewHolder holder = (ViewHolder) mPagerContentView.getTag();
                    mPhotoView = holder.mPhotoView;
                    mVideoLoadingBar = holder.mProgressBar;
                    if (mVideoView != null) {
                        mVideoView.getHolder().removeCallback(mSurfaceCallback);
                        mMediaPlayer.reset();
                    }
                    mVideoView = holder.mSurfaceView;
                    mVideoView.getHolder().addCallback(mSurfaceCallback);
                    if (!mVideoView.getHolder().isCreating()) {
                        mMediaPlayer.setDisplay(mVideoView.getHolder());
                    }
                    if (mEnterAnimationFinished) startPlayVideo();
                } else if (mCurrentViewType == MediaBean.TYPE_PIC && mPhotoView != object) {
                    mPhotoView = (View) object;
                }

            }
        };
        mMediaPager.setPageMargin((int) getResources().getDimension(R.dimen.video_pager_page_margin));
        mAdapter.setOnInitFinishListener(this::startPostponedEnterTransition);
        mMediaPager.setAdapter(mAdapter);
        mMediaPager.setCurrentItem(mStartPosition);
        mMediaPager.addOnPageChangeListener(this);
    }

    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mMediaPlayer.setDisplay(holder);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    };


    private void animateHeaderAndFooter(boolean showHeader, boolean showFooter) {
        AppLogger.d("animateHeaderAndFooter");
        if (showHeader && mHeaderContainer.isShown()) {
            //需要显示Footer,但此时Footer已经显示，则先隐藏再显示
//            mHeaderContainer.setVisibility(View.GONE);
//            setHeaderContent();
//            mHeaderContainer.setVisibility(View.VISIBLE);
            AnimatorUtils.slide(mHeaderContainer, false, () -> {
                setHeaderContent();
                AnimatorUtils.slide(mHeaderContainer, false, () -> mHeaderContainer.setVisibility(View.VISIBLE));
            });
        } else if (showHeader && !mHeaderContainer.isShown()) {
            //需要显示Footer,但此时Footer还没显示，则直接显示
            setHeaderContent();
//            mHeaderContainer.setVisibility(View.VISIBLE);
            AnimatorUtils.slide(mHeaderContainer, false, () -> mHeaderContainer.setVisibility(View.VISIBLE));
        } else if (!showHeader && mHeaderContainer.isShown()) {
            //需要隐藏Footer,但此时Footer已经显示，则直接隐藏
//            mHeaderContainer.setVisibility(View.GONE);
            AnimatorUtils.slide(mHeaderContainer, false, () -> mHeaderContainer.setVisibility(View.GONE))
            ;
        }

        if (showFooter && mFooterContainer.isShown()) {
            //需要显示Footer,但此时Footer已经显示，则先隐藏再显示
//            mFooterContainer.setVisibility(View.GONE);
//            setFooterContent();
//            mFooterContainer.setVisibility(View.VISIBLE);
            AnimatorUtils.slide(mFooterContainer, true, () -> {
                setFooterContent();
                AnimatorUtils.slide(mFooterContainer, true, () -> mFooterContainer.setVisibility(View.VISIBLE));
            });
        } else if (showFooter && !mFooterContainer.isShown()) {
            //需要显示Footer,但此时Footer还没显示，则直接显示
            setFooterContent();
//            mFooterContainer.setVisibility(View.VISIBLE);
            AnimatorUtils.slide(mFooterContainer, true, () -> mFooterContainer.setVisibility(View.VISIBLE));
        } else if (!showFooter && mFooterContainer.isShown()) {
            //需要隐藏Footer,但此时Footer已经显示，则直接隐藏
//            mFooterContainer.setVisibility(View.GONE);
            AnimatorUtils.slide(mFooterContainer, true, () -> mFooterContainer.setVisibility(View.GONE));
        }
    }

    private void setFooterContent() {
        if (mCurrentViewType == MediaBean.TYPE_VIDEO) {
            mVideoFooterContainer.setVisibility(View.VISIBLE);
            mPictureFooterContainer.setVisibility(View.GONE);
            updatePlayState();
        } else if (mCurrentViewType == MediaBean.TYPE_PIC) {
            mPictureFooterContainer.setVisibility(View.VISIBLE);
            mVideoFooterContainer.setVisibility(View.GONE);
        }
    }


    private void setHeaderContent() {
        MediaBean bean = mMediaList.get(mCurrentPosition);
        if (mCurrentViewType == MediaBean.TYPE_VIDEO) {
            mHeaderTitle.setText(TimeUtils.getMediaVideoTimeInString(bean.time));
        } else if (mCurrentViewType == MediaBean.TYPE_PIC) {
            mHeaderTitle.setText(TimeUtils.getMediaPicTimeInString(bean.time));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE | newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_USER) {
            setLandScapeLayout();
        } else if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setPortraitLayout();
        }
        animateHeaderAndFooter(true, true);
    }

    private void setPortraitLayout() {
        ViewUtils.setViewMarginStatusBar(mContentRootView);
        setStatusBarProperty();

        mHeaderOptContainer.setVisibility(View.GONE);

        Resources resources = getResources();

        FrameLayout.LayoutParams footerParams = (FrameLayout.LayoutParams) mVideoFooterContainer.getLayoutParams();
        footerParams.height = (int) resources.getDimension(R.dimen.video_footer_height);
        mVideoFooterContainer.setLayoutParams(footerParams);

        LinearLayout.LayoutParams params;
        params = (LinearLayout.LayoutParams) mVideoPlay.getLayoutParams();
        params.leftMargin = (int) resources.getDimension(R.dimen.video_option_play_margin_left);
        params.rightMargin = (int) resources.getDimension(R.dimen.video_option_play_margin_right);
        mVideoPlay.setLayoutParams(params);

        params = (LinearLayout.LayoutParams) mVideoFullScreen.getLayoutParams();
        params.leftMargin = (int) resources.getDimension(R.dimen.video_option_full_screen_margin_left);
        params.rightMargin = (int) resources.getDimension(R.dimen.video_option_full_screen_margin_right);
        mVideoFullScreen.setLayoutParams(params);

        mVideoFullScreen.setImageResource(R.drawable.icon_full_screen);
        mHeaderBack.setImageResource(R.drawable.btn_close);
        mVideoMore.setVisibility(View.VISIBLE);

    }

    private void setStatusBarProperty() {
        int orientation = getRequestedOrientation();
        if (orientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mContentRootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        } else {
            mContentRootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void setLandScapeLayout() {
        ViewUtils.clearViewMarginStatusBar(mContentRootView);
        setStatusBarProperty();
        mHeaderOptContainer.setVisibility(View.VISIBLE);
        Resources resources = getResources();

        FrameLayout.LayoutParams footerParams = (FrameLayout.LayoutParams) mVideoFooterContainer.getLayoutParams();
        footerParams.height = (int) resources.getDimension(R.dimen.video_footer_height);
        mVideoFooterContainer.setLayoutParams(footerParams);

        LinearLayout.LayoutParams params;
        params = (LinearLayout.LayoutParams) mVideoPlay.getLayoutParams();
        params.leftMargin = (int) resources.getDimension(R.dimen.video_option_play_margin_left);
        params.rightMargin = (int) resources.getDimension(R.dimen.video_option_play_margin_right);
        mVideoPlay.setLayoutParams(params);

        params = (LinearLayout.LayoutParams) mVideoFullScreen.getLayoutParams();
        params.leftMargin = (int) resources.getDimension(R.dimen.video_option_full_screen_margin_left);
        params.rightMargin = (int) resources.getDimension(R.dimen.video_option_full_screen_margin_right);
        mVideoFullScreen.setLayoutParams(params);

        mVideoFullScreen.setImageResource(R.drawable.landscape_icon_screen);
        mHeaderBack.setImageResource(R.drawable.icon_arrow_back);
        mVideoMore.setVisibility(View.GONE);
        updatePlayState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppLogger.d("onPause");

    }

    @Override
    protected void onStop() {
        super.onStop();
        AppLogger.d("onStop");
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        mIsReadToPlay = true;
        mMediaPlayer.start();
        mVideoLoadingBar.setVisibility(View.INVISIBLE);
        mPhotoView.setVisibility(View.INVISIBLE);
        int duration = (int) mMediaPlayer.getDuration();
        mVideoTotalTime.setText(stringForTime(duration));
        mVideoSeekBar.setMax(duration);
        startUpdateTime();
        updatePlayState();
    }


    @OnClick({R.id.act_media_video_opt_full_screen})
    public void rotateScreen() {
        //转屏之前先将头和脚隐藏掉
        animateHeaderAndFooter(false, false);
        int orientation = getRequestedOrientation();
        if (orientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @OnClick(R.id.act_media_video_opt_play)
    public void playOrPause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else if (mIsReadToPlay) {
            mMediaPlayer.start();
            startUpdateTime();
        }

        updatePlayState();
    }

    private void updatePlayState() {
        if (mMediaPlayer.isPlaying()) {
            mVideoPlay.setImageResource(R.drawable.video_opt_pause_drawable);
        } else if (mIsReadToPlay) {
            mVideoPlay.setImageResource(R.drawable.video_opt_play_drawable);
        }
    }

    @OnClick({R.id.act_media_header_opt_delete})
    public void delete() {

    }

    @OnClick(R.id.act_media_picture_opt_collection)
    public void collection() {

    }

    @OnClick({R.id.act_media_header_opt_download, R.id.act_media_picture_opt_download})
    public void download() {
        Toast.makeText(this, "download", Toast.LENGTH_SHORT).show();
    }

    @OnClick({R.id.act_media_header_opt_share, R.id.act_media_picture_opt_share})
    public void share() {
        if (mShareDialog == null || mShareDialog.get() == null) {
            mShareDialog = new WeakReference<>(ShareDialogFragment.newInstance(mMediaList.get(mCurrentPosition)));
        }
        mShareDialog.get().show(getSupportFragmentManager(), ShareDialogFragment.class.getName());
    }

    @OnClick(R.id.act_media_video_opt_more)
    public void more() {
        if (mMoreDialog == null || mMoreDialog.get() == null) {
            mMoreDialog = new WeakReference<>(VideoMoreDialog.newInstance(null));
            mMoreDialog.get().setAction((id, view) -> {
                switch (id) {
                    case R.id.dialog_media_video_delete:
                        delete();
                        break;
                    case R.id.dialog_media_video_download:
                        download();
                        break;
                    case R.id.dialog_media_video_share:
                        share();
                        break;
                }
                mMoreDialog.get().dismiss();
            });
        }
        mMoreDialog.get().show(getSupportFragmentManager(), VideoMoreDialog.class.getName());
    }

    @Override
    public void finishAfterTransition() {
        Intent data = new Intent();
        data.putExtra(JConstant.EXTRA_STARTING_ALBUM_POSITION, mStartPosition);
        data.putExtra(JConstant.EXTRA_CURRENT_ALBUM_POSITION, mCurrentPosition);
        setResult(RESULT_OK, data);
        super.finishAfterTransition();
    }

    @Override
    @OnClick(R.id.act_media_header_back)
    public void onBackPressed() {
        int orientation = getRequestedOrientation();
        if (orientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            cleanUpForExit();
            super.onBackPressed();
        } else {
            rotateScreen();
        }
    }

    private void cleanUpForExit() {
        if (mCurrentViewType == MediaBean.TYPE_VIDEO) {
            stopPlayVideo();
        }
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

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int i1) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                mVideoLoadingBar.setVisibility(View.VISIBLE);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                mVideoLoadingBar.setVisibility(View.INVISIBLE);
                break;
        }
        return false;
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        iMediaPlayer.seekTo(0);
        iMediaPlayer.pause();
        updatePlayState();
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
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mMediaPlayer.pause();
    }


    @Override
    public void onPageSelected(int position) {
        mCurrentPosition = position;
        int type = mMediaList.get(position).mediaType;
        boolean change = type != mCurrentViewType;
        mCurrentViewType = type;
        if (change) {
            animateHeaderAndFooter(true, true);
        } else {
            setHeaderContent();
            setFooterContent();
        }
    }

    private void startPlayVideo() {
        mVideoView.setVisibility(View.VISIBLE);
        mVideoLoadingBar.setVisibility(View.VISIBLE);
        String url = mMediaList.get(mCurrentPosition).srcUrl;
        mMediaPlayer.reset();
        String proxyUrl = BaseApplication.getProxy(this).getProxyUrl(url);
        mMediaPlayer.setDataSource(proxyUrl);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setDisplay(mVideoView.getHolder());
        mMediaPlayer.prepareAsync();
    }

    private void startUpdateTime() {
        long position = mMediaPlayer.getCurrentPosition();
        mVideoPlayTime.setText(stringForTime((int) position));
        mVideoSeekBar.setProgress((int) position);
        if (mMediaPlayer.isPlaying()) mContentRootView.postDelayed(this::startUpdateTime, 200);
    }


    private void stopPlayVideo() {
        mMediaPlayer.reset();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
