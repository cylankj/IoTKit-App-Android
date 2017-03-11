package com.cylan.jiafeigou.utils;

import android.util.Log;

import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by yzd on 16-12-13.
 */

public class CamWarnGlideURL extends JFGGlideURL {
    private boolean isV2;
    private String vid;
    private long timestamp;


    public CamWarnGlideURL(long timeMillis, int index, String vid, String uuid, int regionType, boolean v2) {
        super("", "");

    }

    @Override
    public URL toURL() throws MalformedURLException {

        try {
            String url = JfgCmdInsurance.getCmd().getSignedCloudUrl(0, "");
            Log.d("toURL", "toURL: " + url);
            return new URL(url);
        } catch (Exception e) {
            AppLogger.e(String.format("err:%s", e.getLocalizedMessage()));
            return new URL("");
        }
    }
}
