package com.cylan.jiafeigou.n.view.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.view.bind.BindCameraFragment;
import com.cylan.jiafeigou.n.view.bind.BindDoorBellFragment;
import com.cylan.jiafeigou.n.view.bind.BindScanFragment;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.KEY_AUTO_SHOW_BIND;

public class BindDeviceActivity extends BaseFullScreenFragmentActivity implements BaseDialog.BaseDialogAction {

    private static final int REQ_ACCESS_FINE_LOCATION = 666;
    @BindView(R.id.imgV_top_bar_left)
    ImageView imgVTopBarLeft;
    @BindView(R.id.imgV_top_bar_center)
    TextView tvTopBarCenter;
    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout fLayoutTopBarContainer;

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
        ViewUtils.setViewPaddingStatusBar(fLayoutTopBarContainer);
    }

    @OnClick(R.id.imgV_top_bar_left)
    public void onClose() {
        onBackPressed();
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
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    jump2BellBind();//已经获得了授权
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    //需要重新提示用户授权
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_ACCESS_FINE_LOCATION);
                } else {
                    ToastUtil.showNegativeToast("绑定设备需要位置权限以获取 WiFi 列表,请在设置中手动开启");
                }
                break;
            }
//            case R.id.v_to_bind_cloud_album:
//                ViewUtils.deBounceClick(findViewById(R.id.v_to_bind_cloud_album));
//                Bundle bundle = new Bundle();
//                BindScanFragment fragment = BindScanFragment.newInstance(bundle);
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .setCustomAnimations(0, R.anim.slide_down_out
//                                , R.anim.slide_in_left, R.anim.slide_out_right)
//                        .replace(android.R.id.content, fragment)
//                        .addToBackStack("BindScanFragment")
//                        .commit();
//                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                jump2BellBind();
            } else {
                Toast.makeText(this, "获取位置权限失败,请手动开启", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void jump2BellBind() {
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
