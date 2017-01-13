package com.cylan.jiafeigou.utils;

import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.utils.PackageUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 * Created by yzd on 16-12-13.
 */

public class WonderGlideVideoThumbURL extends WonderGlideURL {
    private static final String VIDEO_PICTURE_URL = "http://jiafeigou-yf.oss-cn-hangzhou.aliyuncs.com/long/%s/%s/wonder/%s.jpg";
    private static final String VID = PackageUtils.getMetaString(ContextUtils.getContext(), "vid");

    public WonderGlideVideoThumbURL(DpMsgDefine.DPWonderItem bean) {
        super(bean);
    }

    @Override
    public URL toURL() throws MalformedURLException {
        String url = String.format(Locale.CANADA, VIDEO_PICTURE_URL, VID, mBean.cid, mBean.time);
        return new URL(url);
    }
}
