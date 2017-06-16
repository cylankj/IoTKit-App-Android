package com.cylan.jiafeigou.cache.db.module;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * 作者：zsl
 * 创建时间：2016/8/30
 * 描述：
 */
@Entity
public class SysMsgBean implements Parcelable {

    @Id
    public int id;
    public String content;
    public int type;
    public String time;
    public String name;
    public int isDone;
    public int isCheck;
    public String sn;
    public int pid;

    public SysMsgBean() {
    }

    public SysMsgBean(String content, int type, String time) {
        this.content = content;
        this.type = type;
        this.time = time;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
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

    public int getIsDone() {
        return isDone;
    }

    public void setIsDone(int isDone) {
        this.isDone = isDone;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.time);
        dest.writeString(this.content);
        dest.writeString(this.sn);
        dest.writeInt(this.type);
        dest.writeInt(this.id);
        dest.writeInt(this.isCheck);
        dest.writeInt(this.isDone);
    }

    public int getPid() {
        return this.pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    protected SysMsgBean(Parcel in) {
        this.name = in.readString();
        this.time = in.readString();
        this.content = in.readString();
        this.sn = in.readString();
        this.type = in.readInt();
        this.id = in.readInt();
        this.isCheck = in.readByte();
        this.isDone = in.readByte();
    }

    @Generated(hash = 892758521)
    public SysMsgBean(int id, String content, int type, String time, String name,
            int isDone, int isCheck, String sn, int pid) {
        this.id = id;
        this.content = content;
        this.type = type;
        this.time = time;
        this.name = name;
        this.isDone = isDone;
        this.isCheck = isCheck;
        this.sn = sn;
        this.pid = pid;
    }

    public static final Creator<SysMsgBean> CREATOR = new Creator<SysMsgBean>() {
        @Override
        public SysMsgBean createFromParcel(Parcel source) {
            return new SysMsgBean(source);
        }

        @Override
        public SysMsgBean[] newArray(int size) {
            return new SysMsgBean[size];
        }
    };
}
