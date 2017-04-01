package com.cylan.jiafeigou.n.mvp.impl.bind;

import com.cylan.ex.JfgException;
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
        addSubscription(Observable.just(alias)
                .map(device1 -> {
                    try {
                        JfgCmdInsurance.getCmd().setAliasByCid(uuid, alias);
                        AppLogger.d("update alias suc");
                    } catch (JfgException e) {
                        AppLogger.e("");
                    }
                    return null;
                })
                .timeout(10, TimeUnit.SECONDS)
                .flatMap(dev -> RxBus.getCacheInstance().toObservable(RxEvent.SetAlias.class))
                .filter(s -> getView() != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(setAlias -> getView().setupAliasDone(0),
                        throwable -> {
                            if (throwable instanceof TimeoutException) {
                                mView.setupAliasDone(-1);
                            }
                            AppLogger.e("err: " + throwable.getLocalizedMessage());
                        }));
    }
}
