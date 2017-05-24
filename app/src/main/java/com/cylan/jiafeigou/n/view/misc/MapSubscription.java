package com.cylan.jiafeigou.n.view.misc;

import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscription;
import rx.exceptions.Exceptions;

/**
 * Created by holy on 2017/3/22.
 */

public class MapSubscription implements Subscription {

    private HashMap<String, Subscription> subscriptions = new HashMap<>(4);
    private volatile boolean unsubscribed;

    public MapSubscription() {
    }

//    public MapSubscription(final Subscription... subscriptions) {
//        this.subscriptions = new HashMap<String, Subscription>(Arrays.asList(subscriptions));
//    }

    @Override
    public boolean isUnsubscribed() {
        return unsubscribed;
    }

    /**
     * Adds a new {@link Subscription} to this {@code CompositeSubscription} if the
     * {@code CompositeSubscription} is not yet unsubscribed. If the {@code CompositeSubscription} <em>is</em>
     * unsubscribed, {@code add} will indicate this by explicitly unsubscribing the new {@code Subscription} as
     * well.
     *
     * @param s the {@link Subscription} to add
     */
    public void add(final Subscription s, String tag) {
//        if (s.isUnsubscribed()) {
//            AppLogger.d("isUnsubscribed:" + tag);
//            return;
//        }
        remove(tag);
        if (!unsubscribed) {
            synchronized (this) {
                if (!unsubscribed) {
                    if (subscriptions == null) {
                        subscriptions = new HashMap<>(4);
                    }
                    subscriptions.put(tag, s);
                    AppLogger.d("add to sub:" + tag);
                    return;
                }
            }
        }
        // call after leaving the synchronized block so we're not holding a lock while executing this
        s.unsubscribe();
    }

    /**
     * Removes a {@link Subscription} from this {@code CompositeSubscription}, and unsubscribes the
     * {@link Subscription}.
     *
     * @param tag the {@link Subscription} to remove
     */
    public void remove(final String tag) {
        if (!unsubscribed) {
            Subscription unsubscribe = null;
            synchronized (this) {
                if (unsubscribed || subscriptions == null) {
                    return;
                }
                unsubscribe = subscriptions.remove(tag);
            }
            if (unsubscribe != null) {
                // if we removed successfully we then need to call unsubscribe on it (outside of the lock)
                unsubscribe.unsubscribe();
            }
        }
    }

    /**
     * Unsubscribes any subscriptions that are currently part of this {@code CompositeSubscription} and remove
     * them from the {@code CompositeSubscription} so that the {@code CompositeSubscription} is empty and in
     * an unoperative state.
     */
    public void clear() {
        if (!unsubscribed) {
            Collection<Map.Entry<String, Subscription>> unsubscribe = null;
            synchronized (this) {
                if (unsubscribed || subscriptions == null) {
                    return;
                } else {
                    unsubscribe = subscriptions.entrySet();
                    subscriptions = null;
                }
                unsubscribeFromAll(unsubscribe);
            }
        }
    }

    @Override
    public void unsubscribe() {
        if (!unsubscribed) {
            Collection<Map.Entry<String, Subscription>> unsubscribe = null;
            synchronized (this) {
                if (unsubscribed || subscriptions == null) {
                    return;
                }
                unsubscribed = true;
                unsubscribe = subscriptions.entrySet();
                subscriptions = null;
            }
            // we will only get here once
            unsubscribeFromAll(unsubscribe);
        }
    }

    private static void unsubscribeFromAll(Collection<Map.Entry<String, Subscription>> subscriptions) {
        if (subscriptions == null) {
            return;
        }
        List<Throwable> es = null;
        for (Map.Entry<String, Subscription> s : subscriptions) {
            try {
                s.getValue().unsubscribe();
            } catch (Throwable e) {
                if (es == null) {
                    es = new ArrayList<Throwable>();
                }
                es.add(e);
            }
        }
        Exceptions.throwIfAny(es);
    }

    /**
     * Returns true if this composite is not unsubscribed and contains subscriptions.
     *
     * @return {@code true} if this composite is not unsubscribed and contains subscriptions.
     * @since 1.0.7
     */
    public boolean hasSubscriptions() {
        if (!unsubscribed) {
            synchronized (this) {
                return !unsubscribed && subscriptions != null && !subscriptions.isEmpty();
            }
        }
        return false;
    }

    public boolean hasSubscription(String tag) {
        if (!unsubscribed) {
            synchronized (this) {
                return !unsubscribed && subscriptions != null && subscriptions.containsKey(tag);
            }
        }
        return false;
    }
}
