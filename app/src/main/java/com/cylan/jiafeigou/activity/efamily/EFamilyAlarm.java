package com.cylan.jiafeigou.activity.efamily;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.DswLog;
import com.cylan.jiafeigou.activity.video.setting.AlarmDaysSelectActivity;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.msg.MsgEfamlGetSetAlarmParent;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.wheel.OnWheelClickedListener;
import com.cylan.jiafeigou.widget.wheel.WheelView;
import com.cylan.jiafeigou.widget.wheel.adapters.NumericWheelAdapter;
import com.cylan.utils.entity.AlarmInfo;

public class EFamilyAlarm extends BaseActivity implements View.OnClickListener {

    public static final int HANDLE_GET = 0x02;
    public static final int HANDLE_SET = 0x03;

    // onActivityResult
    private static final int TO_SET_DAYS = 0;

    View mDays;// mComplete, mTimes, , mAccounts

    TextView mTimesText, mAccountsText, mDaysText;

    String mCid;

    static String[] sWeeks = null;

    // RequestHandler mHandler;

    TextView mStartTime, mEndTime;

    private Dialog mTimeDialog;

    private WheelView hours;
    private WheelView mins;

    private int flag = 0;

    int week = 0;

    private Boolean isLoad = false;

    private MsgEfamlGetSetAlarmParent mMsgEfamlGetSetAlarmParent;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.ly_alarm);

//        setTitle(R.string.TIME);
        setBackBtnOnClickListener(this);
        mDays = findViewById(R.id.days);
        mDays.setOnClickListener(this);
        mDaysText = (TextView) findViewById(R.id.alarm_days);
        findViewById(R.id.alarm_start).setOnClickListener(this);
        findViewById(R.id.alarm_end).setOnClickListener(this);
        mStartTime = (TextView) findViewById(R.id.alarm_start_time);
        mEndTime = (TextView) findViewById(R.id.alarm_end_time);
        mDays.setOnClickListener(this);
        mMsgEfamlGetSetAlarmParent = (MsgEfamlGetSetAlarmParent) getIntent().getSerializableExtra(ClientConstants.ALARMINFO);
        mCid = getIntent().getStringExtra("cid");
        if (sWeeks == null) {
            sWeeks = getResources().getStringArray(R.array.show_weeks);
        }
        if (mMsgEfamlGetSetAlarmParent != null) {
            setSelectText();
            onAlarmChange();
        }
    }

    private void setSelectText() {
        StringBuffer days = new StringBuffer();
        for (int i = 0; i < sWeeks.length; i++) {
            if (AlarmInfo.isSelectedDay(mMsgEfamlGetSetAlarmParent.warn_week, i))
                days.append(sWeeks[i] + " ");
        }
        int str = EFamilyAlarm.getDaysHint(String.format("%07d", Integer.parseInt(Integer.toBinaryString(mMsgEfamlGetSetAlarmParent.warn_week), 10)));

        if (str == 0)
            mDaysText.setText(days);
        else
            mDaysText.setText(getString(str));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ico_back:

                onBackPressed();
                break;

            case R.id.confirm:
                mTimeDialog.dismiss();

                if (flag == 0) {
                    mStartTime.setText(String.format("%02d", hours.getCurrentItem()) + ":" + String.format("%02d", mins.getCurrentItem()));
                } else {
                    String start = mStartTime.getText().toString();
                    String end = String.format("%02d", hours.getCurrentItem()) + ":" + String.format("%02d", mins.getCurrentItem());
                    mEndTime.setText((AlarmInfo.parseTime(start) > AlarmInfo.parseTime(end) ? getString(R.string.TOW) + " " : "") + end);// compareTo

                }
                break;
            case R.id.alarm_start:

                String fromtime = mStartTime.getText().toString();
                // return format != null ? String.format(format, value) :
                // Integer.toString(value);
                pickupTime(Integer.parseInt(fromtime.split(":")[0]), Integer.parseInt(fromtime.split(":")[1]));
                flag = 0;
                break;
            case R.id.alarm_end:

                String time = mEndTime.getText().toString();
                String endtime = time.startsWith(getString(R.string.TOW)) ? time.substring(time.length() - 5, time.length()) : time;

                pickupTime(Integer.parseInt(endtime.split(":")[0]), Integer.parseInt(endtime.split(":")[1]));
                flag = 1;
                break;
            case R.id.days:

                startActivityForResult(new Intent(EFamilyAlarm.this, AlarmDaysSelectActivity.class).putExtra(ClientConstants.ALARM_WEEKS, mMsgEfamlGetSetAlarmParent.warn_week), TO_SET_DAYS);
                break;
        }

    }

    public void onAlarmChange() {

        mStartTime.setText(AlarmInfo.parse2Time(mMsgEfamlGetSetAlarmParent.warn_begin_time));
        mEndTime.setText((mMsgEfamlGetSetAlarmParent.warn_begin_time > mMsgEfamlGetSetAlarmParent.warn_end_time ? getString(R.string.TOW) : "") + AlarmInfo.parse2Time(mMsgEfamlGetSetAlarmParent.warn_end_time));

    }

    public static int getDaysHint(String days) {
        if ("0000011".equals(days)) {
            return R.string.WEEKEND;
        } else if ("1111111".equals(days)) {
            return R.string.EVERY_DAY;
        } else if ("1111100".equals(days)) {
            return R.string.WEEKDAYS;
        } else {
            return 0;
        }
    }

    public void onError(String msg) {
        ToastUtil.showFailToast(this, msg);
    }

    NotifyDialog mNotifydialog;

    private void notify(int btn1text, int btn2tex, int notify) {
        if (mNotifydialog == null) {
            mNotifydialog = new NotifyDialog(this);
        }
        if (mNotifydialog.isShowing())
            return;
        mNotifydialog.hideNegButton();
        mNotifydialog.setButtonText(btn1text, btn2tex);
        mNotifydialog.setCancelable(false);
        mNotifydialog.show(notify, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        }, null);
    }


    void pickupTime(int start, int end) {
        if (mTimeDialog == null) {
            mTimeDialog = new Dialog(this, R.style.func_custom_dialog);
            View content = View.inflate(this, R.layout.dialog_pickup_time, null);
            TextView cancel = (TextView) content.findViewById(R.id.cancle);
            cancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mTimeDialog.dismiss();
                }
            });

            TextView confirm = (TextView) content.findViewById(R.id.confirm);
            confirm.setOnClickListener(this);

            hours = (WheelView) content.findViewById(R.id.hour);
            hours.setViewAdapter(new NumericWheelAdapter(this, 0, 23));

            mins = (WheelView) content.findViewById(R.id.mins);
            mins.setViewAdapter(new NumericWheelAdapter(this, 0, 59, "%02d"));
            mins.setCyclic(true);

            OnWheelClickedListener click = new OnWheelClickedListener() {
                public void onItemClicked(WheelView wheel, int itemIndex) {
                    wheel.setCurrentItem(itemIndex, true);
                }
            };
            hours.addClickingListener(click);
            mins.addClickingListener(click);

            mTimeDialog.setContentView(content);
            mTimeDialog.setOwnerActivity(EFamilyAlarm.this);
            mTimeDialog.setCanceledOnTouchOutside(true);
        }

        hours.setCurrentItem(start);
        mins.setCurrentItem(end);

        try {
            mTimeDialog.show();
            WindowManager windowManager = getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            WindowManager.LayoutParams lp = mTimeDialog.getWindow().getAttributes();
            lp.width = display.getWidth(); //
            mTimeDialog.getWindow().setAttributes(lp);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent arg2) {
        super.onActivityResult(requestCode, resultCode, arg2);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == TO_SET_DAYS) {
                week = arg2.getIntExtra(ClientConstants.ALARM_WEEKS, 0);
                mMsgEfamlGetSetAlarmParent.warn_week = week;
                setSelectText();
            }
        }
    }


    @Override
    public void onBackPressed() {
        Intent intent = getIntent();

        mMsgEfamlGetSetAlarmParent.warn_begin_time = AlarmInfo.parseTime(mStartTime.getText().toString());
        mMsgEfamlGetSetAlarmParent.warn_end_time = AlarmInfo.parseTime(mEndTime.getText().toString().startsWith(getString(R.string.TOW))
                ? mEndTime.getText().toString().substring(mEndTime.getText().toString().length() - 5, mEndTime.getText().toString().length()) : mEndTime.getText().toString());

        setResult(RESULT_OK, intent.putExtra(ClientConstants.ALARMINFO, mMsgEfamlGetSetAlarmParent));
        super.onBackPressed();
    }

}
