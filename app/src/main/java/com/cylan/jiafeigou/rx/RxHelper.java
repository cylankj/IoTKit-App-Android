package com.cylan.jiafeigou.rx;

import android.util.Log;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by cylan-hunt on 16-11-11.
 */

public class RxHelper {

    public static class EmptyAction0<T> implements Action1<T> {
        @Override
        public void call(T t) {

        }
    }

    public static class EmptyException implements Action1<java.lang.Throwable> {

        private final String s;

        public EmptyException(String tag) {
            this.s = tag;
        }

        @Override
        public void call(java.lang.Throwable throwable) {
            AppLogger.i(this.s + ": " + throwable.getLocalizedMessage());
        }
    }

    /**
     * 3次尝试,间隔500ms
     */
    private Func1<Observable<? extends Throwable>, Observable<?>> observableFunc1 = new Func1<Observable<? extends Throwable>, Observable<?>>() {
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
                            Log.d("", "");
                            return Observable.timer(500, TimeUnit.MILLISECONDS);
                        }
                    });
        }
    };

    public static class RxException<T1 extends Integer, T2 extends Throwable, R extends Boolean> implements Func2<Integer, Throwable, Boolean> {

        private String prefix = "";

        public RxException(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Boolean call(Integer integer, Throwable throwable) {
            AppLogger.e(prefix + ":" + throwable.getLocalizedMessage());
            return true;
        }
    }

    /**
     * 异常情况下，返回true,将继续订阅
     */
    public static Func2<Integer, Throwable, Boolean> exceptionFun = new Func2<Integer, Throwable, Boolean>() {
        @Override
        public Boolean call(Integer integer, Throwable throwable) {
            if (BuildConfig.DEBUG) {
                throw new IllegalArgumentException(": " + throwable.getLocalizedMessage());
            }
            //此处return true:表示继续订阅，
            AppLogger.e("DpParser: " + throwable.getLocalizedMessage());
            return true;
        }
    };

    /**
     * simple 过滤器
     */
    public static class Filter<T> implements Func1<T, Boolean> {
        private boolean enable;

        public Filter(boolean enable) {
            this.enable = enable;
        }

        @Override
        public Boolean call(Object object) {
            return enable;
        }
    }
}
