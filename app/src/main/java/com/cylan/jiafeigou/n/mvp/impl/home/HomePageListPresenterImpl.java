package com.cylan.jiafeigou.n.mvp.impl.home;

import android.util.Log;

import com.cylan.jiafeigou.n.mvp.contract.home.HomePageListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
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

/**
 * Created by hunt on 16-5-23.
 */
public class HomePageListPresenterImpl extends AbstractPresenter<HomePageListContract.View> implements HomePageListContract.Presenter {


    private Subscription onRefreshSubscription;

    public HomePageListPresenterImpl(HomePageListContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        unSubscribe(onRefreshSubscription);
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
    public void startRefresh() {
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
    public void onDeleteItem(DeviceBean deviceBean) {
        Log.d("hunt", "hunt....delete item: " + deviceBean);
    }
}
