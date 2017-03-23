package com.cylan.jiafeigou.utils;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.support.Security;

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
        this.vid = Security.getVId(JFGRules.getTrimPackageName());
        if (DataSourceManager.getInstance().getAJFGAccount() != null)
            this.account = DataSourceManager.getInstance().getAJFGAccount().getAccount();
        Device device = DataSourceManager.getInstance().getJFGDevice(bean.cid);
        if (device != null)
            this.regionType = device.regionType;
    }

    @Override
    public String getCacheKey() {
        return mBean.cid + mBean.msgType + mBean.time + mBean.fileName;
    }

    @Override
    public URL toURL() throws MalformedURLException {
        String url = "";
        try {
            String u = String.format(Locale.getDefault(), "/long/%s/%s/wonder/%s/%s",
                    vid, account, mBean.cid, mBean.fileName);
            url = JfgCmdInsurance.getCmd().getSignedCloudUrl(this.regionType, u);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new URL(url);
    }
}
