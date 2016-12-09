package com.cylan.jiafeigou.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.n.mvp.model.MediaBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.wechat.WechatShare;
import com.cylan.utils.RandomUtils;

/**
 * Created by yzd on 16-12-9.
 */

public class ShareUtils {

    public static void shareToWechat(Activity activity, MediaBean mediaBean, final int type) {
        if (mediaBean == null) {
            AppLogger.i("mediaBean is null");
            return;
        }
        WechatShare wechatShare = new WechatShare(activity);
        //find bitmap from glide
        final WechatShare.ShareContent shareContent = new WechatShare.ShareContentImpl();
        //朋友圈，微信
        shareContent.shareType = type;
        final int mimeType = RandomUtils.getRandom(2);//0:picture,1:url
//        if (mimeType == 0) {
        Glide.with(ContextUtils.getContext())
                .load(mediaBean.srcUrl)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(150, 150) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        shareContent.bitmap = resource;
                        shareContent.shareWay = WechatShare.WEIXIN_SHARE_WAY_PIC;
                        wechatShare.shareByWX(shareContent);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
//                        AppLogger.e("fxxx,load image failed: " + e.getLocalizedMessage());
                    }
                });
//        } else {
//            wechatShare.shareByWeixin(shareContent);
//        }
    }
}
