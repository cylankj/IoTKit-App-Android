package com.cylan.jiafeigou.support.share;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.ActivityComponent;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.databinding.ActivityShareBinding;
import com.cylan.jiafeigou.databinding.DialogShareBinding;
import com.cylan.jiafeigou.n.view.panorama.PanoramaShareFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.io.File;

import static com.cylan.jiafeigou.support.share.ShareContanst.SHARE_CONTENT_H5_WITH_UPLOAD;
import static com.cylan.jiafeigou.support.share.ShareContanst.SHARE_CONTENT_PICTURE;
import static com.cylan.jiafeigou.support.share.ShareContanst.SHARE_CONTENT_WEB_URL;
import static com.cylan.jiafeigou.support.share.ShareContanst.SHARE_STYLE;
import static com.cylan.jiafeigou.support.share.ShareContanst.SHARE_TYPE;
import static com.cylan.jiafeigou.support.share.ShareContanst.SHARE_TYPE_FACEBOOK;
import static com.cylan.jiafeigou.support.share.ShareContanst.SHARE_TYPE_QQ;
import static com.cylan.jiafeigou.support.share.ShareContanst.SHARE_TYPE_QZONE;
import static com.cylan.jiafeigou.support.share.ShareContanst.SHARE_TYPE_TIME_LINE;
import static com.cylan.jiafeigou.support.share.ShareContanst.SHARE_TYPE_TWITTER;
import static com.cylan.jiafeigou.support.share.ShareContanst.SHARE_TYPE_WECHAT;
import static com.cylan.jiafeigou.support.share.ShareContanst.SHARE_TYPE_WEIBO;

/**
 * Created by yanzhendong on 2017/6/1.
 */

public class ShareActivity extends BaseActivity<ShareContact.Presenter> implements ShareContact.View {
    private int shareStyle = SHARE_CONTENT_PICTURE;
    private ActivityShareBinding shareBinding;
    private AlertDialog shareDialog;

    @Override
    protected void setActivityComponent(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    protected View getContentRootView() {
        shareBinding = ActivityShareBinding.inflate(getLayoutInflater());
        return shareBinding.getRoot();
    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        Intent intent = getIntent();
        shareStyle = intent.getIntExtra(SHARE_STYLE, SHARE_CONTENT_PICTURE);
        setTitle(null);
        showShareMenuAlert();
    }

    private void showShareMenuAlert() {
        if (!NetUtils.isNetworkAvailable(this)) {
            ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR_1));
            finish();
            return;
        }
        if (shareDialog == null) {
            DialogShareBinding binding = DialogShareBinding.inflate(getLayoutInflater());
            binding.setShareListener(this::onShareItemClick);
            shareDialog = new AlertDialog.Builder(this)
                    .setView(binding.getRoot())
                    .setCancelable(false)
                    .setOnKeyListener((dialog, keyCode, event) -> {
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                            onBackPressed();
                        }
                        return false;
                    })
                    .create();
        }
        if (!shareDialog.isShowing()) {

            shareDialog.show();
        }
    }

    private UMShareListener listener = new UMShareListener() {
        @Override
        public void onStart(SHARE_MEDIA share_media) {
            AppLogger.e("onStart,分享开始啦!,当前分享到的平台为:" + share_media);
        }

        @Override
        public void onResult(SHARE_MEDIA share_media) {
            AppLogger.e("onResult,分享成功啦!,当前分享到的平台为:" + share_media);
        }

        @Override
        public void onError(SHARE_MEDIA share_media, Throwable throwable) {
            AppLogger.e("onError,分享失败啦!,当前分享到的平台为:" + share_media + ",错误原因为:" + throwable.getMessage());
        }

        @Override
        public void onCancel(SHARE_MEDIA share_media) {
            AppLogger.e("onCancel,分享取消啦!,当前分享到的平台为:" + share_media);
        }
    };

    private void onShareItemClick(View view) {
        AppLogger.e("点击了分享条目");

        switch (shareStyle) {
            case SHARE_CONTENT_PICTURE:
                AppLogger.e("图片分享,直接分享");
                if (shareDialog != null) {
                    shareDialog.dismiss();
                }
                String imageUrl = ShareParser.getInstance().parserImageUrl(getIntent().getExtras());
                Glide.with(this).load(imageUrl)
                        .downloadOnly(new SimpleTarget<File>() {
                            @Override
                            public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                                ShareAction shareAction;
                                UMImage image = new UMImage(ShareActivity.this, imageUrl);
                                shareAction = new ShareAction(ShareActivity.this);
                                shareAction.setPlatform(getPlatform(Integer.valueOf((String) view.getTag())));
                                shareAction.withMedia(image);
                                shareAction.withText("SSSSSSS");
                                shareAction.withExtra(image);
                                shareAction.setCallback(listener);
                                shareAction.share();
                            }
                        });

                break;
            case SHARE_CONTENT_WEB_URL:
                AppLogger.e("网址分享,不带上传");
                String shareLinkUrl = ShareParser.getInstance().parserWebLinkUrl(getIntent().getExtras());
                ShareAction shareAction;
                UMWeb web = new UMWeb(shareLinkUrl);
                shareAction = new ShareAction(this);
                shareAction.setPlatform(getPlatform(Integer.valueOf((String) view.getTag())));
                shareAction.withMedia(web);
                shareAction.setCallback(listener);
                shareAction.share();
                break;
            case SHARE_CONTENT_H5_WITH_UPLOAD:
                AppLogger.e("H5分享模式,在指定的 fragment 里分享");
                if (shareDialog != null && shareDialog.isShowing()) {
                    shareDialog.dismiss();
                }
                Bundle bundle = getIntent().getExtras();
                bundle.putInt(SHARE_TYPE, Integer.valueOf((String) view.getTag()));
                PanoramaShareFragment fragment = new PanoramaShareFragment();
                fragment.setArguments(bundle);
                ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(), fragment, android.R.id.content);
                break;
        }
    }

    private SHARE_MEDIA getPlatform(int shareType) {
        switch (shareType) {
            case SHARE_TYPE_TIME_LINE://
                return SHARE_MEDIA.WEIXIN_CIRCLE;
            case SHARE_TYPE_WECHAT:
                return SHARE_MEDIA.WEIXIN;
            case SHARE_TYPE_QQ:
                return SHARE_MEDIA.QQ;
            case SHARE_TYPE_QZONE:
                return SHARE_MEDIA.QZONE;
            case SHARE_TYPE_WEIBO:
                return SHARE_MEDIA.SINA;
            case SHARE_TYPE_FACEBOOK:
                return SHARE_MEDIA.FACEBOOK;
            case SHARE_TYPE_TWITTER:
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
    public void onBackPressed() {
        if (shareDialog != null && shareDialog.isShowing()) {
            shareDialog.dismiss();
        }
        finish();
    }
}
