package com.cylan.jiafeigou;

import android.util.Log;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by cylan-hunt on 17-2-17.
 */

public class RxJavaTest {
    @Test
    public void testRetryWhen() {

        Observable.just("")
                .flatMap(new Func1<String, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(String s) {
                        ArrayList<Integer> list = new ArrayList<Integer>();
                        for (int i = 0; i < 15; i++) {
                            list.add(i);
                        }
                        return Observable.from(list);
                    }
                })
                .flatMap(new Func1<Integer, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(Integer integer) {
                        return Observable.just(integer % 2 == 0 ? null : integer);
                    }
                })
                .toList()
                .map(new Func1<List<Integer>, Object>() {
                    @Override
                    public Object call(List<Integer> list) {
                        System.out.println(list);
                        return null;
                    }
                })
                .toList()

                .subscribe();

//        final AtomicInteger atomicInteger = new AtomicInteger(3);
//        Observable.create(new Observable.OnSubscribe<String>() {
//            @Override
//            public void call(Subscriber<? super String> subscriber) {
//                subscriber.onNext(String.valueOf(System.currentTimeMillis()));
//                subscriber.onError(new Error(String.valueOf(atomicInteger.decrementAndGet())));
//            }
//        }).retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
//            @Override
//            public Observable<?> call(Observable<? extends Throwable> observable) {
//                return observable.takeWhile(new Func1<Throwable, Boolean>() {
//                    @Override
//                    public Boolean call(Throwable throwable) {
//                        return Integer.parseInt(throwable.getMessage()) > 0;
//                    }
//                }).flatMap(new Func1<Throwable, Observable<?>>() {
//                    @Override
//                    public Observable<?> call(Throwable throwable) {
//                        return Observable.timer(1, TimeUnit.SECONDS);
//                    }
//                });
//            }
//        }).subscribe(new TestSubscriber<String>());
//        Assert.assertEquals(atomicInteger.intValue(), 0);
    }

    private long timeTick;
    private Subscription subscription;

    @Test
    public void testInterval() throws InterruptedException {
        timeTick = System.currentTimeMillis();
        subscription = Observable.interval(1, TimeUnit.SECONDS)
                .filter(new Func1<Long, Boolean>() {
                    @Override
                    public Boolean call(Long aLong) {
                        System.out.println("filter");
                        boolean r = System.currentTimeMillis() - timeTick > 5 * 1000L;
//                        if (r) throw new IllegalArgumentException("time out");
                        if (r) subscription.unsubscribe();
                        return true;
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        System.out.println("complete?");
                    }
                })
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        System.out.println("finish");
                    }
                }, throwable -> System.out.println("what"));
        Thread.sleep(20 * 1000L);
    }
}
