package com.cylan.jiafeigou.utils;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.support.OptionsImpl;
import com.cylan.jiafeigou.support.Security;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 * Created by yzd on 16-12-26.
 */

public class JFGFaceGlideURL extends GlideUrl {
    private String vid;
    private String cid;
    private String account;
    private String faceId;
    private boolean stranger;
    private int regionType;

    public JFGFaceGlideURL(String cid, String faceId, boolean stranger) {
        super("http://www.cylan.com.cn", Headers.DEFAULT);
        this.vid = Security.getVId();
        this.cid = cid;
        this.account = DataSourceManager.getInstance().getAccount().getAccount();
        this.stranger = stranger;
        this.regionType = DataSourceManager.getInstance().getStorageType();
    }

    @Override
    public String getCacheKey() {
        return OptionsImpl.getServer() + vid + "-" + cid + "-" + account + "-" + stranger + "-" + regionType + "-" + faceId;
    }


    @Override
    public URL toURL() throws MalformedURLException {
        try {
            String urlV2;
            if (stranger) {
                urlV2 = String.format(Locale.getDefault(), "/7day/%s/%s/AI/%s/%s.jpg", vid, account, cid, faceId);
                urlV2 = BaseApplication.getAppComponent().getCmd().getSignedCloudUrl(this.regionType, urlV2);
            } else {
                urlV2 = String.format(Locale.getDefault(), "/long/%s/%s/AI/%s/%s.jpg", vid, account, cid, faceId);
                urlV2 = BaseApplication.getAppComponent().getCmd().getSignedCloudUrl(this.regionType, urlV2);
            }
            return new URL(urlV2);
        } catch (Exception e) {
            AppLogger.e(String.format("err:%s", e.getLocalizedMessage()));
            return new URL("");
        }
    }
}
