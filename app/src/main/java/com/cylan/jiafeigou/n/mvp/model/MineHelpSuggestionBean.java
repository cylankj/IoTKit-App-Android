package com.cylan.jiafeigou.n.mvp.model;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/18 15:44
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
@Entity
public class MineHelpSuggestionBean {
    @Id
    public Long id;
    public String icon;
    public String text;
    public String date;
    public int type;
    public boolean isShowTime;
    public int pro_falag;

    @Generated(hash = 416044222)
    public MineHelpSuggestionBean(Long id, String icon, String text, String date,
                                  int type, boolean isShowTime, int pro_falag) {
        this.id = id;
        this.icon = icon;
        this.text = text;
        this.date = date;
        this.type = type;
        this.isShowTime = isShowTime;
        this.pro_falag = pro_falag;
    }

    @Generated(hash = 1818015972)
    public MineHelpSuggestionBean() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIcon() {
        return this.icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean getIsShowTime() {
        return this.isShowTime;
    }

    public void setIsShowTime(boolean isShowTime) {
        this.isShowTime = isShowTime;
    }

    public int getPro_falag() {
        return this.pro_falag;
    }

    public void setPro_falag(int pro_falag) {
        this.pro_falag = pro_falag;
    }

}
