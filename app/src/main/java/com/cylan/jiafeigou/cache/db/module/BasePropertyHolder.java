package com.cylan.jiafeigou.cache.db.module;

import android.util.SparseArray;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.base.module.BasePropertyParser;
import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper;
import com.cylan.jiafeigou.cache.db.view.IDBHelper;
import com.cylan.jiafeigou.cache.db.view.IEntity;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;

import java.util.ArrayList;

/**
 * Created by yanzhendong on 2017/3/25.
 */

public abstract class BasePropertyHolder<T> implements IPropertyHolder, IEntity<T> {
    protected transient IPropertyParser propertyParser = BasePropertyParser.getInstance();
    protected transient SparseArray<DPEntity> properties = new SparseArray<>();
    protected transient static IDBHelper dbHelper = BaseDBHelper.getInstance();

    protected abstract int pid();

    public <V> V $(int msgId, V defaultValue) {
        DPEntity value = getProperty(msgId);
        if (value == null) return defaultValue;
        if (value.value == null) {
            value.value = propertyParser.parser(value.getMsgId(), value.getBytes(), value.getVersion());
        }
        if (value.value == null) {
            return defaultValue;
        } else if (defaultValue instanceof DataPoint || defaultValue == null) {
            return (V) value.value;
        } else if (value.value instanceof DpMsgDefine.DPPrimary) {
            return (V) ((DpMsgDefine.DPPrimary) value.value).value;
        } else {
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
        if (property == null) return false;
        DataPoint dataPoint = propertyParser.parser(msgId, bytes, version);
        property.value = dataPoint;
        property.setBytes(dataPoint == null ? null : dataPoint.toBytes());
        property.update();
        return dataPoint != null;
    }

    @Override
    public final boolean setValue(int msgId, DataPoint value) {
        DPEntity property = getProperty(msgId);
        property.value = value;
        property.setBytes(value == null ? null : value.toBytes());
        property.update();
        return value != null;
    }

    protected DPEntity getProperty(int msgId) {
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
