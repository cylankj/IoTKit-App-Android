package com.cylan.jiafeigou.n.mvp.impl.bell;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.db.BaseDPHelper;
import com.cylan.jiafeigou.cache.db.DPCache;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.DoorBellHomeContract;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.JfgCmdInsurance.getCmd;

/**
 * Created by cylan-hunt on 16-8-3.
 */
public class DBellHomePresenterImpl extends BasePresenter<DoorBellHomeContract.View>
        implements DoorBellHomeContract.Presenter {
    /**
     * 凌晨0点时间戳
     */
    private static final long todayInMidNight = TimeUtils.getTodayStartTime();
    private static final long yesterdayInMidNight = todayInMidNight - 24 * 60 * 60 * 1000L;
    private List<BellCallRecordBean> mRecords = new ArrayList<>();
    private boolean isFirst = true;

    private void notifyBellLowBattery() {
        if (isFirst) {
            isFirst = false;
            long lastTime = PreferencesUtils.getLong(JConstant.LAST_ENTER_TIME + mUUID, System.currentTimeMillis());
            DpMsgDefine.DPPrimary<Integer> battery = mSourceManager.getValue(mUUID, DpMsgMap.ID_206_BATTERY);
            if (lastTime < todayInMidNight) {//新的一天
                PreferencesUtils.putLong(JConstant.LAST_ENTER_TIME + mUUID, System.currentTimeMillis());
                if (battery.$() < 20) {
                    mView.onBellBatteryDrainOut();
                }
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if (NetUtils.isNetworkAvailable(mView.getAppContext())) {
            syncLocalDataFromServer();
        } else {
            mView.onSyncLocalDataFinished();//无网络不需要同步
        }
    }

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
        registerSubscription(getNetWorkMonitorSub());
    }

    private Subscription getNetWorkMonitorSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.NetConnectionEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    if (event.available) {
                        syncLocalDataFromServer();
                    } else {
                        mView.onSyncLocalDataRequired();
                    }
                }, Throwable::printStackTrace);
    }

    private void syncLocalDataFromServer() {
        Subscription not_confirm = BaseDPHelper.getInstance().queryUnConfirmDpMsgWithTag(mUUID, DpMsgMap.ID_401_BELL_CALL_STATE, "NOT_CONFIRM")
                .filter(items -> {
                    if (items.size() == 0) {
                        AppLogger.d("没有需要同步的数据");
                        mView.onSyncLocalDataFinished();
                    }
                    return items.size() > 0;
                })
                .observeOn(Schedulers.io())
                .map(items -> {
                    ArrayList<JFGDPMsg> params = new ArrayList<>();
                    JFGDPMsg msg;
                    long seq = -1;
                    for (DPCache bean : items) {
                        msg = new JFGDPMsg(DpMsgMap.ID_401_BELL_CALL_STATE, bean.getVersion());
                        params.add(msg);
                    }
                    try {
                        seq = JfgCmdInsurance.getCmd().robotDelData(mUUID, params, 0);
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.d("刪除門鈴呼叫記錄失敗:" + e.getMessage());
                    }
                    AppLogger.d("正在刪除門鈴呼叫記錄, seq 為:" + seq);
                    return seq;
                })
                .filter(seq -> seq != -1)
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.DeleteDataRsp.class)
                        .filter(rsp -> rsp.seq == seq).first().timeout(10, TimeUnit.SECONDS))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                    if (rsp.resultCode == 0) {
                        AppLogger.d("删除未经确认的数据成功");
                        BaseDPHelper.getInstance().deleteDPMsgWithConfirm(mUUID, DpMsgMap.ID_401_BELL_CALL_STATE).subscribe();
                    }
                    mView.onSyncLocalDataFinished();
                }, Throwable::printStackTrace);
        registerSubscription(not_confirm);
    }


    @Override
    public void fetchBellRecordsList(boolean asc, long time) {
        Subscription subscribe = Observable.just(NetUtils.isNetworkAvailable(mView.getAppContext()))
                .observeOn(Schedulers.io())
                .flatMap(hasNet -> hasNet ? fetchBellRecordListFromServer(asc, time) : fetchBellRecordListFromLocal(asc, time == 0 ? Long.MAX_VALUE : time))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    mView.onRecordsListRsp(result);
                    notifyBellLowBattery();
                }, e -> {
                    if (e instanceof TimeoutException) {
                        AppLogger.d("获取数据超时,正在重试中");
                        fetchBellRecordsList(asc, time);
                    }
                });
        registerSubscription(subscribe);
    }

    private Observable<List<BellCallRecordBean>> fetchBellRecordListFromServer(boolean asc, long time) {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            JFGDPMsg request = new JFGDPMsg(DpMsgMap.ID_401_BELL_CALL_STATE, time);
            ArrayList<JFGDPMsg> params = new ArrayList<>();
            params.add(request);
            try {
                long seq = getCmd().robotGetData(mUUID, params, 20, asc, 0);
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                e.printStackTrace();
                AppLogger.e(e.getMessage());
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.ParseResponseCompleted.class)
                        .filter(rsp -> rsp.seq == seq).first().timeout(10, TimeUnit.SECONDS)
                )
                .map(rsp -> {
                    DpMsgDefine.DPSet<DpMsgDefine.DPBellCallRecord> records = mSourceManager.getValue(mUUID, DpMsgMap.ID_401_BELL_CALL_STATE);
                    return parse(records.value);
                });
    }

    private Observable<List<BellCallRecordBean>> fetchBellRecordListFromLocal(boolean asc, long time) {
        return BaseDPHelper.getInstance().queryDPMsg(mUUID, time, DpMsgMap.ID_401_BELL_CALL_STATE, asc, 20)
                .flatMap(Observable::from)
                .map(item -> {
                    DpMsgDefine.DPBellCallRecord record = null;
                    try {
                        record = DpUtils.unpackData(item.getBytes(), DpMsgDefine.DPBellCallRecord.class);
                        if (record != null) {
                            record.version = item.getVersion();
                            record.id = item.getMsgId();
                        }

                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    AppLogger.d("查询到缓存数据:" + new Gson().toJson(record));
                    return record;
                })
                .buffer(20)
                .map(this::parse);
    }

    private List<BellCallRecordBean> parse(Collection<DpMsgDefine.DPBellCallRecord> response) {
        List<BellCallRecordBean> result = new ArrayList<>();
        BellCallRecordBean record;
        for (DpMsgDefine.DPBellCallRecord callRecord : response) {
            record = BellCallRecordBean.parse(callRecord);
            result.add(record);
        }
        mRecords.addAll(result);
        return result;
    }

    @Override
    public void deleteBellCallRecord(List<BellCallRecordBean> list) {
        Subscription subscribe = Observable.just(NetUtils.isNetworkAvailable(mView.getAppContext()))
                .flatMap(hasNet -> {
                    if (hasNet) {
                        return deleteBellRecordFromServer(list)
                                .filter(success -> success)
                                .flatMap(success -> deleteBellRecordFromLocal(list, true));
                    } else {
                        return deleteBellRecordFromLocal(list, false);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .filter(result -> result)
                .subscribe(result -> mView.onDeleteBellRecordSuccess(list), Throwable::printStackTrace);
        registerSubscription(subscribe);
    }

    private Observable<Boolean> deleteBellRecordFromServer(List<BellCallRecordBean> list) {
        return Observable.just(list)
                .subscribeOn(Schedulers.io())
                .map(items -> {
                    ArrayList<JFGDPMsg> params = new ArrayList<>();
                    JFGDPMsg msg;
                    long seq = -1;
                    for (BellCallRecordBean bean : list) {
                        msg = new JFGDPMsg(DpMsgMap.ID_401_BELL_CALL_STATE, bean.version);
                        params.add(msg);
                    }
                    try {
                        seq = JfgCmdInsurance.getCmd().robotDelData(mUUID, params, 0);
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.d("刪除門鈴呼叫記錄失敗:" + e.getMessage());
                    }
                    AppLogger.d("正在刪除門鈴呼叫記錄, seq 為:" + seq);
                    return seq;
                })
                .filter(seq -> seq != -1)
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.DeleteDataRsp.class)
                        .filter(rsp -> rsp.seq == seq).first().timeout(10, TimeUnit.SECONDS))
                .observeOn(AndroidSchedulers.mainThread())
                .map(rsp -> rsp.resultCode == 0);
    }

    private Observable<Boolean> deleteBellRecordFromLocal(List<BellCallRecordBean> list, boolean confirm) {
        return Observable.from(list)
                .observeOn(Schedulers.io())
                .flatMap(record -> confirm ? BaseDPHelper.getInstance().deleteDPMsgWithConfirm(mUUID, record.version, DpMsgMap.ID_401_BELL_CALL_STATE)
                        : BaseDPHelper.getInstance().deleteDPMsgNotConfirm(mUUID, record.version, DpMsgMap.ID_401_BELL_CALL_STATE))
                .buffer(list.size())
                .map(items -> true);
    }

}
