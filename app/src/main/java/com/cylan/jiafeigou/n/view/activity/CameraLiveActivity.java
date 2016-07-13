package com.cylan.jiafeigou.n.view.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamMessageListPresenterImpl;
import com.cylan.jiafeigou.n.view.cam.CamMessageListFragment;
import com.cylan.jiafeigou.n.view.cam.CameraLiveFragment;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.indicator.PagerSlidingTabStrip;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CameraLiveActivity extends BaseFullScreenFragmentActivity {

    @BindView(R.id.imgV_nav_back)
    ImageView imgVNavBack;
    @BindView(R.id.v_indicator)
    PagerSlidingTabStrip vIndicator;
    @BindView(R.id.rLayout_camera_live_top_bar)
    RelativeLayout rLayoutCameraLiveTopBar;
    @BindView(R.id.vp_camera_live)
    ViewPager vpCameraLive;
    @BindView(R.id.imgV_camera_title_top_setting)
    ImageView imgVCameraTitleTopSetting;

    private SimpleListener simpleListener = new SimpleListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_live);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initTopBar();
        initAdapter();
    }

    private void initAdapter() {
        SimpleAdapterPager simpleAdapterPager = new SimpleAdapterPager(getSupportFragmentManager());
        vpCameraLive.setAdapter(simpleAdapterPager);
        vIndicator.setViewPager(vpCameraLive);
        vIndicator.setOnPageChangeListener(simpleListener);
    }

    private void initTopBar() {
        ViewUtils.setViewPaddingStatusBar(rLayoutCameraLiveTopBar);
    }

    @Override
    public void onBackPressed() {
        finish();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            overridePendingTransition(R.anim.slide_in_left_without_interpolator, R.anim.slide_out_right_without_interpolator);
        }
    }

    @OnClick(R.id.imgV_nav_back)
    public void onNavBack() {
        onBackPressed();
    }

    @OnClick(R.id.imgV_camera_title_top_setting)
    public void onClickSetting() {
    }


    private class SimpleListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}

class SimpleAdapterPager extends FragmentPagerAdapter {

    public SimpleAdapterPager(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("what", position);
        if (position == 0) {
            return CameraLiveFragment.newInstance(bundle);
        } else {
            CamMessageListFragment fragment = CamMessageListFragment.newInstance(new Bundle());
            new CamMessageListPresenterImpl(fragment);
            return fragment;
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    @Override
    public int getCount() {
        return 2;
    }


    @Override
    public CharSequence getPageTitle(int position) {
        return position == 0 ? "视频" : "消息";
    }
}