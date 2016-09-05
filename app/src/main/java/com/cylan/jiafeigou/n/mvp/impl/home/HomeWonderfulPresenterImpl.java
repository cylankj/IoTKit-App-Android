package com.cylan.jiafeigou.n.mvp.impl.home;


import com.cylan.jiafeigou.misc.TimeLineAssembler;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MediaBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.widget.wheel.WheelViewDataSet;
import com.cylan.utils.RandomUtils;
import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

;

/**
 * Created by hunt on 16-5-23.
 */
public class HomeWonderfulPresenterImpl extends AbstractPresenter<HomeWonderfulContract.View>
        implements HomeWonderfulContract.Presenter {

    private WeakReference<List<MediaBean>> weakReferenceList;
    private Subscription onRefreshSubscription;
    private Subscription onTimeLineSubscription;

    public HomeWonderfulPresenterImpl(HomeWonderfulContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        unSubscribe(onTimeLineSubscription, onRefreshSubscription);
    }


    /**
     * 计算过程.
     *
     * @return
     */
    private List<MediaBean> requestList() {
        int count = 10;
        List<MediaBean> list = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            final long time = currentTime - (i * i) * 3600 * 24L * 1000;
            MediaBean baseBean = new MediaBean();
            baseBean.time = time;
            baseBean.timeInStr = getDate(time);
            baseBean.deviceName = "南湖";
            baseBean.mediaType = RandomUtils.getRandom(4);
            list.add(baseBean);
        }
        AppLogger.d("rawList: " + (new Gson().toJson(list)));
        return list;
    }

    /**
     * 备份所有需要显示的数据，再次取的时候，首先从这个reference中取，如果空再查询数据库。
     *
     * @param list
     */
    private synchronized void updateCache(List<MediaBean> list) {
        if (list == null || list.size() == 0)
            return;
        if (weakReferenceList == null) {
            weakReferenceList = new WeakReference<>(list);
            return;
        }
        if (weakReferenceList.get() == null) {
            weakReferenceList = new WeakReference<>(list);
            return;
        }
        if (weakReferenceList != null && weakReferenceList.get() != null) {
            List<MediaBean> rawList = weakReferenceList.get();
            rawList.addAll(list);
            //remove the same one by time
            rawList = new ArrayList<>(new HashSet<>(rawList));
            Collections.sort(rawList);
            //retain them again
            weakReferenceList = new WeakReference<>(rawList);
        }
    }


    /**
     * 组装timeLine的数据
     *
     * @param list
     * @return
     */
    private WheelViewDataSet assembleTimeLineData(List<MediaBean> list) {
        TimeLineAssembler timeLineAssemble = new TimeLineAssembler();
        timeLineAssemble.setMediaBeanLinkedList(new LinkedList<>(list));
        return timeLineAssemble.generateDataSet();
    }

    private String getDate(final long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd", Locale.getDefault());
        return dateFormat.format(new Date(time));
    }

    private void wrapTimeLineDataSet() {
        onTimeLineSubscription = Observable.just(weakReferenceList)
                .subscribeOn(Schedulers.newThread())
                .flatMap(new Func1<WeakReference<List<MediaBean>>, Observable<WheelViewDataSet>>() {
                    @Override
                    public Observable<WheelViewDataSet> call(WeakReference<List<MediaBean>> listWeakReference) {
                        if (listWeakReference == null || listWeakReference.get() == null)
                            return null;
                        return Observable.just(assembleTimeLineData(listWeakReference.get()));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<WheelViewDataSet>() {
                    @Override
                    public void call(WheelViewDataSet wheelViewDataSet) {
                        if (wheelViewDataSet == null)
                            return;
                        if (getView() != null) getView().timeLineDataUpdate(wheelViewDataSet);
                    }
                });
    }

    @Override
    public void startRefresh() {

        final int testDelay = RandomUtils.getRandom(3);
        onRefreshSubscription = Observable.just("")
                .subscribeOn(Schedulers.newThread())
                .delay(testDelay * 1000L, TimeUnit.MILLISECONDS)
                .map(new Func1<String, List<MediaBean>>() {
                    @Override
                    public List<MediaBean> call(String s) {
                        List<MediaBean> list = requestList();
                        updateCache(new ArrayList<>(list));
                        wrapTimeLineDataSet();
                        return list;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<MediaBean>>() {
                    @Override
                    public void call(List<MediaBean> list) {
                        if (getView() != null) getView().onDeviceListRsp(list);

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });

    }

}

