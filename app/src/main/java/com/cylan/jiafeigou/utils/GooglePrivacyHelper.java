package com.cylan.jiafeigou.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import static android.content.Intent.ACTION_DIAL;

/**
 * @author yanzhendong
 */
public class GooglePrivacyHelper {


    public static void callPhone(Context context, String phone) {
        Intent intent = new Intent(ACTION_DIAL, Uri.parse("tel:" + phone));
        context.startActivity(intent);
    }

    public static void shareApp(Context context, String content) {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, content);
        context.startActivity(intent);
    }
}
