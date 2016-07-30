package com.cylan.jiafeigou.n.view.cam;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 自动录像周期设置
 * A simple {@link Fragment} subclass.
 * Use the {@link CapturePeriodDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CapturePeriodDialogFragment extends BaseDialog {

    public CapturePeriodDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
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
        View view = inflater.inflate(R.layout.fragment_set_sensitivity, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

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
