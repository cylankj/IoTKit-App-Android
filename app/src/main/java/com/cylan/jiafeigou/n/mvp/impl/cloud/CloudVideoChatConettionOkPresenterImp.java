package com.cylan.jiafeigou.n.mvp.impl.cloud;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.cylan.jiafeigou.ICloudLiveService;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.engine.CloudLiveService;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudVideoChatConettionOkContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.rxbus.RxBus;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public class CloudVideoChatConettionOkPresenterImp extends AbstractPresenter<CloudVideoChatConettionOkContract.View> implements CloudVideoChatConettionOkContract.Presenter {

    private Subscription loadVideoSub;
    private Subscription loadProAnimSub;
    private int loadNum = 0;


    public CloudVideoChatConettionOkPresenterImp(CloudVideoChatConettionOkContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        loadVideo();
    }

    @Override
    public void stop() {
        if (loadVideoSub != null) {
            loadVideoSub.unsubscribe();
        }

        if (loadProAnimSub != null) {
            loadProAnimSub.unsubscribe();
        }
        }

    @Override
    public void loadVideo() {
        showLoadProgressAnim();
        loadVideoSub = Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .delay(5000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        getView().showLoadResult();
                    }
                })
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        getView().hideLoadingView();
                        if (loadProAnimSub != null) {
                            loadProAnimSub.unsubscribe();
                        }
                    }
                });
    }

    @Override
    public void handlerHangUp(String time) {
        RxBus.getInstance().send(new RxEvent.HangUpVideoTalk(true,time));
        }

    public void showLoadProgressAnim() {
        loadProAnimSub = Observable.interval(500, 300, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        String[] loadContext = {".", "..", "...",};
                        getView().showLoadingView();
                        getView().setLoadingText(loadContext[loadNum++]);
                        if (loadNum == 3) {
                            loadNum = 0;
                        }
                    }
                });
    }

}
