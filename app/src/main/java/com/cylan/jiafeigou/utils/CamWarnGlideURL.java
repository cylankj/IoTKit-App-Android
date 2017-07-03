package com.cylan.jiafeigou.utils;

/**
 * Created by yzd on 16-12-13.
 */

public class CamWarnGlideURL extends JFGGlideURL {
    private int time, index;

    public CamWarnGlideURL(String cid, String fileName, int type) {
        super(cid, fileName, type);
    }

    public CamWarnGlideURL(String cid, String fileName, int time, int index, int type) {
        super(cid, fileName, type);
        this.index = index;
        this.time = time;
    }

    public int getTime() {
        return time;
    }

    public int getIndex() {
        return index;
    }
}
