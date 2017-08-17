package com.cylan.jiafeigou.server;


import org.msgpack.type.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yanzhendong on 2017/8/17.
 * <p>
 * 使用 key-value 键值对来保存设备属性信息,如果是列表类型的数据
 * 不推荐使用该 Cache,
 * key   : uuid-msgId;
 * value : MsgPackValue
 */

public class ValueCacheMap {

    private Map<String, Value> mValueCacheMap = new HashMap<>();
    private static ValueCacheMap instance;

    public static ValueCacheMap getInstance() {
        if (instance == null) {
            synchronized (ValueCacheMap.class) {
                if (instance == null) {
                    instance = new ValueCacheMap();
                    instance.load();
                }
            }
        }
        return instance;
    }

    public void write() {
    }

    public void load() {
    }

}
