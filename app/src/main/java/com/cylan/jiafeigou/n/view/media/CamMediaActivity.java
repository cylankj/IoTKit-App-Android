package com.cylan.jiafeigou.n.view.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.module.JFGDPDevice;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.HackyViewPager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamMediaContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamMediaPresenterImpl;
import com.cylan.jiafeigou.n.view.home.ShareDialogFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.CamWarnGlideURL;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ShareUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.roundedimageview.RoundedImageView;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.KEY_SHARED_ELEMENT_LIST;

public class CamMediaActivity extends BaseFullScreenFragmentActivity<CamMediaContract.Presenter> implements
        CamMediaContract.View {

    public static final String KEY_BUNDLE = "key_bundle";
    public static final String KEY_TIME = "key_time";
    public static final String KEY_INDEX = "key_index";

    @BindView(R.id.vp_container)
    HackyViewPager vpContainer;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.fLayout_cam_handle_bar)
    FrameLayout fLayoutCamHandleBar;
    @BindView(R.id.lLayout_preview)
    LinearLayout lLayoutPreview;

    private int currentIndex = -1, previewFocusIndex = -1;
    private DpMsgDefine.DPAlarm alarmMsg;
    private String uuid;
    private JFGDPDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_media);
        ButterKnife.bind(this);
        uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        device = DataSourceManager.getInstance().getJFGDevice(uuid);
        basePresenter = new CamMediaPresenterImpl(this, uuid);
        alarmMsg = getIntent().getParcelableExtra(KEY_BUNDLE);
        CustomAdapter customAdapter = new CustomAdapter(getSupportFragmentManager());
        customAdapter.setDpAlarm(alarmMsg);
        vpContainer.setAdapter(customAdapter);
        vpContainer.setCurrentItem(currentIndex = getIntent().getIntExtra(KEY_INDEX, 0));
        customAdapter.setCallback(object -> {
            AnimatorUtils.slideAuto(customToolbar, true);
            AnimatorUtils.slideAuto(fLayoutCamHandleBar, false);
        });
        vpContainer.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                currentIndex = position;
            }
        });
        decideWhichView();
        customToolbar.setBackAction(v -> onBackPressed());
    }

    private void decideWhichView() {
        if (device != null && JFGRules.isNeedPanoramicView(device.pid)) {
            vpContainer.setLocked(true);
            lLayoutPreview.setVisibility(View.VISIBLE);
            int count = MiscUtils.getCount(alarmMsg.fileIndex);
            for (int i = 3; i > count; i--) {
                View v = lLayoutPreview.getChildAt(i - 1);
                v.setVisibility(View.GONE);
            }
            for (int i = 0; i < count; i++) {
                final View v = lLayoutPreview.getChildAt(i);
                final int jjj = i;
                v.setOnClickListener(view -> {
                    if (previewFocusIndex != jjj) {
                        previewFocusIndex = jjj;
                        updateFocus(true, jjj);
                    }
                });
                //可能出错,不是对应的index
                GlideUrl url = new CamWarnGlideURL(alarmMsg, i, uuid);
                Glide.with(ContextUtils.getContext())
                        .load(url)
                        .asBitmap()
                        .format(DecodeFormat.DEFAULT)
                        .into(new SimpleTarget<Bitmap>(150, 150) {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                ((RoundedImageView) v).setImageBitmap(resource);
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                AppLogger.e("load failed: " + e.getLocalizedMessage());
                            }
                        });
            }
        } else {
            //normal view
            lLayoutPreview.setVisibility(View.GONE);
        }
        updateFocus(false, currentIndex);
    }

    private static String makeFragmentName(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
    }

    private void updateFocus(boolean auto, int index) {
        int count = MiscUtils.getCount(alarmMsg.fileIndex);
        if (device != null && JFGRules.isNeedPanoramicView(device.pid)) {
            //全景需要兼容,这里的tag的构造方式,看FragmentPagerAdapter,最后一个方法
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(makeFragmentName(vpContainer.getId(), 0));
            if (auto && fragment != null && fragment instanceof PanoramicViewFragment) {
                ((PanoramicViewFragment) fragment).loadBitmap(index);
            }
        } else {
            vpContainer.setCurrentItem(index, false);
        }
        for (int i = 0; i < count; i++) {
            RoundedImageView v = (RoundedImageView) lLayoutPreview.getChildAt(i);
            if (i == index) {
                v.setBorderColor(getResources().getColor(R.color.color_4b9fd5));
                v.setAlpha(1.0f);
                v.setScaleX(1.0f);
                v.setScaleY(1.0f);
            } else {
                v.setBorderColor(getResources().getColor(R.color.color_white));
                v.setAlpha(0.3f);
                v.setScaleX(0.8f);
                v.setScaleY(0.8f);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        customToolbar.setToolbarTitle(TimeUtils.getMediaPicTimeInString(alarmMsg.time * 1000L));
    }

    @OnClick({R.id.imgV_big_pic_download,
            R.id.imgV_big_pic_share,
            R.id.imgV_big_pic_collect})
    public void onClick(View view) {
        ViewUtils.deBounceClick(view);
        switch (view.getId()) {
            case R.id.imgV_big_pic_download:
                if (basePresenter != null)
                    basePresenter.saveImage(new CamWarnGlideURL(alarmMsg, currentIndex, uuid));
                break;
            case R.id.imgV_big_pic_share:
                if (NetUtils.getJfgNetType(getContext()) == 0) {
                    ToastUtil.showToast(getString(R.string.NoNetworkTips));
                    return;
                }
                ShareDialogFragment fragment = initShareDialog();
                fragment.setPictureURL(new CamWarnGlideURL(alarmMsg, currentIndex, uuid));
                fragment.show(getSupportFragmentManager(), "ShareDialogFragment");
                break;
            case R.id.imgV_big_pic_collect:
                if (NetUtils.getJfgNetType(getContext()) == 0) {
                    ToastUtil.showToast(getString(R.string.NoNetworkTips));
                    return;
                }
                if (basePresenter != null)
                    basePresenter.collect(currentIndex, alarmMsg, new CamWarnGlideURL(alarmMsg, currentIndex, uuid));
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finishExt();
    }

    private WeakReference<ShareDialogFragment> shareDialogFragmentWeakReference;

    private ShareDialogFragment initShareDialog() {
        if (shareDialogFragmentWeakReference == null || shareDialogFragmentWeakReference.get() == null) {
            shareDialogFragmentWeakReference = new WeakReference<>(ShareDialogFragment.newInstance((Bundle) null));
        }
        return shareDialogFragmentWeakReference.get();
    }

    @Override
    public void setPresenter(CamMediaContract.Presenter presenter) {
        basePresenter = presenter;
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void savePicResult(boolean state) {
        if (state) {
            ToastUtil.showPositiveToast(getString(R.string.SAVED_PHOTOS));
        } else ToastUtil.showNegativeToast(getString(R.string.set_failed));
    }

    @Override
    public void onErr(int err) {
        switch (err) {
            case 1:
                ToastUtil.showNegativeToast(getString(R.string.Tap2_share_unabletoshare));
                break;
        }
    }

    private class CustomAdapter extends FragmentPagerAdapter {
        private DpMsgDefine.DPAlarm dpAlarm;
        private NormalMediaFragment.CallBack callBack;

        public void setDpAlarm(DpMsgDefine.DPAlarm dpAlarm) {
            this.dpAlarm = dpAlarm;
        }

        public CustomAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(KEY_SHARED_ELEMENT_LIST, dpAlarm);
            bundle.putInt(KEY_INDEX, position);
            bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
            bundle.putInt("totalCount", MiscUtils.getCount(dpAlarm.fileIndex));
            IBaseFragment fragment = null;
            if (device != null && JFGRules.isNeedPanoramicView(device.pid)) {
                fragment = PanoramicViewFragment.newInstance(bundle);
            } else {
                fragment = NormalMediaFragment.newInstance(bundle);
            }
            fragment.setCallBack(this.callBack);
            return fragment;
        }

        @Override
        public int getCount() {
            //全景图片不适合使用viewpager,虽然用起来很简单,切换的时候有bug.
            if (device != null && JFGRules.isNeedPanoramicView(device.pid)) return 1;
            Log.d("getCount", "getCount: ");
            return MiscUtils.getCount(dpAlarm.fileIndex);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //do nothing
        }

        private void setCallback(IBaseFragment.CallBack callback) {
            this.callBack = callback;
        }
    }
}
