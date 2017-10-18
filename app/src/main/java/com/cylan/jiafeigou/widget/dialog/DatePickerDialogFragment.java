package com.cylan.jiafeigou.widget.dialog;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.widget.pick.AbstractWheel;
import com.cylan.jiafeigou.widget.pick.OnWheelChangedListener;
import com.cylan.jiafeigou.widget.pick.WheelVerticalView;
import com.cylan.jiafeigou.widget.pick.adapters.AbstractWheelTextAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TreeSet;

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
    /**
     * <凌晨时间戳,当天最早视频时间></>
     */
    private List<Long> dateStartList = new ArrayList<>();
    private long timeFocus;
    private TimeZone timeZone;
    private int focusHour, focusMinute, focusDateIndex;

    public static DatePickerDialogFragment newInstance(Bundle bundle) {
        DatePickerDialogFragment fragment = new DatePickerDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

//    @Override
//    protected int getCustomWidth() {
//        return ViewGroup.LayoutParams.WRAP_CONTENT;
//    }

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
        initWheelDate(getIndexByTime());
        initWheelHour();
        initWheelMinute();
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
        wheelMinute.addChangingListener(changedListener);
        wheelMinute.setCyclic(false);
        wheelMinute.setInterpolator(new AnticipateOvershootInterpolator());
        wheelMinute.post(() -> wheelMinute.setCurrentItem(this.focusMinute, true));
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
        wheelHour.addChangingListener(changedListener);
        wheelHour.setCyclic(false);
        wheelHour.setInterpolator(new AnticipateOvershootInterpolator());
        wheelHour.post(() -> wheelHour.setCurrentItem(this.focusHour, true));
    }

    private int getIndexByTime() {
        //得到凌晨时间戳
        long time = SystemClock.currentThreadTimeMillis();
        long timeStart = TimeUtils.getSpecificDayStartTime(timeFocus);
        Log.d("getIndexByTime", "getIndexByTime: " + dateStartList);
        //由于dateStartList是降序,所以需要Collections.reverseOrder()
        int index = 0;
        for (index = 0; index < ListUtils.getSize(dateStartList); index++) {
            if (timeStart == TimeUtils.getSpecificDayStartTime(dateStartList.get(index))) {
                break;
            }
        }
        Log.d("getIndexByTime", "getIndexByTime: performance: " + (SystemClock.currentThreadTimeMillis() - time));
        return index;
    }

    public void setTimeFocus(long timeFocus) {
        this.timeFocus = timeFocus;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(timeZone);
        calendar.setTimeInMillis(timeFocus);
        this.focusHour = calendar.get(Calendar.HOUR_OF_DAY);
        this.focusMinute = calendar.get(Calendar.MINUTE);
        AppLogger.d("设置焦点:" + focusMinute + "," + focusHour);
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
        Log.d("setTimeZone", "setTimeZone:" + TimeZone.getDefault());
        Log.d("setTimeZone", "setTimeZone:" + timeZone);
        simpleDateFormat.setTimeZone(timeZone);
    }

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public void setDateList(ArrayList<Long> dateList) {
        if (dateList == null || dateList.size() == 0) {
            return;
        }
        AppLogger.i("count:" + (dateList.size()));
        long time = System.currentTimeMillis();
        //去重
        ArrayList<Long> tmpList = new ArrayList<>(dateList);
        ArrayList<Long> removeList = new ArrayList<>();
        HashMap<String, String> map = new HashMap<>();
        for (Long ll : tmpList) {
            final String date = simpleDateFormat.format(new Date(TimeUtils.wrapToLong(ll)));
            if (!map.containsKey(date)) {
                map.put(date, date);
            } else {
                removeList.add(ll);
            }
        }
        tmpList.removeAll(removeList);
        dateStartList = new ArrayList<>(new TreeSet<>(tmpList));
        Collections.sort(dateStartList, Collections.reverseOrder());//来一个降序
        for (int i = 0; i < dateStartList.size(); i++) {
            Log.d("setDateList", "setDateList " + simpleDateFormat.format(new Date(TimeUtils.wrapToLong(dateStartList.get(i)))));
        }
    }

    private static final int[] weekRes = {
            R.string.SUN_Hisvideo_Timeselector,
            R.string.MON_Hisvideo_Timeselector,
            R.string.TUE_Hisvideo_Timeselector,
            R.string.WED_Hisvideo_Timeselector,
            R.string.THU_Hisvideo_Timeselector,
            R.string.FRI_Hisvideo_Timeselector,
            R.string.SAT_Hisvideo_Timeselector
    };

    private void initWheelDate(int index) {
        focusDateIndex = index;
        AbstractWheelTextAdapter adapter = new AbstractWheelTextAdapter(getContext()) {
            @Override
            public int getItemsCount() {
                return ListUtils.getSize(dateStartList);
            }

            @Override
            protected CharSequence getItemText(int index) {
                int week = TimeUtils.getWeekNum(dateStartList.get(index), timeZone);
                return TimeUtils.getDatePickFormat(dateStartList.get(index), timeZone)
                        + getString(weekRes[week - 1]);
            }
        };
        adapter.setTextColor(getContext().getResources().getColor(R.color.color_4b9fd5));
        wheelDatePick.setViewAdapter(adapter);
        wheelDatePick.addChangingListener(changedListener);
        wheelDatePick.setCyclic(false);
        wheelDatePick.setInterpolator(new AnticipateOvershootInterpolator());
        wheelDatePick.post(() -> wheelDatePick.setCurrentItem(index, true));
    }

    // Wheel changed listener
    private OnWheelChangedListener changedListener = new OnWheelChangedListener() {
        @Override
        public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
            int id = wheel.getId();
            switch (id) {
                case R.id.wheel_date:
                    focusDateIndex = newValue;
                    break;
                case R.id.wheel_hour:
                    focusHour = newValue;
                    break;
                case R.id.wheel_minute:
                    focusMinute = newValue;
                    break;
            }
        }
    };

    @OnClick({R.id.tv_dialog_btn_left, R.id.tv_dialog_btn_right})
    public void onClick(View view) {
        try {//#115932 可能会有 indexOutOf 异常,这里直接 catch 掉,以免崩溃
            switch (view.getId()) {
                case R.id.tv_dialog_btn_right:
                    dismiss();
                    if (focusDateIndex >= ListUtils.getSize(dateStartList)) {
                        AppLogger.d("out of index");
                        return;
                    }
                    final long tmp = TimeUtils.getSpecificDayStartTime(dateStartList.get(focusDateIndex)) + focusHour * 3600 * 1000 + focusMinute * 60 * 1000;
                    AppLogger.d("finalTime: " + TimeUtils.getTimeSpecial(tmp));
                    break;
                case R.id.tv_dialog_btn_left:
                    if (focusDateIndex >= ListUtils.getSize(dateStartList)) {
                        AppLogger.d("out of index");
                        return;
                    }
                    dismiss();

                    final long finalTime = TimeUtils.getSpecificDayStartTime(dateStartList.get(focusDateIndex)) + focusHour * 3600 * 1000 + focusMinute * 60 * 1000;
                    AppLogger.d("finalTime: " + TimeUtils.getTimeSpecial(finalTime) + "," + finalTime);
                    if (action != null && finalTime != timeFocus) {
                        action.onDialogAction(view.getId(), finalTime);
                    }
                    break;
            }
        } catch (Exception e) {
            AppLogger.e(e.getMessage());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
