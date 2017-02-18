package com.cylan.jiafeigou.n.mvp.impl.home;


import android.content.pm.PackageManager;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
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
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.wheel.WonderIndicatorWheelView;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
    private TreeSet<DpMsgDefine.DPWonderItem> mWonderItems = new TreeSet<>();
    private List<DpMsgDefine.DPWonderItem> mCurrentWonderItems = new ArrayList<>();
    private long mCurrentDayStartTime;
    private long mCurrentDayEndTime;
    private boolean mHasTimeWheelInit = false;
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
                    AppLogger.e("收到删除请求,正在删除");
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
        queryTimeLine(0, 21, false)
                .observeOn(AndroidSchedulers.mainThread())
                .map(result -> {
                    AppLogger.e("正在更新 UI 界面");
                    int oldSize = mWonderItems.size();
                    long oldVersion = mWonderItems.isEmpty() ? 0 : mWonderItems.first().version;
                    mWonderItems.addAll(result);
                    int newSize = mWonderItems.size();
                    long topVersion = mWonderItems.first().version;
                    boolean empty = oldSize == newSize && mCurrentWonderItems.size() == 0;
                    updateCurrentItems(TimeUtils.getSpecificDayStartTime(topVersion), TimeUtils.getSpecificDayEndTime(topVersion));
                    if (mCurrentWonderItems.size() > 0) mView.chooseEmptyView(VIEW_TYPE_HIDE);
                    if (empty || oldVersion < mCurrentDayStartTime) {//change
                        mView.onChangeTimeLineDaySuccess(mCurrentWonderItems);
                        mView.onTimeLineRsp(mCurrentWonderItems.get(0).version, true, true);
                    } else if (oldVersion >= mCurrentDayStartTime && oldVersion <= mCurrentDayEndTime) {//update
                        mView.onQueryTimeLineSuccess(mCurrentWonderItems.subList(0, newSize - oldSize), true);
                    }
                    long initValue = -1;
                    if (!mHasTimeWheelInit && mWonderItems.size() > 0) {
                        initValue = mWonderItems.first().version;
                    }
                    AppLogger.e("initValue:" + initValue);
                    return initValue;
                })
                .observeOn(Schedulers.io())
                .filter(initValue -> initValue > 0)
                .map(initValue -> {
                    List<WonderIndicatorWheelView.WheelItem> wheelList = getInitWheelList(initValue);
                    mView.onTimeLineInit(wheelList);
                    mHasTimeWheelInit = true;
                    AppLogger.e("正在初始化时间轴数据,当前 list 大小为:" + wheelList.size());
                    return wheelList;
                })
                .flatMap(Observable::from)
                .map(item -> sendQueryRequest(item.time, 1, true))
                .filter(seq -> seq != -1)
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class).filter(rsp -> rsp.seq == seq).first())
                .map(rsp -> {
                    AppLogger.e("收到时间轴更新数据");
                    ArrayList<JFGDPMsg> msgs = rsp.map.get(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG);
                    long version = -1;
                    if (msgs.size() > 0) {
                        version = msgs.get(0).version;
                    }
                    return version;
                })
                .filter(version -> version != -1)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(() -> AppLogger.e("刷新链取消订阅了"))
                .subscribe(version -> {
                    AppLogger.e("正在更新时间轴条目:" + version);
                    mView.onTimeLineRsp(version, true, version >= mCurrentDayStartTime && version <= mCurrentDayEndTime);
                }, e -> {
                    if (e instanceof TimeoutException) {
                        mView.onQueryTimeLineTimeOut();
                    }
                    e.printStackTrace();
                    AppLogger.e(e.getLocalizedMessage());
                });
    }

    private List<DpMsgDefine.DPWonderItem> updateCurrentItems(long versionStart, long versionEnd) {
        mCurrentDayStartTime = versionStart;
        mCurrentDayEndTime = versionEnd;
        mCurrentWonderItems.clear();
        for (DpMsgDefine.DPWonderItem item : mWonderItems) {
            if (item.version >= versionStart && item.version <= versionEnd) {
                mCurrentWonderItems.add(item);
            }
        }
        return mCurrentWonderItems;
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
        queryTimeLine(mCurrentWonderItems.get(mCurrentWonderItems.size() - 1).version, 21, false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    int oldSize = mCurrentWonderItems.size();
                    for (DpMsgDefine.DPWonderItem item : result) {
                        if (item.version >= mCurrentDayStartTime && item.version <= mCurrentDayEndTime) {
                            mCurrentWonderItems.add(item);
                        }
                    }
                    mView.onQueryTimeLineSuccess(new ArrayList<>(mCurrentWonderItems).subList(oldSize, mCurrentWonderItems.size()), false);
                }, e -> {
                    if (e instanceof TimeoutException) {
                        mView.onQueryTimeLineTimeOut();
                    }
                });
    }

    public Observable<List<DpMsgDefine.DPWonderItem>> queryTimeLine(long version, int count, boolean asc) {
        return Observable.just(sendQueryRequest(version, count, asc))
                .filter(seq -> seq > 0)
                .subscribeOn(Schedulers.io())
                .flatMap(seq -> RxBus.getCacheInstance()
                        .toObservable(RxEvent.ParseResponseCompleted.class)
                        .filter(rsp -> rsp.seq == seq)
                        .first()
                        .timeout(10, TimeUnit.SECONDS))
                .map(rsp -> {
                    AppLogger.e("收到从服务器返回数据!!!");
                    DpMsgDefine.DPSet<DpMsgDefine.DPWonderItem> result = mSourceManager.getValue(mUUID, DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG, rsp.seq);
                    return result.list();
                });
    }

    private List<DpMsgDefine.DPWonderItem> filterAndCacheItems(List<DpMsgDefine.DPWonderItem> response) {
        if (response == null || response.size() == 0) return null;
        AppLogger.e("正在缓冲条目");
        List<DpMsgDefine.DPWonderItem> result = new ArrayList<>();

        return result;
    }

//    private boolean isEmpty(long time) {
////        TreeSet<DpMsgDefine.DPWonderItem> wonderItems = mWonderItems.get(time);
////        return wonderItems == null || wonderItems.size() == 0;
//    }

    @Override
    public void deleteTimeline(int position) {
        Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            ArrayList<JFGDPMsg> params = new ArrayList<>();
            JFGDPMsg msg = new JFGDPMsg(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG, mCurrentWonderItems.get(position).version);
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
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.DeleteDataRsp.class).onBackpressureBuffer().filter(rsp -> rsp.seq == seq).first().timeout(10, TimeUnit.SECONDS))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                    if (rsp.resultCode == 0) {//success
                        AppLogger.e("删除成功");
                        DpMsgDefine.DPWonderItem item = mCurrentWonderItems.remove(position);
                        mWonderItems.remove(item);
                        ToastUtil.showPositiveToast("删除成功");
                        mView.onDeleteWonderSuccess(position);
                        if (mCurrentWonderItems.isEmpty()) {//说明当天的已经删完了
                            mView.chooseEmptyView(VIEW_TYPE_EMPTY);
                            mView.onTimeLineRsp(mCurrentDayStartTime, false, false);
                        }
                        RxBus.getCacheInstance().post(new RxEvent.DeleteWonderRsp(true, position));
                    } else {
                        AppLogger.e("删除失败");
                        ToastUtil.showNegativeToast("删除失败");
                    }
                }, e -> {
                    ToastUtil.showNegativeToast("删除失败!");
                    AppLogger.e(e.getMessage());
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
        Observable.create((Observable.OnSubscribe<List<DpMsgDefine.DPWonderItem>>) subscriber -> {
            mCurrentDayStartTime = TimeUtils.getSpecificDayStartTime(timeStamp);
            mCurrentDayEndTime = TimeUtils.getSpecificDayEndTime(timeStamp);
            updateCurrentItems(mCurrentDayStartTime, mCurrentDayEndTime);
            subscriber.onNext(mCurrentWonderItems);
            subscriber.onCompleted();
        })
                .subscribeOn(Schedulers.io())
                .flatMap(wonderItems -> wonderItems != null && wonderItems.size() > 0 ? Observable.just(mCurrentWonderItems) : queryTimeLine(mCurrentDayEndTime, 21, false).map(ret -> {
                    AppLogger.e("从新请求数据");
                    for (DpMsgDefine.DPWonderItem item : ret) {
                        if (item.version >= mCurrentDayStartTime && item.version <= mCurrentDayEndTime) {
                            mCurrentWonderItems.add(item);
                        }
                    }
                    return mCurrentWonderItems;
                }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(items -> {
                    mView.chooseEmptyView(items.size() > 0 ? VIEW_TYPE_HIDE : VIEW_TYPE_EMPTY);
                    mView.onChangeTimeLineDaySuccess(items);
                }, e -> {
                    if (e instanceof TimeoutException) {
                        mView.onQueryTimeLineTimeOut();
                    }
                });
    }

    @Override
    public void queryTimeLine(long start) {
        long startTime = TimeUtils.getSpecificDayStartTime(start);
        JFGDPMsg msg = new JFGDPMsg(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG, startTime);
        ArrayList<JFGDPMsg> params = new ArrayList<>();
        params.add(msg);
        long[] seq = new long[1];
        robotGetData("", params, 1, true, 0, seq);
    }

    private long sendQueryRequest(long version, int count, boolean asc) {
        try {
            AppLogger.e("正在发送查询请求,version:" + version + "count:" + count + "acs:" + asc);
            ArrayList<JFGDPMsg> params = new ArrayList<>();
            JFGDPMsg msg = new JFGDPMsg(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG, version);
            params.add(msg);
            return JfgCmdInsurance.getCmd().robotGetData("", params, count, asc, 0);//多请求一条数据,用来判断是否是一天最后一条
        } catch (JfgException e) {
            AppLogger.e(e.getMessage());
            return -1;
        }
    }

    private void onWonderfulAccountRsp(DataPoint... values) {
//        boolean init = mWonderItems.size() == 0 || mWonderItems.valueAt(0).size() == 0;
//        TreeSet<DpMsgDefine.DPWonderItem> wonderItems = mWonderItems.get(mPositionDayStart);
//        if (wonderItems == null) {
//            wonderItems = new TreeSet<>();
//            mWonderItems.put(mPositionDayStart, wonderItems);
//        }
//        List<DpMsgDefine.DPWonderItem> results = filter(wonderItems, values);
//        wonderItems.addAll(results);
//        mView.chooseEmptyView(wonderItems.size() > 0 ? VIEW_TYPE_HIDE : VIEW_TYPE_EMPTY);
//        mView.onQueryTimeLineSuccess(results, false);
//        if (values != null && values.length > 0) {
//            if (init) {//init
//                DpMsgDefine.DPWonderItem item = (DpMsgDefine.DPWonderItem) values[0];
//                List<WonderIndicatorWheelView.WheelItem> list = getInitWheelList(TimeUtils.getSpecificDayStartTime(item.time * 1000L));
//                mView.onTimeLineInit(list);
//
//                for (WonderIndicatorWheelView.WheelItem wheelItem : list) {
//                    queryTimeLine(wheelItem.time);
//                }
//
//            } else {
//                DpMsgDefine.DPWonderItem item = (DpMsgDefine.DPWonderItem) values[0];
//                mView.onTimeLineRsp(TimeUtils.getSpecificDayStartTime(item.time * 1000L));
//            }
//        }
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

//    private List<DpMsgDefine.DPWonderItem> filter(TreeSet<DpMsgDefine.DPWonderItem> wonderItems, DataPoint... values) {
////        if (values == null) return null;
////        List<DpMsgDefine.DPWonderItem> result = new ArrayList<>(21);
////        DpMsgDefine.DPWonderItem wonderItem;
////        for (DataPoint value : values) {
////            wonderItem = (DpMsgDefine.DPWonderItem) value;
////            if (mPositionDayStart == 0 || mPositionDayEnd == 0) {
////                mPositionDayStart = TimeUtils.getSpecificDayStartTime(wonderItem.time * 1000L);
////                mPositionDayEnd = TimeUtils.getSpecificDayEndTime(wonderItem.time * 1000L);
////            }
////            if (wonderItem.time * 1000L >= mPositionDayStart && wonderItem.time * 1000L < mPositionDayEnd) {
////                if (wonderItems != null && !wonderItems.contains(wonderItem))
////                    result.add(wonderItem);
////            }
////        }
////        return result;
//    }
}

