package com.cylan.jiafeigou.utils;

/**
 * Created by yzd on 16-12-13.
 */

public class CamWarnGlideURL extends JFGGlideURL {

    public CamWarnGlideURL(long timeMillis, int index, String vid, String uuid, int regionType, boolean v2) {
        super("", "");

    }

    public CamWarnGlideURL(String cid, String fileName) {
        super(cid, fileName);
    }
}
