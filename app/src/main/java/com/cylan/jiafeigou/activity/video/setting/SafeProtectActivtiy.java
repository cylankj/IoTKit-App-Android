package com.cylan.jiafeigou.activity.video.setting;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.support.DswLog;
import com.cylan.publicApi.JniPlay;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.MsgCidGetSetParent;
import com.cylan.jiafeigou.entity.msg.MsgSceneData;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgCidGetReq;
import com.cylan.jiafeigou.entity.msg.req.MsgCidSetReq;
import com.cylan.jiafeigou.entity.msg.rsp.MsgCidlistRsp;
import com.cylan.jiafeigou.utils.CacheUtil;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.wheel.OnWheelChangedListener;
import com.cylan.jiafeigou.widget.wheel.WheelView;
import com.cylan.jiafeigou.widget.wheel.adapters.MyWheelViewAdapter;
import com.cylan.entity.AlarmInfo;
import com.tencent.stat.StatService;

public class SafeProtectActivtiy extends BaseActivity implements OnClickListener, OnCheckedChangeListener, View.OnTouchListener {

    public static int TO_SET_TIME = 0x01;
    public static int TO_SET_VOICE = 0x02;
    public static int TO_SET_SENS = 0x03;

    private ToggleButton mSafeProBtn;
    private LinearLayout mSafeRemindLayout;
    private TextView mDeviceView;
    private TextView mTimeView;
    private TextView mSensValue;
    private MsgCidData info;
    private AlarmInfo alarmInfo;
    private ImageView mSensDot;

    private String key;

    private String[] sWeeks = null;

    private String[] mVoiceList = null;

    private String[] valueList;

    private NotifyDialog mWarnEnableDialog;
    private WheelView pickerView;
    private Dialog sensDialog;
    private MyWheelViewAdapter mSensAdapter;
    private LinearLayout mSensitivityLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_protection);
        setTitle(R.string.SECURE);

        info = (MsgCidData) getIntent().getSerializableExtra(ClientConstants.CIDINFO);

        LinearLayout mAlarmLayout = (LinearLayout) findViewById(R.id.alarm);
        mSafeProBtn = (ToggleButton) findViewById(R.id.toggle_safepro);
        mSafeProBtn.setOnTouchListener(this);
        mSafeRemindLayout = (LinearLayout) findViewById(R.id.safe_voiceandtime);
        mSafeRemindLayout.setVisibility(View.GONE);
        LinearLayout mDeviceVoiceLayout = (LinearLayout) findViewById(R.id.device_voice);
        mSensitivityLayout = (LinearLayout) findViewById(R.id.sensitivity);
        mSensValue = (TextView) findViewById(R.id.sens_value);
        mDeviceView = (TextView) findViewById(R.id.device_select_voice);
        mTimeView = (TextView) findViewById(R.id.device_time);
        mSensDot = (ImageView) findViewById(R.id.sens_dot);

        mAlarmLayout.setOnClickListener(this);
        mDeviceVoiceLayout.setOnClickListener(this);
        mSensitivityLayout.setOnClickListener(this);

        if (valueList == null) {
            valueList = getResources().getStringArray(R.array.show_sensitive);
        }

        if (mVoiceList == null) {
            mVoiceList = getResources().getStringArray(R.array.device_set_voice_name);
        }
        if (sWeeks == null) {
            sWeeks = getResources().getStringArray(R.array.show_weeks);
        }
        key = CacheUtil.getMSG_VIDEO_CONFIG_KEY(info.cid);
        MsgCidGetSetParent msgCidGetSetParent = (MsgCidGetSetParent) CacheUtil.readObject(key);
        if (msgCidGetSetParent == null) {
            getWarmConfig();
        } else {
            if (info.vid > msgCidGetSetParent.vid) {
                getWarmConfig();
            } else {
                onSuc(msgCidGetSetParent);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PreferenceUtil.getKeyFirstSen(this)) {
            mSensDot.setVisibility(View.GONE);
        }
        StatService.trackBeginPage(this, "SafeProtect");

    }

    @Override
    protected void onPause() {
        super.onPause();
        StatService.trackEndPage(this, "SafeProtect");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.alarm:
                if (alarmInfo == null)
                    return;
                startActivityForResult(new Intent(this, Alarm.class).putExtra(ClientConstants.ALARMINFO, alarmInfo), TO_SET_TIME);
                break;
            case R.id.device_voice:
                if (alarmInfo == null)
                    return;
                startActivityForResult(new Intent(this, DeviceVoiceActivity.class).putExtra(ClientConstants.ALARMINFO, alarmInfo), TO_SET_VOICE);
                break;
            case R.id.sensitivity:
                if (alarmInfo == null)
                    return;
                PreferenceUtil.setKeyFirstSen(this, true);
                startActivityForResult(new Intent(this, SensitiveActivity.class).putExtra(ClientConstants.SELECT_SENS_INDEX, alarmInfo.sensitivity), TO_SET_SENS);
                break;
            case R.id.confirm:
                sensDialog.dismiss();
                mSensValue.setText(valueList[pickerView.getCurrentItem()]);
                break;
        }

    }

    private void getWarmConfig() {
        if (!MyApp.getIsLogin()) {
            ToastUtil.showFailToast(SafeProtectActivtiy.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
            return;
        }

        MsgCidGetReq msgCidGetReq = new MsgCidGetReq();
        msgCidGetReq.cid = info.cid;
        MyApp.wsRequest(msgCidGetReq.toBytes());
        mProgressDialog.showDialog(getString(R.string.getting));
        DswLog.i("send MsgCidGetReq msg-->" + msgCidGetReq.toString());
    }

    @Override
    public void handleMsgpackMsg(int msg, MsgpackMsg.MsgHeader msgpackMsg) {
        mProgressDialog.dismissDialog();
        if (MsgpackMsg.CLIENT_CIDGET_RSP == msgpackMsg.msgId) {
            RspMsgHeader rspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (Constants.RETOK == rspMsgHeader.ret) {
                MsgCidGetSetParent msgCidGetRsp = (MsgCidGetSetParent) msgpackMsg;
                if (msgCidGetRsp.cid.equals(info.cid)) {
                    CacheUtil.saveObject(msgCidGetRsp, key);
                    onSuc(msgCidGetRsp);
                }
            } else {
                onError(rspMsgHeader.msg);
            }
        } else if (MsgpackMsg.CLIENT_CIDSET_RSP == msgpackMsg.msgId) {
            RspMsgHeader rspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (rspMsgHeader.ret == -1 || rspMsgHeader.ret == 1) {
                ToastUtil.showToast(this, rspMsgHeader.msg);
            } else {
                if (Constants.RETOK == rspMsgHeader.ret) {
                    MsgCidGetSetParent msgCidSetRsp = (MsgCidGetSetParent) msgpackMsg;
                    if (msgCidSetRsp.cid.equals(info.cid)) {
                        info.vid = msgCidSetRsp.vid;
                        CacheUtil.saveObject(msgCidSetRsp, key);
                        ToastUtil.showSuccessToast(this, getString(R.string.PWD_OK_2));
                        onSuc(msgCidSetRsp);
                    }
                } else {
                    ToastUtil.showFailToast(this, rspMsgHeader.msg);
                }
            }
        }
    }

    public void onSuc(MsgCidGetSetParent msgCidGetSetParent) {
        mProgressDialog.dismissDialog();
        alarmInfo = new AlarmInfo();
        alarmInfo.cid = msgCidGetSetParent.cid;
        alarmInfo.vid = msgCidGetSetParent.vid;
        alarmInfo.timezonestr = msgCidGetSetParent.timezonestr;
        alarmInfo.isEnabled = msgCidGetSetParent.warn_enable == 1;
        alarmInfo.startTime = AlarmInfo.parse2Time(msgCidGetSetParent.warn_begin_time);
        alarmInfo.endTime = AlarmInfo.parse2Time(msgCidGetSetParent.warn_end_time);
        alarmInfo.days = msgCidGetSetParent.warn_week;
        alarmInfo.isLedOpen = msgCidGetSetParent.led == 1;
        alarmInfo.sound = msgCidGetSetParent.sound;
        alarmInfo.direction = msgCidGetSetParent.direction;
        alarmInfo.sound_long = msgCidGetSetParent.sound_long == 0 ? 1 : msgCidGetSetParent.sound_long;
        alarmInfo.auto_record = msgCidGetSetParent.auto_record;
        alarmInfo.sensitivity = msgCidGetSetParent.sensitivity;
        alarmInfo.isNTSC = msgCidGetSetParent.isNTSC == 1;
        alarmInfo.isMobile = msgCidGetSetParent.isMobile == 1;
        mSafeProBtn.setChecked(alarmInfo.isEnabled);
        if (alarmInfo.isEnabled) {
            mSafeRemindLayout.setVisibility(View.VISIBLE);
            mSensitivityLayout.setVisibility(View.VISIBLE);
        } else {
            mSafeRemindLayout.setVisibility(View.GONE);
            mSensitivityLayout.setVisibility(View.GONE);
        }
        mDeviceView.setText(mVoiceList[alarmInfo.sound]);
        mSensValue.setText(valueList[alarmInfo.sensitivity]);
        onAlarmChange();
        //2.4.2新需求
        if (MsgCidlistRsp.getInstance().isSomeoneMode(info.cid, MsgSceneData.MODE_HOME_IN)) {
            mSafeProBtn.setChecked(false);
            mSafeRemindLayout.setVisibility(View.GONE);
            mSafeProBtn.setBackgroundResource(R.drawable.ico_switch_close_unenable);
            mSensitivityLayout.setVisibility(View.GONE);
        } else if (MsgCidlistRsp.getInstance().isSomeoneMode(info.cid, MsgSceneData.MODE_HOME_OUT)) {
            mSafeProBtn.setChecked(true);
            mSafeRemindLayout.setVisibility(View.GONE);
            mSafeProBtn.setBackgroundResource(R.drawable.ico_switch_open_unenable);
            mSensitivityLayout.setVisibility(View.VISIBLE);
        }

        mSafeProBtn.setOnCheckedChangeListener(this);
    }

    public void onError(String msg) {
        mProgressDialog.dismissDialog();
        ToastUtil.showFailToast(this, msg);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.toggle_safepro:
                if (isChecked) {
                    mSafeRemindLayout.setVisibility(View.VISIBLE);
                    mSensitivityLayout.setVisibility(View.VISIBLE);
                } else {
                    mSafeRemindLayout.setVisibility(View.GONE);
                    mSensitivityLayout.setVisibility(View.GONE);
                }
                submitSet();
                break;

        }

    }

    public void onAlarmChange() {

        String start = alarmInfo.startTime;
        String end = (AlarmInfo.parseTime(alarmInfo.startTime) > AlarmInfo.parseTime(alarmInfo.endTime) ? getString(R.string.TOW) + " " : "")
                + alarmInfo.endTime;
        StringBuffer days = new StringBuffer();
        int i = 0;
        for (; i < sWeeks.length; i++) {
            if (AlarmInfo.isSelectedDay(alarmInfo.days, i))
                days.append(sWeeks[i] + " ");
        }
        int str = Alarm.getDaysHint(String.format("%07d", Integer.parseInt(Integer.toBinaryString(alarmInfo.days), 10)));
        if (str == 0)
            mTimeView.setText(days + " " + (alarmInfo.isAllDay() ? getResources().getString(R.string.HOURS) : start + "~" + end));
        else
            mTimeView.setText(getString(str) + " " + (alarmInfo.isAllDay() ? getResources().getString(R.string.HOURS) : start + "~" + end));
    }

    @Override
    public void disconnectServer() {
        if (mProgressDialog.isShow()) {
            mProgressDialog.dismissDialog();
            ToastUtil.showFailToast(SafeProtectActivtiy.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == TO_SET_TIME) {
            AlarmInfo mAlarmInfo = data.getParcelableExtra(ClientConstants.ALARMINFO);
            boolean isChange = true;
            if (mAlarmInfo.startTime.equals(alarmInfo.startTime) && mAlarmInfo.endTime.equals(alarmInfo.endTime) && mAlarmInfo.days == alarmInfo.days) {
                isChange = false;
            }
            alarmInfo.startTime = mAlarmInfo.startTime;
            alarmInfo.endTime = mAlarmInfo.endTime;
            alarmInfo.days = mAlarmInfo.days;

            onAlarmChange();
            if (isChange) {
                submitSet();
            }
        } else if (requestCode == TO_SET_VOICE) {
            boolean isVoiceChange = true;
            AlarmInfo datainfo=data.getParcelableExtra(ClientConstants.ALARMINFO);
            if (alarmInfo.sound == datainfo.sound && alarmInfo.sound_long == datainfo.sound_long) {
                isVoiceChange = false;
            }
            alarmInfo.sound = datainfo.sound;
            alarmInfo.sound_long = datainfo.sound_long;
            mDeviceView.setText(mVoiceList[alarmInfo.sound]);
            if (isVoiceChange) {
                submitSet();
            }
        } else if (requestCode == TO_SET_SENS) {
            boolean isSensChange = true;
            if (alarmInfo.sensitivity == data.getIntExtra(ClientConstants.SELECT_SENS_INDEX, alarmInfo.sensitivity)) {
                isSensChange = false;
            }
            alarmInfo.sensitivity = data.getIntExtra(ClientConstants.SELECT_SENS_INDEX, alarmInfo.sensitivity);
            mSensValue.setText(valueList[alarmInfo.sensitivity]);
            if (isSensChange) {
                submitSet();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void submitSet() {

        alarmInfo.endTime = alarmInfo.endTime.startsWith(getString(R.string.TOW)) ? alarmInfo.endTime.substring(alarmInfo.endTime.length() - 5, alarmInfo.endTime.length()) : alarmInfo.endTime;
        alarmInfo.isEnabled = mSafeProBtn.isChecked();

        MsgCidSetReq msgCidSetReq = new MsgCidSetReq();
        msgCidSetReq.cid = info.cid;
        msgCidSetReq.warn_begin_time = AlarmInfo.parseTime(alarmInfo.startTime);
        msgCidSetReq.warn_end_time = AlarmInfo.parseTime(alarmInfo.endTime);
        msgCidSetReq.warn_enable = alarmInfo.isEnabled ? 1 : 0;
        msgCidSetReq.warn_week = alarmInfo.days;
        msgCidSetReq.sound = alarmInfo.sound;
        msgCidSetReq.led = alarmInfo.isLedOpen ? 1 : 0;
        msgCidSetReq.direction = alarmInfo.direction;
        msgCidSetReq.sound_long = alarmInfo.sound_long;
        msgCidSetReq.timezonestr = alarmInfo.timezonestr;
        msgCidSetReq.auto_record = alarmInfo.auto_record;
        msgCidSetReq.sensitivity = alarmInfo.sensitivity;
        msgCidSetReq.isNTSC = alarmInfo.isNTSC ? 1 : 0;
        msgCidSetReq.isMobile = alarmInfo.isMobile ? 1 : 0;

        JniPlay.SendBytes(msgCidSetReq.toBytes());

        DswLog.i("send MsgCidSetReq--->" + msgCidSetReq.toString());
    }


    private void showWarnEnableChangeDialog() {
        if (mWarnEnableDialog == null)
            mWarnEnableDialog = new NotifyDialog(this);
        mWarnEnableDialog.setButtonText(R.string.WELL_OK, R.string.CANCEL);
        mWarnEnableDialog.show(R.string.SECURE_ALARM_CLOSE, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {

                    case R.id.confirm:
                        mWarnEnableDialog.dismiss();
                        mSafeProBtn.setChecked(false);
                        break;
                    case R.id.cancel:
                        mWarnEnableDialog.dismiss();
                        break;
                }

            }
        }, null);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!MsgCidlistRsp.getInstance().isSomeoneMode(info.cid, MsgSceneData.MODE_STANDARD)) {
                return true;
            }
            if (mSafeProBtn.isChecked() && alarmInfo != null) {
                if (info.sdcard == 0)
                    return false;
                if (alarmInfo.auto_record == ClientConstants.AUTO_RECORD1) {
                    showWarnEnableChangeDialog();
                    return true;
                }
            }
        }
        return false;
    }




    private void pickupSens() {
        if (sensDialog == null) {
            sensDialog = new Dialog(this, R.style.func_custom_dialog);
            View content = View.inflate(this, R.layout.dialog_pickup_time, null);
            LinearLayout dataLayout = (LinearLayout) content.findViewById(R.id.picker_date_layout);
            LinearLayout sensLayout = (LinearLayout) content.findViewById(R.id.picker_sens_layout);
            dataLayout.setVisibility(View.GONE);
            sensLayout.setVisibility(View.VISIBLE);
            TextView cancel = (TextView) content.findViewById(R.id.cancle);
            cancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    sensDialog.dismiss();
                }
            });
            TextView confirm = (TextView) content.findViewById(R.id.confirm);
            confirm.setOnClickListener(this);
            pickerView = (WheelView) content.findViewById(R.id.picker_sens);
            pickerView.addChangingListener(new OnWheelChangedListener() {
                @Override
                public void onChanged(WheelView wheel, int oldValue, int newValue) {
                    if (mSensAdapter != null) {
                        mSensAdapter.setCurrent(newValue);
                        mSensAdapter.notifyDataSetChanged();
                    }
                }
            });
            mSensAdapter = new MyWheelViewAdapter(this, valueList);
            pickerView.setViewAdapter(mSensAdapter);

            sensDialog.setContentView(content);
            sensDialog.setOwnerActivity(SafeProtectActivtiy.this);
            sensDialog.setCanceledOnTouchOutside(true);
        }
        pickerView.setCurrentItem(1);
        try {
            sensDialog.show();
            WindowManager windowManager = getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            WindowManager.LayoutParams lp = sensDialog.getWindow().getAttributes();
            lp.width = display.getWidth(); //
            sensDialog.getWindow().setAttributes(lp);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }


}