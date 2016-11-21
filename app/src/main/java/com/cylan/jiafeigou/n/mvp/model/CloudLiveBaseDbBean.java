package com.cylan.jiafeigou.n.mvp.model;

import com.cylan.jiafeigou.support.db.annotation.Column;
import com.cylan.jiafeigou.support.db.annotation.Table;

/**
 * 作者：zsl
 * 创建时间：2016/10/10
 * 描述：
 */
@Table(name = "CloudLiveBaseDbBean")
public class CloudLiveBaseDbBean {
    @Column(name = "id", isId = true)
    public int id;
    @Column(name = "type")
    public int type;
    @Column(name = "data")
    public byte[] data;
    @Column(name = "userIcon")
    public String userIcon;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUserIcon() {
        return userIcon;
    }

    public void setUserIcon(String userIcon) {
        this.userIcon = userIcon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
