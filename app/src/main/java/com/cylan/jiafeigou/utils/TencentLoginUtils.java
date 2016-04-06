package com.cylan.jiafeigou.utils;

import android.app.Activity;
import android.content.Context;

import com.cylan.jiafeigou.R;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;

public class TencentLoginUtils {

    public Tencent mTencent;

    private static final String SCOPE = "get_simple_userinfo,add_t,get_user_info";//

    public TencentLoginUtils(Context ctx) {
        if (mTencent == null) {
            mTencent = Tencent.createInstance(ctx.getString(R.string.tencent_app_id), ctx);
        }
    }


    public void login(Context ctx, IUiListener mIUiListener) {
        mTencent.login((Activity) ctx, SCOPE, mIUiListener);
    }

    public void logout(Context ctx) {
        mTencent.logout(ctx);
    }

    public boolean isSessionValid() {
        return mTencent.isSessionValid();
    }

    public Tencent getMyTencent() {
        return mTencent;

    }
}
