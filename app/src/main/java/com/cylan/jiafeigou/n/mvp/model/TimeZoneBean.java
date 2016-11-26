package com.cylan.jiafeigou.n.mvp.model;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/15 15:28
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class TimeZoneBean {
    private String gmt;
    private String id;
    private String name;
    private int offset;

    public void setGmt(String gmt) {
        this.gmt = gmt;
    }

    public String getGmt() {
        return gmt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return "TimeZoneBean{" +
                "gmt='" + gmt + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", offset=" + offset +
                '}';
    }
}
