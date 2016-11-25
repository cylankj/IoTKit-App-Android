package com.cylan.jiafeigou.n.view.cam;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SetSensitivityDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetSensitivityDialogFragment extends BaseDialog {

    @BindView(R.id.rbtn_sensitivity_high)
    RadioButton rbtnSensitivityHigh;
    @BindView(R.id.rbtn_sensitivity_middle)
    RadioButton rbtnSensitivityMiddle;
    @BindView(R.id.rbtn_sensitivity_low)
    RadioButton rbtnSensitivityLow;
    @BindView(R.id.rg_sensitivity)
    RadioGroup rgSensitivity;
    @BindView(R.id.tv_cancel)
    TextView tvCancel;

    public SetSensitivityDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create activity_cloud_live_mesg_video_talk_item new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SetSensitivityDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SetSensitivityDialogFragment newInstance(Bundle bundle) {
        SetSensitivityDialogFragment fragment = new SetSensitivityDialogFragment();
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
        View view = inflater.inflate(R.layout.fragment_set_sensitivity, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(false);
        final BeanCamInfo beanCamInfo = getArguments().getParcelable(JConstant.KEY_DEVICE_ITEM_BUNDLE);
        int level = beanCamInfo.cameraAlarmSensitivity;
        final int count = rgSensitivity.getChildCount();
        for (int i = 0; i < count; i++) {
            final int index = i;
            RadioButton box = (RadioButton) rgSensitivity.getChildAt(i);
            box.setChecked(level == i);
            box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        beanCamInfo.cameraAlarmSensitivity = index;
                    }
                }
            });
        }
    }


    @Override
    protected int getCustomHeight() {
        return WindowManager.LayoutParams.WRAP_CONTENT;
    }

    @OnClick(R.id.tv_cancel)
    public void onClick() {
        dismiss();
    }
}
