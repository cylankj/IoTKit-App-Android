package com.cylan.jiafeigou.activity.video.setting;

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
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.wheel.OnWheelChangedListener;
import com.cylan.jiafeigou.widget.wheel.OnWheelClickedListener;
import com.cylan.jiafeigou.widget.wheel.WheelView;
import com.cylan.jiafeigou.widget.wheel.adapters.MyNumercWheelAdapter;
import com.cylan.utils.entity.AlarmInfo;

public class Alarm extends BaseActivity implements View.OnClickListener {


    // onActivityResult
    private static final int TO_SET_DAYS = 0;

    View mDays;

    TextView mDaysText;

    private AlarmInfo info;
    String mCid;

    static String[] sWeeks = null;

    TextView mStartTime, mEndTime;

    private Dialog mTimeDialog;

    private WheelView hours;
    private WheelView mins;

    private int flag = 0;

    int week = 0;


    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.ly_alarm);

        setTitle(R.string.TIME);
        setBackBtnOnClickListener(this);
        mDays = findViewById(R.id.days);
        mDays.setOnClickListener(this);
        mDaysText = (TextView) findViewById(R.id.alarm_days);
        findViewById(R.id.alarm_start).setOnClickListener(this);
        findViewById(R.id.alarm_end).setOnClickListener(this);
        mStartTime = (TextView) findViewById(R.id.alarm_start_time);
        mEndTime = (TextView) findViewById(R.id.alarm_end_time);
        mDays.setOnClickListener(this);
        info = getIntent().getParcelableExtra(ClientConstants.ALARMINFO);
        if (sWeeks == null) {
            sWeeks = getResources().getStringArray(R.array.show_weeks);
        }
        if (info != null) {
            mCid = info.cid;
            setSelectText();
            onAlarmChange();
        }
    }

    private void setSelectText() {
        StringBuffer days = new StringBuffer();
        for (int i = 0; i < sWeeks.length; i++) {
            if (AlarmInfo.isSelectedDay(info.days, i))
                days.append(sWeeks[i] + " ");
        }
        int str = Alarm.getDaysHint(String.format("%07d", Integer.parseInt(Integer.toBinaryString(info.days), 10)));

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
                String[] times = endtime.split(":");
                pickupTime(Integer.parseInt(times[0].length() > 1 ? times[0].substring(times[0].length() - 2, times[0].length()) : time.substring(times[0].length() - 1, times[0].length())), Integer.parseInt(times[1]));
                flag = 1;
                break;
            case R.id.days:

                startActivityForResult(new Intent(Alarm.this, AlarmDaysSelectActivity.class).putExtra(ClientConstants.ALARM_WEEKS, info.days), TO_SET_DAYS);
                break;
        }

    }

    public void onAlarmChange() {

        mStartTime.setText(info.startTime);
        mEndTime.setText((AlarmInfo.parseTime(info.startTime) > AlarmInfo.parseTime(info.endTime) ? getString(R.string.TOW) + " " : "") + info.endTime);

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

            hours.addChangingListener(new OnWheelChangedListener() {
                @Override
                public void onChanged(WheelView wheel, int oldValue, int newValue) {
                    if (hours.getViewAdapter() != null) {
                        ((MyNumercWheelAdapter) hours.getViewAdapter()).setCurrent(newValue);
                        ((MyNumercWheelAdapter) hours.getViewAdapter()).notifyDataSetChanged();
                    }

                }
            });

            hours.setViewAdapter(new MyNumercWheelAdapter(this, 0, 23, "%02d"));

            mins = (WheelView) content.findViewById(R.id.mins);

            mins.addChangingListener(new OnWheelChangedListener() {
                @Override
                public void onChanged(WheelView wheel, int oldValue, int newValue) {
                    if (mins.getViewAdapter() != null) {
                        ((MyNumercWheelAdapter) mins.getViewAdapter()).setCurrent(newValue);
                        ((MyNumercWheelAdapter) mins.getViewAdapter()).notifyDataSetChanged();
                    }

                }
            });

            mins.setViewAdapter(new MyNumercWheelAdapter(this, 0, 59, "%02d"));

            OnWheelClickedListener click = new OnWheelClickedListener() {
                public void onItemClicked(WheelView wheel, int itemIndex) {
                    wheel.setCurrentItem(itemIndex, true);
                }
            };
            hours.addClickingListener(click);
            mins.addClickingListener(click);

            mTimeDialog.setContentView(content);
            mTimeDialog.setOwnerActivity(Alarm.this);
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
                info.days = week;
                setSelectText();
            }
        }
    }


    @Override
    public void onBackPressed() {
        Intent intent = getIntent();

        info.startTime = mStartTime.getText().toString();
        String endtime = mEndTime.getText().toString();
        info.endTime = mEndTime.getText().toString().startsWith(getString(R.string.TOW)) ? endtime.substring(endtime.length() - 5, endtime.length()) : mEndTime.getText()
                .toString();

        setResult(RESULT_OK, intent.putExtra(ClientConstants.ALARMINFO, info));
        super.onBackPressed();
    }

}
