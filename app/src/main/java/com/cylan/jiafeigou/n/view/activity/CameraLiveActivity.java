package com.cylan.jiafeigou.n.view.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
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
import com.cylan.jiafeigou.n.mvp.impl.cam.CamLivePresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamMessageListPresenterImpl;
import com.cylan.jiafeigou.n.view.cam.CamMessageListFragment;
import com.cylan.jiafeigou.n.view.cam.CameraLiveFragment;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomViewPager;
import com.cylan.jiafeigou.widget.indicator.PagerSlidingTabStrip;
import com.superlog.SLog;

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
    CustomViewPager vpCameraLive;
    @BindView(R.id.imgV_camera_title_top_setting)
    ImageView imgVCameraTitleTopSetting;

    private SimplePageListener simpleListener = new SimplePageListener();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_live);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        ButterKnife.bind(this);
        initTopBar();
        initAdapter();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SLog.d("onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        SLog.d("onRestoreInstanceState");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        final boolean isLandScape = this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                handleSystemBar(!isLandScape, 1000);
                rLayoutCameraLiveTopBar.setVisibility(isLandScape ? View.GONE : View.VISIBLE);
                vpCameraLive.setPagingEnabled(!isLandScape);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
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
        if (checkExtraChildFragment()) {
            return;
        } else if (checkExtraFragment())
            return;
        if (this.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) {
            ViewUtils.setRequestedOrientation(this,
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return;
        }
        finishExt();
    }

    @OnClick(R.id.imgV_nav_back)
    public void onNavBack() {
        onBackPressed();
    }

    /**
     * 当点击右上角的螺母按钮时，跳转到设备信息页面
     */
    @OnClick(R.id.imgV_camera_title_top_setting)
    public void onClickSetting() {
        startActivity(new Intent(this, CamSettingActivity.class),
                ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(),
                        R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
    }


    private class SimplePageListener implements ViewPager.OnPageChangeListener {
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
            CameraLiveFragment fragment = CameraLiveFragment.newInstance(bundle);
            new CamLivePresenterImpl(fragment);
            return fragment;
        } else {
            CamMessageListFragment fragment = CamMessageListFragment.newInstance(new Bundle());
            new CamMessageListPresenterImpl(fragment);
            return fragment;
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        super.destroyItem(container, position, object);
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