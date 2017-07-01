package com.cylan.jiafeigou.widget.dialog;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.widget.pick.AbstractWheel;
import com.cylan.jiafeigou.widget.pick.OnWheelChangedListener;
import com.cylan.jiafeigou.widget.pick.WheelVerticalView;
import com.cylan.jiafeigou.widget.pick.adapters.AbstractWheelTextAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class DurationDialogFragment extends BaseDialog<Integer> {


    @BindView(R.id.wheel_duration_pick)
    WheelVerticalView wheelDurationPick;
    @BindView(R.id.tv_dialog_btn_left)
    TextView tvDialogBtnLeft;
    @BindView(R.id.tv_dialog_btn_right)
    TextView tvDialogBtnRight;
    private int newValue;

    public static DurationDialogFragment newInstance(Bundle bundle) {
        DurationDialogFragment fragment = new DurationDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getCustomHeight() {
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    public DurationDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_duration_dialog, container, true);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        AbstractWheelTextAdapter adapter = new AbstractWheelTextAdapter(getContext()) {
            @Override
            public int getItemsCount() {
                return 15;
            }

            @Override
            protected CharSequence getItemText(int index) {
                return index + 1 + " account";
            }
        };
        newValue = value;
        adapter.setTextColor(getContext().getResources().getColor(R.color.color_4b9fd5));
        wheelDurationPick.setViewAdapter(adapter);
        wheelDurationPick.setCurrentItem(value - 1);
        wheelDurationPick.addChangingListener(changedListener);
//        wheelDurationPick.addScrollingListener(scrolledListener);
        wheelDurationPick.setCyclic(false);
        wheelDurationPick.setInterpolator(new AnticipateOvershootInterpolator());
    }

    private OnWheelChangedListener changedListener = (AbstractWheel wheel, int oldValue, int newValue) -> {
        DurationDialogFragment.this.newValue = newValue + 1;
    };

    @OnClick({R.id.tv_dialog_btn_left, R.id.tv_dialog_btn_right})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_dialog_btn_right:
                dismiss();
                break;
            case R.id.tv_dialog_btn_left:
                dismiss();
                if (action != null && value != newValue)
                    action.onDialogAction(1, newValue);
                break;
        }
    }


}
