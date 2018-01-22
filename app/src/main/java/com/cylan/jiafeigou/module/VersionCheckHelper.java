package com.cylan.jiafeigou.module;

import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/12/20.
 */

public class VersionCheckHelper {


    public static String getCurrentVersion(String uuid) {

        return "";
    }

    interface VersionCheckCallback {

    }

    public static Observable<RxEvent.VersionRsp> checkNewVersion(String cid) {
        return Observable.create(new Observable.OnSubscribe<RxEvent.VersionRsp>() {
            @Override
            public void call(Subscriber<? super RxEvent.VersionRsp> subscriber) {
                Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.VersionRsp.class)
                        .filter(new Func1<RxEvent.VersionRsp, Boolean>() {
                            @Override
                            public Boolean call(RxEvent.VersionRsp versionRsp) {
                                return TextUtils.equals(versionRsp.getUuid(), cid);
                            }
                        })
                        .subscribe(new Action1<RxEvent.VersionRsp>() {
                            @Override
                            public void call(RxEvent.VersionRsp versionRsp) {
                                subscriber.onNext(versionRsp);
                                subscriber.onCompleted();
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                subscriber.onError(throwable);
                            }
                        });
                subscriber.add(subscribe);
                try {
                    Command.getInstance().CheckTagDeviceVersion(cid);
                } catch (JfgException e) {
                    e.printStackTrace();
                }
            }
        })
                .subscribeOn(Schedulers.io());
    }

}
