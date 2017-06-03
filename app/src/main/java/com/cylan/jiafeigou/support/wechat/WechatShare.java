package com.cylan.jiafeigou.support.wechat;

//import android.app.Activity;
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.text.TextUtils;
//import android.util.Log;
//
//import com.cylan.jiafeigou.support.log.AppLogger;
//import com.cylan.jiafeigou.utils.PackageUtils;
//import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
//import com.tencent.mm.opensdk.modelmsg.WXImageObject;
//import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
//import com.tencent.mm.opensdk.modelmsg.WXTextObject;
//import com.tencent.mm.opensdk.modelmsg.WXVideoObject;
//import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
//import com.tencent.mm.opensdk.openapi.IWXAPI;
//import com.tencent.mm.opensdk.openapi.WXAPIFactory;
//
//import java.lang.ref.WeakReference;
//
///**
// * Created by cylan-hunt on 16-10-26.
// */
//
//public class WechatShare {
//
//    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
//
//
//    private IWXAPI wxApi;
//
//    private boolean isRegister = true;
//
//    /**
//     * weChatAppId：定义在 app-build.gradle的文件中。
//     * https://open.weixin.qq.com/cgi-bin/appdetail?t=manage/detail&type=app&lang=zh_CN&token=03c10c403cfa77539864a99daa459771ceb6ae52&appid=wx3081bcdae8a842cf
//     *
//     * @param activity
//     */
//    public WechatShare(Activity activity) {
//        WeakReference<Activity> weakReference = new WeakReference<>(activity);
//        String appId = PackageUtils.getMetaString(activity, "weChatAppKey");
//        if (TextUtils.isEmpty(appId)) {
//            AppLogger.e("wechat app msgId is null");
//        }
//        Log.e("WechatShare", "WechatShare: " + appId);
//        // 第三个参数作用:checkSignature
//        //这个context 必须是Activity，或者Service
//        wxApi = WXAPIFactory.createWXAPI(weakReference.get(), appId, true);
//        wxApi.registerApp(appId);
//    }
//
//    public boolean isRegister() {
//        return isRegister;
//    }
//
//    public void unregister() {
//        if (isRegister)
//            return;
//        isRegister = false;
//        wxApi.unregisterApp();
//        //必须要调用detach,否则内存泄露。
//        wxApi.detach();
//        Log.d("WechatShare", "unregister: ");
//    }
//
//    public IWXAPI getWxApi() {
//        return wxApi;
//    }
//
//    private static final int THUMB_SIZE = 150;
//    /**
//     * 文字
//     */
//    public static final int WEIXIN_SHARE_CONTENT_TEXT = 1;
//    /**
//     * 图片
//     */
//    public static final int WEIXIN_SHARE_CONTENT_PIC = 2;
//    /**
//     * 链接
//     */
//    public static final int WEIXIN_SHARE_CONTENT_WEBPAGE = 3;
//
//    /**
//     * Video
//     */
//    public static final int WEIXIN_SHARE_CONTENT_VIDEO = 4;
//    /**
//     * 会话
//     */
//    public static final int WEIXIN_SHARE_TYPE_TALK = SendMessageToWX.Req.WXSceneSession;
//    /**
//     * 朋友圈
//     */
//    public static final int WEIXIN_SHARE_TYPE_FRENDS = SendMessageToWX.Req.WXSceneTimeline;
//
//
//    /**
//     * 通过微信分享
//     *
//     * @param shareContent 分享的方式（文本、图片、链接）
//     *                     分享的类型（朋友圈，会话）
//     */
//    public void shareByWX(ShareContent shareContent) {
//        switch (shareContent.getShareContent()) {
//            case WEIXIN_SHARE_CONTENT_TEXT:
//                shareText(shareContent.getShareScene(), shareContent);
//                break;
//            case WEIXIN_SHARE_CONTENT_PIC:
//                sharePicture(shareContent.getShareScene(), shareContent);
//                break;
//            case WEIXIN_SHARE_CONTENT_WEBPAGE:
//                shareWebPage(shareContent.getShareScene(), shareContent);
//                break;
//            case WEIXIN_SHARE_CONTENT_VIDEO:
//                shareVideo(shareContent.getShareScene(), shareContent);
//        }
//    }
//
//    private void shareVideo(int shareScene, ShareContent shareContent) {
//        WXVideoObject video = new WXVideoObject();
//        video.videoUrl = shareContent.getShareURL();
//
//        WXMediaMessage msg = new WXMediaMessage(video);
//        msg.title = shareContent.getTitle();
//        msg.description = shareContent.getContent();
//
//        if (shareContent.getPicResource() != null) {
//            Bitmap thumbBmp = Bitmap.createScaledBitmap(shareContent.getPicResource(), THUMB_SIZE, THUMB_SIZE, true);
//            msg.thumbData = WechatUtils.bmpToByteArray(thumbBmp, true);  //设置缩略图
//        }
//
//        SendMessageToWX.Req req = new SendMessageToWX.Req();
//        req.transaction = buildTransaction("video");
//        req.message = msg;
//        req.scene = shareScene;
//
//        wxApi.sendReq(req);
//
//    }
//
//    public boolean isWeChatInstalled(Context context) {
//        try {
//            return context
//                    .getPackageManager()
//                    .getPackageInfo("com.tencent.mm", PackageManager.GET_SIGNATURES) != null;
//        } catch (PackageManager.NameNotFoundException e) {
//            return false;
//        }
//    }
//
//    public static abstract class ShareContent {
//
//
//        /**
//         * 文字，图片，链接
//         */
//        public int shareContent;
//        /**
//         * 朋友圈，微信
//         */
//        public int shareScene;
//        public String content;
//        public String title;
//        public String url;
//        public Bitmap bitmap;
//
//
//        protected abstract int getShareContent();
//
//        /**
//         * 分享内容描述
//         *
//         * @return
//         */
//        protected abstract String getContent();
//
//        /**
//         * 标题
//         *
//         * @return
//         */
//        protected abstract String getTitle();
//
//        /**
//         * 服务端h5-url
//         *
//         * @return
//         */
//        protected abstract String getShareURL();
//
//        protected abstract Bitmap getPicResource();
//
//        /**
//         * 分享方式： {@link com.tencent.mm.sdk.modelmsg.SendMessageToWX.Req#WXSceneSession}
//         * 分享方式： {@link com.tencent.mm.sdk.modelmsg.SendMessageToWX.Req#WXSceneTimeline}
//         * 分享方式： {@link com.tencent.mm.sdk.modelmsg.SendMessageToWX.Req#WXSceneFavorite}
//         *
//         * @return
//         */
//        protected abstract int getShareScene();
//
//    }
//
//    public static class ShareContentImpl extends ShareContent {
//        @Override
//        public int getShareContent() {
//            return shareContent;
//        }
//
//        @Override
//        public String getContent() {
//            return content;
//        }
//
//        @Override
//        public String getTitle() {
//            return title;
//        }
//
//        @Override
//        public String getShareURL() {
//            return url;
//        }
//
//        @Override
//        public Bitmap getPicResource() {
//            return bitmap;
//        }
//
//        @Override
//        protected int getShareScene() {
//            return shareScene;
//        }
//    }
//
//    /**
//     * 设置分享文字的内容
//     *
//     * @author Administrator
//     */
//    public class ShareContentText extends ShareContent {
//        @Override
//        protected String getContent() {
//            return content;
//        }
//
//        @Override
//        protected String getTitle() {
//            return title;
//        }
//
//        @Override
//        protected String getShareURL() {
//            return url;
//        }
//
//        @Override
//        protected Bitmap getPicResource() {
//            return bitmap;
//        }
//
//        @Override
//        protected int getShareScene() {
//            return shareScene;
//        }
//
//        @Override
//        protected int getShareContent() {
//            return WEIXIN_SHARE_CONTENT_TEXT;
//        }
//
//    }
//
//    /**
//     * 设置分享图片的内容
//     *
//     * @author Administrator
//     */
//    public class ShareContentPic extends ShareContent {
//
//        @Override
//        protected String getContent() {
//            return content;
//        }
//
//        @Override
//        protected String getTitle() {
//            return title;
//        }
//
//        @Override
//        protected String getShareURL() {
//            return url;
//        }
//
//        @Override
//        protected Bitmap getPicResource() {
//            return bitmap;
//        }
//
//        @Override
//        protected int getShareScene() {
//            return shareScene;
//        }
//
//        @Override
//        protected int getShareContent() {
//            return WEIXIN_SHARE_CONTENT_PIC;
//        }
//    }
//
//    /**
//     * 设置分享链接的内容
//     *
//     * @author Administrator
//     */
//    public class ShareContentWebpage extends ShareContent {
//
//
//        @Override
//        protected String getContent() {
//            return content;
//        }
//
//        @Override
//        protected String getTitle() {
//            return title;
//        }
//
//        @Override
//        protected String getShareURL() {
//            return url;
//        }
//
//        @Override
//        protected Bitmap getPicResource() {
//            return bitmap;
//        }
//
//        @Override
//        protected int getShareScene() {
//            return shareScene;
//        }
//
//        @Override
//        protected int getShareContent() {
//            return WEIXIN_SHARE_CONTENT_WEBPAGE;
//        }
//
//    }
//
//    /*
//     * 分享文字
//     */
//    private void shareText(int shareScene, ShareContent shareContent) {
//        String text = shareContent.getContent();
//        //初始化一个WXTextObject对象
//        WXTextObject textObj = new WXTextObject();
//        textObj.text = text;
//        //用WXTextObject对象初始化一个WXMediaMessage对象
//        WXMediaMessage msg = new WXMediaMessage();
//        msg.mediaObject = textObj;
//        msg.description = text;
//        //构造一个Req
//        SendMessageToWX.Req req = new SendMessageToWX.Req();
//        //transaction字段用于唯一标识一个请求
//        req.transaction = buildTransaction("text");
//        req.message = msg;
//        //发送的目标场景， 可以选择发送到会话 WXSceneSession 或者朋友圈 WXSceneTimeline。 默认发送到会话。
//        req.scene = shareScene;
//        wxApi.sendReq(req);
//    }
//
//    /*
//     * 分享图片
//     */
//    private void sharePicture(int shareType, ShareContent shareContent) {
//        Bitmap bmp = shareContent.getPicResource();
//        WXImageObject imgObj = new WXImageObject(bmp);
//        WXMediaMessage msg = new WXMediaMessage();
//        msg.mediaObject = imgObj;
//        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
////        bmp.recycle();Glide会自己管理它的bitmap对象,所以不用手动recycle
//        msg.thumbData = WechatUtils.bmpToByteArray(thumbBmp, true);  //设置缩略图
//        SendMessageToWX.Req req = new SendMessageToWX.Req();
//        req.transaction = buildTransaction("img");
//        req.message = msg;
//        req.scene = shareType;
//        wxApi.sendReq(req);
//    }
//
//    /*
//     * 分享链接
//     */
//    private void shareWebPage(int shareType, ShareContent shareContent) {
//        WXWebpageObject webpage = new WXWebpageObject();
//        webpage.webpageUrl = shareContent.getShareURL();
//        WXMediaMessage msg = new WXMediaMessage(webpage);
//        msg.title = shareContent.getTitle();
//        msg.description = shareContent.getContent();
//        Bitmap src = shareContent.getPicResource();
//        Bitmap thumb = Bitmap.createScaledBitmap(src, THUMB_SIZE, THUMB_SIZE, true);
//        src.recycle();
//        if (thumb == null) {
//            AppLogger.i("pic is null");
//        } else {
//            msg.thumbData = WechatUtils.bmpToByteArray(thumb, true);
//        }
//
//        SendMessageToWX.Req req = new SendMessageToWX.Req();
//        req.transaction = buildTransaction("webpage");
//        req.message = msg;
//        req.scene = shareType;
//        wxApi.sendReq(req);
//    }
//
//    private String buildTransaction(final String type) {
//        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
//    }
//}
