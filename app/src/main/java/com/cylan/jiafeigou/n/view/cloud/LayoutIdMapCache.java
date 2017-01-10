package com.cylan.jiafeigou.n.view.cloud;

import java.util.HashMap;
import java.util.Map;

/**
 * 作者：zsl
 * 创建时间：2016/10/8
 * 描述：
 */
public class LayoutIdMapCache {

    private Map<Integer, Integer> map = new HashMap<>();

    public LayoutIdMapCache() {
    }

    public void registerType(Integer viewType, int layoutId) {
        map.put(viewType, layoutId);
    }

    public int getLayoutId(Integer integer) {
        return map.get(integer);
    }
}
