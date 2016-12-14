package com.cylan.jiafeigou.n.mvp.model;


import com.cylan.jiafeigou.support.db.annotation.Column;
import com.cylan.jiafeigou.support.db.annotation.Table;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/18 15:44
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
@Table(name = "MineHelpSuggestionBean")
public class MineHelpSuggestionBean {
    @Column(name = "id", isId = true)
    public int id;
    @Column(name = "icon")
    public String icon;
    @Column(name = "text")
    public String text;
    @Column(name = "date")
    public String date;
    @Column(name = "type")
    public int type;
    @Column(name = "isShowTime")
    public boolean isShowTime;

    public void setIsShowTime(boolean isShowTime) {
        this.isShowTime = isShowTime;
    }

    public boolean getIsShowTime() {
        return isShowTime;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
