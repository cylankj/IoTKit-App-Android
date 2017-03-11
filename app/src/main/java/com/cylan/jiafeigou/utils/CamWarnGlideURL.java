package com.cylan.jiafeigou.utils;

import android.util.Log;

import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.io.File;
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
        this.mPath = uuid;
        this.mType = regionType;
        this.isV2 = v2;
        this.mFlag = index;
        this.timestamp = timeMillis;
        this.vid = vid;
        this.uuid = uuid;
        this.mFile = getPath();
    }

    @Override
    public URL toURL() throws MalformedURLException {

        try {
            String url = JfgCmdInsurance.getCmd().getSignedCloudUrl(mType, mFile);
            Log.d("toURL", "toURL: " + url);
            return new URL(url);
        } catch (Exception e) {
            AppLogger.e(String.format("err:%s", e.getLocalizedMessage()));
            return new URL("");
        }
    }

    public String getPath() {
        //v3  报警和门铃截图 	[bucket]/cid/[vid]/[cid]/[timestamp]_[id].jpg
        //v2   [bucket]/[cid]/[timestamp]_[id].jpg
        if (isV2) {
            return File.separator +
                    uuid +
                    File.separator +
                    timestamp / 1000 + "_" + (mFlag + 1) + ".jpg";
        } else {
            return File.separator +
                    uuid +
                    File.separator +
                    vid +
                    File.separator +
                    uuid +
                    File.separator +
                    timestamp / 1000 + "_" + (mFlag + 1) + ".jpg";
        }
    }
}
