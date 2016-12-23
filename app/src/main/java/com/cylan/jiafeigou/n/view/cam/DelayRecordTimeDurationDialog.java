package com.cylan.jiafeigou.n.view.cam;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.pick.WheelPicker;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yzd on 16-12-16.
 */

public class DelayRecordTimeDurationDialog extends BaseDialog<Integer> {

    @BindView(R.id.dialog_record_duration_picker)
    WheelPicker mPicker;
    @BindView(R.id.dialog_record_duration_cancel)
    TextView mCancel;
    @BindView(R.id.dialog_record_duration_ok)
    TextView mOk;
    private String[] options;

    public static DelayRecordTimeDurationDialog newInstance(Bundle bundle) {
        DelayRecordTimeDurationDialog dialog = new DelayRecordTimeDurationDialog();
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_delay_record_time_duration, container, false);
        ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView() {
        int position = 0;
        if (value == 0) {
            options = getResources().getStringArray(R.array.delay_redord_60s_option);
            position = options.length - 1;
        } else if (value == 1) {
            options = getResources().getStringArray(R.array.delay_redord_20s_option);
            position = 7;
        }
        mPicker.setData(Arrays.asList(options));
        mPicker.setSelectedItemPosition(position);
    }

    @OnClick({R.id.dialog_record_duration_cancel, R.id.dialog_record_duration_ok})
    public void onClick(View view) {
        dismiss();
        if (action != null && view.getId() == R.id.dialog_record_duration_ok) {
            action.onDialogAction(view.getId(), getDuration());
        }
    }

    public int getDuration() {
        if (value == 0) {
            return mPicker.getCurrentItemPosition() + 4;
        } else if (value == 1) {
            return mPicker.getCurrentItemPosition() + 1;
        }
        return 1;
    }
}
