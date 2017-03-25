package com.cylan.jiafeigou;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
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
}
