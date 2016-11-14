package com.cylan.jiafeigou.support.rxbus;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.MyTestRunner;
import com.cylan.utils.RandomUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-11-10.
 */
@RunWith(MyTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class RxTest {


    @Test
    public void what() {
        Observable.interval(1000, RandomUtils.getRandom(1000, 2000), TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        System.out.println("what: " + System.currentTimeMillis());
                    }
                });
    }


    @Test
    public void testRetryWhen() {
        Func1<Observable<? extends Throwable>, Observable<?>> observableFunc1
                = new Func1<Observable<? extends Throwable>, Observable<?>>() {
            @Override
            public Observable<?> call(Observable<? extends Throwable> errors) {

                return errors.zipWith(Observable.range(1, 3),
                        new Func2<Throwable, Integer, Integer>() {
                            @Override
                            public Integer call(Throwable throwable, Integer i) {
                                return i;
                            }
                        })
                        .flatMap(new Func1<Integer, Observable<? extends Long>>() {
                            @Override
                            public Observable<? extends Long> call(Integer retryCount) {
                                System.out.println("retryCount: " + retryCount);
                                return Observable.timer(500, TimeUnit.MILLISECONDS);
                            }
                        });
            }
        };
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                final int r = RandomUtils.getRandom(1, 50);
                System.out.println("random: " + r);
                if (r < 30) {
                    System.out.println("com");
                    subscriber.onCompleted();
                    return;
                }
                subscriber.onError(new RuntimeException("always failed"));
            }
        }).retryWhen(observableFunc1).toBlocking().forEach(new Action1<Object>() {
            @Override
            public void call(Object o) {
                System.out.println("what: " + o);
            }
        });
    }

    @Test
    public void testPing() {
        Observable.interval(1000, 2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .map(new Func1<Long, Object>() {
                    @Override
                    public Object call(Long aLong) {
                        System.out.println("good: " + aLong / RandomUtils.getRandom(3));
                        return null;
                    }
                })
                .retry(new Func2<Integer, Throwable, Boolean>() {
                    @Override
                    public Boolean call(Integer integer, Throwable throwable) {
                        System.out.println("what: " + integer);
                        return true;
                    }
                })
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        System.out.println("call: ");
                    }
                });
//                .timeout(500, TimeUnit.MILLISECONDS)
    }

    @Test
    public void testTimeout() {
        System.out.println("f");
        Observable.just(null)
                .delay(2, TimeUnit.SECONDS)
                .map(new Func1<Object, Object>() {
                    @Override
                    public Object call(Object o) {
                        System.out.println("go");
                        return null;
                    }
                })
                .timeout(1, TimeUnit.SECONDS, Observable.just(null)
                        .map(new Func1<Object, Object>() {
                            @Override
                            public Object call(Object o) {
                                System.out.println("timeout");
                                return null;
                            }
                        }))
                .subscribe();
    }
}
