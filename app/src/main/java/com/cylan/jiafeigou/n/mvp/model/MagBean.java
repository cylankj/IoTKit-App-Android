package com.cylan.jiafeigou.n.mvp.model;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/2 13:47
 * 描述	      ${用来存放  magActivity页面所需要的数据的来源}
 */
public class MagBean {

    public long magTime;

    public boolean isOpen;

    public int visibleType;

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    public boolean isFirst;

    public void setMagTime(long magTime) {
        this.magTime = magTime;
    }

    public long getMagTime() {
        return magTime;
    }

    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public boolean getIsOpen() {
        return isOpen;
    }

    public void setVisibleType(int visibleType) {
        this.visibleType = visibleType;
    }

    public int getVisibleType() {
        return visibleType;
    }
}
