package com.cylan.jiafeigou.misc;

import android.content.Context;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.support.OptionsImpl;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.util.Locale;

/**
 * Created by hds on 17-6-13.
 */

public class LinkManager {


    public static String getQrCodeLink() {
        JFGAccount jfgaccount = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
        if (jfgaccount == null || TextUtils.isEmpty(jfgaccount.getAccount())) return "";
        final String pkgName = ContextUtils.getContext().getPackageName();
        if (TextUtils.equals(pkgName, "com.cylan.jiafeigou")) {
            return ContextUtils.getContext().getString(R.string.qrcode_prefix, pkgName,
                    jfgaccount.getAccount());
        }
        final String result = ContextUtils.getContext().getString(R.string.qrcode_prefix, "",
                jfgaccount.getAccount());
        return result.substring(0, result.lastIndexOf("?") + 1) + "id=" + jfgaccount.getAccount();
    }

    public static String getLinkShareByApp() {
        Context context = ContextUtils.getContext();
        final String app = context.getString(R.string.share_content) + context.getString(R.string.share_to_friends_link, context.getPackageName());
        return String.format(Locale.getDefault(), app, context.getPackageName());
    }

    public static String getSmsContent() {
        Context context = ContextUtils.getContext();
        return context.getString(R.string.Tap1_share_tips,
                context.getString(R.string.share_to_friends_link,
                        context.getPackageName()),
                context.getResources().getString(R.string.app_name)); // 正文
    }

    public static String getHelpWebUrl() {
        return ContextUtils.getContext().getString(R.string.help_url,
                OptionsImpl.getServer().split(":")[0],
                ContextUtils.getContext().getString(R.string.agreementSuffix));
    }
}
