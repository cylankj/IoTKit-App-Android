package com.cylan.jiafeigou.support.rxbus;

/**
 * Created by cylan-hunt on 16-9-28.
 */

public interface IEventBus {
    void post(Object object);

    boolean hasObservers();
}
