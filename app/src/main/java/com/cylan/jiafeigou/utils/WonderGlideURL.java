package com.cylan.jiafeigou.utils;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.cylan.entity.JfgEnum;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by yzd on 16-12-13.
 */

public class WonderGlideURL extends GlideUrl {
    protected DpMsgDefine.DPWonderItem mBean;

    public WonderGlideURL(DpMsgDefine.DPWonderItem bean) {
        super("http://www.cylan.com.cn", Headers.DEFAULT);
        if (bean == null || bean.fileName == null || bean.cid == null)
            throw new IllegalArgumentException("DPWonderItem is Not Completed!");
        mBean = bean;
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
            url = JfgCmdInsurance.getCmd().getCloudUrlByType(JfgEnum.JFG_URL.WONDER, flag, mBean.fileName, mBean.cid, "0001");
        } catch (JfgException e) {
            e.printStackTrace();
        }
        return new URL(url);
    }
}
