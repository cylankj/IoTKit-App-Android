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
            R.id.v_to_bind_rs_cam, R.id.v_to_bind_camera, R.id.v_to_bind_doorbell, R.id.v_to_bind_panorama_camera})
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
            case R.id.v_to_bind_rs_cam: {
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

}
