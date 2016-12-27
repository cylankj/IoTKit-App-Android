package com.cylan.jiafeigou.dp;

/**
 * Created by cylan-hunt on 16-12-26.
 */

public class BaseValue implements Comparable<BaseValue> {
    private long id;
    private long version;
    private Object value;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseValue value = (BaseValue) o;

        if (id != value.id) return false;
        return version == value.version;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (version ^ (version >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "BaseValue{" +
                "id=" + id +
                ", version=" + version +
                ", value=" + value +
                '}';
    }

    public long getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public Object getValue() {
        return value;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public int compareTo(BaseValue another) {
        return version > another.version ? -1 : 1;//降序
    }

}
