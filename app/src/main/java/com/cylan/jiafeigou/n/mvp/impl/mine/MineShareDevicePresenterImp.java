package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareDeviceContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public class MineShareDevicePresenterImp extends AbstractPresenter<MineShareDeviceContract.View> implements MineShareDeviceContract.Presenter {

    private CompositeSubscription subscription;

    public MineShareDevicePresenterImp(MineShareDeviceContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        subscription = new CompositeSubscription();
    }

    public void initShareList() {
        Subscription subscription = Observable.just("initShareList")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(ret -> {
                    List<Device> devices = DataSourceManager.getInstance().getAllDevice();
                    ArrayList<String> cids = new ArrayList<>(devices.size());
                    for (Device device : devices) {
                        cids.add(device.uuid);
                    }
                    BaseApplication.getAppComponent().getCmd().getShareList(cids);
                    return ret;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.GetShareListRsp.class).first())
                .map(ret -> DataSourceManager.getInstance().getShareList())
                .timeout(3, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> getView().showLoadingDialog())
                .doOnTerminate(() -> getView().hideLoadingDialog())
                .subscribe(shareDeviceList -> {
                    getView().onInitShareList(shareDeviceList);
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
        this.subscription.add(subscription);
    }

    @Override
    public void stop() {
        super.stop();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
