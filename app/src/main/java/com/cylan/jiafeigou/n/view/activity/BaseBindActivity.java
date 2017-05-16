package com.cylan.jiafeigou.n.view.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.utils.MiscUtils;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Created by hds on 17-3-31.
 */
@RuntimePermissions
public class BaseBindActivity<T extends BasePresenter> extends BaseFullScreenFragmentActivity<T> {


    @Override
    public void onStart() {
        super.onStart();
        BaseBindActivityPermissionsDispatcher.onGrantedLocationPermissionWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        BaseBindActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        if (permissions.length == 1) {
            if (TextUtils.equals(permissions[0], ACCESS_FINE_LOCATION) && grantResults[0] > -1) {
                BaseBindActivityPermissionsDispatcher.onGrantedLocationPermissionWithCheck(this);
            }
        }
    }


    @NeedsPermission(ACCESS_FINE_LOCATION)
    public void onGrantedLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (!MiscUtils.checkGpsAvailable(getApplicationContext())) {
                AlertDialogManager.getInstance().showDialog(this, getString(R.string.GetWifiList_FaiTips), getString(R.string.GetWifiList_FaiTips),
                        getString(R.string.OK), (@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) -> {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }, getString(R.string.CANCEL), (final DialogInterface dialog, @SuppressWarnings("unused") final int id) -> {
                            dialog.cancel();
                            finishExt();
                        }, false);
            }
    }

    @OnPermissionDenied(ACCESS_FINE_LOCATION)
    public void onDeniedLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AlertDialogManager.getInstance().showDialog(this, getString(R.string.turn_on_gps),
                    getString(R.string.turn_on_gps),
                    getString(R.string.OK), (DialogInterface dialog, int which) -> {
//                            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, 33);
                    }, getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                        finishExt();
                    }, false);
        }
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    public void showRationaleForLocation(PermissionRequest request) {
        onDeniedLocationPermission();
    }

    @Override
    public void onBackPressed() {
        finishExt();
    }

}
