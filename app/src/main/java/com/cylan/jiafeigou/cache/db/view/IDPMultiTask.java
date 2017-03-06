package com.cylan.jiafeigou.cache.db.view;

import java.util.List;

/**
 * Created by yanzhendong on 2017/3/3.
 */

//multiTask 集合Item的uuid必须相同且 action 也必须相同,否则只能使用 singleTask
public interface IDPMultiTask<T extends IDPTaskResult> extends IDPTask<T> {

    <R extends IDPMultiTask<T>> R init(List<IDPEntity> cache);
}
