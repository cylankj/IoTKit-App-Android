package com.cylan.jiafeigou.support.share;

import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.view.panorama.PanoramaAlbumContact;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.io.File;

/**
 * Created by yanzhendong on 2017/6/14.
 */

public class ShareManager {

    public static ShareByH5EditorOption byH5(FragmentActivity activity) {
        return new ShareByH5EditorOption(activity);
    }

    public static ShareByH5LinkOption byH5Link(FragmentActivity activity) {
        return new ShareByH5LinkOption(activity);
    }

    public static ShareByWebLinkOption byWeb(FragmentActivity activity) {
        return new ShareByWebLinkOption(activity);
    }

    public static ShareByPictureOption byImg(FragmentActivity activity) {
        return new ShareByPictureOption(activity);
    }


    static abstract class ShareAction implements ShareOptionMenuDialog.ShareOptionClickListener, UMShareListener, DialogInterface.OnCancelListener {
        protected FragmentActivity activity;
        protected ShareOptionMenuDialog dialog;
        protected String title;
        protected String description;
        private LifeCircle lifeCircle;

        public ShareAction(FragmentActivity activity) {
            this.activity = activity;
            lifeCircle = new LifeCircle(activity.getClass().getSimpleName());
            ((Application) ContextUtils.getContext())
                    .registerActivityLifecycleCallbacks(lifeCircle);
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

        public String getPlatformString(SHARE_MEDIA media) {
            switch (media) {
                case FACEBOOK:
                    return "Facebook";
                case TWITTER:
                    return "Twitter";
                case SINA:
                    return ContextUtils.getContext().getString(R.string.Weibo);
                case QQ:
                    return "QQ";
                case WEIXIN:
                    return ContextUtils.getContext().getString(R.string.WeChat);
                case WEIXIN_CIRCLE:
                    return ContextUtils.getContext().getString(R.string.WeChat);
                case QZONE:
                    return ContextUtils.getContext().getString(R.string.Qzone_QQ);
            }
            return media.name();
        }

        public void share() {
            if (dialog == null) {
                dialog = ShareOptionMenuDialog.newInstance(this, this);
            }
            if (activity != null) {
                dialog.show(activity.getSupportFragmentManager(), ShareOptionMenuDialog.class.getSimpleName());
            }
        }

        private void clean() {
            activity = null;
            if (lifeCircle != null) {
                lifeCircle.dismiss();
                ((Application) ContextUtils.getContext()).unregisterActivityLifecycleCallbacks(lifeCircle);
                AppLogger.d("清理");
            }
        }

        @Override
        public void onShareOptionClick(int shareItemType) {
            if (dialog != null) {
                dialog.dismiss();
            }
            dialog = null;
            if (activity != null) {
                LoadingDialog.showLoading(activity, activity.getString(R.string.LOADING));
            }
        }

        @Override
        public void onStart(SHARE_MEDIA share_media) {
            AppLogger.e("onStart,分享开始啦!,当前分享到的平台为:" + share_media);
//            if (activity != null) {
//                LoadingDialog.showLoading(activity.getSupportFragmentManager(), activity.getResources().getString(R.string.LOADING));
//            }
        }

        @Override
        public void onResult(SHARE_MEDIA share_media) {
            AppLogger.e("onResult,分享成功啦!,当前分享到的平台为:" + share_media);
            if (activity != null) {
                ToastUtil.showPositiveToast(activity.getString(R.string.Tap3_ShareDevice_SuccessTips));
            }
            clean();
        }

        @Override
        public void onError(SHARE_MEDIA share_media, Throwable throwable) {
            AppLogger.e("onError,分享失败啦!,当前分享到的平台为:" + share_media + ",错误原因为:" + throwable.getMessage());
            if (activity != null) {
                if (!UMShareAPI.get(activity).isInstall(activity, share_media)) {
                    ToastUtil.showNegativeToast(activity.getString(R.string.Tap1_Album_Share_NotInstalledTips, getPlatformString(share_media)));
                } else {
                    ToastUtil.showNegativeToast(activity.getString(R.string.Tap3_ShareDevice_FailTips));
                }
            }
            clean();
        }

        @Override
        public void onCancel(SHARE_MEDIA share_media) {
            AppLogger.e("onCancel,分享取消啦!,当前分享到的平台为:" + share_media);
            if (activity != null) {
                ToastUtil.showNegativeToast(activity.getString(R.string.Tap3_ShareDevice_CanceldeTips));
            }
            clean();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            this.activity = null;
            this.dialog = null;
            clean();
        }
    }

    public static class ShareByWebLinkOption extends ShareAction {
        protected String webLink;
        protected String webThumb;

        public ShareByWebLinkOption(FragmentActivity activity) {
            super(activity);
        }

        public ShareByWebLinkOption withTitle(String title) {
            this.title = title;
            return this;
        }

        public ShareByWebLinkOption withDescription(String description) {
            this.description = description;
            return this;
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
            if (activity != null) {
                com.umeng.socialize.ShareAction shareAction = new com.umeng.socialize.ShareAction(activity);
                UMWeb web = new UMWeb(webLink);
                if (!TextUtils.isEmpty(webThumb))
                    web.setThumb(new UMImage(activity, new File(webThumb)));
                web.setTitle(!TextUtils.isEmpty(title) ? title : activity.getString(R.string.share_default_title));
                web.setDescription(!TextUtils.isEmpty(description) ? description : activity.getString(R.string.share_default_description));
                shareAction.setPlatform(getPlatform(shareItemType));
                shareAction.withMedia(web);
                shareAction.setCallback(this);
                shareAction.share();
            }
        }
    }

    public static class ShareByH5LinkOption extends ShareAction {

        public ShareByH5LinkOption(FragmentActivity activity) {
            super(activity);
        }

    }

    public static class ShareByH5EditorOption extends ShareAction {
        protected PanoramaAlbumContact.PanoramaItem shareItem;
        protected String filePath;
        protected String thumbPath;
        protected String uuid;

        public ShareByH5EditorOption withUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

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
            AppLogger.e("H5分享模式,在指定的 fragment 里分享");
            if (dialog != null) dialog.dismiss();
            dialog = null;
            if (activity != null) {
                if (NetUtils.getNetType(activity) != ConnectivityManager.TYPE_WIFI) {
                    new AlertDialog.Builder(activity)
                            .setMessage(R.string.Tap1_Firmware_DataTips)
                            .setCancelable(false)
                            .setPositiveButton(R.string.CARRY_ON, (dialog, which) -> {
                                Intent shareIntent = H5ShareEditorActivity.getShareIntent(activity, uuid, shareItemType, filePath, thumbPath, shareItem);
                                activity.startActivity(shareIntent);
                            })
                            .setNegativeButton(R.string.CANCEL, null)
                            .show();
                } else {
                    Intent shareIntent = H5ShareEditorActivity.getShareIntent(activity, uuid, shareItemType, filePath, thumbPath, shareItem);
                    activity.startActivity(shareIntent);
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
            if (activity != null) {
                com.umeng.socialize.ShareAction shareAction = new com.umeng.socialize.ShareAction(activity);
                SHARE_MEDIA platform = getPlatform(shareItemType);
                UMImage image = new UMImage(activity, new File(img));
                shareAction.setPlatform(platform);
                shareAction.withMedia(image);
                shareAction.withExtra(image);
                shareAction.setCallback(this);
                shareAction.share();
            }
        }
    }

    private static final class LifeCircle implements Application.ActivityLifecycleCallbacks {

        private final String activityName;

        public void dismiss() {
        }

        public LifeCircle(String activityName) {
            this.activityName = activityName;
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {
            final String name = activity.getClass().getSimpleName();
            if (TextUtils.equals(activityName, name) && activity instanceof FragmentActivity) {
                LoadingDialog.dismissLoading();
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }
}
