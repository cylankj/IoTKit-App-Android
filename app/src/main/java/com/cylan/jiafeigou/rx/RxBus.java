package com.cylan.jiafeigou.rx;

import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * Created by hunt on 16-5-26.
 */

public class RxBus implements IEventBus {

    private static volatile RxBus mDefaultInstance;
    private final Subject<Object, Object> mBus;

    private final Map<Class<?>, Object> mStickyEventMap;

    public RxBus() {
        // If multiple threads are going to emit events to this
        // then it must be made thread-safe like this instead
        mBus = PublishSubject.create().toSerialized();
        mStickyEventMap = new ConcurrentHashMap<>();
    }

    /**
     * default Instance is for caching layer
     *
     * @return
     */
    public static RxBus getCacheInstance() {
        if (mDefaultInstance == null) {
            synchronized (RxBus.class) {
                if (mDefaultInstance == null) {
                    mDefaultInstance = new RxBus();
                }
            }
        }
        return mDefaultInstance;
    }

    /**
     * 发送事件
     */
    @Override
    public Object post(Object event) {
        mBus.onNext(event);
        Log.d("RxBus", "RxBus: post: " + event);
        return event;
    }

    /**
     * 根据传递的 eventType 类型返回特定类型(eventType)的 被观察者
     */
    public <T> Observable<T> toObservable(Class<T> eventType) {
        return mBus.ofType(eventType);
    }

    /**
     * 判断是否有订阅者
     */
    @Override
    public boolean hasObservers() {
        return mBus.hasObservers();
    }

    public void reset() {
        mDefaultInstance = null;
    }

    /**
     * 发送一个新Sticky事件
     */
    public void postSticky(Object event) {
        synchronized (mStickyEventMap) {
            mStickyEventMap.put(event.getClass(), event);
        }
        post(event);
    }

    public boolean hasStickyEvent(Class<?> clazz) {
        return mStickyEventMap.containsKey(clazz);
    }

    /**
     * 根据传递的 eventType 类型返回特定类型(eventType)的 被观察者
     */
    public <T> Observable<T> toObservableSticky(final Class<T> eventType) {
        synchronized (mStickyEventMap) {
            Observable<T> observable = mBus.ofType(eventType);
            final Object event = mStickyEventMap.get(eventType);

            if (event != null) {
                return observable.mergeWith(Observable.create(subscriber -> subscriber.onNext(eventType.cast(event))));
            } else {
                return observable;
            }
        }
    }

    /**
     * 根据eventType获取Sticky事件
     */
    public <T> T getStickyEvent(Class<T> eventType) {
        synchronized (mStickyEventMap) {
            try {
                return eventType.cast(mStickyEventMap.get(eventType));
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * 移除指定eventType的Sticky事件
     */
    public <T> T removeStickyEvent(Class<T> eventType) {
        synchronized (mStickyEventMap) {
            return eventType.cast(mStickyEventMap.remove(eventType));
        }
    }

    /**
     * 移除所有的Sticky事件
     */
    public void removeAllStickyEvents() {
        synchronized (mStickyEventMap) {
            mStickyEventMap.clear();
        }
    }
}
