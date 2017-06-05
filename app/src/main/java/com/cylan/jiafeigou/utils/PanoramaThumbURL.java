package com.cylan.jiafeigou.utils;

import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.base.module.BaseDeviceInformationFetcher;
import com.cylan.jiafeigou.base.module.DeviceInformation;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by yanzhendong on 2017/5/12.
 */

public class PanoramaThumbURL extends GlideUrl {
    private String fileName;
    private String uuid;

    public PanoramaThumbURL(String uuid, String url) {
        super(url);
        this.uuid = uuid;
        this.fileName = url;
    }

    @Override
    public String getCacheKey() {
        return uuid + ":thumb:" + fileName;
    }

    @Override
    public URL toURL() throws MalformedURLException {
        DeviceInformation information = BaseDeviceInformationFetcher.getInstance().getDeviceInformation();
        if (information != null && !TextUtils.isEmpty(information.ip)) {
            return new URL("http://" + information.ip + "/thumb/" + fileName.split("\\.")[0] + ".thumb");
        }
        return null;
    }

    public void fetchFile(WonderGlideURL.FileInterface fileInterface) {
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
