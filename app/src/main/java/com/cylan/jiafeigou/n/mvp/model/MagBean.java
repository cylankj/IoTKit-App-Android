package com.cylan.jiafeigou.n.mvp.model;

import com.cylan.jiafeigou.support.db.annotation.Column;
import com.cylan.jiafeigou.support.db.annotation.Table;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/2 13:47
 * 描述	      ${用来存放  magActivity页面所需要的数据的来源}
 */
@Table(name = "MagBean")
public class MagBean{

    @Column(name = "id", isId = true)
    public int id;

    @Column(name = "magTime")
    public long magTime;

    @Column(name = "isOpen")
    public boolean isOpen;

    @Column(name = "visibleType")
    public int visibleType;

    @Column(name = "isFirst")
    public boolean isFirst;

    public boolean isFirst() {
        return isFirst;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public void setFirst(boolean first) {
        isFirst = first;

    }

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


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
