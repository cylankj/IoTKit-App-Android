package com.cylan.jiafeigou.n.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.cylan.jiafeigou.support.db.annotation.Column;
import com.cylan.jiafeigou.support.db.annotation.Table;

/**
 * 作者：zsl
 * 创建时间：2016/8/30
 * 描述：
 */
@Table(name = "MineMessageBean")
public class MineMessageBean implements Parcelable {

    @Column(name = "id", isId = true)
    public int id;
    @Column(name = "content")
    public String content;
    @Column(name = "type")
    public int type;
    @Column(name = "startTime")
    public String time;
    @Column(name = "name")
    public String name;


    @Column(name = "isCheck")
    public int isCheck;

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

    @Override
    public int describeContents() {
        return 0;
    }

    public int getIsCheck() {
        return isCheck;
    }

    public void setIsCheck(int isCheck) {
        this.isCheck = isCheck;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.time);
        dest.writeString(this.content);
        dest.writeInt(this.type);
        dest.writeInt(this.id);
        dest.writeInt(this.isCheck);
    }

    protected MineMessageBean(Parcel in) {
        this.name = in.readString();
        this.time = in.readString();
        this.content = in.readString();
        this.type = in.readInt();
        this.id = in.readInt();
        this.isCheck = in.readByte();
    }

    public static final Creator<MineMessageBean> CREATOR = new Creator<MineMessageBean>() {
        @Override
        public MineMessageBean createFromParcel(Parcel source) {
            return new MineMessageBean(source);
        }

        @Override
        public MineMessageBean[] newArray(int size) {
            return new MineMessageBean[size];
        }
    };
}
