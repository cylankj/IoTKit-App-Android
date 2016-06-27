package com.cylan.jiafeigou.n.mvp.contract.login;

import android.app.Activity;

import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.support.sina.SinaWeiboUtil;
import com.cylan.jiafeigou.support.tencent.TencentLoginUtils;

/**
 * Created by chen on 5/30/16.
 */
public interface LoginContract {

    interface ViewRequiredOps {

        void  LoginExecuted(String succeed);

    }

    interface PresenterOps {

        void executeLogin(Activity activity,LoginAccountBean infoLogin);

        void thirdLogin(Activity activity, int type);

        TencentLoginUtils getTencentObj();

        SinaWeiboUtil getSinaObj();
    }

    interface PresenterRequiredOps {
        void loginInited(String msg);
    }
}
