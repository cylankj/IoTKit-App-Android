package com.cylan.jiafeigou.n.mvp.impl.bell;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.dp.DP;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.DoorBellHomeContract;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.TimeUtils;

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
    /**
     * 凌晨0点时间戳
     */
    private static final long todayInMidNight = TimeUtils.getTodayStartTime();
    private static final long yesterdayInMidNight = todayInMidNight - 24 * 60 * 60 * 1000L;

    @Override
    public void onSetContentView() {
        super.onSetContentView();
        mView.onDeviceSyncRsp(mSourceManager.getJFGDevice(mUUID));
    }

    @Override
    protected void onRegisterResponseParser() {
        super.onRegisterResponseParser();
        registerResponseParser(DpMsgMap.ID_401_BELL_CALL_STATE, this::onBellCallRecordRsp);
    }


    protected void onBellCallRecordRsp(DP... response) {
        if (response == null) {
            mView.onRecordsListRsp(null);
            return;
        }
        ArrayList<BellCallRecordBean> result = new ArrayList<>(32);
        BellCallRecordBean callRecord;
        DpMsgDefine.BellCallState bell;
        for (DP value : response) {
            if (!(value instanceof DpMsgDefine.BellCallState)) continue;
            bell = (DpMsgDefine.BellCallState) value;
            callRecord = new BellCallRecordBean();
            callRecord.answerState = bell.isOK;
            callRecord.timeInLong = bell.time * 1000L;
            callRecord.timeStr = TimeUtils.getHH_MM(bell.time * 1000L);
            callRecord.date = getDate(bell.time * 1000L);
            callRecord.type = bell.type;
            callRecord.version = value.version;
            result.add(callRecord);
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
    public void fetchBellRecordsList(boolean asc, long time) {
        AppLogger.d("fetchBellRecordsList:" + asc + time);
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
    public void deleteBellCallRecord(List<BellCallRecordBean> list) {
        ArrayList<JFGDPMsg> params = new ArrayList<>(32);
        JFGDPMsg msg;
        for (BellCallRecordBean bean : list) {
            msg = new JFGDPMsg(DpMsgMap.ID_401_BELL_CALL_STATE, bean.version);
            params.add(msg);
        }
        JfgCmdInsurance.getCmd().robotDelData(mUUID, params, 0);
    }
}
