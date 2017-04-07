package com.cylan.jiafeigou.n.view.cam;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.widget.SafeCheckBox;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 自动录像周期设置
 * A simple {@link Fragment} subclass.
 * Use the {@link CapturePeriodDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CapturePeriodDialogFragment extends BaseDialog {


    @BindView(R.id.lLayout_week)
    LinearLayout lLayoutWeek;

    public CapturePeriodDialogFragment() {
        // Required empty public constructor
    }

    private int checkedSerial;

    /**
     * Use this factory method to create activity_cloud_live_mesg_call_out_item new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SetSensitivityDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CapturePeriodDialogFragment newInstance(Bundle bundle) {
        CapturePeriodDialogFragment fragment = new CapturePeriodDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.layout_dialog_capture_period, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initView();
    }

    private void initView() {
        String uuid = getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID);
        Device device = DataSourceManager.getInstance().getJFGDevice(uuid);
        DpMsgDefine.DPAlarmInfo alarmInfo = device.$(502, new DpMsgDefine.DPAlarmInfo());
        checkedSerial = alarmInfo.day;
        final int checkBoxCount = lLayoutWeek.getChildCount();//应该是7
        for (int i = 0; i < checkBoxCount; i++) {
            final int index = i;
            final SafeCheckBox view = (SafeCheckBox) lLayoutWeek.getChildAt(i);
            //这个setCheck是 安全的.不会回调listener
            view.setChecked((checkedSerial >> (checkBoxCount - 1 - i) & 1) == 1);
            view.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                if (!isChecked && getCount() == 1) {
                    view.setChecked(true);
                    return;
                }
                checkedSerial ^= (1 << (6 - index));//按位取反
            });
        }
    }

    private int getCount() {
        int n = checkedSerial;
        int c = 0; // 计数器
        for (c = 0; n > 0; n >>= 1) // 循环移位
            c += n & 1; // 如果当前位是1，则计数器加1
        return c;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (action != null) {
            action.onDialogAction(0, checkedSerial);
        }
    }

    @Override
    protected int getCustomHeight() {
        return WindowManager.LayoutParams.WRAP_CONTENT;
    }

}
