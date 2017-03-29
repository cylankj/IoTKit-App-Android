package com.cylan.jiafeigou.n.view.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.view.bind.BindGuideFragment;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.nineoldandroids.animation.Animator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

@RuntimePermissions
public class BindCamActivity extends BaseFullScreenFragmentActivity {

    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.imgV_camera_wifi_light_flash)
    ImageView imgVCameraWifiLightFlash;
    @BindView(R.id.imgV_camera_hand)
    ImageView imgVCameraHand;
    @BindView(R.id.imgV_camera_red_dot)
    ImageView imgVCameraRedDot;
    private Animator animator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_cam);
        ButterKnife.bind(this);
        ViewUtils.setViewMarginStatusBar(customToolbar);
        customToolbar.setBackAction(v -> {
            onBackPressed();
        });
        initAnimation();
        JConstant.ConfigApStep = 0;
    }

    private boolean shouldNotifyBackword() {
        if (JConstant.ConfigApStep >= 2) {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.Tap1_AddDevice_tips))
                    .setNegativeButton(getString(R.string.CANCEL), null)
                    .setPositiveButton(getString(R.string.OK), (DialogInterface dialog, int which) -> {
                        popAllFragmentStack();
                        JConstant.ConfigApStep = 0;
                    }).show();
            return true;
        }
        return false;
    }

    protected int[] getOverridePendingTransition() {
        return new int[]{R.anim.slide_in_right, R.anim.slide_out_left};
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JConstant.ConfigApStep = 0;
    }

    @Override
    public void onBackPressed() {
        if (shouldNotifyBackword()) {
            return;
        }
        if (checkExtraChildFragment()) {
            return;
        } else if (checkExtraFragment())
            return;
        if (getIntent() != null && getIntent().hasExtra("fromBindActivity")) {
            Intent intent = new Intent(BindCamActivity.this, BindDeviceActivity.class);
            intent.putExtra("fromBindActivity", "fromBindActivity");
            startActivity(intent);
        }
        finishExt();
    }

    private void initAnimation() {
        animator = AnimatorUtils.onHandMoveAndFlash(imgVCameraHand, imgVCameraRedDot, imgVCameraWifiLightFlash);
        animator.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (animator != null && animator.isRunning())
            animator.cancel();
    }

    @OnClick(R.id.tv_bind_camera_tip)
    public void onClick(View view) {
        ViewUtils.deBounceClick(view);
        Bundle bundle = new Bundle();
        bundle.putString(JConstant.KEY_BIND_DEVICE, getString(R.string.DOG_CAMERA_NAME));
        BindGuideFragment fragment = BindGuideFragment.newInstance(bundle);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_right_in,
                        R.anim.slide_out_left,
                        R.anim.slide_out_right,
                        R.anim.slide_out_right)
                .add(android.R.id.content, fragment, "BindGuideFragment")
                .addToBackStack("null")
                .commit();
        cancelAnimation();
    }

    private void cancelAnimation() {
        if (animator != null && animator.isRunning())
            animator.cancel();
    }

    @Override
    public void onStart() {
        super.onStart();
        BindCamActivityPermissionsDispatcher.onGrantedLocationPermissionWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        BindCamActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        if (permissions.length == 1) {
            if (TextUtils.equals(permissions[0], ACCESS_FINE_LOCATION) && grantResults[0] > -1) {
                BindCamActivityPermissionsDispatcher.onGrantedLocationPermissionWithCheck(this);
            }
        }
    }


    @NeedsPermission(ACCESS_FINE_LOCATION)
    public void onGrantedLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (!MiscUtils.checkGpsAvailable(getApplicationContext())) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.GetWifiList_FaiTips))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.OK), (@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) -> {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        })
                        .setNegativeButton(getString(R.string.CANCEL), (final DialogInterface dialog, @SuppressWarnings("unused") final int id) -> {
                            dialog.cancel();
                            finishExt();
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            }
    }

    @OnPermissionDenied(ACCESS_FINE_LOCATION)
    public void onDeniedLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            new AlertDialog.Builder(this)
                    .setMessage(String.format(getString(R.string.turn_on_gps), ""))
                    .setNegativeButton(getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                        finishExt();
                    })
                    .setPositiveButton(getString(R.string.OK), (DialogInterface dialog, int which) -> {
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                    })
                    .create()
                    .show();
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    public void showRationaleForLocation(PermissionRequest request) {
        onDeniedLocationPermission();
    }


}
