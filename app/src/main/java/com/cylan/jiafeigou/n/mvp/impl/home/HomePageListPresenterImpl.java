package com.cylan.jiafeigou.n.mvp.impl.home;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.jiafeigou.cache.JCache;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.misc.br.TimeTickBroadcast;
import com.cylan.jiafeigou.n.mvp.contract.home.HomePageListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.GreetBean;
import com.cylan.jiafeigou.rx.RxUiEvent;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by hunt on 16-5-23.
 */
public class HomePageListPresenterImpl extends AbstractPresenter<HomePageListContract.View>
        implements HomePageListContract.Presenter {

    private TimeTickBroadcast timeTickBroadcast;
    private Subscription onRefreshSubscription;
    private CompositeSubscription bulkSubscriptions;
    private Subscription onGreetSubscription;

    public HomePageListPresenterImpl(HomePageListContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        //注意事项
        bulkSubscriptions = new CompositeSubscription();
        //注册1
        bulkSubscriptions
                .add(getTimeTickEventSub());
        bulkSubscriptions
                .add(getLoginRspSub());
        bulkSubscriptions
                .add(subDeviceList());
        bulkSubscriptions
                .add(JFGAccountUpdate());
    }

    private Subscription getTimeTickEventSub() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.TimeTickEvent.class)
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.TimeTickEvent>() {
                    @Override
                    public void call(RxEvent.TimeTickEvent o) {
                        //6:00 am - 17:59 pm
                        //18:00 pm-5:59 am
                        if (getView() != null) {
                            getView().onTimeTick(JFGRules.getTimeRule());
                        }
                    }
                });

    }

    private Subscription getLoginRspSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.LoginRsp.class)
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.LoginRsp>() {
                    @Override
                    public void call(RxEvent.LoginRsp o) {
                        if (getView() != null)
                            getView().onLoginState(JCache.isOnline);
                        if (JCache.getAccountCache() != null)
                            getView().onAccountUpdate(JCache.getAccountCache());
                    }
                });
    }

    private Subscription JFGAccountUpdate() {
        return RxBus.getCacheInstance().toObservableSticky(JFGAccount.class)
                .filter(new RxHelper.Filter<>((getView() != null && JCache.isOnline)))
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<JFGAccount, Object>() {
                    @Override
                    public Object call(JFGAccount jfgAccount) {
                        getView().onAccountUpdate(jfgAccount);
                        return null;
                    }
                })
                .retry(new RxHelper.RxException<>("JFGAccount"))
                .subscribe();
    }

    /**
     * 粘性订阅
     *
     * @return
     */
    private Subscription subDeviceList() {
        return RxBus.getUiInstance().toObservable(RxUiEvent.BulkDeviceList.class)
                .filter(new RxHelper.Filter<>(getView() != null))
                .flatMap(new Func1<RxUiEvent.BulkDeviceList, Observable<List<DeviceBean>>>() {
                    @Override
                    public Observable<List<DeviceBean>> call(RxUiEvent.BulkDeviceList list) {
                        List<DeviceBean> beanList = new ArrayList<>();
                        for (JFGDevice device : list.bulkList) {
                            DeviceBean bean = new DeviceBean();
                            bean.fillData(device);
                            beanList.add(bean);
                        }
                        return Observable.just(beanList);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<List<DeviceBean>, DeviceBean>() {
                    @Override
                    public DeviceBean call(List<DeviceBean> oList) {
                        //已经展示的列表
                        List<DeviceBean> vList = getView().getDeviceList();
                        //新列表
                        for (DeviceBean bean : oList) {
                            final int index = vList.indexOf(bean);
                            if (MiscUtils.isInRange(0, oList.size(), index)) {
                                //更新对应的item
                                vList.set(index, bean);
                                getView().onItemUpdate(index);
                            } else {
                                //a new one
                                List<DeviceBean> newList = new ArrayList<>();
                                newList.add(bean);
                                getView().onItemsInsert(newList);
                            }
                        }
                        return null;
                    }
                })
                .retry(RxHelper.exceptionFun)
                .subscribe();
    }

    @Override
    public void stop() {
        unSubscribe(onRefreshSubscription,
                bulkSubscriptions,
                onGreetSubscription);
    }


    @Override
    public void fetchGreet() {
        onGreetSubscription = Observable.just(null)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Object, GreetBean>() {
                    @Override
                    public GreetBean call(Object o) {
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<GreetBean>() {
                    @Override
                    public void call(GreetBean greetBean) {
                        if (getView() != null && JCache.getAccountCache() != null) {
                            getView().onAccountUpdate(JCache.getAccountCache());
                        }
                    }
                });

    }

    @Override
    public void fetchDeviceList() {
        if (!JCache.isOnline) {
            getView().onLoginState(false);
            getView().onRefreshFinish();
        }
        onRefreshSubscription = Observable.just(JCache.isOnline)
                .subscribeOn(Schedulers.newThread())
                .map(new Func1<Boolean, Object>() {
                    @Override
                    public Object call(Boolean aBoolean) {
                        //发送给DpDeviceAssembler
                        RxBus.getCacheInstance().post(new RxUiEvent.QueryBulkDevice());
                        return null;
                    }
                })
                .delay(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object aBoolean) {
                        if (getView() != null) getView().onRefreshFinish();
                    }
                });
    }

    @Override
    public void deleteItem(DeviceBean deviceBean) {

    }

    @Override
    public void registerWorker() {
        initTimeTickBroadcast();
    }

    @Override
    public void unRegisterWorker() {
        Context context = ContextUtils.getContext();
        if (timeTickBroadcast != null && context != null) {
            context.unregisterReceiver(timeTickBroadcast);
            timeTickBroadcast = null;
        }
    }

    private void initTimeTickBroadcast() {
        timeTickBroadcast = new TimeTickBroadcast();
        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);
        getView().getContext()
                .getApplicationContext()
                .registerReceiver(timeTickBroadcast, filter);
    }

}
