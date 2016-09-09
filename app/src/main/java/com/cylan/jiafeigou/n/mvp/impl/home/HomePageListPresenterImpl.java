package com.cylan.jiafeigou.n.mvp.impl.home;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.misc.br.TimeTickBroadcast;
import com.cylan.jiafeigou.n.mvp.contract.home.HomePageListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.GreetBean;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.cylan.utils.RandomUtils;

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
                .add(RxBus.getInstance().toObservable()
                        .throttleFirst(1000, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Object>() {
                            @Override
                            public void call(Object event) {
                                //6:00 am - 17:59 pm
                                //18:00 pm-5:59 am
                                if (event != null
                                        && event instanceof RxEvent.TimeTickEvent) {
                                    if (getView() != null) {
                                        getView().onTimeTick(JFGRules.getTimeRule());
                                        getView().onGreetUpdate(generateBean());
                                    }
                                }
                                //登陆响应
                                if (event != null && (event instanceof RxEvent.LoginRsp)) {
                                    if (getView() != null)
                                        getView().onLoginState(RandomUtils.getRandom(2));
                                }
                            }
                        }));
    }

    private static final String[] poet = {"行人无限秋风思，隔水青山似故乡。..........",
            "一道鹊桥横渺渺，千声玉佩过玲玲。",
            "柔情似水，佳期如梦。", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"};
    private static final String[] nickName = {"女娲", "牛郎", "织女..........", "xxxxxxxxxx"};

    private GreetBean generateBean() {
        GreetBean greetBean = new GreetBean();
        greetBean.nickName = nickName[RandomUtils.getRandom(4)];
        greetBean.poet = poet[RandomUtils.getRandom(4)];
        return greetBean;
    }

    @Override
    public void stop() {
        unSubscribe(onRefreshSubscription,
                _timeTickSubscriptions,
                onGreetSubscription);
    }


    final String[] arrayBell = {"智能门铃", "大门口的门铃", "中南海的门铃"};
    final String[] arrayCam = {"天眼", "哈勃摄像头", "天安门监视"};
    final String[] arrayAlbum = {"云相册", "云相册", "云相册"};
    final String[] arrayMag = {"隔壁的门磁", "哈哈哈家的抽屉", "哈哈哈家的抽屉"};

    /**
     * 计算过程.
     *
     * @return
     */
    private List<DeviceBean> requestList() {
        int count = 4;
        List<DeviceBean> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            DeviceBean baseBean = new DeviceBean();
            baseBean.id = i;
            baseBean.alias = getAlias(i);
            baseBean.cid = "700000" + i;
            baseBean.msgTime = System.currentTimeMillis()
                    - RandomUtils.getRandom(10) * 24 * 60 * 1000L//天
                    - RandomUtils.getRandom(24) * 60 * 60 * 1000L//小时
                    - RandomUtils.getRandom(60) * 60 * 1000L;//分钟
            baseBean.deviceType = i;
            list.add(baseBean);
        }
        return list;
    }

    private String getAlias(final int type) {
        if (type == 0)
            return arrayBell[RandomUtils.getRandom(4)];
        if (type == 1)
            return arrayCam[RandomUtils.getRandom(4)];
        if (type == 2)
            return arrayAlbum[RandomUtils.getRandom(4)];
        if (type == 3)
            return arrayMag[RandomUtils.getRandom(4)];
        return "";
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
                        if (getView() != null) {
                            getView().onGreetUpdate(generateBean());
                        }
                    }
                });

    }

    @Override
    public void fetchDeviceList() {
        final int loginState = RandomUtils.getRandom(2);
        if (loginState == JFGRules.LOGOUT) {
            onRefreshSubscription = Observable.just("")
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            getView().onLoginState(JFGRules.LOGOUT);
                        }
                    });
            return;
        }
        final int testDelay = RandomUtils.getRandom(3);
        onRefreshSubscription = Observable.just("")
                .subscribeOn(Schedulers.newThread())
                .delay(testDelay * 1000L, TimeUnit.MILLISECONDS)
                .map(new Func1<String, List<DeviceBean>>() {
                    @Override
                    public List<DeviceBean> call(String s) {
                        return requestList();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<DeviceBean>>() {
                    @Override
                    public void call(List<DeviceBean> list) {
                        if (getView() != null) getView().onDeviceListRsp(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

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
        Context context = getView() == null ? null :
                (getView().getContext() != null ? getView().getContext().getApplicationContext() : null);
        if (timeTickBroadcast != null && context != null) {
            context.unregisterReceiver(timeTickBroadcast);
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
