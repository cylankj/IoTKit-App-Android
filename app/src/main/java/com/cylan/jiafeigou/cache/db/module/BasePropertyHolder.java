package com.cylan.jiafeigou.cache.db.module;

import android.util.SparseArray;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.base.module.BasePropertyParser;
import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper;
import com.cylan.jiafeigou.cache.db.view.IDBHelper;
import com.cylan.jiafeigou.cache.db.view.IEntity;
import com.cylan.jiafeigou.dp.DataPoint;

import java.util.ArrayList;

/**
 * Created by yanzhendong on 2017/3/25.
 */

public abstract class BasePropertyHolder<T> implements IPropertyHolder, IEntity<T> {
    protected transient IPropertyParser propertyParser = BasePropertyParser.getInstance();
    protected transient SparseArray<DPEntity> properties = new SparseArray<>();
    protected transient static IDBHelper dbHelper = BaseDBHelper.getInstance();

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
            V r = entity == null ? null : entity.getValue(defaultValue);
            if (r != null && defaultValue != null && defaultValue.getClass().isInstance(r)) {
                return r;
            }
            return defaultValue;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    @Override
    public final ArrayList<JFGDPMsg> getQueryParams() {
        return propertyParser.getQueryParameters(pid());
    }

    @Override
    public final boolean setValue(int msgId, byte[] bytes, long version) {
        DPEntity property = getProperty(msgId);
        return property != null && property.setValue(bytes, version);
    }

    @Override
    public final boolean setValue(int msgId, DataPoint value) {
        DPEntity property = getProperty(msgId);
        return property != null && property.setValue(value);
    }

    public DPEntity getProperty(int msgId) {
        if (!propertyParser.accept(pid(), msgId)) return null;
        DPEntity value = properties.get(msgId);
        if (value == null) {
            value = dbHelper.getProperty(uuid(), msgId);
            if (value != null) properties.put(msgId, value);
        }
        return value;
    }

    protected abstract String uuid();
}
