package com.cylan.jiafeigou.cache.db.view;

/**
 * Created by yanzhendong on 2017/3/4.
 */

public interface IEntity<T> {
    T setAction(IAction action);

    String ACTION();

    T setAction(String action);

    String getAction();

    T setState(String state);

    String getState();
}
