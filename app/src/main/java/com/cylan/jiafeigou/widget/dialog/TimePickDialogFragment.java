package com.cylan.jiafeigou.widget.dialog;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.widget.pick.AbstractWheel;
import com.cylan.jiafeigou.widget.pick.OnWheelChangedListener;
import com.cylan.jiafeigou.widget.pick.WheelVerticalView;
import com.cylan.jiafeigou.widget.pick.adapters.AbstractWheelTextAdapter;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class TimePickDialogFragment extends BaseDialog<Integer> {


    @BindView(R.id.tv_dialog_btn_left)
    TextView tvDialogBtnLeft;
    @BindView(R.id.tv_dialog_btn_right)
    TextView tvDialogBtnRight;
    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;
    @BindView(R.id.wheel_hour_pick)
    WheelVerticalView wheelHourPick;
    @BindView(R.id.wheel_minute_pick)
    WheelVerticalView wheelMinutePick;
    private String uuid;
    private int newHour;
    private int newMinute;

    public static TimePickDialogFragment newInstance(Bundle bundle) {
        TimePickDialogFragment fragment = new TimePickDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getCustomHeight() {
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    public TimePickDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.uuid = getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_time_pick_dialog, container, true);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        String title = getArguments().getString(KEY_TITLE);
        tvDialogTitle.setText(title);
        DpMsgDefine.DPAlarmInfo alarmInfo = MiscUtils.safeGet_(DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_502_CAMERA_ALARM_INFO), DpMsgDefine.EMPTY.ALARM_INFO);
        boolean isStart = TextUtils.equals(title, getString(R.string.FROME));
        newHour = isStart ? alarmInfo.timeStart >> 8 : alarmInfo.timeEnd >> 8;
        newMinute = isStart ? ((byte) alarmInfo.timeStart << 8) >> 8 : ((byte) alarmInfo.timeEnd << 8) >> 8;
        initWheel(newHour, wheelHourPick, 24);
        initWheel(newMinute, wheelMinutePick, 60);
    }

    private void initWheel(int index, WheelVerticalView wheelHourPick, final int count) {
        AbstractWheelTextAdapter adapter = new AbstractWheelTextAdapter(getContext()) {
            @Override
            public int getItemsCount() {
                return count;
            }

            @Override
            protected CharSequence getItemText(int index) {
                return new DecimalFormat("00").format(index);
            }
        };
        adapter.setTextColor(getContext().getResources().getColor(R.color.color_4b9fd5));
        wheelHourPick.setViewAdapter(adapter);
        wheelHourPick.setCurrentItem(index);
        wheelHourPick.addChangingListener(changedListener);
        wheelHourPick.setCyclic(true);
        wheelHourPick.setInterpolator(new AnticipateOvershootInterpolator());
    }

    // Wheel changed listener
    private OnWheelChangedListener changedListener = new OnWheelChangedListener() {
        public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
            switch (wheel.getId()) {
                case R.id.wheel_hour_pick:
                    newHour = newValue;
                    break;
                case R.id.wheel_minute_pick:
                    newMinute = newValue;
                    break;
            }
        }
    };

    @OnClick({R.id.tv_dialog_btn_left, R.id.tv_dialog_btn_right})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_dialog_btn_right:
                dismiss();
                break;
            case R.id.tv_dialog_btn_left:
                dismiss();
                int finalTime = MiscUtils.parseTime(newHour + ":" + newMinute);
                if (action != null) {
                    action.onDialogAction(1, finalTime);
                }
                break;
        }
    }


}
