package com.cylan.jiafeigou.n.mvp.impl.home;


import android.content.pm.PackageManager;
import android.support.v4.util.LongSparseArray;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.widget.wheel.WonderIndicatorWheelView;

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

    private ArrayList<Long> mQuerySeq = new ArrayList<>();
    private ArrayList<WonderIndicatorWheelView.Item> mItems;
    private static final long DAY_TIME = 24 * 60 * 60 * 1000L;

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
        TreeSet<DpMsgDefine.DPWonderItem> items = mWonderDaySource.get(mPositionDayStart);
        load(items == null || items.size() == 0 ? 0 : items.first().version, items != null);
    }

    private void load(long version, boolean asc) {
        ArrayList<JFGDPMsg> params = new ArrayList<>();
        JFGDPMsg msg = new JFGDPMsg(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG, version);
        params.add(msg);
        AppLogger.e("version=" + version);
        robotGetData("", params, 21, asc, 0);//多请求一条数据,用来判断是否是一天最后一条
    }

    @Override
    protected void onParseResponseCompleted(long seq) {
        if (mQuerySeq.remove(seq)) {
            DpMsgDefine.DPSet<DpMsgDefine.DPWonderItem> value = mSourceManager.getValue(null, DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG, seq);
            if (value != null && value.value != null && value.value.size() > 0) {
                DpMsgDefine.DPWonderItem first = value.value.first();
                long time = TimeUtils.getSpecificDayEndTime(first.time * 1000L);
                for (WonderIndicatorWheelView.Item item : mItems) {
                    if (item.endTime == time) {
                        item.hasData = true;
                    }
                }
            }
            if (mQuerySeq.isEmpty()) {
                mView.onTimeLineDataUpdate(mItems);
            }
        }

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
        load(mPositionDayEnd, false);
    }

    @Override
    public void queryTimeLine(long start) {
        ArrayList<ArrayList<JFGDPMsg>> querys = new ArrayList<>();
        mItems = new ArrayList<>();
        mQuerySeq.clear();
        ArrayList<JFGDPMsg> params;
        for (int i = 6; i >= 0; i--) {
            params = new ArrayList<>();
            WonderIndicatorWheelView.Item item = new WonderIndicatorWheelView.Item();
            item.startTime = TimeUtils.getSpecificDayStartTime(start - i * DAY_TIME);
            item.endTime = TimeUtils.getSpecificDayEndTime(start - i * DAY_TIME);
            item.hasData = false;
            mItems.add(item);
            JFGDPMsg msg = new JFGDPMsg(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG, item.endTime);
            params.add(msg);
            querys.add(params);
        }
        AppLogger.e("开始查询时间轴数据" + start);
        for (ArrayList<JFGDPMsg> query : querys) {
            post(() -> {
                try {
                    long seq = JfgCmdInsurance.getCmd().robotGetData("", query, 1, false, 0);
                    mQuerySeq.add(seq);
                } catch (JfgException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    protected void onRegisterResponseParser() {
        super.onRegisterResponseParser();
        registerResponseParser(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG, this::onWonderfulAccountRsp);
    }

    private void onWonderfulAccountRsp(DataPoint... values) {
        List<DpMsgDefine.DPWonderItem> results = filter(values);
        TreeSet<DpMsgDefine.DPWonderItem> items = mWonderDaySource.get(mPositionDayStart);
        if (items == null) {
            items = new TreeSet<>();
            mWonderDaySource.put(mPositionDayStart, items);
            queryTimeLine(results.get(0).time * 1000L);
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

