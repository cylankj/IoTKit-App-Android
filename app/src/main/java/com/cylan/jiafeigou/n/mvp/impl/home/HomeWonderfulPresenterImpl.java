package com.cylan.jiafeigou.n.mvp.impl.home;


import android.content.pm.PackageManager;
import android.support.v4.util.LongSparseArray;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.DataSourceManager;
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
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.wheel.WonderIndicatorWheelView;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
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
    private LongSparseArray<TreeSet<DpMsgDefine.DPWonderItem>> mWonderDaySource = new LongSparseArray<>();
    private long mPositionDayStart = 0;
    private long mPositionDayEnd = 0;

    private static final int MAX_DAY_COUNT = 40;
    private static final long DAY_TIME = 24 * 60 * 60 * 1000L;
    private List<Long> mTimeLineSeq = new ArrayList<>();

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
        registerSubscription(getTimeTickEventSub(), getPageScrolledSub(), getDeleteWonderfulSub());
    }

    private Subscription getDeleteWonderfulSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeleteWonder.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deleteWonder -> {
                    AppLogger.e("接收到删除 WonderItem 请求");
                    deleteTimeline(deleteWonder.position);
                });
    }

    @Override
    public void onSetContentView() {
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
                    mView.onPageScrolled();
                });
    }

    @Override
    public void startRefresh() {
        if (DataSourceManager.getInstance().isOnline()) {
            int index = mWonderDaySource.indexOfKey(mPositionDayStart);
            if (index == 0) {//first
                load(0, false);
            } else {
                TreeSet<DpMsgDefine.DPWonderItem> items = mWonderDaySource.get(mPositionDayStart);
                load(items == null || items.size() == 0 ? mPositionDayStart : items.first().version, mPositionDayStart != 0);
            }
        } else {
            mView.onLoginStateChanged(false);
        }

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
    public void deleteTimeline(int position) {
        Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            TreeSet<DpMsgDefine.DPWonderItem> wonderItems = mWonderDaySource.get(mPositionDayStart);
            ArrayList<DpMsgDefine.DPWonderItem> temp = new ArrayList<>(wonderItems);
            long time = temp.get(position).version;
            ArrayList<JFGDPMsg> params = new ArrayList<>();
            JFGDPMsg msg = new JFGDPMsg(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG, time);
            params.add(msg);
            try {
                AppLogger.e("正在删除!");
                long seq = JfgCmdInsurance.getCmd().robotDelData("", params, 0);
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                .timeout(10, TimeUnit.SECONDS)
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.DeleteDataRsp.class).filter(rsp -> rsp.seq == seq).first())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                    if (rsp.resultCode == 0) {//success
                        ToastUtil.showPositiveToast("删除成功");
                        AppLogger.e("删除成功");
                        RxBus.getCacheInstance().post(new RxEvent.DeleteWonderRsp(true, position));
                        mView.onDeleteWonderSuccess(position);
                    } else {
                        AppLogger.e("删除失败");
                        ToastUtil.showNegativeToast("删除失败");
                    }
                }, e -> {
                    ToastUtil.showNegativeToast("删除失败!");
                    e.printStackTrace();
                });
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
        long[] seq = new long[1];
        mTimeLineSeq.add(seq[0]);
        robotGetData("", params, 1, true, 0, seq);
    }

    @Override
    protected void onRegisterResponseParser() {
        super.onRegisterResponseParser();
        registerResponseParser(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG, this::onWonderfulAccountRsp);
    }

    private void onWonderfulAccountRsp(DataPoint... values) {
        boolean init = mWonderDaySource.size() == 0 || mWonderDaySource.valueAt(0).size() == 0;
        TreeSet<DpMsgDefine.DPWonderItem> wonderItems = mWonderDaySource.get(mPositionDayStart);
        List<DpMsgDefine.DPWonderItem> results = filter(values);
        if (wonderItems == null) {
            wonderItems = new TreeSet<>();
            mWonderDaySource.put(mPositionDayStart, wonderItems);
        }
        wonderItems.addAll(results);
        mView.chooseEmptyView(wonderItems.size() > 0 ? VIEW_TYPE_HIDE : VIEW_TYPE_EMPTY);
        mView.onMediaListRsp(results);
        if (values != null && values.length > 0) {
            if (init) {//init
                DpMsgDefine.DPWonderItem item = (DpMsgDefine.DPWonderItem) values[0];
                List<WonderIndicatorWheelView.WheelItem> list = getInitWheelList(TimeUtils.getSpecificDayStartTime(item.time * 1000L));
                mView.onTimeLineInit(list);
                mTimeLineSeq.clear();

                for (WonderIndicatorWheelView.WheelItem wheelItem : list) {
                    queryTimeLine(wheelItem.time);
                }

            } else {
                DpMsgDefine.DPWonderItem item = (DpMsgDefine.DPWonderItem) values[0];
                mView.onTimeLineRsp(TimeUtils.getSpecificDayStartTime(item.time * 1000L));
            }
        }
    }

    private List<WonderIndicatorWheelView.WheelItem> getInitWheelList(long end) {
        List<WonderIndicatorWheelView.WheelItem> result = new ArrayList<>(MAX_DAY_COUNT);
        long start = TimeUtils.getSpecificDayStartTime(end) - (MAX_DAY_COUNT - 5) * DAY_TIME;
        WonderIndicatorWheelView.WheelItem item;
        for (int i = 0; i < MAX_DAY_COUNT; i++) {
            item = new WonderIndicatorWheelView.WheelItem();
            item.time = start + DAY_TIME * i;
            result.add(item);
        }
        result.get(MAX_DAY_COUNT - 5).wonderful = true;
        result.get(MAX_DAY_COUNT - 5).selected = true;
        return result;
    }

    private List<DpMsgDefine.DPWonderItem> filter(DataPoint... values) {
        if (values == null) return null;
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

