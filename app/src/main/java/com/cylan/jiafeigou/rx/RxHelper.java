package com.cylan.jiafeigou.rx;

import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-11-11.
 */

public class RxHelper {

    private static final String TAG = ":";

    public static class EmptyAction0<T> implements Action1<T> {
        @Override
        public void call(T t) {

        }
    }

    public static class EmptyException implements Action1<Throwable> {

        private final String s;

        public EmptyException(String tag) {
            this.s = tag;
        }

        @Override
        public void call(Throwable throwable) {
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
            if (throwable != null) {
                StringBuilder builder = new StringBuilder();
                builder.append(prefix);
                builder.append(TAG);
                builder.append(throwable.getLocalizedMessage());
                builder.append(TAG);
                builder.append(throwable.getClass());
                AppLogger.e(builder.toString());
            }
            return true;
        }
    }


    public static final class ExceptionFun<Integer, Throwable, Boolean> implements Func2<java.lang.Integer, java.lang.Throwable, java.lang.Boolean> {

        public ExceptionFun(String tag) {
            this.tag = tag;
        }

        private String tag;

        @Override
        public java.lang.Boolean call(java.lang.Integer integer, java.lang.Throwable throwable) {
            //此处return true:表示继续订阅，
            AppLogger.e(tag + throwable.getLocalizedMessage());
//            if (BuildConfig.DEBUG) {
//                throw new IllegalArgumentException(": " + throwable.getLocalizedMessage());
//            }
            return true;
        }
    }

    /**
     * simple 过滤器
     */
    public static class Filter<T> implements Func1<T, Boolean> {
        private boolean enable;

        private String tag;

        public Filter(String tag, boolean enable) {
            this.enable = enable;
            this.tag = tag;
        }

        @Override
        public Boolean call(Object object) {
            AppLogger.i(tag + ":" + enable);
            return enable;
        }
    }

    public static Observable<BeanCamInfo> filter(final BeanCamInfo beanCamInfo) {
        return RxBus.getCacheInstance().toObservableSticky(RxUiEvent.BulkDeviceListRsp.class)
                .subscribeOn(Schedulers.computation())
                .filter((RxUiEvent.BulkDeviceListRsp list) ->
                        (list != null
                                && list.allDevices != null
                                && beanCamInfo != null
                                && beanCamInfo.deviceBase != null))
                .flatMap(new Func1<RxUiEvent.BulkDeviceListRsp, Observable<DpMsgDefine.DpWrap>>() {
                    @Override
                    public Observable<DpMsgDefine.DpWrap> call(RxUiEvent.BulkDeviceListRsp list) {
                        for (DpMsgDefine.DpWrap wrap : list.allDevices) {
                            if (wrap.baseDpDevice != null
                                    && TextUtils.equals(wrap.baseDpDevice.uuid, beanCamInfo.deviceBase.uuid)) {
                                return Observable.just(wrap);
                            }
                        }
                        return null;
                    }
                })
                .filter((DpMsgDefine.DpWrap dpWrap) ->
                        (dpWrap != null && dpWrap.baseDpDevice != null))
                .map((DpMsgDefine.DpWrap dpWrap) -> {
                    BeanCamInfo info = new BeanCamInfo();
                    info.convert(dpWrap.baseDpDevice, dpWrap.baseDpMsgList);
                    return info;
                });
    }
}
