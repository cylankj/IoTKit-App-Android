package com.cylan.jiafeigou.utils;

import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.cylan.jiafeigou.cache.db.module.Device;
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

public class JFGGlideURL extends GlideUrl {
    private String vid;
    private String cid;
    private String timestamp;
    private boolean V2 = true;//2.0版本
    private int regionType;


    public JFGGlideURL(String cid, String fileName) {
        super("http://www.cylan.com.cn", Headers.DEFAULT);
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(cid);
        this.vid = Security.getVId();
        if (device != null) {
            this.V2 = TextUtils.isEmpty(device.vid);
            Log.d("V2", "V2? " + V2);
            this.regionType = device.regionType;
            if (this.regionType > 1 || this.regionType < 0) regionType = 0;
        } else Log.d("V2", "V2? is empty ");
        this.timestamp = fileName;
        this.cid = cid;
    }

    @Override
    public String getCacheKey() {
        return OptionsImpl.getServer() + vid + "-" + cid + "-" + timestamp + "-" + V2 + "-" + regionType;
    }


    @Override
    public URL toURL() throws MalformedURLException {
        try {
            String urlV2 = String.format(Locale.getDefault(), "/%s/%s", cid, timestamp);
            String urlV3 = String.format(Locale.getDefault(), "/cid/%s/%s/%s", vid, cid, timestamp);
            urlV2 = BaseApplication.getAppComponent().getCmd().getSignedCloudUrl(this.regionType, urlV2);
            urlV3 = BaseApplication.getAppComponent().getCmd().getSignedCloudUrl(this.regionType, urlV3);
            AppLogger.d("图片 URLV2:" + urlV2 + ",图片 URLV3:" + urlV3);
            return new URL(V2 ? urlV2 : urlV3);
        } catch (Exception e) {
            AppLogger.e(String.format("err:%s", e.getLocalizedMessage()));
            return new URL("");
        }
    }
}
