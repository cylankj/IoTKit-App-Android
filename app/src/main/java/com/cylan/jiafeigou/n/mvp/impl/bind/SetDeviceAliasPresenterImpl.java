package com.cylan.jiafeigou.n.mvp.impl.bind;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bind.SetDeviceAliasContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

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
        Subscription subscription =
                Observable.interval(0, 2, TimeUnit.SECONDS)
                        .takeUntil(aLong -> {
                            Account account = DataSourceManager.getInstance().getAJFGAccount();
                            return account != null && account.isOnline();
                        })
                        .map(s -> alias)
                        .subscribeOn(Schedulers.newThread())
                        .map((String s) -> {
                            try {
                                JfgCmdInsurance.getCmd().setAliasByCid(uuid, s);
                                AppLogger.i("setup alias: " + s);
                            } catch (JfgException e) {
                                e.printStackTrace();
                            }
                            return s;
                        })
                        .delay(1000, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((String s) -> {
                            getView().setupAliasDone();
                        });
        addSubscription(subscription, "setupAlias");
    }
}
