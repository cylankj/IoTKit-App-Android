package com.cylan.jiafeigou.n.view.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
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
        //平视
        DpMsgDefine.DpHangMode dpPrimary = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_509_CAMERA_MOUNT_MODE);
        if (dpPrimary == null) dpPrimary = new DpMsgDefine.DpHangMode();
        rbtnSightHorizontal.setChecked(dpPrimary.safeGetValue() == 0);
        rbtnSightVertical.setChecked(dpPrimary.safeGetValue() == 1);
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
                            DpMsgDefine.DpHangMode mode = new DpMsgDefine.DpHangMode();
                            try {
                                DataSourceManager.getInstance().updateValue(uuid, mode, DpMsgMap.ID_509_CAMERA_MOUNT_MODE);
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
                            DpMsgDefine.DpHangMode mode = new DpMsgDefine.DpHangMode();
                            mode.mode = "1";
                            try {
                                DataSourceManager.getInstance().updateValue(uuid, mode, DpMsgMap.ID_509_CAMERA_MOUNT_MODE);
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
