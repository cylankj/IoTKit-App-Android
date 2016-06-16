package com.cylan.jiafeigou.n.mvp.contract.login;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;

import com.cylan.jiafeigou.n.model.BeanInfoLogin;
import com.cylan.jiafeigou.support.sina.SinaWeiboUtil;
import com.cylan.jiafeigou.support.tencent.TencentLoginUtils;

import java.lang.ref.WeakReference;

/**
 * Created by chen on 5/30/16.
 */
public interface LoginContract {

    interface ViewRequiredOps {

        void  LoginExecuted(String succeed);

    }

    interface PresenterOps {

        void executeLogin(Activity activity,BeanInfoLogin infoLogin);

        void thirdLogin(Activity activity, int type);

        TencentLoginUtils getTencentObj();

        SinaWeiboUtil getSinaObj();
    }

    interface PresenterRequiredOps {
        void loginInited(String msg);
    }
}
