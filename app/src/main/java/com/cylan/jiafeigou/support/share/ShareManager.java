package com.cylan.jiafeigou.support.share;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.view.panorama.PanoramaAlbumContact;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by yanzhendong on 2017/6/14.
 */

public class ShareManager {

    public static ShareByH5EditorOption byH5(FragmentActivity activity) {
        return new ShareByH5EditorOption(activity);
    }

    public static ShareByWebLinkOption byWeb(FragmentActivity activity) {
        return new ShareByWebLinkOption(activity);
    }

    public static ShareByPictureOption byImg(FragmentActivity activity) {
        return new ShareByPictureOption(activity);
    }

    static abstract class ShareAction implements ShareOptionMenuDialog.ShareOptionClickListener, UMShareListener {
        protected WeakReference<FragmentActivity> activity;
        protected WeakReference<ShareOptionMenuDialog> dialog;

        public ShareAction(FragmentActivity activity) {
            this.activity = new WeakReference<>(activity);
        }

        protected SHARE_MEDIA getPlatform(int shareType) {
            switch (shareType) {
                case ShareConstant.SHARE_PLATFORM_TYPE_TIME_LINE://
                    return SHARE_MEDIA.WEIXIN_CIRCLE;
                case ShareConstant.SHARE_PLATFORM_TYPE_WECHAT:
                    return SHARE_MEDIA.WEIXIN;
                case ShareConstant.SHARE_PLATFORM_TYPE_QQ:
                    return SHARE_MEDIA.QQ;
                case ShareConstant.SHARE_PLATFORM_TYPE_QZONE:
                    return SHARE_MEDIA.QZONE;
                case ShareConstant.SHARE_PLATFORM_TYPE_WEIBO:
                    return SHARE_MEDIA.SINA;
                case ShareConstant.SHARE_PLATFORM_TYPE_FACEBOOK:
                    return SHARE_MEDIA.FACEBOOK;
                case ShareConstant.SHARE_PLATFORM_TYPE_TWITTER:
                    return SHARE_MEDIA.TWITTER;
            }
            return SHARE_MEDIA.GENERIC;
        }

        public void share() {
            if (dialog == null || dialog.get() == null) {
                dialog = new WeakReference<>(ShareOptionMenuDialog.newInstance(this));
            }
            if (activity != null && activity.get() != null) {
                dialog.get().show(activity.get().getSupportFragmentManager(), ShareOptionMenuDialog.class.getSimpleName());
            }
        }

        @Override
        public void onShareOptionClick(int shareItemType) {
            if (dialog != null || dialog.get() != null) {
                dialog.get().dismiss();
            }
            dialog = null;
            if (activity != null && activity.get() != null) {
                LoadingDialog.showLoading(activity.get().getSupportFragmentManager(), activity.get().getString(R.string.LOADING));
            }
        }

        @Override
        public void onStart(SHARE_MEDIA share_media) {
            AppLogger.e("onStart,分享开始啦!,当前分享到的平台为:" + share_media);
            if (activity != null && activity.get() != null) {
                LoadingDialog.dismissLoading(activity.get().getSupportFragmentManager());
            }
        }

        @Override
        public void onResult(SHARE_MEDIA share_media) {
            AppLogger.e("onResult,分享成功啦!,当前分享到的平台为:" + share_media);
            if (activity != null && activity.get() != null) {
                ToastUtil.showPositiveToast(activity.get().getString(R.string.Tap3_ShareDevice_SuccessTips));
            }
            activity = null;
        }

        @Override
        public void onError(SHARE_MEDIA share_media, Throwable throwable) {
            AppLogger.e("onError,分享失败啦!,当前分享到的平台为:" + share_media + ",错误原因为:" + throwable.getMessage());
            if (activity != null && activity.get() != null) {
                ToastUtil.showNegativeToast(activity.get().getString(R.string.Tap3_ShareDevice_FailTips));
            }
            activity = null;
        }

        @Override
        public void onCancel(SHARE_MEDIA share_media) {
            AppLogger.e("onCancel,分享取消啦!,当前分享到的平台为:" + share_media);
            if (activity != null && activity.get() != null) {
                ToastUtil.showNegativeToast(activity.get().getString(R.string.Tap3_ShareDevice_CanceldeTips));
            }
            activity = null;
        }
    }

    public static class ShareByWebLinkOption extends ShareAction {
        protected String webLink;
        protected String webThumb;

        public ShareByWebLinkOption(FragmentActivity activity) {
            super(activity);
        }

        public ShareByWebLinkOption withUrl(String url) {
            this.webLink = url;
            return this;
        }

        public ShareByWebLinkOption withThumb(String thumb) {
            this.webThumb = thumb;
            return this;
        }


        @Override
        public void onShareOptionClick(int shareItemType) {
            super.onShareOptionClick(shareItemType);
            AppLogger.e("网址分享,不带上传");
            if (activity != null && activity.get() != null) {
                com.umeng.socialize.ShareAction shareAction = new com.umeng.socialize.ShareAction(activity.get());
                UMWeb web = new UMWeb(webLink);
                web.setThumb(new UMImage(activity.get(), new File(webThumb)));
                web.setTitle(activity.get().getString(R.string.share_default_title));
                web.setDescription(activity.get().getString(R.string.share_default_description));
                shareAction.setPlatform(getPlatform(shareItemType));
                shareAction.withMedia(web);
                shareAction.setCallback(this);
                shareAction.share();
            }
        }
    }

    public static class ShareByH5EditorOption extends ShareAction {
        protected PanoramaAlbumContact.PanoramaItem shareItem;
        protected String filePath;
        protected String thumbPath;

        public ShareByH5EditorOption withItem(PanoramaAlbumContact.PanoramaItem shareItem) {
            this.shareItem = shareItem;
            return this;
        }

        public ShareByH5EditorOption withFile(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public ShareByH5EditorOption withThumb(String thumbPath) {
            this.thumbPath = thumbPath;
            return this;
        }

        public ShareByH5EditorOption(FragmentActivity activity) {
            super(activity);
        }

        @Override
        public void onShareOptionClick(int shareItemType) {
            super.onShareOptionClick(shareItemType);
            AppLogger.e("H5分享模式,在指定的 fragment 里分享");
            if (activity != null && activity.get() != null) {
                if (NetUtils.getNetType(activity.get()) != ConnectivityManager.TYPE_WIFI) {
                    new AlertDialog.Builder(activity.get())
                            .setMessage(R.string.Tap1_Firmware_DataTips)
                            .setCancelable(false)
                            .setPositiveButton(R.string.CARRY_ON, (dialog, which) -> {
                                H5ShareEditorFragment fragment = H5ShareEditorFragment.newInstance(shareItemType, filePath, thumbPath, shareItem, ShareByH5EditorOption.this);
                                ActivityUtils.addFragmentSlideInFromRight(activity.get().getSupportFragmentManager(), fragment, android.R.id.content, true);
                            })
                            .setNegativeButton(R.string.CANCEL, null)
                            .show();
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putInt(ShareConstant.SHARE_PLATFORM_TYPE, shareItemType);
                    H5ShareEditorFragment fragment = new H5ShareEditorFragment();
                    fragment.setArguments(bundle);
                    ActivityUtils.addFragmentSlideInFromRight(activity.get().getSupportFragmentManager(), fragment, android.R.id.content, true);
                }
            }
        }

    }

    public static class ShareByPictureOption extends ShareAction {
        protected String img;

        public ShareByPictureOption(FragmentActivity activity) {
            super(activity);
        }

        public ShareByPictureOption withImg(String img) {
            this.img = img;
            return this;
        }

        @Override
        public void onShareOptionClick(int shareItemType) {
            super.onShareOptionClick(shareItemType);
            AppLogger.e("图片分享,直接分享");
            if (activity != null && activity.get() == null) {
                com.umeng.socialize.ShareAction shareAction = new com.umeng.socialize.ShareAction(activity.get());
                SHARE_MEDIA platform = getPlatform(shareItemType);
                UMImage image = new UMImage(activity.get(), new File(img));
                shareAction.setPlatform(platform);
                shareAction.withMedia(image);
                shareAction.withExtra(image);
                shareAction.setCallback(this);
                shareAction.share();
            }
        }
    }
}
