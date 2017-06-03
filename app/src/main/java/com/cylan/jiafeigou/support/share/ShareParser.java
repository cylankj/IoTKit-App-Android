package com.cylan.jiafeigou.support.share;

import android.graphics.Bitmap;
import android.os.Bundle;

import com.bumptech.glide.load.model.GlideUrl;

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
        String string = bundle.getString(ShareConstant.SHARE_CONTENT_PICTURE_EXTRA_IMAGE_PATH);
//        try {
//            return Glide.with(ContextUtils.getContext()).load(string).downloadOnly(100, 100).get().toString();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
        return string;
    }

    public GlideUrl parseGlideUrl(Bundle bundle) {
        return null;
    }

    public String parserWebLinkUrl(Bundle bundle) {
        return bundle.getString(ShareConstant.SHARE_CONTENT_WEB_URL_EXTRA_LINK_URL);
    }

    public Bitmap parserImageBitmap(Bundle bundle) {
        return null;
    }

}
