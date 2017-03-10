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
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.view.cam.CamMessageListFragment;
import com.cylan.jiafeigou.n.view.cam.CameraLiveFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomViewPager;
import com.cylan.jiafeigou.widget.indicator.PagerSlidingTabStrip;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract.TYPE_HISTORY;
import static com.cylan.jiafeigou.support.photoselect.helpers.Constants.REQUEST_CODE;


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
    private String uuid;
    private SimplePageListener simpleListener = new SimplePageListener();

    private Bundle currentBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_live);
        this.uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        ButterKnife.bind(this);
        initTopBar();
        initAdapter();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        AppLogger.d("onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        AppLogger.d("onRestoreInstanceState");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        final boolean isLandScape = this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        getWindow().getDecorView().post(() -> {
            handleSystemBar(!isLandScape, 1000);
            rLayoutCameraLiveTopBar.setVisibility(isLandScape ? View.GONE : View.VISIBLE);
            vpCameraLive.setPagingEnabled(!isLandScape);
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void setCurrentBundle(Bundle currentBundle) {
        this.currentBundle = currentBundle;
        if (vpCameraLive.getCurrentItem() == 0) return;
        if (currentBundle.getInt(JConstant.KEY_CAM_LIVE_PAGE_PLAY_TYPE, TYPE_HISTORY) == TYPE_HISTORY) {
            vpCameraLive.setCurrentItem(0);
        }
    }

    public Bundle getCurrentBundle() {
        return currentBundle;
    }

    private void initAdapter() {
        SimpleAdapterPager simpleAdapterPager = new SimpleAdapterPager(getSupportFragmentManager(),
                uuid);
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
        Intent intent = new Intent(this, CamSettingActivity.class);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        startActivity(intent, ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(),
                R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    finishExt();
                }
                break;
        }
    }

    private class SimplePageListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (position == 1) {
                Pair<Integer, Long> pair = DataSourceManager.getInstance().getUnreadCount(uuid, 222, 505, 512);
                if (pair != null && pair.first > 0) {
                    DataSourceManager.getInstance().clearUnread(uuid, 505, 512, 222);
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}

class SimpleAdapterPager extends FragmentPagerAdapter {

    private String uuid;

    public SimpleAdapterPager(FragmentManager fm, String uuid) {
        super(fm);
        this.uuid = uuid;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        if (position == 0) {
            return CameraLiveFragment.newInstance(bundle);
        } else {
            return CamMessageListFragment.newInstance(bundle);
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    }

    @Override
    public int getCount() {
        Device device = DataSourceManager.getInstance().getJFGDevice(uuid);
        String shareAccount = device == null ? "" : device.shareAccount;
        return !TextUtils.isEmpty(shareAccount) ? 1 : 2;
    }


    @Override
    public CharSequence getPageTitle(int position) {
        return position == 0 ? ContextUtils.getContext().getString(R.string.Tap1_Camera_Video) : ContextUtils.getContext().getString(R.string.Tap1_Camera_Messages);
    }
}