package com.cylan.jiafeigou.utils;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.support.OptionsImpl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by yzd on 16-12-13.
 */

public class Pan720GlideURL extends GlideUrl {
    private String uuid;
    private String fileName;

    public Pan720GlideURL(String uuid, String fileName) {
        super("http://www.cylan.com.cn", Headers.DEFAULT);
        this.uuid = uuid;
        this.fileName = fileName;
    }

    @Override
    public URL toURL() throws MalformedURLException {
        return new URL(JConstant.PAN_PATH + File.separator + uuid + File.separator + this.fileName);
    }

    @Override
    public String getCacheKey() {
        return OptionsImpl.getServer() + "-" + this.uuid + "-" + this.fileName;
    }
}
