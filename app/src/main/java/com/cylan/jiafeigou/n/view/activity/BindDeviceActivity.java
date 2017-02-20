package com.cylan.jiafeigou.n.view.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.view.bind.BindCameraFragment;
import com.cylan.jiafeigou.n.view.bind.BindDoorBellFragment;
import com.cylan.jiafeigou.n.view.bind.BindScanFragment;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.KEY_AUTO_SHOW_BIND;

public class BindDeviceActivity extends BaseFullScreenFragmentActivity implements BaseDialog.BaseDialogAction {

    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_device);
        ButterKnife.bind(this);
        initTopBar();
        checkShouldJump();
    }

    private void checkShouldJump() {
        Intent intent = getIntent();
        if (intent != null && !TextUtils.isEmpty(intent.getStringExtra(KEY_AUTO_SHOW_BIND))) {
            jump2Cam();
        }
    }

    private void initTopBar() {
        customToolbar.setBackAction((View v) -> {
            onBackPressed();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (shouldNotifyBackForeword())
            return;
        if (checkFinish()) {
            finishExt();
        }
        if (popAllFragmentStack())
            return;
        finishExt();
    }

    private boolean checkFinish() {
        Intent intent = getIntent();
        if (intent != null && !TextUtils.isEmpty(intent.getStringExtra(KEY_AUTO_SHOW_BIND))) {
            return true;
        }
        return false;
    }

    private boolean shouldNotifyBackForeword() {
        if (JConstant.ConfigApStep == 2) {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.Tap1_AddDevice_tips))
                    .setNegativeButton(getString(R.string.CANCEL), null)
                    .setPositiveButton(getString(R.string.OK), (DialogInterface dialog, int which) -> {
                        popAllFragmentStack();
                    }).show();
            return true;
        }
        return false;
    }

    @Override
    public void onDialogAction(int id, Object value) {
        if (id == R.id.tv_dialog_btn_right)
            return;
        popAllFragmentStack();
    }

    @OnClick({R.id.v_to_scan_qrcode, R.id.v_to_bind_camera, R.id.v_to_bind_doorbell})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.v_to_scan_qrcode: {
                ViewUtils.deBounceClick(findViewById(R.id.v_to_scan_qrcode));
                BindScanFragment fragment = BindScanFragment.newInstance(null);
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(0, R.anim.slide_down_out
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(android.R.id.content, fragment)
                        .addToBackStack("BindScanFragment")
                        .commit();
                break;
            }
            case R.id.v_to_bind_camera: {
                jump2Cam();
                break;
            }
            case R.id.v_to_bind_doorbell: {
                ViewUtils.deBounceClick(findViewById(R.id.v_to_bind_doorbell));
                Bundle bundle = new Bundle();
                BindDoorBellFragment fragment = BindDoorBellFragment.newInstance(bundle);
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_up_in, R.anim.slide_down_out
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content, fragment)
                        .addToBackStack("BindDoorBellFragment")
                        .commit();
                break;
            }
        }
    }

    private void jump2Cam() {
        ViewUtils.deBounceClick(findViewById(R.id.v_to_bind_camera));
        Bundle bundle = new Bundle();
        BindCameraFragment fragment = BindCameraFragment.newInstance(bundle);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, fragment)
                .addToBackStack("BindCameraFragment")
                .commit();
    }
    
}
