package com.cylan.jiafeigou.n.view.record;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yzd on 16-12-16.
 */

public class DelayRecordTimeIntervalDialog extends BaseDialog<Integer> {
    public static DelayRecordTimeIntervalDialog newInstance(Bundle bundle) {
        DelayRecordTimeIntervalDialog dialog = new DelayRecordTimeIntervalDialog();
        return dialog;
    }

    @BindView(R.id.dialog_record_rb_20s)
    RadioButton mOption20S;
    @BindView(R.id.dialog_record_rb_60s)
    RadioButton mOption60S;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_delay_record_time_interval, container, false);
        ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView() {
        if (value == 60) {
            mOption60S.setChecked(true);
        } else if (value == 20) {
            mOption20S.setChecked(true);
        }
    }

    @OnClick({R.id.dialog_record_rb_20s, R.id.dialog_record_rb_60s, R.id.dialog_record_time_interval_cancel})
    public void onClick(View view) {
        dismiss();
        if (action != null) {
            action.onDialogAction(view.getId(), view);
        }
    }
}
