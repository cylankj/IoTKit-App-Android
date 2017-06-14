package com.cylan.jiafeigou.support.rxbus;

import android.os.SystemClock;

import com.cylan.jiafeigou.rx.RxBus;

import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.util.Pair;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
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
                                System.out.println("startTime out? " + s);
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

    @Test
    public void testTimeout() throws InterruptedException {
        getString().flatMap((String s) ->
                RxBus.getCacheInstance().toObservable(String.class)
                        .filter((String string) -> {
                            System.out.println("get string: " + string);
                            return string.equals("good");
                        }))
                .timeout(800, TimeUnit.MILLISECONDS, Observable.just(1)
                        .map(new Func1<Integer, String>() {
                            @Override
                            public String call(Integer integer) {
                                System.out.println("timeout");
                                return "timeout";
                            }
                        }))
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        RxBus.getCacheInstance().hasObservers();
                        System.out.println("finish: " + s);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                        System.out.println("err: " + throwable.getLocalizedMessage());
                    }
                });
        Thread.sleep(500);
        Thread.sleep(500);
        RxBus.getCacheInstance().post("budui");
        RxBus.getCacheInstance().post("zailai");
        RxBus.getCacheInstance().post("nihao");
    }

    private Observable<String> getString() {
        return Observable.just("nihao")
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        return s;
                    }
                });
    }


    @Test
    public void testZip() {
        check().zipWith(RxBus.getCacheInstance().toObservable(String.class)
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        System.out.println("what: " + s);
                        return null;
                    }
                }), new Func2<Boolean, String, Object>() {
            @Override
            public Object call(Boolean aBoolean, String s) {
                System.out.println("good: " + aBoolean);
                return null;
            }
        }).subscribe();
    }

    private Observable<Boolean> check() {
        return Observable.just(true)
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        int random = new Random().nextInt(20) % 2;
                        System.out.println("random: " + random);
                        return false;
                    }
                });
    }

    @Test
    public void testUnSubscribeSelf() throws InterruptedException {
        Observable<String> what = RxBus.getCacheInstance().toObservable(String.class)
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        return s;
                    }
                }).filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        System.out.println("get: " + s);
                        return s.equals("sss");
                    }
                });

        Subscription subscription = Observable.create(new Observable.OnSubscribe<Subscriber>() {
            @Override
            public void call(Subscriber<? super Subscriber> subscriber) {
                System.out.println("initSubscription");
                subscriber.onNext(subscriber);
                subscriber.onCompleted();
            }
        }).zipWith(what, new Func2<Subscriber, String, Object>() {
            @Override
            public Object call(Subscriber subscriber, String s) {
                System.out.println("zip? " + s);
                if (s.equals("sss")) {

                    System.out.println("unsu");
                }
                return null;
            }
        }).subscribe();
        RxBus.getCacheInstance().post("ddd");
        RxBus.getCacheInstance().post("sss");
        RxBus.getCacheInstance().post("ddd");
        RxBus.getCacheInstance().post("saaass");
        System.out.println("? " + (subscription.isUnsubscribed()));
        Thread.sleep(500);
        System.out.println("? " + (subscription.isUnsubscribed()));
        System.out.println("result: " + (subscription.isUnsubscribed()));
        RxBus.getCacheInstance().post("next");
        RxBus.getCacheInstance().post("next");
        RxBus.getCacheInstance().post("next");
    }

    @Test
    public void testThread() throws InterruptedException {
        Observable.just("run")
                .flatMap(new Func1<String, Observable<?>>() {
                    @Override
                    public Observable<?> call(String s) {
                        return RxBus.getCacheInstance().toObservable(String.class)
                                .map(new Func1<String, Object>() {
                                    @Override
                                    public Object call(String s) {
                                        System.out.println("what: " + Thread.currentThread());
                                        return null;
                                    }
                                });
                    }
                })
                .subscribe();
        ArrayList<String> list = new ArrayList<>();
        Observable.from(list)
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        System.out.println(s);
                        return s;
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        System.out.println("");
                    }
                });
        RxBus.getCacheInstance().post("googd");
        RxBus.getCacheInstance().post("googd");
        Thread.sleep(200);
    }


    @Test
    public void complete() throws InterruptedException {

        long time = System.currentTimeMillis();

        System.out.println(time / 1000);
        System.out.println(time / 1000 + 10 + 1);
        Observable.just("go")
                .timeout(2, TimeUnit.SECONDS)
                .map(ret -> {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return ret;
                })

                .subscribe(ret -> {
                    System.out.println(ret);
                }, throwable -> System.out.println(throwable));
        Thread.sleep(5000);

        RxBus.getCacheInstance().post("finish0");
//        Thread.sleep(10);
        RxBus.getCacheInstance().post("finish1");
//        Thread.sleep(10);
        RxBus.getCacheInstance().post("finish2");
//        Thread.sleep(10);
        RxBus.getCacheInstance().post("finish3");
        Thread.sleep(10);
        RxBus.getCacheInstance().post("finish4");
        Thread.sleep(10);
        RxBus.getCacheInstance().post("finish5");
        Thread.sleep(10);
        RxBus.getCacheInstance().post("finish6");
        Thread.sleep(3000);
    }

    private int count;

    @Test
    public void testTimeout1() throws InterruptedException {
        Observable.interval(20, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        System.out.println("..."+aLong);
                    }
                });
//        Observable.just(Observable.interval(20, TimeUnit.MILLISECONDS))
//                .takeUntil(longObservable -> {
//                    count++;
//                    return count > 10;
//                })
//                .flatMap((Func1<Observable<Long>, Observable<?>>) longObservable -> {
//                    System.out.println("good: " + count);
//                    return longObservable;
//                })
//                .subscribe(System.out::println);
        RxBus.getCacheInstance().post("1110000");
        RxBus.getCacheInstance().post("111");
        Thread.sleep(500);
//        subscription.unsubscribe();
        RxBus.getCacheInstance().post(new TT(10));
        RxBus.getCacheInstance().post("000dffa");
        RxBus.getCacheInstance().post("000erter");
        RxBus.getCacheInstance().post("000erte11r");
        Thread.sleep(1000);

        String text = "私は720°パノラマカムで何か涼しいものを作った、来て＆それを参照してください！";

        for (int i = 0; i < text.length(); i++)
            System.out.println(text.charAt(i));

    }

    public static final class TT {
        public int ret;

        public TT(int ret) {
            this.ret = ret;
        }
    }


}