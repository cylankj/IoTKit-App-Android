package com.cylan.jiafeigou.support.rxbus;

import android.os.SystemClock;

import com.cylan.jiafeigou.rx.RxBus;

import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.util.Pair;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by cylan-hunt on 16-10-31.
 */
public class RxBusTest {
    @Test
    public void getDefault() throws Exception {
        RxBus rxBus = RxBus.getCacheInstance();
        assertNotNull(rxBus);
    }

    @Test
    public void post() {
        RxBus rxBus = RxBus.getCacheInstance();
        rxBus.toObservable(String.class)
                .subscribeOn(Schedulers.newThread())
                .delay(3, TimeUnit.SECONDS)
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        System.out.println("testPost: " + s + " " + System.currentTimeMillis());
                        return null;
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        System.out.println("" + s);
                    }
                });
        rxBus.toObservable(Integer.class)
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        System.out.println("阻塞");
                    }
                });
        System.out.println("post: " + System.currentTimeMillis());
        rxBus.post("test Post");
        rxBus.post(20);
//        Thread.sleep(6000);
        rxBus.post("一次 Post");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void toObservable() throws Exception {
        RxBus rxBus = RxBus.getCacheInstance();
        Observable<Integer> observable = rxBus.toObservable(Integer.class);
        assertTrue(observable != null);
    }

    @Test
    public void hasObservers() throws Exception {
        RxBus rxBus = RxBus.getCacheInstance();
//        assertTrue(rxBus.hasObservers());
        rxBus.toObservable(String.class)
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        System.out.println("testPost: " + s);
                    }
                });
        rxBus.post("test Post");
        assertTrue("hasObservers", rxBus.hasObservers());
    }

    @Test
    public void reset() throws Exception {
        RxBus.getCacheInstance().toObservable(String.class)
                .timeout(1000, TimeUnit.MILLISECONDS, Observable.just("")
                        .map(new Func1<String, String>() {
                            @Override
                            public String call(String s) {
                                System.out.println("time out? " + s);
                                return null;
                            }
                        }))
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        System.out.println("zheli : " + s);
                    }
                });
        RxBus.getCacheInstance().toObservable(String.class)
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        System.out.println("zheli : " + s);
                    }
                });
        RxBus.getCacheInstance().post("before");
        Thread.sleep(2000);
        RxBus.getCacheInstance().post("nihao");
    }

    @Test
    public void postSticky() throws Exception {
        RxBus rxBus = RxBus.getCacheInstance();
        rxBus.postSticky("this is sticky event");
        rxBus.toObservableSticky(String.class)
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        System.out.println("testPost: " + s);
                        assertNotNull("sticky event is not null? ", s == null);
                    }
                });
    }

    @Test
    public void toObservableSticky() throws Exception {
        RxBus rx = Mockito.mock(RxBus.class);
        Mockito.when(rx.toObservableSticky(String.class))
                .thenReturn(Observable.just("good"));
        rx.postSticky("this is sticky event");
        rx.toObservableSticky(String.class)
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        System.out.println("testPost: " + s);
                        assertNotNull("sticky event is not null? ", s == null);
                    }
                });
    }

    @Test
    public void testException() {
        RxBus rxBus = RxBus.getCacheInstance();
        rxBus.toObservable(Integer.class)
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer) {
                        if (integer == 0)
                            throw new IllegalArgumentException("bifaadf");
                        System.out.println("compute: " + 10 / integer);
                        return 10 / integer;
                    }
                })
                .retry(new Func2<Integer, Throwable, Boolean>() {
                    @Override
                    public Boolean call(Integer integer, Throwable throwable) {
                        //记录消息
                        System.out.println("thr: " + integer + " " + throwable.getLocalizedMessage());
                        return true;
                    }
                })
                .subscribe();
        rxBus.post(5);
        rxBus.post(0);
        rxBus.post(5);
    }

    @Test
    public void getStickyEvent() throws Exception {

    }

    @Test
    public void removeStickyEvent() throws Exception {

    }

    @Test
    public void removeAllStickyEvents() throws Exception {

    }

    @Test
    public void testThrottle() throws InterruptedException {
        RxBus.getCacheInstance().toObservable(String.class)
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        System.out.println("result: " + s);
                    }
                });
        RxBus.getCacheInstance().post("ni hao");
        Thread.sleep(2000);
    }

    @Test
    public void testZipWith() throws InterruptedException {
        RxBus.getCacheInstance().toObservable(String.class)
                .zipWith(RxBus.getCacheInstance().toObservable(Integer.class)
                        .map(new Func1<Integer, Integer>() {
                            @Override
                            public Integer call(Integer integer) {
                                System.out.println("what: " + integer);
                                return integer;
                            }
                        }), new Func2<String, Integer, Pair<Integer, String>>() {
                    @Override
                    public Pair<Integer, String> call(String s, Integer integer) {
                        return new Pair<>(integer, s);
                    }
                })
                .subscribe(new Action1<Pair<Integer, String>>() {
                    @Override
                    public void call(Pair<Integer, String> integerStringPair) {
                        System.out.println("pair: " + integerStringPair.first + " ..." + integerStringPair.second);
                    }
                });
        RxBus.getCacheInstance().post("4nihao");
        RxBus.getCacheInstance().post(5);
        RxBus.getCacheInstance().post(15);
        RxBus.getCacheInstance().post(25);
        RxBus.getCacheInstance().post(35);
        RxBus.getCacheInstance().post(45);
        Thread.sleep(2000);
    }

    @Test
    public void testBlock() {
        RxBus.getCacheInstance().toObservable(String.class)
                .zipWith(RxBus.getCacheInstance().toObservable(Integer.class)
                        .map(new Func1<Integer, Integer>() {
                            @Override
                            public Integer call(Integer integer) {
                                System.out.println("what: " + integer);
                                return integer;
                            }
                        }), new Func2<String, Integer, Pair<Integer, String>>() {
                    @Override
                    public Pair<Integer, String> call(String s, Integer integer) {
                        return new Pair<>(integer, s);
                    }
                })
                .subscribe(new Action1<Pair<Integer, String>>() {
                    @Override
                    public void call(Pair<Integer, String> integerStringPair) {
                        System.out.println("pair: " + integerStringPair.first + " ..." + integerStringPair.second);
                    }
                });
        RxBus.getCacheInstance().toObservable(Long.class)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        System.out.println("long:" + aLong);
                    }
                });
        RxBus.getCacheInstance().post("4nihao");
        RxBus.getCacheInstance().post(5L);
//        RxBus.getCacheInstance().post(5);
//        RxBus.getCacheInstance().post(15);
//        RxBus.getCacheInstance().post(25);
//        RxBus.getCacheInstance().post(35);
//        RxBus.getCacheInstance().post(45);
        SystemClock.sleep(2000);
    }
}