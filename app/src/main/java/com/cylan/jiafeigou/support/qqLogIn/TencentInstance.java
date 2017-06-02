package com.cylan.jiafeigou.support.qqLogIn;

//import android.app.Activity;
//import android.content.Context;
//
//import com.cylan.jiafeigou.misc.JConstant;
//import com.cylan.jiafeigou.support.log.AppLogger;
//import com.cylan.jiafeigou.utils.PackageUtils;
//import com.cylan.jiafeigou.utils.PreferencesUtils;
//import com.tencent.tauth.IUiListener;
//import com.tencent.tauth.Tencent;
//import com.tencent.tauth.UiError;
//
//import org.json.JSONObject;
//
///**
// * 作者：zsl
// * 创建时间：2016/12/12
// * 描述：
// */
//public class TencentInstance {
//
//    private String SCOPE = "all";
//    private static boolean isServerSideLogin = false;
//    public static String APP_KEY;
//    public Tencent mTencent;
//    private static TencentInstance instance;
//    public IUiListener listener;
//    public Context context;
//
//    public static TencentInstance getInstance(Activity activity) {
//        if (instance == null)
//            synchronized (TencentInstance.class) {
//                if (instance == null)
//                    instance = new TencentInstance(activity);
//            }
//        return instance;
//    }
//
//    public TencentInstance(Activity activity) {
////        APP_KEY = "1106028314";
//        APP_KEY = PackageUtils.getMetaString(activity, "qqAppKey");
//        AppLogger.d("qqKey:" + APP_KEY);
//        context = activity;
//        mTencent = Tencent.createInstance(APP_KEY, activity);
//    }
//
//    /**
//     * 登录
//     *
//     * @param scope
//     * @param loginListener
//     */
//    public void logIn(Activity activity, String scope, IUiListener loginListener) {
//        if (!mTencent.isSessionValid()) {
//            mTencent.loginServerSide(activity, scope, loginListener);
//            isServerSideLogin = true;
//        } else {
//            if (!isServerSideLogin) {
//                mTencent.logout(activity);
//                mTencent.loginServerSide(activity, scope, loginListener);
//                isServerSideLogin = true;
//                return;
//            }
//            mTencent.logout(activity);
//            isServerSideLogin = false;
//        }
//    }
//
//    /**
//     * 登录
//     *
//     * @param activity
//     */
//    public void logIn(Activity activity) {
//        if (!mTencent.isSessionValid()) {
//            mTencent.loginServerSide(activity, SCOPE, getQQAuthorizeListener);
//            isServerSideLogin = true;
//        } else {
//            if (!isServerSideLogin) {
//                mTencent.logout(activity);
//                mTencent.loginServerSide(activity, SCOPE, getQQAuthorizeListener);
//                isServerSideLogin = true;
//                return;
//            }
//            mTencent.logout(activity);
//            isServerSideLogin = false;
//        }
//    }
//
//    /**
//     * 登录
//     */
//    public void logIn(IUiListener iUiListener) {
//        listener = iUiListener;
////        if (!mTencent.isSessionValid()) {
//        mTencent.login((Activity) context, SCOPE, iUiListener);
////        }
//    }
//
//    /**
//     * 获取用户信息
//     */
//    private IUiListener getQQAuthorizeListener = new IUiListener() {
//        @Override
//        public void onComplete(Object response) {
//            if (response == null) {
//                AppLogger.d("QQ authorize failed");
//                return;
//            }
//            try {
//                JSONObject jsonObject = (JSONObject) response;
//                String openID = jsonObject.getString("openid");
//                AppLogger.d("OPEN_ID" + openID);
//                String accessToken = jsonObject.getString("access_token");
//                String expires = jsonObject.getString("expires_in");
//                mTencent.setOpenId(openID);
//                mTencent.setAccessToken(accessToken, expires);
//
//                String nickname = jsonObject.getString("nickname");
//                String figureurl = jsonObject.getString("figureurl");
//                PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ICON, figureurl);
//                PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ALIAS, nickname);
//                AppLogger.d("nickname:" + nickname + "figureurl:" + figureurl);
//            } catch (Exception e) {
//                AppLogger.d("QQGetUserInfo error:" + e.getLocalizedMessage());
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void onError(UiError uiError) {
//            AppLogger.d("QQGetUserInfo error:" + uiError.errorMessage);
//        }
//
//        @Override
//        public void onCancel() {
//            AppLogger.d("QQ authorize cancle");
//        }
//    };
//
//    public static void release() {
//        if (instance != null) {
//            instance.mTencent = null;
//            instance.context = null;
//            instance.listener = null;
//            instance = null;
//        }
//    }
//
//}
