package com.cylan.jiafeigou.cache.db.module;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.dp.DataPoint;

import java.util.ArrayList;

/**
 * Created by yanzhendong on 2017/3/25.
 */

public interface IPropertyHolder {
    <V> V $(int msgId, V defaultValue);

    ArrayList<JFGDPMsg> getQueryParams();

    boolean setValue(int msgId, byte[] bytes, long version);

    boolean setValue(int msgId, DataPoint value);

    void updateProperty(int msgId, DPEntity entity);

    void setPropertyParser(IPropertyParser parser);
}
