package com.cylan.jiafeigou.rx;

/**
 * Created by cylan-hunt on 16-9-28.
 */

public interface IEventBus {
    Object post(Object object);

    boolean hasObservers();
}
