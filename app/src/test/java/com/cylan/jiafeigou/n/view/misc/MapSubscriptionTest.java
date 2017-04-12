package com.cylan.jiafeigou.n.view.misc;

import com.cylan.jiafeigou.support.log.AppLogger;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by hds on 17-4-5.
 */
public class MapSubscriptionTest {


    @Test
    public void isUnsubscribed() throws Exception {

    }

    @Test
    public void add() throws Exception {
        MapSubscription mapSubscription = new MapSubscription();
        mapSubscription.add(getTag(), "getTag");
        mapSubscription.add(getTag(), "getTag");
        Thread.sleep(50000);
    }

    private Subscription getTag() {
        return Observable.just("good")
                .delay(3, TimeUnit.SECONDS)
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        System.out.println("what");
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                },e-> AppLogger.d(e.getMessage()));
    }

    @Test
    public void remove() throws Exception {

    }

    @Test
    public void clear() throws Exception {

    }

    @Test
    public void unsubscribe() throws Exception {

    }

    @Test
    public void hasSubscriptions() throws Exception {

    }

}