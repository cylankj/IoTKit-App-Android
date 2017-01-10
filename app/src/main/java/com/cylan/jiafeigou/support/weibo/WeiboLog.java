package com.cylan.jiafeigou.support.weibo;


import android.app.Activity;
import android.content.Context;

import com.cylan.jiafeigou.R;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;

/**
 * Created by hunt on 16-5-24.
 */

public class WeiboLog {

    public Tencent mTencent;

    private static final String SCOPE = "get_simple_userinfo,add_t,get_user_info";//

    public WeiboLog(Context ctx) {
        if (mTencent == null) {
            mTencent = Tencent.createInstance(ctx.getString(R.string.tencent_app_id), ctx);
        }
    }


    public void login(Activity activity, IUiListener mIUiListener) {
        mTencent.login(activity, SCOPE, mIUiListener);
    }

    public void logout(Activity activity) {
        mTencent.logout(activity);
    }

    public boolean isSessionValid() {
        return mTencent.isSessionValid();
    }
}
