package com.cylan.jiafeigou.support.share;

import android.graphics.Bitmap;
import android.os.Bundle;

/**
 * Created by yanzhendong on 2017/6/1.
 */

public class ShareParser {

    private static ShareParser instance;

    public static ShareParser getInstance() {
        if (instance == null) {
            synchronized (ShareParser.class) {
                if (instance == null) {
                    instance = new ShareParser();
                }
            }
        }

        return instance;
    }

    public String parserImageUrl(Bundle bundle) {
        String string = bundle.getString(ShareContanst.SHARE_IMAGE_URL);
//        try {
//            return Glide.with(ContextUtils.getContext()).load(string).downloadOnly(100, 100).get().toString();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
        return string;
    }

    public String parserWebLinkUrl(Bundle bundle) {
        return bundle.getString(ShareContanst.SHARE_WEB_LINK_URL);
    }

    public Bitmap parserImageBitmap(Bundle bundle) {
        return null;
    }

}
