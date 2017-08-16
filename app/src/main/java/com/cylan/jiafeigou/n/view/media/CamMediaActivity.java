package com.cylan.jiafeigou.n.view.media;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.HackyViewPager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamMediaContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamMediaPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.share.ShareManager;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.CamWarnGlideURL;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.pop.RelativePopupWindow;
import com.cylan.jiafeigou.widget.pop.SimplePopupWindow;
import com.cylan.jiafeigou.widget.roundedimageview.RoundedImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

import static com.cylan.jiafeigou.misc.JConstant.KEY_SHARED_ELEMENT_LIST;

@RuntimePermissions
public class CamMediaActivity extends BaseFullScreenFragmentActivity<CamMediaContract.Presenter> implements
        CamMediaContract.View {

    public static final String KEY_BUNDLE = "key_bundle";
    public static final String KEY_INDEX = "key_index";
    public static final String KEY_BELL_RECORD_BUNDLE = "key_bell_record_bundle";

    @BindView(R.id.vp_container)
    HackyViewPager vpContainer;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.fLayout_cam_handle_bar)
    LinearLayout fLayoutCamHandleBar;
    @BindView(R.id.lLayout_preview)
    LinearLayout lLayoutPreview;
    @BindView(R.id.imgV_big_pic_collect)
    ImageView imgVBigPicCollect;
    @BindView(R.id.imgV_big_pic_share)
    ImageView imgVBigPicShare;
    @BindView(R.id.imgV_big_pic_download)
    ImageView imgVBigPicDownload;

    private int currentIndex = -1;
    private CamMessageBean camMessageBean;
    private String uuid;
    private Device device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_media);
        ButterKnife.bind(this);
        uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        camMessageBean = getIntent().getParcelableExtra(KEY_BUNDLE);

        device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        basePresenter = new CamMediaPresenterImpl(this, uuid);

        CustomAdapter customAdapter = new CustomAdapter(getSupportFragmentManager());
        customAdapter.setCamMessageBean(camMessageBean);
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
                if (basePresenter != null)
                    basePresenter.checkCollection(MiscUtils.getVersion(camMessageBean), currentIndex, camMessageBean);
            }
        });

        customToolbar.setBackAction(v -> onBackPressed());
        imgVBigPicShare.setVisibility(getResources().getBoolean(R.bool.show_share_btn) ? View.VISIBLE : View.GONE);
    }

    private void showCollectCase() {
        runOnUiThread(() -> {
            boolean needShow = PreferencesUtils.getBoolean(JConstant.NEED_SHOW_COLLECT_USE_CASE, true);
            if (needShow) {
                PreferencesUtils.putBoolean(JConstant.NEED_SHOW_COLLECT_USE_CASE, false);
                imgVBigPicCollect.post(() -> {
                    SimplePopupWindow popupWindow = new SimplePopupWindow(this, R.drawable.collect_tips, R.string.Tap1_BigPic_FavoriteTips);
                    popupWindow.showOnAnchor(imgVBigPicCollect, RelativePopupWindow.VerticalPosition.ABOVE,
                            RelativePopupWindow.HorizontalPosition.ALIGN_RIGHT, (int) (-imgVBigPicCollect.getWidth() / 2 + getResources().getDimension(R.dimen.x18)), 0);
                });
            }
        });
    }

    private void decideWhichView() {
        if (device != null && JFGRules.isNeedPanoramicView(device.pid)) {
            vpContainer.setLocked(true);
            findViewById(R.id.v_layout).setVisibility(View.VISIBLE);
            int count = MiscUtils.getCount(MiscUtils.getFileIndex(camMessageBean));

            for (int i = 3; i > count; i--) {
                View v = lLayoutPreview.getChildAt(i - 1);
                v.setVisibility(View.GONE);
            }
            for (int i = 0; i < count; i++) {
                final View v = lLayoutPreview.getChildAt(i);
                final int jjj = i;
                v.setOnClickListener(view -> {
                    if (currentIndex == -1 || currentIndex != jjj) {
                        currentIndex = jjj;
                        updateFocus(true, jjj);
                    }
                });
                //可能出错,不是对应的index
                CamWarnGlideURL url = MiscUtils.getCamWarnUrl(uuid, camMessageBean, i + 1);
                Glide.with(this)
                        .load(url)
                        .asBitmap()
                        .format(DecodeFormat.DEFAULT)
                        .listener(new RequestListener<CamWarnGlideURL, Bitmap>() {
                            @Override
                            public boolean onException(Exception e, CamWarnGlideURL model, Target<Bitmap> target, boolean isFirstResource) {
                                AppLogger.e("load failed: " + model.getTime() + "," + model.getIndex());
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, CamWarnGlideURL model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(new SimpleTarget<Bitmap>(150, 150) {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                ((RoundedImageView) v).setImageBitmap(resource);
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                AppLogger.e(MiscUtils.getErr(e));
                            }
                        });
            }
        } else {
            //normal view
            lLayoutPreview.setVisibility(View.GONE);
        }
        updateFocus(false, currentIndex);
    }

    private void updateFocus(boolean auto, int index) {
        int count = MiscUtils.getCount(MiscUtils.getFileIndex(camMessageBean));
        if (device != null && JFGRules.isNeedPanoramicView(device.pid)) {
            //全景需要兼容,这里的tag的构造方式,看FragmentPagerAdapter,最后一个方法
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(MiscUtils.makeFragmentName(vpContainer.getId(), 0));
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
        if (basePresenter != null)
            basePresenter.checkCollection(MiscUtils.getVersion(camMessageBean), index, camMessageBean);
    }

    @Override
    protected void onStart() {
        super.onStart();
        decideWhichView();
        showCollectCase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        customToolbar.setToolbarTitle(TimeUtils.getMediaPicTimeInString(MiscUtils.getFileTime(camMessageBean) * 1000L));
    }


    @OnClick({R.id.imgV_big_pic_download,
            R.id.imgV_big_pic_share,
            R.id.imgV_big_pic_collect})
    public void onClick(View view) {
        ViewUtils.deBounceClick(view);
        switch (view.getId()) {
            case R.id.imgV_big_pic_download:
                CamMediaActivityPermissionsDispatcher.downloadFileWithCheck(this);
                break;
            case R.id.imgV_big_pic_share:
                if (NetUtils.getJfgNetType(getContext()) == 0) {
                    ToastUtil.showToast(getString(R.string.NoNetworkTips));
                    return;
                }
                MiscUtils.getCamWarnUrl(uuid, camMessageBean, currentIndex + 1).fetch(file -> {
                    ShareManager.byImg(CamMediaActivity.this)
                            .withImg(file)
                            .share();
//                    Intent intent = new Intent(this, ShareMediaActivity.class);
//                    intent.putExtra(ShareConstant.SHARE_CONTENT, ShareConstant.SHARE_CONTENT_PICTURE);
//                    intent.putExtra(ShareConstant.SHARE_CONTENT_PICTURE_EXTRA_IMAGE_PATH, file);
//                    startActivity(intent);
                });
                break;
            case R.id.imgV_big_pic_collect:
                if (NetUtils.getJfgNetType(getContext()) == 0) {
                    ToastUtil.showToast(getString(R.string.NoNetworkTips));
                    return;
                }
                Object tag = imgVBigPicCollect.getTag();
                if (tag == null || !(boolean) tag) {
                    if (basePresenter != null)
                        basePresenter.collect(currentIndex, MiscUtils.getVersion(camMessageBean), camMessageBean);
                } else {
                    if (basePresenter != null)
                        basePresenter.unCollect(currentIndex, MiscUtils.getVersion(camMessageBean), camMessageBean);
                }
                LoadingDialog.showLoading(this);
                imgVBigPicCollect.setEnabled(false);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finishExt();
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
    public void onCollectingRsp(int err) {
        imgVBigPicCollect.setEnabled(true);
        runOnUiThread(() -> LoadingDialog.dismissLoading());
        switch (err) {
            case 1050:
                AlertDialogManager.getInstance().showDialog(this, getString(R.string.DailyGreatTips_Full), getString(R.string.DailyGreatTips_Full),
                        getString(R.string.OK), (DialogInterface dialog, int which) -> {
                            Intent intent = new Intent(CamMediaActivity.this, NewHomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra(JConstant.KEY_JUMP_TO_WONDER, JConstant.KEY_JUMP_TO_WONDER);
                            startActivity(intent);
                        }, getString(R.string.CANCEL), null, false);
                break;
            case 0:
                ToastUtil.showToast(getString(R.string.Tap1_BigPic_FavoriteTips));
                imgVBigPicCollect.setTag(true);
                imgVBigPicCollect.setImageResource(R.drawable.icon_collected);
                break;
        }
    }

    @Override
    public int getCurrentIndex() {
        return currentIndex;
    }

    @Override
    public void onItemCollectionCheckRsp(boolean state) {
        imgVBigPicCollect.post(() -> {
            imgVBigPicCollect.setImageResource(state ? R.drawable.icon_collected : R.drawable.icon_collection);
            imgVBigPicCollect.setTag(state);
            LoadingDialog.dismissLoading();
        });
    }

    private class CustomAdapter extends FragmentPagerAdapter {
        private NormalMediaFragment.CallBack callBack;
        private CamMessageBean camMessageBean;

        public void setCamMessageBean(CamMessageBean camMessageBean) {
            this.camMessageBean = camMessageBean;
        }


        public CustomAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            boolean isPan = device != null && JFGRules.isNeedPanoramicView(device.pid);
            Bundle bundle = new Bundle();
            bundle.putParcelable(KEY_SHARED_ELEMENT_LIST, camMessageBean);
            bundle.putInt(KEY_INDEX, isPan ? getIntent().getIntExtra(KEY_INDEX, 0) : position);
            bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
            bundle.putInt("totalCount", MiscUtils.getCount(MiscUtils.getFileIndex(camMessageBean)));
            IBaseFragment fragment = null;
            if (isPan) {
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
            return MiscUtils.getCount(MiscUtils.getFileIndex(camMessageBean));
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //do nothing
        }

        private void setCallback(IBaseFragment.CallBack callback) {
            this.callBack = callback;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CamMediaActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onDownloadPermissionDenied() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AlertDialogManager.getInstance().showDialog(this, getString(R.string.VALID_STORAGE),
                    getString(R.string.VALID_STORAGE),
                    getString(R.string.OK), (DialogInterface dialog, int which) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, 33);
                    }, getString(R.string.CANCEL), (DialogInterface dialog, int which) -> finishExt(), false);
        }
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onDownloadPermissionNeverAskAgain() {
        onDownloadPermissionDenied();
    }


    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void downloadFile() {
        if (basePresenter != null)
            basePresenter.saveImage(MiscUtils.getCamWarnUrl(uuid, camMessageBean, currentIndex + 1));
    }

}
