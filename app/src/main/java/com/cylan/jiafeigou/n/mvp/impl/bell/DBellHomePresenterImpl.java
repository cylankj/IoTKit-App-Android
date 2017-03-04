package com.cylan.jiafeigou.n.mvp.impl.bell;

import com.cylan.jiafeigou.base.module.JFGDoorBellDevice;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPAction;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.bell.DoorBellHomeContract;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        JFGDoorBellDevice device = mSourceManager.getJFGDevice(mUUID);
        mView.onShowProperty(device);
    }

    @Override
    public void fetchBellRecordsList(boolean asc, long time) {
        Subscription subscribe = Observable.just(new DPEntity()
                .setMsgId(DpMsgMap.ID_401_BELL_CALL_STATE)
                .setVersion(time)
                .setAction(new IDPAction.DPQueryAction(asc, 20))
                .setUuid(mUUID))
                .flatMap(this::perform)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.getResultCode() == 0) {
                        mView.onRecordsListRsp(parse(result.getResultResponse()));
                        notifyBellLowBattery();
                    }
                }, e -> {
                    fetchBellRecordsList(asc, time);
                }, () -> mView.onRecordsListRsp(null));
        registerSubscription(subscribe);
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

    private List<IDPEntity> build(List<BellCallRecordBean> items) {
        List<IDPEntity> result = new ArrayList<>();
        IDPEntity entity;
        for (BellCallRecordBean item : items) {
            entity = new DPEntity()
                    .setMsgId(DpMsgMap.ID_401_BELL_CALL_STATE)
                    .setVersion(item.version)
                    .setUuid(mUUID)
                    .setAction(IDPAction.DELETED);
            result.add(entity);
        }
        return result;
    }

    @Override
    public void deleteBellCallRecord(List<BellCallRecordBean> list) {
        Subscription subscribe = Observable.just(build(list))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(this::perform)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.getResultCode() == 0) {
                        mView.onDeleteBellRecordSuccess(list);
                    }
                }, e -> {
                }, () -> {
                });
        registerSubscription(subscribe);
    }
}
