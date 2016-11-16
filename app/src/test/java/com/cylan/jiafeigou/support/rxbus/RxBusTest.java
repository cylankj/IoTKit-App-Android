package com.cylan.jiafeigou.support.rxbus;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.MyTestRunner;
import com.cylan.jiafeigou.rx.RxBus;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.annotation.Config;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
@RunWith(MyTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class RxBusTest {
    @Test
    public void getDefault() throws Exception {
        RxBus rxBus = RxBus.getCacheInstance();
        assertNotNull(rxBus);
    }

    @Test
    public void post() throws Exception {
        RxBus rxBus = RxBus.getCacheInstance();
        rxBus.toObservable(String.class)
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        System.out.println("testPost: " + s);
                    }
                });
        rxBus.post("test Post");
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
                .filter(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        return integer != null && integer != 0;
                    }
                })
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
                        System.out.println("thr: " + integer + " " + throwable.getMessage());
                        return throwable instanceof TimeoutException;
                    }
                })
                .subscribe();
        rxBus.post(5);
        rxBus.post(5);
        rxBus.post(0);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
    public void testTimeout() {
        RxBus.getCacheInstance().toObservable(String.class)
                .subscribeOn(Schedulers.immediate())
                .map(new Func1<String, Object>() {
                    @Override
                    public Object call(String s) {
                        System.out.println("what: " + s);
                        return null;
                    }
                })
                .timeout(1, TimeUnit.SECONDS, Observable.just(null)
                        .map(new Func1<Object, String>() {
                            @Override
                            public String call(Object o) {
                                System.out.println("timeout:");
                                return null;
                            }
                        }))
                .subscribe();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RxBus.getCacheInstance().post("nihao");

    }

    @Test
    public void testZip() {
        Observable.zip(getInt(), getString(), new Func2<Integer, String, String>() {
            @Override
            public String call(Integer integer, String s) {
                return integer + "..." + s;
            }
        }).map(new Func1<String, String>() {
            @Override
            public String call(String o) {
                System.out.println("what: " + o);
                return null;
            }
        }).timeout(2, TimeUnit.SECONDS, Observable.just("")
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        System.out.println("timeout' ");
                        return null;
                    }
                }))
                .subscribe();

        RxBus.getCacheInstance().post(1);
        RxBus.getCacheInstance().post("ni");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RxBus.getCacheInstance().post("ni...");
    }

    private Observable<Integer> getInt() {
        return RxBus.getCacheInstance().toObservable(Integer.class)
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer) {
                        System.out.println("get? " + integer);
                        return integer;
                    }
                });
    }

    private Observable<String> getString() {
        return RxBus.getCacheInstance().toObservable(String.class)
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String string) {
                        System.out.println("get string? " + string);
                        return string;
                    }
                });
    }
}