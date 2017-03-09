package com.cylan.jiafeigou.utils;

import android.util.Log;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.support.Security;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 * Created by yzd on 16-12-13.
 */

public class CamWarnGlideURL extends GlideUrl {
    protected DpMsgDefine.DPAlarm mBean;
    private String uuid;
    private int index;
    private int regionType;

    public CamWarnGlideURL(DpMsgDefine.DPAlarm bean, int index, String uuid, int regionType) {
        super("http://www.cylan.com.cn", Headers.DEFAULT);
        if (bean == null)
            throw new IllegalArgumentException("DPWonderItem is Not Completed!");
        mBean = bean;
        this.index = index;
        this.uuid = uuid;
        this.regionType = regionType;
    }

    @Override
    public String getCacheKey() {
        return uuid + mBean.time + index;
    }

    @Override
    public URL toURL() throws MalformedURLException {

        try {
//            [bucket]/cid/[vid]/[cid]/[timestamp]_[id].jpg
            String u = String.format(Locale.getDefault(), "/%s/%s/%s/%s_%s.jpg",
                    uuid, Security.getVId(JFGRules.getTrimPackageName()), uuid, mBean.time, index);
            String url = JfgCmdInsurance.getCmd().getSignedCloudUrl(regionType, u);
            Log.d("toURL", "toURL: " + url);
            return new URL(url);
        } catch (Exception e) {
            AppLogger.e(String.format("err:%s", e.getLocalizedMessage()));
            return new URL("");
        }

    }
}
