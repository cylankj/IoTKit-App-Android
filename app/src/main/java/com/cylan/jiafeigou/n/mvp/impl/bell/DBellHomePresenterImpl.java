package com.cylan.jiafeigou.n.mvp.impl.bell;

import android.util.Log;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.DoorBellHomeContract;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.TimeUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by cylan-hunt on 16-8-3.
 */
public class DBellHomePresenterImpl extends BasePresenter<DoorBellHomeContract.View>
        implements DoorBellHomeContract.Presenter {
    private boolean isFirst = true;
    /**
     * 凌晨0点时间戳
     */
    private static final long todayInMidNight = TimeUtils.getTodayStartTime();
    private static final long yesterdayInMidNight = todayInMidNight - 24 * 60 * 60 * 1000L;


    private void checkBatteryAndNotifyFirst() {
        if (isFirst) {
            mView.onBellBatteryDrainOut();
            isFirst = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        registerResponseParser(DpMsgMap.ID_401_BELL_CALL_STATE, this::onBellCallRecordRsp);
        checkBatteryAndNotifyFirst();
    }

    protected void onBellCallRecordRsp(String identity, JFGDPMsg... value) {
        ArrayList<BellCallRecordBean> result = new ArrayList<>(32);
        BellCallRecordBean callRecord;
        DpMsgDefine.BellCallState bell;
        for (JFGDPMsg msg : value) {
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
        mView.onRecordsListRsp(result);
    }

    private static final SimpleDateFormat getSimpleDateFormat
            = new SimpleDateFormat("MM月dd日", Locale.getDefault());

    private String getDate(long time) {
        return time >= todayInMidNight ? "今天"
                : (time < todayInMidNight && time > yesterdayInMidNight ? "昨天" : getSimpleDateFormat.format(new Date(time)));
    }


    @Override
    protected void onLoginStateChanged(RxEvent.LoginRsp loginState) {
        mView.onLoginState(loginState.state);
    }


    @Override
    public void fetchBellRecordsList(boolean asc, long time) {
        Log.e(TAG, "fetchBellRecordsList: ");
        try {
            JFGDPMsg request = new JFGDPMsg(DpMsgMap.ID_401_BELL_CALL_STATE, time);
            ArrayList<JFGDPMsg> params = new ArrayList<>();
            params.add(request);
            long seq = JfgCmdInsurance.getCmd().robotGetData(mUUID, params, 20, asc, 0);
            mRequestSeqs.add(seq);
        } catch (JfgException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getDeviceNetState() {
        int result = 0;
//        if (mBellInfo != null && mBellInfo.net != null) {
//            result = mBellInfo.net.net;
//        }
        return result;
    }


    @Override
    public void deleteBellCallRecord(List<BellCallRecordBean> list) {
        ArrayList<JFGDPMsg> params = new ArrayList<>(32);
        JFGDPMsg msg;
        for (BellCallRecordBean bean : list) {
            msg = new JFGDPMsg(DpMsgMap.ID_401_BELL_CALL_STATE, bean.version);
            params.add(msg);
        }
        JfgCmdInsurance.getCmd().robotDelData(mUUID, params, 0);
    }

//    private void wrapBellInfo(DeviceBean base) {
//        mBellInfo = new BeanBellInfo();
//        mBellInfo.deviceBase = new BaseBean();
//        mBellInfo.deviceBase.uuid = base.uuid;
//        mBellInfo.deviceBase.alias = base.alias;
//        mBellInfo.deviceBase.pid = base.pid;
//        mBellInfo.deviceBase.shareAccount = base.shareAccount;
//        mBellInfo.deviceBase.sn = base.sn;
//        mBellInfo.convert(mBellInfo.deviceBase, base.dataList);
//    }
}
