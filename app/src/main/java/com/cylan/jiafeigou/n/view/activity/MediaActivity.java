package com.cylan.jiafeigou.n.view.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.transition.Transition;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.HackyViewPager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.view.adapter.MediaDetailPagerAdapter;
import com.cylan.jiafeigou.n.view.adapter.TransitionListenerAdapter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.photoview.PhotoViewAttacher;
import com.cylan.jiafeigou.support.share.ShareMediaActivity;
import com.cylan.jiafeigou.support.share.ShareConstant;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.utils.WonderGlideURL;
import com.cylan.jiafeigou.widget.SimpleProgressBar;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;
import com.cylan.jiafeigou.widget.dialog.VideoMoreDialog;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;
import rx.Observable;
import tv.danmaku.ijk.media.exo.IjkExoMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static com.cylan.jiafeigou.R.id.tv_dialog_btn_right;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPWonderItem;

@RuntimePermissions
public class MediaActivity extends AppCompatActivity implements IMediaPlayer.OnPreparedListener, IMediaPlayer.OnInfoListener, IMediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener, ViewPager.OnPageChangeListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnVideoSizeChangedListener, PhotoViewAttacher.OnPhotoTapListener {


    private static final int PLAY_STATE_RESET = 0;
    private static final int PLAY_STATE_PAUSED = 1;
    private static final int PLAY_STATE_PLAYING = 2;
    private static final int PLAY_STATE_READY_TO_PLAY = 3;
    private static final int PLAY_STATE_LOADING_START = 4;
    private static final int PLAY_STATE_LOADING_FINISH = 5;
    private static final int REQ_DOWNLOAD = 0X8001;
    private static final long NETWORK_ERROR_WAIT_TIME = 15000;

    @BindView(R.id.act_media_pager)
    HackyViewPager mMediaPager;
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
    @BindView(R.id.act_media_picture_opt_delete)
    ImageView mPictureCollection;
    @BindView(R.id.act_media_pic_option)
    LinearLayout mPictureFooterContainer;
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
    TextureView mVideoView;
    SimpleProgressBar mVideoLoadingBar;
    private IjkExoMediaPlayer mMediaPlayer;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    private long mVideoPlayPosition;
    private int mStartPosition;
    private int mCurrentPosition;
    private ArrayList<DPWonderItem> mMediaList;
    private int mCurrentViewType;
    private DPWonderItem mCurrentMediaBean;
    private static final String STATE_CURRENT_PAGE_POSITION = "state_current_page_position";
    private static final String STATE_ENTER_ANIMATION_FINISHED = "state_enter_animation_finished";
    private MediaDetailPagerAdapter mAdapter;
    private View mPhotoView;
    private View mPagerContentView;
    private boolean mEnterAnimationFinished = false;
    private WeakReference<VideoMoreDialog> mMoreDialog;
    private File mDownloadFile;
    private int mCurrentPlayState = PLAY_STATE_RESET;
    private static final long HEADER_AND_FOOTER_SHOW_TIME = 3000;

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        ButterKnife.bind(this);

        //数据初始化
        mCurrentPosition = mStartPosition = getIntent().getIntExtra(JConstant.KEY_SHARED_ELEMENT_STARTED_POSITION, 0);
        mMediaList = getIntent().getParcelableArrayListExtra(JConstant.KEY_SHARED_ELEMENT_LIST);
        mCurrentMediaBean = mMediaList.get(mStartPosition);
        mCurrentViewType = mCurrentMediaBean.msgType;
        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(STATE_CURRENT_PAGE_POSITION);
        }
        initViewAndListener();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mEnterAnimationFinished = false;
            mHeaderContainer.setVisibility(View.GONE);
            mFooterContainer.setVisibility(View.GONE);
            postponeEnterTransition();
            initShareElementCallback();
        }
        mPictureShare.setVisibility(getResources().getBoolean(R.bool.show_share_btn) ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_PAGE_POSITION, mCurrentPosition);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initShareElementCallback() {
        getWindow().getSharedElementEnterTransition().addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                getWindow().getSharedElementEnterTransition().removeListener(this);
                mEnterAnimationFinished = true;
                animateHeaderAndFooter(true, true);
                if (mCurrentViewType == DPWonderItem.TYPE_VIDEO) {
                    mPhotoView.postDelayed(MediaActivity.this::startPlayVideo, 500);
                }
            }
        });
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
        setPortraitLayout();
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        mMediaPlayer = new IjkExoMediaPlayer(this);

        mVideoSeekBar.setOnSeekBarChangeListener(this);
        mAdapter = new MediaDetailPagerAdapter(mMediaList, mStartPosition) {
            @Override
            public void setPrimaryItem(ViewGroup container, int position, Object object) {
                super.setPrimaryItem(container, position, object);
                if (mCurrentViewType == DPWonderItem.TYPE_VIDEO && mPagerContentView != object) {
                    AppLogger.e("setPrimaryItem");
                    mPagerContentView = (View) object;
                    ViewHolder holder = (ViewHolder) mPagerContentView.getTag();
                    mPhotoView = holder.mPhotoView;
                    mVideoLoadingBar = holder.mProgressBar;
                    if (mVideoView != null) {
                        mVideoView.setSurfaceTextureListener(null);
                        mMediaPlayer.reset();
                    }
                    mVideoView = holder.mSurfaceView;
                    mVideoView.setSurfaceTextureListener(mSurfaceTextureListener);
                    if (mEnterAnimationFinished) startPlayVideo();
                } else if (mCurrentViewType == DPWonderItem.TYPE_PIC && mPhotoView != object) {
                    AppLogger.e("picture");
                    mPhotoView = (View) object;
                }
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                super.destroyItem(container, position, object);
                if (mPagerContentView == object) {
                    mPagerContentView = null;
                    mVideoView = null;
                    mPhotoView = null;
                }
            }
        };
        mAdapter.setPhotoTapListener(this);
        mMediaPager.setPageMargin((int) getResources().getDimension(R.dimen.video_pager_page_margin));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAdapter.setOnInitFinishListener(this::startPostponedEnterTransition);
        } else {
            setFooterContent();
            setHeaderContent();
        }
        mMediaPager.setAdapter(mAdapter);
        mMediaPager.setCurrentItem(mStartPosition);
        mMediaPager.addOnPageChangeListener(this);
        mMediaPager.setOnLockModeTouchListener((view, event) -> {
            if (!mFooterContainer.isShown() && !mHeaderContainer.isShown()) {
                mContentRootView.removeCallbacks(mHideHeaderAndFooterCallback);
                animateHeaderAndFooter(true, true, () -> mContentRootView.postDelayed(mHideHeaderAndFooterCallback, HEADER_AND_FOOTER_SHOW_TIME));
            }
            return true;
        });
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Surface videoSurface = new Surface(surface);
            mMediaPlayer.setSurface(videoSurface);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private void animateHeaderAndFooter(boolean showHeader, boolean showFooter) {
        animateHeaderAndFooter(showHeader, showFooter, null);
    }

    private void animateHeaderAndFooter(boolean showHeader, boolean showFooter, AnimatorUtils.OnFinish finish) {
        AppLogger.d("animateHeaderAndFooter");
        if (showHeader && mHeaderContainer.isShown()) {
            //需要显示Footer,但此时Footer已经显示，则先隐藏再显示
            AnimatorUtils.slide(mHeaderContainer, false, () -> {
                setHeaderContent();
                AnimatorUtils.slide(mHeaderContainer, false, null);
            });
        } else if (showHeader && !mHeaderContainer.isShown()) {
            //需要显示Footer,但此时Footer还没显示，则直接显示
            setHeaderContent();
            AnimatorUtils.slide(mHeaderContainer, false, null);
        } else if (!showHeader && mHeaderContainer.isShown()) {
            //需要隐藏Footer,但此时Footer已经显示，则直接隐藏
            AnimatorUtils.slide(mHeaderContainer, false, null)
            ;
        } else if (!showHeader && !mHeaderContainer.isShown()) {

        }

        if (showFooter && mFooterContainer.isShown()) {
            //需要显示Footer,但此时Footer已经显示，则先隐藏再显示
            AnimatorUtils.slide(mFooterContainer, true, () -> {
                setFooterContent();
                AnimatorUtils.slide(mFooterContainer, true, finish);
            });
        } else if (showFooter && !mFooterContainer.isShown()) {
            //需要显示Footer,但此时Footer还没显示，则直接显示
            setFooterContent();
            AnimatorUtils.slide(mFooterContainer, true, finish);
        } else if (!showFooter && mFooterContainer.isShown()) {
            //需要隐藏Footer,但此时Footer已经显示，则直接隐藏
            AnimatorUtils.slide(mFooterContainer, true, finish);
        } else if (!showFooter && !mFooterContainer.isShown()) {
            finish.onFinish();
        }
    }

    private void setFooterContent() {
        if (mCurrentViewType == DPWonderItem.TYPE_VIDEO) {
            mVideoFooterContainer.setVisibility(View.VISIBLE);
            mPictureFooterContainer.setVisibility(View.GONE);
            updatePlayState();
        } else if (mCurrentViewType == DPWonderItem.TYPE_PIC) {
            mPictureFooterContainer.setVisibility(View.VISIBLE);
            mVideoFooterContainer.setVisibility(View.GONE);
        }
    }


    private void setHeaderContent() {
        if (mCurrentViewType == DPWonderItem.TYPE_VIDEO) {
            mHeaderTitle.setText(TimeUtils.getMediaVideoTimeInString(mCurrentMediaBean.time * 1000L));
        } else if (mCurrentViewType == DPWonderItem.TYPE_PIC) {
            mHeaderTitle.setText(TimeUtils.getMediaVideoTimeInString(mCurrentMediaBean.time * 1000L));
        }

    }

    private Runnable mHideHeaderAndFooterCallback = () -> animateHeaderAndFooter(false, false);

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mContentRootView.removeCallbacks(mHideHeaderAndFooterCallback);
        if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE | newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_USER) {
            setLandScapeLayout();
            animateHeaderAndFooter(true, true, () -> mContentRootView.postDelayed(mHideHeaderAndFooterCallback, HEADER_AND_FOOTER_SHOW_TIME));
        } else if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setPortraitLayout();
            animateHeaderAndFooter(true, true);
        }
    }

    private void setPortraitLayout() {
        setStatusBarProperty();
        ViewUtils.setViewPaddingStatusBar(mHeaderContainer);
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

        mVideoFullScreen.setImageResource(R.drawable.icon_port_fullscreen_selector);
        mHeaderBack.setImageResource(R.drawable.nav_tab_back_selector);
        mVideoMore.setVisibility(View.VISIBLE);
        mMediaPager.setLocked(false);
        updatePlayState();
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
        setStatusBarProperty();
        ViewUtils.clearViewPaddingStatusBar(mHeaderContainer);
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
        mHeaderBack.setImageResource(R.drawable.nav_icon_back_white);
        mVideoMore.setVisibility(View.GONE);
        mMediaPager.setLocked(true);
        updatePlayState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppLogger.d("onPause");
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mVideoPlayPosition = mMediaPlayer.getCurrentPosition();
            mCurrentPlayState = PLAY_STATE_RESET;
            mMediaPlayer.reset();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCurrentViewType == DPWonderItem.TYPE_VIDEO && mVideoPlayPosition > 0) {
            startPlayVideo();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        AppLogger.d("onStop");
        stopVideoPlay();
    }

    private void stopVideoPlay() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        mVideoLoadingBar.setVisibility(View.INVISIBLE);
        mCurrentPlayState = PLAY_STATE_READY_TO_PLAY;
        mMediaPlayer.start();
        if (mVideoPlayPosition > 0) {
            mMediaPlayer.seekTo(mVideoPlayPosition);
            mVideoPlayPosition = 0;
            mCurrentPlayState = PLAY_STATE_PLAYING;
        } else {
            mMediaPlayer.seekTo(100);//这是为了防止首播黑屏的问题，没法
        }
        int duration = (int) mMediaPlayer.getDuration();
        mVideoTotalTime.setText(stringForTime(duration));
        mVideoSeekBar.setMax(duration);
        mCurrentPlayState = PLAY_STATE_PLAYING;
        startUpdateTime();
        updatePlayState();
        AppLogger.e("SSSSSSSSSSSSSSs");
        mPhotoView.setVisibility(View.INVISIBLE);
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
        if (mCurrentPlayState == PLAY_STATE_PLAYING) {
            mCurrentPlayState = PLAY_STATE_PAUSED;
            mMediaPlayer.pause();
        } else if (mCurrentPlayState == PLAY_STATE_READY_TO_PLAY || mCurrentPlayState == PLAY_STATE_PAUSED) {
            mCurrentPlayState = PLAY_STATE_PLAYING;
            mMediaPlayer.start();
            mPhotoView.setVisibility(View.INVISIBLE);
            startUpdateTime();
        } else {
            startPlayVideo();
        }
        updatePlayState();
    }

    private void updatePlayState() {
        switch (mCurrentPlayState) {
            case PLAY_STATE_PLAYING:
//            case PLAY_STATE_LOADING_START:
//            case PLAY_STATE_LOADING_FINISH:
                mVideoPlay.setImageResource(R.drawable.video_opt_pause_drawable);
                break;
            case PLAY_STATE_PAUSED:
            case PLAY_STATE_RESET:
            case PLAY_STATE_READY_TO_PLAY:
                mVideoPlay.setImageResource(R.drawable.video_opt_play_drawable);
                break;
        }
    }

    private SimpleDialogFragment dialogFragment;

    private SimpleDialogFragment initDeleteDialog() {
        if (dialogFragment == null) {
            //为删除dialog设置提示信息
            Bundle args = new Bundle();
            args.putString(BaseDialog.KEY_TITLE, "");
            args.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, getString(R.string.CANCEL));
            args.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, getString(R.string.DELETE));
            args.putString(SimpleDialogFragment.KEY_CONTENT_CONTENT,
                    this.getString(R.string.Tips_SureDelete));
            dialogFragment = SimpleDialogFragment.newInstance(args);
            dialogFragment.setAction((id, value) -> {
                if (id == tv_dialog_btn_right) del();
            });
        }
        return dialogFragment;
    }

    private void del() {
        Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
            RxEvent.DeleteWonder wonder = new RxEvent.DeleteWonder();
            AppLogger.e("正在发送删除请求:" + mCurrentPosition);
            wonder.position = mCurrentPosition;
            RxBus.getCacheInstance().post(wonder);
            subscriber.onNext(wonder.position);
            subscriber.onCompleted();
        }).flatMap(position -> RxBus.getCacheInstance().toObservable(RxEvent.DeleteWonderRsp.class).filter(rsp -> rsp.position == position).first())
                .subscribe(rsp -> {
                    if (rsp.success) {
                        AppLogger.e("删除成功");
                        if (mAdapter.getCount() > 0) {
                            mMediaList.remove(rsp.position);
                            mAdapter.notifyDataSetChanged();
                            int currentItem = mMediaPager.getCurrentItem();
                            mCurrentMediaBean = mMediaList.get(currentItem);
                        }
//                        if (mAdapter.getCount() == 0) {//说明已经删完了,则退出到每日精彩列表页面
                            finish();
//                        }
                    }
                }, AppLogger::e);
    }

    @OnClick({R.id.act_media_header_opt_delete, R.id.act_media_picture_opt_delete})
    public void delete() {
        initDeleteDialog().show(getSupportFragmentManager(), "delete");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MediaActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onDownloadPermissionDenied() {
        Toast.makeText(this, "下载文件需要权限,请手动开启", Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onDownloadPermissionNeverAskAgain() {
        Toast.makeText(this, "下载文件需要权限,请手动开启", Toast.LENGTH_SHORT).show();
    }

    @OnClick({R.id.act_media_header_opt_download, R.id.act_media_picture_opt_download})
    void download() {
        MediaActivityPermissionsDispatcher.downloadFileWithCheck(this);
    }


    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void downloadFile() {
        if (mCurrentViewType == DPWonderItem.TYPE_PIC) {
            mDownloadFile = new File(JConstant.MEDIA_PATH, mCurrentMediaBean.fileName);
        } else if (mCurrentViewType == DPWonderItem.TYPE_VIDEO) {
            mDownloadFile = new File(JConstant.MEDIA_DETAIL_VIDEO_DOWNLOAD_DIR, mCurrentMediaBean.fileName);
        } else {
            return;
        }

        if (mDownloadFile.exists()) {
            ToastUtil.showPositiveToast(getString(R.string.SAVED_PHOTOS));
            return;
        }

        Glide.with(this).load(new WonderGlideURL(mCurrentMediaBean))
                .downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                        ToastUtil.showPositiveToast(getString(R.string.SAVED_PHOTOS));
                        FileUtils.copyFile(resource, mDownloadFile);
                        MediaScannerConnection.scanFile(MediaActivity.this, new String[]{mDownloadFile.getAbsolutePath()}, null, (path, uri) -> {
                                    AppLogger.d("保存到相册成功了:" + path + ":" + uri.toString());
                                }
                        );
                        mDownloadFile = null;
                    }

                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        mDownloadFile = null;
                    }
                });
    }

    @OnClick({R.id.act_media_header_opt_share, R.id.act_media_picture_opt_share})
    public void share() {
        if (NetUtils.isNetworkAvailable(this)) {
            new WonderGlideURL(mCurrentMediaBean).fetchFile(file -> {
                Intent intent = new Intent(this, ShareMediaActivity.class);
                intent.putExtra(ShareConstant.SHARE_CONTENT, ShareConstant.SHARE_CONTENT_PICTURE);
                intent.putExtra(ShareConstant.SHARE_CONTENT_PICTURE_EXTRA_IMAGE_PATH, file);
                startActivity(intent);
            });
        } else {
            ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR_1));
        }
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
                        MediaActivityPermissionsDispatcher.downloadFileWithCheck(this);
                        break;
                    case R.id.dialog_media_video_share:
                        share();
                        break;
                }
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
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            rotateScreen();
        } else {
            cleanUpForExit(super::onBackPressed);
        }
    }

    private void cleanUpForExit(AnimatorUtils.OnFinish finish) {
        animateHeaderAndFooter(false, false, finish);
        if (mCurrentViewType == DPWonderItem.TYPE_VIDEO) {
            mMediaPlayer.reset();
            if (mVideoView != null) mVideoView.setVisibility(View.GONE);
            if (mVideoLoadingBar != null) mVideoLoadingBar.setVisibility(View.GONE);
            if (mPhotoView != null) mPhotoView.setVisibility(View.VISIBLE);
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

    private Runnable mBufferDelayCallback = () -> {
        //显示网络连接失败视图
    };

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
        switch (what) {
            case IjkMediaPlayer.MEDIA_INFO_BUFFERING_START:
                mVideoLoadingBar.setVisibility(View.VISIBLE);
                mContentRootView.postDelayed(mBufferDelayCallback, NETWORK_ERROR_WAIT_TIME);
                break;
            case IjkMediaPlayer.MEDIA_INFO_BUFFERING_END:
                mContentRootView.removeCallbacks(mBufferDelayCallback);
                mVideoLoadingBar.setVisibility(View.INVISIBLE);
                break;
            case IjkMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                break;
        }
        return false;
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        iMediaPlayer.pause();
        iMediaPlayer.seekTo(0);
        mCurrentPlayState = PLAY_STATE_PAUSED;
        mCurrentPosition = 0;
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
//        mMediaPlayer.pause();
    }


    @Override
    public void onPageSelected(int position) {
        mCurrentPosition = position;
        mCurrentMediaBean = mMediaList.get(position);
        int type = mCurrentMediaBean.msgType;
        boolean change = type != mCurrentViewType;
        mCurrentViewType = type;
        if (!change && mHeaderContainer.isShown() && mFooterContainer.isShown()) {
            setHeaderContent();
            setFooterContent();

        } else {
            animateHeaderAndFooter(true, true);
        }
    }

    private void startPlayVideo() {
        AppLogger.e("startPlay");
        mVideoView.setVisibility(View.VISIBLE);
        mVideoLoadingBar.setVisibility(View.VISIBLE);
//        String url = mCurrentMediaBean.fileName;
        String url = "http://yf.cylan.com.cn:82/Garfield/1045020208160b9706425470.mp4";
        mCurrentPlayState = PLAY_STATE_RESET;
        mMediaPlayer.reset();
        String proxyUrl = ((BaseApplication) getApplication()).getProxy().getProxyUrl(url);
        mMediaPlayer.setDataSource(proxyUrl);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
        if (mVideoView != null && mVideoView.isAvailable()) {
            mMediaPlayer.setSurface(new Surface(mVideoView.getSurfaceTexture()));
        }
        mMediaPlayer.prepareAsync();
        mCurrentPlayState = PLAY_STATE_PLAYING;
        updatePlayState();
    }

    private void startUpdateTime() {
        long position = mMediaPlayer.getCurrentPosition();
        mVideoPlayTime.setText(stringForTime((int) position));
        mVideoSeekBar.setProgress((int) position);
        if (mMediaPlayer.isPlaying()) mContentRootView.postDelayed(this::startUpdateTime, 200);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        mCurrentPlayState = PLAY_STATE_RESET;
        mMediaPlayer.reset();
        return false;
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int width, int height, int i2, int i3) {
    }

    @Override
    public void onPhotoTap(View view, float x, float y) {
        animateHeaderAndFooter();
    }

    private void animateHeaderAndFooter() {
        if (mHeaderContainer.isShown() || mFooterContainer.isShown()) {
            animateHeaderAndFooter(false, false);
        } else {
            animateHeaderAndFooter(true, true);
        }
    }


    @Override
    public void onOutsidePhotoTap() {
        animateHeaderAndFooter();
    }

}
