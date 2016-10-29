package com.cylan.jiafeigou.support.wechat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.utils.PackageUtils;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.lang.ref.WeakReference;

/**
 * Created by cylan-hunt on 16-10-26.
 */

public class WechatShare {

    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;

    private IWXAPI wxApi;


    /**
     * weChatAppId：定义在 app-build.gradle的文件中。
     * https://open.weixin.qq.com/cgi-bin/appdetail?t=manage/detail&type=app&lang=zh_CN&token=03c10c403cfa77539864a99daa459771ceb6ae52&appid=wx3081bcdae8a842cf
     *
     * @param activity
     */
    public WechatShare(Activity activity) {
        WeakReference<Activity> weakReference = new WeakReference<>(activity);
        String appId = PackageUtils.getMetaString(ContextUtils.getContext(), "weChatAppId");
        if (TextUtils.isEmpty(appId)) {
            AppLogger.i("wechat app id is null");
        }
        Log.d("WechatShare", "WechatShare: " + appId);
        // 第三个参数作用:checkSignature
        //这个context 必须是Activity，或者Service
        wxApi = WXAPIFactory.createWXAPI(weakReference.get(), appId, true);
        wxApi.registerApp(appId);
    }

    public void unregister() {
        wxApi.unregisterApp();
        //必须要调用detach,否则内存泄露。
        wxApi.detach();
        Log.d("WechatShare", "unregister: ");
    }

    public IWXAPI getWxApi() {
        return wxApi;
    }

    private static final int THUMB_SIZE = 150;
    /**
     * 文字
     */
    public static final int WEIXIN_SHARE_WAY_TEXT = 1;
    /**
     * 图片
     */
    public static final int WEIXIN_SHARE_WAY_PIC = 2;
    /**
     * 链接
     */
    public static final int WEIXIN_SHARE_WAY_WEBPAGE = 3;
    /**
     * 会话
     */
    public static final int WEIXIN_SHARE_TYPE_TALK = SendMessageToWX.Req.WXSceneSession;
    /**
     * 朋友圈
     */
    public static final int WEIXIN_SHARE_TYPE_FRENDS = SendMessageToWX.Req.WXSceneTimeline;


    /**
     * 通过微信分享
     *
     * @param shareContent 分享的方式（文本、图片、链接）
     *                     分享的类型（朋友圈，会话）
     */
    public void shareByWeixin(ShareContent shareContent) {
        switch (shareContent.getShareWay()) {
            case WEIXIN_SHARE_WAY_TEXT:
                shareText(shareContent.getShareType(), shareContent);
                break;
            case WEIXIN_SHARE_WAY_PIC:
                sharePicture(shareContent.getShareType(), shareContent);
                break;
            case WEIXIN_SHARE_WAY_WEBPAGE:
                shareWebPage(shareContent.getShareType(), shareContent);
                break;
        }
    }

    public static abstract class ShareContent {

        protected abstract int getShareWay();

        /**
         * 分享内容描述
         *
         * @return
         */
        protected abstract String getContent();

        /**
         * 标题
         *
         * @return
         */
        protected abstract String getTitle();

        protected abstract String getURL();

        protected abstract int getPicResource();

        /**
         * 分享方式： {@link com.tencent.mm.sdk.modelmsg.SendMessageToWX.Req#WXSceneSession}
         * 分享方式： {@link com.tencent.mm.sdk.modelmsg.SendMessageToWX.Req#WXSceneTimeline}
         * 分享方式： {@link com.tencent.mm.sdk.modelmsg.SendMessageToWX.Req#WXSceneFavorite}
         *
         * @return
         */
        protected abstract int getShareType();

    }

    public static class ShareContentImpl extends ShareContent {
        @Override
        public int getShareWay() {
            return 0;
        }

        @Override
        public String getContent() {
            return null;
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getURL() {
            return null;
        }

        @Override
        public int getPicResource() {
            return 0;
        }

        @Override
        protected int getShareType() {
            return 0;
        }
    }

    /**
     * 设置分享文字的内容
     *
     * @author Administrator
     */
    public class ShareContentText extends ShareContent {

        private String content;

        /**
         * 构造分享文字类
         *
         * @param content 分享的文字内容
         */
        public ShareContentText(String content) {
            this.content = content;
        }

        @Override
        protected String getContent() {

            return content;
        }

        @Override
        protected String getTitle() {
            return null;
        }

        @Override
        protected String getURL() {
            return null;
        }

        @Override
        protected int getPicResource() {
            return -1;
        }

        @Override
        protected int getShareType() {
            return 0;
        }

        @Override
        protected int getShareWay() {
            return WEIXIN_SHARE_WAY_TEXT;
        }

    }

    /**
     * 设置分享图片的内容
     *
     * @author Administrator
     */
    public class ShareContentPic extends ShareContent {
        private int picResource;

        public ShareContentPic(int picResource) {
            this.picResource = picResource;
        }

        @Override
        protected String getContent() {
            return null;
        }

        @Override
        protected String getTitle() {
            return null;
        }

        @Override
        protected String getURL() {
            return null;
        }

        @Override
        protected int getPicResource() {
            return picResource;
        }

        @Override
        protected int getShareType() {
            return 0;
        }

        @Override
        protected int getShareWay() {
            return WEIXIN_SHARE_WAY_PIC;
        }
    }

    /**
     * 设置分享链接的内容
     *
     * @author Administrator
     */
    public class ShareContentWebpage extends ShareContent {
        private String title;
        private String content;
        private String url;
        private int picResource;

        public ShareContentWebpage(String title, String content,
                                   String url, int picResource) {
            this.title = title;
            this.content = content;
            this.url = url;
            this.picResource = picResource;
        }

        @Override
        protected String getContent() {
            return content;
        }

        @Override
        protected String getTitle() {
            return title;
        }

        @Override
        protected String getURL() {
            return url;
        }

        @Override
        protected int getPicResource() {
            return picResource;
        }

        @Override
        protected int getShareType() {
            return 0;
        }

        @Override
        protected int getShareWay() {
            return WEIXIN_SHARE_WAY_WEBPAGE;
        }

    }

    /*
     * 分享文字
     */
    private void shareText(int shareType, ShareContent shareContent) {
        String text = shareContent.getContent();
        //初始化一个WXTextObject对象
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;
        //用WXTextObject对象初始化一个WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        msg.description = text;
        //构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        //transaction字段用于唯一标识一个请求
        req.transaction = buildTransaction("text");
        req.message = msg;
        //发送的目标场景， 可以选择发送到会话 WXSceneSession 或者朋友圈 WXSceneTimeline。 默认发送到会话。
        req.scene = shareType;
        wxApi.sendReq(req);
    }

    /*
     * 分享图片
     */
    private void sharePicture(int shareType, ShareContent shareContent) {
        Context context = ContextUtils.getContext();
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), shareContent.getPicResource());
        WXImageObject imgObj = new WXImageObject(bmp);

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;

        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
        bmp.recycle();
        msg.thumbData = WechatUtils.bmpToByteArray(thumbBmp, true);  //设置缩略图
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("imgshareappdata");
        req.message = msg;
        req.scene = shareType;
        wxApi.sendReq(req);
    }

    /*
     * 分享链接
     */
    private void shareWebPage(int shareType, ShareContent shareContent) {
        Context context = ContextUtils.getContext();
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = shareContent.getURL();
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = shareContent.getTitle();
        msg.description = shareContent.getContent();

        Bitmap thumb = BitmapFactory.decodeResource(context.getResources(), shareContent.getPicResource());
        if (thumb == null) {
            Toast.makeText(context, "图片不能为空", Toast.LENGTH_SHORT).show();
        } else {
            msg.thumbData = WechatUtils.bmpToByteArray(thumb, true);
        }

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = shareType;
        wxApi.sendReq(req);
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }
}
