package com.cylan.jiafeigou.n.view.cam;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

/**
 * 自动录像周期设置
 * A simple {@link Fragment} subclass.
 * Use the {@link CapturePeriodDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CapturePeriodDialogFragment extends BaseDialog {


    @BindView(R.id.cb_capture_mon)
    CheckBox cbCaptureMon;
    @BindView(R.id.cb_capture_tue)
    CheckBox cbCaptureTue;
    @BindView(R.id.cb_capture_wed)
    CheckBox cbCaptureWed;
    @BindView(R.id.cb_capture_thur)
    CheckBox cbCaptureThur;
    @BindView(R.id.cb_capture_fri)
    CheckBox cbCaptureFri;
    @BindView(R.id.cb_capture_sat)
    CheckBox cbCaptureSat;
    @BindView(R.id.cb_capture_sun)
    CheckBox cbCaptureSun;

    public CapturePeriodDialogFragment() {
        // Required empty public constructor
    }

    private int checkedSerial;

    /**
     * Use this factory method to create activity_cloud_live_mesg_video_talk_item new instance of
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

    }

    @OnCheckedChanged(R.id.cb_capture_mon)
    public void onCheckBoxMon(boolean checked) {
//        checkedSerial|=
    }

    @OnCheckedChanged(R.id.cb_capture_tue)
    public void onCheckBoxTue(boolean checked) {
    }

    @OnCheckedChanged(R.id.cb_capture_wed)
    public void onCheckBoxWed(boolean checked) {
    }

    @OnCheckedChanged(R.id.cb_capture_thur)
    public void onCheckBoxThur(boolean checked) {
    }

    @OnCheckedChanged(R.id.cb_capture_fri)
    public void onCheckBoxFri(boolean checked) {
    }

    @OnCheckedChanged(R.id.cb_capture_sat)
    public void onCheckBoxSat(boolean checked) {
    }

    @OnCheckedChanged(R.id.cb_capture_sun)
    public void onCheckBoxSun(boolean checked) {
    }


    @Override
    protected int getCustomHeight() {
        return WindowManager.LayoutParams.WRAP_CONTENT;
    }

}
