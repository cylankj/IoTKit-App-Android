package com.cylan.jiafeigou.n.view.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.view.cam.CameraLiveFragment;
import com.cylan.jiafeigou.n.view.cam.FragmentFacilityInformation;
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
    private FragmentFacilityInformation fragmentFacilityInformation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_live);
        fragmentFacilityInformation = FragmentFacilityInformation.newInstance(new Bundle());
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        ButterKnife.bind(this);
        initTopBar();
        initAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        finish();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            overridePendingTransition(R.anim.slide_in_left_without_interpolator, R.anim.slide_out_right_without_interpolator);
        }
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
        loadFragment(R.id.fLayout_information_message, fragmentFacilityInformation);
    }

    /**
     * 用来加载fragment的方法。
     */
    private void loadFragment(int id,FragmentFacilityInformation fragment) {
        getSupportFragmentManager().beginTransaction()
                //如果需要动画，可以把动画添加进来
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(id,fragment,"FragmentFacilityInformation")
                .addToBackStack("FragmentFacilityInformation")
                .commit();
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
        return CameraLiveFragment.newInstance(bundle);
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