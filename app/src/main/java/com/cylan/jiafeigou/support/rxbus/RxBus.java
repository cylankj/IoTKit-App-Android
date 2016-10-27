package com.cylan.jiafeigou.support.rxbus;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by hunt on 16-5-26.
 */

public class RxBus implements IEventBus {

    private RxBus() {
    }

    private static RxBus rxBus;

    public static RxBus getInstance() {
        if (rxBus == null) {
            synchronized (RxBus.class) {
                if (rxBus == null) rxBus = new RxBus();
            }
        }
        return rxBus;
    }

    // If multiple threads are going to emit events to this
    // then it must be made thread-safe like this instead
    private final static Subject<Object, Object> _bus = new SerializedSubject<>(PublishSubject.create());

    @Override
    public void send(Object o) {
        if (o == null)
            return;
        _bus.onNext(o);
    }

    public Observable<Object> toObservable() {
        return _bus;
    }

    @Override
    public boolean hasObservers() {
        return _bus.hasObservers();
    }

}
