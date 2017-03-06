package com.cylan.jiafeigou.cache.db.view;

/**
 * Created by yanzhendong on 2017/3/1.
 */

public interface IDPTaskFactory {

    <T extends IDPTaskResult> IDPTask<T> getTask(DBAction action, boolean multi, Object initValue);
}
