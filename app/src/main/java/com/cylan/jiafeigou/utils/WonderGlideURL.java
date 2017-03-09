package com.cylan.jiafeigou.utils;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.jiafeigou.base.module.DataSourceManager;
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
            this.account = DataSourceManager.getInstance().getJFGAccount().getAccount();
        JFGDevice device = DataSourceManager.getInstance().getRawJFGDevice(bean.cid);
        if (device != null)
            this.regionType = device.regionType;
    }

    @Override
    public String getCacheKey() {
        return mBean.cid + mBean.msgType + mBean.time + mBean.fileName;
    }

    @Override
    public URL toURL() throws MalformedURLException {
        int flag = Integer.parseInt(mBean.fileName.split("_")[1].substring(0, 1));
        String url = "";
        try {
//            [bucket]/long/[vid]/[account]/wonder/[cid]/[timestamp].jpg
            String u = String.format(Locale.getDefault(), "/long/%s/%s/wonder/%s/%s.jpg/",
                    vid, account, mBean.cid, mBean.time);
            url = JfgCmdInsurance.getCmd().getSignedCloudUrl(this.regionType, u);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new URL(url);
    }
}
