package com.cylan.jiafeigou.n.view.cam;


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
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.pick.OnWheelChangedListener;
import com.cylan.jiafeigou.widget.pick.WheelVerticalView;
import com.cylan.jiafeigou.widget.pick.adapters.AbstractWheelTextAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WarmIntervalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WarmIntervalFragment extends BaseDialog {

    public static final String KEY_LEFT_CONTENT = "key_left";
    public static final String KEY_RIGHT_CONTENT = "key_right";

    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;
    @BindView(R.id.tv_dialog_btn_left)
    TextView tvDialogBtnLeft;
    @BindView(R.id.tv_dialog_btn_right)
    TextView tvDialogBtnRight;
    @BindView(R.id.warm_number)
    WheelVerticalView warmNumber;
    @BindView(R.id.warm_unit)
    WheelVerticalView warmUnit;

    private int numberIndex = 0;
    private int unitIndex = 0;
    private String uuid;
    private Device device;

    public static WarmIntervalFragment newInstance(Bundle bundle) {
        WarmIntervalFragment fragment = new WarmIntervalFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_warm_interval, container, true);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        final String title = bundle.getString(KEY_TITLE);
        final String lContent = bundle.getString(KEY_LEFT_CONTENT);
        final String rContent = bundle.getString(KEY_RIGHT_CONTENT);
        this.uuid = bundle.getString(JConstant.KEY_DEVICE_ITEM_UUID, "");
        if (!TextUtils.isEmpty(title))
            tvDialogTitle.setText(title);
        if (!TextUtils.isEmpty(lContent))
            tvDialogBtnLeft.setText(lContent);
        if (!TextUtils.isEmpty(rContent))
            tvDialogBtnRight.setText(rContent);
        getDialog().setCanceledOnTouchOutside(bundle.getBoolean(KEY_TOUCH_OUT_SIDE_DISMISS, false));
        device = DataSourceManager.getInstance().getDevice(uuid);
        int interval = device.$(DpMsgMap.ID_514_CAM_WARNINTERVAL, 0);
        numberIndex = interval >= 60 ? interval / 60 - 1 : 0;
        unitIndex = interval >= 60 ? 1 : 0;
        initWarmUnit();
        initWarmNumber();
    }

    private void initWarmUnit() {
        AbstractWheelTextAdapter adapter = new AbstractWheelTextAdapter(getContext()) {
            @Override
            public int getItemsCount() {
                return 2;
            }

            @Override
            protected CharSequence getItemText(int index) {
                return index == 0 ? getString(R.string.REPEAT_TIME) : getString(R.string.MINUTE_Cloud);
            }
        };
        adapter.setTextColor(getContext().getResources().getColor(R.color.color_4b9fd5));
        adapter.setItemResource(R.layout.item_text_view);
        warmUnit.setViewAdapter(adapter);
        warmUnit.setCurrentItem(unitIndex);
        warmUnit.addChangingListener(changedListener);
//        warmUnit.setCyclic(true);
        warmUnit.setInterpolator(new AnticipateOvershootInterpolator());
        warmUnit.setVisibleItems(3);
        warmUnit.setViewAdapter(adapter);
    }

    // Wheel changed listener
    private OnWheelChangedListener changedListener = (wheel, oldValue, newValue) -> {
        switch (wheel.getId()) {
            case R.id.warm_number:
                numberIndex = newValue;
                break;
            case R.id.warm_unit:
                unitIndex = newValue;
                warmNumber.invalidateItemsLayout(true);
                warmNumber.setCurrentItem(0);
                break;
        }
    };

    private void initWarmNumber() {
        AbstractWheelTextAdapter adapter = new AbstractWheelTextAdapter(getContext()) {
            @Override
            protected CharSequence getItemText(int index) {
                return unitIndex == 0 ? "30" : "" + (index + 1);
            }

            @Override
            public int getItemsCount() {
                return unitIndex == 0 ? 1 : 10;
            }

        };
        adapter.setTextColor(getContext().getResources().getColor(R.color.color_4b9fd5));
        adapter.setItemResource(R.layout.item_text_view);
        warmNumber.setViewAdapter(adapter);
        warmNumber.addChangingListener(changedListener);
//        warmNumber.setCyclic(true);
        warmNumber.setInterpolator(new AnticipateOvershootInterpolator());
        warmNumber.setVisibleItems(3);
        warmNumber.setViewAdapter(adapter);
        warmNumber.setCurrentItem(numberIndex);
    }

    @OnClick(R.id.tv_dialog_btn_right)
    public void sure() {
        if (action != null) {
            action.onDialogAction(0, unitIndex == 0 ? 30 : 60 * (numberIndex + 1));
        }
        dismiss();
    }

    @OnClick(R.id.tv_dialog_btn_left)
    public void cancel() {
        dismiss();
    }
}
