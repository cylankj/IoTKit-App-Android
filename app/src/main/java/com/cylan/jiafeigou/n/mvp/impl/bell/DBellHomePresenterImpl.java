package com.cylan.jiafeigou.n.mvp.impl.bell;

import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.DoorBellHomeContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BaseBean;
import com.cylan.jiafeigou.n.mvp.model.BeanBellInfo;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.TimeUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by cylan-hunt on 16-8-3.
 */
public class DBellHomePresenterImpl extends AbstractPresenter<DoorBellHomeContract.View>
        implements DoorBellHomeContract.Presenter {
    private boolean isFirst = true;

    public DBellHomePresenterImpl(DoorBellHomeContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    /**
     * 凌晨0点时间戳
     */
    private static final long todayInMidNight = TimeUtils.getTodayStartTime();
    private static final long yesterdayInMidNight = todayInMidNight - 24 * 60 * 60 * 1000L;

    private BeanBellInfo mBellInfo;
    private List<Long> mSeqList = new ArrayList<>(32);

    @Override
    public void start() {
        compositeSubscription.add(onLogStateSubscription());
        compositeSubscription.add(onBellBatteryState());
        compositeSubscription.add(onBellCallListSubscription());
        checkBatteryAndNotifyFirst();
    }

    private void checkBatteryAndNotifyFirst() {
        if (mBellInfo.battery < 80 && isFirst) {
            mView.onBellBatteryDrainOut();
            isFirst = false;
        }
    }

    private Subscription onBellCallListSubscription() {
        return RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                .subscribeOn(Schedulers.io())
                .filter(rsp -> mSeqList.remove(rsp.seq))
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    ArrayList<JFGDPMsg> msgs = response.map.get(DpMsgMap.ID_401_BELL_CALL_STATE);
                    ArrayList<BellCallRecordBean> result = new ArrayList<>(32);
                    BellCallRecordBean callRecord;
                    DpMsgDefine.BellCallState bell;
                    for (JFGDPMsg msg : msgs) {
                        try {
                            bell = DpUtils.unpackData(msg.packValue, DpMsgDefine.BellCallState.class);
                            callRecord = new BellCallRecordBean();
                            callRecord.answerState = bell.isOK;
                            callRecord.timeInLong = bell.time * 1000L;
                            callRecord.timeStr = TimeUtils.getHH_MM(bell.time * 1000L);
                            callRecord.date = getDate(bell.time * 1000L);
                            callRecord.type = bell.type;
                            callRecord.version = msg.version;
                            result.add(callRecord);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return result;
                })
                .subscribe(results -> {
                    mView.onRecordsListRsp(results);
                });
    }

    private static final SimpleDateFormat getSimpleDateFormat
            = new SimpleDateFormat("MM月dd日", Locale.getDefault());

    private String getDate(long time) {
        return time >= todayInMidNight ? "今天"
                : (time < todayInMidNight && time > yesterdayInMidNight ? "昨天" : getSimpleDateFormat.format(new Date(time)));
    }

    /**
     * 查询登陆状态
     *
     * @return
     */
    private Subscription onLogStateSubscription() {
        return RxBus.getCacheInstance().toObservable(RxEvent.LoginRsp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.LoginRsp>() {
                    @Override
                    public void call(RxEvent.LoginRsp o) {
                        if (getView() != null)
                            getView().onLoginState(o.state);
                    }
                });
    }

    private Subscription onBellBatteryState() {
        return RxBus.getCacheInstance().toObservable(RxEvent.JFGRobotSyncData.class)
                .subscribeOn(Schedulers.io())
                .map(rsp -> {
                    if (!TextUtils.equals(mBellInfo.deviceBase.uuid, rsp.identity)) return null;
                    for (JFGDPMsg msg : rsp.dataList) {
                        if (msg.id == DpMsgMap.ID_206_BATTERY) {
                            return msg;
                        }
                    }
                    return null;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(msg -> {
                            if (msg == null) return;
                            try {
                                DpMsgDefine.MsgBattery battery = DpUtils.unpackData(msg.packValue, DpMsgDefine.MsgBattery.class);
                                mBellInfo.battery = battery.battery;
                                if (mBellInfo.battery < 20) {
                                    mView.onBellBatteryDrainOut();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                );

    }

    @Override
    public void stop() {
        unSubscribe(compositeSubscription);
    }

    @Override
    public void fetchBellRecordsList(boolean asc, long time) {
        Log.e(TAG, "fetchBellRecordsList: ");
        try {
            JFGDPMsg request = new JFGDPMsg(DpMsgMap.ID_401_BELL_CALL_STATE, time);
            ArrayList<JFGDPMsg> params = new ArrayList<>();
            params.add(request);
            long seq = JfgCmdInsurance.getCmd().robotGetData(mBellInfo.deviceBase.uuid, params, 20, asc, 0);
            mSeqList.add(seq);
        } catch (JfgException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getDeviceNetState() {
        return mBellInfo.net.net;
    }

    @Override
    public void setBellInfo(DeviceBean bean) {
        wrapBellInfo(bean);
    }

    @Override
    public BeanBellInfo getBellInfo() {
        return mBellInfo;
    }

    @Override
    public void deleteBellCallRecord(List<BellCallRecordBean> list) {
        ArrayList<JFGDPMsg> params = new ArrayList<>(32);
        JFGDPMsg msg;
        for (BellCallRecordBean bean : list) {
            msg = new JFGDPMsg(DpMsgMap.ID_401_BELL_CALL_STATE, bean.version);
            params.add(msg);
        }
        JfgCmdInsurance.getCmd().robotDelData(mBellInfo.deviceBase.uuid, params, 0);
    }

    private void wrapBellInfo(DeviceBean base) {
        mBellInfo = new BeanBellInfo();
        mBellInfo.deviceBase = new BaseBean();
        mBellInfo.deviceBase.uuid = base.uuid;
        mBellInfo.deviceBase.alias = base.alias;
        mBellInfo.deviceBase.pid = base.pid;
        mBellInfo.deviceBase.shareAccount = base.shareAccount;
        mBellInfo.deviceBase.sn = base.sn;
        mBellInfo.convert(mBellInfo.deviceBase, base.dataList);
    }
}
