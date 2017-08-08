package com.cylan.jiafeigou.cache.db.module;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * 作者：zsl
 * 创建时间：2016/8/30
 * 描述：
 */
@Entity
public class SysMsgBean implements Parcelable, Comparable<SysMsgBean> {

    @Id
    public int id;
    public String content;
    public int type;
    public long time;
    public String name;
    public int isDone;
    public int isCheck;
    public String sn;
    public int pid;

    public SysMsgBean() {
    }

    public void setTime(long time) {
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


    public void setContent(String content) {
        this.content = content;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getPid() {
        return this.pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "SysMsgBean{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", type=" + type +
                ", time=" + time +
                ", name='" + name + '\'' +
                ", isDone=" + isDone +
                ", isCheck=" + isCheck +
                ", sn='" + sn + '\'' +
                ", pid=" + pid +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.content);
        dest.writeInt(this.type);
        dest.writeLong(this.time);
        dest.writeString(this.name);
        dest.writeInt(this.isDone);
        dest.writeInt(this.isCheck);
        dest.writeString(this.sn);
        dest.writeInt(this.pid);
    }

    protected SysMsgBean(Parcel in) {
        this.id = in.readInt();
        this.content = in.readString();
        this.type = in.readInt();
        this.time = in.readLong();
        this.name = in.readString();
        this.isDone = in.readInt();
        this.isCheck = in.readInt();
        this.sn = in.readString();
        this.pid = in.readInt();
    }

    @Generated(hash = 784567696)
    public SysMsgBean(int id, String content, int type, long time, String name,
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

    @Override
    public int compareTo(@NonNull SysMsgBean o) {
        return (int) (this.time - o.time);
    }
}
