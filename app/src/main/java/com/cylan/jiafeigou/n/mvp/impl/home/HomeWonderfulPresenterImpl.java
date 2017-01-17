package com.cylan.jiafeigou.n.mvp.impl.home;


import android.content.pm.PackageManager;

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
import com.cylan.jiafeigou.support.wechat.WechatShare;
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
    private static final int LOAD_PAGE_COUNT = 20;
    private TreeSet<DpMsgDefine.DPWonderItem> mWonderItems;
    private WechatShare wechatShare;

    private long mCurrentDayTimeStamp = 0;
    private long mCurrentDayTimeEnd = 0;

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

    /**
     * 备份所有需要显示的数据，再次取的时候，首先从这个reference中取，如果空再查询数据库。
     *
     * @param list
     */
    private synchronized void updateCache(List<DpMsgDefine.DPWonderItem> list) {
        if (mWonderItems == null) {
            mWonderItems = new TreeSet<>();
        }
        if (list == null || list.size() == 0)
            return;
        mWonderItems.addAll(list);
    }


    /**
     * 组装timeLine的数据
     *
     * @param list
     * @return
     */
    private List<Long> assembleTimeLineData(List<DpMsgDefine.DPWonderItem> list) {
        ArrayList<Long> result = new ArrayList<>(1024);
        for (DpMsgDefine.DPWonderItem bean : list) {
            result.add((long) bean.time * 1000);
        }
        return result;
    }

    @Override
    public void startRefresh() {
        load(mCurrentDayTimeStamp, false);
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
        load(mWonderItems.last().version, false);
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
        mCurrentDayTimeStamp = timeStamp;
        mCurrentDayTimeEnd = TimeUtils.getSpecificDayEndTime(timeStamp);
        load(timeStamp, false);
    }

    @Override
    protected void onRegisterResponseParser() {
        super.onRegisterResponseParser();
        registerResponseParser(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG, this::onWonderfulAccountRsp);
    }

    private void onWonderfulAccountRsp(DataPoint... values) {
        List<DpMsgDefine.DPWonderItem> results = filter(values);
        updateCache(results);
        List<Long> times = assembleTimeLineData(results);
        mView.chooseEmptyView(mWonderItems.size() > 0 ? VIEW_TYPE_HIDE : VIEW_TYPE_EMPTY);
        mView.onMediaListRsp(results);
        mView.onTimeLineDataUpdate(times);
    }

    private List<DpMsgDefine.DPWonderItem> filter(DataPoint... values) {
        List<DpMsgDefine.DPWonderItem> result = new ArrayList<>(21);
        DpMsgDefine.DPWonderItem wonderItem;
        for (DataPoint value : values) {
            wonderItem = (DpMsgDefine.DPWonderItem) value;
            if (mCurrentDayTimeStamp == 0 || mCurrentDayTimeEnd == 0) {
                mCurrentDayTimeStamp = TimeUtils.getSpecificDayStartTime(wonderItem.time * 1000L);
                mCurrentDayTimeEnd = TimeUtils.getSpecificDayEndTime(wonderItem.time * 1000L);
            }
            if (wonderItem.time * 1000L >= mCurrentDayTimeStamp && wonderItem.time * 1000L < mCurrentDayTimeEnd) {
                //说明是在同一天
                AppLogger.e("是在同一天");
                result.add(wonderItem);
            }
        }
        return result;
    }
}

