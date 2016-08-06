package com.cylan.jiafeigou.n.mvp.impl.bell;

import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.bell.DoorBellHomeContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.utils.RandomUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by cylan-hunt on 16-8-3.
 */
public class DBellHomePresenterImpl extends AbstractPresenter<DoorBellHomeContract.View>
        implements DoorBellHomeContract.Presenter {
    public DBellHomePresenterImpl(DoorBellHomeContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    /**
     * 凌晨0点时间戳
     */
    private static final long todayInMidNight = TimeUtils.getTodayStartTime();
    private static final long yesterdayInMidNight = todayInMidNight - 24 * 60 * 60 * 1000L;

    @Override
    public void start() {
        compositeSubscription.add(onBellCallListSubscription());
        compositeSubscription.add(onLogStateSubscription());
        getView().onLoginState(RandomUtils.getRandom(1));
    }

    private Subscription onBellCallListSubscription() {
        return Observable.just(null)
                .subscribeOn(Schedulers.io())
                .delay(RandomUtils.getRandom(3) * 1000L + 100, TimeUnit.MICROSECONDS)
                .map(new Func1<Object, ArrayList<BellCallRecordBean>>() {
                    @Override
                    public ArrayList<BellCallRecordBean> call(Object o) {
                        return testList();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<BellCallRecordBean>>() {
                    @Override
                    public void call(ArrayList<BellCallRecordBean> bellInfoBeen) {
                        if (getView() != null) {
                            getView().onRecordsListRsp(bellInfoBeen);
                        }
                    }
                });
    }

    private static final SimpleDateFormat simpleDateFormat =
            new SimpleDateFormat("hh:mm a", Locale.getDefault());

    private static final SimpleDateFormat getSimpleDateFormat
            = new SimpleDateFormat("MM月dd日", Locale.getDefault());

    private ArrayList<BellCallRecordBean> testList() {
        ArrayList<BellCallRecordBean> list = new ArrayList<>(50);
        final int count = 50;
        for (int i = 0; i < count; i++) {
            BellCallRecordBean bean = new BellCallRecordBean();
            bean.answerState = RandomUtils.getRandom(2);
            bean.timeInLong = System.currentTimeMillis() - RandomUtils.getRandom(2000) * 60 * 60 * 1000L;
            bean.timeStr = simpleDateFormat.format(new Date(bean.timeInLong));
            bean.date = getDate(bean.timeInLong);
            list.add(bean);
        }
        Collections.sort(list);
        return list;
    }

    private String getDate(long time) {
        return time >= todayInMidNight ? "今天"
                : (time < todayInMidNight && time > yesterdayInMidNight ? "昨天" : getSimpleDateFormat.format(new Date(time)));
    }

    /**
     * 查询登陆状态
     *
     * @return
     */
    private Subscription onLogStateSubscription() {
        return RxBus.getInstance().toObservable()

                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (o != null && o instanceof RxEvent.LoginRsp) {
                            if (getView() != null) getView().onLoginState(JFGRules.LOGIN);
                        }
                    }
                });
    }

    @Override
    public void stop() {
        unSubscribe(compositeSubscription);
    }

    @Override
    public void fetchBellRecordsList() {

    }
}
