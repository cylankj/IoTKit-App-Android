package com.cylan.jiafeigou.n.mvp.impl.home;


import android.content.pm.PackageManager;

import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract.View.VIEW_TYPE_EMPTY;
import static com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract.View.VIEW_TYPE_HIDE;


/**
 * Created by hunt on 16-5-23.
 */
public class HomeWonderfulPresenterImpl extends BasePresenter<HomeWonderfulContract.View>
        implements HomeWonderfulContract.Presenter {
    private List<DpMsgDefine.DPWonderItem> mWonderItems = new ArrayList<>();
    private static final int MAX_DAY_COUNT = 40;
    private static final long DAY_TIME = 24 * 60 * 60 * 1000L;

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "HomeWonderfulPresenterImpl#getPageScrolledSub",getPageScrolledSub());
    }

    @Override
    public void onStop() {
        super.onStop();
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "HomeWonderfulPresenterImpl#getDeleteWonderfulSub",getDeleteWonderfulSub());
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        onUnRegisterSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP);
    }

    private Subscription getDeleteWonderfulSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeleteWonder.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deleteWonder -> {
                    AppLogger.d("收到删除请求,正在删除");
                    deleteTimeline(deleteWonder.position);
                }, e -> {
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
                });
    }

    @Override
    public void onStart() {
        super.onStart();
//        if (showGuidePage()) {
//            mView.chooseEmptyView(VIEW_TYPE_GUIDE);
//        }
    }

    private boolean showGuidePage() {
        return PreferencesUtils.getBoolean(JConstant.KEY_WONDERFUL_GUIDE, true);
    }

    @Override
    protected boolean registerTimeTick() {
        return true;
    }

    @Override
    protected void onTimeTick() {
        if (mView != null) {
            Observable.just("timeTick")
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> {
                        //6:00 am - 17:59 pm
                        //18:00 pm-5:59 am
                        if (mView != null) {
                            mView.onTimeTick(JFGRules.getTimeRule());
                            AppLogger.i("time tick");
                        }
                    }, throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()));
        }
    }

    private Subscription getPageScrolledSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.PageScrolled.class)
                .subscribeOn(Schedulers.io())
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(timeTickEvent -> {
                    onUnRegisterSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP);
                    mView.onPageScrolled();
                }, e -> {
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
                });
    }

    @Override
    public void startRefresh() {
        Subscription subscribe = Observable.just(new DPEntity()
                .setUuid("")
                .setVersion(0L)
                .setAction(DBAction.QUERY)
                .setOption(DBOption.SingleQueryOption.DESC_15_LIMIT)
                .setMsgId(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG))
                .observeOn(Schedulers.io())
                .flatMap(this::perform)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.getResultCode() == 0) {
                        mWonderItems.clear();
                        if (result.getResultResponse() != null) {
                            mWonderItems.addAll(result.getResultResponse());
                        }
                        mView.chooseEmptyView(mWonderItems.size() > 0 ? VIEW_TYPE_HIDE : VIEW_TYPE_EMPTY);
                        mView.onQueryTimeLineSuccess(mWonderItems, true);
                    }
                }, e -> {
                    e.printStackTrace();
                    AppLogger.d(e.getMessage());
                    mView.onQueryTimeLineCompleted();
                }, () -> mView.onQueryTimeLineCompleted());
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "HomeWonderfulPresenterImpl#startRefresh",subscribe);
    }

    @Override
    public void startLoadMore() {
        Subscription subscribe = Observable.just(mWonderItems.get(mWonderItems.size() - 1).version)
                .map(version -> new DPEntity()
                        .setVersion(version)
                        .setAction(DBAction.QUERY)
                        .setOption(DBOption.SingleQueryOption.DESC_15_LIMIT)
                        .setMsgId(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG))
                .observeOn(Schedulers.io())
                .flatMap(this::perform)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.getResultCode() == 0) {
                        List<DpMsgDefine.DPWonderItem> items = result.getResultResponse();
                        mWonderItems.addAll(items);
                        mView.onQueryTimeLineSuccess(items, false);
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                    mView.onQueryTimeLineCompleted();

                }, () -> {
                    mView.onQueryTimeLineCompleted();
                });
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP,"HomeWonderfulPresenterImpl#startLoadMore", subscribe);
    }

    @Override
    public void deleteTimeline(int position) {
        Subscription subscribe = Observable.just(mWonderItems.get(position).version)
                .observeOn(Schedulers.io())
                .map(version -> new DPEntity()
                        .setUuid("")
                        .setVersion(version)
                        .setAction(DBAction.DELETED)
                        .setMsgId(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG))
                .flatMap(this::perform)
                .observeOn(AndroidSchedulers.mainThread())
                .map(ret -> {
                    DpMsgDefine.DPWonderItem item = null;
                    if (ret.getResultCode() == 0) {//成功了
                        AppLogger.d("删除 TimeLine数据成功: position 为:" + position);
                        item = mWonderItems.remove(position);
                        mWonderItems.remove(item);
                        mView.onDeleteWonderSuccess(position);
                        if (mWonderItems.isEmpty()) {//说明当天的已经删完了
                            mView.chooseEmptyView(VIEW_TYPE_EMPTY);
                        }
                        RxBus.getCacheInstance().post(new RxEvent.DeleteWonderRsp(true, position));
                    }
                    return item;
                })
                .filter(item -> item != null)
                .observeOn(Schedulers.io())
                .map(item -> new DPEntity()
                        .setUuid(item.cid)
                        .setVersion(TimeUtils.wrapToLong(item.time))
                        .setAction(DBAction.DELETED)
                        .setMsgId(511))
                .flatMap(this::perform)
                .subscribe(result -> {
                }, e -> {
                    e.printStackTrace();
                    AppLogger.d(e.getMessage());
                });
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP,"HomeWonderfulPresenterImpl#deleteTimeline", subscribe);
    }

    @Override
    public boolean checkWechat() {
        try {
            return mView
                    .getAppContext()
                    .getPackageManager()
                    .getPackageInfo("com.tencent.mm", PackageManager.GET_SIGNATURES) != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void removeGuideAnymore() {
        PreferencesUtils.putBoolean(JConstant.KEY_WONDERFUL_GUIDE, false);
        mView.chooseEmptyView(VIEW_TYPE_EMPTY);
    }
}

