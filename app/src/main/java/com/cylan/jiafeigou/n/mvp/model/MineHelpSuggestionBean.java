package com.cylan.jiafeigou.n.mvp.model;


import java.util.ArrayList;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/18 15:44
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class MineHelpSuggestionBean {
    public int icon;
    public String text;
    public String date;
    public int type;
    public boolean isShowTime;
    public ArrayList<String> urlList;

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

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getIcon() {
        return icon;
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
}
