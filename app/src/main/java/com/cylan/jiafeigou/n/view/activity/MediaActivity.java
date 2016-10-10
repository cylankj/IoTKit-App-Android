package com.cylan.jiafeigou.n.view.activity;

import android.annotation.TargetApi;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.HackyViewPager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.model.MediaBean;
import com.cylan.jiafeigou.n.view.media.PicDetailsFragment;
import com.cylan.jiafeigou.n.view.media.VideoDetailsFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * https://plus.google.com/+AlexLockwood/posts/RPtwZ5nNebb
 * 存在Transition问题。
 */
public class MediaActivity extends FragmentActivity {

    private static final String STATE_CURRENT_PAGE_POSITION = "state_current_page_position";

    private int mStartingPosition;
    private int mCurrentPosition;
    private boolean mIsReturning;
    @BindView(R.id.tv_big_pic_close)
    ImageView tvBigPicClose;
    @BindView(R.id.tv_big_pic_title)
    TextView tvBigPicTitle;
    @BindView(R.id.fLayout_details_title)
    FrameLayout fLayoutDetailsTitle;
    @BindView(R.id.imgV_big_pic_download)
    ImageView imgVBigPicDownload;
    @BindView(R.id.imgV_big_pic_share)
    ImageView imgVBigPicShare;
    @BindView(R.id.imgV_big_pic_collect)
    ImageView imgVBigPicCollect;
    @BindView(R.id.fLayout_details_action_bar)
    FrameLayout fLayoutDetailsActionBar;
    private PicDetailsFragment mCurrentDetailsFragment;

    private ArrayList<MediaBean> beanArrayList;

    //    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
            initSharedElementCallback();
            setEnterSharedElementCallback(mCallback);
        }
        ViewUtils.setViewMarginStatusBar(fLayoutDetailsTitle);
        mStartingPosition = getIntent().getIntExtra(JConstant.KEY_SHARED_ELEMENT_STARTED_POSITION, 0);
        if (savedInstanceState == null) {
            mCurrentPosition = mStartingPosition;
        } else {
            mCurrentPosition = savedInstanceState.getInt(STATE_CURRENT_PAGE_POSITION);
        }
        ArrayList<MediaBean> list = getIntent().getParcelableArrayListExtra(JConstant.KEY_SHARED_ELEMENT_LIST);
        beanArrayList = list;
        //this adapter fix 'java.lang.IllegalArgumentException: pointerIndex out of range'
        HackyViewPager pager = (HackyViewPager) findViewById(R.id.pager);
        pager.setAdapter(new DetailsFragmentPagerAdapter(getSupportFragmentManager(), list));
        pager.setCurrentItem(mCurrentPosition);
        //刷新当前时间
        updateTitle(mCurrentPosition);
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                updateActionBar(position);
                updateTitle(position);
            }
        });
        AppLogger.d("MediaActivity:onCreate");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ViewUtils.removeActivityFromTransitionManager(this);
    }

    /**
     * 刷新图片头部日期
     *
     * @param index
     */
    private void updateTitle(final int index) {
        if (beanArrayList != null && index < beanArrayList.size()) {
            final int mediaType = beanArrayList.get(index).mediaType;
            final long time = beanArrayList.get(index).time;
            if (mediaType == MediaBean.TYPE_PIC)
                tvBigPicTitle.setText(TimeUtils.getMediaPicTimeInString(time));
            else if (mediaType == MediaBean.TYPE_VIDEO)
                tvBigPicTitle.setText(TimeUtils.getMediaVideoTimeInString(time));
        }
    }

    private void updateActionBar(final int index) {

    }

    private SharedElementCallback mCallback;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initSharedElementCallback() {
        mCallback = new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                if (mIsReturning) {
                    ImageView sharedElement = mCurrentDetailsFragment.getAlbumImage();
                    AppLogger.d("transition:mStartingPosition " + mStartingPosition);
                    AppLogger.d("transition:mCurrentPosition " + mCurrentPosition);
                    if (sharedElement == null) {
                        // If shared element is null, then it has been scrolled off screen and
                        // no longer visible. In this case we cancel the shared element transition by
                        // removing the shared element from the shared elements map.
                        names.clear();
                        sharedElements.clear();
                    } else if (mStartingPosition != mCurrentPosition) {
                        // If the user has swiped to activity_cloud_live_mesg_video_talk_item different ViewPager page, then we need to
                        // remove the old shared element and replace it with the new shared element
                        // that should be transitioned instead.
                        final String transitionName = sharedElement.getTransitionName();
                        AppLogger.d("transition:transitionName " + transitionName);
                        names.clear();
                        names.add(transitionName);
                        sharedElements.clear();
                        sharedElements.put(transitionName, sharedElement);
                    }
                }
            }
        };
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_PAGE_POSITION, mCurrentPosition);
    }

    @Override
    public void finishAfterTransition() {
        mIsReturning = true;
        Intent data = new Intent();
        data.putExtra(JConstant.EXTRA_STARTING_ALBUM_POSITION, mStartingPosition);
        data.putExtra(JConstant.EXTRA_CURRENT_ALBUM_POSITION, mCurrentPosition);
        setResult(RESULT_OK, data);
        super.finishAfterTransition();
    }

    @OnClick({R.id.tv_big_pic_close,
            R.id.imgV_big_pic_download,
            R.id.imgV_big_pic_share,
            R.id.imgV_big_pic_collect})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_big_pic_close:
                onBackPressed();
                break;
            case R.id.imgV_big_pic_download:
                break;
            case R.id.imgV_big_pic_share:
                break;
            case R.id.imgV_big_pic_collect:
                break;
        }
    }

    private class DetailsFragmentPagerAdapter extends FragmentStatePagerAdapter {
        ArrayList<MediaBean> list;

        public DetailsFragmentPagerAdapter(FragmentManager fm, ArrayList<MediaBean> list) {
            super(fm);
            this.list = list;
        }

        @Override
        public Fragment getItem(int position) {
            if (list.get(position).mediaType == MediaBean.TYPE_PIC) {
                return PicDetailsFragment.newInstance(position,
                        mStartingPosition,
                        list.get(position).srcUrl);
            } else if (list.get(position).mediaType == MediaBean.TYPE_VIDEO) {
                return VideoDetailsFragment.newInstance(position,
                        mStartingPosition,
                        list.get(position).srcUrl);
            } else {
                return null;
            }
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            //this item
            if (object instanceof PicDetailsFragment)
                mCurrentDetailsFragment = (PicDetailsFragment) object;
            AppLogger.d("transition: setPrimaryItem: " + position);
        }

        @Override
        public int getCount() {
            return list == null ? 0 : list.size();
        }

    }
}
