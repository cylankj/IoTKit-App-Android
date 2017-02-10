package com.cylan.jiafeigou.n.mvp.impl.home;


import android.content.pm.PackageManager;
import android.support.v4.util.LongSparseArray;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.dp.DataPoint;
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
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

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
    private LongSparseArray<TreeSet<DpMsgDefine.DPWonderItem>> mWonderDaySource = new LongSparseArray<>();
    private long mPositionDayStart = 0;
    private long mPositionDayEnd = 0;

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
        registerSubscription(getTimeTickEventSub(), getPageScrolledSub());
    }

    @Override
    public void onSetContentView() {
        if (showGuidePage()) {
            mView.chooseEmptyView(VIEW_TYPE_GUIDE);
        }
    }

    private boolean showGuidePage() {
        return PreferencesUtils.getBoolean(JConstant.KEY_DELAY_RECORD_GUIDE, true);
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
                    mView.onPageScrolled();
                });
    }

    @Override
    public void startRefresh() {
        TreeSet<DpMsgDefine.DPWonderItem> items = mWonderDaySource.get(mPositionDayStart);
        load(items == null || items.size() == 0 ? mPositionDayStart : items.first().version, items != null && items.size() > 0);
    }

    private void load(long version, boolean asc) {
        ArrayList<JFGDPMsg> params = new ArrayList<>();
        JFGDPMsg msg = new JFGDPMsg(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG, version);
        params.add(msg);
        AppLogger.e("version=" + version);
        robotGetData("", params, 21, asc, 0);//多请求一条数据,用来判断是否是一天最后一条
    }

    @Override
    public void startLoadMore() {
        TreeSet<DpMsgDefine.DPWonderItem> items = mWonderDaySource.get(mPositionDayStart);
        if (items != null) {
            load(items.last().version, false);
        }
    }

    @Override
    public void deleteTimeline(long time) {

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

    @Override
    public void loadSpecificDay(long timeStamp) {
        mPositionDayStart = TimeUtils.getSpecificDayStartTime(timeStamp);
        mPositionDayEnd = TimeUtils.getSpecificDayEndTime(timeStamp);
        TreeSet<DpMsgDefine.DPWonderItem> items = mWonderDaySource.get(mPositionDayStart);
        if (items != null) {
            mView.chooseEmptyView(items.size() > 0 ? VIEW_TYPE_HIDE : VIEW_TYPE_EMPTY);
            mView.onMediaListRsp(new ArrayList<>(items));
        } else {
            load(mPositionDayEnd, false);
        }
    }

    @Override
    public void queryTimeLine(long start) {
        long startTime = TimeUtils.getSpecificDayStartTime(start);
        JFGDPMsg msg = new JFGDPMsg(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG, startTime);
        ArrayList<JFGDPMsg> params = new ArrayList<>();
        params.add(msg);
        robotGetData("", params, 1, true, 0);
    }

    @Override
    protected void onRegisterResponseParser() {
        super.onRegisterResponseParser();
        registerResponseParser(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG, this::onWonderfulAccountRsp);
    }

    private void onWonderfulAccountRsp(DataPoint... values) {
        if (values == null) return;//NP
        DpMsgDefine.DPWonderItem item = (DpMsgDefine.DPWonderItem) values[0];
        mView.onTimeLineRsp(TimeUtils.getSpecificDayStartTime(item.time * 1000L), mWonderDaySource.size() == 0);
        List<DpMsgDefine.DPWonderItem> results = filter(values);
        TreeSet<DpMsgDefine.DPWonderItem> items = mWonderDaySource.get(mPositionDayStart);
        if (items == null) {
            items = new TreeSet<>();
            mWonderDaySource.put(mPositionDayStart, items);
        }
        items.addAll(results);
        mView.chooseEmptyView(items.size() > 0 ? VIEW_TYPE_HIDE : VIEW_TYPE_EMPTY);
        mView.onMediaListRsp(results);

    }

    private List<DpMsgDefine.DPWonderItem> filter(DataPoint... values) {
        List<DpMsgDefine.DPWonderItem> result = new ArrayList<>(21);
        DpMsgDefine.DPWonderItem wonderItem;
        for (DataPoint value : values) {
            wonderItem = (DpMsgDefine.DPWonderItem) value;
            if (mPositionDayStart == 0 || mPositionDayEnd == 0) {
                mPositionDayStart = TimeUtils.getSpecificDayStartTime(wonderItem.time * 1000L);
                mPositionDayEnd = TimeUtils.getSpecificDayEndTime(wonderItem.time * 1000L);
            }
            if (wonderItem.time * 1000L >= mPositionDayStart && wonderItem.time * 1000L < mPositionDayEnd) {
                //说明是在同一天
                AppLogger.e("是在同一天");
                result.add(wonderItem);
            }
        }
        return result;
    }
}

