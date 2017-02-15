package com.cylan.jiafeigou.n.view.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SightSettingActivity extends BaseFullScreenFragmentActivity {

    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.rg_view)
    RadioGroup rgView;
    @BindView(R.id.rbtn_sight_horizontal)
    RadioButton rbtnSightHorizontal;
    @BindView(R.id.rbtn_sight_vertical)
    RadioButton rbtnSightVertical;
    private String uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sight_setting_layout);
        ButterKnife.bind(this);
        customToolbar.setBackAction((View v) -> {
            onBackPressed();
        });
        this.uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        //平视
        int defaultValue = PreferencesUtils.getInt(JConstant.KEY_CAM_SIGHT_HORIZONTAL, 0);
        if (defaultValue == 0) {
            rbtnSightHorizontal.setChecked(true);
            rbtnSightVertical.setChecked(false);
        } else {
            rbtnSightHorizontal.setChecked(false);
            rbtnSightVertical.setChecked(true);
        }
        rgView.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
            switch (checkedId) {
                case R.id.rbtn_sight_vertical:
                    PreferencesUtils.putInt(JConstant.KEY_CAM_SIGHT_HORIZONTAL + uuid, 1);
                    break;
                case R.id.rbtn_sight_horizontal:
                    PreferencesUtils.putInt(JConstant.KEY_CAM_SIGHT_HORIZONTAL + uuid, 0);
                    break;
            }
        });
    }

    @Override
    public void onBackPressed() {
        finishExt();
    }
}
