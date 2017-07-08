package com.cylan.jiafeigou.n.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.view.bind.BindScanFragment;
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
            R.id.v_to_bind_consumer_cam, R.id.v_to_bind_camera, R.id.v_to_bind_doorbell, R.id.v_to_bind_panorama_camera})
    public void onClick(View view) {
        ViewUtils.deBounceClick(view);
        switch (view.getId()) {
            case R.id.v_to_scan_qrcode: {
                BindScanFragment fragment = BindScanFragment.newInstance(null);
                ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(),
                        fragment, android.R.id.content);
                break;
            }
            case R.id.v_to_bind_camera: {
                ViewUtils.deBounceClick(view);
                Intent intent = new Intent(this, BindCamActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.v_to_bind_consumer_cam: {
                ViewUtils.deBounceClick(view);
                Intent intent = new Intent(this, BindRsCamActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.v_to_bind_panorama_camera: {
                jump2PanoramaCam(true);
                break;
            }
            case R.id.v_to_bind_doorbell: {
                Intent intent = new Intent(this, BindBellActivity.class);
                startActivity(intent);
                break;
            }
        }
    }

    private void jump2PanoramaCam(boolean animation) {
        startActivity(new Intent(this, BindPanoramaCamActivity.class));
        ViewUtils.deBounceClick(findViewById(R.id.v_to_bind_panorama_camera));
//        Bundle bundle = new Bundle();
//        BindPanoramaCamera fragment = BindPanoramaCamera.newInstance(bundle);
//        if (animation) {
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
//                            , R.anim.slide_in_left, R.anim.slide_out_right)
//                    .add(android.R.id.content, fragment)
//                    .addToBackStack("BindPanoramaCameraFragment")
//                    .commit();
//        } else {
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .add(android.R.id.content, fragment)
//                    .commit();
//        }
    }

    @OnClick({R.id.v_to_bind_camera_cloud, R.id.v_to_bind_consumer_cam, R.id.v_to_bind_bell_battery, R.id.v_to_bind_bell_no_battery, R.id.v_to_bind_cat_eye_cam})
    public void onClickBind(View view) {
        switch (view.getId()) {
            case R.id.v_to_bind_camera_cloud:
                break;
            case R.id.v_to_bind_consumer_cam:
                break;
            case R.id.v_to_bind_bell_battery:
                break;
            case R.id.v_to_bind_bell_no_battery:
                break;
            case R.id.v_to_bind_cat_eye_cam:
                break;
        }
    }
}
