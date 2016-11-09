package com.cylan.jiafeigou.n.mvp.impl.home;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.cylan.jiafeigou.cache.JCache;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.misc.br.TimeTickBroadcast;
import com.cylan.jiafeigou.n.mvp.contract.home.HomePageListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.GreetBean;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.util.ArrayList;
import java.util.HashSet;
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
    private CompositeSubscription _timeTickSubscriptions;
    private Subscription onGreetSubscription;

    public HomePageListPresenterImpl(HomePageListContract.View view) {
        super(view);
        view.setPresenter(this);
        _timeTickSubscriptions = new CompositeSubscription();
    }

    @Override
    public void start() {
        //注册1
        _timeTickSubscriptions
                .add(getTimeTickEventSub());
        _timeTickSubscriptions
                .add(getLoginRspSub());
        _timeTickSubscriptions
                .add(getDeviceList());
    }

    private Subscription getTimeTickEventSub() {
        return RxBus.getDefault().toObservable(RxEvent.TimeTickEvent.class)
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
        return RxBus.getDefault().toObservable(RxEvent.LoginRsp.class)
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

    /**
     * 粘性订阅
     *
     * @return
     */
    private Subscription getDeviceList() {
        return RxBus.getDefault().toObservableSticky(RxEvent.DeviceList.class)
                .flatMap(new Func1<RxEvent.DeviceList, Observable<List<DeviceBean>>>() {
                    @Override
                    public Observable<List<DeviceBean>> call(RxEvent.DeviceList deviceList) {
                        if (getView() == null
                                || deviceList == null
                                || deviceList.jfgDevices == null)
                            return null;
                        List<DeviceBean> list = convert(deviceList);
                        if (getView().getDeviceList() != null) {
                            list.addAll(getView().getDeviceList());
                            list = new ArrayList<>(new HashSet<>(list));
                        }
                        return Observable.just(list);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<DeviceBean>>() {
                    @Override
                    public void call(List<DeviceBean> deviceList) {
                        getView().onDeviceListRsp(deviceList);
                    }
                });
    }

    private List<DeviceBean> convert(RxEvent.DeviceList deviceList) {
        final int count = deviceList == null || deviceList.jfgDevices == null ? 0 : deviceList.jfgDevices.size();
        List<DeviceBean> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            DeviceBean bean = new DeviceBean();
            bean.alias = deviceList.jfgDevices.get(i).alias;
            bean.pid = deviceList.jfgDevices.get(i).pid;
            bean.uuid = deviceList.jfgDevices.get(i).uuid;
            bean.shareAccount = deviceList.jfgDevices.get(i).shareAccount;
            bean.sn = deviceList.jfgDevices.get(i).sn;
            list.add(bean);
        }
        return list;
    }

    @Override
    public void stop() {
        unSubscribe(onRefreshSubscription,
                _timeTickSubscriptions,
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
        }
        onRefreshSubscription = Observable.just(JCache.isOnline)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {

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
