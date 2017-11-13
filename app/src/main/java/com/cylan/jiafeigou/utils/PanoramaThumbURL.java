package com.cylan.jiafeigou.utils;

import android.text.TextUtils;

import com.bumptech.glide.load.model.GlideUrl;
import com.cylan.jiafeigou.base.module.BaseDeviceInformationFetcher;
import com.cylan.jiafeigou.base.module.DeviceInformation;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by yanzhendong on 2017/5/12.
 */

public class PanoramaThumbURL extends GlideUrl {
    private String fileName;
    private String uuid;

    public PanoramaThumbURL(String uuid, String url) {
        super("http://www.cylan.com.cn");
        this.uuid = uuid;
        this.fileName = url;
    }

    @Override
    public String getCacheKey() {
        return uuid + ":thumb:" + fileName;
    }

    @Override
    public String toStringUrl() {
        DeviceInformation information = BaseDeviceInformationFetcher.getInstance().getDeviceInformation();
        if (information != null && !TextUtils.isEmpty(information.ip)) {
            return "http://" + information.ip + "/thumb/" + fileName.split("\\.")[0] + ".thumb";
        }
        return "http://www.cylan.com.cn";
    }

    @Override
    public URL toURL() throws MalformedURLException {
        return new URL(toStringUrl());
    }
}
