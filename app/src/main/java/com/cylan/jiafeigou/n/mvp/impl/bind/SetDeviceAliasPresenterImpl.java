package com.cylan.jiafeigou.n.mvp.impl.bind;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bind.SetDeviceAliasContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-11-29.
 */

public class SetDeviceAliasPresenterImpl extends AbstractPresenter<SetDeviceAliasContract.View>
        implements SetDeviceAliasContract.Presenter {
    private String uuid;

    public SetDeviceAliasPresenterImpl(SetDeviceAliasContract.View view,
                                       String uuid) {
        super(view);
        view.setPresenter(this);
        this.uuid = uuid;
    }


    @Override
    public void setupAlias(String alias) {
        Subscription subscribe = Observable.interval(0, 2, TimeUnit.SECONDS)
                .takeUntil(aLong -> {
                    Account account = DataSourceManager.getInstance().getAJFGAccount();
                    return account != null && account.isOnline();
                })
                .map(s -> alias)
                .subscribeOn(Schedulers.newThread())
                .map((String s) -> {
                    if (s != null && s.trim().length() == 0) return -1;//如果是空格则跳过,显示默认名称
                    try {
                        int ret = JfgCmdInsurance.getCmd().setAliasByCid(uuid, s);
                        AppLogger.i("setup alias: " + s + ",ret:" + ret);
                        return ret;
                    } catch (JfgException e) {
                        return -1;
                    }
                })
                .timeout(3, TimeUnit.SECONDS)
                .flatMap(result -> RxBus.getCacheInstance().toObservable(RxEvent.SetAlias.class)
                        .flatMap(setAlias -> Observable.just(setAlias != null
                                && setAlias.result != null
                                && setAlias.result.code == JError.ErrorOK ? JError.ErrorOK : -1)))
                .delay(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Integer result) -> {
                    getView().setupAliasDone(result);
                }, throwable -> {
                    if (throwable instanceof TimeoutException) {
                        getView().setupAliasDone(-1);
                    }
                    AppLogger.e(throwable);
                });
        addSubscription(subscribe, "setupAlias");
    }
}
