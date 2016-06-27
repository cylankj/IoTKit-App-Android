package com.cylan.jiafeigou.n.mvp.model.contract;

import android.app.Activity;

import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.support.sina.SinaWeiboUtil;
import com.cylan.jiafeigou.support.tencent.TencentLoginUtils;

/**
 * Created by chen on 5/25/16.
 */
public interface ModelContract {
     interface SplashModelOps {
         void splashTimeda();

         void finishAppDalayda();
     }

    interface LoginModelOps {
        String executeLoginda(LoginAccountBean infoLogin);

        void LoginType(Activity activity,int type, OnLoginListener onLoginListener);

        TencentLoginUtils getTencentObj();

        SinaWeiboUtil getSinaObj();
    }

     interface OnLoginListener {
        void loginSuccess(String result);
        void loginFail(String msg);
    }
    interface HomeWonderfulOps {

        void setHeadBackground();
    }
}
