package com.cylan.jiafeigou.n.mvp.model;

/**
 * Created by chen on 6/6/16.
 */
public class MediaBean implements Comparable<MediaBean> {

    public long time;
    public int mediaType;
    public String srcUrl;
    public String deviceName;
    public String timeInStr;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MediaBean mediaBean = (MediaBean) o;

        return time == mediaBean.time;

    }

    @Override
    public int hashCode() {
        return (int) (time ^ (time >>> 32));
    }


    @Override
    public int compareTo(MediaBean another) {
        return another != null ? (int) (another.time - this.time) : 0;
    }
}

