package com.cylan.jiafeigou.utils;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by yzd on 16-12-26.
 */

public class JFGGlideURL extends GlideUrl {
    private int mType;
    private int mFlag;
    private String mFile;
    private String mPath;

    public JFGGlideURL(int type, int flag, String file, String path) {
        super("http://www.cylan.com.cn", Headers.DEFAULT);
        this.mType = type;
        mFlag = flag;
        this.mFile = file;
        this.mPath = path;
    }

    @Override
    public String getCacheKey() {
        return mType + "-" + mFlag + "-" + mFile + "-" + mPath;
    }


    @Override
    public URL toURL() throws MalformedURLException {
        String url = "";
        try {
            url = JfgCmdInsurance.getCmd().getCloudUrlByType(mType, mFlag, mFile, mPath);
        } catch (JfgException e) {
            e.printStackTrace();
        }
        return new URL(url);
    }
}
