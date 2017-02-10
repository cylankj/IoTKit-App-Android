package com.cylan.jiafeigou.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.wechat.WechatShare;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import java.lang.ref.WeakReference;

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
        Glide.with(ContextUtils.getContext())
                .load(glideUrl)
                .asBitmap()
                .format(DecodeFormat.DEFAULT)
                .into(new SimpleTarget<Bitmap>(150, 150) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        shareContent.bitmap = resource;
                        shareContent.shareContent = WechatShare.WEIXIN_SHARE_CONTENT_PIC;
                        wechatShare.shareByWX(shareContent);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        ToastUtil.showNegativeToast(activity.getString(R.string.SHARE_ERROR));
                        AppLogger.e("fxxx,load image failed: ");
                    }
                });
    }

    public static void sharePictureToTwitter() {

    }

    public static void shareToFacebook(FragmentActivity activity, GlideUrl glideUrl) {
        Glide.with(ContextUtils.getContext())
                .load(glideUrl)
                .asBitmap()
                .format(DecodeFormat.DEFAULT)
                .into(new SimpleTarget<Bitmap>(150, 150) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        SharePhotoContent content=new SharePhotoContent.Builder()
                                .addPhoto(new SharePhoto.Builder().setBitmap(resource).build())
                                .build();
                        ShareDialog dialog=new ShareDialog(activity);
                        dialog.show(content, ShareDialog.Mode.AUTOMATIC);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        ToastUtil.showNegativeToast(activity.getString(R.string.SHARE_ERROR));
                    }
                });

    }

    public static void shareVideoToWechat(FragmentActivity activity, String mVideoURL, int wxSceneSession) {

    }

    public static void shareVideoToTwitter() {

    }

    public static void shareVideoToFacebook() {

    }
}
