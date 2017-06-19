package com.cylan.jiafeigou.support.badge;

/**
 * Created by hds on 17-6-17.
 */

public class CacheObject {

    private int count;
    private Object object;

    public CacheObject setCount(int count) {
        this.count = count;
        return this;
    }

    public CacheObject setObject(Object object) {
        this.object = object;
        return this;
    }

    public int getCount() {
        return count;
    }

    public Object getObject() {
        return object;
    }

    @Override
    public String toString() {
        return "CacheObject{" +
                "count=" + count +
                ", object=" + object +
                '}';
    }
}
