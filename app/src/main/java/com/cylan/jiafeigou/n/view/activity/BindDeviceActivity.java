package com.cylan.jiafeigou.n.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.view.bind.BindScanFragment;
import com.cylan.jiafeigou.n.view.bind.SNInputFragment;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class BindDeviceActivity extends BaseFullScreenFragmentActivity implements BaseDialog.BaseDialogAction {
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_device);
        ButterKnife.bind(this);
        initTopBar();
        boolean show = getResources().getBoolean(R.bool.show_ruishi_interface);
        findViewById(R.id.v_to_bind_consumer_cam).setVisibility(show ? View.VISIBLE : View.GONE);
        findViewById(R.id.v_to_scan_qrcode).setVisibility(getResources()
                .getBoolean(R.bool.show_scan_bind_interface) ? View.VISIBLE : View.GONE);
        findViewById(R.id.v_to_bind_cat_eye_cam).setVisibility(getResources()
                .getBoolean(R.bool.show_cat_eye_bind_interface) ? View.VISIBLE : View.GONE);
    }


    private void initTopBar() {
        customToolbar.setBackAction((View v) -> {
            onBackPressed();
        });
    }


    @Override
    public void onBackPressed() {
        if (popAllFragmentStack())
            return;
        finishExt();
    }

    @Override
    public void onDialogAction(int id, Object value) {
        if (id == R.id.tv_dialog_btn_right)
            return;
        popAllFragmentStack();
    }

    @OnClick({R.id.v_to_scan_qrcode,
            R.id.v_to_bind_camera,
            R.id.v_to_input_sn,
            R.id.v_to_bind_panorama_camera})
    public void onClick(View view) {
        ViewUtils.deBounceClick(view);
        switch (view.getId()) {
            case R.id.v_to_input_sn: {
                SNInputFragment fragment = SNInputFragment.newInstance();
                ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(),
                        fragment, android.R.id.content);
            }
            break;
            case R.id.v_to_scan_qrcode: {
                BindScanFragment fragment = BindScanFragment.newInstance(null);
                ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(),
                        fragment, android.R.id.content);
                break;
            }
            case R.id.v_to_bind_camera: {
                ViewUtils.deBounceClick(view);
                Intent intent = new Intent(this, BindCamActivity.class);
                intent.putExtra(JConstant.KEY_SSID_PREFIX, "DOG-******");
                startActivity(intent);
                break;
            }
            case R.id.v_to_bind_panorama_camera: {
                jump2PanoramaCam(true);
                break;
            }
        }
    }

    private void jump2PanoramaCam(boolean animation) {
        startActivity(new Intent(this, BindPanoramaCamActivity.class));
        ViewUtils.deBounceClick(findViewById(R.id.v_to_bind_panorama_camera));
    }

    @OnClick({R.id.v_to_bind_camera_cloud,
            R.id.v_to_bind_consumer_cam,
            R.id.v_to_bind_bell_battery,
            R.id.v_to_bind_bell_no_battery,
            R.id.v_to_bind_cat_eye_cam})
    public void onClickBind(View view) {
        Intent intent = new Intent(this, BindAnimationActivity.class);
        switch (view.getId()) {
            case R.id.v_to_bind_camera_cloud:
                intent.putExtra(JConstant.KEY_ANIM_GIF, R.raw.cloud_cam_android);
                intent.putExtra(JConstant.KEY_CONNECT_AP_GIF, R.raw.bind_guide);
                intent.putExtra(JConstant.KEY_SSID_PREFIX, "DOG-******");
                intent.putExtra(JConstant.KEY_BIND_DEVICE, getString(R.string.Cloud_Camera));
                break;
            case R.id.v_to_bind_consumer_cam://原来睿视
                intent.putExtra(JConstant.KEY_ANIM_GIF, R.raw.bind_reset_rs);
                intent.putExtra(JConstant.KEY_CONNECT_AP_GIF, R.raw.bind_guide_rs);
                intent.putExtra(JConstant.KEY_SSID_PREFIX, "DOG-******");
                intent.putExtra(JConstant.KEY_BIND_DEVICE, getString(R.string.Consumer_Camera));
                break;
            case R.id.v_to_bind_bell_battery:
                intent.putExtra(JConstant.KEY_ANIM_GIF, R.raw.door_android);
                intent.putExtra(JConstant.KEY_CONNECT_AP_GIF, R.raw.bind_guide);
                intent.putExtra(JConstant.KEY_SSID_PREFIX, "BELL-******");
                intent.putExtra(JConstant.KEY_BIND_DEVICE, getString(R.string.Smart_bell_Battery));
                break;
            case R.id.v_to_bind_bell_no_battery:
                intent.putExtra(JConstant.KEY_ANIM_GIF, R.raw.door_android);
                intent.putExtra(JConstant.KEY_CONNECT_AP_GIF, R.raw.bind_guide);
                intent.putExtra(JConstant.KEY_SSID_PREFIX, "BELL-******");
                intent.putExtra(JConstant.KEY_BIND_DEVICE, getString(R.string.Smart_bell_Power));
                break;
            case R.id.v_to_bind_cat_eye_cam:
                intent.putExtra(JConstant.KEY_ANIM_GIF, R.raw.eyes_android);
                intent.putExtra(JConstant.KEY_CONNECT_AP_GIF, R.raw.bind_guide);
                intent.putExtra(JConstant.KEY_SSID_PREFIX, "DOG-******");
                intent.putExtra(JConstant.KEY_BIND_DEVICE, getString(R.string.Smart_Door_Viewer));
                break;
        }
        startActivity(intent);
    }
}
