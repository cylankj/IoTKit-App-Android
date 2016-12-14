package com.cylan.jiafeigou.n.mvp.model;

import com.cylan.jiafeigou.support.db.annotation.Column;
import com.cylan.jiafeigou.support.db.annotation.Table;

import java.io.Serializable;

/**
 * 作者：zsl
 * 创建时间：2016/8/30
 * 描述：
 */
@Table(name = "MineMessageBean")
public class MineMessageBean implements Serializable {

    @Column(name = "id", isId = true)
    public int id;
    @Column(name = "content")
    public String content;
    @Column(name = "type")
    public int type;
    @Column(name = "time")
    public String time;
    @Column(name = "name")
    public String name;

    public MineMessageBean() {
    }

    public MineMessageBean(String content, int type, String time) {
        this.content = content;
        this.type = type;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public int getType() {
        return type;
    }

    public String getTime() {
        return time;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
