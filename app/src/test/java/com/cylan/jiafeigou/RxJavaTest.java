package com.cylan.jiafeigou;

import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.log.AppLogger;

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
    private Subscription sub = null;

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

    @Test
    public void testCompute() {
        boolean good = false;
        for (int i = 0; i < 10; i++) {
            good |= i % 2 == 0;
        }
        System.out.println(good);

        System.out.println((10 + "%"));
    }

    Subscription subscription;

    @Test
    public void testCompose() throws InterruptedException {
        System.out.println("compose");
        Observable.create(subscriber -> subscription = RxBus.getCacheInstance()
                .toObservable(String.class)
                .timeout(1, TimeUnit.SECONDS, Observable.just("so?"))
                .filter(ret -> ret.startsWith("111"))
                .timeout(1, TimeUnit.SECONDS, Observable.just("what?"))
                .subscribe(ret -> {
                    subscriber.onNext(ret);
                    if (ret.endsWith("222")) {
                        subscriber.onCompleted();
                        if (subscription != null) subscription.unsubscribe();
                    }
                }, throwable -> {
                    subscriber.onError(throwable);
                    if (subscription != null) subscription.unsubscribe();
                })).subscribe(ret -> System.out.println("result?" + ret),
                throwable -> System.out.println("err"));
        Thread.sleep(900);
        RxBus.getCacheInstance().post("0000");
        RxBus.getCacheInstance().post("111");
        Thread.sleep(900);
        RxBus.getCacheInstance().post("1112222");
        RxBus.getCacheInstance().post("111000");
        System.out.println("subscription: " + subscription.isUnsubscribed());
        Thread.sleep(1000);
    }

    @Test
    public void testTask() {
    }

    private static class Task implements Func1<String, Task> {

        @Override
        public Task call(String s) {
            System.out.println("what");
            return this;
        }
    }

    /**
     * a static task
     */
    protected <T, R> void enqueueTask(ITask<T, R> func1) {
    }

    private interface ITask<T, R> extends Func1<T, R> {
        void taskStart();

        void taskErr(Throwable throwable);

        void taskSuccess(R r);
    }

    @Test
    public void testNull(){
        Observable.create(subscriber -> {
            try {
                subscriber.onNext(null);
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribe(ret->{
            System.out.println("result:"+ret);
        }, AppLogger::e);


    }
}
