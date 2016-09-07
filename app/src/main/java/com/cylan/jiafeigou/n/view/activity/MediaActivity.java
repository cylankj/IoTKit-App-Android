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
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.model.MediaBean;
import com.cylan.jiafeigou.n.view.media.DetailsFragment;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MediaActivity extends FragmentActivity {

    private static final String STATE_CURRENT_PAGE_POSITION = "state_current_page_position";

    private int mStartingPosition;
    private int mCurrentPosition;
    private boolean mIsReturning;

    private DetailsFragment mCurrentDetailsFragment;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        postponeEnterTransition();
        setEnterSharedElementCallback(mCallback);

        mStartingPosition = getIntent().getIntExtra(JConstant.KEY_SHARED_ELEMENT_POSITION, 0);
        if (savedInstanceState == null) {
            mCurrentPosition = mStartingPosition;
        } else {
            mCurrentPosition = savedInstanceState.getInt(STATE_CURRENT_PAGE_POSITION);
        }

        ArrayList<MediaBean> list = getIntent().getParcelableArrayListExtra(JConstant.KEY_SHARED_ELEMENT_LIST);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new DetailsFragmentPagerAdapter(getSupportFragmentManager(), list));
        pager.setCurrentItem(mCurrentPosition);
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
            }
        });
    }

    private final SharedElementCallback mCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            AppLogger.d("transition:mIsReturning " + mIsReturning);
            AppLogger.d("transition:mStartingPosition " + mStartingPosition);
            AppLogger.d("transition:mCurrentPosition " + mCurrentPosition);
            if (mIsReturning) {
                ImageView sharedElement = mCurrentDetailsFragment.getAlbumImage();
                if (sharedElement == null) {
                    // If shared element is null, then it has been scrolled off screen and
                    // no longer visible. In this case we cancel the shared element transition by
                    // removing the shared element from the shared elements map.
                    names.clear();
                    sharedElements.clear();
                } else if (mStartingPosition != mCurrentPosition) {
                    // If the user has swiped to a different ViewPager page, then we need to
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


    private class DetailsFragmentPagerAdapter extends FragmentStatePagerAdapter {
        ArrayList<MediaBean> list;

        public DetailsFragmentPagerAdapter(FragmentManager fm, ArrayList<MediaBean> list) {
            super(fm);
            this.list = list;
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            bundle.putString(DetailsFragment.KEY_MEDIA_URL, list.get(position).srcUrl);
//            if (list.get(position).mediaType == MediaBean.TYPE_PIC) {
            return DetailsFragment.newInstance(position,
                    position,
                    list.get(position).srcUrl);
//            } else {
//                return null;
//            }
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            //this item
            mCurrentDetailsFragment = (DetailsFragment) object;
        }

        @Override
        public int getCount() {
            return list == null ? 0 : list.size();
        }
    }
}
