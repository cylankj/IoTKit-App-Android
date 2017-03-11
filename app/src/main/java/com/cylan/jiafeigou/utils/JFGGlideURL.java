package com.cylan.jiafeigou.utils;

import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.support.Security;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 * Created by yzd on 16-12-26.
 */

public class JFGGlideURL extends GlideUrl {
    protected int mType;//图片用途。
    protected int mFlag;
    protected String mFile;
    protected String mPath;
    protected String uuid;

    public JFGGlideURL() {
        super("http://www.cylan.com.cn", Headers.DEFAULT);
    }

    public JFGGlideURL(String uuid, int type, int flag, String file, String path) {
        super("http://www.cylan.com.cn", Headers.DEFAULT);
        this.mType = type;
        mFlag = flag;
        this.mFile = file;
        this.mPath = path;
        this.uuid = uuid;
    }

    @Override
    public String getCacheKey() {
        String cacheKey = mType + "-" + mFlag + "-" + mFile + "-" + mPath;
        Log.d("JFGGlideURL", "cacheKey:" + cacheKey);
        return cacheKey;
    }


    @Override
    public URL toURL() throws MalformedURLException {
        String url = "";
        try {
            JFGDevice device = DataSourceManager.getInstance().getRawJFGDevice(uuid);
            String vid = Security.getVId(JFGRules.getTrimPackageName());
//            [bucket]/cid/[vid]/[cid]/[timestamp]_[id].jpg
            String u;
            if (device != null && TextUtils.isEmpty(device.vid)) {
                //3x
                u = String.format(Locale.getDefault(), "/%s/%s/%s/%s",
                        uuid, vid, uuid, mFile);
            } else {
                //2x
                u = "";
            }
            url = JfgCmdInsurance.getCmd().getSignedCloudUrl(1, u);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new URL(url);
    }
}
