package com.cylan.jiafeigou.n.mvp.impl.bell;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.DoorBellHomeContract;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
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

    @Override
    public void fetchBellRecordsList(boolean asc, long time) {
        Subscription subscription = Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
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
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(items -> mView.onRecordsListRsp((ArrayList<BellCallRecordBean>) items), e -> fetchBellRecordsList(asc, time));
        registerSubscription(subscription);
    }

    private List<BellCallRecordBean> parse(TreeSet<DpMsgDefine.DPBellCallRecord> response) {
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
        Observable.just(list)
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
                .subscribe(rsp -> {
                    if (rsp.resultCode == 0) {//刪除成功了
                        AppLogger.d("刪除門鈴呼叫記錄成功");
                        mView.onDeleteBellRecordSuccess(list);
                    }
                }, e -> {
                    if (e instanceof TimeoutException) {
                        AppLogger.d("刪除門鈴呼叫記錄超時");
                        mView.onDeleteBellCallRecordFailed();
                    }
                });
    }

}
