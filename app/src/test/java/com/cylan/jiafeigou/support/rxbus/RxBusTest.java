package com.cylan.jiafeigou.support.rxbus;

import org.junit.Test;
import org.mockito.Mockito;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by cylan-hunt on 16-10-31.
 */
public class RxBusTest {
    @Test
    public void getDefault() throws Exception {
        RxBus rxBus = RxBus.getDefault();
        assertNotNull(rxBus);
    }

    @Test
    public void post() throws Exception {
        RxBus rxBus = RxBus.getDefault();
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
        RxBus rxBus = RxBus.getDefault();
        Observable<Integer> observable = rxBus.toObservable(Integer.class);
        assertTrue(observable != null);
    }

    @Test
    public void hasObservers() throws Exception {
        RxBus rxBus = RxBus.getDefault();
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
        RxBus rxBus = RxBus.getDefault();
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
        RxBus rxBus = RxBus.getDefault();
        rxBus.toObservable(Integer.class)
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer) {
                        System.out.println("what integer: " + 10 / integer);
                        return 10 / integer;
                    }
                })
                .retry()
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer s) {
//                        try {
                        System.out.println("what int: " + s);
//                        } catch (Throwable e) {
//                            System.out.println("err: " + e.getLocalizedMessage());
//                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        System.out.println("...." + throwable.getLocalizedMessage());
                    }
                });
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

}