package com.cylan.jiafeigou.support.share;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.ActivityComponent;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
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

import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_CONTENT;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_CONTENT_H5_WITH_UPLOAD;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_CONTENT_NONE;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_CONTENT_PICTURE;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_CONTENT_WEB_URL;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE_FACEBOOK;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE_QQ;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE_QZONE;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE_TIME_LINE;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE_TWITTER;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE_WECHAT;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE_WEIBO;

/**
 * Created by yanzhendong on 2017/6/1.
 */

public class ShareMediaActivity extends BaseActivity<ShareMediaContact.Presenter> implements ShareMediaContact.View, ShareOptionMenuDialog.ShareOptionClickListener, UMShareListener {
    private int shareStyle = SHARE_CONTENT_PICTURE;
    private ShareOptionMenuDialog shareOptionMenuDialog;
    private boolean shareStarted = false;

    @Override
    protected void setActivityComponent(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        Intent intent = getIntent();
        shareStyle = intent.getIntExtra(SHARE_CONTENT, SHARE_CONTENT_NONE);
        setTitle(null);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        AppLogger.e("onEnterAnimationComplete");
        if (shareStarted) {
            ToastUtil.showNegativeToast(getString(R.string.Tap3_ShareDevice_CanceldeTips));
            finish();
        } else {
            showShareOptionMenu();
        }
    }


    private void showShareOptionMenu() {
        if (!NetUtils.isNetworkAvailable(this)) {
            ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR_1));
            finish();
            return;
        }
        if (shareOptionMenuDialog == null) {
            shareOptionMenuDialog = ShareOptionMenuDialog.newInstance(cancelListener);
        }
        if (!shareOptionMenuDialog.isAdded()) {
            shareOptionMenuDialog.show(getSupportFragmentManager(), ShareOptionMenuDialog.class.getSimpleName());
        }
    }

    private DialogInterface.OnCancelListener cancelListener = dialog -> {
        finish();
        overridePendingTransition(0, 0);
    };

    @Override
    protected void onPause() {
        super.onPause();
        if (LoadingDialog.isShowing(getSupportFragmentManager())) {
            LoadingDialog.dismissLoading(getSupportFragmentManager());
        }
    }

    private SHARE_MEDIA getPlatform(int shareType) {
        switch (shareType) {
            case SHARE_PLATFORM_TYPE_TIME_LINE://
                return SHARE_MEDIA.WEIXIN_CIRCLE;
            case SHARE_PLATFORM_TYPE_WECHAT:
                return SHARE_MEDIA.WEIXIN;
            case SHARE_PLATFORM_TYPE_QQ:
                return SHARE_MEDIA.QQ;
            case SHARE_PLATFORM_TYPE_QZONE:
                return SHARE_MEDIA.QZONE;
            case SHARE_PLATFORM_TYPE_WEIBO:
                return SHARE_MEDIA.SINA;
            case SHARE_PLATFORM_TYPE_FACEBOOK:
                return SHARE_MEDIA.FACEBOOK;
            case SHARE_PLATFORM_TYPE_TWITTER:
                return SHARE_MEDIA.TWITTER;
        }
        return SHARE_MEDIA.GENERIC;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onShareOptionClick(int shareItemType) {
        AppLogger.e("点击了分享条目");
        if (shareOptionMenuDialog != null) {
            shareOptionMenuDialog.dismiss();
        }
        ShareAction shareAction;
        switch (shareStyle) {
            case SHARE_CONTENT_PICTURE:
                if (!LoadingDialog.isShowing(getSupportFragmentManager())) {
                    LoadingDialog.showLoading(getSupportFragmentManager(), getString(R.string.LOADING), false, cancelListener);
                }
                String imagePath = getIntent().getStringExtra(ShareConstant.SHARE_CONTENT_PICTURE_EXTRA_IMAGE_PATH);
                AppLogger.e("图片分享,直接分享");
                SHARE_MEDIA platform = getPlatform(shareItemType);
                UMImage image = new UMImage(ShareMediaActivity.this, new File(imagePath));
                shareAction = new ShareAction(ShareMediaActivity.this);
                shareAction.setPlatform(platform);
                shareAction.withMedia(image);
                shareAction.withExtra(image);
                shareAction.setCallback(this);
                shareAction.share();
                break;
            case SHARE_CONTENT_WEB_URL:
                AppLogger.e("网址分享,不带上传");
                if (!LoadingDialog.isShowing(getSupportFragmentManager())) {
                    LoadingDialog.showLoading(getSupportFragmentManager(), getString(R.string.LOADING), false, cancelListener);
                }
                String shareLinkUrl = getIntent().getStringExtra(ShareConstant.SHARE_CONTENT_WEB_URL_EXTRA_LINK_URL);
                String shareThumb = getIntent().getStringExtra(ShareConstant.SHARE_CONTENT_WEB_URL_EXTRA_THUMB_PATH);
                UMWeb web = new UMWeb(shareLinkUrl);
                web.setThumb(new UMImage(this, new File(shareThumb)));
                web.setTitle("来自加菲狗的分享");
                web.setDescription("来自加菲狗的网页分享");
                shareAction = new ShareAction(this);
                shareAction.setPlatform(getPlatform(shareItemType));
                shareAction.withMedia(web);
                shareAction.setCallback(this);
                shareAction.share();
                break;
            case SHARE_CONTENT_H5_WITH_UPLOAD:
                AppLogger.e("H5分享模式,在指定的 fragment 里分享");
                Bundle bundle = getIntent().getExtras();
                bundle.putInt(SHARE_PLATFORM_TYPE, shareItemType);
                H5ShareEditorFragment fragment = new H5ShareEditorFragment();
                fragment.setArguments(bundle);
                ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(), fragment, android.R.id.content, true);
                break;
        }
    }

    @Override
    public void onStart(SHARE_MEDIA share_media) {
        AppLogger.e("onStart,分享开始啦!,当前分享到的平台为:" + share_media);
        shareStarted = true;
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
        ToastUtil.showNegativeToast(getString(R.string.Tap3_ShareDevice_FailTips));
        finish();
    }

    @Override
    public void onCancel(SHARE_MEDIA share_media) {
        AppLogger.e("onCancel,分享取消啦!,当前分享到的平台为:" + share_media);
        ToastUtil.showNegativeToast(getString(R.string.Tap3_ShareDevice_CanceldeTips));
        finish();
    }
}
