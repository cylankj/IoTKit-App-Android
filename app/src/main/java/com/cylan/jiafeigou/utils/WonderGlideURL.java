package com.cylan.jiafeigou.utils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.support.OptionsImpl;
import com.cylan.jiafeigou.support.Security;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 * Created by yzd on 16-12-13.
 */

public class WonderGlideURL extends GlideUrl {
    protected DpMsgDefine.DPWonderItem mBean;
    private String vid;
    private String account;
    private int regionType;

    public WonderGlideURL(DpMsgDefine.DPWonderItem bean) {
        super("http://www.cylan.com.cn", Headers.DEFAULT);
        mBean = bean;
        this.vid = Security.getVId();
        if (BaseApplication.getAppComponent().getSourceManager().getAccount() != null)
            this.account = BaseApplication.getAppComponent().getSourceManager().getAccount().getAccount();
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(bean.cid);
        if (device != null)
            this.regionType = device.regionType;
    }

    @Override
    public String getCacheKey() {
        return OptionsImpl.getServer() + "-" + mBean.cid + "-" + mBean.msgType + "-" + mBean.time + "-" + mBean.fileName;
    }

    @Override
    public URL toURL() throws MalformedURLException {
        String url = "";
        try {
            String u = String.format(Locale.getDefault(), "/long/%s/%s/wonder/%s/%s",
                    vid, account, mBean.cid, mBean.fileName);
            url = BaseApplication.getAppComponent().getCmd().getSignedCloudUrl(this.regionType, u);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new URL(url);
    }

    public interface FileInterface {
        void onResourceReady(String filePath);
    }

    public void fetchFile(FileInterface fileInterface) {
        Glide.with(ContextUtils.getContext())
                .load(this)
                .downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                        if (fileInterface != null) {
                            fileInterface.onResourceReady(resource.getAbsolutePath());
                        }
                    }
                });
    }
}
