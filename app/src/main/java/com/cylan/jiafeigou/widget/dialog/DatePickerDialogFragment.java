package com.cylan.jiafeigou.widget.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.widget.pick.WheelVerticalView;
import com.cylan.jiafeigou.widget.pick.adapters.AbstractWheelTextAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by cylan-hunt on 16-12-22.
 */

public class DatePickerDialogFragment extends BaseDialog {

    public static final String KEY_LEFT_CONTENT = "key_left";
    public static final String KEY_RIGHT_CONTENT = "key_right";

    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;
    @BindView(R.id.wheel_date)
    WheelVerticalView wheelDatePick;
    @BindView(R.id.tv_dialog_btn_left)
    TextView tvDialogBtnLeft;
    @BindView(R.id.tv_dialog_btn_right)
    TextView tvDialogBtnRight;
    @BindView(R.id.wheel_hour)
    WheelVerticalView wheelHour;
    @BindView(R.id.wheel_minute)
    WheelVerticalView wheelMinute;
    private Calendar mCalendar = Calendar.getInstance();
    private long startTime;
    private long endTime;
    private long selectTime;
    private TimeZone timeZone = TimeZone.getDefault();

    public static DatePickerDialogFragment newInstance(long startTime, long endTime, long selectTime, int timezoneOffset, String title, String left, String right) {
        DatePickerDialogFragment fragment = new DatePickerDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("start_time", startTime);
        bundle.putLong("end_time", endTime);
        bundle.putLong("select_time", selectTime);
        bundle.putInt("timezone_offset", timezoneOffset);
        bundle.putString(KEY_TITLE, title);
        bundle.putString(KEY_LEFT_CONTENT, left);
        bundle.putString(KEY_RIGHT_CONTENT, right);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_date_pick_dialog, container, true);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        final String title = bundle.getString(KEY_TITLE);
        final String lContent = bundle.getString(KEY_LEFT_CONTENT);
        final String rContent = bundle.getString(KEY_RIGHT_CONTENT);
        startTime = bundle.getLong("start_time");
        endTime = bundle.getLong("end_time");
        selectTime = bundle.getLong("select_time");
        int timezoneOffset = bundle.getInt("timezone_offset", -1);
        if (timezoneOffset != -1) {
            timeZone.setRawOffset(timezoneOffset);
            simpleDateFormat.setTimeZone(timeZone);
        }

        if (!TextUtils.isEmpty(title)) {
            tvDialogTitle.setText(title);
        }
        if (!TextUtils.isEmpty(lContent)) {
            tvDialogBtnLeft.setText(lContent);
        }
        if (!TextUtils.isEmpty(rContent)) {
            tvDialogBtnRight.setText(rContent);
        }
        getDialog().setCanceledOnTouchOutside(bundle.getBoolean(KEY_TOUCH_OUT_SIDE_DISMISS, false));
        initWheelDate();
        initWheelHour();
        initWheelMinute();
        moveToTime(selectTime);
    }

    private void initWheelMinute() {
        AbstractWheelTextAdapter adapter = new AbstractWheelTextAdapter(getContext()) {
            @Override
            public int getItemsCount() {
                return 60;
            }

            @Override
            protected CharSequence getItemText(int index) {
                return String.format(Locale.getDefault(), "%02d", index);
            }
        };
        adapter.setTextColor(getContext().getResources().getColor(R.color.color_4b9fd5));
        wheelMinute.setViewAdapter(adapter);
        wheelMinute.setCyclic(false);
        wheelMinute.setInterpolator(new AnticipateOvershootInterpolator());
    }

    private void initWheelHour() {
        AbstractWheelTextAdapter adapter = new AbstractWheelTextAdapter(getContext()) {
            @Override
            public int getItemsCount() {
                return 24;
            }

            @Override
            protected CharSequence getItemText(int index) {
                return String.format(Locale.getDefault(), "%02d", index) + getString(R.string.HOUR);
            }
        };
        adapter.setTextColor(getContext().getResources().getColor(R.color.color_4b9fd5));
        wheelHour.setViewAdapter(adapter);
        wheelHour.setCyclic(false);
        wheelHour.setInterpolator(new AnticipateOvershootInterpolator());
    }

    private void moveToTime(long time) {
        mCalendar.setTimeInMillis(selectTime);
        int zero = mCalendar.get(Calendar.DAY_OF_YEAR);
        mCalendar.setTimeInMillis(time);
        int day = mCalendar.get(Calendar.DAY_OF_YEAR);
        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendar.get(Calendar.MINUTE);
        wheelDatePick.setCurrentItem(day - zero);
        wheelHour.setCurrentItem(hour);
        wheelMinute.setCurrentItem(minute);
    }


    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private static final int[] weekRes = {
            R.string.SUN_Hisvideo_Timeselector,
            R.string.MON_Hisvideo_Timeselector,
            R.string.TUE_Hisvideo_Timeselector,
            R.string.WED_Hisvideo_Timeselector,
            R.string.THU_Hisvideo_Timeselector,
            R.string.FRI_Hisvideo_Timeselector,
            R.string.SAT_Hisvideo_Timeselector
    };

    private void initWheelDate() {
        AbstractWheelTextAdapter adapter = new AbstractWheelTextAdapter(getContext()) {
            @Override
            public int getItemsCount() {
                return (int) Math.max((startTime - endTime) / (24 * 60 * 60 * 1000), 1);
            }

            @Override
            protected CharSequence getItemText(int index) {
                mCalendar.setTimeInMillis(startTime);
                mCalendar.roll(Calendar.DAY_OF_YEAR, index);
                long targetTime = mCalendar.getTimeInMillis();
                mCalendar.setTimeInMillis(targetTime);
                return TimeUtils.getDatePickFormat(mCalendar.getTimeInMillis(), timeZone)
                        + getString(weekRes[mCalendar.get(Calendar.DAY_OF_WEEK) - 1]);
            }
        };
        adapter.setTextColor(getContext().getResources().getColor(R.color.color_4b9fd5));
        wheelDatePick.setViewAdapter(adapter);
        wheelDatePick.setCyclic(false);
        wheelDatePick.setInterpolator(new AnticipateOvershootInterpolator());
    }

    @OnClick({R.id.tv_dialog_btn_left, R.id.tv_dialog_btn_right})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_dialog_btn_right:
                dismiss();
                break;
            case R.id.tv_dialog_btn_left:
                dismiss();
                mCalendar.setTimeInMillis(startTime);
                mCalendar.set(Calendar.HOUR_OF_DAY, 0);
                mCalendar.set(Calendar.MINUTE, 0);
                mCalendar.roll(Calendar.DAY_OF_YEAR, wheelDatePick.getCurrentItem());
                mCalendar.roll(Calendar.HOUR_OF_DAY, wheelHour.getCurrentItem());
                mCalendar.roll(Calendar.MINUTE, wheelMinute.getCurrentItem());
                long select = mCalendar.getTimeInMillis();
                AppLogger.d("finalTime: " + TimeUtils.getTimeSpecial(select) + "," + select);
                if (action != null && select != this.selectTime) {
                    action.onDialogAction(view.getId(), select);
                }
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
