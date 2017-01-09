package com.cylan.jiafeigou.n.view.cloud;

import java.util.HashMap;
import java.util.Map;

/**
 * 作者：zsl
 * 创建时间：2016/10/8
 * 描述：
 */
public class ViewTypeMapCache {
    private Map<Class<?>, Integer> map = new HashMap<>();

    public ViewTypeMapCache() {
    }

    public void registerType(Class<?> keyClazz, int viewType) {
        map.put(keyClazz, viewType);
    }

    public int getType(Class<?> clazz) {
        Object o = map.get(clazz);
        if (o == null)
            return 0;
        return map.get(clazz);
    }

    public int getSize() {
        return map.size();
    }
}
