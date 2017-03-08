package com.cylan.jiafeigou.utils;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
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
    private int mType;
    private int mFlag;
    private String mFile;
    private String mPath;
    private String uuid;

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
        return mType + "-" + mFlag + "-" + mFile + "-" + mPath;
    }


    @Override
    public URL toURL() throws MalformedURLException {
        String url = "";
        try {
//            [bucket]/cid/[vid]/[cid]/[timestamp]_[id].jpg
            String u = String.format(Locale.getDefault(), "/%s/%s/%s/%s",
                    uuid, Security.getVId(JFGRules.getTrimPackageName()), uuid, mFile);
            url = JfgCmdInsurance.getCmd().getSignedCloudUrl(1, u);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new URL(url);
    }
}
