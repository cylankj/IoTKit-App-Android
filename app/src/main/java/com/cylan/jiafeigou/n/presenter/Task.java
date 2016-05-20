package com.cylan.jiafeigou.n.presenter;

/**
 * Created by hunt on 16-5-20.
 */
public interface Task<T> {

    void taskStart();

    void taskFinish(T task);
}
