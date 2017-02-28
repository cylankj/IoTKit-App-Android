package com.cylan.jiafeigou.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.support.wechat.WechatShare;
import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by yzd on 16-12-9.
 */

public class ShareUtils {
    private static WeakReference<WechatShare> shareWeakReference = null;

    public static void sharePictureToWechat(Activity activity, GlideUrl glideUrl, final int type) {
        if (shareWeakReference == null || shareWeakReference.get() == null)
            shareWeakReference = new WeakReference<>(new WechatShare(activity));
        //find bitmap from glide
        WechatShare wechatShare = shareWeakReference.get();
        final WechatShare.ShareContent shareContent = new WechatShare.ShareContentImpl();
        //朋友圈，微信
        shareContent.shareScene = type;
        shareContent.shareContent = WechatShare.WEIXIN_SHARE_CONTENT_PIC;
        Glide.with(ContextUtils.getContext())
                .load(glideUrl)
                .asBitmap()
                .format(DecodeFormat.DEFAULT)
                .into(new SimpleTarget<Bitmap>(150, 150) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        shareContent.bitmap = resource;
                        wechatShare.shareByWX(shareContent);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        ToastUtil.showNegativeToast(activity.getString(R.string.Tap3_ShareDevice_FailTips));
                    }
                });
    }

    public static void sharePictureToTwitter(FragmentActivity activity, GlideUrl glideUrl) {
        if (!isTwitterInstalled()) {
            ToastUtil.showNegativeToast(activity.getString(R.string.Tap0_Login_NoInstalled, "twitter"));
        }

        Glide.with(ContextUtils.getContext())
                .load(glideUrl)
                .downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                        File file = new File(JConstant.MEDIA_DETAIL_PICTURE_DOWNLOAD_DIR, "Tweet.temp");
                        FileUtils.copyFile(resource, file);
                        TweetComposer.Builder builder = new TweetComposer.Builder(activity)
                                .image(Uri.fromFile(file))
                                .text("这是我通过 Tweet 分享的图片");
                        builder.show();
                    }
                });
    }

    public static boolean isFacebookInstalled() {
        try {
            return ContextUtils.getContext()
                    .getPackageManager()
                    .getPackageInfo("com.facebook.katana", PackageManager.GET_SIGNATURES) != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isTwitterInstalled() {
        try {
            return ContextUtils.getContext()
                    .getPackageManager()
                    .getPackageInfo("com.twitter.android", PackageManager.GET_SIGNATURES) != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isWechatInstalled() {
        try {
            return ContextUtils.getContext()
                    .getPackageManager()
                    .getPackageInfo("com.tencent.mm", PackageManager.GET_SIGNATURES) != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void shareToFacebook(Activity activity, GlideUrl glideUrl) {
        if (!FacebookSdk.isInitialized()) {
            FacebookSdk.sdkInitialize(activity.getApplicationContext());
        }
        if (!isFacebookInstalled()) {
            ToastUtil.showNegativeToast(activity.getString(R.string.Tap0_Login_NoInstalled, "facebook"));
            return;
        }

        Glide.with(ContextUtils.getContext())
                .load(glideUrl)
                .asBitmap()
                .format(DecodeFormat.DEFAULT)
                .into(new SimpleTarget<Bitmap>(150, 150) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        SharePhotoContent content = new SharePhotoContent.Builder()
                                .addPhoto(new SharePhoto.Builder().setBitmap(resource).build())
                                .build();
                        ShareDialog dialog = new ShareDialog(activity);
                        dialog.show(content, ShareDialog.Mode.AUTOMATIC);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        ToastUtil.showNegativeToast(activity.getString(R.string.Tap3_ShareDevice_FailTips));
                    }
                });

    }

    public static void shareVideoToWechat(Activity activity, String VideoURL, int WXSceneSession, GlideUrl videoThumbURL) {
        if (shareWeakReference == null || shareWeakReference.get() == null)
            shareWeakReference = new WeakReference<>(new WechatShare(activity));
        //find bitmap from glide
        WechatShare wechatShare = shareWeakReference.get();
        final WechatShare.ShareContent shareContent = new WechatShare.ShareContentImpl();
        //朋友圈，微信
        shareContent.shareScene = WXSceneSession;
        shareContent.shareContent = WechatShare.WEIXIN_SHARE_CONTENT_VIDEO;
        shareContent.url = VideoURL;
        shareContent.title = "This Is My Video To Share";
        Glide.with(activity)
                .load(videoThumbURL)
                .asBitmap()
                .format(DecodeFormat.DEFAULT)
                .into(new SimpleTarget<Bitmap>(150, 150) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        shareContent.bitmap = resource;
                        wechatShare.shareByWX(shareContent);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        wechatShare.shareByWX(shareContent);
                    }
                });


    }

    public static void shareVideoToTwitter(Activity activity, String videoURL, GlideUrl videoThumbURL) {
        if (!isTwitterInstalled()) {
            ToastUtil.showNegativeToast(activity.getString(R.string.Tap0_Login_NoInstalled, "twitter"));
        }

        Glide.with(ContextUtils.getContext())
                .load(videoThumbURL)
                .downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                        File file = new File(JConstant.MEDIA_DETAIL_PICTURE_DOWNLOAD_DIR, "Tweet.temp");
                        FileUtils.copyFile(resource, file);
                        URL url = null;
                        try {
                            url = new URL(videoURL);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        TweetComposer.Builder builder = new TweetComposer.Builder(activity)
                                .image(Uri.fromFile(file))
                                .url(url)
                                .text("这是我通过 Tweet 分享的图片");
                        builder.show();
                    }
                });
    }

    public static void shareVideoToFacebook(Activity activity, String videoURL, GlideUrl videoThumbURL) {
        if (!FacebookSdk.isInitialized()) {
            FacebookSdk.sdkInitialize(activity.getApplicationContext());
        }
        if (!isFacebookInstalled()) {
            ToastUtil.showNegativeToast(activity.getString(R.string.Tap0_Login_NoInstalled, "facebook"));
            return;
        }

        ShareLinkContent content = new ShareLinkContent.Builder()
                .setImageUrl(Uri.parse(videoThumbURL.toStringUrl()))
                .setContentUrl(Uri.parse(videoURL))
                .setContentTitle("这是我分享的视频")
                .build();
        ShareDialog dialog = new ShareDialog(activity);
        if (dialog.canShow(content)) {
            dialog.show(content, ShareDialog.Mode.AUTOMATIC);
        } else {
            ToastUtil.showNegativeToast(activity.getString(R.string.Tap3_ShareDevice_FailTips));
        }
    }
}
