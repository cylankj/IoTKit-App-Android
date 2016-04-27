package com.cylan.jiafeigou.activity.efamily;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.support.DswLog;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.activity.video.setting.Alarm;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.msg.EfamilyAlarmDeviceInfo;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.MsgEfamlGetSetAlarmParent;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgEfamilyGetAlarmReq;
import com.cylan.jiafeigou.entity.msg.req.MsgEfamilySetAlarmReq;
import com.cylan.jiafeigou.utils.CacheUtil;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.entity.AlarmInfo;

import java.util.ArrayList;
import java.util.List;

public class EFamilySafeProtectActivity extends BaseActivity implements OnClickListener, OnCheckedChangeListener {

    public static int TO_SET_TIME = 0x01;

    private LinearLayout mAlarmLayout;
    private TextView mTimeView;
    private MsgCidData mData;
    private AlarmInfo alarmInfo;

    private String key;

    private String[] sWeeks = null;
    private LinearLayout mEfamilySafe;
    private LinearLayout mEfamilyll;
    private LinearLayout mNoDevice;
    private List<AlarmInfo.CidList> mCidList;
    private LinearLayout addView;
    private ToggleButton safeToggle;
    private TextView safeState;
    private MsgEfamlGetSetAlarmParent mMsgEfamlGetSetAlarmParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_efamily_protection);
        setTitle(R.string.SECURE);

        mData = (MsgCidData) getIntent().getSerializableExtra(ClientConstants.CIDINFO);
        alarmInfo = getIntent().getParcelableExtra(EFamilySettingActivity.EXT_DATA);

        initView();

        if (sWeeks == null) {
            sWeeks = getResources().getStringArray(R.array.show_weeks);
        }

        key = CacheUtil.getMSG_EFAMILY_CONFIG_KEY(mData.cid);
        initDate();
    }

    private void dealWarmState() {
        if (mMsgEfamlGetSetAlarmParent.data.size() == 0) {
            mEfamilySafe.setVisibility(View.GONE);
            mEfamilyll.setVisibility(View.GONE);
            mNoDevice.setVisibility(View.VISIBLE);
        }
        for (int i = 0; i < mMsgEfamlGetSetAlarmParent.data.size(); i++) {
            addView = (LinearLayout) View.inflate(this, R.layout.activity_efamily_item_add_safe_state, null);
            safeState = (TextView) addView.findViewById(R.id.safe_state);
            safeToggle = (ToggleButton) addView.findViewById(R.id.safe_toggle);
            safeToggle.setTag(i);
            switch (mMsgEfamlGetSetAlarmParent.data.get(i).os) {
                case Constants.OS_MAGNET:
                    mEfamilySafe.addView(addView);
//                    safeState.setText(StringUtils.isEmptyOrNull(mMsgEfamlGetSetAlarmParent.data.get(i).alias) ?
//                            (mMsgEfamlGetSetAlarmParent.data.get(i).cid.substring(0, 2) + "**" + mMsgEfamlGetSetAlarmParent.data.get(i).cid.substring(mMsgEfamlGetSetAlarmParent.data.get(i).cid.length() - 2, mMsgEfamlGetSetAlarmParent.data.get(i).cid.length()))
//                            : mMsgEfamlGetSetAlarmParent.data.get(i).alias + getString(R.string.EFAMILY_SECURE_DOOR));
                    safeToggle.setChecked(mMsgEfamlGetSetAlarmParent.data.get(i).warn_enable != 0);
                    safeToggle.setOnCheckedChangeListener(this);
                    break;
                case Constants.OS_IR:
                    mEfamilySafe.addView(addView);
//                    safeState.setText(StringUtils.isEmptyOrNull(mMsgEfamlGetSetAlarmParent.data.get(i).alias) ?
//                            (mMsgEfamlGetSetAlarmParent.data.get(i).cid.substring(0, 2) + "**" + mMsgEfamlGetSetAlarmParent.data.get(i).cid.substring(mMsgEfamlGetSetAlarmParent.data.get(i).cid.length() - 2, mMsgEfamlGetSetAlarmParent.data.get(i).cid.length()))
//                            : mMsgEfamlGetSetAlarmParent.data.get(i).alias + getString(R.string.infared_smartSens));
                    safeToggle.setChecked(mMsgEfamlGetSetAlarmParent.data.get(i).warn_enable != 0);
                    safeToggle.setOnCheckedChangeListener(this);
                    break;
                case Constants.OS_TEMP_HUMI:
                    mEfamilySafe.addView(addView);
//                    safeState.setText(getString(R.string.EFAMILY_SECURE_TEMP));
                    safeToggle.setChecked(mMsgEfamlGetSetAlarmParent.data.get(i).warn_enable != 0);
                    safeToggle.setOnCheckedChangeListener(this);
                    break;
            }

        }
    }

    private void initDate() {
        MsgEfamlGetSetAlarmParent msgEfamlGetSetAlarmParent = (MsgEfamlGetSetAlarmParent) CacheUtil.readObject(key);
        if (msgEfamlGetSetAlarmParent == null) {
            getWarmConfig();
        } else {
            if (mData.vid >= msgEfamlGetSetAlarmParent.vid) {
                getWarmConfig();
            } else {
                onSuc(msgEfamlGetSetAlarmParent);
            }
        }
    }

    private void getWarmConfig() {

        if (!MyApp.getIsLogin()) {
            ToastUtil.showFailToast(EFamilySafeProtectActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
            return;
        }
        MsgEfamilyGetAlarmReq msgEfamilyGetAlarmReq = new MsgEfamilyGetAlarmReq(mData.cid);
        DswLog.i("send MsgEfamilyGetAlarmReq msg-->" + msgEfamilyGetAlarmReq.toString());
        MyApp.wsRequest(msgEfamilyGetAlarmReq.toBytes());

    }

    private void onSuc(MsgEfamlGetSetAlarmParent msgEfamlGetSetAlarmParent) {
        mProgressDialog.dismissDialog();
        mMsgEfamlGetSetAlarmParent = msgEfamlGetSetAlarmParent;
        dealWarmState();
        onAlarmChange();
    }


    private void initView() {
        mAlarmLayout = (LinearLayout) findViewById(R.id.warn_time);
        mTimeView = (TextView) findViewById(R.id.device_time);

        mEfamilySafe = (LinearLayout) findViewById(R.id.efamily_safe_protect);
        mNoDevice = (LinearLayout) findViewById(R.id.no_device_layout);
        mEfamilyll = (LinearLayout) findViewById(R.id.efamily_ll);

        mAlarmLayout.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.warn_time:
                startActivityForResult(new Intent(this, EFamilyAlarm.class).putExtra(ClientConstants.ALARMINFO, mMsgEfamlGetSetAlarmParent).putExtra("cid", mData.cid), TO_SET_TIME);
                break;
        }

    }

    public void onError(String msg) {
        mProgressDialog.dismissDialog();
        ToastUtil.showFailToast(this, msg);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        for (int i = 0; i < mMsgEfamlGetSetAlarmParent.data.size(); i++) {
            if (i == Integer.parseInt(buttonView.getTag().toString())) {
                switch (mMsgEfamlGetSetAlarmParent.data.get(i).os) {
                    case Constants.OS_TEMP_HUMI:
                        mMsgEfamlGetSetAlarmParent.data.get(i).warn_enable = isChecked ? 9 : 0;
                        submitSet();
                        break;
                    case Constants.OS_MAGNET:
                        mMsgEfamlGetSetAlarmParent.data.get(i).warn_enable = isChecked ? 11 : 0;
                        submitSet();
                        break;
                    case Constants.OS_IR:
                        mMsgEfamlGetSetAlarmParent.data.get(i).warn_enable = isChecked ? 10 : 0;
                        submitSet();
                        break;
                }
            }
        }
    }

    public void onAlarmChange() {

        String start = AlarmInfo.parse2Time(mMsgEfamlGetSetAlarmParent.warn_begin_time);
        String end = (mMsgEfamlGetSetAlarmParent.warn_begin_time > mMsgEfamlGetSetAlarmParent.warn_end_time ? getString(R.string.TOW) + " " : "")
                + AlarmInfo.parse2Time(mMsgEfamlGetSetAlarmParent.warn_end_time);
        StringBuffer days = new StringBuffer();
        int i = 0;
        for (; i < sWeeks.length; i++) {
            if (AlarmInfo.isSelectedDay(mMsgEfamlGetSetAlarmParent.warn_week, i))
                days.append(sWeeks[i] + " ");
        }
        int str = Alarm.getDaysHint(String.format("%07d", Integer.parseInt(Integer.toBinaryString(mMsgEfamlGetSetAlarmParent.warn_week), 10)));
        if (str == 0)
            mTimeView.setText(days + " " + ("0:00".equals(AlarmInfo.parse2Time(mMsgEfamlGetSetAlarmParent.warn_begin_time)) && "23:59".equals(AlarmInfo.parse2Time(mMsgEfamlGetSetAlarmParent.warn_end_time)) ? getResources().getString(R.string.HOURS) : start + "~" + end));
        else
            mTimeView.setText(getString(str) + " " + ("0:00".equals(AlarmInfo.parse2Time(mMsgEfamlGetSetAlarmParent.warn_begin_time)) && "23:59".equals(AlarmInfo.parse2Time(mMsgEfamlGetSetAlarmParent.warn_end_time)) ? getResources().getString(R.string.HOURS) : start + "~" + end));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == TO_SET_TIME) {
            MsgEfamlGetSetAlarmParent msgEfamlGetSetAlarmParent = (MsgEfamlGetSetAlarmParent) data.getSerializableExtra(ClientConstants.ALARMINFO);
            boolean isChange = true;
            if (msgEfamlGetSetAlarmParent.warn_begin_time == mMsgEfamlGetSetAlarmParent.warn_begin_time &&
                    msgEfamlGetSetAlarmParent.warn_end_time == mMsgEfamlGetSetAlarmParent.warn_end_time &&
                    msgEfamlGetSetAlarmParent.warn_week == mMsgEfamlGetSetAlarmParent.warn_week) {
                isChange = false;
            }
            mMsgEfamlGetSetAlarmParent.warn_begin_time = msgEfamlGetSetAlarmParent.warn_begin_time;
            mMsgEfamlGetSetAlarmParent.warn_end_time = msgEfamlGetSetAlarmParent.warn_end_time;
            mMsgEfamlGetSetAlarmParent.warn_week = msgEfamlGetSetAlarmParent.warn_week;

            onAlarmChange();
            if (isChange) {
                submitSet();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void submitSet() {
        String endTime = AlarmInfo.parse2Time(mMsgEfamlGetSetAlarmParent.warn_end_time);
        mMsgEfamlGetSetAlarmParent.warn_end_time = AlarmInfo.parse2Time(mMsgEfamlGetSetAlarmParent.warn_end_time).startsWith(getString(R.string.TOW)) ? AlarmInfo.parseTime(endTime.substring(endTime.length() - 5, endTime.length())) : mMsgEfamlGetSetAlarmParent.warn_end_time;

        MsgEfamilySetAlarmReq msgEfamilySetAlarmReq = new MsgEfamilySetAlarmReq(mData.cid);
        msgEfamilySetAlarmReq.warn_begin_time = mMsgEfamlGetSetAlarmParent.warn_begin_time;
        msgEfamilySetAlarmReq.warn_end_time = mMsgEfamlGetSetAlarmParent.warn_end_time;
        msgEfamilySetAlarmReq.warn_week = mMsgEfamlGetSetAlarmParent.warn_week;
        List<EfamilyAlarmDeviceInfo> list = new ArrayList<>();
        for (int i = 0; i < mMsgEfamlGetSetAlarmParent.data.size(); i++) {
            EfamilyAlarmDeviceInfo info = new EfamilyAlarmDeviceInfo();
            info.cid = mMsgEfamlGetSetAlarmParent.data.get(i).cid;
            info.warn_enable = mMsgEfamlGetSetAlarmParent.data.get(i).warn_enable;
            list.add(info);
        }
        msgEfamilySetAlarmReq.data = list;
        DswLog.i("send MsgEfamilySetAlarmReq msg-->" + msgEfamilySetAlarmReq.toString());
        MyApp.wsRequest(msgEfamilySetAlarmReq.toBytes());
    }


    @Override
    public void disconnectServer() {
        if (mProgressDialog.isShow()) {
            mProgressDialog.dismissDialog();
            ToastUtil.showFailToast(EFamilySafeProtectActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
        }
    }

    @Override
    public void handleMsgpackMsg(int msgId, MsgpackMsg.MsgHeader msgHeader) {
        DswLog.i("rev -->" + msgHeader.msgId);
        if (MsgpackMsg.CLIENT_EFAML_GET_ALARM_RSP == msgHeader.msgId) {
            RspMsgHeader rspMsgHeader = (RspMsgHeader) msgHeader;

            if (Constants.RETOK == rspMsgHeader.ret) {
                MsgEfamlGetSetAlarmParent msgEfamilyGetAlarmRsp = (MsgEfamlGetSetAlarmParent) msgHeader;
                if (msgEfamilyGetAlarmRsp.caller.equals(mData.cid)) {
                    CacheUtil.saveObject(msgEfamilyGetAlarmRsp, key);
                    onSuc(msgEfamilyGetAlarmRsp);
                }
            } else {
                onError(rspMsgHeader.msg);
            }
        } else if (MsgpackMsg.CLIENT_EFAML_SET_ALARM_RSP == msgHeader.msgId) {
            RspMsgHeader rspMsgHeader = (RspMsgHeader) msgHeader;
            mProgressDialog.dismissDialog();
            if (rspMsgHeader.ret == -1 || rspMsgHeader.ret == 1) {
                ToastUtil.showToast(this, rspMsgHeader.msg);
            } else {
                if (Constants.RETOK == rspMsgHeader.ret) {
                    MsgEfamlGetSetAlarmParent msgEfamilySetAlarmRsp = (MsgEfamlGetSetAlarmParent) msgHeader;
                    if (msgEfamilySetAlarmRsp.caller.equals(mData.cid)) {
                        mData.vid = msgEfamilySetAlarmRsp.vid;
                        CacheUtil.saveObject(msgEfamilySetAlarmRsp, key);
                        ToastUtil.showSuccessToast(this, getString(R.string.PWD_OK_2));
                    }
                } else {
                    ToastUtil.showFailToast(this, rspMsgHeader.msg);
                }

            }
        }

    }
}
