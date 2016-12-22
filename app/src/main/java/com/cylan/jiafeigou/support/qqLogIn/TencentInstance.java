package com.cylan.jiafeigou.support.qqLogIn;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.cylan.jiafeigou.utils.ContextUtils;
import com.tencent.connect.UserInfo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;

/**
 * 作者：zsl
 * 创建时间：2016/12/12
 * 描述：
 */
public class TencentInstance {

    private static boolean isServerSideLogin = false;

    public static String APP_KEY;

    private Tencent mTencent;

    public TencentInstance(){
        APP_KEY = "1103156296";
        mTencent = Tencent.createInstance(APP_KEY, ContextUtils.getContext());
    }

    /**
     * 登录
     * @param scope
     * @param loginListener
     */
    public void logIn(Activity activity, String scope, IUiListener loginListener){
        if (!mTencent.isSessionValid()) {
            mTencent.loginServerSide(activity, scope, loginListener);
            isServerSideLogin = true;
        } else {
            if (!isServerSideLogin) {
                mTencent.logout(activity);
                mTencent.loginServerSide(activity, scope, loginListener);
                isServerSideLogin = true;
                return;
            }
            mTencent.logout(activity);
            isServerSideLogin = false;
        }
    }

    /**
     * 获取到用户的信息
     */
    public void getUserInfo(Context context){
        UserInfo userInfo = new UserInfo(context,mTencent.getQQToken());
//        userInfo.getUserInfo(new BaseUIListener(this,"get_simple_userinfo"));
    }
}
