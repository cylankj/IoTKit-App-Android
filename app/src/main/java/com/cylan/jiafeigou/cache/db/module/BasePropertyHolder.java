package com.cylan.jiafeigou.cache.db.module;

import android.util.Log;
import android.util.SparseArray;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.base.module.BasePropertyParser;
import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.cache.db.view.IEntity;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;

/**
 * Created by yanzhendong on 2017/3/25.
 *
 * @Deprecated Use Device Instead
 */
public abstract class BasePropertyHolder<T> implements IPropertyHolder, IEntity<T> {

    @Deprecated //使用 ValueResolver 来解析,反射效率比较低
    protected transient IPropertyParser propertyParser;

    protected transient SparseArray<DPEntity> properties = new SparseArray<>();

    private static final Object lock = new Object();

    protected abstract int pid();

    /**
     * @param msgId
     * @param defaultValue
     * @param <V>
     * @return
     * @deprecated
     */
    public <V> V $(int msgId, V defaultValue) {

//        Box<PropertyItem> box = BaseApplication.getPropertyItemBox();
//        PropertyItem item = box.get(CacheHolderKt.msgIdKey(uuid(), msgId));
//        V cast = null;
//        if (item != null) {
//            cast = item.cast(defaultValue);
//            Log.i(JConstant.CYLAN_TAG, "item cast :" + cast.toString());
//        }
//        return cast == null ? defaultValue : cast;

        synchronized (lock) {
            try {
                DPEntity entity = getProperty(msgId);
                V result = entity == null ? null : entity.getValue(defaultValue);
                result = result == null ? defaultValue : result;
                if (result != null && defaultValue != null && defaultValue.getClass().isInstance(result)) {
                    return result;
                }
                return defaultValue;
            } catch (Throwable e) {
                AppLogger.e("unpack err::" + msgId);
                return defaultValue;
            }
        }
    }

    @Override
    public void setPropertyParser(IPropertyParser propertyParser) {
        this.propertyParser = propertyParser;
    }

    @Override
    public final ArrayList<JFGDPMsg> getQueryParams() {
        return BasePropertyParser.getInstance().getQueryParameters(pid());
    }

    @Override
    public ArrayList<JFGDPMsg> getAllQueryParams() {
        return null;
    }

    @Override
    public ArrayList<JFGDPMsg> getQueryParameters(int pid, int level) {
        return BasePropertyParser.getInstance().getQueryParameters(pid(), level);
    }

    @Override
    @Deprecated
    public final boolean setValue(int msgId, byte[] bytes, long version) {
        return false;
    }

    @Override
    public final boolean setValue(int msgId, DataPoint value) {
        DPEntity property = getProperty(msgId);
        return property != null && property.setValue(value, value == null ? null : value.toBytes(), value == null ? 0 : value.getVersion());
    }

    @Deprecated
    public DPEntity getProperty(int msgId) {
        if (!BasePropertyParser.getInstance().accept(pid(), msgId)) return null;
        return properties.get(msgId);
    }

    @Override
    public void updateProperty(int msgId, DPEntity entity) {
        if (!BasePropertyParser.getInstance().accept(pid(), msgId)) return;
        Log.d("updateProperty", "updateProperty:" + msgId + "," + (entity == null ? "" : entity.getUuid()));
        properties.put(msgId, entity);
    }

    protected abstract String uuid();
}
