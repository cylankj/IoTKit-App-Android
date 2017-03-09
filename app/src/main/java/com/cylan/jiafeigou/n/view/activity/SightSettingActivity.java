package com.cylan.jiafeigou.n.view.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomToolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SightSettingActivity extends BaseFullScreenFragmentActivity {

    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
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
        customToolbar.setBackAction((View v) -> onBackPressed());
        this.uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        //平视
        int defaultValue = PreferencesUtils.getInt(JConstant.KEY_CAM_SIGHT_HORIZONTAL + uuid, 0);
        rbtnSightHorizontal.setChecked(defaultValue == 0);
        rbtnSightVertical.setChecked(defaultValue == 1);
    }

    @Override
    public void onBackPressed() {
        finishExt();
    }

    @OnClick({R.id.sv_sight_horizontal, R.id.sv_sight_vertical})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sv_sight_horizontal:
                PreferencesUtils.putInt(JConstant.KEY_CAM_SIGHT_HORIZONTAL + uuid, 0);
                rbtnSightHorizontal.setChecked(true);
                break;
            case R.id.sv_sight_vertical:
                PreferencesUtils.putInt(JConstant.KEY_CAM_SIGHT_HORIZONTAL + uuid, 1);
                rbtnSightVertical.setChecked(true);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ToastUtil.showToast(getString(R.string.SCENE_SAVED));
    }
}
