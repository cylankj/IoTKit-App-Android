package com.cylan.jiafeigou.n.view.splash;

import android.support.v4.app.ActivityCompat;

import com.cylan.jiafeigou.SmartcallActivity;
import com.cylan.jiafeigou.utils.UiHelper;

import java.lang.ref.WeakReference;

import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.PermissionUtils;

import static android.support.v4.app.ActivityCompat.requestPermissions;

/**
 * Created by chen on 5/27/16.
 */


final class SplashPermissionDispatcher {
    /**
     * 所有提示语句暂用camera的
     */


    private SplashPermissionDispatcher() {
    }

    public static void showCameraWithCheck(SmartcallActivity target, String[] permisions, int requestCode) {
        if (PermissionUtils.hasSelfPermissions(target, permisions)) {
            //    target.showCamera();
        } else {
            if (PermissionUtils.shouldShowRequestPermissionRationale(target, permisions)) {
                target.showRationaleForCamera(new ShowCameraPermissionRequest(target, permisions, requestCode));
            } else {
                ActivityCompat.requestPermissions(target, permisions, requestCode);
            }
        }
    }

    public static void showWriteSdCardWithCheck(SmartcallActivity target) {
        if (PermissionUtils.hasSelfPermissions(target, UiHelper.PERMISSION_SHOWMULTIS)) {
            //    target.showCamera();
            target.showWriteSdCard();
        } else {
            if (PermissionUtils.shouldShowRequestPermissionRationale(target, UiHelper.PERMISSION_SHOWMULTIS)) {
                target.showRationaleForCamera(new ShowCameraPermissionRequest(target, UiHelper.PERMISSION_SHOWMULTIS, UiHelper.REQUEST_SHOWMULTIS));
            } else {
                ActivityCompat.requestPermissions(target, UiHelper.PERMISSION_SHOWMULTIS, UiHelper.REQUEST_SHOWMULTIS);
            }
        }
    }

    /**
     * 一次可以检测某一个或者检测所有需要的权限
     */
    static void onRequestPermissionsResult(SmartcallActivity target, String[] permissions, int requestCode, int[] grantResults) {
        switch (requestCode) {
            //一次检测照相机权限
            case UiHelper.REQUEST_SHOWCAMERA:
                if (PermissionUtils.getTargetSdkVersion(target) < 23 && !PermissionUtils.hasSelfPermissions(target, UiHelper.PERMISSION_SHOWCAMERA)) {
                    target.onCameraDenied();
                    return;
                }
                if (PermissionUtils.verifyPermissions(grantResults)) {
                    //     target.showCamera();
                } else {
                    if (!PermissionUtils.shouldShowRequestPermissionRationale(target, UiHelper.PERMISSION_SHOWCAMERA)) {
                        target.onCameraNeverAskAgain();
                    } else {
                        target.onCameraDenied();
                    }
                }
                break;
            //一次检测所有需要的权限
            case UiHelper.REQUEST_SHOWMULTIS:
                if (PermissionUtils.getTargetSdkVersion(target) < 23 && !PermissionUtils.hasSelfPermissions(target, UiHelper.PERMISSION_SHOWCONTACTS)) {
                    //sdk < 23 的手机
                    return;
                }
                if (PermissionUtils.verifyPermissions(grantResults)) {
                    //    you have all permision
                    target.showWriteSdCard();
                    return;
                }
                //没有某些权限不影响运行
                if (!PermissionUtils.hasSelfPermissions(target, UiHelper.PERMISSION_SHOWCONTACTS)) {
                    //没有通讯录权限
                }

                if (!PermissionUtils.hasSelfPermissions(target, UiHelper.PERMISSION_SHOWCAMERA)) {
                    //没有相机,录像机权限
                }

                //没有某些权限,影响app运行,需要做处理
                if (!PermissionUtils.hasSelfPermissions(target, UiHelper.PERMISSION_SHOWWRITE_STORAGE)) {
                    //没有读写权限
                    target.onWriteSdCardDenied();
                } else {
                    target.showWriteSdCard();
                }
                break;
            default:
                break;
        }
    }

    private static class ShowCameraPermissionRequest implements PermissionRequest {
        private final WeakReference<SmartcallActivity> weakTarget;
        private String[] permissionShow;
        private int requestCode;

        private ShowCameraPermissionRequest(SmartcallActivity target, String[] permision, int request) {
            this.weakTarget = new WeakReference<>(target);
            this.permissionShow = permision;
            this.requestCode = request;
        }

        @Override
        public void proceed() {
            SmartcallActivity target = weakTarget.get();
            if (target == null) return;
            requestPermissions(target, permissionShow, requestCode);
        }

        @Override
        public void cancel() {
            SmartcallActivity target = weakTarget.get();
            if (target == null) return;
            target.onCameraDenied();
        }

    }
}
