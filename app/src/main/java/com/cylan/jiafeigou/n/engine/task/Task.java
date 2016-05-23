package com.cylan.jiafeigou.n.engine.task;

/**
 * Created by hunt on 16-5-20.
 */
public interface Task<T> {

    T taskStart();

    void taskFinish(T result);

    void taskFailed(Throwable throwable);
}
