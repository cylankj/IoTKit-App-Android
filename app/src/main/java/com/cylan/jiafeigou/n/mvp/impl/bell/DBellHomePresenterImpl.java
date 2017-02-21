package com.cylan.jiafeigou.n.mvp.impl.bell;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.DoorBellHomeContract;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.TimeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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


    protected void onBellCallRecordRsp(DataPoint... response) {
        if (response == null) {
            mView.onRecordsListRsp(null);
            return;
        }
        ArrayList<BellCallRecordBean> result = new ArrayList<>(32);
        BellCallRecordBean callRecord;
        DpMsgDefine.DPBellCallRecord bell;
        for (DataPoint value : response) {
            if (!(value instanceof DpMsgDefine.DPBellCallRecord)) continue;
            bell = (DpMsgDefine.DPBellCallRecord) value;
            callRecord = new BellCallRecordBean();
            callRecord.answerState = bell.isOK;
            callRecord.timeInLong = bell.time * 1000L;
            callRecord.timeStr = TimeUtils.getHH_MM(bell.time * 1000L);
            callRecord.date = TimeUtils.getBellRecordTime(bell.time * 1000L);
            callRecord.type = bell.type;
            callRecord.version = value.version;
            result.add(callRecord);
        }
        mView.onRecordsListRsp(result);

//        if (LoadingDialog.isShowing(getSupportFragmentManager())) {
//            LoadingDialog.dismissLoading(getSupportFragmentManager());
//            ToastUtil.showNegativeToast(getString(R.string.REQUEST_TIME_OUT));
//        }
    }

    @Override
    public void fetchBellRecordsList(boolean asc, long time) {
        Subscription subscription = Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            JFGDPMsg request = new JFGDPMsg(DpMsgMap.ID_401_BELL_CALL_STATE, time);
            ArrayList<JFGDPMsg> params = new ArrayList<>();
            params.add(request);
            try {
                long seq = JfgCmdInsurance.getCmd().robotGetData(mUUID, params, 20, asc, 0);
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                e.printStackTrace();
                AppLogger.e(e.getMessage());
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                        .filter(rsp -> rsp.seq == seq).first().timeout(10, TimeUnit.SECONDS)
                )
                .map(rsp -> cacheItems(rsp.map.get(DpMsgMap.ID_401_BELL_CALL_STATE)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(items -> {
                    if (items == null) {
                        mView.onQueryRecordListTimeOut();
                    } else {
                        mView.onRecordsListRsp((ArrayList<BellCallRecordBean>) items);
                    }
                }, e -> {
                    mView.onQueryRecordListTimeOut();
                });
        registerSubscription(subscription);
    }

    private List<BellCallRecordBean> cacheItems(ArrayList<JFGDPMsg> response) {
        List<BellCallRecordBean> result = null;
        if (response == null) {
            return result;
        }
        result = new ArrayList<>();
        BellCallRecordBean record;
        DpMsgDefine.DPBellCallRecord bellCallRecord;
        for (JFGDPMsg msg : response) {
            if (!contain(msg.version)) {
                try {
                    bellCallRecord = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPBellCallRecord.class);
                    if (bellCallRecord != null) {
                        bellCallRecord.version = msg.version;
                        record = BellCallRecordBean.parse(bellCallRecord);
                        result.add(record);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        mRecords.addAll(result);
        return result;
    }

    private boolean contain(long version) {
        for (BellCallRecordBean bean : mRecords) {
            if (bean.version == version)
                return true;
        }
        return false;
    }

    @Override
    public void deleteBellCallRecord(List<BellCallRecordBean> list) {
        ArrayList<JFGDPMsg> params = new ArrayList<>(32);
        JFGDPMsg msg;
        for (BellCallRecordBean bean : list) {
            msg = new JFGDPMsg(DpMsgMap.ID_401_BELL_CALL_STATE, bean.version);
            params.add(msg);
        }
        robotDelDataAsync(mUUID, params, 0);
    }

}
