package com.cylan.jiafeigou.cache.db.view;

/**
 * Created by yanzhendong on 2017/3/3.
 */

public interface IDPSingleTask<T extends IDPTaskResult> extends IDPTask<T> {
    <R extends IDPSingleTask<T>> R init(IDPEntity cache) throws Exception;
}
