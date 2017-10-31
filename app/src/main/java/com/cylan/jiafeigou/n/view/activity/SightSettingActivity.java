package com.cylan.jiafeigou.n.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioButton;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomToolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.JConstant.KEY_CAM_SIGHT_SETTING;

public class SightSettingActivity extends BaseFullScreenFragmentActivity {

    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.rbtn_sight_horizontal)
    RadioButton rbtnSightHorizontal;
    @BindView(R.id.rbtn_sight_vertical)
    RadioButton rbtnSightVertical;
    private String initValue;
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sight_setting_layout);
        ButterKnife.bind(this);
        customToolbar.setBackAction((View v) -> onBackPressed());
        this.uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        //平视,1.平视.0俯视.默认平视
        String dpPrimary = device.$(509, "1");
        initValue = dpPrimary;
        try {
            //0:俯视
            rbtnSightHorizontal.setChecked(TextUtils.equals("1", dpPrimary));
            rbtnSightVertical.setChecked(TextUtils.equals("0", dpPrimary));
        } catch (Exception e) {
        }
        subscription = getDeviceUnBindSub();
    }

    private Subscription getDeviceUnBindSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceUnBindedEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(event -> TextUtils.equals(event.uuid, uuid))
                .subscribe(event -> {

                    onDeviceUnBind();

                }, e -> AppLogger.d(e.getMessage()));
    }

    private void onDeviceUnBind() {
        AppLogger.d("当前设备已解绑");
        AlertDialogManager.getInstance().showDialog(this, getString(R.string.Tap1_device_deleted), getString(R.string.Tap1_device_deleted),
                getString(R.string.OK), (dialog, which) -> {
                    finish();
                    Intent intent = new Intent(getContext(), NewHomeActivity.class);
                    startActivity(intent);
                }, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferencesUtils.putBoolean(KEY_CAM_SIGHT_SETTING + uuid, false);
    }

    @OnClick({R.id.sv_sight_horizontal, R.id.sv_sight_vertical})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sv_sight_horizontal:
                Observable.just("save")
                        .subscribeOn(Schedulers.io())
                        .map(s -> {
                            try {
                                DpMsgDefine.DPPrimary<String> dpPrimary = new DpMsgDefine.DPPrimary<String>("1");
                                BaseApplication.getAppComponent().getSourceManager().updateValue(uuid, dpPrimary, DpMsgMap.ID_509_CAMERA_MOUNT_MODE);
                            } catch (IllegalAccessException e) {
                                AppLogger.e("err: ");
                            }
                            return null;
                        })
                        .subscribe(ret -> {
                        }, AppLogger::e);
                rbtnSightHorizontal.setChecked(true);
                break;
            case R.id.sv_sight_vertical:
                Observable.just("save")
                        .subscribeOn(Schedulers.io())
                        .map(s -> {
                            DpMsgDefine.DPPrimary<String> dpPrimary = new DpMsgDefine.DPPrimary<String>("0");
                            try {
                                BaseApplication.getAppComponent().getSourceManager().updateValue(uuid, dpPrimary, DpMsgMap.ID_509_CAMERA_MOUNT_MODE);
                            } catch (IllegalAccessException e) {
                                AppLogger.e("err: ");
                            }
                            return null;
                        })
                        .subscribe(ret -> {
                        }, AppLogger::e);
                rbtnSightVertical.setChecked(true);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        //平视
        String dpPrimary = device.$(509, "1");
        if (!TextUtils.equals(dpPrimary, initValue)) {
            ToastUtil.showToast(getString(R.string.SCENE_SAVED));
        }

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }
    }
}
