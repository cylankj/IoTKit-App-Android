package com.cylan.jiafeigou.n.mvp.impl.bind;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.bind.SetDeviceAliasContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;

import java.util.concurrent.TimeUnit;

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

    private int count = 0;

    @Override
    public void setupAlias(String alias) {
//        count = 0;
//        Subscription subscribe = Observable.interval(0, 2, TimeUnit.SECONDS)
//                .timeout(5, TimeUnit.SECONDS)//5s超时
//                .takeUntil(aLong -> {
//                    Account account = BaseApplication.getAppComponent().getSourceManager().getAccount();
//                    return account != null && account.isOnline();
//                })
//                .map(s -> alias)
//                .subscribeOn(Schedulers.io())
//                .map((String s) -> {
//                    // TODO: 2017/8/1 现在不过滤空格了
//                    if (s != null && s.length() == 0) return -1;//如果是空格则跳过,显示默认名称
//                    try {
//                        count++;
//                        if (count > 5) {
//                            throw new RxEvent.HelperBreaker("超时了");
//                        }
//                        int ret = BaseApplication.getAppComponent().getCmd().setAliasByCid(uuid, s);
//                        if (NetUtils.getJfgNetType() == 0) throw new RxEvent.HelperBreaker("无网络");
//                        AppLogger.i("setup alias: " + s + ",ret:" + ret);
//                        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
//                        device.setAlias(s);
//                        return ret;
//                    } catch (JfgException e) {
//                        return -1;
//                    }
//                })
//                .flatMap(result -> RxBus.getCacheInstance().toObservable(RxEvent.SetAlias.class)
//                        .flatMap(setAlias -> Observable.just(setAlias != null
//                                && setAlias.result != null
//                                && setAlias.result.code == JError.ErrorOK ? JError.ErrorOK : -1))
//                        .first())
//                .first()
//                .delay(1000, TimeUnit.MILLISECONDS)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe((Integer result) -> {
//                    getView().setupAliasDone(result);
//                }, throwable -> {
//                    if (throwable instanceof TimeoutException) {
//                        getView().setupAliasDone(-1);
//                    }
//                    AppLogger.e(throwable);
//                });

        
        Subscription subscribe = Observable.just("setupAlias")
                .subscribeOn(AndroidSchedulers.mainThread())
                .delay(1, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    if (alias != null && alias.length() == 0) return -1;//如果是空格则跳过,显示默认名称
                    try {
                        count++;
                        if (count > 5) {
                            throw new RxEvent.HelperBreaker("超时了");
                        }
                        int ret = BaseApplication.getAppComponent().getCmd().setAliasByCid(uuid, alias);
                        if (NetUtils.getJfgNetType() == 0) throw new RxEvent.HelperBreaker("无网络");
                        AppLogger.i("setup alias: " + alias + ",ret:" + ret);
                        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
                        device.setAlias(alias);
                        return ret;
                    } catch (JfgException e) {
                        return -1;
                    }
                })
                .flatMap(result -> RxBus.getCacheInstance().toObservable(RxEvent.SetAlias.class))
                .first(setAlias -> setAlias != null && setAlias.result != null && setAlias.result.code == JError.ErrorOK)
                .timeout(10, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(result -> {
                    getView().setupAliasDone(result == null ? -1 : JError.ErrorOK);
                }, e -> {
                    getView().setupAliasDone(-1);
                    AppLogger.e(MiscUtils.getErr(e));
                });


        addSubscription(subscribe, "setupAlias");
    }
}
