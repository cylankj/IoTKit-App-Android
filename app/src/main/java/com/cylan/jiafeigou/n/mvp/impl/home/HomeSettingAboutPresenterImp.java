package com.cylan.jiafeigou.n.mvp.impl.home;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.cylan.jiafeigou.n.mvp.contract.home.HomeSettingAboutContract;
import com.cylan.jiafeigou.utils.ToastUtil;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public class HomeSettingAboutPresenterImp implements HomeSettingAboutContract.Presenter, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;  //
    HomeSettingAboutContract.View view;

    public HomeSettingAboutPresenterImp(HomeSettingAboutContract.View view) {
        this.view = view;
    }

    @Override
    public void callHotPhone(String phone) {
        //用intent启动拨打电话
        if (ActivityCompat.checkSelfPermission(view.getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) view.getContext(),
                    new String[]{Manifest.permission.CALL_PHONE},
                    MY_PERMISSIONS_REQUEST_CALL_PHONE);
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
            view.getContext().startActivity(intent);
            return;
        } else {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
            view.getContext().startActivity(intent);
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CALL_PHONE:
                //如果请求被取消，那么 result 数组将为空
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 已经获取对应权限
                } else {
                    // 未获取到授权，取消需要该权限的方法
                    ToastUtil.showToast(view.getContext(), "权限未授予");
                }

                break;
        }

    }
}
