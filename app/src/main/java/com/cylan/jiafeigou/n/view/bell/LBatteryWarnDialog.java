package com.cylan.jiafeigou.n.view.bell;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by cylan-hunt on 16-8-8.
 * 门铃低电量提示框
 */
public class LBatteryWarnDialog extends BaseDialog {
    @BindView(R.id.tv_low_battery_content)
    TextView tvLowBatteryContent;

    /**
     * Use this factory method to create activity_cloud_live_mesg_call_out_item new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SetSensitivityDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LBatteryWarnDialog newInstance(Bundle bundle) {
        LBatteryWarnDialog fragment = new LBatteryWarnDialog();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.layout_dialog_low_battery, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected int getCustomHeight() {
        return WindowManager.LayoutParams.WRAP_CONTENT;
    }

    @OnClick({R.id.tv_low_battery_dismiss, R.id.imgv_close_dialog})
    public void onClick(View view) {
        dismiss();
    }

}
