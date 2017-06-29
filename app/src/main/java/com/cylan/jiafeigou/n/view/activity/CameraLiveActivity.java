package com.cylan.jiafeigou.n.view.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.ver.AbstractVersion;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.view.cam.CamMessageListFragment;
import com.cylan.jiafeigou.n.view.cam.CameraLiveFragmentEx;
import com.cylan.jiafeigou.support.badge.Badge;
import com.cylan.jiafeigou.support.badge.TreeNode;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.CustomViewPager;
import com.cylan.jiafeigou.widget.HintTextView;
import com.cylan.jiafeigou.widget.ImageViewTip;
import com.cylan.jiafeigou.widget.TmpHint;
import com.cylan.jiafeigou.widget.indicator.PagerSlidingTabStrip;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.support.photoselect.helpers.Constants.REQUEST_CODE;

@Badge(parentTag = "NewHomeActivity")
public class CameraLiveActivity extends BaseFullScreenFragmentActivity {

    @BindView(R.id.vp_camera_live)
    CustomViewPager vpCameraLive;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;

    private ImageViewTip imgVCameraTitleTopSetting;
    private PagerSlidingTabStrip vIndicator;

    private String uuid;

//    private Bundle currentBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_live);
        ButterKnife.bind(this);
        this.uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        if (TextUtils.isEmpty(uuid)) {
            AppLogger.e("what the hell uuid is null");
            finishExt();
        }
        initToolbar(getIntent().hasExtra(JConstant.KEY_NEW_MSG_PASS));
        initAdapter();
        Intent intent = getIntent();
        boolean jumpToMessage = intent.hasExtra(JConstant.KEY_JUMP_TO_MESSAGE);
        if (jumpToMessage) {
            //跳转到
            if (vpCameraLive.getAdapter().getCount() > 1) {
                vpCameraLive.setCurrentItem(1);
            }
        }
        JConstant.KEY_CURRENT_PLAY_VIEW = this.getClass().getName();
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
    protected void onStart() {
        super.onStart();
        if (MiscUtils.isLand()) {
            handleSystemBar(false, 1);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        final boolean isLandScape = this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        getWindow().getDecorView().post(() -> {
            if (isLandScape) {
                handleSystemBar(false, 1);
            } else {
                showSystemUI();
            }
            customToolbar.setVisibility(isLandScape ? View.GONE : View.VISIBLE);
            vpCameraLive.setPagingEnabled(!isLandScape);
        });
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
            Log.d("show", "show: " + visibility);
            if (visibility == 0 && MiscUtils.isLand())
                handleSystemBar(false, 100);
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        Log.d("onNewIntent", "onNewIntent:" + uuid);
        if (TextUtils.isEmpty(uuid)) {
            AppLogger.e("what the hell uuid is null");
            finishExt();
        }
        initToolbar(getIntent().hasExtra(JConstant.KEY_NEW_MSG_PASS));
        initAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateRedHint();
    }

    private void updateRedHint() {
        if (imgVCameraTitleTopSetting != null) {
            TreeNode node = BaseApplication.getAppComponent().getTreeHelper().findTreeNodeByName(this.getClass().getSimpleName());
            boolean newNode = node != null && node.getTraversalCount() > 0;
            boolean result = hasNewFirmware();
            //延时摄影，暂时隐藏。
            imgVCameraTitleTopSetting.setShowDot(result || newNode);
        }
    }

    /**
     * true 有新版本
     *
     * @return
     */
    private boolean hasNewFirmware() {
        try {
            final String content = PreferencesUtils.getString(JConstant.KEY_FIRMWARE_CONTENT + getUuid());
            AbstractVersion.BinVersion version = new Gson().fromJson(content, AbstractVersion.BinVersion.class);
            final String newVersion = version.getTagVersion();
            final String currentVersion = BaseApplication.getAppComponent().getSourceManager().getDevice(getUuid()).$(207, "");
            if (BindUtils.versionCompare(currentVersion, newVersion) < 0) {
                PreferencesUtils.remove(JConstant.KEY_FIRMWARE_CONTENT + getUuid());
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * 夸fragment传值
     *
     * @param bundle
     */
    public void addPutBundle(Bundle bundle) {
        final String tag = MiscUtils.makeFragmentName(vpCameraLive.getId(), 0);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null && fragment instanceof CameraLiveFragmentEx) {
            fragment.getArguments().putAll(bundle);
            vpCameraLive.setCurrentItem(0);
        }
    }

    private void initAdapter() {
        if (vpCameraLive.getAdapter() == null) {
            SimpleAdapterPager simpleAdapterPager = new SimpleAdapterPager(getSupportFragmentManager(), uuid);
            vpCameraLive.setAdapter(simpleAdapterPager);
        }
        final String tag = MiscUtils.makeFragmentName(vpCameraLive.getId(), 0);
        vpCameraLive.setPagingScrollListener(event -> {
            //消息页面,不需要拦截
            if (vpCameraLive.getCurrentItem() == 1) return true;
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
            if (fragment != null && fragment instanceof CameraLiveFragmentEx) {
                Rect rect = ((CameraLiveFragmentEx) fragment).mLiveViewRectInWindow;
                //true:不在区域内，
                boolean contains = !rect.contains((int) event.getRawX(), (int) event.getY());
                Log.d("contains", "contains:" + contains);
                return contains;
            } else
                return true;
        });
    }

    private void initToolbar(final boolean newMsg) {
        customToolbar.post(() -> {
            vIndicator = (PagerSlidingTabStrip) customToolbar.findViewById(R.id.v_indicator);
            vIndicator.setViewPager(vpCameraLive);
            vIndicator.setOnPageChangeListener(new SimplePageListener(uuid));
            imgVCameraTitleTopSetting = (ImageViewTip) customToolbar.findViewById(R.id.imgV_camera_title_top_setting);
            View vHint = vIndicator.findViewById(getString(R.string.Tap1_Camera_Messages).hashCode());
            if (vHint != null && vHint instanceof TmpHint) {
                ((TmpHint) vHint).showHint(newMsg);
            }
            updateRedHint();
            customToolbar.findViewById(R.id.imgV_nav_back).setOnClickListener(v -> onNavBack());
        });
    }


    @Override
    public void onBackPressed() {
        Log.d("onBackPressed", this.getClass().getSimpleName());
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

    public void onNavBack() {
        onBackPressed();
    }

    /**
     * 当点击右上角的螺母按钮时，跳转到设备信息页面
     */
    @OnClick(R.id.imgV_camera_title_top_setting)
    public void onClickSetting() {
        Log.d("onClickSetting", "onClickSetting: " + getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID));
        Log.d("onClickSetting", "onClickSetting  uuid: " + uuid);
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
        private String uuid;

        private SimplePageListener(String uuid) {
            this.uuid = uuid;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (position == 1) {
                try {
                    BaseApplication.getAppComponent().getSourceManager().clearValue(uuid, 1001, 1002, 1003);
                    View vHint = vIndicator.findViewById(getString(R.string.Tap1_Camera_Messages).hashCode());
                    if (vHint != null && vHint instanceof TmpHint) {
                        ((TmpHint) vHint).showHint(false);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
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
    private boolean justJump = false;

    public SimpleAdapterPager(FragmentManager fm, String uuid) {
        super(fm);
        this.uuid = uuid;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        if (position == 0) {
            return CameraLiveFragmentEx.newInstance(bundle);
        } else {
            return CamMessageListFragment.newInstance(bundle);
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    }

    @Override
    public int getCount() {
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        String shareAccount = device == null ? "" : device.shareAccount;
        return !TextUtils.isEmpty(shareAccount) ? 1 : 2;
    }


    @Override
    public CharSequence getPageTitle(int position) {
        return position == 0 ? ContextUtils.getContext().getString(R.string.Tap1_Camera_Video) : ContextUtils.getContext().getString(R.string.Tap1_Camera_Messages);
    }

    public void justJump(boolean jumpToMessage) {
        this.justJump = jumpToMessage;
    }
}