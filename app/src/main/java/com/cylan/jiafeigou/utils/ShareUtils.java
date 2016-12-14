package com.cylan.jiafeigou.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.n.mvp.model.MediaBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.wechat.WechatShare;

import java.lang.ref.WeakReference;

/**
 * Created by yzd on 16-12-9.
 */

public class ShareUtils {
    private static WeakReference<WechatShare> shareWeakReference = null;

    public static void shareToWechat(Activity activity, MediaBean mediaBean, final int type) {
        if (mediaBean == null) {
            AppLogger.i("mediaBean is null");
            return;
        }
        if (shareWeakReference == null || shareWeakReference.get() == null)
            shareWeakReference = new WeakReference<>(new WechatShare(activity));
        //find bitmap from glide
        WechatShare wechatShare = shareWeakReference.get();
        final WechatShare.ShareContent shareContent = new WechatShare.ShareContentImpl();
        //朋友圈，微信
        shareContent.shareType = type;
        Glide.with(ContextUtils.getContext())
                .load(new WonderGlideURL(mediaBean))
                .asBitmap()
                .format(DecodeFormat.DEFAULT)
                .into(new SimpleTarget<Bitmap>(150, 150) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        shareContent.bitmap = Bitmap.createBitmap(resource);
                        shareContent.shareWay = WechatShare.WEIXIN_SHARE_WAY_PIC;
                        wechatShare.shareByWX(shareContent);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        AppLogger.e("fxxx,load image failed: ");
                    }
                });
    }
}
