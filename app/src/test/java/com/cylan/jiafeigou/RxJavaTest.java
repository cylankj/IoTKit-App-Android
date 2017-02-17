package com.cylan.jiafeigou;

import com.cylan.jiafeigou.rx.RxBus;

import org.junit.Test;

/**
 * Created by cylan-hunt on 17-2-17.
 */

public class RxJavaTest {
    @Test
    public void testRetryWhen() {

        RxBus.getCacheInstance().post("nihao");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RxBus.getCacheInstance().post("men");

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
