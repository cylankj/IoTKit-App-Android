package com.cylan.jiafeigou.utils;

//import android.app.Activity;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.drawable.Drawable;
//import android.net.Uri;
//import android.os.Bundle;
//import android.support.v4.app.FragmentActivity;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.DecodeFormat;
//import com.bumptech.glide.load.model.GlideUrl;
//import com.bumptech.glide.request.animation.GlideAnimation;
//import com.bumptech.glide.request.target.SimpleTarget;
//import com.cylan.jiafeigou.R;
//import com.cylan.jiafeigou.misc.JConstant;
//import com.cylan.jiafeigou.support.log.AppLogger;
//import com.cylan.jiafeigou.support.wechat.WechatShare;
//import com.facebook.FacebookCallback;
//import com.facebook.FacebookException;
//import com.facebook.FacebookSdk;
//import com.facebook.internal.CallbackManagerImpl;
//import com.facebook.share.Sharer;
//import com.facebook.share.model.ShareLinkContent;
//import com.facebook.share.model.SharePhoto;
//import com.facebook.share.model.SharePhotoContent;
//import com.facebook.share.widget.ShareDialog;
//import com.google.gson.Gson;
//import com.sina.weibo.sdk.api.WebpageObject;
//import com.sina.weibo.sdk.api.WeiboMessage;
//import com.sina.weibo.sdk.api.WeiboMultiMessage;
//import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
//import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
//import com.sina.weibo.sdk.api.share.WeiboShareSDK;
//import com.sina.weibo.sdk.auth.sso.SsoHandler;
//import com.tencent.connect.share.QQShare;
//import com.tencent.tauth.IUiListener;
//import com.tencent.tauth.Tencent;
//import com.tencent.tauth.UiError;
//import com.twitter.sdk.android.tweetcomposer.TweetComposer;
//import com.twitter.sdk.android.tweetcomposer.TweetUploadService;
//
//import java.io.File;
//import java.lang.ref.WeakReference;
//import java.net.MalformedURLException;
//import java.net.URL;
//
///**
// * Created by yzd on 16-12-9.
// */
//
//public class ShareUtils {
//    private static WeakReference<WechatShare> shareWeakReference = null;
//    private static WeakReference<Tencent> mTencent;
//    private static String SINA_APP_KEY;
//    private static SsoHandler ssoHandler;
//
//
//    public static boolean isQQInstalled() {
//        return true;
//    }
//
//    public static boolean isWeiBoInstalled() {
//        return true;
//    }
//
//    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (ssoHandler != null) {
//            ssoHandler.authorizeCallBack(requestCode, resultCode, data);
//            ssoHandler = null;
//        }
//    }
//
//    public static void sharePictureToWechat(Activity activity, GlideUrl glideUrl, final int type) {
//        if (shareWeakReference == null || shareWeakReference.get() == null)
//            shareWeakReference = new WeakReference<>(new WechatShare(activity));
//        //find bitmap from glide
//        WechatShare wechatShare = shareWeakReference.get();
//        final WechatShare.ShareContent shareContent = new WechatShare.ShareContentImpl();
//        //朋友圈，微信
//        shareContent.shareScene = type;
//        shareContent.shareContent = WechatShare.WEIXIN_SHARE_CONTENT_PIC;
//        Glide.with(ContextUtils.getContext())
//                .load(glideUrl)
//                .asBitmap()
//                .format(DecodeFormat.DEFAULT)
//                .into(new SimpleTarget<Bitmap>(150, 150) {
//                    @Override
//                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
//                        shareContent.bitmap = resource;
//                        wechatShare.shareByWX(shareContent);
//                    }
//
//                    @Override
//                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
//                        ToastUtil.showNegativeToast(activity.getString(R.string.Tap3_ShareDevice_FailTips));
//                    }
//                });
//    }
//
//    public static void sharePictureToTwitter(FragmentActivity activity, GlideUrl glideUrl) {
//        if (!isTwitterInstalled()) {
//            ToastUtil.showNegativeToast(activity.getString(R.string.Tap0_Login_NoInstalled, "twitter"));
//        }
//
//        Glide.with(ContextUtils.getContext())
//                .load(glideUrl)
//                .downloadOnly(new SimpleTarget<File>() {
//                    @Override
//                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
//                        File file = new File(JConstant.MEDIA_DETAIL_PICTURE_DOWNLOAD_DIR, "Tweet.temp");
//                        FileUtils.copyFile(resource, file);
//                        TweetComposer.Builder builder = new TweetComposer.Builder(activity)
//                                .image(Uri.fromFile(file));
//
//                        builder.show();
//                    }
//                });
//    }
//
//    public static boolean isFacebookInstalled() {
//        try {
//            return ContextUtils.getContext()
//                    .getPackageManager()
//                    .getPackageInfo("com.facebook.katana", PackageManager.GET_SIGNATURES) != null;
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    public static boolean isTwitterInstalled() {
//        try {
//            return ContextUtils.getContext()
//                    .getPackageManager()
//                    .getPackageInfo("com.twitter.android", PackageManager.GET_SIGNATURES) != null;
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    public static boolean isWechatInstalled() {
//        try {
//            return ContextUtils.getContext()
//                    .getPackageManager()
//                    .getPackageInfo("com.tencent.mm", PackageManager.GET_SIGNATURES) != null;
//        } catch (PackageManager.NameNotFoundException e) {
//            return false;
//        }
//    }
//
//    public static void shareToFacebook(Activity activity, GlideUrl glideUrl) {
//        if (!FacebookSdk.isInitialized()) {
//            FacebookSdk.sdkInitialize(activity.getApplicationContext());
//        }
//        if (!isFacebookInstalled()) {
//            ToastUtil.showNegativeToast(activity.getString(R.string.Tap0_Login_NoInstalled, "facebook"));
//            return;
//        }
//
//        Glide.with(ContextUtils.getContext())
//                .load(glideUrl)
//                .asBitmap()
//                .format(DecodeFormat.DEFAULT)
//                .into(new SimpleTarget<Bitmap>(150, 150) {
//                    @Override
//                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
//                        SharePhotoContent content = new SharePhotoContent.Builder()
//                                .addPhoto(new SharePhoto.Builder().setBitmap(resource).build())
//                                .build();
//                        ShareDialog dialog = new ShareDialog(activity);
//                        dialog.registerCallback(new CallbackManagerImpl(), new FacebookCallback<Sharer.Result>() {
//                            @Override
//                            public void onSuccess(Sharer.Result result) {
//                                ToastUtil.showPositiveToast(activity.getString(R.string.Tap3_ShareDevice_SuccessTips));
//                                AppLogger.d("shareToFacebook:success");
//                            }
//
//                            @Override
//                            public void onCancel() {
//                                ToastUtil.showPositiveToast(activity.getString(R.string.Tap3_ShareDevice_CanceldeTips));
//                                AppLogger.d("shareToFacebook:canceled");
//                            }
//
//                            @Override
//                            public void onError(FacebookException e) {
//                                ToastUtil.showNegativeToast(activity.getString(R.string.Tap3_ShareDevice_FailTips));
//                                AppLogger.d("shareToFacebook:failed");
//                            }
//                        });
//                        dialog.show(content, ShareDialog.Mode.AUTOMATIC);
//                    }
//
//                    @Override
//                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
//                        ToastUtil.showNegativeToast(activity.getString(R.string.Tap3_ShareDevice_FailTips));
//                    }
//                });
//
//    }
//
//    public static void shareVideoToWechat(Activity activity, String VideoURL, int WXSceneSession, GlideUrl videoThumbURL) {
//        if (shareWeakReference == null || shareWeakReference.get() == null)
//            shareWeakReference = new WeakReference<>(new WechatShare(activity));
//        //find bitmap from glide
//        WechatShare wechatShare = shareWeakReference.get();
//        final WechatShare.ShareContent shareContent = new WechatShare.ShareContentImpl();
//        //朋友圈，微信
//        shareContent.shareScene = WXSceneSession;
//        shareContent.shareContent = WechatShare.WEIXIN_SHARE_CONTENT_VIDEO;
//        shareContent.url = VideoURL;
//        Glide.with(activity)
//                .load(videoThumbURL)
//                .asBitmap()
//                .format(DecodeFormat.DEFAULT)
//                .into(new SimpleTarget<Bitmap>(150, 150) {
//                    @Override
//                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                        shareContent.bitmap = resource;
//                        wechatShare.shareByWX(shareContent);
//                    }
//
//                    @Override
//                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
//                        wechatShare.shareByWX(shareContent);
//                    }
//                });
//    }
//
//    public static void shareWebPageWechat(Activity activity, String VideoURL, int WXSceneSession, GlideUrl videoThumbURL) {
//        if (shareWeakReference == null || shareWeakReference.get() == null)
//            shareWeakReference = new WeakReference<>(new WechatShare(activity));
//        //find bitmap from glide
//        WechatShare wechatShare = shareWeakReference.get();
//        final WechatShare.ShareContent shareContent = new WechatShare.ShareContentImpl();
//        //朋友圈，微信
//        shareContent.shareScene = WXSceneSession;
//        shareContent.shareContent = WechatShare.WEIXIN_SHARE_CONTENT_WEBPAGE;
//        shareContent.url = VideoURL;
//        Glide.with(activity)
//                .load(videoThumbURL)
//                .asBitmap()
//                .format(DecodeFormat.DEFAULT)
//                .into(new SimpleTarget<Bitmap>(150, 150) {
//                    @Override
//                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                        shareContent.bitmap = resource;
//                        wechatShare.shareByWX(shareContent);
//                    }
//
//                    @Override
//                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
//                        wechatShare.shareByWX(shareContent);
//                    }
//                });
//
//
//    }
//
//    public static void shareVideoToTwitter(Activity activity, String videoURL, GlideUrl videoThumbURL) {
//        if (!isTwitterInstalled()) {
//            ToastUtil.showNegativeToast(activity.getString(R.string.Tap0_Login_NoInstalled, "twitter"));
//        }
//
//        Glide.with(ContextUtils.getContext())
//                .load(videoThumbURL)
//                .downloadOnly(new SimpleTarget<File>() {
//                    @Override
//                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
//                        File file = new File(JConstant.MEDIA_DETAIL_PICTURE_DOWNLOAD_DIR, "Tweet.temp");
//                        FileUtils.copyFile(resource, file);
//                        URL url = null;
//                        try {
//                            url = new URL(videoURL);
//                        } catch (MalformedURLException e) {
//                            e.printStackTrace();
//                        }
//                        TweetComposer.Builder builder = new TweetComposer.Builder(activity)
//                                .image(Uri.fromFile(file))
//                                .url(url);
//                        builder.show();
//                    }
//                });
//    }
//
//    public static void shareVideoToFacebook(Activity activity, String videoURL, GlideUrl videoThumbURL) {
//        if (!FacebookSdk.isInitialized()) {
//            FacebookSdk.sdkInitialize(activity.getApplicationContext());
//        }
//        if (!isFacebookInstalled()) {
//            ToastUtil.showNegativeToast(activity.getString(R.string.Tap0_Login_NoInstalled, "facebook"));
//            return;
//        }
//
//        ShareLinkContent content = new ShareLinkContent.Builder()
//                .setImageUrl(Uri.parse(videoThumbURL.toStringUrl()))
//                .setContentUrl(Uri.parse(videoURL))
//                .build();
//        ShareDialog dialog = new ShareDialog(activity);
//        dialog.registerCallback(new CallbackManagerImpl(), new FacebookCallback<Sharer.Result>() {
//            @Override
//            public void onSuccess(Sharer.Result result) {
//                ToastUtil.showPositiveToast(activity.getString(R.string.Tap3_ShareDevice_SuccessTips));
//                AppLogger.d("shareToFacebook:success");
//            }
//
//            @Override
//            public void onCancel() {
//                ToastUtil.showPositiveToast(activity.getString(R.string.Tap3_ShareDevice_CanceldeTips));
//                AppLogger.d("shareToFacebook:canceled");
//            }
//
//            @Override
//            public void onError(FacebookException e) {
//                ToastUtil.showNegativeToast(activity.getString(R.string.Tap3_ShareDevice_FailTips));
//                AppLogger.d("shareToFacebook:failed");
//            }
//        });
//        if (dialog.canShow(content)) {
//            dialog.show(content, ShareDialog.Mode.AUTOMATIC);
//        } else {
//            ToastUtil.showNegativeToast(activity.getString(R.string.Tap3_ShareDevice_FailTips));
//        }
//    }
//
//    public static void sharePictureToQQ(Activity activity, GlideUrl glideUrl) {
//        if (mTencent == null || mTencent.get() == null) {
//            String APP_KEY = PackageUtils.getMetaString(activity, "qqAppKey");
//            mTencent = new WeakReference<>(Tencent.createInstance(APP_KEY, activity));
//        }
//        Glide.with(activity)
//                .load(glideUrl)
//                .downloadOnly(new SimpleTarget<File>() {
//                    @Override
//                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
//                        Bundle bundle = new Bundle();
////                        bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, resource.getAbsolutePath());
//                        bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
//                        bundle.putString(QQShare.SHARE_TO_QQ_TARGET_URL, "http://www.cylan.com.cn");
//                        bundle.putString(QQShare.SHARE_TO_QQ_TITLE, "this is my share");
//                        bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, resource.getAbsolutePath());
//                        mTencent.get().shareToQQ(activity, bundle, new IUiListener() {
//                            @Override
//                            public void onComplete(Object o) {
//                                ToastUtil.showPositiveToast("分享成功");
//                            }
//
//                            @Override
//                            public void onError(UiError uiError) {
//                                AppLogger.e("QQ 分享:" + new Gson().toJson(uiError));
//                                ToastUtil.showNegativeToast("分享失败");
//                            }
//
//                            @Override
//                            public void onCancel() {
//                                ToastUtil.showNegativeToast("分享取消");
//                            }
//                        });
//                    }
//                });
//
//    }
//
//    public static void shareVideoToQQ(Activity activity, String h5) {
//        if (mTencent == null || mTencent.get() == null) {
//            String APP_KEY = PackageUtils.getMetaString(activity, "qqAppKey");
//            mTencent = new WeakReference<>(Tencent.createInstance(APP_KEY, activity));
//        }
//        Bundle bundle = new Bundle();
//        bundle.putString(QQShare.SHARE_TO_QQ_TARGET_URL, h5);
//        bundle.putString(QQShare.SHARE_TO_QQ_TITLE, "QQ 分享");
//        mTencent.get().shareToQQ(activity, bundle, new IUiListener() {
//            @Override
//            public void onComplete(Object o) {
//                ToastUtil.showPositiveToast("分享成功");
//            }
//
//            @Override
//            public void onError(UiError uiError) {
//                ToastUtil.showNegativeToast("分享失败");
//            }
//
//            @Override
//            public void onCancel() {
//                ToastUtil.showNegativeToast("分享取消");
//            }
//        });
//    }
//
//    public static void sharePictureToQZone(Activity activity) {
//        if (mTencent == null || mTencent.get() == null) {
//            String APP_KEY = PackageUtils.getMetaString(activity, "qqAppKey");
//            mTencent = new WeakReference<>(Tencent.createInstance(APP_KEY, activity));
//        }
//
//    }
//
//    public static void shareVideoToQZone(Activity activity) {
//
//    }
//
//    public static void sharePictureToWeiBo(Activity activity) {
//
//    }
//
//    public static void shareVideoToWeibo(Activity activity) {
//        SINA_APP_KEY = PackageUtils.getMetaString(activity, "sinaAppKey");
//        IWeiboShareAPI weiboAPI = WeiboShareSDK.createWeiboAPI(activity, SINA_APP_KEY);
//        WeiboMessage message = new WeiboMessage();
//        WebpageObject object = new WebpageObject();
//    }
//
//    public static void shareH5ToWeibo(Activity activity, String desc) {
//        SINA_APP_KEY = PackageUtils.getMetaString(activity, "sinaAppKey");
////        Oauth2AccessToken oauth2AccessToken = AccessTokenKeeper.readAccessToken(activity);
////        AuthInfo authInfo = new AuthInfo(activity, SINA_APP_KEY, REDIRECT_URL, SCOPE);
////        if (!oauth2AccessToken.isSessionValid()) {
////            ssoHandler = new SsoHandler(activity, authInfo);
////            ssoHandler.authorize(new WeiboAuthListener() {
////                @Override
////                public void onComplete(Bundle bundle) {
////                    Oauth2AccessToken oauth2AccessToken = Oauth2AccessToken.parseAccessToken(bundle);
////                    if (oauth2AccessToken.isSessionValid()) {
////                        AccessTokenKeeper.writeAccessToken(activity, oauth2AccessToken);
////                        AppLogger.e("将进行微博分享!!!!!!");
////                        IWeiboShareAPI weiboAPI = WeiboShareSDK.createWeiboAPI(activity, SINA_APP_KEY);
////                        weiboAPI.registerApp();
////                        SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
////                        WeiboMessage message = new WeiboMessage();
////                        WebpageObject object = new WebpageObject();
////                        object.description = desc;
////                        object.title = "微博分享";
////                        message.mediaObject = object;
////                        request.message = message;
////                        request.transaction = String.valueOf(System.currentTimeMillis());
////                        weiboAPI.sendRequest(activity, request, authInfo, oauth2AccessToken.getToken(), null);
////                    }
////                }
////
////                @Override
////                public void onWeiboException(WeiboException e) {
////
////                }
////
////                @Override
////                public void onCancel() {
////
////                }
////            });
////        } else {
//        AppLogger.e("将进行微博分享!!!!!!");
//        IWeiboShareAPI weiboAPI = WeiboShareSDK.createWeiboAPI(activity, SINA_APP_KEY);
//        weiboAPI.registerApp();
//        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
//        WeiboMultiMessage message = new WeiboMultiMessage();
//        WebpageObject object = new WebpageObject();
//        object.description = desc;
//        object.title = "微博分享";
//        message.mediaObject = object;
//        request.multiMessage = message;
//        request.transaction = String.valueOf(System.currentTimeMillis());
//        weiboAPI.sendRequest(activity, request);
////        }
//    }
//
//
//    public static class MyResultReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (TweetUploadService.UPLOAD_SUCCESS.equals(intent.getAction())) {
//                // success
//                ToastUtil.showPositiveToast(context.getString(R.string.Tap3_ShareDevice_SuccessTips));
//                AppLogger.d("shareToTweeter:success");
//            } else {
//                // failure
//                ToastUtil.showNegativeToast(context.getString(R.string.Tap3_ShareDevice_FailTips));
//                AppLogger.d("shareToTweeter:failed");
//            }
//        }
//    }
//}
