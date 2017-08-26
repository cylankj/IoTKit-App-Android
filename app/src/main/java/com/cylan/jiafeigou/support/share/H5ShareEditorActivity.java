package com.cylan.jiafeigou.support.share;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.bumptech.glide.Glide;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.ActivityComponent;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.databinding.FragmentPanoramaShareBinding;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.view.panorama.PanoramaAlbumContact;
import com.cylan.jiafeigou.n.view.panorama.PanoramaShareContact;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.io.File;
import java.util.Random;

import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_CONTENT_H5_WITH_UPLOAD_EXTRA_FILE_PATH;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_CONTENT_H5_WITH_UPLOAD_EXTRA_SHARE_ITEM;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_CONTENT_H5_WITH_UPLOAD_EXTRA_THUMB_PATH;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE_FACEBOOK;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE_QQ;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE_QZONE;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE_TIME_LINE;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE_TWITTER;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE_WECHAT;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE_WEIBO;

/**
 * Created by yanzhendong on 2017/5/27.
 */

public class H5ShareEditorActivity extends BaseActivity<PanoramaShareContact.Presenter> implements PanoramaShareContact.View, UMShareListener {

    private FragmentPanoramaShareBinding shareBinding;
    private ObservableField<String> description = new ObservableField<>();
    private ObservableBoolean uploadSuccess = new ObservableBoolean(false);
    private int shareType;
    private PanoramaAlbumContact.PanoramaItem shareItem;
    private String filePath;
    private String thumbPath;
    private Random random = new Random();
    private ValueAnimator animator;

    public static Intent getShareIntent(Context context, String uuid, int shareType, String filePath, String thumbPath, PanoramaAlbumContact.PanoramaItem shareItem) {
        Intent intent = new Intent(context, H5ShareEditorActivity.class);
        intent.putExtra(ShareConstant.SHARE_CONTENT_H5_WITH_UPLOAD_EXTRA_FILE_PATH, filePath);
        intent.putExtra(ShareConstant.SHARE_CONTENT_H5_WITH_UPLOAD_EXTRA_THUMB_PATH, thumbPath);
        intent.putExtra(ShareConstant.SHARE_CONTENT_H5_WITH_UPLOAD_EXTRA_SHARE_ITEM, shareItem);
        intent.putExtra(ShareConstant.SHARE_PLATFORM_TYPE, shareType);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        return intent;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    protected void setActivityComponent(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    protected View getContentRootView() {
        shareBinding = FragmentPanoramaShareBinding.inflate(getLayoutInflater());
        return shareBinding.getRoot();
    }

    private void startCount() {
        if (animator != null) {
            animator.cancel();
        }
        animator = ValueAnimator.ofInt(1, 99);
        animator.setDuration(random.nextInt(4000));
        animator.addUpdateListener(animation -> {
            shareBinding.sharePercent.setText(String.format("%s%%", animation.getAnimatedValue()));
        });
        animator.start();
    }

    private void endCount() {
        if (animator != null) {
            int animatedValue = (int) animator.getAnimatedValue();
            animator.cancel();
            animator = ValueAnimator.ofInt(animatedValue, 100);
            animator.setDuration(500);
            animator.addUpdateListener(animation -> shareBinding.sharePercent.setText(String.format("%s%%", animation.getAnimatedValue())));
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    shareBinding.sharePercent.setText("100%");
                    uploadSuccess.set(true);
                    shareBinding.shareRetry.setVisibility(View.GONE);
                }
            });
            animator.start();
        } else {
            shareBinding.sharePercent.setText("100%");
            uploadSuccess.set(true);
            shareBinding.shareRetry.setVisibility(View.GONE);
        }
    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        Intent intent = getIntent();
        shareType = intent.getIntExtra(SHARE_PLATFORM_TYPE, 0);
        shareItem = intent.getParcelableExtra(SHARE_CONTENT_H5_WITH_UPLOAD_EXTRA_SHARE_ITEM);
        filePath = intent.getStringExtra(SHARE_CONTENT_H5_WITH_UPLOAD_EXTRA_FILE_PATH);
        thumbPath = intent.getStringExtra(SHARE_CONTENT_H5_WITH_UPLOAD_EXTRA_THUMB_PATH);
        shareBinding.setWay(getNameByType(shareType));
        shareBinding.setDescription(description);
        shareBinding.setUploadSuccess(uploadSuccess);
        shareBinding.setBackClick(this::cancelShare);
        shareBinding.setShareClick(this::share);
        shareBinding.shareContextEditor.requestFocus();
        shareBinding.shareRetry.setOnClickListener(v -> {
            if (presenter != null) {
                presenter.upload(shareItem.fileName, filePath);
                startCount();
            }
        });
        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(shareBinding.shareContextEditor, 0);
        }
        Glide.with(this).load(filePath).into(shareBinding.sharePreview);
        // TODO: 2017/8/12 并不能判断服务器是否有这个文件 ,废弃掉了
//        if (presenter != null) presenter.check(uuid, shareItem.time);
        if (presenter != null) {
            presenter.upload(shareItem.fileName, filePath);
            startCount();
        }
    }

    private String getNameByType(int shareType) {
        switch (shareType) {
            case SHARE_PLATFORM_TYPE_TIME_LINE://
                return getString(R.string.Tap2_Share_Moments);
            case SHARE_PLATFORM_TYPE_WECHAT:
                return getString(R.string.WeChat);
            case SHARE_PLATFORM_TYPE_QQ:
                return "QQ";
            case SHARE_PLATFORM_TYPE_QZONE:
                return getString(R.string.Qzone_QQ);
            case SHARE_PLATFORM_TYPE_WEIBO:
                return getString(R.string.Weibo);
            case SHARE_PLATFORM_TYPE_FACEBOOK:
                return "Facebook";
            case SHARE_PLATFORM_TYPE_TWITTER:
                return "Twitter";
        }
        return "";
    }

    @Override
    public void onUploadResult(int code) {
        if (code != -1) {
            if (code == 200) {
                endCount();
            } else {
                uploadSuccess.set(false);
                shareBinding.sharePercent.setVisibility(View.GONE);
                shareBinding.shareRetry.setVisibility(View.VISIBLE);
            }
        }
        AppLogger.d("上传到服务器返回的结果为:" + code);
    }

    @Override
    public void onShareH5Result(boolean success, String h5) {
        if (success && !TextUtils.isEmpty(h5)) {
            shareWithH5ByType(shareType, h5);
            AppLogger.d("得到上传服务器返回的 h5网址,将进行对应的分享");
        }
    }

    private String getDescription() {
        if (TextUtils.isEmpty(description.get())) {
            return getString(R.string.share_default_description);
        } else {
            return description.get();
        }
    }

    private void shareWithH5ByType(int shareType, String h5) {
        Intent intent = getIntent();
        String thumbPath = intent.getStringExtra(ShareConstant.SHARE_CONTENT_H5_WITH_UPLOAD_EXTRA_THUMB_PATH);
        UMWeb umWeb = new UMWeb(h5);
        umWeb.setTitle(getString(R.string.share_default_title));
        umWeb.setDescription(getDescription());
        if (!TextUtils.isEmpty(thumbPath)) {
            umWeb.setThumb(new UMImage(this, new File(thumbPath)));
        }
        ShareAction shareAction = new ShareAction(this);
        shareAction.withMedia(umWeb);
//        if (listener != null) {
        shareAction.setCallback(this);
//        }
        switch (shareType) {
            case SHARE_PLATFORM_TYPE_TIME_LINE://
                shareAction.setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE);
                break;
            case SHARE_PLATFORM_TYPE_WECHAT:
                shareAction.setPlatform(SHARE_MEDIA.WEIXIN);
                break;
            case SHARE_PLATFORM_TYPE_QQ:
                shareAction.setPlatform(SHARE_MEDIA.QQ);
                break;
            case SHARE_PLATFORM_TYPE_QZONE:
                shareAction.setPlatform(SHARE_MEDIA.QZONE);
                break;
            case SHARE_PLATFORM_TYPE_WEIBO:
                shareAction.setPlatform(SHARE_MEDIA.SINA);
                break;
            case SHARE_PLATFORM_TYPE_FACEBOOK:
                shareAction.setPlatform(SHARE_MEDIA.FACEBOOK);
                break;
            case SHARE_PLATFORM_TYPE_TWITTER:
                shareAction.setPlatform(SHARE_MEDIA.TWITTER);
                break;
        }
        shareAction.share();
    }

    @Override
    protected void onPrepareToExit(Action action) {
        cancelShare(null);

    }

    public void cancelShare(View view) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.Tap3_ShareDevice_UnshareTips)
                .setCancelable(false)
                .setPositiveButton(R.string.OK, (dialog, which) -> {
                    IMEUtils.hide(this);
                    finish();
                })
                .setNegativeButton(R.string.CANCEL, null)
                .show();

    }

    public void share(View view) {
        if (NetUtils.getNetType(this) == -1) {
            ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR_1));
        } else {
            if (!LoadingDialog.isShowLoading()) {
                LoadingDialog.showLoading(this, getString(R.string.LOADING), false, dialog -> finish());
            }
            if (presenter != null) presenter.share(shareItem, getDescription(), thumbPath);
        }
    }

    @Override
    public void onStart(SHARE_MEDIA share_media) {
        AppLogger.e("onStart,分享开始啦!,当前分享到的平台为:" + share_media);
        LoadingDialog.dismissLoading();
    }

    @Override
    public void onResult(SHARE_MEDIA share_media) {
        AppLogger.e("onResult,分享成功啦!,当前分享到的平台为:" + share_media);
        ToastUtil.showPositiveToast(getString(R.string.Tap3_ShareDevice_SuccessTips));
        finish();

    }

    @Override
    public void onError(SHARE_MEDIA share_media, Throwable throwable) {
        AppLogger.e("onError,分享失败啦!,当前分享到的平台为:" + share_media + ",错误原因为:" + throwable.getMessage());
        if (!UMShareAPI.get(this).isInstall(this, share_media)) {
            ToastUtil.showNegativeToast(getString(R.string.Tap1_Album_Share_NotInstalledTips, share_media.toString()));
        } else {
            ToastUtil.showNegativeToast(getString(R.string.Tap3_ShareDevice_FailTips));
        }
    }

    @Override
    public void onCancel(SHARE_MEDIA share_media) {
        AppLogger.e("onCancel,分享取消啦!,当前分享到的平台为:" + share_media);
//        ToastUtil.showNegativeToast(getString(R.string.Tap3_ShareDevice_CanceldeTips));
        onResult(share_media);
    }
}
