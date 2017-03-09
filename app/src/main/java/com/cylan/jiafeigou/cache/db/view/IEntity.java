package com.cylan.jiafeigou.cache.db.view;

/**
 * Created by yanzhendong on 2017/3/4.
 */

public interface IEntity<T> {
    T setAction(DBAction action);

    DBAction action();

    T setState(DBState state);

    DBState state();

    T setOption(DBOption option);

    <R extends DBOption> R option(Class<R> clz);
}
