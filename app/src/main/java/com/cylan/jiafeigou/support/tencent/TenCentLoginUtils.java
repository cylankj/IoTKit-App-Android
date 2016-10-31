package com.cylan.jiafeigou.support.tencent;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.utils.PackageUtils;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;

public class TenCentLoginUtils {

    public Tencent mTenCent;

    private static final String SCOPE = "get_simple_userinfo,add_t,get_user_info";//

    public TenCentLoginUtils(Context ctx) {
        final String appKey = PackageUtils.getMetaString(ctx, "qqAppKey");
        if (TextUtils.isEmpty(appKey)) {
            AppLogger.e("qq appKey is empty");
        }
        if (mTenCent == null) {
            mTenCent = Tencent.createInstance(appKey, ctx);
        }
    }


    public void login(Context ctx, IUiListener mIUiListener) {
        mTenCent.login((Activity) ctx, SCOPE, mIUiListener);
    }

    public void logout(Context ctx) {
        mTenCent.logout(ctx);
    }

    public boolean isSessionValid() {
        return mTenCent.isSessionValid();
    }

    public Tencent getMyTencent() {
        return mTenCent;
    }
}
