package com.cylan.jiafeigou.support.wechat;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.utils.PackageUtils;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.lang.ref.SoftReference;

/**
 * Created by cylan-hunt on 16-10-26.
 */

public class WechatShare {

    private IWXAPI api;
    private static SoftReference<WechatShare> wechatShareSoftReference;

    public static WechatShare getInstance() {
        if (wechatShareSoftReference == null || wechatShareSoftReference.get() == null)
            wechatShareSoftReference = new SoftReference<>(new WechatShare());
        return wechatShareSoftReference.get();
    }

    public WechatShare() {
        Context context = ContextUtils.getContext();
        String appId = PackageUtils.getMetaString(ContextUtils.getContext(), "weChatAppKey");
        if (TextUtils.isEmpty(appId)) {
            AppLogger.i("wechat app id is null");
        }
        api = WXAPIFactory.createWXAPI(context, appId, true);// 第三个参数作用？
        api.registerApp(appId);
    }

    /**
     * @param text
     * @param isTimeline： true：朋友圈，false:朋友
     */
    public void shareTextObject(String text, boolean isTimeline) {
        WXTextObject wxImageObject = new WXTextObject(text);
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = wxImageObject;
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("text");// 用于唯一标识一个请求
        req.message = msg;
        req.scene = isTimeline ? SendMessageToWX.Req.WXSceneTimeline :
                SendMessageToWX.Req.WXSceneSession;
        if (api != null) {
            api.sendReq(req);
        }
    }

    /**
     * @param bitmap
     * @param isTimeline： true：朋友圈，false:朋友
     */
    public void shareImageObject(Bitmap bitmap, boolean isTimeline) {
        WXImageObject wxImageObject = new WXImageObject(bitmap);
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = wxImageObject;
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("text");// 用于唯一标识一个请求
        req.message = msg;
        req.scene = isTimeline ? SendMessageToWX.Req.WXSceneTimeline
                : SendMessageToWX.Req.WXSceneSession;
        if (api != null) {
            api.sendReq(req);
        }
    }

    /**
     * @param type appdata,emoji,music,webpage,video,img
     * @return
     */
    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis())
                : type + System.currentTimeMillis();
    }
}
