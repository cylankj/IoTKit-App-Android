package com.cylan.jiafeigou.cache.db.module;

import android.util.Log;
import android.util.SparseArray;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.cache.db.view.IEntity;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;

/**
 * Created by yanzhendong on 2017/3/25.
 */

public abstract class BasePropertyHolder<T> implements IPropertyHolder, IEntity<T> {
    protected transient IPropertyParser propertyParser;
    protected transient SparseArray<DPEntity> properties = new SparseArray<>();

    protected abstract int pid();

    /**
     * @param msgId
     * @param defaultValue
     * @param <V>
     * @return
     */
    public <V> V $(int msgId, V defaultValue) {
        try {
            DPEntity entity = getProperty(msgId);
            V result = entity == null ? null : entity.getValue(defaultValue);
            return result == null ? defaultValue : result;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    @Override
    public void setPropertyParser(IPropertyParser propertyParser) {
        this.propertyParser = propertyParser;
    }

    @Override
    public final ArrayList<JFGDPMsg> getQueryParams() {
        return propertyParser.getQueryParameters(pid());
    }

    @Override
    @Deprecated
    public final boolean setValue(int msgId, byte[] bytes, long version) {
        return false;
    }

    @Override
    public final boolean setValue(int msgId, DataPoint value) {
        DPEntity property = getProperty(msgId);
        return property != null && property.setValue(value, value == null ? null : value.toBytes(), value == null ? 0 : value.version);
    }

    public DPEntity getProperty(int msgId) {
        if (propertyParser == null || !propertyParser.accept(pid(), msgId)) return null;
        return properties.get(msgId);
    }

    @Override
    public void updateProperty(int msgId, DPEntity entity) {
        if (!propertyParser.accept(pid(), msgId)) return;
        Log.d("updateProperty", "updateProperty:" + msgId);
        properties.put(msgId, entity);
    }

    protected abstract String uuid();
}
