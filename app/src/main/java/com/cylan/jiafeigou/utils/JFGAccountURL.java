package com.cylan.jiafeigou.utils;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.support.OptionsImpl;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 * Created by yanzhendong on 2017/6/27.
 */

public class JFGAccountURL extends GlideUrl {
    private final String account;
    private final int storageType;

    public JFGAccountURL(String account) {
        super("http://www.cylan.com.cn", Headers.DEFAULT);
        this.account = account;
        this.storageType = DataSourceManager.getInstance().getStorageType();
    }

    @Override
    public String getCacheKey() {
        return OptionsImpl.getServer() + OptionsImpl.getVid() + "-" + account + "-" + storageType;
    }

    @Override
    public String toStringUrl() {
        String url = null;
        try {
            url = BaseApplication.getAppComponent().getCmd().getSignedCloudUrl(storageType, String.format(Locale.getDefault(), "/image/%s.jpg", account));

        } catch (Exception e) {
            AppLogger.e(String.format("err:%s", e.getLocalizedMessage()));
        }
        return url == null ? "" : url;
    }

    @Override
    public URL toURL() throws MalformedURLException {
      return   new URL(toStringUrl());
    }

}
