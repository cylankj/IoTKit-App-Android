package com.cylan.jiafeigou.cache.db.module;


import android.support.annotation.NonNull;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 */
@Entity
public class FeedBackBean implements Comparable<FeedBackBean> {
    @Id(autoincrement = true)
    public Long id;
    private String content;
    private long msgTime;
    private int viewType;

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public int getViewType() {
        return viewType;
    }

    @Generated(hash = 1265079766)
    public FeedBackBean(Long id, String content, long msgTime, int viewType) {
        this.id = id;
        this.content = content;
        this.msgTime = msgTime;
        this.viewType = viewType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FeedBackBean that = (FeedBackBean) o;

        if (msgTime != that.msgTime) return false;
        return content != null ? content.equals(that.content) : that.content == null;
    }

    @Override
    public int hashCode() {
        int result = content != null ? content.hashCode() : 0;
        result = 31 * result + (int) (msgTime ^ (msgTime >>> 32));
        return result;
    }

    @Generated(hash = 270960145)
    public FeedBackBean() {
    }

//    @Generated(hash = 416044222)


//    @Generated(hash = 1818015972)
//    public FeedBackBean() {
//    }

    public void setId(long id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setMsgTime(long msgTime) {
        this.msgTime = msgTime;
    }

    public String getContent() {
        return content;
    }

    public long getMsgTime() {
        return msgTime;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "FeedBackBean{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", msgTime=" + msgTime +
                ", viewType=" + viewType +
                '}';
    }

    @Override
    public int compareTo(@NonNull FeedBackBean o) {
        return (int) (this.msgTime - o.msgTime);
    }
}
