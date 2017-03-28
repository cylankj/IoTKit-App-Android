package com.cylan.jiafeigou.n.view.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomToolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.schedulers.Schedulers;

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
        Device device = DataSourceManager.getInstance().getJFGDevice(uuid);
        //平视
        String dpPrimary = device.$(509, "0");
        try {
            rbtnSightHorizontal.setChecked(Integer.parseInt(dpPrimary) == 0);
            rbtnSightVertical.setChecked(Integer.parseInt(dpPrimary) == 1);
        } catch (Exception e) {
        }
    }

    @Override
    public void onBackPressed() {
        finishExt();
    }

    @OnClick({R.id.sv_sight_horizontal, R.id.sv_sight_vertical})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sv_sight_horizontal:
                Observable.just("save")
                        .subscribeOn(Schedulers.newThread())
                        .map(s -> {
                            try {
                                DpMsgDefine.DPPrimary<String> dpPrimary = new DpMsgDefine.DPPrimary<String>("0");
                                DataSourceManager.getInstance().updateValue(uuid, dpPrimary, DpMsgMap.ID_509_CAMERA_MOUNT_MODE);
                            } catch (IllegalAccessException e) {
                                AppLogger.e("err: ");
                            }
                            return null;
                        })
                        .subscribe();
                rbtnSightHorizontal.setChecked(true);
                break;
            case R.id.sv_sight_vertical:
                Observable.just("save")
                        .subscribeOn(Schedulers.newThread())
                        .map(s -> {
                            DpMsgDefine.DPPrimary<String> dpPrimary = new DpMsgDefine.DPPrimary<String>("1");
                            try {
                                DataSourceManager.getInstance().updateValue(uuid, dpPrimary, DpMsgMap.ID_509_CAMERA_MOUNT_MODE);
                            } catch (IllegalAccessException e) {
                                AppLogger.e("err: ");
                            }
                            return null;
                        })
                        .subscribe();
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
