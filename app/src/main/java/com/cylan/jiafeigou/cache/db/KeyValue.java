package com.cylan.jiafeigou.cache.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by yanzhendong on 2017/12/18.
 */
@Entity()
public class KeyValue {
    @Id
    public long key;
    public String value;
    @Generated(hash = 1910484162)
    public KeyValue(long key, String value) {
        this.key = key;
        this.value = value;
    }
    @Generated(hash = 92014137)
    public KeyValue() {
    }
    public long getKey() {
        return this.key;
    }
    public void setKey(long key) {
        this.key = key;
    }
    public String getValue() {
        return this.value;
    }
    public void setValue(String value) {
        this.value = value;
    }
}
