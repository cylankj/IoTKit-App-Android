package com.cylan.jiafeigou.server;

import org.msgpack.type.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yanzhendong on 2017/8/17.
 * 用来存放 list 类型的数据的 map,提供了持久化到文件的能力
 */

public class ListValueCacheMap {
    private Map<String, List<Value>> mListValueCacheMap = new HashMap<>();
}
