package com.cylan.jiafeigou.n.mvp.impl.bind;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.bind.SnContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractFragmentPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by hds on 17-7-12.
 */

public class SnPresenter extends AbstractFragmentPresenter<SnContract.View> implements SnContract.Presenter {

    public SnPresenter(SnContract.View view) {
        super(view);
    }

    @Override
    public void getPid(String sn) {
        Subscription subscription = Observable.just(sn)
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    try {
                        byte[] data = DpUtils.pack(sn);
                        return BaseApplication.getAppComponent().getCmd().sendUniservalDataSeq(1, data);
                    } catch (JfgException e) {
                        unSubscribe("getPid");
                        return -1L;
                    }
                })
                .flatMap(aLong -> RxBus.getCacheInstance().toObservable(RxEvent.UniversalDataRsp.class)
                        .filter(ret -> ret.seq == aLong && ret.data != null)
                        .timeout(3, TimeUnit.SECONDS))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    int pid = DpUtils.unpackDataWithoutThrow(ret.data, int.class, -1);
                    mView.getPidRsp(JError.ErrorOK, pid);
                    throw new RxEvent.HelperBreaker("手动结束");
                }, throwable -> {
                    if (throwable instanceof TimeoutException) {
                        mView.getPidRsp(-1, -1);
                    }
                });
        addSubscription(subscription, "getPid");
    }
}
