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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract.View.VIEW_TYPE_EMPTY;
import static com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract.View.VIEW_TYPE_GUIDE;
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
        registerSubscription(getTimeTickEventSub(), getPageScrolledSub());
    }

    @Override
    public void onStop() {
        super.onStop();
        registerSubscription(getDeleteWonderfulSub());
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        onUnRegisterSubscription();
    }

    private Subscription getDeleteWonderfulSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeleteWonder.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deleteWonder -> {
                    AppLogger.d("收到删除请求,正在删除");
                    deleteTimeline(deleteWonder.position);
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (showGuidePage()) {
            mView.chooseEmptyView(VIEW_TYPE_GUIDE);
        }
    }

    private boolean showGuidePage() {
        return PreferencesUtils.getBoolean(JConstant.KEY_WONDERFUL_GUIDE, true);
    }


    private Subscription getTimeTickEventSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.TimeTickEvent.class)
                .subscribeOn(Schedulers.newThread())
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(timeTickEvent -> {
                    mView.onTimeTick(JFGRules.getTimeRule());
                });
    }

    private Subscription getPageScrolledSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.PageScrolled.class)
                .subscribeOn(Schedulers.io())
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(timeTickEvent -> {
                    onUnRegisterSubscription();
                    mView.onPageScrolled();
                });
    }

    @Override
    public void startRefresh() {
        Subscription subscribe = Observable.just(new DPEntity()
                .setUuid("")
                .setVersion(0L)
                .setAction(DBAction.QUERY)
                .setOption(new DBOption.SingleQueryOption(false, 20))
                .setMsgId(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG))
                .observeOn(Schedulers.io())
                .flatMap(this::perform)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.getResultCode() == 0) {
                        mWonderItems.clear();
                        mWonderItems.addAll(result.getResultResponse());
                        mView.chooseEmptyView(mWonderItems.size() > 0 ? VIEW_TYPE_HIDE : VIEW_TYPE_EMPTY);
                        mView.onQueryTimeLineSuccess(mWonderItems, true);
                    }
                }, e -> {
                    e.printStackTrace();
                    AppLogger.d(e.getMessage());
                    mView.onQueryTimeLineCompleted();
                }, () -> mView.onQueryTimeLineCompleted());
        registerSubscription(subscribe);
    }

    @Override
    public void startLoadMore() {
        Observable.just(mWonderItems.get(mWonderItems.size() - 1).version)
                .map(version -> new DPEntity()
                        .setVersion(version)
                        .setAction(DBAction.QUERY)
                        .setOption(new DBOption.SingleQueryOption(false, 20))
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
                    AppLogger.d(e.getMessage());
                    mView.onQueryTimeLineCompleted();

                }, () -> {
                    mView.onQueryTimeLineCompleted();
                });
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
                .subscribe(result -> {
                    if (result.getResultCode() == 0) {//成功了
                        AppLogger.d("删除 TimeLine数据成功: position 为:" + position);
                        DpMsgDefine.DPWonderItem item = mWonderItems.remove(position);
                        mWonderItems.remove(item);
                        mView.onDeleteWonderSuccess(position);
                        if (mWonderItems.isEmpty()) {//说明当天的已经删完了
                            mView.chooseEmptyView(VIEW_TYPE_EMPTY);
                        }
                        RxBus.getCacheInstance().post(new RxEvent.DeleteWonderRsp(true, position));
                    }
                }, e -> {
                });
        registerSubscription(subscribe);
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

